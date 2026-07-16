import os
import json
import uuid
import logging
from datetime import datetime, timedelta
from typing import List, Dict, Any, Optional
from fastapi import APIRouter, Depends, HTTPException, status
from pydantic import BaseModel

from app.core.dependencies import get_current_user, role_required, get_db_session
from app.db.models.user import User, UserRole
from app.core.json_db_helper import load_db_from_postgres, save_db_to_postgres

logger = logging.getLogger("app.academic_calendar")
router = APIRouter()

DB_FILE = os.path.join(os.path.dirname(os.path.dirname(os.path.dirname(os.path.dirname(__file__)))), "academic_calendar_db.json")

def load_initial_db_from_file() -> Dict[str, Any]:
    """
    Load the initial database from the disk file academic_calendar_db.json if it exists,
    otherwise fallback to a default structure.
    """
    if os.path.exists(DB_FILE):
        try:
            with open(DB_FILE, "r") as f:
                data = json.load(f)
                if "setup" not in data:
                    data["setup"] = {
                        "academicYear": "2026-2027",
                        "institutionName": "National Law University",
                        "startDate": "2026-07-01",
                        "endDate": "2027-06-30",
                        "oddSemStart": "2026-07-01",
                        "oddSemEnd": "2026-12-31",
                        "evenSemStart": "2027-01-01",
                        "evenSemEnd": "2027-06-30"
                    }
                if "events" not in data:
                    data["events"] = {}
                return data
        except Exception as e:
            logger.error(f"Error loading initial db from file: {e}")
            
    return {
        "setup": {
            "academicYear": "2026-2027",
            "institutionName": "National Law University",
            "startDate": "2026-07-01",
            "endDate": "2027-06-30",
            "oddSemStart": "2026-07-01",
            "oddSemEnd": "2026-12-31",
            "evenSemStart": "2027-01-01",
            "evenSemEnd": "2027-06-30"
        },
        "events": {}
    }

def load_db() -> Dict[str, Any]:
    return load_db_from_postgres(DB_FILE, load_initial_db_from_file)

def save_db(db: Dict[str, Any]) -> None:
    save_db_to_postgres(DB_FILE, db)

# Pydantic Schemas
class CalendarEventRequest(BaseModel):
    title: str
    category: str
    start_date: str
    end_date: str
    description: str
    academic_year: str
    department: str
    batch: str
    location: str
    is_holiday: bool
    dept_id: Optional[str] = None
    audience: Optional[Dict[str, bool]] = None

class CalendarEventResponse(BaseModel):
    id: str
    title: str
    category: str
    start_date: str
    end_date: str
    description: str
    academic_year: str
    department: str
    batch: str
    location: str
    is_holiday: bool
    created_at: str
    updated_at: str
    created_by: str
    dept_id: Optional[str] = None
    audience: Optional[Dict[str, bool]] = None

class SetupConfig(BaseModel):
    academicYear: str
    institutionName: str
    startDate: str
    endDate: str
    oddSemStart: str
    oddSemEnd: str
    evenSemStart: str
    evenSemEnd: str

# SETUP
@router.get("/setup", response_model=SetupConfig, summary="Get calendar setup config")
async def get_setup(current_user: User = Depends(get_current_user)):
    db = load_db()
    return db.get("setup", {})

@router.post("/setup", summary="Update calendar setup config")
async def update_setup(
    payload: SetupConfig,
    current_user: User = Depends(role_required([UserRole.PRINCIPAL, UserRole.SUPER_ADMIN, UserRole.ADMIN]))
):
    db = load_db()
    db["setup"] = payload.dict()
    save_db(db)
    return db["setup"]

# DIRECT EVENTS MANAGEMENT (For Principal & UI compatibility)
@router.get("/events/conflicts", summary="Get all event dates and their departments for conflict warning")
async def get_event_conflicts(current_user: User = Depends(get_current_user)):
    db = load_db()
    events = list(db.get("events", {}).values())
    
    conflicts = {}
    for ev in events:
        dept = ev.get("department") or "Institution-Wide"
        dept_id = ev.get("dept_id")
        try:
            start_dt = datetime.strptime(ev["start_date"], "%Y-%m-%d")
            end_dt = datetime.strptime(ev["end_date"], "%Y-%m-%d")
            curr_dt = start_dt
            while curr_dt <= end_dt:
                date_str = curr_dt.strftime("%Y-%m-%d")
                if date_str not in conflicts:
                    conflicts[date_str] = []
                conflicts[date_str].append({
                    "event_id": ev["id"],
                    "title": ev["title"],
                    "department": dept,
                    "dept_id": dept_id
                })
                curr_dt += timedelta(days=1)
        except Exception:
            pass
    return conflicts

@router.get("/events", response_model=List[CalendarEventResponse], summary="Retrieve all calendar events")
async def get_calendar_events(
    current_user: User = Depends(get_current_user)
):
    db = load_db()
    result = list(db.get("events", {}).values())
    
    filtered_events = []
    for ev in result:
        event_dept_id = ev.get("dept_id")
        if current_user.role in [UserRole.PRINCIPAL, UserRole.SUPER_ADMIN, UserRole.ADMIN]:
            filtered_events.append(ev)
        elif current_user.role in [UserRole.HOD, UserRole.FACULTY, UserRole.STUDENT]:
            if not event_dept_id or event_dept_id == current_user.department_id:
                filtered_events.append(ev)
                
    for ev in filtered_events:
        if "department" not in ev:
            ev["department"] = ""
        if "batch" not in ev:
            ev["batch"] = ""
        if "dept_id" not in ev:
            ev["dept_id"] = None
        if "audience" not in ev:
            ev["audience"] = {
                "students": True,
                "faculty": True,
                "admin": True,
                "principal": True
            }
    return sorted(filtered_events, key=lambda x: x.get("start_date", ""))

@router.post("/events", response_model=CalendarEventResponse, summary="Create a new calendar event")
async def create_calendar_event(
    payload: CalendarEventRequest,
    current_user: User = Depends(role_required([UserRole.PRINCIPAL, UserRole.SUPER_ADMIN, UserRole.ADMIN, UserRole.HOD]))
):
    db = load_db()
    event_id = f"CAL-{uuid.uuid4().hex[:4].upper()}"
    
    dept_id = payload.dept_id
    if current_user.role == UserRole.HOD:
        dept_id = current_user.department_id
        if not dept_id:
            raise HTTPException(status_code=400, detail="HOD must belong to a department to create calendar events")
            
    new_event = {
        "id": event_id,
        "title": payload.title,
        "category": payload.category,
        "start_date": payload.start_date,
        "end_date": payload.end_date,
        "description": payload.description,
        "academic_year": payload.academic_year,
        "department": payload.department,
        "batch": payload.batch,
        "location": payload.location,
        "is_holiday": payload.is_holiday,
        "created_at": datetime.now().isoformat(),
        "updated_at": datetime.now().isoformat(),
        "created_by": current_user.full_name,
        "dept_id": dept_id,
        "audience": payload.audience or {
            "students": True,
            "faculty": True,
            "admin": True,
            "principal": True
        }
    }
    
    if "events" not in db:
        db["events"] = {}
        
    db["events"][event_id] = new_event
    save_db(db)
    return new_event

@router.put("/events/{event_id}", response_model=CalendarEventResponse, summary="Update an existing calendar event")
async def update_calendar_event(
    event_id: str,
    payload: CalendarEventRequest,
    current_user: User = Depends(role_required([UserRole.PRINCIPAL, UserRole.SUPER_ADMIN, UserRole.ADMIN, UserRole.HOD]))
):
    db = load_db()
    if "events" not in db or event_id not in db["events"]:
        raise HTTPException(status_code=404, detail="Calendar event not found")
        
    event = db["events"][event_id]
    
    if current_user.role == UserRole.HOD:
        if event.get("dept_id") != current_user.department_id:
            raise HTTPException(status_code=403, detail="HOD can only update their own department's events")
            
    dept_id = event.get("dept_id")
    if current_user.role != UserRole.HOD:
        dept_id = payload.dept_id
    else:
        dept_id = current_user.department_id
        
    event["title"] = payload.title
    event["category"] = payload.category
    event["start_date"] = payload.start_date
    event["end_date"] = payload.end_date
    event["description"] = payload.description
    event["academic_year"] = payload.academic_year
    event["department"] = payload.department
    event["batch"] = payload.batch
    event["location"] = payload.location
    event["is_holiday"] = payload.is_holiday
    event["dept_id"] = dept_id
    if payload.audience is not None:
        event["audience"] = payload.audience
    event["updated_at"] = datetime.now().isoformat()
    
    save_db(db)
    return event

@router.delete("/events/{event_id}", summary="Delete a calendar event")
async def delete_calendar_event(
    event_id: str,
    current_user: User = Depends(role_required([UserRole.PRINCIPAL, UserRole.SUPER_ADMIN, UserRole.ADMIN, UserRole.HOD]))
):
    db = load_db()
    if "events" not in db or event_id not in db["events"]:
        raise HTTPException(status_code=404, detail="Calendar event not found")
        
    event = db["events"][event_id]
    if current_user.role == UserRole.HOD:
        if event.get("dept_id") != current_user.department_id:
            raise HTTPException(status_code=403, detail="HOD can only delete their own department's events")
            
    del db["events"][event_id]
    save_db(db)
    return {"ok": True}

# DRAFT ALIASES FOR COMPATIBILITY
@router.get("/draft/events", summary="Get all draft events (alias to events)")
async def get_draft_events(current_user: User = Depends(role_required([UserRole.PRINCIPAL, UserRole.SUPER_ADMIN, UserRole.ADMIN, UserRole.HOD]))):
    return await get_calendar_events(current_user)

@router.post("/draft/events", summary="Create a draft event (alias to events)")
async def create_draft_event(
    payload: CalendarEventRequest,
    current_user: User = Depends(role_required([UserRole.PRINCIPAL, UserRole.SUPER_ADMIN, UserRole.ADMIN, UserRole.HOD]))
):
    return await create_calendar_event(payload, current_user)

@router.put("/draft/events/{event_id}", summary="Update a draft event (alias to events)")
async def update_draft_event(
    event_id: str,
    payload: CalendarEventRequest,
    current_user: User = Depends(role_required([UserRole.PRINCIPAL, UserRole.SUPER_ADMIN, UserRole.ADMIN, UserRole.HOD]))
):
    return await update_calendar_event(event_id, payload, current_user)

@router.delete("/draft/events/{event_id}", summary="Delete a draft event (alias to events)")
async def delete_draft_event(
    event_id: str,
    current_user: User = Depends(role_required([UserRole.PRINCIPAL, UserRole.SUPER_ADMIN, UserRole.ADMIN, UserRole.HOD]))
):
    return await delete_calendar_event(event_id, current_user)

@router.post("/publish", summary="Publish calendar (no-op alias)")
async def publish_calendar(current_user: User = Depends(role_required([UserRole.PRINCIPAL, UserRole.SUPER_ADMIN, UserRole.ADMIN]))):
    return {"ok": True, "version": "1.0"}

@router.get("/history", summary="Get calendar history (empty alias)")
async def get_history(current_user: User = Depends(role_required([UserRole.PRINCIPAL, UserRole.SUPER_ADMIN, UserRole.ADMIN]))):
    return []

# PORTAL SYNCHRONIZATION (For Student/Faculty/Admin/HOD to read)
@router.get("/published", summary="Get the published calendar synchronized for the current user's role")
async def get_published_calendar(
    current_user: User = Depends(get_current_user),
    db_session: Any = Depends(get_db_session)
):
    from sqlalchemy.future import select
    from app.db.models.payroll import WorkingDayConfig
    
    db = load_db()
    events = list(db.get("events", {}).values())
    
    q = await db_session.execute(select(WorkingDayConfig).where(WorkingDayConfig.is_deleted.is_(False)))
    configs = q.scalars().all()
    working_days_configs = []
    for c in configs:
        working_days_configs.append({
            "month": c.month, 
            "year": c.year, 
            "total_working_days": c.total_working_days, 
            "overrides_json": c.overrides_json
        })
    
    # Filter and format events for UI display
    # UI calendar expects date, time, venue, desc, is_holiday, and audience visibility
    filtered_events = []
    for ev in events:
        # Check audience visibility. Default to True if not specified.
        audience = ev.get("audience", {
            "students": True,
            "faculty": True,
            "admin": True,
            "principal": True
        })
        
        event_dept_id = ev.get("dept_id")
        
        is_visible = False
        if current_user.role in [UserRole.ADMIN, UserRole.PRINCIPAL, UserRole.SUPER_ADMIN]:
            is_visible = True
        else:
            if event_dept_id:
                if current_user.department_id == event_dept_id:
                    if current_user.role == UserRole.STUDENT and audience.get("students", False):
                        is_visible = True
                    elif current_user.role in [UserRole.FACULTY, UserRole.HOD] and audience.get("faculty", False):
                        is_visible = True
            else:
                if current_user.role == UserRole.STUDENT and audience.get("students", False):
                    is_visible = True
                elif current_user.role in [UserRole.FACULTY, UserRole.HOD] and audience.get("faculty", False):
                    is_visible = True
            
        if is_visible:
            # Expand multi-day events if start_date and end_date span multiple days
            try:
                start_dt = datetime.strptime(ev["start_date"], "%Y-%m-%d")
                end_dt = datetime.strptime(ev["end_date"], "%Y-%m-%d")
                
                curr_dt = start_dt
                while curr_dt <= end_dt:
                    date_str = curr_dt.strftime("%Y-%m-%d")
                    filtered_events.append({
                        "id": f"{ev['id']}-{date_str}" if start_dt != end_dt else ev["id"],
                        "title": ev["title"],
                        "category": ev["category"],
                        "date": date_str,
                        "time": "All Day",
                        "venue": ev.get("location", ""),
                        "desc": ev.get("description", ""),
                        "is_holiday": ev.get("is_holiday", False),
                        "audience": audience,
                        "department": ev.get("department", ""),
                        "dept_id": ev.get("dept_id")
                    })
                    curr_dt += timedelta(days=1)
            except Exception as e:
                # Fallback in case date parsing fails
                logger.error(f"Error parsing dates for event {ev.get('id')}: {e}")
                filtered_events.append({
                    "id": ev["id"],
                    "title": ev["title"],
                    "category": ev["category"],
                    "date": ev.get("start_date"),
                    "time": "All Day",
                    "venue": ev.get("location", ""),
                    "desc": ev.get("description", ""),
                    "is_holiday": ev.get("is_holiday", False),
                    "audience": audience,
                    "department": ev.get("department", ""),
                    "dept_id": ev.get("dept_id")
                })
            
    return {
        "setup": db.get("setup", {
            "academicYear": "2026-2027",
            "institutionName": "National Law University",
            "startDate": "2026-07-01",
            "endDate": "2027-06-30",
            "oddSemStart": "2026-07-01",
            "oddSemEnd": "2026-12-31",
            "evenSemStart": "2027-01-01",
            "evenSemEnd": "2027-06-30"
        }),
        "version": "1.0",
        "published_at": datetime.now().strftime("%Y-%m-%d"),
        "events": filtered_events,
        "working_days_configs": working_days_configs
    }
