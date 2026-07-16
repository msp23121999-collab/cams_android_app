from datetime import date, datetime
from sqlalchemy import ForeignKey, Integer, Numeric, String, Date, DateTime, JSON
from sqlalchemy.orm import Mapped, mapped_column

from app.db.base import Base
from app.db.models.mixins import TimestampSoftDeleteMixin


class FacultyProfile(TimestampSoftDeleteMixin, Base):
    __tablename__ = "faculty_profiles"

    user_id: Mapped[str] = mapped_column(ForeignKey("users.id"), unique=True, nullable=False)
    faculty_id: Mapped[str | None] = mapped_column(String(64), nullable=True)
    designation: Mapped[str] = mapped_column(String(128), nullable=False)
    specialization: Mapped[str | None] = mapped_column(String(255), nullable=True)
    
    # Personal Info
    employee_code: Mapped[str | None] = mapped_column(String(64), nullable=True)
    gender: Mapped[str | None] = mapped_column(String(16), nullable=True)
    date_of_birth: Mapped[date | None] = mapped_column(Date, nullable=True)
    blood_group: Mapped[str | None] = mapped_column(String(16), nullable=True)
    marital_status: Mapped[str | None] = mapped_column(String(32), nullable=True)
    nationality: Mapped[str | None] = mapped_column(String(64), nullable=True)
    community: Mapped[str | None] = mapped_column(String(64), nullable=True)
    alternate_phone: Mapped[str | None] = mapped_column(String(32), nullable=True)
    personal_email: Mapped[str | None] = mapped_column(String(255), nullable=True)
    
    # Address
    current_address: Mapped[str | None] = mapped_column(String(512), nullable=True)
    permanent_address: Mapped[str | None] = mapped_column(String(512), nullable=True)
    city: Mapped[str | None] = mapped_column(String(128), nullable=True)
    state: Mapped[str | None] = mapped_column(String(128), nullable=True)
    pincode: Mapped[str | None] = mapped_column(String(16), nullable=True)
    
    # Photo
    profile_photo_url: Mapped[str | None] = mapped_column(String(512), nullable=True)
    
    # Employment Info
    faculty_type: Mapped[str | None] = mapped_column(String(64), nullable=True)
    employment_category: Mapped[str | None] = mapped_column(String(64), nullable=True)
    date_of_joining: Mapped[date | None] = mapped_column(Date, nullable=True)
    employment_status: Mapped[str | None] = mapped_column(String(64), default="Active", nullable=True)
    approval_status: Mapped[str] = mapped_column(String(64), default="PENDING_PRINCIPAL", server_default="APPROVED", nullable=False)
    reporting_hod_id: Mapped[str | None] = mapped_column(String(36), nullable=True)
    reporting_principal_id: Mapped[str | None] = mapped_column(String(36), nullable=True)
    confirmation_date: Mapped[date | None] = mapped_column(Date, nullable=True)
    
    # JSON arrays/objects for dynamic sections
    educational_qualifications: Mapped[list | None] = mapped_column(JSON, nullable=True)
    experience_details: Mapped[list | None] = mapped_column(JSON, nullable=True)
    academic_responsibilities: Mapped[list | None] = mapped_column(JSON, nullable=True)
    certifications_achievements: Mapped[list | None] = mapped_column(JSON, nullable=True)
    promotion_history: Mapped[list | None] = mapped_column(JSON, nullable=True)
    increment_history: Mapped[list | None] = mapped_column(JSON, nullable=True)
    documents_repository: Mapped[dict | None] = mapped_column(JSON, nullable=True)
    notification_preferences: Mapped[dict | None] = mapped_column(JSON, nullable=True)


class FacultyWorkload(TimestampSoftDeleteMixin, Base):
    __tablename__ = "faculty_workload"

    faculty_id: Mapped[str] = mapped_column(ForeignKey("users.id"), nullable=False)
    semester: Mapped[int] = mapped_column(Integer, nullable=False)
    teaching_hours: Mapped[int] = mapped_column(Integer, nullable=False)


class FacultyResearch(TimestampSoftDeleteMixin, Base):
    __tablename__ = "faculty_research"

    faculty_id: Mapped[str] = mapped_column(ForeignKey("users.id"), nullable=False)
    title: Mapped[str] = mapped_column(String(255), nullable=False)
    publication: Mapped[str | None] = mapped_column(String(255), nullable=True)
    grant_amount: Mapped[float | None] = mapped_column(Numeric(12, 2), nullable=True)
    
    # Extra Publication details
    publisher: Mapped[str | None] = mapped_column(String(255), nullable=True)
    publication_date: Mapped[date | None] = mapped_column(Date, nullable=True)
    isbn_issn: Mapped[str | None] = mapped_column(String(64), nullable=True)
    research_type: Mapped[str | None] = mapped_column(String(64), nullable=True)
    proof_file_url: Mapped[str | None] = mapped_column(String(512), nullable=True)
    status: Mapped[str] = mapped_column(String(64), default="PENDING", nullable=False)
    comments: Mapped[str | None] = mapped_column(String(512), nullable=True)


class PublicationPlan(TimestampSoftDeleteMixin, Base):
    __tablename__ = "publication_plans"

    faculty_id: Mapped[str] = mapped_column(ForeignKey("users.id"), nullable=False)
    title: Mapped[str] = mapped_column(String(255), nullable=False)
    journal_conference: Mapped[str] = mapped_column(String(255), nullable=False)
    target_date: Mapped[date] = mapped_column(Date, nullable=False)
    status: Mapped[str] = mapped_column(String(32), default="PLANNED", nullable=False)
    research_area: Mapped[str | None] = mapped_column(String(255), nullable=True)
    publication_type: Mapped[str | None] = mapped_column(String(128), nullable=True)
    expected_publication_date: Mapped[date | None] = mapped_column(Date, nullable=True)
    academic_year: Mapped[str | None] = mapped_column(String(64), nullable=True)


class ResearchCompliance(TimestampSoftDeleteMixin, Base):
    __tablename__ = "research_compliance"

    faculty_id: Mapped[str] = mapped_column(ForeignKey("users.id"), nullable=False)
    requirement_name: Mapped[str] = mapped_column(String(255), nullable=False)
    deadline: Mapped[date] = mapped_column(Date, nullable=False)
    status: Mapped[str] = mapped_column(String(32), default="PENDING", nullable=False)
    submitted_at: Mapped[datetime | None] = mapped_column(DateTime, nullable=True)


class FacultyProfileUpdateRequest(TimestampSoftDeleteMixin, Base):
    __tablename__ = "faculty_profile_update_requests"

    user_id: Mapped[str] = mapped_column(ForeignKey("users.id"), nullable=False)
    status: Mapped[str] = mapped_column(String(64), default="PENDING", nullable=False)

    # Requested official details
    faculty_id: Mapped[str | None] = mapped_column(String(64), nullable=True)
    employee_code: Mapped[str | None] = mapped_column(String(64), nullable=True)
    official_email: Mapped[str | None] = mapped_column(String(255), nullable=True)
    official_phone: Mapped[str | None] = mapped_column(String(32), nullable=True)
    gender: Mapped[str | None] = mapped_column(String(16), nullable=True)
    date_of_birth: Mapped[date | None] = mapped_column(Date, nullable=True)
    blood_group: Mapped[str | None] = mapped_column(String(16), nullable=True)
    nationality: Mapped[str | None] = mapped_column(String(64), nullable=True)
    designation: Mapped[str | None] = mapped_column(String(64), nullable=True)
    department_name: Mapped[str | None] = mapped_column(String(255), nullable=True)

    # HOD review details
    comments: Mapped[str | None] = mapped_column(String(512), nullable=True)
    processed_at: Mapped[datetime | None] = mapped_column(DateTime(timezone=True), nullable=True)
    processed_by: Mapped[str | None] = mapped_column(ForeignKey("users.id"), nullable=True)
