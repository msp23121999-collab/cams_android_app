import enum
from datetime import date, time
from typing import TYPE_CHECKING

from sqlalchemy import Date, Enum, ForeignKey, Integer, String, Time, Boolean, UniqueConstraint
from sqlalchemy.orm import Mapped, mapped_column, relationship

from app.db.base import Base
from app.db.models.mixins import TimestampSoftDeleteMixin

if TYPE_CHECKING:
    from app.db.models.user import User


class ApprovalStatus(str, enum.Enum):
    PENDING = "PENDING"
    APPROVED = "APPROVED"
    REJECTED = "REJECTED"
    CHANGES_REQUESTED = "CHANGES_REQUESTED"


class Weekday(str, enum.Enum):
    MONDAY = "MONDAY"
    TUESDAY = "TUESDAY"
    WEDNESDAY = "WEDNESDAY"
    THURSDAY = "THURSDAY"
    FRIDAY = "FRIDAY"
    SATURDAY = "SATURDAY"


class ExamType(str, enum.Enum):
    CIA = "CIA"
    SEMESTER = "SEMESTER"


class Department(TimestampSoftDeleteMixin, Base):
    __tablename__ = "departments"

    name: Mapped[str] = mapped_column(String(255), unique=True, nullable=False)
    code: Mapped[str] = mapped_column(String(32), unique=True, nullable=False)
    hod_id: Mapped[str | None] = mapped_column(ForeignKey("users.id"), nullable=True)
    course_name: Mapped[str | None] = mapped_column(String(255), nullable=True)
    duration_years: Mapped[int | None] = mapped_column(Integer, nullable=True)
    sem_count: Mapped[int | None] = mapped_column(Integer, nullable=True)
    establish_year: Mapped[int | None] = mapped_column(Integer, nullable=True)
    program_level: Mapped[str | None] = mapped_column(String(32), nullable=True)
    intake: Mapped[int | None] = mapped_column(Integer, default=60, nullable=True)
    affiliation_code: Mapped[str | None] = mapped_column(String(255), nullable=True)

    users: Mapped[list["User"]] = relationship("User", back_populates="department", foreign_keys="[User.department_id]")


class Course(TimestampSoftDeleteMixin, Base):
    __tablename__ = "courses"
    __table_args__ = (UniqueConstraint("code", "degree_id", name="uq_courses_code_degree"),)

    dept_id: Mapped[str | None] = mapped_column(ForeignKey("departments.id"), nullable=True)
    degree_id: Mapped[str | None] = mapped_column(ForeignKey("degrees.id"), nullable=True)
    code: Mapped[str] = mapped_column(String(32), nullable=False)
    name: Mapped[str] = mapped_column(String(255), nullable=False)
    credits: Mapped[int] = mapped_column(Integer, nullable=False)
    semester: Mapped[int] = mapped_column(Integer, nullable=False)


class Section(TimestampSoftDeleteMixin, Base):
    __tablename__ = "sections"

    course_id: Mapped[str] = mapped_column(ForeignKey("courses.id"), nullable=False)
    section_name: Mapped[str] = mapped_column(String(32), nullable=False)
    faculty_id: Mapped[str | None] = mapped_column(ForeignKey("users.id"), nullable=True)


class Timetable(TimestampSoftDeleteMixin, Base):
    __tablename__ = "timetable"

    section_id: Mapped[str] = mapped_column(ForeignKey("sections.id"), nullable=False)
    subject_id: Mapped[str] = mapped_column(ForeignKey("courses.id"), nullable=False)
    faculty_id: Mapped[str] = mapped_column(ForeignKey("users.id"), nullable=False)
    room: Mapped[str] = mapped_column(String(64), nullable=False)
    weekday: Mapped[Weekday] = mapped_column(Enum(Weekday, name="weekday"), nullable=False)
    start_time: Mapped[time] = mapped_column(Time, nullable=False)
    end_time: Mapped[time] = mapped_column(Time, nullable=False)


class TimetableApproval(TimestampSoftDeleteMixin, Base):
    __tablename__ = "timetable_approvals"

    timetable_id: Mapped[str] = mapped_column(ForeignKey("timetable.id"), nullable=False)
    status: Mapped[ApprovalStatus] = mapped_column(Enum(ApprovalStatus, name="approval_status"), nullable=False)
    approved_by: Mapped[str | None] = mapped_column(ForeignKey("users.id"), nullable=True)
    comments: Mapped[str | None] = mapped_column(String(1000), nullable=True)
    rejection_remarks: Mapped[str | None] = mapped_column(String(2048), nullable=True)
    approved_date: Mapped[str | None] = mapped_column(String(64), nullable=True)
    rejected_by: Mapped[str | None] = mapped_column(ForeignKey("users.id"), nullable=True)
    rejected_date: Mapped[str | None] = mapped_column(String(64), nullable=True)



class Exam(TimestampSoftDeleteMixin, Base):
    __tablename__ = "exams"

    course_id: Mapped[str] = mapped_column(ForeignKey("courses.id"), nullable=False)
    type: Mapped[ExamType] = mapped_column(Enum(ExamType, name="exam_type"), nullable=False)
    center: Mapped[str] = mapped_column(String(128), nullable=False)
    date: Mapped[date] = mapped_column(Date, nullable=False)
    start_time: Mapped[time] = mapped_column(Time, nullable=False)
    end_time: Mapped[time] = mapped_column(Time, nullable=False)


class ExamSetting(TimestampSoftDeleteMixin, Base):
    __tablename__ = "exam_settings"

    exam_id: Mapped[str] = mapped_column(ForeignKey("exams.id"), nullable=False)
    halls: Mapped[str] = mapped_column(String(255), nullable=False)
    rules: Mapped[str] = mapped_column(String(4000), nullable=False)
    is_published: Mapped[bool] = mapped_column(default=False, nullable=False)


class Degree(TimestampSoftDeleteMixin, Base):
    __tablename__ = "degrees"
    __table_args__ = (UniqueConstraint("code", "program_level", "applicable_batch", name="uq_degree_code_program_batch"),)

    code: Mapped[str] = mapped_column(String(32), nullable=False)
    name: Mapped[str] = mapped_column(String(255), nullable=False)
    applicable_batch: Mapped[str] = mapped_column(String(128), nullable=False)
    program_level: Mapped[str] = mapped_column(String(32), nullable=False)
    duration_years: Mapped[int] = mapped_column(Integer, nullable=False)
    dept_id: Mapped[str | None] = mapped_column(ForeignKey("departments.id"), nullable=True)
    credit_pattern: Mapped[str | None] = mapped_column(String(255), nullable=True)
    exam_formula: Mapped[str | None] = mapped_column(String(255), nullable=True)
    passing_marks: Mapped[int] = mapped_column(Integer, default=40, nullable=False)
    grade_boundaries: Mapped[str | None] = mapped_column(String(2000), nullable=True)

    department: Mapped["Department"] = relationship("Department", foreign_keys=[dept_id])


class SystemSetting(TimestampSoftDeleteMixin, Base):
    __tablename__ = "system_settings"

    college_name: Mapped[str] = mapped_column(String(255), nullable=False)
    logo_url: Mapped[str | None] = mapped_column(String(1000), nullable=True)
    address: Mapped[str | None] = mapped_column(String(1000), nullable=True)
    affiliation_number: Mapped[str | None] = mapped_column(String(128), nullable=True)
    aicte_ugc_code: Mapped[str | None] = mapped_column(String(128), nullable=True)
    accreditation_body: Mapped[str | None] = mapped_column(String(128), nullable=True)
    bank_name: Mapped[str | None] = mapped_column(String(255), nullable=True)
    bank_account_no: Mapped[str | None] = mapped_column(String(128), nullable=True)
    bank_ifsc: Mapped[str | None] = mapped_column(String(64), nullable=True)
    bank_branch: Mapped[str | None] = mapped_column(String(255), nullable=True)


class SystemSettingHistory(TimestampSoftDeleteMixin, Base):
    __tablename__ = "system_setting_history"

    setting_id: Mapped[str] = mapped_column(ForeignKey("system_settings.id"), nullable=False)
    user_id: Mapped[str | None] = mapped_column(ForeignKey("users.id"), nullable=True)
    field_name: Mapped[str] = mapped_column(String(128), nullable=False)
    old_value: Mapped[str | None] = mapped_column(String(4000), nullable=True)
    new_value: Mapped[str | None] = mapped_column(String(4000), nullable=True)
class AcademicYear(TimestampSoftDeleteMixin, Base):
    __tablename__ = "academic_years"

    name: Mapped[str] = mapped_column(String(128), nullable=False)
    start_date: Mapped[date] = mapped_column(Date, nullable=False)
    end_date: Mapped[date] = mapped_column(Date, nullable=False)
    degree_id: Mapped[str] = mapped_column(ForeignKey("degrees.id"), nullable=False)
    batch: Mapped[str] = mapped_column(String(128), nullable=False)
    current_semester: Mapped[int] = mapped_column(Integer, nullable=False, default=1)
    is_semester_open: Mapped[bool] = mapped_column(Boolean, default=True, nullable=False)
    is_exam_period: Mapped[bool] = mapped_column(Boolean, default=False, nullable=False)
    is_active: Mapped[bool] = mapped_column(Boolean, default=True, nullable=False)


class SubjectAllocation(TimestampSoftDeleteMixin, Base):
    __tablename__ = "subject_allocations"

    academic_year_id: Mapped[str] = mapped_column(ForeignKey("academic_years.id"), nullable=False)
    course_id: Mapped[str] = mapped_column(ForeignKey("courses.id"), nullable=False)
    section_id: Mapped[str] = mapped_column(ForeignKey("sections.id"), nullable=False)
    faculty_id: Mapped[str] = mapped_column(ForeignKey("users.id"), nullable=False)
    department_id: Mapped[str] = mapped_column(ForeignKey("departments.id"), nullable=False)
    semester: Mapped[int | None] = mapped_column(Integer, nullable=True)
    allocated_by: Mapped[str | None] = mapped_column(ForeignKey("users.id"), nullable=True)
    is_active: Mapped[bool] = mapped_column(Boolean, default=True, nullable=False)
