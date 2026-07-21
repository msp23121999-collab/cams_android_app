from datetime import datetime

from sqlalchemy import Boolean, DateTime, ForeignKey, String
from sqlalchemy.orm import Mapped, mapped_column

from app.db.base import Base
from app.db.models.mixins import TimestampSoftDeleteMixin


class HallTicket(TimestampSoftDeleteMixin, Base):
    __tablename__ = "hall_tickets"

    student_id: Mapped[str] = mapped_column(ForeignKey("students.id"), nullable=False)
    exam_id: Mapped[str | None] = mapped_column(ForeignKey("exams.id"), nullable=True)
    exam_name: Mapped[str] = mapped_column(String(255), nullable=False)
    is_eligible: Mapped[bool] = mapped_column(Boolean, default=True, server_default="true", nullable=False)
    ineligibility_reason: Mapped[str | None] = mapped_column(String(1024), nullable=True)
    is_issued: Mapped[bool] = mapped_column(Boolean, default=False, server_default="false", nullable=False)
    file_url: Mapped[str | None] = mapped_column(String(512), nullable=True)
    issued_at: Mapped[datetime | None] = mapped_column(DateTime(timezone=True), nullable=True)

    # Extra fields used by the existing Android HallTicketDto
    exam_center: Mapped[str | None] = mapped_column(String(255), nullable=True)
    exam_date: Mapped[str | None] = mapped_column(String(64), nullable=True)
    student_signature_url: Mapped[str | None] = mapped_column(String(512), nullable=True)
    principal_signature_url: Mapped[str | None] = mapped_column(String(512), nullable=True)
    coe_signature_url: Mapped[str | None] = mapped_column(String(512), nullable=True)
