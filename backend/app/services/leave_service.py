from datetime import date, timedelta
from fastapi import HTTPException, status
from sqlalchemy import select, and_, or_
from sqlalchemy.ext.asyncio import AsyncSession
from sqlalchemy.orm import selectinload

from app.db.models.leave import LeaveRequest, LeaveApproval, LeaveBalance, LeaveStatus
from app.db.models.user import User, UserRole
from app.db.models.faculty import FacultyProfile


class LeaveService:
    def __init__(self, db: AsyncSession) -> None:
        self.db = db

    async def get_balances(self, user_id: str) -> LeaveBalance:
        stmt = select(LeaveBalance).where(LeaveBalance.user_id == user_id)
        res = await self.db.execute(stmt)
        balance = res.scalar_one_or_none()

        if not balance:
            # Create default balances if none exists
            balance = LeaveBalance(
                user_id=user_id,
                casual_leave=10.0,
                sick_leave=5.0,
                earned_leave=12.0,
                on_duty_leave=10.0
            )
            self.db.add(balance)
            await self.db.commit()
            await self.db.refresh(balance)

        return balance

    async def create_leave_request(
        self,
        user_id: str,
        type: str,
        from_date: date,
        to_date: date,
        reason: str,
        emergency_contact: str,
        attachment_url: str | None = None
    ) -> LeaveRequest:
        # 1. Past dates check
        if from_date < date.today():
            raise HTTPException(
                status_code=status.HTTP_400_BAD_REQUEST,
                detail="Cannot apply for leave on past dates."
            )
        if to_date < from_date:
            raise HTTPException(
                status_code=status.HTTP_400_BAD_REQUEST,
                detail="To Date cannot be before From Date."
            )

        # 2. Calculate number of days
        num_days = float((to_date - from_date).days + 1)

        # 3. Check balance
        balance = await self.get_balances(user_id)
        
        # Map leave types to columns
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
        
        mapped_col = type_map.get(type)
        if mapped_col:
            avail_balance = getattr(balance, mapped_col)
            if num_days > avail_balance:
                raise HTTPException(
                    status_code=status.HTTP_400_BAD_REQUEST,
                    detail=f"Insufficient leave balance. You requested {num_days} days of {type}, but only {avail_balance} are available."
                )

        # 4. Check overlapping requests
        overlap_stmt = select(LeaveRequest).where(
            and_(
                LeaveRequest.user_id == user_id,
                LeaveRequest.status != LeaveStatus.REJECTED,
                or_(
                    and_(LeaveRequest.from_date <= from_date, LeaveRequest.to_date >= from_date),
                    and_(LeaveRequest.from_date <= to_date, LeaveRequest.to_date >= to_date),
                    and_(LeaveRequest.from_date >= from_date, LeaveRequest.to_date <= to_date)
                )
            )
        )
        overlap_res = await self.db.execute(overlap_stmt)
        if overlap_res.scalars().first():
            raise HTTPException(
                status_code=status.HTTP_400_BAD_REQUEST,
                detail="You already have an active leave request overlapping with these dates."
            )

        # 5. Create request
        request = LeaveRequest(
            user_id=user_id,
            type=type,
            from_date=from_date,
            to_date=to_date,
            num_days=num_days,
            reason=reason,
            emergency_contact=emergency_contact,
            attachment_url=attachment_url,
            status=LeaveStatus.PENDING
        )
        self.db.add(request)
        await self.db.commit()
        await self.db.refresh(request)
        return request

    async def get_leave_history(self, user_id: str) -> list[LeaveRequest]:
        stmt = (
            select(LeaveRequest)
            .where(LeaveRequest.user_id == user_id)
            .order_by(LeaveRequest.created_at.desc())
        )
        res = await self.db.execute(stmt)
        return list(res.scalars().all())

    async def get_pending_approvals(self, hod_id: str) -> list[dict]:
        # HOD can approve requests where they are the reporting HOD
        # First get users reporting to this HOD
        profile_stmt = select(FacultyProfile.user_id).where(FacultyProfile.reporting_hod_id == hod_id)
        profile_res = await self.db.execute(profile_stmt)
        reported_user_ids = [row[0] for row in profile_res.all()]

        # HOD can also approve requests from their department if no reporting HOD is explicitly set
        hod_user_stmt = select(User).where(User.id == hod_id)
        hod_user_res = await self.db.execute(hod_user_stmt)
        hod_user = hod_user_res.scalar_one_or_none()
        
        dept_user_ids = []
        if hod_user and hod_user.department_id:
            dept_users_stmt = select(User.id).where(
                and_(
                    User.department_id == hod_user.department_id,
                    User.role == UserRole.FACULTY
                )
            )
            dept_users_res = await self.db.execute(dept_users_stmt)
            dept_user_ids = [row[0] for row in dept_users_res.all()]

        all_target_ids = list(set(reported_user_ids + dept_user_ids))

        if not all_target_ids:
            return []

        stmt = (
            select(LeaveRequest)
            .where(LeaveRequest.status.in_([LeaveStatus.PENDING, LeaveStatus.PENDING_HOD]))
            .options(selectinload(LeaveRequest.user))
            .where(
                and_(
                    LeaveRequest.user_id.in_(all_target_ids),
                    LeaveRequest.status == LeaveStatus.PENDING
                )
            )
            .order_by(LeaveRequest.created_at.desc())
        )
        res = await self.db.execute(stmt)
        leaves = res.scalars().all()
        
        return [
            {
                "id": l.id,
                "user_id": l.user_id,
                "user_name": l.user.full_name if l.user else "Faculty Member",
                "type": l.type,
                "from_date": l.from_date,
                "to_date": l.to_date,
                "num_days": l.num_days,
                "reason": l.reason,
                "emergency_contact": l.emergency_contact,
                "attachment_url": l.attachment_url,
                "status": l.status,
            }
            for l in leaves
        ]

    async def process_approval(
        self,
        leave_id: str,
        approver_id: str,
        status: LeaveStatus,
        remarks: str | None = None
    ) -> LeaveRequest:
        # 1. Fetch leave request
        stmt = select(LeaveRequest).where(LeaveRequest.id == leave_id)
        res = await self.db.execute(stmt)
        request = res.scalar_one_or_none()
        if not request:
            raise HTTPException(status_code=404, detail="Leave request not found")

        if request.status not in (LeaveStatus.PENDING, LeaveStatus.PENDING_HOD):
            raise HTTPException(status_code=400, detail="This leave request has already been processed")

        # Ownership check: HOD may only approve leaves for faculty who report to
        # them (FacultyProfile.reporting_hod_id) or belong to their own department.
        approver_stmt = select(User).where(User.id == approver_id)
        approver_res = await self.db.execute(approver_stmt)
        approver = approver_res.scalar_one_or_none()
        if approver and approver.role == UserRole.HOD:
            applicant_stmt = select(User).where(User.id == request.user_id)
            applicant_res = await self.db.execute(applicant_stmt)
            applicant = applicant_res.scalar_one_or_none()

            profile_stmt = select(FacultyProfile.reporting_hod_id).where(FacultyProfile.user_id == request.user_id)
            profile_res = await self.db.execute(profile_stmt)
            reporting_hod_id = profile_res.scalar_one_or_none()

            is_authorized = (
                reporting_hod_id == approver_id
                or (applicant is not None and applicant.department_id == approver.department_id)
            )
            if not is_authorized:
                raise HTTPException(status_code=403, detail="You can only approve leaves for faculty in your own department")

        # 2. Create approval entry
        approval = LeaveApproval(
            leave_id=leave_id,
            approved_by=approver_id,
            status=status,
            remarks=remarks
        )
        self.db.add(approval)

        # 3. Update request status
        request.status = status
        
        # 4. If approved, deduct from balance
        if status == LeaveStatus.APPROVED:
            balance = await self.get_balances(request.user_id)
            
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
            
            mapped_col = type_map.get(request.type)
            if mapped_col:
                current_bal = getattr(balance, mapped_col)
                # Deduct, ensuring it doesn't go below 0 (though we checked on create)
                new_bal = max(0.0, current_bal - request.num_days)
                setattr(balance, mapped_col, new_bal)
                self.db.add(balance)

        await self.db.commit()
        await self.db.refresh(request)
        return request
