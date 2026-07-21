"""
Online Meetings — simple in-memory store.
No database required; data lives for the lifetime of the server process.
Both the staff portal (write) and the student portal (read) hit this endpoint.
"""
from typing import Any, List
from fastapi import APIRouter, Depends, Body

import os
import json

from app.core.dependencies import get_current_user, role_required
from app.db.models.user import User, UserRole

from app.core.json_db_helper import load_json_store, save_json_store

router = APIRouter()

STAFF_ROLES = [UserRole.FACULTY, UserRole.HOD, UserRole.PRINCIPAL, UserRole.ADMIN, UserRole.SUPER_ADMIN]

# In-memory store — falls back to load from file on startup
MEETINGS_FILE = os.path.join(
    os.path.dirname(os.path.dirname(os.path.dirname(os.path.dirname(__file__)))),
    "online_meetings_db.json"
)

def load_meetings() -> list:
    """Online meetings, stored in the database rather than a file.

    A file in the application directory is replaced on every container redeploy, so
    scheduled meetings disappeared on each deploy. The previous module-level cache
    (`_meetings_store`, loaded once at import) also diverged between uvicorn workers:
    a meeting created on one worker was invisible to the others.
    """
    data = load_json_store(MEETINGS_FILE, list)
    return data if isinstance(data, list) else []


def save_meetings_to_file(data: list):
    save_json_store(MEETINGS_FILE, data)


# NOTE: there was a module-level `_meetings_store = load_meetings()` cache here.
# It is removed: reads already go through load_meetings(), so it was only ever
# written, and now that the store is database-backed it would have issued a query
# at import time — before the app is configured — and diverged between workers.

RECORDINGS_FILE = os.path.join(
    os.path.dirname(os.path.dirname(os.path.dirname(os.path.dirname(__file__)))),
    "lecture_recordings_db.json"
)


def load_recordings() -> list:
    data = load_json_store(RECORDINGS_FILE, list)
    return data if isinstance(data, list) else []


def save_recordings_to_file(data: list):
    save_json_store(RECORDINGS_FILE, data)


@router.get("", summary="Get all online meetings")
def get_meetings(current_user: User = Depends(get_current_user)) -> List[Any]:
    return load_meetings()


@router.post("", summary="Replace the full meeting list (admin bulk-import only)")
def save_meetings(meetings: List[Any], current_user: User = Depends(role_required([UserRole.ADMIN, UserRole.SUPER_ADMIN]))) -> dict:
    save_meetings_to_file(meetings)
    return {"ok": True, "count": len(meetings)}


@router.post("/create", summary="Schedule a single online meeting (faculty-scoped)")
def create_meeting(
    meeting: dict = Body(...),
    current_user: User = Depends(role_required(STAFF_ROLES))
) -> dict:
    import re
    import uuid
    from datetime import datetime
    from fastapi import HTTPException

    title = (meeting.get("title") or "").strip()
    if not title:
        raise HTTPException(status_code=400, detail="Title is required")
    link = meeting.get("meetingLink") or meeting.get("meeting_link") or ""
    if not link or not re.match(r"^https?://", link):
        raise HTTPException(status_code=400, detail="A valid http(s) meeting link is required")
    date = meeting.get("date") or ""
    time = meeting.get("time") or ""
    if not date or not time:
        raise HTTPException(status_code=400, detail="Date and time are required")

    meetings = load_meetings()
    new_meeting = dict(meeting)
    new_meeting["id"] = new_meeting.get("id") or f"MTG-{uuid.uuid4().hex[:10]}"
    new_meeting["organizer_id"] = current_user.id
    new_meeting["organizer"] = new_meeting.get("organizer") or current_user.full_name
    new_meeting["status"] = new_meeting.get("status", "Scheduled")
    new_meeting.setdefault("participants", 0)
    new_meeting.setdefault("attended", 0)
    new_meeting.setdefault("recordingAvailable", False)
    new_meeting["created_at"] = datetime.utcnow().isoformat()
    meetings.append(new_meeting)
    save_meetings_to_file(meetings)
    return {"ok": True, "meeting": new_meeting}


@router.delete("/{meeting_id}", summary="Delete an online meeting (organizer only)")
def delete_meeting(meeting_id: str, current_user: User = Depends(role_required(STAFF_ROLES))) -> dict:
    from fastapi import HTTPException
    meetings = load_meetings()
    target = next((m for m in meetings if m.get("id") == meeting_id), None)
    if not target:
        raise HTTPException(status_code=404, detail="Meeting not found")
    if target.get("organizer_id") != current_user.id and current_user.role not in (UserRole.HOD, UserRole.PRINCIPAL, UserRole.ADMIN, UserRole.SUPER_ADMIN):
        raise HTTPException(status_code=403, detail="You can only delete meetings you scheduled")
    meetings = [m for m in meetings if m.get("id") != meeting_id]
    save_meetings_to_file(meetings)
    return {"ok": True}


@router.get("/recordings", summary="Get all lecture recordings")
def get_recordings(current_user: User = Depends(get_current_user)) -> list:
    recordings = load_recordings()
    if current_user.role == UserRole.FACULTY:
        return [r for r in recordings if r.get("faculty_id") == current_user.id]
    return recordings


@router.post("/recordings", summary="Replace the full recordings list (admin bulk-import only)")
def save_recordings(recordings: List[Any], current_user: User = Depends(role_required([UserRole.ADMIN, UserRole.SUPER_ADMIN]))) -> dict:
    save_recordings_to_file(recordings)
    return {"ok": True, "count": len(recordings)}


@router.post("/recordings/create", summary="Add a single lecture recording (faculty-scoped)")
def create_recording(
    recording: dict = Body(...),
    current_user: User = Depends(role_required(STAFF_ROLES))
) -> dict:
    import re
    import uuid

    link = recording.get("driveLink") or recording.get("drive_link") or ""
    if not link or not re.match(r"^https?://", link):
        from fastapi import HTTPException
        raise HTTPException(status_code=400, detail="A valid http(s) drive link is required")

    recordings = load_recordings()
    new_recording = dict(recording)
    new_recording["id"] = new_recording.get("id") or f"REC-{uuid.uuid4().hex[:10]}"
    new_recording["faculty_id"] = current_user.id
    new_recording["faculty_name"] = current_user.full_name
    new_recording["status"] = new_recording.get("status", "Published")
    recordings.append(new_recording)
    save_recordings_to_file(recordings)
    return {"ok": True, "recording": new_recording}


@router.delete("/recordings/{recording_id}", summary="Delete a lecture recording (owner only)")
def delete_recording(recording_id: str, current_user: User = Depends(role_required(STAFF_ROLES))) -> dict:
    from fastapi import HTTPException
    recordings = load_recordings()
    target = next((r for r in recordings if r.get("id") == recording_id), None)
    if not target:
        raise HTTPException(status_code=404, detail="Recording not found")
    if target.get("faculty_id") != current_user.id and current_user.role not in (UserRole.HOD, UserRole.PRINCIPAL, UserRole.ADMIN, UserRole.SUPER_ADMIN):
        raise HTTPException(status_code=403, detail="You can only delete your own recordings")
    recordings = [r for r in recordings if r.get("id") != recording_id]
    save_recordings_to_file(recordings)
    return {"ok": True}

