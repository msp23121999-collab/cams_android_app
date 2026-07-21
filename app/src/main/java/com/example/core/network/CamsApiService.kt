package com.example.core.network

import retrofit2.http.HTTP
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.PUT
import retrofit2.http.DELETE
import retrofit2.http.PATCH
import retrofit2.http.Body
import retrofit2.http.Path
import retrofit2.http.Multipart
import retrofit2.http.Part
import retrofit2.http.Query
import retrofit2.http.Streaming
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
data class RefreshRequest(@Json(name = "refresh_token") val refreshToken: String?)

@JsonClass(generateAdapter = true)
data class RefreshResponse(
    @Json(name = "access_token") val accessToken: String,
    @Json(name = "refresh_token") val refreshToken: String?
)

@JsonClass(generateAdapter = true)
data class ForgotPasswordRequest(val email: String)

@JsonClass(generateAdapter = true)
data class ResetPasswordRequest(
    val token: String,
    @Json(name = "new_password") val newPassword: String
)

@JsonClass(generateAdapter = true)
data class MessageResponseDto(val detail: String)

@JsonClass(generateAdapter = true)
data class UserMeResponse(
    val id: String,
    val email: String,
    @Json(name = "full_name") val fullName: String,
    val role: String,
    @Json(name = "department_id") val departmentId: String?,
    @Json(name = "email_notifications_enabled") val emailNotificationsEnabled: Boolean = true
)

@JsonClass(generateAdapter = true)
data class DeviceTokenRequest(
    val token: String,
    val platform: String = "android"
)

@JsonClass(generateAdapter = true)
data class RequestEmailChangeRequest(
    @Json(name = "new_email") val newEmail: String,
    // The server re-authenticates before moving the account's address, so a stolen
    // session alone cannot be used to take the account over via password reset.
    @Json(name = "current_password") val currentPassword: String
)

@JsonClass(generateAdapter = true)
data class ConfirmEmailChangeRequest(val token: String)

@JsonClass(generateAdapter = true)
data class NotificationPreferencesRequest(@Json(name = "email_notifications_enabled") val emailNotificationsEnabled: Boolean)

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
    val id: String? = null,
    @Json(name = "roll_no") val rollNo: String? = null,
    val email: String? = null,
    @Json(name = "batch_year") val batchYear: Int? = null,
    @Json(name = "mentor_name") val mentorName: String? = null,
    @Json(name = "mentor_email") val mentorEmail: String? = null,
    @Json(name = "mentor_phone") val mentorPhone: String? = null,
    val cgpa: Double? = null,
    val skills: List<String>? = null,
    @Json(name = "profile_photo_url") val profilePhotoUrl: String? = null,
    @Json(name = "course_name") val courseName: String? = null,
    val section: String? = null,
    @Json(name = "class_advisor_name") val classAdvisorName: String? = null,
    @Json(name = "class_advisor_email") val classAdvisorEmail: String? = null,
    @Json(name = "class_advisor_phone") val classAdvisorPhone: String? = null,
    val batch: String? = null,
    @Json(name = "year_of_study") val yearOfStudy: String? = null,
    @Json(name = "department_name") val departmentName: String? = null,
    @Json(name = "scholarship_amount") val scholarshipAmount: Double? = null,
    @Json(name = "scholarship_name") val scholarshipName: String? = null,
    @Json(name = "deduction_amount") val deductionAmount: Double? = null,
    @Json(name = "deduction_reason") val deductionReason: String? = null,
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
    val department: String? = null,
    @Json(name = "document_aadhaar_url") val documentAadhaarUrl: String? = null,
    @Json(name = "document_community_url") val documentCommunityUrl: String? = null,
    @Json(name = "document_tc_url") val documentTcUrl: String? = null,
    @Json(name = "document_other_url") val documentOtherUrl: String? = null,
    val internships: List<InternshipEntryDto>? = null
)

@JsonClass(generateAdapter = true)
data class InternshipEntryDto(
    val id: String,
    val organization: String,
    val type: String,
    val role: String,
    val startDate: String,
    val endDate: String,
    val supervisor: String,
    val responsibilities: String,
    val status: String,
    val certificateUrl: String? = null
)

@JsonClass(generateAdapter = true)
data class HallTicketDto(
    val id: String,
    @Json(name = "student_id") val studentId: String,
    @Json(name = "exam_id") val examId: String? = null,
    @Json(name = "exam_name") val examName: String = "",
    @Json(name = "issued_at") val issuedAt: String? = null,
    @Json(name = "is_eligible") val isEligible: Boolean = true,
    @Json(name = "ineligibility_reason") val ineligibilityReason: String? = null,
    @Json(name = "is_issued") val isIssued: Boolean = false,
    @Json(name = "student_signature_url") val studentSignatureUrl: String? = null,
    @Json(name = "principal_signature_url") val principalSignatureUrl: String? = null,
    @Json(name = "coe_signature_url") val coeSignatureUrl: String? = null,
    @Json(name = "student_name") val studentName: String? = null,
    @Json(name = "exam_center") val examCenter: String? = null,
    @Json(name = "exam_date") val examDate: String? = null,
    @Json(name = "file_url") val fileUrl: String? = null
)

@JsonClass(generateAdapter = true)
data class GenerateHallTicketsRequest(
    @Json(name = "student_ids") val studentIds: List<String>,
    @Json(name = "exam_name") val examName: String,
    @Json(name = "exam_center") val examCenter: String? = null,
    @Json(name = "exam_date") val examDate: String? = null,
    @Json(name = "is_eligible") val isEligible: Boolean = true
)

interface CamsApiService {

    @POST("auth/login")
    suspend fun login(@Body request: LoginRequest): Response<LoginResponse>

    @GET("auth/me")
    suspend fun getCurrentUser(): Response<UserMeResponse>

    @POST("auth/refresh")
    suspend fun refreshToken(@Body request: RefreshRequest): Response<RefreshResponse>

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

    @GET("marks/internal/student/me")
    suspend fun getStudentInternalMarks(): Response<List<StudentInternalMarkDto>>

    @GET("students/fees")
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

    @Multipart
    @POST("assignments/{asg_id}/upload-submission")
    suspend fun uploadAssignmentSubmission(
        @Path("asg_id") asgId: String,
        @Part file: okhttp3.MultipartBody.Part
    ): Response<FileUploadResponseDto>

    @POST("fees/{record_id}/create-order")
    suspend fun createOrder(@Path("record_id") recordId: String, @Body request: CreateOrderRequestDto): Response<CreateOrderResponseDto>

    @POST("fees/{record_id}/verify-payment")
    suspend fun verifyPayment(@Path("record_id") recordId: String, @Body request: VerifyPaymentRequestDto): Response<VerifyPaymentResponseDto>

    @GET("students/fees/receipts")
    suspend fun getFeeReceipts(): Response<List<ReceiptDto>>

    @GET("students/fees/loan")
    suspend fun getStudentLoan(): Response<StudentLoanDto?>

    @PUT("students/fees/loan")
    suspend fun upsertStudentLoan(@Body request: StudentLoanRequestDto): Response<StudentLoanDto>

    @GET("students/fees/assistance-requests")
    suspend fun getAssistanceRequests(): Response<List<AssistanceRequestDto>>

    @POST("students/fees/assistance-requests")
    suspend fun createAssistanceRequest(@Body request: AssistanceRequestCreateDto): Response<AssistanceRequestDto>

    @POST("internship-drives/apply")
    suspend fun applyToInternshipDrive(@Body request: InternshipApplyRequestDto): Response<InternshipApplicationResponseDto>

    @GET("internship-drives/applications")
    suspend fun getInternshipApplications(): Response<List<InternshipApplicationResponseDto>>

    @POST("clubs/{id}/leave")
    suspend fun leaveClub(@Path("id") id: String): Response<ClubDto>

    @GET("students/courses")
    suspend fun getStudentCourses(): Response<List<StudentCourseDto>>

    @Multipart
    @POST("students/profile/document")
    suspend fun uploadProfileDocument(
        @Query("document_type") documentType: String,
        @Part file: okhttp3.MultipartBody.Part
    ): Response<UploadDocumentResponseDto>

    @GET("leave/history")
    suspend fun getLeaves(): Response<List<LeaveRequestDto>>

    @GET("students/hall-tickets/")
    suspend fun getHallTickets(): Response<List<HallTicketDto>>

    @GET("admin/hall-tickets/")
    suspend fun getAdminHallTickets(): Response<List<HallTicketDto>>

    @POST("admin/hall-tickets/generate")
    suspend fun generateHallTickets(@Body request: GenerateHallTicketsRequest): Response<List<HallTicketDto>>

    @POST("leave/apply")
    suspend fun applyLeave(@Body request: LeaveApplicationRequest): Response<LeaveRequestDto>

    @GET("students/notices")
    suspend fun getNotices(
        @Query("skip") skip: Int = 0,
        @Query("limit") limit: Int = 20
    ): Response<List<NoticeDto>>

    @GET("faculty/notices")
    suspend fun getFacultyNotices(): Response<List<NoticeDto>>

    @GET("students/parent/notices")
    suspend fun getParentNotices(): Response<List<NoticeDto>>

    @POST("students/parent/inquiries")
    suspend fun submitParentInquiry(@Body request: ParentInquiryRequest): Response<Unit>

    @GET("students/parent/college-info")
    suspend fun getCollegeInfo(): Response<CollegeInfoDto>

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

    @GET("legal-events/registrations")
    suspend fun getLegalEventRegistrations(
        @Query("student_email") studentEmail: String
    ): Response<List<LegalEventRegistrationDto>>

    @POST("legal-events/registrations")
    suspend fun registerForLegalEvent(@Body registration: LegalEventRegistrationDto): Response<Unit>

    @GET("legal-events/questions")
    suspend fun getLegalEventQuestions(
        @Query("student_email") studentEmail: String
    ): Response<List<LegalEventQuestionDto>>

    @POST("legal-events/questions")
    suspend fun submitLegalEventQuestion(@Body question: LegalEventQuestionDto): Response<Unit>

    @GET("clubs")
    suspend fun getStudentClubs(
        @Query("skip") skip: Int = 0,
        @Query("limit") limit: Int = 20
    ): Response<List<ClubDto>>

    @GET("clubs/announcements")
    suspend fun getClubAnnouncements(): Response<List<ClubAnnouncementDto>>

    @GET("academic-calendar/published")
    suspend fun getAcademicCalendar(): Response<AcademicCalendarResponse>

    @GET("academic-calendar/events")
    suspend fun getHodCalendarEvents(): Response<List<HODCalendarEventDto>>

    @POST("academic-calendar/events")
    suspend fun createHodCalendarEvent(@Body request: HODCalendarEventCreateRequest): Response<HODCalendarEventDto>

    @DELETE("academic-calendar/events/{eventId}")
    suspend fun deleteHodCalendarEvent(@Path("eventId") eventId: String): Response<Unit>

    @GET("study-materials/student/approved")
    suspend fun getSyllabus(): Response<List<SyllabusDto>>

    @GET("teaching-logs/syllabus-progress")
    suspend fun getSyllabusProgress(): Response<Map<String, com.example.features.academics.models.SyllabusProgress>>

    @GET("teaching-logs/lesson-plan-tracking")
    suspend fun getLessonPlanTracking(): Response<List<com.example.features.academics.models.LessonPlanItem>>

    @GET("students/notifications")
    suspend fun getNotifications(
        @Query("skip") skip: Int = 0,
        @Query("limit") limit: Int = 20
    ): Response<List<NotificationDto>>

    @POST("students/notifications/{id}/read")
    suspend fun markNotificationRead(@Path("id") id: String): Response<Unit>

    @DELETE("students/notifications/{id}")
    suspend fun deleteNotification(@Path("id") id: String): Response<Unit>

    @POST("students/notifications/read")
    suspend fun markAllNotificationsRead(): Response<Unit>

    @GET("students/parent/children")
    suspend fun getParentChildren(): Response<List<ChildSummaryDto>>

    @GET("students/parent/child/profile")
    suspend fun getParentChildProfile(@Query("child_id") childId: String?): Response<ParentChildProfileDto>

    @GET("students/parent/child/internal-marks")
    suspend fun getParentChildMarks(@Query("child_id") childId: String?): Response<List<ParentChildMarkDto>>

    @GET("students/parent/child/attendance")
    suspend fun getParentChildAttendance(@Query("child_id") childId: String?): Response<ParentChildAttendanceOverviewDto>

    @GET("students/parent/child/fees")
    suspend fun getParentChildFees(@Query("child_id") childId: String?): Response<ParentChildFeeDto>

    @GET("students/parent/child/timetable")
    suspend fun getParentChildTimetable(@Query("child_id") childId: String?): Response<List<ParentChildTimetableDayDto>>

    @POST("students/parent/child/fees/{record_id}/create-order")
    suspend fun createParentFeeOrder(
        @Path("record_id") recordId: String,
        @Query("child_id") childId: String?,
        @Body request: CreateOrderRequestDto
    ): Response<CreateOrderResponseDto>

    @POST("students/parent/child/fees/{record_id}/verify-payment")
    suspend fun verifyParentFeePayment(
        @Path("record_id") recordId: String,
        @Query("child_id") childId: String?,
        @Body request: VerifyPaymentRequestDto
    ): Response<VerifyPaymentResponseDto>

    // Faculty Portal Endpoints
    @GET("faculty/dashboard")
    suspend fun getFacultyDashboardMetrics(): Response<FacultyDashboardMetricsDto>

    @GET("subject-allocations/my-subjects")
    suspend fun getFacultySubjects(): Response<List<FacultySubjectDto>>

    @GET("faculty/profile")
    suspend fun getFacultyProfile(): Response<FacultyProfileDto>

    @PUT("faculty/profile")
    suspend fun updateFacultyProfile(@Body request: FacultyProfileUpdateRequest): Response<FacultyProfileDto>

    @GET("faculty/research/list")
    suspend fun getFacultyResearch(): Response<List<ResearchEntryDto>>

    @POST("faculty/research")
    suspend fun createFacultyResearch(@Body request: ResearchEntryRequest): Response<ResearchEntryDto>

    @PUT("faculty/research/{researchId}")
    suspend fun updateFacultyResearch(@Path("researchId") researchId: String, @Body request: ResearchEntryRequest): Response<ResearchEntryDto>

    @DELETE("faculty/research/{researchId}")
    suspend fun deleteFacultyResearch(@Path("researchId") researchId: String): Response<Map<String, Any>>

    @GET("faculty/profile/activity-summary")
    suspend fun getFacultyActivitySummary(): Response<ActivitySummaryDto>

    @GET("faculty/timetable")
    suspend fun getFacultyTimetable(): Response<List<FacultyTimetableItemDto>>

    // HOD Portal Endpoints
    @GET("faculty/hod/dashboard")
    suspend fun getHODDashboardMetrics(): Response<HODDashboardMetricsDto>

    @GET("faculty/hod/timetable/active-faculty")
    suspend fun getHODActiveFaculty(): Response<List<HODFacultyResponseDto>>

    @GET("faculty/hod/management/students")
    suspend fun getHODManagementStudents(): Response<HODManagementStudentsDto>

    @POST("faculty/students/{studentId}/verify")
    suspend fun verifyHODStudentProfile(@Path("studentId") studentId: String, @Body request: StudentVerifyRequest): Response<Map<String, Any>>

    @GET("leave/hod/pending")
    suspend fun getHODPendingLeaves(): Response<List<LeaveRequestDto>>

    @POST("faculty/hod/leaves/approve/{id}")
    suspend fun approveHODLeave(@Path("id") id: String, @Body request: ApprovalRequest): Response<Map<String, Any>>

    @GET("faculty/hod/timetable/metadata")
    suspend fun getHODTimetableMetadata(): Response<HODTimetableMetadataDto>

    @GET("faculty/hod/timetable/section/{section_id}")
    suspend fun getHODTimetableSection(@Path("section_id") sectionId: String): Response<List<TimetableSlotDto>>

    // Principal Portal Endpoints
    @GET("users/principal/dashboard")
    suspend fun getPrincipalDashboardStats(): Response<PrincipalDashboardStatsDto>

    @GET("users/principal/timetable/approvals")
    suspend fun getPendingTimetableApprovals(): Response<List<TimetableApprovalDto>>

    @POST("users/principal/timetable/approve/{id}")
    suspend fun approveTimetable(@Path("id") id: String, @Body request: ApprovalRequest): Response<Unit>

    @GET("users/principal/leaves")
    suspend fun getPendingLeaveApprovals(): Response<List<LeaveRequestDto>>

    @POST("users/principal/leaves/approve/{id}")
    suspend fun approveLeave(@Path("id") id: String, @Body request: ApprovalRequest): Response<LeaveRequestDto>

    @GET("users/faculty")
    suspend fun getPrincipalPendingFaculty(): Response<List<PrincipalPendingFacultyDto>>

    @POST("users/status/{user_id}")
    suspend fun approvePrincipalFaculty(@Path("user_id") userId: String): Response<Unit>

    @POST("faculty/hod/faculty/reject/{user_id}")
    suspend fun rejectPrincipalFaculty(@Path("user_id") userId: String): Response<Unit>

    @GET("users/principal/circulars")
    suspend fun getPrincipalCirculars(): Response<List<NoticeDto>>

    @POST("users/principal/circulars")
    suspend fun publishPrincipalCircular(@Body request: NoticeCreateRequest): Response<NoticeDto>

    @GET("notices/")
    suspend fun getHodNotices(): Response<List<NoticeDto>>

    @POST("notices/")
    suspend fun createHodNotice(@Body request: NoticeCreateRequest): Response<Map<String, Any>>

    @DELETE("notices/{noticeId}")
    suspend fun deleteHodNotice(@Path("noticeId") noticeId: String): Response<Map<String, Any>>

    @GET("notifications/")
    suspend fun getAdminNotifications(): Response<List<NotificationDto>>

    @GET("notifications/unread-count")
    suspend fun getAdminNotificationsUnreadCount(): Response<Map<String, Int>>

    @POST("notifications/{notificationId}/read")
    suspend fun markAdminNotificationRead(@Path("notificationId") notificationId: String): Response<Map<String, Any>>

    @POST("notifications/read-all")
    suspend fun markAllAdminNotificationsRead(): Response<Map<String, Any>>

    @DELETE("notifications/{notificationId}")
    suspend fun deleteAdminNotification(@Path("notificationId") notificationId: String): Response<Map<String, Any>>

    @GET("users/principal/grievances")
    suspend fun getGrievancesForApproval(): Response<List<GrievanceDto>>

    @POST("users/principal/grievances/resolve/{grievanceId}")
    suspend fun resolveGrievance(@Path("grievanceId") grievanceId: String, @Body request: GrievanceResolveRequest): Response<Map<String, Any>>

    @GET("research-plan/principal/compliance")
    suspend fun getResearchCompliance(): Response<PrincipalComplianceResponseDto>

    @POST("research-plan/cron/check-deadlines")
    suspend fun runComplianceScan(): Response<Map<String, Any>>

    @GET("faculty/principal/faculty-overview")
    suspend fun getPrincipalFacultyOverview(): Response<List<PrincipalFacultyOverviewDto>>

    @GET("users/principal/infrastructure")
    suspend fun getInfrastructureDetails(): Response<InfrastructureResponseDto>

    // Admin Portal Endpoints
    @GET("users/dashboard")
    suspend fun getAdminDashboardMetrics(): Response<AdminDashboardMetricsDto>

    @GET("users/backups/history")
    suspend fun getBackups(): Response<List<BackupDto>>

    // Admin Academic Catalog APIs
    @GET("users/degrees/list")
    suspend fun getDegreesList(): Response<List<DegreeDto>>

    @GET("faculty/courses/list")
    suspend fun getAllCourses(): Response<List<CourseDto>>

    @GET("users/academic-years/list")
    suspend fun getBatches(): Response<List<AdminAcademicYearDto>>

    @GET("users/fees/tracker-data")
    suspend fun getFeeTracker(): Response<List<FeeTrackerDto>>

    @GET("faculty/payroll")
    suspend fun getPayrollDetails(): Response<List<PayrollDto>>

    @GET("faculty/hod/management/students")
    suspend fun getAttendanceDefaulters(): Response<List<AttendanceDefaulterDto>>

    @POST("faculty/notifications/read/{notif_id}")
    suspend fun markFacultyNotificationRead(@Path("notif_id") notifId: String): Response<Unit>

    @POST("faculty/notifications/read-all")
    suspend fun markAllFacultyNotificationsRead(): Response<Unit>

    @GET("faculty/notifications")
    suspend fun getFacultyNotifications(): Response<List<NotificationDto>>

    @GET("online-meetings")
    suspend fun getOnlineMeetings(): Response<List<OnlineMeetingDto>>

    @POST("online-meetings/create")
    suspend fun createOnlineMeeting(@Body request: CreateMeetingRequest): Response<Map<String, Any>>

    @DELETE("online-meetings/{meetingId}")
    suspend fun deleteOnlineMeeting(@Path("meetingId") meetingId: String): Response<Map<String, Any>>

    @POST("clubs/{id}/join")
    suspend fun joinClub(@Path("id") id: String): Response<ClubDto>

    @GET("students/council")
    suspend fun getCouncilData(): Response<CouncilDataDto>

    @POST("students/council/proposals")
    suspend fun submitCouncilProposal(@Body request: CouncilProposalRequest): Response<Unit>

    @POST("students/council/feedback")
    suspend fun submitCouncilFeedback(@Body request: CouncilFeedbackRequest): Response<FeedbackDto>

    @POST("students/council/feedback/{id}/upvote")
    suspend fun upvoteCouncilFeedback(@Path("id") id: Int): Response<FeedbackDto>

    @POST("students/grievances/raise")
    suspend fun submitGrievance(@Body grievance: GrievanceRaiseRequest): Response<Unit>

    @GET("students/lexnova/stats")
    suspend fun getLexNovaStats(): Response<List<LexNovaKpiDto>>

    @GET("students/alumni")
    suspend fun getAlumniNetwork(): Response<List<AlumniMentorDto>>

    @GET("internship-drives")
    suspend fun getInternshipDrives(): Response<List<InternshipDriveDto>>

    @GET("students/certifications")
    suspend fun getCertifications(): Response<List<CertificationRecordDto>>

    @POST("students/certifications")
    suspend fun createCertification(@Body request: CertificationCreateDto): Response<CertificationRecordDto>

    @DELETE("students/certifications/{id}")
    suspend fun deleteCertification(@Path("id") id: String): Response<Unit>

    @GET("activity-points/student")
    suspend fun getActivityPoints(): Response<List<ActivityPointDto>>

    @POST("activity-points/apply")
    suspend fun claimActivityPoints(@Body request: ActivityPointClaimRequest): Response<ActivityPointDto>

    @DELETE("activity-points/{id}")
    suspend fun deleteActivityPoint(@Path("id") id: String): Response<Unit>

    @Multipart
    @POST("activity-points/upload-document")
    suspend fun uploadActivityPointDocument(@Part file: okhttp3.MultipartBody.Part): Response<UploadDocumentResponseDto>

    @GET("activity-points/faculty")
    suspend fun getFacultyActivityPoints(): Response<List<ActivityPointDto>>

    @POST("activity-points/review/{applicationId}")
    suspend fun reviewActivityPoints(
        @Path("applicationId") applicationId: String,
        @Body request: ActivityPointReviewRequest
    ): Response<ActivityPointDto>

    @GET("activity-points/categories")
    suspend fun getActivityPointCategories(): Response<List<ActivityPointCategoryDto>>

    @POST("activity-points/categories")
    suspend fun createActivityPointCategory(@Body request: ActivityPointCategoryRequest): Response<ActivityPointCategoryDto>

    @PUT("activity-points/categories/{categoryId}")
    suspend fun updateActivityPointCategory(@Path("categoryId") categoryId: String, @Body request: ActivityPointCategoryRequest): Response<ActivityPointCategoryDto>

    @DELETE("activity-points/categories/{categoryId}")
    suspend fun deleteActivityPointCategory(@Path("categoryId") categoryId: String): Response<Map<String, Any>>

    @GET("community-service/opportunities")
    suspend fun getServiceOpportunities(): Response<List<ServiceOpportunityDto>>

    @POST("community-service/opportunities/{id}/apply")
    suspend fun applyToServiceOpportunity(@Path("id") id: Int): Response<ServiceLogDto>

    @POST("community-service/log-hours")
    suspend fun logServiceHours(@Body request: LogServiceHoursRequest): Response<ServiceLogDto>

    @GET("community-service/logs")
    suspend fun getServiceLogs(): Response<List<ServiceLogDto>>

    @DELETE("community-service/logs/{id}")
    suspend fun deleteServiceLog(@Path("id") id: String): Response<Unit>

    @Multipart
    @POST("community-service/upload-document")
    suspend fun uploadServiceDocument(@Part file: okhttp3.MultipartBody.Part): Response<UploadDocumentResponseDto>

    @GET("innovation-wall/projects")
    suspend fun getInnovationProjects(): Response<List<InnovationProjectDto>>

    @POST("innovation-wall/projects")
    suspend fun createInnovationProject(@Body request: InnovationProjectCreateRequest): Response<InnovationProjectDto>

    @POST("innovation-wall/projects/{id}/like")
    suspend fun toggleInnovationProjectLike(@Path("id") id: String): Response<InnovationProjectDto>

    @POST("innovation-wall/projects/{id}/comments")
    suspend fun addInnovationProjectComment(@Path("id") id: String, @Body request: InnovationCommentRequest): Response<InnovationProjectDto>

    @DELETE("innovation-wall/projects/{id}")
    suspend fun deleteInnovationProject(@Path("id") id: String): Response<Unit>

    @GET("students/papers")
    suspend fun getResearchPapers(): Response<List<ResearchPaperDto>>

    @POST("students/papers")
    suspend fun submitResearchPaper(@Body request: ResearchPaperSubmitRequest): Response<ResearchPaperDto>

    @Multipart
    @POST("students/papers/upload")
    suspend fun uploadResearchPaper(@Part file: okhttp3.MultipartBody.Part): Response<PaperUploadResponseDto>

    @GET("students/parent/child/performance")
    suspend fun getParentChildPerformance(@Query("child_id") childId: String?): Response<List<PerformanceDataDto>>

    @GET("students/parent/child/subject-attendance")
    suspend fun getParentChildSubjectAttendance(@Query("child_id") childId: String?): Response<List<SubjectAttendanceDto>>

    @GET("assignments/my-assignments")
    suspend fun getFacultyAssignments(): Response<List<FacultyAssignmentDto>>

    @POST("assignments/create")
    suspend fun createAssignment(@Body request: CreateAssignmentRequest): Response<FacultyAssignmentDto>

    @PUT("assignments/{asgId}")
    suspend fun updateAssignment(@Path("asgId") asgId: String, @Body request: CreateAssignmentRequest): Response<FacultyAssignmentDto>

    @DELETE("assignments/{asgId}")
    suspend fun deleteAssignment(@Path("asgId") asgId: String): Response<Unit>

    @GET("assignments/submissions")
    suspend fun getAssignmentSubmissions(): Response<List<FacultyAssignmentSubmissionDto>>

    @POST("assignments/grade/{submissionId}")
    suspend fun gradeSubmission(@Path("submissionId") submissionId: String, @Body request: GradeSubmissionRequest): Response<FacultyAssignmentSubmissionDto>

    @GET("faculty/students/list")
    suspend fun getFacultyStudents(@Query("semester") semester: Int? = null): Response<List<FacultyStudentDto>>

    @GET("faculty/attendance/sections")
    suspend fun getFacultyAttendanceSections(): Response<List<FacultyAttendanceSectionDto>>

    @GET("faculty/attendance/students")
    suspend fun getFacultyAttendanceStudents(
        @Query("section_id") sectionId: String,
        @Query("subject_id") subjectId: String
    ): Response<List<FacultyAttendanceStudentDto>>

    @POST("faculty/attendance/mark-bulk")
    suspend fun markFacultyAttendanceBulk(@Body request: BulkAttendanceMarkRequest): Response<Unit>

    @GET("smart-classroom/activities")
    suspend fun getClassroomActivities(): Response<List<ClassroomActivityDto>>

    @POST("smart-classroom/activities")
    suspend fun createClassroomActivity(@Body request: CreateClassroomActivityRequest): Response<ClassroomActivityDto>

    @GET("smart-classroom/interactions")
    suspend fun getClassroomInteractions(): Response<List<StudentInteractionDto>>

    @POST("smart-classroom/interactions")
    suspend fun createClassroomInteraction(@Body request: CreateInteractionRequest): Response<StudentInteractionDto>

    @GET("smart-classroom/session-summaries")
    suspend fun getSessionSummaries(): Response<List<SessionSummaryDto>>

    @POST("smart-classroom/session-summaries")
    suspend fun createSessionSummary(@Body request: CreateSessionSummaryRequest): Response<SessionSummaryDto>

    @GET("marks/internal")
    suspend fun getInternalMarks(
        @Query("section_id") sectionId: String,
        @Query("subject_id") subjectId: String,
        @Query("academic_year") academicYear: String? = null
    ): Response<List<InternalMarkStudentDto>>

    @POST("marks/internal")
    suspend fun saveInternalMarks(@Body request: SaveInternalMarksRequest): Response<Unit>

    @POST("marks/internal/submit")
    suspend fun submitInternalMarks(@Body request: SubmitMarksRequest): Response<Unit>

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

    @POST("study-materials/edit/{materialId}")
    suspend fun editStudyMaterial(
        @Path("materialId") materialId: String,
        @Body payload: UploadMaterialRequestDto
    ): Response<FacultyMaterialDto>

    @POST("study-materials/archive/{materialId}")
    suspend fun archiveStudyMaterial(@Path("materialId") materialId: String): Response<Unit>

    @GET("faculty/mentor/students")
    suspend fun getMentorStudents(): Response<List<FacultyMentorshipStudentDto>>

    @GET("faculty/mentor/students/{student_id}/record")
    suspend fun getMentorStudentRecord(@Path("student_id") studentId: String): Response<FacultyMentorshipRecordDto>

    @POST("faculty/mentor/students/{student_id}/record")
    suspend fun saveMentorStudentRecord(@Path("student_id") studentId: String, @Body payload: FacultyMentorshipRecordDto): Response<FacultyMentorshipRecordDto>

    @GET("leave/balances")
    suspend fun getLeaveBalances(): Response<LeaveBalanceDto>

    @Multipart
    @POST("leave/apply")
    suspend fun applyLeaveMultipart(
        @Part("type") type: okhttp3.RequestBody,
        @Part("from_date") fromDate: okhttp3.RequestBody,
        @Part("to_date") toDate: okhttp3.RequestBody,
        @Part("reason") reason: okhttp3.RequestBody,
        @Part("emergency_contact") emergencyContact: okhttp3.RequestBody,
        @Part file: okhttp3.MultipartBody.Part?
    ): Response<LeaveRequestDto>

    @DELETE("leave/{leaveId}")
    suspend fun cancelLeave(@Path("leaveId") leaveId: String): Response<Map<String, Any>>

    @GET("teaching-logs/diaries")
    suspend fun getClassDiaries(): Response<List<ClassDiaryDto>>

    @POST("teaching-logs/diaries")
    suspend fun createClassDiary(@Body request: ClassDiaryRequest): Response<ClassDiaryDto>

    @PUT("teaching-logs/diaries/{id}")
    suspend fun updateClassDiary(@Path("id") id: String, @Body request: ClassDiaryRequest): Response<ClassDiaryDto>

    @POST("teaching-logs/diaries/{id}/review")
    suspend fun reviewClassDiary(@Path("id") id: String, @Body request: DiaryReviewRequest): Response<ClassDiaryDto>

    @GET("class-advisor/my-assignment")
    suspend fun getAdvisorAssignment(): Response<AdvisorAssignmentDto>

    @GET("class-advisor/students")
    suspend fun getAdvisorClassStudents(): Response<List<AdvisorStudentDto>>

    @GET("leave/advisor/students")
    suspend fun getAdvisorStudentLeaves(): Response<List<AdvisorLeaveDto>>

    @POST("leave/advisor/approve/{leaveId}")
    suspend fun advisorApproveLeave(@Path("leaveId") leaveId: String, @Body request: LeaveApprovalRequest): Response<AdvisorLeaveDto>

    @GET("messages/contacts")
    suspend fun getMessageContacts(): Response<List<MessageContactDto>>

    @GET("messages/conversations")
    suspend fun getConversations(): Response<List<ConversationDto>>

    @GET("messages/thread/{userId}")
    suspend fun getMessageThread(@Path("userId") userId: String): Response<List<MessageDto>>

    @POST("messages/send")
    suspend fun sendMessage(@Body request: SendMessageRequest): Response<MessageDto>

    @POST("messages/read/{userId}")
    suspend fun markThreadRead(@Path("userId") userId: String): Response<Unit>

    @GET("faculty/salary-slips")
    suspend fun getFacultySalarySlips(): Response<List<FacultySalarySlipDto>>

    @GET("internship-drives")
    suspend fun getFacultyInternshipDrives(): Response<List<FacultyInternshipDriveDto>>

    @GET("internship-drives/applications")
    suspend fun getFacultyInternshipApplications(): Response<List<InternshipApplicationDto>>

    @PATCH("internship-drives/applications/{applicationId}")
    suspend fun reviewInternshipApplication(
        @Path("applicationId") applicationId: String,
        @Body request: InternshipApplicationReviewRequest
    ): Response<InternshipApplicationDto>

    @GET("internship-drives/partners")
    suspend fun getPartnerCompanies(): Response<List<PartnerCompanyDto>>

    @POST("internship-drives/partners")
    suspend fun createPartnerCompany(@Body request: PartnerCompanyRequest): Response<PartnerCompanyDto>

    @PUT("internship-drives/partners/{partnerId}")
    suspend fun updatePartnerCompany(@Path("partnerId") partnerId: String, @Body request: PartnerCompanyRequest): Response<PartnerCompanyDto>

    @DELETE("internship-drives/partners/{partnerId}")
    suspend fun deletePartnerCompany(@Path("partnerId") partnerId: String): Response<Map<String, Any>>

    @GET("legal-events")
    suspend fun getLegalEvents(): Response<List<FacultyLegalEventDto>>

    @POST("legal-events/faculty")
    suspend fun postLegalEvent(@Body request: CreateLegalEventRequest): Response<Unit>

    @GET("legal-events/pending")
    suspend fun getPendingLegalEvents(): Response<List<FacultyLegalEventDto>>

    @PATCH("legal-events/{eventId}/approve")
    suspend fun approveLegalEvent(@Path("eventId") eventId: String): Response<Map<String, Any>>

    @PATCH("legal-events/{eventId}/reject")
    suspend fun rejectLegalEvent(@Path("eventId") eventId: String, @Body remarks: String? = null): Response<Map<String, Any>>

    @GET("online-meetings/recordings")
    suspend fun getFacultyRecordings(): Response<List<FacultyRecordingDto>>

    @POST("online-meetings/recordings/create")
    suspend fun createRecording(@Body request: CreateRecordingRequest): Response<Unit>

    @DELETE("online-meetings/recordings/{recordingId}")
    suspend fun deleteRecording(@Path("recordingId") recordingId: String): Response<Unit>

    @POST("auth/change-password")
    suspend fun changePassword(@Body request: ChangePasswordRequest): Response<Unit>

    @POST("auth/forgot-password")
    suspend fun forgotPassword(@Body request: ForgotPasswordRequest): Response<MessageResponseDto>

    @POST("auth/reset-password")
    suspend fun resetPassword(@Body request: ResetPasswordRequest): Response<MessageResponseDto>

    @POST("notifications/device-token")
    suspend fun registerDeviceToken(@Body request: DeviceTokenRequest): Response<Map<String, Any>>

    @HTTP(method = "DELETE", path = "notifications/device-token", hasBody = true)
    suspend fun unregisterDeviceToken(@Body request: DeviceTokenRequest): Response<Map<String, Any>>

    @POST("auth/request-email-change")
    suspend fun requestEmailChange(@Body request: RequestEmailChangeRequest): Response<MessageResponseDto>

    @POST("auth/confirm-email-change")
    suspend fun confirmEmailChange(@Body request: ConfirmEmailChangeRequest): Response<MessageResponseDto>

    @retrofit2.http.PATCH("auth/notification-preferences")
    suspend fun updateNotificationPreferences(@Body request: NotificationPreferencesRequest): Response<UserMeResponse>

    // Additional HOD Endpoints
    @GET("teaching-logs/hod/dashboard")
    suspend fun getHODTeachingLogsDashboard(): Response<HODTeachingLogsDashboardDto>

    @GET("teaching-logs/pending-entries")
    suspend fun getHODPendingEntries(): Response<List<HODPendingEntryDto>>

    @GET("teaching-logs/hod/syllabus/metadata")
    suspend fun getHODSyllabusMetadata(): Response<HODSyllabusMetadataDto>

    @GET("teaching-logs/hod/syllabus/courses")
    suspend fun getHODSyllabusCourses(): Response<List<HODCourseDto>>

    @GET("teaching-logs/hod/syllabus/courses/{courseName}/plan")
    suspend fun getHODCoursePlan(@Path("courseName") courseName: String): Response<Map<String, List<String>>>

    @POST("teaching-logs/hod/syllabus/courses/{courseName}/plan")
    suspend fun saveHODCoursePlan(@Path("courseName") courseName: String, @Body request: HODLessonPlanSaveRequest): Response<Map<String, Any>>

    @GET("faculty/hod/attendance/monitoring")
    suspend fun getHODAttendanceMonitoring(): Response<List<HODAttendanceMonitoringDto>>

    @GET("faculty/hod/reports/department")
    suspend fun getHODDepartmentReports(): Response<HODDepartmentReportDto>

    @GET("faculty/hod/reports/department")
    suspend fun getDepartmentReportFor(@Query("department_id") departmentId: String?): Response<HODDepartmentReportDto>

    @GET("faculty/hod/reports/students")
    suspend fun getHODStudentReports(): Response<HODStudentReportDto>

    @Streaming
    @GET("faculty/hod/reports/export/department")
    suspend fun exportHODDepartmentReportCsv(): Response<okhttp3.ResponseBody>

    @Streaming
    @GET("faculty/hod/reports/export/students")
    suspend fun exportHODStudentReportCsv(): Response<okhttp3.ResponseBody>

    @GET("research-plan/hod/monitoring")
    suspend fun getHODResearchMonitoring(): Response<List<HODResearchMonitoringDto>>

    @GET("research-plan/hod/pending-proofs")
    suspend fun getHODPendingProofs(): Response<List<HODPendingProofDto>>

    @POST("research-plan/hod/verify/{id}")
    suspend fun verifyResearchProof(@Path("id") proofId: String, @Body request: VerificationRequestDto): Response<Map<String, Any>>

    @GET("faculty/hod/workload")
    suspend fun getHODWorkloads(): Response<List<HODWorkloadDto>>

    @GET("marks/internal/hod/pending")
    suspend fun getHODPendingMarks(): Response<List<HODPendingMarksGroupDto>>

    @POST("marks/internal/hod/approve")
    suspend fun approveHODMarks(@Body request: ApproveMarksGroupRequest): Response<Map<String, Any>>

    @GET("faculty/hod/mentors")
    suspend fun getHODMentors(): Response<HODMentorsPayloadDto>

    @POST("faculty/hod/mentor/assign")
    suspend fun assignHODMentor(@Body request: MentorAssignmentRequestDto): Response<Unit>

    @GET("subject-allocations/setup")
    suspend fun getAcademicSetup(): Response<AcademicSetupDto>

    @GET("subject-allocations/subjects")
    suspend fun getAllocationSubjects(): Response<List<SubjectInfoDto>>

    @GET("subject-allocations/course-sections")
    suspend fun getCourseSections(@Query("course_id") courseId: String): Response<List<AcademicSetupSectionDto>>

    @GET("subject-allocations/faculty")
    suspend fun getAllocationFaculty(): Response<List<FacultyWorkloadInfoDto>>

    @GET("subject-allocations/allocations")
    suspend fun getSubjectAllocations(): Response<List<SubjectAllocationDto>>

    @POST("subject-allocations/allocate")
    suspend fun allocateSubjects(@Body request: List<SubjectAllocationCreateDto>): Response<Map<String, Any>>

    @GET("faculty/substitutions/sync")
    suspend fun getSubstitutions(): Response<List<HODSubstitutionDto>>

    @POST("faculty/hod/substitution/assign")
    suspend fun assignSubstitution(@Body request: SubstitutionAssignRequest): Response<Map<String, Any>>

    @GET("faculty/hod/substitution/available-faculty")
    suspend fun getAvailableSubstituteFaculty(): Response<List<HODFacultyResponseDto>>

    @POST("faculty/hod/timetable/submit")
    suspend fun submitHodTimetable(@Body request: TimetableSubmitRequestDto): Response<Map<String, Any>>

    @GET("study-materials/hod/pending")
    suspend fun getHodPendingMaterials(): Response<List<HodPendingMaterialDto>>

    @POST("study-materials/hod/review/{materialId}")
    suspend fun reviewHodMaterial(@Path("materialId") materialId: String, @Body request: MaterialReviewRequest): Response<Map<String, Any>>

    @GET("study-materials/principal/pending")
    suspend fun getPrincipalPendingMaterials(): Response<List<HodPendingMaterialDto>>

    @POST("study-materials/principal/review/{materialId}")
    suspend fun reviewPrincipalMaterial(@Path("materialId") materialId: String, @Body request: MaterialReviewRequest): Response<Map<String, Any>>

    @GET("class-advisor/hod/classes")
    suspend fun getHodClasses(): Response<ClassAdvisorSetupDto>

    @POST("class-advisor/hod/assign")
    suspend fun assignClassAdvisor(@Body request: AdvisorAssignmentRequestDto): Response<Map<String, Any>>

    @GET("faculty/attendance/correction-requests")
    suspend fun getAttendanceCorrectionRequests(@Query("status_filter") statusFilter: String = "all"): Response<List<AttendanceCorrectionDto>>

    @POST("faculty/attendance/correction-requests/{requestId}/approve")
    suspend fun approveAttendanceCorrection(@Path("requestId") requestId: String): Response<Map<String, Any>>

    @POST("faculty/attendance/correction-requests/{requestId}/reject")
    suspend fun rejectAttendanceCorrection(@Path("requestId") requestId: String, @Body request: RejectCorrectionRequestDto): Response<Map<String, Any>>

    @GET("faculty/profile/update-requests/pending")
    suspend fun getPendingProfileUpdateRequests(): Response<List<FacultyProfileUpdateRequestDto>>

    @POST("faculty/profile/update-requests/{requestId}/approve")
    suspend fun approveProfileUpdateRequest(@Path("requestId") requestId: String, @Body request: ProfileUpdateReviewRequest): Response<FacultyProfileUpdateRequestDto>

    @POST("faculty/profile/update-requests/{requestId}/reject")
    suspend fun rejectProfileUpdateRequest(@Path("requestId") requestId: String, @Body request: ProfileUpdateReviewRequest): Response<FacultyProfileUpdateRequestDto>

    @POST("faculty/profile/update-requests/{requestId}/request-changes")
    suspend fun requestChangesProfileUpdateRequest(@Path("requestId") requestId: String, @Body request: ProfileUpdateReviewRequest): Response<FacultyProfileUpdateRequestDto>

    @GET("users/salary-slips")
    suspend fun getFacultyPayrollAdmin(): Response<List<AdminPayrollDto>>

    @GET("users/backups/history")
    suspend fun getBackupHistoryAdmin(): Response<List<AdminBackupDto>>

    @GET("users/system-settings")
    suspend fun getSystemSettingsAdmin(): Response<AdminSystemSettingsDto>

    @GET("users/backups/audit-logs")
    suspend fun getAuditLogsAdmin(): Response<List<AdminAuditLogDto>>

    @POST("users/system-settings")
    suspend fun saveSystemSettings(@Body request: AdminSystemSettingsDto): Response<AdminSystemSettingsDto>

    @POST("users/backups/create")
    suspend fun createBackup(): Response<Map<String, Any>>

    @DELETE("users/backups/{backupId}")
    suspend fun deleteBackup(@Path("backupId") backupId: String): Response<Map<String, Any>>

    @POST("users/backups/restore/{backupId}")
    suspend fun restoreBackup(@Path("backupId") backupId: String): Response<Map<String, Any>>

    @GET("users/backups/settings")
    suspend fun getBackupSettings(): Response<AdminBackupSettingsDto>

    @POST("users/backups/settings")
    suspend fun saveBackupSettings(@Body request: AdminBackupSettingsDto): Response<AdminBackupSettingsDto>

    @POST("users/academic-years/initialize")
    suspend fun initializeAcademicYear(@Body request: Map<String, @JvmSuppressWildcards Any?>): Response<AdminAcademicYearDto>

    @PUT("users/academic-years/update/{ayId}")
    suspend fun updateAcademicYear(@Path("ayId") ayId: String, @Body request: Map<String, @JvmSuppressWildcards Any?>): Response<AdminAcademicYearDto>

    @DELETE("users/academic-years/delete/{ayId}")
    suspend fun deleteAcademicYear(@Path("ayId") ayId: String): Response<Map<String, Any>>



    @GET("users/list")
    suspend fun getAllUsers(): Response<List<AdminUserDto>>

    @GET("users/departments/list")
    suspend fun getDepartmentsList(): Response<List<AdminDepartmentDto>>

    @GET("users/degrees/list")
    suspend fun getDegreesListAdmin(): Response<List<AdminDegreeDto>>

    @GET("users/courses/by-degree/all")
    suspend fun getAllCoursesAdmin(): Response<List<AdminCourseDto>>

    @GET("users/backups/history")
    suspend fun getBackupsAdmin(): Response<List<AdminBackupDto>>

    @GET("users/attendance-defaulters")
    suspend fun getAttendanceDefaultersAdmin(): Response<List<AdminAttendanceDefaulterDto>>

    @PATCH("users/attendance-defaulters/{studentId}/pay")
    suspend fun markAttendanceFinePaid(@Path("studentId") studentId: String): Response<Map<String, Any>>

    @PATCH("users/attendance-defaulters/{studentId}/adjust")
    suspend fun adjustAttendancePercentage(@Path("studentId") studentId: String, @Body request: Map<String, @JvmSuppressWildcards Any?>): Response<Map<String, Any>>

    @POST("users/departments/create")
    suspend fun createDepartment(@Body request: Map<String, @JvmSuppressWildcards Any?>): Response<AdminDepartmentDto>

    @DELETE("users/departments/delete/{deptId}")
    suspend fun deleteDepartment(@Path("deptId") deptId: String): Response<Map<String, Any>>

    @POST("users/degrees/create")
    suspend fun createDegree(@Body request: Map<String, @JvmSuppressWildcards Any?>): Response<AdminDegreeDto>

    @DELETE("users/degrees/delete/{degreeId}")
    suspend fun deleteDegree(@Path("degreeId") degreeId: String): Response<Map<String, Any>>

    @POST("users/courses/create")
    suspend fun createCourse(@Body request: Map<String, @JvmSuppressWildcards Any?>): Response<AdminCourseDto>

    @PUT("users/courses/update/{courseId}")
    suspend fun updateCourse(@Path("courseId") courseId: String, @Body request: Map<String, @JvmSuppressWildcards Any?>): Response<AdminCourseDto>

    @DELETE("users/courses/delete/{courseId}")
    suspend fun deleteCourse(@Path("courseId") courseId: String): Response<Map<String, Any>>

    @GET("users/courses/by-degree/{degreeId}")
    suspend fun getCoursesByDegree(@Path("degreeId") degreeId: String): Response<List<AdminCourseDto>>

    @GET("fees/admin/fee-structures")
    suspend fun getAdminFeeStructures(): Response<List<AdminFeeStructureDto>>

    @GET("fees/admin/scholarship-types/list")
    suspend fun getAdminScholarshipTypes(): Response<List<AdminScholarshipTypeDto>>

    @GET("fees/admin/search-students")
    suspend fun searchStudentsForFees(@Query("query") query: String): Response<List<AdminFeeStudentDto>>

    @GET("fees/admin/student/{studentId}")
    suspend fun getAdminStudentFees(@Path("studentId") studentId: String): Response<List<AdminStudentFeeRecordDto>>

    @POST("fees/admin/collect")
    suspend fun adminCollectFee(@Body request: AdminCollectFeeRequest): Response<Map<String, Any>>


    // --- Admin Endpoints ---
    @GET("admin/departments")
    suspend fun getDepartments(): Response<List<AdminDepartmentDto>>

    @GET("admin/dashboard/metrics")
    suspend fun getDashboardMetrics(): Response<AdminDashboardMetricsDto>

    @GET("admin/users")
    suspend fun getUsers(): Response<List<UserDto>>

    @GET("admin/courses")
    suspend fun getCourses(): Response<List<AdminCourseDto>>

    // --- Principal Endpoints ---
    @GET("principal/faculty/pending")
    suspend fun getPendingFaculty(): Response<List<PendingFacultyDto>>

    @POST("principal/faculty/{id}/approve")
    suspend fun approveFaculty(@Path("id") id: String): Response<Unit>

    @GET("principal/circulars")
    suspend fun getCirculars(): Response<List<CircularDto>>

    @POST("principal/circulars")
    suspend fun publishCircular(@Body request: CircularPublishRequest): Response<Unit>

    @GET("principal/dashboard/stats")
    suspend fun getDashboardStats(): Response<PrincipalDashboardStatsDto>

    @GET("principal/grievances")
    suspend fun getGrievances(): Response<List<GrievanceDto>>

    // --- Moot Court Memorial Drafts ---
    @GET("students/moot-court/memorials")
    suspend fun getMootCourtMemorials(): Response<List<MootCourtMemorialDto>>

    @POST("students/moot-court/memorials")
    suspend fun createMootCourtMemorial(@Body request: CreateMemorialRequest): Response<MootCourtMemorialDto>

    @PUT("students/moot-court/memorials/{memorial_id}")
    suspend fun updateMootCourtMemorial(
        @Path("memorial_id") memorialId: String,
        @Body request: UpdateMemorialRequest
    ): Response<MootCourtMemorialDto>

    @retrofit2.http.DELETE("students/moot-court/memorials/{memorial_id}")
    suspend fun deleteMootCourtMemorial(@Path("memorial_id") memorialId: String): Response<MessageResponseDto>

    // --- Saved Citations ---
    @GET("students/citations")
    suspend fun getSavedCitations(): Response<List<SavedCitationDto>>

    @POST("students/citations")
    suspend fun createSavedCitation(@Body request: CreateCitationRequest): Response<SavedCitationDto>

    @retrofit2.http.DELETE("students/citations/{citation_id}")
    suspend fun deleteSavedCitation(@Path("citation_id") citationId: String): Response<MessageResponseDto>

    // --- Chatbot ---
    @POST("chatbot/message")
    suspend fun sendChatMessage(@Body request: ChatMessageRequest): Response<ChatMessageResponse>

    @GET("chatbot/history")
    suspend fun getChatHistory(): Response<List<ChatSessionSummaryDto>>

    @GET("chatbot/session/{session_id}")
    suspend fun getChatSessionMessages(@Path("session_id") sessionId: String): Response<List<ChatMessageDto>>

    @retrofit2.http.DELETE("chatbot/session/{session_id}")
    suspend fun deleteChatSession(@Path("session_id") sessionId: String): Response<Unit>

    // ==================== ERP: Hostel ====================

    @GET("hostel/blocks")
    suspend fun getHostelBlocks(): Response<List<HostelBlockDto>>

    @POST("hostel/blocks")
    suspend fun createHostelBlock(@Body request: HostelBlockCreateRequest): Response<HostelBlockDto>

    @PUT("hostel/blocks/{blockId}")
    suspend fun updateHostelBlock(@Path("blockId") blockId: String, @Body request: Map<String, @JvmSuppressWildcards Any?>): Response<HostelBlockDto>

    @DELETE("hostel/blocks/{blockId}")
    suspend fun deleteHostelBlock(@Path("blockId") blockId: String): Response<Map<String, Any>>

    @GET("hostel/rooms")
    suspend fun getHostelRooms(@Query("block_id") blockId: String? = null): Response<List<HostelRoomDto>>

    @POST("hostel/rooms")
    suspend fun createHostelRoom(@Body request: HostelRoomCreateRequest): Response<HostelRoomDto>

    @DELETE("hostel/rooms/{roomId}")
    suspend fun deleteHostelRoom(@Path("roomId") roomId: String): Response<Map<String, Any>>

    @GET("hostel/allocations")
    suspend fun getHostelAllocations(@Query("status") status: String? = null): Response<List<HostelAllocationDto>>

    @POST("hostel/allocations")
    suspend fun createHostelAllocation(@Body request: HostelAllocationCreateRequest): Response<HostelAllocationDto>

    @POST("hostel/allocations/{allocationId}/vacate")
    suspend fun vacateHostelAllocation(@Path("allocationId") allocationId: String): Response<Map<String, Any>>

    // ==================== ERP: Inventory ====================

    @GET("inventory/items")
    suspend fun getInventoryItems(@Query("category") category: String? = null, @Query("low_stock_only") lowStockOnly: Boolean? = null): Response<List<InventoryItemDto>>

    @POST("inventory/items")
    suspend fun createInventoryItem(@Body request: InventoryItemCreateRequest): Response<InventoryItemDto>

    @PUT("inventory/items/{itemId}")
    suspend fun updateInventoryItem(@Path("itemId") itemId: String, @Body request: Map<String, @JvmSuppressWildcards Any?>): Response<InventoryItemDto>

    @DELETE("inventory/items/{itemId}")
    suspend fun deleteInventoryItem(@Path("itemId") itemId: String): Response<Map<String, Any>>

    @POST("inventory/items/{itemId}/movement")
    suspend fun recordInventoryMovement(@Path("itemId") itemId: String, @Body request: StockMovementRequest): Response<InventoryItemDto>

    @GET("inventory/transactions")
    suspend fun getInventoryTransactions(@Query("item_id") itemId: String? = null): Response<List<InventoryTransactionDto>>

    // ==================== ERP: Library ====================

    @GET("library/books")
    suspend fun getLibraryBooks(@Query("category") category: String? = null): Response<List<LibraryBookDto>>

    @POST("library/books")
    suspend fun createLibraryBook(@Body request: LibraryBookCreateRequest): Response<LibraryBookDto>

    @PUT("library/books/{bookId}")
    suspend fun updateLibraryBook(@Path("bookId") bookId: String, @Body request: Map<String, @JvmSuppressWildcards Any?>): Response<LibraryBookDto>

    @DELETE("library/books/{bookId}")
    suspend fun deleteLibraryBook(@Path("bookId") bookId: String): Response<Map<String, Any>>

    @GET("library/issues")
    suspend fun getLibraryIssues(@Query("status") status: String? = null): Response<List<LibraryIssueDto>>

    @POST("library/issues")
    suspend fun createLibraryIssue(@Body request: LibraryIssueCreateRequest): Response<LibraryIssueDto>

    @POST("library/issues/{issueId}/return")
    suspend fun returnLibraryIssue(@Path("issueId") issueId: String, @Body request: LibraryReturnRequest): Response<LibraryIssueDto>

    // ==================== ERP: Transport ====================

    @GET("transport/routes")
    suspend fun getTransportRoutes(): Response<List<TransportRouteDto>>

    @POST("transport/routes")
    suspend fun createTransportRoute(@Body request: TransportRouteCreateRequest): Response<TransportRouteDto>

    @PUT("transport/routes/{routeId}")
    suspend fun updateTransportRoute(@Path("routeId") routeId: String, @Body request: Map<String, @JvmSuppressWildcards Any?>): Response<TransportRouteDto>

    @DELETE("transport/routes/{routeId}")
    suspend fun deleteTransportRoute(@Path("routeId") routeId: String): Response<Map<String, Any>>

    @GET("transport/vehicles")
    suspend fun getTransportVehicles(): Response<List<TransportVehicleDto>>

    @POST("transport/vehicles")
    suspend fun createTransportVehicle(@Body request: TransportVehicleCreateRequest): Response<TransportVehicleDto>

    @PUT("transport/vehicles/{vehicleId}")
    suspend fun updateTransportVehicle(@Path("vehicleId") vehicleId: String, @Body request: Map<String, @JvmSuppressWildcards Any?>): Response<TransportVehicleDto>

    @DELETE("transport/vehicles/{vehicleId}")
    suspend fun deleteTransportVehicle(@Path("vehicleId") vehicleId: String): Response<Map<String, Any>>

    @GET("transport/passes")
    suspend fun getTransportPasses(@Query("status") status: String? = null): Response<List<TransportPassDto>>

    @POST("transport/passes")
    suspend fun createTransportPass(@Body request: TransportPassCreateRequest): Response<TransportPassDto>

    @POST("transport/passes/{passId}/cancel")
    suspend fun cancelTransportPass(@Path("passId") passId: String): Response<Map<String, Any>>

    // ==================== Budget & Grants ====================

    @GET("budget/summary")
    suspend fun getBudgetSummary(): Response<BudgetSummaryDto>

    @GET("budget/line-items")
    suspend fun getBudgetLineItems(): Response<List<BudgetLineItemDto>>

    @POST("budget/line-items")
    suspend fun createBudgetLineItem(@Body request: BudgetLineItemCreateRequest): Response<BudgetLineItemDto>

    @PUT("budget/line-items/{itemId}")
    suspend fun updateBudgetLineItem(@Path("itemId") itemId: String, @Body request: Map<String, @JvmSuppressWildcards Any?>): Response<BudgetLineItemDto>

    @DELETE("budget/line-items/{itemId}")
    suspend fun deleteBudgetLineItem(@Path("itemId") itemId: String): Response<Map<String, Any>>

    @POST("budget/line-items/{itemId}/expenses")
    suspend fun recordBudgetExpense(@Path("itemId") itemId: String, @Body request: BudgetExpenseCreateRequest): Response<BudgetExpenseDto>

    @GET("budget/line-items/{itemId}/expenses")
    suspend fun getBudgetExpenses(@Path("itemId") itemId: String): Response<List<BudgetExpenseDto>>

    @GET("budget/grants")
    suspend fun getGrants(): Response<List<GrantDto>>

    @POST("budget/grants")
    suspend fun createGrant(@Body request: GrantCreateRequest): Response<GrantDto>

    @PUT("budget/grants/{grantId}")
    suspend fun updateGrant(@Path("grantId") grantId: String, @Body request: Map<String, @JvmSuppressWildcards Any?>): Response<GrantDto>

    @DELETE("budget/grants/{grantId}")
    suspend fun deleteGrant(@Path("grantId") grantId: String): Response<Map<String, Any>>

}

@JsonClass(generateAdapter = true)
data class BudgetSummaryDto(
    @Json(name = "total_allocated") val totalAllocated: Double,
    @Json(name = "total_spent") val totalSpent: Double,
    @Json(name = "total_remaining") val totalRemaining: Double,
    @Json(name = "total_grants_sanctioned") val totalGrantsSanctioned: Double,
    @Json(name = "total_grants_disbursed") val totalGrantsDisbursed: Double,
    @Json(name = "active_grants_count") val activeGrantsCount: Int
)

@JsonClass(generateAdapter = true)
data class BudgetLineItemDto(
    val id: String,
    @Json(name = "fiscal_year") val fiscalYear: String,
    val title: String,
    val category: String,
    @Json(name = "department_id") val departmentId: String? = null,
    @Json(name = "department_name") val departmentName: String? = null,
    @Json(name = "allocated_amount") val allocatedAmount: Double,
    @Json(name = "spent_amount") val spentAmount: Double,
    @Json(name = "remaining_amount") val remainingAmount: Double,
    val status: String,
    val notes: String? = null,
    @Json(name = "created_at") val createdAt: String
)

@JsonClass(generateAdapter = true)
data class BudgetLineItemCreateRequest(
    @Json(name = "fiscal_year") val fiscalYear: String,
    val title: String,
    val category: String,
    @Json(name = "department_id") val departmentId: String? = null,
    @Json(name = "allocated_amount") val allocatedAmount: Double,
    val notes: String? = null
)

@JsonClass(generateAdapter = true)
data class BudgetExpenseDto(
    val id: String,
    @Json(name = "line_item_id") val lineItemId: String,
    val description: String,
    val amount: Double,
    @Json(name = "expense_date") val expenseDate: String,
    @Json(name = "recorded_by_name") val recordedByName: String? = null,
    @Json(name = "created_at") val createdAt: String
)

@JsonClass(generateAdapter = true)
data class BudgetExpenseCreateRequest(
    val description: String,
    val amount: Double,
    @Json(name = "expense_date") val expenseDate: String
)

@JsonClass(generateAdapter = true)
data class GrantDto(
    val id: String,
    val title: String,
    @Json(name = "funding_agency") val fundingAgency: String,
    @Json(name = "department_id") val departmentId: String? = null,
    @Json(name = "department_name") val departmentName: String? = null,
    @Json(name = "principal_investigator") val principalInvestigator: String? = null,
    @Json(name = "sanctioned_amount") val sanctionedAmount: Double,
    @Json(name = "disbursed_amount") val disbursedAmount: Double,
    val status: String,
    @Json(name = "start_date") val startDate: String? = null,
    @Json(name = "end_date") val endDate: String? = null,
    val notes: String? = null,
    @Json(name = "created_at") val createdAt: String
)

@JsonClass(generateAdapter = true)
data class GrantCreateRequest(
    val title: String,
    @Json(name = "funding_agency") val fundingAgency: String,
    @Json(name = "department_id") val departmentId: String? = null,
    @Json(name = "principal_investigator") val principalInvestigator: String? = null,
    @Json(name = "sanctioned_amount") val sanctionedAmount: Double,
    @Json(name = "start_date") val startDate: String? = null,
    @Json(name = "end_date") val endDate: String? = null,
    val notes: String? = null
)

@JsonClass(generateAdapter = true)
data class MootCourtMemorialDto(
    val id: String,
    @Json(name = "student_id") val studentId: String,
    val title: String,
    @Json(name = "case_name") val caseName: String?,
    val content: String,
    val status: String,
    @Json(name = "created_at") val createdAt: String,
    @Json(name = "updated_at") val updatedAt: String
)

@JsonClass(generateAdapter = true)
data class CreateMemorialRequest(
    val title: String,
    @Json(name = "case_name") val caseName: String?,
    val content: String,
    val status: String? = null
)

@JsonClass(generateAdapter = true)
data class UpdateMemorialRequest(
    val title: String? = null,
    @Json(name = "case_name") val caseName: String? = null,
    val content: String? = null,
    val status: String? = null
)

@JsonClass(generateAdapter = true)
data class SavedCitationDto(
    val id: String,
    @Json(name = "student_id") val studentId: String,
    @Json(name = "case_name") val caseName: String,
    @Json(name = "citation_text") val citationText: String,
    val note: String?,
    @Json(name = "created_at") val createdAt: String,
    @Json(name = "updated_at") val updatedAt: String
)

@JsonClass(generateAdapter = true)
data class CreateCitationRequest(
    @Json(name = "case_name") val caseName: String,
    @Json(name = "citation_text") val citationText: String,
    val note: String?
)

@JsonClass(generateAdapter = true)
data class ChatMessageRequest(
    val message: String,
    @Json(name = "session_id") val sessionId: String? = null
)

@JsonClass(generateAdapter = true)
data class ChatMessageResponse(
    val response: String,
    @Json(name = "session_id") val sessionId: String,
    @Json(name = "message_id") val messageId: String
)

@JsonClass(generateAdapter = true)
data class ChatSessionSummaryDto(
    val id: String,
    val title: String?,
    @Json(name = "created_at") val createdAt: String
)

@JsonClass(generateAdapter = true)
data class ChatMessageDto(
    val id: String,
    @Json(name = "session_id") val sessionId: String,
    val role: String,
    val content: String,
    @Json(name = "created_at") val createdAt: String
)

@JsonClass(generateAdapter = true)
data class ChangePasswordRequest(
    @Json(name = "current_password") val currentPassword: String,
    @Json(name = "new_password") val newPassword: String
)

@JsonClass(generateAdapter = true)
data class ChildSummaryDto(
    @Json(name = "student_id") val id: String,
    @Json(name = "full_name") val fullName: String,
    @Json(name = "roll_no") val rollNo: String?,
    val semester: Int? = null,
    @Json(name = "course_name") val courseName: String? = null,
    @Json(name = "profile_photo_url") val profilePhotoUrl: String? = null
)

@JsonClass(generateAdapter = true)
data class InnovationProjectDto(
    val id: String,
    val title: String,
    val description: String,
    val category: String,
    val mentor: String,
    val team: List<String>,
    val badges: List<String>,
    val likes: Int,
    @Json(name = "liked_by_me") val likedByMe: Boolean,
    val comments: Int
)

@JsonClass(generateAdapter = true)
data class InnovationProjectCreateRequest(
    val title: String,
    val description: String,
    val category: String,
    val mentor: String,
    val team: List<String> = emptyList()
)

@JsonClass(generateAdapter = true)
data class InnovationCommentRequest(
    val text: String
)

@JsonClass(generateAdapter = true)
data class ResearchPaperDto(
    val id: Int,
    val title: String,
    val abstract: String,
    val category: String,
    val status: String,
    val guide: String,
    val team: List<String>,
    val submissionDate: String,
    val awards: List<String> = emptyList(),
    val fileSize: String,
    val featured: Boolean = false,
    val fileUrl: String? = null
)

@JsonClass(generateAdapter = true)
data class ResearchPaperSubmitRequest(
    val title: String,
    val abstract: String,
    val category: String,
    val guide: String,
    val team: List<String>,
    val fileUrl: String? = null,
    val fileSize: String? = null
)

@JsonClass(generateAdapter = true)
data class PaperUploadResponseDto(
    val fileUrl: String,
    val fileSize: String
)

@JsonClass(generateAdapter = true)
data class LegalEventSpeakerDto(
    val name: String = "TBA",
    val designation: String = "",
    val type: String = "",
    val bio: String = "",
    val initials: String = "NA"
)

@JsonClass(generateAdapter = true)
data class LegalEventDto(
    val id: String,
    val title: String,
    val category: String,
    val speaker: LegalEventSpeakerDto = LegalEventSpeakerDto(),
    val date: String,
    val time: String,
    val duration: String = "",
    val status: String,
    val mode: String = "",
    val platform: String = "",
    val meetingLink: String? = null,
    val totalSeats: Int = 0,
    val availableSeats: Int = 0,
    val registrationDeadline: String? = null,
    val description: String = "",
    val agenda: List<String> = emptyList(),
    @Json(name = "activityPoints") val activityPoints: Int = 0,
    val certificateAvailable: Boolean = true
)

@JsonClass(generateAdapter = true)
data class LegalEventRegistrationDto(
    val eventId: String,
    val studentEmail: String,
    val studentName: String? = null,
    val status: String = "Confirmed",
    val attended: Boolean = false
)

@JsonClass(generateAdapter = true)
data class LegalEventQuestionDto(
    val id: String,
    val eventId: String,
    val eventTitle: String,
    val studentEmail: String,
    val studentName: String? = null,
    val question: String,
    val topic: String,
    val status: String = "Pending",
    val submittedAt: String = ""
)

@JsonClass(generateAdapter = true)
data class ClubDto(
    val id: String,
    val name: String,
    val description: String?,
    val category: String?,
    @Json(name = "member_count") val membersCount: Int,
    @Json(name = "current_user_role") val userRole: String?,
    @Json(name = "president_name") val presidentName: String? = null
)

@JsonClass(generateAdapter = true)
data class ClubAnnouncementDto(
    val id: String,
    @Json(name = "club_id") val clubId: String,
    @Json(name = "club_name") val clubName: String,
    val title: String,
    @Json(name = "is_urgent") val isUrgent: Boolean,
    @Json(name = "posted_by_name") val postedByName: String,
    @Json(name = "created_at") val createdAt: String
)

@JsonClass(generateAdapter = true)
data class CouncilDataDto(
    val representatives: List<CouncilRepDto>,
    val initiatives: List<InitiativeDto>,
    val feedback: List<FeedbackDto>,
    val metrics: CouncilMetricsDto
)

@JsonClass(generateAdapter = true)
data class CouncilMetricsDto(
    val proposals: Int,
    val resolved: Int,
    @Json(name = "fund_utilization_percent") val fundUtilizationPercent: Int
)

@JsonClass(generateAdapter = true)
data class CouncilProposalRequest(
    val title: String,
    val description: String
)

@JsonClass(generateAdapter = true)
data class CouncilFeedbackRequest(
    val title: String
)

@JsonClass(generateAdapter = true)
data class ParentInquiryRequest(
    val name: String,
    val email: String,
    val subject: String,
    val message: String
)

@JsonClass(generateAdapter = true)
data class CollegeInfoDto(
    val contacts: List<CollegeContactDto>,
    @Json(name = "campus_name") val campusName: String,
    @Json(name = "campus_address") val campusAddress: String
)

@JsonClass(generateAdapter = true)
data class CollegeContactDto(
    val role: String,
    val phone: String
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
    val title: String = "",
    val category: String = "",
    val organizer: String = "",
    @Json(name = "organizer_id") val organizerId: String? = null,
    val date: String = "",
    val time: String = "",
    val duration: String = "",
    val platform: String = "",
    val meetingLink: String = "",
    val status: String = "",
    val participants: Int = 0,
    val attended: Boolean = false,
    val agenda: List<String> = emptyList(),
    val notes: String? = null,
    val recordingAvailable: Boolean = false,
    val recordingUrl: String? = null,
    val description: String = "",
    val room: String? = null
)

@JsonClass(generateAdapter = true)
data class CreateMeetingRequest(
    val title: String,
    val category: String,
    val date: String,
    val time: String,
    val duration: String,
    val platform: String,
    val meetingLink: String,
    val description: String = "",
    val agenda: List<String> = emptyList()
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
    @Json(name = "company_name") val companyName: String,
    val role: String,
    val `package`: String?,
    @Json(name = "drive_date") val driveDate: String?,
    val status: String,
    val description: String?
)

@JsonClass(generateAdapter = true)
data class InternshipApplyRequestDto(
    @Json(name = "drive_id") val driveId: String
)

@JsonClass(generateAdapter = true)
data class InternshipApplicationResponseDto(
    val id: String,
    @Json(name = "drive_id") val driveId: String,
    @Json(name = "student_id") val studentId: String,
    val status: String
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
    val type: String,
    @Json(name = "file_url") val fileUrl: String? = null
)

@JsonClass(generateAdapter = true)
data class CertificationCreateDto(
    val title: String,
    val issuer: String,
    val date: String,
    val category: String,
    val type: String = "training",
    @Json(name = "file_url") val fileUrl: String? = null
)

@JsonClass(generateAdapter = true)
data class ActivityPointDto(
    val id: String,
    @Json(name = "student_id") val studentId: String? = null,
    @Json(name = "student_name") val studentName: String? = null,
    @Json(name = "roll_no") val rollNo: String? = null,
    val title: String,
    val category: String,
    val date: String,
    @Json(name = "claimed_points") val claimedPoints: Double,
    @Json(name = "approved_points") val approvedPoints: Double?,
    val status: String,
    val description: String,
    @Json(name = "supporting_document") val supportingDocument: String?,
    @Json(name = "reviewed_by") val reviewedBy: String?,
    @Json(name = "reviewed_at") val reviewedAt: String?,
    @Json(name = "faculty_remarks") val facultyRemarks: String?
)

@JsonClass(generateAdapter = true)
data class ActivityPointClaimRequest(
    val title: String,
    val category: String,
    val description: String,
    val date: String,
    @Json(name = "claimed_points") val claimedPoints: Double,
    @Json(name = "supporting_document") val supportingDocument: String? = null
)

@JsonClass(generateAdapter = true)
data class ActivityPointReviewRequest(
    val status: String,
    @Json(name = "approved_points") val approvedPoints: Double,
    val remarks: String? = null
)

@JsonClass(generateAdapter = true)
data class ActivityPointCategoryDto(
    val id: String,
    val code: String,
    val name: String,
    @Json(name = "max_points") val maxPoints: Double,
    val description: String? = null
)

@JsonClass(generateAdapter = true)
data class ActivityPointCategoryRequest(
    val code: String,
    val name: String,
    @Json(name = "max_points") val maxPoints: Double,
    val description: String? = null
)

@JsonClass(generateAdapter = true)
data class ServiceOpportunityDto(
    val id: Int,
    val title: String,
    val organizer: String,
    val date: String,
    val location: String,
    val spots: Int,
    val hours: String,
    val tags: List<String>
)

@JsonClass(generateAdapter = true)
data class ServiceLogDto(
    val id: String,
    val title: String,
    val organization: String,
    val category: String,
    val date: String,
    val hours: Double,
    val status: String,
    @Json(name = "is_verified") val isVerified: Boolean,
    @Json(name = "certificate_url") val certificateUrl: String?,
    val description: String,
    @Json(name = "proof_document") val proofDocument: String?
)

@JsonClass(generateAdapter = true)
data class LogServiceHoursRequest(
    val title: String,
    val organization: String,
    val category: String,
    val date: String,
    val hours: Double,
    val description: String,
    @Json(name = "proof_document") val proofDocument: String? = null
)

@JsonClass(generateAdapter = true)
data class FacultyAssignmentDto(
    val id: String,
    val title: String,
    val type: String? = null,
    val subject: String? = null,
    val unit: String? = null,
    val topic: String? = null,
    val description: String? = null,
    val instructions: String? = null,
    @Json(name = "total_marks") val totalMarks: Int? = null,
    val deadline: String? = null,
    val status: String,
    @Json(name = "faculty_id") val facultyId: String,
    @Json(name = "faculty_name") val facultyName: String? = null,
    val semester: String? = null,
    val section: String? = null
)

@JsonClass(generateAdapter = true)
data class CreateAssignmentRequest(
    val title: String,
    val type: String,
    val subject: String,
    val unit: String,
    val topic: String,
    val description: String,
    val instructions: String,
    @Json(name = "total_marks") val totalMarks: Int,
    val deadline: String,
    val status: String,
    val semester: String,
    val section: String
)

@JsonClass(generateAdapter = true)
data class FacultyAssignmentSubmissionDto(
    val id: String,
    @Json(name = "assignment_id") val assignmentId: String,
    @Json(name = "student_id") val studentId: String,
    @Json(name = "submitted_file_url") val submittedFileUrl: String? = null,
    @Json(name = "submitted_text") val submittedText: String? = null,
    @Json(name = "marks_obtained") val marksObtained: Double? = null,
    val grade: String? = null,
    val feedback: String? = null,
    val remarks: String? = null,
    val status: String,
    @Json(name = "submitted_at") val submittedAt: String,
    @Json(name = "student_name") val studentName: String? = null,
    @Json(name = "register_number") val registerNumber: String? = null
)

@JsonClass(generateAdapter = true)
data class GradeSubmissionRequest(
    @Json(name = "marks_obtained") val marksObtained: Double,
    val grade: String,
    val feedback: String,
    val remarks: String,
    val status: String
)

@JsonClass(generateAdapter = true)
data class FacultyAttendanceSectionDto(
    @Json(name = "section_id") val sectionId: String,
    @Json(name = "section_name") val sectionName: String,
    @Json(name = "subject_id") val subjectId: String,
    @Json(name = "subject_code") val subjectCode: String,
    @Json(name = "subject_name") val subjectName: String,
    @Json(name = "course_name") val courseName: String,
    val semester: String
)

@JsonClass(generateAdapter = true)
data class FacultyAttendanceStudentDto(
    val regNo: String,
    val name: String,
    val overallAttendance: Int,
    val presentCount: Int,
    val absentCount: Int,
    val odCount: Int,
    val mlCount: Int
)

@JsonClass(generateAdapter = true)
data class ClassroomActivityDto(
    val id: String,
    @Json(name = "section_id") val sectionId: String,
    @Json(name = "activity_type") val activityType: String,
    val topic: String,
    @Json(name = "duration_minutes") val durationMinutes: Int,
    val remarks: String? = null,
    @Json(name = "created_at") val createdAt: String
)

@JsonClass(generateAdapter = true)
data class CreateClassroomActivityRequest(
    @Json(name = "section_id") val sectionId: String,
    @Json(name = "activity_type") val activityType: String,
    val topic: String,
    @Json(name = "duration_minutes") val durationMinutes: Int,
    val remarks: String? = null
)

@JsonClass(generateAdapter = true)
data class StudentInteractionDto(
    val id: String,
    @Json(name = "section_id") val sectionId: String,
    val type: String,
    @Json(name = "question_text") val questionText: String,
    val options: List<String>? = null,
    @Json(name = "responses_count") val responsesCount: Int,
    @Json(name = "is_active") val isActive: Boolean,
    @Json(name = "created_at") val createdAt: String
)

@JsonClass(generateAdapter = true)
data class CreateInteractionRequest(
    @Json(name = "section_id") val sectionId: String,
    val type: String,
    @Json(name = "question_text") val questionText: String,
    val options: List<String>? = null
)

@JsonClass(generateAdapter = true)
data class SessionSummaryDto(
    val id: String,
    @Json(name = "section_id") val sectionId: String,
    @Json(name = "subject_code") val subjectCode: String,
    @Json(name = "topic_covered") val topicCovered: String,
    @Json(name = "subtopic_covered") val subtopicCovered: String? = null,
    @Json(name = "teaching_method") val teachingMethod: String,
    @Json(name = "resources_used") val resourcesUsed: List<String>? = null,
    val remarks: String? = null,
    val date: String
)

@JsonClass(generateAdapter = true)
data class CreateSessionSummaryRequest(
    @Json(name = "section_id") val sectionId: String,
    @Json(name = "subject_code") val subjectCode: String,
    @Json(name = "topic_covered") val topicCovered: String,
    @Json(name = "subtopic_covered") val subtopicCovered: String? = null,
    @Json(name = "teaching_method") val teachingMethod: String,
    val remarks: String? = null
)

@JsonClass(generateAdapter = true)
data class InternalMarkStudentDto(
    @Json(name = "student_id") val studentId: String,
    @Json(name = "student_name") val studentName: String,
    @Json(name = "registration_number") val registrationNumber: String,
    @Json(name = "internal_exam_mark") val internalExamMark: Double,
    @Json(name = "assignment_mark") val assignmentMark: Double,
    @Json(name = "presentation_mark") val presentationMark: Double,
    @Json(name = "viva_voice_mark") val vivaVoiceMark: Double,
    @Json(name = "attendance_mark") val attendanceMark: Double,
    @Json(name = "total_mark") val totalMark: Double,
    val status: String,
    @Json(name = "hod_message") val hodMessage: String? = null,
    @Json(name = "faculty_reply") val facultyReply: String? = null,
    @Json(name = "is_message_visible_to_student") val isMessageVisibleToStudent: Boolean = false
)

@JsonClass(generateAdapter = true)
data class InternalMarkEntryRequest(
    @Json(name = "student_id") val studentId: String,
    @Json(name = "internal_exam_mark") val internalExamMark: Double,
    @Json(name = "assignment_mark") val assignmentMark: Double,
    @Json(name = "presentation_mark") val presentationMark: Double,
    @Json(name = "viva_voice_mark") val vivaVoiceMark: Double,
    @Json(name = "attendance_mark") val attendanceMark: Double,
    @Json(name = "total_mark") val totalMark: Double
)

@JsonClass(generateAdapter = true)
data class SaveInternalMarksRequest(
    @Json(name = "section_id") val sectionId: String,
    @Json(name = "subject_id") val subjectId: String,
    @Json(name = "academic_year") val academicYear: String,
    val semester: String? = null,
    val marks: List<InternalMarkEntryRequest>
)

@JsonClass(generateAdapter = true)
data class SubmitMarksRequest(
    @Json(name = "section_id") val sectionId: String,
    @Json(name = "subject_id") val subjectId: String,
    @Json(name = "academic_year") val academicYear: String
)

@JsonClass(generateAdapter = true)
data class BulkAttendanceMarkRequest(
    val date: String,
    @Json(name = "section_id") val sectionId: String,
    @Json(name = "subject_id") val subjectId: String,
    val hour: Int,
    @Json(name = "student_statuses") val studentStatuses: Map<String, String>
)

@JsonClass(generateAdapter = true)
data class FacultyStudentDto(
    @Json(name = "student_id") val id: String,
    @Json(name = "full_name") val name: String,
    @Json(name = "roll_no") val rollNo: String,
    val email: String,
    val phone: String? = null,
    val semester: Int,
    @Json(name = "batch_year") val batch: Int,
    @Json(name = "department_name") val departmentName: String? = null,
    @Json(name = "section_name") val sectionName: String? = null
)

@JsonClass(generateAdapter = true)
data class FacultyMaterialDto(
    val id: String,
    val title: String,
    val description: String? = null,
    val subject: String,
    val unit: String? = null,
    val topic: String? = null,
    val category: String? = null,
    val keywords: List<String> = emptyList(),
    @Json(name = "file_url") val fileUrl: String? = null,
    @Json(name = "file_format") val fileFormat: String? = null,
    val status: String,
    val version: Int = 1,
    @Json(name = "uploaded_date") val uploadedDate: String? = null,
    @Json(name = "last_updated_date") val lastUpdatedDate: String? = null,
    @Json(name = "faculty_id") val facultyId: String? = null,
    @Json(name = "faculty_name") val facultyName: String? = null,
    val semester: String? = null
)

@JsonClass(generateAdapter = true)
data class FacultyRecordingDto(
    val id: String,
    val title: String,
    val course: String? = null,
    val semester: String? = null,
    val section: String? = null,
    val unit: String? = null,
    val topic: String? = null,
    val description: String? = null,
    @Json(name = "recording_date") val recordingDate: String? = null,
    @Json(name = "upload_date") val uploadDate: String? = null,
    @Json(name = "file_name") val fileName: String? = null,
    @Json(name = "drive_link") val driveLink: String? = null,
    @Json(name = "file_size") val fileSize: String? = null,
    val status: String? = null,
    @Json(name = "academic_year") val academicYear: String? = null,
    val duration: Double? = null,
    @Json(name = "faculty_id") val facultyId: String? = null,
    @Json(name = "faculty_name") val facultyName: String? = null
) {
    val durationDisplay: String
        get() = duration?.let { "${it.toInt()} min" } ?: "N/A"
}

@JsonClass(generateAdapter = true)
data class CreateRecordingRequest(
    val title: String,
    val course: String,
    val semester: String,
    val section: String,
    val unit: String? = null,
    val topic: String? = null,
    val description: String? = null,
    @Json(name = "recording_date") val recordingDate: String,
    @Json(name = "drive_link") val driveLink: String,
    @Json(name = "academic_year") val academicYear: String? = null
)

@JsonClass(generateAdapter = true)
data class ApprovalRequest(val status: String, val remarks: String)

@JsonClass(generateAdapter = true)
data class GrievanceResolveRequest(val status: String, val comments: String? = null)

@JsonClass(generateAdapter = true)
data class PrincipalFacultyOverviewDto(
    val id: String,
    @Json(name = "full_name") val fullName: String,
    val email: String,
    val phone: String? = null,
    val role: String,
    val designation: String,
    @Json(name = "department_id") val departmentId: String? = null,
    @Json(name = "department_name") val departmentName: String? = null,
    @Json(name = "employee_code") val employeeCode: String? = null,
    @Json(name = "faculty_type") val facultyType: String? = null,
    @Json(name = "employment_status") val employmentStatus: String? = null,
    @Json(name = "date_of_joining") val dateOfJoining: String? = null,
    @Json(name = "is_on_leave_today") val isOnLeaveToday: Boolean = false
)

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
    val metrics: List<AdminMetricDto> = emptyList(),
    @Json(name = "total_users") val totalUsers: Int? = null,
    @Json(name = "total_students") val totalStudents: Int? = null,
    @Json(name = "total_staff") val totalStaff: Int? = null,
    @Json(name = "total_departments") val totalDepartments: Int? = null,
    @Json(name = "daily_fee_collection") val dailyFeeCollection: Double? = null
)

@JsonClass(generateAdapter = true)
data class AdminMetricDto(
    val id: String = "",
    val label: String = "",
    val value: String = ""
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
data class TimetableSlotInputDto(
    @Json(name = "subject_id") val subjectId: String,
    @Json(name = "faculty_id") val facultyId: String,
    val room: String,
    val weekday: String,
    @Json(name = "start_time") val startTime: String,
    @Json(name = "end_time") val endTime: String
)

@JsonClass(generateAdapter = true)
data class TimetableSubmitRequestDto(
    @Json(name = "section_id") val sectionId: String,
    val slots: List<TimetableSlotInputDto>
)

@JsonClass(generateAdapter = true)
data class HodPendingMaterialDto(
    val id: String,
    val title: String,
    val description: String = "",
    val subject: String = "",
    val category: String = "",
    @Json(name = "file_url") val fileUrl: String? = null,
    @Json(name = "file_format") val fileFormat: String? = null,
    val status: String = "",
    @Json(name = "uploaded_date") val uploadedDate: String = "",
    @Json(name = "faculty_id") val facultyId: String = "",
    @Json(name = "faculty_name") val facultyName: String = ""
)

@JsonClass(generateAdapter = true)
data class MaterialReviewRequest(
    val status: String,
    val remarks: String
)

@JsonClass(generateAdapter = true)
data class ClassAdvisorRowDto(
    @Json(name = "academic_year_id") val academicYearId: String,
    val batch: String,
    @Json(name = "section_name") val sectionName: String,
    @Json(name = "faculty_id") val facultyId: String? = null,
    @Json(name = "faculty_name") val facultyName: String? = null
)

@JsonClass(generateAdapter = true)
data class ClassAdvisorFacultyDto(
    val id: String,
    val name: String,
    val email: String
)

@JsonClass(generateAdapter = true)
data class ClassAdvisorSetupDto(
    val classes: List<ClassAdvisorRowDto> = emptyList(),
    val faculty: List<ClassAdvisorFacultyDto> = emptyList()
)

@JsonClass(generateAdapter = true)
data class AdvisorAssignmentRequestDto(
    @Json(name = "academic_year_id") val academicYearId: String,
    val batch: String,
    @Json(name = "section_name") val sectionName: String,
    @Json(name = "faculty_id") val facultyId: String
)

@JsonClass(generateAdapter = true)
data class AttendanceCorrectionDto(
    val id: String,
    val studentRegNo: String = "",
    val studentName: String = "",
    val subject: String = "",
    val date: String = "",
    val previousStatus: String = "",
    val updatedStatus: String = "",
    val reason: String = "",
    val requestedAt: String = "",
    val status: String = "",
    val remarks: String? = null
)

@JsonClass(generateAdapter = true)
data class RejectCorrectionRequestDto(
    val remarks: String
)

@JsonClass(generateAdapter = true)
data class FacultyProfileUpdateRequestDto(
    val id: String,
    @Json(name = "user_id") val userId: String,
    @Json(name = "full_name") val fullName: String? = null,
    @Json(name = "department_name") val departmentName: String? = null,
    val status: String,
    @Json(name = "official_email") val officialEmail: String? = null,
    @Json(name = "official_phone") val officialPhone: String? = null,
    @Json(name = "requested_designation") val requestedDesignation: String? = null,
    @Json(name = "requested_department_name") val requestedDepartmentName: String? = null,
    val comments: String? = null
)

@JsonClass(generateAdapter = true)
data class ProfileUpdateReviewRequest(
    val status: String,
    val comments: String? = null
)

@JsonClass(generateAdapter = true)
data class HODFacultyResponseDto(
    val id: String,
    @Json(name = "full_name") val fullName: String,
    val email: String,
    val phone: String? = null,
    @Json(name = "department_name") val departmentName: String? = null,
    val designation: String? = null,
    @Json(name = "subjects_count") val subjectsCount: Int = 0
)

@JsonClass(generateAdapter = true)
data class HODManagementStudentDto(
    val id: String,
    @Json(name = "roll_no") val rollNo: String,
    @Json(name = "full_name") val fullName: String,
    val email: String,
    val phone: String? = null,
    val course: String? = null,
    val semester: Int,
    val status: String,
    val cgpa: Double? = null,
    @Json(name = "attendance_rate") val attendanceRate: Int,
    @Json(name = "is_on_leave") val isOnLeave: Boolean = false,
    @Json(name = "verification_status") val verificationStatus: String? = null,
    @Json(name = "hod_remarks") val hodRemarks: String? = null
)

@JsonClass(generateAdapter = true)
data class HODManagementMetricsDto(
    @Json(name = "total_students") val totalStudents: Int = 0,
    @Json(name = "active_students") val activeStudents: Int = 0,
    @Json(name = "students_on_leave") val studentsOnLeave: Int = 0,
    @Json(name = "average_attendance") val averageAttendance: Int = 0
)

@JsonClass(generateAdapter = true)
data class HODManagementStudentsDto(
    val students: List<HODManagementStudentDto> = emptyList(),
    val metrics: HODManagementMetricsDto = HODManagementMetricsDto()
)

@JsonClass(generateAdapter = true)
data class StudentVerifyRequest(
    val action: String,
    val remarks: String? = null
)

@JsonClass(generateAdapter = true)
data class HODMetricItemDto(
    val id: String,
    val label: String,
    val value: String
)

@JsonClass(generateAdapter = true)
data class HODDashboardMetricsDto(
    val metrics: List<HODMetricItemDto> = emptyList()
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
data class FacultyDashboardMetricsDto(
    val metrics: List<DashboardMetricItemDto> = emptyList()
)

@JsonClass(generateAdapter = true)
data class DashboardMetricItemDto(
    val id: String,
    val label: String,
    val value: String
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
    @Json(name = "user_id") val userId: String,
    @Json(name = "full_name") val fullName: String,
    val email: String,
    val phone: String? = null,
    @Json(name = "department_id") val departmentId: String? = null,
    @Json(name = "department_name") val departmentName: String? = null,
    val designation: String,
    val specialization: String? = null,
    @Json(name = "faculty_id") val facultyId: String? = null,
    @Json(name = "employee_code") val employeeCode: String? = null,
    val gender: String? = null,
    @Json(name = "date_of_birth") val dateOfBirth: String? = null,
    @Json(name = "blood_group") val bloodGroup: String? = null,
    @Json(name = "marital_status") val maritalStatus: String? = null,
    val nationality: String? = null,
    val community: String? = null,
    @Json(name = "alternate_phone") val alternatePhone: String? = null,
    @Json(name = "personal_email") val personalEmail: String? = null,
    @Json(name = "current_address") val currentAddress: String? = null,
    @Json(name = "permanent_address") val permanentAddress: String? = null,
    val city: String? = null,
    val state: String? = null,
    val pincode: String? = null,
    @Json(name = "profile_photo_url") val profilePhotoUrl: String? = null,
    @Json(name = "faculty_type") val facultyType: String? = null,
    @Json(name = "employment_category") val employmentCategory: String? = null,
    @Json(name = "date_of_joining") val dateOfJoining: String? = null,
    @Json(name = "employment_status") val employmentStatus: String? = null,
    @Json(name = "reporting_hod_id") val reportingHodId: String? = null,
    @Json(name = "reporting_hod_name") val reportingHodName: String? = null,
    @Json(name = "reporting_principal_id") val reportingPrincipalId: String? = null,
    @Json(name = "reporting_principal_name") val reportingPrincipalName: String? = null,
    @Json(name = "confirmation_date") val confirmationDate: String? = null,
    @Json(name = "educational_qualifications") val qualifications: List<QualificationDto>? = null,
    @Json(name = "experience_details") val experience: List<ExperienceDto>? = null,
    @Json(name = "academic_responsibilities") val academicResponsibilities: List<String>? = null,
    @Json(name = "certifications_achievements") val certificationsAchievements: List<String>? = null,
    @Json(name = "approval_status") val approvalStatus: String? = null
)

@JsonClass(generateAdapter = true)
data class FacultyProfileUpdateRequest(
    @Json(name = "marital_status") val maritalStatus: String? = null,
    val community: String? = null,
    @Json(name = "alternate_phone") val alternatePhone: String? = null,
    @Json(name = "personal_email") val personalEmail: String? = null,
    @Json(name = "current_address") val currentAddress: String? = null,
    @Json(name = "permanent_address") val permanentAddress: String? = null,
    val city: String? = null,
    val state: String? = null,
    val pincode: String? = null,
    @Json(name = "profile_photo_url") val profilePhotoUrl: String? = null,
    val gender: String? = null,
    @Json(name = "date_of_birth") val dateOfBirth: String? = null,
    @Json(name = "blood_group") val bloodGroup: String? = null,
    val nationality: String? = null,
    @Json(name = "official_phone") val officialPhone: String? = null
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
    @Json(name = "research_type") val researchType: String?,
    @Json(name = "publication_date") val publicationDate: String?,
    @Json(name = "grant_amount") val grantAmount: Double?,
    val publisher: String?,
    @Json(name = "isbn_issn") val isbnIssn: String? = null,
    @Json(name = "proof_file_url") val proofFileUrl: String? = null,
    val comments: String? = null,
    val status: String?
)

@JsonClass(generateAdapter = true)
data class ResearchEntryRequest(
    val title: String,
    val publication: String? = null,
    @Json(name = "grant_amount") val grantAmount: Double? = null,
    val publisher: String? = null,
    @Json(name = "publication_date") val publicationDate: String? = null,
    @Json(name = "isbn_issn") val isbnIssn: String? = null,
    @Json(name = "research_type") val researchType: String? = null,
    @Json(name = "proof_file_url") val proofFileUrl: String? = null
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
    val semester: Int = 0,
    val batch: String? = null,
    @Json(name = "batch_year") val batchYear: Int? = null,
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
    val title: String = "",
    val issuer: String = "",
    val category: String = "",
    val date: String = "",
    @Json(name = "is_verified") val isVerified: Boolean = false
)

@JsonClass(generateAdapter = true)
data class ParentChildMarkDto(
    val subject: String = "",
    @Json(name = "subject_name") val subjectName: String = "",
    @Json(name = "academic_year") val academicYear: String = "",
    @Json(name = "internal_1") val internal1: String = "",
    @Json(name = "internal_2") val internal2: String = "",
    val model: String = "",
    val assignments: String = "",
    val attendance: String = "",
    val total: String = "",
    @Json(name = "internal_exam_mark") val internalExamMark: Double = 0.0,
    @Json(name = "assignment_mark") val assignmentMark: Double = 0.0,
    @Json(name = "presentation_mark") val presentationMark: Double = 0.0,
    @Json(name = "viva_voice_mark") val vivaVoiceMark: Double = 0.0,
    @Json(name = "attendance_mark") val attendanceMark: Double = 0.0,
    @Json(name = "total_mark") val totalMark: Double = 0.0
)

@JsonClass(generateAdapter = true)
data class ParentChildAttendanceDto(
    val date: String = "",
    val status: String = ""
)

@JsonClass(generateAdapter = true)
data class ParentChildFeeDto(
    @Json(name = "total_fees") val totalFees: Double,
    @Json(name = "scholarship_deduction") val scholarshipDeduction: Double,
    @Json(name = "other_deductions") val otherDeductions: Double,
    @Json(name = "net_fees") val netFees: Double,
    @Json(name = "amount_paid") val amountPaid: Double,
    @Json(name = "pending_balance") val pendingBalance: Double,
    @Json(name = "due_date") val dueDate: String? = null,
    val records: List<ParentChildFeeRecordDto> = emptyList()
)

@JsonClass(generateAdapter = true)
data class ParentChildFeeRecordDto(
    @Json(name = "record_id") val id: String,
    @Json(name = "fee_type") val title: String,
    val amount: Double,
    @Json(name = "due_date") val date: String? = null,
    val status: String = "",
    @Json(name = "remaining_amount") val remainingAmount: Double? = null
)

@JsonClass(generateAdapter = true)
data class ParentChildTimetableDayDto(
    @Json(name = "weekday") val dayOfWeek: String = "",
    @Json(name = "start_time") val startTime: String = "",
    @Json(name = "end_time") val endTime: String = "",
    @Json(name = "subject_name") val subjectName: String = "",
    @Json(name = "subject_code") val subjectCode: String = "",
    @Json(name = "room") val roomNo: String = "",
    @Json(name = "faculty_name") val facultyName: String? = null
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
data class StudentInternalMarkDto(
    @Json(name = "subject_id") val subjectId: String,
    @Json(name = "subject_name") val subjectName: String,
    @Json(name = "academic_year") val academicYear: String,
    val semester: String,
    @Json(name = "is_approved") val isApproved: Boolean,
    @Json(name = "internal_exam_mark") val internalExamMark: Double,
    @Json(name = "assignment_mark") val assignmentMark: Double,
    @Json(name = "presentation_mark") val presentationMark: Double,
    @Json(name = "viva_voice_mark") val vivaVoiceMark: Double,
    @Json(name = "attendance_mark") val attendanceMark: Double,
    @Json(name = "total_mark") val totalMark: Double,
    @Json(name = "hod_message") val hodMessage: String? = null,
    @Json(name = "faculty_reply") val facultyReply: String? = null
)

@JsonClass(generateAdapter = true)
data class TimetableSlotDto(
    val id: String,
    @Json(name = "subject_id") val subjectId: String = "",
    @Json(name = "faculty_id") val facultyId: String = "",
    @Json(name = "weekday") val dayOfWeek: String,
    @Json(name = "start_time") val startTime: String,
    @Json(name = "end_time") val endTime: String,
    // Nullable: the backend returns null when a course/faculty reference cannot be
    // resolved, rather than substituting placeholder data.
    @Json(name = "subject_name") val subjectName: String? = null,
    @Json(name = "subject_code") val subjectCode: String? = null,
    @Json(name = "faculty_name") val facultyName: String? = null,
    @Json(name = "room") val roomNo: String? = null,
    val status: String? = null,
    val semester: Int? = null
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
    val status: String,
    @Json(name = "paid_amount") val paidAmount: Double? = null,
    @Json(name = "remaining_amount") val remainingAmount: Double? = null,
    @Json(name = "total_amount") val totalAmount: Double? = null,
    val semester: Int? = null
)

@JsonClass(generateAdapter = true)
data class CreateOrderRequestDto(val amount: Double)

@JsonClass(generateAdapter = true)
data class CreateOrderResponseDto(
    @Json(name = "order_id") val orderId: String,
    val amount: Double,
    val currency: String,
    @Json(name = "key_id") val keyId: String
)

@JsonClass(generateAdapter = true)
data class VerifyPaymentRequestDto(
    @Json(name = "razorpay_order_id") val razorpayOrderId: String,
    @Json(name = "razorpay_payment_id") val razorpayPaymentId: String,
    @Json(name = "razorpay_signature") val razorpaySignature: String
)

@JsonClass(generateAdapter = true)
data class VerifyPaymentResponseDto(
    val status: String,
    @Json(name = "record_id") val recordId: String,
    @Json(name = "fee_status") val feeStatus: String?
)

@JsonClass(generateAdapter = true)
data class ReceiptDto(
    val id: String,
    val head: String,
    val date: String,
    val mode: String,
    val amount: Double
)

@JsonClass(generateAdapter = true)
data class StudentLoanDto(
    val id: String,
    val bank: String,
    val branch: String,
    val sanctioned: Double,
    @Json(name = "interest_rate") val interestRate: Double,
    val emi: Double,
    val outstanding: Double,
    val status: String
)

@JsonClass(generateAdapter = true)
data class StudentLoanRequestDto(
    val bank: String,
    val branch: String,
    val sanctioned: Double,
    @Json(name = "interest_rate") val interestRate: Double,
    val emi: Double,
    val outstanding: Double
)

@JsonClass(generateAdapter = true)
data class AssistanceRequestDto(
    val id: String,
    val type: String,
    val reason: String,
    val status: String,
    @Json(name = "admin_remarks") val adminRemarks: String? = null,
    @Json(name = "created_at") val createdAt: String
)

@JsonClass(generateAdapter = true)
data class AssistanceRequestCreateDto(
    val type: String,
    val reason: String
)

@JsonClass(generateAdapter = true)
data class StudentCourseDto(
    val id: String,
    @Json(name = "subject_name") val subjectName: String? = null,
    @Json(name = "subject_code") val subjectCode: String? = null,
    val name: String? = null,
    val code: String? = null,
    val credits: Int? = null,
    @Json(name = "faculty_name") val facultyName: String? = null,
    val semester: Int? = null
)

@JsonClass(generateAdapter = true)
data class UploadDocumentResponseDto(
    val status: String,
    @Json(name = "file_url") val fileUrl: String
)

@JsonClass(generateAdapter = true)
data class StudyMaterialDto(
    val id: String,
    val title: String,
    @Json(name = "subject") val subjectName: String? = null,
    @Json(name = "file_url") val fileUrl: String,
    @Json(name = "uploaded_at") val uploadDate: String? = null,
    val type: String? = null,
    @Json(name = "faculty_name") val facultyName: String? = null
)

@JsonClass(generateAdapter = true)
data class AssignmentDto(
    val id: String,
    val title: String,
    val type: String,
    val subject: String,
    val unit: String? = null,
    val topic: String? = null,
    val deadline: String,
    val status: String,
    val description: String?,
    val instructions: String? = null,
    @Json(name = "total_marks") val totalMarks: Int? = null,
    @Json(name = "faculty_id") val facultyId: String? = null,
    @Json(name = "faculty_name") val facultyName: String? = null,
    val semester: String? = null,
    val section: String? = null,
    val attachments: List<AssignmentAttachmentDto> = emptyList(),
    @Json(name = "my_submission") val mySubmission: AssignmentSubmissionDto?
)

@JsonClass(generateAdapter = true)
data class AssignmentAttachmentDto(
    val name: String,
    val url: String,
    val size: String,
    val type: String
)

@JsonClass(generateAdapter = true)
data class AssignmentSubmissionDto(
    val id: String,
    @Json(name = "submitted_file_url") val submittedFileUrl: String?,
    @Json(name = "submitted_text") val submittedText: String?,
    val status: String,
    @Json(name = "marks_obtained") val marksObtained: Double?,
    val grade: String?,
    val feedback: String?,
    @Json(name = "submitted_at") val submittedAt: String
)

@JsonClass(generateAdapter = true)
data class AssignmentSubmitRequest(
    @Json(name = "file_url") val fileUrl: String?,
    @Json(name = "submitted_text") val submittedText: String?
)

@JsonClass(generateAdapter = true)
data class LeaveRequestDto(
    val id: String,
    val type: String,
    @Json(name = "from_date") val startDate: String,
    @Json(name = "to_date") val endDate: String,
    @Json(name = "num_days") val numDays: Double? = null,
    val reason: String,
    val status: String,
    val remarks: String?,
    @Json(name = "user_name") val userName: String? = null,
    @Json(name = "department_name") val departmentName: String? = null,
    @Json(name = "user_roll_no") val userRollNo: String? = null
)

@JsonClass(generateAdapter = true)
data class LeaveBalanceDto(
    @Json(name = "casual_leave") val casualLeave: Double,
    @Json(name = "sick_leave") val sickLeave: Double,
    @Json(name = "earned_leave") val earnedLeave: Double,
    @Json(name = "on_duty_leave") val onDutyLeave: Double
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
    val category: String?,
    @Json(name = "audience_type") val audienceType: String? = null,
    val priority: String? = null,
    @Json(name = "attachment_url") val attachmentUrl: String? = null,
    @Json(name = "publisher_name") val publisherName: String? = null,
    @Json(name = "publisher_role") val publisherRole: String? = null
)

@JsonClass(generateAdapter = true)
data class NoticeCreateRequest(
    val title: String,
    val body: String,
    @Json(name = "audience_type") val audienceType: String? = null,
    val priority: String? = null
)

@JsonClass(generateAdapter = true)
data class AcademicCalendarResponse(
    val events: List<CalendarEventDto>,
    val setup: AcademicCalendarSetupDto? = null
)

@JsonClass(generateAdapter = true)
data class AcademicCalendarSetupDto(
    @Json(name = "academicYear") val academicYear: String? = null
)

@JsonClass(generateAdapter = true)
data class CalendarEventDto(
    val id: String? = null,
    val date: String? = null,
    @Json(name = "event_name") val eventName: String? = null,
    @Json(name = "is_holiday") val isHoliday: Boolean = false,
    val title: String? = null,
    val category: String? = null,
    val time: String? = null,
    val venue: String? = null,
    val desc: String? = null
)

@JsonClass(generateAdapter = true)
data class HODCalendarEventDto(
    val id: String = "",
    val title: String = "",
    val category: String = "",
    @Json(name = "start_date") val startDate: String = "",
    @Json(name = "end_date") val endDate: String = "",
    val description: String = "",
    @Json(name = "academic_year") val academicYear: String = "",
    val department: String = "",
    val batch: String = "",
    val location: String = "",
    @Json(name = "is_holiday") val isHoliday: Boolean = false,
    @Json(name = "dept_id") val deptId: String? = null
)

@JsonClass(generateAdapter = true)
data class HODCalendarEventCreateRequest(
    val title: String,
    val category: String,
    @Json(name = "start_date") val startDate: String,
    @Json(name = "end_date") val endDate: String,
    val description: String,
    @Json(name = "academic_year") val academicYear: String,
    val department: String,
    val batch: String,
    val location: String,
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
data class ClassDiaryDto(
    val id: String,
    @Json(name = "faculty_id") val facultyId: String? = null,
    @Json(name = "faculty_name") val facultyName: String? = null,
    val date: String,
    val subject: String,
    val course: String? = null,
    val semester: String? = null,
    val section: String? = null,
    val hour: String? = null,
    val unit: String? = null,
    val topic: String? = null,
    val subtopic: String? = null,
    @Json(name = "teaching_method") val teachingMethod: String? = null,
    @Json(name = "learning_outcome") val learningOutcome: String? = null,
    @Json(name = "class_activity") val classActivity: String? = null,
    val remarks: String? = null,
    val status: String = "Draft",
    @Json(name = "completion_status") val completionStatus: String? = null,
    @Json(name = "hod_status") val hodStatus: String? = null,
    @Json(name = "hod_remarks") val hodRemarks: String? = null,
    @Json(name = "hod_reviewed_by") val hodReviewedBy: String? = null,
    @Json(name = "hod_reviewed_at") val hodReviewedAt: String? = null
)

@JsonClass(generateAdapter = true)
data class ClassDiaryRequest(
    val date: String,
    val subject: String,
    val course: String,
    val semester: String,
    val section: String,
    val hour: String,
    val unit: String,
    val topic: String,
    val subtopic: String,
    @Json(name = "teaching_method") val teachingMethod: String,
    @Json(name = "learning_outcome") val learningOutcome: String,
    @Json(name = "class_activity") val classActivity: String,
    val remarks: String,
    val status: String
)

@JsonClass(generateAdapter = true)
data class DiaryReviewRequest(
    @Json(name = "hod_status") val hodStatus: String,
    @Json(name = "hod_remarks") val hodRemarks: String? = null
)

@JsonClass(generateAdapter = true)
data class AdvisorAssignmentDto(
    @Json(name = "is_advisor") val isAdvisor: Boolean
)

@JsonClass(generateAdapter = true)
data class AdvisorStudentDto(
    @Json(name = "student_id") val studentId: String,
    val name: String,
    @Json(name = "roll_no") val rollNo: String,
    val department: String,
    val semester: Int,
    @Json(name = "year_of_study") val yearOfStudy: String,
    @Json(name = "attendance_percentage") val attendancePercentage: Double,
    @Json(name = "total_marks") val totalMarks: Double,
    @Json(name = "fee_status") val feeStatus: String,
    @Json(name = "leave_status") val leaveStatus: String,
    val phone: String? = null
)

@JsonClass(generateAdapter = true)
data class AdvisorLeaveDto(
    val id: String,
    @Json(name = "user_id") val userId: String,
    @Json(name = "user_name") val userName: String? = null,
    val type: String,
    @Json(name = "from_date") val fromDate: String,
    @Json(name = "to_date") val toDate: String,
    @Json(name = "num_days") val numDays: Double,
    val reason: String,
    val status: String,
    val remarks: String? = null
)

@JsonClass(generateAdapter = true)
data class LeaveApprovalRequest(
    val status: String,
    val remarks: String? = null
)

@JsonClass(generateAdapter = true)
data class MessageContactDto(
    val id: String,
    @Json(name = "full_name") val fullName: String,
    val role: String
)

@JsonClass(generateAdapter = true)
data class ConversationDto(
    @Json(name = "user_id") val userId: String,
    @Json(name = "user_name") val userName: String,
    @Json(name = "user_role") val userRole: String,
    @Json(name = "last_message") val lastMessage: String,
    @Json(name = "last_message_at") val lastMessageAt: String,
    @Json(name = "unread_count") val unreadCount: Int
)

@JsonClass(generateAdapter = true)
data class MessageDto(
    val id: String,
    @Json(name = "sender_id") val senderId: String,
    @Json(name = "receiver_id") val receiverId: String,
    val body: String,
    @Json(name = "is_read") val isRead: Boolean,
    @Json(name = "created_at") val createdAt: String
)

@JsonClass(generateAdapter = true)
data class SendMessageRequest(
    @Json(name = "receiver_id") val receiverId: String,
    val body: String
)

@JsonClass(generateAdapter = true)
data class NotificationDto(
    val id: String,
    val title: String? = null,
    val type: String? = null,
    val message: String,
    val date: String? = null,
    @Json(name = "created_at") val createdAt: String? = null,
    @Json(name = "is_read") val isRead: Boolean
)
// End of CamsApiService.kt
@JsonClass(generateAdapter = true)
data class MentorshipRecordDto(
    @Json(name = "meeting_log") val meetingLog: String? = null,
    @Json(name = "academic_review") val academicReview: String? = null,
    @Json(name = "improvement_plan") val improvementPlan: String? = null,
    val remarks: String? = null,
    @Json(name = "follow_up") val followUp: String? = null
)

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
    @Json(name = "follow_up") val followUp: String?
)

@JsonClass(generateAdapter = true)
data class FacultySalarySlipDto(
    val id: String,
    @Json(name = "faculty_id") val facultyId: String,
    @Json(name = "faculty_name") val facultyName: String = "",
    @Json(name = "employee_id") val employeeId: String? = null,
    @Json(name = "department_name") val departmentName: String? = null,
    val designation: String? = null,
    val month: Int,
    val year: Int,
    @Json(name = "working_days") val workingDays: Int = 30,
    @Json(name = "leave_days") val leaveDays: Int = 0,
    val basic: Double = 0.0,
    @Json(name = "pf_deduction") val pfDeduction: Double = 0.0,
    @Json(name = "leave_deduction") val leaveDeduction: Double = 0.0,
    @Json(name = "total_deductions") val totalDeductions: Double = 0.0,
    @Json(name = "net_salary") val netSalary: Double = 0.0,
    @Json(name = "pdf_url") val pdfUrl: String? = null,
    @Json(name = "absent_days") val absentDays: Int = 0,
    @Json(name = "absent_deduction") val absentDeduction: Double = 0.0,
    @Json(name = "remaining_leave_balance") val remainingLeaveBalance: Double = 0.0
)

@JsonClass(generateAdapter = true)
data class FacultyInternshipDriveDto(
    val id: String,
    @Json(name = "company_name") val company: String,
    val role: String,
    @Json(name = "package") val packageInfo: String? = null,
    @Json(name = "drive_date") val driveDate: String? = null,
    val status: String = "Hiring",
    val description: String? = null
)

@JsonClass(generateAdapter = true)
data class InternshipApplicationDto(
    val id: String,
    @Json(name = "drive_id") val driveId: String,
    @Json(name = "student_id") val studentId: String,
    val status: String = "Applied",
    @Json(name = "student_name") val studentName: String? = null,
    @Json(name = "roll_no") val rollNo: String? = null,
    @Json(name = "company_name") val companyName: String? = null,
    val role: String? = null
)

@JsonClass(generateAdapter = true)
data class InternshipApplicationReviewRequest(
    val status: String
)

@JsonClass(generateAdapter = true)
data class PartnerCompanyDto(
    val id: String,
    val name: String,
    val industry: String,
    val status: String = "Active",
    @Json(name = "contact_email") val contactEmail: String? = null,
    @Json(name = "contact_phone") val contactPhone: String? = null,
    val notes: String? = null
)

@JsonClass(generateAdapter = true)
data class PartnerCompanyRequest(
    val name: String,
    val industry: String,
    val status: String = "Active",
    @Json(name = "contact_email") val contactEmail: String? = null,
    @Json(name = "contact_phone") val contactPhone: String? = null,
    val notes: String? = null
)

@JsonClass(generateAdapter = true)
data class FacultyLegalEventDto(
    val id: String,
    val title: String,
    val category: String? = null,
    val description: String? = null,
    val date: String? = null,
    val time: String? = null,
    val duration: String? = null,
    val status: String? = null,
    val mode: String? = null,
    val platform: String? = null,
    val meetingLink: String? = null,
    val totalSeats: Int? = null,
    val availableSeats: Int? = null,
    val eventType: String? = null,
    val organizingInstitute: String? = null,
    @Json(name = "posted_by") val postedBy: String? = null,
    @Json(name = "posted_by_name") val postedByName: String? = null,
    @Json(name = "rejection_remarks") val rejectionRemarks: String? = null
)

@JsonClass(generateAdapter = true)
data class CreateLegalEventRequest(
    val title: String,
    val category: String,
    val description: String,
    val date: String,
    val time: String,
    val duration: String,
    val mode: String,
    val platform: String? = null,
    val meetingLink: String? = null,
    val totalSeats: Int,
    val eventType: String
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
    @Json(name = "department_name") val departmentName: String = "",
    @Json(name = "sem_count") val semCount: Int = 10
)

@JsonClass(generateAdapter = true)
data class HODLessonPlanSaveRequest(
    val units: Map<String, List<String>>
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
    @Json(name = "absentFacultyId") val absentFacultyId: String = "",
    @Json(name = "absentFacultyName") val absentFacultyName: String = "",
    @Json(name = "substituteFacultyId") val substituteFacultyId: String = "",
    @Json(name = "substituteFacultyName") val substituteFacultyName: String = "",
    val date: String = "",
    @Json(name = "periodLabel") val periodLabel: String = "",
    val status: String = "",
    val subject: String = "",
    val section: String = ""
)

@JsonClass(generateAdapter = true)
data class SubstitutionAssignRequest(
    @Json(name = "absent_faculty_id") val absentFacultyId: String,
    @Json(name = "absent_faculty_name") val absentFacultyName: String,
    @Json(name = "substitute_faculty_id") val substituteFacultyId: String,
    @Json(name = "substitute_faculty_name") val substituteFacultyName: String,
    val subject: String,
    val section: String,
    val date: String,
    @Json(name = "period_label") val periodLabel: String
)

@JsonClass(generateAdapter = true)
data class SubjectInfoDto(
    val id: String,
    val course: String,
    val semester: Int,
    @Json(name = "subject_code") val subjectCode: String,
    @Json(name = "subject_name") val subjectName: String,
    @Json(name = "degree_id") val degreeId: String? = null
)

@JsonClass(generateAdapter = true)
data class FacultyWorkloadInfoDto(
    val id: String,
    // Backend returns "name" (see /subject-allocations/faculty), not "full_name".
    @Json(name = "name") val fullName: String = "",
    val designation: String? = null,
    val specialization: String? = null,
    @Json(name = "current_workload_hours") val currentWorkloadHours: Int = 0,
    @Json(name = "max_workload_hours") val maxWorkloadHours: Int = 18
)

@JsonClass(generateAdapter = true)
data class SubjectAllocationCreateDto(
    @Json(name = "course_id") val courseId: String,
    @Json(name = "section_id") val sectionId: String,
    @Json(name = "faculty_id") val facultyId: String
)


@JsonClass(generateAdapter = true)
data class SubjectAllocationDto(
    val id: String = "",
    @Json(name = "course_id") val courseId: String = "",
    @Json(name = "section_id") val sectionId: String = "",
    @Json(name = "faculty_id") val facultyId: String = "",
    @Json(name = "academic_year_id") val academicYearId: String = "",
    @Json(name = "department_id") val departmentId: String = ""
)

@JsonClass(generateAdapter = true)
data class AcademicSetupSectionDto(
    val id: String,
    val name: String
)

@JsonClass(generateAdapter = true)
data class AcademicSetupDto(
    @Json(name = "academic_year") val academicYear: String = "",
    @Json(name = "academic_year_id") val academicYearId: String = "",
    val department: String = "",
    @Json(name = "department_id") val departmentId: String = "",
    val course: String = "",
    val semester: Int = 1,
    val degree: String = "",
    val sections: List<AcademicSetupSectionDto> = emptyList()
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
    @Json(name = "days_overdue") val daysOverdue: Int = 0,
    val area: String = ""
)

@JsonClass(generateAdapter = true)
data class HODPendingProofDto(
    @Json(name = "proof_id") val id: String = "",
    val title: String = "",
    val faculty_name: String = "",
    val journal_name: String = "",
    val issn_isbn: String = ""
)


@JsonClass(generateAdapter = true)
data class HODMentorStudentDto(
    val id: String = "",
    @Json(name = "user_id") val userId: String = "",
    @Json(name = "roll_no") val rollNo: String = "",
    val name: String = "",
    val email: String = "",
    val semester: Int = 0,
    @Json(name = "batch_year") val batchYear: Int = 0,
    @Json(name = "mentor_id") val mentorId: String? = null
)

@JsonClass(generateAdapter = true)
data class HODMentorDto(
    val id: String = "",
    val name: String = "",
    val email: String = "",
    val students: List<HODMentorStudentDto> = emptyList()
)

@JsonClass(generateAdapter = true)
data class HODMentorsPayloadDto(
    val faculty: List<HODMentorDto> = emptyList(),
    val students: List<HODMentorStudentDto> = emptyList()
)

@JsonClass(generateAdapter = true)
data class HODDepartmentReportDto(
    val summary: HODDepartmentReportSummaryDto = HODDepartmentReportSummaryDto(),
    @Json(name = "faculty_breakdown") val facultyBreakdown: List<HODFacultyBreakdownDto> = emptyList()
)

@JsonClass(generateAdapter = true)
data class HODDepartmentReportSummaryDto(
    @Json(name = "total_faculty") val totalFaculty: Int = 0,
    @Json(name = "active_faculty") val activeFaculty: Int = 0,
    @Json(name = "faculty_on_leave") val facultyOnLeave: Int = 0,
    @Json(name = "publications_submitted") val publicationsSubmitted: Int = 0,
    @Json(name = "publications_published") val publicationsPublished: Int = 0,
    @Json(name = "materials_approved") val materialsApproved: Int = 0,
    @Json(name = "avg_workload_hours") val avgWorkloadHours: Double = 0.0,
    @Json(name = "total_absences") val totalAbsences: Int = 0,
    @Json(name = "completed_substitutions") val completedSubstitutions: Int = 0,
    @Json(name = "total_verified_research") val totalVerifiedResearch: Int = 0,
    @Json(name = "pending_research_proofs") val pendingResearchProofs: Int = 0,
    @Json(name = "total_materials") val totalMaterials: Int = 0,
    @Json(name = "verified_materials") val verifiedMaterials: Int = 0
)

@JsonClass(generateAdapter = true)
data class HODFacultyBreakdownDto(
    @Json(name = "faculty_id") val facultyId: String = "",
    @Json(name = "faculty_name") val facultyName: String = "",
    val email: String = "",
    @Json(name = "employee_code") val employeeCode: String = "",
    val designation: String = "",
    @Json(name = "department_name") val departmentName: String = "",
    val qualification: String? = null,
    @Json(name = "employment_status") val employmentStatus: String = "Active",
    @Json(name = "workload_hours") val workloadHours: Int = 0,
    @Json(name = "verified_research") val verifiedResearch: Int = 0,
    @Json(name = "materials_submitted") val materialsSubmitted: Int = 0,
    @Json(name = "substitutions_handled") val substitutionsHandled: Int = 0
)

@JsonClass(generateAdapter = true)
data class HODStudentReportDto(
    val summary: HODStudentReportSummaryDto = HODStudentReportSummaryDto(),
    @Json(name = "student_rows") val studentRows: List<HODStudentReportRowDto> = emptyList()
)

@JsonClass(generateAdapter = true)
data class HODStudentReportSummaryDto(
    @Json(name = "total_students") val totalStudents: Int = 0,
    @Json(name = "total_attendance_records") val totalAttendanceRecords: Int = 0,
    @Json(name = "overall_attendance_pct") val overallAttendancePct: Double = 0.0,
    @Json(name = "total_leaves_applied") val totalLeavesApplied: Int = 0,
    @Json(name = "total_leaves_approved") val totalLeavesApproved: Int = 0,
    @Json(name = "average_cgpa") val averageCgpa: Double = 0.0,
    @Json(name = "total_arrears") val totalArrears: Int = 0,
    @Json(name = "total_internships") val totalInternships: Int = 0,
    @Json(name = "total_certifications") val totalCertifications: Int = 0,
    @Json(name = "total_sports") val totalSports: Int = 0
)

@JsonClass(generateAdapter = true)
data class HODStudentReportRowDto(
    @Json(name = "student_id") val studentId: String = "",
    val name: String = "",
    val email: String = "",
    @Json(name = "roll_no") val rollNo: String = "",
    val semester: Int = 0,
    val department: String = "",
    val cgpa: Double = 0.0,
    @Json(name = "arrear_count") val arrearCount: Int = 0,
    @Json(name = "total_classes") val totalClasses: Int = 0,
    val present: Int = 0,
    val absent: Int = 0,
    @Json(name = "attendance_pct") val attendancePct: Double = 0.0,
    @Json(name = "leaves_applied") val leavesApplied: Int = 0,
    @Json(name = "leaves_approved") val leavesApproved: Int = 0
)


@JsonClass(generateAdapter = true)
data class HODPendingMarksGroupDto(
    @Json(name = "section_id") val sectionId: String,
    @Json(name = "subject_id") val subjectId: String,
    @Json(name = "academic_year") val academicYear: String,
    val semester: Int,
    @Json(name = "course_name") val courseName: String,
    @Json(name = "section_name") val sectionName: String
)

@JsonClass(generateAdapter = true)
data class ApproveMarksGroupRequest(
    @Json(name = "section_id") val sectionId: String,
    @Json(name = "subject_id") val subjectId: String,
    @Json(name = "academic_year") val academicYear: String
)

@JsonClass(generateAdapter = true)
data class HODWorkloadDto(
    @Json(name = "faculty_id") val id: String = "",
    @Json(name = "faculty_name") val name: String = "",
    val semester: Int = 0,
    @Json(name = "teaching_hours") val teachingHours: Int = 0
)


@JsonClass(generateAdapter = true)
data class HODAcademicMonitoringDto(
    val subject: String = "",
    val faculty: String = "",
    val completion: Double = 0.0
)

@JsonClass(generateAdapter = true)
data class HODPendingEntryDto(
    val subject: String = "",
    val section: String = "",
    val hour: String = "",
    val date: String = "",
    @Json(name = "faculty_name") val facultyName: String = ""
)

@JsonClass(generateAdapter = true)
data class HODAttendanceMonitoringDto(
    val subject: String = "",
    @Json(name = "subject_code") val subjectCode: String = "",
    val semester: Int = 1,
    @Json(name = "student_count") val studentsCount: Int = 0,
    @Json(name = "attendance_percentage") val attendancePercentage: Double = 0.0,
    @Json(name = "low_attendance_count") val lowAttendanceCount: Int = 0
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
    @Json(name = "faculty_id") val facultyId: String,
    @Json(name = "student_ids") val studentIds: List<String>
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
    val id: String = "",
    @Json(name = "subject_code") val subjectCode: String = "",
    @Json(name = "subject_name") val subjectName: String = "",
    @Json(name = "section_id") val sectionId: String = "",
    @Json(name = "section_name") val sectionName: String = "",
    val room: String = "",
    @Json(name = "weekday") val dayOfWeek: String = "",
    @Json(name = "start_time") val startTime: String = "",
    @Json(name = "end_time") val endTime: String = "",
    @Json(name = "course_name") val courseName: String = "",
    val semester: String = "",
    @Json(name = "academic_year") val academicYear: String = ""
)

@JsonClass(generateAdapter = true)
data class AdminPayrollDto(
    val id: String,
    @Json(name = "faculty_id") val facultyId: String? = null,
    @Json(name = "faculty_name") val facultyName: String? = null,
    @Json(name = "department_name") val departmentName: String? = null,
    val designation: String? = null,
    val month: Int? = null,
    val year: Int? = null,
    val basic: Double? = null,
    @Json(name = "net_salary") val netSalary: Double? = null,
    @Json(name = "total_deductions") val totalDeductions: Double? = null,
    @Json(name = "pdf_url") val pdfUrl: String? = null
)

@JsonClass(generateAdapter = true)
data class AdminBackupDto(
    val id: String,
    val filename: String? = null,
    @Json(name = "size_bytes") val sizeBytes: Long? = null,
    val status: String? = null,
    @Json(name = "trigger_type") val triggerType: String? = null,
    @Json(name = "is_incremental") val isIncremental: Boolean? = null,
    @Json(name = "error_message") val errorMessage: String? = null,
    @Json(name = "created_at") val createdAt: String? = null
)

@JsonClass(generateAdapter = true)
data class AdminBackupSettingsDto(
    @Json(name = "auto_backup_enabled") val autoBackupEnabled: Boolean = true,
    @Json(name = "schedule_time") val scheduleTime: String = "21:00",
    @Json(name = "retention_count") val retentionCount: Int = 30
)

@JsonClass(generateAdapter = true)
data class AdminAcademicYearDto(
    val id: String,
    val name: String = "",
    @Json(name = "start_date") val startDate: String = "",
    @Json(name = "end_date") val endDate: String = "",
    @Json(name = "degree_id") val degreeId: String = "",
    val batch: String = "",
    @Json(name = "current_semester") val currentSemester: Int = 1,
    @Json(name = "is_semester_open") val isSemesterOpen: Boolean = true,
    @Json(name = "is_exam_period") val isExamPeriod: Boolean = false,
    @Json(name = "is_active") val isActive: Boolean = true,
    @Json(name = "degree_code") val degreeCode: String? = null,
    @Json(name = "degree_name") val degreeName: String? = null
)

@JsonClass(generateAdapter = true)
data class AdminSystemSettingsDto(
    val id: String? = null,
    @Json(name = "college_name") val collegeName: String? = null,
    @Json(name = "logo_url") val logoUrl: String? = null,
    val address: String? = null,
    @Json(name = "affiliation_number") val affiliationNumber: String? = null,
    @Json(name = "aicte_ugc_code") val aicteUgcCode: String? = null,
    @Json(name = "accreditation_body") val accreditationBody: String? = null,
    @Json(name = "bank_name") val bankName: String? = null,
    @Json(name = "bank_account_no") val bankAccountNo: String? = null,
    @Json(name = "bank_ifsc") val bankIfsc: String? = null,
    @Json(name = "bank_branch") val bankBranch: String? = null
)

@JsonClass(generateAdapter = true)
data class AdminAuditLogDto(
    val id: String,
    val action: String? = null,
    @Json(name = "entity_id") val entityId: String? = null,
    @Json(name = "user_name") val userName: String? = null,
    val timestamp: String? = null
)

@JsonClass(generateAdapter = true)
data class AdminUserDto(
    val id: String,
    val email: String,
    val phone: String? = null,
    @Json(name = "full_name") val fullName: String,
    val role: String,
    @Json(name = "is_active") val isActive: Boolean,
    @Json(name = "department_id") val departmentId: String? = null,
    @Json(name = "department_name") val departmentName: String? = null
)

@JsonClass(generateAdapter = true)
data class AdminDepartmentDto(
    val id: String,
    val code: String,
    val name: String,
    @Json(name = "course_name") val courseName: String? = null,
    @Json(name = "program_level") val programLevel: String? = null
)

@JsonClass(generateAdapter = true)
data class AdminDegreeDto(
    val id: String,
    val name: String,
    val code: String,
    @Json(name = "duration_years") val durationYears: Int? = null,
    @Json(name = "program_level") val programLevel: String? = null,
    @Json(name = "applicable_batch") val applicableBatch: String? = null,
    @Json(name = "dept_id") val deptId: String? = null
)

@JsonClass(generateAdapter = true)
data class AdminCourseDto(
    val id: String,
    val code: String,
    val name: String,
    val semester: Int,
    val credits: Int? = null,
    @Json(name = "degree_id") val degreeId: String? = null,
    @Json(name = "dept_id") val deptId: String? = null
)

@JsonClass(generateAdapter = true)
data class AdminAttendanceDefaulterDto(
    @Json(name = "student_id") val studentId: String,
    @Json(name = "user_id") val userId: String? = null,
    val name: String? = null,
    @Json(name = "roll_no") val rollNo: String? = null,
    @Json(name = "degree_code") val degreeCode: String? = null,
    @Json(name = "degree_name") val degreeName: String? = null,
    val batch: String? = null,
    val semester: Int? = null,
    val section: String? = null,
    @Json(name = "attendance_percentage") val attendancePercentage: Double? = null,
    @Json(name = "fine_paid") val finePaid: Boolean = false
)

@JsonClass(generateAdapter = true)
data class AdminFeeStructureDto(
    val id: String,
    @Json(name = "dept_id") val deptId: String? = null,
    @Json(name = "dept_name") val deptName: String? = null,
    val semester: Int? = null,
    val amount: Double? = null,
    @Json(name = "due_date") val dueDate: String? = null,
    @Json(name = "fee_type") val feeType: String? = null
)

@JsonClass(generateAdapter = true)
data class AdminScholarshipTypeDto(
    val id: String,
    val name: String? = null,
    val description: String? = null,
    @Json(name = "reduction_type") val reductionType: String? = null,
    @Json(name = "reduction_value") val reductionValue: Double? = null,
    val scope: String? = null
)

@JsonClass(generateAdapter = true)
data class AdminFeeStudentDto(
    @Json(name = "student_id") val studentId: String,
    @Json(name = "user_id") val userId: String? = null,
    val name: String? = null,
    @Json(name = "roll_no") val rollNo: String? = null,
    val email: String? = null,
    @Json(name = "department_name") val departmentName: String? = null,
    @Json(name = "department_id") val departmentId: String? = null,
    val semester: Int? = null,
    @Json(name = "batch_year") val batchYear: Int? = null
)

@JsonClass(generateAdapter = true)
data class AdminStudentFeeRecordDto(
    @Json(name = "record_id") val recordId: String,
    @Json(name = "fee_type") val feeType: String? = null,
    val semester: Int? = null,
    val amount: Double? = null,
    @Json(name = "paid_amount") val paidAmount: Double? = null,
    @Json(name = "remaining_amount") val remainingAmount: Double? = null,
    @Json(name = "due_date") val dueDate: String? = null,
    val status: String? = null
)

@JsonClass(generateAdapter = true)
data class AdminCollectFeeRequest(
    @Json(name = "fee_record_id") val feeRecordId: String,
    val amount: Double,
    val mode: String = "Cash"
)




@JsonClass(generateAdapter = true)
data class UserDto(
    val id: String,
    val name: String,
    val email: String,
    val role: String,
    val status: String
)

@JsonClass(generateAdapter = true)
data class PendingFacultyDto(
    val id: String,
    val name: String,
    val department: String,
    val requestDate: String
)

@JsonClass(generateAdapter = true)
data class CircularDto(
    val id: String,
    val title: String,
    val content: String,
    val date: String,
    val author: String
)

@JsonClass(generateAdapter = true)
data class CircularPublishRequest(
    val title: String,
    val content: String,
    val targetAudience: String
)

// ==================== ERP: Hostel ====================

@JsonClass(generateAdapter = true)
data class HostelBlockDto(
    val id: String,
    val name: String,
    val code: String,
    @Json(name = "hostel_type") val hostelType: String = "BOYS",
    @Json(name = "warden_name") val wardenName: String? = null,
    @Json(name = "warden_phone") val wardenPhone: String? = null,
    val address: String? = null,
    @Json(name = "total_rooms") val totalRooms: Int = 0,
    @Json(name = "total_capacity") val totalCapacity: Int = 0,
    val occupied: Int = 0
)

@JsonClass(generateAdapter = true)
data class HostelBlockCreateRequest(
    val name: String,
    val code: String,
    @Json(name = "hostel_type") val hostelType: String = "BOYS",
    @Json(name = "warden_name") val wardenName: String? = null,
    @Json(name = "warden_phone") val wardenPhone: String? = null,
    val address: String? = null
)

@JsonClass(generateAdapter = true)
data class HostelRoomDto(
    val id: String,
    @Json(name = "block_id") val blockId: String,
    @Json(name = "block_name") val blockName: String? = null,
    @Json(name = "room_number") val roomNumber: String,
    val floor: Int = 0,
    val capacity: Int = 2,
    @Json(name = "room_type") val roomType: String? = null,
    @Json(name = "monthly_rent") val monthlyRent: Double? = null,
    val occupied: Int = 0,
    val available: Int = 0
)

@JsonClass(generateAdapter = true)
data class HostelRoomCreateRequest(
    @Json(name = "block_id") val blockId: String,
    @Json(name = "room_number") val roomNumber: String,
    val floor: Int = 0,
    val capacity: Int = 2,
    @Json(name = "room_type") val roomType: String? = null,
    @Json(name = "monthly_rent") val monthlyRent: Double? = null
)

@JsonClass(generateAdapter = true)
data class HostelAllocationDto(
    val id: String,
    @Json(name = "room_id") val roomId: String,
    @Json(name = "room_number") val roomNumber: String? = null,
    @Json(name = "block_name") val blockName: String? = null,
    @Json(name = "student_id") val studentId: String,
    @Json(name = "student_name") val studentName: String? = null,
    @Json(name = "roll_no") val rollNo: String? = null,
    @Json(name = "allocated_on") val allocatedOn: String,
    @Json(name = "vacated_on") val vacatedOn: String? = null,
    val status: String,
    val remarks: String? = null
)

@JsonClass(generateAdapter = true)
data class HostelAllocationCreateRequest(
    @Json(name = "room_id") val roomId: String,
    @Json(name = "student_id") val studentId: String,
    @Json(name = "allocated_on") val allocatedOn: String? = null,
    val remarks: String? = null
)

// ==================== ERP: Inventory ====================

@JsonClass(generateAdapter = true)
data class InventoryItemDto(
    val id: String,
    val name: String,
    val code: String,
    val category: String? = null,
    val unit: String = "pcs",
    val quantity: Int = 0,
    @Json(name = "min_quantity") val minQuantity: Int = 0,
    @Json(name = "unit_price") val unitPrice: Double? = null,
    val location: String? = null,
    val supplier: String? = null,
    @Json(name = "is_low_stock") val isLowStock: Boolean = false
)

@JsonClass(generateAdapter = true)
data class InventoryItemCreateRequest(
    val name: String,
    val code: String,
    val category: String? = null,
    val unit: String = "pcs",
    val quantity: Int = 0,
    @Json(name = "min_quantity") val minQuantity: Int = 0,
    @Json(name = "unit_price") val unitPrice: Double? = null,
    val location: String? = null,
    val supplier: String? = null
)

@JsonClass(generateAdapter = true)
data class StockMovementRequest(
    val movement: String,
    val quantity: Int,
    val reason: String? = null
)

@JsonClass(generateAdapter = true)
data class InventoryTransactionDto(
    val id: String,
    @Json(name = "item_id") val itemId: String,
    @Json(name = "item_name") val itemName: String? = null,
    val movement: String,
    val quantity: Int,
    @Json(name = "resulting_quantity") val resultingQuantity: Int = 0,
    val reason: String? = null,
    @Json(name = "performed_by_name") val performedByName: String? = null,
    @Json(name = "created_at") val createdAt: String? = null
)

// ==================== ERP: Library ====================

@JsonClass(generateAdapter = true)
data class LibraryBookDto(
    val id: String,
    val title: String,
    val author: String? = null,
    @Json(name = "accession_no") val accessionNo: String,
    val isbn: String? = null,
    val category: String? = null,
    val publisher: String? = null,
    @Json(name = "published_year") val publishedYear: Int? = null,
    @Json(name = "shelf_location") val shelfLocation: String? = null,
    @Json(name = "total_copies") val totalCopies: Int = 1,
    @Json(name = "available_copies") val availableCopies: Int = 1
)

@JsonClass(generateAdapter = true)
data class LibraryBookCreateRequest(
    val title: String,
    @Json(name = "accession_no") val accessionNo: String,
    val author: String? = null,
    val isbn: String? = null,
    val category: String? = null,
    val publisher: String? = null,
    @Json(name = "published_year") val publishedYear: Int? = null,
    @Json(name = "shelf_location") val shelfLocation: String? = null,
    @Json(name = "total_copies") val totalCopies: Int = 1
)

@JsonClass(generateAdapter = true)
data class LibraryIssueCreateRequest(
    @Json(name = "book_id") val bookId: String,
    @Json(name = "member_id") val memberId: String,
    @Json(name = "issued_on") val issuedOn: String? = null,
    @Json(name = "due_on") val dueOn: String? = null
)

@JsonClass(generateAdapter = true)
data class LibraryReturnRequest(
    @Json(name = "returned_on") val returnedOn: String? = null,
    @Json(name = "fine_amount") val fineAmount: Double? = null,
    val remarks: String? = null
)

@JsonClass(generateAdapter = true)
data class LibraryIssueDto(
    val id: String,
    @Json(name = "book_id") val bookId: String,
    @Json(name = "book_title") val bookTitle: String? = null,
    @Json(name = "member_id") val memberId: String,
    @Json(name = "member_name") val memberName: String? = null,
    @Json(name = "issued_on") val issuedOn: String,
    @Json(name = "due_on") val dueOn: String,
    @Json(name = "returned_on") val returnedOn: String? = null,
    @Json(name = "fine_amount") val fineAmount: Double = 0.0,
    val status: String,
    @Json(name = "is_overdue") val isOverdue: Boolean = false,
    val remarks: String? = null
)

// ==================== ERP: Transport ====================

@JsonClass(generateAdapter = true)
data class TransportRouteDto(
    val id: String,
    val name: String,
    val code: String,
    @Json(name = "start_point") val startPoint: String,
    @Json(name = "end_point") val endPoint: String,
    @Json(name = "distance_km") val distanceKm: Double? = null,
    val fare: Double? = null,
    val stops: String? = null,
    @Json(name = "vehicle_count") val vehicleCount: Int = 0,
    @Json(name = "pass_count") val passCount: Int = 0
)

@JsonClass(generateAdapter = true)
data class TransportRouteCreateRequest(
    val name: String,
    val code: String,
    @Json(name = "start_point") val startPoint: String,
    @Json(name = "end_point") val endPoint: String,
    @Json(name = "distance_km") val distanceKm: Double? = null,
    val fare: Double? = null,
    val stops: String? = null
)

@JsonClass(generateAdapter = true)
data class TransportVehicleDto(
    val id: String,
    @Json(name = "registration_no") val registrationNo: String,
    @Json(name = "vehicle_type") val vehicleType: String? = null,
    val capacity: Int = 40,
    @Json(name = "driver_name") val driverName: String? = null,
    @Json(name = "driver_phone") val driverPhone: String? = null,
    @Json(name = "route_id") val routeId: String? = null,
    @Json(name = "route_name") val routeName: String? = null,
    val status: String = "ACTIVE"
)

@JsonClass(generateAdapter = true)
data class TransportVehicleCreateRequest(
    @Json(name = "registration_no") val registrationNo: String,
    @Json(name = "vehicle_type") val vehicleType: String? = null,
    val capacity: Int = 40,
    @Json(name = "driver_name") val driverName: String? = null,
    @Json(name = "driver_phone") val driverPhone: String? = null,
    @Json(name = "route_id") val routeId: String? = null,
    val status: String = "ACTIVE"
)

@JsonClass(generateAdapter = true)
data class TransportPassDto(
    val id: String,
    @Json(name = "route_id") val routeId: String,
    @Json(name = "route_name") val routeName: String? = null,
    @Json(name = "student_id") val studentId: String,
    @Json(name = "student_name") val studentName: String? = null,
    @Json(name = "roll_no") val rollNo: String? = null,
    @Json(name = "pickup_point") val pickupPoint: String? = null,
    @Json(name = "valid_from") val validFrom: String,
    @Json(name = "valid_to") val validTo: String,
    @Json(name = "fare_paid") val farePaid: Double? = null,
    val status: String
)

@JsonClass(generateAdapter = true)
data class TransportPassCreateRequest(
    @Json(name = "route_id") val routeId: String,
    @Json(name = "student_id") val studentId: String,
    @Json(name = "pickup_point") val pickupPoint: String? = null,
    @Json(name = "valid_from") val validFrom: String? = null,
    @Json(name = "valid_to") val validTo: String? = null,
    @Json(name = "fare_paid") val farePaid: Double? = null
)

