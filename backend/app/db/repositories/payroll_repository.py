from datetime import datetime, timezone
from sqlalchemy import select, update
from sqlalchemy.ext.asyncio import AsyncSession
from app.db.models.payroll import Salary, Deduction, SalarySlip, DeductionType

class PayrollRepository:
    def __init__(self, db: AsyncSession) -> None:
        self.db = db

    async def get_salary_by_faculty(self, faculty_id: str) -> list[Salary]:
        result = await self.db.execute(
            select(Salary).where(Salary.faculty_id == faculty_id, Salary.is_deleted.is_(False))
        )
        return list(result.scalars().all())

    async def get_all_salaries(self) -> list[Salary]:
        result = await self.db.execute(
            select(Salary).where(Salary.is_deleted.is_(False))
        )
        return list(result.scalars().all())

    async def get_deductions_by_salary(self, salary_id: str) -> list[Deduction]:
        result = await self.db.execute(
            select(Deduction).where(Deduction.salary_id == salary_id, Deduction.is_deleted.is_(False))
        )
        return list(result.scalars().all())

    async def get_slips_by_salary(self, salary_id: str) -> list[SalarySlip]:
        result = await self.db.execute(
            select(SalarySlip).where(SalarySlip.salary_id == salary_id, SalarySlip.is_deleted.is_(False))
        )
        return list(result.scalars().all())

    async def process_salary(self, faculty_id: str, basic: float, allowances: float, gross: float, month: int, year: int) -> Salary:
        sal = Salary(faculty_id=faculty_id, basic=basic, allowances=allowances, gross=gross, month=month, year=year)
        self.db.add(sal)
        await self.db.flush()
        return sal

    async def add_deduction(self, salary_id: str, type_val: DeductionType, days: int, amount: float) -> Deduction:
        ded = Deduction(salary_id=salary_id, type=type_val, days=days, amount=amount)
        self.db.add(ded)
        await self.db.flush()
        return ded

    async def create_salary_slip(self, salary_id: str, pdf_url: str) -> SalarySlip:
        slip = SalarySlip(
            salary_id=salary_id,
            pdf_url=pdf_url,
            generated_at=datetime.now(timezone.utc)
        )
        self.db.add(slip)
        await self.db.flush()
        return slip
