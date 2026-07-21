from datetime import datetime

from pydantic import BaseModel, ConfigDict


class MootCourtMemorialResponse(BaseModel):
    model_config = ConfigDict(from_attributes=True)

    id: str
    student_id: str
    title: str
    case_name: str | None = None
    content: str
    status: str
    created_at: datetime
    updated_at: datetime


class MootCourtMemorialCreateRequest(BaseModel):
    title: str
    case_name: str | None = None
    content: str
    status: str = "draft"


class MootCourtMemorialUpdateRequest(BaseModel):
    title: str | None = None
    case_name: str | None = None
    content: str | None = None
    status: str | None = None
