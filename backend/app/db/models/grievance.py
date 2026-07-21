from sqlalchemy import ForeignKey, String
from sqlalchemy.orm import Mapped, mapped_column

from app.db.base import Base
from app.db.models.mixins import TimestampSoftDeleteMixin


class Grievance(TimestampSoftDeleteMixin, Base):
    __tablename__ = "grievances"

    raised_by: Mapped[str] = mapped_column(ForeignKey("users.id"), nullable=False)
    category: Mapped[str] = mapped_column(String(128), nullable=False)
    subject: Mapped[str] = mapped_column(String(255), nullable=False, server_default="General")
    priority: Mapped[str] = mapped_column(String(16), nullable=False, server_default="Medium")
    description: Mapped[str] = mapped_column(String(4000), nullable=False)
    status: Mapped[str] = mapped_column(String(32), nullable=False)
    assigned_to: Mapped[str | None] = mapped_column(ForeignKey("users.id"), nullable=True)
    resolution_date: Mapped[str | None] = mapped_column(String(32), nullable=True)
    resolution_rating: Mapped[int | None] = mapped_column(nullable=True)
    resolution_feedback: Mapped[str | None] = mapped_column(String(2000), nullable=True)
