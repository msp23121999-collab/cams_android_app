from datetime import datetime

from sqlalchemy import DateTime, ForeignKey, String, UniqueConstraint
from sqlalchemy.orm import Mapped, mapped_column

from app.db.base import Base
from app.db.models.mixins import TimestampSoftDeleteMixin


class DeviceToken(TimestampSoftDeleteMixin, Base):
    """An FCM registration token for one installation of the app.

    A user may have several (phone + tablet), and a device may change hands, so the
    token is unique rather than the user: re-registering an existing token simply
    re-points it at the current user instead of creating a duplicate.
    """

    __tablename__ = "device_tokens"
    __table_args__ = (UniqueConstraint("token", name="uq_device_tokens_token"),)

    user_id: Mapped[str] = mapped_column(ForeignKey("users.id"), nullable=False, index=True)
    token: Mapped[str] = mapped_column(String(512), nullable=False)
    platform: Mapped[str] = mapped_column(String(16), nullable=False, default="android")
    # Used to retire tokens that have gone quiet; FCM also tells us when one is dead.
    last_seen_at: Mapped[datetime | None] = mapped_column(DateTime(timezone=True), nullable=True)
