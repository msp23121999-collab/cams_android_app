import enum
from datetime import date
from sqlalchemy import Date, ForeignKey, String, Integer, Float, JSON
from sqlalchemy.orm import Mapped, mapped_column, relationship
from app.db.base import Base
from app.db.models.mixins import TimestampSoftDeleteMixin

class ResearchPlanStatus(str, enum.Enum):
    DRAFT = "DRAFT"
    SUBMITTED = "SUBMITTED"
    COMPLETED = "COMPLETED"

class ProofStatus(str, enum.Enum):
    PENDING_VERIFICATION = "PENDING_VERIFICATION"
    VERIFIED = "VERIFIED"
    REJECTED = "REJECTED"
    RESUBMISSION_REQUIRED = "RESUBMISSION_REQUIRED"

class ResearchPlan(TimestampSoftDeleteMixin, Base):
    __tablename__ = "research_plans"

    faculty_id: Mapped[str] = mapped_column(ForeignKey("users.id"), nullable=False)
    title: Mapped[str] = mapped_column(String(255), nullable=False)
    area: Mapped[str] = mapped_column(String(128), nullable=False)  # Constitutional Law, Criminal Law, etc.
    target_journal_conference: Mapped[str] = mapped_column(String(255), nullable=False)
    type: Mapped[str] = mapped_column(String(128), nullable=False)  # Journal, Conference Paper, etc.
    start_date: Mapped[date] = mapped_column(Date, nullable=False)
    expected_completion_date: Mapped[date] = mapped_column(Date, nullable=False)
    objectives: Mapped[str] = mapped_column(String(1024), nullable=False)
    abstract_summary: Mapped[str] = mapped_column(String(2048), nullable=False)
    status: Mapped[ResearchPlanStatus] = mapped_column(String(64), default=ResearchPlanStatus.DRAFT, nullable=False)
    
    # 3-Month Cycle Control
    cycle_start_date: Mapped[date] = mapped_column(Date, nullable=False)
    cycle_due_date: Mapped[date] = mapped_column(Date, nullable=False)

    faculty = relationship("User", foreign_keys=[faculty_id])
    progress_updates = relationship("ResearchProgressUpdate", back_populates="plan", cascade="all, delete-orphan")
    proofs = relationship("PublicationProof", back_populates="plan", cascade="all, delete-orphan")


class ResearchProgressUpdate(TimestampSoftDeleteMixin, Base):
    __tablename__ = "research_progress_updates"

    plan_id: Mapped[str] = mapped_column(ForeignKey("research_plans.id"), nullable=False)
    progress_date: Mapped[date] = mapped_column(Date, nullable=False)
    current_stage: Mapped[str] = mapped_column(String(128), nullable=False)  # Topic Selection, Literature Review, etc.
    percentage_completed: Mapped[int] = mapped_column(Integer, nullable=False)
    work_completed: Mapped[str] = mapped_column(String(1024), nullable=False)
    remarks: Mapped[str | None] = mapped_column(String(1024), nullable=True)

    plan = relationship("ResearchPlan", back_populates="progress_updates")


class PublicationProof(TimestampSoftDeleteMixin, Base):
    __tablename__ = "publication_proofs"

    plan_id: Mapped[str] = mapped_column(ForeignKey("research_plans.id"), nullable=False)
    publication_date: Mapped[date] = mapped_column(Date, nullable=False)
    journal_name: Mapped[str] = mapped_column(String(255), nullable=False)
    issn_isbn: Mapped[str] = mapped_column(String(64), nullable=False)
    doi_number: Mapped[str | None] = mapped_column(String(64), nullable=True)
    publication_link: Mapped[str | None] = mapped_column(String(512), nullable=True)
    proof_file_url: Mapped[str] = mapped_column(String(512), nullable=False)
    status: Mapped[ProofStatus] = mapped_column(String(64), default=ProofStatus.PENDING_VERIFICATION, nullable=False)
    remarks: Mapped[str | None] = mapped_column(String(1024), nullable=True)

    plan = relationship("ResearchPlan", back_populates="proofs")
    verifications = relationship("ResearchVerification", back_populates="proof", cascade="all, delete-orphan")


class ResearchVerification(TimestampSoftDeleteMixin, Base):
    __tablename__ = "research_verifications"

    proof_id: Mapped[str] = mapped_column(ForeignKey("publication_proofs.id"), nullable=False)
    verified_by: Mapped[str] = mapped_column(ForeignKey("users.id"), nullable=False)
    status: Mapped[ProofStatus] = mapped_column(String(64), nullable=False)
    remarks: Mapped[str | None] = mapped_column(String(1024), nullable=True)

    proof = relationship("PublicationProof", back_populates="verifications")
    verifier = relationship("User", foreign_keys=[verified_by])

