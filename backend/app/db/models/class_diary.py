from sqlalchemy import ForeignKey, String
from sqlalchemy.orm import Mapped, mapped_column

from app.db.base import Base
from app.db.models.mixins import TimestampSoftDeleteMixin


class ClassDiary(TimestampSoftDeleteMixin, Base):
    """Real SQL-backed class diary entry.

    Mirrors the field set previously stored in the JSON `class_diaries` blob
    (see app/api/v1/endpoints/teaching_logs.py). Written on every diary
    create/update alongside the JSON store so existing JSON-driven read
    endpoints (fuzzy subject/unit/topic matching for syllabus tracking) keep
    working unchanged; full migration of those read paths is deferred to the
    Faculty Class Diary / HOD Class Diary screens, where the matching logic
    itself will be redesigned around real foreign keys.
    """

    __tablename__ = "class_diaries"

    faculty_id: Mapped[str] = mapped_column(ForeignKey("users.id"), nullable=False)
    date: Mapped[str] = mapped_column(String(16), nullable=False)
    subject: Mapped[str] = mapped_column(String(255), nullable=False)
    course: Mapped[str | None] = mapped_column(String(255), nullable=True)
    semester: Mapped[str | None] = mapped_column(String(32), nullable=True)
    section: Mapped[str | None] = mapped_column(String(64), nullable=True)
    hour: Mapped[str | None] = mapped_column(String(32), nullable=True)
    year: Mapped[str | None] = mapped_column(String(16), nullable=True)
    unit: Mapped[str | None] = mapped_column(String(255), nullable=True)
    topic: Mapped[str | None] = mapped_column(String(1024), nullable=True)
    subtopic: Mapped[str | None] = mapped_column(String(1024), nullable=True)
    teaching_method: Mapped[str | None] = mapped_column(String(255), nullable=True)
    learning_outcome: Mapped[str | None] = mapped_column(String(2048), nullable=True)
    class_activity: Mapped[str | None] = mapped_column(String(2048), nullable=True)
    remarks: Mapped[str | None] = mapped_column(String(2048), nullable=True)
    status: Mapped[str] = mapped_column(String(32), default="Draft", server_default="Draft", nullable=False)
    deviation_reason: Mapped[str | None] = mapped_column(String(1024), nullable=True)
    revised_date: Mapped[str | None] = mapped_column(String(16), nullable=True)
    attachment_url: Mapped[str | None] = mapped_column(String(512), nullable=True)
    attachment_name: Mapped[str | None] = mapped_column(String(255), nullable=True)
    completion_status: Mapped[str | None] = mapped_column(String(32), default="Completed", server_default="Completed", nullable=True)

    # Denormalized JSON-store id so we can keep the JSON blob and this table
    # in sync (same id used on both sides) during the dual-write period.
    json_entry_id: Mapped[str | None] = mapped_column(String(64), nullable=True, unique=True)
