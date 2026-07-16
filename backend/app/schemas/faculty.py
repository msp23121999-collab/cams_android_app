from datetime import date, datetime
from pydantic import BaseModel
from app.db.models.attendance import AttendanceStatus

class AttendanceMarkRequest(BaseModel):
    student_id: str
    section_id: str
    date: date
    status: AttendanceStatus

class StudyMaterialUploadRequest(BaseModel):
    section_id: str
    title: str
    type: str
    file_url: str

class AssignmentCreateRequest(BaseModel):
    section_id: str
    title: str
    deadline: date

class ResearchEntryRequest(BaseModel):
    title: str
    publication: str | None = None
    grant_amount: float | None = None
    publisher: str | None = None
    publication_date: date | None = None
    isbn_issn: str | None = None
    research_type: str | None = None
    proof_file_url: str | None = None

class ResearchResponse(BaseModel):
    id: str
    title: str
    publication: str | None
    grant_amount: float | None
    publisher: str | None = None
    publication_date: date | None = None
    isbn_issn: str | None = None
    research_type: str | None = None
    proof_file_url: str | None = None
    status: str = "PENDING"
    comments: str | None = None

class LeaveVerifyRequest(BaseModel):
    status: str
    remarks: str | None = None

class WorkloadResponse(BaseModel):
    faculty_id: str
    faculty_name: str
    semester: int
    teaching_hours: int

class FacultyPayrollResponse(BaseModel):
    salary_id: str
    basic: float
    allowances: float
    gross: float
    deductions_total: float
    net_pay: float
    month: int
    year: int
    pdf_url: str | None

class QualificationSchema(BaseModel):
    degree: str
    specialization: str
    university: str
    institution: str
    year_of_completion: int
    percentage_cgpa: str

class ExperienceSchema(BaseModel):
    institution_name: str
    designation: str
    from_date: date
    to_date: date
    total_years: float

class FacultyProfileResponse(BaseModel):
    user_id: str
    full_name: str
    email: str
    phone: str | None = None
    department_id: str | None = None
    department_name: str | None = None
    designation: str
    specialization: str | None = None
    faculty_id: str | None = None
    employee_code: str | None = None
    gender: str | None = None
    date_of_birth: date | None = None
    blood_group: str | None = None
    marital_status: str | None = None
    nationality: str | None = None
    community: str | None = None
    alternate_phone: str | None = None
    personal_email: str | None = None
    current_address: str | None = None
    permanent_address: str | None = None
    city: str | None = None
    state: str | None = None
    pincode: str | None = None
    profile_photo_url: str | None = None
    faculty_type: str | None = None
    employment_category: str | None = None
    date_of_joining: date | None = None
    employment_status: str | None = None
    reporting_hod_id: str | None = None
    reporting_hod_name: str | None = None
    reporting_principal_id: str | None = None
    reporting_principal_name: str | None = None
    confirmation_date: date | None = None
    educational_qualifications: list[QualificationSchema] | None = None
    experience_details: list[ExperienceSchema] | None = None
    academic_responsibilities: list[str] | None = None
    certifications_achievements: list[str] | None = None
    promotion_history: list[dict] | None = None
    increment_history: list[dict] | None = None
    documents_repository: dict[str, str] | None = None
    notification_preferences: dict | None = None
    approval_status: str | None = None

class FacultyProfileUpdateRequest(BaseModel):
    marital_status: str | None = None
    community: str | None = None
    alternate_phone: str | None = None
    personal_email: str | None = None
    current_address: str | None = None
    permanent_address: str | None = None
    city: str | None = None
    state: str | None = None
    pincode: str | None = None
    profile_photo_url: str | None = None
    educational_qualifications: list[QualificationSchema] | None = None
    experience_details: list[ExperienceSchema] | None = None
    certifications_achievements: list[str] | None = None
    notification_preferences: dict | None = None
    gender: str | None = None
    date_of_birth: date | None = None
    blood_group: str | None = None
    nationality: str | None = None
    official_phone: str | None = None


class FacultyProfileUpdateRequestCreate(BaseModel):
    faculty_id: str | None = None
    employee_code: str | None = None
    official_email: str | None = None
    official_phone: str | None = None
    gender: str | None = None
    date_of_birth: date | None = None
    blood_group: str | None = None
    nationality: str | None = None
    designation: str | None = None
    department_name: str | None = None


class FacultyProfileUpdateRequestResponse(BaseModel):
    id: str
    user_id: str
    full_name: str | None = None
    department_name: str | None = None
    status: str
    faculty_id: str | None = None
    employee_code: str | None = None
    official_email: str | None = None
    official_phone: str | None = None
    gender: str | None = None
    date_of_birth: date | None = None
    blood_group: str | None = None
    nationality: str | None = None
    requested_designation: str | None = None
    requested_department_name: str | None = None
    comments: str | None = None
    processed_at: datetime | None = None
    processed_by: str | None = None
    processed_by_name: str | None = None
    created_at: datetime


class FacultyProfileUpdateRequestReview(BaseModel):
    status: str
    comments: str | None = None

class FacultyProfileAdminUpdateRequest(BaseModel):
    designation: str
    specialization: str | None = None
    community: str | None = None
    employee_code: str | None = None
    department_id: str | None = None
    faculty_type: str | None = None
    employment_category: str | None = None
    date_of_joining: date | None = None
    employment_status: str | None = None
    reporting_hod_id: str | None = None
    reporting_principal_id: str | None = None
    confirmation_date: date | None = None
    academic_responsibilities: list[str] | None = None
    promotion_history: list[dict] | None = None
    increment_history: list[dict] | None = None
    documents_repository: dict[str, str] | None = None

class FacultyActivitySummaryResponse(BaseModel):
    classes_conducted: int
    attendance_marked: int
    study_materials_uploaded: int
    assignments_created: int
    leave_requests_submitted: int


class FacultyWorkloadDetailResponse(BaseModel):
    faculty_id: str
    faculty_name: str
    designation: str
    specialization: str | None = None
    weekly_hours: int
    subject_allocations: list[str]
    class_allocations: list[str]
    substitution_count: int


class FacultyAvailabilitySlot(BaseModel):
    weekday: str
    start_time: str
    end_time: str
    subject_code: str | None = None
    section_name: str | None = None
    room: str | None = None
    is_free: bool


class FacultyAvailabilityResponse(BaseModel):
    faculty_id: str
    faculty_name: str
    slots: list[FacultyAvailabilitySlot]


class WorkloadReportsResponse(BaseModel):
    average_workload: float
    overloaded_faculty: list[dict]
    underloaded_faculty: list[dict]
    workload_distribution: list[dict]


class StudyMaterialAuditResponse(BaseModel):
    id: str
    user_id: str | None = None
    user_name: str | None = None
    action: str
    entity_id: str | None = None
    material_title: str | None = None
    timestamp: datetime


class PublicationPlanResponse(BaseModel):
    id: str
    faculty_id: str
    faculty_name: str | None = None
    department_name: str | None = None
    title: str
    research_area: str | None = None
    publication_type: str | None = None
    journal_conference: str
    target_date: date
    expected_publication_date: date | None = None
    academic_year: str | None = None
    status: str


class PublicationPlanCreate(BaseModel):
    faculty_id: str
    title: str
    research_area: str | None = None
    publication_type: str | None = None
    journal_conference: str
    target_date: date
    expected_publication_date: date | None = None
    academic_year: str | None = None
    status: str = "PLANNED"


class ResearchProofResponse(BaseModel):
    id: str
    faculty_id: str
    faculty_name: str | None = None
    title: str
    publication: str | None = None
    grant_amount: float | None = None
    proof_file_url: str | None = None
    status: str
    comments: str | None = None


class ResearchProofVerifyRequest(BaseModel):
    status: str
    comments: str | None = None


class ResearchComplianceResponse(BaseModel):
    id: str
    faculty_id: str
    faculty_name: str | None = None
    requirement_name: str
    deadline: date
    status: str
    submitted_at: datetime | None = None


class ResearchComplianceCreate(BaseModel):
    faculty_id: str
    requirement_name: str
    deadline: date
    status: str = "PENDING"


class FacultyResearchSummary(BaseModel):
    faculty_id: str
    faculty_name: str
    verified_publications: int
    publication_plans_active: int
    completed_compliance: int
    pending_compliance: int


class ResearchReportsResponse(BaseModel):
    total_publications: int
    pending_proofs: int
    compliance_rate: float
    faculty_summaries: list[FacultyResearchSummary]


class MessageResponse(BaseModel):
    id: str
    sender_id: str
    sender_name: str | None = None
    receiver_id: str
    receiver_name: str | None = None
    body: str
    is_read: bool
    read_at: datetime | None = None
    created_at: datetime | None = None


class MessageCreate(BaseModel):
    receiver_id: str
    body: str


class NoticeCreateRequest(BaseModel):
    title: str
    body: str
    audience_type: str | None = None
    audience_types: list[str] | None = None
    publish_date: date | None = None
    priority: str | None = None
    event_date: date | None = None
    degree_id: str | None = None
    batch_id: str | None = None
    department_id: str | None = None
    attachment_url: str | None = None


class NoticeUpdateRequest(BaseModel):
    title: str | None = None
    body: str | None = None
    audience_type: str | None = None
    publish_date: date | None = None


class NotificationSendRequest(BaseModel):
    user_ids: list[str]
    type: str
    message: str
    sent_via: str = "In-App"


class NotificationDetailResponse(BaseModel):
    id: str
    user_id: str
    user_name: str | None = None
    type: str
    message: str
    is_read: bool
    sent_via: str
    created_at: datetime | None = None


class HODTimetableItemResponse(BaseModel):
    id: str
    section_id: str
    section_name: str
    subject_id: str
    subject_name: str
    subject_code: str
    faculty_id: str
    faculty_name: str
    room: str
    weekday: str
    start_time: str
    end_time: str
    status: str
    comments: str | None = None
    semester: int


class HODTimetableCreate(BaseModel):
    section_id: str
    subject_id: str
    faculty_id: str
    room: str
    weekday: str
    start_time: str
    end_time: str


class HODTimetableUpdate(BaseModel):
    section_id: str | None = None
    subject_id: str | None = None
    faculty_id: str | None = None
    room: str | None = None
    weekday: str | None = None
    start_time: str | None = None
    end_time: str | None = None


class HODSectionResponse(BaseModel):
    id: str
    course_id: str
    course_name: str
    semester: int
    section_name: str
    faculty_id: str | None = None
    faculty_name: str | None = None


class HODCourseResponse(BaseModel):
    id: str
    code: str
    name: str
    credits: int
    semester: int


class HODFacultyResponse(BaseModel):
    id: str
    full_name: str
    email: str
    phone: str | None = None
    department_name: str | None = None

