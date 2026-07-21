package com.example.core.repository

import com.example.core.network.CamsApiService
import com.example.features.hod.models.*
import java.io.IOException

class HODRepositoryImpl(private val apiService: CamsApiService) : HODRepository {
    override suspend fun getDashboardMetrics(): HODDashboardMetrics {
        val response = apiService.getHODDashboardMetrics()
        if (!response.isSuccessful) throw IOException("Failed to fetch HOD dashboard metrics: ${response.code()}")
        val dto = response.body() ?: throw IOException("Empty response body")
        val byId = dto.metrics.associateBy { it.id }
        return HODDashboardMetrics(
            departmentHealthIndex = byId["health"]?.value ?: "-",
            activeFacultyCount = byId["faculty"]?.value ?: "-",
            avgWorkloadHours = byId["workload"]?.value ?: "-",
            pendingVerifications = byId["pending_materials"]?.value ?: "-"
        )
    }

    override suspend fun getRecentActivities(): List<HODActivity> {
        val activities = mutableListOf<HODActivity>()
        try {
            val leaves = apiService.getHODPendingLeaves().body() ?: emptyList()
            leaves.take(5).forEach { leave ->
                activities.add(HODActivity(
                    title = "${leave.userName ?: "Faculty"} applied for ${leave.type}",
                    time = leave.startDate,
                    type = "leave"
                ))
            }
        } catch (e: Exception) { /* leave requests unavailable, continue with what we have */ }
        try {
            val proofs = apiService.getHODPendingProofs().body() ?: emptyList()
            proofs.take(5).forEach { proof ->
                activities.add(HODActivity(
                    title = "${proof.faculty_name}: \"${proof.title}\" pending verification",
                    time = "",
                    type = "research"
                ))
            }
        } catch (e: Exception) { /* research proofs unavailable, continue with what we have */ }
        return activities
    }

    override suspend fun getActiveFaculty(): List<com.example.core.network.HODFacultyResponseDto> {
        val response = apiService.getHODActiveFaculty()
        if (response.isSuccessful) return response.body() ?: emptyList()
        throw IOException("Failed to fetch faculty list: ${response.code()}")
    }

    override suspend fun getHODManagementStudents(): com.example.core.network.HODManagementStudentsDto {
        val response = apiService.getHODManagementStudents()
        if (response.isSuccessful) return response.body() ?: com.example.core.network.HODManagementStudentsDto()
        throw IOException("Failed to fetch student roster: ${response.code()}")
    }

    override suspend fun verifyStudentProfile(studentId: String, action: String, remarks: String?): Boolean {
        val response = apiService.verifyHODStudentProfile(studentId, com.example.core.network.StudentVerifyRequest(action, remarks))
        if (response.isSuccessful) return true
        throw IOException("Failed to verify student: ${response.code()}")
    }

    override suspend fun getPendingLeaveApprovals(): List<com.example.core.network.LeaveRequestDto> {
        val response = apiService.getHODPendingLeaves()
        if (response.isSuccessful) return response.body() ?: emptyList()
        throw IOException("Failed to fetch pending leave approvals: ${response.code()}")
    }

    override suspend fun approveLeave(id: String, status: String, remarks: String?) {
        try {
            val request = com.example.core.network.ApprovalRequest(status = status, remarks = remarks ?: "")
            val response = apiService.approveHODLeave(id, request)
            if (!response.isSuccessful) {
                throw IOException("Failed to approve leave: ${response.message()}")
            }
        } catch (e: IOException) {
            throw e
        } catch (e: Exception) {
            throw IOException("Failed to approve leave: ${e.message}")
        }
    }

    override suspend fun getTimetableMetadata(): com.example.core.network.HODTimetableMetadataDto {
        return try {
            val response = apiService.getHODTimetableMetadata()
            if (response.isSuccessful) {
                response.body() ?: throw IOException("Empty response body")
            } else {
                throw IOException("Failed to fetch timetable metadata: ${response.code()}")
            }
        } catch (e: IOException) {
            throw e
        } catch (e: Exception) {
            throw IOException("Failed to fetch timetable metadata: ${e.message}")
        }
    }

    override suspend fun getTimetableSection(sectionId: String): List<com.example.core.network.TimetableSlotDto> {
        val response = apiService.getHODTimetableSection(sectionId)
        if (response.isSuccessful) return response.body() ?: emptyList()
        throw IOException("Failed to fetch timetable section: ${response.code()}")
    }

    override suspend fun submitTimetable(sectionId: String, slots: List<com.example.core.network.TimetableSlotInputDto>) {
        val response = apiService.submitHodTimetable(com.example.core.network.TimetableSubmitRequestDto(sectionId, slots))
        if (!response.isSuccessful) throw IOException("Failed to submit timetable: ${response.code()}")
    }

    override suspend fun getTeachingLogsDashboard(): com.example.core.network.HODTeachingLogsDashboardDto {
        return try {
            val response = apiService.getHODTeachingLogsDashboard()
            if (response.isSuccessful) {
                response.body() ?: throw IOException("Empty response body")
            } else {
                throw IOException("Failed to fetch teaching logs dashboard: ${response.code()}")
            }
        } catch (e: IOException) {
            throw e
        } catch (e: Exception) {
            throw IOException("Failed to fetch teaching logs dashboard: ${e.message}")
        }
    }

    override suspend fun getSyllabusMetadata(): com.example.core.network.HODSyllabusMetadataDto {
        return try {
            val response = apiService.getHODSyllabusMetadata()
            if (response.isSuccessful) {
                response.body() ?: throw IOException("Empty response body")
            } else {
                throw IOException("Failed to fetch syllabus metadata: ${response.code()}")
            }
        } catch (e: IOException) {
            throw e
        } catch (e: Exception) {
            throw IOException("Failed to fetch syllabus metadata: ${e.message}")
        }
    }

    override suspend fun getSyllabusCourses(): List<com.example.core.network.HODCourseDto> {
        val response = apiService.getHODSyllabusCourses()
        if (response.isSuccessful) return response.body() ?: emptyList()
        throw IOException("Failed to fetch syllabus courses: ${response.code()}")
    }

    override suspend fun getCoursePlan(courseName: String): Map<String, List<String>> {
        val response = apiService.getHODCoursePlan(courseName)
        if (response.isSuccessful) return response.body() ?: emptyMap()
        throw IOException("Failed to fetch course plan: ${response.code()}")
    }

    override suspend fun saveCoursePlan(courseName: String, units: Map<String, List<String>>) {
        val response = apiService.saveHODCoursePlan(courseName, com.example.core.network.HODLessonPlanSaveRequest(units))
        if (!response.isSuccessful) throw IOException("Failed to save course plan: ${response.code()}")
    }

    override suspend fun getAttendanceMonitoring(): List<com.example.core.network.HODAttendanceMonitoringDto> {
        val response = apiService.getHODAttendanceMonitoring()
        if (response.isSuccessful) return response.body() ?: emptyList()
        throw IOException("Failed to fetch attendance monitoring: ${response.code()}")
    }

    override suspend fun getPendingEntries(): List<com.example.core.network.HODPendingEntryDto> {
        val response = apiService.getHODPendingEntries()
        if (response.isSuccessful) return response.body() ?: emptyList()
        throw IOException("Failed to fetch pending entries: ${response.code()}")
    }

    override suspend fun getDepartmentReports(): com.example.core.network.HODDepartmentReportDto {
        return try {
            val response = apiService.getHODDepartmentReports()
            if (response.isSuccessful) {
                response.body() ?: throw IOException("Empty response body")
            } else {
                throw IOException("Failed to fetch department reports: ${response.code()}")
            }
        } catch (e: IOException) {
            throw e
        } catch (e: Exception) {
            throw IOException("Failed to fetch department reports: ${e.message}")
        }
    }

    override suspend fun getStudentReports(): com.example.core.network.HODStudentReportDto {
        return try {
            val response = apiService.getHODStudentReports()
            if (response.isSuccessful) {
                response.body() ?: throw IOException("Empty response body")
            } else {
                throw IOException("Failed to fetch student reports: ${response.code()}")
            }
        } catch (e: IOException) {
            throw e
        } catch (e: Exception) {
            throw IOException("Failed to fetch student reports: ${e.message}")
        }
    }

    override suspend fun exportDepartmentReportCsv(): String {
        val response = apiService.exportHODDepartmentReportCsv()
        if (response.isSuccessful) return response.body()?.string() ?: ""
        throw IOException("Failed to export department report: ${response.code()}")
    }

    override suspend fun exportStudentReportCsv(): String {
        val response = apiService.exportHODStudentReportCsv()
        if (response.isSuccessful) return response.body()?.string() ?: ""
        throw IOException("Failed to export student report: ${response.code()}")
    }

    override suspend fun getCalendarEvents(): List<com.example.core.network.HODCalendarEventDto> {
        val response = apiService.getHodCalendarEvents()
        if (response.isSuccessful) return response.body() ?: emptyList()
        throw IOException("Failed to fetch calendar events: ${response.code()}")
    }

    override suspend fun createCalendarEvent(request: com.example.core.network.HODCalendarEventCreateRequest): com.example.core.network.HODCalendarEventDto {
        val response = apiService.createHodCalendarEvent(request)
        if (response.isSuccessful) return response.body() ?: throw IOException("Empty response body")
        throw IOException("Failed to create calendar event: ${response.code()}")
    }

    override suspend fun deleteCalendarEvent(eventId: String) {
        val response = apiService.deleteHodCalendarEvent(eventId)
        if (!response.isSuccessful) throw IOException("Failed to delete calendar event: ${response.code()}")
    }

    override suspend fun getResearchMonitoring(): List<com.example.core.network.HODResearchMonitoringDto> {
        val response = apiService.getHODResearchMonitoring()
        if (response.isSuccessful) return response.body() ?: emptyList()
        throw IOException("Failed to fetch research monitoring: ${response.code()}")
    }

    override suspend fun getPendingProofs(): List<com.example.core.network.HODPendingProofDto> {
        val response = apiService.getHODPendingProofs()
        if (response.isSuccessful) return response.body() ?: emptyList()
        throw IOException("Failed to fetch pending proofs: ${response.code()}")
    }

    override suspend fun verifyResearchProof(proofId: String, request: com.example.core.network.VerificationRequestDto): Map<String, Any> {
        return try {
            val response = apiService.verifyResearchProof(proofId, request)
            if (response.isSuccessful) {
                response.body() ?: emptyMap()
            } else {
                throw IOException("Failed to verify research proof: ${response.code()}")
            }
        } catch (e: IOException) {
            throw e
        } catch (e: Exception) {
            throw IOException("Failed to verify research proof: ${e.message}")
        }
    }

    override suspend fun getHODWorkloads(): List<com.example.core.network.HODWorkloadDto> {
        val response = apiService.getHODWorkloads()
        if (response.isSuccessful) return response.body() ?: emptyList()
        throw IOException("Failed to fetch faculty workloads: ${response.code()}")
    }

    override suspend fun getHODPendingMarks(): List<com.example.core.network.HODPendingMarksGroupDto> {
        val response = apiService.getHODPendingMarks()
        if (response.isSuccessful) return response.body() ?: emptyList()
        throw IOException("Failed to fetch pending marks: ${response.code()}")
    }

    override suspend fun approveHODMarks(sectionId: String, subjectId: String, academicYear: String) {
        val response = apiService.approveHODMarks(com.example.core.network.ApproveMarksGroupRequest(sectionId, subjectId, academicYear))
        if (!response.isSuccessful) throw IOException("Failed to approve marks: ${response.code()}")
    }

    override suspend fun getHODMentors(): com.example.core.network.HODMentorsPayloadDto {
        val response = apiService.getHODMentors()
        if (response.isSuccessful) return response.body() ?: com.example.core.network.HODMentorsPayloadDto()
        throw IOException("Failed to fetch mentors: ${response.code()}")
    }

    override suspend fun getHodPendingMaterials(): List<com.example.core.network.HodPendingMaterialDto> {
        val response = apiService.getHodPendingMaterials()
        if (response.isSuccessful) return response.body() ?: emptyList()
        throw IOException("Failed to fetch pending materials: ${response.code()}")
    }

    override suspend fun reviewHodMaterial(materialId: String, status: String, remarks: String) {
        val response = apiService.reviewHodMaterial(materialId, com.example.core.network.MaterialReviewRequest(status, remarks))
        if (!response.isSuccessful) throw IOException("Failed to review material: ${response.code()}")
    }

    override suspend fun getHodClasses(): com.example.core.network.ClassAdvisorSetupDto {
        val response = apiService.getHodClasses()
        if (response.isSuccessful) return response.body() ?: com.example.core.network.ClassAdvisorSetupDto()
        throw IOException("Failed to fetch class advisor setup: ${response.code()}")
    }

    override suspend fun assignClassAdvisor(academicYearId: String, batch: String, sectionName: String, facultyId: String) {
        val response = apiService.assignClassAdvisor(com.example.core.network.AdvisorAssignmentRequestDto(academicYearId, batch, sectionName, facultyId))
        if (!response.isSuccessful) throw IOException("Failed to assign class advisor: ${response.code()}")
    }

    override suspend fun getAttendanceCorrectionRequests(): List<com.example.core.network.AttendanceCorrectionDto> {
        val response = apiService.getAttendanceCorrectionRequests()
        if (response.isSuccessful) return response.body() ?: emptyList()
        throw IOException("Failed to fetch attendance correction requests: ${response.code()}")
    }

    override suspend fun approveAttendanceCorrection(requestId: String) {
        val response = apiService.approveAttendanceCorrection(requestId)
        if (!response.isSuccessful) throw IOException("Failed to approve correction: ${response.code()}")
    }

    override suspend fun rejectAttendanceCorrection(requestId: String, remarks: String) {
        val response = apiService.rejectAttendanceCorrection(requestId, com.example.core.network.RejectCorrectionRequestDto(remarks))
        if (!response.isSuccessful) throw IOException("Failed to reject correction: ${response.code()}")
    }

    override suspend fun getPendingProfileUpdateRequests(): List<com.example.core.network.FacultyProfileUpdateRequestDto> {
        val response = apiService.getPendingProfileUpdateRequests()
        if (response.isSuccessful) return response.body() ?: emptyList()
        throw IOException("Failed to fetch pending profile requests: ${response.code()}")
    }

    override suspend fun reviewProfileUpdateRequest(requestId: String, action: String, comments: String?) {
        val request = com.example.core.network.ProfileUpdateReviewRequest(action, comments)
        val response = when (action) {
            "APPROVED" -> apiService.approveProfileUpdateRequest(requestId, request)
            "REJECTED" -> apiService.rejectProfileUpdateRequest(requestId, request)
            else -> apiService.requestChangesProfileUpdateRequest(requestId, request)
        }
        if (!response.isSuccessful) throw IOException("Failed to review profile request: ${response.code()}")
    }

    override suspend fun assignHODMentor(facultyId: String, studentIds: List<String>) {
        val response = apiService.assignHODMentor(com.example.core.network.MentorAssignmentRequestDto(facultyId, studentIds))
        if (!response.isSuccessful) throw IOException("Failed to assign mentor: ${response.code()}")
    }

    override suspend fun getAcademicSetup(): Result<com.example.core.network.AcademicSetupDto> {
        return try {
            val response = apiService.getAcademicSetup()
            val body = response.body()
            if (response.isSuccessful && body != null) {
                Result.success(body)
            } else {
                Result.failure(Exception("Failed or empty"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun getSubjectAllocations(): Result<List<com.example.core.network.SubjectAllocationDto>> {
        return try {
            val response = apiService.getSubjectAllocations()
            val body = response.body()
            if (response.isSuccessful && body != null) {
                Result.success(body)
            } else {
                Result.failure(Exception("Failed or empty"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun getSubstitutions(): Result<List<com.example.core.network.HODSubstitutionDto>> {
        return try {
            val response = apiService.getSubstitutions()
            if (response.isSuccessful) {
                val body = response.body()
                if (body != null) Result.success(body) else Result.success(emptyList())
            } else {
                Result.failure(Exception("Failed: ${response.code()}"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun getCourseSections(courseId: String): Result<List<com.example.core.network.AcademicSetupSectionDto>> {
        return try {
            val response = apiService.getCourseSections(courseId)
            val body = response.body()
            if (response.isSuccessful && body != null) Result.success(body) else Result.failure(Exception("Failed to fetch sections: ${response.code()}"))
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun getAllocationSubjects(): Result<List<com.example.core.network.SubjectInfoDto>> {
        return try {
            val response = apiService.getAllocationSubjects()
            val body = response.body()
            if (response.isSuccessful && body != null) Result.success(body) else Result.failure(Exception("Failed to fetch subjects: ${response.code()}"))
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun getAllocationFaculty(): Result<List<com.example.core.network.FacultyWorkloadInfoDto>> {
        return try {
            val response = apiService.getAllocationFaculty()
            val body = response.body()
            if (response.isSuccessful && body != null) Result.success(body) else Result.failure(Exception("Failed to fetch faculty: ${response.code()}"))
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun allocateSubjects(allocations: List<com.example.core.network.SubjectAllocationCreateDto>): Result<Unit> {
        return try {
            val response = apiService.allocateSubjects(allocations)
            if (response.isSuccessful) Result.success(Unit) else Result.failure(Exception("Failed to allocate: ${response.code()}"))
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun getAvailableSubstituteFaculty(): List<com.example.core.network.HODFacultyResponseDto> {
        val response = apiService.getAvailableSubstituteFaculty()
        if (response.isSuccessful) return response.body() ?: emptyList()
        throw IOException("Failed to fetch available faculty: ${response.code()}")
    }

    override suspend fun assignSubstitution(
        absentFacultyId: String, absentFacultyName: String,
        substituteFacultyId: String, substituteFacultyName: String,
        subject: String, section: String, date: String, periodLabel: String
    ) {
        val response = apiService.assignSubstitution(
            com.example.core.network.SubstitutionAssignRequest(
                absentFacultyId, absentFacultyName, substituteFacultyId, substituteFacultyName,
                subject, section, date, periodLabel
            )
        )
        if (!response.isSuccessful) throw IOException("Failed to assign substitution: ${response.code()}")
    }
}
