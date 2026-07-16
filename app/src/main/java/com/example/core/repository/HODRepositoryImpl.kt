package com.example.core.repository

import com.example.core.network.CamsApiService
import com.example.features.hod.models.*
import java.io.IOException

class HODRepositoryImpl(private val apiService: CamsApiService) : HODRepository {
    override suspend fun getDashboardMetrics(): HODDashboardMetrics {
        val response = apiService.getHODDashboardMetrics()
        if (response.isSuccessful) {
            val dto = response.body()!!
            return HODDashboardMetrics(
                totalFaculty = dto.totalFaculty,
                totalStudents = dto.totalStudents,
                pendingApprovals = dto.pendingApprovals,
                activeSubjects = dto.activeSubjects
            )
        }
        throw IOException("Failed to fetch HOD dashboard metrics")
    }

    override suspend fun getRecentActivities(): List<HODActivity> {
        val response = apiService.getHODActivities()
        if (response.isSuccessful) {
            return response.body()!!.map { dto ->
                HODActivity(
                    title = dto.title,
                    time = dto.time,
                    type = dto.type
                )
            }
        }
        throw IOException("Failed to fetch HOD activities")
    }

    override suspend fun getFacultyManagementData(): List<com.example.core.network.FacultyStudentDto> {
        val response = apiService.getFacultyStudents()
        if (response.isSuccessful) return response.body()!!
        throw IOException("Failed to fetch faculty management data")
    }

    override suspend fun getStudentManagementData(): List<com.example.core.network.FacultyStudentDto> {
        val response = apiService.getFacultyStudents()
        if (response.isSuccessful) return response.body()!!
        throw IOException("Failed to fetch student management data")
    }

    override suspend fun getPendingLeaveApprovals(): List<com.example.core.network.LeaveRequestDto> {
        val response = apiService.getHODPendingLeaves()
        if (response.isSuccessful) return response.body() ?: emptyList()
        throw IOException("Failed to fetch pending leave approvals")
    }

    override suspend fun approveLeave(id: String, status: String, remarks: String?) {
        val request = com.example.core.network.ApprovalRequest(status = status, remarks = remarks ?: "")
        val response = apiService.approveHODLeave(id, request)
        if (!response.isSuccessful) {
            throw IOException("Failed to approve leave: ${response.message()}")
        }
    }

    override suspend fun getTimetableMetadata(): com.example.core.network.HODTimetableMetadataDto {
        val response = apiService.getHODTimetableMetadata()
        if (response.isSuccessful) return response.body()!!
        throw IOException("Failed to fetch timetable metadata")
    }

    override suspend fun getTimetableSection(sectionId: String): List<com.example.core.network.TimetableSlotDto> {
        val response = apiService.getHODTimetableSection(sectionId)
        if (response.isSuccessful) return response.body() ?: emptyList()
        throw IOException("Failed to fetch timetable slots")
    }

    override suspend fun getTeachingLogsDashboard(): com.example.core.network.HODTeachingLogsDashboardDto {
        val response = apiService.getHODTeachingLogsDashboard()
        if (response.isSuccessful) return response.body()!!
        throw java.io.IOException("Failed to fetch teaching logs dashboard")
    }

    override suspend fun getSyllabusMetadata(): com.example.core.network.HODSyllabusMetadataDto {
        val response = apiService.getHODSyllabusMetadata()
        if (response.isSuccessful) return response.body()!!
        throw java.io.IOException("Failed to fetch syllabus metadata")
    }

    override suspend fun getSyllabusCourses(): List<com.example.core.network.HODCourseDto> {
        val response = apiService.getHODSyllabusCourses()
        if (response.isSuccessful) return response.body() ?: emptyList()
        throw java.io.IOException("Failed to fetch syllabus courses")
    }

    override suspend fun getAttendanceMonitoring(): List<com.example.core.network.HODAttendanceMonitoringDto> {
        val response = apiService.getHODAttendanceMonitoring()
        if (response.isSuccessful) return response.body() ?: emptyList()
        throw java.io.IOException("Failed to fetch attendance monitoring data")
    }

    override suspend fun getDepartmentReports(): com.example.core.network.HODDepartmentReportDto {
        val response = apiService.getHODDepartmentReports()
        if (response.isSuccessful) return response.body()!!
        throw java.io.IOException("Failed to fetch department reports")
    }

    override suspend fun getResearchMonitoring(): List<com.example.core.network.HODResearchMonitoringDto> {
        val response = apiService.getHODResearchMonitoring()
        if (response.isSuccessful) return response.body() ?: emptyList()
        throw java.io.IOException("Failed to fetch research monitoring data")
    }

    override suspend fun getPendingProofs(): List<com.example.core.network.HODPendingProofDto> {
        val response = apiService.getHODPendingProofs()
        if (response.isSuccessful) return response.body() ?: emptyList()
        throw java.io.IOException("Failed to fetch pending proofs")
    }

    override suspend fun verifyResearchProof(proofId: String, request: com.example.core.network.VerificationRequestDto): Map<String, String> {
        val response = apiService.verifyResearchProof(proofId, request)
        if (response.isSuccessful) return response.body()!!
        throw java.io.IOException("Failed to verify research proof")
    }

    override suspend fun getHODWorkloads(): List<com.example.core.network.HODWorkloadDto> {
        val response = apiService.getHODWorkloads()
        if (response.isSuccessful) return response.body() ?: emptyList()
        throw java.io.IOException("Failed to fetch workloads")
    }

    override suspend fun getHODMentors(): List<com.example.core.network.HODMentorDto> {
        val response = apiService.getHODMentors()
        if (response.isSuccessful) return response.body() ?: emptyList()
        throw java.io.IOException("Failed to fetch mentors")
    }

    override suspend fun assignHODMentor(studentId: String, facultyId: String) {
        val response = apiService.assignHODMentor(com.example.core.network.MentorAssignmentRequestDto(studentId, facultyId))
        if (!response.isSuccessful) throw java.io.IOException("Failed to assign mentor")
    }

    override suspend fun getAcademicSetup(): Result<com.example.core.network.AcademicSetupDto> {
        return try {
            val response = apiService.getAcademicSetup()
            if (response.isSuccessful && response.body() != null) {
                Result.success(response.body()!!)
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
            if (response.isSuccessful && response.body() != null) {
                Result.success(response.body()!!)
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
}
