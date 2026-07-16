import calendar as cal_module
from datetime import date
from sqlalchemy.ext.asyncio import AsyncSession
from sqlalchemy import select
from app.db.repositories.payroll_repository import PayrollRepository
from app.db.models.payroll import Salary, Deduction, SalarySlip, DeductionType, WorkingDayConfig
from app.db.models.pf import PFConfiguration, PFContribution, PFCalculationMethod
from app.db.models.user import User, UserRole


def to_float(val) -> float:
    return float(val) if val is not None else 0.0


class PayrollService:
    def __init__(self, db: AsyncSession) -> None:
        self.db = db
        self.repo = PayrollRepository(db)

    def get_role_rates(self, role: UserRole | str) -> tuple[float, float]:
        # returns (daily_rate, basic_salary)
        role_str = str(role).split('.')[-1] if hasattr(role, 'value') else str(role)
        if role_str == "PRINCIPAL":
            return 2000.0, 60000.0
        elif role_str == "HOD":
            return 1200.0, 36000.0
        else:
            return 700.0, 21000.0

    async def _get_total_working_days(self, month: int, year: int) -> int:
        """
        Fetch total working days for the given month/year from the admin-configured
        WorkingDayConfig. If no config exists, fall back to counting Mon-Sat
        (all days except Sunday) in the month.
        """
        q = await self.db.execute(
            select(WorkingDayConfig).where(
                WorkingDayConfig.month == month,
                WorkingDayConfig.year == year,
                WorkingDayConfig.is_deleted.is_(False)
            )
        )
        cfg = q.scalars().first()
        if cfg:
            return cfg.total_working_days

        # Default: count Mon-Sat in the month (exclude Sundays)
        days_in_month = cal_module.monthrange(year, month)[1]
        working = 0
        for d in range(1, days_in_month + 1):
            if date(year, month, d).weekday() != 6:  # 6 = Sunday
                working += 1
        return working

    async def _get_approved_leave_days(self, faculty_id: str, month: int, year: int) -> int:
        from app.db.models.leave import LeaveRequest, LeaveStatus
        
        _, days_in_month = cal_module.monthrange(year, month)
        month_start = date(year, month, 1)
        month_end = date(year, month, days_in_month)
        
        q = await self.db.execute(
            select(LeaveRequest).where(
                LeaveRequest.user_id == faculty_id,
                LeaveRequest.status == LeaveStatus.APPROVED,
                LeaveRequest.is_deleted.is_(False),
                LeaveRequest.from_date <= month_end,
                LeaveRequest.to_date >= month_start
            )
        )
        leaves = q.scalars().all()
        
        total_leave_days = 0
        for leave in leaves:
            overlap_start = max(leave.from_date, month_start)
            overlap_end = min(leave.to_date, month_end)
            if overlap_end >= overlap_start:
                overlap_days = (overlap_end - overlap_start).days + 1
                total_leave_days += overlap_days
                
        return total_leave_days

    async def _get_absent_days(self, faculty_id: str, month: int, year: int) -> int:
        from app.db.models.attendance import StaffAttendance
        days_in_month = cal_module.monthrange(year, month)[1]
        month_start = date(year, month, 1)
        month_end = date(year, month, days_in_month)
        
        q = await self.db.execute(
            select(StaffAttendance).where(
                StaffAttendance.faculty_id == faculty_id,
                StaffAttendance.status == "Absent",
                StaffAttendance.is_deleted.is_(False),
                StaffAttendance.date >= month_start,
                StaffAttendance.date <= month_end
            )
        )
        return len(q.scalars().all())

    async def get_approved_leave_days_in_range(self, faculty_id: str, start_date: date, end_date: date) -> float:
        from app.db.models.leave import LeaveRequest, LeaveStatus
        
        q = await self.db.execute(
            select(LeaveRequest).where(
                LeaveRequest.user_id == faculty_id,
                LeaveRequest.status == LeaveStatus.APPROVED,
                LeaveRequest.is_deleted.is_(False),
                LeaveRequest.from_date <= end_date,
                LeaveRequest.to_date >= start_date
            )
        )
        leaves = q.scalars().all()
        
        total_leave_days = 0.0
        for leave in leaves:
            overlap_start = max(leave.from_date, start_date)
            overlap_end = min(leave.to_date, end_date)
            if overlap_end >= overlap_start:
                overlap_days = (overlap_end - overlap_start).days + 1
                total_leave_days += float(overlap_days)
                
        return total_leave_days

    async def get_payroll_preview(self, faculty_id: str, month: int, year: int, joining_date: date | None = None) -> dict:
        fac = await self.db.get(User, faculty_id)
        if not fac or fac.is_deleted:
            raise ValueError("Faculty user not found")
            
        daily_rate, basic = self.get_role_rates(fac.role)
        
        pf_config_q = await self.db.execute(select(PFConfiguration).where(PFConfiguration.faculty_id == faculty_id))
        pf_config = pf_config_q.scalars().first()
        
        if not joining_date:
            joining_date = pf_config.joining_date if pf_config else None
            
        if not joining_date:
            from app.db.models.faculty import FacultyProfile
            prof_q = await self.db.execute(select(FacultyProfile).where(FacultyProfile.user_id == faculty_id))
            prof = prof_q.scalars().first()
            if prof:
                joining_date = prof.date_of_joining
        
        total_working_days = await self._get_total_working_days(month, year)
        if total_working_days <= 0:
            total_working_days = 30
            
        today = date.today()
        is_current_month = (today.year == year and today.month == month)
        
        if is_current_month:
            yesterday_day = today.day - 1
            if joining_date and joining_date.year == year and joining_date.month == month:
                working_days = await self._count_working_days_until(month, year, yesterday_day, from_day=joining_date.day)
            else:
                working_days = await self._count_working_days_until(month, year, yesterday_day) if yesterday_day > 0 else 0
            working_days = max(1, working_days)
        else:
            if joining_date and joining_date.year == year and joining_date.month == month:
                days_in_month = cal_module.monthrange(year, month)[1]
                working_days = await self._count_working_days_until(month, year, days_in_month, from_day=joining_date.day)
                working_days = max(1, working_days)
            else:
                working_days = total_working_days
                
        if total_working_days > 0:
            prorated_basic = round(basic * working_days / total_working_days, 2)
        else:
            prorated_basic = basic
            
        leave_days = await self._get_approved_leave_days(faculty_id, month, year)
        absent_days = await self._get_absent_days(faculty_id, month, year)
        absent_deduction = round(absent_days * daily_rate, 2)
        
        # Semester Calculation
        if month <= 6:
            semester_months = list(range(1, 7))
        else:
            semester_months = list(range(7, 13))
            
        cumulative_leaves_incl_current = 0.0
        for m in semester_months:
            if m <= month:
                cumulative_leaves_incl_current += await self._get_approved_leave_days(faculty_id, m, year)
                
        exceeding_incl_current = max(0.0, cumulative_leaves_incl_current - 10.0)
        
        cumulative_leaves_prev = 0.0
        for m in semester_months:
            if m < month:
                cumulative_leaves_prev += await self._get_approved_leave_days(faculty_id, m, year)
                
        exceeding_prev = max(0.0, cumulative_leaves_prev - 10.0)
        
        # Leave deduction removed per new requirements
        leave_deduction = 0.0
        remaining_leave_balance = 0.0
        
        pf_val = 0.0
        if pf_config:
            if pf_config.calculation_method == PFCalculationMethod.FIXED:
                full_pf = to_float(pf_config.value)
                if total_working_days > 0:
                    pf_val = round(full_pf * working_days / total_working_days, 2)
                else:
                    pf_val = full_pf
            elif pf_config.calculation_method == PFCalculationMethod.PERCENTAGE:
                pf_val = round(prorated_basic * (to_float(pf_config.value) / 100.0), 2)
                
        pf_val = 0.0
        total_deductions = round(pf_val + leave_deduction + absent_deduction, 2)
        net_salary = round(prorated_basic - total_deductions, 2)
        
        from sqlalchemy import func
        pf_sum_q = await self.db.execute(
            select(func.sum(Salary.pf_deduction))
            .where(Salary.faculty_id == faculty_id, Salary.is_deleted.is_(False))
        )
        total_pf_accum = float(pf_sum_q.scalar_one_or_none() or 0.0)
        
        return {
            "faculty_id": faculty_id,
            "faculty_name": fac.full_name,
            "employee_id": fac.id[:8].upper(),
            "designation": "Head of Department" if fac.role.value == "HOD" else "Principal" if fac.role.value == "PRINCIPAL" else "Assistant Professor",
            "joining_date": joining_date,
            "month": month,
            "year": year,
            "working_days": working_days,
            "total_working_days": total_working_days,
            "leave_days": leave_days,
            "absent_days": absent_days,
            "basic": prorated_basic,
            "pf_deduction": pf_val,
            "total_pf_accumulated": total_pf_accum,
            "leave_deduction": leave_deduction,
            "absent_deduction": absent_deduction,
            "total_deductions": total_deductions,
            "net_salary": net_salary,
            "daily_salary_rate": daily_rate,
            "semester_leave_allowed": 10,
            "semester_leave_used": cumulative_leaves_incl_current,
            "remaining_leave_balance": remaining_leave_balance
        }

    async def get_faculty_payrolls(self, faculty_id: str) -> list[dict]:
        salaries = await self.repo.get_salary_by_faculty(faculty_id)
        details = []
        for s in salaries:
            deductions = await self.repo.get_deductions_by_salary(s.id)
            slips = await self.repo.get_slips_by_salary(s.id)
            
            pf_q = await self.db.execute(select(PFContribution).where(PFContribution.salary_id == s.id))
            pf_contrib = pf_q.scalars().first()
            pf_amt = to_float(pf_contrib.amount) if pf_contrib else 0.0
            
            ded_total = sum(to_float(d.amount) for d in deductions) + pf_amt
            net_pay = to_float(s.gross) - ded_total
            
            details.append({
                "salary_id": s.id,
                "basic": s.basic,
                "allowances": s.allowances,
                "gross": s.gross,
                "deductions_total": ded_total,
                "pf_amount": pf_amt,
                "net_pay": net_pay,
                "month": s.month,
                "year": s.year,
                "pdf_url": slips[0].pdf_url if slips else None
            })
        return details

    async def run_monthly_payroll(self, faculty_id: str, month: int, year: int, basic: float, allowances: float, lop_days: int) -> Salary:
        pf_config_q = await self.db.execute(select(PFConfiguration).where(PFConfiguration.faculty_id == faculty_id))
        pf_config = pf_config_q.scalars().first()
        joining_date = pf_config.joining_date if pf_config else None

        preview = await self.get_payroll_preview(faculty_id, month, year)

        gross = basic + allowances
        net_val = round(gross - preview["pf_deduction"] - preview["leave_deduction"] - (lop_days * preview["daily_salary_rate"]), 2)

        # Check if salary record already exists
        exist_q = await self.db.execute(
            select(Salary).where(
                Salary.faculty_id == faculty_id,
                Salary.month == month,
                Salary.year == year,
                Salary.is_deleted.is_(False)
            )
        )
        existing_sal = exist_q.scalars().first()

        if existing_sal:
            sal = existing_sal
            sal.basic = basic
            sal.allowances = allowances
            sal.gross = gross
            sal.employee_id = preview["employee_id"]
            sal.designation = preview["designation"]
            sal.working_days = preview["working_days"]
            sal.leave_days = preview["leave_days"]
            sal.leave_deduction = preview["leave_deduction"]
            sal.pf_deduction = preview["pf_deduction"]
            sal.net_salary = net_val
            sal.joining_date = joining_date
            sal.total_working_days = preview["total_working_days"]
        else:
            sal = Salary(
                faculty_id=faculty_id,
                basic=basic,
                allowances=allowances,
                gross=gross,
                month=month,
                year=year,
                employee_id=preview["employee_id"],
                designation=preview["designation"],
                working_days=preview["working_days"],
                leave_days=preview["leave_days"],
                leave_deduction=preview["leave_deduction"],
                pf_deduction=preview["pf_deduction"],
                net_salary=net_val,
                joining_date=joining_date,
                total_working_days=preview["total_working_days"]
            )
            self.db.add(sal)
            await self.db.flush()
        
        lop_amt = 0.0
        if lop_days > 0:
            lop_amt = round(lop_days * preview["daily_salary_rate"], 2)
            lop_q = await self.db.execute(
                select(Deduction).where(
                    Deduction.salary_id == sal.id,
                    Deduction.type == DeductionType.LOP,
                    Deduction.is_deleted.is_(False)
                )
            )
            lop_ded = lop_q.scalars().first()
            if lop_ded:
                lop_ded.days = lop_days
                lop_ded.amount = lop_amt
            else:
                await self.repo.add_deduction(sal.id, DeductionType.LOP, lop_days, lop_amt)
        else:
            lop_q = await self.db.execute(
                select(Deduction).where(
                    Deduction.salary_id == sal.id,
                    Deduction.type == DeductionType.LOP,
                    Deduction.is_deleted.is_(False)
                )
            )
            lop_ded = lop_q.scalars().first()
            if lop_ded:
                lop_ded.is_deleted = True
            
        # Check SalarySlip
        slip_q = await self.db.execute(
            select(SalarySlip).where(
                SalarySlip.salary_id == sal.id,
                SalarySlip.is_deleted.is_(False)
            )
        )
        slip = slip_q.scalars().first()
        pdf_url = f"/uploads/payroll/salary_slip_{faculty_id}_{year}_{month}.pdf"
        if slip:
            slip.pdf_url = pdf_url
        else:
            await self.repo.create_salary_slip(sal.id, pdf_url)
            
        # Check PFContribution
        pf_contrib_q = await self.db.execute(
            select(PFContribution).where(
                PFContribution.salary_id == sal.id,
                PFContribution.is_deleted.is_(False)
            )
        )
        contrib = pf_contrib_q.scalars().first()
        if preview["pf_deduction"] > 0:
            if contrib:
                contrib.amount = preview["pf_deduction"]
            else:
                contrib = PFContribution(
                    faculty_id=faculty_id,
                    salary_id=sal.id,
                    month=month,
                    year=year,
                    amount=preview["pf_deduction"],
                    employer_amount=0.0,
                    is_historical=False
                )
                self.db.add(contrib)
        elif contrib:
            contrib.is_deleted = True

        return sal

    async def run_bulk_payroll(self, month: int, year: int) -> dict:
        q = await self.db.execute(
            select(User).where(
                User.role.in_([UserRole.FACULTY, UserRole.HOD, UserRole.PRINCIPAL]),
                User.is_deleted.is_(False),
                User.is_active.is_(True)
            )
        )
        faculties = q.scalars().all()
        
        generated_count = 0
        skipped_count = 0
        
        for f in faculties:
            exist_q = await self.db.execute(
                select(Salary).where(
                    Salary.faculty_id == f.id,
                    Salary.month == month,
                    Salary.year == year,
                    Salary.is_deleted.is_(False)
                )
            )
            if exist_q.scalars().first():
                skipped_count += 1
                continue
                
            preview = await self.get_payroll_preview(f.id, month, year)
            
            sal = Salary(
                faculty_id=f.id,
                basic=preview["basic"],
                allowances=0.0,
                gross=preview["basic"],
                month=month,
                year=year,
                employee_id=preview["employee_id"],
                designation=preview["designation"],
                working_days=preview["working_days"],
                leave_days=preview["leave_days"],
                leave_deduction=preview["leave_deduction"],
                pf_deduction=preview["pf_deduction"],
                net_salary=preview["net_salary"],
                joining_date=preview["joining_date"],
                total_working_days=preview["total_working_days"]
            )
            self.db.add(sal)
            await self.db.flush()
            
            if preview["absent_days"] > 0:
                await self.repo.add_deduction(sal.id, DeductionType.LOP, preview["absent_days"], preview["absent_deduction"])
                
            pdf_url = f"/uploads/payroll/salary_slip_{f.id}_{year}_{month}.pdf"
            await self.repo.create_salary_slip(sal.id, pdf_url)
            
            if preview["pf_deduction"] > 0:
                contrib = PFContribution(
                    faculty_id=f.id,
                    salary_id=sal.id,
                    month=month,
                    year=year,
                    amount=preview["pf_deduction"],
                    employer_amount=0.0,
                    is_historical=False
                )
                self.db.add(contrib)
                
            generated_count += 1
            
        await self.db.commit()
        return {"generated": generated_count, "skipped": skipped_count}

    async def _count_working_days_until(self, month: int, year: int, until_day: int, from_day: int = 1) -> int:
        """
        Count working days in the given month from from_day to until_day (inclusive),
        respecting the admin-configured WorkingDayConfig overrides.
        """
        import json as json_module
        
        q = await self.db.execute(
            select(WorkingDayConfig).where(
                WorkingDayConfig.month == month,
                WorkingDayConfig.year == year,
                WorkingDayConfig.is_deleted.is_(False)
            )
        )
        cfg = q.scalars().first()
        overrides = {}
        if cfg and cfg.overrides_json:
            try:
                overrides = json_module.loads(cfg.overrides_json)
            except Exception:
                overrides = {}
        
        count = 0
        for d in range(from_day, min(until_day, cal_module.monthrange(year, month)[1]) + 1):
            date_str = f"{year}-{str(month).zfill(2)}-{str(d).zfill(2)}"
            day_of_week = date(year, month, d).weekday()
            default_status = "holiday" if day_of_week == 6 else "working"  # Sunday=holiday
            
            override_val = overrides.get(date_str, default_status)
            status = override_val if isinstance(override_val, str) else override_val.get("status")
            
            if status in ["working", "event"]:
                count += 1
        return count

    async def generate_historical_payroll_for_faculty(self, faculty_id: str) -> dict:
        f = await self.db.get(User, faculty_id)
        if not f or f.is_deleted:
            return {"detail": "Faculty not found"}
            
        pf_config_q = await self.db.execute(select(PFConfiguration).where(PFConfiguration.faculty_id == faculty_id))
        pf_config = pf_config_q.scalars().first()
        if not pf_config:
            return {"detail": "PF configuration not found (cannot determine DOJ)"}
            
        doj = pf_config.joining_date
        
        today = date.today()
        current_year = today.year
        current_month = today.month
        
        start_year = doj.year
        start_month = doj.month
        
        generated = 0
        skipped = 0
        
        year = start_year
        month = start_month
        
        while (year < current_year) or (year == current_year and month <= current_month):
            exist_q = await self.db.execute(
                select(Salary).where(
                    Salary.faculty_id == faculty_id,
                    Salary.month == month,
                    Salary.year == year,
                    Salary.is_deleted.is_(False)
                )
            )
            if exist_q.scalars().first():
                skipped += 1
            else:
                preview = await self.get_payroll_preview(faculty_id, month, year)
                
                sal = Salary(
                    faculty_id=faculty_id,
                    basic=preview["basic"],
                    allowances=0.0,
                    gross=preview["basic"],
                    month=month,
                    year=year,
                    employee_id=preview["employee_id"],
                    designation=preview["designation"],
                    working_days=preview["working_days"],
                    leave_days=preview["leave_days"],
                    leave_deduction=preview["leave_deduction"],
                    pf_deduction=preview["pf_deduction"],
                    net_salary=preview["net_salary"],
                    joining_date=doj,
                    total_working_days=preview["total_working_days"]
                )
                self.db.add(sal)
                await self.db.flush()
                
                if preview["absent_days"] > 0:
                    await self.repo.add_deduction(sal.id, DeductionType.LOP, preview["absent_days"], preview["absent_deduction"])
                
                pdf_url = f"/uploads/payroll/salary_slip_{faculty_id}_{year}_{month}.pdf"
                await self.repo.create_salary_slip(sal.id, pdf_url)
                
                if preview["pf_deduction"] > 0:
                    contrib = PFContribution(
                        faculty_id=faculty_id,
                        salary_id=sal.id,
                        month=month,
                        year=year,
                        amount=preview["pf_deduction"],
                        employer_amount=0.0,
                        is_historical=False
                    )
                    self.db.add(contrib)
                generated += 1
                
            month += 1
            if month > 12:
                month = 1
                year += 1
                
        await self.db.commit()
        return {"generated": generated, "skipped": skipped}
