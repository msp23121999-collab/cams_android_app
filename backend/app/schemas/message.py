from datetime import datetime

from pydantic import BaseModel


class MessageResponse(BaseModel):
    id: str
    sender_id: str
    receiver_id: str
    body: str
    is_read: bool
    created_at: datetime

    class Config:
        from_attributes = True


class SendMessageRequest(BaseModel):
    receiver_id: str
    body: str


class ConversationResponse(BaseModel):
    user_id: str
    user_name: str
    user_role: str
    last_message: str
    last_message_at: datetime
    unread_count: int


class ContactResponse(BaseModel):
    id: str
    full_name: str
    role: str
