import os
import json
import uuid
import datetime
from typing import List, Dict, Any, Optional
from fastapi import APIRouter, Depends, HTTPException, Request, status
from sqlalchemy.ext.asyncio import AsyncSession
from sqlalchemy import select, update, func, and_

from app.core.dependencies import get_db_session, role_required
from app.db.models.user import User, UserRole
from app.db.models.classroom import ClassroomActivity, StudentInteraction, SessionSummary
from app.db.models.study_material import StudyMaterial
from app.db.models.academic import Course, Section, Timetable, Department, Degree, Weekday
from app.schemas.classroom import (
    ClassroomActivityCreate, ClassroomActivityResponse,
    StudentInteractionCreate, StudentInteractionResponse,
    SessionSummaryCreate, SessionSummaryResponse,
    ClassroomReportResponse
)

router = APIRouter()


@router.get("/today-classes", response_model=list[dict])
async def get_today_classes(
    day: Optional[str] = None,
    current_user: User = Depends(role_required([UserRole.FACULTY, UserRole.HOD])),
    db: AsyncSession = Depends(get_db_session)
) -> list[dict]:
    """Fetch timetable items for the requested weekday (defaults to today)."""
    if not day:
        # Get current weekday in uppercase (e.g. MONDAY)
        day = datetime.datetime.now().strftime("%A").upper()
    
    # Validate day with Weekday enum
    try:
        weekday_enum = Weekday[day]
    except KeyError:
        # Default to MONDAY if today is Sunday or invalid
        weekday_enum = Weekday.MONDAY

    # Query timetable for current faculty and weekday
    stmt = (
        select(Timetable)
        .where(
            and_(
                Timetable.faculty_id == current_user.id,
                Timetable.weekday == weekday_enum,
                Timetable.is_deleted.is_(False)
            )
        )
        .order_by(Timetable.start_time)
    )
    result = await db.execute(stmt)
    items = result.scalars().all()

    response_items = []
    for item in items:
        # Fetch Course
        course_q = await db.execute(select(Course).where(Course.id == item.subject_id))
        course = course_q.scalar_one_or_none()
        
        # Fetch Section
        section_q = await db.execute(select(Section).where(Section.id == item.section_id))
        section = section_q.scalar_one_or_none()
        
        course_name = "B.A. LL.B"
        semester = "III"
        academic_year = "2026-2027"
        
        if course:
            semester = str(course.semester)
            if course.dept_id:
                dept_q = await db.execute(select(Department).where(Department.id == course.dept_id))
                dept = dept_q.scalar_one_or_none()
                if dept and dept.course_name:
                    course_name = dept.course_name
            if course.degree_id:
                reg_q = await db.execute(select(Degree).where(Degree.id == course.degree_id))
                reg = reg_q.scalar_one_or_none()
                if reg:
                    academic_year = reg.applicable_batch
        
        response_items.append({
            "id": item.id,
            "subject_code": course.code if course else "LAW101",
            "subject_name": course.name if course else "Law Subject",
            "section_id": item.section_id,
            "section_name": section.section_name if section else "A",
            "room": item.room,
            "weekday": item.weekday.value,
            "start_time": item.start_time.strftime("%H:%M"),
            "end_time": item.end_time.strftime("%H:%M"),
            "course_name": course_name,
            "semester": semester,
            "academic_year": academic_year
        })
    return response_items

@router.get("/resources", response_model=list[dict])
async def get_resources(
    subject: Optional[str] = None,
    current_user: User = Depends(role_required([UserRole.FACULTY, UserRole.HOD])),
    db: AsyncSession = Depends(get_db_session)
) -> list[dict]:
    """Get real Faculty Study Materials from the PostgreSQL database table."""
    # Query study_materials joined with section and course to get the subject name
    stmt = (
        select(StudyMaterial, Course)
        .join(Section, Section.id == StudyMaterial.section_id)
        .join(Course, Course.id == Section.course_id)
        .where(
            and_(
                StudyMaterial.faculty_id == current_user.id,
                StudyMaterial.is_deleted.is_(False)
            )
        )
    )
    
    # Optional filter by subject
    if subject:
        stmt = stmt.where(Course.name.ilike(f"%{subject}%"))
        
    result = await db.execute(stmt)
    rows = result.all()

    response_items = []
    for material, course in rows:
        response_items.append({
            "id": material.id,
            "title": material.title,
            "description": f"Learning resource for {course.name} ({course.code})",
            "category": material.type,  # e.g., "Lecture Notes", "PPT Presentation", "Bare Act Material", "Case Law Material"
            "subject": course.name,
            "metadata": {
                "file_url": material.file_url,
                "file_format": material.file_url.split(".")[-1].upper() if "." in material.file_url else "PDF",
                "is_verified": material.is_verified,
                "subject_code": course.code
            }
        })
        
    return response_items

@router.post("/activities", response_model=ClassroomActivityResponse)
async def create_activity(
    payload: ClassroomActivityCreate,
    current_user: User = Depends(role_required([UserRole.FACULTY, UserRole.HOD])),
    db: AsyncSession = Depends(get_db_session)
) -> ClassroomActivity:
    """Save a new classroom activity log."""
    activity = ClassroomActivity(
        faculty_id=current_user.id,
        section_id=payload.section_id,
        activity_type=payload.activity_type,
        topic=payload.topic,
        duration_minutes=payload.duration_minutes,
        remarks=payload.remarks
    )
    db.add(activity)
    await db.commit()
    await db.refresh(activity)
    return activity

@router.get("/activities", response_model=list[ClassroomActivityResponse])
async def get_activities(
    section_id: Optional[str] = None,
    current_user: User = Depends(role_required([UserRole.FACULTY, UserRole.HOD])),
    db: AsyncSession = Depends(get_db_session)
) -> list[ClassroomActivity]:
    """Fetch past classroom activities for this faculty."""
    conditions = [ClassroomActivity.faculty_id == current_user.id, ClassroomActivity.is_deleted.is_(False)]
    if section_id:
        conditions.append(ClassroomActivity.section_id == section_id)
        
    stmt = select(ClassroomActivity).where(and_(*conditions)).order_by(ClassroomActivity.created_at.desc())
    result = await db.execute(stmt)
    return list(result.scalars().all())

@router.post("/interactions", response_model=StudentInteractionResponse)
async def create_interaction(
    payload: StudentInteractionCreate,
    current_user: User = Depends(role_required([UserRole.FACULTY, UserRole.HOD])),
    db: AsyncSession = Depends(get_db_session)
) -> StudentInteraction:
    """Create a question or a live poll interaction."""
    interaction = StudentInteraction(
        faculty_id=current_user.id,
        section_id=payload.section_id,
        type=payload.type,
        question_text=payload.question_text,
        options=payload.options,
        responses_count=0,
        is_active=True
    )
    db.add(interaction)
    await db.commit()
    await db.refresh(interaction)
    return interaction

@router.get("/interactions", response_model=list[StudentInteractionResponse])
async def get_interactions(
    section_id: Optional[str] = None,
    current_user: User = Depends(role_required([UserRole.FACULTY, UserRole.HOD])),
    db: AsyncSession = Depends(get_db_session)
) -> list[StudentInteraction]:
    """Fetch student interactions (active/inactive)."""
    conditions = [StudentInteraction.faculty_id == current_user.id, StudentInteraction.is_deleted.is_(False)]
    if section_id:
        conditions.append(StudentInteraction.section_id == section_id)
        
    stmt = select(StudentInteraction).where(and_(*conditions)).order_by(StudentInteraction.created_at.desc())
    result = await db.execute(stmt)
    return list(result.scalars().all())

@router.post("/interactions/{interaction_id}/vote", response_model=StudentInteractionResponse)
async def vote_interaction(
    interaction_id: str,
    current_user: User = Depends(role_required([UserRole.FACULTY, UserRole.HOD, UserRole.STUDENT])),
    db: AsyncSession = Depends(get_db_session)
) -> StudentInteraction:
    """Submit a vote/response to a live poll or question, incrementing the count."""
    stmt = select(StudentInteraction).where(
        and_(
            StudentInteraction.id == interaction_id,
            StudentInteraction.is_deleted.is_(False)
        )
    )
    result = await db.execute(stmt)
    interaction = result.scalar_one_or_none()
    
    if not interaction:
        raise HTTPException(status_code=404, detail="Interaction not found")
        
    if not interaction.is_active:
        raise HTTPException(status_code=400, detail="Interaction is no longer active")
        
    interaction.responses_count += 1
    db.add(interaction)
    await db.commit()
    await db.refresh(interaction)
    return interaction

@router.post("/session-summaries", response_model=SessionSummaryResponse)
async def create_session_summary(
    payload: SessionSummaryCreate,
    current_user: User = Depends(role_required([UserRole.FACULTY, UserRole.HOD])),
    db: AsyncSession = Depends(get_db_session)
) -> SessionSummary:
    """Submit a class session summary log."""
    summary = SessionSummary(
        faculty_id=current_user.id,
        section_id=payload.section_id,
        subject_code=payload.subject_code,
        topic_covered=payload.topic_covered,
        subtopic_covered=payload.subtopic_covered,
        teaching_method=payload.teaching_method,
        resources_used=payload.resources_used,
        remarks=payload.remarks,
        date=datetime.date.today()
    )
    db.add(summary)
    await db.commit()
    await db.refresh(summary)
    return summary

@router.get("/session-summaries", response_model=list[SessionSummaryResponse])
async def get_session_summaries(
    section_id: Optional[str] = None,
    current_user: User = Depends(role_required([UserRole.FACULTY, UserRole.HOD])),
    db: AsyncSession = Depends(get_db_session)
) -> list[SessionSummary]:
    """Fetch past session summaries for this faculty."""
    conditions = [SessionSummary.faculty_id == current_user.id, SessionSummary.is_deleted.is_(False)]
    if section_id:
        conditions.append(SessionSummary.section_id == section_id)
        
    stmt = select(SessionSummary).where(and_(*conditions)).order_by(SessionSummary.date.desc(), SessionSummary.created_at.desc())
    result = await db.execute(stmt)
    return list(result.scalars().all())

@router.get("/reports", response_model=ClassroomReportResponse)
async def get_classroom_reports(
    current_user: User = Depends(role_required([UserRole.FACULTY, UserRole.HOD])),
    db: AsyncSession = Depends(get_db_session)
) -> dict:
    """Fetch comprehensive activity, interaction, and summary data for generating reports."""
    # 1. Activities
    act_stmt = select(ClassroomActivity).where(
        and_(ClassroomActivity.faculty_id == current_user.id, ClassroomActivity.is_deleted.is_(False))
    ).order_by(ClassroomActivity.created_at.desc())
    act_result = await db.execute(act_stmt)
    activities = act_result.scalars().all()

    # 2. Interactions
    int_stmt = select(StudentInteraction).where(
        and_(StudentInteraction.faculty_id == current_user.id, StudentInteraction.is_deleted.is_(False))
    ).order_by(StudentInteraction.created_at.desc())
    int_result = await db.execute(int_stmt)
    interactions = int_result.scalars().all()

    # 3. Summaries
    sum_stmt = select(SessionSummary).where(
        and_(SessionSummary.faculty_id == current_user.id, SessionSummary.is_deleted.is_(False))
    ).order_by(SessionSummary.date.desc(), SessionSummary.created_at.desc())
    sum_result = await db.execute(sum_stmt)
    summaries = sum_result.scalars().all()

    return {
        "activities": list(activities),
        "interactions": list(interactions),
        "summaries": list(summaries)
    }
