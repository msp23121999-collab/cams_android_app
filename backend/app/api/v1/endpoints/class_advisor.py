from datetime import date
from typing import List, Optional
from fastapi import APIRouter, Depends, HTTPException, status
from pydantic import BaseModel
from sqlalchemy import select, and_, delete, distinct
from sqlalchemy.ext.asyncio import AsyncSession

from app.core.dependencies import get_db_session, role_required, get_current_user
from app.db.models.user import User, UserRole
from app.db.models.academic import Department, AcademicYear, Section, Course, Degree, Timetable, TimetableApproval, ApprovalStatus
from app.db.models.student import Student
from app.db.models.class_advisor import ClassAdvisor
from app.db.models.leave import LeaveRequest, LeaveApproval, LeaveStatus
from app.db.models.fee import FeeRecord, FeeStructure, FeeStatus
from app.db.models.marks import InternalMark
from app.db.models.attendance import Attendance

router = APIRouter()

# Pydantic Schemas
class AdvisorAssignmentRequest(BaseModel):
    academic_year_id: str
    batch: str
    section_name: str
    faculty_id: str

class ClassAdvisorResponse(BaseModel):
    academic_year_id: str
    batch: str
    section_name: str
    faculty_id: Optional[str] = None
    faculty_name: Optional[str] = None

class ClassAdvisorSetupResponse(BaseModel):
    classes: List[ClassAdvisorResponse]
    faculty: List[dict]

class MyAssignmentResponse(BaseModel):
    is_advisor: bool
    class_details: Optional[dict] = None

class StudentListItem(BaseModel):
    student_id: str
    name: str
    roll_no: str
    department: str
    semester: int
    year_of_study: str
    attendance_percentage: float
    total_marks: float
    fee_status: str
    leave_status: str

class LeaveRequestActionPayload(BaseModel):
    status: str
    remarks: Optional[str] = None

@router.get("/hod/classes", response_model=ClassAdvisorSetupResponse)
async def get_hod_classes(
    current_user: User = Depends(role_required([UserRole.HOD])),
    db: AsyncSession = Depends(get_db_session)
):
    if not current_user.department_id:
        raise HTTPException(status_code=400, detail="HOD not assigned to a department")

    # Fetch active batches / academic years
    ay_res = await db.execute(
        select(AcademicYear)
        .join(Degree, AcademicYear.degree_id == Degree.id)
        .where(
            AcademicYear.is_active.is_(True),
            AcademicYear.is_deleted.is_(False),
            Degree.dept_id == current_user.department_id
        ).order_by(AcademicYear.created_at.desc())
    )
    active_ays = []
    seen_batches = set()
    for ay in ay_res.scalars().all():
        if ay.batch == "All":
            continue
        if ay.batch not in seen_batches:
            seen_batches.add(ay.batch)
            active_ays.append(ay)

    # Get department faculty list for dropdown
    fac_res = await db.execute(
        select(User).where(
            User.department_id == current_user.department_id,
            User.role.in_([UserRole.FACULTY, UserRole.HOD]),
            User.is_active.is_(True)
        )
    )
    faculty_list = [{"id": f.id, "name": f.full_name, "email": f.email} for f in fac_res.scalars().all()]

    # Fetch existing class advisor assignments in this department
    advisor_res = await db.execute(
        select(ClassAdvisor).where(ClassAdvisor.department_id == current_user.department_id)
    )
    existing_advisors = {
        f"{adv.academic_year_id}:{adv.batch}:{adv.section_name}": adv
        for adv in advisor_res.scalars().all()
    }

    # Fetch sections to determine active section names (e.g. A, B)
    sec_res = await db.execute(
        select(distinct(Section.section_name))
        .join(Course, Section.course_id == Course.id)
        .where(Course.dept_id == current_user.department_id)
    )
    section_names = [r[0] for r in sec_res.all()]
    if not section_names:
        section_names = ["A"]

    classes = []
    for ay in active_ays:
        for sec_name in section_names:
            key = f"{ay.id}:{ay.batch}:{sec_name}"
            existing = existing_advisors.get(key)
            
            faculty_name = None
            if existing:
                fac_user = await db.get(User, existing.faculty_id)
                if fac_user:
                    faculty_name = fac_user.full_name

            classes.append(ClassAdvisorResponse(
                academic_year_id=ay.id,
                batch=ay.batch,
                section_name=sec_name,
                faculty_id=existing.faculty_id if existing else None,
                faculty_name=faculty_name
            ))

    return ClassAdvisorSetupResponse(classes=classes, faculty=faculty_list)

@router.post("/hod/assign")
async def assign_class_advisor(
    payload: AdvisorAssignmentRequest,
    current_user: User = Depends(role_required([UserRole.HOD])),
    db: AsyncSession = Depends(get_db_session)
):
    if not current_user.department_id:
        raise HTTPException(status_code=400, detail="HOD not assigned to a department")

    # Check if assignment already exists
    stmt = select(ClassAdvisor).where(
        ClassAdvisor.department_id == current_user.department_id,
        ClassAdvisor.academic_year_id == payload.academic_year_id,
        ClassAdvisor.batch == payload.batch,
        ClassAdvisor.section_name == payload.section_name
    )
    res = await db.execute(stmt)
    existing = res.scalars().first()

    if existing:
        existing.faculty_id = payload.faculty_id
    else:
        new_assignment = ClassAdvisor(
            academic_year_id=payload.academic_year_id,
            faculty_id=payload.faculty_id,
            department_id=current_user.department_id,
            batch=payload.batch,
            section_name=payload.section_name
        )
        db.add(new_assignment)

    await db.commit()
    return {"message": "Class advisor assigned successfully"}

@router.get("/my-assignment", response_model=MyAssignmentResponse)
async def get_my_assignment(
    current_user: User = Depends(role_required([UserRole.FACULTY, UserRole.HOD])),
    db: AsyncSession = Depends(get_db_session)
):
    stmt = select(ClassAdvisor).where(ClassAdvisor.faculty_id == current_user.id)
    res = await db.execute(stmt)
    assignment = res.scalars().first()

    if not assignment:
        return MyAssignmentResponse(is_advisor=False)

    dept = await db.get(Department, assignment.department_id)
    ay = await db.get(AcademicYear, assignment.academic_year_id)

    class_details = {
        "academic_year_id": assignment.academic_year_id,
        "academic_year_name": ay.name if ay else "",
        "batch": assignment.batch,
        "section_name": assignment.section_name,
        "department_id": assignment.department_id,
        "department_name": dept.name if dept else "Unknown",
        "semester": ay.current_semester if ay else 1,
        "degree_id": ay.degree_id if ay else None
    }

    return MyAssignmentResponse(is_advisor=True, class_details=class_details)

@router.get("/students", response_model=List[StudentListItem])
async def get_advisor_students(
    current_user: User = Depends(role_required([UserRole.FACULTY, UserRole.HOD])),
    db: AsyncSession = Depends(get_db_session)
):
    # Get advisor assignment
    adv_stmt = select(ClassAdvisor).where(ClassAdvisor.faculty_id == current_user.id)
    adv_res = await db.execute(adv_stmt)
    assignment = adv_res.scalars().first()
    if not assignment:
        raise HTTPException(status_code=403, detail="You are not assigned as a Class Advisor")

    try:
        batch_year = int(assignment.batch.split("-")[0])
    except Exception:
        batch_year = 2026

    # Fetch students
    stud_stmt = (
        select(Student, User)
        .join(User, Student.user_id == User.id)
        .outerjoin(Section, Student.section_id == Section.id)
        .where(
            Student.department_id == assignment.department_id,
            Student.batch_year == batch_year,
            Student.is_deleted.is_(False)
        )
    )
    if assignment.section_name:
        stud_stmt = stud_stmt.where(
            Section.section_name == assignment.section_name
        )

    students_db = await db.execute(stud_stmt)
    results = []
    
    year_map = {1: "1st", 2: "1st", 3: "2nd", 4: "2nd", 5: "3rd", 6: "3rd", 7: "4th", 8: "4th", 9: "5th", 10: "5th"}
    dept = await db.get(Department, assignment.department_id)
    dept_name = dept.name if dept else "Unknown"

    for student, user in students_db.all():
        # 1. Attendance Percentage
        if student.section_id:
            att_stmt = select(Attendance).where(
                Attendance.section_id == student.section_id,
                Attendance.is_deleted.is_(False)
            )
            att_res = await db.execute(att_stmt)
            att_records = att_res.scalars().all()
            total_att = len(att_records)
            
            absent_count = 0
            for r in att_records:
                absentees = r.absentee_ids or []
                if student.id in absentees:
                    absent_count += 1
            
            present_att = total_att - absent_count
            att_percentage = (present_att / total_att * 100) if total_att > 0 else 100.0
        else:
            att_percentage = 100.0

        # 2. Internal Marks Total
        marks_stmt = select(InternalMark).where(
            InternalMark.student_id == student.id,
            InternalMark.status == "APPROVED"
        )
        marks_res = await db.execute(marks_stmt)
        marks_records = marks_res.scalars().all()
        total_marks = sum(float(m.total_mark) for m in marks_records)

        # 3. Fee Status
        fee_records_q = await db.execute(
            select(FeeRecord, FeeStructure)
            .join(FeeStructure, FeeRecord.fee_structure_id == FeeStructure.id)
            .where(FeeRecord.student_id == student.id)
        )
        fee_records = fee_records_q.all()
        total_fee = sum(float(f[1].amount) for f in fee_records)
        paid_fee = sum(float(f[1].amount) for f in fee_records if f[0].status == FeeStatus.PAID)
        pending_fee = total_fee - paid_fee

        fee_status = "Pending"
        if total_fee > 0:
            if pending_fee == 0:
                fee_status = "Paid"
            elif paid_fee > 0:
                fee_status = "Partially Paid"

        # 4. Leave Status (Last Request)
        leave_stmt = select(LeaveRequest).where(LeaveRequest.user_id == user.id).order_by(LeaveRequest.created_at.desc()).limit(1)
        leave_res = await db.execute(leave_stmt)
        last_leave = leave_res.scalars().first()
        leave_status = last_leave.status if last_leave else "No Leaves"

        year_str = f"{year_map.get(student.semester, '3rd')} Year"

        results.append(StudentListItem(
            student_id=student.id,
            name=user.full_name,
            roll_no=student.roll_no,
            department=dept_name,
            semester=student.semester,
            year_of_study=year_str,
            attendance_percentage=round(att_percentage, 1),
            total_marks=round(total_marks, 1),
            fee_status=fee_status,
            leave_status=leave_status
        ))

    return results

@router.get("/dashboard-stats")
async def get_advisor_dashboard_stats(
    current_user: User = Depends(role_required([UserRole.FACULTY, UserRole.HOD])),
    db: AsyncSession = Depends(get_db_session)
):
    # Get advisor assignment
    adv_stmt = select(ClassAdvisor).where(ClassAdvisor.faculty_id == current_user.id)
    adv_res = await db.execute(adv_stmt)
    assignment = adv_res.scalars().first()
    if not assignment:
        raise HTTPException(status_code=403, detail="Not assigned as a Class Advisor")

    try:
        batch_year = int(assignment.batch.split("-")[0])
    except Exception:
        batch_year = 2026

    # Fetch students
    stud_stmt = (
        select(Student, User)
        .join(User, Student.user_id == User.id)
        .outerjoin(Section, Student.section_id == Section.id)
        .where(
            Student.department_id == assignment.department_id,
            Student.batch_year == batch_year,
            Student.is_deleted.is_(False)
        )
    )
    if assignment.section_name:
        stud_stmt = stud_stmt.where(
            Section.section_name == assignment.section_name
        )

    students_db = await db.execute(stud_stmt)
    students = students_db.all()
    total_students = len(students)

    attendance_alerts = 0
    fee_dues = 0
    pending_leaves = 0
    marks_pending_hod = 0

    student_ids = [s[0].id for s in students]
    user_ids = [s[1].id for s in students]

    for student, user in students:
        # Attendance Check
        att_stmt = select(Attendance).where(Attendance.student_id == student.id)
        att_res = await db.execute(att_stmt)
        att_records = att_res.scalars().all()
        total_att = len(att_records)
        present_att = sum(1 for r in att_records if r.status == "present" or r.status == "od")
        att_pct = (present_att / total_att * 100) if total_att > 0 else 100.0
        if att_pct < 75.0:
            attendance_alerts += 1

        # Fee Dues Check
        fee_records_q = await db.execute(
            select(FeeRecord, FeeStructure)
            .join(FeeStructure, FeeRecord.fee_structure_id == FeeStructure.id)
            .where(FeeRecord.student_id == student.id)
        )
        fee_records = fee_records_q.all()
        total_fee = sum(float(f[1].amount) for f in fee_records)
        paid_fee = sum(float(f[1].amount) for f in fee_records if f[0].status == FeeStatus.PAID)
        if total_fee - paid_fee > 0:
            fee_dues += 1

    # Leave requests check
    if user_ids:
        leave_stmt = select(LeaveRequest).where(
            LeaveRequest.user_id.in_(user_ids),
            LeaveRequest.status == LeaveStatus.PENDING
        )
        leave_res = await db.execute(leave_stmt)
        pending_leaves = len(leave_res.scalars().all())

    # Marks pending check
    if student_ids:
        marks_stmt = select(InternalMark).where(
            InternalMark.student_id.in_(student_ids),
            InternalMark.status == "SUBMITTED"
        )
        marks_res = await db.execute(marks_stmt)
        marks_pending_hod = len(marks_res.scalars().all())

    return {
        "total_students": total_students,
        "attendance_alerts": attendance_alerts,
        "marks_pending_hod": marks_pending_hod,
        "students_with_fee_due": fee_dues,
        "pending_leave_requests": pending_leaves
    }

@router.get("/leaves")
async def get_advisor_class_leaves(
    current_user: User = Depends(role_required([UserRole.FACULTY, UserRole.HOD])),
    db: AsyncSession = Depends(get_db_session)
):
    adv_stmt = select(ClassAdvisor).where(ClassAdvisor.faculty_id == current_user.id)
    adv_res = await db.execute(adv_stmt)
    assignment = adv_res.scalars().first()
    if not assignment:
        raise HTTPException(status_code=403, detail="Not assigned as a Class Advisor")

    try:
        batch_year = int(assignment.batch.split("-")[0])
    except Exception:
        batch_year = 2026

    # Fetch student user IDs
    stud_stmt = (
        select(Student.user_id)
        .outerjoin(Section, Student.section_id == Section.id)
        .where(
            Student.department_id == assignment.department_id,
            Student.batch_year == batch_year,
            Student.is_deleted.is_(False)
        )
    )
    if assignment.section_name:
        stud_stmt = stud_stmt.where(
            Section.section_name == assignment.section_name
        )

    res = await db.execute(stud_stmt)
    user_ids = [r[0] for r in res.all()]

    if not user_ids:
        return []

    leave_stmt = (
        select(LeaveRequest, User)
        .join(User, LeaveRequest.user_id == User.id)
        .where(LeaveRequest.user_id.in_(user_ids))
        .order_by(LeaveRequest.created_at.desc())
    )
    leaves_res = await db.execute(leave_stmt)
    
    result = []
    for leave, user in leaves_res.all():
        # Get approval if any
        app_stmt = select(LeaveApproval).where(LeaveApproval.leave_id == leave.id)
        app_res = await db.execute(app_stmt)
        app = app_res.scalar_one_or_none()

        result.append({
            "id": leave.id,
            "student_name": user.full_name,
            "type": leave.type,
            "from_date": leave.from_date.isoformat(),
            "to_date": leave.to_date.isoformat(),
            "num_days": leave.num_days,
            "reason": leave.reason,
            "status": leave.status,
            "attachment_url": leave.attachment_url,
            "remarks": app.remarks if app else None
        })

    return result

@router.post("/leaves/{leave_id}/action")
async def action_advisor_student_leave(
    leave_id: str,
    payload: LeaveRequestActionPayload,
    current_user: User = Depends(role_required([UserRole.FACULTY, UserRole.HOD])),
    db: AsyncSession = Depends(get_db_session)
):
    leave = await db.get(LeaveRequest, leave_id)
    if not leave:
        raise HTTPException(status_code=404, detail="Leave request not found")

    action = payload.status.upper()
    if "REJECT" in action:
        if not payload.remarks or not payload.remarks.strip():
            raise HTTPException(
                status_code=status.HTTP_422_UNPROCESSABLE_ENTITY,
                detail="Remarks are required for rejection."
            )

    # Record approval
    app_stmt = select(LeaveApproval).where(LeaveApproval.leave_id == leave_id)
    app_res = await db.execute(app_stmt)
    approval = app_res.scalar_one_or_none()

    if not approval:
        approval = LeaveApproval(
            leave_id=leave_id,
            approved_by=current_user.id,
            status=payload.status,
            remarks=payload.remarks
        )
        db.add(approval)
    else:
        approval.status = payload.status
        approval.remarks = payload.remarks

    # Update leave status
    leave.status = payload.status
    await db.commit()
    return {"message": f"Leave successfully {payload.status.lower()}"}

@router.get("/class-timetable")
async def get_class_advisor_timetable(
    current_user: User = Depends(role_required([UserRole.FACULTY, UserRole.HOD])),
    db: AsyncSession = Depends(get_db_session)
):
    # Get advisor assignment
    adv_stmt = select(ClassAdvisor).where(ClassAdvisor.faculty_id == current_user.id)
    adv_res = await db.execute(adv_stmt)
    assignment = adv_res.scalars().first()
    if not assignment:
        raise HTTPException(status_code=403, detail="You are not assigned as a Class Advisor")

    # Fetch Academic Year details
    ay = await db.get(AcademicYear, assignment.academic_year_id)
    semester = ay.current_semester if ay else 1

    timetable_stmt = (
        select(Timetable)
        .join(Section, Timetable.section_id == Section.id)
        .join(Course, Timetable.subject_id == Course.id)
        .where(
            Course.semester == semester,
            Section.section_name == assignment.section_name,
            Timetable.is_deleted.is_(False),
            Section.is_deleted.is_(False),
            Course.is_deleted.is_(False)
        )
    )
    if ay and ay.degree_id:
        timetable_stmt = timetable_stmt.where(Course.degree_id == ay.degree_id)
    else:
        timetable_stmt = timetable_stmt.where(Course.dept_id == assignment.department_id)

    result = await db.execute(timetable_stmt)
    items = result.scalars().all()

    response_items = []
    for item in items:
        # Check approved slots
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
        
        response_items.append({
            "id": item.id,
            "subject_code": course.code if course else "",
            "subject_name": course.name if course else "",
            "faculty_name": faculty.full_name if faculty else "Faculty",
            "room": item.room,
            "weekday": item.weekday.value,
            "start_time": item.start_time.strftime("%H:%M"),
            "end_time": item.end_time.strftime("%H:%M")
        })
    return response_items

