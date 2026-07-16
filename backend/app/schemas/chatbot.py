from datetime import datetime
from pydantic import BaseModel
from typing import List, Optional
from app.db.models.chatbot import MessageRole

class ChatMessageBase(BaseModel):
    role: MessageRole
    content: str

    class Config:
        use_enum_values = True

class ChatMessageResponse(ChatMessageBase):
    id: str
    session_id: str
    created_at: datetime

    class Config:
        from_attributes = True
        use_enum_values = True

class ChatSessionBase(BaseModel):
    title: Optional[str] = None

class ChatSessionResponse(ChatSessionBase):
    id: str
    user_id: str
    is_active: bool
    created_at: datetime
    messages: List[ChatMessageResponse] = []

    class Config:
        from_attributes = True

class ChatSessionListResponse(ChatSessionBase):
    id: str
    created_at: datetime

    class Config:
        from_attributes = True

class ChatRequest(BaseModel):
    message: str
    session_id: Optional[str] = None

class ChatResponse(BaseModel):
    response: str
    session_id: str
    message_id: str

class Suggestion(BaseModel):
    text: str
    action: str
