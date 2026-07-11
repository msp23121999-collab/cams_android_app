from datetime import datetime
from sqlalchemy import BigInteger, Boolean, DateTime, ForeignKey, Integer, String
from sqlalchemy.orm import Mapped, mapped_column

from app.db.base import Base
from app.db.models.mixins import TimestampSoftDeleteMixin


class BackupConfiguration(TimestampSoftDeleteMixin, Base):
    __tablename__ = "backup_configurations"

    auto_backup_enabled: Mapped[bool] = mapped_column(Boolean, default=True, nullable=False)
    schedule_time: Mapped[str] = mapped_column(String(5), default="21:00", nullable=False)  # HH:MM format
    retention_count: Mapped[int] = mapped_column(Integer, default=30, nullable=False)


class BackupHistory(TimestampSoftDeleteMixin, Base):
    __tablename__ = "backup_history"

    filename: Mapped[str] = mapped_column(String(255), nullable=False)
    filepath: Mapped[str] = mapped_column(String(1000), nullable=False)
    size_bytes: Mapped[int] = mapped_column(BigInteger, nullable=False)
    status: Mapped[str] = mapped_column(String(50), default="SUCCESS", nullable=False)  # SUCCESS, FAILED
    trigger_type: Mapped[str] = mapped_column(String(50), default="MANUAL", nullable=False)  # MANUAL, SCHEDULED
    created_by: Mapped[str | None] = mapped_column(ForeignKey("users.id"), nullable=True)
    error_message: Mapped[str | None] = mapped_column(String(4000), nullable=True)
    is_incremental: Mapped[bool] = mapped_column(Boolean, default=False, nullable=False)
