from datetime import date
from sqlalchemy import Date, ForeignKey, Integer, String, Text, JSON, Boolean
from sqlalchemy.orm import Mapped, mapped_column

from app.db.base import Base
from app.db.models.mixins import TimestampSoftDeleteMixin


class ClassroomActivity(TimestampSoftDeleteMixin, Base):
    __tablename__ = "classroom_activities"

    faculty_id: Mapped[str] = mapped_column(ForeignKey("users.id"), nullable=False)
    section_id: Mapped[str] = mapped_column(ForeignKey("sections.id"), nullable=False)
    activity_type: Mapped[str] = mapped_column(String(64), nullable=False)  # e.g., "Lecture", "Moot Court"
    topic: Mapped[str] = mapped_column(String(255), nullable=False)
    duration_minutes: Mapped[int] = mapped_column(Integer, nullable=False)
    remarks: Mapped[str | None] = mapped_column(Text, nullable=True)


class StudentInteraction(TimestampSoftDeleteMixin, Base):
    __tablename__ = "student_interactions"

    faculty_id: Mapped[str] = mapped_column(ForeignKey("users.id"), nullable=False)
    section_id: Mapped[str] = mapped_column(ForeignKey("sections.id"), nullable=False)
    type: Mapped[str] = mapped_column(String(32), nullable=False)  # "QUESTION" or "POLL"
    question_text: Mapped[str] = mapped_column(Text, nullable=False)
    options: Mapped[list | None] = mapped_column(JSON, nullable=True)  # List of string options for POLL
    responses_count: Mapped[int] = mapped_column(Integer, default=0, nullable=False)
    is_active: Mapped[bool] = mapped_column(Boolean, default=True, nullable=False)


class SessionSummary(TimestampSoftDeleteMixin, Base):
    __tablename__ = "session_summaries"

    faculty_id: Mapped[str] = mapped_column(ForeignKey("users.id"), nullable=False)
    section_id: Mapped[str] = mapped_column(ForeignKey("sections.id"), nullable=False)
    subject_code: Mapped[str] = mapped_column(String(32), nullable=False)
    topic_covered: Mapped[str] = mapped_column(String(255), nullable=False)
    subtopic_covered: Mapped[str | None] = mapped_column(String(255), nullable=True)
    teaching_method: Mapped[str] = mapped_column(String(128), nullable=False)  # e.g., "Case Discussion"
    resources_used: Mapped[list | None] = mapped_column(JSON, nullable=True)  # List of strings
    remarks: Mapped[str | None] = mapped_column(Text, nullable=True)
    date: Mapped[date] = mapped_column(Date, default=date.today, nullable=False)
