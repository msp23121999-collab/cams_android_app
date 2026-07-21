"""Hostel management models: blocks -> rooms -> student allocations."""
import enum
from datetime import date

from sqlalchemy import Date, ForeignKey, Integer, Numeric, String, UniqueConstraint
from sqlalchemy.orm import Mapped, mapped_column, relationship

from app.db.base import Base
from app.db.models.mixins import TimestampSoftDeleteMixin


class HostelType(str, enum.Enum):
    BOYS = "BOYS"
    GIRLS = "GIRLS"


class AllocationStatus(str, enum.Enum):
    ACTIVE = "ACTIVE"
    VACATED = "VACATED"


class HostelBlock(TimestampSoftDeleteMixin, Base):
    __tablename__ = "hostel_blocks"
    __table_args__ = (UniqueConstraint("code", name="uq_hostel_block_code"),)

    name: Mapped[str] = mapped_column(String(128), nullable=False)
    code: Mapped[str] = mapped_column(String(32), nullable=False, index=True)
    hostel_type: Mapped[HostelType] = mapped_column(
        String(16), default=HostelType.BOYS.value, nullable=False
    )
    warden_name: Mapped[str | None] = mapped_column(String(128), nullable=True)
    warden_phone: Mapped[str | None] = mapped_column(String(20), nullable=True)
    address: Mapped[str | None] = mapped_column(String(512), nullable=True)

    rooms: Mapped[list["HostelRoom"]] = relationship(
        "HostelRoom", back_populates="block", cascade="all, delete-orphan"
    )


class HostelRoom(TimestampSoftDeleteMixin, Base):
    __tablename__ = "hostel_rooms"
    __table_args__ = (UniqueConstraint("block_id", "room_number", name="uq_hostel_room_block_number"),)

    block_id: Mapped[str] = mapped_column(ForeignKey("hostel_blocks.id"), nullable=False, index=True)
    room_number: Mapped[str] = mapped_column(String(32), nullable=False)
    floor: Mapped[int] = mapped_column(Integer, default=0, nullable=False)
    capacity: Mapped[int] = mapped_column(Integer, default=2, nullable=False)
    room_type: Mapped[str | None] = mapped_column(String(64), nullable=True)
    monthly_rent: Mapped[float | None] = mapped_column(Numeric(12, 2), nullable=True)

    block: Mapped[HostelBlock] = relationship("HostelBlock", back_populates="rooms")
    allocations: Mapped[list["HostelAllocation"]] = relationship(
        "HostelAllocation", back_populates="room", cascade="all, delete-orphan"
    )


class HostelAllocation(TimestampSoftDeleteMixin, Base):
    __tablename__ = "hostel_allocations"

    room_id: Mapped[str] = mapped_column(ForeignKey("hostel_rooms.id"), nullable=False, index=True)
    student_id: Mapped[str] = mapped_column(ForeignKey("students.id"), nullable=False, index=True)
    allocated_on: Mapped[date] = mapped_column(Date, nullable=False)
    vacated_on: Mapped[date | None] = mapped_column(Date, nullable=True)
    status: Mapped[AllocationStatus] = mapped_column(
        String(16), default=AllocationStatus.ACTIVE.value, nullable=False, index=True
    )
    remarks: Mapped[str | None] = mapped_column(String(512), nullable=True)

    room: Mapped[HostelRoom] = relationship("HostelRoom", back_populates="allocations")
