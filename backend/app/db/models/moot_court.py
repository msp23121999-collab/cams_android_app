from sqlalchemy import ForeignKey, String, Text
from sqlalchemy.orm import Mapped, mapped_column

from app.db.base import Base
from app.db.models.mixins import TimestampSoftDeleteMixin


class MootCourtMemorial(TimestampSoftDeleteMixin, Base):
    __tablename__ = "moot_court_memorials"

    student_id: Mapped[str] = mapped_column(ForeignKey("students.id"), nullable=False)
    title: Mapped[str] = mapped_column(String(255), nullable=False)
    case_name: Mapped[str | None] = mapped_column(String(255), nullable=True)
    content: Mapped[str] = mapped_column(Text, nullable=False)
    status: Mapped[str] = mapped_column(String(32), default="draft", server_default="draft", nullable=False)
