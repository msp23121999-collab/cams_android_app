import os
import json
import uuid
import shutil
import difflib
from datetime import datetime, date, time
from typing import List, Dict, Any, Optional
from fastapi import APIRouter, Depends, HTTPException, Request, status, UploadFile, File
from pydantic import BaseModel

from app.core.dependencies import get_current_user, role_required, get_db_session
from app.db.models.user import User, UserRole
from app.db.models.academic import Course, Section, Timetable, Department, Degree, Weekday, SubjectAllocation
from app.db.models.attendance import Attendance
from app.db.models.student import Student
from sqlalchemy.ext.asyncio import AsyncSession
from sqlalchemy import select, and_, func

router = APIRouter()

from app.core.json_db_helper import load_db_from_postgres, save_db_to_postgres

DB_FILE = os.path.join(os.path.dirname(os.path.dirname(os.path.dirname(os.path.dirname(__file__)))), "db.json")

# Helper to load/save JSON database
def load_db() -> Dict[str, Any]:
    return load_db_from_postgres(
        DB_FILE,
        lambda: {
            "class_diaries": {},
            "academic_activities": [],
            "notifications": [],
            "audit_logs": [],
            "lesson_plans": {},
            "subject_allocations": [],
            "timetable": [],
            "attendance_mock": {}
        }
    )

def save_db(data: Dict[str, Any]) -> None:
    save_db_to_postgres(DB_FILE, data)


def match_subject(sub1: str, sub2: str) -> bool:
    if not sub1 or not sub2:
        return False
        
    def get_suffix_num(sub: str) -> str:
        cleaned = sub.replace("(", " ").replace(")", " ").replace("-", " ")
        tokens = [t.strip().upper() for t in cleaned.split() if t.strip()]
        if not tokens:
            return ""
        last_token = tokens[-1]
        roman_numerals = {"I", "II", "III", "IV", "V", "VI", "VII", "VIII", "IX", "X"}
        if last_token in roman_numerals or last_token.isdigit():
            return last_token
        if len(tokens) >= 2:
            prev_token = tokens[-2]
            if prev_token in roman_numerals or prev_token.isdigit():
                return prev_token
        return ""

    suffix1 = get_suffix_num(sub1)
    suffix2 = get_suffix_num(sub2)
    if suffix1 != suffix2:
        return False

    s1 = "".join(c for c in sub1.lower() if c.isalnum())
    s2 = "".join(c for c in sub2.lower() if c.isalnum())
    if s1 == s2:
        return True
    ratio = difflib.SequenceMatcher(None, s1, s2).ratio()
    if ratio >= 0.85:
        return True
    if s1 in s2 or s2 in s1:
        return True
    return False

def is_topic_covered(d: Dict[str, Any], subject: str, unit_name: str, topic_name: str) -> bool:
    d_sub = d.get("subject")
    if not d_sub or not match_subject(d_sub, subject):
        return False
    
    d_unit = d.get("unit")
    if not d_unit:
        return False

    # ── Unit Prefix Matching ─────────────────────────────────────────────────────
    def unit_id(s: str) -> str:
        """Extract normalised unit number, e.g. 'unit i', 'unit iv' from full key."""
        s = s.strip().lower()
        # Take everything before the first colon
        colon_idx = s.find(":")
        if colon_idx > 0:
            s = s[:colon_idx].strip()
        return s

    plan_uid = unit_id(unit_name)
    diary_uids = [unit_id(u) for u in d_unit.split(",") if u.strip()]
    if plan_uid not in diary_uids:
        return False
        
    t_clean = topic_name.strip().lower()
    
    # ── Topic Matching ───────────────────────────────────────────────────────────
    # Both topic and subtopic fields in the diary may be comma-separated lists of
    # the lesson plan topics the faculty selected (e.g. "Topic A, Topic B").
    # We split both on commas and compare each item using exact match first,
    # then similarity ratio.
    def items_from(raw: str) -> list:
        return [x.strip().lower() for x in (raw or "").split(",") if x.strip()]

    diary_topics = items_from(d.get("topic", ""))
    diary_subtopics = items_from(d.get("subtopic", ""))
    all_diary_items = diary_subtopics if diary_subtopics else diary_topics

    for item in all_diary_items:
        if t_clean == item:
            return True
        ratio = difflib.SequenceMatcher(None, t_clean, item).ratio()
        if ratio >= 0.82:
            return True
        # Substring only if both are long enough to avoid false positives
        if len(t_clean) > 12 and len(item) > 12 and (t_clean in item or item in t_clean):
            return True

    # Fallback: check against full topic field (useful for free-text entries)
    for item in diary_topics:
        if t_clean == item:
            return True
        if len(t_clean) > 12 and len(item) > 12 and t_clean in item:
            return True

    return False

# Schemas
class DiaryEntryInput(BaseModel):
    id: Optional[str] = None
    date: str
    subject: str
    course: str
    semester: str
    section: str
    hour: str
    year: Optional[str] = ""
    unit: str
    topic: str
    subtopic: str
    teaching_method: str
    learning_outcome: str
    class_activity: str
    remarks: str
    status: str  # "Draft" or "Submitted"
    deviation_reason: Optional[str] = ""
    revised_date: Optional[str] = ""
    attachment_url: Optional[str] = ""
    attachment_name: Optional[str] = ""
    completion_status: Optional[str] = "Completed"

class ActivityInput(BaseModel):
    activity_type: str
    topic: str
    duration: float
    date: str

# Endpoints
@router.get("/dashboard")
async def get_dashboard(
    current_user: User = Depends(get_current_user),
    db_session: AsyncSession = Depends(get_db_session)
):
    db = load_db()
    faculty_id = current_user.id
    diaries = [d for d in db["class_diaries"].values() if d["faculty_id"] == faculty_id]
    
    # Calculate stats
    conducted_today = sum(1 for d in diaries if d["date"] == datetime.now().strftime("%Y-%m-%d") and d["status"] == "Submitted")
    
    this_month_prefix = datetime.now().strftime("%Y-%m")
    conducted_this_month = sum(1 for d in diaries if d["date"].startswith(this_month_prefix) and d["status"] == "Submitted")
    
    # Pending diaries
    pending_entries = 0
    
    # Subjects Assigned count
    subjects_stmt = select(func.count(Timetable.subject_id.distinct())).where(
        and_(
            Timetable.faculty_id == current_user.id,
            Timetable.is_deleted.is_(False)
        )
    )
    subjects_res = await db_session.execute(subjects_stmt)
    subjects_count = subjects_res.scalar_one() or 0
    
    # Fetch today's classes from DB
    day = datetime.now().strftime("%A").upper()
    try:
        weekday_enum = Weekday[day]
    except KeyError:
        weekday_enum = Weekday.MONDAY

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
    result = await db_session.execute(stmt)
    items = result.scalars().all()

    upcoming_classes = []
    for idx, item in enumerate(items):
        # Fetch Course
        course_q = await db_session.execute(select(Course).where(Course.id == item.subject_id))
        course = course_q.scalar_one_or_none()
        
        # Fetch Section
        section_q = await db_session.execute(select(Section).where(Section.id == item.section_id))
        section = section_q.scalar_one_or_none()
        
        subject_name = course.name if course else "Law Subject"
        start_hour = item.start_time.hour
        if start_hour == 9:
            hour = "Hour 1"
        elif start_hour == 10:
            hour = "Hour 2"
        elif start_hour == 11:
            hour = "Hour 3"
        elif start_hour == 12:
            hour = "Hour 4"
        elif start_hour == 14:
            hour = "Hour 5"
        elif start_hour == 15:
            hour = "Hour 6"
        else:
            hour = f"Hour {idx + 1}"
        time_str = f"{item.start_time.strftime('%H:%M')} - {item.end_time.strftime('%H:%M')}"
        
        upcoming_classes.append({
            "hour": hour,
            "subject": subject_name,
            "time": time_str,
            "room": item.room,
            "section": section.section_name if section else "A"
        })

    # Get all distinct subjects from faculty timetable
    distinct_subjects_stmt = select(Course).join(Timetable, Timetable.subject_id == Course.id).where(
        and_(
            Timetable.faculty_id == current_user.id,
            Timetable.is_deleted.is_(False)
        )
    ).distinct()
    distinct_subjects_res = await db_session.execute(distinct_subjects_stmt)
    faculty_courses = distinct_subjects_res.scalars().all()

    subject_progress = []
    lp = db["lesson_plans"]
    
    total_pct = 0.0
    for course in faculty_courses:
        course_name = course.name
        # Find if lesson plan exists
        plan = lp.get(course_name) or lp.get(course_name.replace("Law of ", "").replace("Constitutional ", "Constitutional Law")) # simple fuzzy matching
        
        # If no plan, check if we can match any key
        if not plan:
            for k in lp.keys():
                if match_subject(k, course_name):
                    plan = lp[k]
                    course_name = k
                    break
        
        if plan:
            plan_total = 0
            plan_done = 0
            for unit_name, topics in plan.items():
                for t in topics:
                    plan_total += 1
                    is_covered = any(
                        is_topic_covered(d, course_name, unit_name, t)
                        for d in diaries
                    )
                    if is_covered:
                        plan_done += 1
            pct = round((plan_done / plan_total) * 100, 1) if plan_total > 0 else 0.0
            
            # Find current unit (the first unit that has remaining topics)
            current_unit = "Completed"
            for unit_name, topics in plan.items():
                completed_topics_in_unit = []
                for t in topics:
                    is_covered = any(
                        is_topic_covered(d, course_name, unit_name, t)
                        for d in diaries
                    )
                    if is_covered:
                        completed_topics_in_unit.append(t)
                if len(completed_topics_in_unit) < len(topics):
                    current_unit = unit_name
                    break
        else:
            pct = 0.0
            current_unit = "None"
            
        subject_progress.append({
            "subject": course.name,
            "completion": pct,
            "current_unit": current_unit
        })
        total_pct += pct
        
    avg_completion = round(total_pct / len(faculty_courses), 1) if faculty_courses else 0.0
    
    return {
        "metrics": {
            "conducted_today": conducted_today,
            "conducted_this_month": conducted_this_month,
            "pending_entries": pending_entries,
            "subjects_assigned": subjects_count,
            "average_syllabus_completion": avg_completion
        },
        "subject_progress": subject_progress,
        "recent_activities": sorted(diaries, key=lambda x: x.get("created_at", ""), reverse=True)[:5],
        "upcoming_classes": upcoming_classes
    }

@router.get("/today-classes")
async def get_today_classes(
    current_user: User = Depends(get_current_user),
    db: AsyncSession = Depends(get_db_session)
):
    # Get current weekday in uppercase (e.g. MONDAY)
    day = datetime.now().strftime("%A").upper()
    try:
        weekday_enum = Weekday[day]
    except KeyError:
        weekday_enum = Weekday.MONDAY

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

    # Load diaries to check status
    teaching_db = load_db()
    diaries = teaching_db.get("class_diaries", {}).values()
    today_str = datetime.now().strftime("%Y-%m-%d")

    results = []
    for idx, item in enumerate(items):
        # Fetch Course
        course_q = await db.execute(select(Course).where(Course.id == item.subject_id))
        course = course_q.scalar_one_or_none()
        
        # Fetch Section
        section_q = await db.execute(select(Section).where(Section.id == item.section_id))
        section = section_q.scalar_one_or_none()
        
        course_name = "B.A. LL.B"
        semester = "III"
        subject_name = "Law Subject"
        
        if course:
            subject_name = course.name
            semester = str(course.semester)
            if course.dept_id:
                dept_q = await db.execute(select(Department).where(Department.id == course.dept_id))
                dept = dept_q.scalar_one_or_none()
                if dept and dept.course_name:
                    course_name = dept.course_name

        start_hour = item.start_time.hour
        if start_hour == 9:
            hour = "Hour 1"
        elif start_hour == 10:
            hour = "Hour 2"
        elif start_hour == 11:
            hour = "Hour 3"
        elif start_hour == 12:
            hour = "Hour 4"
        elif start_hour == 14:
            hour = "Hour 5"
        elif start_hour == 15:
            hour = "Hour 6"
        else:
            hour = f"Hour {idx + 1}"
        time_str = f"{item.start_time.strftime('%H:%M')} - {item.end_time.strftime('%H:%M')}"
        
        # Check if diary submitted for today
        submitted = any(
            match_subject(d.get("subject"), subject_name) and 
            d.get("hour") == hour and 
            d.get("date") == today_str and 
            d.get("status") == "Submitted" and
            d.get("faculty_id") == current_user.id
            for d in diaries
        )
        draft = any(
            match_subject(d.get("subject"), subject_name) and 
            d.get("hour") == hour and 
            d.get("date") == today_str and 
            d.get("status") == "Draft" and
            d.get("faculty_id") == current_user.id
            for d in diaries
        )
        
        status = "Pending"
        if submitted:
            status = "Submitted"
        elif draft:
            status = "Draft"

        results.append({
            "id": item.id,
            "weekday": item.weekday.value,
            "hour": hour,
            "time": time_str,
            "subject": subject_name,
            "course": course_name,
            "semester": semester,
            "section": section.section_name if section else "A",
            "section_id": item.section_id,
            "subject_id": item.subject_id,
            "room": item.room,
            "faculty_id": item.faculty_id,
            "status": status,
            "date": today_str
        })
    return results

@router.get("/attendance-summary/{section_id}/{subject_id}")
async def get_attendance_summary(
    section_id: str,
    subject_id: str,
    db: AsyncSession = Depends(get_db_session)
):
    course = await db.get(Course, subject_id)
    if not course:
        raise HTTPException(status_code=404, detail="Course not found")

    # Get students belonging to that section
    stmt_s = select(Student).where(
        and_(
            Student.section_id == section_id,
            Student.is_deleted.is_(False)
        )
    )
    res_s = await db.execute(stmt_s)
    students = res_s.scalars().all()
    total_students_count = len(students)

    # Query attendance records for this section and subject
    stmt_att = select(Attendance).where(
        and_(
            Attendance.section_id == section_id,
            Attendance.subject_id == subject_id,
            Attendance.is_deleted.is_(False)
        )
    )
    res_att = await db.execute(stmt_att)
    records = res_att.scalars().all()

    total_records = len(records)
    if total_records == 0 or total_students_count == 0:
        return {"total": total_students_count, "present": total_students_count, "absent": 0, "percentage": 100.0}

    total_possible = total_students_count * total_records
    total_absent = sum(len(r.absentee_ids or []) for r in records)
    total_od = sum(len(r.od_ids or []) for r in records)
    total_present = total_possible - total_absent - total_od

    # Present counts include Presents and ODs
    present_count = total_present + total_od
    absent_count = total_absent
    percentage = round((present_count / total_possible) * 100, 2)

    return {
        "total": total_students_count,
        "present": present_count,
        "absent": absent_count,
        "percentage": percentage
    }

async def auto_register_study_material(
    payload: DiaryEntryInput,
    faculty_id: str,
    db_session: AsyncSession
) -> None:
    if payload.status == "Submitted" and payload.attachment_url:
        from app.db.models.study_material import StudyMaterial
        from app.db.models.academic import Section, Course
        from app.db.models.audit import AuditLog
        
        # Check if this file URL is already registered for this faculty & subject to avoid duplicates
        existing_q = select(StudyMaterial).where(
            and_(
                StudyMaterial.faculty_id == faculty_id,
                StudyMaterial.file_url == payload.attachment_url,
                StudyMaterial.is_deleted.is_(False)
            )
        )
        existing_res = await db_session.execute(existing_q)
        if existing_res.scalars().first():
            return
            
        # Find matching Section for the StudyMaterial:
        sec_q = (
            select(Section)
            .join(Course, Section.course_id == Course.id)
            .where(
                and_(
                    Course.name == payload.subject,
                    Section.section_name == payload.section
                )
            )
        )
        res = await db_session.execute(sec_q)
        section = res.scalars().first()
        if not section:
            # Fallback 1: match by subject name only
            sec_q2 = select(Section).join(Course, Section.course_id == Course.id).where(Course.name == payload.subject)
            res2 = await db_session.execute(sec_q2)
            section = res2.scalars().first()
            if not section:
                # Fallback 2: grab any section
                res_f = await db_session.execute(select(Section))
                section = res_f.scalars().first()
        
        if section:
            # Determine file type
            ext = (payload.attachment_name or "").split(".")[-1].lower()
            doc_type = "Lecture Notes"
            if ext in ["pdf"]:
                doc_type = "PDF Handout"
            elif ext in ["ppt", "pptx"]:
                doc_type = "Presentation (PPT)"
            elif ext in ["doc", "docx"]:
                doc_type = "Word Document"
                
            new_material = StudyMaterial(
                section_id=section.id,
                faculty_id=faculty_id,
                title=f"{payload.subject} - {payload.topic} (Class Handout)",
                type=doc_type,
                file_url=payload.attachment_url,
                is_verified=True,
                status="APPROVED",
                comments=f"Auto-generated from Class Diary Entry: {payload.topic}"
            )
            db_session.add(new_material)
            
            # Log to AuditLog
            audit_entry = AuditLog(
                user_id=faculty_id,
                action="SUBMITTED",
                entity="StudyMaterial",
                entity_id=new_material.id,
                timestamp=datetime.now()
            )
            db_session.add(audit_entry)
            await db_session.commit()

@router.post("/diaries")
async def create_diary_entry(
    payload: DiaryEntryInput,
    current_user: User = Depends(get_current_user),
    db_session: AsyncSession = Depends(get_db_session)
):
    db = load_db()
    faculty_id = current_user.id
    
    # Prevent creating/submitting if a log has already been submitted for this class
    existing_diaries = db.get("class_diaries", {}).values()
    already_submitted = any(
        match_subject(d.get("subject"), payload.subject) and
        d.get("hour") == payload.hour and
        d.get("date") == payload.date and
        d.get("section") == payload.section and
        d.get("status") == "Submitted" and
        d.get("faculty_id") == faculty_id
        for d in existing_diaries
    )
    if already_submitted and current_user.role == UserRole.FACULTY:
        raise HTTPException(status_code=400, detail="A class log has already been submitted for this class and cannot be modified.")

    entry_id = payload.id if payload.id else f"diary_{uuid.uuid4().hex}"
    
    now_str = datetime.now().isoformat()
    
    entry = {
        "id": entry_id,
        "faculty_id": faculty_id,
        "faculty_name": current_user.full_name,
        "date": payload.date,
        "subject": payload.subject,
        "course": payload.course,
        "semester": payload.semester,
        "section": payload.section,
        "hour": payload.hour,
        "year": payload.year or "",
        "unit": payload.unit,
        "topic": payload.topic,
        "subtopic": payload.subtopic,
        "teaching_method": payload.teaching_method,
        "learning_outcome": payload.learning_outcome,
        "class_activity": payload.class_activity,
        "remarks": payload.remarks,
        "status": payload.status,
        "deviation_reason": payload.deviation_reason or "",
        "revised_date": payload.revised_date or "",
        "attachment_url": payload.attachment_url or "",
        "attachment_name": payload.attachment_name or "",
        "completion_status": payload.completion_status or "Completed",
        "created_at": now_str,
        "updated_at": now_str
    }
    
    db["class_diaries"][entry_id] = entry
    
    # Audit log
    db["audit_logs"].append({
        "id": f"audit_{uuid.uuid4().hex}",
        "user": current_user.full_name,
        "role": current_user.role.value,
        "action": f"Diary {'Created' if payload.status == 'Submitted' else 'Saved Draft'}",
        "timestamp": now_str,
        "ip_address": "127.0.0.1",
        "remarks": f"Logged class for {payload.subject} ({payload.date})"
    })
    
    save_db(db)
    await auto_register_study_material(payload, faculty_id, db_session)
    return entry

def parse_semester_to_int(sem: Optional[str]) -> Optional[int]:
    if not sem:
        return None
    sem_clean = sem.strip().upper()
    if not sem_clean or sem_clean == "ALL" or sem_clean == "ALL SEMESTERS":
        return None
    
    roman_map = {"I": 1, "II": 2, "III": 3, "IV": 4, "V": 5, "VI": 6, "VII": 7, "VIII": 8, "IX": 9, "X": 10}
    if sem_clean in roman_map:
        return roman_map[sem_clean]
        
    import re
    match = re.search(r'\d+', sem_clean)
    if match:
        return int(match.group())
        
    return None

async def get_scope_filters(
    current_user: User,
    db_session: AsyncSession,
    department_id: Optional[str] = None,
    semester: Optional[str] = None,
    section: Optional[str] = None
):
    course_query = select(Course).outerjoin(Degree, Course.degree_id == Degree.id).where(Course.is_deleted.is_(False))
    section_filter = None
    if section and section.strip() and section.lower() != "all" and section.lower() != "all sections":
        section_filter = section.strip()

    sem_int = parse_semester_to_int(semester)

    # Get active semesters from academic_years
    from app.db.models.academic import AcademicYear
    ay_query = select(AcademicYear.current_semester).where(
        AcademicYear.is_active == True,
        AcademicYear.is_deleted == False
    )
    ay_res = await db_session.execute(ay_query)
    active_semesters = [r for r in ay_res.scalars().all() if r is not None]

    if current_user.role in [UserRole.PRINCIPAL, UserRole.SUPER_ADMIN, UserRole.ADMIN]:
        if department_id and department_id.strip() and department_id.lower() != "all" and department_id.lower() != "all departments":
            from sqlalchemy import or_
            course_query = course_query.where(or_(Course.dept_id == department_id, Degree.dept_id == department_id))
        if sem_int is not None:
            course_query = course_query.where(Course.semester == sem_int)
        else:
            if active_semesters:
                course_query = course_query.where(Course.semester.in_(active_semesters))
            
    elif current_user.role == UserRole.HOD:
        if not current_user.department_id:
            raise HTTPException(status_code=400, detail="HOD not assigned to a department")
        from sqlalchemy import or_
        course_query = course_query.where(or_(Course.dept_id == current_user.department_id, Degree.dept_id == current_user.department_id))
        if sem_int is not None:
            course_query = course_query.where(Course.semester == sem_int)
        else:
            if active_semesters:
                course_query = course_query.where(Course.semester.in_(active_semesters))
            
    elif current_user.role in [UserRole.STUDENT, UserRole.PARENT]:
        student = None
        if current_user.role == UserRole.STUDENT:
            student_q = await db_session.execute(select(Student).where(Student.user_id == current_user.id, Student.is_deleted.is_(False)))
            student = student_q.scalar_one_or_none()
        else: # Parent
            from app.db.models.student import ParentStudentMap
            pm_q = await db_session.execute(select(ParentStudentMap).where(ParentStudentMap.parent_id == current_user.id, ParentStudentMap.is_deleted.is_(False)))
            pm = pm_q.scalars().first()
            if pm:
                student_q = await db_session.execute(select(Student).where(Student.id == pm.student_id, Student.is_deleted.is_(False)))
                student = student_q.scalar_one_or_none()
        
        if not student:
            raise HTTPException(status_code=404, detail="Student profile not found")
        
        course_query = course_query.where(Course.semester == student.semester)
        if student.degree_id:
            course_query = course_query.where(Course.degree_id == student.degree_id)
        else:
            course_query = course_query.where(Course.dept_id == student.department_id)
            

            
        # Student section name
        section_name = "A"
        if student.section_id:
            sec_q = await db_session.execute(select(Section).where(Section.id == student.section_id))
            sec = sec_q.scalar_one_or_none()
            if sec:
                section_name = sec.section_name
        section_filter = section_name
        
    elif current_user.role == UserRole.FACULTY:
        distinct_subjects_stmt = select(Course).join(Timetable, Timetable.subject_id == Course.id).where(
            and_(
                Timetable.faculty_id == current_user.id,
                Timetable.is_deleted.is_(False)
            )
        ).distinct()
        distinct_subjects_res = await db_session.execute(distinct_subjects_stmt)
        faculty_courses = distinct_subjects_res.scalars().all()
        course_ids = [c.id for c in faculty_courses]
        if course_ids:
            course_query = course_query.where(Course.id.in_(course_ids))
        else:
            course_query = course_query.where(Course.id == 'non-existent')

    course_res = await db_session.execute(course_query)
    courses = course_res.scalars().all()
    return courses, section_filter

@router.get("/diaries")
async def list_diaries(
    current_user: User = Depends(get_current_user),
    db_session: AsyncSession = Depends(get_db_session),
    subject: Optional[str] = None,
    unit: Optional[str] = None,
    status: Optional[str] = None,
    department_id: Optional[str] = None,
    semester: Optional[str] = None,
    section: Optional[str] = None
):
    db = load_db()
    diaries = list(db["class_diaries"].values())
    
    courses, section_filter = await get_scope_filters(
        current_user, db_session, department_id, semester, section
    )
    course_names = {c.name.lower().strip() for c in courses}
    
    apply_scope = True
    if current_user.role in [UserRole.PRINCIPAL, UserRole.SUPER_ADMIN, UserRole.ADMIN] and not department_id and not semester:
        apply_scope = False
        
    if apply_scope:
        diaries = [d for d in diaries if any(match_subject(d.get("subject"), cname) for cname in course_names)]
        
    if section_filter:
        diaries = [d for d in diaries if d.get("section", "").lower().strip() == section_filter.lower().strip()]
        
    if subject:
        diaries = [d for d in diaries if match_subject(d.get("subject"), subject)]
    if unit:
        diaries = [d for d in diaries if d.get("unit", "").lower() == unit.lower()]
    if status:
        diaries = [d for d in diaries if d.get("status", "").lower() == status.lower()]
    if semester and semester.lower() != "all" and semester.lower() != "all semesters":
        sem_int = parse_semester_to_int(semester)
        diaries = [d for d in diaries if d.get("semester", "").strip() == str(semester).strip() or (sem_int is not None and parse_semester_to_int(d.get("semester")) == sem_int)]
    else:
        # Only show current semesters
        from app.db.models.academic import AcademicYear
        ay_query = select(AcademicYear.current_semester).where(
            AcademicYear.is_active == True,
            AcademicYear.is_deleted == False
        )
        ay_res = await db_session.execute(ay_query)
        active_semesters = [r for r in ay_res.scalars().all() if r is not None]
        if active_semesters:
            diaries = [d for d in diaries if parse_semester_to_int(d.get("semester")) in active_semesters]
    if section and section.lower() != "all" and section.lower() != "all sections":
        diaries = [d for d in diaries if d.get("section", "").lower().strip() == section.lower().strip()]
        
    return sorted(diaries, key=lambda x: x.get("date", ""), reverse=True)

@router.put("/diaries/{id}")
async def update_diary_entry(
    id: str,
    payload: DiaryEntryInput,
    current_user: User = Depends(get_current_user),
    db_session: AsyncSession = Depends(get_db_session)
):
    db = load_db()
    
    if id not in db["class_diaries"]:
        raise HTTPException(status_code=404, detail="Diary entry not found")
        
    entry = db["class_diaries"][id]
    
    # Check correction window limit for submitted entries
    if entry["status"] == "Submitted" and current_user.role == UserRole.FACULTY:
        raise HTTPException(status_code=400, detail="Submitted class logs are final and cannot be edited.")
                
    now_str = datetime.now().isoformat()
    entry.update({
        "unit": payload.unit,
        "topic": payload.topic,
        "subtopic": payload.subtopic,
        "teaching_method": payload.teaching_method,
        "learning_outcome": payload.learning_outcome,
        "class_activity": payload.class_activity,
        "remarks": payload.remarks,
        "status": payload.status,
        "deviation_reason": payload.deviation_reason or entry.get("deviation_reason", ""),
        "revised_date": payload.revised_date or entry.get("revised_date", ""),
        "attachment_url": payload.attachment_url or entry.get("attachment_url", ""),
        "attachment_name": payload.attachment_name or entry.get("attachment_name", ""),
        "year": payload.year or entry.get("year", ""),
        "completion_status": payload.completion_status or entry.get("completion_status", "Completed"),
        "updated_at": now_str
    })
    
    db["class_diaries"][id] = entry
    
    # Audit log
    db["audit_logs"].append({
        "id": f"audit_{uuid.uuid4().hex}",
        "user": current_user.full_name,
        "role": current_user.role.value,
        "action": "Diary Updated",
        "timestamp": now_str,
        "ip_address": "127.0.0.1",
        "remarks": f"Updated class diary for {entry['subject']} ({entry['date']})"
    })
    
    save_db(db)
    await auto_register_study_material(payload, current_user.id, db_session)
    return entry

@router.get("/syllabus-progress")
async def get_syllabus_progress(
    current_user: User = Depends(get_current_user),
    db_session: AsyncSession = Depends(get_db_session),
    department_id: Optional[str] = None,
    semester: Optional[str] = None,
    section: Optional[str] = None,
    subject: Optional[str] = None
):
    db = load_db()
    
    courses, section_filter = await get_scope_filters(
        current_user, db_session, department_id, semester, section
    )
    course_names_lower = {c.name.lower().strip(): c for c in courses}
    
    diaries = [d for d in db["class_diaries"].values() if d["status"] == "Submitted"]
    
    apply_scope = True
    if current_user.role in [UserRole.PRINCIPAL, UserRole.SUPER_ADMIN, UserRole.ADMIN] and not department_id and not semester:
        apply_scope = False
        
    if apply_scope:
        diaries = [d for d in diaries if any(match_subject(d.get("subject"), name_lower) for name_lower in course_names_lower)]
        
    if section_filter:
        diaries = [d for d in diaries if d.get("section", "").lower().strip() == section_filter.lower().strip()]
        
    if semester and semester.lower() != "all" and semester.lower() != "all semesters":
        sem_int = parse_semester_to_int(semester)
        diaries = [d for d in diaries if d.get("semester", "").strip() == str(semester).strip() or (sem_int is not None and parse_semester_to_int(d.get("semester")) == sem_int)]
    else:
        # Only show current semesters
        from app.db.models.academic import AcademicYear
        ay_query = select(AcademicYear.current_semester).where(
            AcademicYear.is_active == True,
            AcademicYear.is_deleted == False
        )
        ay_res = await db_session.execute(ay_query)
        active_semesters = [r for r in ay_res.scalars().all() if r is not None]
        if active_semesters:
            diaries = [d for d in diaries if parse_semester_to_int(d.get("semester")) in active_semesters]

    # Calculate days remaining in 90-day academic cycle from active academic year
    from app.db.models.academic import AcademicYear
    days_remaining = 90
    try:
        ay_query = select(AcademicYear).where(AcademicYear.is_active == True, AcademicYear.is_deleted == False)
        ay_result = await db_session.execute(ay_query)
        active_ay = ay_result.scalars().first()
        if active_ay:
            elapsed = (datetime.now().date() - active_ay.start_date).days
            days_remaining = max(0, 90 - elapsed)
    except Exception as e:
        print(f"Error querying academic year for syllabus progress: {e}")
        
    lp = db["lesson_plans"]
    
    progress = {}
    for sub, units in lp.items():
        matched_course = None
        for name_lower, c in course_names_lower.items():
            if match_subject(sub, c.name):
                matched_course = c
                break
        if not matched_course and apply_scope:
            continue
            
        if subject and not match_subject(sub, subject):
            continue
            
        sub_progress = []
        total_topics = 0
        total_completed = 0
        
        for unit_name, topics in units.items():
            completed_topics_in_unit = []
            topics_details = []
            
            for t in topics:
                is_covered = any(
                    is_topic_covered(d, sub, unit_name, t)
                    for d in diaries
                )
                if is_covered:
                    completed_topics_in_unit.append(t)
                
                actual_hours = sum(
                    1 for d in diaries if 
                    is_topic_covered(d, sub, unit_name, t)
                )
                
                accountable_hours = 3
                if "equality" in t.lower() or "rights" in t.lower() or "contract" in t.lower():
                    accountable_hours = 4
                elif "intro" in t.lower() or "history" in t.lower():
                    accountable_hours = 2
                    
                topics_details.append({
                    "topic_name": t,
                    "actual_hours": actual_hours,
                    "accountable_hours": accountable_hours,
                    "is_covered": actual_hours > 0
                })
            
            unit_total = len(topics)
            unit_done = len(completed_topics_in_unit)
            unit_pct = round((unit_done / unit_total) * 100, 1) if unit_total > 0 else 0
            
            total_topics += unit_total
            total_completed += unit_done
            
            sub_progress.append({
                "unit": unit_name,
                "topics_planned": unit_total,
                "topics_covered": unit_done,
                "progress_percentage": unit_pct,
                "completed_topics": completed_topics_in_unit,
                "remaining_topics": [t for t in topics if t not in completed_topics_in_unit],
                "topics_details": topics_details
            })
            
        course_sem = None
        if matched_course:
            course_sem = matched_course.semester
        else:
            try:
                c_q = await db_session.execute(select(Course.semester).where(Course.name == sub, Course.is_deleted.is_(False)))
                course_sem = c_q.scalar()
            except Exception:
                pass

        progress[sub] = {
            "overall_completion": round((total_completed / total_topics) * 100, 1) if total_topics > 0 else 0,
            "total_units": len(units),
            "completed_units": sum(1 for up in sub_progress if up["progress_percentage"] == 100),
            "units_progress": sub_progress,
            "days_remaining": days_remaining,
            "semester": course_sem
        }

        
    return progress

class SaveLessonPlanInput(BaseModel):
    subject: str
    units: Dict[str, List[str]]

@router.get("/lesson-plans")
async def get_all_lesson_plans(current_user: User = Depends(get_current_user)):
    db = load_db()
    return db.get("lesson_plans", {})

@router.post("/lesson-plans")
async def save_lesson_plan(
    payload: SaveLessonPlanInput,
    current_user: User = Depends(get_current_user),
    db_session: AsyncSession = Depends(get_db_session)
):
    if current_user.role == UserRole.FACULTY:
        try:
            # Get course names from SubjectAllocation
            alloc_stmt = select(Course.name).join(SubjectAllocation, SubjectAllocation.course_id == Course.id).where(
                SubjectAllocation.faculty_id == current_user.id,
                SubjectAllocation.is_deleted.is_(False)
            )
            alloc_res = await db_session.execute(alloc_stmt)
            allocated_names = set(alloc_res.scalars().all())

            # Get course names from Timetable
            tt_stmt = select(Course.name).join(Timetable, Timetable.subject_id == Course.id).where(
                Timetable.faculty_id == current_user.id,
                Timetable.is_deleted.is_(False)
            )
            tt_res = await db_session.execute(tt_stmt)
            tt_allocated_names = set(tt_res.scalars().all())

            all_allocated = allocated_names.union(tt_allocated_names)
            
            print(f"DEBUG Save Lesson Plan: User={current_user.full_name} ({current_user.id}), Allocated={all_allocated}, Requested={payload.subject}")

            is_allocated = False
            for name in all_allocated:
                if match_subject(name, payload.subject):
                    is_allocated = True
                    break

            if not is_allocated:
                raise HTTPException(
                    status_code=status.HTTP_403_FORBIDDEN,
                    detail="You can only configure lesson plans for your allocated subjects."
                )
        except HTTPException:
            raise
        except Exception as e:
            raise HTTPException(
                status_code=status.HTTP_500_INTERNAL_SERVER_ERROR,
                detail=f"Validation failed: {str(e)}"
            )

    db = load_db()
    db["lesson_plans"][payload.subject] = payload.units
    save_db(db)
    return {"status": "success", "lesson_plan": db["lesson_plans"][payload.subject]}

@router.get("/lesson-plan-tracking")
async def get_lesson_plan_tracking(
    current_user: User = Depends(get_current_user),
    db_session: AsyncSession = Depends(get_db_session),
    department_id: Optional[str] = None,
    semester: Optional[str] = None,
    section: Optional[str] = None,
    subject: Optional[str] = None
):
    db = load_db()
    
    courses, section_filter = await get_scope_filters(
        current_user, db_session, department_id, semester, section
    )
    course_names_lower = {c.name.lower().strip(): c for c in courses}
    
    diaries = [d for d in db["class_diaries"].values() if d["status"] == "Submitted"]
    
    apply_scope = True
    if current_user.role in [UserRole.PRINCIPAL, UserRole.SUPER_ADMIN, UserRole.ADMIN] and not department_id and not semester:
        apply_scope = False
        
    if apply_scope:
        diaries = [d for d in diaries if any(match_subject(d.get("subject"), name_lower) for name_lower in course_names_lower)]
        
    if section_filter:
        diaries = [d for d in diaries if d.get("section", "").lower().strip() == section_filter.lower().strip()]
        
    if semester and semester.lower() != "all" and semester.lower() != "all semesters":
        sem_int = parse_semester_to_int(semester)
        diaries = [d for d in diaries if d.get("semester", "").strip() == str(semester).strip() or (sem_int is not None and parse_semester_to_int(d.get("semester")) == sem_int)]
    else:
        # Only show current semesters
        from app.db.models.academic import AcademicYear
        ay_query = select(AcademicYear.current_semester).where(
            AcademicYear.is_active == True,
            AcademicYear.is_deleted == False
        )
        ay_res = await db_session.execute(ay_query)
        active_semesters = [r for r in ay_res.scalars().all() if r is not None]
        if active_semesters:
            diaries = [d for d in diaries if parse_semester_to_int(d.get("semester")) in active_semesters]

    lp = db["lesson_plans"]
    tracking = []
    
    for sub, units in lp.items():
        matched_course = None
        for name_lower, c in course_names_lower.items():
            if match_subject(sub, c.name):
                matched_course = c
                break
        if not matched_course and apply_scope:
            continue
            
        if subject and not match_subject(sub, subject):
            continue
            
        all_planned_topics = []
        for u_name, topics in units.items():
            for t in topics:
                all_planned_topics.append({"unit": u_name, "topic": t})
                
        sub_diaries = sorted([d for d in diaries if match_subject(d["subject"], sub)], key=lambda x: x["date"])
        
        for idx, p_topic in enumerate(all_planned_topics):
            covering_diary = next((d for d in sub_diaries if is_topic_covered(d, sub, p_topic["unit"], p_topic["topic"])), None)
            
            status = "Pending"
            actual_topic = ""
            date_taught = ""
            deviation = False
            deviation_reason = ""
            
            if covering_diary:
                status = "On Schedule"
                actual_topic = covering_diary.get("subtopic") or covering_diary.get("topic", "")
                date_taught = covering_diary["date"]
            
            tracking.append({
                "subject": sub,
                "unit": p_topic["unit"],
                "planned_topic": p_topic["topic"],
                "actual_topic": actual_topic,
                "date_taught": date_taught,
                "status": status,
                "deviation": deviation,
                "deviation_reason": deviation_reason
            })
            
    return tracking

@router.post("/activities")
async def log_activity(payload: ActivityInput, current_user: User = Depends(get_current_user)):
    db = load_db()
    faculty_id = current_user.id
    
    now_str = datetime.now().isoformat()
    
    activity = {
        "id": f"act_{uuid.uuid4().hex}",
        "faculty_id": faculty_id,
        "faculty_name": current_user.full_name,
        "activity_type": payload.activity_type,
        "topic": payload.topic,
        "duration": payload.duration,
        "date": payload.date,
        "created_at": now_str
    }
    
    db["academic_activities"].append(activity)
    
    db["audit_logs"].append({
        "id": f"audit_{uuid.uuid4().hex}",
        "user": current_user.full_name,
        "role": current_user.role.value,
        "action": "Activity Logged",
        "timestamp": now_str,
        "ip_address": "127.0.0.1",
        "remarks": f"Logged {payload.activity_type} on {payload.date}"
    })
    
    save_db(db)
    return activity

@router.get("/activities")
async def list_activities(current_user: User = Depends(get_current_user)):
    db = load_db()
    faculty_id = current_user.id
    
    activities = db["academic_activities"]
    if current_user.role == UserRole.FACULTY:
        activities = [a for a in activities if a["faculty_id"] == faculty_id]
        
    return sorted(activities, key=lambda x: x["date"], reverse=True)

@router.get("/pending-entries")
async def get_pending_entries(
    current_user: User = Depends(get_current_user),
    db_session: AsyncSession = Depends(get_db_session)
):
    from datetime import timedelta
    db = load_db()
    diaries = list(db["class_diaries"].values())
    
    stmt = select(Timetable).where(Timetable.is_deleted.is_(False))
    res = await db_session.execute(stmt)
    slots = res.scalars().all()
    
    course_res = await db_session.execute(select(Course).where(Course.is_deleted.is_(False)))
    courses = {c.id: c for c in course_res.scalars().all()}
    
    section_res = await db_session.execute(select(Section).where(Section.is_deleted.is_(False)))
    sections = {s.id: s for s in section_res.scalars().all()}
    
    user_res = await db_session.execute(select(User).where(User.is_active == True))
    users = {u.id: u for u in user_res.scalars().all()}
    
    today = datetime.now().date()
    start_of_week = today - timedelta(days=today.weekday())
    
    pending = []
    for i in range(5):
        day_date = start_of_week + timedelta(days=i)
        if day_date > today:
            break
        weekday_name = day_date.strftime("%A").upper()
        day_slots = [s for s in slots if s.weekday.value == weekday_name]
        
        for slot in day_slots:
            course = courses.get(slot.subject_id)
            section = sections.get(slot.section_id)
            fac = users.get(slot.faculty_id)
            if not course or not section or not fac:
                continue
                
            date_str = day_date.strftime("%Y-%m-%d")
            has_diary = False
            for d in diaries:
                if d.get("date") == date_str:
                    d_sub = d.get("subject", "").lower().strip()
                    c_name = course.name.lower().strip()
                    if d_sub == c_name or c_name in d_sub or d_sub in c_name:
                        d_sec = d.get("section", "").lower().replace("section", "").strip()
                        c_sec = section.section_name.lower().replace("section", "").strip()
                        if d_sec == c_sec:
                            if d.get("faculty_id") == slot.faculty_id or d.get("faculty_name", "").lower() == fac.full_name.lower():
                                has_diary = True
                                break
            
            if not has_diary:
                hour_str = f"Hour {slot.start_time.strftime('%H:%M')}"
                if slot.start_time.hour == 9:
                    hour_str = "Hour 1"
                elif slot.start_time.hour == 10:
                    hour_str = "Hour 2"
                elif slot.start_time.hour == 11:
                    hour_str = "Hour 3"
                elif slot.start_time.hour == 12:
                    hour_str = "Hour 4"
                elif slot.start_time.hour == 14:
                    hour_str = "Hour 5"
                
                pending.append({
                    "subject": course.name,
                    "department_id": course.dept_id,
                    "section": section.section_name,
                    "hour": hour_str,
                    "date": date_str,
                    "faculty_name": fac.full_name
                })
                
    return pending

@router.get("/notifications")
async def get_notifications(current_user: User = Depends(get_current_user)):
    db = load_db()
    faculty_id = current_user.id
    notifs = [n for n in db.get("notifications", []) if n["faculty_id"] == faculty_id]
    return sorted(notifs, key=lambda x: x["date"], reverse=True)

@router.post("/notifications/read/{id}")
async def mark_notification_read(id: str):
    db = load_db()
    for n in db.get("notifications", []):
        if n["id"] == id:
            n["is_read"] = True
            break
    save_db(db)
    return {"detail": "Notification read"}

@router.get("/audit-logs")
async def get_audit_logs(current_user: User = Depends(role_required([UserRole.FACULTY, UserRole.HOD, UserRole.PRINCIPAL]))):
    db = load_db()
    return sorted(db.get("audit_logs", []), key=lambda x: x["timestamp"], reverse=True)

@router.post("/upload-file")
async def upload_diary_file(
    file: UploadFile = File(...),
    current_user: User = Depends(get_current_user)
):
    static_dir = os.path.join(os.path.dirname(os.path.dirname(os.path.dirname(os.path.dirname(__file__)))), "static")
    uploads_dir = os.path.join(static_dir, "uploads")
    os.makedirs(uploads_dir, exist_ok=True)
    
    original_filename = file.filename or "file"
    safe_filename = f"diary_{uuid.uuid4().hex}_{original_filename.replace(' ', '_')}"
    file_path = os.path.join(uploads_dir, safe_filename)
    
    with open(file_path, "wb") as buffer:
        shutil.copyfileobj(file.file, buffer)
    return {"file_url": f"/mock-uploads/{safe_filename}", "filename": file.filename}

# Distinct HOD and Principal visibility scopes
@router.get("/hod/dashboard")
async def get_hod_dashboard(
    current_user: User = Depends(role_required([UserRole.HOD, UserRole.PRINCIPAL])),
    db_session: AsyncSession = Depends(get_db_session),
    semester: Optional[str] = None,
    section: Optional[str] = None
):
    db = load_db()
    
    dept_id = current_user.department_id
    if not dept_id and current_user.role == UserRole.PRINCIPAL:
        from app.db.models.academic import Department
        depts_q = await db_session.execute(select(Department).where(Department.is_deleted == False))
        dept = depts_q.scalars().first()
        dept_id = dept.id if dept else None
        
    if not dept_id:
        raise HTTPException(status_code=400, detail="Department context missing")
        
    from sqlalchemy import or_
    course_query = (
        select(Course)
        .outerjoin(Degree, Course.degree_id == Degree.id)
        .where(
            Course.is_deleted.is_(False),
            or_(Course.dept_id == dept_id, Degree.dept_id == dept_id)
        )
    )
    sem_int = parse_semester_to_int(semester)
    
    # Get active semesters from academic_years
    from app.db.models.academic import AcademicYear
    ay_query = select(AcademicYear.current_semester).where(
        AcademicYear.is_active == True,
        AcademicYear.is_deleted == False
    )
    ay_res = await db_session.execute(ay_query)
    active_semesters = [r for r in ay_res.scalars().all() if r is not None]

    if sem_int is not None:
        course_query = course_query.where(Course.semester == sem_int)
    else:
        if active_semesters:
            course_query = course_query.where(Course.semester.in_(active_semesters))

    course_res = await db_session.execute(course_query)
    filtered_courses = course_res.scalars().all()
    filtered_course_names = {c.name.lower().strip(): c for c in filtered_courses}
    
    diaries = list(db["class_diaries"].values())
    submitted_diaries = [d for d in diaries if d["status"] == "Submitted"]
    
    # Filter diaries by courses in this department
    dept_diaries = []
    for d in submitted_diaries:
        d_sub = d.get("subject", "").lower().strip()
        matched = False
        for name_lower, c in filtered_course_names.items():
            if match_subject(d_sub, c.name):
                matched = True
                break
        if matched:
            dept_diaries.append(d)
            
    if section and section.lower() != "all" and section.lower() != "all sections":
        dept_diaries = [d for d in dept_diaries if d.get("section", "").lower().strip() == section.lower().strip()]
        
    if semester and semester.lower() != "all" and semester.lower() != "all semesters":
        dept_diaries = [d for d in dept_diaries if d.get("semester", "").strip() == str(semester).strip() or (sem_int is not None and parse_semester_to_int(d.get("semester")) == sem_int)]
    else:
        if active_semesters:
            dept_diaries = [d for d in dept_diaries if parse_semester_to_int(d.get("semester")) in active_semesters]
        
    lp = db["lesson_plans"]
    subjects_stats = []
    for sub, units in lp.items():
        matched_course = None
        for name_lower, c in filtered_course_names.items():
            if match_subject(sub, c.name):
                matched_course = c
                break
        if not matched_course:
            continue
            
        total_topics = 0
        done_topics = 0
        for unit_name, topics in units.items():
            for t in topics:
                total_topics += 1
                if any(is_topic_covered(d, sub, unit_name, t) for d in dept_diaries):
                    done_topics += 1
        pct = round((done_topics / total_topics) * 100, 1) if total_topics > 0 else 0
        
        subjects_stats.append({
            "subject": sub,
            "completion": pct,
            "faculty": "Faculty User"
        })
        
    return {
        "faculty_activities": sorted(dept_diaries, key=lambda x: x.get("created_at", ""), reverse=True)[:10],
        "syllabus_status": subjects_stats,
        "pending_diaries_count": 2,
        "total_lectures_conducted": len(dept_diaries)
    }

@router.get("/principal/dashboard")
async def get_principal_dashboard(
    current_user: User = Depends(role_required([UserRole.PRINCIPAL, UserRole.SUPER_ADMIN])),
    db_session: AsyncSession = Depends(get_db_session),
    department_id: Optional[str] = None,
    semester: Optional[str] = None,
    section: Optional[str] = None
):
    db = load_db()
    diaries = list(db["class_diaries"].values())
    
    from app.db.models.academic import Department
    depts_q = await db_session.execute(select(Department).where(Department.is_deleted == False))
    depts = depts_q.scalars().all()
    
    # Load all degrees to map degree_id to dept_id for courses without direct dept_id
    degrees_res = await db_session.execute(select(Degree).where(Degree.is_deleted == False))
    degrees_map = {d.id: d.dept_id for d in degrees_res.scalars().all()}
    
    def get_course_dept_id(c: Course) -> Optional[str]:
        if c.dept_id:
            return c.dept_id
        if c.degree_id:
            return degrees_map.get(c.degree_id)
        return None
    
    if department_id and department_id.strip() and department_id.lower() != "all" and department_id.lower() != "all departments":
        target_depts = [dept for dept in depts if dept.id == department_id]
    else:
        target_depts = depts
        
    from sqlalchemy import or_
    course_query = select(Course).outerjoin(Degree, Course.degree_id == Degree.id).where(Course.is_deleted.is_(False))
    if department_id and department_id.strip() and department_id.lower() != "all" and department_id.lower() != "all departments":
        course_query = course_query.where(or_(Course.dept_id == department_id, Degree.dept_id == department_id))
    sem_int = parse_semester_to_int(semester)
    
    # Get active semesters from academic_years
    from app.db.models.academic import AcademicYear
    ay_query = select(AcademicYear.current_semester).where(
        AcademicYear.is_active == True,
        AcademicYear.is_deleted == False
    )
    ay_res = await db_session.execute(ay_query)
    active_semesters = [r for r in ay_res.scalars().all() if r is not None]

    if sem_int is not None:
        course_query = course_query.where(Course.semester == sem_int)
    else:
        if active_semesters:
            course_query = course_query.where(Course.semester.in_(active_semesters))

    course_res = await db_session.execute(course_query)
    filtered_courses = course_res.scalars().all()
    filtered_course_names = {c.name.lower().strip(): c for c in filtered_courses}
    
    submitted_diaries = [d for d in diaries if d["status"] == "Submitted"]
    
    scoped_diaries = []
    for d in submitted_diaries:
        d_sub = d.get("subject", "").lower().strip()
        matched = False
        for name_lower, c in filtered_course_names.items():
            if match_subject(d_sub, c.name):
                matched = True
                break
        if matched:
            scoped_diaries.append(d)
            
    if section and section.lower() != "all" and section.lower() != "all sections":
        scoped_diaries = [d for d in scoped_diaries if d.get("section", "").lower().strip() == section.lower().strip()]
        
    if semester and semester.lower() != "all" and semester.lower() != "all semesters":
        scoped_diaries = [d for d in scoped_diaries if d.get("semester", "").strip() == str(semester).strip() or (sem_int is not None and parse_semester_to_int(d.get("semester")) == sem_int)]
    else:
        if active_semesters:
            scoped_diaries = [d for d in scoped_diaries if parse_semester_to_int(d.get("semester")) in active_semesters]

    lp = db["lesson_plans"]
    total_planned_topics = 0
    total_completed_topics = 0
    
    for sub, units in lp.items():
        matched_course = None
        for name_lower, c in filtered_course_names.items():
            if match_subject(sub, c.name):
                matched_course = c
                break
        if not matched_course and (department_id or semester):
            continue
            
        for unit_name, topics in units.items():
            for t in topics:
                total_planned_topics += 1
                is_covered = any(is_topic_covered(d, sub, unit_name, t) for d in scoped_diaries)
                if is_covered:
                    total_completed_topics += 1
                    
    avg_completion = round((total_completed_topics / total_planned_topics) * 100, 1) if total_planned_topics > 0 else 0
    
    dept_stats = []
    for dept in depts:
        dept_courses = [c for c in filtered_courses if get_course_dept_id(c) == dept.id]
        dept_course_names = {c.name.lower().strip() for c in dept_courses}
        
        dept_diaries = []
        for d in scoped_diaries:
            d_sub = d.get("subject", "").lower().strip()
            matched = False
            for c_name in dept_course_names:
                if match_subject(d_sub, c_name):
                    matched = True
                    break
            if matched:
                dept_diaries.append(d)
                
        dept_planned = 0
        dept_completed = 0
        for sub, units in lp.items():
            matched_c = None
            for c in dept_courses:
                if match_subject(sub, c.name):
                    matched_c = c
                    break
            if not matched_c:
                continue
            for unit_name, topics in units.items():
                for t in topics:
                    dept_planned += 1
                    is_covered = any(is_topic_covered(d, sub, unit_name, t) for d in dept_diaries)
                    if is_covered:
                        dept_completed += 1
        
        dept_comp_rate = round((dept_completed / dept_planned) * 100, 1) if dept_planned > 0 else 0
        active_fac = len({d["faculty_id"] for d in dept_diaries})
        if active_fac == 0:
            active_fac = 2 if dept.code == 'L2' else 1
            
        dept_stats.append({
            "department": dept.name,
            "completion": dept_comp_rate,
            "total_classes": len(dept_diaries),
            "active_faculty": active_fac
        })
        
    faculty_performance = []
    fac_diaries_map = {}
    for d in scoped_diaries:
        fid = d.get("faculty_id")
        fname = d.get("faculty_name", "Faculty User")
        if fid not in fac_diaries_map:
            fac_diaries_map[fid] = {"name": fname, "diaries": []}
        fac_diaries_map[fid]["diaries"].append(d)
        
    for fid, fac_info in fac_diaries_map.items():
        fname = fac_info["name"]
        fdiaries = fac_info["diaries"]
        
        fac_dept = "Unknown"
        for d in fdiaries:
            d_sub = d.get("subject", "").lower().strip()
            for c in filtered_courses:
                if match_subject(d_sub, c.name):
                    dept_obj = next((dp for dp in depts if dp.id == get_course_dept_id(c)), None)
                    if dept_obj:
                        fac_dept = dept_obj.name
                    break
            if fac_dept != "Unknown":
                break
                
        classes_logged = len(fdiaries)
        compliance_pct = 95
        if classes_logged < 5:
            compliance_pct = 80
        if classes_logged == 0:
            compliance_pct = 0
            
        faculty_performance.append({
            "faculty": fname,
            "department": fac_dept,
            "classes_logged": classes_logged,
            "compliance_rate": f"{compliance_pct}%"
        })
        
    if not faculty_performance:
        for dept in target_depts:
            faculty_performance.append({
                "faculty": f"Faculty {dept.code}",
                "department": dept.name,
                "classes_logged": len(scoped_diaries),
                "compliance_rate": "95%"
            })
            
    return {
        "avg_college_completion": avg_completion,
        "department_progress": dept_stats,
        "faculty_performance": faculty_performance,
        "recent_logs": sorted(scoped_diaries, key=lambda x: x.get("date", ""), reverse=True)[:10]
    }


# ── HOD Syllabus Management Endpoints ───────────────────────────────────────

class HODSyllabusMetadataResponse(BaseModel):
    department_name: str
    sem_count: int

class HODCourseResponse(BaseModel):
    id: str
    code: str
    name: str
    credits: int
    semester: int

class HODLessonPlanSaveInput(BaseModel):
    units: Dict[str, List[str]]

@router.get("/hod/syllabus/metadata", response_model=HODSyllabusMetadataResponse)
async def get_hod_syllabus_metadata(
    current_user: User = Depends(role_required([UserRole.HOD])),
    db: AsyncSession = Depends(get_db_session)
):
    if not current_user.department_id:
        raise HTTPException(status_code=400, detail="HOD not assigned to a department")
    
    dept = await db.get(Department, current_user.department_id)
    if not dept:
        raise HTTPException(status_code=404, detail="Department not found")
        
    return HODSyllabusMetadataResponse(
        department_name=dept.name,
        sem_count=dept.sem_count or 10
    )

@router.get("/hod/syllabus/courses", response_model=List[HODCourseResponse])
async def get_hod_syllabus_courses(
    current_user: User = Depends(role_required([UserRole.HOD])),
    db: AsyncSession = Depends(get_db_session)
):
    if not current_user.department_id:
        raise HTTPException(status_code=400, detail="HOD not assigned to a department")
        
    stmt = select(Course).where(
        Course.dept_id == current_user.department_id,
        Course.is_deleted.is_(False)
    )
    res = await db.execute(stmt)
    courses = res.scalars().all()
    
    seen_codes = set()
    unique_courses = []
    for c in courses:
        # Normalize code to prevent case/spacing mismatches
        code_norm = c.code.strip().lower()
        if code_norm not in seen_codes:
            seen_codes.add(code_norm)
            unique_courses.append(c)
            
    return [
        HODCourseResponse(
            id=c.id,
            code=c.code,
            name=c.name,
            credits=c.credits,
            semester=c.semester
        )
        for c in unique_courses
    ]

@router.get("/hod/syllabus/courses/{course_name}/plan", response_model=Dict[str, List[str]])
async def get_hod_course_plan(
    course_name: str,
    current_user: User = Depends(role_required([UserRole.HOD, UserRole.PRINCIPAL, UserRole.SUPER_ADMIN, UserRole.ADMIN]))
):
    db = load_db()
    lp = db.get("lesson_plans", {})
    
    # Try exact match or fuzzy match
    plan = lp.get(course_name)
    if not plan:
        for k in lp.keys():
            if match_subject(k, course_name):
                plan = lp[k]
                break
                
    return plan or {}

@router.post("/hod/syllabus/courses/{course_name}/plan")
async def save_hod_course_plan(
    course_name: str,
    payload: HODLessonPlanSaveInput,
    current_user: User = Depends(role_required([UserRole.HOD]))
):
    db = load_db()
    if "lesson_plans" not in db:
        db["lesson_plans"] = {}
        
    # Find matching key to update if exists, otherwise create new
    target_key = course_name
    for k in db["lesson_plans"].keys():
        if match_subject(k, course_name):
            target_key = k
            break
            
    db["lesson_plans"][target_key] = payload.units
    save_db(db)
    return {"status": "success", "plan": db["lesson_plans"][target_key]}

