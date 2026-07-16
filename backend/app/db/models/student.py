from sqlalchemy import ForeignKey, Integer, String, JSON, Numeric
from sqlalchemy import ForeignKey, Integer, String, JSON, Date

from sqlalchemy.orm import Mapped, mapped_column

from app.db.base import Base
from app.db.models.mixins import TimestampSoftDeleteMixin
from datetime import date


class Student(TimestampSoftDeleteMixin, Base):
    __tablename__ = "students"

    user_id: Mapped[str] = mapped_column(ForeignKey("users.id"), unique=True, nullable=False)
    roll_no: Mapped[str] = mapped_column(String(64), unique=True, nullable=False)
    department_id: Mapped[str] = mapped_column(ForeignKey("departments.id"), nullable=False)
    semester: Mapped[int] = mapped_column(Integer, nullable=False)
    batch_year: Mapped[int] = mapped_column(Integer, nullable=False)
    
    # Profile & Academic Tracking enhancements
    mentor_id: Mapped[str | None] = mapped_column(ForeignKey("users.id"), nullable=True)
    cgpa: Mapped[float | None] = mapped_column(nullable=True)
    skills: Mapped[list[str] | None] = mapped_column(JSON, nullable=True)
    degree_id: Mapped[str | None] = mapped_column(ForeignKey("degrees.id"), nullable=True)
    quota: Mapped[str | None] = mapped_column(String(64), nullable=True)
    cgpa: Mapped[float | None] = mapped_column(Numeric(3, 2), nullable=True)
    certifications: Mapped[list | None] = mapped_column(JSON, default=list, nullable=True)
    internships: Mapped[list | None] = mapped_column(JSON, default=list, nullable=True)
    sports_records: Mapped[list | None] = mapped_column(JSON, default=list, nullable=True)
    moot_courts: Mapped[list | None] = mapped_column(JSON, default=list, nullable=True)
    profile_photo_url: Mapped[str | None] = mapped_column(String(1000), nullable=True)
    section_id: Mapped[str | None] = mapped_column(ForeignKey("sections.id"), nullable=True)

    # Extended personal information
    full_name: Mapped[str] = mapped_column(String(128), nullable=True)
    date_of_birth: Mapped[date] = mapped_column(nullable=True)
    gender: Mapped[str] = mapped_column(String(16), nullable=True)
    blood_group: Mapped[str] = mapped_column(String(8), nullable=True)
    nationality: Mapped[str] = mapped_column(String(64), nullable=True)
    mobile_number: Mapped[str] = mapped_column(String(20), nullable=True)
    current_address: Mapped[str] = mapped_column(String(256), nullable=True)
    permanent_address: Mapped[str] = mapped_column(String(256), nullable=True)
    aadhaar_number: Mapped[str] = mapped_column(String(20), nullable=True)
    passport_number: Mapped[str] = mapped_column(String(20), nullable=True)
    community_category: Mapped[str] = mapped_column(String(64), nullable=True)
    religion: Mapped[str] = mapped_column(String(64), nullable=True)
    emergency_contact_name: Mapped[str] = mapped_column(String(128), nullable=True)
    emergency_contact_relationship: Mapped[str] = mapped_column(String(64), nullable=True)
    emergency_contact_number: Mapped[str] = mapped_column(String(20), nullable=True)
    father_name: Mapped[str | None] = mapped_column(String(128), nullable=True)
    father_occupation: Mapped[str | None] = mapped_column(String(128), nullable=True)
    father_mobile: Mapped[str | None] = mapped_column(String(20), nullable=True)
    father_email: Mapped[str | None] = mapped_column(String(128), nullable=True)
    father_office_address: Mapped[str | None] = mapped_column(String(256), nullable=True)
    mother_name: Mapped[str | None] = mapped_column(String(128), nullable=True)
    mother_occupation: Mapped[str | None] = mapped_column(String(128), nullable=True)
    mother_mobile: Mapped[str | None] = mapped_column(String(20), nullable=True)
    mother_email: Mapped[str | None] = mapped_column(String(128), nullable=True)
    mother_office_address: Mapped[str | None] = mapped_column(String(256), nullable=True)
    parent_annual_income: Mapped[str | None] = mapped_column(String(64), nullable=True)
    languages_known: Mapped[list[str] | None] = mapped_column(JSON, nullable=True)
    hobbies_interests: Mapped[list[str] | None] = mapped_column(JSON, nullable=True)
    special_skills: Mapped[list[str] | None] = mapped_column(JSON, nullable=True)
    medical_info: Mapped[str] = mapped_column(String(256), nullable=True)

    # Verification workflow enhancements
    verification_status: Mapped[str | None] = mapped_column(String(64), default="DRAFT", server_default="DRAFT", nullable=True)
    staff_remarks: Mapped[str | None] = mapped_column(String(500), nullable=True)
    hod_remarks: Mapped[str | None] = mapped_column(String(500), nullable=True)
    document_aadhaar_url: Mapped[str | None] = mapped_column(String(1000), nullable=True)
    document_community_url: Mapped[str | None] = mapped_column(String(1000), nullable=True)
    document_tc_url: Mapped[str | None] = mapped_column(String(1000), nullable=True)
    document_other_url: Mapped[str | None] = mapped_column(String(1000), nullable=True)
    edit_request_status: Mapped[str | None] = mapped_column(String(64), nullable=True)
    edit_request_reason: Mapped[str | None] = mapped_column(String(1000), nullable=True)

    # Scholarship assignment — stores the id/slug of the scholarship type from scholarship_types_list.json
    scholarship_type_id: Mapped[str | None] = mapped_column(String(128), nullable=True)



class ParentStudentMap(TimestampSoftDeleteMixin, Base):
    __tablename__ = "parent_student_map"

    parent_id: Mapped[str] = mapped_column(ForeignKey("users.id"), nullable=False)
    student_id: Mapped[str] = mapped_column(ForeignKey("students.id"), nullable=False)


class MentorshipRecord(TimestampSoftDeleteMixin, Base):
    __tablename__ = "mentorship_records"

    student_id: Mapped[str] = mapped_column(ForeignKey("students.id"), unique=True, nullable=False)
    mentor_id: Mapped[str] = mapped_column(ForeignKey("users.id"), nullable=False)
    meeting_log: Mapped[str | None] = mapped_column(String(4000), nullable=True)
    academic_review: Mapped[str | None] = mapped_column(String(4000), nullable=True)
    improvement_plan: Mapped[str | None] = mapped_column(String(4000), nullable=True)
    remarks: Mapped[str | None] = mapped_column(String(4000), nullable=True)
    follow_up: Mapped[str | None] = mapped_column(String(4000), nullable=True)

