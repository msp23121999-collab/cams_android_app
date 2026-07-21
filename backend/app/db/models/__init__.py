from app.db.models.academic import Course, Department, Exam, ExamSetting, Section, Timetable, TimetableApproval, Degree, SystemSetting, SystemSettingHistory, AcademicYear
from app.db.models.attendance import Attendance, StaffAttendance, AttendanceCorrection
from app.db.models.audit import ActivityLog, AuditLog
from app.db.models.chatbot import ChatSession, ChatMessage
from app.db.models.device_token import DeviceToken
from app.db.models.communication import Notice, NoticeAcknowledgement, Notification, Message
from app.db.models.faculty import FacultyProfile, FacultyResearch, FacultyWorkload, PublicationPlan, ResearchCompliance, FacultyProfileUpdateRequest
from app.db.models.fee import FeeRecord, FeeStructure, Payment
from app.db.models.grievance import Grievance
from app.db.models.leave import LeaveApproval, LeaveRequest
from app.db.models.marks import Mark
from app.db.models.payroll import Deduction, Salary, SalarySlip, SalarySlipRequest
from app.db.models.research import ResearchPlan, ResearchProgressUpdate, PublicationProof, ResearchVerification
from app.db.models.student import ParentStudentMap, Student, MentorshipRecord
from app.db.models.study_material import Assignment, StudyMaterial
from app.db.models.substitution import FacultyAbsence, SubstitutionAllocation
from app.db.models.user import User
from app.db.models.class_advisor import ClassAdvisor
from app.db.models.class_diary import ClassDiary
from app.db.models.backup import BackupConfiguration, BackupHistory
from app.db.models.pf import PFConfiguration, PFHistoricalPeriod, PFContribution, PFClaim, PFAuditLog, PFCalculationMethod, PFLeaveExclusion
from app.db.models.password_reset import PasswordResetToken
from app.db.models.moot_court import MootCourtMemorial
from app.db.models.citation import SavedCitation
from app.db.models.internship import InternshipDrive, InternshipApplication, PartnerCompany
from app.db.models.activity_point_category import ActivityPointCategory
from app.db.models.hostel import HostelBlock, HostelRoom, HostelAllocation
from app.db.models.inventory import InventoryItem, InventoryTransaction
from app.db.models.library import LibraryBook, LibraryIssue
from app.db.models.transport import TransportRoute, TransportVehicle, TransportPass
from app.db.models.budget import BudgetLineItem, BudgetExpense, Grant

__all__ = [
    "HostelBlock", "HostelRoom", "HostelAllocation",
    "InventoryItem", "InventoryTransaction",
    "LibraryBook", "LibraryIssue",
    "TransportRoute", "TransportVehicle", "TransportPass",
    "BudgetLineItem", "BudgetExpense", "Grant",
    "MootCourtMemorial",
    "SavedCitation",
    "InternshipDrive",
    "InternshipApplication",
    "PartnerCompany",
    "ActivityPointCategory",
    "AcademicYear",
    "ActivityLog",
    "Assignment",
    "Attendance",
    "StaffAttendance",
    "AttendanceCorrection",
    "AuditLog",
    "BackupConfiguration",
    "BackupHistory",
    "ChatSession",
    "ChatMessage",
    "ClassAdvisor",
    "ClassDiary",
    "Course",
    "Deduction",
    "Department",
    "Exam",
    "ExamSetting",
    "FacultyAbsence",
    "FacultyProfile",
    "FacultyProfileUpdateRequest",
    "FacultyResearch",
    "FacultyWorkload",
    "PublicationPlan",
    "ResearchCompliance",
    "FeeRecord",
    "FeeStructure",
    "Grievance",
    "LeaveApproval",
    "LeaveRequest",
    "Mark",
    "Message",
    "Notice",
    "NoticeAcknowledgement",
    "Notification",
    "ParentStudentMap",
    "Payment",
    "PasswordResetToken",
    "PFConfiguration",
    "PFHistoricalPeriod",
    "PFContribution",
    "PFClaim",
    "PFAuditLog",
    "PFCalculationMethod",
    "PFLeaveExclusion",
    "Degree",
    "ResearchPlan",
    "ResearchProgressUpdate",
    "PublicationProof",
    "ResearchVerification",
    "Salary",
    "SalarySlip",
    "SalarySlipRequest",
    "Section",
    "Student",
    "MentorshipRecord",
    "StudyMaterial",
    "SubstitutionAllocation",
    "SystemSetting",
    "SystemSettingHistory",
    "Timetable",
    "TimetableApproval",
    "User",
]

