import enum
from datetime import date, datetime

from sqlalchemy import Date, DateTime, Enum, ForeignKey, Numeric, String
from sqlalchemy.orm import Mapped, mapped_column

from app.db.base import Base
from app.db.models.mixins import TimestampSoftDeleteMixin


class FeeStatus(str, enum.Enum):
    PAID = "paid"
    PENDING = "pending"
    OVERDUE = "overdue"
    # A record with payments against it that do not yet cover the balance.
    # The fee summary service has always been able to *report* "partially_paid",
    # but the stored status could not represent it, so a partly-settled record had
    # to sit in PENDING and the two views of the same record disagreed.
    PARTIALLY_PAID = "partially_paid"


class FeeStructure(TimestampSoftDeleteMixin, Base):
    __tablename__ = "fee_structure"

    dept_id: Mapped[str] = mapped_column(ForeignKey("departments.id"), nullable=False)
    semester: Mapped[int] = mapped_column(nullable=False)
    amount: Mapped[float] = mapped_column(Numeric(12, 2), nullable=False)
    due_date: Mapped[date] = mapped_column(Date, nullable=False)
    fee_type: Mapped[str] = mapped_column(String(64), nullable=False)


class FeeRecord(TimestampSoftDeleteMixin, Base):
    __tablename__ = "fee_records"

    student_id: Mapped[str] = mapped_column(ForeignKey("students.id"), nullable=False)
    fee_structure_id: Mapped[str] = mapped_column(ForeignKey("fee_structure.id"), nullable=False)
    status: Mapped[FeeStatus] = mapped_column(Enum(FeeStatus, name="fee_status"), nullable=False)


class Payment(TimestampSoftDeleteMixin, Base):
    __tablename__ = "payments"

    fee_record_id: Mapped[str] = mapped_column(ForeignKey("fee_records.id"), nullable=False)
    amount: Mapped[float] = mapped_column(Numeric(12, 2), nullable=False)
    mode: Mapped[str] = mapped_column(String(32), nullable=False)
    txn_id: Mapped[str] = mapped_column(String(128), unique=True, nullable=False)
    receipt_url: Mapped[str | None] = mapped_column(String(512), nullable=True)
    paid_at: Mapped[datetime] = mapped_column(DateTime(timezone=True), nullable=False)

    # Razorpay integration fields
    razorpay_order_id: Mapped[str | None] = mapped_column(String(128), nullable=True)
    razorpay_payment_id: Mapped[str | None] = mapped_column(String(128), nullable=True)
    razorpay_signature: Mapped[str | None] = mapped_column(String(256), nullable=True)
    status: Mapped[str] = mapped_column(String(32), default="created", server_default="created", nullable=False)
