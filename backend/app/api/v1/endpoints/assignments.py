"""Assignments endpoint — real SQLAlchemy-backed storage.

Previously this stored everything in a JSON blob (assignments_db.json) via
load_db_from_postgres/save_db_to_postgres, and used a bespoke get_token_user()
that manually decoded the JWT without a DB lookup, bypassing the app's normal
role_required authorization pattern. This rewrite uses the real Assignment /
AssignmentSubmission tables and the standard get_current_user/role_required
dependencies, and resolves the submitting student's real name/roll number
from the Student table instead of hardcoding placeholders.
"""
import json
import os
import uuid
from datetime import datetime, timezone
from typing import Optional

from fastapi import APIRouter, Depends, HTTPException, UploadFile, File
from pydantic import BaseModel
from sqlalchemy.ext.asyncio import AsyncSession
from sqlalchemy import select

from app.core.dependencies import get_db_session, get_current_user, role_required
from app.db.models.user import User, UserRole
from app.db.models.student import Student, ParentStudentMap
from app.db.models.academic import Section, Course
from app.db.models.study_material import Assignment, AssignmentSubmission
from app.db.repositories.student_repository import StudentRepository

router = APIRouter()

_BACKEND_DIR = os.path.dirname(os.path.dirname(os.path.dirname(os.path.dirname(os.path.abspath(__file__)))))
UPLOADS_DIR = os.path.join(_BACKEND_DIR, "static", "uploads", "assignments")


# ─── Pydantic Schemas ──────────────────────────────────────────────────────

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
    deadline: str  # ISO format
    status: str    # "Draft" | "Published"
    semester: str
    section: str
    attachments: Optional[list[AttachmentSchema]] = []


class UpdateAssignmentRequest(CreateAssignmentRequest):
    pass


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
    file_url: Optional[str] = None
    submitted_text: Optional[str] = ""


class AssignmentResponse(BaseModel):
    id: str
    title: str
    type: str | None = None
    subject: str | None = None
    unit: str | None = None
    topic: str | None = None
    description: str | None = None
    instructions: str | None = None
    total_marks: int | None = None
    deadline: str | None = None
    status: str
    faculty_id: str
    faculty_name: str | None = None
    semester: str | None = None
    section: str | None = None
    attachments: list[AttachmentSchema] = []
    my_submission: Optional[dict] = None


class SubmissionResponse(BaseModel):
    id: str
    assignment_id: str
    student_id: str
    submitted_file_url: str | None = None
    submitted_text: str | None = None
    marks_obtained: float | None = None
    grade: str | None = None
    feedback: str | None = None
    remarks: str | None = None
    status: str
    submitted_at: datetime
    student_name: str | None = None
    register_number: str | None = None


def _parse_attachments(raw: str | None) -> list[dict]:
    if not raw:
        return []
    try:
        return json.loads(raw)
    except Exception:
        return []


def _asg_to_response(a: Assignment, my_submission: dict | None = None, faculty_name: str | None = None) -> AssignmentResponse:
    return AssignmentResponse(
        id=a.id,
        title=a.title,
        type=a.type,
        subject=a.subject,
        unit=a.unit,
        topic=a.topic,
        description=a.description,
        instructions=a.instructions,
        total_marks=a.total_marks,
        deadline=a.deadline.isoformat() if a.deadline else None,
        status=a.status,
        faculty_id=a.faculty_id,
        faculty_name=faculty_name,
        semester=a.semester,
        section=a.section,
        attachments=_parse_attachments(a.attachments),
        my_submission=my_submission,
    )


def _parse_deadline(deadline_str: str) -> datetime:
    cleaned = deadline_str.replace("Z", "+00:00") if deadline_str.endswith("Z") else deadline_str
    try:
        return datetime.fromisoformat(cleaned)
    except Exception:
        return datetime.now(timezone.utc)


STAFF_ROLES = [UserRole.FACULTY, UserRole.HOD, UserRole.PRINCIPAL, UserRole.ADMIN, UserRole.SUPER_ADMIN]


# ─── Faculty / Staff Endpoints ─────────────────────────────────────────────

@router.get("/my-assignments", response_model=list[AssignmentResponse])
async def get_my_assignments(
    current_user: User = Depends(role_required(STAFF_ROLES)),
    db: AsyncSession = Depends(get_db_session),
):
    rows = await db.execute(
        select(Assignment)
        .where(Assignment.faculty_id == current_user.id, Assignment.is_deleted.is_(False))
        .order_by(Assignment.created_at.desc())
    )
    return [_asg_to_response(a) for a in rows.scalars().all()]


@router.post("/create", response_model=AssignmentResponse)
async def create_assignment(
    payload: CreateAssignmentRequest,
    current_user: User = Depends(role_required(STAFF_ROLES)),
    db: AsyncSession = Depends(get_db_session),
):
    section_res = await db.execute(select(Section).where(Section.section_name == payload.section, Section.is_deleted.is_(False)))
    section = section_res.scalars().first()

    assignment = Assignment(
        section_id=section.id if section else None,
        faculty_id=current_user.id,
        title=payload.title,
        deadline=_parse_deadline(payload.deadline).date(),
        submission_count=0,
        type=payload.type,
        subject=payload.subject,
        unit=payload.unit,
        topic=payload.topic,
        description=payload.description,
        instructions=payload.instructions,
        total_marks=payload.total_marks,
        status=payload.status,
        semester=payload.semester,
        section=payload.section,
        attachments=json.dumps([a.model_dump() for a in payload.attachments]) if payload.attachments else None,
    )
    if not assignment.section_id:
        raise HTTPException(status_code=400, detail=f"Unknown section '{payload.section}'")

    db.add(assignment)
    await db.flush()

    # Notify students/parents in that section (and the faculty's HOD).
    try:
        students_q = await db.execute(select(Student).where(Student.section_id == assignment.section_id, Student.is_deleted.is_(False)))
        students = students_q.scalars().all()
        if students:
            from app.services.notification_service import NotificationService
            from app.db.models.academic import Department
            notif_service = NotificationService(db)

            if current_user.department_id:
                dept_q = await db.execute(select(Department).where(Department.id == current_user.department_id))
                dept = dept_q.scalars().first()
                if dept and dept.hod_id:
                    await notif_service.send_notification(
                        user_id=dept.hod_id,
                        type_val="new_assignment",
                        message=f"Faculty {current_user.full_name} has published a new assignment '{payload.title}'.",
                    )

            for student in students:
                await notif_service.send_notification(
                    user_id=student.user_id,
                    type_val="new_assignment",
                    message=f"New assignment '{payload.title}' has been published. Due date: {payload.deadline}.",
                )
                pm_q = await db.execute(select(ParentStudentMap).where(ParentStudentMap.student_id == student.id, ParentStudentMap.is_deleted.is_(False)))
                for pm in pm_q.scalars().all():
                    await notif_service.send_notification(
                        user_id=pm.parent_id,
                        type_val="new_assignment",
                        message=f"A new assignment '{payload.title}' has been published for your child's section. Due date: {payload.deadline}.",
                    )
    except Exception:
        pass

    await db.commit()
    await db.refresh(assignment)
    return _asg_to_response(assignment)


@router.put("/{asg_id}", response_model=AssignmentResponse)
async def update_assignment(
    asg_id: str,
    payload: UpdateAssignmentRequest,
    current_user: User = Depends(role_required(STAFF_ROLES)),
    db: AsyncSession = Depends(get_db_session),
):
    asg_res = await db.execute(select(Assignment).where(Assignment.id == asg_id, Assignment.is_deleted.is_(False)))
    assignment = asg_res.scalar_one_or_none()
    if not assignment:
        raise HTTPException(status_code=404, detail="Assignment not found")
    if assignment.faculty_id != current_user.id and current_user.role not in (UserRole.HOD, UserRole.PRINCIPAL, UserRole.ADMIN, UserRole.SUPER_ADMIN):
        raise HTTPException(status_code=403, detail="You can only edit your own assignments")

    assignment.title = payload.title
    assignment.type = payload.type
    assignment.subject = payload.subject
    assignment.unit = payload.unit
    assignment.topic = payload.topic
    assignment.description = payload.description
    assignment.instructions = payload.instructions
    assignment.total_marks = payload.total_marks
    assignment.deadline = _parse_deadline(payload.deadline).date()
    assignment.status = payload.status
    assignment.semester = payload.semester
    assignment.section = payload.section
    assignment.attachments = json.dumps([a.model_dump() for a in payload.attachments]) if payload.attachments else None

    await db.commit()
    await db.refresh(assignment)
    return _asg_to_response(assignment)


@router.delete("/{asg_id}")
async def delete_assignment(
    asg_id: str,
    current_user: User = Depends(role_required(STAFF_ROLES)),
    db: AsyncSession = Depends(get_db_session),
):
    asg_res = await db.execute(select(Assignment).where(Assignment.id == asg_id, Assignment.is_deleted.is_(False)))
    assignment = asg_res.scalar_one_or_none()
    if not assignment:
        raise HTTPException(status_code=404, detail="Assignment not found")
    if assignment.faculty_id != current_user.id and current_user.role not in (UserRole.HOD, UserRole.PRINCIPAL, UserRole.ADMIN, UserRole.SUPER_ADMIN):
        raise HTTPException(status_code=403, detail="You can only delete your own assignments")

    assignment.is_deleted = True
    subs_res = await db.execute(select(AssignmentSubmission).where(AssignmentSubmission.assignment_id == asg_id))
    for sub in subs_res.scalars().all():
        sub.is_deleted = True

    await db.commit()
    return {"detail": "Assignment deleted successfully"}


@router.post("/{asg_id}/action", response_model=AssignmentResponse)
async def assignment_action(
    asg_id: str,
    payload: ActionRequest,
    current_user: User = Depends(role_required(STAFF_ROLES)),
    db: AsyncSession = Depends(get_db_session),
):
    asg_res = await db.execute(select(Assignment).where(Assignment.id == asg_id, Assignment.is_deleted.is_(False)))
    assignment = asg_res.scalar_one_or_none()
    if not assignment:
        raise HTTPException(status_code=404, detail="Assignment not found")
    if assignment.faculty_id != current_user.id and current_user.role not in (UserRole.HOD, UserRole.PRINCIPAL, UserRole.ADMIN, UserRole.SUPER_ADMIN):
        raise HTTPException(status_code=403, detail="You can only manage your own assignments")

    if payload.action == "extend_deadline":
        if not payload.deadline:
            raise HTTPException(status_code=400, detail="Deadline is required for extending")
        assignment.deadline = _parse_deadline(payload.deadline).date()
    elif payload.action == "close":
        assignment.status = "Closed"
    elif payload.action == "archive":
        assignment.status = "Archived"
    elif payload.action == "duplicate":
        new_assignment = Assignment(
            section_id=assignment.section_id,
            faculty_id=assignment.faculty_id,
            title=f"{assignment.title} - Copy",
            deadline=assignment.deadline,
            submission_count=0,
            type=assignment.type,
            subject=assignment.subject,
            unit=assignment.unit,
            topic=assignment.topic,
            description=assignment.description,
            instructions=assignment.instructions,
            total_marks=assignment.total_marks,
            status="Draft",
            semester=assignment.semester,
            section=assignment.section,
            attachments=assignment.attachments,
        )
        db.add(new_assignment)
        await db.commit()
        await db.refresh(new_assignment)
        return _asg_to_response(new_assignment)
    else:
        raise HTTPException(status_code=400, detail="Invalid action")

    await db.commit()
    await db.refresh(assignment)
    return _asg_to_response(assignment)


@router.get("/submissions", response_model=list[SubmissionResponse])
async def get_student_submissions(
    current_user: User = Depends(role_required(STAFF_ROLES)),
    db: AsyncSession = Depends(get_db_session),
):
    asg_res = await db.execute(select(Assignment).where(Assignment.faculty_id == current_user.id, Assignment.is_deleted.is_(False)))
    asg_ids = [a.id for a in asg_res.scalars().all()]
    if not asg_ids:
        return []

    subs_res = await db.execute(
        select(AssignmentSubmission).where(AssignmentSubmission.assignment_id.in_(asg_ids), AssignmentSubmission.is_deleted.is_(False))
    )
    submissions = subs_res.scalars().all()

    result = []
    for s in submissions:
        student_res = await db.execute(select(Student, User).join(User, Student.user_id == User.id).where(Student.id == s.student_id))
        row = student_res.first()
        student_name = row[1].full_name if row else None
        register_number = row[0].roll_no if row else None
        result.append(SubmissionResponse(
            id=s.id,
            assignment_id=s.assignment_id,
            student_id=s.student_id,
            submitted_file_url=s.submitted_file_url,
            submitted_text=s.submitted_text,
            marks_obtained=s.marks_obtained,
            grade=s.grade,
            feedback=s.feedback,
            remarks=s.remarks,
            status=s.status,
            submitted_at=s.submitted_at,
            student_name=student_name,
            register_number=register_number,
        ))
    return result


@router.post("/grade/{submission_id}", response_model=SubmissionResponse)
async def grade_submission(
    submission_id: str,
    payload: GradeSubmissionRequest,
    current_user: User = Depends(role_required(STAFF_ROLES)),
    db: AsyncSession = Depends(get_db_session),
):
    sub_res = await db.execute(select(AssignmentSubmission).where(AssignmentSubmission.id == submission_id, AssignmentSubmission.is_deleted.is_(False)))
    submission = sub_res.scalar_one_or_none()
    if not submission:
        raise HTTPException(status_code=404, detail="Submission not found")

    assignment = await db.get(Assignment, submission.assignment_id)
    if not assignment:
        raise HTTPException(status_code=404, detail="Parent assignment not found")
    if assignment.faculty_id != current_user.id and current_user.role not in (UserRole.HOD, UserRole.PRINCIPAL, UserRole.ADMIN, UserRole.SUPER_ADMIN):
        raise HTTPException(status_code=403, detail="You can only grade submissions for your own assignments")
    if assignment.total_marks is not None and payload.marks_obtained > assignment.total_marks:
        raise HTTPException(status_code=400, detail=f"Marks obtained cannot exceed the assignment's total marks ({assignment.total_marks})")
    if payload.marks_obtained < 0:
        raise HTTPException(status_code=400, detail="Marks obtained cannot be negative")

    submission.marks_obtained = payload.marks_obtained
    submission.grade = payload.grade
    submission.feedback = payload.feedback
    submission.remarks = payload.remarks
    submission.status = payload.status

    await db.commit()
    await db.refresh(submission)
    return SubmissionResponse(
        id=submission.id,
        assignment_id=submission.assignment_id,
        student_id=submission.student_id,
        submitted_file_url=submission.submitted_file_url,
        submitted_text=submission.submitted_text,
        marks_obtained=submission.marks_obtained,
        grade=submission.grade,
        feedback=submission.feedback,
        remarks=submission.remarks,
        status=submission.status,
        submitted_at=submission.submitted_at,
    )


@router.get("/reports")
async def get_assignment_reports(
    current_user: User = Depends(role_required(STAFF_ROLES)),
    db: AsyncSession = Depends(get_db_session),
):
    asg_res = await db.execute(select(Assignment).where(Assignment.faculty_id == current_user.id, Assignment.is_deleted.is_(False)))
    my_asgs = list(asg_res.scalars().all())
    asg_ids = [a.id for a in my_asgs]

    my_subs: list[AssignmentSubmission] = []
    if asg_ids:
        subs_res = await db.execute(
            select(AssignmentSubmission).where(AssignmentSubmission.assignment_id.in_(asg_ids), AssignmentSubmission.is_deleted.is_(False))
        )
        my_subs = list(subs_res.scalars().all())

    total_assignments = len(my_asgs)
    published_count = len([a for a in my_asgs if a.status == "Published"])
    closed_count = len([a for a in my_asgs if a.status == "Closed"])
    draft_count = len([a for a in my_asgs if a.status == "Draft"])

    total_submitted = len([s for s in my_subs if s.status in ("Submitted", "Late Submission")])
    late_submissions = len([s for s in my_subs if s.status == "Late Submission"])
    pending_evals = len([s for s in my_subs if s.marks_obtained is None and s.status in ("Submitted", "Late Submission")])

    asg_by_id = {a.id: a for a in my_asgs}
    subject_stats: dict[str, dict] = {}
    for a in my_asgs:
        subj = a.subject or "Unknown"
        subject_stats.setdefault(subj, {"total": 0, "submissions": 0, "marks_sum": 0.0, "marks_count": 0})
        subject_stats[subj]["total"] += 1

    for s in my_subs:
        a = asg_by_id.get(s.assignment_id)
        if a:
            subj = a.subject or "Unknown"
            if subj in subject_stats:
                subject_stats[subj]["submissions"] += 1
                if s.marks_obtained is not None:
                    subject_stats[subj]["marks_sum"] += s.marks_obtained
                    subject_stats[subj]["marks_count"] += 1

    subject_report = []
    for subj, sdata in subject_stats.items():
        avg_marks = round(sdata["marks_sum"] / sdata["marks_count"], 1) if sdata["marks_count"] > 0 else 0.0
        subject_report.append({
            "subject": subj,
            "assignments_count": sdata["total"],
            "submissions_count": sdata["submissions"],
            "average_marks": avg_marks,
        })

    student_stats: dict[str, dict] = {}
    for s in my_subs:
        student_stats.setdefault(s.student_id, {"submitted": 0, "graded": 0, "total_marks": 0.0, "possible_marks": 0.0})
        student_stats[s.student_id]["submitted"] += 1
        if s.status == "Evaluated" and s.marks_obtained is not None:
            a = asg_by_id.get(s.assignment_id)
            student_stats[s.student_id]["graded"] += 1
            student_stats[s.student_id]["total_marks"] += s.marks_obtained
            student_stats[s.student_id]["possible_marks"] += (a.total_marks if a and a.total_marks else 0)

    student_report = []
    for student_id, stats in student_stats.items():
        name_res = await db.execute(select(Student, User).join(User, Student.user_id == User.id).where(Student.id == student_id))
        row = name_res.first()
        score_percentage = round((stats["total_marks"] / stats["possible_marks"]) * 100, 1) if stats["possible_marks"] > 0 else 0.0
        student_report.append({
            "student_name": row[1].full_name if row else None,
            "register_number": row[0].roll_no if row else None,
            "submitted_count": stats["submitted"],
            "graded_count": stats["graded"],
            "performance_percentage": score_percentage,
        })

    return {
        "summary": {
            "total_assignments": total_assignments,
            "published": published_count,
            "closed": closed_count,
            "draft": draft_count,
            "total_submissions": total_submitted,
            "late_submissions": late_submissions,
            "pending_evaluations": pending_evals,
        },
        "subject_wise": subject_report,
        "student_performance": student_report,
    }


# ─── Student Endpoints ─────────────────────────────────────────────────────

@router.get("/active-assignments", response_model=list[AssignmentResponse])
async def get_active_assignments(
    skip: int = 0,
    limit: int = 20,
    current_user: User = Depends(role_required([UserRole.STUDENT])),
    db: AsyncSession = Depends(get_db_session),
):
    student_repo = StudentRepository(db)
    student = await student_repo.get_student_by_user_id(current_user.id)
    if not student:
        raise HTTPException(status_code=404, detail="Student profile not found")

    asg_res = await db.execute(
        select(Assignment).where(
            Assignment.status.in_(["Published", "Closed"]),
            Assignment.is_deleted.is_(False),
        )
    )
    assignments = asg_res.scalars().all()
    if student.section_id:
        assignments = [a for a in assignments if a.section_id == student.section_id]

    assignments = sorted(assignments, key=lambda a: a.deadline)
    assignments = assignments[skip: skip + limit]

    faculty_ids = {a.faculty_id for a in assignments}
    faculty_map: dict[str, str] = {}
    if faculty_ids:
        fac_res = await db.execute(select(User).where(User.id.in_(faculty_ids)))
        faculty_map = {f.id: f.full_name for f in fac_res.scalars().all()}

    result = []
    for a in assignments:
        sub_res = await db.execute(
            select(AssignmentSubmission).where(
                AssignmentSubmission.assignment_id == a.id,
                AssignmentSubmission.student_id == student.id,
                AssignmentSubmission.is_deleted.is_(False),
            )
        )
        sub = sub_res.scalar_one_or_none()
        my_submission = None
        if sub:
            my_submission = {
                "id": sub.id,
                "submitted_file_url": sub.submitted_file_url,
                "submitted_text": sub.submitted_text,
                "status": sub.status,
                "marks_obtained": sub.marks_obtained,
                "grade": sub.grade,
                "feedback": sub.feedback,
                "submitted_at": sub.submitted_at.isoformat(),
            }
        result.append(_asg_to_response(a, my_submission=my_submission, faculty_name=faculty_map.get(a.faculty_id)))

    return sorted(result, key=lambda x: x.deadline or "", reverse=False)


@router.post("/{asg_id}/upload-submission")
async def upload_submission_file(
    asg_id: str,
    file: UploadFile = File(...),
    current_user: User = Depends(role_required([UserRole.STUDENT])),
    db: AsyncSession = Depends(get_db_session),
):
    """Upload a file to attach to an assignment submission. Returns a
    file_url the client references in POST /assignments/submit/{asg_id}."""
    asg_res = await db.execute(select(Assignment).where(Assignment.id == asg_id, Assignment.is_deleted.is_(False)))
    if not asg_res.scalar_one_or_none():
        raise HTTPException(status_code=404, detail="Assignment not found")

    os.makedirs(UPLOADS_DIR, exist_ok=True)
    original_filename = file.filename or "submission"
    safe_filename = f"sub_{asg_id}_{current_user.id}_{uuid.uuid4().hex}_{original_filename.replace(' ', '_')}"
    full_path = os.path.join(UPLOADS_DIR, safe_filename)

    with open(full_path, "wb") as f:
        f.write(await file.read())

    return {"file_url": f"/api/v1/files/assignments/{safe_filename}", "filename": original_filename}


@router.post("/submit/{asg_id}", response_model=SubmissionResponse)
async def student_submit_assignment(
    asg_id: str,
    payload: StudentSubmitRequest,
    current_user: User = Depends(role_required([UserRole.STUDENT])),
    db: AsyncSession = Depends(get_db_session),
):
    student_repo = StudentRepository(db)
    student = await student_repo.get_student_by_user_id(current_user.id)
    if not student:
        raise HTTPException(status_code=404, detail="Student profile not found")

    asg_res = await db.execute(select(Assignment).where(Assignment.id == asg_id, Assignment.is_deleted.is_(False)))
    assignment = asg_res.scalar_one_or_none()
    if not assignment:
        raise HTTPException(status_code=404, detail="Assignment not found")
    if assignment.status in ("Closed", "Archived"):
        raise HTTPException(status_code=400, detail="Assignment is closed for submissions")

    now = datetime.now(timezone.utc)
    deadline_dt = datetime.combine(assignment.deadline, datetime.min.time(), tzinfo=timezone.utc) if assignment.deadline else now
    sub_status = "Submitted" if now <= deadline_dt else "Late Submission"

    existing_res = await db.execute(
        select(AssignmentSubmission).where(
            AssignmentSubmission.assignment_id == asg_id,
            AssignmentSubmission.student_id == student.id,
            AssignmentSubmission.is_deleted.is_(False),
        )
    )
    submission = existing_res.scalar_one_or_none()

    if submission:
        submission.submitted_file_url = payload.file_url
        submission.submitted_text = payload.submitted_text
        submission.status = sub_status
        submission.submitted_at = now
        submission.marks_obtained = None
        submission.grade = None
        submission.feedback = None
        submission.remarks = None
    else:
        submission = AssignmentSubmission(
            assignment_id=asg_id,
            student_id=student.id,
            submitted_file_url=payload.file_url,
            submitted_text=payload.submitted_text,
            status=sub_status,
            submitted_at=now,
        )
        db.add(submission)
        assignment.submission_count = (assignment.submission_count or 0) + 1

    await db.commit()
    await db.refresh(submission)

    return SubmissionResponse(
        id=submission.id,
        assignment_id=submission.assignment_id,
        student_id=submission.student_id,
        submitted_file_url=submission.submitted_file_url,
        submitted_text=submission.submitted_text,
        marks_obtained=submission.marks_obtained,
        grade=submission.grade,
        feedback=submission.feedback,
        remarks=submission.remarks,
        status=submission.status,
        submitted_at=submission.submitted_at,
        student_name=current_user.full_name,
        register_number=student.roll_no,
    )
