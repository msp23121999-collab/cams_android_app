import uuid
from typing import List, Optional
from fastapi import APIRouter, Depends, HTTPException
from sqlalchemy.ext.asyncio import AsyncSession
from sqlalchemy import select
from pydantic import BaseModel

from app.db.session import get_db
from app.db.models.marks import InternalMark
from app.db.models.student import Student
from app.db.models.academic import Course, Section, Department
from app.db.models.user import User
from app.core.dependencies import get_current_user, role_required

router = APIRouter()

class InternalMarkRequest(BaseModel):
    student_id: str
    internal_exam_mark: float
    assignment_mark: float
    presentation_mark: float
    viva_voice_mark: float
    attendance_mark: float
    total_mark: float
    status: Optional[str] = "DRAFT"

class SaveInternalMarksPayload(BaseModel):
    section_id: str
    subject_id: str
    academic_year: str
    semester: Optional[str] = None
    marks: List[InternalMarkRequest]

class SubmitMarksPayload(BaseModel):
    section_id: str
    subject_id: str
    academic_year: str

class ApproveMarksPayload(BaseModel):
    section_id: str
    subject_id: str
    academic_year: str

@router.get("/internal")
async def get_internal_marks(
    section_id: str,
    subject_id: str,
    academic_year: Optional[str] = None,
    db: AsyncSession = Depends(get_db)
):
    target_section = await db.get(Section, section_id)
    if not target_section:
        stmt_sec = select(Section).where(
            Section.section_name == section_id,
            Section.course_id == subject_id
        )
        res_sec = await db.execute(stmt_sec)
        target_section = res_sec.scalars().first()
        if not target_section:
            return []
    course = await db.get(Course, subject_id)
    if not course:
        return []

    # Fetch students in this section (matched by name) and department
    sec_stmt = select(Section.id).where(Section.section_name == target_section.section_name)
    sec_res = await db.execute(sec_stmt)
    matching_section_ids = sec_res.scalars().all()

    stmt = (
        select(Student, User)
        .join(User, Student.user_id == User.id)
        .where(
            Student.section_id.in_(matching_section_ids),
            Student.semester == course.semester,
            Student.is_deleted.is_(False)
        )
    )
    # Also filter by department if course has one
    if course.dept_id:
        stmt = stmt.where(Student.department_id == course.dept_id)
    res = await db.execute(stmt)
    students = res.all()
    
    # Fetch existing marks using matching_section_ids
    query = select(InternalMark).where(
        InternalMark.section_id.in_(matching_section_ids),
        InternalMark.subject_id == subject_id
    )
    if academic_year:
        query = query.where(InternalMark.academic_year == academic_year)
    marks_q = await db.execute(query)
    existing_marks = {m.student_id: m for m in marks_q.scalars().all()}
    
    result = []
    for s, u in students:
        em = existing_marks.get(s.id)
        result.append({
            "student_id": s.id,
            "student_name": u.full_name or "Unknown",
            "registration_number": s.roll_no,
            "internal_exam_mark": float(em.internal_exam_mark) if em else 0.0,
            "assignment_mark": float(em.assignment_mark) if em else 0.0,
            "presentation_mark": float(em.presentation_mark) if em else 0.0,
            "viva_voice_mark": float(em.viva_voice_mark) if em else 0.0,
            "attendance_mark": float(em.attendance_mark) if em else 0.0,
            "total_mark": float(em.total_mark) if em else 0.0,
            "status": em.status if em else "NOT ENTERED",
            "hod_message": em.hod_message if em else None,
            "faculty_reply": em.faculty_reply if em else None,
            "is_message_visible_to_student": em.is_message_visible_to_student if em else False
        })
    return result

@router.post("/internal")
async def save_internal_marks(
    payload: SaveInternalMarksPayload,
    db: AsyncSession = Depends(get_db)
):
    # Fetch existing marks
    marks_q = await db.execute(
        select(InternalMark)
        .where(InternalMark.section_id == payload.section_id)
        .where(InternalMark.subject_id == payload.subject_id)
        .where(InternalMark.academic_year == payload.academic_year)
    )
    existing_marks = {m.student_id: m for m in marks_q.scalars().all()}
    
    for m_req in payload.marks:
        if m_req.student_id in existing_marks:
            em = existing_marks[m_req.student_id]
            em.internal_exam_mark = m_req.internal_exam_mark
            em.assignment_mark = m_req.assignment_mark
            em.presentation_mark = m_req.presentation_mark
            em.viva_voice_mark = m_req.viva_voice_mark
            em.attendance_mark = m_req.attendance_mark
            em.total_mark = m_req.total_mark
            em.status = "DRAFT"
        else:
            new_mark = InternalMark(
                id=str(uuid.uuid4()),
                student_id=m_req.student_id,
                section_id=payload.section_id,
                subject_id=payload.subject_id,
                academic_year=payload.academic_year,
                semester=payload.semester,
                internal_exam_mark=m_req.internal_exam_mark,
                assignment_mark=m_req.assignment_mark,
                presentation_mark=m_req.presentation_mark,
                viva_voice_mark=m_req.viva_voice_mark,
                attendance_mark=m_req.attendance_mark,
                total_mark=m_req.total_mark,
                status="DRAFT"
            )
            db.add(new_mark)
    
    await db.commit()
    return {"message": "Marks saved successfully"}

@router.post("/internal/submit")
async def submit_internal_marks(
    payload: SubmitMarksPayload,
    current_user: User = Depends(get_current_user),
    db: AsyncSession = Depends(get_db)
):
    marks_q = await db.execute(
        select(InternalMark)
        .where(InternalMark.section_id == payload.section_id)
        .where(InternalMark.subject_id == payload.subject_id)
        .where(InternalMark.academic_year == payload.academic_year)
    )
    marks = list(marks_q.scalars().all())

    # Ensure all student records for this section have internal mark rows initialized
    target_section = await db.get(Section, payload.section_id)
    if not target_section:
        raise HTTPException(status_code=404, detail="Section not found")
    course = await db.get(Course, payload.subject_id)
    if not course:
        raise HTTPException(status_code=404, detail="Course not found")

    sec_stmt = select(Section.id).where(Section.section_name == target_section.section_name)
    sec_res = await db.execute(sec_stmt)
    matching_section_ids = sec_res.scalars().all()

    students_stmt = (
        select(Student)
        .where(
            Student.section_id.in_(matching_section_ids),
            Student.semester == course.semester,
            Student.is_deleted.is_(False)
        )
    )
    if course.dept_id:
        students_stmt = students_stmt.where(Student.department_id == course.dept_id)
    students_res = await db.execute(students_stmt)
    students = students_res.scalars().all()

    existing_student_ids = {m.student_id for m in marks}
    for s in students:
        if s.id not in existing_student_ids:
            new_mark = InternalMark(
                id=str(uuid.uuid4()),
                student_id=s.id,
                section_id=payload.section_id,
                subject_id=payload.subject_id,
                academic_year=payload.academic_year,
                semester=str(course.semester),
                internal_exam_mark=0.0,
                assignment_mark=0.0,
                presentation_mark=0.0,
                viva_voice_mark=0.0,
                attendance_mark=0.0,
                total_mark=0.0,
                status="SUBMITTED"
            )
            db.add(new_mark)
            marks.append(new_mark)

    for m in marks:
        m.status = "SUBMITTED"
        
    course = await db.get(Course, payload.subject_id)
    if course:
        dept_q = await db.execute(select(Department).where(Department.id == course.dept_id))
        dept = dept_q.scalars().first()
        if dept and dept.hod_id:
            from app.services.notification_service import NotificationService
            notif_service = NotificationService(db)
            await notif_service.send_notification(
                user_id=dept.hod_id,
                type_val="marks_submission",
                message=f"Faculty {current_user.full_name} has submitted internal marks for {course.name} ({payload.academic_year})."
            )

    await db.commit()
    return {"message": "Marks submitted to HOD successfully"}


@router.get("/internal/hod/pending")
async def get_pending_hod_marks(
    current_user: User = Depends(get_current_user),
    db: AsyncSession = Depends(get_db)
):
    # Fetch departments where the user is HOD
    dept_q = await db.execute(select(Department.id).where(Department.hod_id == current_user.id))
    hod_dept_ids = dept_q.scalars().all()

    marks_q = await db.execute(
        select(InternalMark)
        .where(InternalMark.status == "SUBMITTED")
    )
    marks = marks_q.scalars().all()
    
    # Group by section_id and subject_id
    grouped = {}
    for m in marks:
        # fetch course
        course = await db.get(Course, m.subject_id)
        
        # Filter: Only show if course's dept_id matches one of HOD's departments
        # If user has other roles (e.g. admin) we can bypass or allow it. Let's make sure HOD filtering is applied.
        if course and hod_dept_ids and course.dept_id not in hod_dept_ids:
            continue

        key = f"{m.section_id}:{m.subject_id}:{m.academic_year}"
        if key not in grouped:
            section = await db.get(Section, m.section_id)
            grouped[key] = {
                "section_id": m.section_id,
                "subject_id": m.subject_id,
                "academic_year": m.academic_year,
                "semester": m.semester,
                "course_name": course.name if course else "Unknown",
                "section_name": section.section_name if section else "Unknown"
            }
    
    return list(grouped.values())

@router.post("/internal/hod/approve")
async def approve_internal_marks(
    payload: ApproveMarksPayload,
    db: AsyncSession = Depends(get_db)
):
    marks_q = await db.execute(
        select(InternalMark)
        .where(InternalMark.section_id == payload.section_id)
        .where(InternalMark.subject_id == payload.subject_id)
        .where(InternalMark.academic_year == payload.academic_year)
    )
    marks = marks_q.scalars().all()
    if not marks:
        raise HTTPException(status_code=404, detail="No marks found to approve")
        
    for m in marks:
        m.status = "APPROVED"
        
    course = await db.get(Course, payload.subject_id)
    if course:
        from app.services.notification_service import NotificationService
        notif_service = NotificationService(db)
        for m in marks:
            student = await db.get(Student, m.student_id)
            if student and student.user_id:
                await notif_service.send_notification(
                    user_id=student.user_id,
                    type_val="marks_approval",
                    message=f"Your internal marks for {course.name} ({payload.academic_year}) have been approved by the HOD."
                )

    await db.commit()
    return {"message": "Marks approved successfully"}

class HODMarkMessagePayload(BaseModel):
    section_id: str
    subject_id: str
    academic_year: str
    student_id: str
    message: str

@router.post("/internal/hod/message")
async def save_hod_mark_message(
    payload: HODMarkMessagePayload,
    db: AsyncSession = Depends(get_db)
):
    stmt = select(InternalMark).where(
        InternalMark.section_id == payload.section_id,
        InternalMark.subject_id == payload.subject_id,
        InternalMark.academic_year == payload.academic_year,
        InternalMark.student_id == payload.student_id
    )
    res = await db.execute(stmt)
    mark = res.scalars().first()
    if not mark:
        raise HTTPException(status_code=404, detail="Internal mark not found")
    
    mark.hod_message = payload.message
    await db.commit()
    return {"message": "HOD message saved successfully"}

class FacultyMarkMessagePayload(BaseModel):
    section_id: str
    subject_id: str
    academic_year: str
    student_id: str
    reply: str
    is_visible: bool

@router.post("/internal/faculty/message")
async def save_faculty_mark_message(
    payload: FacultyMarkMessagePayload,
    db: AsyncSession = Depends(get_db)
):
    stmt = select(InternalMark).where(
        InternalMark.section_id == payload.section_id,
        InternalMark.subject_id == payload.subject_id,
        InternalMark.academic_year == payload.academic_year,
        InternalMark.student_id == payload.student_id
    )
    res = await db.execute(stmt)
    mark = res.scalars().first()
    if not mark:
        raise HTTPException(status_code=404, detail="Internal mark not found")
    
    mark.faculty_reply = payload.reply
    mark.is_message_visible_to_student = payload.is_visible
    await db.commit()
    return {"message": "Faculty reply saved successfully"}


@router.get("/internal/student/me")
async def get_student_marks(
    current_user: User = Depends(get_current_user),
    db: AsyncSession = Depends(get_db)
):
    student_q = await db.execute(select(Student).where(Student.user_id == current_user.id))
    student = student_q.scalars().first()
    
    if not student:
        raise HTTPException(status_code=404, detail="Student record not found")
        
    # Get all courses for student's degree and current semester
    courses_q = await db.execute(
        select(Course)
        .where(
            Course.degree_id == student.degree_id if student.degree_id else Course.dept_id == student.department_id,
            Course.semester == student.semester,
            Course.is_deleted.is_(False)
        )
    )
    courses = courses_q.scalars().all()
    
    # Get marks for this student
    marks_q = await db.execute(
        select(InternalMark)
        .where(InternalMark.student_id == student.id)
    )
    marks_map = {m.subject_id: m for m in marks_q.scalars().all()}
    
    response_list = []
    for c in courses:
        m = marks_map.get(c.id)
        is_approved = (m.status == "APPROVED") if m else False
        
        response_list.append({
            "subject_id": c.id,
            "subject_name": c.name,
            "academic_year": m.academic_year if m else "2026-2027",
            "semester": str(c.semester),
            "is_approved": is_approved,
            "internal_exam_mark": float(m.internal_exam_mark) if is_approved else 0.0,
            "assignment_mark": float(m.assignment_mark) if is_approved else 0.0,
            "presentation_mark": float(m.presentation_mark) if is_approved else 0.0,
            "viva_voice_mark": float(m.viva_voice_mark) if is_approved else 0.0,
            "attendance_mark": float(m.attendance_mark) if is_approved else 0.0,
            "total_mark": float(m.total_mark) if is_approved else 0.0,
            "hod_message": m.hod_message if m and m.is_message_visible_to_student else None,
            "faculty_reply": m.faculty_reply if m and m.is_message_visible_to_student else None,
        })
        
    return response_list
