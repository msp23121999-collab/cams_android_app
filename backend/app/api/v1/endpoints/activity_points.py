import os
import json
import uuid
from datetime import datetime
from typing import List, Dict, Any, Optional
from fastapi import APIRouter, Depends, HTTPException, Request, status, UploadFile, File
from pydantic import BaseModel

from app.core.dependencies import get_current_user, get_db_session, role_required
from app.db.models.user import User, UserRole
from app.db.models.activity_point_category import ActivityPointCategory
from sqlalchemy import select, func
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

class ActivityPointResponse(BaseModel):
    id: str
    student_id: str
    student_name: str
    roll_no: str
    title: str
    category: str
    description: str
    date: str
    supporting_document: Optional[str] = None
    claimed_points: float
    approved_points: float
    status: str
    reviewed_by: Optional[str] = None
    reviewed_at: Optional[str] = None
    faculty_remarks: Optional[str] = None

class UploadDocumentResponse(BaseModel):
    status: str
    file_url: str

class ActivityPointCategoryResponse(BaseModel):
    id: str
    code: str
    name: str
    max_points: float
    description: str | None = None

    class Config:
        from_attributes = True

class ActivityPointCategoryRequest(BaseModel):
    code: str
    name: str
    max_points: float
    description: str | None = None

@router.post("/upload-document", response_model=UploadDocumentResponse, summary="Upload a supporting document for an activity point claim")
async def upload_support_document(
    file: UploadFile = File(...),
    current_user: User = Depends(role_required([UserRole.STUDENT]))
):
    static_dir = os.path.join(os.path.dirname(os.path.dirname(os.path.dirname(os.path.dirname(os.path.abspath(__file__))))), "static")
    docs_dir = os.path.join(static_dir, "uploads", "documents")
    os.makedirs(docs_dir, exist_ok=True)

    ext = os.path.splitext(file.filename)[1] if file.filename else ".pdf"
    if ext.lower() not in [".pdf", ".jpg", ".jpeg", ".png"]:
        raise HTTPException(status_code=400, detail="Only PDF, JPG, and PNG are allowed.")

    safe_filename = f"activity_point_{current_user.id}_{uuid.uuid4().hex}{ext}"
    file_path = os.path.join(docs_dir, safe_filename)

    with open(file_path, "wb") as f:
        f.write(await file.read())

    file_url = f"/api/v1/files/documents/{safe_filename}"
    return {"status": "success", "file_url": file_url}


@router.get("/student", response_model=List[ActivityPointResponse], summary="Get logged in student's applications")
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

@router.post("/apply", response_model=ActivityPointResponse, summary="Student applies for activity points")
async def apply_activity_points(
    payload: ApplyActivityPointsRequest,
    current_user: User = Depends(role_required([UserRole.STUDENT])),
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

@router.get("/faculty", response_model=List[ActivityPointResponse], summary="Get all applications for faculty review")
async def get_faculty_applications(
    current_user: User = Depends(role_required([UserRole.FACULTY, UserRole.HOD, UserRole.SUPER_ADMIN]))
):
    db = load_db()
    result = list(db["applications"].values())
    return sorted(result, key=lambda x: x["id"], reverse=True)

@router.post("/review/{application_id}", response_model=ActivityPointResponse, summary="Faculty reviews an application")
async def review_application(
    application_id: str,
    payload: ReviewActivityPointsRequest,
    current_user: User = Depends(role_required([UserRole.FACULTY, UserRole.HOD, UserRole.SUPER_ADMIN])),
    db_session: AsyncSession = Depends(get_db_session)
):
    allowed_statuses = {"Approved", "Rejected", "Under Verification", "Pending Review"}
    if payload.status not in allowed_statuses:
        raise HTTPException(status_code=400, detail=f"status must be one of {sorted(allowed_statuses)}")

    db = load_db()
    if application_id not in db["applications"]:
        raise HTTPException(status_code=404, detail="Application not found")

    app = db["applications"][application_id]

    if payload.status == "Approved":
        if payload.approved_points < 0 or payload.approved_points > app["claimed_points"]:
            raise HTTPException(status_code=400, detail=f"approved_points must be between 0 and {app['claimed_points']}")

        cat_q = await db_session.execute(
            select(ActivityPointCategory).where(
                ActivityPointCategory.code == app["category"], ActivityPointCategory.is_deleted.is_(False)
            )
        )
        category = cat_q.scalar_one_or_none()
        if category:
            already_approved = sum(
                a["approved_points"] for a in db["applications"].values()
                if a["student_id"] == app["student_id"] and a["category"] == app["category"]
                and a["status"] == "Approved" and a["id"] != application_id
            )
            if already_approved + payload.approved_points > float(category.max_points):
                raise HTTPException(
                    status_code=400,
                    detail=f"Approving these points would exceed the {category.max_points}-point cap for '{category.name}' "
                           f"(student already has {already_approved} approved in this category)"
                )

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




@router.get("/categories", response_model=List[ActivityPointCategoryResponse], summary="List activity point categories with their point caps")
async def list_categories(
    current_user: User = Depends(get_current_user),
    db_session: AsyncSession = Depends(get_db_session)
):
    rows = await db_session.execute(
        select(ActivityPointCategory).where(ActivityPointCategory.is_deleted.is_(False)).order_by(ActivityPointCategory.name)
    )
    return list(rows.scalars().all())


@router.post("/categories", response_model=ActivityPointCategoryResponse, summary="Create an activity point category (staff only)")
async def create_category(
    payload: ActivityPointCategoryRequest,
    current_user: User = Depends(role_required([UserRole.FACULTY, UserRole.HOD, UserRole.PRINCIPAL, UserRole.ADMIN, UserRole.SUPER_ADMIN])),
    db_session: AsyncSession = Depends(get_db_session)
):
    existing = await db_session.execute(
        select(ActivityPointCategory).where(ActivityPointCategory.code == payload.code, ActivityPointCategory.is_deleted.is_(False))
    )
    if existing.scalar_one_or_none():
        raise HTTPException(status_code=400, detail="A category with this code already exists")

    category = ActivityPointCategory(
        code=payload.code, name=payload.name, max_points=payload.max_points, description=payload.description
    )
    db_session.add(category)
    await db_session.commit()
    await db_session.refresh(category)
    return category


@router.put("/categories/{category_id}", response_model=ActivityPointCategoryResponse, summary="Update an activity point category (staff only)")
async def update_category(
    category_id: str,
    payload: ActivityPointCategoryRequest,
    current_user: User = Depends(role_required([UserRole.FACULTY, UserRole.HOD, UserRole.PRINCIPAL, UserRole.ADMIN, UserRole.SUPER_ADMIN])),
    db_session: AsyncSession = Depends(get_db_session)
):
    category = await db_session.get(ActivityPointCategory, category_id)
    if not category or category.is_deleted:
        raise HTTPException(status_code=404, detail="Category not found")
    category.code = payload.code
    category.name = payload.name
    category.max_points = payload.max_points
    category.description = payload.description
    await db_session.commit()
    await db_session.refresh(category)
    return category


@router.delete("/categories/{category_id}", summary="Delete an activity point category (staff only)")
async def delete_category(
    category_id: str,
    current_user: User = Depends(role_required([UserRole.HOD, UserRole.PRINCIPAL, UserRole.ADMIN, UserRole.SUPER_ADMIN])),
    db_session: AsyncSession = Depends(get_db_session)
):
    category = await db_session.get(ActivityPointCategory, category_id)
    if not category or category.is_deleted:
        raise HTTPException(status_code=404, detail="Category not found")
    category.is_deleted = True
    await db_session.commit()
    return {"ok": True}
