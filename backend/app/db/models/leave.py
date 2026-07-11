import enum
from datetime import date, datetime

from sqlalchemy import Date, Enum, ForeignKey, String, Float, DateTime
from sqlalchemy.orm import Mapped, mapped_column, relationship

from app.db.base import Base
from app.db.models.mixins import TimestampSoftDeleteMixin


class LeaveStatus(str, enum.Enum):
    # ── Legacy / General ────────────────────────────────────────────────
    SUBMITTED = "SUBMITTED"
    UNDER_REVIEW = "UNDER_REVIEW"
    PENDING = "PENDING"
    APPROVED = "APPROVED"
    REJECTED = "REJECTED"
    RESUBMISSION_REQUIRED = "RESUBMISSION_REQUIRED"

    # ── Multi-Level Workflow (Faculty → HOD → Principal) ────────────────
    PENDING_HOD = "PENDING_HOD"
    PENDING_PRINCIPAL = "PENDING_PRINCIPAL"
    APPROVED_BY_HOD = "APPROVED_BY_HOD"
    REJECTED_BY_HOD = "REJECTED_BY_HOD"
    FINAL_APPROVED = "FINAL_APPROVED"
    REJECTED_BY_PRINCIPAL = "REJECTED_BY_PRINCIPAL"

    # ── Transitional / Compatibility mappings ────────────────────────────
    HOD_APPROVED = "HOD_APPROVED"
    HOD_REJECTED = "HOD_REJECTED"
    PRINCIPAL_APPROVED = "PRINCIPAL_APPROVED"
    PRINCIPAL_REJECTED = "PRINCIPAL_REJECTED"

    # ── Legacy advisor/student flow ─────────────────────────────────────
    FACULTY_APPROVED = "FACULTY_APPROVED"
    ADVISOR_APPROVED = "ADVISOR_APPROVED"
    REJECTED_BY_FACULTY = "REJECTED_BY_FACULTY"
    REJECTED_BY_ADVISOR = "REJECTED_BY_ADVISOR"


class LeaveRequest(TimestampSoftDeleteMixin, Base):
    __tablename__ = "leaves"

    user_id: Mapped[str] = mapped_column(ForeignKey("users.id"), nullable=False)
    type: Mapped[str] = mapped_column(String(64), nullable=False)
    from_date: Mapped[date] = mapped_column(Date, nullable=False)
    to_date: Mapped[date] = mapped_column(Date, nullable=False)
    num_days: Mapped[float] = mapped_column(Float, default=1.0, nullable=False)
    reason: Mapped[str] = mapped_column(String(1024), nullable=False)
    emergency_contact: Mapped[str] = mapped_column(String(32), nullable=False)
    attachment_url: Mapped[str | None] = mapped_column(String(512), nullable=True)
    status: Mapped[LeaveStatus] = mapped_column(
        Enum(LeaveStatus, name="leave_status"),
        default=LeaveStatus.PENDING_HOD,
        nullable=False
    )

    # ── HOD Stage ───────────────────────────────────────────────────────
    hod_status: Mapped[str | None] = mapped_column(String(32), nullable=True)
    hod_action_by: Mapped[str | None] = mapped_column(String(36), nullable=True)
    hod_action_date: Mapped[datetime | None] = mapped_column(DateTime, nullable=True)
    hod_remarks: Mapped[str | None] = mapped_column(String(1024), nullable=True)

    # ── Principal Stage ─────────────────────────────────────────────────
    principal_action_by: Mapped[str | None] = mapped_column(String(36), nullable=True)
    principal_action_date: Mapped[datetime | None] = mapped_column(DateTime, nullable=True)
    principal_remarks: Mapped[str | None] = mapped_column(String(1024), nullable=True)

    user = relationship("User", foreign_keys=[user_id])


class LeaveApproval(TimestampSoftDeleteMixin, Base):
    __tablename__ = "leave_approvals"

    leave_id: Mapped[str] = mapped_column(ForeignKey("leaves.id"), nullable=False)
    approved_by: Mapped[str] = mapped_column(ForeignKey("users.id"), nullable=False)
    status: Mapped[LeaveStatus] = mapped_column(Enum(LeaveStatus, name="leave_approval_status"), nullable=False)
    remarks: Mapped[str | None] = mapped_column(String(1024), nullable=True)

    leave = relationship("LeaveRequest", foreign_keys=[leave_id])
    approver = relationship("User", foreign_keys=[approved_by])


class LeaveBalance(TimestampSoftDeleteMixin, Base):
    __tablename__ = "leave_balances"

    user_id: Mapped[str] = mapped_column(ForeignKey("users.id"), unique=True, nullable=False)
    casual_leave: Mapped[float] = mapped_column(Float, default=10.0, nullable=False)
    sick_leave: Mapped[float] = mapped_column(Float, default=5.0, nullable=False)
    earned_leave: Mapped[float] = mapped_column(Float, default=12.0, nullable=False)
    on_duty_leave: Mapped[float] = mapped_column(Float, default=10.0, nullable=False)

    user = relationship("User", foreign_keys=[user_id])
