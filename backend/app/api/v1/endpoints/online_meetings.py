"""
Online Meetings — simple in-memory store.
No database required; data lives for the lifetime of the server process.
Both the staff portal (write) and the student portal (read) hit this endpoint.
"""
from typing import Any, List
from fastapi import APIRouter

import os
import json

router = APIRouter()

# In-memory store — falls back to load from file on startup
MEETINGS_FILE = os.path.join(
    os.path.dirname(os.path.dirname(os.path.dirname(os.path.dirname(__file__)))),
    "online_meetings_db.json"
)

def load_meetings() -> list:
    if not os.path.exists(MEETINGS_FILE):
        return []
    with open(MEETINGS_FILE, "r") as f:
        try:
            return json.load(f)
        except Exception:
            return []

def save_meetings_to_file(data: list):
    with open(MEETINGS_FILE, "w") as f:
        json.dump(data, f, indent=4)

_meetings_store: List[Any] = load_meetings()

RECORDINGS_FILE = os.path.join(
    os.path.dirname(os.path.dirname(os.path.dirname(os.path.dirname(__file__)))),
    "lecture_recordings_db.json"
)


def load_recordings() -> list:
    if not os.path.exists(RECORDINGS_FILE):
        return []
    with open(RECORDINGS_FILE, "r") as f:
        try:
            return json.load(f)
        except Exception:
            return []


def save_recordings_to_file(data: list):
    with open(RECORDINGS_FILE, "w") as f:
        json.dump(data, f, indent=4)


@router.get("", summary="Get all online meetings")
def get_meetings() -> List[Any]:
    return load_meetings()


@router.post("", summary="Replace the full meeting list (staff portal saves here)")
def save_meetings(meetings: List[Any]) -> dict:
    global _meetings_store
    _meetings_store = meetings
    save_meetings_to_file(meetings)
    return {"ok": True, "count": len(_meetings_store)}


@router.get("/recordings", summary="Get all lecture recordings")
def get_recordings() -> list:
    return load_recordings()


@router.post("/recordings", summary="Replace the full recordings list")
def save_recordings(recordings: List[Any]) -> dict:
    save_recordings_to_file(recordings)
    return {"ok": True, "count": len(recordings)}

