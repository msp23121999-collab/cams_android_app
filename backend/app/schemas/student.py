from datetime import date, datetime
from pydantic import BaseModel, field_validator
from app.db.models.attendance import AttendanceStatus
from app.db.models.leave import LeaveStatus
from app.db.models.marks import MarkExamType

class StudentProfileResponse(BaseModel):
    id: str
    roll_no: str
    semester: int
    batch_year: int
    email: str
    full_name: str
    mentor_name: str | None = None
    mentor_email: str | None = None
    mentor_phone: str | None = None
    cgpa: float | None = None
    skills: list[str] | None = None
    profile_photo_url: str | None = None
    course_name: str | None = None
    section: str | None = None
    class_advisor_name: str | None = None
    class_advisor_email: str | None = None
    class_advisor_phone: str | None = None
    batch: str | None = None
    year_of_study: str | None = None
    department_name: str | None = None
    scholarship_amount: float | None = 0.0
    scholarship_name: str | None = None
    deduction_amount: float | None = 0.0
    deduction_reason: str | None = None
    
    # Extended personal information
    date_of_birth: date | None = None
    gender: str | None = None
    blood_group: str | None = None
    nationality: str | None = None
    mobile_number: str | None = None
    current_address: str | None = None
    permanent_address: str | None = None
    aadhaar_number: str | None = None
    passport_number: str | None = None
    community_category: str | None = None
    religion: str | None = None
    emergency_contact_name: str | None = None
    emergency_contact_relationship: str | None = None
    emergency_contact_number: str | None = None
    father_name: str | None = None
    father_occupation: str | None = None
    father_mobile: str | None = None
    father_email: str | None = None
    father_office_address: str | None = None
    mother_name: str | None = None
    mother_occupation: str | None = None
    mother_mobile: str | None = None
    mother_email: str | None = None
    mother_office_address: str | None = None
    parent_annual_income: str | None = None
    languages_known: list[str] | None = None
    hobbies_interests: list[str] | None = None
    special_skills: list[str] | None = None
    medical_info: str | None = None
    certifications: list[dict] | None = None
    internships: list[dict] | None = None
    sports_records: list[dict] | None = None
    moot_courts: list[dict] | None = None
    
    # Verification workflow enhancements
    verification_status: str | None = None
    staff_remarks: str | None = None
    hod_remarks: str | None = None
    document_aadhaar_url: str | None = None
    document_community_url: str | None = None
    document_tc_url: str | None = None
    document_other_url: str | None = None
    edit_request_status: str | None = None
    edit_request_reason: str | None = None

class StudentProfileUpdateRequest(BaseModel):
    full_name: str | None = None
    date_of_birth: date | None = None
    gender: str | None = None
    blood_group: str | None = None
    nationality: str | None = None
    mobile_number: str | None = None
    current_address: str | None = None
    permanent_address: str | None = None
    aadhaar_number: str | None = None
    passport_number: str | None = None
    community_category: str | None = None
    religion: str | None = None
    emergency_contact_name: str | None = None
    emergency_contact_relationship: str | None = None
    emergency_contact_number: str | None = None
    father_name: str | None = None
    father_occupation: str | None = None
    father_mobile: str | None = None
    father_email: str | None = None
    father_office_address: str | None = None
    mother_name: str | None = None
    mother_occupation: str | None = None
    mother_mobile: str | None = None
    mother_email: str | None = None
    mother_office_address: str | None = None
    parent_annual_income: str | None = None
    languages_known: list[str] | None = None
    hobbies_interests: list[str] | None = None
    special_skills: list[str] | None = None
    medical_info: str | None = None
    profile_photo_url: str | None = None
    internships: list[dict] | None = None
    certifications: list[dict] | None = None
    sports_records: list[dict] | None = None
    moot_courts: list[dict] | None = None
    
    # Verification workflow enhancements
    verification_status: str | None = None
    document_aadhaar_url: str | None = None
    document_community_url: str | None = None
    document_tc_url: str | None = None
    document_other_url: str | None = None
    edit_request_status: str | None = None
    edit_request_reason: str | None = None

    @field_validator("mobile_number", "emergency_contact_number", "father_mobile", "mother_mobile", mode="before")
    @classmethod
    def validate_phone_numbers(cls, v):
        if v is None or v == "":
            return v
        v_str = str(v).strip()
        import re
        if not re.match(r"^\d{10}$", v_str):
            raise ValueError("Contact number must be exactly 10 digits")
        return v_str

class AttendanceRecordSchema(BaseModel):
    id: str
    date: date
    status: AttendanceStatus
    subject_name: str
    subject_code: str
    section_name: str

class AttendanceSummaryResponse(BaseModel):
    percentage: float
    total: int
    present: int
    absent: int
    od: int
    records: list[AttendanceRecordSchema]

class MarkRecordSchema(BaseModel):
    id: str
    exam_type: MarkExamType
    mark: float
    max_mark: float

class FeeRecordSchema(BaseModel):
    record_id: str
    fee_type: str
    amount: float
    due_date: date
    status: str
    paid_amount: float | None = 0.0
    remaining_amount: float | None = 0.0
    gross_amount: float | None = 0.0
    scholarship_amount: float | None = 0.0
    scholarship_name: str | None = ""
    deduction_amount: float | None = 0.0
    deduction_reason: str | None = ""

class StudentFeeSummaryResponse(BaseModel):
    total_fees: float
    scholarship_deduction: float
    other_deductions: float
    net_fees: float
    amount_paid: float
    pending_balance: float
    due_date: date | None = None
    assigned_scholarship_type_id: str | None = None
    records: list[FeeRecordSchema]

class FeePaymentRequest(BaseModel):
    amount: float
    mode: str
    txn_id: str

class LeaveApplicationRequest(BaseModel):
    app_category: str
    type: str
    session_type: str | None = None
    priority: str | None = None
    from_date: date
    to_date: date
    reason: str
    photo_url: str | None = None
    latitude: float | None = None
    longitude: float | None = None
    location_address: str | None = None
    capture_time: datetime | None = None
    verification_status: str | None = None
    distance_from_campus: float | None = None
    device_id: str | None = None
    location_accuracy: float | None = None
    geo_fence_status: str | None = None
    device_network_info: str | None = None
    metadata_: dict | None = None

class LeaveRequestResponse(BaseModel):
    id: str
    type: str
    app_category: str = "Leave"
    session_type: str | None = None
    priority: str | None = None
    from_date: date
    to_date: date
    reason: str
    status: LeaveStatus
    photo_url: str | None = None
    latitude: float | None = None
    longitude: float | None = None
    location_address: str | None = None
    capture_time: datetime | None = None
    verification_status: str | None = None
    distance_from_campus: float | None = None
    device_id: str | None = None
    location_accuracy: float | None = None
    geo_fence_status: str | None = None
    device_network_info: str | None = None
    metadata_: dict | None = None
    user_name: str | None = None
    user_roll_no: str | None = None
    department_name: str | None = None
    user_id: str | None = None
    remarks: str | None = None
    hod_remarks: str | None = None
    principal_remarks: str | None = None
    rejection_remarks: str | None = None
    num_days: float | None = None
    emergency_contact: str | None = None
    attachment_url: str | None = None
    user_role: str | None = None

class GrievanceRaiseRequest(BaseModel):
    category: str
    subject: str = "General"
    priority: str = "Medium"
    description: str

class GrievanceResponse(BaseModel):
    id: str
    category: str
    subject: str
    priority: str
    description: str
    status: str
    assigned_to: str | None
    date: str
    assigned_officer: str | None = None
    resolution_date: str | None = None
    resolution_rating: int | None = None
    resolution_feedback: str | None = None
    student_name: str | None = None
    student_roll: str | None = None
    student_dept: str | None = None
    hod_name: str | None = None

class TimetableItemResponse(BaseModel):
    id: str
    subject_code: str | None = None
    subject_name: str | None = None
    faculty_name: str | None = None
    room: str
    weekday: str
    start_time: str
    end_time: str

class NoticeResponse(BaseModel):
    id: str
    title: str
    body: str
    audience_type: str
    publish_date: date
    event_date: date | None = None
    audience_types: str | None = None
    degree_id: str | None = None
    batch_id: str | None = None
    department_id: str | None = None
    attachment_url: str | None = None
    priority: str | None = None
    category: str | None = None
    publisher_name: str | None = None
    publisher_role: str | None = None

class StudyMaterialResponse(BaseModel):
    id: str
    title: str
    type: str
    file_url: str
    is_verified: bool
    status: str = "PENDING"
    comments: str | None = None
    faculty_name: str | None = None
    section_name: str | None = None
    uploaded_at: str | None = None
    subject: str | None = None
    semester: int | None = None
    created_at: datetime | None = None

class NotificationResponse(BaseModel):
    id: str
    message: str
    type: str
    is_read: bool
    sent_via: str

class ReceiptResponse(BaseModel):
    id: str
    head: str
    date: str
    mode: str
    amount: float

class StudentLoanRequest(BaseModel):
    bank: str
    branch: str
    sanctioned: float
    interest_rate: float
    emi: float
    outstanding: float

class StudentLoanResponse(BaseModel):
    id: str
    bank: str
    branch: str
    sanctioned: float
    interest_rate: float
    emi: float
    outstanding: float
    status: str

class AssistanceRequestCreate(BaseModel):
    type: str
    reason: str

class AssistanceRequestResponse(BaseModel):
    id: str
    type: str
    reason: str
    status: str
    admin_remarks: str | None = None
    created_at: datetime

class CertificationCreate(BaseModel):
    title: str
    issuer: str
    date: str
    category: str
    type: str = "training"
    file_url: str | None = None

class CertificationResponse(BaseModel):
    id: str
    title: str
    issuer: str
    date: str
    category: str
    type: str
    is_verified: bool
    file_url: str | None = None
