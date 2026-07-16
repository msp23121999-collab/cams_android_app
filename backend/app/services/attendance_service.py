from datetime import date
from sqlalchemy.ext.asyncio import AsyncSession
from sqlalchemy import select
from app.db.repositories.academic_repository import AcademicRepository
from app.db.models.attendance import Attendance

class AttendanceService:
    def __init__(self, db: AsyncSession) -> None:
        self.repo = AcademicRepository(db)

    async def get_student_attendance_summary(self, student_id: str) -> dict:
        from app.db.models.student import Student
        
        student_stmt = select(Student).where(Student.id == student_id)
        student_res = await self.repo.db.execute(student_stmt)
        student = student_res.scalar_one_or_none()
        
        records = await self.repo.get_attendance_with_details_by_student(student_id)
        
        total_records = len(records)
        present = sum(1 for r in records if r["status"].value == "present")
        absent = sum(1 for r in records if r["status"].value == "absent")
        od = sum(1 for r in records if r["status"].value == "od")
        
        leave_absent_periods = 0
        
        if student:
            from app.db.models.leave import LeaveRequest, LeaveStatus
            from datetime import timedelta
            
            leaves_stmt = select(LeaveRequest).where(
                LeaveRequest.user_id == student.user_id,
                LeaveRequest.is_deleted.is_(False),
                LeaveRequest.status.in_([
                    LeaveStatus.APPROVED,
                    LeaveStatus.FINAL_APPROVED,
                    LeaveStatus.APPROVED_BY_HOD,
                    LeaveStatus.FACULTY_APPROVED,
                    LeaveStatus.ADVISOR_APPROVED
                ])
            )
            leaves_res = await self.repo.db.execute(leaves_stmt)
            approved_leaves = leaves_res.scalars().all()
            
            recorded_dates = {r["date"].strftime("%Y-%m-%d") if hasattr(r["date"], "strftime") else str(r["date"]) for r in records}
            
            for leave in approved_leaves:
                current_date = leave.from_date
                while current_date <= leave.to_date:
                    date_str = current_date.strftime("%Y-%m-%d")
                    if current_date.weekday() != 6:  # Skip Sunday
                        if date_str not in recorded_dates:
                            leave_absent_periods += 6
                    current_date += timedelta(days=1)
        
        effective_absent = absent + leave_absent_periods
        effective_total = total_records + leave_absent_periods
        
        if effective_total == 0:
            percentage = 100.0
        else:
            percentage = ((present + od) / effective_total) * 100
            
        return {
            "percentage": round(percentage, 2),
            "total": effective_total,
            "present": present,
            "absent": effective_absent,
            "od": od,
            "records": records
        }

    async def mark_student_attendance(self, student_id: str, section_id: str, date_val: date, status: str) -> Attendance:
        return await self.repo.mark_attendance(student_id, section_id, date_val, status)
