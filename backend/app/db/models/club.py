from sqlalchemy import ForeignKey, Integer, String, UniqueConstraint
from sqlalchemy.orm import Mapped, mapped_column

from app.db.base import Base
from app.db.models.mixins import TimestampSoftDeleteMixin


class Club(TimestampSoftDeleteMixin, Base):
    __tablename__ = "clubs"

    name: Mapped[str] = mapped_column(String(255), nullable=False)
    description: Mapped[str | None] = mapped_column(String(2048), nullable=True)
    category: Mapped[str | None] = mapped_column(String(128), nullable=True)
    member_count: Mapped[int] = mapped_column(Integer, default=0, server_default="0", nullable=False)


class ClubMembership(TimestampSoftDeleteMixin, Base):
    __tablename__ = "club_memberships"
    __table_args__ = (UniqueConstraint("club_id", "user_id", name="uq_club_membership_club_user"),)

    club_id: Mapped[str] = mapped_column(ForeignKey("clubs.id"), nullable=False)
    user_id: Mapped[str] = mapped_column(ForeignKey("users.id"), nullable=False)
    role: Mapped[str] = mapped_column(String(32), default="Member", server_default="Member", nullable=False)


class ClubAnnouncement(TimestampSoftDeleteMixin, Base):
    __tablename__ = "club_announcements"

    club_id: Mapped[str] = mapped_column(ForeignKey("clubs.id"), nullable=False)
    posted_by: Mapped[str] = mapped_column(ForeignKey("users.id"), nullable=False)
    title: Mapped[str] = mapped_column(String(255), nullable=False)
    is_urgent: Mapped[bool] = mapped_column(default=False, server_default="0", nullable=False)
