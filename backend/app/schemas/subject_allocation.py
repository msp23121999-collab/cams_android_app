from pydantic import BaseModel
from datetime import date
from typing import List, Optional

class SubjectAllocationCreate(BaseModel):
    course_id: str
    section_id: str
    faculty_id: str

class SubjectAllocationBase(BaseModel):
    course_id: str
    section_id: str
    faculty_id: str

class SubjectAllocationResponse(SubjectAllocationBase):
    id: str
    academic_year_id: str
    department_id: str
    allocated_by_id: Optional[str] = None
    allocated_date: Optional[date] = None

    model_config = {"from_attributes": True}

class AcademicSetupResponse(BaseModel):
    academic_year: str
    academic_year_id: str
    department: str
    department_id: str
    course: str
    semester: int
    degree: str
    sections: List[dict]
    batches: Optional[List[dict]] = None

class FacultyWorkloadInfo(BaseModel):
    id: str
    name: str
    designation: str
    specialization: Optional[str]
    current_workload_hours: int
    max_workload_hours: int
    assigned_subjects: List[str]

class SubjectInfo(BaseModel):
    id: str
    course: str
    semester: int
    subject_code: str
    subject_name: str
    subject_type: str
    credits: int
    hours_per_week: int
    batch: Optional[str] = None
    batch_id: Optional[str] = None
    degree_id: Optional[str] = None

class AllocationHistoryResponse(BaseModel):
    id: str
    academic_year: str
    semester: int
    course: str
    subject_name: str
    faculty_name: str
    allocated_date: str
    allocated_by: str
