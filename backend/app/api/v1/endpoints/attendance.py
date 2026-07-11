from datetime import date, datetime
from typing import Literal
from fastapi import APIRouter, Depends, HTTPException, Query, status
from pydantic import BaseModel
from sqlalchemy.ext.asyncio import AsyncSession
from sqlalchemy import select, func

from app.core.dependencies import get_db_session, role_required
from app.db.models.user import User, UserRole
from app.db.models.attendance import StaffAttendance
from app.services.staff_attendance_service import StaffAttendanceService

router = APIRouter()

class ToggleRequest(BaseModel):
    faculty_id: str
    is_present: bool

class CheckInRequest(BaseModel):
    faculty_id: str
    check_in_time: str  # "HH:MM:SS"

class CheckOutRequest(BaseModel):
    faculty_id: str
    check_out_time: str  # "HH:MM:SS"

@router.get("/today")
async def get_today_attendance(
    current_user: User = Depends(role_required([UserRole.ADMIN, UserRole.SUPER_ADMIN, UserRole.PRINCIPAL])),
    db: AsyncSession = Depends(get_db_session)
):
    service = StaffAttendanceService(db)
    today_val = date.today()
    
    # Check if initialized for today
    q = await db.execute(
        select(StaffAttendance).where(
            StaffAttendance.date == today_val,
            StaffAttendance.is_deleted.is_(False)
        )
    )
    records = q.scalars().all()
    if not records:
        # Auto-initialize today's records if they don't exist
        await service.initialize_daily_attendance(today_val)
        q = await db.execute(
            select(StaffAttendance).where(
                StaffAttendance.date == today_val,
                StaffAttendance.is_deleted.is_(False)
            )
        )
        records = q.scalars().all()

    # Build response list containing faculty info
    response = []
    for r in records:
        user = await db.get(User, r.faculty_id)
        if user:
            await db.refresh(user, ["department"])
            response.append({
                "faculty_id": r.faculty_id,
                "faculty_name": user.full_name,
                "department": user.department.name if user.department else "General",
                "designation": getattr(user, "role", UserRole.FACULTY).value,
                "status": r.status,
                "check_in": r.check_in,
                "check_out": r.check_out,
                "working_hours": float(r.working_hours) if r.working_hours else None,
                "source": r.source
            })
    return response

@router.post("/toggle")
async def toggle_staff_attendance(
    payload: ToggleRequest,
    current_user: User = Depends(role_required([UserRole.ADMIN, UserRole.SUPER_ADMIN])),
    db: AsyncSession = Depends(get_db_session)
):
    service = StaffAttendanceService(db)
    today_val = date.today()
    att = await service.toggle_attendance(payload.faculty_id, today_val, payload.is_present)
    return {"detail": "Status toggled successfully", "status": att.status}

@router.post("/check-in")
async def staff_check_in(
    payload: CheckInRequest,
    current_user: User = Depends(role_required([UserRole.ADMIN, UserRole.SUPER_ADMIN])),
    db: AsyncSession = Depends(get_db_session)
):
    service = StaffAttendanceService(db)
    today_val = date.today()
    try:
        att = await service.check_in(payload.faculty_id, today_val, payload.check_in_time)
        return {"detail": "Check In registered successfully", "check_in": att.check_in, "status": att.status}
    except ValueError as e:
        raise HTTPException(status_code=400, detail=str(e))

@router.post("/check-out")
async def staff_check_out(
    payload: CheckOutRequest,
    current_user: User = Depends(role_required([UserRole.ADMIN, UserRole.SUPER_ADMIN])),
    db: AsyncSession = Depends(get_db_session)
):
    service = StaffAttendanceService(db)
    today_val = date.today()
    try:
        att = await service.check_out(payload.faculty_id, today_val, payload.check_out_time)
        return {"detail": "Check Out registered successfully", "check_out": att.check_out, "working_hours": float(att.working_hours) if att.working_hours else 0.0}
    except ValueError as e:
        raise HTTPException(status_code=400, detail=str(e))

@router.get("/history/{faculty_id}")
async def get_staff_history(
    faculty_id: str,
    start_date: str = Query(..., description="YYYY-MM-DD"),
    end_date: str = Query(..., description="YYYY-MM-DD"),
    current_user: User = Depends(role_required([UserRole.ADMIN, UserRole.SUPER_ADMIN, UserRole.PRINCIPAL, UserRole.FACULTY, UserRole.HOD])),
    db: AsyncSession = Depends(get_db_session)
):
    service = StaffAttendanceService(db)
    try:
        sd = datetime.strptime(start_date, "%Y-%m-%d").date()
        ed = datetime.strptime(end_date, "%Y-%m-%d").date()
    except Exception:
        raise HTTPException(status_code=400, detail="Invalid date format. Use YYYY-MM-DD.")
    
    return await service.get_faculty_history(faculty_id, sd, ed)

@router.get("/analytics")
async def get_dashboard_analytics(
    current_user: User = Depends(role_required([UserRole.ADMIN, UserRole.SUPER_ADMIN, UserRole.PRINCIPAL])),
    db: AsyncSession = Depends(get_db_session)
):
    today_val = date.today()
    
    # 1. Total active staff counts
    staff_roles = [UserRole.FACULTY, UserRole.HOD, UserRole.ADMIN, UserRole.SUPER_ADMIN, UserRole.PRINCIPAL]
    users_q = await db.execute(
        select(func.count(User.id)).where(
            User.role.in_(staff_roles),
            User.is_active.is_(True),
            User.is_deleted.is_(False)
        )
    )
    total_staff = users_q.scalar_one() or 0

    # 2. Status counts for today
    att_q = await db.execute(
        select(StaffAttendance.status, func.count(StaffAttendance.id))
        .where(StaffAttendance.date == today_val, StaffAttendance.is_deleted.is_(False))
        .group_by(StaffAttendance.status)
    )
    counts = dict(att_q.all())
    
    present = counts.get("Present", 0) + counts.get("Late", 0)
    absent = counts.get("Absent", 0)
    holiday = counts.get("Holiday", 0) + counts.get("Sunday", 0)
    late = counts.get("Late", 0)
    
    # Leave category check
    on_leave = sum(val for key, val in counts.items() if "leave" in key.lower() or key in ["On Duty", "OD"])

    # Checked In & Out count
    ci_q = await db.execute(
        select(func.count(StaffAttendance.id)).where(
            StaffAttendance.date == today_val,
            StaffAttendance.check_in.isnot(None),
            StaffAttendance.is_deleted.is_(False)
        )
    )
    checked_in = ci_q.scalar_one() or 0

    co_q = await db.execute(
        select(func.count(StaffAttendance.id)).where(
            StaffAttendance.date == today_val,
            StaffAttendance.check_out.isnot(None),
            StaffAttendance.is_deleted.is_(False)
        )
    )
    checked_out = co_q.scalar_one() or 0

    return {
        "total_faculty": total_staff,
        "present_faculty": present,
        "absent_faculty": absent,
        "on_leave_faculty": on_leave,
        "holiday_faculty": holiday,
        "late_faculty": late,
        "checked_in_count": checked_in,
        "checked_out_count": checked_out
    }

@router.get("/reports")
async def generate_reports(
    type: Literal["daily", "weekly", "monthly", "faculty", "department", "leave", "late", "hours"] = Query("daily"),
    start_date: str = Query(..., description="YYYY-MM-DD"),
    end_date: str = Query(..., description="YYYY-MM-DD"),
    current_user: User = Depends(role_required([UserRole.ADMIN, UserRole.SUPER_ADMIN, UserRole.PRINCIPAL])),
    db: AsyncSession = Depends(get_db_session)
):
    try:
        sd = datetime.strptime(start_date, "%Y-%m-%d").date()
        ed = datetime.strptime(end_date, "%Y-%m-%d").date()
    except Exception:
        raise HTTPException(status_code=400, detail="Invalid date format. Use YYYY-MM-DD.")

    # Query all attendance records in range
    q = await db.execute(
        select(StaffAttendance).where(
            StaffAttendance.date >= sd,
            StaffAttendance.date <= ed,
            StaffAttendance.is_deleted.is_(False)
        ).order_by(StaffAttendance.date.desc())
    )
    records = q.scalars().all()

    report_data = []
    for r in records:
        user = await db.get(User, r.faculty_id)
        if not user:
            continue
        await db.refresh(user, ["department"])
        dept = user.department.name if user.department else "General"
        
        row = {
            "date": r.date.isoformat(),
            "faculty_id": r.faculty_id,
            "faculty_name": user.full_name,
            "department": dept,
            "role": user.role.value,
            "status": r.status,
            "check_in": r.check_in or "-",
            "check_out": r.check_out or "-",
            "working_hours": float(r.working_hours) if r.working_hours else 0.0,
            "source": r.source
        }

        # Apply specific report type filters
        if type == "leave" and "leave" not in r.status.lower() and r.status not in ["On Duty", "OD"]:
            continue
        if type == "late" and r.status != "Late":
            continue
        if type == "hours" and not r.working_hours:
            continue

        report_data.append(row)

    return report_data

class ChangeStatusRequest(BaseModel):
    faculty_id: str
    status: str

@router.post("/change-status")
async def change_staff_status(
    payload: ChangeStatusRequest,
    current_user: User = Depends(role_required([UserRole.ADMIN, UserRole.SUPER_ADMIN])),
    db: AsyncSession = Depends(get_db_session)
):
    service = StaffAttendanceService(db)
    today_val = date.today()
    try:
        att = await service.change_status(payload.faculty_id, today_val, payload.status)
        return {"detail": "Status manually updated", "status": att.status}
    except Exception as e:
        raise HTTPException(status_code=400, detail=str(e))
