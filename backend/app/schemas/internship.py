from datetime import date

from pydantic import BaseModel


class InternshipDriveResponse(BaseModel):
    id: str
    company_name: str
    role: str
    package: str | None = None
    drive_date: date | None = None
    status: str = "Hiring"
    description: str | None = None

    class Config:
        from_attributes = True


class InternshipDriveCreateRequest(BaseModel):
    company_name: str
    role: str
    package: str | None = None
    drive_date: date | None = None
    status: str = "Hiring"
    description: str | None = None


class InternshipApplicationRequest(BaseModel):
    drive_id: str


class InternshipApplicationResponse(BaseModel):
    id: str
    drive_id: str
    student_id: str
    status: str = "Applied"
    student_name: str | None = None
    roll_no: str | None = None
    company_name: str | None = None
    role: str | None = None

    class Config:
        from_attributes = True


class InternshipApplicationReviewRequest(BaseModel):
    status: str


class PartnerCompanyResponse(BaseModel):
    id: str
    name: str
    industry: str
    status: str = "Active"
    contact_email: str | None = None
    contact_phone: str | None = None
    notes: str | None = None

    class Config:
        from_attributes = True


class PartnerCompanyCreateRequest(BaseModel):
    name: str
    industry: str
    status: str = "Active"
    contact_email: str | None = None
    contact_phone: str | None = None
    notes: str | None = None
