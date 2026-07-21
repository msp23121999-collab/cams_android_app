"""Inventory management models: stock items and their movement ledger."""
import enum

from sqlalchemy import ForeignKey, Integer, Numeric, String, UniqueConstraint
from sqlalchemy.orm import Mapped, mapped_column, relationship

from app.db.base import Base
from app.db.models.mixins import TimestampSoftDeleteMixin


class StockMovement(str, enum.Enum):
    IN = "IN"
    OUT = "OUT"
    ADJUST = "ADJUST"


class InventoryItem(TimestampSoftDeleteMixin, Base):
    __tablename__ = "inventory_items"
    __table_args__ = (UniqueConstraint("code", name="uq_inventory_item_code"),)

    name: Mapped[str] = mapped_column(String(255), nullable=False)
    code: Mapped[str] = mapped_column(String(64), nullable=False, index=True)
    category: Mapped[str | None] = mapped_column(String(128), nullable=True, index=True)
    unit: Mapped[str] = mapped_column(String(32), default="pcs", nullable=False)
    quantity: Mapped[int] = mapped_column(Integer, default=0, nullable=False)
    # Reorder threshold — the UI highlights items at or below this level.
    min_quantity: Mapped[int] = mapped_column(Integer, default=0, nullable=False)
    unit_price: Mapped[float | None] = mapped_column(Numeric(12, 2), nullable=True)
    location: Mapped[str | None] = mapped_column(String(255), nullable=True)
    supplier: Mapped[str | None] = mapped_column(String(255), nullable=True)

    transactions: Mapped[list["InventoryTransaction"]] = relationship(
        "InventoryTransaction", back_populates="item", cascade="all, delete-orphan"
    )


class InventoryTransaction(TimestampSoftDeleteMixin, Base):
    __tablename__ = "inventory_transactions"

    item_id: Mapped[str] = mapped_column(ForeignKey("inventory_items.id"), nullable=False, index=True)
    movement: Mapped[StockMovement] = mapped_column(String(16), nullable=False)
    quantity: Mapped[int] = mapped_column(Integer, nullable=False)
    # Snapshot of the resulting stock level, so the ledger stays auditable even
    # if the item's current quantity changes later.
    resulting_quantity: Mapped[int] = mapped_column(Integer, default=0, nullable=False)
    reason: Mapped[str | None] = mapped_column(String(512), nullable=True)
    performed_by: Mapped[str | None] = mapped_column(ForeignKey("users.id"), nullable=True)

    item: Mapped[InventoryItem] = relationship("InventoryItem", back_populates="transactions")
