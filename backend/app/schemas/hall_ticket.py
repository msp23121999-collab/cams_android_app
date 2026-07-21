from datetime import datetime

from pydantic import BaseModel


class HallTicketResponse(BaseModel):
    id: str
    student_id: str
    exam_id: str | None = None
    exam_name: str
    is_eligible: bool = True
    ineligibility_reason: str | None = None
    is_issued: bool = False
    file_url: str | None = None
    issued_at: datetime | None = None

    # Matches the existing Android HallTicketDto shape.
    student_signature_url: str | None = None
    principal_signature_url: str | None = None
    coe_signature_url: str | None = None
    student_name: str | None = None
    exam_center: str | None = None
    exam_date: str | None = None

    class Config:
        from_attributes = True


class GenerateHallTicketsRequest(BaseModel):
    student_ids: list[str]
    exam_name: str
    exam_id: str | None = None
    is_eligible: bool = True
    ineligibility_reason: str | None = None
    exam_center: str | None = None
    exam_date: str | None = None
