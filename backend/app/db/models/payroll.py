import enum
from datetime import date, datetime

from sqlalchemy import Date, DateTime, Enum, ForeignKey, Integer, Numeric, String, Text, UniqueConstraint
from sqlalchemy.orm import Mapped, mapped_column

from app.db.base import Base
from app.db.models.mixins import TimestampSoftDeleteMixin


class DeductionType(str, enum.Enum):
    LOP = "LOP"
    CL = "CL"
    PF = "PF"


class Salary(TimestampSoftDeleteMixin, Base):
    __tablename__ = "salary"

    faculty_id: Mapped[str] = mapped_column(ForeignKey("users.id"), nullable=False)
    basic: Mapped[float] = mapped_column(Numeric(12, 2), nullable=False)
    allowances: Mapped[float] = mapped_column(Numeric(12, 2), nullable=False)
    gross: Mapped[float] = mapped_column(Numeric(12, 2), nullable=False)
    month: Mapped[int] = mapped_column(Integer, nullable=False)
    year: Mapped[int] = mapped_column(Integer, nullable=False)
    
    # New columns for Salary Slip module
    employee_id: Mapped[str | None] = mapped_column(String(64), nullable=True)
    designation: Mapped[str | None] = mapped_column(String(128), nullable=True)
    working_days: Mapped[int] = mapped_column(Integer, default=30, nullable=False, server_default='30')
    leave_days: Mapped[int] = mapped_column(Integer, default=0, nullable=False, server_default='0')


    net_salary: Mapped[float] = mapped_column(Numeric(12, 2), default=0.0, nullable=False, server_default='0.0')
    leave_deduction: Mapped[float | None] = mapped_column(Numeric(12, 2), nullable=True)
    pf_deduction: Mapped[float | None] = mapped_column(Numeric(12, 2), nullable=True)
    joining_date: Mapped[date | None] = mapped_column(Date, nullable=True)
    # Total configured working days for the month (from admin calendar setup)
    total_working_days: Mapped[int] = mapped_column(Integer, default=30, nullable=False, server_default='30')


class Deduction(TimestampSoftDeleteMixin, Base):
    __tablename__ = "deductions"

    salary_id: Mapped[str] = mapped_column(ForeignKey("salary.id"), nullable=False)
    type: Mapped[DeductionType] = mapped_column(Enum(DeductionType, name="deduction_type"), nullable=False)
    days: Mapped[int] = mapped_column(Integer, nullable=False)
    amount: Mapped[float] = mapped_column(Numeric(12, 2), nullable=False)


class SalarySlip(TimestampSoftDeleteMixin, Base):
    __tablename__ = "salary_slips"

    salary_id: Mapped[str] = mapped_column(ForeignKey("salary.id"), nullable=False)
    pdf_url: Mapped[str] = mapped_column(String(512), nullable=False)
    generated_at: Mapped[datetime] = mapped_column(DateTime(timezone=True), nullable=False)
    delivered_at: Mapped[datetime | None] = mapped_column(DateTime(timezone=True), nullable=True)


class SalarySlipRequest(TimestampSoftDeleteMixin, Base):
    __tablename__ = "salary_slip_requests"

    faculty_id: Mapped[str] = mapped_column(ForeignKey("users.id"), nullable=False)
    request_type: Mapped[str] = mapped_column(String(64), nullable=False)  # "Salary Slip", "Duplicate Salary Slip", "Salary Certificate", "Salary Correction"
    month: Mapped[int] = mapped_column(Integer, nullable=False)
    year: Mapped[int] = mapped_column(Integer, nullable=False)
    remarks: Mapped[str | None] = mapped_column(String(1024), nullable=True)
    status: Mapped[str] = mapped_column(String(32), default="PENDING", nullable=False)  # "PENDING", "APPROVED", "REJECTED"
    admin_remarks: Mapped[str | None] = mapped_column(String(1024), nullable=True)
    salary_slip_id: Mapped[str | None] = mapped_column(ForeignKey("salary_slips.id"), nullable=True)


class WorkingDayConfig(TimestampSoftDeleteMixin, Base):
    """
    Stores working day overrides per month+year as configured by admin.
    overrides_json is a JSON string of {"YYYY-MM-DD": "working"|"holiday"} entries.
    total_working_days is the pre-computed count of working days for the month.
    """
    __tablename__ = "working_day_config"
    __table_args__ = (
        UniqueConstraint("month", "year", name="uq_working_day_config_month_year"),
    )

    month: Mapped[int] = mapped_column(Integer, nullable=False)
    year: Mapped[int] = mapped_column(Integer, nullable=False)
    total_working_days: Mapped[int] = mapped_column(Integer, nullable=False, default=26)
    overrides_json: Mapped[str | None] = mapped_column(Text, nullable=True)  # JSON string
