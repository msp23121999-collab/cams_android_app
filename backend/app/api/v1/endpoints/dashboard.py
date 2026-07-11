import os
import json
from datetime import date, datetime, time as dt_time
from typing import List, Dict, Any
from fastapi import APIRouter, Depends
from sqlalchemy.ext.asyncio import AsyncSession
from sqlalchemy import select, func, text

from app.core.dependencies import get_db_session, role_required
from app.db.models.user import User, UserRole
from app.db.models.academic import Department, Course, Section, Timetable
from app.db.models.leave import LeaveRequest
from app.db.models.study_material import StudyMaterial, Assignment
from app.db.models.faculty import FacultyWorkload, FacultyResearch
from app.db.models.substitution import SubstitutionAllocation, FacultyAbsence

router = APIRouter()

from app.core.json_db_helper import load_db_from_postgres

DB_FILE = os.path.join(os.path.dirname(os.path.dirname(os.path.dirname(os.path.dirname(__file__)))), "db.json")



@router.get("/overview")
async def get_hod_dashboard_overview(
    dept_id: str | None = None,
    semester: str | None = None,
    section: str | None = None,
    current_user: User = Depends(role_required([UserRole.HOD, UserRole.PRINCIPAL])),
    db: AsyncSession = Depends(get_db_session)
):
    target_dept_id = dept_id
    if current_user.role == UserRole.HOD:
        target_dept_id = current_user.department_id

    today = date.today()

    # ─── 1. OVERVIEW ────────────────────────────────────────────────────────────
    from app.api.v1.endpoints.teaching_logs import parse_semester_to_int
    sem_int = parse_semester_to_int(semester)

    # Get active semesters from academic_years
    from app.db.models.academic import AcademicYear
    ay_query = select(AcademicYear.current_semester).where(
        AcademicYear.is_active == True,
        AcademicYear.is_deleted == False
    )
    ay_res = await db.execute(ay_query)
    active_semesters = [r for r in ay_res.scalars().all() if r is not None]

    if not target_dept_id:
        student_query_str = "SELECT id FROM students WHERE is_deleted = false"
        student_params = {}
    else:
        student_query_str = "SELECT id FROM students WHERE department_id = :dept AND is_deleted = false"
        student_params = {"dept": target_dept_id}

    if sem_int is not None:
        student_query_str += " AND semester = :sem"
        student_params["sem"] = sem_int
    else:
        if active_semesters:
            student_query_str += " AND semester = ANY(:active_sems)"
            student_params["active_sems"] = active_semesters
            
    if section:
        student_query_str += " AND section_id IN (SELECT id FROM sections WHERE section_name = :section_name)"
        student_params["section_name"] = section

    stu_res = await db.execute(text(student_query_str), student_params)
    student_ids = [str(r[0]) for r in stu_res.all()]
    total_students = len(student_ids)

    if not target_dept_id:
        total_faculty = await db.scalar(
            select(func.count(User.id)).where(
                User.role.in_([UserRole.FACULTY, UserRole.HOD]),
                User.is_active.is_(True),
                User.is_deleted.is_(False)
            )
        ) or 0
    else:
        total_faculty = await db.scalar(
            select(func.count(User.id)).where(
                User.department_id == target_dept_id,
                User.role.in_([UserRole.FACULTY, UserRole.HOD]),
                User.is_active.is_(True),
                User.is_deleted.is_(False)
            )
        ) or 0

    total_departments = await db.scalar(
        select(func.count(Department.id)).where(Department.is_deleted.is_(False))
    ) or 0

    from app.db.models.academic import Degree
    from sqlalchemy import or_
    if not target_dept_id:
        active_courses_stmt = select(Course).where(Course.is_deleted.is_(False))
    else:
        active_courses_stmt = (
            select(Course)
            .outerjoin(Degree, Course.degree_id == Degree.id)
            .where(
                or_(Course.dept_id == target_dept_id, Degree.dept_id == target_dept_id),
                Course.is_deleted.is_(False)
            )
        )
    if sem_int is not None:
        active_courses_stmt = active_courses_stmt.where(Course.semester == sem_int)
    else:
        if active_semesters:
            active_courses_stmt = active_courses_stmt.where(Course.semester.in_(active_semesters))
    active_courses_res = await db.execute(active_courses_stmt)
    active_courses = len(active_courses_res.scalars().all())

    # ─── 2. ATTENDANCE SUMMARY ────────────────────────────────────────────────
    avg_attendance = 100.0
    today_present = 0
    today_absent = 0

    if student_ids:
        # Overall attendance stats
        att_res = await db.execute(
            text("""
                SELECT status, COUNT(*) FROM attendance
                WHERE student_id = ANY(:ids) AND is_deleted = false
                GROUP BY status
            """),
            {"ids": student_ids}
        )
        att_by_status = {r[0]: r[1] for r in att_res.all()}
        total_att = sum(att_by_status.values())
        present_att = att_by_status.get("present", 0) + att_by_status.get("od", 0)
        avg_attendance = round((present_att / total_att) * 100, 1) if total_att > 0 else 100.0

        # Today's attendance - get most recent date with attendance records
        latest_date_res = await db.execute(
            text("SELECT MAX(date) FROM attendance WHERE student_id = ANY(:ids) AND is_deleted = false"),
            {"ids": student_ids}
        )
        latest_date = latest_date_res.scalar() or today

        today_att_res = await db.execute(
            text("""
                SELECT status, COUNT(*) FROM attendance
                WHERE student_id = ANY(:ids) AND date = :dt AND is_deleted = false
                GROUP BY status
            """),
            {"ids": student_ids, "dt": latest_date}
        )
        today_by_status = {r[0]: r[1] for r in today_att_res.all()}
        today_present = today_by_status.get("present", 0) + today_by_status.get("od", 0)
        today_absent = today_by_status.get("absent", 0)

    # Faculty attendance (derived from leave status)
    if not target_dept_id:
        faculty_res = await db.execute(
            select(User).where(
                User.role.in_([UserRole.FACULTY, UserRole.HOD]),
                User.is_active.is_(True),
                User.is_deleted.is_(False)
            )
        )
    else:
        faculty_res = await db.execute(
            select(User).where(
                User.department_id == target_dept_id,
                User.role.in_([UserRole.FACULTY, UserRole.HOD]),
                User.is_active.is_(True),
                User.is_deleted.is_(False)
            )
        )
    faculties = faculty_res.scalars().all()

    # Find faculty on approved leave today
    if faculties:
        fac_ids = [f.id for f in faculties]
        on_leave_res = await db.execute(
            select(LeaveRequest.user_id).where(
                LeaveRequest.user_id.in_(fac_ids),
                LeaveRequest.status == "APPROVED",
                LeaveRequest.from_date <= today,
                LeaveRequest.to_date >= today,
                LeaveRequest.is_deleted.is_(False)
            )
        )
        leave_user_ids = set(on_leave_res.scalars().all())
    else:
        leave_user_ids = set()

    faculty_attendance_list = [
        {
            "id": f.id,
            "name": f.full_name,
            "status": "On Leave" if f.id in leave_user_ids else "Present"
        }
        for f in faculties
    ]

    # ─── 3. LEAVE & OD APPROVALS ─────────────────────────────────────────────
    # Get all users in department / college
    if not target_dept_id:
        dept_users_res = await db.execute(
            select(User.id).where(User.is_deleted.is_(False))
        )
    else:
        dept_users_res = await db.execute(
            select(User.id).where(User.department_id == target_dept_id, User.is_deleted.is_(False))
        )
    dept_user_ids = dept_users_res.scalars().all()

    pending_requests = []
    approved_count = 0
    rejected_count = 0
    pending_count = 0

    if dept_user_ids:
        leave_rows_res = await db.execute(
            select(LeaveRequest, User)
            .join(User, LeaveRequest.user_id == User.id)
            .where(
                LeaveRequest.user_id.in_(dept_user_ids),
                LeaveRequest.is_deleted.is_(False)
            )
            .order_by(LeaveRequest.created_at.desc())
        )
        for leave, user in leave_rows_res.all():
            st = leave.status.value if hasattr(leave.status, "value") else str(leave.status)
            if st == "PENDING":
                pending_count += 1
                pending_requests.append({
                    "id": leave.id,
                    "applicant_name": user.full_name,
                    "role": user.role.value if hasattr(user.role, "value") else str(user.role),
                    "type": leave.type,
                    "from_date": leave.from_date.isoformat(),
                    "to_date": leave.to_date.isoformat(),
                    "reason": leave.reason,
                    "status": "PENDING"
                })
            elif st == "APPROVED":
                approved_count += 1
            elif st == "REJECTED":
                rejected_count += 1

    # ─── 4. ACADEMIC MONITORING ──────────────────────────────────────────────
    from app.db.models.academic import Degree
    from sqlalchemy import or_
    if not target_dept_id:
        sec_q = (
            select(Section.id)
            .join(Course, Section.course_id == Course.id)
            .where(
                Section.is_deleted.is_(False)
            )
        )
    else:
        sec_q = (
            select(Section.id)
            .join(Course, Section.course_id == Course.id)
            .outerjoin(Degree, Course.degree_id == Degree.id)
            .where(
                or_(Course.dept_id == target_dept_id, Degree.dept_id == target_dept_id),
                Section.is_deleted.is_(False)
            )
        )
    if sem_int is not None:
        sec_q = sec_q.where(Course.semester == sem_int)
    else:
        if active_semesters:
            sec_q = sec_q.where(Course.semester.in_(active_semesters))
    if section:
        sec_q = sec_q.where(Section.section_name == section)
    section_ids_res = await db.execute(sec_q)
    section_ids = section_ids_res.scalars().all()

    weekday_map = {0: "MONDAY", 1: "TUESDAY", 2: "WEDNESDAY", 3: "THURSDAY", 4: "FRIDAY", 5: "SATURDAY"}
    today_weekday = weekday_map.get(today.weekday(), "")
    ongoing_classes_count = 0

    if section_ids and today_weekday:
        now_time = datetime.now().time()
        ongoing_classes_count = await db.scalar(
            select(func.count(Timetable.id)).where(
                Timetable.section_id.in_(section_ids),
                Timetable.weekday == today_weekday,
                Timetable.start_time <= now_time,
                Timetable.end_time >= now_time,
                Timetable.is_deleted.is_(False)
            )
        ) or 0

    # Syllabus completion from JSON DB in Postgres
    diaries = []
    lp = {}
    try:
        db_json = load_db_from_postgres(DB_FILE, lambda: {})
        diaries = list(db_json.get("class_diaries", {}).values())
        lp = db_json.get("lesson_plans", {})
    except Exception:
        pass

    from app.api.v1.endpoints.teaching_logs import match_subject, is_topic_covered
    from app.db.models.academic import Degree
    from sqlalchemy import or_

    if not target_dept_id:
        course_query = select(Course).where(Course.is_deleted.is_(False))
    else:
        course_query = select(Course).outerjoin(Degree, Course.degree_id == Degree.id).where(Course.is_deleted.is_(False))
        course_query = course_query.where(or_(Course.dept_id == target_dept_id, Degree.dept_id == target_dept_id))
    if sem_int is not None:
        course_query = course_query.where(Course.semester == sem_int)
    else:
        if active_semesters:
            course_query = course_query.where(Course.semester.in_(active_semesters))
    course_res = await db.execute(course_query)
    dept_courses = course_res.scalars().all()

    # Filter diaries by semester and section:
    if sem_int is not None:
        diaries = [d for d in diaries if d.get("semester", "").strip() == str(semester).strip() or (parse_semester_to_int(d.get("semester")) == sem_int)]
    else:
        if active_semesters:
            diaries = [d for d in diaries if parse_semester_to_int(d.get("semester")) in active_semesters]
    if section:
        diaries = [d for d in diaries if d.get("section", "").lower().strip() == section.lower().strip()]

    subjects_stats = []
    for sub, units in lp.items():
        matched_course = None
        for c in dept_courses:
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
                if any(is_topic_covered(d, sub, unit_name, t) for d in diaries):
                    done_topics += 1
        pct = round((done_topics / total_topics) * 100, 1) if total_topics > 0 else 0.0
        
        # Try to find faculty name from the diaries
        faculty_name = "Faculty User"
        sub_diaries = [d for d in diaries if match_subject(d.get("subject"), sub)]
        if sub_diaries:
            faculty_name = sub_diaries[0].get("faculty_name", "Faculty User")
            
        subjects_stats.append({
            "subject": sub,
            "completion": pct,
            "faculty": faculty_name,
            "semester": matched_course.semester if matched_course else None
        })

    # Assignments
    if not target_dept_id:
        assignments_res = await db.execute(
            select(Assignment, User)
            .join(User, Assignment.faculty_id == User.id)
            .where(Assignment.is_deleted.is_(False))
        )
    else:
        assignments_res = await db.execute(
            select(Assignment, User)
            .join(User, Assignment.faculty_id == User.id)
            .where(User.department_id == target_dept_id, Assignment.is_deleted.is_(False))
        )
    pending_activities = [
        {
            "id": a.id,
            "title": a.title,
            "deadline": a.deadline.isoformat(),
            "faculty_name": u.full_name,
            "submission_count": a.submission_count
        }
        for a, u in assignments_res.all()
    ]

    # ─── 5. VERIFY MATERIAL ──────────────────────────────────────────────────
    if not target_dept_id:
        materials_res = await db.execute(
            select(StudyMaterial, User)
            .join(User, StudyMaterial.faculty_id == User.id)
            .where(StudyMaterial.is_deleted.is_(False))
            .order_by(StudyMaterial.created_at.desc())
        )
    else:
        materials_res = await db.execute(
            select(StudyMaterial, User)
            .join(User, StudyMaterial.faculty_id == User.id)
            .where(User.department_id == target_dept_id, StudyMaterial.is_deleted.is_(False))
            .order_by(StudyMaterial.created_at.desc())
        )
    pending_materials = []
    approved_mat_count = 0
    rejected_mat_count = 0
    pending_mat_count = 0

    for mat, user in materials_res.all():
        st = mat.status
        if st == "PENDING":
            pending_mat_count += 1
            pending_materials.append({
                "id": mat.id,
                "title": mat.title,
                "faculty_name": user.full_name,
                "type": mat.type,
                "status": st,
                "file_url": mat.file_url,
                "created_at": mat.created_at.isoformat() if mat.created_at else today.isoformat()
            })
        elif st == "APPROVED":
            approved_mat_count += 1
        elif st == "REJECTED":
            rejected_mat_count += 1

    # ─── 6. FACULTY WORKLOAD ─────────────────────────────────────────────────
    if not target_dept_id:
        workloads_res = await db.execute(
            select(FacultyWorkload, User)
            .join(User, FacultyWorkload.faculty_id == User.id)
            .where(FacultyWorkload.is_deleted.is_(False))
        )
    else:
        workloads_res = await db.execute(
            select(FacultyWorkload, User)
            .join(User, FacultyWorkload.faculty_id == User.id)
            .where(User.department_id == target_dept_id, FacultyWorkload.is_deleted.is_(False))
        )
    workload_list = []
    overloaded_count = 0
    underloaded_count = 0

    for wl, user in workloads_res.all():
        if wl.teaching_hours > 18:
            status_str = "Overloaded"
            overloaded_count += 1
        elif wl.teaching_hours < 12:
            status_str = "Underloaded"
            underloaded_count += 1
        else:
            status_str = "Normal"
        workload_list.append({
            "faculty_name": user.full_name,
            "teaching_hours": wl.teaching_hours,
            "status": status_str
        })

    # ─── 7. SUBSTITUTION & TIMETABLE ─────────────────────────────────────────
    if not target_dept_id:
        sub_res = await db.execute(
            select(SubstitutionAllocation, User)
            .join(FacultyAbsence, SubstitutionAllocation.absence_id == FacultyAbsence.id)
            .join(User, FacultyAbsence.faculty_id == User.id)
            .where(SubstitutionAllocation.is_deleted.is_(False))
            .order_by(SubstitutionAllocation.date.desc())
        )
    else:
        sub_res = await db.execute(
            select(SubstitutionAllocation, User)
            .join(FacultyAbsence, SubstitutionAllocation.absence_id == FacultyAbsence.id)
            .join(User, FacultyAbsence.faculty_id == User.id)
            .where(User.department_id == target_dept_id, SubstitutionAllocation.is_deleted.is_(False))
            .order_by(SubstitutionAllocation.date.desc())
        )
    sub_list = []
    for sub, user in sub_res.all():
        sub_fac_name = "Unassigned"
        if sub.substitute_faculty_id:
            sf_res = await db.execute(
                select(User.full_name).where(User.id == sub.substitute_faculty_id)
            )
            sub_fac_name = sf_res.scalar() or "Unassigned"
        sub_list.append({
            "id": sub.id,
            "faculty_name": user.full_name,
            "date": sub.date.isoformat(),
            "status": sub.status.value if hasattr(sub.status, "value") else str(sub.status),
            "substitute_faculty": sub_fac_name
        })

    timetable_list = []
    if section_ids and today_weekday:
        tt_res = await db.execute(
            select(Timetable, Course, User, Section)
            .join(Course, Timetable.subject_id == Course.id)
            .join(User, Timetable.faculty_id == User.id)
            .join(Section, Timetable.section_id == Section.id)
            .where(
                Timetable.section_id.in_(section_ids),
                Timetable.weekday == today_weekday,
                Timetable.is_deleted.is_(False)
            )
            .order_by(Timetable.start_time)
        )
        now_time = datetime.now().time()
        for tt, course, user, sec in tt_res.all():
            if tt.start_time <= now_time <= tt.end_time:
                slot_status = "Ongoing"
            elif now_time > tt.end_time:
                slot_status = "Completed"
            else:
                slot_status = "Scheduled"
            timetable_list.append({
                "room": tt.room,
                "subject": course.name,
                "section": sec.section_name,
                "faculty_name": user.full_name,
                "time": f"{tt.start_time.strftime('%H:%M')} - {tt.end_time.strftime('%H:%M')}",
                "status": slot_status
            })

    # ─── 8. RESEARCH MONITORING ──────────────────────────────────────────────
    if not target_dept_id:
        research_res = await db.execute(
            select(FacultyResearch, User)
            .join(User, FacultyResearch.faculty_id == User.id)
            .where(FacultyResearch.is_deleted.is_(False))
            .order_by(FacultyResearch.created_at.desc())
        )
    else:
        research_res = await db.execute(
            select(FacultyResearch, User)
            .join(User, FacultyResearch.faculty_id == User.id)
            .where(User.department_id == target_dept_id, FacultyResearch.is_deleted.is_(False))
            .order_by(FacultyResearch.created_at.desc())
        )
    research_list = []
    active_res_count = 0
    pending_res_count = 0
    completed_res_count = 0

    for res_obj, user in research_res.all():
        st = res_obj.status
        if st in ("APPROVED", "ACTIVE"):
            active_res_count += 1
        elif st == "PENDING":
            pending_res_count += 1
        elif st == "COMPLETED":
            completed_res_count += 1
        research_list.append({
            "id": res_obj.id,
            "faculty_name": user.full_name,
            "title": res_obj.title,
            "publication": res_obj.publication or "N/A",
            "grant_amount": float(res_obj.grant_amount) if res_obj.grant_amount else 0.0,
            "status": st
        })

    # ─── RETURN RESPONSE ─────────────────────────────────────────────────────
    return {
        "overview": {
            "total_students": total_students,
            "total_faculty": total_faculty,
            "total_departments": total_departments,
            "active_courses": active_courses
        },
        "attendance_summary": {
            "student_attendance_rate": avg_attendance,
            "present_count": today_present,
            "absent_count": today_absent,
            "faculty_attendance": faculty_attendance_list
        },
        "leave_approvals": {
            "pending_count": pending_count,
            "approved_count": approved_count,
            "rejected_count": rejected_count,
            "recent_requests": pending_requests[:10]
        },
        "academic_monitoring": {
            "ongoing_classes_count": ongoing_classes_count,
            "syllabus_completion": subjects_stats,
            "pending_activities": pending_activities[:10]
        },
        "verify_material": {
            "pending_count": pending_mat_count,
            "approved_count": approved_mat_count,
            "rejected_count": rejected_mat_count,
            "recent_materials": pending_materials[:10]
        },
        "faculty_workload": {
            "workloads": workload_list,
            "overloaded_count": overloaded_count,
            "underloaded_count": underloaded_count
        },
        "substitution_timetable": {
            "substitution_requests": sub_list[:10],
            "today_timetable": timetable_list
        },
        "research_monitoring": {
            "active_projects": active_res_count,
            "pending_approvals": pending_res_count,
            "completed_research": completed_res_count,
            "recent_research": research_list[:10]
        }
    }


def _empty_response():
    return {
        "overview": {"total_students": 0, "total_faculty": 0, "total_departments": 0, "active_courses": 0},
        "attendance_summary": {"student_attendance_rate": 100.0, "present_count": 0, "absent_count": 0, "faculty_attendance": []},
        "leave_approvals": {"pending_count": 0, "approved_count": 0, "rejected_count": 0, "recent_requests": []},
        "academic_monitoring": {"ongoing_classes_count": 0, "syllabus_completion": [], "pending_activities": []},
        "verify_material": {"pending_count": 0, "approved_count": 0, "rejected_count": 0, "recent_materials": []},
        "faculty_workload": {"workloads": [], "overloaded_count": 0, "underloaded_count": 0},
        "substitution_timetable": {"substitution_requests": [], "today_timetable": []},
        "research_monitoring": {"active_projects": 0, "pending_approvals": 0, "completed_research": 0, "recent_research": []}
    }

