from datetime import date

from sqlalchemy import Date, ForeignKey, Numeric, String
from sqlalchemy.orm import Mapped, mapped_column

from app.db.base import Base
from app.db.models.mixins import TimestampSoftDeleteMixin


class StudentLoan(TimestampSoftDeleteMixin, Base):
    """Self-declared education loan record a student keeps on file with the institution."""
    __tablename__ = "student_loans"

    student_id: Mapped[str] = mapped_column(ForeignKey("students.id"), nullable=False)
    bank: Mapped[str] = mapped_column(String(128), nullable=False)
    branch: Mapped[str] = mapped_column(String(128), nullable=False)
    sanctioned: Mapped[float] = mapped_column(Numeric(12, 2), nullable=False)
    interest_rate: Mapped[float] = mapped_column(Numeric(5, 2), nullable=False)
    emi: Mapped[float] = mapped_column(Numeric(12, 2), nullable=False)
    outstanding: Mapped[float] = mapped_column(Numeric(12, 2), nullable=False)
    status: Mapped[str] = mapped_column(String(32), default="ACTIVE", server_default="ACTIVE", nullable=False)


class FinancialAssistanceRequest(TimestampSoftDeleteMixin, Base):
    """Student-submitted fee concession / financial aid request awaiting admin review."""
    __tablename__ = "financial_assistance_requests"

    student_id: Mapped[str] = mapped_column(ForeignKey("students.id"), nullable=False)
    type: Mapped[str] = mapped_column(String(64), nullable=False)
    reason: Mapped[str] = mapped_column(String(2048), nullable=False)
    status: Mapped[str] = mapped_column(String(32), default="PENDING", server_default="PENDING", nullable=False)
    admin_remarks: Mapped[str | None] = mapped_column(String(1024), nullable=True)
