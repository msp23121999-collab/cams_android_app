package com.example.core.network

import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Body
import retrofit2.http.Path
import retrofit2.Response

// Placeholder for LoginRequest/Response models, can be expanded based on the exact API schema
data class LoginRequest(val email: String, val password: String)
data class LoginResponse(val token: String, val user: UserDto)
data class UserDto(val id: String, val email: String, val fullName: String, val role: String)

data class DashboardDto(
    val attendancePercentage: Double,
    val cgpa: Double,
    val upcomingExamsCount: Int,
    val pendingAssignmentsCount: Int
)

data class StudentProfileDto(
    val id: String,
    val fullName: String,
    val rollNo: String,
    val email: String,
    val phone: String,
    val department: String,
    val semester: Int,
    val batchYear: Int,
    val mentorName: String?,
    val mentorEmail: String?,
    val mentorPhone: String?,
    val cgpa: Double?,
    val verificationStatus: String?,
    val dob: String?,
    val gender: String?,
    val bloodGroup: String?,
    val nationality: String?,
    val communityCategory: String?,
    val currentAddress: String?,
    val permanentAddress: String?,
    val aadhaarNo: String?,
    val fatherName: String?,
    val motherName: String?,
    val skills: List<String>?
)

interface CamsApiService {

    @POST("auth/login")
    suspend fun login(@Body request: LoginRequest): Response<LoginResponse>

    @GET("student/dashboard")
    suspend fun getStudentDashboard(): Response<DashboardDto>

    @GET("student/profile")
    suspend fun getStudentProfile(): Response<StudentProfileDto>

    @GET("student/attendance")
    suspend fun getAttendance(): Response<List<AttendanceRecordDto>>

    @GET("student/marks")
    suspend fun getInternalMarks(): Response<List<InternalMarkDto>>

    @GET("student/timetable")
    suspend fun getTimetable(): Response<List<TimetableSlotDto>>

    @GET("student/fees")
    suspend fun getFees(): Response<List<FeeRecordDto>>

    @GET("student/materials")
    suspend fun getStudyMaterials(): Response<List<StudyMaterialDto>>

    @GET("student/assignments")
    suspend fun getAssignments(): Response<List<AssignmentDto>>

    @GET("student/leaves")
    suspend fun getLeaves(): Response<List<LeaveRequestDto>>

    @GET("student/notices")
    suspend fun getNotices(): Response<List<NoticeDto>>

    @GET("student/grievances")
    suspend fun getStudentGrievances(): Response<List<GrievanceDto>>

    @GET("student/online-meetings")
    suspend fun getStudentMeetings(): Response<List<OnlineMeetingDto>>

    @GET("student/legal-events")
    suspend fun getStudentLegalEvents(): Response<List<LegalEventDto>>

    @GET("student/clubs")
    suspend fun getStudentClubs(): Response<List<ClubDto>>

    @GET("academic-calendar")
    suspend fun getAcademicCalendar(): Response<List<CalendarEventDto>>

    @GET("student/syllabus")
    suspend fun getSyllabus(): Response<List<SyllabusDto>>

    @GET("student/notifications")
    suspend fun getNotifications(): Response<List<NotificationDto>>

    @GET("parent/child/profile")
    suspend fun getParentChildProfile(): Response<ParentChildProfileDto>

    @GET("parent/child/marks")
    suspend fun getParentChildMarks(): Response<List<ParentChildMarkDto>>

    @GET("parent/child/attendance")
    suspend fun getParentChildAttendance(): Response<List<ParentChildAttendanceDto>>

    @GET("parent/child/fees")
    suspend fun getParentChildFees(): Response<ParentChildFeeDto>

    @GET("parent/child/timetable")
    suspend fun getParentChildTimetable(): Response<List<ParentChildTimetableDayDto>>

    // Faculty Portal Endpoints
    @GET("faculty/dashboard/metrics")
    suspend fun getFacultyDashboardMetrics(): Response<FacultyDashboardMetricsDto>

    @GET("faculty/subjects")
    suspend fun getFacultySubjects(): Response<List<FacultySubjectDto>>

    @GET("faculty/profile")
    suspend fun getFacultyProfile(): Response<FacultyProfileDto>

    @GET("faculty/research")
    suspend fun getFacultyResearch(): Response<List<ResearchEntryDto>>

    @GET("faculty/activity/summary")
    suspend fun getFacultyActivitySummary(): Response<ActivitySummaryDto>

    @GET("faculty/timetable")
    suspend fun getFacultyTimetable(): Response<List<FacultyTimetableDayDto>>

    // HOD Portal Endpoints
    @GET("hod/dashboard/metrics")
    suspend fun getHODDashboardMetrics(): Response<HODDashboardMetricsDto>

    @GET("hod/activities")
    suspend fun getHODActivities(): Response<List<HODActivityDto>>

    // Principal Portal Endpoints
    @GET("principal/dashboard/stats")
    suspend fun getPrincipalDashboardStats(): Response<PrincipalDashboardStatsDto>

    @GET("principal/approvals/timetable")
    suspend fun getPendingTimetableApprovals(): Response<List<TimetableApprovalDto>>

    @POST("principal/approvals/timetable/{id}")
    suspend fun approveTimetable(@Path("id") id: String, @Body request: ApprovalRequest): Response<Unit>

    @GET("principal/approvals/leave")
    suspend fun getPendingLeaveApprovals(): Response<List<LeaveApprovalDto>>

    @POST("principal/approvals/leave/{id}")
    suspend fun approveLeave(@Path("id") id: String, @Body request: ApprovalRequest): Response<Unit>

    @GET("principal/grievances")
    suspend fun getGrievancesForApproval(): Response<List<GrievanceDto>>

    @GET("principal/research-compliance")
    suspend fun getResearchCompliance(): Response<List<ResearchComplianceDto>>

    @GET("principal/infrastructure")
    suspend fun getInfrastructureDetails(): Response<InfrastructureDto>

    // Admin Portal Endpoints
    @GET("admin/dashboard/metrics")
    suspend fun getAdminDashboardMetrics(): Response<AdminDashboardMetricsDto>

    @GET("admin/setup/degrees")
    suspend fun getDegrees(): Response<List<DegreeDto>>

    @GET("admin/setup/batches")
    suspend fun getBatches(): Response<List<BatchDto>>

    @GET("admin/fees/tracker")
    suspend fun getFeeTracker(): Response<List<FeeTrackerDto>>

    @GET("admin/faculty/payroll")
    suspend fun getPayrollDetails(): Response<List<PayrollDto>>

    @GET("admin/attendance/defaulters")
    suspend fun getAttendanceDefaulters(): Response<List<AttendanceDefaulterDto>>

    @POST("student/clubs/{id}/join")
    suspend fun joinClub(@Path("id") id: Int): Response<Unit>

    @GET("student/council")
    suspend fun getCouncilData(): Response<CouncilDataDto>

    @POST("student/grievances")
    suspend fun submitGrievance(@Body grievance: GrievanceRequest): Response<Unit>

    @GET("student/lexnova/stats")
    suspend fun getLexNovaStats(): Response<List<LexNovaKpiDto>>

    @GET("student/internship-drives")
    suspend fun getInternshipDrives(): Response<List<InternshipDriveDto>>

    @GET("student/internships")
    suspend fun getInternships(): Response<List<InternshipRecordDto>>

    @GET("student/certifications")
    suspend fun getCertifications(): Response<List<CertificationRecordDto>>

    @GET("student/activity-points")
    suspend fun getActivityPoints(): Response<List<ActivityPointApplicationDto>>

    @GET("student/community-service")
    suspend fun getCommunityService(): Response<CommunityServiceDataDto>

    @GET("student/innovation-projects")
    suspend fun getInnovationProjects(): Response<List<InnovationProjectDto>>

    @GET("api/parent/child/performance")
    suspend fun getParentChildPerformance(): Response<List<PerformanceDataDto>>

    @GET("api/parent/child/subject-attendance")
    suspend fun getParentChildSubjectAttendance(): Response<List<SubjectAttendanceDto>>

    @GET("faculty/assignments")
    suspend fun getFacultyAssignments(): Response<List<FacultyAssignmentDto>>

    @GET("faculty/students")
    suspend fun getFacultyStudents(): Response<List<FacultyStudentDto>>

    @GET("faculty/study-materials")
    suspend fun getFacultyMaterials(): Response<List<FacultyMaterialDto>>

    @GET("faculty/recordings")
    suspend fun getFacultyRecordings(): Response<List<FacultyRecordingDto>>
}

data class InnovationProjectDto(
    val id: String,
    val title: String,
    val abstract: String,
    val authors: List<String>,
    val tags: List<String>,
    val likes: Int,
    val comments: Int,
    val date: String
)

data class LegalEventDto(
    val id: String,
    val title: String,
    val category: String,
    val date: String,
    val time: String,
    val status: String,
    val speakerName: String,
    val activityPoints: Int
)

data class ClubDto(val id: Int, val name: String, val description: String, val category: String, val membersCount: Int, val userRole: String)
data class CouncilDataDto(val representatives: List<CouncilRepDto>, val initiatives: List<InitiativeDto>, val feedback: List<FeedbackDto>)
data class CouncilRepDto(val name: String, val role: String, val year: String, val imageUrl: String?)
data class InitiativeDto(val id: Int, val title: String, val status: String, val progress: Float, val category: String)
data class FeedbackDto(val id: Int, val title: String, val status: String, val upvotes: Int)
data class GrievanceDto(val id: String, val date: String, val category: String, val subject: String, val priority: String, val status: String, val description: String, val assignedOfficer: String? = null, val resolutionDate: String? = null, val resolutionRating: Int? = null, val resolutionFeedback: String? = null)
data class GrievanceRequest(val category: String, val priority: String, val subject: String, val description: String)
data class OnlineMeetingDto(val id: String, val title: String, val category: String, val organizer: String, val date: String, val time: String, val duration: String, val platform: String, val meetingLink: String, val status: String, val participants: Int)
data class LexNovaKpiDto(val title: String, val value: String, val subtitle: String, val type: String)
data class InternshipDriveDto(val id: String, val company: String, val role: String, val stipend: String, val status: String, val deadline: String)
data class InternshipRecordDto(val id: String, val organization: String, val sector: String, val role: String, val startDate: String, val endDate: String, val mentor: String, val description: String, val status: String)
data class CertificationRecordDto(val id: String, val title: String, val issuer: String, val date: String, val category: String, val isVerified: Boolean, val type: String)
data class ActivityPointApplicationDto(val id: String, val title: String, val category: String, val date: String, val pointsClaimed: Int, val pointsAwarded: Int?, val status: String, val description: String, val attachmentUrl: String?, val approvedBy: String?, val approvedDate: String?, val remarks: String?)
data class CommunityServiceDataDto(val opportunities: List<ServiceOpportunityDto>, val logs: List<ServiceLogDto>)
data class ServiceOpportunityDto(val id: Int, val title: String, val organizer: String, val date: String, val location: String, val slots: Int, val duration: String, val tags: List<String>)
data class ServiceLogDto(val id: Int, val title: String, val date: String, val hours: Int, val status: String, val isVerified: Boolean)
data class FacultyAssignmentDto(
    val id: String,
    val title: String,
    val dueDate: String,
    val submitted: Int,
    val total: Int,
    val status: String
)

data class FacultyStudentDto(
    val id: String,
    val name: String,
    val rollNo: String,
    val email: String,
    val phone: String,
    val semester: Int,
    val batch: String,
    val attendance: Double,
    val cgpa: Double
)

data class FacultyMaterialDto(
    val id: String,
    val title: String,
    val subject: String,
    val type: String,
    val date: String,
    val size: String,
    val downloads: Int
)

data class FacultyRecordingDto(
    val id: String,
    val title: String,
    val subject: String,
    val date: String,
    val duration: String,
    val views: Int
)

data class ApprovalRequest(val status: String, val remarks: String)

data class PrincipalDashboardStatsDto(
    val totalStudents: Int,
    val totalFaculty: Int,
    val activeApprovals: Int,
    val revenueThisMonth: Double
)

data class TimetableApprovalDto(
    val id: String,
    val facultyName: String,
    val subjectName: String,
    val requestedChanges: String,
    val date: String
)

data class LeaveApprovalDto(
    val id: String,
    val applicantName: String,
    val leaveType: String,
    val startDate: String,
    val endDate: String,
    val reason: String
)

data class ResearchComplianceDto(
    val id: String,
    val title: String,
    val researcher: String,
    val status: String,
    val complianceCheck: String
)

data class InfrastructureDto(
    val totalLabs: Int,
    val activeClassrooms: Int,
    val libraryStatus: String,
    val maintenanceRequests: Int
)

data class AdminDashboardMetricsDto(
    val totalUsers: Int,
    val onlineNow: Int,
    val storageUsed: String,
    val systemHealth: String
)

data class DegreeDto(val id: String, val name: String, val code: String)
data class BatchDto(val id: String, val year: String, val status: String)

data class FeeTrackerDto(
    val studentName: String,
    val rollNo: String,
    val totalFees: Double,
    val paid: Double,
    val pending: Double
)

data class PayrollDto(
    val facultyId: String,
    val name: String,
    val basicPay: Double,
    val allowances: Double,
    val deductions: Double,
    val netSalary: Double
)

data class AttendanceDefaulterDto(
    val studentName: String,
    val rollNo: String,
    val attendancePercentage: Double,
    val parentContact: String
)

data class HODDashboardMetricsDto(
    val totalFaculty: String,
    val totalStudents: String,
    val pendingApprovals: String,
    val activeSubjects: String
)

data class HODActivityDto(
    val title: String,
    val time: String,
    val type: String
)

data class FacultyDashboardMetricsDto(
    val classesToday: String,
    val pendingAttendance: String,
    val pendingAssignments: String,
    val leaveBalance: String
)

data class FacultySubjectDto(
    val subjectCode: String,
    val subjectName: String,
    val degreeCode: String?,
    val section: String,
    val year: Int,
    val semester: Int,
    val batch: String
)

data class FacultyProfileDto(
    val facultyId: String,
    val fullName: String,
    val designation: String,
    val departmentName: String,
    val email: String,
    val phone: String,
    val employeeCode: String,
    val specialization: String,
    val qualifications: List<QualificationDto>,
    val experience: List<ExperienceDto>
)

data class QualificationDto(
    val degree: String,
    val specialization: String,
    val university: String,
    val institution: String,
    val yearOfCompletion: Int,
    val percentageCgpa: String
)

data class ExperienceDto(
    val institutionName: String,
    val designation: String,
    val fromDate: String,
    val toDate: String,
    val totalYears: Double
)

data class ResearchEntryDto(
    val id: String,
    val title: String,
    val publication: String?,
    val researchType: String,
    val publicationDate: String?
)

data class ActivitySummaryDto(
    val classesConducted: Int,
    val attendanceMarked: Int,
    val studyMaterialsUploaded: Int,
    val assignmentsCreated: Int,
    val leaveRequestsSubmitted: Int
)

data class FacultyTimetableDayDto(
    val day: String,
    val periods: List<FacultyTimetablePeriodDto>
)

data class FacultyTimetablePeriodDto(
    val periodNo: Int,
    val time: String,
    val subject: String,
    val code: String,
    val room: String,
    val batch: String
)

// DTOs for parent portal
data class ParentChildProfileDto(
    val id: String,
    val fullName: String,
    val rollNo: String,
    val semester: String,
    val batch: String,
    val cgpa: Double,
    val mentorName: String,
    val mentorEmail: String,
    val mentorPhone: String,
    val dob: String,
    val gender: String,
    val bloodGroup: String,
    val nationality: String,
    val aadhaarNo: String,
    val contactMobile: String,
    val contactEmail: String,
    val emergencyContact: String,
    val emergencyPhone: String,
    val fatherName: String,
    val fatherOccupation: String,
    val fatherMobile: String,
    val fatherEmail: String,
    val motherName: String,
    val motherOccupation: String,
    val motherMobile: String,
    val motherEmail: String,
    val certifications: List<ParentChildCertificationDto>
)

data class ParentChildCertificationDto(
    val title: String,
    val issuer: String,
    val category: String,
    val date: String,
    val status: String
)

data class ParentChildMarkDto(
    val subject: String,
    val academicYear: String,
    val internal1: String,
    val internal2: String,
    val model: String,
    val assignments: String,
    val attendance: String,
    val total: String
)

data class ParentChildAttendanceDto(
    val date: String,
    val status: String
)

data class ParentChildFeeDto(
    val totalFees: Double,
    val scholarshipDeduction: Double,
    val otherDeductions: Double,
    val netFees: Double,
    val amountPaid: Double,
    val pendingBalance: Double,
    val dueDate: String,
    val records: List<ParentChildFeeRecordDto>
)

data class ParentChildFeeRecordDto(
    val id: String,
    val title: String,
    val amount: Double,
    val date: String,
    val status: String
)

data class ParentChildTimetableDayDto(
    val day: String,
    val periods: List<ParentChildTimetablePeriodDto>
)

data class ParentChildTimetablePeriodDto(
    val periodNo: Int,
    val time: String,
    val subject: String,
    val code: String,
    val room: String,
    val faculty: String
)

data class PerformanceDataDto(
    val semester: String,
    val gpa: Double,
    val attendance: Int
)

data class SubjectAttendanceDto(
    val subject: String,
    val totalClasses: Int,
    val attendedClasses: Int,
    val percentage: Int
)

// DTOs for student portal
data class AttendanceRecordDto(
    val id: String,
    val date: String,
    val status: String,
    val subjectName: String,
    val subjectCode: String
)

data class InternalMarkDto(
    val id: String,
    val subjectName: String,
    val subjectCode: String,
    val markObtained: Double,
    val maxMark: Double,
    val component: String // e.g. "Internal Assessment 1"
)

data class TimetableSlotDto(
    val id: String,
    val dayOfWeek: String,
    val startTime: String,
    val endTime: String,
    val subjectName: String,
    val subjectCode: String,
    val facultyName: String,
    val roomNo: String
)

data class FeeRecordDto(
    val id: String,
    val title: String,
    val amount: Double,
    val dueDate: String,
    val status: String // "Paid", "Pending"
)

data class StudyMaterialDto(
    val id: String,
    val title: String,
    val subjectName: String,
    val fileUrl: String,
    val uploadDate: String
)

data class AssignmentDto(
    val id: String,
    val title: String,
    val subjectName: String,
    val dueDate: String,
    val status: String, // "Submitted", "Pending"
    val description: String?
)

data class LeaveRequestDto(
    val id: String,
    val startDate: String,
    val endDate: String,
    val reason: String,
    val status: String, // "Approved", "Pending", "Rejected"
    val appliedDate: String
)

data class NoticeDto(
    val id: String,
    val title: String,
    val content: String,
    val date: String,
    val category: String
)

data class CalendarEventDto(
    val id: String,
    val date: String,
    val eventName: String,
    val isHoliday: Boolean
)

data class SyllabusDto(
    val id: String,
    val subjectName: String,
    val subjectCode: String,
    val semester: Int,
    val fileUrl: String
)

data class NotificationDto(
    val id: String,
    val title: String,
    val message: String,
    val date: String,
    val isRead: Boolean
)
