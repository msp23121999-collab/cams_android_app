import enum
from datetime import date

from sqlalchemy import Date, Enum, ForeignKey, String, Numeric, JSON
from sqlalchemy.orm import Mapped, mapped_column

from app.db.base import Base
from app.db.models.mixins import TimestampSoftDeleteMixin


class AttendanceStatus(str, enum.Enum):
    PRESENT = "present"
    ABSENT = "absent"
    OD = "od"


class Attendance(TimestampSoftDeleteMixin, Base):
    __tablename__ = "attendance"

    section_id: Mapped[str] = mapped_column(ForeignKey("sections.id"), nullable=False)
    subject_id: Mapped[str] = mapped_column(ForeignKey("courses.id"), nullable=False)
    faculty_id: Mapped[str] = mapped_column(ForeignKey("users.id"), nullable=False)
    date: Mapped[date] = mapped_column(Date, nullable=False)
    hour: Mapped[str] = mapped_column(String(32), nullable=False)
    
    absentee_ids: Mapped[list[str] | None] = mapped_column(JSON, default=list, nullable=True)
    od_ids: Mapped[list[str] | None] = mapped_column(JSON, default=list, nullable=True)


class StaffAttendance(TimestampSoftDeleteMixin, Base):
    __tablename__ = "staff_attendance"

    faculty_id: Mapped[str] = mapped_column(ForeignKey("users.id"), nullable=False)
    date: Mapped[date] = mapped_column(Date, nullable=False)
    status: Mapped[str] = mapped_column(String(64), nullable=False)
    check_in: Mapped[str | None] = mapped_column(String(32), nullable=True)
    check_out: Mapped[str | None] = mapped_column(String(32), nullable=True)
    working_hours: Mapped[float | None] = mapped_column(Numeric(5, 2), nullable=True)
    source: Mapped[str] = mapped_column(String(64), default="Manual", nullable=False)


class AttendanceCorrection(TimestampSoftDeleteMixin, Base):
    __tablename__ = "attendance_corrections"

    student_reg_no: Mapped[str] = mapped_column(String(64), nullable=False)
    student_name: Mapped[str] = mapped_column(String(256), nullable=False)
    subject: Mapped[str] = mapped_column(String(256), nullable=False)
    date: Mapped[date] = mapped_column(Date, nullable=False)
    previous_status: Mapped[str] = mapped_column(String(64), nullable=False)
    updated_status: Mapped[str] = mapped_column(String(64), nullable=False)
    reason: Mapped[str] = mapped_column(String(1024), nullable=False)
    status: Mapped[str] = mapped_column(String(64), default="PENDING", nullable=False)
    remarks: Mapped[str | None] = mapped_column(String(1024), nullable=True)

