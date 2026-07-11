import os
import shutil
import uuid
import json
from datetime import date, datetime
from fastapi import APIRouter, Depends, HTTPException, status, UploadFile, File, Response
from sqlalchemy.ext.asyncio import AsyncSession
from sqlalchemy import select, func, and_, update
from pydantic import BaseModel


from app.core.dependencies import get_db_session, role_required, get_current_user
from app.db.models.user import User, UserRole
from app.db.models.academic import Course, Section, Timetable, Department, Degree, Weekday, TimetableApproval, ApprovalStatus, SubjectAllocation
from app.db.models.faculty import FacultyProfile, FacultyResearch, FacultyWorkload, PublicationPlan, ResearchCompliance, FacultyProfileUpdateRequest
from app.db.models.study_material import StudyMaterial, Assignment
from app.db.models.payroll import Salary, SalarySlip, Deduction, SalarySlipRequest, DeductionType
from app.db.models.attendance import Attendance, AttendanceCorrection
from app.db.models.leave import LeaveRequest
from app.db.models.student import Student, MentorshipRecord
from app.db.models.substitution import FacultyAbsence, SubstitutionAllocation, SubstitutionStatus
from app.db.models.audit import AuditLog
from app.schemas.dashboard import DashboardResponse, MetricSchema
from app.schemas.faculty import (
    AttendanceMarkRequest, StudyMaterialUploadRequest, AssignmentCreateRequest,
    ResearchEntryRequest, ResearchResponse, LeaveVerifyRequest, WorkloadResponse,
    FacultyPayrollResponse, FacultyProfileResponse, FacultyProfileUpdateRequest as FacultyProfileUpdateRequestSchema,
    FacultyProfileAdminUpdateRequest, FacultyActivitySummaryResponse,
    FacultyProfileUpdateRequestCreate, FacultyProfileUpdateRequestResponse,
    FacultyProfileUpdateRequestReview,
    HODTimetableItemResponse, HODTimetableCreate, HODTimetableUpdate,
    HODCourseResponse, HODFacultyResponse, HODSectionResponse
)
from app.schemas.student import LeaveApplicationRequest, LeaveRequestResponse, NoticeResponse, StudyMaterialResponse
from app.schemas.payroll import SalarySlipRequestCreate, SalarySlipRequestResponse, SalarySlipDetailedResponse, DeductionDetail, AdminSalarySlipResponse
from app.schemas.substitution import (
    FacultyAbsenceCreate, SubstitutionAllocateRequest, SubstitutionStatusUpdateRequest,
    SubstitutionAllocationResponse, FacultyAbsenceResponse, FacultyResponse,
    TimetableDetails, SubstitutionReportResponse
)
from app.services.attendance_service import AttendanceService
from app.services.payroll_service import PayrollService
from app.services.academic_service import AcademicService
from app.services.notification_service import NotificationService
from app.db.repositories.student_repository import StudentRepository


router = APIRouter()

@router.get("/dashboard", response_model=DashboardResponse)
async def faculty_dashboard(
    current_user: User = Depends(role_required([UserRole.FACULTY, UserRole.HOD])),
    db: AsyncSession = Depends(get_db_session)
) -> DashboardResponse:
    import datetime
    
    # 1. Today's Classes
    today_weekday = datetime.datetime.now().strftime("%A").upper()
    try:
        weekday_enum = Weekday[today_weekday]
    except KeyError:
        weekday_enum = None

    classes_count = 0
    if weekday_enum:
        classes_q = await db.execute(
            select(func.count(Timetable.id))
            .where(
                Timetable.faculty_id == current_user.id,
                Timetable.weekday == weekday_enum,
                Timetable.is_deleted.is_(False)
            )
        )
        classes_count = classes_q.scalar() or 0

    # 2. Pending Attendance
    pending_attendance_count = 0
    if weekday_enum:
        today_date = date.today()
        timetable_sections_q = await db.execute(
            select(Timetable.section_id)
            .where(
                Timetable.faculty_id == current_user.id,
                Timetable.weekday == weekday_enum,
                Timetable.is_deleted.is_(False)
            )
        )
        section_ids = [row[0] for row in timetable_sections_q.all() if row[0]]
        
        for sec_id in section_ids:
            att_exists_q = await db.execute(
                select(Attendance.id)
                .where(
                    Attendance.section_id == sec_id,
                    Attendance.date == today_date
                )
                .limit(1)
            )
            if not att_exists_q.scalar_one_or_none():
                pending_attendance_count += 1

    # 3. Pending Assignments
    assignments_q = await db.execute(
        select(func.count(Assignment.id))
        .where(
            Assignment.faculty_id == current_user.id,
            Assignment.is_deleted.is_(False)
        )
    )
    assignments_count = assignments_q.scalar() or 0

    # 4. Leave Balance
    leaves_q = await db.execute(
        select(LeaveRequest.from_date, LeaveRequest.to_date)
        .where(
            LeaveRequest.user_id == current_user.id,
            LeaveRequest.status == "APPROVED",
            LeaveRequest.is_deleted.is_(False)
        )
    )
    approved_days = 0
    for from_date, to_date in leaves_q.all():
        if from_date and to_date:
            approved_days += (to_date - from_date).days + 1
    
    leave_balance = max(15 - approved_days, 0)

    return DashboardResponse(
        metrics=[
            MetricSchema(id="classes", label="Today's Classes", value=str(classes_count)),
            MetricSchema(id="pending", label="Pending Attendance", value=str(pending_attendance_count)),
            MetricSchema(id="assignments", label="Pending Assignments", value=str(assignments_count)),
            MetricSchema(id="leave", label="Leave Balance", value=f"{leave_balance} days"),
        ]
    )

@router.get("/timetable", response_model=list[dict])
async def get_timetable(
    current_user: User = Depends(role_required([UserRole.FACULTY, UserRole.HOD, UserRole.PRINCIPAL, UserRole.ADMIN, UserRole.SUPER_ADMIN])),
    db: AsyncSession = Depends(get_db_session)
) -> list[dict]:
    return await _get_timetable_for_user(None, current_user, db)


@router.get("/timetable/{user_id}", response_model=list[dict])
async def get_timetable_by_user(
    user_id: str,
    current_user: User = Depends(role_required([UserRole.FACULTY, UserRole.HOD, UserRole.PRINCIPAL, UserRole.ADMIN, UserRole.SUPER_ADMIN])),
    db: AsyncSession = Depends(get_db_session)
) -> list[dict]:
    return await _get_timetable_for_user(user_id, current_user, db)


async def _get_timetable_for_user(user_id: str | None, current_user: User, db: AsyncSession) -> list[dict]:
    service = AcademicService(db)
    target_user_id = user_id if user_id else current_user.id
    items = await service.get_faculty_timetable(target_user_id)
    
    response_items = []
    for item in items:
        course_q = await db.execute(select(Course).where(Course.id == item.subject_id))
        course = course_q.scalar_one_or_none()
        
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
            "subject_code": course.code if course else "CSE101",
            "subject_name": course.name if course else "Python",
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

@router.post("/attendance/mark")
async def mark_attendance(
    payload: AttendanceMarkRequest,
    current_user: User = Depends(role_required([UserRole.FACULTY, UserRole.HOD])),
    db: AsyncSession = Depends(get_db_session)
) -> dict:
    service = AttendanceService(db)
    att = await service.mark_student_attendance(
        student_id=payload.student_id,
        section_id=payload.section_id,
        date_val=payload.date,
        status=payload.status
    )
    await db.commit()
    return {"detail": "Attendance marked successfully", "attendance_id": att.id}


# --- NEW FACULTY ATTENDANCE MODULE ENDPOINTS ---

class BulkAttendanceMarkRequest(BaseModel):
    date: str
    section_id: str
    subject_id: str
    hour: int
    student_statuses: dict[str, str]

class AttendanceCorrectionRequest(BaseModel):
    studentRegNo: str
    studentName: str
    subject: str
    date: str
    previousStatus: str
    updatedStatus: str
    reason: str

class AttendanceWarningRequest(BaseModel):
    studentRegNo: str
    message: str


@router.get("/attendance/dashboard-stats")
async def get_attendance_dashboard_stats(
    current_user: User = Depends(role_required([UserRole.FACULTY, UserRole.HOD, UserRole.PRINCIPAL, UserRole.ADMIN, UserRole.SUPER_ADMIN])),
    db: AsyncSession = Depends(get_db_session)
):
    sections_q = await db.execute(select(Timetable.section_id).where(Timetable.faculty_id == current_user.id, Timetable.is_deleted.is_(False)))
    section_ids = [row[0] for row in sections_q.all() if row[0]]

    if not section_ids:
        return {
            "totalClasses": 0,
            "conductedThisMonth": 0,
            "avgAttendance": 0,
            "defaulterCount": 0,
            "totalStudents": 0,
            "presentToday": "0 / 0",
            "monthlyTrend": [],
            "subjectDistribution": [],
            "defaulterRiskProfile": {
                "critical": 0,
                "highRisk": 0,
                "warning": 0
            }
        }

    stmt_all = select(Attendance).where(Attendance.faculty_id == current_user.id, Attendance.is_deleted.is_(False))
    res_all = await db.execute(stmt_all)
    records = res_all.scalars().all()
    
    total_classes = len({r.date for r in records})

    today = date.today()
    start_of_month = date(today.year, today.month, 1)
    conducted_this_month = len({r.date for r in records if r.date >= start_of_month})

    # Count students per section
    sec_students_count = {}
    for sec_id in section_ids:
        count_stmt = select(func.count(Student.id)).where(Student.section_id == sec_id, Student.is_deleted.is_(False))
        count_res = await db.execute(count_stmt)
        sec_students_count[sec_id] = count_res.scalar_one() or 0

    total_possible = 0
    total_present_or_od = 0
    for r in records:
        students_in_sec = sec_students_count.get(r.section_id, 0)
        absent_count = len(r.absentee_ids or [])
        total_possible += students_in_sec
        total_present_or_od += (students_in_sec - absent_count)

    avg_attendance = round((total_present_or_od / total_possible) * 100) if total_possible > 0 else 100

    stmt_sections = select(Section).where(Section.id.in_(section_ids), Section.is_deleted.is_(False))
    res_sec = await db.execute(stmt_sections)
    sections = res_sec.scalars().all()
    course_ids = [s.course_id for s in sections]

    student_ids = set()
    students_list = []
    if course_ids:
        res_c = await db.execute(select(Course).where(Course.id.in_(course_ids)))
        courses = res_c.scalars().all()
        for c in courses:
            stmt_s = select(Student, User).join(User, Student.user_id == User.id).where(
                Student.department_id == c.dept_id,
                Student.semester == c.semester,
                Student.is_deleted.is_(False)
            )
            res_s = await db.execute(stmt_s)
            for student, user in res_s.all():
                if student.id not in student_ids:
                    student_ids.add(student.id)
                    students_list.append((student, user))
    total_students = len(students_list)

    defaulter_count = 0
    critical_count = 0
    high_risk_count = 0
    warning_count = 0
    for s, u in students_list:
        # Count attendance records in s.section_id
        s_records = [r for r in records if r.section_id == s.section_id]
        s_total = len(s_records)
        if s_total > 0:
            s_absent = sum(1 for r in s_records if r.absentee_ids and s.id in r.absentee_ids)
            s_pct = round(((s_total - s_absent) / s_total) * 100)
        else:
            s_pct = 100
        
        if s_pct < 75:
            defaulter_count += 1
            if s_pct < 50:
                critical_count += 1
            elif s_pct < 65:
                high_risk_count += 1
            else:
                warning_count += 1

    today_records = [r for r in records if r.date == today]
    today_total = 0
    today_present = 0
    for r in today_records:
        students_in_sec = sec_students_count.get(r.section_id, 0)
        absent_count = len(r.absentee_ids or [])
        today_total += students_in_sec
        today_present += (students_in_sec - absent_count)
        
    present_today = f"{today_present} / {today_total}" if today_total > 0 else "0 / 0"

    monthly_trend = []
    months_names = ["Jan", "Feb", "Mar", "Apr", "May", "Jun", "Jul", "Aug", "Sep", "Oct", "Nov", "Dec"]
    for m_idx in range(1, 13):
        m_records = [r for r in records if r.date.month == m_idx and r.date.year == today.year]
        if m_records:
            m_possible = 0
            m_present_or_od = 0
            for r in m_records:
                students_in_sec = sec_students_count.get(r.section_id, 0)
                absent_count = len(r.absentee_ids or [])
                m_possible += students_in_sec
                m_present_or_od += (students_in_sec - absent_count)
            m_pct = round((m_present_or_od / m_possible) * 100) if m_possible > 0 else 100
            monthly_trend.append({"month": months_names[m_idx - 1], "percentage": m_pct})

    subject_distribution = []
    if course_ids:
        for c_id in set(course_ids):
            c_records = [r for r in records if r.subject_id == c_id]
            course_obj = await db.get(Course, c_id)
            if c_records and course_obj:
                c_possible = 0
                c_present_or_od = 0
                for r in c_records:
                    students_in_sec = sec_students_count.get(r.section_id, 0)
                    absent_count = len(r.absentee_ids or [])
                    c_possible += students_in_sec
                    c_present_or_od += (students_in_sec - absent_count)
                c_pct = round((c_present_or_od / c_possible) * 100) if c_possible > 0 else 100
                subject_distribution.append({
                    "subject": f"{course_obj.name} ({course_obj.code})",
                    "percentage": c_pct
                })
    # subject_distribution stays empty if no real data exists

    return {
        "totalClasses": total_classes,
        "conductedThisMonth": conducted_this_month,
        "avgAttendance": avg_attendance,
        "defaulterCount": defaulter_count,
        "totalStudents": total_students,
        "presentToday": present_today,
        "monthlyTrend": monthly_trend,
        "subjectDistribution": subject_distribution,
        "defaulterRiskProfile": {
            "critical": critical_count,
            "highRisk": high_risk_count,
            "warning": warning_count
        }
    }


@router.get("/attendance/sections")
async def get_faculty_attendance_sections(
    current_user: User = Depends(role_required([UserRole.FACULTY, UserRole.HOD, UserRole.PRINCIPAL, UserRole.ADMIN, UserRole.SUPER_ADMIN])),
    db: AsyncSession = Depends(get_db_session)
):
    sections_q = await db.execute(
        select(Timetable.section_id, Timetable.subject_id)
        .where(Timetable.faculty_id == current_user.id, Timetable.is_deleted.is_(False))
    )
    rows = sections_q.all()
    results = []
    seen = set()
    
    sections_list_q = await db.execute(select(Section).where(Section.is_deleted.is_(False)))
    sections = sections_list_q.scalars().all()
    
    for s_id, sub_id in rows:
        key = (s_id, sub_id)
        if key in seen:
            continue
        seen.add(key)
        
        sec = next((s for s in sections if s.id == s_id), None)
        course = await db.get(Course, sub_id)
        if sec and course:
            dept_name = "B.A. LL.B"
            if course.dept_id:
                dept = await db.get(Department, course.dept_id)
                if dept and dept.course_name:
                    dept_name = dept.course_name
            results.append({
                "section_id": s_id,
                "section_name": sec.section_name,
                "subject_id": sub_id,
                "subject_code": course.code,
                "subject_name": course.name,
                "course_name": dept_name,
                "semester": str(course.semester)
            })
    return results


@router.get("/attendance/students")
async def get_faculty_attendance_students(
    section_id: str,
    subject_id: str,
    current_user: User = Depends(role_required([UserRole.FACULTY, UserRole.HOD, UserRole.PRINCIPAL, UserRole.ADMIN, UserRole.SUPER_ADMIN])),
    db: AsyncSession = Depends(get_db_session)
):
    sec = await db.get(Section, section_id)
    if not sec:
        raise HTTPException(status_code=404, detail="Section not found")
    course = await db.get(Course, subject_id)
    if not course:
        raise HTTPException(status_code=404, detail="Course not found")

    sec_stmt = select(Section.id).where(Section.section_name == sec.section_name)
    sec_res = await db.execute(sec_stmt)
    matching_section_ids = sec_res.scalars().all()

    stmt_s = select(Student, User).join(User, Student.user_id == User.id).where(
        Student.section_id.in_(matching_section_ids),
        Student.department_id == course.dept_id,
        Student.semester == course.semester,
        Student.is_deleted.is_(False)
    )
    res_s = await db.execute(stmt_s)
    students = res_s.all()
    
    # Query all sessions in this section & subject
    att_stmt = select(Attendance).where(
        Attendance.section_id == section_id,
        Attendance.subject_id == subject_id,
        Attendance.is_deleted.is_(False)
    )
    att_res = await db.execute(att_stmt)
    att_records = att_res.scalars().all()
    s_total = len(att_records)

    results = []
    for s, u in students:
        s_present = 0
        s_absent = 0
        s_od = 0
        for r in att_records:
            if r.absentee_ids and s.id in r.absentee_ids:
                s_absent += 1
            elif r.od_ids and s.id in r.od_ids:
                s_od += 1
            else:
                s_present += 1
                
        s_pct = round(((s_present + s_od) / s_total) * 100) if s_total > 0 else 100

        results.append({
            "regNo": s.roll_no,
            "name": u.full_name,
            "overallAttendance": s_pct,
            "presentCount": s_present,
            "absentCount": s_absent,
            "odCount": s_od,
            "mlCount": 0
        })
    return results


@router.post("/attendance/mark-bulk")
async def mark_attendance_bulk(
    payload: BulkAttendanceMarkRequest,
    current_user: User = Depends(role_required([UserRole.FACULTY, UserRole.HOD])),
    db: AsyncSession = Depends(get_db_session)
):
    sec = await db.get(Section, payload.section_id)
    if not sec:
        raise HTTPException(status_code=404, detail="Section not found")

    from datetime import datetime
    date_val = datetime.strptime(payload.date, "%Y-%m-%d").date()

    hour_str = f"Hour {payload.hour}"
    stmt_exist = select(Attendance).where(
        Attendance.section_id == payload.section_id,
        Attendance.subject_id == payload.subject_id,
        Attendance.date == date_val,
        Attendance.hour == hour_str,
        Attendance.is_deleted.is_(False)
    )
    res_exist = await db.execute(stmt_exist)
    exist_record = res_exist.scalar_one_or_none()

    if exist_record:
        att = exist_record
        att.absentee_ids = []
        att.od_ids = []
    else:
        att = Attendance(
            section_id=payload.section_id,
            subject_id=payload.subject_id,
            faculty_id=current_user.id,
            date=date_val,
            hour=hour_str,
            absentee_ids=[],
            od_ids=[]
        )
        db.add(att)

    absentees = []
    ods = []
    for roll_no, status_char in payload.student_statuses.items():
        res_s = await db.execute(select(Student).where(Student.roll_no == roll_no, Student.is_deleted.is_(False)))
        student = res_s.scalar_one_or_none()
        if not student:
            continue
        
        if status_char in ("A", "ML"):
            absentees.append(student.id)
        elif status_char == "OD":
            ods.append(student.id)

    att.absentee_ids = absentees
    att.od_ids = ods

    from datetime import datetime as dt_class, timezone
    new_audit = AuditLog(
        user_id=current_user.id,
        action="Attendance Locked",
        entity="attendance",
        entity_id=None,
        ip_address="127.0.0.1",
        timestamp=dt_class.now(timezone.utc)
    )
    db.add(new_audit)
    
    if current_user.department_id:
        from app.db.models.academic import Department
        dept_q = await db.execute(select(Department).where(Department.id == current_user.department_id))
        dept = dept_q.scalars().first()
        if dept and dept.hod_id:
            from app.services.notification_service import NotificationService
            notif_service = NotificationService(db)
            await notif_service.send_notification(
                user_id=dept.hod_id,
                type_val="attendance_lock",
                message=f"Faculty {current_user.full_name} has locked and submitted attendance for Section {sec.section_name} on {payload.date}."
            )

    await db.commit()
    return {"detail": "Attendance bulk marked successfully"}


@router.get("/attendance/records")
async def get_submitted_attendance_records(
    current_user: User = Depends(role_required([UserRole.FACULTY, UserRole.HOD, UserRole.PRINCIPAL, UserRole.ADMIN, UserRole.SUPER_ADMIN])),
    db: AsyncSession = Depends(get_db_session)
):
    from sqlalchemy import or_
    if current_user.role == UserRole.FACULTY:
        stmt = (
            select(Attendance, Section, Course, Department)
            .join(Section, Attendance.section_id == Section.id)
            .join(Course, Attendance.subject_id == Course.id)
            .outerjoin(Department, Course.dept_id == Department.id)
            .where(
                Attendance.faculty_id == current_user.id,
                Attendance.is_deleted.is_(False)
            )
            .order_by(Attendance.date.desc())
        )
    elif current_user.role == UserRole.HOD:
        from app.db.models.academic import Degree
        stmt = (
            select(Attendance, Section, Course, Department)
            .join(Section, Attendance.section_id == Section.id)
            .join(Course, Attendance.subject_id == Course.id)
            .outerjoin(Department, Course.dept_id == Department.id)
            .outerjoin(Degree, Course.degree_id == Degree.id)
            .where(
                or_(Course.dept_id == current_user.department_id, Degree.dept_id == current_user.department_id),
                Attendance.is_deleted.is_(False)
            )
            .order_by(Attendance.date.desc())
        )
    else:
        stmt = (
            select(Attendance, Section, Course, Department)
            .join(Section, Attendance.section_id == Section.id)
            .join(Course, Attendance.subject_id == Course.id)
            .outerjoin(Department, Course.dept_id == Department.id)
            .where(
                Attendance.is_deleted.is_(False)
            )
            .order_by(Attendance.date.desc())
        )

    res = await db.execute(stmt)
    rows = res.all()

    if not rows:
        return []

    row_section_ids = list(set(sec.id for att, sec, course, dept in rows))
    students_q = await db.execute(
        select(Student.id, Student.roll_no, Student.section_id)
        .where(
            Student.section_id.in_(row_section_ids),
            Student.is_deleted.is_(False)
        )
    )
    all_sec_students = students_q.all()
    section_students = {}
    for s_id, s_roll, sec_id in all_sec_students:
        section_students.setdefault(sec_id, []).append((s_id, s_roll))

    results = []
    for att, sec, course, dept in rows:
        sec_students = section_students.get(sec.id, [])
        absent_ids = set(att.absentee_ids or [])
        od_ids = set(att.od_ids or [])
        
        p_count = 0
        a_count = 0
        od_count = 0
        student_statuses = {}
        
        for s_id, roll_no in sec_students:
            if s_id in absent_ids:
                student_statuses[roll_no] = "A"
                a_count += 1
            elif s_id in od_ids:
                student_statuses[roll_no] = "OD"
                od_count += 1
            else:
                student_statuses[roll_no] = "P"
                p_count += 1

        hour_val = 1
        try:
            hour_val = int(att.hour.split()[-1])
        except (ValueError, IndexError):
            pass

        record_id = f"rec_{att.id}"
        results.append({
            "id": record_id,
            "date": att.date.strftime("%Y-%m-%d"),
            "subject": course.name,
            "course": dept.course_name if dept and dept.course_name else "B.A. LL.B",
            "semester": str(course.semester),
            "section": sec.section_name,
            "hour": hour_val,
            "presentCount": p_count,
            "absentCount": a_count,
            "odCount": od_count,
            "mlCount": 0,
            "locked": True,
            "studentStatuses": student_statuses
        })

    return results


@router.get("/attendance/corrections")
async def get_attendance_corrections(
    current_user: User = Depends(role_required([UserRole.FACULTY, UserRole.HOD, UserRole.PRINCIPAL, UserRole.ADMIN, UserRole.SUPER_ADMIN])),
    db: AsyncSession = Depends(get_db_session)
):
    # Check if empty, and if so insert the initial seed record to match the previous behavior
    q_count = await db.execute(select(func.count(AttendanceCorrection.id)))
    count = q_count.scalar() or 0
    if count == 0:
        from datetime import date
        seed = AttendanceCorrection(
            id="corr_1",
            student_reg_no="8873",
            student_name="Arunkumar L",
            subject="Introduction to Constitutional Law",
            date=date(2026, 6, 10),
            previous_status="Absent",
            updated_status="Present",
            reason="Marked absent incorrectly due to a technical glitch during initial scanning.",
            status="APPROVED"
        )
        db.add(seed)
        await db.commit()

    q = await db.execute(
        select(AttendanceCorrection)
        .where(AttendanceCorrection.is_deleted.is_(False))
        .order_by(AttendanceCorrection.created_at.desc())
    )
    corrs = q.scalars().all()
    return [
        {
            "id": c.id,
            "studentRegNo": c.student_reg_no,
            "studentName": c.student_name,
            "subject": c.subject,
            "date": c.date.strftime("%Y-%m-%d") if c.date else "",
            "previousStatus": c.previous_status,
            "updatedStatus": c.updated_status,
            "reason": c.reason,
            "requestedAt": c.created_at.strftime("%Y-%m-%d %I:%M %p") if c.created_at else "",
            "status": c.status,
            "remarks": c.remarks
        }
        for c in corrs
    ]


@router.post("/attendance/correction/submit")
async def submit_attendance_correction(
    payload: AttendanceCorrectionRequest,
    current_user: User = Depends(role_required([UserRole.FACULTY, UserRole.HOD])),
    db: AsyncSession = Depends(get_db_session)
):
    res_s = await db.execute(select(Student).where(Student.roll_no == payload.studentRegNo, Student.is_deleted.is_(False)))
    student = res_s.scalar_one_or_none()
    if not student:
        raise HTTPException(status_code=404, detail="Student not found")

    from datetime import datetime
    date_val = datetime.strptime(payload.date, "%Y-%m-%d").date()

    new_corr = AttendanceCorrection(
        student_reg_no=payload.studentRegNo,
        student_name=payload.studentName,
        subject=payload.subject,
        date=date_val,
        previous_status=payload.previousStatus,
        updated_status=payload.updatedStatus,
        reason=payload.reason,
        status="PENDING"
    )
    db.add(new_corr)
    await db.flush()  # To populate id

    from datetime import datetime as dt_class, timezone
    new_audit = AuditLog(
        user_id=current_user.id,
        action="Attendance Correction Requested",
        entity="attendance_correction",
        entity_id=new_corr.id,
        ip_address="127.0.0.1",
        timestamp=dt_class.now(timezone.utc)
    )
    db.add(new_audit)

    await db.commit()
    return {"detail": "Correction request logged and pending HOD approval"}


@router.get("/attendance/correction-requests")
async def get_attendance_correction_requests(
    status_filter: str = "all",
    current_user: User = Depends(role_required([UserRole.HOD, UserRole.PRINCIPAL, UserRole.SUPER_ADMIN])),
    db: AsyncSession = Depends(get_db_session)
):
    stmt = select(AttendanceCorrection).where(AttendanceCorrection.is_deleted.is_(False))
    if status_filter != "all":
        stmt = stmt.where(func.upper(AttendanceCorrection.status) == status_filter.upper())
    else:
        stmt = stmt.order_by(AttendanceCorrection.created_at.desc())
        
    q = await db.execute(stmt)
    corrs = q.scalars().all()
    return [
        {
            "id": c.id,
            "studentRegNo": c.student_reg_no,
            "studentName": c.student_name,
            "subject": c.subject,
            "date": c.date.strftime("%Y-%m-%d") if c.date else "",
            "previousStatus": c.previous_status,
            "updatedStatus": c.updated_status,
            "reason": c.reason,
            "requestedAt": c.created_at.strftime("%Y-%m-%d %I:%M %p") if c.created_at else "",
            "status": c.status,
            "remarks": c.remarks
        }
        for c in corrs
    ]


class RejectCorrectionRequest(BaseModel):
    remarks: str


@router.post("/attendance/correction-requests/{requestId}/approve")
async def approve_attendance_correction(
    requestId: str,
    current_user: User = Depends(role_required([UserRole.HOD])),
    db: AsyncSession = Depends(get_db_session)
):
    corr = await db.get(AttendanceCorrection, requestId)
    if not corr or corr.is_deleted:
        raise HTTPException(status_code=404, detail="Correction request not found")
        
    if corr.status == "APPROVED":
        raise HTTPException(status_code=400, detail="Already approved")

    # Update DB
    res_s = await db.execute(select(Student).where(Student.roll_no == corr.student_reg_no, Student.is_deleted.is_(False)))
    student = res_s.scalar_one_or_none()
    if not student:
        raise HTTPException(status_code=404, detail="Student not found in DB")

    stmt_att = select(Attendance).where(
        Attendance.section_id == student.section_id,
        Attendance.date == corr.date,
        Attendance.is_deleted.is_(False)
    )
    res_att = await db.execute(stmt_att)
    att_records = res_att.scalars().all()

    if not att_records:
        res_sec = await db.execute(select(Section).where(Section.id == student.section_id))
        sec = res_sec.scalar_one_or_none()
        sec_id = sec.id if sec else student.section_id
        subject_id = sec.course_id if sec else None
        if not subject_id:
            res_c = await db.execute(select(Course.id))
            subject_id = res_c.scalars().first() or "dummy-subject"
            
        f_q = await db.execute(select(User.id).where(User.role == UserRole.FACULTY))
        faculty_id = f_q.scalars().first() or "dummy-faculty"

        new_att = Attendance(
            section_id=sec_id,
            subject_id=subject_id,
            faculty_id=faculty_id,
            date=corr.date,
            hour="Hour 1",
            absentee_ids=[],
            od_ids=[]
        )
        db.add(new_att)
        att_records = [new_att]

    for att in att_records:
        absentees = list(att.absentee_ids or [])
        ods = list(att.od_ids or [])
        if student.id in absentees:
            absentees.remove(student.id)
        if student.id in ods:
            ods.remove(student.id)

        if corr.updated_status in ["Absent", "absent"]:
            absentees.append(student.id)
        elif corr.updated_status in ["On Duty", "OD", "od"]:
            ods.append(student.id)

        att.absentee_ids = absentees
        att.od_ids = ods

    corr.status = "APPROVED"

    from datetime import datetime as dt_class, timezone
    new_audit = AuditLog(
        user_id=current_user.id,
        action="Attendance Correction Approved",
        entity="attendance_correction",
        entity_id=corr.id,
        ip_address="127.0.0.1",
        timestamp=dt_class.now(timezone.utc)
    )
    db.add(new_audit)
    await db.commit()
    
    return {"detail": "Correction approved and attendance updated"}


@router.post("/attendance/correction-requests/{requestId}/reject")
async def reject_attendance_correction(
    requestId: str,
    payload: RejectCorrectionRequest,
    current_user: User = Depends(role_required([UserRole.HOD])),
    db: AsyncSession = Depends(get_db_session)
):
    corr = await db.get(AttendanceCorrection, requestId)
    if not corr or corr.is_deleted:
        raise HTTPException(status_code=404, detail="Correction request not found")

    corr.status = "REJECTED"
    corr.remarks = payload.remarks

    from datetime import datetime as dt_class, timezone
    new_audit = AuditLog(
        user_id=current_user.id,
        action="Attendance Correction Rejected",
        entity="attendance_correction",
        entity_id=corr.id,
        ip_address="127.0.0.1",
        timestamp=dt_class.now(timezone.utc)
    )
    db.add(new_audit)
    await db.commit()
    
    return {"detail": "Correction rejected"}



@router.post("/attendance/send-warning")
async def send_attendance_warning(
    payload: AttendanceWarningRequest,
    current_user: User = Depends(role_required([UserRole.FACULTY, UserRole.HOD])),
    db: AsyncSession = Depends(get_db_session)
):
    res_s = await db.execute(select(Student).where(Student.roll_no == payload.studentRegNo, Student.is_deleted.is_(False)))
    student = res_s.scalar_one_or_none()
    if not student:
        raise HTTPException(status_code=404, detail="Student not found")

    from app.services.notification_service import NotificationService
    notif_service = NotificationService(db)
    await notif_service.send_notification(
        user_id=student.user_id,
        type_val="warning",
        message=payload.message,
        sent_via="In-App"
    )
    
    await notif_service.send_notification(
        user_id=current_user.id,
        type_val="warning",
        message=payload.message,
        sent_via="In-App"
    )

    await db.commit()
    return {"detail": "Warning notification sent successfully"}


@router.get("/attendance/notifications")
async def get_faculty_attendance_notifications(
    current_user: User = Depends(role_required([UserRole.FACULTY, UserRole.HOD, UserRole.PRINCIPAL, UserRole.ADMIN, UserRole.SUPER_ADMIN])),
    db: AsyncSession = Depends(get_db_session)
):
    from app.services.notification_service import NotificationService
    notif_service = NotificationService(db)
    records = await notif_service.get_user_notifications(current_user.id)
    
    results = []
    for r in records:
        results.append({
            "id": r.id,
            "type": r.type,
            "message": r.message,
            "dateTime": r.created_at.strftime("%Y-%m-%d %I:%M %p") if getattr(r, "created_at", None) else ""
        })
        
    if not results:
        results = [
            {
                "id": "not_1",
                "type": "info",
                "message": "Welcome to the new Real-Data Attendance Dashboard.",
                "dateTime": date.today().strftime("%Y-%m-%d 09:00 AM")
            }
        ]
    return results


@router.get("/notifications")
async def get_faculty_notifications(
    current_user: User = Depends(get_current_user),
    db: AsyncSession = Depends(get_db_session)
):
    from app.services.notification_service import NotificationService
    notif_service = NotificationService(db)
    records = await notif_service.get_user_notifications(current_user.id)
    
    results = []
    for r in records:
        results.append({
            "id": r.id,
            "type": r.type,
            "message": r.message,
            "is_read": r.is_read,
            "created_at": r.created_at.isoformat() if getattr(r, "created_at", None) else None
        })
    return results


@router.post("/notifications/read/{notif_id}")
async def mark_faculty_notification_read(
    notif_id: str,
    current_user: User = Depends(get_current_user),
    db: AsyncSession = Depends(get_db_session)
):
    from app.db.models.communication import Notification
    await db.execute(
        update(Notification)
        .where(Notification.id == notif_id, Notification.user_id == current_user.id)
        .values(is_read=True)
    )
    await db.commit()
    return {"detail": "Notification marked as read"}



@router.get("/attendance/audit-logs")
async def get_faculty_attendance_audit_logs(
    current_user: User = Depends(role_required([UserRole.FACULTY, UserRole.HOD, UserRole.PRINCIPAL, UserRole.ADMIN, UserRole.SUPER_ADMIN])),
    db: AsyncSession = Depends(get_db_session)
):
    from app.db.models.audit import AuditLog
    stmt = select(AuditLog).where(AuditLog.user_id == current_user.id, AuditLog.is_deleted.is_(False)).order_by(AuditLog.timestamp.desc())
    res = await db.execute(stmt)
    logs = res.scalars().all()
    
    results = []
    for log in logs:
        results.append({
            "id": log.id,
            "user": current_user.full_name,
            "role": current_user.role.value,
            "action": log.action,
            "dateTime": log.timestamp.strftime("%Y-%m-%d %I:%M %p") if log.timestamp else "",
            "ipAddress": log.ip_address or "127.0.0.1",
            "remarks": f"Performed action: {log.action} on entity: {log.entity}"
        })
    return results


# ─── Faculty Portal Data Integration Endpoints ─────────────────────────────────
# These endpoints expose data from Admin modules (User Accounts, Department Setup,
# Course/Subject Setup) to the faculty portal with appropriate access controls.

@router.get("/students/list")
async def get_faculty_students_list(
    dept_id: str | None = None,
    semester: int | None = None,
    current_user: User = Depends(role_required([UserRole.FACULTY, UserRole.HOD, UserRole.PRINCIPAL, UserRole.ADMIN, UserRole.SUPER_ADMIN])),
    db: AsyncSession = Depends(get_db_session)
):
    """
    Returns all students from the User Account module.
    Optionally filtered by department ID and/or semester.
    Data source: User Account module (Admin portal).
    """
    stmt = select(Student, User).join(User, Student.user_id == User.id).where(
        Student.is_deleted.is_(False),
        User.is_deleted.is_(False)
    )

    if current_user.role == UserRole.HOD:
        dept_id = await get_hod_department_id(current_user, db)
    elif current_user.role == UserRole.FACULTY:
        if current_user.department_id:
            dept_id = current_user.department_id

    if dept_id:
        stmt = stmt.where(Student.department_id == dept_id)
    if semester is not None:
        stmt = stmt.where(Student.semester == semester)

    res = await db.execute(stmt)
    rows = res.all()

    results = []
    for student, user in rows:
        dept = None
        if student.department_id:
            dept = await db.get(Department, student.department_id)
        degree = None
        if student.degree_id:
            degree = await db.get(Degree, student.degree_id)
        section_name = None
        if student.section_id:
            sec_obj = await db.get(Section, student.section_id)
            if sec_obj:
                section_name = sec_obj.section_name
        results.append({
            "student_id": student.id,
            "user_id": user.id,
            "roll_no": student.roll_no,
            "full_name": user.full_name,
            "email": user.email,
            "phone": student.mobile_number or user.phone,
            "semester": student.semester,
            "batch_year": student.batch_year,
            "quota": student.quota,
            "department_id": student.department_id,
            "department_name": dept.name if dept else None,
            "department_code": dept.code if dept else None,
            "degree_id": student.degree_id,
            "degree_code": degree.code if degree else None,
            "section_name": section_name,
            "is_active": user.is_active,
            "verification_status": student.verification_status or "DRAFT",
            "staff_remarks": student.staff_remarks,
            "hod_remarks": student.hod_remarks,
            "document_aadhaar_url": student.document_aadhaar_url,
            "document_community_url": student.document_community_url,
            "document_tc_url": student.document_tc_url,
            "document_other_url": student.document_other_url,
            "edit_request_status": student.edit_request_status,
            "edit_request_reason": student.edit_request_reason,
            "date_of_birth": student.date_of_birth.strftime("%Y-%m-%d") if student.date_of_birth else "",
            "gender": student.gender,
            "blood_group": student.blood_group,
            "nationality": student.nationality,
            "current_address": student.current_address,
            "permanent_address": student.permanent_address,
            "aadhaar_number": student.aadhaar_number,
            "passport_number": student.passport_number,
            "community_category": student.community_category,
            "religion": student.religion,
            "emergency_contact_name": student.emergency_contact_name,
            "emergency_contact_relationship": student.emergency_contact_relationship,
            "emergency_contact_number": student.emergency_contact_number,
            "father_name": student.father_name,
            "father_occupation": student.father_occupation,
            "father_mobile": student.father_mobile,
            "father_email": student.father_email,
            "father_office_address": student.father_office_address,
            "mother_name": student.mother_name,
            "mother_occupation": student.mother_occupation,
            "mother_mobile": student.mother_mobile,
            "mother_email": student.mother_email,
            "mother_office_address": student.mother_office_address,
            "parent_annual_income": student.parent_annual_income,
            "languages_known": student.languages_known,
            "hobbies_interests": student.hobbies_interests,
            "special_skills": student.special_skills,
            "medical_info": student.medical_info,
            "certifications": student.certifications
        })
    return results


@router.get("/departments/list")
async def get_faculty_departments_list(
    current_user: User = Depends(role_required([UserRole.FACULTY, UserRole.HOD, UserRole.PRINCIPAL, UserRole.ADMIN, UserRole.SUPER_ADMIN])),
    db: AsyncSession = Depends(get_db_session)
):
    """
    Returns all departments from the Department Setup module (Admin portal).
    Accessible to faculty so they can filter students/courses by department.
    """
    stmt = select(Department).where(Department.is_deleted.is_(False))
    if current_user.role in [UserRole.FACULTY, UserRole.HOD] and current_user.department_id:
        stmt = stmt.where(Department.id == current_user.department_id)
        
    result = await db.execute(stmt.order_by(Department.name))
    departments = result.scalars().all()
    return [
        {
            "id": d.id,
            "name": d.name,
            "code": d.code,
            "course_name": d.course_name,
            "duration_years": d.duration_years,
            "sem_count": d.sem_count,
            "establish_year": d.establish_year,
            "program_level": d.program_level,
            "intake": d.intake,
            "affiliation_code": d.affiliation_code,
            "hod_id": d.hod_id,
        }
        for d in departments
    ]


@router.get("/courses/list")
async def get_faculty_courses_list(
    dept_id: str | None = None,
    semester: int | None = None,
    current_user: User = Depends(role_required([UserRole.FACULTY, UserRole.HOD, UserRole.PRINCIPAL, UserRole.ADMIN, UserRole.SUPER_ADMIN])),
    db: AsyncSession = Depends(get_db_session)
):
    """
    Returns all courses/subjects from the Course Setup module (Admin portal).
    Optionally filtered by department ID and/or semester.
    If the current user is a FACULTY, only returns courses allocated to them.
    Data source: Course Setup module (Admin portal).
    """
    if current_user.role in [UserRole.FACULTY, UserRole.HOD] and current_user.department_id:
        dept_id = current_user.department_id

    if current_user.role == UserRole.FACULTY:
        # Get course IDs from SubjectAllocation
        alloc_stmt = select(SubjectAllocation.course_id).where(
            SubjectAllocation.faculty_id == current_user.id,
            SubjectAllocation.is_active.is_(True),
            SubjectAllocation.is_deleted.is_(False)
        )
        alloc_res = await db.execute(alloc_stmt)
        alloc_course_ids = set(alloc_res.scalars().all())

        # Get course IDs from Timetable
        tt_stmt = select(Timetable.subject_id).where(
            Timetable.faculty_id == current_user.id,
            Timetable.is_deleted.is_(False)
        )
        tt_res = await db.execute(tt_stmt)
        tt_course_ids = set(tt_res.scalars().all())

        allocated_course_ids = alloc_course_ids.union(tt_course_ids)

        if not allocated_course_ids:
            return []

        stmt = select(Course).where(
            Course.id.in_(allocated_course_ids),
            Course.is_deleted.is_(False)
        )
    else:
        stmt = select(Course).where(Course.is_deleted.is_(False))
        
    if dept_id:
        stmt = stmt.where(Course.dept_id == dept_id)
    if semester is not None:
        stmt = stmt.where(Course.semester == semester)

    result = await db.execute(stmt.order_by(Course.semester, Course.name))
    courses = result.scalars().all()

    output = []
    for c in courses:
        dept = None
        if c.dept_id:
            dept = await db.get(Department, c.dept_id)
        degree = None
        if c.degree_id:
            degree = await db.get(Degree, c.degree_id)
        output.append({
            "id": c.id,
            "code": c.code,
            "name": c.name,
            "credits": c.credits,
            "semester": c.semester,
            "dept_id": c.dept_id,
            "department_name": dept.name if dept else None,
            "department_code": dept.code if dept else None,
            "degree_id": c.degree_id,
            "degree_code": degree.code if degree else None,
            "degree_batch": degree.applicable_batch if degree else None,
        })
    return output
# ─────────────────────────────────────────────────────────────────────────────────


@router.get("/leaves", response_model=list[LeaveRequestResponse])
async def get_leaves(
    current_user: User = Depends(role_required([UserRole.FACULTY, UserRole.HOD])),
    db: AsyncSession = Depends(get_db_session)
) -> list[LeaveRequestResponse]:
    service = AcademicService(db)
    records = await service.get_user_leaves(current_user.id)
    return [
        LeaveRequestResponse(
            id=r.id,
            type=r.type,
            from_date=r.from_date,
            to_date=r.to_date,
            reason=r.reason,
            status=r.status
        )
        for r in records
    ]

@router.post("/leaves/apply", response_model=LeaveRequestResponse)
async def apply_leave(
    payload: LeaveApplicationRequest,
    current_user: User = Depends(role_required([UserRole.FACULTY, UserRole.HOD])),
    db: AsyncSession = Depends(get_db_session)
) -> LeaveRequestResponse:
    from app.schemas.student import LeaveApplicationRequest as StudentLeaveReq
    service = AcademicService(db)
    req = await service.apply_leave(
        user_id=current_user.id,
        app_category=payload.app_category,
        type_val=payload.type,
        session_type=payload.session_type,
        priority=payload.priority,
        from_date=payload.from_date,
        to_date=payload.to_date,
        reason=payload.reason,
        photo_url=payload.photo_url,
        latitude=payload.latitude,
        longitude=payload.longitude,
        location_address=payload.location_address,
        capture_time=payload.capture_time,
        verification_status=payload.verification_status,
        distance_from_campus=payload.distance_from_campus,
        device_id=payload.device_id,
        location_accuracy=payload.location_accuracy,
        geo_fence_status=payload.geo_fence_status,
        device_network_info=payload.device_network_info,
        metadata_=payload.metadata_
    )
    
    # Notify HOD or Principal depending on role
    from app.services.notification_service import NotificationService
    notif_service = NotificationService(db)
    if current_user.role == UserRole.HOD:
        principal_q = await db.execute(select(User).where(User.role == UserRole.PRINCIPAL))
        for principal in principal_q.scalars().all():
            await notif_service.send_notification(
                user_id=principal.id,
                type_val="leave_request",
                message=f"HOD {current_user.full_name} has applied for leave ({req.type}) from {req.from_date} to {req.to_date}."
            )
    else:
        if current_user.department_id:
            from app.db.models.academic import Department
            dept_q = await db.execute(select(Department).where(Department.id == current_user.department_id))
            dept = dept_q.scalars().first()
            if dept and dept.hod_id:
                await notif_service.send_notification(
                    user_id=dept.hod_id,
                    type_val="leave_request",
                    message=f"Faculty {current_user.full_name} has applied for leave ({req.type}) from {req.from_date} to {req.to_date}."
                )
                
    await db.commit()
    return LeaveRequestResponse(
        id=req.id,
        type=req.type,
        from_date=req.from_date,
        to_date=req.to_date,
        reason=req.reason,
        status=req.status
    )

@router.get("/payroll", response_model=list[FacultyPayrollResponse])
async def get_payroll(
    current_user: User = Depends(role_required([UserRole.FACULTY, UserRole.HOD, UserRole.PRINCIPAL])),
    db: AsyncSession = Depends(get_db_session)
) -> list[FacultyPayrollResponse]:
    # Query approved requests
    req_q = await db.execute(
        select(SalarySlipRequest).where(
            SalarySlipRequest.faculty_id == current_user.id,
            SalarySlipRequest.status == "APPROVED",
            SalarySlipRequest.is_deleted.is_(False)
        )
    )
    approved_requests = req_q.scalars().all()
    approved_slip_ids = {req.salary_slip_id for req in approved_requests if req.salary_slip_id}

    service = PayrollService(db)
    records = await service.get_faculty_payrolls(current_user.id)
    
    # Filter records to only those linked to approved requests
    filtered_records = []
    for r in records:
        slip_q = await db.execute(
            select(SalarySlip).where(
                SalarySlip.salary_id == r["salary_id"],
                SalarySlip.is_deleted.is_(False)
            )
        )
        slips = slip_q.scalars().all()
        for slip in slips:
            if slip.id in approved_slip_ids:
                filtered_records.append(
                    FacultyPayrollResponse(
                        salary_id=r["salary_id"],
                        basic=r["basic"],
                        allowances=r["allowances"],
                        gross=r["gross"],
                        deductions_total=r["deductions_total"],
                        net_pay=r["net_pay"],
                        month=r["month"],
                        year=r["year"],
                        pdf_url=slip.pdf_url
                    )
                )
                break
                
    return filtered_records

@router.post("/materials", response_model=StudyMaterialResponse)
async def upload_material(
    payload: StudyMaterialUploadRequest,
    current_user: User = Depends(role_required([UserRole.FACULTY, UserRole.HOD])),
    db: AsyncSession = Depends(get_db_session)
) -> StudyMaterialResponse:
    service = AcademicService(db)
    material = await service.upload_material(
        section_id=payload.section_id,
        faculty_id=current_user.id,
        title=payload.title,
        type_val=payload.type,
        file_url=payload.file_url
    )
    
    # Log to AuditLog
    audit_entry = AuditLog(
        user_id=current_user.id,
        action="SUBMITTED",
        entity="StudyMaterial",
        entity_id=material.id,
        timestamp=datetime.now()
    )
    db.add(audit_entry)
    
    await db.commit()
    return StudyMaterialResponse(
        id=material.id, 
        title=material.title, 
        type=material.type, 
        file_url=material.file_url, 
        is_verified=material.is_verified,
        status=material.status if hasattr(material, "status") else "PENDING",
        comments=material.comments if hasattr(material, "comments") else None
    )

@router.post("/assignments")
async def create_assignment(
    payload: AssignmentCreateRequest,
    current_user: User = Depends(role_required([UserRole.FACULTY, UserRole.HOD])),
    db: AsyncSession = Depends(get_db_session)
) -> dict:
    service = AcademicService(db)
    assign = await service.create_assignment(
        section_id=payload.section_id,
        faculty_id=current_user.id,
        title=payload.title,
        deadline=payload.deadline
    )
    
    # Notify HOD, Students, and Parents
    from app.services.notification_service import NotificationService
    notif_service = NotificationService(db)
    
    # 1. Notify HOD
    if current_user.department_id:
        from app.db.models.academic import Department
        dept_q = await db.execute(select(Department).where(Department.id == current_user.department_id))
        dept = dept_q.scalars().first()
        if dept and dept.hod_id:
            await notif_service.send_notification(
                user_id=dept.hod_id,
                type_val="new_assignment",
                message=f"Faculty {current_user.full_name} has published a new assignment '{payload.title}'."
            )
            
    # 2. Notify students and parents
    from app.db.models.student import Student, ParentStudentMap
    students_q = await db.execute(select(Student).where(Student.section_id == payload.section_id, Student.is_deleted.is_(False)))
    for student in students_q.scalars().all():
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
    return {"detail": "Assignment created successfully", "assignment_id": assign.id}

@router.post("/research", response_model=ResearchResponse)
async def create_research(
    payload: ResearchEntryRequest,
    current_user: User = Depends(role_required([UserRole.FACULTY, UserRole.HOD])),
    db: AsyncSession = Depends(get_db_session)
) -> ResearchResponse:
    res = FacultyResearch(
        faculty_id=current_user.id,
        title=payload.title,
        publication=payload.publication,
        grant_amount=payload.grant_amount,
        proof_file_url=payload.proof_file_url,
        status="PENDING" if payload.proof_file_url else "APPROVED"
    )
    db.add(res)
    await db.flush()
    
    # Log submission to AuditLog
    audit_entry = AuditLog(
        user_id=current_user.id,
        action="RESEARCH_SUBMITTED" if payload.proof_file_url else "RESEARCH_CREATED",
        entity="FacultyResearch",
        entity_id=res.id,
        timestamp=datetime.now()
    )
    db.add(audit_entry)
    
    await db.commit()
    return ResearchResponse(
        id=res.id, 
        title=res.title, 
        publication=res.publication, 
        grant_amount=res.grant_amount,
        proof_file_url=res.proof_file_url,
        status=res.status,
        comments=res.comments
    )

@router.get("/notices", response_model=list[NoticeResponse])
async def get_notices(
    current_user: User = Depends(role_required([UserRole.FACULTY, UserRole.HOD])),
    db: AsyncSession = Depends(get_db_session)
) -> list[NoticeResponse]:
    from app.db.models.communication import Notice, NoticeAcknowledgement
    q = (
        select(Notice, User)
        .join(NoticeAcknowledgement, NoticeAcknowledgement.notice_id == Notice.id)
        .join(User, Notice.created_by == User.id)
        .where(NoticeAcknowledgement.user_id == current_user.id, Notice.is_deleted.is_(False))
        .order_by(Notice.publish_date.desc())
    )
    res = await db.execute(q)
    rows = res.all()
    return [
        NoticeResponse(
            id=str(r.id),
            title=r.title,
            body=r.body,
            audience_type=r.audience_type,
            publish_date=r.publish_date,
            event_date=r.event_date,
            audience_types=r.audience_types,
            degree_id=r.degree_id,
            batch_id=r.batch_id,
            department_id=r.department_id,
            attachment_url=r.attachment_url,
            priority=r.priority,
            publisher_name=creator.full_name,
            publisher_role=r.publisher_role or (creator.role.value if hasattr(creator.role, "value") else str(creator.role))
        )
        for r, creator in rows
    ]

# --- HOD DEPT OPERATIONS ROUTERS ---

# Helper functions to get active users
async def get_active_faculty_ids(db: AsyncSession, department_id: str | None = None) -> set[str]:
    q = select(User.id).where(
        User.role.in_([UserRole.FACULTY, UserRole.HOD]),
        User.is_active.is_(True),
        User.is_deleted.is_(False)
    )
    if department_id:
        q = q.where(User.department_id == department_id)
    res = await db.execute(q)
    return set(res.scalars().all())

async def get_active_student_ids(db: AsyncSession, department_id: str | None = None) -> set[str]:
    q = select(User.id).where(
        User.role == UserRole.STUDENT,
        User.is_active.is_(True),
        User.is_deleted.is_(False)
    )
    if department_id:
        q = q.where(User.department_id == department_id)
    res = await db.execute(q)
    return set(res.scalars().all())


async def get_hod_department_id(user: User, db: AsyncSession) -> str | None:
    if user.department_id:
        return user.department_id
    from app.db.models.academic import Department
    dept_q = await db.execute(select(Department.id).where(Department.hod_id == user.id, Department.is_deleted.is_(False)))
    return dept_q.scalar_one_or_none()


@router.get("/hod/dashboard", response_model=DashboardResponse)
async def hod_dashboard(
    current_user: User = Depends(role_required([UserRole.HOD])),
    db: AsyncSession = Depends(get_db_session)
) -> DashboardResponse:
    dept_id = await get_hod_department_id(current_user, db)
    if not dept_id:
        from app.db.models.academic import Department
        dept_q = await db.execute(select(Department).where(Department.is_deleted.is_(False)).order_by(Department.code))
        dept = dept_q.scalars().first()
        if dept:
            dept_id = dept.id
    active_faculty = await get_active_faculty_ids(db, dept_id)

    # Resolve pending materials verification (excluding deleted materials, and only from active faculty in department)
    result = await db.execute(
        select(StudyMaterial).where(
            StudyMaterial.is_verified.is_(False),
            StudyMaterial.is_deleted.is_(False),
            StudyMaterial.faculty_id.in_(active_faculty)
        )
    )
    materials = result.scalars().all()
    # Deduplicate study materials by (title, section_id, faculty_id, type, file_url)
    seen_materials = set()
    unique_pending_materials = 0
    for m in materials:
        m_key = (m.title, m.section_id, m.faculty_id, m.type, m.file_url)
        if m_key not in seen_materials:
            seen_materials.add(m_key)
            unique_pending_materials += 1

    # Average teaching hours (only from active faculty in department)
    workload_q = await db.execute(
        select(FacultyWorkload).where(
            FacultyWorkload.is_deleted.is_(False),
            FacultyWorkload.faculty_id.in_(active_faculty)
        )
    )
    workloads = workload_q.scalars().all()
    # Deduplicate workloads by (faculty_id, semester)
    seen_workloads = set()
    unique_workloads = []
    for w in workloads:
        w_key = (w.faculty_id, w.semester)
        if w_key not in seen_workloads:
            seen_workloads.add(w_key)
            unique_workloads.append(w)
            
    avg_hours = sum(w.teaching_hours for w in unique_workloads) / len(unique_workloads) if unique_workloads else 0

    # Calculate Department Health Index based on average student attendance
    health_stmt = select(
        func.count(Attendance.id),
        func.count(Attendance.id).filter(Attendance.status.in_(["present", "od"]))
    ).where(Attendance.is_deleted.is_(False))
    
    health_res = await db.execute(health_stmt)
    health_row = health_res.first()
    total_att = health_row[0] if health_row else 0
    present_att = health_row[1] if health_row else 0
    health_idx = int((present_att / total_att) * 100) if total_att > 0 else 100

    return DashboardResponse(
        metrics=[
            MetricSchema(id="health", label="Department Health Index", value=f"{health_idx}%"),
            MetricSchema(id="faculty", label="Active Faculty Count", value=str(len(workloads) or 1)),
            MetricSchema(id="workload", label="Avg Workload Hours", value=f"{avg_hours:.1f} hrs"),
            MetricSchema(id="pending_materials", label="Pending Verifications", value=str(unique_pending_materials)),
        ]
    )

@router.get("/hod/workload", response_model=list[WorkloadResponse])
async def get_faculty_workloads(
    dept_id: str | None = None,
    current_user: User = Depends(role_required([UserRole.HOD, UserRole.PRINCIPAL])),
    db: AsyncSession = Depends(get_db_session)
) -> list[WorkloadResponse]:
    target_dept_id = dept_id
    if current_user.role == UserRole.HOD:
        target_dept_id = current_user.department_id
    elif not target_dept_id:
        target_dept_id = current_user.department_id
        if not target_dept_id:
            from app.db.models.academic import Department
            dept_q = await db.execute(select(Department).where(Department.is_deleted.is_(False)).order_by(Department.code))
            first_dept = dept_q.scalars().first()
            if first_dept:
                target_dept_id = first_dept.id

    active_faculty = await get_active_faculty_ids(db, target_dept_id)
    if not active_faculty:
        return []

    result = await db.execute(
        select(FacultyWorkload).where(
            FacultyWorkload.is_deleted.is_(False),
            FacultyWorkload.faculty_id.in_(active_faculty)
        )
    )
    workloads = result.scalars().all()
    
    response_list = []
    seen_workloads = set()
    for w in workloads:
        w_key = (w.faculty_id, w.semester)
        if w_key in seen_workloads:
            continue
        seen_workloads.add(w_key)
        
        fac_q = await db.execute(select(User).where(User.id == w.faculty_id))
        fac = fac_q.scalar_one_or_none()
        response_list.append(
            WorkloadResponse(
                faculty_id=w.faculty_id,
                faculty_name=fac.full_name if fac else "Faculty member",
                semester=w.semester,
                teaching_hours=w.teaching_hours
            )
        )
    return response_list

@router.get("/hod/leaves")
async def hod_get_leaves(
    current_user: User = Depends(role_required([UserRole.HOD])),
    db: AsyncSession = Depends(get_db_session)
):
    from app.db.models.student import Student
    from app.db.models.leave import LeaveRequest, LeaveStatus

    dept_id = current_user.department_id
    if not dept_id:
        return {
            "metrics": {"pending_requests": 0, "approved_requests": 0, "rejected_requests": 0, "od_requests": 0},
            "faculty_leaves": [],
            "student_leaves": [],
            "eo_tag_verifications": []
        }

    # ── Faculty leaves: only show PENDING_HOD (new workflow) ──────────────
    fac_stmt = select(User).where(
        User.department_id == dept_id,
        User.role == UserRole.FACULTY,
        User.is_deleted.is_(False)
    )
    fac_res = await db.execute(fac_stmt)
    faculty_users = fac_res.scalars().all()
    faculty_map = {u.id: u.full_name for u in faculty_users}

    # ── Student leaves: use legacy PENDING/ADVISOR_APPROVED flow ──────────
    stud_stmt = (
        select(Student, User)
        .join(User, Student.user_id == User.id)
        .where(
            Student.department_id == dept_id,
            Student.is_deleted.is_(False)
        )
    )
    stud_res = await db.execute(stud_stmt)
    student_rows = stud_res.all()
    student_map = {u.id: u.full_name for s, u in student_rows}
    student_info_map = {s.user_id: s for s, u in student_rows}

    from app.db.models.academic import Section
    sec_q = await db.execute(select(Section).where(Section.faculty_id.is_not(None), Section.is_deleted.is_(False)))
    sec_with_advisor = {sec.id for sec in sec_q.scalars().all()}

    # ── Fetch leaves ──────────────────────────────────────────────────────
    all_faculty_ids = list(faculty_map.keys())
    all_student_ids = list(student_map.keys())
    all_user_ids = all_faculty_ids + all_student_ids

    if not all_user_ids:
        return {
            "metrics": {"pending_requests": 0, "approved_requests": 0, "rejected_requests": 0, "od_requests": 0},
            "faculty_leaves": [],
            "student_leaves": [],
            "eo_tag_verifications": []
        }

    leaves_stmt = (
        select(LeaveRequest, User)
        .join(User, LeaveRequest.user_id == User.id)
        .where(
            LeaveRequest.user_id.in_(all_user_ids),
            LeaveRequest.is_deleted.is_(False)
        )
        .order_by(LeaveRequest.from_date.desc())
    )
    leaves_res = await db.execute(leaves_stmt)
    leaves_rows = leaves_res.all()

    faculty_leaves = []
    student_leaves = []
    eo_tag_verifications = []

    pending_count = 0
    approved_count = 0
    rejected_count = 0
    od_count = 0

    seen_leaves = set()

    for lr, user in leaves_rows:
        l_key = (lr.user_id, lr.from_date, lr.to_date, lr.type)
        if l_key in seen_leaves:
            continue
        seen_leaves.add(l_key)

        is_pending = False
        is_approved = False
        is_rejected = False

        if user.role == UserRole.FACULTY:
            # New workflow: only PENDING_HOD shown as actionable
            if lr.status == LeaveStatus.PENDING_HOD:
                is_pending = True
            elif lr.status in [LeaveStatus.PENDING_PRINCIPAL, LeaveStatus.APPROVED_BY_HOD, LeaveStatus.FINAL_APPROVED, LeaveStatus.HOD_APPROVED, LeaveStatus.PRINCIPAL_APPROVED, LeaveStatus.APPROVED]:
                is_approved = True
            elif lr.status in [LeaveStatus.REJECTED_BY_HOD, LeaveStatus.REJECTED_BY_PRINCIPAL, LeaveStatus.HOD_REJECTED, LeaveStatus.PRINCIPAL_REJECTED, LeaveStatus.REJECTED]:
                is_rejected = True
            else:
                continue  # skip PENDING (student flow) and other old statuses

        elif user.role == UserRole.STUDENT:
            stud_obj = student_info_map.get(user.id)
            has_advisor = False
            if stud_obj:
                has_advisor = (stud_obj.mentor_id is not None) or (stud_obj.section_id in sec_with_advisor)

            if lr.status == LeaveStatus.ADVISOR_APPROVED:
                is_pending = True
            elif lr.status == LeaveStatus.PENDING and not has_advisor:
                is_pending = True
            elif lr.status in [LeaveStatus.APPROVED, LeaveStatus.HOD_APPROVED]:
                is_approved = True
            elif lr.status == LeaveStatus.REJECTED:
                is_rejected = True
            elif lr.status == LeaveStatus.PENDING and has_advisor:
                continue

        # Count metrics
        if is_pending:
            pending_count += 1
        elif is_approved:
            approved_count += 1
        elif is_rejected:
            rejected_count += 1

        if lr.type == "OD":
            od_count += 1

        # Status label for UI
        if is_pending:
            display_status = "PENDING_HOD" if user.role == UserRole.FACULTY else "PENDING"
        elif user.role == UserRole.FACULTY:
            display_status = lr.status.value
        else:
            display_status = lr.status.value

        item = {
            "id": lr.id,
            "user_id": user.id,
            "applicant_name": user.full_name,
            "type": lr.type,
            "from_date": str(lr.from_date),
            "to_date": str(lr.to_date),
            "num_days": getattr(lr, "num_days", 1.0),
            "reason": lr.reason,
            "emergency_contact": getattr(lr, "emergency_contact", None),
            "attachment_url": getattr(lr, "attachment_url", None),
            "status": display_status,
            "hod_remarks": getattr(lr, "hod_remarks", None),
            "principal_remarks": getattr(lr, "principal_remarks", None),
        }

        if user.role == UserRole.FACULTY:
            faculty_leaves.append(item)
        elif user.role == UserRole.STUDENT:
            if lr.type == "OD":
                eo_tag_verifications.append(item)
            student_leaves.append(item)

    return {
        "metrics": {
            "pending_requests": pending_count,
            "approved_requests": approved_count,
            "rejected_requests": rejected_count,
            "od_requests": od_count
        },
        "faculty_leaves": faculty_leaves,
        "student_leaves": student_leaves,
        "eo_tag_verifications": eo_tag_verifications
    }


@router.post("/hod/leaves/approve/{leave_id}")
async def hod_approve_leave(
    leave_id: str,
    payload: LeaveVerifyRequest,
    current_user: User = Depends(role_required([UserRole.HOD])),
    db: AsyncSession = Depends(get_db_session)
) -> dict:
    from app.db.models.leave import LeaveStatus, LeaveRequest
    from datetime import datetime, timezone

    # Fetch leave
    leave_q = await db.execute(select(LeaveRequest).where(LeaveRequest.id == leave_id, LeaveRequest.is_deleted.is_(False)))
    leave = leave_q.scalar_one_or_none()
    if not leave:
        raise HTTPException(status_code=404, detail="Leave request not found")

    action = payload.status.upper() if isinstance(payload.status, str) else payload.status.value

    # ── APPROVE → forward to Principal ──────────────────────────────────
    if action == "APPROVED" or action == "HOD_APPROVED" or action == "APPROVED_BY_HOD" or action == "PENDING_PRINCIPAL":
        leave.status = LeaveStatus.PENDING_PRINCIPAL
        leave.hod_status = "APPROVED"
        leave.hod_action_by = current_user.id
        leave.hod_action_date = datetime.utcnow()
        leave.hod_remarks = None
        db.add(leave)
        await db.commit()

        # Notify faculty applicant
        from app.services.notification_service import NotificationService
        notif_service = NotificationService(db)
        app_q = await db.execute(select(User).where(User.id == leave.user_id))
        applicant = app_q.scalar_one_or_none()
        if applicant:
            await notif_service.send_notification(
                user_id=applicant.id,
                type_val="leave_approval",
                message=f"Your {leave.type} leave request ({leave.from_date} to {leave.to_date}) has been approved by HOD {current_user.full_name} and forwarded to Principal for final approval."
            )

        # Notify all principals
        principal_q = await db.execute(select(User).where(User.role == UserRole.PRINCIPAL, User.is_deleted.is_(False)))
        for principal in principal_q.scalars().all():
            await notif_service.send_notification(
                user_id=principal.id,
                type_val="leave_request",
                message=f"{applicant.full_name if applicant else 'A faculty member'}'s {leave.type} leave request ({leave.from_date} to {leave.to_date}) has been approved by HOD {current_user.full_name} and is awaiting your final decision."
            )

        await db.commit()
        return {"detail": "Leave approved by HOD. Forwarded to Principal for final approval."}

    # ── REJECT → mandatory remarks, workflow ends ────────────────────────
    elif action == "REJECTED" or action == "HOD_REJECTED" or action == "REJECTED_BY_HOD":
        remarks = getattr(payload, "remarks", None)
        if not remarks or not remarks.strip():
            raise HTTPException(status_code=422, detail="Remarks are required for rejection.")

        leave.status = LeaveStatus.REJECTED_BY_HOD
        leave.hod_status = "REJECTED"
        leave.hod_action_by = current_user.id
        leave.hod_action_date = datetime.utcnow()
        leave.hod_remarks = remarks.strip()
        db.add(leave)
        await db.commit()

        # Notify faculty applicant of rejection
        from app.services.notification_service import NotificationService
        notif_service = NotificationService(db)
        app_q = await db.execute(select(User).where(User.id == leave.user_id))
        applicant = app_q.scalar_one_or_none()
        if applicant:
            await notif_service.send_notification(
                user_id=applicant.id,
                type_val="leave_rejection",
                message=f"Your {leave.type} leave request ({leave.from_date} to {leave.to_date}) has been rejected by HOD {current_user.full_name}. Remarks: {remarks.strip()}"
            )

        await db.commit()
        return {"detail": "Leave rejected by HOD. Faculty has been notified."}

    else:
        raise HTTPException(status_code=400, detail=f"Invalid action: {payload.status}")

@router.get("/hod/materials", response_model=list[StudyMaterialResponse])
async def hod_get_materials(
    dept_id: str | None = None,
    current_user: User = Depends(role_required([UserRole.HOD, UserRole.PRINCIPAL])),
    db: AsyncSession = Depends(get_db_session)
) -> list[StudyMaterialResponse]:
    target_dept_id = dept_id
    if current_user.role == UserRole.HOD:
        target_dept_id = current_user.department_id
    elif not target_dept_id:
        target_dept_id = current_user.department_id
        if not target_dept_id:
            from app.db.models.academic import Department
            dept_q = await db.execute(select(Department).where(Department.is_deleted.is_(False)).order_by(Department.code))
            first_dept = dept_q.scalars().first()
            if first_dept:
                target_dept_id = first_dept.id

    active_faculty = await get_active_faculty_ids(db, target_dept_id)
    if not active_faculty:
        return []

    # Get active semesters from academic_years
    from app.db.models.academic import AcademicYear
    ay_query = select(AcademicYear.current_semester).where(
        AcademicYear.is_active == True,
        AcademicYear.is_deleted == False
    )
    ay_res = await db.execute(ay_query)
    active_semesters = [r for r in ay_res.scalars().all() if r is not None]

    q = (
        select(StudyMaterial, User.full_name, Section.section_name, Course.name, Course.semester)
        .outerjoin(User, StudyMaterial.faculty_id == User.id)
        .outerjoin(Section, StudyMaterial.section_id == Section.id)
        .outerjoin(Course, Section.course_id == Course.id)
        .where(
            StudyMaterial.is_deleted.is_(False),
            StudyMaterial.faculty_id.in_(active_faculty)
        )
    )
    if active_semesters:
        q = q.where(Course.semester.in_(active_semesters))
        
    result = await db.execute(q)
    rows = result.all()
    
    response_list = []
    seen_materials = set()
    for material, faculty_name, section_name, course_name, semester in rows:
        m_key = (material.title, material.section_id, material.faculty_id, material.type, material.file_url)
        if m_key in seen_materials:
            continue
        seen_materials.add(m_key)
        
        response_list.append(
            StudyMaterialResponse(
                id=material.id, 
                title=material.title, 
                type=material.type, 
                file_url=material.file_url, 
                is_verified=material.is_verified,
                status=material.status if hasattr(material, "status") else "PENDING",
                comments=material.comments if hasattr(material, "comments") else None,
                faculty_name=faculty_name,
                section_name=section_name,
                subject=course_name,
                semester=semester,
                created_at=material.created_at
            )
        )
    return response_list

@router.post("/hod/materials/verify/{material_id}")
async def hod_verify_material(
    material_id: str,
    payload: LeaveVerifyRequest,
    current_user: User = Depends(role_required([UserRole.HOD, UserRole.PRINCIPAL])),
    db: AsyncSession = Depends(get_db_session)
) -> dict:
    q = select(StudyMaterial).where(StudyMaterial.id == material_id)
    res = await db.execute(q)
    material = res.scalar_one_or_none()
    if not material:
        raise HTTPException(status_code=404, detail="Study material not found.")
        
    is_verified = (payload.status == "APPROVED")
    material.is_verified = is_verified
    material.status = payload.status
    material.comments = payload.remarks
    db.add(material)
    
    # Log to AuditLog
    audit_entry = AuditLog(
        user_id=current_user.id,
        action=f"VERIFY_{payload.status}",
        entity="StudyMaterial",
        entity_id=material_id,
        timestamp=datetime.now()
    )
    db.add(audit_entry)
    
    await db.commit()
    return {"detail": f"Material verification set to {is_verified}"}


# --- FACULTY PROFILE MODULE ENDPOINTS ---

async def get_or_create_faculty_profile(user_id: str, db: AsyncSession) -> FacultyProfile:
    q = await db.execute(select(FacultyProfile).where(FacultyProfile.user_id == user_id))
    profile = q.scalar_one_or_none()
    if not profile:
        profile = FacultyProfile(
            user_id=user_id,
            faculty_id=user_id[:8].upper(),
            designation="Assistant Professor",
            specialization="",
            educational_qualifications=[],
            experience_details=[],
            academic_responsibilities=[],
            certifications_achievements=[],
            promotion_history=[],
            increment_history=[],
            documents_repository={},
            notification_preferences={}
        )
        db.add(profile)
        await db.flush()
    else:
        if not profile.faculty_id:
            profile.faculty_id = user_id[:8].upper()
            db.add(profile)
            await db.flush()
    return profile

async def build_profile_response(user: User, profile: FacultyProfile, db: AsyncSession) -> FacultyProfileResponse:
    dept_name = None
    if user.department_id:
        dept_q = await db.execute(select(Department).where(Department.id == user.department_id))
        dept = dept_q.scalar_one_or_none()
        dept_name = dept.name if dept else None

    hod_name = None
    if profile.reporting_hod_id:
        hod_q = await db.execute(select(User).where(User.id == profile.reporting_hod_id))
        hod = hod_q.scalar_one_or_none()
        hod_name = hod.full_name if hod else None

    principal_name = None
    if profile.reporting_principal_id:
        pr_q = await db.execute(select(User).where(User.id == profile.reporting_principal_id))
        pr = pr_q.scalar_one_or_none()
        principal_name = pr.full_name if pr else None

    # Parse JSON properties safely to match response schema list/dict types
    def list_val(val):
        return list(val) if val is not None else []

    def dict_val(val):
        return dict(val) if val is not None else {}

    return FacultyProfileResponse(
        user_id=user.id,
        full_name=user.full_name,
        email=user.email,
        phone=user.phone,
        department_id=user.department_id,
        department_name=dept_name,
        designation=profile.designation,
        specialization=profile.specialization,
        faculty_id=profile.faculty_id,
        employee_code=profile.employee_code,
        gender=profile.gender,
        date_of_birth=profile.date_of_birth,
        blood_group=profile.blood_group,
        marital_status=profile.marital_status,
        nationality=profile.nationality,
        community=profile.community,
        alternate_phone=profile.alternate_phone,
        personal_email=profile.personal_email,
        current_address=profile.current_address,
        permanent_address=profile.permanent_address,
        city=profile.city,
        state=profile.state,
        pincode=profile.pincode,
        profile_photo_url=profile.profile_photo_url,
        faculty_type=profile.faculty_type,
        employment_category=profile.employment_category,
        date_of_joining=profile.date_of_joining,
        employment_status=profile.employment_status,
        reporting_hod_id=profile.reporting_hod_id,
        reporting_hod_name=hod_name,
        reporting_principal_id=profile.reporting_principal_id,
        reporting_principal_name=principal_name,
        confirmation_date=profile.confirmation_date,
        educational_qualifications=list_val(profile.educational_qualifications),
        experience_details=list_val(profile.experience_details),
        academic_responsibilities=list_val(profile.academic_responsibilities),
        certifications_achievements=list_val(profile.certifications_achievements),
        promotion_history=list_val(profile.promotion_history),
        increment_history=list_val(profile.increment_history),
        documents_repository=dict_val(profile.documents_repository),
        notification_preferences=dict_val(profile.notification_preferences),
        approval_status=profile.approval_status
    )

@router.get("/profile", response_model=FacultyProfileResponse)
async def get_my_profile(
    current_user: User = Depends(role_required([UserRole.FACULTY, UserRole.HOD, UserRole.PRINCIPAL])),
    db: AsyncSession = Depends(get_db_session)
) -> FacultyProfileResponse:
    profile = await get_or_create_faculty_profile(current_user.id, db)
    return await build_profile_response(current_user, profile, db)

@router.put("/profile", response_model=FacultyProfileResponse)
async def update_my_profile(
    payload: FacultyProfileUpdateRequestSchema,
    current_user: User = Depends(role_required([UserRole.FACULTY, UserRole.HOD, UserRole.PRINCIPAL])),
    db: AsyncSession = Depends(get_db_session)
) -> FacultyProfileResponse:
    profile = await get_or_create_faculty_profile(current_user.id, db)
    
    # Update allowed fields
    if payload.marital_status is not None: profile.marital_status = payload.marital_status
    if payload.community is not None: profile.community = payload.community
    if payload.alternate_phone is not None: profile.alternate_phone = payload.alternate_phone
    if payload.personal_email is not None: profile.personal_email = payload.personal_email
    if payload.current_address is not None: profile.current_address = payload.current_address
    if payload.permanent_address is not None: profile.permanent_address = payload.permanent_address
    if payload.city is not None: profile.city = payload.city
    if payload.state is not None: profile.state = payload.state
    if payload.pincode is not None: profile.pincode = payload.pincode
    if payload.profile_photo_url is not None: profile.profile_photo_url = payload.profile_photo_url
    
    if payload.educational_qualifications is not None:
        profile.educational_qualifications = [q.model_dump() for q in payload.educational_qualifications]
    if payload.experience_details is not None:
        profile.experience_details = [e.model_dump() for e in payload.experience_details]
    if payload.certifications_achievements is not None:
        profile.certifications_achievements = payload.certifications_achievements
    if payload.notification_preferences is not None:
        profile.notification_preferences = payload.notification_preferences
        
    await db.commit()
    await db.refresh(profile)
    return await build_profile_response(current_user, profile, db)


@router.get("/profile/update-requests/my", response_model=list[FacultyProfileUpdateRequestResponse])
async def get_my_profile_update_requests(
    current_user: User = Depends(role_required([UserRole.FACULTY, UserRole.HOD, UserRole.PRINCIPAL])),
    db: AsyncSession = Depends(get_db_session)
) -> list[FacultyProfileUpdateRequestResponse]:
    q = await db.execute(
        select(FacultyProfileUpdateRequest)
        .where(FacultyProfileUpdateRequest.user_id == current_user.id, FacultyProfileUpdateRequest.is_deleted.is_(False))
        .order_by(FacultyProfileUpdateRequest.created_at.desc())
    )
    requests = q.scalars().all()
    
    dept_name = None
    if current_user.department_id:
        dept_obj = await db.get(Department, current_user.department_id)
        dept_name = dept_obj.name if dept_obj else None
        
    res_list = []
    for r in requests:
        res_list.append(
            FacultyProfileUpdateRequestResponse(
                id=r.id,
                user_id=r.user_id,
                full_name=current_user.full_name,
                department_name=dept_name,
                status=r.status,
                faculty_id=r.faculty_id,
                employee_code=r.employee_code,
                official_email=r.official_email,
                official_phone=r.official_phone,
                gender=r.gender,
                date_of_birth=r.date_of_birth,
                blood_group=r.blood_group,
                nationality=r.nationality,
                requested_designation=r.designation,
                requested_department_name=r.department_name,
                comments=r.comments,
                processed_at=r.processed_at,
                processed_by=r.processed_by,
                created_at=r.created_at
            )
        )
    return res_list


@router.post("/profile/update-requests", response_model=FacultyProfileUpdateRequestResponse)
async def submit_profile_update_request(
    payload: FacultyProfileUpdateRequestCreate,
    current_user: User = Depends(role_required([UserRole.FACULTY, UserRole.HOD, UserRole.PRINCIPAL])),
    db: AsyncSession = Depends(get_db_session)
) -> FacultyProfileUpdateRequestResponse:
    pending_q = await db.execute(
        select(FacultyProfileUpdateRequest)
        .where(
            FacultyProfileUpdateRequest.user_id == current_user.id,
            FacultyProfileUpdateRequest.status == "PENDING",
            FacultyProfileUpdateRequest.is_deleted.is_(False)
        )
    )
    if pending_q.scalar_one_or_none():
        raise HTTPException(
            status_code=status.HTTP_400_BAD_REQUEST,
            detail="You already have a pending profile update request awaiting HOD approval."
        )
        
    if payload.gender is not None and (payload.gender.strip() == "" or payload.gender.strip() == "N/A"):
        raise HTTPException(status_code=400, detail="Gender is required and cannot be empty or N/A.")
    if payload.blood_group is not None and (payload.blood_group.strip() == "" or payload.blood_group.strip() == "N/A"):
        raise HTTPException(status_code=400, detail="Blood Group is required and cannot be empty or N/A.")
    if payload.nationality is not None and (payload.nationality.strip() == "" or payload.nationality.strip() == "N/A"):
        raise HTTPException(status_code=400, detail="Nationality is required and cannot be empty or N/A.")
    if payload.official_phone is not None and (payload.official_phone.strip() == "" or payload.official_phone.strip() == "N/A"):
        raise HTTPException(status_code=400, detail="Official Phone is required and cannot be empty or N/A.")
        
    new_req = FacultyProfileUpdateRequest(
        user_id=current_user.id,
        status="PENDING",
        faculty_id=payload.faculty_id,
        employee_code=payload.employee_code,
        official_email=payload.official_email,
        official_phone=payload.official_phone,
        gender=payload.gender,
        date_of_birth=payload.date_of_birth,
        blood_group=payload.blood_group,
        nationality=payload.nationality,
        designation=payload.designation,
        department_name=payload.department_name
    )
    db.add(new_req)
    await db.flush()
    
    audit = AuditLog(
        user_id=current_user.id,
        action="SUBMIT_PROFILE_UPDATE_REQUEST",
        entity="FacultyProfileUpdateRequest",
        entity_id=new_req.id,
        timestamp=datetime.now()
    )
    db.add(audit)
    
    await db.commit()
    await db.refresh(new_req)
    
    dept_name = None
    if current_user.department_id:
        dept_obj = await db.get(Department, current_user.department_id)
        dept_name = dept_obj.name if dept_obj else None
        
    return FacultyProfileUpdateRequestResponse(
        id=new_req.id,
        user_id=new_req.user_id,
        full_name=current_user.full_name,
        department_name=dept_name,
        status=new_req.status,
        faculty_id=new_req.faculty_id,
        employee_code=new_req.employee_code,
        official_email=new_req.official_email,
        official_phone=new_req.official_phone,
        gender=new_req.gender,
        date_of_birth=new_req.date_of_birth,
        blood_group=new_req.blood_group,
        nationality=new_req.nationality,
        requested_designation=new_req.designation,
        requested_department_name=new_req.department_name,
        created_at=new_req.created_at
    )


@router.get("/profile/update-requests/pending", response_model=list[FacultyProfileUpdateRequestResponse])
async def get_hod_pending_profile_requests(
    current_user: User = Depends(role_required([UserRole.HOD])),
    db: AsyncSession = Depends(get_db_session)
) -> list[FacultyProfileUpdateRequestResponse]:
    dept_id = await get_hod_department_id(current_user, db)
    if not dept_id:
        return []
        
    faculties_q = await db.execute(
        select(User).where(
            User.department_id == dept_id,
            User.role == UserRole.FACULTY,
            User.is_deleted.is_(False)
        )
    )
    faculty_users = faculties_q.scalars().all()
    faculty_ids = [f.id for f in faculty_users]
    
    if not faculty_ids:
        return []
        
    q = await db.execute(
        select(FacultyProfileUpdateRequest)
        .where(
            FacultyProfileUpdateRequest.user_id.in_(faculty_ids),
            FacultyProfileUpdateRequest.status == "PENDING",
            FacultyProfileUpdateRequest.is_deleted.is_(False)
        )
        .order_by(FacultyProfileUpdateRequest.created_at.desc())
    )
    requests = q.scalars().all()
    
    user_map = {f.id: f for f in faculty_users}
    dept_name = None
    if dept_id:
        dept_obj = await db.get(Department, dept_id)
        dept_name = dept_obj.name if dept_obj else None
        
    res_list = []
    for r in requests:
        f_user = user_map.get(r.user_id)
        res_list.append(
            FacultyProfileUpdateRequestResponse(
                id=r.id,
                user_id=r.user_id,
                full_name=f_user.full_name if f_user else "Faculty Member",
                department_name=dept_name,
                status=r.status,
                faculty_id=r.faculty_id,
                employee_code=r.employee_code,
                official_email=r.official_email,
                official_phone=r.official_phone,
                gender=r.gender,
                date_of_birth=r.date_of_birth,
                blood_group=r.blood_group,
                nationality=r.nationality,
                requested_designation=r.designation,
                requested_department_name=r.department_name,
                comments=r.comments,
                created_at=r.created_at
            )
        )
    return res_list


@router.get("/profile/update-requests/history", response_model=list[FacultyProfileUpdateRequestResponse])
async def get_hod_profile_requests_history(
    current_user: User = Depends(role_required([UserRole.HOD])),
    db: AsyncSession = Depends(get_db_session)
) -> list[FacultyProfileUpdateRequestResponse]:
    dept_id = await get_hod_department_id(current_user, db)
    if not dept_id:
        return []
        
    faculties_q = await db.execute(
        select(User).where(
            User.department_id == dept_id,
            User.role == UserRole.FACULTY,
            User.is_deleted.is_(False)
        )
    )
    faculty_users = faculties_q.scalars().all()
    faculty_ids = [f.id for f in faculty_users]
    
    if not faculty_ids:
        return []
        
    q = await db.execute(
        select(FacultyProfileUpdateRequest)
        .where(
            FacultyProfileUpdateRequest.user_id.in_(faculty_ids),
            FacultyProfileUpdateRequest.status.in_(["APPROVED", "REJECTED", "CHANGES_REQUESTED"]),
            FacultyProfileUpdateRequest.is_deleted.is_(False)
        )
        .order_by(FacultyProfileUpdateRequest.processed_at.desc())
    )
    requests = q.scalars().all()
    
    hod_ids = list(set([r.processed_by for r in requests if r.processed_by]))
    hods = []
    if hod_ids:
        hods_q = await db.execute(select(User).where(User.id.in_(hod_ids)))
        hods = hods_q.scalars().all()
    hod_map = {h.id: h.full_name for h in hods}
    user_map = {f.id: f for f in faculty_users}
    dept_name = None
    if dept_id:
        dept_obj = await db.get(Department, dept_id)
        dept_name = dept_obj.name if dept_obj else None
        
    res_list = []
    for r in requests:
        f_user = user_map.get(r.user_id)
        res_list.append(
            FacultyProfileUpdateRequestResponse(
                id=r.id,
                user_id=r.user_id,
                full_name=f_user.full_name if f_user else "Faculty Member",
                department_name=dept_name,
                status=r.status,
                faculty_id=r.faculty_id,
                employee_code=r.employee_code,
                official_email=r.official_email,
                official_phone=r.official_phone,
                gender=r.gender,
                date_of_birth=r.date_of_birth,
                blood_group=r.blood_group,
                nationality=r.nationality,
                requested_designation=r.designation,
                requested_department_name=r.department_name,
                comments=r.comments,
                processed_at=r.processed_at,
                processed_by=r.processed_by,
                processed_by_name=hod_map.get(r.processed_by) if r.processed_by else None,
                created_at=r.created_at
            )
        )
    return res_list


@router.post("/profile/update-requests/{request_id}/approve", response_model=FacultyProfileUpdateRequestResponse)
async def approve_profile_update_request(
    request_id: str,
    payload: FacultyProfileUpdateRequestReview,
    current_user: User = Depends(role_required([UserRole.HOD])),
    db: AsyncSession = Depends(get_db_session)
) -> FacultyProfileUpdateRequestResponse:
    req = await db.get(FacultyProfileUpdateRequest, request_id)
    if not req or req.is_deleted or req.status != "PENDING":
        raise HTTPException(status_code=404, detail="Pending request not found.")
        
    req.status = "APPROVED"
    req.comments = payload.comments
    req.processed_at = datetime.now()
    req.processed_by = current_user.id
    
    profile = await get_or_create_faculty_profile(req.user_id, db)
    if req.faculty_id is not None: profile.faculty_id = req.faculty_id
    if req.employee_code is not None: profile.employee_code = req.employee_code
    if req.gender is not None: profile.gender = req.gender
    if req.date_of_birth is not None: profile.date_of_birth = req.date_of_birth
    if req.blood_group is not None: profile.blood_group = req.blood_group
    if req.nationality is not None: profile.nationality = req.nationality
    if req.designation is not None: profile.designation = req.designation
    
    target_user = await db.get(User, req.user_id)
    if target_user:
        if req.official_email is not None: target_user.email = req.official_email
        if req.official_phone is not None: target_user.phone = req.official_phone
        if req.department_name is not None:
            from app.db.models.academic import Department
            dept_q = await db.execute(select(Department).where(Department.name == req.department_name, Department.is_deleted.is_(False)))
            dept_obj = dept_q.scalar_one_or_none()
            if dept_obj:
                target_user.department_id = dept_obj.id
        db.add(target_user)
        
    profile.approval_status = "APPROVED"
    db.add(profile)
    db.add(req)
    
    audit = AuditLog(
        user_id=current_user.id,
        action="APPROVE_PROFILE_UPDATE_REQUEST",
        entity="FacultyProfileUpdateRequest",
        entity_id=request_id,
        timestamp=datetime.now()
    )
    db.add(audit)
    
    await db.commit()
    await db.refresh(req)
    
    f_user = await db.get(User, req.user_id)
    
    return FacultyProfileUpdateRequestResponse(
        id=req.id,
        user_id=req.user_id,
        full_name=f_user.full_name if f_user else "Faculty Member",
        status=req.status,
        faculty_id=req.faculty_id,
        employee_code=req.employee_code,
        official_email=req.official_email,
        official_phone=req.official_phone,
        gender=req.gender,
        date_of_birth=req.date_of_birth,
        blood_group=req.blood_group,
        nationality=req.nationality,
        requested_designation=req.designation,
        requested_department_name=req.department_name,
        comments=req.comments,
        processed_at=req.processed_at,
        processed_by=req.processed_by,
        processed_by_name=current_user.full_name,
        created_at=req.created_at
    )


@router.post("/profile/update-requests/{request_id}/reject", response_model=FacultyProfileUpdateRequestResponse)
async def reject_profile_update_request(
    request_id: str,
    payload: FacultyProfileUpdateRequestReview,
    current_user: User = Depends(role_required([UserRole.HOD])),
    db: AsyncSession = Depends(get_db_session)
) -> FacultyProfileUpdateRequestResponse:
    req = await db.get(FacultyProfileUpdateRequest, request_id)
    if not req or req.is_deleted or req.status != "PENDING":
        raise HTTPException(status_code=404, detail="Pending request not found.")
        
    req.status = "REJECTED"
    req.comments = payload.comments
    req.processed_at = datetime.now()
    req.processed_by = current_user.id
    
    db.add(req)
    
    audit = AuditLog(
        user_id=current_user.id,
        action="REJECT_PROFILE_UPDATE_REQUEST",
        entity="FacultyProfileUpdateRequest",
        entity_id=request_id,
        timestamp=datetime.now()
    )
    db.add(audit)
    
    await db.commit()
    await db.refresh(req)
    
    f_user = await db.get(User, req.user_id)
    
    return FacultyProfileUpdateRequestResponse(
        id=req.id,
        user_id=req.user_id,
        full_name=f_user.full_name if f_user else "Faculty Member",
        status=req.status,
        faculty_id=req.faculty_id,
        employee_code=req.employee_code,
        official_email=req.official_email,
        official_phone=req.official_phone,
        gender=req.gender,
        date_of_birth=req.date_of_birth,
        blood_group=req.blood_group,
        nationality=req.nationality,
        comments=req.comments,
        processed_at=req.processed_at,
        processed_by=req.processed_by,
        processed_by_name=current_user.full_name,
        created_at=req.created_at
    )


@router.post("/profile/update-requests/{request_id}/request-changes", response_model=FacultyProfileUpdateRequestResponse)
async def request_changes_profile_update_request(
    request_id: str,
    payload: FacultyProfileUpdateRequestReview,
    current_user: User = Depends(role_required([UserRole.HOD])),
    db: AsyncSession = Depends(get_db_session)
) -> FacultyProfileUpdateRequestResponse:
    req = await db.get(FacultyProfileUpdateRequest, request_id)
    if not req or req.is_deleted or req.status != "PENDING":
        raise HTTPException(status_code=404, detail="Pending request not found.")
        
    req.status = "CHANGES_REQUESTED"
    req.comments = payload.comments
    req.processed_at = datetime.now()
    req.processed_by = current_user.id
    
    db.add(req)
    
    audit = AuditLog(
        user_id=current_user.id,
        action="REQUEST_CHANGES_PROFILE_UPDATE_REQUEST",
        entity="FacultyProfileUpdateRequest",
        entity_id=request_id,
        timestamp=datetime.now()
    )
    db.add(audit)
    
    await db.commit()
    await db.refresh(req)
    
    f_user = await db.get(User, req.user_id)
    
    return FacultyProfileUpdateRequestResponse(
        id=req.id,
        user_id=req.user_id,
        full_name=f_user.full_name if f_user else "Faculty Member",
        status=req.status,
        faculty_id=req.faculty_id,
        employee_code=req.employee_code,
        official_email=req.official_email,
        official_phone=req.official_phone,
        gender=req.gender,
        date_of_birth=req.date_of_birth,
        blood_group=req.blood_group,
        nationality=req.nationality,
        comments=req.comments,
        processed_at=req.processed_at,
        processed_by=req.processed_by,
        processed_by_name=current_user.full_name,
        created_at=req.created_at
    )

@router.get("/profile/activity-summary", response_model=FacultyActivitySummaryResponse)
@router.get("/profile/activity-summary/{user_id}", response_model=FacultyActivitySummaryResponse)
async def get_activity_summary(
    user_id: str | None = None,
    current_user: User = Depends(role_required([UserRole.FACULTY, UserRole.HOD, UserRole.PRINCIPAL, UserRole.ADMIN, UserRole.SUPER_ADMIN])),
    db: AsyncSession = Depends(get_db_session)
) -> FacultyActivitySummaryResponse:
    # Default to current user if not specified
    target_user_id = user_id if user_id else current_user.id
    
    # 1. Classes Conducted (From local json)
    from app.api.v1.endpoints.teaching_logs import load_db as load_teaching_db
    classes_conducted = 0
    try:
        teaching_db = load_teaching_db()
        diaries = [d for d in teaching_db.get("class_diaries", {}).values() if d.get("faculty_id") == target_user_id and d.get("status") == "Submitted"]
        classes_conducted = len(diaries)
    except Exception:
        pass

    # 2. Attendance Marked (Total rows in sections handled by this faculty)
    sections_q = await db.execute(select(Timetable.section_id).where(Timetable.faculty_id == target_user_id, Timetable.is_deleted.is_(False)))
    section_ids = [row[0] for row in sections_q.all() if row[0]]
    attendance_marked = 0
    if section_ids:
        att_q = await db.execute(
            select(func.count(Attendance.id)).where(
                Attendance.section_id.in_(section_ids),
                Attendance.is_deleted.is_(False)
            )
        )
        attendance_marked = att_q.scalar_one_or_none() or 0

    # 3. Study Materials Uploaded
    sm_q = await db.execute(select(func.count(StudyMaterial.id)).where(StudyMaterial.faculty_id == target_user_id, StudyMaterial.is_deleted.is_(False)))
    study_materials_uploaded = sm_q.scalar_one_or_none() or 0

    # 4. Assignments Created
    ass_q = await db.execute(select(func.count(Assignment.id)).where(Assignment.faculty_id == target_user_id, Assignment.is_deleted.is_(False)))
    assignments_created = ass_q.scalar_one_or_none() or 0

    # 5. Leave Requests Submitted
    leaves_q = await db.execute(select(func.count(LeaveRequest.id)).where(LeaveRequest.user_id == target_user_id, LeaveRequest.is_deleted.is_(False)))
    leave_requests_submitted = leaves_q.scalar_one_or_none() or 0

    return FacultyActivitySummaryResponse(
        classes_conducted=classes_conducted,
        attendance_marked=attendance_marked,
        study_materials_uploaded=study_materials_uploaded,
        assignments_created=assignments_created,
        leave_requests_submitted=leave_requests_submitted
    )

@router.get("/profile/{user_id}", response_model=FacultyProfileResponse)
async def get_faculty_profile_by_id(
    user_id: str,
    current_user: User = Depends(role_required([UserRole.FACULTY, UserRole.HOD, UserRole.PRINCIPAL, UserRole.ADMIN, UserRole.SUPER_ADMIN])),
    db: AsyncSession = Depends(get_db_session)
) -> FacultyProfileResponse:
    # Resolve target user
    user = await db.get(User, user_id)
    if not user:
        raise HTTPException(status_code=404, detail="User not found")
        
    profile = await get_or_create_faculty_profile(user.id, db)
    return await build_profile_response(user, profile, db)

@router.put("/profile/{user_id}", response_model=FacultyProfileResponse)
async def admin_update_faculty_profile(
    user_id: str,
    payload: FacultyProfileAdminUpdateRequest,
    current_user: User = Depends(role_required([UserRole.ADMIN, UserRole.SUPER_ADMIN])),
    db: AsyncSession = Depends(get_db_session)
) -> FacultyProfileResponse:
    user = await db.get(User, user_id)
    if not user:
        raise HTTPException(status_code=404, detail="User not found")
        
    profile = await get_or_create_faculty_profile(user.id, db)
    
    # Update official info
    profile.designation = payload.designation
    profile.specialization = payload.specialization
    if payload.community is not None: profile.community = payload.community
    if payload.employee_code is not None: profile.employee_code = payload.employee_code
    if payload.faculty_type is not None: profile.faculty_type = payload.faculty_type
    if payload.employment_category is not None: profile.employment_category = payload.employment_category
    if payload.date_of_joining is not None: profile.date_of_joining = payload.date_of_joining
    if payload.employment_status is not None: profile.employment_status = payload.employment_status
    if payload.reporting_hod_id is not None: profile.reporting_hod_id = payload.reporting_hod_id
    if payload.reporting_principal_id is not None: profile.reporting_principal_id = payload.reporting_principal_id
    if payload.confirmation_date is not None: profile.confirmation_date = payload.confirmation_date
    
    if payload.academic_responsibilities is not None:
        profile.academic_responsibilities = payload.academic_responsibilities
    if payload.promotion_history is not None:
        profile.promotion_history = payload.promotion_history
    if payload.increment_history is not None:
        profile.increment_history = payload.increment_history
    if payload.documents_repository is not None:
        profile.documents_repository = payload.documents_repository
        
    if payload.department_id is not None:
        user.department_id = payload.department_id
        
    await db.commit()
    await db.refresh(profile)
    await db.refresh(user)
    return await build_profile_response(user, profile, db)

@router.get("/hierarchy", response_model=dict)
async def get_hierarchy_contacts(
    current_user: User = Depends(role_required([UserRole.FACULTY, UserRole.HOD, UserRole.ADMIN, UserRole.SUPER_ADMIN, UserRole.PRINCIPAL])),
    db: AsyncSession = Depends(get_db_session)
):
    hods_q = await db.execute(select(User).where(User.role == UserRole.HOD, User.is_deleted.is_(False)))
    hods = [{"id": u.id, "full_name": u.full_name} for u in hods_q.scalars().all()]
    
    principals_q = await db.execute(select(User).where(User.role == UserRole.PRINCIPAL, User.is_deleted.is_(False)))
    principals = [{"id": u.id, "full_name": u.full_name} for u in principals_q.scalars().all()]
    
    return {"hods": hods, "principals": principals}

@router.post("/documents/upload")
async def upload_document(
    file: UploadFile = File(...),
    current_user: User = Depends(role_required([UserRole.FACULTY, UserRole.HOD, UserRole.ADMIN, UserRole.SUPER_ADMIN, UserRole.PRINCIPAL])),
    db: AsyncSession = Depends(get_db_session)
):
    static_dir = os.path.join(os.path.dirname(os.path.dirname(os.path.dirname(os.path.dirname(__file__)))), "static")
    uploads_dir = os.path.join(static_dir, "uploads")
    os.makedirs(uploads_dir, exist_ok=True)
    
    filename = file.filename or "file"
    safe_filename = f"doc_{uuid.uuid4().hex}_{filename.replace(' ', '_')}"
    file_path = os.path.join(uploads_dir, safe_filename)
    
    with open(file_path, "wb") as buffer:
        shutil.copyfileobj(file.file, buffer)
        
    return {"file_url": f"/mock-uploads/{safe_filename}", "filename": file.filename or "file"}

@router.get("/research/list", response_model=list[ResearchResponse])
@router.get("/research/list/{user_id}", response_model=list[ResearchResponse])
async def list_faculty_research(
    user_id: str | None = None,
    current_user: User = Depends(role_required([UserRole.FACULTY, UserRole.HOD, UserRole.PRINCIPAL, UserRole.ADMIN, UserRole.SUPER_ADMIN])),
    db: AsyncSession = Depends(get_db_session)
) -> list[ResearchResponse]:
    target_user_id = user_id if user_id else current_user.id
    q = await db.execute(select(FacultyResearch).where(FacultyResearch.faculty_id == target_user_id, FacultyResearch.is_deleted.is_(False)))
    records = q.scalars().all()
    results = []
    for r in records:
        grant_amt = r.grant_amount
        results.append(
            ResearchResponse(
                id=r.id,
                title=r.title,
                publication=r.publication,
                grant_amount=float(grant_amt) if grant_amt is not None else None,  # type: ignore
                publisher=r.publisher,
                publication_date=r.publication_date,
                isbn_issn=r.isbn_issn,
                research_type=r.research_type,
                proof_file_url=r.proof_file_url,
                status=r.status,
                comments=r.comments
            )
        )
    return results

@router.put("/research/{research_id}", response_model=ResearchResponse)
async def update_faculty_research(
    research_id: str,
    payload: ResearchEntryRequest,
    current_user: User = Depends(role_required([UserRole.FACULTY, UserRole.HOD])),
    db: AsyncSession = Depends(get_db_session)
) -> ResearchResponse:
    r = await db.get(FacultyResearch, research_id)
    if not r or r.is_deleted or r.faculty_id != current_user.id:
        raise HTTPException(status_code=404, detail="Research record not found")
        
    r.title = payload.title
    r.publication = payload.publication
    r.grant_amount = payload.grant_amount
    r.publisher = payload.publisher
    r.publication_date = payload.publication_date
    r.isbn_issn = payload.isbn_issn
    r.research_type = payload.research_type
    
    await db.commit()
    await db.refresh(r)
    grant_amt = r.grant_amount
    return ResearchResponse(
        id=r.id,
        title=r.title,
        publication=r.publication,
        grant_amount=float(grant_amt) if grant_amt is not None else None,  # type: ignore
        publisher=r.publisher,
        publication_date=r.publication_date,
        isbn_issn=r.isbn_issn,
        research_type=r.research_type,
        proof_file_url=r.proof_file_url,
        status=r.status,
        comments=r.comments
    )

@router.delete("/research/{research_id}")
async def delete_faculty_research(
    research_id: str,
    current_user: User = Depends(role_required([UserRole.FACULTY, UserRole.HOD])),
    db: AsyncSession = Depends(get_db_session)
):
    r = await db.get(FacultyResearch, research_id)
    if not r or r.is_deleted or r.faculty_id != current_user.id:
        raise HTTPException(status_code=404, detail="Research record not found")
        
    r.is_deleted = True
    await db.commit()
    return {"detail": "Research record deleted successfully"}


@router.get("/directory", response_model=list[dict])
async def get_faculty_directory(
    current_user: User = Depends(role_required([UserRole.FACULTY, UserRole.HOD, UserRole.PRINCIPAL, UserRole.ADMIN, UserRole.SUPER_ADMIN])),
    db: AsyncSession = Depends(get_db_session)
):
    q = await db.execute(
        select(User)
        .where(
            User.role.in_([UserRole.FACULTY, UserRole.HOD]),
            User.is_deleted.is_(False)
        )
    )
    users = q.scalars().all()
    
    directory = []
    for u in users:
        profile_q = await db.execute(select(FacultyProfile).where(FacultyProfile.user_id == u.id))
        p = profile_q.scalar_one_or_none()
        
        dept_name = None
        if u.department_id:
            dept_q = await db.execute(select(Department).where(Department.id == u.department_id))
            dept = dept_q.scalar_one_or_none()
            dept_name = dept.name if dept else None
            
        directory.append({
            "user_id": u.id,
            "full_name": u.full_name,
            "email": u.email,
            "phone": u.phone,
            "designation": p.designation if p else "Assistant Professor",
            "department_name": dept_name,
            "profile_photo_url": p.profile_photo_url if p else None,
            "specialization": p.specialization if p else None
        })
    return directory


# --- HOD REPORTS & ANALYTICS ---

@router.get("/hod/reports/department")
async def get_hod_department_report(
    department_id: str | None = None,
    current_user: User = Depends(role_required([UserRole.HOD, UserRole.PRINCIPAL])),
    db: AsyncSession = Depends(get_db_session)
) -> dict:
    """Department-wide aggregate report using bulk queries (no N+1)."""
    from app.db.models.leave import LeaveRequest
    from app.db.models.academic import Department
    
    dept_id = department_id if current_user.role == UserRole.PRINCIPAL else current_user.department_id
    active_faculty = await get_active_faculty_ids(db, dept_id)
    fac_ids = list(active_faculty)
    fac_count = len(fac_ids)

    # Workload
    wl_rows = (await db.execute(
        select(FacultyWorkload.faculty_id, FacultyWorkload.teaching_hours, FacultyWorkload.semester)
        .where(FacultyWorkload.faculty_id.in_(fac_ids), FacultyWorkload.is_deleted.is_(False))
    )).all()
    # Deduplicate in-memory by (faculty_id, semester)
    wl_by_fac = {}
    seen_wl = set()
    for fid, hours, sem in wl_rows:
        wl_key = (fid, sem)
        if wl_key not in seen_wl:
            seen_wl.add(wl_key)
            wl_by_fac[fid] = wl_by_fac.get(fid, 0) + hours
            
    total_hours = sum(wl_by_fac.values())
    wl_count = len(wl_by_fac)
    avg_hours = round(total_hours / wl_count, 1) if wl_count else 0

    # Absences (only from active department faculty)
    total_absences = (await db.execute(
        select(func.count(FacultyAbsence.id)).where(
            FacultyAbsence.is_deleted.is_(False),
            FacultyAbsence.faculty_id.in_(fac_ids)
        )
    )).scalar_one()

    # Substitutions (only from active department absences)
    completed_substitutions = (await db.execute(
        select(func.count(SubstitutionAllocation.id)).where(
            SubstitutionAllocation.status == SubstitutionStatus.COMPLETED,
            SubstitutionAllocation.is_deleted.is_(False)
        ).join(FacultyAbsence, SubstitutionAllocation.absence_id == FacultyAbsence.id)
         .where(FacultyAbsence.faculty_id.in_(fac_ids))
    )).scalar_one()

    # Research (only approved from active department faculty, deduplicated by faculty_id, title)
    research_rows_all = (await db.execute(
        select(FacultyResearch.faculty_id, FacultyResearch.title, FacultyResearch.status)
        .where(FacultyResearch.faculty_id.in_(fac_ids), FacultyResearch.is_deleted.is_(False))
    )).all()
    
    seen_res = set()
    research_by_fac = {}
    total_research = 0
    pending_research = 0
    publications_submitted = 0
    publications_published = 0
    for fid, title, status in research_rows_all:
        r_key = (fid, title)
        if r_key not in seen_res:
            seen_res.add(r_key)
            publications_submitted += 1
            if status == "APPROVED":
                research_by_fac[fid] = research_by_fac.get(fid, 0) + 1
                publications_published += 1
            elif status == "PENDING":
                pending_research += 1

    # Study materials (deduplicated by title, section_id, faculty_id, type)
    mat_rows_all = (await db.execute(
        select(StudyMaterial.faculty_id, StudyMaterial.title, StudyMaterial.section_id, StudyMaterial.type, StudyMaterial.is_verified)
        .where(StudyMaterial.faculty_id.in_(fac_ids), StudyMaterial.is_deleted.is_(False))
    )).all()
    
    seen_mat = set()
    mat_by_fac = {}
    total_materials = 0
    verified_materials = 0
    for fid, title, sect_id, m_type, is_verified in mat_rows_all:
        m_key = (title, sect_id, fid, m_type)
        if m_key not in seen_mat:
            seen_mat.add(m_key)
            mat_by_fac[fid] = mat_by_fac.get(fid, 0) + 1
            total_materials += 1
            if is_verified:
                verified_materials += 1

    # Substitutions handled per faculty (substitute must be active)
    sub_rows = (await db.execute(
        select(SubstitutionAllocation.substitute_faculty_id, func.count(SubstitutionAllocation.id).label("cnt"))
        .where(
            SubstitutionAllocation.substitute_faculty_id.in_(fac_ids),
            SubstitutionAllocation.status == SubstitutionStatus.COMPLETED,
            SubstitutionAllocation.is_deleted.is_(False)
        )
        .group_by(SubstitutionAllocation.substitute_faculty_id)
    )).all()
    sub_by_fac = {r[0]: r[1] for r in sub_rows}

    # Faculty on leave today
    today_date = date.today()
    on_leave_res = await db.execute(
        select(func.count(func.distinct(LeaveRequest.user_id)))
        .where(
            LeaveRequest.status == "APPROVED",
            LeaveRequest.from_date <= today_date,
            LeaveRequest.to_date >= today_date,
            LeaveRequest.is_deleted.is_(False),
            LeaveRequest.user_id.in_(fac_ids)
        )
    )
    faculty_on_leave = on_leave_res.scalar_one()

    # Query active faculty users, profile details, and department
    q_fac = select(User, FacultyProfile, Department).outerjoin(
        FacultyProfile, User.id == FacultyProfile.user_id
    ).outerjoin(
        Department, User.department_id == Department.id
    ).where(
        User.role.in_([UserRole.FACULTY, UserRole.HOD]),
        User.is_active.is_(True),
        User.is_deleted.is_(False)
    )
    if dept_id:
        q_fac = q_fac.where(User.department_id == dept_id)
        
    fac_res = await db.execute(q_fac)
    fac_records = fac_res.all()

    faculty_breakdown = []
    for user, prof, dept in fac_records:
        fid = user.id
        faculty_breakdown.append({
            "faculty_id": fid,
            "faculty_name": user.full_name,
            "email": user.email,
            "employee_code": prof.employee_code if (prof and prof.employee_code) else fid,
            "designation": prof.designation if prof else "Faculty Member",
            "department_name": dept.name if dept else "Department of Law",
            "qualification": prof.educational_qualifications if prof else None,
            "employment_status": prof.employment_status if prof else "Active",
            "workload_hours": wl_by_fac.get(fid, 0),
            "verified_research": research_by_fac.get(fid, 0),
            "materials_submitted": mat_by_fac.get(fid, 0),
            "substitutions_handled": sub_by_fac.get(fid, 0),
        })

    return {
        "summary": {
            "total_faculty": fac_count,
            "active_faculty": fac_count,  # active and not deleted
            "faculty_on_leave": faculty_on_leave,
            "publications_submitted": publications_submitted,
            "publications_published": publications_published,
            "materials_approved": verified_materials,
            "avg_workload_hours": avg_hours,
            "total_absences": total_absences,
            "completed_substitutions": completed_substitutions,
            "total_verified_research": total_research,
            "pending_research_proofs": pending_research,
            "total_materials": total_materials,
            "verified_materials": verified_materials,
        },
        "faculty_breakdown": faculty_breakdown
    }


@router.get("/hod/reports/faculty/{faculty_id}")
async def get_hod_faculty_report(
    faculty_id: str,
    department_id: str | None = None,
    current_user: User = Depends(role_required([UserRole.HOD, UserRole.PRINCIPAL])),
    db: AsyncSession = Depends(get_db_session)
) -> dict:
    """Detailed performance report for a specific faculty member."""
    from app.db.models.leave import LeaveRequest
    
    fac_q = select(User).where(
        User.id == faculty_id,
        User.is_active.is_(True),
        User.is_deleted.is_(False)
    )
    if current_user.role == UserRole.HOD:
        fac_q = fac_q.where(User.department_id == current_user.department_id)
    fac = (await db.execute(fac_q)).scalar_one_or_none()
    if not fac:
        raise HTTPException(status_code=404, detail="Faculty member not found or is inactive.")

    # Profile
    prof_q = select(FacultyProfile).where(FacultyProfile.user_id == faculty_id, FacultyProfile.is_deleted.is_(False))
    prof = (await db.execute(prof_q)).scalar_one_or_none()

    # Workload (deduplicated by semester)
    wl_q = select(FacultyWorkload).where(FacultyWorkload.faculty_id == faculty_id, FacultyWorkload.is_deleted.is_(False))
    wl_items = (await db.execute(wl_q)).scalars().all()
    seen_wl = set()
    unique_wl_items = []
    for w in wl_items:
        w_key = (w.faculty_id, w.semester)
        if w_key not in seen_wl:
            seen_wl.add(w_key)
            unique_wl_items.append(w)
            
    teaching_hours = sum(w.teaching_hours for w in unique_wl_items)
    semester = unique_wl_items[0].semester if unique_wl_items else 0

    # Timetable / Classes
    tt_q = select(Timetable).where(Timetable.faculty_id == faculty_id, Timetable.is_deleted.is_(False))
    tt_items = (await db.execute(tt_q)).scalars().all()
    subject_list = []
    for tt in tt_items:
        c_q = select(Course).where(Course.id == tt.subject_id, Course.is_deleted.is_(False))
        c = (await db.execute(c_q)).scalar_one_or_none()
        if c and c.name not in subject_list:
            subject_list.append(c.name)

    # Research Activities (deduplicated by title)
    research_q = select(FacultyResearch).where(FacultyResearch.faculty_id == faculty_id, FacultyResearch.is_deleted.is_(False))
    research_items_all = (await db.execute(research_q)).scalars().all()
    seen_res = set()
    research_items = []
    for r in research_items_all:
        if r.title not in seen_res:
            seen_res.add(r.title)
            research_items.append(r)

    # Publication plans
    pub_plan_q = select(PublicationPlan).where(PublicationPlan.faculty_id == faculty_id, PublicationPlan.is_deleted.is_(False))
    pub_plans = (await db.execute(pub_plan_q)).scalars().all()
    
    # Research Compliance
    comp_q = select(ResearchCompliance).where(ResearchCompliance.faculty_id == faculty_id, ResearchCompliance.is_deleted.is_(False))
    compliances = (await db.execute(comp_q)).scalars().all()

    # Materials / Submission Status (deduplicated by title, section_id, type)
    mat_q = select(StudyMaterial).where(StudyMaterial.faculty_id == faculty_id, StudyMaterial.is_deleted.is_(False))
    mat_items_all = (await db.execute(mat_q)).scalars().all()
    seen_mat = set()
    mat_items = []
    for m in mat_items_all:
        m_key = (m.title, m.section_id, m.type)
        if m_key not in seen_mat:
            seen_mat.add(m_key)
            mat_items.append(m)

    # Detailed Materials List
    mat_detail_q = select(StudyMaterial, Section, Course).outerjoin(
        Section, StudyMaterial.section_id == Section.id
    ).outerjoin(
        Course, Section.course_id == Course.id
    ).where(
        StudyMaterial.faculty_id == faculty_id,
        StudyMaterial.is_deleted.is_(False)
    )
    mat_detail_res = await db.execute(mat_detail_q)
    mat_list = []
    for mat, sec, crs in mat_detail_res.all():
        mat_list.append({
            "id": mat.id,
            "title": mat.title,
            "type": mat.type,
            "file_url": mat.file_url,
            "is_verified": mat.is_verified,
            "status": mat.status if hasattr(mat, "status") else ("APPROVED" if mat.is_verified else "PENDING"),
            "section_name": sec.section_name if sec else "A",
            "course_name": crs.name if crs else "Law Subject",
            "comments": mat.comments if hasattr(mat, "comments") else None
        })

    # Leaves (deduplicated by from_date, to_date)
    leave_q = select(LeaveRequest).where(LeaveRequest.user_id == faculty_id, LeaveRequest.is_deleted.is_(False))
    leave_items_all = (await db.execute(leave_q)).scalars().all()
    seen_leaves = set()
    leave_items = []
    for l in leave_items_all:
        l_key = (l.from_date, l.to_date)
        if l_key not in seen_leaves:
            seen_leaves.add(l_key)
            leave_items.append(l)

    # Calculate Attendance Summary based on leaves
    leave_days = 0
    for l in leave_items:
        if l.status == "APPROVED":
            delta = (l.to_date - l.from_date).days + 1
            leave_days += delta
    working_days = 180
    present_days = max(0, working_days - leave_days)
    attendance_percentage = round((present_days / working_days) * 100, 1) if working_days > 0 else 100.0

    # Substitution Records
    # 1. As Substitute
    sub_as_sub_q = select(
        SubstitutionAllocation, FacultyAbsence, User, Course, Section
    ).join(
        FacultyAbsence, SubstitutionAllocation.absence_id == FacultyAbsence.id
    ).join(
        User, FacultyAbsence.faculty_id == User.id
    ).outerjoin(
        Timetable, SubstitutionAllocation.timetable_id == Timetable.id
    ).outerjoin(
        Course, Timetable.subject_id == Course.id
    ).outerjoin(
        Section, Timetable.section_id == Section.id
    ).where(
        SubstitutionAllocation.substitute_faculty_id == faculty_id,
        SubstitutionAllocation.is_deleted.is_(False)
    )
    sub_as_sub_res = await db.execute(sub_as_sub_q)
    as_substitute_list = []
    for alloc, absence, absent_user, course, section in sub_as_sub_res.all():
        as_substitute_list.append({
            "id": alloc.id,
            "date": alloc.date.isoformat(),
            "status": alloc.status.value if hasattr(alloc.status, "value") else str(alloc.status),
            "absent_faculty_name": absent_user.full_name,
            "subject": course.name if course else "Law Class",
            "section": section.section_name if section else "A",
            "remarks": alloc.remarks
        })

    # 2. As Absent Faculty (Substituted by someone else)
    sub_as_abs_q = select(
        SubstitutionAllocation, User, Course, Section
    ).join(
        FacultyAbsence, SubstitutionAllocation.absence_id == FacultyAbsence.id
    ).outerjoin(
        User, SubstitutionAllocation.substitute_faculty_id == User.id
    ).outerjoin(
        Timetable, SubstitutionAllocation.timetable_id == Timetable.id
    ).outerjoin(
        Course, Timetable.subject_id == Course.id
    ).outerjoin(
        Section, Timetable.section_id == Section.id
    ).where(
        FacultyAbsence.faculty_id == faculty_id,
        SubstitutionAllocation.is_deleted.is_(False)
    )
    sub_as_abs_res = await db.execute(sub_as_abs_q)
    as_absent_list = []
    for alloc, sub_user, course, section in sub_as_abs_res.all():
        as_absent_list.append({
            "id": alloc.id,
            "date": alloc.date.isoformat(),
            "status": alloc.status.value if hasattr(alloc.status, "value") else str(alloc.status),
            "substitute_faculty_name": sub_user.full_name if sub_user else "Not Assigned",
            "subject": course.name if course else "Law Class",
            "section": section.section_name if section else "A",
            "remarks": alloc.remarks
        })

    # Parse Certifications, Workshops, Awards
    certifications = []
    workshops = []
    awards = []
    if prof and prof.certifications_achievements:
        ca = prof.certifications_achievements
        if isinstance(ca, dict):
            certifications = ca.get("certifications", [])
            workshops = ca.get("workshops", [])
            awards = ca.get("awards", [])

    return {
        "faculty": {
            "id": fac.id,
            "name": fac.full_name,
            "email": fac.email,
            "phone": fac.phone,
            "designation": prof.designation if prof else "Faculty Member",
            "specialization": prof.specialization if prof else None,
            "qualification": prof.educational_qualifications if prof else None,
            "employee_code": prof.employee_code if (prof and prof.employee_code) else fac.id,
            "employment_status": prof.employment_status if prof else "Active",
            "date_of_joining": prof.date_of_joining.isoformat() if (prof and prof.date_of_joining) else None,
            "department_name": fac.department.name if fac.department else "Department of Law",
        },
        "workload": {
            "teaching_hours": teaching_hours,
            "semester": semester,
            "subjects_handled": subject_list,
            "total_timetable_slots": len(tt_items),
        },
        "attendance": {
            "working_days": working_days,
            "present_days": present_days,
            "leave_days": leave_days,
            "attendance_percentage": attendance_percentage,
        },
        "research": {
            "total_entries": len(research_items),
            "approved": sum(1 for r in research_items if r.status == "APPROVED"),
            "pending": sum(1 for r in research_items if r.status == "PENDING"),
            "rejected": sum(1 for r in research_items if r.status == "REJECTED"),
            "entries": [
                {
                    "id": r.id,
                    "title": r.title,
                    "publication": r.publication,
                    "status": r.status,
                    "grant_amount": float(r.grant_amount) if r.grant_amount else None
                }
                for r in research_items
            ],
            "plans": [
                {
                    "id": p.id,
                    "title": p.title,
                    "journal_conference": p.journal_conference,
                    "target_date": p.target_date.isoformat(),
                    "status": p.status
                }
                for p in pub_plans
            ],
            "compliance": [
                {
                    "id": c.id,
                    "requirement_name": c.requirement_name,
                    "deadline": c.deadline.isoformat(),
                    "status": c.status,
                    "submitted_at": c.submitted_at.isoformat() if c.submitted_at else None
                }
                for c in compliances
            ]
        },
        "materials": {
            "total": len(mat_items),
            "verified": sum(1 for m in mat_items if m.is_verified),
            "pending": sum(1 for m in mat_items if not m.is_verified),
            "entries": mat_list
        },
        "substitutions": {
            "total_handled": len(as_substitute_list),
            "completed": sum(1 for s in as_substitute_list if s["status"] == "COMPLETED"),
            "pending": sum(1 for s in as_substitute_list if s["status"] in ["PENDING", "ALLOCATED"]),
            "as_substitute": as_substitute_list,
            "as_absent": as_absent_list
        },
        "leaves": {
            "total_applied": len(leave_items),
            "approved": sum(1 for l in leave_items if l.status == "APPROVED"),
            "pending": sum(1 for l in leave_items if l.status == "PENDING"),
            "rejected": sum(1 for l in leave_items if l.status == "REJECTED"),
            "entries": [
                {
                    "id": l.id,
                    "type": l.type,
                    "from_date": l.from_date.isoformat(),
                    "to_date": l.to_date.isoformat(),
                    "reason": l.reason,
                    "status": l.status
                }
                for l in leave_items
            ]
        },
        "certifications": certifications,
        "workshops": workshops,
        "awards": awards,
    }


@router.get("/hod/reports/students")
async def get_hod_student_report(
    department_id: str | None = None,
    current_user: User = Depends(role_required([UserRole.HOD, UserRole.PRINCIPAL])),
    db: AsyncSession = Depends(get_db_session)
) -> dict:
    """Department student aggregate report: attendance, leaves, grievances, marks, arrears, certifications, internships, sports."""
    from app.db.models.student import Student
    from app.db.models.attendance import Attendance
    from app.db.models.leave import LeaveRequest
    from app.db.models.marks import Mark, MarkExamType
    from app.db.models.academic import Course, Section, Department

    dept_id = department_id if current_user.role == UserRole.PRINCIPAL else current_user.department_id
    active_students_ids = await get_active_student_ids(db, dept_id)

    # Student user counts
    std_q = select(User).where(User.id.in_(active_students_ids)).order_by(User.full_name)
    students = (await db.execute(std_q)).scalars().all()
    total_students = len(students)

    if total_students == 0:
        return {
            "summary": {
                "total_students": 0,
                "total_attendance_records": 0,
                "overall_attendance_pct": 0,
                "total_leaves_applied": 0,
                "total_leaves_approved": 0,
                "average_cgpa": 0.0,
                "total_arrears": 0,
                "total_internships": 0,
                "total_certifications": 0,
                "total_sports": 0,
            },
            "student_rows": []
        }

    # Fetch student profiles (roll no, semester, batch year, cgpa, json columns)
    student_profile_q = select(Student).where(
        Student.user_id.in_(active_students_ids),
        Student.is_deleted.is_(False)
    )
    student_profiles = (await db.execute(student_profile_q)).scalars().all()
    student_by_user_id = {sp.user_id: sp for sp in student_profiles}
    student_ids = [sp.id for sp in student_profiles]

    # Fetch departments
    dept_q = select(Department).where(Department.is_deleted.is_(False))
    depts = (await db.execute(dept_q)).scalars().all()
    dept_by_id = {d.id: d for d in depts}

    # Fetch attendance
    att_records = []
    if student_ids:
        att_q = select(Attendance).where(
            Attendance.is_deleted.is_(False),
            Attendance.student_id.in_(student_ids)
        )
        att_records_all = (await db.execute(att_q)).scalars().all()
        # Deduplicate
        seen_att = set()
        for a in att_records_all:
            a_key = (a.student_id, getattr(a, 'date', None) or a.id)
            if a_key not in seen_att:
                seen_att.add(a_key)
                att_records.append(a)

    total_att = len(att_records)
    
    # Helper to check status safely
    def is_present(status_val):
        val = getattr(status_val, 'value', str(status_val)).lower()
        return val == "present" or val == "od"

    def is_absent(status_val):
        val = getattr(status_val, 'value', str(status_val)).lower()
        return val == "absent"

    present_count = sum(1 for a in att_records if is_present(a.status))
    overall_attendance_pct = round((present_count / total_att * 100), 1) if total_att > 0 else 0

    # Fetch leaves
    all_leaves = []
    leave_q = select(LeaveRequest).where(
        LeaveRequest.is_deleted.is_(False),
        LeaveRequest.user_id.in_(active_students_ids)
    )
    all_leaves_all = (await db.execute(leave_q)).scalars().all()
    seen_leaves = set()
    for l in all_leaves_all:
        l_key = (l.user_id, l.from_date, l.to_date)
        if l_key not in seen_leaves:
            seen_leaves.add(l_key)
            all_leaves.append(l)

    # Fetch marks, sections, courses
    all_marks = []
    section_by_id = {}
    course_by_id = {}
    if student_ids:
        marks_q = select(Mark).where(
            Mark.is_deleted.is_(False),
            Mark.student_id.in_(student_ids)
        )
        all_marks = (await db.execute(marks_q)).scalars().all()
        
        sections_q = select(Section).where(Section.is_deleted.is_(False))
        secs = (await db.execute(sections_q)).scalars().all()
        section_by_id = {s.id: s for s in secs}
        
        courses_q = select(Course).where(Course.is_deleted.is_(False))
        cs = (await db.execute(courses_q)).scalars().all()
        course_by_id = {c.id: c for c in cs}

    # Per-student breakdown
    student_rows = []
    for s in students[:50]:  # cap at 50 for performance
        sp = student_by_user_id.get(s.id)
        if not sp:
            continue
            
        # Attendance
        s_att_records = [a for a in att_records if a.student_id == sp.id]
        s_present = sum(1 for a in s_att_records if is_present(a.status))
        s_total = len(s_att_records)
        s_pct = round((s_present / s_total * 100), 1) if s_total > 0 else 0

        # Leaves
        s_leaves = [l for l in all_leaves if l.user_id == s.id]

        # Department & Program
        dept = dept_by_id.get(s.department_id or sp.department_id)
        dept_name = dept.name if dept else "Department of Law"
        
        # Marks, CGPA & Arrears
        s_marks = [m for m in all_marks if m.student_id == sp.id]
        subjects = []
        arrear_subjects = []
        sem_marks = []
        
        for m in s_marks:
            sec = section_by_id.get(m.section_id)
            course = course_by_id.get(sec.course_id) if sec else None
            if course:
                marks_val = float(m.mark)
                max_val = float(m.max_mark)
                subjects.append({
                    "code": course.code,
                    "name": course.name,
                    "marks": marks_val
                })
                
                # Arrear check (ExamType or MarkExamType SEMESTER where mark < 50% max)
                exam_type_str = getattr(m.exam_type, 'value', str(m.exam_type)).upper()
                if exam_type_str == "SEMESTER":
                    sem_marks.append((marks_val / max_val) * 10.0)
                    if marks_val < max_val * 0.50:
                        arrear_subjects.append(course.name)

        # Dynamic CGPA calculation
        if sem_marks:
            cgpa = float(sum(sem_marks) / len(sem_marks))
        else:
            cgpa = float(sp.cgpa) if sp.cgpa else 0.0

        arrear_count = len(arrear_subjects)
        
        # Certifications
        certs = sp.certifications if sp.certifications else []
        
        # Internships
        internships = sp.internships if sp.internships else []
        internship = internships[0] if len(internships) > 0 else {
            "org": "No Internships Registered",
            "domain": "N/A",
            "status": "N/A",
            "details": "No internship details available.",
            "duration": "N/A"
        }
        
        # Sports
        sports = sp.sports_records if sp.sports_records else []

        student_rows.append({
            "student_id": s.id,
            "name": s.full_name,
            "email": s.email,
            "roll_no": sp.roll_no,
            "semester": sp.semester,
            "batch_year": sp.batch_year,
            "department": dept_name,
            "program": "BA LLB (Hons)" if "BA" in dept_name else "B.Com LLB (Hons)" if "Com" in dept_name else "LLB (Hons)",
            "cgpa": round(cgpa, 2),
            "arrear_count": arrear_count,
            "arrear_subjects": arrear_subjects,
            "subjects": subjects,
            "total_classes": s_total,
            "present": s_present,
            "absent": s_total - s_present,
            "attendance_pct": s_pct,
            "leaves_applied": len(s_leaves),
            "leaves_approved": sum(1 for l in s_leaves if getattr(l.status, 'value', str(l.status)).upper() == "APPROVED"),
            "certifications": certs,
            "internship": internship,
            "sports": sports,
            "academic_year": f"{sp.batch_year}-{sp.batch_year + 1}"
        })

    # Summary Metrics
    total_std = len(student_rows)
    if total_std > 0:
        avg_cgpa = round(sum(r["cgpa"] for r in student_rows) / total_std, 2)
        total_arrears = sum(1 for r in student_rows if r["arrear_count"] > 0)
        total_internships = sum(1 for r in student_rows if r["internship"]["status"] == "Completed")
        total_certs = sum(len(r["certifications"]) for r in student_rows)
        total_sports = sum(len(r["sports"]) for r in student_rows)
    else:
        avg_cgpa = 0.0
        total_arrears = 0
        total_internships = 0
        total_certs = 0
        total_sports = 0

    return {
        "summary": {
            "total_students": total_std,
            "total_attendance_records": total_att,
            "overall_attendance_pct": overall_attendance_pct,
            "total_leaves_applied": len(all_leaves),
            "total_leaves_approved": sum(1 for l in all_leaves if getattr(l.status, 'value', str(l.status)).upper() == "APPROVED"),
            "average_cgpa": avg_cgpa,
            "total_arrears": total_arrears,
            "total_internships": total_internships,
            "total_certifications": total_certs,
            "total_sports": total_sports,
        },
        "student_rows": student_rows
    }


@router.get("/hod/reports/export/department")
async def export_department_report_csv(
    department_id: str | None = None,
    current_user: User = Depends(role_required([UserRole.HOD, UserRole.PRINCIPAL])),
    db: AsyncSession = Depends(get_db_session)
):
    """Export department faculty breakdown as CSV."""
    dept_data = await get_hod_department_report(department_id=department_id, current_user=current_user, db=db)
    breakdown = dept_data["faculty_breakdown"]

    import csv, io
    output = io.StringIO()
    writer = csv.writer(output)
    writer.writerow([
        "Faculty ID", "Faculty Name", "Email",
        "Workload Hours/Week", "Verified Research",
        "Materials Submitted", "Substitutions Handled"
    ])
    for f in breakdown:
        writer.writerow([
            f["faculty_id"], f["faculty_name"], f["email"],
            f["workload_hours"], f["verified_research"],
            f["materials_submitted"], f["substitutions_handled"]
        ])

    return Response(
        content=output.getvalue(),
        media_type="text/csv",
        headers={"Content-Disposition": "attachment; filename=department_report.csv"}
    )


@router.get("/hod/reports/export/students")
async def export_students_report_csv(
    department_id: str | None = None,
    current_user: User = Depends(role_required([UserRole.HOD, UserRole.PRINCIPAL])),
    db: AsyncSession = Depends(get_db_session)
):
    """Export student attendance report as CSV."""
    std_data = await get_hod_student_report(department_id=department_id, current_user=current_user, db=db)
    rows = std_data["student_rows"]

    import csv, io
    output = io.StringIO()
    writer = csv.writer(output)
    writer.writerow([
        "Student ID", "Name", "Email", "Roll No", "Semester",
        "Attendance %", "Total Classes", "Present", "Absent",
        "Leaves Applied", "Leaves Approved"
    ])
    for r in rows:
        writer.writerow([
            r["student_id"], r["name"], r["email"], r["roll_no"], r["semester"],
            r["attendance_pct"], r["total_classes"], r["present"], r["absent"],
            r["leaves_applied"], r["leaves_approved"]
        ])

    return Response(
        content=output.getvalue(),
        media_type="text/csv",
        headers={"Content-Disposition": "attachment; filename=student_report.csv"}
    )

@router.get("/substitutions/sync")
async def get_substitutions() -> list:
    import json, os
    db_path = os.path.join(os.path.dirname(os.path.dirname(os.path.dirname(__file__))), "substitutions_db.json")
    if not os.path.exists(db_path):
        return []
    try:
        with open(db_path, "r") as f:
            return json.load(f)
    except Exception:
        return []

@router.post("/substitutions/sync")
async def sync_substitutions(
    payload: list,
    current_user: User = Depends(role_required([UserRole.HOD, UserRole.FACULTY])),
    db: AsyncSession = Depends(get_db_session)
) -> list:
    import json, os
    from app.db.models.communication import Notification
    
    db_path = os.path.join(os.path.dirname(os.path.dirname(os.path.dirname(__file__))), "substitutions_db.json")
    
    old_records = []
    if os.path.exists(db_path):
        try:
            with open(db_path, "r") as f:
                old_records = json.load(f)
        except Exception:
            pass
            
    with open(db_path, "w") as f:
        json.dump(payload, f, indent=2)
        
    old_map = {r["id"]: r for r in old_records}
    
    for record in payload:
        rid = record["id"]
        sub_id = record.get("substituteFacultyId")
        sub_name = record.get("substituteFacultyName")
        absent_name = record.get("absentFacultyName")
        subject = record.get("subject")
        section = record.get("section")
        date_val = record.get("date")
        period = record.get("periodLabel")
        status = record.get("status")
        
        notif_msg = (
            f'"New Substitute Class Assigned"\n'
            f'You have been assigned to handle {subject} for {section} on {date_val} '
            f'during {period} in place of {absent_name}.'
        )
        
        if rid not in old_map:
            if sub_id:
                # Check duplicate
                check_q = select(Notification).where(
                    Notification.user_id == sub_id,
                    Notification.type == "SUBSTITUTION",
                    Notification.message == notif_msg,
                    Notification.is_deleted.is_(False)
                )
                check_res = await db.execute(check_q)
                if not check_res.scalar_one_or_none():
                    notif = Notification(
                        user_id=sub_id,
                        type="SUBSTITUTION",
                        message=notif_msg,
                        is_read=False,
                        sent_via="In-App"
                    )
                    db.add(notif)
        else:
            old = old_map[rid]
            old_sub_id = old.get("substituteFacultyId")
            
            if old_sub_id != sub_id:
                if sub_id:
                    check_q = select(Notification).where(
                        Notification.user_id == sub_id,
                        Notification.type == "SUBSTITUTION",
                        Notification.message == notif_msg,
                        Notification.is_deleted.is_(False)
                    )
                    check_res = await db.execute(check_q)
                    if not check_res.scalar_one_or_none():
                        new_notif = Notification(
                            user_id=sub_id,
                            type="SUBSTITUTION",
                            message=notif_msg,
                            is_read=False,
                            sent_via="In-App"
                        )
                        db.add(new_notif)
                if old_sub_id:
                    cancel_msg = f'Cancelled: Substitution assignment to handle {subject} for {section} on {date_val} has been cancelled.'
                    notif = Notification(
                        user_id=old_sub_id,
                        type="SUBSTITUTION",
                        message=cancel_msg,
                        is_read=True,
                        sent_via="In-App"
                    )
                    db.add(notif)
            elif old.get("status") != status or old.get("remarks") != record.get("remarks"):
                if sub_id:
                    update_msg = f'Updated status to {status}: handle {subject} for {section} on {date_val} in place of {absent_name}.'
                    notif = Notification(
                        user_id=sub_id,
                        type="SUBSTITUTION",
                        message=update_msg,
                        is_read=False,
                        sent_via="In-App"
                    )
                    db.add(notif)

    new_ids = {r["id"] for r in payload}
    for oid, old in old_map.items():
        if oid not in new_ids:
            old_sub_id = old.get("substituteFacultyId")
            subject = old.get("subject")
            section = old.get("section")
            date_val = old.get("date")
            if old_sub_id:
                cancel_msg = f'Cancelled: Substitution assignment to handle {subject} for {section} on {date_val} has been cancelled.'
                notif = Notification(
                    user_id=old_sub_id,
                    type="SUBSTITUTION",
                    message=cancel_msg,
                    is_read=True,
                    sent_via="In-App"
                )
                db.add(notif)
                
    await db.commit()
    return payload


# --- HOD TIMETABLE MANAGEMENT ROUTERS ---

@router.get("/hod/timetable", response_model=list[HODTimetableItemResponse])
async def get_hod_timetable(
    dept_id: str | None = None,
    semester: int | None = None,
    section_name: str | None = None,
    current_user: User = Depends(role_required([UserRole.HOD, UserRole.PRINCIPAL])),
    db: AsyncSession = Depends(get_db_session)
) -> list[HODTimetableItemResponse]:
    from app.db.models.academic import Timetable, Section, Course, TimetableApproval
    
    q = (
        select(Timetable, Section, Course, User, TimetableApproval.status, TimetableApproval.comments)
        .join(Section, Timetable.section_id == Section.id)
        .join(Course, Timetable.subject_id == Course.id)
        .join(User, Timetable.faculty_id == User.id)
        .outerjoin(TimetableApproval, Timetable.id == TimetableApproval.timetable_id)
        .where(Timetable.is_deleted.is_(False))
    )
    
    active_dept_id = dept_id
    if current_user.role == UserRole.HOD:
        active_dept_id = current_user.department_id
    elif not active_dept_id:
        active_dept_id = current_user.department_id

    if active_dept_id:
        q = q.where(Course.dept_id == active_dept_id)
        
    if semester is not None:
        q = q.where(Course.semester == semester)
    else:
        # Get active semesters from academic_years
        from app.db.models.academic import AcademicYear
        ay_query = select(AcademicYear.current_semester).where(
            AcademicYear.is_active == True,
            AcademicYear.is_deleted == False
        )
        ay_res = await db.execute(ay_query)
        active_semesters = [r for r in ay_res.scalars().all() if r is not None]
        if active_semesters:
            q = q.where(Course.semester.in_(active_semesters))
        
    if section_name:
        q = q.where(Section.section_name == section_name)
        
    res = await db.execute(q)
    rows = res.all()
    
    resp = []
    seen = set()
    for t, sec, course, fac, app_status, app_comments in rows:
        if t.id in seen:
            continue
        seen.add(t.id)
        
        status_val = "DRAFT"
        if app_status:
            status_val = app_status.value if hasattr(app_status, "value") else str(app_status)
            
        resp.append(
            HODTimetableItemResponse(
                id=t.id,
                section_id=t.section_id,
                section_name=sec.section_name,
                subject_id=t.subject_id,
                subject_name=course.name,
                subject_code=course.code,
                faculty_id=t.faculty_id,
                faculty_name=fac.full_name,
                room=t.room,
                weekday=t.weekday.value if hasattr(t.weekday, "value") else str(t.weekday),
                start_time=t.start_time.strftime("%H:%M") if hasattr(t.start_time, "strftime") else str(t.start_time),
                end_time=t.end_time.strftime("%H:%M") if hasattr(t.end_time, "strftime") else str(t.end_time),
                status=status_val,
                comments=app_comments,
                semester=course.semester
            )
        )
    return resp


async def validate_timetable_slot(
    db: AsyncSession,
    section_id: str,
    subject_id: str,
    faculty_id: str,
    room: str,
    weekday_enum,
    s_time,
    e_time,
    exclude_id: str | None = None
):
    from app.db.models.user import User, UserRole
    from app.db.models.academic import Timetable
    
    # 1. Verify active faculty
    fac_q = await db.execute(
        select(User).where(
            User.id == faculty_id,
            User.is_active.is_(True),
            User.is_deleted.is_(False),
            User.role.in_([UserRole.FACULTY, UserRole.HOD])
        )
    )
    faculty = fac_q.scalar_one_or_none()
    if not faculty:
        raise HTTPException(
            status_code=400,
            detail="Selected faculty is inactive or does not exist."
        )

    # Ensure faculty belongs to the subject's department
    from app.db.models.academic import Course
    course_q = await db.execute(select(Course).where(Course.id == subject_id))
    course = course_q.scalar_one_or_none()
    if course and faculty.department_id != course.dept_id:
        raise HTTPException(
            status_code=400,
            detail=f"Faculty member {faculty.full_name} does not belong to the selected department/program."
        )


    # Overlap conditions (weekday, not deleted, and overlapping time range)
    overlap_cond = and_(
        Timetable.weekday == weekday_enum,
        Timetable.is_deleted.is_(False),
        Timetable.start_time < e_time,
        Timetable.end_time > s_time
    )
    if exclude_id:
        overlap_cond = and_(overlap_cond, Timetable.id != exclude_id)

    # 2. Prevent faculty double booking
    fac_conflict_q = await db.execute(
        select(Timetable).where(Timetable.faculty_id == faculty_id, overlap_cond)
    )
    if fac_conflict_q.scalars().first():
        raise HTTPException(
            status_code=400,
            detail=f"Faculty member {faculty.full_name} is already assigned to another class during this period."
        )

    # 3. Prevent classroom conflicts
    room_conflict_q = await db.execute(
        select(Timetable).where(Timetable.room == room, overlap_cond)
    )
    if room_conflict_q.scalars().first():
        raise HTTPException(
            status_code=400,
            detail=f"Classroom {room} is already booked for another class during this period."
        )

    # 4. Prevent overlapping classes for the same section
    sec_conflict_q = await db.execute(
        select(Timetable).where(Timetable.section_id == section_id, overlap_cond)
    )
    if sec_conflict_q.scalars().first():
        raise HTTPException(
            status_code=400,
            detail="This section already has a class scheduled during this period."
        )

    # 5. Prevent duplicate timetable entries
    dup_cond = and_(
        Timetable.section_id == section_id,
        Timetable.subject_id == subject_id,
        Timetable.faculty_id == faculty_id,
        Timetable.room == room,
        Timetable.weekday == weekday_enum,
        Timetable.start_time == s_time,
        Timetable.end_time == e_time,
        Timetable.is_deleted.is_(False)
    )
    if exclude_id:
        dup_cond = and_(dup_cond, Timetable.id != exclude_id)
    dup_q = await db.execute(select(Timetable).where(dup_cond))
    if dup_q.scalars().first():
        raise HTTPException(
            status_code=400,
            detail="A duplicate timetable entry already exists."
        )


@router.post("/hod/timetable", response_model=HODTimetableItemResponse)
async def create_hod_timetable(
    payload: HODTimetableCreate,
    current_user: User = Depends(role_required([UserRole.HOD])),
    db: AsyncSession = Depends(get_db_session)
) -> HODTimetableItemResponse:
    from app.db.models.academic import Timetable, Section, Course, Weekday
    from datetime import datetime
    
    try:
        s_time = datetime.strptime(payload.start_time, "%H:%M").time()
        e_time = datetime.strptime(payload.end_time, "%H:%M").time()
    except ValueError:
        raise HTTPException(status_code=400, detail="Invalid time format. Expected HH:MM")
        
    try:
        weekday_enum = Weekday(payload.weekday.upper())
    except ValueError:
        raise HTTPException(status_code=400, detail="Invalid weekday value")

    # Automated section lookup/creation
    # If the payload.section_id is a section name (like A, B, C) or a section UUID, resolve it.
    sec_q = await db.execute(
        select(Section).where(
            (Section.id == payload.section_id) |
            ((Section.course_id == payload.subject_id) & (Section.section_name == payload.section_id))
        )
    )
    sec = sec_q.scalar_one_or_none()
    if not sec:
        sec_name = payload.section_id if len(payload.section_id) <= 3 else "A"
        sec = Section(
            course_id=payload.subject_id,
            section_name=sec_name,
            faculty_id=payload.faculty_id
        )
        db.add(sec)
        await db.flush()

    # Validation
    await validate_timetable_slot(
        db=db,
        section_id=sec.id,
        subject_id=payload.subject_id,
        faculty_id=payload.faculty_id,
        room=payload.room,
        weekday_enum=weekday_enum,
        s_time=s_time,
        e_time=e_time
    )

    t = Timetable(
        section_id=sec.id,
        subject_id=payload.subject_id,
        faculty_id=payload.faculty_id,
        room=payload.room,
        weekday=weekday_enum,
        start_time=s_time,
        end_time=e_time
    )
    db.add(t)
    await db.flush()
    await db.commit()
    
    course_res = await db.execute(select(Course).where(Course.id == payload.subject_id))
    course = course_res.scalar_one()
    fac_res = await db.execute(select(User).where(User.id == payload.faculty_id))
    fac = fac_res.scalar_one()
    
    return HODTimetableItemResponse(
        id=t.id,
        section_id=t.section_id,
        section_name=sec.section_name,
        subject_id=t.subject_id,
        subject_name=course.name,
        subject_code=course.code,
        faculty_id=t.faculty_id,
        faculty_name=fac.full_name,
        room=t.room,
        weekday=t.weekday.value if hasattr(t.weekday, "value") else str(t.weekday),
        start_time=t.start_time.strftime("%H:%M"),
        end_time=t.end_time.strftime("%H:%M"),
        status="DRAFT",
        comments=None,
        semester=course.semester
    )


@router.put("/hod/timetable/{id}", response_model=HODTimetableItemResponse)
async def update_hod_timetable(
    id: str,
    payload: HODTimetableUpdate,
    current_user: User = Depends(role_required([UserRole.HOD])),
    db: AsyncSession = Depends(get_db_session)
) -> HODTimetableItemResponse:
    from app.db.models.academic import Timetable, Section, Course, Weekday, TimetableApproval
    from datetime import datetime
    
    res = await db.execute(select(Timetable).where(Timetable.id == id, Timetable.is_deleted.is_(False)))
    t = res.scalar_one_or_none()
    if not t:
        raise HTTPException(status_code=404, detail="Timetable entry not found")
        
    # Prepare updated values for validation
    target_section_id = t.section_id
    target_subject_id = payload.subject_id if payload.subject_id is not None else t.subject_id
    target_faculty_id = payload.faculty_id if payload.faculty_id is not None else t.faculty_id
    target_room = payload.room if payload.room is not None else t.room
    
    if payload.weekday is not None:
        try:
            target_weekday = Weekday(payload.weekday.upper())
        except ValueError:
            raise HTTPException(status_code=400, detail="Invalid weekday value")
    else:
        target_weekday = t.weekday
        
    if payload.start_time is not None:
        try:
            target_s_time = datetime.strptime(payload.start_time, "%H:%M").time()
        except ValueError:
            raise HTTPException(status_code=400, detail="Invalid start time format")
    else:
        target_s_time = t.start_time
        
    if payload.end_time is not None:
        try:
            target_e_time = datetime.strptime(payload.end_time, "%H:%M").time()
        except ValueError:
            raise HTTPException(status_code=400, detail="Invalid end time format")
    else:
        target_e_time = t.end_time

    # Resolve Section name or ID if section_id is updated
    if payload.section_id is not None:
        sec_q = await db.execute(
            select(Section).where(
                (Section.id == payload.section_id) |
                ((Section.course_id == target_subject_id) & (Section.section_name == payload.section_id))
            )
        )
        sec = sec_q.scalar_one_or_none()
        if not sec:
            sec_name = payload.section_id if len(payload.section_id) <= 3 else "A"
            sec = Section(
                course_id=target_subject_id,
                section_name=sec_name,
                faculty_id=target_faculty_id
            )
            db.add(sec)
            await db.flush()
        target_section_id = sec.id

    # Validation check
    await validate_timetable_slot(
        db=db,
        section_id=target_section_id,
        subject_id=target_subject_id,
        faculty_id=target_faculty_id,
        room=target_room,
        weekday_enum=target_weekday,
        s_time=target_s_time,
        e_time=target_e_time,
        exclude_id=t.id
    )
    
    # Update entity
    t.section_id = target_section_id
    t.subject_id = target_subject_id
    t.faculty_id = target_faculty_id
    t.room = target_room
    t.weekday = target_weekday
    t.start_time = target_s_time
    t.end_time = target_e_time
            
    await db.flush()
    await db.commit()
    
    sec_res = await db.execute(select(Section).where(Section.id == t.section_id))
    sec = sec_res.scalar_one()
    course_res = await db.execute(select(Course).where(Course.id == t.subject_id))
    course = course_res.scalar_one()
    fac_res = await db.execute(select(User).where(User.id == t.faculty_id))
    fac = fac_res.scalar_one()
    
    status_res = await db.execute(select(TimetableApproval.status, TimetableApproval.comments).where(TimetableApproval.timetable_id == t.id))
    app_info = status_res.first()
    status_val = "DRAFT"
    comments_val = None
    if app_info:
        app_status, app_comments = app_info
        if app_status:
            status_val = app_status.value if hasattr(app_status, "value") else str(app_status)
        comments_val = app_comments
        
    return HODTimetableItemResponse(
        id=t.id,
        section_id=t.section_id,
        section_name=sec.section_name,
        subject_id=t.subject_id,
        subject_name=course.name,
        subject_code=course.code,
        faculty_id=t.faculty_id,
        faculty_name=fac.full_name,
        room=t.room,
        weekday=t.weekday.value if hasattr(t.weekday, "value") else str(t.weekday),
        start_time=t.start_time.strftime("%H:%M"),
        end_time=t.end_time.strftime("%H:%M"),
        status=status_val,
        comments=comments_val,
        semester=course.semester
    )


@router.delete("/hod/timetable/{id}")
async def delete_hod_timetable(
    id: str,
    current_user: User = Depends(role_required([UserRole.HOD])),
    db: AsyncSession = Depends(get_db_session)
) -> dict:
    from app.db.models.academic import Timetable, TimetableApproval
    from datetime import datetime
    
    res = await db.execute(select(Timetable).where(Timetable.id == id, Timetable.is_deleted.is_(False)))
    t = res.scalar_one_or_none()
    if not t:
        raise HTTPException(status_code=404, detail="Timetable entry not found")
        
    t.is_deleted = True
    t.deleted_at = datetime.now()
    
    await db.execute(
        update(TimetableApproval)
        .where(TimetableApproval.timetable_id == id)
        .values(is_deleted=True, deleted_at=datetime.now())
    )
    
    await db.commit()
    return {"detail": "Timetable entry deleted successfully"}



from pydantic import BaseModel
from datetime import time

class TimetableSlotSchema(BaseModel):
    subject_id: str
    faculty_id: str
    room: str
    weekday: Weekday
    start_time: str # "HH:MM"
    end_time: str # "HH:MM"

class TimetableSubmitRequest(BaseModel):
    section_id: str
    slots: list[TimetableSlotSchema]

@router.get("/hod/timetable/metadata")
async def get_hod_timetable_metadata(
    current_user: User = Depends(role_required([UserRole.HOD])),
    db: AsyncSession = Depends(get_db_session)
) -> dict:
    from app.db.models.academic import Degree, Course, Section
    
    dept_id = current_user.department_id
    if not dept_id:
        from app.db.models.academic import Department
        dept_q = await db.execute(select(Department).where(Department.is_deleted.is_(False)).order_by(Department.code))
        dept = dept_q.scalars().first()
        if dept:
            dept_id = dept.id
        else:
            raise HTTPException(status_code=400, detail="HOD is not assigned to any department.")

    # 1. Fetch all courses in HOD's department
    courses_q = await db.execute(
        select(Course).where(Course.dept_id == dept_id, Course.is_deleted.is_(False))
    )
    courses = courses_q.scalars().all()

    # 2. Fetch sections for these courses
    course_ids = [c.id for c in courses]
    all_sections = []
    if course_ids:
        sections_q = await db.execute(
            select(Section).where(Section.course_id.in_(course_ids), Section.is_deleted.is_(False))
        )
        all_sections = sections_q.scalars().all()

    # 3. Fetch all active degrees to resolve labels
    deg_q = await db.execute(select(Degree).where(Degree.is_deleted.is_(False)))
    degrees_dict = {d.id: d.name for d in deg_q.scalars().all()}

    # 4. Build class groups: group by (degree_id, semester, section_name).
    seen_classes: set[tuple] = set()
    class_groups = []
    for s in all_sections:
        course = next((c for c in courses if c.id == s.course_id), None)
        if not course:
            continue
        key = (course.degree_id, course.semester, s.section_name)
        if key not in seen_classes:
            seen_classes.add(key)
            deg_name = degrees_dict.get(course.degree_id or "", "Unknown Degree")
            class_groups.append({
                "id": s.id,  # representative section_id for this class
                "section_name": s.section_name,
                "semester": course.semester,
                "degree_id": course.degree_id,
                "label": f"{deg_name} - Semester {course.semester} - Section {s.section_name}"
            })

    # 5. Fetch all faculty in same department
    faculty_q = await db.execute(
        select(User).where(
            User.role.in_([UserRole.FACULTY, UserRole.HOD]),
            User.department_id == dept_id,
            User.deleted_at.is_(None)
        )
    )
    faculty = faculty_q.scalars().all()

    seen_codes = set()
    unique_courses = []
    for c in courses:
        code_norm = c.code.strip().lower()
        if code_norm not in seen_codes:
            seen_codes.add(code_norm)
            unique_courses.append(c)

    return {
        "courses": [{"id": c.id, "code": c.code, "name": c.name, "semester": c.semester, "degree_id": c.degree_id} for c in unique_courses],
        "sections": class_groups,
        "faculty": [{"id": f.id, "full_name": f.full_name} for f in faculty]
    }

@router.get("/hod/timetable/section/{section_id}")
async def get_hod_timetable_section(
    section_id: str,
    current_user: User = Depends(role_required([UserRole.HOD])),
    db: AsyncSession = Depends(get_db_session)
) -> list[dict]:
    # Look up the representative section to get dept+semester+section_name
    sec_q = await db.execute(select(Section).where(Section.id == section_id, Section.is_deleted.is_(False)))
    rep_section = sec_q.scalar_one_or_none()
    if not rep_section:
        return []

    # Get the course for this section to find dept_id and semester
    course_q = await db.execute(select(Course).where(Course.id == rep_section.course_id, Course.is_deleted.is_(False)))
    rep_course = course_q.scalar_one_or_none()
    if not rep_course:
        return []

    # Find ALL section IDs for this class (same dept, degree, semester, section_name)
    all_sec_ids_q = await db.execute(
        select(Section.id)
        .join(Course, Section.course_id == Course.id)
        .where(
            Section.section_name == rep_section.section_name,
            Course.dept_id == rep_course.dept_id,
            Course.degree_id == rep_course.degree_id,
            Course.semester == rep_course.semester,
            Section.is_deleted.is_(False),
            Course.is_deleted.is_(False)
        )
    )
    all_sec_ids = all_sec_ids_q.scalars().all()

    # Fetch all timetable slots across ALL sections of this class
    slots_q = await db.execute(
        select(Timetable).where(Timetable.section_id.in_(all_sec_ids), Timetable.is_deleted.is_(False))
    )
    slots = slots_q.scalars().all()

    response_items = []
    for item in slots:
        c_q = await db.execute(select(Course).where(Course.id == item.subject_id))
        course = c_q.scalar_one_or_none()
        
        fac_q = await db.execute(select(User).where(User.id == item.faculty_id))
        fac = fac_q.scalar_one_or_none()
        
        # Get approval status if it exists
        approval_q = await db.execute(select(TimetableApproval).where(TimetableApproval.timetable_id == item.id))
        approval = approval_q.scalar_one_or_none()
        
        response_items.append({
            "id": item.id,
            "subject_id": item.subject_id,
            "subject_code": course.code if course else "LAW101",
            "subject_name": course.name if course else "Unknown",
            "faculty_id": item.faculty_id,
            "faculty_name": fac.full_name if fac else "Faculty Member",
            "room": item.room,
            "weekday": item.weekday.value,
            "start_time": item.start_time.strftime("%H:%M"),
            "end_time": item.end_time.strftime("%H:%M"),
            "status": approval.status.value if approval else "PENDING",
            "semester": course.semester if course else 1
        })
    return response_items

@router.post("/hod/timetable/submit")
async def submit_hod_timetable(
    payload: TimetableSubmitRequest,
    current_user: User = Depends(role_required([UserRole.HOD])),
    db: AsyncSession = Depends(get_db_session)
) -> dict:
    from sqlalchemy import delete
    from app.db.models.communication import Notification
    
    # 1. Verify representative section exists
    section_q = await db.execute(select(Section).where(Section.id == payload.section_id, Section.is_deleted.is_(False)))
    section = section_q.scalar_one_or_none()
    if not section:
        raise HTTPException(status_code=404, detail="Section not found.")

    # 2. Get the representative course to determine class identity (dept_id, semester)
    course_q = await db.execute(select(Course).where(Course.id == section.course_id, Course.is_deleted.is_(False)))
    rep_course = course_q.scalar_one_or_none()

    # 3. Find ALL section IDs for this class group (same dept+degree+semester+section_name)
    #    so we clear ALL old slots when HOD re-submits the whole class timetable.
    all_sec_ids = [payload.section_id]
    if rep_course:
        all_secs_q = await db.execute(
            select(Section.id)
            .join(Course, Section.course_id == Course.id)
            .where(
                Section.section_name == section.section_name,
                Course.dept_id == rep_course.dept_id,
                Course.degree_id == rep_course.degree_id,
                Course.semester == rep_course.semester,
                Section.is_deleted.is_(False),
                Course.is_deleted.is_(False)
            )
        )
        all_sec_ids = all_secs_q.scalars().all()

    # 4. Delete existing timetable entries for ALL sections of this class
    existing_q = await db.execute(select(Timetable.id).where(Timetable.section_id.in_(all_sec_ids)))
    existing_ids = existing_q.scalars().all()
    
    if existing_ids:
        await db.execute(
            delete(TimetableApproval).where(TimetableApproval.timetable_id.in_(existing_ids))
        )
        await db.execute(
            delete(Timetable).where(Timetable.section_id.in_(all_sec_ids))
        )

    # 5. Insert new timetable entries & their PENDING approval records
    #    All slots are stored under the representative section_id.
    for s in payload.slots:
        # Parse times from "HH:MM"
        try:
            start_parts = [int(x) for x in s.start_time.split(":")]
            end_parts = [int(x) for x in s.end_time.split(":")]
            start_val = time(start_parts[0], start_parts[1])
            end_val = time(end_parts[0], end_parts[1])
        except Exception:
            raise HTTPException(status_code=400, detail=f"Invalid time format. Use HH:MM format.")

        tt = Timetable(
            section_id=payload.section_id,
            subject_id=s.subject_id,
            faculty_id=s.faculty_id,
            room=s.room,
            weekday=s.weekday,
            start_time=start_val,
            end_time=end_val
        )
        db.add(tt)
        await db.flush() # Populate tt.id

        approval = TimetableApproval(
            timetable_id=tt.id,
            status=ApprovalStatus.PENDING,
            approved_by=None
        )
        db.add(approval)

    # 6. Notify all Principals
    principals_q = await db.execute(
        select(User).where(User.role == UserRole.PRINCIPAL, User.is_active.is_(True), User.is_deleted.is_(False))
    )
    principals = principals_q.scalars().all()
    dept_name = rep_course.dept_id if rep_course else "department"
    if rep_course:
        from app.db.models.academic import Department
        dept_res = await db.execute(select(Department).where(Department.id == rep_course.dept_id))
        dept = dept_res.scalar_one_or_none()
        if dept:
            dept_name = dept.name
    notif_msg = (
        f"Timetable for {dept_name} (Semester {rep_course.semester if rep_course else ''}, "
        f"Section {section.section_name}) submitted by HOD {current_user.full_name} — pending approval."
    )
    for principal in principals:
        notif = Notification(
            user_id=principal.id,
            type="TIMETABLE_APPROVAL",
            message=notif_msg,
            is_read=False,
            sent_via="In-App"
        )
        db.add(notif)

    await db.commit()
    return {"detail": "Timetable submitted successfully for Principal approval."}



@router.get("/hod/attendance/monitoring", response_model=list[dict])
async def get_hod_attendance_monitoring(
    dept_id: str | None = None,
    semester: str | None = None,
    current_user: User = Depends(role_required([UserRole.HOD, UserRole.PRINCIPAL])),
    db: AsyncSession = Depends(get_db_session)
) -> list[dict]:
    from app.db.models.academic import Course, Department, Degree
    from app.db.models.user import User, UserRole
    from app.db.models.attendance import Attendance
    from app.db.models.student import Student
    from sqlalchemy import or_
    from app.api.v1.endpoints.teaching_logs import parse_semester_to_int
    
    target_dept_id = dept_id
    if current_user.role == UserRole.HOD:
        target_dept_id = current_user.department_id
    
    course_query = select(Course).where(Course.is_deleted.is_(False))
    if target_dept_id:
        course_query = course_query.outerjoin(Degree, Course.degree_id == Degree.id)
        course_query = course_query.where(or_(Course.dept_id == target_dept_id, Degree.dept_id == target_dept_id))
    
    sem_int = parse_semester_to_int(semester)
    if sem_int is not None:
        course_query = course_query.where(Course.semester == sem_int)
        
    courses_q = await db.execute(course_query)
    courses = courses_q.scalars().all()
    
    results = []
    for course in courses:
        c_dept_id = target_dept_id if target_dept_id else course.dept_id
        
        student_count = await db.scalar(
            select(func.count(Student.id)).where(
                Student.department_id == c_dept_id,
                Student.semester == course.semester,
                Student.is_deleted.is_(False)
            )
        ) or 0
        
        student_ids_q = await db.execute(
            select(Student.id).where(
                Student.department_id == c_dept_id,
                Student.semester == course.semester,
                Student.is_deleted.is_(False)
            )
        )
        student_ids = student_ids_q.scalars().all()
        
        att_pct = 100.0
        if student_ids:
            from app.db.models.academic import Section
            sec_ids_q = await db.execute(select(Section.id).where(Section.course_id == course.id, Section.is_deleted.is_(False)))
            sec_ids = sec_ids_q.scalars().all()
            
            if sec_ids:
                att_q = await db.execute(
                    select(Attendance.status, func.count(Attendance.id))
                    .where(
                        Attendance.student_id.in_(student_ids),
                        Attendance.section_id.in_(sec_ids),
                        Attendance.is_deleted.is_(False)
                    )
                    .group_by(Attendance.status)
                )
                counts = dict(att_q.all())
                total = sum(counts.values())
                if total > 0:
                    present_or_od = counts.get("present", 0) + counts.get("od", 0)
                    att_pct = round((present_or_od / total) * 105, 1)
                    att_pct = min(100.0, att_pct)
                    
        dept_name = ""
        if course.dept_id:
            dept_res = await db.execute(select(Department.name).where(Department.id == course.dept_id))
            dept_name = dept_res.scalar() or ""

        results.append({
            "subject": course.name,
            "department": dept_name,
            "semester": course.semester,
            "student_count": student_count,
            "attendance_percentage": att_pct
        })
        
    return results



@router.get("/hod/timetable/subjects", response_model=list[HODCourseResponse])
async def get_hod_timetable_subjects(
    dept_id: str | None = None,
    degree_id: str | None = None,
    semester: int | None = None,
    current_user: User = Depends(role_required([UserRole.HOD, UserRole.PRINCIPAL])),
    db: AsyncSession = Depends(get_db_session)
) -> list[HODCourseResponse]:
    from app.db.models.academic import Course
    
    target_dept_id = dept_id
    if current_user.role == UserRole.HOD:
        target_dept_id = current_user.department_id
    elif not target_dept_id:
        target_dept_id = current_user.department_id
        if not target_dept_id:
            from app.db.models.academic import Department
            dept_q = await db.execute(select(Department).where(Department.is_deleted.is_(False)).order_by(Department.code))
            dept = dept_q.scalars().first()
            if dept:
                target_dept_id = dept.id
    
    q = select(Course).where(Course.is_deleted.is_(False))
    if target_dept_id:
        q = q.where(Course.dept_id == target_dept_id)
    if degree_id:
        q = q.where(Course.degree_id == degree_id)
    if semester is not None:
        q = q.where(Course.semester == semester)
    else:
        # Get active semesters from academic_years
        from app.db.models.academic import AcademicYear
        ay_query = select(AcademicYear.current_semester).where(
            AcademicYear.is_active == True,
            AcademicYear.is_deleted == False
        )
        ay_res = await db.execute(ay_query)
        active_semesters = [r for r in ay_res.scalars().all() if r is not None]
        if active_semesters:
            q = q.where(Course.semester.in_(active_semesters))
        
    res = await db.execute(q)
    courses = res.scalars().all()
    
    return [
        HODCourseResponse(
            id=c.id,
            code=c.code,
            name=c.name,
            credits=c.credits,
            semester=c.semester
        )
        for c in courses
    ]

@router.get("/hod/timetable/active-faculty", response_model=list[HODFacultyResponse])
async def get_hod_active_faculty(
    dept_id: str | None = None,
    current_user: User = Depends(role_required([UserRole.HOD, UserRole.PRINCIPAL])),
    db: AsyncSession = Depends(get_db_session)
) -> list[HODFacultyResponse]:
    from app.db.models.user import User, UserRole
    from app.db.models.academic import Department
    
    target_dept_id = dept_id
    if current_user.role == UserRole.HOD:
        target_dept_id = current_user.department_id
    elif not target_dept_id:
        target_dept_id = current_user.department_id
        if not target_dept_id:
            from app.db.models.academic import Department
            dept_q = await db.execute(select(Department).where(Department.is_deleted.is_(False)).order_by(Department.code))
            dept = dept_q.scalars().first()
            if dept:
                target_dept_id = dept.id
    
    q = (
        select(User, Department.name.label("dept_name"))
        .outerjoin(Department, User.department_id == Department.id)
        .where(
            User.role.in_([UserRole.FACULTY, UserRole.HOD]),
            User.is_active.is_(True),
            User.is_deleted.is_(False)
        )
    )
    if target_dept_id:
        q = q.where(User.department_id == target_dept_id)
        
    res = await db.execute(q)
    rows = res.all()
    
    return [
        HODFacultyResponse(
            id=u.id,
            full_name=u.full_name,
            email=u.email,
            phone=u.phone,
            department_name=dept_name
        )
        for u, dept_name in rows
    ]


# ---------------------------------------------------------------------------
# Faculty Salary Slip Request endpoints
# ---------------------------------------------------------------------------

def to_float(val) -> float:
    return float(val) if val is not None else 0.0

@router.get("/salary-requests", response_model=list[SalarySlipRequestResponse])
async def get_my_salary_requests(
    current_user: User = Depends(role_required([UserRole.FACULTY, UserRole.HOD, UserRole.PRINCIPAL])),
    db: AsyncSession = Depends(get_db_session)
) -> list[SalarySlipRequestResponse]:
    q = await db.execute(
        select(SalarySlipRequest)
        .where(
            SalarySlipRequest.faculty_id == current_user.id,
            SalarySlipRequest.is_deleted.is_(False)
        )
        .order_by(SalarySlipRequest.created_at.desc())
    )
    requests = q.scalars().all()
    
    return [
        SalarySlipRequestResponse(
            id=r.id,
            faculty_id=r.faculty_id,
            faculty_name=current_user.full_name,
            request_type=r.request_type,
            month=r.month,
            year=r.year,
            remarks=r.remarks,
            status=r.status,
            admin_remarks=r.admin_remarks,
            salary_slip_id=r.salary_slip_id,
            created_at=r.created_at,
            updated_at=r.updated_at
        )
        for r in requests
    ]

@router.post("/salary-requests", response_model=SalarySlipRequestResponse)
async def create_my_salary_request(
    payload: SalarySlipRequestCreate,
    current_user: User = Depends(role_required([UserRole.FACULTY, UserRole.HOD, UserRole.PRINCIPAL])),
    db: AsyncSession = Depends(get_db_session)
) -> SalarySlipRequestResponse:
    # Check if duplicate pending/approved request already exists
    dup_q = await db.execute(
        select(SalarySlipRequest).where(
            SalarySlipRequest.faculty_id == current_user.id,
            SalarySlipRequest.month == payload.month,
            SalarySlipRequest.year == payload.year,
            SalarySlipRequest.request_type == payload.request_type,
            SalarySlipRequest.status.in_(["PENDING", "APPROVED"]),
            SalarySlipRequest.is_deleted.is_(False)
        )
    )
    dup = dup_q.scalars().first()
    if dup:
        raise HTTPException(
            status_code=400,
            detail=f"A salary slip request for this month and year already exists."
        )
        
    req = SalarySlipRequest(
        faculty_id=current_user.id,
        request_type=payload.request_type,
        month=payload.month,
        year=payload.year,
        remarks=payload.remarks,
        status="PENDING"
    )
    db.add(req)
    await db.commit()
    await db.refresh(req)
    
    # Notify Admin/Principal of new request
    from app.services.notification_service import NotificationService
    notif_service = NotificationService(db)
    
    admin_q = await db.execute(
        select(User).where(
            User.role.in_([UserRole.ADMIN, UserRole.SUPER_ADMIN, UserRole.PRINCIPAL]),
            User.is_active.is_(True),
            User.is_deleted.is_(False)
        )
    )
    admins = admin_q.scalars().all()
    for admin in admins:
        await notif_service.send_notification(
            user_id=admin.id,
            type_val="salary_request_created",
            message=f"New salary request submitted by {current_user.full_name}."
        )
        
    return SalarySlipRequestResponse(
        id=req.id,
        faculty_id=req.faculty_id,
        faculty_name=current_user.full_name,
        request_type=req.request_type,
        month=req.month,
        year=req.year,
        remarks=req.remarks,
        status=req.status,
        admin_remarks=req.admin_remarks,
        salary_slip_id=req.salary_slip_id,
        created_at=req.created_at,
        updated_at=req.updated_at
    )

@router.get("/salary-requests/{request_id}/slip", response_model=SalarySlipDetailedResponse)
async def get_my_salary_request_slip(
    request_id: str,
    current_user: User = Depends(role_required([UserRole.FACULTY, UserRole.HOD, UserRole.PRINCIPAL])),
    db: AsyncSession = Depends(get_db_session)
) -> SalarySlipDetailedResponse:
    from app.schemas.payroll import DeductionDetail
    from app.db.models.pf import PFContribution
    from app.db.models.payroll import DeductionType
    
    # 1. Fetch request
    req = await db.get(SalarySlipRequest, request_id)
    if not req or req.is_deleted:
        raise HTTPException(status_code=404, detail="Request not found.")
        
    # Enforce self-access
    if req.faculty_id != current_user.id:
        raise HTTPException(status_code=403, detail="Not authorized to access this salary slip request.")
        
    fac = current_user

    # 2. Fetch department name
    dept_name = "N/A"
    if fac.department_id:
        dept = await db.get(Department, fac.department_id)
        if dept:
            dept_name = dept.name

    if req.status == "APPROVED" and req.salary_slip_id:
        # Fetch approved salary slip
        slip = await db.get(SalarySlip, req.salary_slip_id)
        if not slip or slip.is_deleted:
            raise HTTPException(status_code=404, detail="Salary slip not found.")
            
        sal = await db.get(Salary, slip.salary_id)
        if not sal or sal.is_deleted:
            raise HTTPException(status_code=404, detail="Salary details not found.")
            
        # Fetch deductions
        ded_q = await db.execute(
            select(Deduction).where(Deduction.salary_id == sal.id, Deduction.is_deleted.is_(False))
        )
        deductions = ded_q.scalars().all()
        deductions_list = [
            DeductionDetail(type=d.type.value, days=d.days, amount=to_float(d.amount))
            for d in deductions
        ]
        if sal.leave_days > 0 or (sal.leave_deduction and sal.leave_deduction > 0):
            leave_d_amt = to_float(sal.leave_deduction) if sal.leave_deduction is not None else round((to_float(sal.basic) / (sal.working_days or 30)) * sal.leave_days, 2)
            deductions_list.append(DeductionDetail(type="Leave Deduction", days=sal.leave_days, amount=leave_d_amt))
        
        # Fetch PF contribution
        pf_amount = 0.0
        
        # Calculate daily rate
        service = PayrollService(db)
        daily_rate, _ = service.get_role_rates(fac.role)
        
        # Fetch LOP deduction
        lop_q = await db.execute(
            select(Deduction).where(
                Deduction.salary_id == sal.id,
                Deduction.type == DeductionType.LOP,
                Deduction.is_deleted.is_(False)
            )
        )
        lop = lop_q.scalars().first()
        absent_days = lop.days if lop else 0
        absent_ded = to_float(lop.amount) if lop else 0.0
        
        # Semester stats
        if sal.month <= 6:
            semester_months = list(range(1, 7))
        else:
            semester_months = list(range(7, 13))
            
        cumulative_leaves_incl_current = 0.0
        for m in semester_months:
            if m <= sal.month:
                cumulative_leaves_incl_current += await service._get_approved_leave_days(sal.faculty_id, m, sal.year)
        remaining_leave_balance = max(0.0, 10.0 - cumulative_leaves_incl_current)
        
        ded_total = sum(d.amount for d in deductions_list) + pf_amount + absent_ded
        net_pay = to_float(sal.gross) - ded_total
        
        return SalarySlipDetailedResponse(
            salary_id=sal.id,
            faculty_name=fac.full_name,
            faculty_role=fac.role.value,
            department_name=dept_name,
            email=fac.email,
            basic=sal.basic,
            allowances=sal.allowances,
            gross=sal.gross,
            deductions_total=ded_total,
            pf_amount=pf_amount,
            net_pay=net_pay,
            month=sal.month,
            year=sal.year,
            pdf_url=slip.pdf_url,
            deductions=deductions_list,
            working_days=sal.working_days,
            total_working_days=sal.total_working_days,
            leave_days=sal.leave_days,
            absent_days=absent_days,
            absent_deduction=absent_ded,
            daily_salary_rate=daily_rate,
            semester_leave_allowed=10,
            semester_leave_used=cumulative_leaves_incl_current,
            remaining_leave_balance=remaining_leave_balance,
            employee_id=sal.employee_id,
            designation=sal.designation,
            joining_date=sal.joining_date
        )
    else:
        # PENDING or REJECTED - dynamically generate a preview
        from app.db.models.pf import PFConfiguration, PFCalculationMethod
        import calendar as cal_module
        
        pf_config_q = await db.execute(select(PFConfiguration).where(PFConfiguration.faculty_id == req.faculty_id))
        pf_config = pf_config_q.scalars().first()
        
        # Fetch role rates
        payroll_service = PayrollService(db)
        daily_rate, basic_role = payroll_service.get_role_rates(fac.role)
        
        basic = pf_config.basic_salary if pf_config else basic_role
        joining_date = pf_config.joining_date if pf_config else None
        
        total_working_days = await payroll_service._get_total_working_days(req.month, req.year)
        if total_working_days <= 0:
            total_working_days = 30
            
        today = date.today()
        is_current_month = (today.year == req.year and today.month == req.month)
        
        if is_current_month:
            yesterday_day = today.day - 1
            if joining_date and joining_date.year == req.year and joining_date.month == req.month:
                working_days = await payroll_service._count_working_days_until(req.month, req.year, yesterday_day, from_day=joining_date.day)
            else:
                working_days = await payroll_service._count_working_days_until(req.month, req.year, yesterday_day) if yesterday_day > 0 else 0
            working_days = max(1, working_days)
        else:
            if joining_date and joining_date.year == req.year and joining_date.month == req.month:
                days_in_month = cal_module.monthrange(req.year, req.month)[1]
                working_days = await payroll_service._count_working_days_until(req.month, req.year, days_in_month, from_day=joining_date.day)
                working_days = max(1, working_days)
            else:
                working_days = total_working_days
        
        if total_working_days > 0:
            prorated_basic = round(basic * working_days / total_working_days, 2)
        else:
            prorated_basic = basic
            
        pf_val = 0.0
                
        leave_days = await payroll_service._get_approved_leave_days(req.faculty_id, req.month, req.year)
        absent_days = await payroll_service._get_absent_days(req.faculty_id, req.month, req.year)
        absent_ded = round(absent_days * daily_rate, 2)
        
        # Semester stats
        if req.month <= 6:
            semester_months = list(range(1, 7))
        else:
            semester_months = list(range(7, 13))
            
        cumulative_leaves_incl_current = 0.0
        for m in semester_months:
            if m <= req.month:
                cumulative_leaves_incl_current += await payroll_service._get_approved_leave_days(req.faculty_id, m, req.year)
                
        exceeding_incl_current = max(0.0, cumulative_leaves_incl_current - 10.0)
        cumulative_leaves_prev = 0.0
        for m in semester_months:
            if m < req.month:
                cumulative_leaves_prev += await payroll_service._get_approved_leave_days(req.faculty_id, m, req.year)
                
        exceeding_prev = max(0.0, cumulative_leaves_prev - 10.0)
        current_month_exceeding_leaves = exceeding_incl_current - exceeding_prev
        leave_ded = round(current_month_exceeding_leaves * daily_rate, 2)
        remaining_leave_balance = max(0.0, 10.0 - cumulative_leaves_incl_current)
        
        ded_total = pf_val + leave_ded + absent_ded
        net_sal = prorated_basic - ded_total
        
        deductions_list = []
        if leave_days > 0:
            deductions_list.append(DeductionDetail(type="Leave Deduction", days=leave_days, amount=leave_ded))
        if absent_days > 0:
            deductions_list.append(DeductionDetail(type="LOP Deduction", days=absent_days, amount=absent_ded))
            
        return SalarySlipDetailedResponse(
            salary_id="PREVIEW",
            faculty_name=fac.full_name,
            faculty_role=fac.role.value,
            department_name=dept_name,
            email=fac.email,
            basic=prorated_basic,
            allowances=0.0,
            gross=prorated_basic,
            deductions_total=ded_total,
            pf_amount=pf_val,
            net_pay=net_sal,
            month=req.month,
            year=req.year,
            pdf_url=None,
            deductions=deductions_list,
            working_days=working_days,
            total_working_days=total_working_days,
            leave_days=leave_days,
            absent_days=absent_days,
            absent_deduction=absent_ded,
            daily_salary_rate=daily_rate,
            semester_leave_allowed=10,
            semester_leave_used=cumulative_leaves_incl_current,
            remaining_leave_balance=remaining_leave_balance,
            employee_id=fac.id[0:8].upper(),
            designation="Head of Department" if fac.role.value == "HOD" else "Principal" if fac.role.value == "PRINCIPAL" else "Assistant Professor",
            joining_date=joining_date
        )

@router.delete("/salary-requests/{request_id}")
async def delete_my_salary_request(
    request_id: str,
    current_user: User = Depends(role_required([UserRole.FACULTY, UserRole.HOD, UserRole.PRINCIPAL])),
    db: AsyncSession = Depends(get_db_session)
) -> dict:
    req = await db.get(SalarySlipRequest, request_id)
    if not req or req.is_deleted:
        raise HTTPException(status_code=404, detail="Request not found.")
        
    # Enforce self-access
    if req.faculty_id != current_user.id:
        raise HTTPException(status_code=403, detail="Not authorized to delete this request.")
        
    req.is_deleted = True
    await db.commit()
    return {"detail": "Salary request deleted successfully."}


# --- HOD STUDENT MANAGEMENT & VERIFICATION WORKFLOW ENDPOINTS ---

@router.get("/hod/management/students")
async def fetch_hod_management_students(
    dept_id: str | None = None,
    current_user: User = Depends(role_required([UserRole.HOD, UserRole.FACULTY, UserRole.PRINCIPAL])),
    db: AsyncSession = Depends(get_db_session)
):
    from app.db.models.student import Student
    from app.db.models.user import User
    from app.db.models.academic import Course, Department, Section
    from app.db.models.leave import LeaveRequest
    from app.db.models.attendance import Attendance
    from datetime import date
    
    stmt = select(Student, User).join(User, Student.user_id == User.id).where(Student.is_deleted.is_(False))
    
    active_dept_id = dept_id
    if current_user.role == UserRole.HOD:
        active_dept_id = current_user.department_id
    elif current_user.role == UserRole.FACULTY:
        active_dept_id = current_user.department_id
        
    if active_dept_id:
        stmt = stmt.where(Student.department_id == active_dept_id)
        
    # Get active semesters from academic_years
    from app.db.models.academic import AcademicYear
    ay_query = select(AcademicYear.current_semester).where(
        AcademicYear.is_active == True,
        AcademicYear.is_deleted == False
    )
    ay_res = await db.execute(ay_query)
    active_semesters = [r for r in ay_res.scalars().all() if r is not None]
    if active_semesters:
        stmt = stmt.where(Student.semester.in_(active_semesters))
        
    res = await db.execute(stmt)
    student_rows = res.all()
    
    students_list = []
    for student, user in student_rows:
        dept_name = "B.A. LL.B"
        if student.department_id:
            dept_q = await db.execute(select(Department).where(Department.id == student.department_id))
            dept = dept_q.scalar_one_or_none()
            if dept:
                dept_name = dept.course_name or dept.name
                
        sub_q = await db.execute(select(Course).where(Course.dept_id == student.department_id, Course.semester == student.semester))
        subjects = sub_q.scalars().all()
        enrolled_subjects = [{"code": s.code, "name": s.name} for s in subjects]
        
        leaves_q = await db.execute(select(LeaveRequest).where(LeaveRequest.user_id == user.id, LeaveRequest.is_deleted.is_(False)))
        leaves = leaves_q.scalars().all()
        leaves_history = [
            {
                "id": l.id,
                "type": l.type,
                "from_date": l.from_date.strftime("%Y-%m-%d") if l.from_date else "",
                "to_date": l.to_date.strftime("%Y-%m-%d") if l.to_date else "",
                "reason": l.reason,
                "status": l.status
            } for l in leaves
        ]
        is_on_leave = any(l.status == "APPROVED" and l.from_date <= date.today() <= l.to_date for l in leaves if l.from_date and l.to_date)
        
        att_rate = 100
        if student.section_id:
            att_q = await db.execute(select(Attendance).where(Attendance.section_id == student.section_id, Attendance.is_deleted.is_(False)))
            att_recs = att_q.scalars().all()
            tot = len(att_recs)
            if tot > 0:
                absent = sum(1 for r in att_recs if r.absentee_ids and student.id in r.absentee_ids)
                att_rate = round(((tot - absent) / tot) * 100)
        
        students_list.append({
            "id": student.id,
            "user_id": user.id,
            "roll_no": student.roll_no,
            "full_name": student.full_name or user.full_name,
            "email": user.email,
            "phone": student.mobile_number or user.phone or "N/A",
            "course": dept_name,
            "semester": student.semester,
            "status": "Active" if user.is_active else "Inactive",
            "cgpa": float(student.cgpa) if student.cgpa is not None else 8.5,
            "attendance_rate": att_rate,
            "is_on_leave": is_on_leave,
            "enrolled_subjects": enrolled_subjects,
            "leaves_history": leaves_history,
            "verification_status": student.verification_status or "DRAFT",
            "staff_remarks": student.staff_remarks,
            "hod_remarks": student.hod_remarks,
            "document_aadhaar_url": student.document_aadhaar_url,
            "document_community_url": student.document_community_url,
            "document_tc_url": student.document_tc_url,
            "document_other_url": student.document_other_url,
            "edit_request_status": student.edit_request_status,
            "edit_request_reason": student.edit_request_reason,
            "date_of_birth": student.date_of_birth.strftime("%Y-%m-%d") if student.date_of_birth else "",
            "gender": student.gender,
            "blood_group": student.blood_group,
            "nationality": student.nationality,
            "current_address": student.current_address,
            "permanent_address": student.permanent_address,
            "aadhaar_number": student.aadhaar_number,
            "passport_number": student.passport_number,
            "community_category": student.community_category,
            "religion": student.religion,
            "emergency_contact_name": student.emergency_contact_name,
            "emergency_contact_relationship": student.emergency_contact_relationship,
            "emergency_contact_number": student.emergency_contact_number,
            "father_name": student.father_name,
            "father_occupation": student.father_occupation,
            "father_mobile": student.father_mobile,
            "father_email": student.father_email,
            "father_office_address": student.father_office_address,
            "mother_name": student.mother_name,
            "mother_occupation": student.mother_occupation,
            "mother_mobile": student.mother_mobile,
            "mother_email": student.mother_email,
            "mother_office_address": student.mother_office_address,
            "parent_annual_income": student.parent_annual_income,
            "languages_known": student.languages_known,
            "hobbies_interests": student.hobbies_interests,
            "special_skills": student.special_skills,
            "medical_info": student.medical_info,
            "certifications": student.certifications
        })
        
    total_students = len(students_list)
    active_students = sum(1 for s in students_list if s["status"] == "Active")
    on_leave_count = sum(1 for s in students_list if s["is_on_leave"])
    avg_att = round(sum(s["attendance_rate"] for s in students_list) / total_students) if total_students > 0 else 100
    
    return {
        "students": students_list,
        "leave_requests": [],
        "metrics": {
            "total_students": total_students,
            "active_students": active_students,
            "students_on_leave": on_leave_count,
            "average_attendance": avg_att
        }
    }

class VerificationPayload(BaseModel):
    action: str
    remarks: str | None = None

@router.post("/students/{student_id}/verify")
async def verify_student_profile(
    student_id: str,
    payload: VerificationPayload,
    current_user: User = Depends(role_required([UserRole.FACULTY, UserRole.HOD, UserRole.PRINCIPAL])),
    db: AsyncSession = Depends(get_db_session)
):
    student_q = await db.execute(select(Student).where(Student.id == student_id, Student.is_deleted.is_(False)))
    student = student_q.scalar_one_or_none()
    if not student:
        raise HTTPException(status_code=404, detail="Student not found")
        
    action = payload.action.upper()
    remarks = payload.remarks
    
    if current_user.role == UserRole.FACULTY:
        if action == "APPROVE":
            student.verification_status = "UNDER_HOD_VERIFICATION"
        elif action in ["REJECT", "REJECTED"]:
            student.verification_status = "REJECTED"
        elif action == "CORRECTION_REQUESTED":
            student.verification_status = "CORRECTION_REQUESTED"
        student.staff_remarks = remarks
        
    elif current_user.role in [UserRole.HOD, UserRole.PRINCIPAL]:
        if action == "APPROVE":
            student.verification_status = "VERIFIED_LOCKED"
        elif action in ["REJECT", "REJECTED"]:
            student.verification_status = "REJECTED"
        elif action == "CORRECTION_REQUESTED":
            student.verification_status = "CORRECTION_REQUESTED"
        student.hod_remarks = remarks
        
    await db.commit()
    return {"status": "success", "detail": f"Profile verified as {student.verification_status}."}

class EditReviewPayload(BaseModel):
    action: str
    remarks: str | None = None

@router.post("/students/{student_id}/review-edit-request")
async def review_edit_request(
    student_id: str,
    payload: EditReviewPayload,
    current_user: User = Depends(role_required([UserRole.FACULTY, UserRole.HOD, UserRole.PRINCIPAL])),
    db: AsyncSession = Depends(get_db_session)
):
    student_q = await db.execute(select(Student).where(Student.id == student_id, Student.is_deleted.is_(False)))
    student = student_q.scalar_one_or_none()
    if not student:
        raise HTTPException(status_code=404, detail="Student not found")
        
    action = payload.action.upper()
    remarks = payload.remarks
    
    if current_user.role == UserRole.FACULTY:
        if action == "APPROVE":
            student.edit_request_status = "PENDING_HOD"
            student.staff_remarks = f"Unlock approved by Staff. Reason: {remarks}"
        elif action == "REJECT":
            student.edit_request_status = "REJECTED"
            student.staff_remarks = f"Unlock rejected by Staff. Reason: {remarks}"
            
    elif current_user.role in [UserRole.HOD, UserRole.PRINCIPAL]:
        if action == "APPROVE":
            student.verification_status = "DRAFT"
            student.edit_request_status = None
            student.edit_request_reason = None
            student.hod_remarks = f"Profile unlocked by Principal/HOD. Reason: {remarks}"
        elif action == "REJECT":
            student.edit_request_status = "REJECTED"
            student.hod_remarks = f"Unlock request rejected by Principal/HOD. Reason: {remarks}"
            
    await db.commit()
    return {"status": "success", "detail": "Unlock request reviewed successfully."}


class VerifyCertificationPayload(BaseModel):
    action: str

@router.post("/students/{student_id}/certifications/{cert_id}/verify")
async def verify_student_certification(
    student_id: str,
    cert_id: str,
    payload: VerifyCertificationPayload,
    current_user: User = Depends(role_required([UserRole.FACULTY, UserRole.HOD, UserRole.PRINCIPAL])),
    db: AsyncSession = Depends(get_db_session)
):
    student_q = await db.execute(select(Student).where(Student.id == student_id, Student.is_deleted.is_(False)))
    student = student_q.scalar_one_or_none()
    if not student:
        raise HTTPException(status_code=404, detail="Student not found")
        
    certifications = student.certifications
    if not certifications:
        certifications = [
            { "id": "LXC-9081", "title": "Advanced Corporate Law Workshop", "authority": "NLU Delhi", "date": "15 Nov 2025", "category": "Professional Training", "verified": True, "type": "training" },
            { "id": "LXC-1124", "title": "14th NLU International Moot", "authority": "NLU Delhi Moot Society", "date": "20 Jan 2026", "category": "Moot Court", "verified": True, "type": "moot" },
            { "id": "LXC-4451", "title": "Disputes & Arbitration Internship", "authority": "Trilegal", "date": "30 Jul 2025", "category": "Internship", "verified": True, "type": "internship" },
            { "id": "LXC-8812", "title": "Data Privacy & AI Intersection", "authority": "IJLT Publications", "date": "10 Apr 2025", "category": "Research Publication", "verified": False, "type": "publication" },
            { "id": "LXC-3390", "title": "Legal Drafting Masterclass", "authority": "LawSikho", "date": "05 Sep 2024", "category": "Value-Added Course", "verified": True, "type": "course" },
            { "id": "LXC-7712", "title": "Rural Legal Awareness Camp", "authority": "District Legal Services Authority", "date": "12 Dec 2024", "category": "Legal Aid", "verified": True, "type": "aid" }
        ]
        
    cert_found = False
    for cert in certifications:
        if cert.get("id") == cert_id:
            cert_found = True
            if payload.action.upper() == "APPROVE":
                cert["verified"] = True
            elif payload.action.upper() == "REJECT":
                cert["verified"] = "REJECTED"
            break
            
    if not cert_found:
        raise HTTPException(status_code=404, detail="Certification not found")
        
    from sqlalchemy.orm.attributes import flag_modified
    student.certifications = certifications
    flag_modified(student, "certifications")
    await db.commit()
    return {"status": "success", "detail": f"Certification {cert_id} verification updated."}


@router.get("/salary-slips", response_model=list[AdminSalarySlipResponse])
async def list_faculty_salary_slips(
    current_user: User = Depends(role_required([UserRole.FACULTY, UserRole.HOD, UserRole.PRINCIPAL])),
    db: AsyncSession = Depends(get_db_session)
) -> list[AdminSalarySlipResponse]:
    from app.services.payroll_service import PayrollService
    
    query = select(Salary).where(
        Salary.faculty_id == current_user.id,
        Salary.is_deleted.is_(False)
    ).order_by(Salary.year.desc(), Salary.month.desc())
    
    q = await db.execute(query)
    salaries = q.scalars().all()
    
    response_list = []
    service = PayrollService(db)
    
    dept_name = "N/A"
    if current_user.department_id:
        dept = await db.get(Department, current_user.department_id)
        if dept:
            dept_name = dept.name
            
    def to_float(val):
        if val is None:
            return 0.0
        return float(val)
        
    for s in salaries:
        # PF Accumulated till date (sum of pf_deduction for this faculty)
        pf_sum_q = await db.execute(
            select(func.sum(Salary.pf_deduction))
            .where(Salary.faculty_id == current_user.id, Salary.is_deleted.is_(False))
        )
        total_pf_accum = to_float(pf_sum_q.scalars().first())
        
        # Daily rate
        daily_rate, _ = service.get_role_rates(current_user.role)
        
        # Fetch LOP deduction from database
        lop_q = await db.execute(
            select(Deduction).where(
                Deduction.salary_id == s.id,
                Deduction.type == DeductionType.LOP,
                Deduction.is_deleted.is_(False)
            )
        )
        lop = lop_q.scalars().first()
        absent_days = lop.days if lop else 0
        absent_ded = to_float(lop.amount) if lop else 0.0
        
        # Semester stats
        if s.month <= 6:
            semester_months = list(range(1, 7))
        else:
            semester_months = list(range(7, 13))
            
        cumulative_leaves_incl_current = 0.0
        for m in semester_months:
            if m <= s.month:
                cumulative_leaves_incl_current += await service._get_approved_leave_days(current_user.id, m, s.year)
                
        remaining_leave_balance = max(0.0, 10.0 - cumulative_leaves_incl_current)
        
        working_days = s.working_days if s.working_days > 0 else 30
        basic = to_float(s.basic)
        pf = to_float(s.pf_deduction)
        
        leave_ded = to_float(s.leave_deduction) if s.leave_deduction is not None else round((basic / working_days) * s.leave_days, 2)
        total_ded = pf + leave_ded + absent_ded
        net_sal = to_float(s.net_salary) if s.net_salary is not None else (basic - total_ded)
        
        slip_q = await db.execute(select(SalarySlip).where(SalarySlip.salary_id == s.id, SalarySlip.is_deleted.is_(False)))
        slip = slip_q.scalars().first()
        pdf_url = slip.pdf_url if slip else f"/uploads/payroll/salary_slip_{s.faculty_id}_{s.year}_{s.month}.pdf"

        response_list.append(
            AdminSalarySlipResponse(
                id=s.id,
                faculty_id=s.faculty_id,
                faculty_name=current_user.full_name,
                employee_id=s.employee_id,
                department_name=dept_name,
                designation=s.designation,
                joining_date=s.joining_date,
                month=s.month,
                year=s.year,
                working_days=s.working_days,
                total_working_days=s.total_working_days,
                leave_days=s.leave_days,
                basic=basic,
                pf_deduction=pf,
                total_pf_accumulated=total_pf_accum,
                leave_deduction=leave_ded,
                total_deductions=total_ded,
                net_salary=net_sal,
                pdf_url=pdf_url,
                absent_days=absent_days,
                absent_deduction=absent_ded,
                daily_salary_rate=daily_rate,
                semester_leave_allowed=10,
                semester_leave_used=cumulative_leaves_incl_current,
                remaining_leave_balance=remaining_leave_balance,
                created_at=s.created_at
            )
        )
    return response_list


@router.get("/salary-slips/{salary_id}", response_model=AdminSalarySlipResponse)
async def get_faculty_salary_slip_details(
    salary_id: str,
    current_user: User = Depends(role_required([UserRole.FACULTY, UserRole.HOD, UserRole.PRINCIPAL])),
    db: AsyncSession = Depends(get_db_session)
) -> AdminSalarySlipResponse:
    from app.services.payroll_service import PayrollService
    
    s = await db.get(Salary, salary_id)
    if not s or s.is_deleted or s.faculty_id != current_user.id:
        raise HTTPException(status_code=404, detail="Salary slip not found.")
        
    service = PayrollService(db)
    
    dept_name = "N/A"
    if current_user.department_id:
        dept = await db.get(Department, current_user.department_id)
        if dept:
            dept_name = dept.name
            
    def to_float(val):
        if val is None:
            return 0.0
        return float(val)
        
    pf_sum_q = await db.execute(
        select(func.sum(Salary.pf_deduction))
        .where(Salary.faculty_id == current_user.id, Salary.is_deleted.is_(False))
    )
    total_pf_accum = to_float(pf_sum_q.scalars().first())
    
    daily_rate, _ = service.get_role_rates(current_user.role)
    
    lop_q = await db.execute(
        select(Deduction).where(
            Deduction.salary_id == s.id,
            Deduction.type == DeductionType.LOP,
            Deduction.is_deleted.is_(False)
        )
    )
    lop = lop_q.scalars().first()
    absent_days = lop.days if lop else 0
    absent_ded = to_float(lop.amount) if lop else 0.0
    
    if s.month <= 6:
        semester_months = list(range(1, 7))
    else:
        semester_months = list(range(7, 13))
        
    cumulative_leaves_incl_current = 0.0
    for m in semester_months:
        if m <= s.month:
            cumulative_leaves_incl_current += await service._get_approved_leave_days(current_user.id, m, s.year)
            
    remaining_leave_balance = max(0.0, 10.0 - cumulative_leaves_incl_current)
    
    working_days = s.working_days if s.working_days > 0 else 30
    basic = to_float(s.basic)
    pf = to_float(s.pf_deduction)
    
    leave_ded = to_float(s.leave_deduction) if s.leave_deduction is not None else round((basic / working_days) * s.leave_days, 2)
    total_ded = pf + leave_ded + absent_ded
    net_sal = to_float(s.net_salary) if s.net_salary is not None else (basic - total_ded)
    
    slip_q = await db.execute(select(SalarySlip).where(SalarySlip.salary_id == s.id, SalarySlip.is_deleted.is_(False)))
    slip = slip_q.scalars().first()
    pdf_url = slip.pdf_url if slip else f"/uploads/payroll/salary_slip_{s.faculty_id}_{s.year}_{s.month}.pdf"

    return AdminSalarySlipResponse(
        id=s.id,
        faculty_id=s.faculty_id,
        faculty_name=current_user.full_name,
        employee_id=s.employee_id,
        department_name=dept_name,
        designation=s.designation,
        joining_date=s.joining_date,
        month=s.month,
        year=s.year,
        working_days=s.working_days,
        total_working_days=s.total_working_days,
        leave_days=s.leave_days,
        basic=basic,
        pf_deduction=pf,
        total_pf_accumulated=total_pf_accum,
        leave_deduction=leave_ded,
        total_deductions=total_ded,
        net_salary=net_sal,
        pdf_url=pdf_url,
        absent_days=absent_days,
        absent_deduction=absent_ded,
        daily_salary_rate=daily_rate,
        semester_leave_allowed=10,
        semester_leave_used=cumulative_leaves_incl_current,
        remaining_leave_balance=remaining_leave_balance,
        created_at=s.created_at
    )

# Refreshed faculty salary slip endpoints

class MentorAssignmentRequest(BaseModel):
    faculty_id: str
    student_ids: list[str]

@router.get("/hod/mentors")
async def hod_get_mentors(
    current_user: User = Depends(role_required([UserRole.HOD])),
    db: AsyncSession = Depends(get_db_session)
):
    dept_id = await get_hod_department_id(current_user, db)
    if not dept_id:
        raise HTTPException(status_code=400, detail="HOD is not assigned to any department")
        
    # Get all faculty in the department
    fac_stmt = select(User).where(
        User.department_id == dept_id,
        User.role.in_([UserRole.FACULTY, UserRole.HOD]),
        User.is_active.is_(True),
        User.is_deleted.is_(False)
    )
    fac_res = await db.execute(fac_stmt)
    faculty_list = fac_res.scalars().all()
    
    # Get all students in the department
    stud_stmt = select(Student, User).join(User, Student.user_id == User.id).where(
        Student.department_id == dept_id,
        Student.is_deleted.is_(False)
    )
    stud_res = await db.execute(stud_stmt)
    students_list = stud_res.all()
    
    # Map faculty to their assigned students
    faculty_data = []
    for f in faculty_list:
        assigned_students = []
        for s, u in students_list:
            if s.mentor_id == f.id:
                assigned_students.append({
                    "id": s.id,
                    "user_id": s.user_id,
                    "roll_no": s.roll_no,
                    "name": u.full_name,
                    "email": u.email,
                    "semester": s.semester,
                    "batch_year": s.batch_year
                })
        faculty_data.append({
            "id": f.id,
            "name": f.full_name,
            "email": f.email,
            "students": assigned_students
        })
        
    all_students = []
    for s, u in students_list:
        all_students.append({
            "id": s.id,
            "user_id": s.user_id,
            "roll_no": s.roll_no,
            "name": u.full_name,
            "email": u.email,
            "semester": s.semester,
            "batch_year": s.batch_year,
            "mentor_id": s.mentor_id
        })
        
    return {
        "faculty": faculty_data,
        "students": all_students
    }

@router.post("/hod/mentor/assign")
async def hod_assign_mentor(
    payload: MentorAssignmentRequest,
    current_user: User = Depends(role_required([UserRole.HOD])),
    db: AsyncSession = Depends(get_db_session)
):
    dept_id = await get_hod_department_id(current_user, db)
    if not dept_id:
        raise HTTPException(status_code=400, detail="HOD is not assigned to any department")

    # Verify the faculty belongs to the HOD's department
    fac = await db.get(User, payload.faculty_id)
    if not fac or fac.department_id != dept_id:
        raise HTTPException(status_code=400, detail="Faculty member not found or is in a different department")
        
    # Reset existing mentees of this faculty to None
    await db.execute(
        update(Student)
        .where(
            Student.mentor_id == payload.faculty_id,
            Student.department_id == dept_id
        )
        .values(mentor_id=None)
    )
    
    # Assign new mentees
    if payload.student_ids:
        await db.execute(
            update(Student)
            .where(
                Student.id.in_(payload.student_ids),
                Student.department_id == dept_id
            )
            .values(mentor_id=payload.faculty_id)
        )
        
    await db.commit()
    return {"message": "Mentor assignment updated successfully"}


# ── Faculty Mentor Student Management Endpoints ──────────────────────────────

class MentorshipStudentResponse(BaseModel):
    id: str
    roll_no: str
    name: str
    email: str
    batch: str
    semester: int

class MentorshipRecordResponse(BaseModel):
    student_id: str
    mentor_id: str
    meeting_log: str | None = ""
    academic_review: str | None = ""
    improvement_plan: str | None = ""
    remarks: str | None = ""
    follow_up: str | None = ""

class MentorshipRecordSaveRequest(BaseModel):
    meeting_log: str | None = ""
    academic_review: str | None = ""
    improvement_plan: str | None = ""
    remarks: str | None = ""
    follow_up: str | None = ""

@router.get("/mentor/students", response_model=list[MentorshipStudentResponse])
async def get_mentor_students(
    current_user: User = Depends(role_required([UserRole.FACULTY, UserRole.HOD])),
    db: AsyncSession = Depends(get_db_session)
):
    stmt = (
        select(Student, User)
        .join(User, Student.user_id == User.id)
        .where(
            Student.mentor_id == current_user.id,
            Student.is_deleted.is_(False)
        )
    )
    res = await db.execute(stmt)
    results = []
    for s, u in res.all():
        results.append(MentorshipStudentResponse(
            id=s.id,
            roll_no=s.roll_no,
            name=u.full_name,
            email=u.email,
            batch=str(s.batch_year),
            semester=s.semester
        ))
    return results

@router.get("/mentor/students/{student_id}/record", response_model=MentorshipRecordResponse)
async def get_student_mentorship_record(
    student_id: str,
    current_user: User = Depends(role_required([UserRole.FACULTY, UserRole.HOD])),
    db: AsyncSession = Depends(get_db_session)
):
    # Verify student is mentored by this faculty
    stu = await db.get(Student, student_id)
    if not stu or stu.is_deleted:
        raise HTTPException(status_code=404, detail="Student not found")
    if stu.mentor_id != current_user.id:
        raise HTTPException(status_code=403, detail="You are not assigned as the mentor for this student")

    stmt = select(MentorshipRecord).where(MentorshipRecord.student_id == student_id)
    res = await db.execute(stmt)
    rec = res.scalars().first()
    
    if not rec:
        return MentorshipRecordResponse(
            student_id=student_id,
            mentor_id=current_user.id,
            meeting_log="",
            academic_review="",
            improvement_plan="",
            remarks="",
            follow_up=""
        )
        
    return MentorshipRecordResponse(
        student_id=rec.student_id,
        mentor_id=rec.mentor_id,
        meeting_log=rec.meeting_log or "",
        academic_review=rec.academic_review or "",
        improvement_plan=rec.improvement_plan or "",
        remarks=rec.remarks or "",
        follow_up=rec.follow_up or ""
    )

@router.post("/mentor/students/{student_id}/record", response_model=MentorshipRecordResponse)
async def save_student_mentorship_record(
    student_id: str,
    payload: MentorshipRecordSaveRequest,
    current_user: User = Depends(role_required([UserRole.FACULTY, UserRole.HOD])),
    db: AsyncSession = Depends(get_db_session)
):
    # Verify student is mentored by this faculty
    stu = await db.get(Student, student_id)
    if not stu or stu.is_deleted:
        raise HTTPException(status_code=404, detail="Student not found")
    if stu.mentor_id != current_user.id:
        raise HTTPException(status_code=403, detail="You are not assigned as the mentor for this student")

    stmt = select(MentorshipRecord).where(MentorshipRecord.student_id == student_id)
    res = await db.execute(stmt)
    rec = res.scalars().first()
    
    if not rec:
        rec = MentorshipRecord(
            student_id=student_id,
            mentor_id=current_user.id,
            meeting_log=payload.meeting_log,
            academic_review=payload.academic_review,
            improvement_plan=payload.improvement_plan,
            remarks=payload.remarks,
            follow_up=payload.follow_up
        )
        db.add(rec)
    else:
        rec.meeting_log = payload.meeting_log
        rec.academic_review = payload.academic_review
        rec.improvement_plan = payload.improvement_plan
        rec.remarks = payload.remarks
        rec.follow_up = payload.follow_up
        
    await db.commit()
    await db.refresh(rec)
    
    return MentorshipRecordResponse(
        student_id=rec.student_id,
        mentor_id=rec.mentor_id,
        meeting_log=rec.meeting_log or "",
        academic_review=rec.academic_review or "",
        improvement_plan=rec.improvement_plan or "",
        remarks=rec.remarks or "",
        follow_up=rec.follow_up or ""
    )


@router.post("/hod/faculty/reject/{user_id}")
async def hod_reject_faculty(
    user_id: str,
    current_user: User = Depends(role_required([UserRole.HOD])),
    db: AsyncSession = Depends(get_db_session)
):
    profile_q = await db.execute(select(FacultyProfile).where(FacultyProfile.user_id == user_id))
    profile = profile_q.scalar_one_or_none()
    if not profile:
        raise HTTPException(status_code=404, detail="Faculty profile not found")
    profile.approval_status = "REJECTED"
    
    # Notify faculty member
    notif_service = NotificationService(db)
    await notif_service.send_notification(
        user_id=user_id,
        type_val="faculty_onboarding_rejection",
        message=f"Your onboarding request has been rejected by HOD {current_user.full_name}."
    )
    
    await db.commit()
    return {"detail": "Faculty onboarding rejected by HOD."}


@router.get("/hod/substitution/available-faculty", response_model=list[FacultyResponse])
async def get_hod_substitution_available_faculty(
    current_user: User = Depends(role_required([UserRole.HOD])),
    db: AsyncSession = Depends(get_db_session)
) -> list[FacultyResponse]:
    dept_id = await get_hod_department_id(current_user, db)
    if not dept_id:
        return []

    q = await db.execute(
        select(User).where(
            User.department_id == dept_id,
            User.role.in_([UserRole.FACULTY, UserRole.HOD]),
            User.is_active.is_(True),
            User.is_deleted.is_(False)
        )
    )
    users = q.scalars().all()
    return [
        FacultyResponse(
            id=u.id,
            full_name=u.full_name,
            email=u.email
        ) for u in users
    ]


@router.get("/hod/communication/students")
async def get_hod_communication_students(
    current_user: User = Depends(role_required([UserRole.HOD])),
    db: AsyncSession = Depends(get_db_session)
):
    """Returns all active students in the HOD department for audience selector."""
    dept_id = await get_hod_department_id(current_user, db)
    if not dept_id:
        return []

    q = await db.execute(
        select(Student, User)
        .join(User, Student.user_id == User.id)
        .where(
            Student.department_id == dept_id,
            Student.is_deleted.is_(False),
            User.is_deleted.is_(False),
            User.is_active.is_(True),
        )
        .order_by(Student.semester, User.full_name)
    )
    rows = q.all()

    return [
        {
            "id": user.id,
            "full_name": user.full_name,
            "email": user.email,
            "semester": student.semester,
        }
        for student, user in rows
    ]


# ── HOD Communication Center — Announcement Endpoints ─────────────────────────

def _parse_hod_audience(audience_type: str):
    aud = (audience_type or "").strip()
    for sem in range(1, 11):
        if aud.lower() == f"semester {sem}":
            return (["STUDENT"], sem)
    if aud in ("All BA LLB Faculty", "Faculty Only"):
        return (["FACULTY", "HOD"], None)
    if aud in ("All BA LLB Students", "Students Only", "Section A Students", "Section A"):
        return (["STUDENT"], None)
    return (["STUDENT", "FACULTY", "HOD"], None)


async def _deliver_hod_notice(db: AsyncSession, notice, dept_id: str):
    from app.db.models.communication import NoticeAcknowledgement
    from app.db.models.student import Student
    from sqlalchemy import or_, and_

    audience_roles, target_semester = _parse_hod_audience(notice.audience_type or "")
    role_enum_map = {
        "STUDENT": UserRole.STUDENT,
        "FACULTY": UserRole.FACULTY,
        "HOD": UserRole.HOD,
    }
    target_role_enums = [role_enum_map[r] for r in audience_roles if r in role_enum_map]

    users_q = await db.execute(
        select(User).outerjoin(Student, Student.user_id == User.id).where(
            User.role.in_(target_role_enums),
            User.is_active.is_(True),
            User.is_deleted.is_(False),
            or_(
                and_(User.role == UserRole.STUDENT, Student.department_id == dept_id, Student.is_deleted.is_(False)),
                and_(User.role != UserRole.STUDENT, User.department_id == dept_id)
            )
        )
    )
    candidate_users = users_q.scalars().all()

    if target_semester is not None:
        student_user_ids = [u.id for u in candidate_users if u.role == UserRole.STUDENT]
        final_user_ids = []
        if student_user_ids:
            stud_q = await db.execute(
                select(Student).where(
                    Student.user_id.in_(student_user_ids),
                    Student.semester == target_semester,
                    Student.is_deleted.is_(False),
                )
            )
            final_user_ids = [s.user_id for s in stud_q.scalars().all()]
        for u in candidate_users:
            if u.role != UserRole.STUDENT:
                final_user_ids.append(u.id)
    else:
        final_user_ids = [u.id for u in candidate_users]

    final_user_ids = list(set(final_user_ids))

    for uid in final_user_ids:
        exist_res = await db.execute(
            select(NoticeAcknowledgement).where(
                NoticeAcknowledgement.notice_id == notice.id,
                NoticeAcknowledgement.user_id == uid,
            )
        )
        if exist_res.scalar_one_or_none():
            continue
        db.add(NoticeAcknowledgement(
            notice_id=notice.id,
            user_id=uid,
            is_read=False,
            is_acknowledged=False,
            status="DELIVERED",
        ))
    await db.flush()


@router.get("/hod/communication/announcements")
async def get_hod_announcements(
    current_user: User = Depends(role_required([UserRole.HOD])),
    db: AsyncSession = Depends(get_db_session),
):
    from app.db.models.communication import Notice, NoticeAcknowledgement
    from sqlalchemy import func

    q = await db.execute(
        select(Notice)
        .where(Notice.created_by == current_user.id, Notice.is_deleted.is_(False))
        .order_by(Notice.publish_date.desc())
    )
    notices = q.scalars().all()

    results = []
    for n in notices:
        total_q = await db.execute(
            select(func.count()).where(NoticeAcknowledgement.notice_id == n.id)
        )
        total_sent = total_q.scalar() or 0
        read_q = await db.execute(
            select(func.count()).where(
                NoticeAcknowledgement.notice_id == n.id,
                NoticeAcknowledgement.is_read.is_(True),
            )
        )
        read_count = read_q.scalar() or 0

        results.append({
            "id": str(n.id),
            "title": n.title,
            "body": n.body,
            "audience_type": n.audience_type,
            "publish_date": n.publish_date.isoformat() if n.publish_date else None,
            "expiry_date": n.expiry_date.isoformat() if n.expiry_date else None,
            "category": n.category,
            "priority": n.priority,
            "status": n.status,
            "total_sent": total_sent,
            "read": read_count,
        })
    return results


@router.post("/hod/communication/announcements")
async def create_hod_announcement(
    payload: dict,
    current_user: User = Depends(role_required([UserRole.HOD])),
    db: AsyncSession = Depends(get_db_session),
):
    from app.db.models.communication import Notice
    from datetime import date as date_type

    title = (payload.get("title") or "").strip()
    body = (payload.get("body") or "").strip()
    if not title or not body:
        raise HTTPException(status_code=400, detail="Title and body are required.")

    audience_type = payload.get("audience_type", "All BA LLB Students")

    pub_date_raw = payload.get("publish_date")
    try:
        pub_date = date_type.fromisoformat(pub_date_raw) if pub_date_raw else date_type.today()
    except (ValueError, TypeError):
        pub_date = date_type.today()

    exp_date = None
    exp_date_raw = payload.get("expiry_date")
    if exp_date_raw:
        try:
            exp_date = date_type.fromisoformat(exp_date_raw)
        except (ValueError, TypeError):
            exp_date = None

    dept_id = await get_hod_department_id(current_user, db)
    if not dept_id:
        raise HTTPException(status_code=403, detail="HOD department not found.")

    notice = Notice(
        created_by=current_user.id,
        title=title,
        body=body,
        audience_type=audience_type,
        publish_date=pub_date,
        expiry_date=exp_date,
        category=payload.get("category", "General Information"),
        priority=payload.get("priority", "Medium"),
        status="Active",
        publisher_role="HOD",
        department_id=dept_id,
        attachment_url=payload.get("attachment_url") or None,
    )
    db.add(notice)
    await db.flush()

    await _deliver_hod_notice(db, notice, dept_id)

    await db.commit()
    await db.refresh(notice)
    return {
        "id": str(notice.id),
        "title": notice.title,
        "body": notice.body,
        "audience_type": notice.audience_type,
        "publish_date": notice.publish_date.isoformat() if notice.publish_date else None,
        "expiry_date": notice.expiry_date.isoformat() if notice.expiry_date else None,
        "category": notice.category,
        "priority": notice.priority,
        "status": notice.status,
    }


@router.put("/hod/communication/announcements/{announcement_id}")
async def update_hod_announcement(
    announcement_id: str,
    payload: dict,
    current_user: User = Depends(role_required([UserRole.HOD])),
    db: AsyncSession = Depends(get_db_session),
):
    from app.db.models.communication import Notice
    from datetime import date as date_type

    res = await db.execute(
        select(Notice).where(
            Notice.id == announcement_id,
            Notice.created_by == current_user.id,
            Notice.is_deleted.is_(False),
        )
    )
    notice = res.scalar_one_or_none()
    if not notice:
        raise HTTPException(status_code=404, detail="Announcement not found.")

    if payload.get("title"):
        notice.title = payload["title"].strip()
    if payload.get("body"):
        notice.body = payload["body"].strip()
    if "audience_type" in payload:
        notice.audience_type = payload["audience_type"]
    if "category" in payload:
        notice.category = payload["category"]
    if "priority" in payload:
        notice.priority = payload["priority"]
    if payload.get("publish_date"):
        try:
            notice.publish_date = date_type.fromisoformat(payload["publish_date"])
        except (ValueError, TypeError):
            pass
    if "expiry_date" in payload:
        try:
            notice.expiry_date = date_type.fromisoformat(payload["expiry_date"]) if payload["expiry_date"] else None
        except (ValueError, TypeError):
            notice.expiry_date = None
    if "attachment_url" in payload:
        notice.attachment_url = payload["attachment_url"] or None

    await db.commit()
    await db.refresh(notice)
    return {
        "id": str(notice.id),
        "title": notice.title,
        "body": notice.body,
        "audience_type": notice.audience_type,
        "publish_date": notice.publish_date.isoformat() if notice.publish_date else None,
        "expiry_date": notice.expiry_date.isoformat() if notice.expiry_date else None,
        "category": notice.category,
        "priority": notice.priority,
        "status": notice.status,
    }


@router.delete("/hod/communication/announcements/{announcement_id}")
async def delete_hod_announcement(
    announcement_id: str,
    current_user: User = Depends(role_required([UserRole.HOD])),
    db: AsyncSession = Depends(get_db_session),
):
    from app.db.models.communication import Notice

    res = await db.execute(
        select(Notice).where(
            Notice.id == announcement_id,
            Notice.created_by == current_user.id,
            Notice.is_deleted.is_(False),
        )
    )
    notice = res.scalar_one_or_none()
    if not notice:
        raise HTTPException(status_code=404, detail="Announcement not found.")

    notice.is_deleted = True
    notice.status = "Cancelled"
    await db.commit()
    return {"status": "cancelled", "id": announcement_id}


@router.post("/hod/communication/announcements/upload-image")
async def upload_hod_announcement_image(
    file: UploadFile = File(...),
    current_user: User = Depends(role_required([UserRole.HOD])),
):
    """Upload an optional image/attachment for a HOD announcement.
    Returns the public URL to be stored with the announcement.
    """
    if not file or not file.filename:
        raise HTTPException(status_code=400, detail="No file provided.")

    ext = os.path.splitext(file.filename)[1].lower()
    if ext not in [".jpg", ".jpeg", ".png", ".gif", ".webp", ".pdf"]:
        raise HTTPException(
            status_code=400,
            detail="Unsupported file type. Allowed: JPG, PNG, GIF, WEBP, PDF."
        )

    upload_dir = os.path.join("app", "static", "uploads", "announcements")
    os.makedirs(upload_dir, exist_ok=True)

    filename = f"{uuid.uuid4()}{ext}"
    filepath = os.path.join(upload_dir, filename)

    content_bytes = await file.read()
    with open(filepath, "wb") as buf:
        buf.write(content_bytes)

    url = f"/static/uploads/announcements/{filename}"
    return {"url": url, "filename": filename}
