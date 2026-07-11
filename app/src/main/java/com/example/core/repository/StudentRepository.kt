package com.example.core.repository

import com.example.core.network.*
import com.example.features.student.models.*
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import retrofit2.Response

interface StudentRepository {
    suspend fun getDashboard(): DashboardResponse?
    suspend fun getProfile(): StudentProfileResponse?
    suspend fun getAttendance(): AttendanceSummaryResponse?
    suspend fun getInternalMarks(): List<InternalMarkDto>
    suspend fun getTimetable(): List<TimetableSlotDto>
    suspend fun getFees(): StudentFeeSummaryResponse?
    suspend fun getStudyMaterials(): List<StudyMaterialDto>
    suspend fun getAssignments(): List<AssignmentDto>
    suspend fun submitAssignment(asgId: String, file: String?, text: String?): Boolean
    suspend fun getLeaves(): List<LeaveRequestDto>
    suspend fun applyLeave(type: String, fromDate: String, toDate: String, reason: String): Boolean
    suspend fun getNotices(): List<NoticeDto>
    suspend fun getAcademicCalendar(): List<CalendarEventDto>
    suspend fun getGrievances(): List<GrievanceDto>
    suspend fun raiseGrievance(category: String, description: String): Boolean
    suspend fun getOnlineMeetings(): List<OnlineMeetingDto>
    suspend fun getLegalEvents(): List<LegalEventDto>
    suspend fun getClubs(): List<ClubDto>
    suspend fun getSyllabus(): List<SyllabusDto>
    suspend fun getNotifications(): List<NotificationDto>
    suspend fun joinClub(id: Int)
    suspend fun getCouncilData(): CouncilDataDto?
    suspend fun submitGrievance(category: String, priority: String, subject: String, description: String)
    suspend fun getLexNovaStats(): List<LexNovaKpiDto>
    suspend fun getInternshipDrives(): List<InternshipDriveDto>
    suspend fun getInternshipsList(): List<InternshipRecordDto>
    suspend fun getCertificationsList(): List<CertificationRecordDto>
    suspend fun getActivityPoints(): List<ActivityPointDto>
    suspend fun claimActivityPoints(title: String, category: String, description: String, points: Int): Boolean
    suspend fun getCommunityServiceData(): CommunityServiceDataDto?
    suspend fun getInnovationProjects(): List<InnovationProjectDto>
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
                    id = "current",
                    fullName = dto.fullName ?: "",
                    rollNo = dto.aadhaarNo ?: "", // Fallback
                    email = "", // Missing in dto
                    semester = dto.semester ?: 1,
                    batchYear = 2023, // Fallback
                    mentorName = null,
                    mentorEmail = null,
                    mentorPhone = null,
                    cgpa = null,
                    skills = null,
                    courseName = dto.department,
                    section = "A",
                    classAdvisorName = null,
                    classAdvisorEmail = null,
                    classAdvisorPhone = null,
                    batch = null,
                    yearOfStudy = null,
                    departmentName = dto.department,
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
                    motherName = dto.motherName
                )
            } else null
        } else null
    }

    override suspend fun getAttendance(): AttendanceSummaryResponse? {
        val response = apiService.getAttendance()
        return if (response.isSuccessful) response.body() else null
    }

    override suspend fun getInternalMarks(): List<InternalMarkDto> {
        val response = apiService.getInternalMarks()
        return if (response.isSuccessful) response.body() ?: emptyList() else emptyList()
    }

    override suspend fun getTimetable(): List<TimetableSlotDto> {
        val response = apiService.getTimetable()
        return if (response.isSuccessful) response.body() ?: emptyList() else emptyList()
    }

    override suspend fun getFees(): StudentFeeSummaryResponse? {
        val response = apiService.getFees()
        return if (response.isSuccessful) response.body() else null
    }

    override suspend fun getStudyMaterials(): List<StudyMaterialDto> {
        val response = apiService.getStudyMaterials()
        return if (response.isSuccessful) response.body() ?: emptyList() else emptyList()
    }

    override suspend fun getAssignments(): List<AssignmentDto> {
        val response = apiService.getAssignments()
        return if (response.isSuccessful) response.body() ?: emptyList() else emptyList()
    }

    override suspend fun submitAssignment(asgId: String, file: String?, text: String?): Boolean {
        val response = apiService.submitAssignment(asgId, AssignmentSubmitRequest(file, text))
        return response.isSuccessful
    }

    override suspend fun getLeaves(): List<LeaveRequestDto> {
        val response = apiService.getLeaves()
        return if (response.isSuccessful) response.body() ?: emptyList() else emptyList()
    }

    override suspend fun applyLeave(type: String, fromDate: String, toDate: String, reason: String): Boolean {
        val response = apiService.applyLeave(LeaveApplicationRequest(type = type, fromDate = fromDate, toDate = toDate, reason = reason))
        return response.isSuccessful
    }

    override suspend fun getNotices(): List<NoticeDto> {
        val response = apiService.getNotices()
        return if (response.isSuccessful) response.body() ?: emptyList() else emptyList()
    }

    override suspend fun getAcademicCalendar(): List<CalendarEventDto> {
        val response = apiService.getAcademicCalendar()
        return if (response.isSuccessful) response.body() ?: emptyList() else emptyList()
    }

    override suspend fun getGrievances(): List<GrievanceDto> = apiService.getStudentGrievances().body() ?: emptyList()

    override suspend fun raiseGrievance(category: String, description: String): Boolean {
        val response = apiService.raiseGrievance(GrievanceRaiseRequest(category, "General", description))
        return response.isSuccessful
    }
    override suspend fun getOnlineMeetings(): List<OnlineMeetingDto> = apiService.getStudentMeetings().body() ?: emptyList()
    override suspend fun getLegalEvents(): List<LegalEventDto> = apiService.getStudentLegalEvents().body() ?: emptyList()
    override suspend fun getClubs(): List<ClubDto> = apiService.getStudentClubs().body() ?: emptyList()
    override suspend fun getSyllabus(): List<SyllabusDto> = apiService.getSyllabus().body() ?: emptyList()
    override suspend fun getNotifications(): List<NotificationDto> = apiService.getNotifications().body() ?: emptyList()
    override suspend fun joinClub(id: Int) { apiService.joinClub(id) }
    override suspend fun getCouncilData(): CouncilDataDto? = apiService.getCouncilData().body()
    override suspend fun submitGrievance(category: String, priority: String, subject: String, description: String) {
        apiService.raiseGrievance(GrievanceRaiseRequest(category, subject, description, priority))
    }
    override suspend fun getLexNovaStats(): List<LexNovaKpiDto> = apiService.getLexNovaStats().body() ?: emptyList()
    override suspend fun getInternshipDrives(): List<InternshipDriveDto> = apiService.getInternshipDrives().body() ?: emptyList()
    override suspend fun getInternshipsList(): List<InternshipRecordDto> = apiService.getInternships().body() ?: emptyList()
    override suspend fun getCertificationsList(): List<CertificationRecordDto> = apiService.getCertifications().body() ?: emptyList()
    override suspend fun getActivityPoints(): List<ActivityPointDto> = apiService.getActivityPoints().body() ?: emptyList()

    override suspend fun claimActivityPoints(title: String, category: String, description: String, points: Int): Boolean {
        val response = apiService.claimActivityPoints(ActivityPointClaimRequest(title, category, description, points))
        return response.isSuccessful
    }
    override suspend fun getCommunityServiceData(): CommunityServiceDataDto? = apiService.getCommunityService().body()
    override suspend fun getInnovationProjects(): List<InnovationProjectDto> = apiService.getInnovationProjects().body() ?: emptyList()
}
