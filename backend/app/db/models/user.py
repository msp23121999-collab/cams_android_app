import enum
from datetime import datetime
from typing import TYPE_CHECKING

from sqlalchemy import Boolean, DateTime, Enum, ForeignKey, String
from sqlalchemy.orm import Mapped, mapped_column, relationship

from app.db.base import Base
from app.db.models.mixins import TimestampSoftDeleteMixin

if TYPE_CHECKING:
    from app.db.models.academic import Department


class UserRole(str, enum.Enum):
    SUPER_ADMIN = "SUPER_ADMIN"
    ADMIN = "ADMIN"
    PRINCIPAL = "PRINCIPAL"
    HOD = "HOD"
    FACULTY = "FACULTY"
    STUDENT = "STUDENT"
    PARENT = "PARENT"


class User(TimestampSoftDeleteMixin, Base):
    __tablename__ = "users"

    email: Mapped[str] = mapped_column(String(255), unique=True, index=True, nullable=False)
    phone: Mapped[str | None] = mapped_column(String(32), unique=True, nullable=True)
    full_name: Mapped[str] = mapped_column(String(255), nullable=False)
    hashed_password: Mapped[str] = mapped_column(String(255), nullable=False)
    role: Mapped[UserRole] = mapped_column(Enum(UserRole, name="user_role"), nullable=False)
    is_active: Mapped[bool] = mapped_column(Boolean, default=True, nullable=False)
    department_id: Mapped[str | None] = mapped_column(ForeignKey("departments.id"), nullable=True)

    # Email-change verification flow (see /auth/request-email-change, /auth/confirm-email-change)
    pending_email: Mapped[str | None] = mapped_column(String(255), nullable=True)
    email_change_token: Mapped[str | None] = mapped_column(String(128), nullable=True, index=True)
    email_change_token_expires_at: Mapped[datetime | None] = mapped_column(DateTime(timezone=True), nullable=True)

    # Notification preferences
    email_notifications_enabled: Mapped[bool] = mapped_column(Boolean, default=True, nullable=False)

    department: Mapped["Department | None"] = relationship("Department", back_populates="users", foreign_keys="[User.department_id]")
