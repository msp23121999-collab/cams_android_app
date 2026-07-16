from datetime import date
from pydantic import BaseModel
from app.db.models.user import UserRole

class UserCreateRequest(BaseModel):
    email: str
    phone: str | None = None
    full_name: str
    password: str
    role: UserRole
    department_id: str | None = None
    roll_no: str | None = None
    semester: int | None = None
    batch_year: int | None = None
    degree_id: str | None = None
    quota: str | None = None
    community_category: str | None = None
    scholarship_amount: float | None = None
    scholarship_name: str | None = None
    deduction_amount: float | None = None
    deduction_reason: str | None = None
    scholarship_type_id: str | None = None
    employee_code: str | None = None
    designation: str | None = None
    date_of_joining: date | None = None
    confirmation_date: date | None = None
    reporting_hod_id: str | None = None
    reporting_principal_id: str | None = None
    academic_responsibilities: list[str] | None = None

class UserUpdateRequest(BaseModel):
    email: str
    phone: str | None = None
    full_name: str
    password: str | None = None
    role: UserRole
    department_id: str | None = None
    roll_no: str | None = None
    semester: int | None = None
    batch_year: int | None = None
    degree_id: str | None = None
    quota: str | None = None
    community_category: str | None = None
    scholarship_amount: float | None = None
    scholarship_name: str | None = None
    deduction_amount: float | None = None
    deduction_reason: str | None = None
    scholarship_type_id: str | None = None
    employee_code: str | None = None
    designation: str | None = None
    date_of_joining: date | None = None
    confirmation_date: date | None = None
    reporting_hod_id: str | None = None
    reporting_principal_id: str | None = None
    academic_responsibilities: list[str] | None = None

class UserResponse(BaseModel):
    model_config = {"from_attributes": True}
    id: str
    email: str  # plain str to support .local test domains
    phone: str | None = None
    full_name: str
    role: UserRole
    is_active: bool
    department_id: str | None = None
    department_code: str | None = None
    department_name: str | None = None
    batch_year: int | None = None
    batch: str | None = None

class FacultyListResponse(BaseModel):
    id: str
    email: str
    phone: str | None
    full_name: str
    role: UserRole
    is_active: bool
    department_id: str | None = None
    department_name: str | None = None
    department_code: str | None = None
    department_code: str | None = None
    department_name: str | None = None

class FeeStructureCreateRequest(BaseModel):
    dept_id: str
    semester: int
    amount: float
    due_date: date
    fee_type: str

class CollectFeeRequest(BaseModel):
    fee_record_id: str
    amount: float
    mode: str
    txn_id: str | None = None

class PayrollRunRequest(BaseModel):
    faculty_id: str
    month: int
    year: int
    basic: float
    allowances: float
    lop_days: int = 0

class TimetableApprovalRequest(BaseModel):
    status: str
    comments: str | None = None

class NoticeCreateRequest(BaseModel):
    title: str
    body: str
    audience_type: str | None = None
    audience_types: list[str] | None = None
    category: str | None = None
    expiry_date: date | None = None
    priority: str | None = None
    target_department: str | None = None
    event_date: date | None = None
    degree_id: str | None = None
    batch_id: str | None = None
    department_id: str | None = None
    attachment_url: str | None = None

class DepartmentCreateRequest(BaseModel):
    name: str
    code: str
    course_name: str | None = None
    duration_years: int | None = None
    sem_count: int | None = None
    establish_year: int | None = None
    program_level: str | None = None
    intake: int | None = 60
    affiliation_code: str | None = None

class DepartmentResponse(BaseModel):
    id: str
    name: str
    code: str
    course_name: str | None
    duration_years: int | None
    sem_count: int | None
    establish_year: int | None
    program_level: str | None
    intake: int | None
    affiliation_code: str | None


class DegreeCreateRequest(BaseModel):
    code: str
    name: str
    applicable_batch: str
    program_level: str
    duration_years: int
    dept_id: str | None = None
    credit_pattern: str | None = None
    exam_formula: str | None = None
    passing_marks: int = 40
    grade_boundaries: str | None = None


class DegreeResponse(BaseModel):
    id: str
    code: str
    name: str
    applicable_batch: str
    program_level: str
    duration_years: int
    dept_id: str | None = None
    credit_pattern: str | None
    exam_formula: str | None
    passing_marks: int
    grade_boundaries: str | None


class CourseCreateRequest(BaseModel):
    code: str
    name: str
    credits: int
    semester: int
    degree_id: str | None = None
    dept_id: str | None = None


class CourseUpdateRequest(BaseModel):
    code: str | None = None
    name: str | None = None
    credits: int | None = None
    semester: int | None = None
    degree_id: str | None = None
    dept_id: str | None = None


class CourseResponse(BaseModel):
    id: str
    code: str
    name: str
    credits: int
    semester: int
    degree_id: str | None
    dept_id: str | None


class SystemSettingsUpdateRequest(BaseModel):
    college_name: str
    logo_url: str | None = None
    address: str | None = None
    affiliation_number: str | None = None
    aicte_ugc_code: str | None = None
    accreditation_body: str | None = None
    bank_name: str | None = None
    bank_account_no: str | None = None
    bank_ifsc: str | None = None
    bank_branch: str | None = None


class SystemSettingsResponse(BaseModel):
    id: str
    college_name: str
    logo_url: str | None
    address: str | None
    affiliation_number: str | None
    aicte_ugc_code: str | None
    accreditation_body: str | None
    bank_name: str | None
    bank_account_no: str | None
    bank_ifsc: str | None
    bank_branch: str | None


class AcademicYearCreateRequest(BaseModel):
    name: str
    start_date: date
    end_date: date
    degree_id: str
    batch: str
    current_semester: int = 1


class AcademicYearUpdateRequest(BaseModel):
    name: str | None = None
    start_date: date | None = None
    end_date: date | None = None
    current_semester: int | None = None
    is_semester_open: bool | None = None
    is_exam_period: bool | None = None
    is_active: bool | None = None


class AcademicYearResponse(BaseModel):
    id: str
    name: str
    start_date: date
    end_date: date
    degree_id: str
    batch: str
    current_semester: int
    is_semester_open: bool
    is_exam_period: bool
    is_active: bool
    degree_code: str | None = None
    degree_name: str | None = None


class SetSemesterRequest(BaseModel):
    batch: str
    department_ids: list[str]
    semester_type: str  # "ODD" or "EVEN"


class CopyCoursesRequest(BaseModel):
    source_degree_id: str
    target_degree_id: str
    dept_id: str





