package com.example.core.repository

import com.example.core.network.CamsApiService
import com.example.features.hod.models.*
import java.io.IOException

class HODRepositoryImpl(private val apiService: CamsApiService) : HODRepository {
    override suspend fun getDashboardMetrics(): HODDashboardMetrics {
        return try {
            val response = apiService.getHODDashboardMetrics()
            if (response.isSuccessful) {
                val dto = response.body() ?: throw IOException("Empty response body")
                HODDashboardMetrics(
                    totalFaculty = dto.totalFaculty,
                    totalStudents = dto.totalStudents,
                    pendingApprovals = dto.pendingApprovals,
                    activeSubjects = dto.activeSubjects
                )
            } else {
                throw IOException("Failed to fetch HOD dashboard metrics: ${response.code()}")
            }
        } catch (e: IOException) {
            throw e
        } catch (e: Exception) {
            throw IOException("Failed to fetch HOD dashboard metrics: ${e.message}")
        }
    }

    override suspend fun getRecentActivities(): List<HODActivity> {
        return try {
            val response = apiService.getHODActivities()
            if (response.isSuccessful) {
                (response.body() ?: emptyList()).map { dto ->
                    HODActivity(
                        title = dto.title,
                        time = dto.time,
                        type = dto.type
                    )
                }
            } else {
                emptyList()
            }
        } catch (e: Exception) {
            emptyList()
        }
    }

    override suspend fun getFacultyManagementData(): List<com.example.core.network.FacultyStudentDto> {
        return try {
            val response = apiService.getFacultyStudents()
            if (response.isSuccessful) response.body() ?: emptyList() else emptyList()
        } catch (e: Exception) { emptyList() }
    }

    override suspend fun getStudentManagementData(): List<com.example.core.network.FacultyStudentDto> {
        return try {
            val response = apiService.getFacultyStudents()
            if (response.isSuccessful) response.body() ?: emptyList() else emptyList()
        } catch (e: Exception) { emptyList() }
    }

    override suspend fun getPendingLeaveApprovals(): List<com.example.core.network.LeaveRequestDto> {
        return try {
            val response = apiService.getHODPendingLeaves()
            if (response.isSuccessful) response.body() ?: emptyList() else emptyList()
        } catch (e: Exception) { emptyList() }
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
        return try {
            val response = apiService.getHODTimetableSection(sectionId)
            if (response.isSuccessful) response.body() ?: emptyList() else emptyList()
        } catch (e: Exception) { emptyList() }
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
        return try {
            val response = apiService.getHODSyllabusCourses()
            if (response.isSuccessful) response.body() ?: emptyList() else emptyList()
        } catch (e: Exception) { emptyList() }
    }

    override suspend fun getAttendanceMonitoring(): List<com.example.core.network.HODAttendanceMonitoringDto> {
        return try {
            val response = apiService.getHODAttendanceMonitoring()
            if (response.isSuccessful) response.body() ?: emptyList() else emptyList()
        } catch (e: Exception) { emptyList() }
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

    override suspend fun getResearchMonitoring(): List<com.example.core.network.HODResearchMonitoringDto> {
        return try {
            val response = apiService.getHODResearchMonitoring()
            if (response.isSuccessful) response.body() ?: emptyList() else emptyList()
        } catch (e: Exception) { emptyList() }
    }

    override suspend fun getPendingProofs(): List<com.example.core.network.HODPendingProofDto> {
        return try {
            val response = apiService.getHODPendingProofs()
            if (response.isSuccessful) response.body() ?: emptyList() else emptyList()
        } catch (e: Exception) { emptyList() }
    }

    override suspend fun verifyResearchProof(proofId: String, request: com.example.core.network.VerificationRequestDto): Map<String, String> {
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
        return try {
            val response = apiService.getHODWorkloads()
            if (response.isSuccessful) response.body() ?: emptyList() else emptyList()
        } catch (e: Exception) { emptyList() }
    }

    override suspend fun getHODMentors(): List<com.example.core.network.HODMentorDto> {
        return try {
            val response = apiService.getHODMentors()
            if (response.isSuccessful) response.body() ?: emptyList() else emptyList()
        } catch (e: Exception) { emptyList() }
    }

    override suspend fun assignHODMentor(studentId: String, facultyId: String) {
        try {
            val response = apiService.assignHODMentor(com.example.core.network.MentorAssignmentRequestDto(studentId, facultyId))
            if (!response.isSuccessful) throw IOException("Failed to assign mentor: ${response.code()}")
        } catch (e: IOException) {
            throw e
        } catch (e: Exception) {
            throw IOException("Failed to assign mentor: ${e.message}")
        }
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
}
