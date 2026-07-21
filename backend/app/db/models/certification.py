from sqlalchemy import ForeignKey, String
from sqlalchemy.orm import Mapped, mapped_column

from app.db.base import Base
from app.db.models.mixins import TimestampSoftDeleteMixin


class Certification(TimestampSoftDeleteMixin, Base):
    __tablename__ = "certifications"

    student_id: Mapped[str] = mapped_column(ForeignKey("students.id"), nullable=False)
    title: Mapped[str] = mapped_column(String(255), nullable=False)
    issuer: Mapped[str] = mapped_column(String(255), nullable=False)
    date: Mapped[str] = mapped_column(String(32), nullable=False)
    category: Mapped[str] = mapped_column(String(64), nullable=False)
    type: Mapped[str] = mapped_column(String(32), default="training", server_default="training", nullable=False)
    is_verified: Mapped[bool] = mapped_column(default=False, server_default="0", nullable=False)
    file_url: Mapped[str | None] = mapped_column(String(512), nullable=True)
