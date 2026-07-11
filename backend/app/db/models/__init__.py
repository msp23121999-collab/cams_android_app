from app.db.models.academic import Course, Department, Exam, ExamSetting, Section, Timetable, TimetableApproval, Degree, SystemSetting, SystemSettingHistory, AcademicYear
from app.db.models.attendance import Attendance, StaffAttendance, AttendanceCorrection
from app.db.models.audit import ActivityLog, AuditLog
from app.db.models.chatbot import ChatSession, ChatMessage
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
from app.db.models.backup import BackupConfiguration, BackupHistory
from app.db.models.pf import PFConfiguration, PFHistoricalPeriod, PFContribution, PFClaim, PFAuditLog, PFCalculationMethod, PFLeaveExclusion

__all__ = [
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

