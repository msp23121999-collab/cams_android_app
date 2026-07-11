import enum
from datetime import date, datetime
from sqlalchemy import Boolean, Date, DateTime, Enum, ForeignKey, Integer, Numeric, String, Text
from sqlalchemy.orm import Mapped, mapped_column

from app.db.base import Base
from app.db.models.mixins import TimestampSoftDeleteMixin

class PFCalculationMethod(str, enum.Enum):
    FIXED = "FIXED"
    PERCENTAGE = "PERCENTAGE"

class PFConfiguration(TimestampSoftDeleteMixin, Base):
    __tablename__ = "pf_configurations"

    faculty_id: Mapped[str] = mapped_column(ForeignKey("users.id"), unique=True, nullable=False)
    joining_date: Mapped[date] = mapped_column(Date, nullable=False)
    pf_start_date: Mapped[date] = mapped_column(Date, nullable=False)
    historical_opening_balance: Mapped[float] = mapped_column(Numeric(12, 2), default=0.00, nullable=False)
    calculation_method: Mapped[PFCalculationMethod] = mapped_column(Enum(PFCalculationMethod, name="pf_calculation_method"), default=PFCalculationMethod.FIXED, nullable=False)
    value: Mapped[float] = mapped_column(Numeric(12, 2), default=0.00, nullable=False) # fixed amount or percentage rate
    based_on_earned_salary: Mapped[bool] = mapped_column(Boolean, default=False, nullable=False)
    basic_salary: Mapped[float] = mapped_column(Numeric(12, 2), default=0.00, nullable=False)  # stored basic salary for PERCENTAGE calc

class PFHistoricalPeriod(TimestampSoftDeleteMixin, Base):
    __tablename__ = "pf_historical_periods"

    faculty_id: Mapped[str] = mapped_column(ForeignKey("users.id"), nullable=False)
    from_date: Mapped[date] = mapped_column(Date, nullable=False)
    to_date: Mapped[date] = mapped_column(Date, nullable=False)
    amount_per_month: Mapped[float] = mapped_column(Numeric(12, 2), nullable=False)
    months: Mapped[int] = mapped_column(Integer, nullable=False)
    total_amount: Mapped[float] = mapped_column(Numeric(12, 2), nullable=False)

class PFContribution(TimestampSoftDeleteMixin, Base):
    __tablename__ = "pf_contributions"

    faculty_id: Mapped[str] = mapped_column(ForeignKey("users.id"), nullable=False)
    salary_id: Mapped[str | None] = mapped_column(ForeignKey("salary.id"), nullable=True)
    month: Mapped[int] = mapped_column(Integer, nullable=False)
    year: Mapped[int] = mapped_column(Integer, nullable=False)
    amount: Mapped[float] = mapped_column(Numeric(12, 2), nullable=False)
    employer_amount: Mapped[float] = mapped_column(Numeric(12, 2), nullable=False)
    is_historical: Mapped[bool] = mapped_column(Boolean, default=False, nullable=False)

class PFClaim(TimestampSoftDeleteMixin, Base):
    __tablename__ = "pf_claims"

    faculty_id: Mapped[str] = mapped_column(ForeignKey("users.id"), nullable=False)
    claim_date: Mapped[date] = mapped_column(Date, nullable=False)
    amount: Mapped[float] = mapped_column(Numeric(12, 2), nullable=False)
    reference_number: Mapped[str] = mapped_column(String(128), nullable=False)
    remarks: Mapped[str | None] = mapped_column(Text, nullable=True)

class PFAuditLog(TimestampSoftDeleteMixin, Base):
    __tablename__ = "pf_audit_logs"

    faculty_id: Mapped[str] = mapped_column(ForeignKey("users.id"), nullable=False)
    action: Mapped[str] = mapped_column(String(255), nullable=False)
    details: Mapped[str | None] = mapped_column(Text, nullable=True)
    performed_by: Mapped[str] = mapped_column(ForeignKey("users.id"), nullable=False)

class PFLeaveExclusion(TimestampSoftDeleteMixin, Base):
    __tablename__ = "pf_leave_exclusions"

    faculty_id: Mapped[str] = mapped_column(ForeignKey("users.id"), nullable=False)
    from_date: Mapped[date] = mapped_column(Date, nullable=False)
    to_date: Mapped[date] = mapped_column(Date, nullable=False)
    reason: Mapped[str | None] = mapped_column(String(255), nullable=True)

