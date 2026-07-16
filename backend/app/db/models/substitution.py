import enum
from datetime import date, datetime
from typing import TYPE_CHECKING

from sqlalchemy import Date, DateTime, Enum, ForeignKey, String
from sqlalchemy.orm import Mapped, mapped_column, relationship

from app.db.base import Base
from app.db.models.mixins import TimestampSoftDeleteMixin

if TYPE_CHECKING:
    from app.db.models.user import User
    from app.db.models.academic import Timetable


class SubstitutionStatus(str, enum.Enum):
    PENDING = "PENDING"
    ALLOCATED = "ALLOCATED"
    ACKNOWLEDGED = "ACKNOWLEDGED"
    COMPLETED = "COMPLETED"
    PARTIALLY_COMPLETED = "PARTIALLY_COMPLETED"
    NOT_CONDUCTED = "NOT_CONDUCTED"


class AllocationMethod(str, enum.Enum):
    MANUAL = "MANUAL"
    AUTOMATIC = "AUTOMATIC"


class FacultyAbsence(TimestampSoftDeleteMixin, Base):
    __tablename__ = "faculty_absences"

    faculty_id: Mapped[str] = mapped_column(ForeignKey("users.id"), nullable=False)
    date: Mapped[date] = mapped_column(Date, nullable=False)
    reason: Mapped[str | None] = mapped_column(String(512), nullable=True)

    # Relationships
    faculty: Mapped["User"] = relationship("User", foreign_keys=[faculty_id])
    allocations: Mapped[list["SubstitutionAllocation"]] = relationship("SubstitutionAllocation", back_populates="absence", cascade="all, delete-orphan")


class SubstitutionAllocation(TimestampSoftDeleteMixin, Base):
    __tablename__ = "substitution_allocations"

    absence_id: Mapped[str] = mapped_column(ForeignKey("faculty_absences.id"), nullable=False)
    timetable_id: Mapped[str] = mapped_column(ForeignKey("timetable.id"), nullable=False)
    date: Mapped[date] = mapped_column(Date, nullable=False)
    substitute_faculty_id: Mapped[str | None] = mapped_column(ForeignKey("users.id"), nullable=True)
    status: Mapped[SubstitutionStatus] = mapped_column(
        Enum(SubstitutionStatus, name="substitution_status"), default=SubstitutionStatus.PENDING, nullable=False
    )
    allocation_method: Mapped[AllocationMethod | None] = mapped_column(
        Enum(AllocationMethod, name="allocation_method"), nullable=True
    )
    completed_at: Mapped[datetime | None] = mapped_column(DateTime(timezone=True), nullable=True)
    remarks: Mapped[str | None] = mapped_column(String(1024), nullable=True)

    # Relationships
    absence: Mapped[FacultyAbsence] = relationship("FacultyAbsence", back_populates="allocations")
    timetable: Mapped["Timetable"] = relationship("Timetable", foreign_keys=[timetable_id])
    substitute_faculty: Mapped["User | None"] = relationship("User", foreign_keys=[substitute_faculty_id])
