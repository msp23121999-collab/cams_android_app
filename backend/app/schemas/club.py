from datetime import datetime
from pydantic import BaseModel


class ClubResponse(BaseModel):
    id: str
    name: str
    description: str | None = None
    category: str | None = None
    member_count: int = 0
    current_user_role: str | None = None
    president_name: str | None = None

    class Config:
        from_attributes = True


class ClubAnnouncementCreate(BaseModel):
    title: str
    is_urgent: bool = False


class ClubAnnouncementResponse(BaseModel):
    id: str
    club_id: str
    club_name: str
    title: str
    is_urgent: bool
    posted_by_name: str
    created_at: datetime

    class Config:
        from_attributes = True
