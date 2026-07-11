from datetime import date, datetime
from pydantic import BaseModel
from app.db.models.substitution import SubstitutionStatus, AllocationMethod

class FacultyAbsenceCreate(BaseModel):
    faculty_id: str
    date: date
    reason: str | None = None

class SubstitutionAllocateRequest(BaseModel):
    substitute_faculty_id: str
    remarks: str | None = None

class SubstitutionStatusUpdateRequest(BaseModel):
    status: SubstitutionStatus
    remarks: str | None = None

class FacultyResponse(BaseModel):
    id: str
    full_name: str
    email: str

class TimetableDetails(BaseModel):
    id: str
    subject_code: str
    subject_name: str
    section_name: str
    room: str
    start_time: str
    end_time: str

class SubstitutionAllocationResponse(BaseModel):
    id: str
    absence_id: str
    date: date
    timetable_id: str
    timetable: TimetableDetails
    substitute_faculty_id: str | None = None
    substitute_faculty_name: str | None = None
    status: SubstitutionStatus
    allocation_method: AllocationMethod | None = None
    completed_at: datetime | None = None
    remarks: str | None = None

class FacultyAbsenceResponse(BaseModel):
    id: str
    faculty_id: str
    faculty_name: str
    date: date
    reason: str | None = None
    allocations: list[SubstitutionAllocationResponse] = []

class SubstitutionReportResponse(BaseModel):
    total_absences: int
    total_affected_classes: int
    allocated_count: int
    completed_count: int
    pending_count: int
    faculty_contributions: list[dict]
    status_breakdown: dict
