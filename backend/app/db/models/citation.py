from sqlalchemy import ForeignKey, String, Text
from sqlalchemy.orm import Mapped, mapped_column

from app.db.base import Base
from app.db.models.mixins import TimestampSoftDeleteMixin


class SavedCitation(TimestampSoftDeleteMixin, Base):
    __tablename__ = "saved_citations"

    student_id: Mapped[str] = mapped_column(ForeignKey("students.id"), nullable=False)
    case_name: Mapped[str] = mapped_column(String(255), nullable=False)
    citation_text: Mapped[str] = mapped_column(String(255), nullable=False)
    note: Mapped[str | None] = mapped_column(Text, nullable=True)
