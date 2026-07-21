# 07. Database Documentation

PostgreSQL Schema and SQLAlchemy ORM Models.

### Academic Definitions
**File:** `backend/app/db/models/academic.py`

- **Table Model:** `ApprovalStatus`
- **Table Model:** `Weekday`
- **Table Model:** `ExamType`
- **Table Model:** `Department`
- **Table Model:** `Course`
- **Table Model:** `Section`
- **Table Model:** `Timetable`
- **Table Model:** `TimetableApproval`
- **Table Model:** `Exam`
- **Table Model:** `ExamSetting`
- **Table Model:** `Degree`
- **Table Model:** `SystemSetting`
- **Table Model:** `SystemSettingHistory`
- **Table Model:** `AcademicYear`
- **Table Model:** `SubjectAllocation`

### Activity_point_category Definitions
**File:** `backend/app/db/models/activity_point_category.py`

- **Table Model:** `ActivityPointCategory`

### Attendance Definitions
**File:** `backend/app/db/models/attendance.py`

- **Table Model:** `AttendanceStatus`
- **Table Model:** `Attendance`
- **Table Model:** `StaffAttendance`
- **Table Model:** `AttendanceCorrection`

### Audit Definitions
**File:** `backend/app/db/models/audit.py`

- **Table Model:** `AuditLog`
- **Table Model:** `ActivityLog`

### Backup Definitions
**File:** `backend/app/db/models/backup.py`

- **Table Model:** `BackupConfiguration`
- **Table Model:** `BackupHistory`

### Budget Definitions
**File:** `backend/app/db/models/budget.py`

- **Table Model:** `BudgetStatus`
- **Table Model:** `GrantStatus`
- **Table Model:** `BudgetLineItem`
- **Table Model:** `BudgetExpense`
- **Table Model:** `Grant`

### Certification Definitions
**File:** `backend/app/db/models/certification.py`

- **Table Model:** `Certification`

### Chatbot Definitions
**File:** `backend/app/db/models/chatbot.py`

- **Table Model:** `MessageRole`
- **Table Model:** `ChatSession`
- **Table Model:** `ChatMessage`

### Citation Definitions
**File:** `backend/app/db/models/citation.py`

- **Table Model:** `SavedCitation`

### Classroom Definitions
**File:** `backend/app/db/models/classroom.py`

- **Table Model:** `ClassroomActivity`
- **Table Model:** `StudentInteraction`
- **Table Model:** `SessionSummary`

### Class_advisor Definitions
**File:** `backend/app/db/models/class_advisor.py`

- **Table Model:** `ClassAdvisor`

### Class_diary Definitions
**File:** `backend/app/db/models/class_diary.py`

- **Table Model:** `ClassDiary`

### Club Definitions
**File:** `backend/app/db/models/club.py`

- **Table Model:** `Club`
- **Table Model:** `ClubMembership`
- **Table Model:** `ClubAnnouncement`

### Communication Definitions
**File:** `backend/app/db/models/communication.py`

- **Table Model:** `Notice`
- **Table Model:** `NoticeAcknowledgement`
- **Table Model:** `Notification`
- **Table Model:** `Message`

### Device_token Definitions
**File:** `backend/app/db/models/device_token.py`

- **Table Model:** `DeviceToken`

### Faculty Definitions
**File:** `backend/app/db/models/faculty.py`

- **Table Model:** `FacultyProfile`
- **Table Model:** `FacultyWorkload`
- **Table Model:** `FacultyResearch`
- **Table Model:** `PublicationPlan`
- **Table Model:** `ResearchCompliance`
- **Table Model:** `FacultyProfileUpdateRequest`

### Fee Definitions
**File:** `backend/app/db/models/fee.py`

- **Table Model:** `FeeStatus`
- **Table Model:** `FeeStructure`
- **Table Model:** `FeeRecord`
- **Table Model:** `Payment`

### Financial_aid Definitions
**File:** `backend/app/db/models/financial_aid.py`

- **Table Model:** `StudentLoan`
- **Table Model:** `FinancialAssistanceRequest`

### Grievance Definitions
**File:** `backend/app/db/models/grievance.py`

- **Table Model:** `Grievance`

### Hall_ticket Definitions
**File:** `backend/app/db/models/hall_ticket.py`

- **Table Model:** `HallTicket`

### Hostel Definitions
**File:** `backend/app/db/models/hostel.py`

- **Table Model:** `HostelType`
- **Table Model:** `AllocationStatus`
- **Table Model:** `HostelBlock`
- **Table Model:** `HostelRoom`
- **Table Model:** `HostelAllocation`

### Internship Definitions
**File:** `backend/app/db/models/internship.py`

- **Table Model:** `InternshipDrive`
- **Table Model:** `InternshipApplication`
- **Table Model:** `PartnerCompany`

### Inventory Definitions
**File:** `backend/app/db/models/inventory.py`

- **Table Model:** `StockMovement`
- **Table Model:** `InventoryItem`
- **Table Model:** `InventoryTransaction`

### Leave Definitions
**File:** `backend/app/db/models/leave.py`

- **Table Model:** `LeaveStatus`
- **Table Model:** `LeaveRequest`
- **Table Model:** `LeaveApproval`
- **Table Model:** `LeaveBalance`

### Library Definitions
**File:** `backend/app/db/models/library.py`

- **Table Model:** `IssueStatus`
- **Table Model:** `LibraryBook`
- **Table Model:** `LibraryIssue`

### Marks Definitions
**File:** `backend/app/db/models/marks.py`

- **Table Model:** `MarkExamType`
- **Table Model:** `Mark`
- **Table Model:** `InternalMark`

### Mixins Definitions
**File:** `backend/app/db/models/mixins.py`

- **Table Model:** `TimestampSoftDeleteMixin`

### Moot_court Definitions
**File:** `backend/app/db/models/moot_court.py`

- **Table Model:** `MootCourtMemorial`

### Password_reset Definitions
**File:** `backend/app/db/models/password_reset.py`

- **Table Model:** `PasswordResetToken`

### Payroll Definitions
**File:** `backend/app/db/models/payroll.py`

- **Table Model:** `DeductionType`
- **Table Model:** `Salary`
- **Table Model:** `Deduction`
- **Table Model:** `SalarySlip`
- **Table Model:** `SalarySlipRequest`
- **Table Model:** `WorkingDayConfig`

### Pf Definitions
**File:** `backend/app/db/models/pf.py`

- **Table Model:** `PFCalculationMethod`
- **Table Model:** `PFConfiguration`
- **Table Model:** `PFHistoricalPeriod`
- **Table Model:** `PFContribution`
- **Table Model:** `PFClaim`
- **Table Model:** `PFAuditLog`
- **Table Model:** `PFLeaveExclusion`

### Research Definitions
**File:** `backend/app/db/models/research.py`

- **Table Model:** `ResearchPlanStatus`
- **Table Model:** `ProofStatus`
- **Table Model:** `ResearchPlan`
- **Table Model:** `ResearchProgressUpdate`
- **Table Model:** `PublicationProof`
- **Table Model:** `ResearchVerification`

### Student Definitions
**File:** `backend/app/db/models/student.py`

- **Table Model:** `Student`
- **Table Model:** `ParentStudentMap`
- **Table Model:** `MentorshipRecord`

### Study_material Definitions
**File:** `backend/app/db/models/study_material.py`

- **Table Model:** `StudyMaterial`
- **Table Model:** `Assignment`
- **Table Model:** `AssignmentSubmission`

### Substitution Definitions
**File:** `backend/app/db/models/substitution.py`

- **Table Model:** `SubstitutionStatus`
- **Table Model:** `AllocationMethod`
- **Table Model:** `FacultyAbsence`
- **Table Model:** `SubstitutionAllocation`

### Transport Definitions
**File:** `backend/app/db/models/transport.py`

- **Table Model:** `VehicleStatus`
- **Table Model:** `PassStatus`
- **Table Model:** `TransportRoute`
- **Table Model:** `TransportVehicle`
- **Table Model:** `TransportPass`

### User Definitions
**File:** `backend/app/db/models/user.py`

- **Table Model:** `UserRole`
- **Table Model:** `User`

