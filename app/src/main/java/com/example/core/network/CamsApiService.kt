package com.example.core.network

import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Body
import retrofit2.http.Path
import retrofit2.Response
import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class LoginRequest(val email: String, val password: String)

@JsonClass(generateAdapter = true)
data class LoginResponse(
    @Json(name = "access_token") val accessToken: String,
    @Json(name = "refresh_token") val refreshToken: String?,
    @Json(name = "subdomain_target") val subdomainTarget: String,
    val role: String
)

@JsonClass(generateAdapter = true)
data class UserMeResponse(
    val id: String,
    val email: String,
    @Json(name = "full_name") val fullName: String,
    val role: String,
    @Json(name = "department_id") val departmentId: String?
)

@JsonClass(generateAdapter = true)
data class DashboardDto(
    val metrics: List<MetricDto>
)

@JsonClass(generateAdapter = true)
data class MetricDto(
    val id: String,
    val label: String,
    val value: String
)

@JsonClass(generateAdapter = true)
data class StudentProfileDto(
    @Json(name = "full_name") val fullName: String?,
    @Json(name = "date_of_birth") val dob: String?,
    val gender: String?,
    @Json(name = "blood_group") val bloodGroup: String?,
    val nationality: String?,
    @Json(name = "mobile_number") val phone: String?,
    @Json(name = "current_address") val currentAddress: String?,
    @Json(name = "permanent_address") val permanentAddress: String?,
    @Json(name = "aadhaar_number") val aadhaarNo: String?,
    @Json(name = "passport_number") val passportNo: String?,
    @Json(name = "community_category") val communityCategory: String?,
    val religion: String?,
    @Json(name = "father_name") val fatherName: String?,
    @Json(name = "mother_name") val motherName: String?,
    @Json(name = "verification_status") val verificationStatus: String?,
    val semester: Int? = null,
    val department: String? = null
)

interface CamsApiService {

    @POST("auth/login")
    suspend fun login(@Body request: LoginRequest): Response<LoginResponse>

    @GET("auth/me")
    suspend fun getCurrentUser(): Response<UserMeResponse>

    @GET("students/dashboard")
    suspend fun getStudentDashboard(): Response<DashboardDto>

    @GET("students/profile")
    suspend fun getStudentProfile(): Response<StudentProfileDto>

    @GET("students/attendance")
    suspend fun getAttendance(): Response<AttendanceSummaryResponse>

    @GET("students/marks")
    suspend fun getInternalMarks(): Response<List<InternalMarkDto>>

    @GET("students/fees")
    suspend fun getFees(): Response<StudentFeeSummaryResponse>

    @GET("students/timetable")
    suspend fun getTimetable(): Response<List<TimetableSlotDto>>

    @GET("students/study-materials")
    suspend fun getStudyMaterials(): Response<List<StudyMaterialDto>>

    @GET("assignments/active-assignments")
    suspend fun getAssignments(): Response<List<AssignmentDto>>

    @POST("assignments/submit/{asg_id}")
    suspend fun submitAssignment(
        @Path("asg_id") asgId: String,
        @Body payload: AssignmentSubmitRequest
    ): Response<Unit>

    @GET("students/leaves")
    suspend fun getLeaves(): Response<List<LeaveRequestDto>>

    @POST("students/leaves/apply")
    suspend fun applyLeave(@Body request: LeaveApplicationRequest): Response<LeaveRequestDto>

    @GET("students/notices")
    suspend fun getNotices(): Response<List<NoticeDto>>

    @GET("students/grievances")
    suspend fun getStudentGrievances(): Response<List<GrievanceDto>>

    @POST("students/grievances/raise")
    suspend fun raiseGrievance(@Body grievance: GrievanceRaiseRequest): Response<GrievanceDto>

    @GET("students/online-meetings")
    suspend fun getStudentMeetings(): Response<List<OnlineMeetingDto>>

    @GET("students/legal-events")
    suspend fun getStudentLegalEvents(): Response<List<LegalEventDto>>

    @GET("students/clubs")
    suspend fun getStudentClubs(): Response<List<ClubDto>>

    @GET("students/academic-calendar")
    suspend fun getAcademicCalendar(): Response<List<CalendarEventDto>>

    @GET("students/syllabus")
    suspend fun getSyllabus(): Response<List<SyllabusDto>>

    @GET("students/notifications")
    suspend fun getNotifications(): Response<List<NotificationDto>>

    @GET("parents/child/profile")
    suspend fun getParentChildProfile(): Response<ParentChildProfileDto>

    @GET("parents/child/marks")
    suspend fun getParentChildMarks(): Response<List<ParentChildMarkDto>>

    @GET("parents/child/attendance")
    suspend fun getParentChildAttendance(): Response<List<ParentChildAttendanceDto>>

    @GET("parents/child/fees")
    suspend fun getParentChildFees(): Response<ParentChildFeeDto>

    @GET("parents/child/timetable")
    suspend fun getParentChildTimetable(): Response<List<ParentChildTimetableDayDto>>

    // Faculty Portal Endpoints
    @GET("faculties/dashboard/metrics")
    suspend fun getFacultyDashboardMetrics(): Response<FacultyDashboardMetricsDto>

    @GET("faculties/subjects")
    suspend fun getFacultySubjects(): Response<List<FacultySubjectDto>>

    @GET("faculties/profile")
    suspend fun getFacultyProfile(): Response<FacultyProfileDto>

    @GET("faculties/research")
    suspend fun getFacultyResearch(): Response<List<ResearchEntryDto>>

    @GET("faculties/activity/summary")
    suspend fun getFacultyActivitySummary(): Response<ActivitySummaryDto>

    @GET("faculties/timetable")
    suspend fun getFacultyTimetable(): Response<List<FacultyTimetableDayDto>>

    // HOD Portal Endpoints
    @GET("hods/dashboard/metrics")
    suspend fun getHODDashboardMetrics(): Response<HODDashboardMetricsDto>

    @GET("hods/activities")
    suspend fun getHODActivities(): Response<List<HODActivityDto>>

    // Principal Portal Endpoints
    @GET("principals/dashboard/stats")
    suspend fun getPrincipalDashboardStats(): Response<PrincipalDashboardStatsDto>

    @GET("principals/approvals/timetable")
    suspend fun getPendingTimetableApprovals(): Response<List<TimetableApprovalDto>>

    @POST("principals/approvals/timetable/{id}")
    suspend fun approveTimetable(@Path("id") id: String, @Body request: ApprovalRequest): Response<Unit>

    @GET("principals/approvals/leave")
    suspend fun getPendingLeaveApprovals(): Response<List<LeaveApprovalDto>>

    @POST("principals/approvals/leave/{id}")
    suspend fun approveLeave(@Path("id") id: String, @Body request: ApprovalRequest): Response<Unit>

    @GET("principals/grievances")
    suspend fun getGrievancesForApproval(): Response<List<GrievanceDto>>

    @GET("principals/research-compliance")
    suspend fun getResearchCompliance(): Response<List<ResearchComplianceDto>>

    @GET("principals/infrastructure")
    suspend fun getInfrastructureDetails(): Response<InfrastructureDto>

    // Admin Portal Endpoints
    @GET("admins/dashboard/metrics")
    suspend fun getAdminDashboardMetrics(): Response<AdminDashboardMetricsDto>

    @GET("admins/setup/degrees")
    suspend fun getDegrees(): Response<List<DegreeDto>>

    @GET("admins/setup/batches")
    suspend fun getBatches(): Response<List<BatchDto>>

    @GET("admins/fees/tracker")
    suspend fun getFeeTracker(): Response<List<FeeTrackerDto>>

    @GET("admins/faculty/payroll")
    suspend fun getPayrollDetails(): Response<List<PayrollDto>>

    @GET("admins/attendance/defaulters")
    suspend fun getAttendanceDefaulters(): Response<List<AttendanceDefaulterDto>>

    @POST("students/clubs/{id}/join")
    suspend fun joinClub(@Path("id") id: Int): Response<Unit>

    @GET("students/council")
    suspend fun getCouncilData(): Response<CouncilDataDto>

    @POST("students/grievances")
    suspend fun submitGrievance(@Body grievance: GrievanceRaiseRequest): Response<Unit>

    @GET("students/lexnova/stats")
    suspend fun getLexNovaStats(): Response<List<LexNovaKpiDto>>

    @GET("students/internship-drives")
    suspend fun getInternshipDrives(): Response<List<InternshipDriveDto>>

    @GET("students/internships")
    suspend fun getInternships(): Response<List<InternshipRecordDto>>

    @GET("students/certifications")
    suspend fun getCertifications(): Response<List<CertificationRecordDto>>

    @GET("students/activity-points")
    suspend fun getActivityPoints(): Response<List<ActivityPointDto>>

    @POST("students/activity-points/claim")
    suspend fun claimActivityPoints(@Body request: ActivityPointClaimRequest): Response<Unit>

    @GET("students/community-service")
    suspend fun getCommunityService(): Response<CommunityServiceDataDto>

    @GET("students/innovation-projects")
    suspend fun getInnovationProjects(): Response<List<InnovationProjectDto>>

    @GET("parents/child/performance")
    suspend fun getParentChildPerformance(): Response<List<PerformanceDataDto>>

    @GET("parents/child/subject-attendance")
    suspend fun getParentChildSubjectAttendance(): Response<List<SubjectAttendanceDto>>

    @GET("faculties/assignments")
    suspend fun getFacultyAssignments(): Response<List<FacultyAssignmentDto>>

    @GET("faculties/students")
    suspend fun getFacultyStudents(): Response<List<FacultyStudentDto>>

    @GET("faculties/study-materials")
    suspend fun getFacultyMaterials(): Response<List<FacultyMaterialDto>>

    @GET("faculties/recordings")
    suspend fun getFacultyRecordings(): Response<List<FacultyRecordingDto>>
}

@JsonClass(generateAdapter = true)
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

@JsonClass(generateAdapter = true)
data class LegalEventDto(
    val id: String,
    val title: String,
    val category: String,
    val date: String,
    val time: String,
    val status: String,
    @Json(name = "speaker_name") val speakerName: String,
    @Json(name = "activity_points") val activityPoints: Int
)

@JsonClass(generateAdapter = true)
data class ClubDto(
    val id: Int, 
    val name: String, 
    val description: String, 
    val category: String, 
    @Json(name = "members_count") val membersCount: Int, 
    @Json(name = "user_role") val userRole: String
)

@JsonClass(generateAdapter = true)
data class CouncilDataDto(
    val representatives: List<CouncilRepDto>, 
    val initiatives: List<InitiativeDto>, 
    val feedback: List<FeedbackDto>
)

@JsonClass(generateAdapter = true)
data class CouncilRepDto(
    val name: String, 
    val role: String, 
    val year: String, 
    @Json(name = "image_url") val imageUrl: String?
)

@JsonClass(generateAdapter = true)
data class InitiativeDto(
    val id: Int, 
    val title: String, 
    val status: String, 
    val progress: Float, 
    val category: String
)

@JsonClass(generateAdapter = true)
data class FeedbackDto(
    val id: Int, 
    val title: String, 
    val status: String, 
    val upvotes: Int
)

@JsonClass(generateAdapter = true)
data class GrievanceDto(
    val id: String, 
    val category: String, 
    val subject: String,
    val description: String, 
    val priority: String,
    val status: String,
    val date: String,
    @Json(name = "assigned_officer") val assignedOfficer: String? = null,
    @Json(name = "resolution_date") val resolutionDate: String? = null,
    @Json(name = "resolution_rating") val resolutionRating: Int? = null,
    @Json(name = "resolution_feedback") val resolutionFeedback: String? = null
)

@JsonClass(generateAdapter = true)
data class GrievanceRaiseRequest(
    val category: String, 
    val subject: String,
    val description: String,
    val priority: String = "Medium"
)

@JsonClass(generateAdapter = true)
data class OnlineMeetingDto(
    val id: String, 
    val title: String, 
    val category: String, 
    val organizer: String, 
    val date: String, 
    val time: String, 
    val duration: String, 
    val platform: String, 
    @Json(name = "meeting_link") val meetingLink: String, 
    val status: String, 
    val participants: Int
)

@JsonClass(generateAdapter = true)
data class LexNovaKpiDto(
    val title: String, 
    val value: String, 
    val subtitle: String, 
    val type: String
)

@JsonClass(generateAdapter = true)
data class InternshipDriveDto(
    val id: String, 
    val company: String, 
    val role: String, 
    val stipend: String, 
    val status: String, 
    val deadline: String
)

@JsonClass(generateAdapter = true)
data class InternshipRecordDto(
    val id: String, 
    val organization: String, 
    val sector: String, 
    val role: String, 
    @Json(name = "start_date") val startDate: String, 
    @Json(name = "end_date") val endDate: String, 
    val mentor: String, 
    val description: String, 
    val status: String
)

@JsonClass(generateAdapter = true)
data class CertificationRecordDto(
    val id: String, 
    val title: String, 
    val issuer: String, 
    val date: String, 
    val category: String, 
    @Json(name = "is_verified") val isVerified: Boolean, 
    val type: String
)

@JsonClass(generateAdapter = true)
data class ActivityPointDto(
    val id: String, 
    val title: String, 
    val category: String, 
    val date: String, 
    @Json(name = "points_claimed") val pointsClaimed: Int, 
    @Json(name = "points_awarded") val pointsAwarded: Int?, 
    val status: String, 
    val description: String, 
    @Json(name = "attachment_url") val attachmentUrl: String?, 
    @Json(name = "approved_by") val approvedBy: String?, 
    @Json(name = "approved_date") val approvedDate: String?, 
    val remarks: String?
)

@JsonClass(generateAdapter = true)
data class ActivityPointClaimRequest(
    val title: String,
    val category: String,
    val description: String,
    @Json(name = "claimed_points") val claimedPoints: Int
)

@JsonClass(generateAdapter = true)
data class CommunityServiceDataDto(
    val opportunities: List<ServiceOpportunityDto>, 
    val logs: List<ServiceLogDto>
)

@JsonClass(generateAdapter = true)
data class ServiceOpportunityDto(
    val id: Int, 
    val title: String, 
    val organizer: String, 
    val date: String, 
    val location: String, 
    val slots: Int, 
    val duration: String, 
    val tags: List<String>
)

@JsonClass(generateAdapter = true)
data class ServiceLogDto(
    val id: Int, 
    val title: String, 
    val date: String, 
    val hours: Int, 
    val status: String, 
    @Json(name = "is_verified") val isVerified: Boolean
)

@JsonClass(generateAdapter = true)
data class FacultyAssignmentDto(
    val id: String,
    val title: String,
    @Json(name = "due_date") val dueDate: String,
    val submitted: Int,
    val total: Int,
    val status: String
)

@JsonClass(generateAdapter = true)
data class FacultyStudentDto(
    val id: String,
    val name: String,
    @Json(name = "roll_no") val rollNo: String,
    val email: String,
    val phone: String,
    val semester: Int,
    val batch: String,
    val attendance: Double,
    val cgpa: Double
)

@JsonClass(generateAdapter = true)
data class FacultyMaterialDto(
    val id: String,
    val title: String,
    val subject: String,
    val type: String,
    val date: String,
    val size: String,
    val downloads: Int
)

@JsonClass(generateAdapter = true)
data class FacultyRecordingDto(
    val id: String,
    val title: String,
    val subject: String,
    val date: String,
    val duration: String,
    val views: Int
)

@JsonClass(generateAdapter = true)
data class ApprovalRequest(val status: String, val remarks: String)

@JsonClass(generateAdapter = true)
data class PrincipalDashboardStatsDto(
    @Json(name = "total_students") val totalStudents: Int,
    @Json(name = "total_faculty") val totalFaculty: Int,
    @Json(name = "active_approvals") val activeApprovals: Int,
    @Json(name = "revenue_this_month") val revenueThisMonth: Double
)

@JsonClass(generateAdapter = true)
data class TimetableApprovalDto(
    val id: String,
    @Json(name = "faculty_name") val facultyName: String,
    @Json(name = "subject_name") val subjectName: String,
    @Json(name = "requested_changes") val requestedChanges: String,
    val date: String
)

@JsonClass(generateAdapter = true)
data class LeaveApprovalDto(
    val id: String,
    @Json(name = "applicant_name") val applicantName: String,
    @Json(name = "leave_type") val leaveType: String,
    @Json(name = "start_date") val startDate: String,
    @Json(name = "end_date") val endDate: String,
    val reason: String
)

@JsonClass(generateAdapter = true)
data class ResearchComplianceDto(
    val id: String,
    val title: String,
    val researcher: String,
    val status: String,
    @Json(name = "compliance_check") val complianceCheck: String
)

@JsonClass(generateAdapter = true)
data class InfrastructureDto(
    @Json(name = "total_labs") val totalLabs: Int,
    @Json(name = "active_classrooms") val activeClassrooms: Int,
    @Json(name = "library_status") val libraryStatus: String,
    @Json(name = "maintenance_requests") val maintenanceRequests: Int
)

@JsonClass(generateAdapter = true)
data class AdminDashboardMetricsDto(
    @Json(name = "total_users") val totalUsers: Int,
    @Json(name = "online_now") val onlineNow: Int,
    @Json(name = "storage_used") val storageUsed: String,
    @Json(name = "system_health") val systemHealth: String
)

@JsonClass(generateAdapter = true)
data class DegreeDto(val id: String, val name: String, val code: String)

@JsonClass(generateAdapter = true)
data class BatchDto(val id: String, val year: String, val status: String)

@JsonClass(generateAdapter = true)
data class FeeTrackerDto(
    @Json(name = "student_name") val studentName: String,
    @Json(name = "roll_no") val rollNo: String,
    @Json(name = "total_fees") val totalFees: Double,
    val paid: Double,
    val pending: Double
)

@JsonClass(generateAdapter = true)
data class PayrollDto(
    @Json(name = "faculty_id") val facultyId: String,
    val name: String,
    @Json(name = "basic_pay") val basicPay: Double,
    val allowances: Double,
    val deductions: Double,
    @Json(name = "net_salary") val netSalary: Double
)

@JsonClass(generateAdapter = true)
data class AttendanceDefaulterDto(
    @Json(name = "student_name") val studentName: String,
    @Json(name = "roll_no") val rollNo: String,
    @Json(name = "attendance_percentage") val attendancePercentage: Double,
    @Json(name = "parent_contact") val parentContact: String
)

@JsonClass(generateAdapter = true)
data class HODDashboardMetricsDto(
    @Json(name = "total_faculty") val totalFaculty: String,
    @Json(name = "total_students") val totalStudents: String,
    @Json(name = "pending_approvals") val pendingApprovals: String,
    @Json(name = "active_subjects") val activeSubjects: String
)

@JsonClass(generateAdapter = true)
data class HODActivityDto(
    val title: String,
    val time: String,
    val type: String
)

@JsonClass(generateAdapter = true)
data class FacultyDashboardMetricsDto(
    @Json(name = "classes_today") val classesToday: String,
    @Json(name = "pending_attendance") val pendingAttendance: String,
    @Json(name = "pending_assignments") val pendingAssignments: String,
    @Json(name = "leave_balance") val leaveBalance: String
)

@JsonClass(generateAdapter = true)
data class FacultySubjectDto(
    @Json(name = "subject_code") val subjectCode: String,
    @Json(name = "subject_name") val subjectName: String,
    @Json(name = "degree_code") val degreeCode: String?,
    val section: String,
    val year: Int,
    val semester: Int,
    val batch: String
)

@JsonClass(generateAdapter = true)
data class FacultyProfileDto(
    @Json(name = "faculty_id") val facultyId: String,
    @Json(name = "full_name") val fullName: String,
    val designation: String,
    @Json(name = "department_name") val departmentName: String,
    val email: String,
    val phone: String,
    @Json(name = "employee_code") val employeeCode: String,
    val specialization: String,
    val qualifications: List<QualificationDto>,
    val experience: List<ExperienceDto>
)

@JsonClass(generateAdapter = true)
data class QualificationDto(
    val degree: String,
    val specialization: String,
    val university: String,
    val institution: String,
    @Json(name = "year_of_completion") val yearOfCompletion: Int,
    @Json(name = "percentage_cgpa") val percentageCgpa: String
)

@JsonClass(generateAdapter = true)
data class ExperienceDto(
    @Json(name = "institution_name") val institutionName: String,
    val designation: String,
    @Json(name = "from_date") val fromDate: String,
    @Json(name = "to_date") val toDate: String,
    @Json(name = "total_years") val totalYears: Double
)

@JsonClass(generateAdapter = true)
data class ResearchEntryDto(
    val id: String,
    val title: String,
    val publication: String?,
    @Json(name = "research_type") val researchType: String,
    @Json(name = "publication_date") val publicationDate: String?
)

@JsonClass(generateAdapter = true)
data class ActivitySummaryDto(
    @Json(name = "classes_conducted") val classesConducted: Int,
    @Json(name = "attendance_marked") val attendanceMarked: Int,
    @Json(name = "study_materials_uploaded") val studyMaterialsUploaded: Int,
    @Json(name = "assignments_created") val assignmentsCreated: Int,
    @Json(name = "leave_requests_submitted") val leaveRequestsSubmitted: Int
)

@JsonClass(generateAdapter = true)
data class FacultyTimetableDayDto(
    val day: String,
    val periods: List<FacultyTimetablePeriodDto>
)

@JsonClass(generateAdapter = true)
data class FacultyTimetablePeriodDto(
    @Json(name = "period_no") val periodNo: Int,
    val time: String,
    val subject: String,
    val code: String,
    val room: String,
    val batch: String
)

@JsonClass(generateAdapter = true)
data class ParentChildProfileDto(
    val id: String,
    @Json(name = "full_name") val fullName: String,
    @Json(name = "roll_no") val rollNo: String,
    val semester: String,
    val batch: String,
    val cgpa: Double,
    @Json(name = "mentor_name") val mentorName: String,
    @Json(name = "mentor_email") val mentorEmail: String,
    @Json(name = "mentor_phone") val mentorPhone: String,
    val dob: String,
    val gender: String,
    @Json(name = "blood_group") val bloodGroup: String,
    val nationality: String,
    @Json(name = "aadhaar_no") val aadhaarNo: String,
    @Json(name = "contact_mobile") val contactMobile: String,
    @Json(name = "contact_email") val contactEmail: String,
    @Json(name = "emergency_contact") val emergencyContact: String,
    @Json(name = "emergency_phone") val emergencyPhone: String,
    @Json(name = "father_name") val fatherName: String,
    @Json(name = "father_occupation") val fatherOccupation: String,
    @Json(name = "father_mobile") val fatherMobile: String,
    @Json(name = "father_email") val fatherEmail: String,
    @Json(name = "mother_name") val motherName: String,
    @Json(name = "mother_occupation") val motherOccupation: String,
    @Json(name = "mother_mobile") val motherMobile: String,
    @Json(name = "mother_email") val motherEmail: String,
    val certifications: List<ParentChildCertificationDto>
)

@JsonClass(generateAdapter = true)
data class ParentChildCertificationDto(
    val title: String,
    val issuer: String,
    val category: String,
    val date: String,
    val status: String
)

@JsonClass(generateAdapter = true)
data class ParentChildMarkDto(
    val subject: String,
    @Json(name = "academic_year") val academicYear: String,
    @Json(name = "internal_1") val internal1: String,
    @Json(name = "internal_2") val internal2: String,
    val model: String,
    val assignments: String,
    val attendance: String,
    val total: String
)

@JsonClass(generateAdapter = true)
data class ParentChildAttendanceDto(
    val date: String,
    val status: String
)

@JsonClass(generateAdapter = true)
data class ParentChildFeeDto(
    @Json(name = "total_fees") val totalFees: Double,
    @Json(name = "scholarship_deduction") val scholarshipDeduction: Double,
    @Json(name = "other_deductions") val otherDeductions: Double,
    @Json(name = "net_fees") val netFees: Double,
    @Json(name = "amount_paid") val amountPaid: Double,
    @Json(name = "pending_balance") val pendingBalance: Double,
    @Json(name = "due_date") val dueDate: String,
    val records: List<ParentChildFeeRecordDto>
)

@JsonClass(generateAdapter = true)
data class ParentChildFeeRecordDto(
    val id: String,
    val title: String,
    val amount: Double,
    val date: String,
    val status: String
)

@JsonClass(generateAdapter = true)
data class ParentChildTimetableDayDto(
    val day: String,
    val periods: List<ParentChildTimetablePeriodDto>
)

@JsonClass(generateAdapter = true)
data class ParentChildTimetablePeriodDto(
    @Json(name = "period_no") val periodNo: Int,
    val time: String,
    val subject: String,
    val code: String,
    val room: String,
    val faculty: String
)

@JsonClass(generateAdapter = true)
data class PerformanceDataDto(
    val semester: String,
    val gpa: Double,
    val attendance: Int
)

@JsonClass(generateAdapter = true)
data class SubjectAttendanceDto(
    val subject: String,
    @Json(name = "total_classes") val totalClasses: Int,
    @Json(name = "attended_classes") val attendedClasses: Int,
    val percentage: Int
)

@JsonClass(generateAdapter = true)
data class AttendanceSummaryResponse(
    val percentage: Float,
    val total: Int,
    val present: Int,
    val absent: Int,
    val od: Int,
    val records: List<AttendanceRecordDto>
)

@JsonClass(generateAdapter = true)
data class AttendanceRecordDto(
    val id: String,
    val date: String,
    val status: String,
    @Json(name = "subject_name") val subjectName: String,
    @Json(name = "subject_code") val subjectCode: String
)

@JsonClass(generateAdapter = true)
data class InternalMarkDto(
    val id: String,
    @Json(name = "subject_name") val subjectName: String,
    @Json(name = "subject_code") val subjectCode: String,
    @Json(name = "mark_obtained") val markObtained: Double,
    @Json(name = "max_mark") val maxMark: Double,
    val component: String
)

@JsonClass(generateAdapter = true)
data class TimetableSlotDto(
    val id: String,
    @Json(name = "day_of_week") val dayOfWeek: String,
    @Json(name = "start_time") val startTime: String,
    @Json(name = "end_time") val endTime: String,
    @Json(name = "subject_name") val subjectName: String,
    @Json(name = "subject_code") val subjectCode: String,
    @Json(name = "faculty_name") val facultyName: String,
    @Json(name = "room_no") val roomNo: String
)

@JsonClass(generateAdapter = true)
data class StudentFeeSummaryResponse(
    @Json(name = "total_fees") val totalFees: Double,
    @Json(name = "scholarship_deduction") val scholarshipDeduction: Double,
    @Json(name = "other_deductions") val otherDeductions: Double,
    @Json(name = "net_fees") val netFees: Double,
    @Json(name = "amount_paid") val amountPaid: Double,
    @Json(name = "pending_balance") val pendingBalance: Double,
    @Json(name = "due_date") val dueDate: String?,
    val records: List<FeeRecordDto>
)

@JsonClass(generateAdapter = true)
data class FeeRecordDto(
    @Json(name = "record_id") val id: String,
    @Json(name = "fee_type") val title: String,
    val amount: Double,
    @Json(name = "due_date") val dueDate: String,
    val status: String
)

@JsonClass(generateAdapter = true)
data class StudyMaterialDto(
    val id: String,
    val title: String,
    @Json(name = "subject_name") val subjectName: String,
    @Json(name = "file_url") val fileUrl: String,
    @Json(name = "upload_date") val uploadDate: String
)

@JsonClass(generateAdapter = true)
data class AssignmentDto(
    val id: String,
    val title: String,
    val type: String,
    val subject: String,
    val deadline: String,
    val status: String,
    val description: String?,
    @Json(name = "my_submission") val mySubmission: AssignmentSubmissionDto?
)

@JsonClass(generateAdapter = true)
data class AssignmentSubmissionDto(
    val id: String,
    @Json(name = "submission_date") val submissionDate: String,
    val status: String,
    @Json(name = "submitted_file") val submittedFile: String?,
    @Json(name = "submitted_text") val submittedText: String?,
    val evaluation: AssignmentEvaluationDto?
)

@JsonClass(generateAdapter = true)
data class AssignmentEvaluationDto(
    @Json(name = "marks_obtained") val marksObtained: Double?,
    @Json(name = "total_marks") val totalMarks: Double?,
    val grade: String?,
    val feedback: String?
)

@JsonClass(generateAdapter = true)
data class AssignmentSubmitRequest(
    @Json(name = "submitted_file") val submittedFile: String?,
    @Json(name = "submitted_text") val submittedText: String?
)

@JsonClass(generateAdapter = true)
data class LeaveRequestDto(
    val id: String,
    val type: String,
    @Json(name = "from_date") val startDate: String,
    @Json(name = "to_date") val endDate: String,
    val reason: String,
    val status: String,
    val remarks: String?
)

@JsonClass(generateAdapter = true)
data class LeaveApplicationRequest(
    @Json(name = "app_category") val appCategory: String = "Leave",
    val type: String,
    @Json(name = "from_date") val fromDate: String,
    @Json(name = "to_date") val toDate: String,
    val reason: String
)

@JsonClass(generateAdapter = true)
data class NoticeDto(
    val id: String,
    val title: String,
    val body: String,
    @Json(name = "publish_date") val date: String,
    @Json(name = "audience_type") val category: String
)

@JsonClass(generateAdapter = true)
data class CalendarEventDto(
    val id: String,
    val date: String,
    @Json(name = "event_name") val eventName: String,
    @Json(name = "is_holiday") val isHoliday: Boolean
)

@JsonClass(generateAdapter = true)
data class SyllabusDto(
    val id: String,
    @Json(name = "subject_name") val subjectName: String,
    @Json(name = "subject_code") val subjectCode: String,
    val semester: Int,
    @Json(name = "file_url") val fileUrl: String
)

@JsonClass(generateAdapter = true)
data class NotificationDto(
    val id: String,
    val title: String,
    val message: String,
    val date: String,
    @Json(name = "is_read") val isRead: Boolean
)
// End of CamsApiService.kt
