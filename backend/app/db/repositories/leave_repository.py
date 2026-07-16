from datetime import date, datetime
from sqlalchemy import select, update
from sqlalchemy.ext.asyncio import AsyncSession
from app.db.models.leave import LeaveRequest, LeaveApproval, LeaveStatus

class LeaveRepository:
    def __init__(self, db: AsyncSession) -> None:
        self.db = db

    async def get_leaves_by_user(self, user_id: str) -> list[LeaveRequest]:
        result = await self.db.execute(
            select(LeaveRequest).where(LeaveRequest.user_id == user_id, LeaveRequest.is_deleted.is_(False))
        )
        return list(result.scalars().all())

    async def get_all_leaves(self) -> list[LeaveRequest]:
        result = await self.db.execute(
            select(LeaveRequest).where(LeaveRequest.is_deleted.is_(False))
        )
        return list(result.scalars().all())

    async def get_leave_by_id(self, leave_id: str) -> LeaveRequest | None:
        result = await self.db.execute(
            select(LeaveRequest).where(LeaveRequest.id == leave_id, LeaveRequest.is_deleted.is_(False))
        )
        return result.scalar_one_or_none()

    async def create_leave_request(
        self,
        user_id: str,
        app_category: str,
        type_val: str,
        session_type: str | None,
        priority: str | None,
        from_date: date,
        to_date: date,
        reason: str,
        photo_url: str | None = None,
        latitude: float | None = None,
        longitude: float | None = None,
        location_address: str | None = None,
        capture_time: datetime | None = None,
        verification_status: str | None = None,
        distance_from_campus: float | None = None,
        device_id: str | None = None,
        location_accuracy: float | None = None,
        geo_fence_status: str | None = None,
        device_network_info: str | None = None,
        metadata_: dict | None = None,
    ) -> LeaveRequest:
        from app.db.models.student import Student
        from app.db.models.user import User

        emergency_contact = "9999999999"
        student_res = await self.db.execute(
            select(Student.emergency_contact_number).where(Student.user_id == user_id)
        )
        student_contact = student_res.scalar_one_or_none()
        if student_contact:
            emergency_contact = student_contact
        else:
            user_res = await self.db.execute(
                select(User.phone).where(User.id == user_id)
            )
            user_phone = user_res.scalar_one_or_none()
            if user_phone:
                emergency_contact = user_phone

        num_days = float((to_date - from_date).days + 1)
        if session_type != "Full Day" and num_days == 1.0:
            num_days = 0.5

        req = LeaveRequest(
            user_id=user_id,
            type=f"[{app_category}] {type_val}",
            from_date=from_date,
            to_date=to_date,
            num_days=num_days,
            reason=reason,
            emergency_contact=emergency_contact,
            status=LeaveStatus.PENDING,
            attachment_url=photo_url,
        )
        self.db.add(req)
        await self.db.flush()
        return req


    async def approve_leave(self, leave_id: str, approved_by: str, status: LeaveStatus, remarks: str | None = None) -> LeaveApproval:
        # Get request to find user_id, type and num_days
        req = await self.get_leave_by_id(leave_id)

        # Update leave request status
        await self.db.execute(
            update(LeaveRequest)
            .where(LeaveRequest.id == leave_id)
            .values(status=status)
        )

        # Deduct balance if APPROVED
        if status == LeaveStatus.APPROVED and req:
            from app.services.leave_service import LeaveService
            service = LeaveService(self.db)
            balance = await service.get_balances(req.user_id)
            
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
            
            mapped_col = None
            for key, val in type_map.items():
                if key in req.type:
                    mapped_col = val
                    break
            if mapped_col:
                current_bal = getattr(balance, mapped_col)
                new_bal = max(0.0, current_bal - (req.num_days or 1.0))
                setattr(balance, mapped_col, new_bal)
                self.db.add(balance)

        # Add approval entry
        approval = LeaveApproval(
            leave_id=leave_id,
            approved_by=approved_by,
            status=status,
            remarks=remarks
        )
        self.db.add(approval)
        await self.db.flush()
        return approval
