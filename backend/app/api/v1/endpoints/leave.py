import os
import shutil
import uuid
from datetime import date, datetime
from fastapi import APIRouter, Depends, HTTPException, UploadFile, File, Form, status
from sqlalchemy.ext.asyncio import AsyncSession
from sqlalchemy import select

from app.core.dependencies import get_current_user, get_db_session, role_required
from app.db.models.user import User, UserRole
from app.db.models.leave import LeaveStatus, LeaveRequest, LeaveApproval
from app.schemas.leave_schemas import LeaveBalanceResponse, LeaveResponse, LeaveApprovalRequest
from app.services.leave_service import LeaveService

router = APIRouter()


# ─────────────────────────────────────────────────────────────────────────────
# HELPERS
# ─────────────────────────────────────────────────────────────────────────────

async def _build_leave_response(leave: LeaveRequest, db: AsyncSession) -> LeaveResponse:
    """Build a full LeaveResponse with HOD + Principal audit trail."""
    hod_name: str | None = None
    principal_name: str | None = None

    if leave.hod_action_by:
        q = await db.execute(select(User.full_name).where(User.id == leave.hod_action_by))
        hod_name = q.scalar_one_or_none()

    if leave.principal_action_by:
        q = await db.execute(select(User.full_name).where(User.id == leave.principal_action_by))
        principal_name = q.scalar_one_or_none()

    # Legacy approved_by_name — use HOD or Principal name depending on final status
    approved_by_name = hod_name or principal_name

    return LeaveResponse(
        id=leave.id,
        user_id=leave.user_id,
        type=leave.type,
        from_date=leave.from_date,
        to_date=leave.to_date,
        num_days=leave.num_days,
        reason=leave.reason,
        emergency_contact=leave.emergency_contact,
        attachment_url=leave.attachment_url,
        status=leave.status,
        remarks=leave.hod_remarks or leave.principal_remarks,
        approved_by_name=approved_by_name,
        hod_status=getattr(leave, "hod_status", None),
        hod_action_by_name=hod_name,
        hod_action_date=leave.hod_action_date,
        hod_remarks=leave.hod_remarks,
        principal_action_by_name=principal_name,
        principal_action_date=leave.principal_action_date,
        principal_remarks=leave.principal_remarks,
    )


# ─────────────────────────────────────────────────────────────────────────────
# LEAVE BALANCES
# ─────────────────────────────────────────────────────────────────────────────

@router.get("/balances", response_model=LeaveBalanceResponse)
async def get_leave_balances(
    current_user: User = Depends(get_current_user),
    db: AsyncSession = Depends(get_db_session)
) -> LeaveBalanceResponse:
    service = LeaveService(db)
    balances = await service.get_balances(current_user.id)
    return LeaveBalanceResponse(
        casual_leave=balances.casual_leave,
        sick_leave=balances.sick_leave,
        earned_leave=balances.earned_leave,
        on_duty_leave=balances.on_duty_leave
    )


# ─────────────────────────────────────────────────────────────────────────────
# LEAVE HISTORY  (faculty's own leaves, full audit trail)
# ─────────────────────────────────────────────────────────────────────────────

@router.get("/history", response_model=list[LeaveResponse])
async def get_leave_history(
    current_user: User = Depends(get_current_user),
    db: AsyncSession = Depends(get_db_session)
) -> list[LeaveResponse]:
    service = LeaveService(db)
    leaves = await service.get_leave_history(current_user.id)

    return [await _build_leave_response(l, db) for l in leaves]


# ─────────────────────────────────────────────────────────────────────────────
# APPLY LEAVE
# ─────────────────────────────────────────────────────────────────────────────

@router.post("/apply", response_model=LeaveResponse)
async def apply_leave(
    type: str = Form(...),
    from_date: str = Form(...),
    to_date: str = Form(...),
    reason: str = Form(...),
    emergency_contact: str = Form(...),
    file: UploadFile | None = File(None),
    current_user: User = Depends(get_current_user),
    db: AsyncSession = Depends(get_db_session)
) -> LeaveResponse:
    # Parse dates
    try:
        f_date = date.fromisoformat(from_date)
        t_date = date.fromisoformat(to_date)
    except ValueError:
        raise HTTPException(status_code=400, detail="Invalid date format. Use YYYY-MM-DD.")

    attachment_url = None
    if file and file.filename:
        ext = os.path.splitext(file.filename)[1].lower()
        if ext not in [".pdf", ".jpg", ".jpeg", ".png"]:
            raise HTTPException(status_code=400, detail="Unsupported file format. Only PDF, JPG, and PNG are allowed.")

        upload_dir = os.path.join("app", "static", "uploads", "leaves")
        os.makedirs(upload_dir, exist_ok=True)

        filename = f"{uuid.uuid4()}{ext}"
        filepath = os.path.join(upload_dir, filename)
        with open(filepath, "wb") as buffer:
            shutil.copyfileobj(file.file, buffer)
        attachment_url = f"/static/uploads/leaves/{filename}"

    service = LeaveService(db)
    leave = await service.create_leave_request(
        user_id=current_user.id,
        type=type,
        from_date=f_date,
        to_date=t_date,
        reason=reason,
        emergency_contact=emergency_contact,
        attachment_url=attachment_url
    )

    # ── Override status to PENDING_HOD for Faculty / HOD users ──────────
    if current_user.role in [UserRole.FACULTY, UserRole.HOD]:
        leave.status = LeaveStatus.PENDING_HOD

    # ── Send notifications ───────────────────────────────────────────────
    from app.services.notification_service import NotificationService
    from app.db.repositories.student_repository import StudentRepository
    notif_service = NotificationService(db)

    if current_user.role == UserRole.STUDENT:
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
                        message=f"Student {current_user.full_name} has applied for leave ({leave.type}) from {leave.from_date} to {leave.to_date}."
                    )
            if student.department_id:
                from app.db.models.academic import Department
                dept_q = await db.execute(select(Department).where(Department.id == student.department_id))
                dept = dept_q.scalars().first()
                if dept and dept.hod_id:
                    await notif_service.send_notification(
                        user_id=dept.hod_id,
                        type_val="leave_request",
                        message=f"Student {current_user.full_name} has applied for leave ({leave.type}) from {leave.from_date} to {leave.to_date}."
                    )
            from app.db.models.student import ParentStudentMap
            pm_q = await db.execute(select(ParentStudentMap).where(ParentStudentMap.student_id == student.id, ParentStudentMap.is_deleted.is_(False)))
            for pm in pm_q.scalars().all():
                await notif_service.send_notification(
                    user_id=pm.parent_id,
                    type_val="leave_request",
                    message=f"Your child {current_user.full_name} has applied for leave ({leave.type}) from {leave.from_date} to {leave.to_date}."
                )

    elif current_user.role in [UserRole.FACULTY, UserRole.HOD]:
        # Notify HOD of the department
        if current_user.department_id:
            from app.db.models.academic import Department
            dept_q = await db.execute(select(Department).where(Department.id == current_user.department_id))
            dept = dept_q.scalars().first()
            if dept and dept.hod_id and dept.hod_id != current_user.id:
                await notif_service.send_notification(
                    user_id=dept.hod_id,
                    type_val="leave_request",
                    message=f"Faculty {current_user.full_name} has applied for {type} leave from {f_date} to {t_date}. Awaiting your review."
                )

    await db.commit()
    await db.refresh(leave)

    return await _build_leave_response(leave, db)


# ─────────────────────────────────────────────────────────────────────────────
# HOD PENDING LEAVES (legacy route kept for student advisor flow)
# ─────────────────────────────────────────────────────────────────────────────

@router.get("/hod/pending", response_model=list[LeaveResponse])
async def get_hod_pending_leaves(
    current_user: User = Depends(role_required([UserRole.HOD, UserRole.ADMIN, UserRole.SUPER_ADMIN])),
    db: AsyncSession = Depends(get_db_session)
) -> list[LeaveResponse]:
    service = LeaveService(db)
    leaves = await service.get_pending_approvals(current_user.id)
    return [LeaveResponse(**l) for l in leaves]


# ─────────────────────────────────────────────────────────────────────────────
# HOD APPROVE (legacy route kept for backward compatibility with student leaves)
# ─────────────────────────────────────────────────────────────────────────────

@router.post("/hod/approve/{leave_id}", response_model=LeaveResponse)
async def hod_approve_leave(
    leave_id: str,
    payload: LeaveApprovalRequest,
    current_user: User = Depends(role_required([UserRole.HOD, UserRole.ADMIN, UserRole.SUPER_ADMIN])),
    db: AsyncSession = Depends(get_db_session)
) -> LeaveResponse:
    service = LeaveService(db)
    leave = await service.process_approval(
        leave_id=leave_id,
        approver_id=current_user.id,
        status=payload.status,
        remarks=payload.remarks
    )

    from app.services.notification_service import NotificationService
    notif_service = NotificationService(db)

    user_q = await db.execute(select(User).where(User.id == leave.user_id))
    applicant = user_q.scalar_one_or_none()

    status_str = "approved" if payload.status == LeaveStatus.APPROVED else "rejected"
    notif_type = "leave_approval" if payload.status == LeaveStatus.APPROVED else "leave_rejection"

    if applicant:
        await notif_service.send_notification(
            user_id=applicant.id,
            type_val=notif_type,
            message=f"Your leave application from {leave.from_date} to {leave.to_date} has been {status_str} by HOD {current_user.full_name}."
        )

    return LeaveResponse(
        id=leave.id,
        user_id=leave.user_id,
        type=leave.type,
        from_date=leave.from_date,
        to_date=leave.to_date,
        num_days=leave.num_days,
        reason=leave.reason,
        emergency_contact=leave.emergency_contact,
        attachment_url=leave.attachment_url,
        status=leave.status,
        remarks=payload.remarks,
        approved_by_name=current_user.full_name
    )


# ─────────────────────────────────────────────────────────────────────────────
# ADVISOR STUDENTS LEAVES
# ─────────────────────────────────────────────────────────────────────────────

@router.get("/advisor/students", response_model=list[LeaveResponse])
async def get_advisor_students_leaves(
    current_user: User = Depends(get_current_user),
    db: AsyncSession = Depends(get_db_session)
) -> list[LeaveResponse]:
    from app.db.models.student import Student
    from app.db.models.academic import Section

    sec_stmt = select(Section.id).where(Section.faculty_id == current_user.id, Section.is_deleted.is_(False))
    sec_res = await db.execute(sec_stmt)
    section_ids = [row[0] for row in sec_res.all()]

    student_stmt = select(Student.user_id).where(
        Student.is_deleted.is_(False),
        ((Student.mentor_id == current_user.id) |
         (Student.section_id.in_(section_ids) if section_ids else False))
    )
    student_res = await db.execute(student_stmt)
    student_user_ids = [row[0] for row in student_res.all()]

    if not student_user_ids:
        return []

    stmt = (
        select(LeaveRequest, User)
        .join(User, LeaveRequest.user_id == User.id)
        .where(
            LeaveRequest.user_id.in_(student_user_ids),
            LeaveRequest.is_deleted.is_(False)
        )
        .order_by(LeaveRequest.created_at.desc())
    )
    res = await db.execute(stmt)
    rows = res.all()

    res_list = []
    for l, u in rows:
        app_stmt = select(LeaveApproval).where(LeaveApproval.leave_id == l.id).order_by(LeaveApproval.created_at.desc())
        app_res = await db.execute(app_stmt)
        approval = app_res.scalars().first()

        approved_by_name = None
        if approval:
            approver_stmt = select(User.full_name).where(User.id == approval.approved_by)
            approver_res = await db.execute(approver_stmt)
            approved_by_name = approver_res.scalar_one_or_none()

        res_list.append(
            LeaveResponse(
                id=l.id,
                user_id=l.user_id,
                type=l.type,
                from_date=l.from_date,
                to_date=l.to_date,
                num_days=l.num_days,
                reason=l.reason,
                emergency_contact=l.emergency_contact,
                attachment_url=l.attachment_url,
                status=l.status,
                remarks=approval.remarks if approval else None,
                approved_by_name=approved_by_name,
                user_name=u.full_name
            )
        )
    return res_list


@router.post("/advisor/approve/{leave_id}", response_model=LeaveResponse)
async def advisor_approve_leave(
    leave_id: str,
    payload: LeaveApprovalRequest,
    current_user: User = Depends(get_current_user),
    db: AsyncSession = Depends(get_db_session)
) -> LeaveResponse:
    stmt = select(LeaveRequest).where(LeaveRequest.id == leave_id, LeaveRequest.is_deleted.is_(False))
    res = await db.execute(stmt)
    leave = res.scalar_one_or_none()
    if not leave:
        raise HTTPException(status_code=404, detail="Leave request not found")

    if leave.status != LeaveStatus.PENDING:
        raise HTTPException(status_code=400, detail="This leave request has already been processed")

    if payload.status == LeaveStatus.APPROVED:
        if leave.num_days <= 5:
            new_status = LeaveStatus.APPROVED
        else:
            new_status = LeaveStatus.ADVISOR_APPROVED
    else:
        new_status = LeaveStatus.REJECTED

    if new_status == LeaveStatus.APPROVED:
        from app.services.leave_service import LeaveService
        service = LeaveService(db)
        balance = await service.get_balances(leave.user_id)

        type_map = {
            "Casual Leave (CL)": "casual_leave",
            "Casual Leave": "casual_leave",
            "Sick Leave (SL)": "sick_leave",
            "Sick Leave": "sick_leave",
            "Earned Leave (EL)": "earned_leave",
            "Earned Leave": "earned_leave",
            "On Duty Leave (OD)": "on_duty_leave",
            "On Duty Leave": "on_duty_leave",
        }

        mapped_col = type_map.get(leave.type)
        if mapped_col:
            current_bal = getattr(balance, mapped_col)
            new_bal = max(0.0, current_bal - leave.num_days)
            setattr(balance, mapped_col, new_bal)
            db.add(balance)

    approval = LeaveApproval(
        leave_id=leave_id,
        approved_by=current_user.id,
        status=new_status,
        remarks=payload.remarks
    )
    db.add(approval)

    leave.status = new_status
    await db.commit()
    await db.refresh(leave)

    from app.services.notification_service import NotificationService
    notif_service = NotificationService(db)

    user_q = await db.execute(select(User).where(User.id == leave.user_id))
    applicant = user_q.scalar_one_or_none()
    applicant_name = applicant.full_name if applicant else "Student"

    if new_status == LeaveStatus.ADVISOR_APPROVED:
        principal_q = await db.execute(select(User).where(User.role == UserRole.PRINCIPAL, User.is_deleted.is_(False)))
        for principal in principal_q.scalars().all():
            await notif_service.send_notification(
                user_id=principal.id,
                type_val="leave_request",
                message=f"Student {applicant_name}'s leave request approved by Advisor {current_user.full_name} is pending Principal review."
            )
        if applicant:
            await notif_service.send_notification(
                user_id=applicant.id,
                type_val="leave_approval",
                message=f"Your leave request has been approved by your Advisor {current_user.full_name} and forwarded to Principal."
            )
    elif new_status == LeaveStatus.APPROVED:
        if applicant:
            await notif_service.send_notification(
                user_id=applicant.id,
                type_val="leave_approval",
                message=f"Your leave request has been approved directly by your Advisor {current_user.full_name}."
            )
    else:
        if applicant:
            await notif_service.send_notification(
                user_id=applicant.id,
                type_val="leave_rejection",
                message=f"Your leave request has been rejected by your Advisor {current_user.full_name}."
            )

    return LeaveResponse(
        id=leave.id,
        user_id=leave.user_id,
        type=leave.type,
        from_date=leave.from_date,
        to_date=leave.to_date,
        num_days=leave.num_days,
        reason=leave.reason,
        emergency_contact=leave.emergency_contact,
        attachment_url=leave.attachment_url,
        status=leave.status,
        remarks=payload.remarks,
        approved_by_name=current_user.full_name,
        user_name=applicant_name
    )
