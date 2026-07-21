package com.example.core.repository

import com.example.core.network.*
import com.example.features.student.models.*
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import retrofit2.Response

interface StudentRepository {
    suspend fun getDashboard(): DashboardResponse?
    suspend fun getProfile(): StudentProfileResponse?
    suspend fun updateProfile(profile: StudentProfileResponse): StudentProfileResponse?
    suspend fun submitForVerification(): StudentProfileResponse?
    suspend fun uploadProfileDocument(documentType: String, file: okhttp3.MultipartBody.Part): String?
    suspend fun getMentorshipRecord(): MentorshipRecord?
    suspend fun getAttendance(): AttendanceSummaryResponse?
    suspend fun getInternalMarks(): List<StudentInternalMarkDto>
    suspend fun getTimetable(): List<TimetableSlotDto>
    suspend fun getCourses(): List<StudentCourseDto>
    suspend fun getFees(): StudentFeeSummaryResponse?
    suspend fun createFeeOrder(recordId: String, amount: Double): Response<CreateOrderResponseDto>
    suspend fun getFeeReceipts(): List<ReceiptDto>
    suspend fun getStudentLoan(): StudentLoanDto?
    suspend fun upsertStudentLoan(bank: String, branch: String, sanctioned: Double, interestRate: Double, emi: Double, outstanding: Double): Result<StudentLoanDto>
    suspend fun getAssistanceRequests(): List<AssistanceRequestDto>
    suspend fun createAssistanceRequest(type: String, reason: String): Result<AssistanceRequestDto>
    suspend fun verifyFeePayment(recordId: String, orderId: String, paymentId: String, signature: String): Response<VerifyPaymentResponseDto>
    suspend fun getAssignments(): List<AssignmentDto>
    suspend fun getAssignmentsPaged(skip: Int, limit: Int): Response<List<AssignmentDto>>
    suspend fun getStudyMaterials(): List<StudyMaterialDto>
    suspend fun getStudyMaterialsPaged(skip: Int, limit: Int): Response<List<StudyMaterialDto>>
    suspend fun submitAssignment(asgId: String, file: String?, text: String?): Boolean
    suspend fun uploadAssignmentSubmission(asgId: String, file: okhttp3.MultipartBody.Part): FileUploadResponseDto?
    suspend fun getLeaves(): List<LeaveRequestDto>
    suspend fun getHallTickets(): List<HallTicketDto>
    suspend fun applyLeave(type: String, fromDate: String, toDate: String, reason: String, appCategory: String = "Leave"): Boolean
    suspend fun getNotices(): List<NoticeDto>
    suspend fun getNoticesPaged(skip: Int, limit: Int): Response<List<NoticeDto>>
    suspend fun getAcademicCalendar(): List<CalendarEventDto>
    suspend fun getAcademicCalendarYear(): String?
    suspend fun getGrievances(): List<GrievanceDto>
    suspend fun getGrievancesPaged(skip: Int, limit: Int): Response<List<GrievanceDto>>
    suspend fun raiseGrievance(category: String, description: String): Boolean
    suspend fun getOnlineMeetings(): List<OnlineMeetingDto>
    suspend fun getLegalEvents(): List<LegalEventDto>
    suspend fun getLegalEventsPaged(skip: Int, limit: Int): Response<List<LegalEventDto>>
    suspend fun getMyLegalEventRegistrations(): List<LegalEventRegistrationDto>
    suspend fun registerForLegalEvent(eventId: String): Boolean
    suspend fun getMyLegalEventQuestions(): List<LegalEventQuestionDto>
    suspend fun submitLegalEventQuestion(eventId: String, eventTitle: String, question: String, topic: String): Boolean
    suspend fun getClubs(): List<ClubDto>
    suspend fun getClubsPaged(skip: Int, limit: Int): Response<List<ClubDto>>
    suspend fun getClubAnnouncements(): List<ClubAnnouncementDto>
    suspend fun getSyllabus(): List<SyllabusDto>
    suspend fun getSyllabusProgress(): Map<String, com.example.features.academics.models.SyllabusProgress>
    suspend fun getLessonPlanTracking(): List<com.example.features.academics.models.LessonPlanItem>
    suspend fun getNotifications(): List<NotificationDto>
    suspend fun getNotificationsPaged(skip: Int, limit: Int): Response<List<NotificationDto>>
    suspend fun markNotificationRead(id: String): Boolean
    suspend fun deleteNotification(id: String): Boolean
    suspend fun markAllNotificationsRead(): Boolean
    suspend fun joinClub(id: String): ClubDto?
    suspend fun leaveClub(id: String): ClubDto?
    suspend fun getCouncilData(): CouncilDataDto?
    suspend fun submitCouncilProposal(title: String, description: String): Boolean
    suspend fun submitCouncilFeedback(title: String): FeedbackDto?
    suspend fun upvoteCouncilFeedback(id: Int): FeedbackDto?
    suspend fun submitGrievance(category: String, priority: String, subject: String, description: String)
    suspend fun getLexNovaStats(): List<LexNovaKpiDto>
    suspend fun getInternshipDrives(): List<InternshipDriveDto>
    suspend fun getAlumniNetwork(): List<AlumniMentorDto>
    suspend fun getInternshipsList(): List<InternshipDriveDto>
    suspend fun getInternshipsListPaged(skip: Int, limit: Int): Response<List<InternshipDriveDto>>
    suspend fun getPersonalInternships(): List<com.example.features.campus_life.models.InternshipRecord>
    suspend fun addInternship(internship: com.example.features.campus_life.models.InternshipRecord): Boolean
    suspend fun deleteInternship(id: String): Boolean
    suspend fun applyToInternshipDrive(driveId: String): InternshipApplicationResponseDto?
    suspend fun getInternshipApplications(): List<InternshipApplicationResponseDto>
    suspend fun getCertificationsList(): List<CertificationRecordDto>
    suspend fun createCertification(title: String, issuer: String, date: String, category: String, type: String, fileUrl: String? = null): CertificationRecordDto?
    suspend fun deleteCertification(id: String): Boolean
    suspend fun getActivityPoints(): List<ActivityPointDto>
    suspend fun claimActivityPoints(title: String, category: String, description: String, date: String, points: Double, supportingDocument: String? = null): ActivityPointDto?
    suspend fun deleteActivityPoint(id: String): Boolean
    suspend fun uploadActivityPointDocument(file: okhttp3.MultipartBody.Part): String?
    suspend fun getServiceOpportunities(): List<ServiceOpportunityDto>
    suspend fun getServiceLogs(): List<ServiceLogDto>
    suspend fun applyToServiceOpportunity(id: Int): ServiceLogDto?
    suspend fun logServiceHours(title: String, organization: String, category: String, date: String, hours: Double, description: String, proofDocument: String? = null): ServiceLogDto?
    suspend fun deleteServiceLog(id: String): Boolean
    suspend fun uploadServiceDocument(file: okhttp3.MultipartBody.Part): String?
    suspend fun getInnovationProjects(): List<InnovationProjectDto>
    suspend fun createInnovationProject(title: String, description: String, category: String, mentor: String, team: List<String>): InnovationProjectDto?
    suspend fun toggleInnovationProjectLike(id: String): InnovationProjectDto?
    suspend fun addInnovationProjectComment(id: String, text: String): InnovationProjectDto?
    suspend fun deleteInnovationProject(id: String): Boolean
    suspend fun getResearchPapers(): List<ResearchPaperDto>
    suspend fun submitResearchPaper(title: String, abstract: String, category: String, guide: String, team: List<String>, fileUrl: String?, fileSize: String?): ResearchPaperDto?
    suspend fun uploadResearchPaper(file: okhttp3.MultipartBody.Part): PaperUploadResponseDto?
    suspend fun changePassword(currentPassword: String, newPassword: String)

    // Moot Court Memorials
    suspend fun getMootCourtMemorials(): List<MootCourtMemorialDto>
    suspend fun createMootCourtMemorial(title: String, caseName: String?, content: String, status: String?): MootCourtMemorialDto?
    suspend fun updateMootCourtMemorial(id: String, title: String?, caseName: String?, content: String?, status: String?): MootCourtMemorialDto?
    suspend fun deleteMootCourtMemorial(id: String): Boolean

    // Saved Citations
    suspend fun getSavedCitations(): List<SavedCitationDto>
    suspend fun createSavedCitation(caseName: String, citationText: String, note: String?): SavedCitationDto?
    suspend fun deleteSavedCitation(id: String): Boolean

    // Chatbot
    suspend fun sendChatMessage(message: String, sessionId: String?): ChatMessageResponse?
    suspend fun getChatHistory(): List<ChatSessionSummaryDto>
    suspend fun getChatSessionMessages(sessionId: String): List<ChatMessageDto>
    suspend fun deleteChatSession(sessionId: String): Boolean
}

class StudentRepositoryImpl(
    private val apiService: CamsApiService
) : StudentRepository {

    override suspend fun getDashboard(): DashboardResponse? {
        val response = apiService.getStudentDashboard()
        return if (response.isSuccessful) {
            val dto = response.body()
            if (dto != null) {
                DashboardResponse(
                    metrics = dto.metrics.map { MetricSchema(it.id, it.label, it.value) }
                )
            } else null
        } else null
    }

    override suspend fun getProfile(): StudentProfileResponse? {
        val response = apiService.getStudentProfile()
        return if (response.isSuccessful) {
            val dto = response.body()
            if (dto != null) {
                StudentProfileResponse(
                    id = dto.id ?: "current",
                    fullName = dto.fullName ?: "",
                    rollNo = dto.rollNo ?: "",
                    email = dto.email ?: "",
                    semester = dto.semester ?: 1,
                    batchYear = dto.batchYear ?: 0,
                    mentorName = dto.mentorName,
                    mentorEmail = dto.mentorEmail,
                    mentorPhone = dto.mentorPhone,
                    cgpa = dto.cgpa,
                    skills = dto.skills,
                    courseName = dto.courseName ?: dto.department,
                    section = dto.section,
                    classAdvisorName = dto.classAdvisorName,
                    classAdvisorEmail = dto.classAdvisorEmail,
                    classAdvisorPhone = dto.classAdvisorPhone,
                    batch = dto.batch,
                    yearOfStudy = dto.yearOfStudy,
                    departmentName = dto.departmentName ?: dto.department,
                    profilePhotoUrl = dto.profilePhotoUrl,
                    verificationStatus = dto.verificationStatus ?: "DRAFT",
                    dateOfBirth = dto.dob,
                    gender = dto.gender,
                    bloodGroup = dto.bloodGroup,
                    nationality = dto.nationality,
                    mobileNumber = dto.phone,
                    currentAddress = dto.currentAddress,
                    permanentAddress = dto.permanentAddress,
                    aadhaarNumber = dto.aadhaarNo,
                    communityCategory = dto.communityCategory,
                    fatherName = dto.fatherName,
                    motherName = dto.motherName,
                    documentAadhaarUrl = dto.documentAadhaarUrl,
                    documentCommunityUrl = dto.documentCommunityUrl,
                    documentTcUrl = dto.documentTcUrl,
                    documentOtherUrl = dto.documentOtherUrl
                )
            } else null
        } else null
    }

    override suspend fun updateProfile(profile: StudentProfileResponse): StudentProfileResponse? {
        val dto = StudentProfileDto(
            fullName = profile.fullName,
            dob = profile.dateOfBirth,
            gender = profile.gender,
            bloodGroup = profile.bloodGroup,
            nationality = profile.nationality,
            phone = profile.mobileNumber,
            currentAddress = profile.currentAddress,
            permanentAddress = profile.permanentAddress,
            aadhaarNo = profile.aadhaarNumber,
            passportNo = null,
            communityCategory = profile.communityCategory,
            religion = null,
            fatherName = profile.fatherName,
            motherName = profile.motherName,
            verificationStatus = profile.verificationStatus,
            semester = profile.semester,
            department = profile.departmentName
        )
        val response = apiService.updateStudentProfile(dto)
        if (response.isSuccessful) {
            return getProfile() // Fetch fresh profile
        }
        return null
    }

    override suspend fun submitForVerification(): StudentProfileResponse? {
        val response = apiService.submitStudentProfile()
        if (response.isSuccessful) {
            return getProfile() // Fetch fresh profile
        }
        return null
    }

    override suspend fun uploadProfileDocument(documentType: String, file: okhttp3.MultipartBody.Part): String? {
        val response = apiService.uploadProfileDocument(documentType, file)
        return if (response.isSuccessful) response.body()?.fileUrl else null
    }

    override suspend fun getMentorshipRecord(): MentorshipRecord? {
        val response = apiService.getMentorshipRecord()
        if (response.isSuccessful) {
            val dto = response.body()
            if (dto != null) {
                return MentorshipRecord(
                    meetingLog = dto.meetingLog ?: "",
                    academicReview = dto.academicReview ?: "",
                    improvementPlan = dto.improvementPlan ?: "",
                    remarks = dto.remarks ?: "",
                    followUp = dto.followUp ?: ""
                )
            }
        }
        return null
    }

    override suspend fun getAttendance(): AttendanceSummaryResponse? {
        val response = apiService.getAttendance()
        return if (response.isSuccessful) response.body() else null
    }

    override suspend fun getInternalMarks(): List<StudentInternalMarkDto> {
        val response = apiService.getStudentInternalMarks()
        return if (response.isSuccessful) response.body() ?: emptyList() else emptyList()
    }

    override suspend fun getTimetable(): List<TimetableSlotDto> {
        val response = apiService.getTimetable()
        return if (response.isSuccessful) response.body() ?: emptyList() else emptyList()
    }

    override suspend fun getCourses(): List<StudentCourseDto> {
        val response = apiService.getStudentCourses()
        return if (response.isSuccessful) response.body() ?: emptyList() else emptyList()
    }

    override suspend fun getFees(): StudentFeeSummaryResponse? {
        val response = apiService.getFees()
        return if (response.isSuccessful) response.body() else null
    }

    override suspend fun createFeeOrder(recordId: String, amount: Double): Response<CreateOrderResponseDto> {
        return apiService.createOrder(recordId, CreateOrderRequestDto(amount))
    }

    override suspend fun verifyFeePayment(recordId: String, orderId: String, paymentId: String, signature: String): Response<VerifyPaymentResponseDto> {
        return apiService.verifyPayment(recordId, VerifyPaymentRequestDto(orderId, paymentId, signature))
    }

    override suspend fun getFeeReceipts(): List<ReceiptDto> {
        val response = apiService.getFeeReceipts()
        return if (response.isSuccessful) response.body() ?: emptyList() else emptyList()
    }

    override suspend fun getStudentLoan(): StudentLoanDto? {
        val response = apiService.getStudentLoan()
        return if (response.isSuccessful) response.body() else null
    }

    override suspend fun upsertStudentLoan(bank: String, branch: String, sanctioned: Double, interestRate: Double, emi: Double, outstanding: Double): Result<StudentLoanDto> {
        return try {
            val response = apiService.upsertStudentLoan(StudentLoanRequestDto(bank, branch, sanctioned, interestRate, emi, outstanding))
            if (response.isSuccessful && response.body() != null) {
                Result.success(response.body()!!)
            } else {
                Result.failure(Exception(response.errorBody()?.string() ?: "Failed to save loan details"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun getAssistanceRequests(): List<AssistanceRequestDto> {
        val response = apiService.getAssistanceRequests()
        return if (response.isSuccessful) response.body() ?: emptyList() else emptyList()
    }

    override suspend fun createAssistanceRequest(type: String, reason: String): Result<AssistanceRequestDto> {
        return try {
            val response = apiService.createAssistanceRequest(AssistanceRequestCreateDto(type, reason))
            if (response.isSuccessful && response.body() != null) {
                Result.success(response.body()!!)
            } else {
                Result.failure(Exception(response.errorBody()?.string() ?: "Failed to submit request"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun getAssignments(): List<AssignmentDto> {
        val response = apiService.getAssignments(0, 100)
        return if (response.isSuccessful) response.body() ?: emptyList() else emptyList()
    }

    override suspend fun getAssignmentsPaged(skip: Int, limit: Int): Response<List<AssignmentDto>> {
        return apiService.getAssignments(skip, limit)
    }

    override suspend fun getStudyMaterials(): List<StudyMaterialDto> {
        val response = apiService.getStudyMaterials(0, 100)
        return if (response.isSuccessful) response.body() ?: emptyList() else emptyList()
    }

    override suspend fun getStudyMaterialsPaged(skip: Int, limit: Int): Response<List<StudyMaterialDto>> {
        return apiService.getStudyMaterials(skip, limit)
    }

    override suspend fun submitAssignment(asgId: String, file: String?, text: String?): Boolean {
        val response = apiService.submitAssignment(asgId, AssignmentSubmitRequest(file, text))
        return response.isSuccessful
    }

    override suspend fun uploadAssignmentSubmission(asgId: String, file: okhttp3.MultipartBody.Part): FileUploadResponseDto? {
        val response = apiService.uploadAssignmentSubmission(asgId, file)
        return if (response.isSuccessful) response.body() else null
    }

    override suspend fun getLeaves(): List<LeaveRequestDto> {
        val response = apiService.getLeaves()
        return if (response.isSuccessful) response.body() ?: emptyList() else emptyList()
    }

    override suspend fun getHallTickets(): List<HallTicketDto> {
        val response = apiService.getHallTickets()
        return if (response.isSuccessful) response.body() ?: emptyList() else emptyList()
    }

    override suspend fun applyLeave(type: String, fromDate: String, toDate: String, reason: String, appCategory: String): Boolean {
        val response = apiService.applyLeave(LeaveApplicationRequest(appCategory = appCategory, type = type, fromDate = fromDate, toDate = toDate, reason = reason))
        return response.isSuccessful
    }

    override suspend fun getNotices(): List<NoticeDto> {
        val response = apiService.getNotices(0, 100)
        return if (response.isSuccessful) response.body() ?: emptyList() else emptyList()
    }

    override suspend fun getNoticesPaged(skip: Int, limit: Int): Response<List<NoticeDto>> {
        return apiService.getNotices(skip, limit)
    }

    override suspend fun getAcademicCalendar(): List<CalendarEventDto> {
        val response = apiService.getAcademicCalendar()
        return if (response.isSuccessful) response.body()?.events ?: emptyList() else emptyList()
    }

    override suspend fun getAcademicCalendarYear(): String? {
        val response = apiService.getAcademicCalendar()
        return if (response.isSuccessful) response.body()?.setup?.academicYear else null
    }

    override suspend fun getGrievances(): List<GrievanceDto> {
        val response = apiService.getStudentGrievances(0, 100)
        return if (response.isSuccessful) response.body() ?: emptyList() else emptyList()
    }
    
    override suspend fun getGrievancesPaged(skip: Int, limit: Int): Response<List<GrievanceDto>> {
        return apiService.getStudentGrievances(skip, limit)
    }

    override suspend fun raiseGrievance(category: String, description: String): Boolean {
        val response = apiService.raiseGrievance(GrievanceRaiseRequest(category, "General", description))
        return response.isSuccessful
    }
    override suspend fun getOnlineMeetings(): List<OnlineMeetingDto> = apiService.getStudentMeetings().body() ?: emptyList()
    override suspend fun getLegalEvents(): List<LegalEventDto> {
        val response = apiService.getStudentLegalEvents(0, 100)
        return if (response.isSuccessful) response.body() ?: emptyList() else emptyList()
    }
    override suspend fun getLegalEventsPaged(skip: Int, limit: Int): Response<List<LegalEventDto>> {
        return apiService.getStudentLegalEvents(skip, limit)
    }

    // Treat a blank email as absent: registrations keyed on an empty string would
    // silently match nothing on read and corrupt records on write.
    private suspend fun currentStudentEmail(): String? = getProfile()?.email?.takeIf { it.isNotBlank() }

    override suspend fun getMyLegalEventRegistrations(): List<LegalEventRegistrationDto> {
        val email = currentStudentEmail() ?: return emptyList()
        val response = apiService.getLegalEventRegistrations(email)
        return if (response.isSuccessful) response.body() ?: emptyList() else emptyList()
    }

    override suspend fun registerForLegalEvent(eventId: String): Boolean {
        val profile = getProfile() ?: return false
        if (profile.email.isBlank()) return false
        return try {
            apiService.registerForLegalEvent(
                LegalEventRegistrationDto(eventId = eventId, studentEmail = profile.email, studentName = profile.fullName)
            ).isSuccessful
        } catch (e: Exception) {
            false
        }
    }

    override suspend fun getMyLegalEventQuestions(): List<LegalEventQuestionDto> {
        val email = currentStudentEmail() ?: return emptyList()
        val response = apiService.getLegalEventQuestions(email)
        return if (response.isSuccessful) response.body() ?: emptyList() else emptyList()
    }

    override suspend fun submitLegalEventQuestion(eventId: String, eventTitle: String, question: String, topic: String): Boolean {
        val profile = getProfile() ?: return false
        if (profile.email.isBlank()) return false
        return try {
            apiService.submitLegalEventQuestion(
                LegalEventQuestionDto(
                    id = "Q-${java.util.UUID.randomUUID().toString().takeLast(8)}",
                    eventId = eventId,
                    eventTitle = eventTitle,
                    studentEmail = profile.email,
                    studentName = profile.fullName,
                    question = question,
                    topic = topic,
                    status = "Pending",
                    submittedAt = java.time.LocalDate.now().toString()
                )
            ).isSuccessful
        } catch (e: Exception) {
            false
        }
    }


    override suspend fun getClubs(): List<ClubDto> {
        val response = apiService.getStudentClubs(0, 100)
        return if (response.isSuccessful) response.body() ?: emptyList() else emptyList()
    }
    override suspend fun getClubsPaged(skip: Int, limit: Int): Response<List<ClubDto>> {
        return apiService.getStudentClubs(skip, limit)
    }

    override suspend fun getClubAnnouncements(): List<ClubAnnouncementDto> {
        val response = apiService.getClubAnnouncements()
        return if (response.isSuccessful) response.body() ?: emptyList() else emptyList()
    }

    override suspend fun getSyllabus(): List<SyllabusDto> = apiService.getSyllabus().body() ?: emptyList()

    override suspend fun getSyllabusProgress(): Map<String, com.example.features.academics.models.SyllabusProgress> {
        val response = apiService.getSyllabusProgress()
        return if (response.isSuccessful) response.body() ?: emptyMap() else emptyMap()
    }

    override suspend fun getLessonPlanTracking(): List<com.example.features.academics.models.LessonPlanItem> {
        val response = apiService.getLessonPlanTracking()
        return if (response.isSuccessful) response.body() ?: emptyList() else emptyList()
    }
    override suspend fun getNotifications(): List<NotificationDto> {
        val response = apiService.getNotifications(0, 100)
        return if (response.isSuccessful) response.body() ?: emptyList() else emptyList()
    }
    
    override suspend fun getNotificationsPaged(skip: Int, limit: Int): Response<List<NotificationDto>> {
        return apiService.getNotifications(skip, limit)
    }

    override suspend fun markNotificationRead(id: String): Boolean {
        return try { apiService.markNotificationRead(id).isSuccessful } catch (e: Exception) { false }
    }

    override suspend fun deleteNotification(id: String): Boolean {
        return try { apiService.deleteNotification(id).isSuccessful } catch (e: Exception) { false }
    }

    override suspend fun markAllNotificationsRead(): Boolean {
        return try { apiService.markAllNotificationsRead().isSuccessful } catch (e: Exception) { false }
    }

    override suspend fun joinClub(id: String): ClubDto? {
        val response = apiService.joinClub(id)
        return if (response.isSuccessful) response.body() else null
    }
    override suspend fun leaveClub(id: String): ClubDto? {
        val response = apiService.leaveClub(id)
        return if (response.isSuccessful) response.body() else null
    }
    override suspend fun getCouncilData(): CouncilDataDto? = apiService.getCouncilData().body()

    override suspend fun submitCouncilProposal(title: String, description: String): Boolean {
        return try { apiService.submitCouncilProposal(CouncilProposalRequest(title, description)).isSuccessful } catch (e: Exception) { false }
    }

    override suspend fun submitCouncilFeedback(title: String): FeedbackDto? {
        val response = apiService.submitCouncilFeedback(CouncilFeedbackRequest(title))
        return if (response.isSuccessful) response.body() else null
    }

    override suspend fun upvoteCouncilFeedback(id: Int): FeedbackDto? {
        val response = apiService.upvoteCouncilFeedback(id)
        return if (response.isSuccessful) response.body() else null
    }
    override suspend fun submitGrievance(category: String, priority: String, subject: String, description: String) {
        apiService.raiseGrievance(GrievanceRaiseRequest(category, subject, description, priority))
    }
    override suspend fun getLexNovaStats(): List<LexNovaKpiDto> = apiService.getLexNovaStats().body() ?: emptyList()
    override suspend fun getInternshipDrives(): List<InternshipDriveDto> = apiService.getInternshipDrives().body() ?: emptyList()
    override suspend fun getAlumniNetwork(): List<AlumniMentorDto> = apiService.getAlumniNetwork().body() ?: emptyList()
    override suspend fun getInternshipsList(): List<InternshipDriveDto> {
        val response = apiService.getInternshipDrives()
        return if (response.isSuccessful) response.body() ?: emptyList() else emptyList()
    }

    override suspend fun getInternshipsListPaged(skip: Int, limit: Int): Response<List<InternshipDriveDto>> {
        val response = apiService.getInternshipDrives()
        if (!response.isSuccessful) return Response.error(response.code(), response.errorBody() ?: okhttp3.ResponseBody.create(null, ""))
        val all = response.body() ?: emptyList()
        return Response.success(all.drop(skip).take(limit))
    }

    override suspend fun getPersonalInternships(): List<com.example.features.campus_life.models.InternshipRecord> {
        val response = apiService.getStudentProfile()
        if (!response.isSuccessful) return emptyList()
        val dto = response.body() ?: return emptyList()
        return dto.internships?.map {
            com.example.features.campus_life.models.InternshipRecord(
                id = it.id,
                organization = it.organization,
                type = it.type,
                role = it.role,
                startDate = it.startDate,
                endDate = it.endDate,
                supervisor = it.supervisor,
                responsibilities = it.responsibilities,
                status = it.status,
                certificateUrl = it.certificateUrl
            )
        } ?: emptyList()
    }

    override suspend fun addInternship(internship: com.example.features.campus_life.models.InternshipRecord): Boolean {
        val current = getPersonalInternships()
        return savePersonalInternships(current + internship)
    }

    override suspend fun deleteInternship(id: String): Boolean {
        val current = getPersonalInternships()
        return savePersonalInternships(current.filter { it.id != id })
    }

    private suspend fun savePersonalInternships(list: List<com.example.features.campus_life.models.InternshipRecord>): Boolean {
        val profileResponse = apiService.getStudentProfile()
        if (!profileResponse.isSuccessful) return false
        val profileDto = profileResponse.body() ?: return false
        val updatedDto = profileDto.copy(
            internships = list.map {
                InternshipEntryDto(
                    id = it.id,
                    organization = it.organization,
                    type = it.type,
                    role = it.role,
                    startDate = it.startDate,
                    endDate = it.endDate,
                    supervisor = it.supervisor,
                    responsibilities = it.responsibilities,
                    status = it.status,
                    certificateUrl = it.certificateUrl
                )
            }
        )
        val response = apiService.updateStudentProfile(updatedDto)
        return response.isSuccessful
    }

    override suspend fun applyToInternshipDrive(driveId: String): InternshipApplicationResponseDto? {
        val response = apiService.applyToInternshipDrive(InternshipApplyRequestDto(driveId))
        return if (response.isSuccessful) response.body() else null
    }

    override suspend fun getInternshipApplications(): List<InternshipApplicationResponseDto> {
        val response = apiService.getInternshipApplications()
        return if (response.isSuccessful) response.body() ?: emptyList() else emptyList()
    }
    override suspend fun getCertificationsList(): List<CertificationRecordDto> = apiService.getCertifications().body() ?: emptyList()

    override suspend fun createCertification(title: String, issuer: String, date: String, category: String, type: String, fileUrl: String?): CertificationRecordDto? {
        val response = apiService.createCertification(CertificationCreateDto(title, issuer, date, category, type, fileUrl))
        return if (response.isSuccessful) response.body() else null
    }

    override suspend fun deleteCertification(id: String): Boolean {
        return try { apiService.deleteCertification(id).isSuccessful } catch (e: Exception) { false }
    }
    override suspend fun getActivityPoints(): List<ActivityPointDto> = apiService.getActivityPoints().body() ?: emptyList()

    override suspend fun claimActivityPoints(title: String, category: String, description: String, date: String, points: Double, supportingDocument: String?): ActivityPointDto? {
        val response = apiService.claimActivityPoints(ActivityPointClaimRequest(title, category, description, date, points, supportingDocument))
        return if (response.isSuccessful) response.body() else null
    }

    override suspend fun deleteActivityPoint(id: String): Boolean {
        return try { apiService.deleteActivityPoint(id).isSuccessful } catch (e: Exception) { false }
    }

    override suspend fun uploadActivityPointDocument(file: okhttp3.MultipartBody.Part): String? {
        val response = apiService.uploadActivityPointDocument(file)
        return if (response.isSuccessful) response.body()?.fileUrl else null
    }
    override suspend fun getServiceOpportunities(): List<ServiceOpportunityDto> =
        apiService.getServiceOpportunities().body() ?: emptyList()

    override suspend fun getServiceLogs(): List<ServiceLogDto> =
        apiService.getServiceLogs().body() ?: emptyList()

    override suspend fun applyToServiceOpportunity(id: Int): ServiceLogDto? {
        val response = apiService.applyToServiceOpportunity(id)
        return if (response.isSuccessful) response.body() else null
    }

    override suspend fun logServiceHours(title: String, organization: String, category: String, date: String, hours: Double, description: String, proofDocument: String?): ServiceLogDto? {
        val response = apiService.logServiceHours(LogServiceHoursRequest(title, organization, category, date, hours, description, proofDocument))
        return if (response.isSuccessful) response.body() else null
    }

    override suspend fun deleteServiceLog(id: String): Boolean {
        return try { apiService.deleteServiceLog(id).isSuccessful } catch (e: Exception) { false }
    }

    override suspend fun uploadServiceDocument(file: okhttp3.MultipartBody.Part): String? {
        val response = apiService.uploadServiceDocument(file)
        return if (response.isSuccessful) response.body()?.fileUrl else null
    }
    override suspend fun getInnovationProjects(): List<InnovationProjectDto> = apiService.getInnovationProjects().body() ?: emptyList()

    override suspend fun createInnovationProject(title: String, description: String, category: String, mentor: String, team: List<String>): InnovationProjectDto? {
        val response = apiService.createInnovationProject(InnovationProjectCreateRequest(title, description, category, mentor, team))
        return if (response.isSuccessful) response.body() else null
    }

    override suspend fun toggleInnovationProjectLike(id: String): InnovationProjectDto? {
        val response = apiService.toggleInnovationProjectLike(id)
        return if (response.isSuccessful) response.body() else null
    }

    override suspend fun addInnovationProjectComment(id: String, text: String): InnovationProjectDto? {
        val response = apiService.addInnovationProjectComment(id, InnovationCommentRequest(text))
        return if (response.isSuccessful) response.body() else null
    }

    override suspend fun deleteInnovationProject(id: String): Boolean {
        return try { apiService.deleteInnovationProject(id).isSuccessful } catch (e: Exception) { false }
    }

    override suspend fun getResearchPapers(): List<ResearchPaperDto> =
        apiService.getResearchPapers().body() ?: emptyList()

    override suspend fun submitResearchPaper(title: String, abstract: String, category: String, guide: String, team: List<String>, fileUrl: String?, fileSize: String?): ResearchPaperDto? {
        val response = apiService.submitResearchPaper(ResearchPaperSubmitRequest(title, abstract, category, guide, team, fileUrl, fileSize))
        return if (response.isSuccessful) response.body() else null
    }

    override suspend fun uploadResearchPaper(file: okhttp3.MultipartBody.Part): PaperUploadResponseDto? {
        val response = apiService.uploadResearchPaper(file)
        return if (response.isSuccessful) response.body() else null
    }
    
    override suspend fun changePassword(currentPassword: String, newPassword: String) {
        val response = apiService.changePassword(
            com.example.core.network.ChangePasswordRequest(currentPassword, newPassword)
        )
        if (!response.isSuccessful) {
            val detail = try {
                val body = response.errorBody()?.string()
                if (body.isNullOrBlank()) null
                else org.json.JSONObject(body).optString("detail", "").ifBlank { null }
            } catch (e: Exception) { null }
            throw Exception(detail ?: "Failed to change password (error ${response.code()})")
        }
    }

    override suspend fun getMootCourtMemorials(): List<MootCourtMemorialDto> {
        val response = apiService.getMootCourtMemorials()
        return if (response.isSuccessful) response.body() ?: emptyList() else emptyList()
    }

    override suspend fun createMootCourtMemorial(title: String, caseName: String?, content: String, status: String?): MootCourtMemorialDto? {
        val response = apiService.createMootCourtMemorial(CreateMemorialRequest(title, caseName, content, status))
        return if (response.isSuccessful) response.body() else null
    }

    override suspend fun updateMootCourtMemorial(id: String, title: String?, caseName: String?, content: String?, status: String?): MootCourtMemorialDto? {
        val response = apiService.updateMootCourtMemorial(id, UpdateMemorialRequest(title, caseName, content, status))
        return if (response.isSuccessful) response.body() else null
    }

    override suspend fun deleteMootCourtMemorial(id: String): Boolean {
        return apiService.deleteMootCourtMemorial(id).isSuccessful
    }

    override suspend fun getSavedCitations(): List<SavedCitationDto> {
        val response = apiService.getSavedCitations()
        return if (response.isSuccessful) response.body() ?: emptyList() else emptyList()
    }

    override suspend fun createSavedCitation(caseName: String, citationText: String, note: String?): SavedCitationDto? {
        val response = apiService.createSavedCitation(CreateCitationRequest(caseName, citationText, note))
        return if (response.isSuccessful) response.body() else null
    }

    override suspend fun deleteSavedCitation(id: String): Boolean {
        return apiService.deleteSavedCitation(id).isSuccessful
    }

    override suspend fun sendChatMessage(message: String, sessionId: String?): ChatMessageResponse? {
        val response = apiService.sendChatMessage(ChatMessageRequest(message, sessionId))
        return if (response.isSuccessful) response.body() else null
    }

    override suspend fun getChatHistory(): List<ChatSessionSummaryDto> {
        val response = apiService.getChatHistory()
        return if (response.isSuccessful) response.body() ?: emptyList() else emptyList()
    }

    override suspend fun getChatSessionMessages(sessionId: String): List<ChatMessageDto> {
        val response = apiService.getChatSessionMessages(sessionId)
        return if (response.isSuccessful) response.body() ?: emptyList() else emptyList()
    }

    override suspend fun deleteChatSession(sessionId: String): Boolean {
        return apiService.deleteChatSession(sessionId).isSuccessful
    }
}
