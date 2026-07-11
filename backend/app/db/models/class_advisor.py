from sqlalchemy import ForeignKey, String
from sqlalchemy.orm import Mapped, mapped_column

from app.db.base import Base
from app.db.models.mixins import TimestampSoftDeleteMixin


class ClassAdvisor(TimestampSoftDeleteMixin, Base):
    __tablename__ = "class_advisors"

    academic_year_id: Mapped[str] = mapped_column(ForeignKey("academic_years.id"), nullable=False)
    faculty_id: Mapped[str] = mapped_column(ForeignKey("users.id"), nullable=False)
    department_id: Mapped[str] = mapped_column(ForeignKey("departments.id"), nullable=False)
    batch: Mapped[str] = mapped_column(String(128), nullable=False)
    section_name: Mapped[str] = mapped_column(String(32), nullable=False)
