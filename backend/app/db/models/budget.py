"""Institutional budget & grants tracking: department budget line items
plus external research/infrastructure grants."""
import enum

from sqlalchemy import ForeignKey, Numeric, String, Date
from sqlalchemy.orm import Mapped, mapped_column, relationship

from app.db.base import Base
from app.db.models.mixins import TimestampSoftDeleteMixin


class BudgetStatus(str, enum.Enum):
    ACTIVE = "ACTIVE"
    CLOSED = "CLOSED"


class GrantStatus(str, enum.Enum):
    PROPOSED = "PROPOSED"
    APPROVED = "APPROVED"
    DISBURSED = "DISBURSED"
    COMPLETED = "COMPLETED"
    REJECTED = "REJECTED"


class BudgetLineItem(TimestampSoftDeleteMixin, Base):
    """A single fiscal-year budget allocation, optionally scoped to a department."""
    __tablename__ = "budget_line_items"

    fiscal_year: Mapped[str] = mapped_column(String(16), nullable=False)
    title: Mapped[str] = mapped_column(String(255), nullable=False)
    category: Mapped[str] = mapped_column(String(64), nullable=False, default="General")
    department_id: Mapped[str | None] = mapped_column(ForeignKey("departments.id"), nullable=True)
    allocated_amount: Mapped[float] = mapped_column(Numeric(14, 2), nullable=False, default=0)
    spent_amount: Mapped[float] = mapped_column(Numeric(14, 2), nullable=False, default=0)
    status: Mapped[str] = mapped_column(String(16), default=BudgetStatus.ACTIVE.value, nullable=False)
    notes: Mapped[str | None] = mapped_column(String(1024), nullable=True)
    created_by: Mapped[str] = mapped_column(ForeignKey("users.id"), nullable=False)

    expenses: Mapped[list["BudgetExpense"]] = relationship(
        "BudgetExpense", back_populates="line_item", cascade="all, delete-orphan"
    )


class BudgetExpense(TimestampSoftDeleteMixin, Base):
    """An individual spend recorded against a budget line item."""
    __tablename__ = "budget_expenses"

    line_item_id: Mapped[str] = mapped_column(ForeignKey("budget_line_items.id"), nullable=False)
    description: Mapped[str] = mapped_column(String(512), nullable=False)
    amount: Mapped[float] = mapped_column(Numeric(14, 2), nullable=False)
    expense_date: Mapped[str] = mapped_column(String(32), nullable=False)
    recorded_by: Mapped[str] = mapped_column(ForeignKey("users.id"), nullable=False)

    line_item: Mapped["BudgetLineItem"] = relationship("BudgetLineItem", back_populates="expenses")


class Grant(TimestampSoftDeleteMixin, Base):
    """An external research/infrastructure grant awarded to the institution."""
    __tablename__ = "grants"

    title: Mapped[str] = mapped_column(String(255), nullable=False)
    funding_agency: Mapped[str] = mapped_column(String(255), nullable=False)
    department_id: Mapped[str | None] = mapped_column(ForeignKey("departments.id"), nullable=True)
    principal_investigator: Mapped[str | None] = mapped_column(String(255), nullable=True)
    sanctioned_amount: Mapped[float] = mapped_column(Numeric(14, 2), nullable=False, default=0)
    disbursed_amount: Mapped[float] = mapped_column(Numeric(14, 2), nullable=False, default=0)
    status: Mapped[str] = mapped_column(String(16), default=GrantStatus.PROPOSED.value, nullable=False)
    start_date: Mapped[str | None] = mapped_column(String(32), nullable=True)
    end_date: Mapped[str | None] = mapped_column(String(32), nullable=True)
    notes: Mapped[str | None] = mapped_column(String(1024), nullable=True)
    created_by: Mapped[str] = mapped_column(ForeignKey("users.id"), nullable=False)
