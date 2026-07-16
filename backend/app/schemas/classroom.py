from datetime import date, datetime
from pydantic import BaseModel


class ClassroomActivityCreate(BaseModel):
    section_id: str
    activity_type: str
    topic: str
    duration_minutes: int
    remarks: str | None = None


class ClassroomActivityResponse(BaseModel):
    id: str
    created_at: datetime
    faculty_id: str
    section_id: str
    activity_type: str
    topic: str
    duration_minutes: int
    remarks: str | None

    class Config:
        from_attributes = True


class StudentInteractionCreate(BaseModel):
    section_id: str
    type: str  # "QUESTION" or "POLL"
    question_text: str
    options: list[str] | None = None


class StudentInteractionResponse(BaseModel):
    id: str
    created_at: datetime
    faculty_id: str
    section_id: str
    type: str
    question_text: str
    options: list[str] | None
    responses_count: int
    is_active: bool

    class Config:
        from_attributes = True


class SessionSummaryCreate(BaseModel):
    section_id: str
    subject_code: str
    topic_covered: str
    subtopic_covered: str | None = None
    teaching_method: str
    resources_used: list[str] | None = None
    remarks: str | None = None


class SessionSummaryResponse(BaseModel):
    id: str
    created_at: datetime
    faculty_id: str
    section_id: str
    subject_code: str
    topic_covered: str
    subtopic_covered: str | None
    teaching_method: str
    resources_used: list[str] | None
    remarks: str | None
    date: date

    class Config:
        from_attributes = True


class ClassroomReportResponse(BaseModel):
    activities: list[ClassroomActivityResponse]
    interactions: list[StudentInteractionResponse]
    summaries: list[SessionSummaryResponse]
