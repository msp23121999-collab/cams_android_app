from datetime import date, datetime

from sqlalchemy import Date, DateTime, Float, ForeignKey, Integer, String
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
    submission_count: Mapped[int] = mapped_column(Integer, default=0, server_default="0", nullable=False)

    # Expanded fields to support the full assignment feature set (moved off the
    # old JSON-blob storage).
    type: Mapped[str | None] = mapped_column(String(64), nullable=True)
    subject: Mapped[str | None] = mapped_column(String(255), nullable=True)
    unit: Mapped[str | None] = mapped_column(String(128), nullable=True)
    topic: Mapped[str | None] = mapped_column(String(255), nullable=True)
    description: Mapped[str | None] = mapped_column(String(4000), nullable=True)
    instructions: Mapped[str | None] = mapped_column(String(4000), nullable=True)
    total_marks: Mapped[int | None] = mapped_column(Integer, nullable=True)
    status: Mapped[str] = mapped_column(String(32), default="Draft", server_default="Draft", nullable=False)
    semester: Mapped[str | None] = mapped_column(String(32), nullable=True)
    section: Mapped[str | None] = mapped_column(String(64), nullable=True)
    attachments: Mapped[str | None] = mapped_column(String(4000), nullable=True)  # JSON-encoded list


class AssignmentSubmission(TimestampSoftDeleteMixin, Base):
    __tablename__ = "assignment_submissions"

    assignment_id: Mapped[str] = mapped_column(ForeignKey("assignments.id"), nullable=False)
    student_id: Mapped[str] = mapped_column(ForeignKey("students.id"), nullable=False)
    submitted_file_url: Mapped[str | None] = mapped_column(String(512), nullable=True)
    submitted_text: Mapped[str | None] = mapped_column(String(4000), nullable=True)
    marks_obtained: Mapped[float | None] = mapped_column(Float, nullable=True)
    grade: Mapped[str | None] = mapped_column(String(16), nullable=True)
    feedback: Mapped[str | None] = mapped_column(String(2048), nullable=True)
    remarks: Mapped[str | None] = mapped_column(String(2048), nullable=True)
    status: Mapped[str] = mapped_column(String(32), default="Submitted", server_default="Submitted", nullable=False)
    submitted_at: Mapped[datetime] = mapped_column(DateTime(timezone=True), nullable=False)
