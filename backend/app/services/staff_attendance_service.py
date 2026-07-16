import json
import logging
from datetime import date, datetime, time
from sqlalchemy import select, and_, update
from sqlalchemy.ext.asyncio import AsyncSession
from app.db.models.user import User, UserRole
from app.db.models.attendance import StaffAttendance
from app.db.models.leave import LeaveRequest, LeaveStatus
from app.db.models.payroll import WorkingDayConfig
from app.services.firebase_service import FirebaseService

logger = logging.getLogger(__name__)

class StaffAttendanceService:
    def __init__(self, db: AsyncSession) -> None:
        self.db = db

    async def evaluate_attendance_status_for_date(self, faculty_id: str, date_val: date) -> str:
        """
        Evaluate today's status based on rules:
        1. Academic Calendar / WorkingDayConfig (Highest Priority)
        2. Approved Leave Requests (Second Priority)
        3. Default: Absent
        """
        # 1. Check Academic Calendar / WorkingDayConfig
        q = await self.db.execute(
            select(WorkingDayConfig).where(
                WorkingDayConfig.month == date_val.month,
                WorkingDayConfig.year == date_val.year,
                WorkingDayConfig.is_deleted.is_(False)
            )
        )
        config = q.scalar_one_or_none()
        is_holiday = False
        holiday_reason = "Holiday"

        if config:
            if config.overrides_json:
                try:
                    overrides = json.loads(config.overrides_json)
                    date_str = date_val.isoformat()
                    if date_str in overrides:
                        override_val = overrides[date_str]
                        status = override_val if isinstance(override_val, str) else override_val.get("status")
                        reason = "" if isinstance(override_val, str) else override_val.get("reason", "")
                        
                        if status == "holiday":
                            is_holiday = True
                            if reason:
                                holiday_reason = reason
                        elif status == "event":
                            is_holiday = False  # Usually events are working days unless specified, wait. 
                            # If it's an event, we might want to return the event reason. But for attendance, is an event a working day? Yes.
                            # Let's just track it as a special status if needed, or leave it. The requirement says "change working day to holiday... or event".
                            pass
                except Exception as e:
                    logger.error(f"Error parsing overrides_json: {str(e)}")
            
            # If no override and it's Sunday, default Sunday Holiday
            if not is_holiday and date_val.weekday() == 6:
                is_holiday = True
                holiday_reason = "Sunday"
        else:
            # Fallback default: Sundays are holidays
            if date_val.weekday() == 6:
                is_holiday = True
                holiday_reason = "Sunday"

        if is_holiday:
            return holiday_reason

        # 2. Check Approved Leaves
        leave_q = await self.db.execute(
            select(LeaveRequest).where(
                LeaveRequest.user_id == faculty_id,
                LeaveRequest.status == LeaveStatus.APPROVED,
                LeaveRequest.from_date <= date_val,
                LeaveRequest.to_date >= date_val,
                LeaveRequest.is_deleted.is_(False)
            )
        )
        leave = leave_q.scalar_one_or_none()
        if leave:
            return leave.type  # e.g. "Medical Leave", "Casual Leave", etc.

        # 3. Default
        return "Absent"

    async def initialize_daily_attendance(self, date_val: date) -> dict:
        """
        Runs at 12:01 AM. Creates attendance entries for all active staff and syncs to Firebase.
        """
        from sqlalchemy.orm import selectinload
        # Get all active faculty/staff roles
        staff_roles = [UserRole.FACULTY, UserRole.HOD, UserRole.ADMIN, UserRole.SUPER_ADMIN, UserRole.PRINCIPAL]
        users_q = await self.db.execute(
            select(User).options(selectinload(User.department)).where(
                User.role.in_(staff_roles),
                User.is_active.is_(True),
                User.is_deleted.is_(False)
            )
        )
        users = users_q.scalars().all()
        
        firebase_data = {}
        for user in users:
            # Check if entry already exists
            exist_q = await self.db.execute(
                select(StaffAttendance).where(
                    StaffAttendance.faculty_id == user.id,
                    StaffAttendance.date == date_val,
                    StaffAttendance.is_deleted.is_(False)
                )
            )
            att = exist_q.scalar_one_or_none()
            if not att:
                status = await self.evaluate_attendance_status_for_date(user.id, date_val)
                att = StaffAttendance(
                    faculty_id=user.id,
                    date=date_val,
                    status=status,
                    source="Manual"
                )
                self.db.add(att)
            
            # Add to bulk sync
            dept_name = user.department.name if user.department else "General"

            firebase_data[user.id] = {
                "faculty_id": user.id,
                "faculty_name": user.full_name,
                "department": dept_name,
                "status": att.status,
                "check_in": att.check_in,
                "check_out": att.check_out
            }

        await self.db.commit()
        # Overwrite today's attendance node in Firebase Realtime Database
        await FirebaseService.bulk_sync(firebase_data)
        return {"processed": len(users)}

    async def toggle_attendance(self, faculty_id: str, date_val: date, is_present: bool) -> StaffAttendance:
        """
        Toggle presence status.
        """
        # Find or create record
        q = await self.db.execute(
            select(StaffAttendance).where(
                StaffAttendance.faculty_id == faculty_id,
                StaffAttendance.date == date_val,
                StaffAttendance.is_deleted.is_(False)
            )
        )
        att = q.scalar_one_or_none()
        if not att:
            att = StaffAttendance(
                faculty_id=faculty_id,
                date=date_val,
                status="Absent",
                source="Manual"
            )
            self.db.add(att)

        new_status = "Present" if is_present else "Absent"
        att.status = new_status
        att.check_in = None
        att.check_out = None
        att.working_hours = None
        
        await self.db.commit()
        await self.db.refresh(att)

        # Trigger Firebase Sync
        user = await self.db.get(User, faculty_id)
        if user:
            await self.db.refresh(user, ["department"])
            dept_name = user.department.name if user.department else "General"
            await FirebaseService.sync_faculty(
                faculty_id=faculty_id,
                faculty_name=user.full_name,
                department=dept_name,
                status=att.status,
                check_in=att.check_in,
                check_out=att.check_out
            )
        return att

    async def change_status(self, faculty_id: str, date_val: date, status: str) -> StaffAttendance:
        """
        Change status to a specific value manually.
        """
        q = await self.db.execute(
            select(StaffAttendance).where(
                StaffAttendance.faculty_id == faculty_id,
                StaffAttendance.date == date_val,
                StaffAttendance.is_deleted.is_(False)
            )
        )
        att = q.scalar_one_or_none()
        if not att:
            att = StaffAttendance(
                faculty_id=faculty_id,
                date=date_val,
                status=status,
                source="Manual"
            )
            self.db.add(att)
        else:
            att.status = status
        
        await self.db.commit()
        await self.db.refresh(att)

        # Trigger Firebase Sync
        user = await self.db.get(User, faculty_id)
        if user:
            await self.db.refresh(user, ["department"])
            dept_name = user.department.name if user.department else "General"
            await FirebaseService.sync_faculty(
                faculty_id=faculty_id,
                faculty_name=user.full_name,
                department=dept_name,
                status=att.status,
                check_in=att.check_in,
                check_out=att.check_out
            )
        return att

    async def check_in(self, faculty_id: str, date_val: date, check_in_time: str) -> StaffAttendance:
        """
        Perform staff check-in.
        """
        q = await self.db.execute(
            select(StaffAttendance).where(
                StaffAttendance.faculty_id == faculty_id,
                StaffAttendance.date == date_val,
                StaffAttendance.is_deleted.is_(False)
            )
        )
        att = q.scalar_one_or_none()
        if not att:
            att = StaffAttendance(
                faculty_id=faculty_id,
                date=date_val,
                status="Present",
                source="Manual"
            )
            self.db.add(att)

        if att.check_in:
            raise ValueError("Double Check In is not allowed.")

        # Determine Late Status if past 09:15 AM
        try:
            ci_t = datetime.strptime(check_in_time, "%H:%M:%S").time()
            late_threshold = time(9, 15, 0)
            if ci_t > late_threshold and att.status not in ["Casual Leave", "Medical Leave", "Holiday", "Sunday"]:
                att.status = "Late"
            elif att.status not in ["Casual Leave", "Medical Leave", "Holiday", "Sunday"]:
                att.status = "Present"
        except Exception:
            att.status = "Present"

        att.check_in = check_in_time
        await self.db.commit()
        await self.db.refresh(att)

        # Sync
        user = await self.db.get(User, faculty_id)
        if user:
            await self.db.refresh(user, ["department"])
            dept_name = user.department.name if user.department else "General"
            await FirebaseService.sync_faculty(
                faculty_id=faculty_id,
                faculty_name=user.full_name,
                department=dept_name,
                status=att.status,
                check_in=att.check_in,
                check_out=att.check_out
            )
        return att

    async def check_out(self, faculty_id: str, date_val: date, check_out_time: str) -> StaffAttendance:
        """
        Perform staff check-out.
        """
        q = await self.db.execute(
            select(StaffAttendance).where(
                StaffAttendance.faculty_id == faculty_id,
                StaffAttendance.date == date_val,
                StaffAttendance.is_deleted.is_(False)
            )
        )
        att = q.scalar_one_or_none()
        if not att:
            raise ValueError("No check-in record found for today.")

        if not att.check_in:
            raise ValueError("Check In before Check Out is not allowed.")

        if att.check_out:
            raise ValueError("Double Check Out is not allowed.")

        att.check_out = check_out_time

        # Calculate working hours
        try:
            fmt = "%H:%M:%S"
            t_in = datetime.strptime(att.check_in, fmt)
            t_out = datetime.strptime(check_out_time, fmt)
            delta = t_out - t_in
            hours = round(delta.total_seconds() / 3600.0, 2)
            if hours < 0:
                raise ValueError("Check Out before Check In is not allowed.")
            att.working_hours = hours
        except Exception as e:
            logger.error(f"Error calculating working hours: {str(e)}")
            att.working_hours = 0.0

        await self.db.commit()
        await self.db.refresh(att)

        # Sync
        user = await self.db.get(User, faculty_id)
        if user:
            await self.db.refresh(user, ["department"])
            dept_name = user.department.name if user.department else "General"
            await FirebaseService.sync_faculty(
                faculty_id=faculty_id,
                faculty_name=user.full_name,
                department=dept_name,
                status=att.status,
                check_in=att.check_in,
                check_out=att.check_out
            )
        return att

    async def get_faculty_history(self, faculty_id: str, start_date: date, end_date: date) -> list[dict]:
        """
        Fetch staff attendance history between two dates.
        """
        q = await self.db.execute(
            select(StaffAttendance).where(
                StaffAttendance.faculty_id == faculty_id,
                StaffAttendance.date >= start_date,
                StaffAttendance.date <= end_date,
                StaffAttendance.is_deleted.is_(False)
            ).order_by(StaffAttendance.date.desc())
        )
        records = q.scalars().all()
        return [
            {
                "id": r.id,
                "date": r.date.isoformat(),
                "status": r.status,
                "check_in": r.check_in,
                "check_out": r.check_out,
                "working_hours": float(r.working_hours) if r.working_hours else None,
                "source": r.source
            }
            for r in records
        ]
