import os
import uuid
from datetime import datetime
from typing import Dict, Any, List

from fastapi import APIRouter, Depends, HTTPException
from pydantic import BaseModel

from app.core.dependencies import get_current_user, role_required
from app.db.models.user import User, UserRole
from app.core.json_db_helper import load_db_from_postgres, save_db_to_postgres

router = APIRouter()

DB_FILE = os.path.join(os.path.dirname(os.path.dirname(os.path.dirname(os.path.dirname(__file__)))), "innovation_wall_db.json")


def get_default_db() -> Dict[str, Any]:
    return {
        "projects": {
            "IW-001": {
                "id": "IW-001",
                "student_id": "mock-student-id-123",
                "title": "LexBrief - AI Case Summarizer",
                "description": "A tool that uses NLP to auto-summarize long judgments into concise briefs for junior associates.",
                "category": "Legal Technology",
                "mentor": "Prof. Ananya Rao",
                "team": ["Ananya Sharma", "Rohit Verma"],
                "badges": ["Top Rated"],
                "liked_by": [],
                "comment_count": 2,
                "created_at": "2026-03-01T10:00:00"
            },
            "IW-002": {
                "id": "IW-002",
                "student_id": "mock-student-id-123",
                "title": "Rural Legal Aid Connect",
                "description": "A platform connecting rural citizens with pro-bono legal aid volunteers via SMS-based intake forms.",
                "category": "Community Projects",
                "mentor": "Prof. Vikram Singh",
                "team": ["Priya Menon"],
                "badges": [],
                "liked_by": [],
                "comment_count": 0,
                "created_at": "2026-04-10T10:00:00"
            }
        }
    }


def load_db() -> Dict[str, Any]:
    return load_db_from_postgres(DB_FILE, get_default_db)


def save_db(db: Dict[str, Any]) -> None:
    save_db_to_postgres(DB_FILE, db)


class ProjectCreateRequest(BaseModel):
    title: str
    description: str
    category: str
    mentor: str
    team: list[str] = []


class CommentCreateRequest(BaseModel):
    text: str


class ProjectResponse(BaseModel):
    id: str
    title: str
    description: str
    category: str
    mentor: str
    team: List[str]
    badges: List[str]
    likes: int
    liked_by_me: bool
    comments: int


class DeleteResponse(BaseModel):
    ok: bool


def _serialize(project: dict, current_user_id: str) -> dict:
    return {
        "id": project["id"],
        "title": project["title"],
        "description": project["description"],
        "category": project["category"],
        "mentor": project["mentor"],
        "team": project["team"],
        "badges": project["badges"],
        "likes": len(project["liked_by"]),
        "liked_by_me": current_user_id in project["liked_by"],
        "comments": project["comment_count"]
    }


@router.get("/projects", response_model=List[ProjectResponse], summary="List all innovation wall projects")
async def get_projects(
    current_user: User = Depends(get_current_user)
):
    db = load_db()
    projects = list(db["projects"].values())
    projects.sort(key=lambda p: p["created_at"], reverse=True)
    return [_serialize(p, current_user.id) for p in projects]


@router.post("/projects", response_model=ProjectResponse, summary="Submit a new innovation project")
async def create_project(
    payload: ProjectCreateRequest,
    current_user: User = Depends(role_required([UserRole.STUDENT])),
):
    db = load_db()
    project_id = f"IW-{uuid.uuid4().hex[:6].upper()}"
    new_project = {
        "id": project_id,
        "student_id": current_user.id,
        "title": payload.title,
        "description": payload.description,
        "category": payload.category,
        "mentor": payload.mentor,
        "team": payload.team,
        "badges": [],
        "liked_by": [],
        "comment_count": 0,
        "created_at": datetime.now().isoformat()
    }
    db["projects"][project_id] = new_project
    save_db(db)
    return _serialize(new_project, current_user.id)


@router.post("/projects/{project_id}/like", response_model=ProjectResponse, summary="Toggle like on a project")
async def toggle_like(
    project_id: str,
    current_user: User = Depends(role_required([UserRole.STUDENT])),
):
    db = load_db()
    project = db["projects"].get(project_id)
    if not project:
        raise HTTPException(status_code=404, detail="Project not found")

    if current_user.id in project["liked_by"]:
        project["liked_by"].remove(current_user.id)
    else:
        project["liked_by"].append(current_user.id)

    save_db(db)
    return _serialize(project, current_user.id)


@router.post("/projects/{project_id}/comments", response_model=ProjectResponse, summary="Add a comment to a project")
async def add_comment(
    project_id: str,
    payload: CommentCreateRequest,
    current_user: User = Depends(role_required([UserRole.STUDENT])),
):
    db = load_db()
    project = db["projects"].get(project_id)
    if not project:
        raise HTTPException(status_code=404, detail="Project not found")

    project["comment_count"] = project.get("comment_count", 0) + 1
    save_db(db)
    return _serialize(project, current_user.id)


@router.delete("/projects/{project_id}", response_model=DeleteResponse, summary="Delete own project")
async def delete_project(
    project_id: str,
    current_user: User = Depends(role_required([UserRole.STUDENT])),
):
    db = load_db()
    project = db["projects"].get(project_id)
    if not project:
        raise HTTPException(status_code=404, detail="Project not found")
    if project["student_id"] != current_user.id:
        raise HTTPException(status_code=403, detail="Not authorized to delete this project")
    del db["projects"][project_id]
    save_db(db)
    return {"ok": True}
