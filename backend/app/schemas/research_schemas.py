from datetime import date
from pydantic import BaseModel
from app.db.models.research import ResearchPlanStatus, ProofStatus

# Research Plan Schemas
class ResearchPlanCreate(BaseModel):
    title: str
    area: str
    target_journal_conference: str
    type: str
    start_date: date
    expected_completion_date: date
    objectives: str
    abstract_summary: str
    status: ResearchPlanStatus = ResearchPlanStatus.DRAFT

class ResearchPlanResponse(BaseModel):
    id: str
    faculty_id: str
    title: str
    area: str
    target_journal_conference: str
    type: str
    start_date: date
    expected_completion_date: date
    objectives: str
    abstract_summary: str
    status: str
    cycle_start_date: date
    cycle_due_date: date
    faculty_name: str | None = None
    days_overdue: int = 0
    is_overdue: bool = False
    latest_progress_percentage: int = 0
    latest_progress_stage: str | None = None

    class Config:
        from_attributes = True

# Progress Update Schemas
class ProgressUpdateCreate(BaseModel):
    current_stage: str
    percentage_completed: int
    work_completed: str
    remarks: str | None = None

class ProgressUpdateResponse(BaseModel):
    id: str
    plan_id: str
    progress_date: date
    current_stage: str
    percentage_completed: int
    work_completed: str
    remarks: str | None = None

    class Config:
        from_attributes = True

# Publication Proof Schemas
class ProofSubmitRequest(BaseModel):
    publication_date: date
    journal_name: str
    issn_isbn: str
    doi_number: str | None = None
    publication_link: str | None = None
    proof_file_url: str

class ProofResponse(BaseModel):
    id: str
    plan_id: str
    publication_date: date
    journal_name: str
    issn_isbn: str
    doi_number: str | None = None
    publication_link: str | None = None
    proof_file_url: str
    status: str
    remarks: str | None = None

    class Config:
        from_attributes = True

# Verification Schemas
class VerificationRequest(BaseModel):
    status: ProofStatus
    remarks: str | None = None

class VerificationResponse(BaseModel):
    id: str
    proof_id: str
    verified_by: str
    status: str
    remarks: str | None = None
    verified_at: date

    class Config:
        from_attributes = True

# Compliance Dashboard Schema
class DepartmentCompliance(BaseModel):
    department_name: str
    completed: int
    pending: int
    overdue: int

class PrincipalComplianceResponse(BaseModel):
    completed_count: int
    pending_count: int
    overdue_count: int
    department_wise: list[DepartmentCompliance]
    overdue_faculty_list: list[dict]
