import enum

from sqlalchemy import Enum, ForeignKey, Numeric, String
from sqlalchemy.orm import Mapped, mapped_column

from app.db.base import Base
from app.db.models.mixins import TimestampSoftDeleteMixin


class MarkExamType(str, enum.Enum):
    CIA = "CIA"
    SEMESTER = "semester"


class Mark(TimestampSoftDeleteMixin, Base):
    __tablename__ = "marks"

    student_id: Mapped[str] = mapped_column(ForeignKey("students.id"), nullable=False)
    section_id: Mapped[str] = mapped_column(ForeignKey("sections.id"), nullable=False)
    exam_type: Mapped[MarkExamType] = mapped_column(Enum(MarkExamType, name="mark_exam_type"), nullable=False)
    mark: Mapped[float] = mapped_column(Numeric(5, 2), nullable=False)
    max_mark: Mapped[float] = mapped_column(Numeric(5, 2), nullable=False)


class InternalMark(TimestampSoftDeleteMixin, Base):
    __tablename__ = "internal_marks"

    student_id: Mapped[str] = mapped_column(ForeignKey("students.id"), nullable=False)
    section_id: Mapped[str] = mapped_column(ForeignKey("sections.id"), nullable=False)
    subject_id: Mapped[str] = mapped_column(ForeignKey("courses.id"), nullable=False)
    academic_year: Mapped[str] = mapped_column(String(32), nullable=False)
    semester: Mapped[str | None] = mapped_column(String(32), nullable=True)
    
    internal_exam_mark: Mapped[float] = mapped_column(Numeric(5, 2), default=0.0)
    assignment_mark: Mapped[float] = mapped_column(Numeric(5, 2), default=0.0)
    presentation_mark: Mapped[float] = mapped_column(Numeric(5, 2), default=0.0)
    viva_voice_mark: Mapped[float] = mapped_column(Numeric(5, 2), default=0.0)
    attendance_mark: Mapped[float] = mapped_column(Numeric(5, 2), default=0.0)
    total_mark: Mapped[float] = mapped_column(Numeric(5, 2), default=0.0)
    status: Mapped[str] = mapped_column(String(32), default="DRAFT")
    
    hod_message: Mapped[str | None] = mapped_column(String(1024), nullable=True)
    faculty_reply: Mapped[str | None] = mapped_column(String(1024), nullable=True)
    is_message_visible_to_student: Mapped[bool] = mapped_column(default=False)

