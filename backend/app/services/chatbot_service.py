from sqlalchemy.ext.asyncio import AsyncSession
from groq import AsyncGroq

from app.core.config import settings
from app.db.models.user import User, UserRole
from app.db.models.chatbot import MessageRole
from app.db.repositories.chatbot_repository import ChatbotRepository

class ChatbotService:
    @staticmethod
    async def process_message(db: AsyncSession, user: User, message: str, session_id: str | None = None) -> dict:
        is_new_session = False
        if not session_id:
            session = await ChatbotRepository.create_session(db, user.id, title=message[:50])
            session_id = session.id
            is_new_session = True
        else:
            session = await ChatbotRepository.get_session(db, session_id, user.id)
            if not session:
                raise ValueError("Session not found")

        # Save user message
        await ChatbotRepository.add_message(db, session_id, MessageRole.USER, message)

        # Build context
        system_instruction = f"""
You are the CAMS (Campus Academic Management System) Academic Assistant.
You are talking to {user.full_name}, whose role is {user.role}.
Help them with academic queries, finding resources, and system navigation.
Be concise, professional, and helpful.
"""

        # Initialize Groq Client
        if settings.GROQ_API_KEY:
            client = AsyncGroq(api_key=settings.GROQ_API_KEY)
            
            messages = [{"role": "system", "content": system_instruction}]
            if not is_new_session:
                for msg in session.messages:
                    messages.append({
                        "role": "user" if msg.role == MessageRole.USER else "assistant",
                        "content": msg.content
                    })
            messages.append({"role": "user", "content": message})
            
            try:
                chat_completion = await client.chat.completions.create(
                    messages=messages,
                    model=settings.CHATBOT_MODEL,
                    temperature=0.7,
                    max_tokens=settings.CHATBOT_MAX_TOKENS,
                )
                reply_text = chat_completion.choices[0].message.content
            except Exception as e:
                reply_text = f"Sorry, I encountered an error communicating with the AI service: {str(e)}"
        else:
            reply_text = "I am operating in mock mode because no GROQ_API_KEY is set. I received your message: " + message

        # Save model response
        model_msg = await ChatbotRepository.add_message(db, session_id, MessageRole.MODEL, reply_text)
        await db.commit()

        return {
            "response": reply_text,
            "session_id": session_id,
            "message_id": model_msg.id
        }

    @staticmethod
    async def get_history(db: AsyncSession, user_id: str, limit: int = 10):
        return await ChatbotRepository.get_user_sessions(db, user_id, limit)

    @staticmethod
    async def get_session_messages(db: AsyncSession, user_id: str, session_id: str):
        session = await ChatbotRepository.get_session(db, session_id, user_id)
        if session:
            return session.messages
        return []

    @staticmethod
    async def clear_session(db: AsyncSession, user_id: str, session_id: str):
        return await ChatbotRepository.delete_session(db, session_id, user_id)
