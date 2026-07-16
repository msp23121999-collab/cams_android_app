from datetime import date, datetime

from sqlalchemy import Date, ForeignKey, String, DateTime, Boolean, Integer
from sqlalchemy.orm import Mapped, mapped_column

from app.db.base import Base
from app.db.models.mixins import TimestampSoftDeleteMixin


class Notice(TimestampSoftDeleteMixin, Base):
    __tablename__ = "notices"

    created_by: Mapped[str] = mapped_column(ForeignKey("users.id"), nullable=False)
    title: Mapped[str] = mapped_column(String(255), nullable=False)
    body: Mapped[str] = mapped_column(String(4000), nullable=False)
    audience_type: Mapped[str] = mapped_column(String(64), nullable=False)
    publish_date: Mapped[date] = mapped_column(Date, nullable=False)
    category: Mapped[str | None] = mapped_column(String(64), nullable=True)
    expiry_date: Mapped[date | None] = mapped_column(Date, nullable=True)
    priority: Mapped[str | None] = mapped_column(String(32), nullable=True)
    status: Mapped[str | None] = mapped_column(String(32), nullable=True)
    publisher_role: Mapped[str | None] = mapped_column(String(32), nullable=True)
    target_department: Mapped[str | None] = mapped_column(String(64), nullable=True)
    target_semester: Mapped[int | None] = mapped_column(Integer, nullable=True)
    target_section: Mapped[str | None] = mapped_column(String(64), nullable=True)
    event_date: Mapped[date | None] = mapped_column(Date, nullable=True)
    audience_types: Mapped[str | None] = mapped_column(String(512), nullable=True)
    degree_id: Mapped[str | None] = mapped_column(ForeignKey("degrees.id"), nullable=True)
    batch_id: Mapped[str | None] = mapped_column(String(64), nullable=True)
    department_id: Mapped[str | None] = mapped_column(ForeignKey("departments.id"), nullable=True)
    attachment_url: Mapped[str | None] = mapped_column(String(512), nullable=True)



class NoticeAcknowledgement(TimestampSoftDeleteMixin, Base):
    __tablename__ = "notice_acknowledgements"

    notice_id: Mapped[str] = mapped_column(ForeignKey("notices.id"), nullable=False)
    user_id: Mapped[str] = mapped_column(ForeignKey("users.id"), nullable=False)
    is_read: Mapped[bool] = mapped_column(Boolean, default=False, nullable=False)
    read_at: Mapped[datetime | None] = mapped_column(DateTime, nullable=True)
    is_acknowledged: Mapped[bool] = mapped_column(Boolean, default=False, nullable=False)
    acknowledged_at: Mapped[datetime | None] = mapped_column(DateTime, nullable=True)
    status: Mapped[str] = mapped_column(String(32), default="DELIVERED", nullable=False)
    is_archived: Mapped[bool] = mapped_column(Boolean, default=False, nullable=False)
    archived_at: Mapped[datetime | None] = mapped_column(DateTime, nullable=True)


class Notification(TimestampSoftDeleteMixin, Base):
    __tablename__ = "notifications"

    user_id: Mapped[str] = mapped_column(ForeignKey("users.id"), nullable=False)
    type: Mapped[str] = mapped_column(String(64), nullable=False)
    message: Mapped[str] = mapped_column(String(1024), nullable=False)
    is_read: Mapped[bool] = mapped_column(default=False, nullable=False)
    sent_via: Mapped[str] = mapped_column(String(32), nullable=False)


class Message(TimestampSoftDeleteMixin, Base):
    __tablename__ = "messages"

    sender_id: Mapped[str] = mapped_column(ForeignKey("users.id"), nullable=False)
    receiver_id: Mapped[str] = mapped_column(ForeignKey("users.id"), nullable=False)
    body: Mapped[str] = mapped_column(String(4000), nullable=False)
    is_read: Mapped[bool] = mapped_column(Boolean, default=False, nullable=False)
    read_at: Mapped[datetime | None] = mapped_column(DateTime, nullable=True)
