from datetime import date

from sqlalchemy import Date, ForeignKey, String, UniqueConstraint
from sqlalchemy.orm import Mapped, mapped_column

from app.db.base import Base
from app.db.models.mixins import TimestampSoftDeleteMixin


class InternshipDrive(TimestampSoftDeleteMixin, Base):
    __tablename__ = "internship_drives"

    company_name: Mapped[str] = mapped_column(String(255), nullable=False)
    role: Mapped[str] = mapped_column(String(255), nullable=False)
    package: Mapped[str | None] = mapped_column(String(128), nullable=True)
    drive_date: Mapped[date | None] = mapped_column(Date, nullable=True)
    status: Mapped[str] = mapped_column(String(32), default="Hiring", server_default="Hiring", nullable=False)
    description: Mapped[str | None] = mapped_column(String(2048), nullable=True)


class InternshipApplication(TimestampSoftDeleteMixin, Base):
    __tablename__ = "internship_applications"
    __table_args__ = (UniqueConstraint("drive_id", "student_id", name="uq_internship_application_drive_student"),)

    drive_id: Mapped[str] = mapped_column(ForeignKey("internship_drives.id"), nullable=False)
    student_id: Mapped[str] = mapped_column(ForeignKey("students.id"), nullable=False)
    status: Mapped[str] = mapped_column(String(32), default="Applied", server_default="Applied", nullable=False)


class PartnerCompany(TimestampSoftDeleteMixin, Base):
    __tablename__ = "partner_companies"

    name: Mapped[str] = mapped_column(String(255), nullable=False)
    industry: Mapped[str] = mapped_column(String(128), nullable=False)
    status: Mapped[str] = mapped_column(String(32), default="Active", server_default="Active", nullable=False)
    contact_email: Mapped[str | None] = mapped_column(String(255), nullable=True)
    contact_phone: Mapped[str | None] = mapped_column(String(32), nullable=True)
    notes: Mapped[str | None] = mapped_column(String(1024), nullable=True)
