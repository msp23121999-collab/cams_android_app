package com.example.core.repository

import com.example.core.network.CamsApiService
import com.example.core.network.ApprovalRequest
import com.example.core.network.CalendarEventDto
import com.example.features.principal.models.*
import java.io.IOException

interface PrincipalRepository {
    suspend fun getDashboardStats(): PrincipalDashboardMetrics
    suspend fun getPendingTimetableApprovals(): List<TimetableApproval>
    suspend fun approveTimetable(id: String, status: String, remarks: String)
    suspend fun getPendingLeaveApprovals(): List<LeaveApproval>
    suspend fun approveLeave(id: String, status: String, remarks: String)
    suspend fun getPendingFaculty(): List<PrincipalPendingFaculty>
    suspend fun approveFaculty(id: String)
    suspend fun rejectFaculty(id: String)
    suspend fun getAcademicCalendar(): List<CalendarEventDto>
    suspend fun getGrievances(): List<com.example.core.network.GrievanceDto>
    suspend fun getCirculars(): List<com.example.core.network.NoticeDto>
    suspend fun publishCircular(title: String, body: String, targetAudience: String)
    suspend fun getResearchCompliance(): com.example.core.network.PrincipalComplianceResponseDto?
    suspend fun getInfrastructureDetails(): com.example.core.network.InfrastructureResponseDto?
}

class PrincipalRepositoryImpl(private val apiService: CamsApiService) : PrincipalRepository {
    override suspend fun getDashboardStats(): PrincipalDashboardMetrics {
        return try {
            val response = apiService.getPrincipalDashboardStats()
            if (response.isSuccessful) {
                val dto = response.body() ?: throw IOException("Empty response body")
                PrincipalDashboardMetrics(
                    totalDepartments = dto.totalDepartments?.toString() ?: "0",
                    totalFaculty = dto.totalStaff?.toString() ?: "0",
                    totalStudents = dto.totalStudents?.toString() ?: "0",
                    averageAttendance = "N/A"
                )
            } else {
                throw IOException("Failed to fetch Principal dashboard stats: ${response.code()}")
            }
        } catch (e: IOException) {
            throw e
        } catch (e: Exception) {
            throw IOException("Failed to fetch Principal dashboard stats: ${e.message}")
        }
    }

    override suspend fun getAcademicCalendar(): List<CalendarEventDto> {
        return try {
            val response = apiService.getAcademicCalendar()
            if (response.isSuccessful) {
                response.body()?.events ?: emptyList()
            } else {
                emptyList()
            }
        } catch (e: Exception) {
            emptyList()
        }
    }

    override suspend fun getPendingTimetableApprovals(): List<TimetableApproval> {
        return try {
            val response = apiService.getPendingTimetableApprovals()
            if (response.isSuccessful) {
                (response.body() ?: emptyList()).map { dto ->
                    TimetableApproval(
                        id = dto.id,
                        facultyName = dto.facultyName,
                        subjectName = dto.subjectName,
                        requestedChanges = dto.requestedChanges,
                        date = dto.date
                    )
                }
            } else {
                emptyList()
            }
        } catch (e: Exception) {
            emptyList()
        }
    }

    override suspend fun approveTimetable(id: String, status: String, remarks: String) {
        try {
            apiService.approveTimetable(id, ApprovalRequest(status, remarks))
        } catch (e: Exception) {
            // Log but don't crash — fire-and-forget action
        }
    }

    override suspend fun getPendingLeaveApprovals(): List<LeaveApproval> {
        return try {
            val response = apiService.getPendingLeaveApprovals()
            if (response.isSuccessful) {
                (response.body() ?: emptyList()).map { dto ->
                    LeaveApproval(
                        id = dto.id,
                        applicantName = dto.userName ?: "Unknown",
                        leaveType = dto.type,
                        startDate = dto.startDate,
                        endDate = dto.endDate,
                        reason = dto.reason,
                        departmentName = dto.departmentName
                    )
                }
            } else {
                emptyList()
            }
        } catch (e: Exception) {
            emptyList()
        }
    }

    override suspend fun approveLeave(id: String, status: String, remarks: String) {
        try {
            apiService.approveLeave(id, ApprovalRequest(status, remarks))
        } catch (e: Exception) {
            // Log but don't crash
        }
    }

    override suspend fun getPendingFaculty(): List<PrincipalPendingFaculty> {
        return try {
            val response = apiService.getPrincipalPendingFaculty()
            if (response.isSuccessful) {
                (response.body() ?: emptyList()).map { dto ->
                    PrincipalPendingFaculty(
                        id = dto.id,
                        email = dto.email,
                        fullName = dto.fullName,
                        departmentName = dto.departmentName,
                        designation = dto.designation
                    )
                }
            } else {
                emptyList()
            }
        } catch (e: Exception) {
            emptyList()
        }
    }

    override suspend fun approveFaculty(id: String) {
        try {
            apiService.approvePrincipalFaculty(id)
        } catch (e: Exception) {
            // Log but don't crash
        }
    }

    override suspend fun rejectFaculty(id: String) {
        try {
            apiService.rejectPrincipalFaculty(id)
        } catch (e: Exception) {
            // Log but don't crash
        }
    }

    override suspend fun getGrievances(): List<com.example.core.network.GrievanceDto> {
        return try {
            val response = apiService.getGrievancesForApproval()
            if (response.isSuccessful) response.body() ?: emptyList() else emptyList()
        } catch (e: Exception) { emptyList() }
    }

    override suspend fun getCirculars(): List<com.example.core.network.NoticeDto> {
        return try {
            val response = apiService.getPrincipalCirculars()
            if (response.isSuccessful) response.body() ?: emptyList() else emptyList()
        } catch (e: Exception) { emptyList() }
    }

    override suspend fun publishCircular(title: String, body: String, targetAudience: String) {
        try {
            val audienceType = if (targetAudience.lowercase() == "all") null else targetAudience
            apiService.publishPrincipalCircular(com.example.core.network.NoticeCreateRequest(title, body, audienceType))
        } catch (e: Exception) {
            // Log but don't crash
        }
    }

    override suspend fun getResearchCompliance(): com.example.core.network.PrincipalComplianceResponseDto? {
        return try {
            val response = apiService.getResearchCompliance()
            if (response.isSuccessful) response.body() else null
        } catch (e: Exception) { null }
    }

    override suspend fun getInfrastructureDetails(): com.example.core.network.InfrastructureResponseDto? {
        return try {
            val response = apiService.getInfrastructureDetails()
            if (response.isSuccessful) response.body() else null
        } catch (e: Exception) { null }
    }
}
