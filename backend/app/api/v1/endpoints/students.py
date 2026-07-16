from datetime import date
from fastapi import APIRouter, Depends, HTTPException, status, UploadFile, File
from pydantic import BaseModel
from sqlalchemy.ext.asyncio import AsyncSession
from sqlalchemy import select

from app.core.dependencies import get_current_user, get_db_session, role_required
from app.db.models.user import User, UserRole
from app.db.models.student import Student, MentorshipRecord
from app.db.models.academic import Course, Section, Timetable, TimetableApproval, ApprovalStatus
from app.schemas.dashboard import DashboardResponse, MetricSchema
from app.schemas.student import (
    StudentProfileResponse, StudentProfileUpdateRequest, AttendanceSummaryResponse, AttendanceRecordSchema,
    MarkRecordSchema, FeeRecordSchema, FeePaymentRequest, LeaveApplicationRequest,
    LeaveRequestResponse, GrievanceRaiseRequest, GrievanceResponse, TimetableItemResponse,
    NoticeResponse, StudyMaterialResponse, NotificationResponse, StudentFeeSummaryResponse
)
from app.schemas.admin import CourseResponse
from app.services.attendance_service import AttendanceService
from app.services.fee_service import FeeService
from app.services.academic_service import AcademicService
from app.services.notification_service import NotificationService
from app.db.repositories.student_repository import StudentRepository

router = APIRouter()

class StudentMentorshipRecordResponse(BaseModel):
    mentor_name: str | None = None
    mentor_email: str | None = None
    meeting_log: str | None = ""
    academic_review: str | None = ""
    improvement_plan: str | None = ""
    remarks: str | None = ""
    follow_up: str | None = ""

@router.get("/mentorship-record", response_model=StudentMentorshipRecordResponse)
async def get_my_mentorship_record(
    current_user: User = Depends(role_required([UserRole.STUDENT])),
    db: AsyncSession = Depends(get_db_session)
) -> StudentMentorshipRecordResponse:
    student_q = await db.execute(select(Student).where(Student.user_id == current_user.id))
    student = student_q.scalar_one_or_none()
    if not student:
        raise HTTPException(status_code=404, detail="Student profile not found")
        
    mentor_name = None
    mentor_email = None
    if student.mentor_id:
        mentor_q = await db.execute(select(User).where(User.id == student.mentor_id))
        mentor = mentor_q.scalar_one_or_none()
        if mentor:
            mentor_name = mentor.full_name
            mentor_email = mentor.email
            
    rec_q = await db.execute(select(MentorshipRecord).where(MentorshipRecord.student_id == student.id))
    rec = rec_q.scalar_one_or_none()
    
    if not rec:
        return StudentMentorshipRecordResponse(
            mentor_name=mentor_name,
            mentor_email=mentor_email,
            meeting_log="",
            academic_review="",
            improvement_plan="",
            remarks="",
            follow_up=""
        )
        
    return StudentMentorshipRecordResponse(
        mentor_name=mentor_name,
        mentor_email=mentor_email,
        meeting_log=rec.meeting_log or "",
        academic_review=rec.academic_review or "",
        improvement_plan=rec.improvement_plan or "",
        remarks=rec.remarks or "",
        follow_up=rec.follow_up or ""
    )

@router.get("/dashboard", response_model=DashboardResponse)
async def student_dashboard(
    current_user: User = Depends(role_required([UserRole.STUDENT])),
    db: AsyncSession = Depends(get_db_session)
) -> DashboardResponse:
    # Resolve student profile
    student_repo = StudentRepository(db)
    student = await student_repo.get_student_by_user_id(current_user.id)
    if not student:
        raise HTTPException(status_code=404, detail="Student profile not found")

    # Compute dynamic stats
    att_service = AttendanceService(db)
    fee_service = FeeService(db)
    
    att_summary = await att_service.get_student_attendance_summary(student.id)
    fee_summary = await fee_service.get_student_fee_summary(student.id)
    
    return DashboardResponse(
        metrics=[
            MetricSchema(id="attendance", label="Attendance %", value=f"{att_summary['percentage']}%"),
            MetricSchema(id="fees", label="Due Fees", value=f"₹{fee_summary['due_amount']:,}"),
            MetricSchema(id="exam", label="Upcoming Exams", value="1"),
            MetricSchema(id="cgpa", label="CGPA", value=f"{student.cgpa:.2f}" if student.cgpa is not None else "0.00"),
        ]
    )


@router.get("/semester-timeline")
async def get_semester_timeline(
    current_user: User = Depends(role_required([UserRole.STUDENT])),
    db: AsyncSession = Depends(get_db_session)
) -> dict:
    """
    Returns the full semester timeline model for the current student.
    Semester = 6 months with:
      - Regular class weeks
      - Internal Assessment (IA) weeks ×2
      - Events / holidays
      - Study leave
      - End-semester exams
    Used by the AI Attendance Impact calculator to project realistic percentages.
    """
    from datetime import date, timedelta
    import math

    student_repo = StudentRepository(db)
    student = await student_repo.get_student_by_user_id(current_user.id)
    if not student:
        raise HTTPException(status_code=404, detail="Student profile not found")

    # ── Semester boundary calculation ──────────────────────────────────────────
    # Determine semester start based on batch_year and current semester number.
    # Odd semesters (1,3,5...) start in June/July (monsoon term)
    # Even semesters (2,4,6...) start in December/January (winter term)
    today = date.today()
    sem = student.semester or 1
    batch_year = student.batch_year or (today.year - 1)

    # Figure out which calendar year this semester belongs to
    sem_pair = (sem + 1) // 2  # year of study: sem 1&2 → year 1, sem 3&4 → year 2 …
    acad_year_start = batch_year + (sem_pair - 1)  # e.g., batch 2023 + 0 for year 1

    if sem % 2 == 1:
        # Odd semester: June of that year
        sem_start = date(acad_year_start, 6, 16)
        sem_end   = date(acad_year_start, 12, 15)
    else:
        # Even semester: January of following year
        sem_start = date(acad_year_start + 1, 1, 6)
        sem_end   = date(acad_year_start + 1, 6, 15)

    # Clamp to reasonable bounds (avoid future start for new batches)
    if sem_start > today:
        sem_start = today.replace(day=1)
    if sem_end < sem_start:
        sem_end = date(sem_start.year + 1, sem_start.month, sem_start.day)

    total_calendar_days = (sem_end - sem_start).days + 1
    total_weeks = max(1, total_calendar_days // 7)

    # ── Academic event schedule ─────────────────────────────────────────────────
    # Standard 6-month semester structure:
    #   Week 1-2:  Orientation / Registration (no regular classes)
    #   Week 3-8:  Regular classes (Unit I)
    #   Week 9:    Internal Assessment 1 (IA1) — exams only, 3 class periods
    #   Week 10-15: Regular classes (Unit II)
    #   Week 16:   Internal Assessment 2 (IA2) — exams only, 3 class periods
    #   Week 17-20: Regular classes (Unit III + revision)
    #   Week 21:   Study Leave
    #   Week 22-26: End Semester Examinations (no regular class attendance)

    PERIODS_PER_DAY_REGULAR = 6
    PERIODS_PER_DAY_IA      = 3   # IA weeks: 3 exam periods + 3 class periods
    WORKING_DAYS_PER_WEEK   = 5   # Mon–Fri

    # Orientation weeks: 2
    orientation_weeks  = min(2, total_weeks)
    # IA weeks: 2 (one after ~8 weeks, one after ~15 weeks)
    ia_weeks           = 2 if total_weeks >= 20 else 1
    # Study leave: 1 week
    study_leave_weeks  = 1 if total_weeks >= 22 else 0
    # End-sem exam weeks: 3 (no attendance counted)
    end_sem_weeks      = min(3, max(0, total_weeks - 20))
    # Regular class weeks
    regular_weeks      = max(0, total_weeks - orientation_weeks - ia_weeks - study_leave_weeks - end_sem_weeks)

    # ── Expected class periods ──────────────────────────────────────────────────
    regular_periods   = regular_weeks * WORKING_DAYS_PER_WEEK * PERIODS_PER_DAY_REGULAR
    ia_periods        = ia_weeks * WORKING_DAYS_PER_WEEK * PERIODS_PER_DAY_IA
    total_expected_periods = regular_periods + ia_periods

    # ── Elapsed periods up to today ─────────────────────────────────────────────
    elapsed_calendar_days = max(0, (min(today, sem_end) - sem_start).days + 1)
    
    # Calculate elapsed working days (Mon-Sat)
    elapsed_working_days = 0
    from datetime import timedelta
    cur_d = sem_start
    while cur_d <= min(today, sem_end):
        if cur_d.weekday() != 6:  # Skip Sunday
            elapsed_working_days += 1
        cur_d += timedelta(days=1)
        
    elapsed_periods = elapsed_working_days * PERIODS_PER_DAY_REGULAR

    elapsed_periods = min(elapsed_periods, total_expected_periods)
    remaining_periods = total_expected_periods - elapsed_periods

    # ── IA event dates ──────────────────────────────────────────────────────────
    ia1_start = sem_start + timedelta(weeks=orientation_weeks + int(regular_weeks * 0.45))
    ia1_end   = ia1_start + timedelta(days=4)
    ia2_start = sem_start + timedelta(weeks=orientation_weeks + int(regular_weeks * 0.85) + ia_weeks // 2)
    ia2_end   = ia2_start + timedelta(days=4)
    study_start = sem_end - timedelta(weeks=end_sem_weeks + study_leave_weeks)
    end_sem_start = sem_end - timedelta(weeks=end_sem_weeks)

    return {
        "semester_number": sem,
        "semester_start": sem_start.isoformat(),
        "semester_end": sem_end.isoformat(),
        "today": today.isoformat(),
        "total_weeks": total_weeks,
        "structure": {
            "orientation_weeks": orientation_weeks,
            "regular_class_weeks": regular_weeks,
            "ia_weeks": ia_weeks,
            "study_leave_weeks": study_leave_weeks,
            "end_sem_exam_weeks": end_sem_weeks,
        },
        "periods": {
            "total_expected": total_expected_periods,
            "elapsed_so_far": elapsed_periods,
            "remaining": remaining_periods,
            "per_day_regular": PERIODS_PER_DAY_REGULAR,
            "per_day_ia": PERIODS_PER_DAY_IA,
            "working_days_per_week": WORKING_DAYS_PER_WEEK,
        },
        "events": {
            "ia1_start": ia1_start.isoformat(),
            "ia1_end": ia1_end.isoformat(),
            "ia2_start": ia2_start.isoformat(),
            "ia2_end": ia2_end.isoformat(),
            "study_leave_start": study_start.isoformat(),
            "end_sem_start": end_sem_start.isoformat(),
        },
        "progress_pct": round((elapsed_periods / max(1, total_expected_periods)) * 100, 1),
    }

@router.get("/profile", response_model=StudentProfileResponse)
async def student_profile(
    current_user: User = Depends(role_required([UserRole.STUDENT])),
    db: AsyncSession = Depends(get_db_session)
) -> StudentProfileResponse:
    student_repo = StudentRepository(db)
    student = await student_repo.get_student_by_user_id(current_user.id)
    if not student:
        raise HTTPException(status_code=404, detail="Student profile not found")
        
    mentor_name = None
    mentor_email = None
    mentor_phone = None
    if student.mentor_id:
        mentor_q = await db.execute(select(User).where(User.id == student.mentor_id))
        mentor = mentor_q.scalar_one_or_none()
        if mentor:
            mentor_name = mentor.full_name
            mentor_email = mentor.email
            mentor_phone = mentor.phone
    
    course_name = None
    section_name = None
    class_advisor_name = None
    class_advisor_email = None
    class_advisor_phone = None
    department_name = None
    
    from app.db.models.academic import Degree, Department
    if student.degree_id:
        deg_q = await db.execute(select(Degree).where(Degree.id == student.degree_id))
        deg = deg_q.scalar_one_or_none()
        if deg:
            course_name = deg.name
            
    if student.department_id:
        dept_q = await db.execute(select(Department).where(Department.id == student.department_id))
        dept = dept_q.scalar_one_or_none()
        if dept:
            department_name = dept.name
            if not course_name:
                course_name = dept.name
            
    if student.section_id:
        sec_q = await db.execute(select(Section).where(Section.id == student.section_id))
        sec = sec_q.scalar_one_or_none()
        if sec:
            section_name = sec.section_name
            if sec.faculty_id:
                adv_q = await db.execute(select(User).where(User.id == sec.faculty_id))
                adv = adv_q.scalar_one_or_none()
                if adv:
                    class_advisor_name = adv.full_name
                    class_advisor_email = adv.email
                    class_advisor_phone = adv.phone

    batch = f"{student.batch_year}-{student.batch_year + 5}" if student.batch_year else "2021-2026"
    year_map = {1: "1st", 2: "1st", 3: "2nd", 4: "2nd", 5: "3rd", 6: "3rd", 7: "4th", 8: "4th", 9: "5th", 10: "5th"}
    year_of_study = f"{year_map.get(student.semester, '3rd')} Year"

    return StudentProfileResponse(
        id=student.id,
        roll_no=student.roll_no,
        semester=student.semester,
        batch_year=student.batch_year,
        email=current_user.email,
        full_name=student.full_name or current_user.full_name,
        mentor_name=mentor_name,
        mentor_email=mentor_email,
        mentor_phone=mentor_phone,
        cgpa=student.cgpa,
        skills=student.skills,
        course_name=course_name,
        section=section_name,
        class_advisor_name=class_advisor_name,
        class_advisor_email=class_advisor_email,
        class_advisor_phone=class_advisor_phone,
        batch=batch,
        year_of_study=year_of_study,
        department_name=department_name,
        # Extended personal information
        date_of_birth=student.date_of_birth,
        gender=student.gender,
        blood_group=student.blood_group,
        nationality=student.nationality,
        mobile_number=student.mobile_number,
        current_address=student.current_address,
        permanent_address=student.permanent_address,
        aadhaar_number=student.aadhaar_number,
        passport_number=student.passport_number,
        community_category=student.community_category,
        religion=student.religion,
        emergency_contact_name=student.emergency_contact_name,
        emergency_contact_relationship=student.emergency_contact_relationship,
        emergency_contact_number=student.emergency_contact_number,
        father_name=student.father_name,
        father_occupation=student.father_occupation,
        father_mobile=student.father_mobile,
        father_email=student.father_email,
        father_office_address=student.father_office_address,
        mother_name=student.mother_name,
        mother_occupation=student.mother_occupation,
        mother_mobile=student.mother_mobile,
        mother_email=student.mother_email,
        mother_office_address=student.mother_office_address,
        parent_annual_income=student.parent_annual_income,
        languages_known=student.languages_known,
        hobbies_interests=student.hobbies_interests,
        special_skills=student.special_skills,
        medical_info=student.medical_info,
        certifications=student.certifications,
        internships=student.internships,
        sports_records=student.sports_records,
        moot_courts=student.moot_courts,
        profile_photo_url=student.profile_photo_url,
        verification_status=student.verification_status,
        staff_remarks=student.staff_remarks,
        hod_remarks=student.hod_remarks,
        document_aadhaar_url=student.document_aadhaar_url,
        document_community_url=student.document_community_url,
        document_tc_url=student.document_tc_url,
        document_other_url=student.document_other_url,
        edit_request_status=student.edit_request_status,
        edit_request_reason=student.edit_request_reason
    )

@router.put("/profile", response_model=StudentProfileResponse)
async def update_student_profile(
    payload: StudentProfileUpdateRequest,
    current_user: User = Depends(role_required([UserRole.STUDENT])),
    db: AsyncSession = Depends(get_db_session)
) -> StudentProfileResponse:
    student_repo = StudentRepository(db)
    student = await student_repo.get_student_by_user_id(current_user.id)
    if not student:
        raise HTTPException(status_code=404, detail="Student profile not found")
        
    if student.verification_status in ["SUBMITTED", "UNDER_STAFF", "UNDER_HOD", "VERIFIED_LOCKED"]:
        raise HTTPException(
            status_code=400,
            detail="Your profile is undergoing verification or is locked and cannot be updated. Please request an edit to make corrections."
        )

    # Update Student fields if provided in request
    for field, value in payload.model_dump(exclude_unset=True).items():
        setattr(student, field, value)
        
    # Also update User's full_name if it was updated in the request
    if payload.full_name is not None:
        current_user.full_name = payload.full_name
        
    await db.commit()
    
    mentor_name = None
    mentor_email = None
    mentor_phone = None
    if student.mentor_id:
        mentor_q = await db.execute(select(User).where(User.id == student.mentor_id))
        mentor = mentor_q.scalar_one_or_none()
        if mentor:
            mentor_name = mentor.full_name
            mentor_email = mentor.email
            mentor_phone = mentor.phone
            
    return StudentProfileResponse(
        id=student.id,
        roll_no=student.roll_no,
        semester=student.semester,
        batch_year=student.batch_year,
        email=current_user.email,
        full_name=student.full_name or current_user.full_name,
        mentor_name=mentor_name,
        mentor_email=mentor_email,
        mentor_phone=mentor_phone,
        cgpa=student.cgpa,
        skills=student.skills,
        date_of_birth=student.date_of_birth,
        gender=student.gender,
        blood_group=student.blood_group,
        nationality=student.nationality,
        mobile_number=student.mobile_number,
        current_address=student.current_address,
        permanent_address=student.permanent_address,
        aadhaar_number=student.aadhaar_number,
        passport_number=student.passport_number,
        community_category=student.community_category,
        religion=student.religion,
        emergency_contact_name=student.emergency_contact_name,
        emergency_contact_relationship=student.emergency_contact_relationship,
        emergency_contact_number=student.emergency_contact_number,
        father_name=student.father_name,
        father_occupation=student.father_occupation,
        father_mobile=student.father_mobile,
        father_email=student.father_email,
        father_office_address=student.father_office_address,
        mother_name=student.mother_name,
        mother_occupation=student.mother_occupation,
        mother_mobile=student.mother_mobile,
        mother_email=student.mother_email,
        mother_office_address=student.mother_office_address,
        parent_annual_income=student.parent_annual_income,
        languages_known=student.languages_known,
        hobbies_interests=student.hobbies_interests,
        special_skills=student.special_skills,
        medical_info=student.medical_info,
        certifications=student.certifications,
        internships=student.internships,
        sports_records=student.sports_records,
        moot_courts=student.moot_courts,
        profile_photo_url=student.profile_photo_url,
        verification_status=student.verification_status,
        staff_remarks=student.staff_remarks,
        hod_remarks=student.hod_remarks,
        document_aadhaar_url=student.document_aadhaar_url,
        document_community_url=student.document_community_url,
        document_tc_url=student.document_tc_url,
        document_other_url=student.document_other_url,
        edit_request_status=student.edit_request_status,
        edit_request_reason=student.edit_request_reason
    )

@router.post("/profile/photo")
async def upload_profile_photo(
    file: UploadFile = File(...),
    current_user: User = Depends(role_required([UserRole.STUDENT])),
    db: AsyncSession = Depends(get_db_session)
):
    import os
    import uuid
    
    student_repo = StudentRepository(db)
    student = await student_repo.get_student_by_user_id(current_user.id)
    if not student:
        raise HTTPException(status_code=404, detail="Student profile not found")
        
    static_dir = os.path.join(os.path.dirname(os.path.dirname(os.path.dirname(os.path.dirname(os.path.abspath(__file__))))), "static")
    uploads_dir = os.path.join(static_dir, "uploads")
    os.makedirs(uploads_dir, exist_ok=True)
    
    ext = os.path.splitext(file.filename)[1] if file.filename else ".jpg"
    safe_filename = f"avatar_{student.id}_{uuid.uuid4().hex}{ext}"
    file_path = os.path.join(uploads_dir, safe_filename)
    
    with open(file_path, "wb") as f:
        f.write(await file.read())
        
    photo_url = f"/mock-uploads/{safe_filename}"
    student.profile_photo_url = photo_url
    await db.commit()
    
    return {"status": "success", "profile_photo_url": photo_url}

@router.post("/leaves/upload")
async def upload_leave_document(
    file: UploadFile = File(...),
    current_user: User = Depends(role_required([UserRole.STUDENT])),
    db: AsyncSession = Depends(get_db_session)
):
    import os
    import uuid
    
    student_repo = StudentRepository(db)
    student = await student_repo.get_student_by_user_id(current_user.id)
    if not student:
        raise HTTPException(status_code=404, detail="Student profile not found")
        
    static_dir = os.path.join(os.path.dirname(os.path.dirname(os.path.dirname(os.path.dirname(os.path.abspath(__file__))))), "static")
    uploads_dir = os.path.join(static_dir, "uploads", "leaves")
    os.makedirs(uploads_dir, exist_ok=True)
    
    ext = os.path.splitext(file.filename)[1] if file.filename else ".pdf"
    if ext.lower() not in [".pdf", ".jpg", ".jpeg", ".png"]:
        raise HTTPException(status_code=400, detail="Only PDF, JPG, and PNG are allowed.")
        
    safe_filename = f"leave_{student.id}_{uuid.uuid4().hex}{ext}"
    file_path = os.path.join(uploads_dir, safe_filename)
    
    with open(file_path, "wb") as f:
        f.write(await file.read())
        
    file_url = f"/mock-uploads/{safe_filename}"
    return {"status": "success", "file_url": file_url}

@router.get("/attendance", response_model=AttendanceSummaryResponse)
async def get_attendance(
    current_user: User = Depends(role_required([UserRole.STUDENT])),
    db: AsyncSession = Depends(get_db_session)
) -> AttendanceSummaryResponse:
    student_repo = StudentRepository(db)
    student = await student_repo.get_student_by_user_id(current_user.id)
    if not student:
        raise HTTPException(status_code=404, detail="Student profile not found")
        
    att_service = AttendanceService(db)
    summary = await att_service.get_student_attendance_summary(student.id)
    
    records = [
        AttendanceRecordSchema(
            id=r["id"],
            date=r["date"],
            status=r["status"],
            subject_name=r["subject_name"],
            subject_code=r["subject_code"],
            section_name=r["section_name"]
        )
        for r in summary.get("records", [])
    ]
    
    return AttendanceSummaryResponse(
        percentage=summary["percentage"],
        total=summary["total"],
        present=summary["present"],
        absent=summary["absent"],
        od=summary["od"],
        records=records
    )

@router.get("/marks", response_model=list[MarkRecordSchema])
async def get_marks(
    current_user: User = Depends(role_required([UserRole.STUDENT])),
    db: AsyncSession = Depends(get_db_session)
) -> list[MarkRecordSchema]:
    student_repo = StudentRepository(db)
    student = await student_repo.get_student_by_user_id(current_user.id)
    if not student:
        raise HTTPException(status_code=404, detail="Student profile not found")
        
    acad_service = AcademicService(db)
    marks = await acad_service.acad_repo.get_marks_by_student(student.id)
    
    return [
        MarkRecordSchema(id=m.id, exam_type=m.exam_type, mark=m.mark, max_mark=m.max_mark)
        for m in marks
    ]

@router.get("/fees", response_model=StudentFeeSummaryResponse)
async def get_fees(
    current_user: User = Depends(role_required([UserRole.STUDENT])),
    db: AsyncSession = Depends(get_db_session)
) -> StudentFeeSummaryResponse:
    student_repo = StudentRepository(db)
    student = await student_repo.get_student_by_user_id(current_user.id)
    if not student:
        raise HTTPException(status_code=404, detail="Student profile not found")
        
    fee_service = FeeService(db)
    summary = await fee_service.get_student_fee_summary(student.id)
    return summary

@router.post("/fees/pay/{record_id}")
async def pay_fee(
    record_id: str,
    payload: FeePaymentRequest,
    current_user: User = Depends(role_required([UserRole.STUDENT])),
    db: AsyncSession = Depends(get_db_session)
) -> dict:
    fee_service = FeeService(db)
    payment = await fee_service.pay_fee(
        record_id=record_id,
        amount=payload.amount,
        mode=payload.mode,
        txn_id=payload.txn_id
    )
    await db.commit()
    return {"detail": "Payment successful", "payment_id": payment.id, "receipt_url": payment.receipt_url}

@router.get("/timetable", response_model=list[TimetableItemResponse])
async def get_timetable(
    current_user: User = Depends(role_required([UserRole.STUDENT])),
    db: AsyncSession = Depends(get_db_session)
) -> list[TimetableItemResponse]:
    student_repo = StudentRepository(db)
    student = await student_repo.get_student_by_user_id(current_user.id)
    if not student:
        raise HTTPException(status_code=404, detail="Student profile not found")
        
    section_name = "A"
    if student.section_id:
        sec_q = await db.execute(select(Section).where(Section.id == student.section_id))
        sec = sec_q.scalar_one_or_none()
        if sec:
            section_name = sec.section_name
    else:
        sec_q = await db.execute(
            select(Section.section_name)
            .join(Course, Section.course_id == Course.id)
            .where(
                Course.degree_id == student.degree_id if student.degree_id else Course.dept_id == student.department_id,
                Course.semester == student.semester,
                Section.is_deleted.is_(False)
            )
            .distinct()
        )
        first_sec_name = sec_q.scalar()
        if first_sec_name:
            section_name = first_sec_name

    timetable_stmt = (
        select(Timetable)
        .join(Section, Timetable.section_id == Section.id)
        .join(Course, Timetable.subject_id == Course.id)
        .where(
            Course.semester == student.semester,
            Section.section_name == section_name,
            Timetable.is_deleted.is_(False),
            Section.is_deleted.is_(False),
            Course.is_deleted.is_(False)
        )
    )
    if student.degree_id:
        timetable_stmt = timetable_stmt.where(Course.degree_id == student.degree_id)
    else:
        timetable_stmt = timetable_stmt.where(Course.dept_id == student.department_id)

    result = await db.execute(timetable_stmt)
    items = result.scalars().all()

    response_items = []
    for item in items:
        app_q = await db.execute(
            select(TimetableApproval).where(
                TimetableApproval.timetable_id == item.id,
                TimetableApproval.status == ApprovalStatus.APPROVED
            )
        )
        app = app_q.scalar_one_or_none()
        if not app:
            continue
            
        course_q = await db.execute(select(Course).where(Course.id == item.subject_id))
        course = course_q.scalar_one_or_none()
        
        fac_q = await db.execute(select(User).where(User.id == item.faculty_id))
        faculty = fac_q.scalar_one_or_none()
        
        response_items.append(
            TimetableItemResponse(
                id=item.id,
                subject_code=course.code if course else "LAW101",
                subject_name=course.name if course else "Constitutional Law",
                faculty_name=faculty.full_name if faculty else "Faculty",
                room=item.room,
                weekday=item.weekday.value,
                start_time=item.start_time.strftime("%H:%M"),
                end_time=item.end_time.strftime("%H:%M")
            )
        )
    return response_items

@router.get("/courses", response_model=list[CourseResponse])
async def get_student_courses(
    current_user: User = Depends(role_required([UserRole.STUDENT])),
    db: AsyncSession = Depends(get_db_session)
) -> list[CourseResponse]:
    student_repo = StudentRepository(db)
    student = await student_repo.get_student_by_user_id(current_user.id)
    if not student:
        raise HTTPException(status_code=404, detail="Student profile not found")
        
    stmt = select(Course).where(Course.is_deleted.is_(False), Course.semester == student.semester)
    if student.degree_id:
        stmt = stmt.where(Course.degree_id == student.degree_id)
    else:
        stmt = stmt.where(Course.dept_id == student.department_id)
        
    result = await db.execute(stmt.order_by(Course.code))
    courses = result.scalars().all()
    
    # Deduplicate by course code to prevent repeated subjects on the student dashboard
    seen_codes: set = set()
    unique_courses = []
    for c in courses:
        key = c.code.strip().lower() if c.code else str(c.id)
        if key not in seen_codes:
            seen_codes.add(key)
            unique_courses.append(c)
    
    return [
        CourseResponse(
            id=c.id,
            code=c.code,
            name=c.name,
            credits=c.credits,
            semester=c.semester,
            degree_id=c.degree_id,
            dept_id=c.dept_id
        )
        for c in unique_courses
    ]

@router.get("/leaves", response_model=list[LeaveRequestResponse])
async def get_leaves(
    current_user: User = Depends(role_required([UserRole.STUDENT])),
    db: AsyncSession = Depends(get_db_session)
) -> list[LeaveRequestResponse]:
    from app.db.models.leave import LeaveRequest
    from sqlalchemy import select as sa_select
    q = await db.execute(
        sa_select(LeaveRequest)
        .where(LeaveRequest.user_id == current_user.id, LeaveRequest.is_deleted.is_(False))
        .order_by(LeaveRequest.created_at.desc())
    )
    records = q.scalars().all()

    result = []
    for r in records:
        # Resolve HOD name if hod_action_by is set
        hod_name = None
        if r.hod_action_by:
            hod_q = await db.execute(sa_select(User).where(User.id == r.hod_action_by))
            hod_user = hod_q.scalar_one_or_none()
            if hod_user:
                hod_name = hod_user.full_name

        # Determine app_category from type field
        app_category = "Leave"
        if hasattr(r, "app_category") and r.app_category:
            app_category = r.app_category
        elif r.type and r.type.lower() in ("od", "on-duty", "on duty"):
            app_category = "OD"

        # Build rich remarks string for student display
        remarks = None
        # Check advisor approval
        from app.db.models.leave import LeaveApproval
        app_stmt = sa_select(LeaveApproval).where(LeaveApproval.leave_id == r.id).order_by(LeaveApproval.created_at.desc())
        app_res = await db.execute(app_stmt)
        approval = app_res.scalars().first()
        if approval:
            approver_stmt = sa_select(User.full_name).where(User.id == approval.approved_by)
            approver_res = await db.execute(approver_stmt)
            advisor_name = approver_res.scalar_one_or_none()
            if approval.remarks:
                remarks = f"Advisor ({advisor_name or 'Faculty'}): {approval.remarks}"

        if r.hod_remarks:
            hod_note = f"HOD ({hod_name or 'HOD'}): {r.hod_remarks}"
            remarks = f"{remarks}\n{hod_note}" if remarks else hod_note

        if r.principal_remarks:
            principal_note = f"Principal: {r.principal_remarks}"
            remarks = f"{remarks}\n{principal_note}" if remarks else principal_note

        result.append(LeaveRequestResponse(
            id=r.id,
            type=r.type,
            app_category=app_category,
            from_date=r.from_date,
            to_date=r.to_date,
            reason=r.reason,
            status=r.status,
            num_days=r.num_days,
            attachment_url=r.attachment_url,
            hod_remarks=r.hod_remarks,
            principal_remarks=r.principal_remarks,
            remarks=remarks,
        ))
    return result


@router.post("/leaves/apply", response_model=LeaveRequestResponse)
async def apply_leave(
    payload: LeaveApplicationRequest,
    current_user: User = Depends(role_required([UserRole.STUDENT])),
    db: AsyncSession = Depends(get_db_session)
) -> LeaveRequestResponse:
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
    
    # Notify class advisor, HOD, and Parent
    from app.services.notification_service import NotificationService
    notif_service = NotificationService(db)
    
    student_repo = StudentRepository(db)
    student = await student_repo.get_student_by_user_id(current_user.id)
    if student:
        if student.section_id:
            from app.db.models.academic import Section
            sec_q = await db.execute(select(Section).where(Section.id == student.section_id))
            sec = sec_q.scalar_one_or_none()
            if sec and sec.faculty_id:
                await notif_service.send_notification(
                    user_id=sec.faculty_id,
                    type_val="leave_request",
                    message=f"Student {current_user.full_name} has applied for leave ({req.type}) from {req.from_date} to {req.to_date}."
                )
        if student.department_id:
            from app.db.models.academic import Department
            dept_q = await db.execute(select(Department).where(Department.id == student.department_id))
            dept = dept_q.scalars().first()
            if dept and dept.hod_id:
                await notif_service.send_notification(
                    user_id=dept.hod_id,
                    type_val="leave_request",
                    message=f"Student {current_user.full_name} has applied for leave ({req.type}) from {req.from_date} to {req.to_date}."
                )
        from app.db.models.student import ParentStudentMap
        pm_q = await db.execute(select(ParentStudentMap).where(ParentStudentMap.student_id == student.id, ParentStudentMap.is_deleted.is_(False)))
        for pm in pm_q.scalars().all():
            await notif_service.send_notification(
                user_id=pm.parent_id,
                type_val="leave_request",
                message=f"Your child {current_user.full_name} has applied for leave ({req.type}) from {req.from_date} to {req.to_date}."
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

@router.get("/grievances", response_model=list[GrievanceResponse])
async def get_grievances(
    current_user: User = Depends(role_required([UserRole.STUDENT])),
    db: AsyncSession = Depends(get_db_session)
) -> list[GrievanceResponse]:
    service = AcademicService(db)
    records = await service.get_user_grievances(current_user.id)
    return [
        GrievanceResponse(id=r.id, category=r.category, description=r.description, status=r.status, assigned_to=r.assigned_to)
        for r in records
    ]

@router.post("/grievances/raise", response_model=GrievanceResponse)
async def raise_grievance(
    payload: GrievanceRaiseRequest,
    current_user: User = Depends(role_required([UserRole.STUDENT])),
    db: AsyncSession = Depends(get_db_session)
) -> GrievanceResponse:
    service = AcademicService(db)
    # Assign to admin automatically
    admin_q = await db.execute(select(User).where(User.role == UserRole.ADMIN))
    admin = admin_q.scalars().first()
    admin_id = admin.id if admin else None
    
    g = await service.raise_grievance(
        raised_by=current_user.id,
        category=payload.category,
        description=payload.description
    )
    g.assigned_to = admin_id
    await db.commit()
    return GrievanceResponse(id=g.id, category=g.category, description=g.description, status=g.status, assigned_to=g.assigned_to)

@router.get("/study-materials", response_model=list[StudyMaterialResponse])
async def get_study_materials(
    current_user: User = Depends(role_required([UserRole.STUDENT])),
    db: AsyncSession = Depends(get_db_session)
) -> list[StudyMaterialResponse]:
    service = AcademicService(db)
    records = await service.sm_repo.get_all_materials()
    return [
        StudyMaterialResponse(id=r.id, title=r.title, type=r.type, file_url=r.file_url, is_verified=r.is_verified)
        for r in records if r.is_verified
    ]

@router.get("/notices", response_model=list[NoticeResponse])
async def get_notices(
    current_user: User = Depends(role_required([UserRole.STUDENT])),
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

@router.get("/notifications", response_model=list[NotificationResponse])
async def get_notifications(
    current_user: User = Depends(role_required([UserRole.STUDENT])),
    db: AsyncSession = Depends(get_db_session)
) -> list[NotificationResponse]:
    service = NotificationService(db)
    records = await service.get_user_notifications(current_user.id)
    return [
        NotificationResponse(id=r.id, message=r.message, type=r.type, is_read=r.is_read, sent_via=r.sent_via)
        for r in records
    ]

@router.post("/notifications/read")
async def read_notifications(
    current_user: User = Depends(role_required([UserRole.STUDENT])),
    db: AsyncSession = Depends(get_db_session)
) -> dict:
    service = NotificationService(db)
    await service.mark_all_as_read(current_user.id)
    await db.commit()
    return {"detail": "Notifications marked as read"}

# --- PARENT PORTAL SUPPORT ROUTERS ---

@router.get("/parent/child/dashboard", response_model=DashboardResponse)
async def parent_child_dashboard(
    current_user: User = Depends(role_required([UserRole.PARENT])),
    db: AsyncSession = Depends(get_db_session)
) -> DashboardResponse:
    # Resolve child profile via ParentStudentMap
    student_repo = StudentRepository(db)
    mappings = await student_repo.get_parent_student_map(current_user.id)
    if not mappings:
        raise HTTPException(status_code=404, detail="No student associated with this parent")
        
    student = await student_repo.get_student_by_id(mappings[0].student_id)
    if not student:
        raise HTTPException(status_code=404, detail="Child student profile not found")

    att_service = AttendanceService(db)
    fee_service = FeeService(db)
    
    att_summary = await att_service.get_student_attendance_summary(student.id)
    fee_summary = await fee_service.get_student_fee_summary(student.id)
    
    return DashboardResponse(
        metrics=[
            MetricSchema(id="attendance", label="Child Attendance %", value=f"{att_summary['percentage']}%"),
            MetricSchema(id="fees", label="Child Due Fees", value=f"₹{fee_summary['due_amount']:,}"),
            MetricSchema(id="exam", label="Upcoming Exams", value="1"),
            MetricSchema(id="cgpa", label="CGPA", value="8.5"),
        ]
    )

@router.get("/parent/notices", response_model=list[NoticeResponse])
async def get_parent_notices(
    current_user: User = Depends(role_required([UserRole.PARENT])),
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


# --- Parent child detail endpoints ---

async def _get_child_student(current_user: User, db: AsyncSession) -> Student:
    student_repo = StudentRepository(db)
    mappings = await student_repo.get_parent_student_map(current_user.id)
    if not mappings:
        raise HTTPException(status_code=404, detail="No student associated with this parent")
    student = await student_repo.get_student_by_id(mappings[0].student_id)
    if not student:
        raise HTTPException(status_code=404, detail="Child student profile not found")
    return student

@router.get("/parent/child/profile", response_model=StudentProfileResponse)
async def parent_child_profile(
    current_user: User = Depends(role_required([UserRole.PARENT])),
    db: AsyncSession = Depends(get_db_session)
) -> StudentProfileResponse:
    student = await _get_child_student(current_user, db)
    
    mentor_name = None
    mentor_email = None
    mentor_phone = None
    if student.mentor_id:
        mentor_q = await db.execute(select(User).where(User.id == student.mentor_id))
        mentor = mentor_q.scalar_one_or_none()
        if mentor:
            mentor_name = mentor.full_name
            mentor_email = mentor.email
            mentor_phone = mentor.phone
    
    student_user_q = await db.execute(select(User).where(User.id == student.user_id))
    student_user = student_user_q.scalar_one_or_none()
    student_email = student_user.email if student_user else ""
    
    course_name = None
    section_name = None
    class_advisor_name = None
    department_name = None
    
    from app.db.models.academic import Degree, Department
    if student.degree_id:
        deg_q = await db.execute(select(Degree).where(Degree.id == student.degree_id))
        deg = deg_q.scalar_one_or_none()
        if deg:
            course_name = deg.name
            
    if student.department_id:
        dept_q = await db.execute(select(Department).where(Department.id == student.department_id))
        dept = dept_q.scalar_one_or_none()
        if dept:
            department_name = dept.name
            if not course_name:
                course_name = dept.name
            
    if student.section_id:
        sec_q = await db.execute(select(Section).where(Section.id == student.section_id))
        sec = sec_q.scalar_one_or_none()
        if sec:
            section_name = sec.section_name
            if sec.faculty_id:
                adv_q = await db.execute(select(User).where(User.id == sec.faculty_id))
                adv = adv_q.scalar_one_or_none()
                if adv:
                    class_advisor_name = adv.full_name

    batch = f"{student.batch_year}-{student.batch_year + 5}" if student.batch_year else "2021-2026"
    year_map = {1: "1st", 2: "1st", 3: "2nd", 4: "2nd", 5: "3rd", 6: "3rd", 7: "4th", 8: "4th", 9: "5th", 10: "5th"}
    year_of_study = f"{year_map.get(student.semester, '3rd')} Year"

    return StudentProfileResponse(
        id=student.id,
        roll_no=student.roll_no,
        semester=student.semester,
        batch_year=student.batch_year,
        email=student_email,
        full_name=student.full_name or (student_user.full_name if student_user else ""),
        mentor_name=mentor_name,
        mentor_email=mentor_email,
        mentor_phone=mentor_phone,
        cgpa=student.cgpa,
        skills=student.skills,
        course_name=course_name,
        section=section_name,
        class_advisor_name=class_advisor_name,
        batch=batch,
        year_of_study=year_of_study,
        department_name=department_name,
        date_of_birth=student.date_of_birth,
        gender=student.gender,
        blood_group=student.blood_group,
        nationality=student.nationality,
        mobile_number=student.mobile_number,
        current_address=student.current_address,
        permanent_address=student.permanent_address,
        aadhaar_number=student.aadhaar_number,
        passport_number=student.passport_number,
        community_category=student.community_category,
        religion=student.religion,
        emergency_contact_name=student.emergency_contact_name,
        emergency_contact_relationship=student.emergency_contact_relationship,
        emergency_contact_number=student.emergency_contact_number,
        father_name=student.father_name,
        father_occupation=student.father_occupation,
        father_mobile=student.father_mobile,
        father_email=student.father_email,
        father_office_address=student.father_office_address,
        mother_name=student.mother_name,
        mother_occupation=student.mother_occupation,
        mother_mobile=student.mother_mobile,
        mother_email=student.mother_email,
        mother_office_address=student.mother_office_address,
        parent_annual_income=student.parent_annual_income,
        languages_known=student.languages_known,
        hobbies_interests=student.hobbies_interests,
        special_skills=student.special_skills,
        medical_info=student.medical_info,
        certifications=student.certifications,
        internships=student.internships,
        sports_records=student.sports_records,
        moot_courts=student.moot_courts
    )

@router.get("/parent/child/attendance", response_model=AttendanceSummaryResponse)
async def parent_child_attendance(
    current_user: User = Depends(role_required([UserRole.PARENT])),
    db: AsyncSession = Depends(get_db_session)
) -> AttendanceSummaryResponse:
    student = await _get_child_student(current_user, db)
    att_service = AttendanceService(db)
    summary = await att_service.get_student_attendance_summary(student.id)
    records = [
        AttendanceRecordSchema(
            id=r["id"],
            date=r["date"],
            status=r["status"],
            subject_name=r["subject_name"],
            subject_code=r["subject_code"],
            section_name=r["section_name"]
        )
        for r in summary.get("records", [])
    ]
    return AttendanceSummaryResponse(
        percentage=summary["percentage"],
        total=summary["total"],
        present=summary["present"],
        absent=summary["absent"],
        od=summary["od"],
        records=records
    )

@router.get("/parent/child/marks", response_model=list[MarkRecordSchema])
async def parent_child_marks(
    current_user: User = Depends(role_required([UserRole.PARENT])),
    db: AsyncSession = Depends(get_db_session)
) -> list[MarkRecordSchema]:
    student = await _get_child_student(current_user, db)
    acad_service = AcademicService(db)
    marks = await acad_service.acad_repo.get_marks_by_student(student.id)
    return [
        MarkRecordSchema(id=m.id, exam_type=m.exam_type, mark=m.mark, max_mark=m.max_mark)
        for m in marks
    ]

@router.get("/parent/child/fees", response_model=StudentFeeSummaryResponse)
async def parent_child_fees(
    current_user: User = Depends(role_required([UserRole.PARENT])),
    db: AsyncSession = Depends(get_db_session)
) -> StudentFeeSummaryResponse:
    student = await _get_child_student(current_user, db)
    fee_service = FeeService(db)
    summary = await fee_service.get_student_fee_summary(student.id)
    return summary

@router.get("/parent/child/timetable", response_model=list[TimetableItemResponse])
async def parent_child_timetable(
    current_user: User = Depends(role_required([UserRole.PARENT])),
    db: AsyncSession = Depends(get_db_session)
) -> list[TimetableItemResponse]:
    student = await _get_child_student(current_user, db)
    
    section_name = "A"
    if student.section_id:
        sec_q = await db.execute(select(Section).where(Section.id == student.section_id))
        sec = sec_q.scalar_one_or_none()
        if sec:
            section_name = sec.section_name
    else:
        sec_q = await db.execute(
            select(Section.section_name)
            .join(Course, Section.course_id == Course.id)
            .where(
                Course.degree_id == student.degree_id if student.degree_id else Course.dept_id == student.department_id,
                Course.semester == student.semester,
                Section.is_deleted.is_(False)
            )
            .distinct()
        )
        first_sec_name = sec_q.scalar()
        if first_sec_name:
            section_name = first_sec_name

    timetable_stmt = (
        select(Timetable)
        .join(Section, Timetable.section_id == Section.id)
        .join(Course, Timetable.subject_id == Course.id)
        .where(
            Course.semester == student.semester,
            Section.section_name == section_name,
            Timetable.is_deleted.is_(False),
            Section.is_deleted.is_(False),
            Course.is_deleted.is_(False)
        )
    )
    if student.degree_id:
        timetable_stmt = timetable_stmt.where(Course.degree_id == student.degree_id)
    else:
        timetable_stmt = timetable_stmt.where(Course.dept_id == student.department_id)

    result = await db.execute(timetable_stmt)
    items = result.scalars().all()

    response_items = []
    for item in items:
        app_q = await db.execute(
            select(TimetableApproval).where(
                TimetableApproval.timetable_id == item.id,
                TimetableApproval.status == ApprovalStatus.APPROVED
            )
        )
        app = app_q.scalar_one_or_none()
        if not app:
            continue
            
        course_q = await db.execute(select(Course).where(Course.id == item.subject_id))
        course = course_q.scalar_one_or_none()
        
        fac_q = await db.execute(select(User).where(User.id == item.faculty_id))
        faculty = fac_q.scalar_one_or_none()
        
        response_items.append(
            TimetableItemResponse(
                id=item.id,
                subject_code=course.code if course else "LAW101",
                subject_name=course.name if course else "Constitutional Law",
                faculty_name=faculty.full_name if faculty else "Faculty",
                room=item.room,
                weekday=item.weekday.value,
                start_time=item.start_time.strftime("%H:%M"),
                end_time=item.end_time.strftime("%H:%M")
            )
        )
    return response_items

@router.get("/parent/child/courses", response_model=list[CourseResponse])
async def parent_child_courses(
    current_user: User = Depends(role_required([UserRole.PARENT])),
    db: AsyncSession = Depends(get_db_session)
) -> list[CourseResponse]:
    student = await _get_child_student(current_user, db)
    
    stmt = select(Course).where(Course.is_deleted.is_(False), Course.semester == student.semester)
    if student.degree_id:
        stmt = stmt.where(Course.degree_id == student.degree_id)
    else:
        stmt = stmt.where(Course.dept_id == student.department_id)
        
    result = await db.execute(stmt.order_by(Course.code))
    courses = result.scalars().all()
    
    return [
        CourseResponse(
            id=c.id,
            code=c.code,
            name=c.name,
            credits=c.credits,
            semester=c.semester,
            degree_id=c.degree_id,
            dept_id=c.dept_id
        )
        for c in courses
    ]

@router.get("/parent/child/internal-marks")
async def parent_child_internal_marks(
    current_user: User = Depends(role_required([UserRole.PARENT])),
    db: AsyncSession = Depends(get_db_session)
):
    from app.db.models.marks import InternalMark
    from app.db.models.academic import Course
    
    student = await _get_child_student(current_user, db)
    if not student:
        raise HTTPException(status_code=404, detail="Child student record not found")
        
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
        })
        
    return response_list

@router.get("/papers")
async def get_student_papers(
    current_user: User = Depends(role_required([UserRole.STUDENT])),
    db: AsyncSession = Depends(get_db_session)
):
    import json, os
    db_path = os.path.join(os.path.dirname(os.path.dirname(os.path.dirname(__file__))), "papers_db.json")
    if not os.path.exists(db_path):
        initial_papers = [
            {
                "id": 1,
                "title": "Comparative Analysis of Data Privacy Laws: GDPR vs DPDP Act",
                "abstract": "An in-depth academic paper comparing the data protection frameworks of the EU and India, focusing on corporate compliance costs and individual rights enforcement.",
                "category": "Cyber Law",
                "status": "Published",
                "guide": "Dr. Vikram Seth",
                "team": ["student_full_name", "Rohit Kumar"],
                "submissionDate": "Nov 12, 2025",
                "awards": ["Best Research Paper - Cyber Law 2025"],
                "fileSize": "2.4 MB",
                "featured": True,
                "fileUrl": "/static/uploads/1.pdf"
            },
            {
                "id": 2,
                "title": "Efficacy of Alternate Dispute Resolution in Commercial Contracts",
                "abstract": "Analyzing the success rate of arbitration and mediation clauses in Tier-1 corporate disputes over the last decade in India.",
                "category": "Corporate",
                "status": "Under Review",
                "guide": "Prof. S.K. Gupta",
                "team": ["Aditya Nanda", "Zoya Khan", "Rahul Verma"],
                "submissionDate": "Dec 05, 2025",
                "awards": [],
                "fileSize": "1.8 MB",
                "featured": False,
                "fileUrl": "/static/uploads/2.pdf"
            },
            {
                "id": 3,
                "title": "Bail Jurisprudence: The 'Bail Not Jail' Principle in Practice",
                "abstract": "A statistical study of bail rejection rates in trial courts versus high courts, focusing on the newly enacted Bharatiya Nyaya Sanhita (BNS) provisions.",
                "category": "Criminal",
                "status": "Published",
                "guide": "Dr. A. Krishnan",
                "team": ["student_full_name"],
                "submissionDate": "Oct 28, 2025",
                "awards": ["Excellence in Criminal Jurisprudence"],
                "fileSize": "3.1 MB",
                "featured": True,
                "fileUrl": "/static/uploads/1.pdf"
            }
        ]
        with open(db_path, "w") as f:
            json.dump(initial_papers, f, indent=2)
            
    try:
        with open(db_path, "r") as f:
            papers = json.load(f)
        user_name = current_user.full_name or "Data not fetched"
        for p in papers:
            if "team" in p and isinstance(p["team"], list):
                p["team"] = [user_name if name in ("Priya Lakshmi", "Ananya Sharma", "Karan Singh", "student_full_name") else name for name in p["team"]]
        return papers
    except Exception:
        return []

@router.post("/papers")
async def submit_student_paper(
    payload: dict,
    current_user: User = Depends(role_required([UserRole.STUDENT])),
    db: AsyncSession = Depends(get_db_session)
):
    import json, os
    db_path = os.path.join(os.path.dirname(os.path.dirname(os.path.dirname(__file__))), "papers_db.json")
    
    papers = []
    if os.path.exists(db_path):
        try:
            with open(db_path, "r") as f:
                papers = json.load(f)
        except Exception:
            pass
            
    user_name = current_user.full_name or "Data not fetched"
    submitted_team = payload.get("team", [])
    if submitted_team:
        # Bind the logged in user as the primary author
        submitted_team[0] = user_name
    else:
        submitted_team = [user_name]

    new_paper = {
        "id": len(papers) + 1,
        "title": payload.get("title", ""),
        "abstract": payload.get("abstract", ""),
        "category": payload.get("category", "Corporate"),
        "status": "Under Review",
        "guide": payload.get("guide", ""),
        "team": submitted_team,
        "submissionDate": payload.get("submissionDate", date.today().strftime("%b %d, %Y")),
        "awards": [],
        "fileSize": payload.get("fileSize", "1.5 MB"),
        "featured": False,
        "fileUrl": payload.get("fileUrl") or None
    }
    
    papers.insert(0, new_paper)
    with open(db_path, "w") as f:
        json.dump(papers, f, indent=2)
        
    return new_paper

@router.post("/papers/upload")
async def upload_student_paper_file(
    file: UploadFile = File(...),
    current_user: User = Depends(role_required([UserRole.STUDENT])),
    db: AsyncSession = Depends(get_db_session)
):
    import os, uuid
    static_dir = os.path.join(os.path.dirname(os.path.dirname(os.path.dirname(os.path.dirname(os.path.abspath(__file__))))), "static")
    papers_dir = os.path.join(static_dir, "uploads", "papers")
    os.makedirs(papers_dir, exist_ok=True)
    
    ext = os.path.splitext(file.filename)[1] if file.filename else ".pdf"
    if ext.lower() != ".pdf":
        raise HTTPException(status_code=400, detail="Only PDF files are allowed.")
        
    filename = f"paper_{uuid.uuid4().hex}{ext}"
    filepath = os.path.join(papers_dir, filename)
    
    content = await file.read()
    with open(filepath, "wb") as f:
        f.write(content)
        
    # Get the file size format
    size_mb = len(content) / (1024 * 1024)
    file_size_str = f"{size_mb:.1f} MB"
        
    return {"fileUrl": f"/static/uploads/papers/{filename}", "fileSize": file_size_str}

# --- STUDENT VERIFICATION WORKFLOW ENDPOINTS ---

@router.post("/profile/submit", response_model=StudentProfileResponse)
async def submit_student_profile(
    current_user: User = Depends(role_required([UserRole.STUDENT])),
    db: AsyncSession = Depends(get_db_session)
) -> StudentProfileResponse:
    student_repo = StudentRepository(db)
    student = await student_repo.get_student_by_user_id(current_user.id)
    if not student:
        raise HTTPException(status_code=404, detail="Student profile not found")
        
    # Set verification status to Submitted (Under Staff Verification)
    student.verification_status = "SUBMITTED"
    student.staff_remarks = None
    student.hod_remarks = None
    
    await db.commit()
    await db.refresh(student)
    
    # Reload profile response
    return await student_profile(current_user, db)

@router.post("/profile/document")
async def upload_student_document(
    document_type: str,
    file: UploadFile = File(...),
    current_user: User = Depends(role_required([UserRole.STUDENT])),
    db: AsyncSession = Depends(get_db_session)
):
    import os
    import uuid
    
    if document_type not in ["aadhaar", "community", "tc", "other", "certification"]:
        raise HTTPException(status_code=400, detail="Invalid document type")
        
    student_repo = StudentRepository(db)
    student = await student_repo.get_student_by_user_id(current_user.id)
    if not student:
        raise HTTPException(status_code=404, detail="Student profile not found")
        
    static_dir = os.path.join(os.path.dirname(os.path.dirname(os.path.dirname(os.path.dirname(os.path.abspath(__file__))))), "static")
    docs_dir = os.path.join(static_dir, "uploads", "documents")
    os.makedirs(docs_dir, exist_ok=True)
    
    ext = os.path.splitext(file.filename)[1] if file.filename else ".pdf"
    if ext.lower() not in [".pdf", ".jpg", ".jpeg", ".png"]:
        raise HTTPException(status_code=400, detail="Only PDF, JPG, and PNG are allowed.")
        
    safe_filename = f"{document_type}_{student.id}_{uuid.uuid4().hex}{ext}"
    file_path = os.path.join(docs_dir, safe_filename)
    
    with open(file_path, "wb") as f:
        f.write(await file.read())
        
    file_url = f"/mock-uploads/{safe_filename}"
    
    # Save corresponding URL in DB if not certification
    if document_type == "aadhaar":
        student.document_aadhaar_url = file_url
    elif document_type == "community":
        student.document_community_url = file_url
    elif document_type == "tc":
        student.document_tc_url = file_url
    elif document_type == "other":
        student.document_other_url = file_url
        
    await db.commit()
    
    return {"status": "success", "file_url": file_url}

class EditRequestPayload(BaseModel):
    reason: str

@router.post("/profile/request-edit")
async def request_student_profile_edit(
    payload: EditRequestPayload,
    current_user: User = Depends(role_required([UserRole.STUDENT])),
    db: AsyncSession = Depends(get_db_session)
):
    student_repo = StudentRepository(db)
    student = await student_repo.get_student_by_user_id(current_user.id)
    if not student:
        raise HTTPException(status_code=404, detail="Student profile not found")
        
    if student.verification_status != "VERIFIED_LOCKED":
        raise HTTPException(status_code=400, detail="Profile is not locked. You can edit it directly.")
        
    student.edit_request_status = "PENDING_STAFF"
    student.edit_request_reason = payload.reason
    
    await db.commit()
    return {"status": "success", "detail": "Edit request submitted successfully to class advisor / staff."}

@router.get("/temp-debug-users")
async def temp_debug_users(db: AsyncSession = Depends(get_db_session)):
    res = await db.execute(select(User))
    users = res.scalars().all()
    return [{"email": u.email, "role": u.role, "id": u.id, "full_name": u.full_name} for u in users]



