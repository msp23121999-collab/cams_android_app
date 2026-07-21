from sqlalchemy import Numeric, String
from sqlalchemy.orm import Mapped, mapped_column

from app.db.base import Base
from app.db.models.mixins import TimestampSoftDeleteMixin


class ActivityPointCategory(TimestampSoftDeleteMixin, Base):
    __tablename__ = "activity_point_categories"

    code: Mapped[str] = mapped_column(String(64), unique=True, nullable=False)
    name: Mapped[str] = mapped_column(String(255), nullable=False)
    max_points: Mapped[float] = mapped_column(Numeric(6, 2), nullable=False)
    description: Mapped[str | None] = mapped_column(String(1024), nullable=True)
