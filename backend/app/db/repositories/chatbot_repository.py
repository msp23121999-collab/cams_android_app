from sqlalchemy import select
from sqlalchemy.ext.asyncio import AsyncSession
from sqlalchemy.orm import selectinload

from app.db.models.chatbot import ChatSession, ChatMessage, MessageRole

class ChatbotRepository:
    @staticmethod
    async def create_session(db: AsyncSession, user_id: str, title: str | None = None) -> ChatSession:
        session = ChatSession(user_id=user_id, title=title)
        db.add(session)
        await db.flush()
        return session

    @staticmethod
    async def get_session(db: AsyncSession, session_id: str, user_id: str) -> ChatSession | None:
        query = select(ChatSession).options(selectinload(ChatSession.messages)).where(
            ChatSession.id == session_id,
            ChatSession.user_id == user_id,
            ChatSession.is_active == True
        )
        result = await db.execute(query)
        return result.scalar_one_or_none()

    @staticmethod
    async def get_user_sessions(db: AsyncSession, user_id: str, limit: int = 10) -> list[ChatSession]:
        query = select(ChatSession).where(
            ChatSession.user_id == user_id,
            ChatSession.is_active == True
        ).order_by(ChatSession.created_at.desc()).limit(limit)
        result = await db.execute(query)
        return list(result.scalars().all())

    @staticmethod
    async def add_message(db: AsyncSession, session_id: str, role: MessageRole, content: str) -> ChatMessage:
        message = ChatMessage(session_id=session_id, role=role, content=content)
        db.add(message)
        await db.flush()
        return message

    @staticmethod
    async def delete_session(db: AsyncSession, session_id: str, user_id: str) -> bool:
        session = await ChatbotRepository.get_session(db, session_id, user_id)
        if session:
            session.is_active = False
            return True
        return False
