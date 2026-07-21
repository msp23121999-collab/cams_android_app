from datetime import date
from sqlalchemy import select, update
from sqlalchemy.ext.asyncio import AsyncSession
from app.db.models.communication import Notice, Notification
from app.db.models.grievance import Grievance

class CommunicationRepository:
    def __init__(self, db: AsyncSession) -> None:
        self.db = db

    async def get_notices_by_audience(self, audience_type: str) -> list[Notice]:
        result = await self.db.execute(
            select(Notice)
            .where(Notice.audience_type == audience_type, Notice.is_deleted.is_(False))
            .order_by(Notice.publish_date.desc())
        )
        return list(result.scalars().all())

    async def get_all_notices(self) -> list[Notice]:
        result = await self.db.execute(
            select(Notice).where(Notice.is_deleted.is_(False)).order_by(Notice.publish_date.desc())
        )
        return list(result.scalars().all())

    async def create_notice(
        self,
        created_by: str,
        title: str,
        body: str,
        audience_type: str,
        publish_date: date,
        audience_types: list[str] | None = None,
        event_date: date | None = None,
        degree_id: str | None = None,
        batch_id: str | None = None,
        department_id: str | None = None,
        attachment_url: str | None = None,
        priority: str | None = None
    ) -> Notice:
        audience_types_str = ",".join(audience_types) if audience_types else audience_type
        notice = Notice(
            created_by=created_by,
            title=title,
            body=body,
            audience_type=audience_type,
            publish_date=publish_date,
            audience_types=audience_types_str,
            event_date=event_date,
            degree_id=degree_id,
            batch_id=batch_id,
            department_id=department_id,
            attachment_url=attachment_url,
            priority=priority,
            status="Active"
        )
        self.db.add(notice)
        await self.db.flush()
        return notice

    async def get_notifications_by_user(self, user_id: str) -> list[Notification]:
        result = await self.db.execute(
            select(Notification)
            .where(Notification.user_id == user_id, Notification.is_deleted.is_(False))
        )
        return list(result.scalars().all())

    async def create_notification(self, user_id: str, type_val: str, message: str, sent_via: str) -> Notification:
        notif = Notification(user_id=user_id, type=type_val, message=message, sent_via=sent_via, is_read=False)
        self.db.add(notif)
        await self.db.flush()
        return notif

    async def mark_notifications_read(self, user_id: str) -> None:
        await self.db.execute(
            update(Notification)
            .where(Notification.user_id == user_id)
            .values(is_read=True)
        )
        await self.db.flush()

    async def mark_notification_read(self, notification_id: str, user_id: str) -> None:
        await self.db.execute(
            update(Notification)
            .where(Notification.id == notification_id, Notification.user_id == user_id)
            .values(is_read=True)
        )
        await self.db.flush()

    async def soft_delete_notification(self, notification_id: str, user_id: str) -> None:
        from datetime import datetime, timezone
        await self.db.execute(
            update(Notification)
            .where(Notification.id == notification_id, Notification.user_id == user_id)
            .values(is_deleted=True, deleted_at=datetime.now(timezone.utc))
        )
        await self.db.flush()

    async def get_grievances_by_user(self, user_id: str) -> list[Grievance]:
        result = await self.db.execute(
            select(Grievance).where(Grievance.raised_by == user_id, Grievance.is_deleted.is_(False))
        )
        return list(result.scalars().all())

    async def get_all_grievances(self) -> list[Grievance]:
        result = await self.db.execute(
            select(Grievance).where(Grievance.is_deleted.is_(False))
        )
        return list(result.scalars().all())

    async def create_grievance(self, raised_by: str, category: str, description: str, subject: str = "General", priority: str = "Medium", assigned_to: str | None = None) -> Grievance:
        g = Grievance(raised_by=raised_by, category=category, subject=subject, priority=priority, description=description, status="PENDING", assigned_to=assigned_to)
        self.db.add(g)
        await self.db.flush()
        return g

    async def update_grievance_status(self, grievance_id: str, status: str) -> None:
        await self.db.execute(
            update(Grievance)
            .where(Grievance.id == grievance_id)
            .values(status=status)
        )
        await self.db.flush()
