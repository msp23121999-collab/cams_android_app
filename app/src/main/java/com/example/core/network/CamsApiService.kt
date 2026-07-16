package com.example.core.network

import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.PUT
import retrofit2.http.Body
import retrofit2.http.Path
import retrofit2.http.Multipart
import retrofit2.http.Part
import retrofit2.http.Query
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

@JsonClass(generateAdapter = true)
data class HallTicketDto(
    val id: String,
    @Json(name = "student_id") val studentId: String,
    @Json(name = "exam_id") val examId: String,
    @Json(name = "is_eligible") val isEligible: Boolean = true,
    @Json(name = "ineligibility_reason") val ineligibilityReason: String? = null,
    @Json(name = "is_issued") val isIssued: Boolean = false,
    @Json(name = "student_signature_url") val studentSignatureUrl: String? = null,
    @Json(name = "principal_signature_url") val principalSignatureUrl: String? = null,
    @Json(name = "coe_signature_url") val coeSignatureUrl: String? = null,
    @Json(name = "student_name") val studentName: String? = null,
    @Json(name = "exam_center") val examCenter: String? = null,
    @Json(name = "exam_date") val examDate: String? = null
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

    @PUT("students/profile")
    suspend fun updateStudentProfile(@Body profile: StudentProfileDto): Response<StudentProfileDto>

    @POST("students/profile/submit")
    suspend fun submitStudentProfile(): Response<StudentProfileDto>

    @GET("students/mentorship-record")
    suspend fun getMentorshipRecord(): Response<MentorshipRecordDto>

    @GET("students/attendance")
    suspend fun getAttendance(): Response<AttendanceSummaryResponse>

    @GET("students/marks")
    suspend fun getInternalMarks(): Response<List<InternalMarkDto>>

    @GET("fees/")
    suspend fun getFees(): Response<StudentFeeSummaryResponse>

    @GET("students/timetable")
    suspend fun getTimetable(): Response<List<TimetableSlotDto>>

    @GET("students/study-materials")
    suspend fun getStudyMaterials(
        @Query("skip") skip: Int = 0,
        @Query("limit") limit: Int = 20
    ): Response<List<StudyMaterialDto>>

    @GET("assignments/active-assignments")
    suspend fun getAssignments(
        @Query("skip") skip: Int = 0,
        @Query("limit") limit: Int = 20
    ): Response<List<AssignmentDto>>

    @POST("assignments/submit/{asg_id}")
    suspend fun submitAssignment(
        @Path("asg_id") asgId: String,
        @Body payload: AssignmentSubmitRequest
    ): Response<Unit>

    @GET("leave/history")
    suspend fun getLeaves(): Response<List<LeaveRequestDto>>

    @GET("hall-tickets/")
    suspend fun getHallTickets(): Response<List<HallTicketDto>>

    @POST("leave/apply")
    suspend fun applyLeave(@Body request: LeaveApplicationRequest): Response<LeaveRequestDto>

    @GET("students/notices")
    suspend fun getNotices(
        @Query("skip") skip: Int = 0,
        @Query("limit") limit: Int = 20
    ): Response<List<NoticeDto>>

    @GET("students/grievances")
    suspend fun getStudentGrievances(
        @Query("skip") skip: Int = 0,
        @Query("limit") limit: Int = 20
    ): Response<List<GrievanceDto>>

    @POST("students/grievances/raise")
    suspend fun raiseGrievance(@Body grievance: GrievanceRaiseRequest): Response<GrievanceDto>

    @GET("online-meetings")
    suspend fun getStudentMeetings(): Response<List<OnlineMeetingDto>>

    @GET("legal-events")
    suspend fun getStudentLegalEvents(
        @Query("skip") skip: Int = 0,
        @Query("limit") limit: Int = 20
    ): Response<List<LegalEventDto>>

    @GET("clubs")
    suspend fun getStudentClubs(
        @Query("skip") skip: Int = 0,
        @Query("limit") limit: Int = 20
    ): Response<List<ClubDto>>

    @GET("academic-calendar/published")
    suspend fun getAcademicCalendar(): Response<List<CalendarEventDto>>

    @GET("study-materials/student/approved")
    suspend fun getSyllabus(): Response<List<SyllabusDto>>

    @GET("students/notifications")
    suspend fun getNotifications(
        @Query("skip") skip: Int = 0,
        @Query("limit") limit: Int = 20
    ): Response<List<NotificationDto>>

    @GET("parents/children")
    suspend fun getParentChildren(): Response<List<ChildSummaryDto>>

    @GET("parents/child/profile")
    suspend fun getParentChildProfile(@Query("child_id") childId: String?): Response<ParentChildProfileDto>

    @GET("parents/child/marks")
    suspend fun getParentChildMarks(@Query("child_id") childId: String?): Response<List<ParentChildMarkDto>>

    @GET("parents/child/attendance")
    suspend fun getParentChildAttendance(@Query("child_id") childId: String?): Response<ParentChildAttendanceOverviewDto>

    @GET("parents/child/fees")
    suspend fun getParentChildFees(@Query("child_id") childId: String?): Response<ParentChildFeeDto>

    @GET("parents/child/timetable")
    suspend fun getParentChildTimetable(@Query("child_id") childId: String?): Response<List<ParentChildTimetableDayDto>>

    // Faculty Portal Endpoints
    @GET("faculty/dashboard/metrics")
    suspend fun getFacultyDashboardMetrics(): Response<FacultyDashboardMetricsDto>

    @GET("faculty/subjects")
    suspend fun getFacultySubjects(): Response<List<FacultySubjectDto>>

    @GET("faculty/profile")
    suspend fun getFacultyProfile(): Response<FacultyProfileDto>

    @GET("faculty/research/list")
    suspend fun getFacultyResearch(): Response<List<ResearchEntryDto>>

    @GET("faculty/profile/activity-summary")
    suspend fun getFacultyActivitySummary(): Response<ActivitySummaryDto>

    @GET("faculty/timetable")
    suspend fun getFacultyTimetable(): Response<List<FacultyTimetableItemDto>>

    // HOD Portal Endpoints
    @GET("hods/dashboard/metrics")
    suspend fun getHODDashboardMetrics(): Response<HODDashboardMetricsDto>

    @GET("hods/activities")
    suspend fun getHODActivities(): Response<List<HODActivityDto>>

    @GET("leave/hod/pending")
    suspend fun getHODPendingLeaves(): Response<List<LeaveRequestDto>>

    @POST("leave/hod/approve/{id}")
    suspend fun approveHODLeave(@Path("id") id: String, @Body request: ApprovalRequest): Response<LeaveRequestDto>

    @GET("hod/timetable/metadata")
    suspend fun getHODTimetableMetadata(): Response<HODTimetableMetadataDto>

    @GET("hod/timetable/section/{section_id}")
    suspend fun getHODTimetableSection(@Path("section_id") sectionId: String): Response<List<TimetableSlotDto>>

    // Principal Portal Endpoints
    @GET("principals/dashboard/stats")
    suspend fun getPrincipalDashboardStats(): Response<PrincipalDashboardStatsDto>

    @GET("principals/approvals/timetable")
    suspend fun getPendingTimetableApprovals(): Response<List<TimetableApprovalDto>>

    @POST("principals/approvals/timetable/{id}")
    suspend fun approveTimetable(@Path("id") id: String, @Body request: ApprovalRequest): Response<Unit>

    @GET("users/principal/leaves")
    suspend fun getPendingLeaveApprovals(): Response<List<LeaveRequestDto>>

    @POST("users/principal/leaves/approve/{id}")
    suspend fun approveLeave(@Path("id") id: String, @Body request: ApprovalRequest): Response<LeaveRequestDto>

    @GET("users/principal/faculty/pending")
    suspend fun getPrincipalPendingFaculty(): Response<List<PrincipalPendingFacultyDto>>

    @POST("users/principal/faculty/approve/{user_id}")
    suspend fun approvePrincipalFaculty(@Path("user_id") userId: String): Response<Unit>

    @POST("users/principal/faculty/reject/{user_id}")
    suspend fun rejectPrincipalFaculty(@Path("user_id") userId: String): Response<Unit>

    @GET("users/principal/circulars")
    suspend fun getPrincipalCirculars(): Response<List<NoticeDto>>

    @POST("users/principal/circulars")
    suspend fun publishPrincipalCircular(@Body request: NoticeCreateRequest): Response<NoticeDto>

    @GET("principals/grievances")
    suspend fun getGrievancesForApproval(): Response<List<GrievanceDto>>

    @GET("principals/research-compliance")
    suspend fun getResearchCompliance(): Response<PrincipalComplianceResponseDto>

    @GET("principals/infrastructure")
    suspend fun getInfrastructureDetails(): Response<InfrastructureResponseDto>

    // Admin Portal Endpoints
    @GET("admins/dashboard/metrics")
    suspend fun getAdminDashboardMetrics(): Response<AdminDashboardMetricsDto>

    @GET("users/backups/history")
    suspend fun getBackups(): Response<List<BackupDto>>

    // Admin Academic Catalog APIs
    @GET("degrees/list")
    suspend fun getDegreesList(): Response<List<DegreeDto>>

    @GET("faculty/courses/list")
    suspend fun getAllCourses(): Response<List<CourseDto>>

    @GET("admins/batches")
    suspend fun getBatches(): Response<List<BatchDto>>

    @GET("admins/fees/tracker")
    suspend fun getFeeTracker(): Response<List<FeeTrackerDto>>

    @GET("admins/faculty/payroll")
    suspend fun getPayrollDetails(): Response<List<PayrollDto>>

    @GET("admins/attendance/defaulters")
    suspend fun getAttendanceDefaulters(): Response<List<AttendanceDefaulterDto>>

    @POST("faculty/notifications/read/{notif_id}")
    suspend fun markFacultyNotificationRead(@Path("notif_id") notifId: String): Response<Unit>

    @GET("faculty/notifications")
    suspend fun getFacultyNotifications(): Response<List<NotificationDto>>

    @GET("online-meetings")
    suspend fun getOnlineMeetings(): Response<List<OnlineMeetingDto>>

    @POST("clubs/{id}/join")
    suspend fun joinClub(@Path("id") id: Int): Response<Unit>

    @GET("students/council")
    suspend fun getCouncilData(): Response<CouncilDataDto>

    @POST("students/grievances")
    suspend fun submitGrievance(@Body grievance: GrievanceRaiseRequest): Response<Unit>

    @GET("students/lexnova/stats")
    suspend fun getLexNovaStats(): Response<List<LexNovaKpiDto>>

    @GET("lexsphere/alumni")
    suspend fun getAlumniNetwork(): Response<List<AlumniMentorDto>>

    @GET("internship-drives")
    suspend fun getInternshipDrives(): Response<List<InternshipDriveDto>>

    @GET("internship-drives")
    suspend fun getInternships(
        @Query("skip") skip: Int = 0,
        @Query("limit") limit: Int = 20
    ): Response<List<InternshipRecordDto>>

    @GET("students/certifications")
    suspend fun getCertifications(): Response<List<CertificationRecordDto>>

    @GET("students/activity-points")
    suspend fun getActivityPoints(): Response<List<ActivityPointDto>>

    @POST("activity-points/claim")
    suspend fun claimActivityPoints(@Body request: ActivityPointClaimRequest): Response<Unit>

    @GET("students/community-service")
    suspend fun getCommunityService(): Response<CommunityServiceDataDto>

    @GET("students/innovation-projects")
    suspend fun getInnovationProjects(): Response<List<InnovationProjectDto>>

    @GET("parents/child/performance")
    suspend fun getParentChildPerformance(@Query("child_id") childId: String?): Response<List<PerformanceDataDto>>

    @GET("parents/child/subject-attendance")
    suspend fun getParentChildSubjectAttendance(@Query("child_id") childId: String?): Response<List<SubjectAttendanceDto>>

    @GET("assignments/my-assignments")
    suspend fun getFacultyAssignments(): Response<List<FacultyAssignmentDto>>

    @GET("faculty/students/list")
    suspend fun getFacultyStudents(): Response<List<FacultyStudentDto>>

    @GET("study-materials/my-materials")
    suspend fun getFacultyMaterials(): Response<List<FacultyMaterialDto>>

    @Multipart
    @POST("study-materials/upload-file")
    suspend fun uploadMaterialFile(
        @Part file: okhttp3.MultipartBody.Part
    ): Response<FileUploadResponseDto>

    @POST("study-materials/upload")
    suspend fun uploadStudyMaterial(
        @Body payload: UploadMaterialRequestDto
    ): Response<FacultyMaterialDto>

    @GET("faculty/mentor/students")
    suspend fun getMentorStudents(): Response<List<FacultyMentorshipStudentDto>>

    @GET("faculty/mentor/students/{student_id}/record")
    suspend fun getMentorStudentRecord(@Path("student_id") studentId: String): Response<FacultyMentorshipRecordDto>

    @POST("faculty/mentor/students/{student_id}/record")
    suspend fun saveMentorStudentRecord(@Path("student_id") studentId: String, @Body payload: FacultyMentorshipRecordDto): Response<FacultyMentorshipRecordDto>

    @GET("faculty/salary-slips")
    suspend fun getFacultySalarySlips(): Response<List<FacultySalarySlipDto>>

    @GET("internship-drives")
    suspend fun getFacultyInternshipDrives(): Response<List<FacultyInternshipDriveDto>>

    @GET("legal-events")
    suspend fun getLegalEvents(): Response<List<FacultyLegalEventDto>>

    @GET("online-meetings/recordings")
    suspend fun getFacultyRecordings(): Response<List<FacultyRecordingDto>>

    @POST("auth/change-password")
    suspend fun changePassword(@Body request: ChangePasswordRequest): Response<Unit>

    // Additional HOD Endpoints
    @GET("hods/teaching-logs")
    suspend fun getHODTeachingLogsDashboard(): Response<HODTeachingLogsDashboardDto>

    @GET("hods/syllabus-metadata")
    suspend fun getHODSyllabusMetadata(): Response<HODSyllabusMetadataDto>

    @GET("hods/syllabus-courses")
    suspend fun getHODSyllabusCourses(): Response<List<HODCourseDto>>

    @GET("hods/attendance-monitoring")
    suspend fun getHODAttendanceMonitoring(): Response<List<HODAttendanceMonitoringDto>>

    @GET("hods/department-reports")
    suspend fun getHODDepartmentReports(): Response<HODDepartmentReportDto>

    @GET("hods/research-monitoring")
    suspend fun getHODResearchMonitoring(): Response<List<HODResearchMonitoringDto>>

    @GET("hods/pending-proofs")
    suspend fun getHODPendingProofs(): Response<List<HODPendingProofDto>>

    @POST("hods/research-proofs/{id}/verify")
    suspend fun verifyResearchProof(@Path("id") proofId: String, @Body request: VerificationRequestDto): Response<Map<String, String>>

    @GET("hods/workloads")
    suspend fun getHODWorkloads(): Response<List<HODWorkloadDto>>

    @GET("hods/mentors")
    suspend fun getHODMentors(): Response<List<HODMentorDto>>

    @POST("hods/mentors/assign")
    suspend fun assignHODMentor(@Body request: MentorAssignmentRequestDto): Response<Unit>

    @GET("hods/academic-setup")
    suspend fun getAcademicSetup(): Response<AcademicSetupDto>

    @GET("hods/subject-allocations")
    suspend fun getSubjectAllocations(): Response<List<SubjectAllocationDto>>

    @GET("hods/substitutions")
    suspend fun getSubstitutions(): Response<List<HODSubstitutionDto>>

    @GET("admins/payroll")
    suspend fun getFacultyPayrollAdmin(): Response<List<AdminPayrollDto>>

    @GET("admins/backups")
    suspend fun getBackupHistoryAdmin(): Response<List<AdminBackupDto>>

    @GET("admins/system-settings")
    suspend fun getSystemSettingsAdmin(): Response<AdminSystemSettingsDto>

    @GET("admins/audit-logs")
    suspend fun getAuditLogsAdmin(): Response<List<AdminAuditLogDto>>


    @GET("admins/users")
    suspend fun getAllUsers(): Response<List<AdminUserDto>>

    @GET("admins/departments")
    suspend fun getDepartmentsList(): Response<List<AdminDepartmentDto>>

    @GET("admins/degrees-list")
    suspend fun getDegreesListAdmin(): Response<List<AdminDegreeDto>>

    @GET("admins/courses-list")
    suspend fun getAllCoursesAdmin(): Response<List<AdminCourseDto>>

    @GET("admins/backups-list")
    suspend fun getBackupsAdmin(): Response<List<AdminBackupDto>>

    @GET("admins/attendance-defaulters-admin")
    suspend fun getAttendanceDefaultersAdmin(): Response<List<AdminAttendanceDefaulterDto>>

    @GET("admins/fee-structures")
    suspend fun getAdminFeeStructures(): Response<List<AdminFeeStructureDto>>

    @GET("admins/scholarship-types")
    suspend fun getAdminScholarshipTypes(): Response<List<AdminScholarshipTypeDto>>

    @GET("admins/search-fees")
    suspend fun searchStudentsForFees(@Query("query") query: String): Response<List<AdminFeeStudentDto>>

}

@JsonClass(generateAdapter = true)
data class ChangePasswordRequest(
    @Json(name = "current_password") val currentPassword: String,
    @Json(name = "new_password") val newPassword: String
)

@JsonClass(generateAdapter = true)
data class ChildSummaryDto(
    val id: String, 
    @Json(name = "full_name") val fullName: String, 
    @Json(name = "roll_no") val rollNo: String?, 
    @Json(name = "course_name") val courseName: String?, 
    @Json(name = "profile_photo_url") val profilePhotoUrl: String?
)

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
data class AlumniMentorDto(
    val id: String,
    val name: String,
    val designation: String,
    val company: String,
    @Json(name = "graduation_year") val graduationYear: String,
    @Json(name = "image_url") val imageUrl: String?,
    @Json(name = "linkedin_url") val linkedinUrl: String?
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
    val totalDepartments: Int? = null,
    val totalStaff: Int? = null,
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
data class PrincipalPendingFacultyDto(
    val id: String,
    val email: String,
    @Json(name = "full_name") val fullName: String,
    @Json(name = "department_name") val departmentName: String,
    val designation: String?
)

@JsonClass(generateAdapter = true)
data class ResearchComplianceDto(
    val id: String,
    val title: String,
    val researcher: String,
    val status: String,
    @Json(name = "compliance_check") val complianceCheck: String,
    val completedCount: Int = 0,
    val pendingCount: Int = 0,
    val overdueCount: Int = 0,
    val overdueFacultyList: List<String> = emptyList(),
    val facultyName: String = "",
    val department: String = "",
    val publicationTitle: String = "",
    val dueDate: String = ""
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
data class BackupDto(
    val id: String,
    val filename: String,
    @Json(name = "size_bytes") val sizeBytes: Long,
    val status: String,
    @Json(name = "created_at") val createdAt: String
)

@JsonClass(generateAdapter = true)
data class DegreeDto(
    val id: String, 
    val name: String, 
    val code: String,
    @Json(name = "duration_years") val durationYears: Int? = null,
    @Json(name = "program_level") val programLevel: String? = null
)

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
data class HODTimetableMetadataDto(
    val courses: List<CourseDto>,
    val sections: List<SectionDto>,
    val faculty: List<FacultyDto>
)

@JsonClass(generateAdapter = true)
data class CourseDto(
    val id: String,
    val code: String,
    val name: String,
    val semester: Int,
    @Json(name = "degree_id") val degreeId: String,
    val credits: Int? = null
)

@JsonClass(generateAdapter = true)
data class SectionDto(
    val id: String,
    @Json(name = "section_name") val sectionName: String,
    val semester: Int,
    @Json(name = "degree_id") val degreeId: String,
    val label: String
)

@JsonClass(generateAdapter = true)
data class FacultyDto(
    val id: String,
    @Json(name = "full_name") val fullName: String
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
    @Json(name = "publication_date") val publicationDate: String?,
    @Json(name = "grant_amount") val grantAmount: Double?,
    val publisher: String?,
    val status: String?
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
    val id: String = "",
    @Json(name = "full_name") val fullName: String = "",
    @Json(name = "roll_no") val rollNo: String = "",
    val semester: String = "",
    val batch: String? = null,
    @Json(name = "batch_year") val batchYear: String? = null,
    val cgpa: Double? = 0.0,
    @Json(name = "mentor_name") val mentorName: String? = "N/A",
    @Json(name = "mentor_email") val mentorEmail: String? = "",
    @Json(name = "mentor_phone") val mentorPhone: String? = "",
    val email: String? = "",
    val dob: String? = "",
    val gender: String? = "",
    @Json(name = "blood_group") val bloodGroup: String? = "",
    val nationality: String? = "",
    @Json(name = "aadhaar_no") val aadhaarNo: String? = "",
    @Json(name = "contact_mobile") val contactMobile: String? = "",
    @Json(name = "contact_email") val contactEmail: String? = "",
    @Json(name = "emergency_contact") val emergencyContact: String? = "",
    @Json(name = "emergency_phone") val emergencyPhone: String? = "",
    @Json(name = "father_name") val fatherName: String? = "",
    @Json(name = "father_occupation") val fatherOccupation: String? = "",
    @Json(name = "father_mobile") val fatherMobile: String? = "",
    @Json(name = "father_email") val fatherEmail: String? = "",
    @Json(name = "mother_name") val motherName: String? = "",
    @Json(name = "mother_occupation") val motherOccupation: String? = "",
    @Json(name = "mother_mobile") val motherMobile: String? = "",
    @Json(name = "mother_email") val motherEmail: String? = "",
    val certifications: List<ParentChildCertificationDto>? = emptyList()
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
    val subject: String = "",
    val subjectName: String = "",
    @Json(name = "academic_year") val academicYear: String = "",
    @Json(name = "internal_1") val internal1: String = "",
    @Json(name = "internal_2") val internal2: String = "",
    val model: String = "",
    val assignments: String = "",
    val attendance: String = "",
    val total: String = "",
    val internalExamMark: Double = 0.0,
    val assignmentMark: Double = 0.0,
    val presentationMark: Double = 0.0,
    val vivaVoiceMark: Double = 0.0,
    val attendanceMark: Double = 0.0,
    val totalMark: Double = 0.0
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
    val dayOfWeek: String = "",
    val startTime: String = "",
    val endTime: String = "",
    val subjectName: String = "",
    val subjectCode: String = "",
    val roomNo: String = "",
    val facultyName: String = ""
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
    val component: String,
    val examType: String = "",
    val mark: Double? = null
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
    val remarks: String?,
    @Json(name = "user_name") val userName: String? = null,
    @Json(name = "department_name") val departmentName: String? = null,
    @Json(name = "user_roll_no") val userRollNo: String? = null
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
    @Json(name = "publish_date") val date: String?,
    @Json(name = "audience_type") val category: String?
)

@JsonClass(generateAdapter = true)
data class NoticeCreateRequest(
    val title: String,
    val body: String,
    @Json(name = "audience_type") val audienceType: String? = null
)

@JsonClass(generateAdapter = true)
data class CalendarEventDto(
    val id: String,
    val date: String,
    @Json(name = "event_name") val eventName: String,
    @Json(name = "is_holiday") val isHoliday: Boolean,
    val title: String? = null
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
data class MentorshipRecordDto(val meetingLog: String?, val academicReview: String?, val improvementPlan: String?, val remarks: String?, val followUp: String?)

@JsonClass(generateAdapter = true)
data class FacultyMentorshipStudentDto(
    val id: String,
    @Json(name = "roll_no") val rollNo: String,
    val name: String,
    val email: String,
    val batch: String?,
    val semester: Int?
)

@JsonClass(generateAdapter = true)
data class FacultyMentorshipRecordDto(
    @Json(name = "student_id") val studentId: String,
    @Json(name = "mentor_id") val mentorId: String?,
    @Json(name = "meeting_log") val meetingLog: String?,
    @Json(name = "academic_review") val academicReview: String?,
    @Json(name = "improvement_plan") val improvementPlan: String?,
    val remarks: String?,
    @Json(name = "follow_up_date") val followUpDate: String?
)

@JsonClass(generateAdapter = true)
data class FacultySalarySlipDto(
    val id: String,
    @Json(name = "user_id") val userId: String,
    val month: Int,
    val year: Int,
    @Json(name = "base_salary") val baseSalary: Double,
    val deductions: Double,
    @Json(name = "net_salary") val netSalary: Double,
    @Json(name = "slip_url") val slipUrl: String?,
    val status: String
)

@JsonClass(generateAdapter = true)
data class FacultyInternshipDriveDto(
    val id: String,
    val company: String,
    val role: String,
    val location: String?,
    val deadline: String?,
    @Json(name = "is_active") val isActive: Boolean,
    @Json(name = "description") val description: String?
)

@JsonClass(generateAdapter = true)
data class FacultyLegalEventDto(
    val id: String,
    val title: String,
    val description: String?,
    val date: String?,
    val location: String?,
    @Json(name = "is_virtual") val isVirtual: Boolean,
    @Json(name = "registration_link") val registrationLink: String?,
    val status: String?
)


@JsonClass(generateAdapter = true)
data class FileUploadResponseDto(
    @Json(name="file_url") val fileUrl: String,
    val filename: String
)

@JsonClass(generateAdapter = true)
data class UploadMaterialRequestDto(
    val title: String,
    val description: String,
    val subject: String,
    val unit: String,
    val topic: String,
    val category: String,
    val keywords: List<String>,
    @Json(name="file_url") val fileUrl: String,
    @Json(name="file_format") val fileFormat: String,
    val status: String
)


@JsonClass(generateAdapter = true)
data class RoomDto(
    val id: String,
    val name: String,
    val type: String,
    val capacity: Int
)

@JsonClass(generateAdapter = true)
data class BuildingDto(
    val id: String,
    val name: String,
    val floors: Int,
    val rooms: List<RoomDto>
)

@JsonClass(generateAdapter = true)
data class InfrastructureResponseDto(
    val buildings: List<BuildingDto>
)


@JsonClass(generateAdapter = true)
data class HODSyllabusMetadataDto(
    val semCount: Int = 10,
    val totalSubjects: Int = 0,
    val completedSubjects: Int = 0,
    val delayedSubjects: Int = 0
)

@JsonClass(generateAdapter = true)
data class HODCourseDto(
    val id: String,
    val name: String = "",
    val code: String = "",
    val semester: Int = 1,
    val credits: Int = 3
)



@JsonClass(generateAdapter = true)
data class HODSubstitutionDto(
    val id: String,
    @Json(name = "faculty_id") val facultyId: String,
    @Json(name = "substitute_id") val substituteId: String,
    val date: String,
    val period: Int,
    val status: String = "",
    val subject: String = "",
    val absent_faculty: String = "",
    val substitute_faculty: String = ""
)


@JsonClass(generateAdapter = true)
data class SubjectAllocationDto(
    val id: String = "",
    val courseId: String = "",
    val sectionId: String = "",
    val facultyId: String = "",
    @Json(name = "subject_name") val subjectName: String = "",
    @Json(name = "subject_code") val subjectCode: String = ""
)

@JsonClass(generateAdapter = true)
data class AcademicSetupDto(
    @Json(name = "academic_year") val academicYear: String = "",
    val semester: Int = 1
)

@JsonClass(generateAdapter = true)
data class OverdueFacultyDto(
    val facultyName: String = "",
    val department: String = "",
    val publicationTitle: String = "",
    val dueDate: String = ""
)

@JsonClass(generateAdapter = true)
data class PrincipalComplianceResponseDto(
    val completedCount: Int = 0,
    val pendingCount: Int = 0,
    val overdueCount: Int = 0,
    val overdueFacultyList: List<OverdueFacultyDto> = emptyList()
)


@JsonClass(generateAdapter = true)
data class HODResearchMonitoringDto(
    val id: String = "",
    val title: String = "",
    val faculty_name: String = "",
    val type: String = "",
    val status: String = "",
    val latest_progress_percentage: Int = 0,
    val area: String = ""
)

@JsonClass(generateAdapter = true)
data class HODPendingProofDto(
    val id: String = "",
    val title: String = "",
    val faculty_name: String = "",
    val journal_name: String = "",
    val issn_isbn: String = ""
)


@JsonClass(generateAdapter = true)
data class HODMentorDto(
    val faculty_id: String = "",
    val faculty_name: String = "",
    val department: String = "",
    val total_students: Int = 0
)

@JsonClass(generateAdapter = true)
data class HODDepartmentReportDto(
    val totalFaculty: Int = 0,
    val totalStudents: Int = 0,
    val attendanceAverage: String = "0%",
    val syllabusCompletionAverage: String = "0%",
    val performanceDistribution: Map<String, Int> = emptyMap()
)


@JsonClass(generateAdapter = true)
data class HODWorkloadDto(
    val id: String = "",
    val name: String = "",
    val email: String = "",
    val teaching_hours: Int = 0
)


@JsonClass(generateAdapter = true)
data class HODAcademicMonitoringDto(
    val subject: String = "",
    val faculty: String = "",
    val completion: Int = 0
)

@JsonClass(generateAdapter = true)
data class HODAttendanceMonitoringDto(
    val subject: String = "",
    val subjectCode: String = "",
    val semester: Int = 1,
    val studentsCount: Int = 0,
    val attendancePercentage: Double = 0.0,
    val lowAttendanceCount: Int = 0
)


@JsonClass(generateAdapter = true)
data class ParentChildAttendanceOverviewDto(
    val percentage: Float = 0f,
    val total: Int = 0,
    val present: Int = 0,
    val absent: Int = 0,
    val od: Int = 0,
    val records: List<ParentChildAttendanceDto> = emptyList()
)

@JsonClass(generateAdapter = true)
data class VerificationRequestDto(
    val status: String,
    val remarks: String
)


@JsonClass(generateAdapter = true)
data class HODTeachingLogsDashboardDto(
    val total_lectures_conducted: Int = 0,
    val pending_diaries_count: Int = 0,
    val syllabus_status: List<HODAcademicMonitoringDto> = emptyList()
)


@JsonClass(generateAdapter = true)
data class MentorAssignmentRequestDto(
    val studentId: String,
    val facultyId: String
)


@JsonClass(generateAdapter = true)
data class HODSyllabusCourseDto(
    val courseName: String = "",
    val facultyName: String = "",
    val completionPercentage: Int = 0,
    val status: String = "ON_TRACK"
)

@JsonClass(generateAdapter = true)
data class FacultyTimetableItemDto(
    val dayOfWeek: String = "",
    val startTime: String = "",
    val endTime: String = "",
    val courseName: String = "",
    val sessionType: String = "",
    val roomNo: String = "",
    val degreeName: String = "",
    val semester: String = ""
)

@JsonClass(generateAdapter = true)
data class AdminPayrollDto(
    val id: String,
    val facultyId: String?,
    val facultyName: String?,
    val month: String?,
    val amount: Double?,
    val status: String?
)

@JsonClass(generateAdapter = true)
data class AdminBackupDto(
    val id: String,
    val filename: String?,
    val sizeBytes: Long?,
    val status: String?,
    val createdAt: String?
)

@JsonClass(generateAdapter = true)
data class AdminSystemSettingsDto(
    val institutionName: String?,
    val academicYear: String?,
    val semester: Int?
)

@JsonClass(generateAdapter = true)
data class AdminAuditLogDto(
    val id: String,
    val userId: String?,
    val action: String?,
    val details: String?,
    val timestamp: String?
)

@JsonClass(generateAdapter = true)
data class AdminUserDto(
    val id: String,
    val email: String,
    val phone: String?,
    val fullName: String,
    val role: String,
    val isActive: Boolean,
    val departmentId: String?
)

@JsonClass(generateAdapter = true)
data class AdminDepartmentDto(
    val id: String,
    val code: String,
    val name: String,
    val hodId: String?
)

@JsonClass(generateAdapter = true)
data class AdminDegreeDto(
    val id: String, 
    val name: String, 
    val code: String,
    val durationYears: Int?,
    val programLevel: String?
)

@JsonClass(generateAdapter = true)
data class AdminCourseDto(
    val id: String,
    val code: String,
    val name: String,
    val semester: Int,
    val credits: Int?
)

@JsonClass(generateAdapter = true)
data class AdminAttendanceDefaulterDto(
    val studentId: String,
    val studentName: String?,
    val department: String?,
    val attendancePercentage: Double?,
    val status: String?
)

@JsonClass(generateAdapter = true)
data class AdminFeeStructureDto(
    val id: String,
    val name: String?,
    val amount: Double?,
    val semester: Int?,
    val departmentId: String?
)

@JsonClass(generateAdapter = true)
data class AdminScholarshipTypeDto(
    val id: String,
    val name: String?,
    val percentage: Double?
)

@JsonClass(generateAdapter = true)
data class AdminFeeStudentDto(
    val studentId: String,
    val studentName: String?,
    val department: String?,
    val currentSemester: Int?,
    val totalFees: Double?,
    val paidFees: Double?,
    val dueFees: Double?
)
