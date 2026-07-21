import os
import uuid
from datetime import datetime
from typing import Dict, Any, List, Optional

from fastapi import APIRouter, Depends, HTTPException, UploadFile, File
from pydantic import BaseModel

from app.core.dependencies import get_current_user, role_required
from app.db.models.user import User, UserRole
from app.core.json_db_helper import load_db_from_postgres, save_db_to_postgres

router = APIRouter()

DB_FILE = os.path.join(os.path.dirname(os.path.dirname(os.path.dirname(os.path.dirname(__file__)))), "community_service_db.json")


def get_default_db() -> Dict[str, Any]:
    return {
        "opportunities": [
            {
                "id": 1,
                "title": "Legal Awareness Camp for Rural Women",
                "organizer": "Nyaya Bandhu NGO",
                "date": "2026-08-10",
                "location": "Thanjavur District Court Grounds",
                "spots": 15,
                "hours": "6 hrs",
                "tags": ["legal_aid", "outreach"]
            },
            {
                "id": 2,
                "title": "Free Legal Aid Clinic",
                "organizer": "District Legal Services Authority",
                "date": "2026-08-22",
                "location": "DLSA Office, Chennai",
                "spots": 8,
                "hours": "4 hrs",
                "tags": ["legal_aid", "clinic"]
            },
            {
                "id": 3,
                "title": "Prison Inmate Rights Awareness Drive",
                "organizer": "Prisoners' Rights Forum",
                "date": "2026-09-05",
                "location": "Central Prison, Puzhal",
                "spots": 10,
                "hours": "5 hrs",
                "tags": ["human_rights", "outreach"]
            }
        ],
        "logs": {}
    }


def load_db() -> Dict[str, Any]:
    return load_db_from_postgres(DB_FILE, get_default_db)


def save_db(db: Dict[str, Any]) -> None:
    save_db_to_postgres(DB_FILE, db)


class LogHoursRequest(BaseModel):
    title: str
    organization: str
    category: str
    date: str
    hours: float
    description: str
    proof_document: str | None = None


class OpportunityResponse(BaseModel):
    id: int
    title: str
    organizer: str
    date: str
    location: str
    spots: int
    hours: str
    tags: List[str]


class ServiceLogResponse(BaseModel):
    id: str
    student_id: str
    title: str
    organization: str
    category: str
    date: str
    hours: float
    status: str
    is_verified: bool
    certificate_url: Optional[str] = None
    description: str
    proof_document: Optional[str] = None


class DeleteResponse(BaseModel):
    ok: bool


class ServiceUploadResponse(BaseModel):
    status: str
    file_url: str


@router.get("/opportunities", response_model=List[OpportunityResponse], summary="List volunteer opportunities")
async def get_opportunities(
    current_user: User = Depends(get_current_user)
):
    db = load_db()
    return db["opportunities"]


@router.post("/opportunities/{opportunity_id}/apply", response_model=ServiceLogResponse, summary="Apply to a volunteer opportunity")
async def apply_to_opportunity(
    opportunity_id: int,
    current_user: User = Depends(role_required([UserRole.STUDENT])),
):
    db = load_db()
    opportunity = next((o for o in db["opportunities"] if o["id"] == opportunity_id), None)
    if not opportunity:
        raise HTTPException(status_code=404, detail="Opportunity not found")

    log_id = f"CS-{uuid.uuid4().hex[:6].upper()}"
    new_log = {
        "id": log_id,
        "student_id": current_user.id,
        "title": opportunity["title"],
        "organization": opportunity["organizer"],
        "category": opportunity["tags"][0] if opportunity["tags"] else "general",
        "date": opportunity["date"],
        "hours": 0.0,
        "status": "Applied",
        "is_verified": False,
        "certificate_url": None,
        "description": f"Applied to volunteer opportunity: {opportunity['title']}",
        "proof_document": None
    }
    db["logs"][log_id] = new_log
    save_db(db)
    return new_log


@router.post("/log-hours", response_model=ServiceLogResponse, summary="Log completed community service hours")
async def log_hours(
    payload: LogHoursRequest,
    current_user: User = Depends(role_required([UserRole.STUDENT])),
):
    db = load_db()
    log_id = f"CS-{uuid.uuid4().hex[:6].upper()}"
    new_log = {
        "id": log_id,
        "student_id": current_user.id,
        "title": payload.title,
        "organization": payload.organization,
        "category": payload.category,
        "date": payload.date,
        "hours": payload.hours,
        "status": "Pending Review",
        "is_verified": False,
        "certificate_url": None,
        "description": payload.description,
        "proof_document": payload.proof_document
    }
    db["logs"][log_id] = new_log
    save_db(db)
    return new_log


@router.get("/logs", response_model=List[ServiceLogResponse], summary="Get logged-in student's service logs")
async def get_logs(
    current_user: User = Depends(role_required([UserRole.STUDENT])),
):
    db = load_db()
    result = [log for log in db["logs"].values() if log["student_id"] == current_user.id]
    return sorted(result, key=lambda x: x["date"], reverse=True)


@router.delete("/logs/{log_id}", response_model=DeleteResponse, summary="Withdraw / delete a service log")
async def delete_log(
    log_id: str,
    current_user: User = Depends(role_required([UserRole.STUDENT])),
):
    db = load_db()
    log = db["logs"].get(log_id)
    if not log:
        raise HTTPException(status_code=404, detail="Log not found")
    if log["student_id"] != current_user.id:
        raise HTTPException(status_code=403, detail="Not authorized to delete this log")
    del db["logs"][log_id]
    save_db(db)
    return {"ok": True}


@router.post("/upload-document", response_model=ServiceUploadResponse, summary="Upload a proof document for a service log")
async def upload_service_document(
    file: UploadFile = File(...),
    current_user: User = Depends(role_required([UserRole.STUDENT])),
):
    static_dir = os.path.join(os.path.dirname(os.path.dirname(os.path.dirname(os.path.dirname(os.path.abspath(__file__))))), "static")
    docs_dir = os.path.join(static_dir, "uploads", "documents")
    os.makedirs(docs_dir, exist_ok=True)

    ext = os.path.splitext(file.filename)[1] if file.filename else ".pdf"
    if ext.lower() not in [".pdf", ".jpg", ".jpeg", ".png"]:
        raise HTTPException(status_code=400, detail="Only PDF, JPG, and PNG are allowed.")

    safe_filename = f"community_service_{current_user.id}_{uuid.uuid4().hex}{ext}"
    file_path = os.path.join(docs_dir, safe_filename)

    with open(file_path, "wb") as f:
        f.write(await file.read())

    file_url = f"/api/v1/files/documents/{safe_filename}"
    return {"status": "success", "file_url": file_url}
