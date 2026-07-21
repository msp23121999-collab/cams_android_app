from sqlalchemy.ext.asyncio import AsyncSession
from app.db.repositories.communication_repository import CommunicationRepository
from app.db.models.communication import Notification

class NotificationService:
    def __init__(self, db: AsyncSession) -> None:
        self.repo = CommunicationRepository(db)

    async def get_user_notifications(self, user_id: str) -> list[Notification]:
        return await self.repo.get_notifications_by_user(user_id)

    async def send_notification(self, user_id: str, type_val: str, message: str, sent_via: str = "In-App") -> Notification:
        return await self.repo.create_notification(user_id, type_val, message, sent_via)

    async def mark_all_as_read(self, user_id: str) -> None:
        await self.repo.mark_notifications_read(user_id)

    async def mark_one_as_read(self, notification_id: str, user_id: str) -> None:
        await self.repo.mark_notification_read(notification_id, user_id)

    async def delete_notification(self, notification_id: str, user_id: str) -> None:
        await self.repo.soft_delete_notification(notification_id, user_id)
