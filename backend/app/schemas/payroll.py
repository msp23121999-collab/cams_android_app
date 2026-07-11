from datetime import date, datetime
from pydantic import BaseModel

class SalarySlipRequestCreate(BaseModel):
    request_type: str  # "Salary Slip", "Duplicate Salary Slip", "Salary Certificate", "Salary Correction"
    month: int
    year: int
    remarks: str | None = None

class SalarySlipRequestResponse(BaseModel):
    id: str
    faculty_id: str
    faculty_name: str | None = None
    request_type: str
    month: int
    year: int
    remarks: str | None = None
    status: str
    admin_remarks: str | None = None
    salary_slip_id: str | None = None
    created_at: datetime
    updated_at: datetime

    class Config:
        from_attributes = True

class SalarySlipRequestUpdate(BaseModel):
    status: str | None = None  # "APPROVED" or "REJECTED"
    admin_remarks: str | None = None

class DeductionDetail(BaseModel):
    type: str
    days: int
    amount: float

class SalarySlipDetailedResponse(BaseModel):
    salary_id: str
    faculty_name: str
    faculty_role: str
    department_name: str
    email: str
    basic: float
    allowances: float
    gross: float
    deductions_total: float
    pf_amount: float
    net_pay: float
    month: int
    year: int
    pdf_url: str | None
    deductions: list[DeductionDetail] = []
    working_days: int = 30
    total_working_days: int = 30
    leave_days: int = 0
    absent_days: int = 0
    absent_deduction: float = 0.0
    daily_salary_rate: float = 0.0
    semester_leave_allowed: int = 10
    semester_leave_used: float = 0.0
    remaining_leave_balance: float = 10.0
    employee_id: str | None = None
    designation: str | None = None
    joining_date: date | None = None



class AdminSalarySlipCreate(BaseModel):
    faculty_id: str
    employee_id: str | None = None
    designation: str | None = None
    month: int
    year: int
    working_days: int = 30
    total_working_days: int = 30
    leave_days: int = 0
    basic: float
    pf_deduction: float = 0.0
    joining_date: date | None = None
    leave_deduction: float | None = None
    net_salary: float | None = None
    absent_days: int = 0
    absent_deduction: float = 0.0


class AdminSalarySlipResponse(BaseModel):
    id: str
    faculty_id: str
    faculty_name: str
    employee_id: str | None = None
    department_name: str | None = None
    designation: str | None = None
    joining_date: date | None = None
    month: int
    year: int
    working_days: int
    total_working_days: int = 30
    leave_days: int
    basic: float
    pf_deduction: float
    total_pf_accumulated: float
    leave_deduction: float
    total_deductions: float
    net_salary: float
    pdf_url: str | None = None
    absent_days: int = 0
    absent_deduction: float = 0.0
    daily_salary_rate: float = 0.0
    semester_leave_allowed: int = 10
    semester_leave_used: float = 0.0
    remaining_leave_balance: float = 10.0
    created_at: datetime

    class Config:
        from_attributes = True


class AdminSalarySlipUpdate(BaseModel):
    employee_id: str | None = None
    designation: str | None = None
    working_days: int
    leave_days: int
    basic: float
    pf_deduction: float
    joining_date: date | None = None
    leave_deduction: float | None = None
    net_salary: float | None = None
    absent_days: int = 0
    absent_deduction: float = 0.0
