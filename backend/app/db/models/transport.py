"""Transport management models: routes, vehicles and student bus passes."""
import enum
from datetime import date

from sqlalchemy import Date, ForeignKey, Integer, Numeric, String, UniqueConstraint
from sqlalchemy.orm import Mapped, mapped_column, relationship

from app.db.base import Base
from app.db.models.mixins import TimestampSoftDeleteMixin


class VehicleStatus(str, enum.Enum):
    ACTIVE = "ACTIVE"
    MAINTENANCE = "MAINTENANCE"
    RETIRED = "RETIRED"


class PassStatus(str, enum.Enum):
    ACTIVE = "ACTIVE"
    EXPIRED = "EXPIRED"
    CANCELLED = "CANCELLED"


class TransportRoute(TimestampSoftDeleteMixin, Base):
    __tablename__ = "transport_routes"
    __table_args__ = (UniqueConstraint("code", name="uq_transport_route_code"),)

    name: Mapped[str] = mapped_column(String(255), nullable=False)
    code: Mapped[str] = mapped_column(String(32), nullable=False, index=True)
    start_point: Mapped[str] = mapped_column(String(255), nullable=False)
    end_point: Mapped[str] = mapped_column(String(255), nullable=False)
    distance_km: Mapped[float | None] = mapped_column(Numeric(8, 2), nullable=True)
    fare: Mapped[float | None] = mapped_column(Numeric(12, 2), nullable=True)
    stops: Mapped[str | None] = mapped_column(String(2000), nullable=True)

    vehicles: Mapped[list["TransportVehicle"]] = relationship(
        "TransportVehicle", back_populates="route"
    )
    passes: Mapped[list["TransportPass"]] = relationship(
        "TransportPass", back_populates="route", cascade="all, delete-orphan"
    )


class TransportVehicle(TimestampSoftDeleteMixin, Base):
    __tablename__ = "transport_vehicles"
    __table_args__ = (UniqueConstraint("registration_no", name="uq_transport_vehicle_reg"),)

    registration_no: Mapped[str] = mapped_column(String(32), nullable=False, index=True)
    vehicle_type: Mapped[str | None] = mapped_column(String(64), nullable=True)
    capacity: Mapped[int] = mapped_column(Integer, default=40, nullable=False)
    driver_name: Mapped[str | None] = mapped_column(String(128), nullable=True)
    driver_phone: Mapped[str | None] = mapped_column(String(20), nullable=True)
    route_id: Mapped[str | None] = mapped_column(ForeignKey("transport_routes.id"), nullable=True, index=True)
    status: Mapped[VehicleStatus] = mapped_column(
        String(16), default=VehicleStatus.ACTIVE.value, nullable=False, index=True
    )

    route: Mapped["TransportRoute | None"] = relationship("TransportRoute", back_populates="vehicles")


class TransportPass(TimestampSoftDeleteMixin, Base):
    __tablename__ = "transport_passes"

    route_id: Mapped[str] = mapped_column(ForeignKey("transport_routes.id"), nullable=False, index=True)
    student_id: Mapped[str] = mapped_column(ForeignKey("students.id"), nullable=False, index=True)
    pickup_point: Mapped[str | None] = mapped_column(String(255), nullable=True)
    valid_from: Mapped[date] = mapped_column(Date, nullable=False)
    valid_to: Mapped[date] = mapped_column(Date, nullable=False)
    fare_paid: Mapped[float | None] = mapped_column(Numeric(12, 2), nullable=True)
    status: Mapped[PassStatus] = mapped_column(
        String(16), default=PassStatus.ACTIVE.value, nullable=False, index=True
    )

    route: Mapped[TransportRoute] = relationship("TransportRoute", back_populates="passes")
