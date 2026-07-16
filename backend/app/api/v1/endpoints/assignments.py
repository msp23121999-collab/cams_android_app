import os
import json
import uuid
from datetime import datetime
from typing import List, Dict, Any, Optional
from fastapi import APIRouter, Depends, HTTPException, Request, status
from pydantic import BaseModel
from sqlalchemy.ext.asyncio import AsyncSession
from sqlalchemy import select
from app.core.dependencies import get_db_session

from app.core.security import decode_token

router = APIRouter()

from app.core.json_db_helper import load_db_from_postgres, save_db_to_postgres

DB_FILE = os.path.join(
    os.path.dirname(os.path.dirname(os.path.dirname(os.path.dirname(__file__)))),
    "assignments_db.json"
)

def load_db() -> Dict[str, Any]:
    return load_db_from_postgres(DB_FILE, lambda: {"assignments": {}, "submissions": {}})

def save_db(db: Dict[str, Any]) -> None:
    save_db_to_postgres(DB_FILE, db)

# Lightweight auth: decode JWT without any DB lookup.
# Works reliably even if DB connections are slow or user lookup fails.
class TokenUser:
    def __init__(self, user_id: str, role: str, name: str = "Faculty"):
        self.id = user_id
        self.role = role
        self.full_name = name

def get_token_user(request: Request) -> TokenUser:
    """Extract user info from the Bearer JWT without a database call."""
    auth_header = request.headers.get("Authorization", "")
    token = None
    if auth_header.startswith("Bearer "):
        token = auth_header[7:]
    if not token:
        # Fall back to cookie
        token = request.cookies.get("access_token")
    if not token:
        raise HTTPException(status_code=status.HTTP_401_UNAUTHORIZED, detail="Not authenticated")
    try:
        payload = decode_token(token)
    except ValueError:
        raise HTTPException(status_code=status.HTTP_401_UNAUTHORIZED, detail="Invalid or expired token. Please log in again.")
    if payload.get("type") != "access":
        raise HTTPException(status_code=status.HTTP_401_UNAUTHORIZED, detail="Invalid token type")
    user_id = payload.get("sub", "")
    role = payload.get("role", "FACULTY")
    return TokenUser(user_id=user_id, role=role)

def require_staff(request: Request) -> TokenUser:
    """Require any staff role (FACULTY, HOD, PRINCIPAL, SUPER_ADMIN, ADMIN)."""
    user = get_token_user(request)
    allowed = {"FACULTY", "HOD", "PRINCIPAL", "SUPER_ADMIN", "ADMIN"}
    if user.role not in allowed:
        raise HTTPException(status_code=status.HTTP_403_FORBIDDEN, detail="Insufficient permissions")
    return user

def require_student(request: Request) -> TokenUser:
    """Require STUDENT role."""
    user = get_token_user(request)
    if user.role != "STUDENT":
        raise HTTPException(status_code=status.HTTP_403_FORBIDDEN, detail="Student access required")
    return user

# Pydantic Schemas
class AttachmentSchema(BaseModel):
    name: str
    url: str
    size: str
    type: str

class CreateAssignmentRequest(BaseModel):
    title: str
    type: str
    subject: str
    unit: str
    topic: str
    description: str
    instructions: str
    total_marks: int
    deadline: str  # ISO Format
    status: str    # "Draft" | "Published"
    semester: str
    section: str
    attachments: Optional[List[AttachmentSchema]] = []

class UpdateAssignmentRequest(BaseModel):
    title: str
    type: str
    subject: str
    unit: str
    topic: str
    description: str
    instructions: str
    total_marks: int
    deadline: str
    status: str
    semester: str
    section: str
    attachments: Optional[List[AttachmentSchema]] = []

class ActionRequest(BaseModel):
    action: str  # "extend_deadline" | "close" | "archive" | "duplicate"
    deadline: Optional[str] = None

class GradeSubmissionRequest(BaseModel):
    marks_obtained: float
    grade: str
    feedback: str
    remarks: str
    status: str  # "Evaluated" | "Returned for Resubmission" | "Pending Evaluation"

class StudentSubmitRequest(BaseModel):
    submitted_file: dict  # {"name": "...", "url": "...", "size": "..."}
    submitted_text: Optional[str] = ""

# --- Faculty Endpoints ---


@router.get("/my-assignments")
async def get_my_assignments(request: Request):
    current_user = require_staff(request)
    db = load_db()
    result = []
    for a in db["assignments"].values():
        if a.get("faculty_id") == current_user.id:
            result.append(a)
    return sorted(result, key=lambda x: x.get("issue_date", ""), reverse=True)

@router.post("/create")
async def create_assignment(
    payload: CreateAssignmentRequest,
    request: Request,
    db: AsyncSession = Depends(get_db_session)
):
    current_user = require_staff(request)
    try:
        from app.db.models.user import User, UserRole
        from app.db.models.student import Student, ParentStudentMap
        from app.db.models.academic import Section, Course
        
        db_data = load_db()
        asg_id = f"asg_{uuid.uuid4().hex[:10]}"
        now_str = datetime.now().isoformat()
        new_asg = {
            "id": asg_id,
            "title": payload.title,
            "type": payload.type,
            "subject": payload.subject,
            "unit": payload.unit,
            "topic": payload.topic,
            "description": payload.description,
            "instructions": payload.instructions,
            "total_marks": payload.total_marks,
            "issue_date": now_str,
            "deadline": payload.deadline,
            "status": payload.status,
            "faculty_id": current_user.id,
            "faculty_name": "Faculty",
            "semester": payload.semester,
            "section": payload.section,
            "attachments": [a.model_dump() for a in payload.attachments] if payload.attachments else []
        }
        db_data["assignments"][asg_id] = new_asg
        save_db(db_data)
        
        # Trigger notifications in the SQLite DB
        sec_stmt = select(Section).where(Section.section_name == payload.section)
        sec_res = await db.execute(sec_stmt)
        sections = sec_res.scalars().all()
        section_ids = [s.id for s in sections]
        
        if section_ids:
            students_q = await db.execute(select(Student).where(Student.section_id.in_(section_ids), Student.is_deleted.is_(False)))
            students = students_q.scalars().all()
            
            from app.services.notification_service import NotificationService
            notif_service = NotificationService(db)
            
            # 1. Notify HOD of faculty's department
            user_q = await db.execute(select(User).where(User.id == current_user.id))
            faculty_member = user_q.scalar_one_or_none()
            if faculty_member and faculty_member.department_id:
                from app.db.models.academic import Department
                dept_q = await db.execute(select(Department).where(Department.id == faculty_member.department_id))
                dept = dept_q.scalars().first()
                if dept and dept.hod_id:
                    await notif_service.send_notification(
                        user_id=dept.hod_id,
                        type_val="new_assignment",
                        message=f"Faculty {faculty_member.full_name} has published a new assignment '{payload.title}'."
                    )
            
            # 2. Notify students and parents
            for student in students:
                await notif_service.send_notification(
                    user_id=student.user_id,
                    type_val="new_assignment",
                    message=f"New assignment '{payload.title}' has been published. Due date: {payload.deadline}."
                )
                pm_q = await db.execute(select(ParentStudentMap).where(ParentStudentMap.student_id == student.id, ParentStudentMap.is_deleted.is_(False)))
                for pm in pm_q.scalars().all():
                    await notif_service.send_notification(
                        user_id=pm.parent_id,
                        type_val="new_assignment",
                        message=f"A new assignment '{payload.title}' has been published for your child's section. Due date: {payload.deadline}."
                    )
            await db.commit()
            
        return new_asg
    except HTTPException:
        raise
    except Exception as e:
        raise HTTPException(status_code=500, detail=f"Failed to create assignment: {str(e)}")


@router.put("/{asg_id}")
async def update_assignment(request: Request, asg_id: str, payload: UpdateAssignmentRequest):
    require_staff(request)
    db = load_db()
    if asg_id not in db["assignments"]:
        raise HTTPException(status_code=404, detail="Assignment not found")
    asg = db["assignments"][asg_id]
    asg["title"] = payload.title
    asg["type"] = payload.type
    asg["subject"] = payload.subject
    asg["unit"] = payload.unit
    asg["topic"] = payload.topic
    asg["description"] = payload.description
    asg["instructions"] = payload.instructions
    asg["total_marks"] = payload.total_marks
    asg["deadline"] = payload.deadline
    asg["status"] = payload.status
    asg["semester"] = payload.semester
    asg["section"] = payload.section
    asg["attachments"] = [a.model_dump() for a in payload.attachments] if payload.attachments else []
    db["assignments"][asg_id] = asg
    save_db(db)
    return asg

@router.delete("/{asg_id}")
async def delete_assignment(request: Request, asg_id: str):
    require_staff(request)
    db = load_db()
    if asg_id not in db["assignments"]:
        raise HTTPException(status_code=404, detail="Assignment not found")
    del db["assignments"][asg_id]
    subs_to_del = [sid for sid, s in db["submissions"].items() if s["assignment_id"] == asg_id]
    for sid in subs_to_del:
        del db["submissions"][sid]
    save_db(db)
    return {"detail": "Assignment deleted successfully"}

@router.post("/{asg_id}/action")
async def assignment_action(request: Request, asg_id: str, payload: ActionRequest):
    require_staff(request)
    db = load_db()
    if asg_id not in db["assignments"]:
        raise HTTPException(status_code=404, detail="Assignment not found")
    asg = db["assignments"][asg_id]
    if payload.action == "extend_deadline":
        if not payload.deadline:
            raise HTTPException(status_code=400, detail="Deadline is required for extending")
        asg["deadline"] = payload.deadline
    elif payload.action == "close":
        asg["status"] = "Closed"
    elif payload.action == "archive":
        asg["status"] = "Archived"
    elif payload.action == "duplicate":
        new_id = f"asg_{uuid.uuid4().hex[:10]}"
        new_asg = asg.copy()
        new_asg["id"] = new_id
        new_asg["title"] = f"{asg['title']} - Copy"
        new_asg["status"] = "Draft"
        new_asg["issue_date"] = datetime.now().isoformat()
        db["assignments"][new_id] = new_asg
        save_db(db)
        return new_asg
    else:
        raise HTTPException(status_code=400, detail="Invalid action")
    db["assignments"][asg_id] = asg
    save_db(db)
    return asg

@router.get("/submissions")
async def get_student_submissions(request: Request):
    current_user = require_staff(request)
    db = load_db()
    result = []
    for s in db["submissions"].values():
        asg_id = s["assignment_id"]
        asg = db["assignments"].get(asg_id)
        if asg:
            if asg.get("faculty_id") == current_user.id:
                s_copy = s.copy()
                s_copy["assignment_title"] = asg["title"]
                s_copy["assignment_type"] = asg["type"]
                s_copy["subject"] = asg["subject"]
                s_copy["total_marks"] = asg["total_marks"]
                s_copy["deadline"] = asg["deadline"]
                result.append(s_copy)
    return result

@router.post("/grade/{submission_id}")
async def grade_submission(request: Request, submission_id: str, payload: GradeSubmissionRequest):
    current_user = require_staff(request)
    db = load_db()
    if submission_id not in db["submissions"]:
        raise HTTPException(status_code=404, detail="Submission not found")
    sub = db["submissions"][submission_id]
    sub["evaluation"] = {
        "marks_obtained": payload.marks_obtained,
        "total_marks": db["assignments"][sub["assignment_id"]]["total_marks"],
        "grade": payload.grade,
        "feedback": payload.feedback,
        "remarks": payload.remarks,
        "status": payload.status,
        "graded_date": datetime.now().isoformat(),
        "graded_by": "Faculty"
    }
    if payload.status == "Returned for Resubmission":
        sub["status"] = "Pending"
    db["submissions"][submission_id] = sub
    save_db(db)
    return sub

@router.get("/reports")
async def get_assignment_reports(request: Request):
    current_user = require_staff(request)
    db = load_db()
    # Collect matching assignments
    my_asgs = [a for a in db["assignments"].values() if a.get("faculty_id") == current_user.id]
    asg_ids = [a["id"] for a in my_asgs]
    
    # Collect submissions for these assignments
    my_subs = [s for s in db["submissions"].values() if s["assignment_id"] in asg_ids]
    
    total_assignments = len(my_asgs)
    published_count = len([a for a in my_asgs if a["status"] == "Published"])
    closed_count = len([a for a in my_asgs if a["status"] == "Closed"])
    draft_count = len([a for a in my_asgs if a["status"] == "Draft"])
    
    total_submitted = len([s for s in my_subs if s["status"] in ["Submitted", "Late Submission"]])
    late_submissions = len([s for s in my_subs if s["status"] == "Late Submission"])
    pending_evals = len([s for s in my_subs if s.get("evaluation") is None and s["status"] in ["Submitted", "Late Submission"]])
    
    # Subject wise breakdown
    subject_stats = {}
    for a in my_asgs:
        subj = a["subject"]
        if subj not in subject_stats:
            subject_stats[subj] = {"total": 0, "submissions": 0, "marks_sum": 0.0, "marks_count": 0}
        subject_stats[subj]["total"] += 1
        
    for s in my_subs:
        asg = db["assignments"].get(s["assignment_id"])
        if asg:
            subj = asg["subject"]
            if subj in subject_stats:
                subject_stats[subj]["submissions"] += 1
                eval_data = s.get("evaluation")
                if eval_data and eval_data.get("marks_obtained") is not None:
                    subject_stats[subj]["marks_sum"] += eval_data["marks_obtained"]
                    subject_stats[subj]["marks_count"] += 1
                    
    subject_report = []
    for subj, sdata in subject_stats.items():
        avg_marks = 0.0
        if sdata["marks_count"] > 0:
            avg_marks = round(sdata["marks_sum"] / sdata["marks_count"], 1)
        subject_report.append({
            "subject": subj,
            "assignments_count": sdata["total"],
            "submissions_count": sdata["submissions"],
            "average_marks": avg_marks
        })
        
    # Student performance metrics
    student_stats = {}
    for s in my_subs:
        sname = s["student_name"]
        reg = s["register_number"]
        key = (sname, reg)
        if key not in student_stats:
            student_stats[key] = {"submitted": 0, "graded": 0, "total_marks": 0.0, "possible_marks": 0.0}
        
        student_stats[key]["submitted"] += 1
        eval_data = s.get("evaluation")
        if eval_data and eval_data.get("status") == "Evaluated":
            student_stats[key]["graded"] += 1
            student_stats[key]["total_marks"] += eval_data["marks_obtained"]
            student_stats[key]["possible_marks"] += eval_data["total_marks"]
            
    student_report = []
    for (sname, reg), stats in student_stats.items():
        score_percentage = 0.0
        if stats["possible_marks"] > 0:
            score_percentage = round((stats["total_marks"] / stats["possible_marks"]) * 100, 1)
        student_report.append({
            "student_name": sname,
            "register_number": reg,
            "submitted_count": stats["submitted"],
            "graded_count": stats["graded"],
            "performance_percentage": score_percentage
        })

    return {
        "summary": {
            "total_assignments": total_assignments,
            "published": published_count,
            "closed": closed_count,
            "draft": draft_count,
            "total_submissions": total_submitted,
            "late_submissions": late_submissions,
            "pending_evaluations": pending_evals
        },
        "subject_wise": subject_report,
        "student_performance": student_report
    }

# --- Student Endpoints ---

@router.get("/active-assignments")
async def get_active_assignments(request: Request):
    current_user = require_student(request)
    db = load_db()
    result = []
    for a in db["assignments"].values():
        if a["status"] in ("Published", "Closed"):
            user_sub = None
            for s in db["submissions"].values():
                if s["assignment_id"] == a["id"] and s["student_id"] == current_user.id:
                    user_sub = s
                    break
            a_copy = a.copy()
            a_copy["my_submission"] = user_sub
            result.append(a_copy)
    return sorted(result, key=lambda x: x.get("deadline", ""), reverse=False)

@router.post("/submit/{asg_id}")
async def student_submit_assignment(request: Request, asg_id: str, payload: StudentSubmitRequest):
    current_user = require_student(request)
    db = load_db()
    if asg_id not in db["assignments"]:
        raise HTTPException(status_code=404, detail="Assignment not found")
    asg = db["assignments"][asg_id]
    if asg["status"] in ("Closed", "Archived"):
        raise HTTPException(status_code=400, detail="Assignment is closed for submissions")
    existing_sub_id = None
    for sid, s in db["submissions"].items():
        if s["assignment_id"] == asg_id and s["student_id"] == current_user.id:
            existing_sub_id = sid
            break
    now = datetime.now()
    deadline_str = asg["deadline"].replace("Z", "+00:00") if asg["deadline"].endswith("Z") else asg["deadline"]
    try:
        deadline = datetime.fromisoformat(deadline_str.replace("+00:00", ""))
    except Exception:
        deadline = now
    sub_status = "Submitted" if now <= deadline else "Late Submission"
    if existing_sub_id:
        sub = db["submissions"][existing_sub_id]
        history_entry = {
            "submission_date": sub["submission_date"],
            "submitted_file": sub["submitted_file"],
            "submitted_text": sub.get("submitted_text", "")
        }
        if "history" not in sub:
            sub["history"] = []
        sub["history"].append(history_entry)
        sub["submission_date"] = now.isoformat()
        sub["status"] = sub_status
        sub["submitted_file"] = payload.submitted_file
        sub["submitted_text"] = payload.submitted_text
        sub["evaluation"] = None
        db["submissions"][existing_sub_id] = sub
        save_db(db)
        return sub
    else:
        new_sub_id = f"sub_{uuid.uuid4().hex[:10]}"
        new_sub = {
            "id": new_sub_id,
            "assignment_id": asg_id,
            "student_id": current_user.id,
            "student_name": "Student",
            "register_number": "23BALLB001",
            "submission_date": now.isoformat(),
            "status": sub_status,
            "submitted_file": payload.submitted_file,
            "submitted_text": payload.submitted_text,
            "evaluation": None,
            "history": []
        }
        db["submissions"][new_sub_id] = new_sub
        save_db(db)
        return new_sub

