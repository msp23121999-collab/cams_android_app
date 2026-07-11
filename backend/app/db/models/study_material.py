from datetime import date

from sqlalchemy import Date, ForeignKey, Integer, String
from sqlalchemy.orm import Mapped, mapped_column

from app.db.base import Base
from app.db.models.mixins import TimestampSoftDeleteMixin


class StudyMaterial(TimestampSoftDeleteMixin, Base):
    __tablename__ = "study_materials"

    section_id: Mapped[str] = mapped_column(ForeignKey("sections.id"), nullable=False)
    faculty_id: Mapped[str] = mapped_column(ForeignKey("users.id"), nullable=False)
    title: Mapped[str] = mapped_column(String(255), nullable=False)
    type: Mapped[str] = mapped_column(String(64), nullable=False)
    file_url: Mapped[str] = mapped_column(String(512), nullable=False)
    is_verified: Mapped[bool] = mapped_column(default=False, nullable=False)
    status: Mapped[str] = mapped_column(String(32), default="PENDING", nullable=False)
    comments: Mapped[str | None] = mapped_column(String(1024), nullable=True)

    # Approval history tracking
    approved_by: Mapped[str | None] = mapped_column(ForeignKey("users.id"), nullable=True)
    approved_date: Mapped[str | None] = mapped_column(String(64), nullable=True)
    rejected_by: Mapped[str | None] = mapped_column(ForeignKey("users.id"), nullable=True)
    rejected_date: Mapped[str | None] = mapped_column(String(64), nullable=True)
    rejection_remarks: Mapped[str | None] = mapped_column(String(2048), nullable=True)


class Assignment(TimestampSoftDeleteMixin, Base):
    __tablename__ = "assignments"

    section_id: Mapped[str] = mapped_column(ForeignKey("sections.id"), nullable=False)
    faculty_id: Mapped[str] = mapped_column(ForeignKey("users.id"), nullable=False)
    title: Mapped[str] = mapped_column(String(255), nullable=False)
    deadline: Mapped[date] = mapped_column(Date, nullable=False)
    submission_count: Mapped[int] = mapped_column(Integer, default=0, nullable=False)
