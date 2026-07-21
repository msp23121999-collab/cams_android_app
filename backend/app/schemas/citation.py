from datetime import datetime

from pydantic import BaseModel, ConfigDict


class SavedCitationResponse(BaseModel):
    model_config = ConfigDict(from_attributes=True)

    id: str
    student_id: str
    case_name: str
    citation_text: str
    note: str | None = None
    created_at: datetime
    updated_at: datetime


class SavedCitationCreateRequest(BaseModel):
    case_name: str
    citation_text: str
    note: str | None = None
