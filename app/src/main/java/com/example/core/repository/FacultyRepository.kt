package com.example.core.repository

import com.example.features.faculty.models.*
import com.example.features.parent.models.CollegeNotice
import com.example.features.parent.models.TimetableDay
import com.example.core.network.*

interface FacultyRepository {
    suspend fun getDashboardMetrics(): FacultyDashboardMetrics
    suspend fun getAssignedSubjects(): List<FacultySubject>
    suspend fun getProfile(): FacultyProfile
    suspend fun updateProfile(update: com.example.core.network.FacultyProfileUpdateRequest): FacultyProfile
    suspend fun getResearchEntries(): List<ResearchEntry>
    suspend fun createResearchEntry(request: com.example.core.network.ResearchEntryRequest): com.example.core.network.ResearchEntryDto
    suspend fun updateResearchEntry(researchId: String, request: com.example.core.network.ResearchEntryRequest): com.example.core.network.ResearchEntryDto
    suspend fun deleteResearchEntry(researchId: String): Boolean
    suspend fun getActivitySummary(): ActivitySummary
    suspend fun getTimetable(): List<TimetableDay>
    suspend fun getNotices(): List<CollegeNotice>
    suspend fun getOnlineMeetings(): List<OnlineMeetingDto>
    suspend fun createOnlineMeeting(request: CreateMeetingRequest): Boolean
    suspend fun deleteOnlineMeeting(meetingId: String): Boolean
    suspend fun getAssignments(): List<FacultyAssignmentDto>
    suspend fun createAssignment(request: CreateAssignmentRequest): FacultyAssignmentDto
    suspend fun updateAssignment(assignmentId: String, request: CreateAssignmentRequest): FacultyAssignmentDto
    suspend fun deleteAssignment(assignmentId: String): Boolean
    suspend fun getAssignmentSubmissions(): List<FacultyAssignmentSubmissionDto>
    suspend fun gradeSubmission(submissionId: String, request: GradeSubmissionRequest): FacultyAssignmentSubmissionDto
    suspend fun getStudents(semester: Int? = null): List<FacultyStudentDto>
    suspend fun getAttendanceSections(): List<FacultyAttendanceSectionDto>
    suspend fun getAttendanceStudents(sectionId: String, subjectId: String): List<FacultyAttendanceStudentDto>
    suspend fun markAttendanceBulk(request: BulkAttendanceMarkRequest): Boolean
    suspend fun getClassroomActivities(): List<ClassroomActivityDto>
    suspend fun createClassroomActivity(request: CreateClassroomActivityRequest): Boolean
    suspend fun getClassroomInteractions(): List<StudentInteractionDto>
    suspend fun createClassroomInteraction(request: CreateInteractionRequest): Boolean
    suspend fun getSessionSummaries(): List<SessionSummaryDto>
    suspend fun createSessionSummary(request: CreateSessionSummaryRequest): Boolean
    suspend fun getInternalMarks(sectionId: String, subjectId: String, academicYear: String?): List<InternalMarkStudentDto>
    suspend fun saveInternalMarks(request: SaveInternalMarksRequest): Boolean
    suspend fun submitInternalMarks(request: SubmitMarksRequest): Boolean
    suspend fun getStudyMaterials(): List<FacultyMaterialDto>
    suspend fun uploadMaterialFile(file: okhttp3.MultipartBody.Part): FileUploadResponseDto
    suspend fun uploadStudyMaterial(payload: UploadMaterialRequestDto): FacultyMaterialDto
    suspend fun editStudyMaterial(materialId: String, payload: UploadMaterialRequestDto): FacultyMaterialDto
    suspend fun archiveStudyMaterial(materialId: String): Boolean

    suspend fun getLectureRecordings(): List<FacultyRecordingDto>
    suspend fun createRecording(request: CreateRecordingRequest): Boolean
    suspend fun deleteRecording(recordingId: String): Boolean
    suspend fun getMentorStudents(): List<FacultyMentorshipStudentDto>
    suspend fun getMentorStudentRecord(studentId: String): FacultyMentorshipRecordDto?
    suspend fun saveMentorStudentRecord(studentId: String, payload: FacultyMentorshipRecordDto): FacultyMentorshipRecordDto?
    suspend fun getFacultySalarySlips(): List<FacultySalarySlipDto>
    suspend fun getInternshipDrives(): List<FacultyInternshipDriveDto>
    suspend fun getInternshipApplications(): List<InternshipApplicationDto>
    suspend fun reviewInternshipApplication(applicationId: String, status: String): InternshipApplicationDto
    suspend fun getPartnerCompanies(): List<PartnerCompanyDto>
    suspend fun createPartnerCompany(request: PartnerCompanyRequest): PartnerCompanyDto
    suspend fun updatePartnerCompany(partnerId: String, request: PartnerCompanyRequest): PartnerCompanyDto
    suspend fun deletePartnerCompany(partnerId: String): Boolean
    suspend fun getLegalEvents(): List<FacultyLegalEventDto>
    suspend fun postLegalEvent(request: CreateLegalEventRequest): Boolean
    suspend fun getFacultyNotifications(): List<NotificationDto>
    suspend fun markFacultyNotificationRead(notificationId: String): Boolean
    suspend fun markAllFacultyNotificationsRead(): Boolean
    suspend fun getAcademicCalendar(): List<CalendarEventDto>
    suspend fun getLeaveBalances(): LeaveBalanceDto
    suspend fun getLeaveHistory(): List<LeaveRequestDto>
    suspend fun applyForLeave(type: String, fromDate: String, toDate: String, reason: String, emergencyContact: String): LeaveRequestDto
    suspend fun cancelLeave(leaveId: String): Boolean
    suspend fun getClassDiaries(): List<ClassDiaryDto>
    suspend fun createClassDiary(request: ClassDiaryRequest): ClassDiaryDto
    suspend fun updateClassDiary(id: String, request: ClassDiaryRequest): ClassDiaryDto
    suspend fun reviewClassDiary(id: String, hodStatus: String, hodRemarks: String?): ClassDiaryDto
    suspend fun getAdvisorAssignment(): AdvisorAssignmentDto
    suspend fun getAdvisorClassStudents(): List<AdvisorStudentDto>
    suspend fun getAdvisorStudentLeaves(): List<AdvisorLeaveDto>
    suspend fun advisorApproveLeave(leaveId: String, status: String, remarks: String?): AdvisorLeaveDto
    suspend fun getMessageContacts(): List<MessageContactDto>
    suspend fun getConversations(): List<ConversationDto>
    suspend fun getMessageThread(userId: String): List<MessageDto>
    suspend fun sendMessage(receiverId: String, body: String): MessageDto
    suspend fun markThreadRead(userId: String): Boolean
    suspend fun getFacultyActivityPoints(): List<ActivityPointDto>
    suspend fun reviewActivityPoints(applicationId: String, status: String, approvedPoints: Double, remarks: String?): ActivityPointDto
    suspend fun getActivityPointCategories(): List<ActivityPointCategoryDto>
    suspend fun createActivityPointCategory(request: ActivityPointCategoryRequest): ActivityPointCategoryDto
    suspend fun updateActivityPointCategory(categoryId: String, request: ActivityPointCategoryRequest): ActivityPointCategoryDto
    suspend fun deleteActivityPointCategory(categoryId: String): Boolean
}
