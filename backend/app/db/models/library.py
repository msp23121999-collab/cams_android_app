"""Library management models: book catalogue and issue/return ledger."""
import enum
from datetime import date

from sqlalchemy import Date, ForeignKey, Integer, Numeric, String, UniqueConstraint
from sqlalchemy.orm import Mapped, mapped_column, relationship

from app.db.base import Base
from app.db.models.mixins import TimestampSoftDeleteMixin


class IssueStatus(str, enum.Enum):
    ISSUED = "ISSUED"
    RETURNED = "RETURNED"
    OVERDUE = "OVERDUE"


class LibraryBook(TimestampSoftDeleteMixin, Base):
    __tablename__ = "library_books"
    __table_args__ = (UniqueConstraint("accession_no", name="uq_library_book_accession"),)

    title: Mapped[str] = mapped_column(String(512), nullable=False, index=True)
    author: Mapped[str | None] = mapped_column(String(255), nullable=True)
    # Accession number is the library's own unique identifier for a copy set.
    accession_no: Mapped[str] = mapped_column(String(64), nullable=False, index=True)
    isbn: Mapped[str | None] = mapped_column(String(32), nullable=True)
    category: Mapped[str | None] = mapped_column(String(128), nullable=True, index=True)
    publisher: Mapped[str | None] = mapped_column(String(255), nullable=True)
    published_year: Mapped[int | None] = mapped_column(Integer, nullable=True)
    shelf_location: Mapped[str | None] = mapped_column(String(128), nullable=True)
    total_copies: Mapped[int] = mapped_column(Integer, default=1, nullable=False)
    available_copies: Mapped[int] = mapped_column(Integer, default=1, nullable=False)

    issues: Mapped[list["LibraryIssue"]] = relationship(
        "LibraryIssue", back_populates="book", cascade="all, delete-orphan"
    )


class LibraryIssue(TimestampSoftDeleteMixin, Base):
    __tablename__ = "library_issues"

    book_id: Mapped[str] = mapped_column(ForeignKey("library_books.id"), nullable=False, index=True)
    # Borrower is any system user (student or staff).
    member_id: Mapped[str] = mapped_column(ForeignKey("users.id"), nullable=False, index=True)
    issued_on: Mapped[date] = mapped_column(Date, nullable=False)
    due_on: Mapped[date] = mapped_column(Date, nullable=False)
    returned_on: Mapped[date | None] = mapped_column(Date, nullable=True)
    fine_amount: Mapped[float] = mapped_column(Numeric(12, 2), default=0, nullable=False)
    status: Mapped[IssueStatus] = mapped_column(
        String(16), default=IssueStatus.ISSUED.value, nullable=False, index=True
    )
    remarks: Mapped[str | None] = mapped_column(String(512), nullable=True)

    book: Mapped[LibraryBook] = relationship("LibraryBook", back_populates="issues")
