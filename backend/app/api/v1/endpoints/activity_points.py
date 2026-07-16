import os
import json
import uuid
from datetime import datetime
from typing import List, Dict, Any, Optional
from fastapi import APIRouter, Depends, HTTPException, Request, status
from pydantic import BaseModel

from app.core.dependencies import get_current_user, get_db_session, role_required
from app.db.models.user import User, UserRole
from sqlalchemy import select
from sqlalchemy.ext.asyncio import AsyncSession

router = APIRouter()

from app.core.json_db_helper import load_db_from_postgres, save_db_to_postgres

DB_FILE = os.path.join(os.path.dirname(os.path.dirname(os.path.dirname(os.path.dirname(__file__)))), "activity_points_db.json")

def get_default_db() -> Dict[str, Any]:
    initial_db = {
        "applications": {}
    }
    preseeded = [
        {
            "id": "AP-8091",
            "student_id": "mock-student-id-123",
            "student_name": "Ananya Sharma",
            "roll_no": "21BALLB045",
            "title": "State Law Moot Court Competition",
            "category": "moot_court",
            "description": "Participated as primary speaker and secured second runner-up position in State Moot.",
            "date": "2026-02-15",
            "supporting_document": "State_Moot_Certificate.pdf",
            "claimed_points": 15.0,
            "approved_points": 15.0,
            "status": "Approved",
            "reviewed_by": "Dr. Ramesh Rao",
            "reviewed_at": "2026-02-28T14:30:00",
            "faculty_remarks": "Outstanding performance in oral rounds. Verified from certificate."
        },
        {
            "id": "AP-8092",
            "student_id": "mock-student-id-123",
            "student_name": "Ananya Sharma",
            "roll_no": "21BALLB045",
            "title": "AI & Law Research Workshop",
            "category": "workshop",
            "description": "Attended a 2-day technical workshop on the intersection of AI, copyright, and patent law.",
            "date": "2026-05-10",
            "supporting_document": "AI_Workshop_Participation.pdf",
            "claimed_points": 5.0,
            "approved_points": 0.0,
            "status": "Under Verification",
            "reviewed_by": None,
            "reviewed_at": None,
            "faculty_remarks": None
        },
        {
            "id": "AP-8093",
            "student_id": "mock-student-id-123",
            "student_name": "Ananya Sharma",
            "roll_no": "21BALLB045",
            "title": "Inter-University Sports Meet (Badminton)",
            "category": "sports",
            "description": "Represented the college in women's doubles badminton tournament.",
            "date": "2026-06-01",
            "supporting_document": "Sports_Meet_Doubles.pdf",
            "claimed_points": 10.0,
            "approved_points": 0.0,
            "status": "Pending Review",
            "reviewed_by": None,
            "reviewed_at": None,
            "faculty_remarks": None
        }
    ]
    for item in preseeded:
        initial_db["applications"][item["id"]] = item
    return initial_db

def load_db() -> Dict[str, Any]:
    return load_db_from_postgres(DB_FILE, get_default_db)

def save_db(db: Dict[str, Any]) -> None:
    save_db_to_postgres(DB_FILE, db)

# Pydantic Schemas
class ApplyActivityPointsRequest(BaseModel):
    title: str
    category: str
    description: str
    date: str
    claimed_points: float
    supporting_document: Optional[str] = None

class ReviewActivityPointsRequest(BaseModel):
    status: str # "Approved" | "Rejected" | "Under Verification" | "Pending Review"
    approved_points: float
    remarks: Optional[str] = None

@router.get("/student", summary="Get logged in student's applications")
async def get_student_applications(
    current_user: User = Depends(get_current_user)
):
    db = load_db()
    result = []
    for app in db["applications"].values():
        # Only return applications that belong to the currently logged in student
        if app["student_id"] == current_user.id:
            result.append(app)
    return sorted(result, key=lambda x: x["id"], reverse=True)

@router.post("/apply", summary="Student applies for activity points")
async def apply_activity_points(
    payload: ApplyActivityPointsRequest,
    current_user: User = Depends(get_current_user),
    db_session: AsyncSession = Depends(get_db_session)
):
    db = load_db()
    app_id = f"AP-{uuid.uuid4().hex[:4].upper()}"
    
    # Query student roll number from database
    from app.db.models.student import Student
    student_q = await db_session.execute(select(Student.roll_no).where(Student.user_id == current_user.id))
    roll_no = student_q.scalar_one_or_none() or ""
    
    new_app = {
        "id": app_id,
        "student_id": current_user.id,
        "student_name": current_user.full_name,
        "roll_no": roll_no,
        "title": payload.title,
        "category": payload.category,
        "description": payload.description,
        "date": payload.date,
        "supporting_document": payload.supporting_document or "Proof_Document.pdf",
        "claimed_points": payload.claimed_points,
        "approved_points": 0.0,
        "status": "Pending Review",
        "reviewed_by": None,
        "reviewed_at": None,
        "faculty_remarks": None
    }
    
    db["applications"][app_id] = new_app
    save_db(db)
    return new_app

@router.get("/faculty", summary="Get all applications for faculty review")
async def get_faculty_applications(
    current_user: User = Depends(role_required([UserRole.FACULTY, UserRole.HOD, UserRole.SUPER_ADMIN]))
):
    db = load_db()
    result = list(db["applications"].values())
    return sorted(result, key=lambda x: x["id"], reverse=True)

@router.post("/review/{application_id}", summary="Faculty reviews an application")
async def review_application(
    application_id: str,
    payload: ReviewActivityPointsRequest,
    current_user: User = Depends(role_required([UserRole.FACULTY, UserRole.HOD, UserRole.SUPER_ADMIN]))
):
    db = load_db()
    if application_id not in db["applications"]:
        raise HTTPException(status_code=404, detail="Application not found")
        
    app = db["applications"][application_id]
    
    app["status"] = payload.status
    app["approved_points"] = payload.approved_points if payload.status == "Approved" else 0.0
    app["faculty_remarks"] = payload.remarks
    app["reviewed_by"] = current_user.full_name
    app["reviewed_at"] = datetime.now().isoformat()
    
    save_db(db)
    return app


@router.delete("/{application_id}", summary="Delete / withdraw an application")
async def delete_application(
    application_id: str,
    current_user: User = Depends(get_current_user)
):
    db = load_db()
    if application_id not in db["applications"]:
        raise HTTPException(status_code=404, detail="Application not found")
        
    app = db["applications"][application_id]
    
    # Check authorization
    if app["student_id"] != current_user.id:
        raise HTTPException(status_code=403, detail="Not authorized to delete this application")
        
    # Check status (allowed deleting all statuses)
    
    del db["applications"][application_id]
    save_db(db)
    return {"ok": True}


