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
        val response = apiService.getPrincipalDashboardStats()
        if (response.isSuccessful) {
            val dto = response.body()!!
            return PrincipalDashboardMetrics(
                totalDepartments = dto.totalDepartments?.toString() ?: "0",
                totalFaculty = dto.totalStaff?.toString() ?: "0",
                totalStudents = dto.totalStudents?.toString() ?: "0",
                averageAttendance = "N/A"
            )
        }
        throw IOException("Failed to fetch Principal dashboard stats")
    }

    override suspend fun getAcademicCalendar(): List<CalendarEventDto> {
        val response = apiService.getAcademicCalendar()
        if (response.isSuccessful) {
            return response.body() ?: emptyList()
        }
        return emptyList()
    }

    override suspend fun getPendingTimetableApprovals(): List<TimetableApproval> {
        val response = apiService.getPendingTimetableApprovals()
        if (response.isSuccessful) {
            return response.body()!!.map { dto ->
                TimetableApproval(
                    id = dto.id,
                    facultyName = dto.facultyName,
                    subjectName = dto.subjectName,
                    requestedChanges = dto.requestedChanges,
                    date = dto.date
                )
            }
        }
        return emptyList()
    }

    override suspend fun approveTimetable(id: String, status: String, remarks: String) {
        apiService.approveTimetable(id, ApprovalRequest(status, remarks))
    }

    override suspend fun getPendingLeaveApprovals(): List<LeaveApproval> {
        val response = apiService.getPendingLeaveApprovals()
        if (response.isSuccessful) {
            return response.body()!!.map { dto ->
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
        }
        return emptyList()
    }

    override suspend fun approveLeave(id: String, status: String, remarks: String) {
        apiService.approveLeave(id, ApprovalRequest(status, remarks))
    }

    override suspend fun getPendingFaculty(): List<PrincipalPendingFaculty> {
        val response = apiService.getPrincipalPendingFaculty()
        if (response.isSuccessful) {
            return response.body()!!.map { dto ->
                PrincipalPendingFaculty(
                    id = dto.id,
                    email = dto.email,
                    fullName = dto.fullName,
                    departmentName = dto.departmentName,
                    designation = dto.designation
                )
            }
        }
        return emptyList()
    }

    override suspend fun approveFaculty(id: String) {
        apiService.approvePrincipalFaculty(id)
    }

    override suspend fun rejectFaculty(id: String) {
        apiService.rejectPrincipalFaculty(id)
    }

    override suspend fun getGrievances(): List<com.example.core.network.GrievanceDto> {
        val response = apiService.getGrievancesForApproval()
        if (response.isSuccessful) return response.body() ?: emptyList()
        return emptyList()
    }

    override suspend fun getCirculars(): List<com.example.core.network.NoticeDto> {
        val response = apiService.getPrincipalCirculars()
        if (response.isSuccessful) return response.body() ?: emptyList()
        return emptyList()
    }

    override suspend fun publishCircular(title: String, body: String, targetAudience: String) {
        val audienceType = if (targetAudience.lowercase() == "all") null else targetAudience
        apiService.publishPrincipalCircular(com.example.core.network.NoticeCreateRequest(title, body, audienceType))
    }

    override suspend fun getResearchCompliance(): com.example.core.network.PrincipalComplianceResponseDto? {
        val response = apiService.getResearchCompliance()
        if (response.isSuccessful) {
            return response.body()
        }
        return null
    }

    override suspend fun getInfrastructureDetails(): com.example.core.network.InfrastructureResponseDto? {
        val response = apiService.getInfrastructureDetails()
        if (response.isSuccessful) {
            return response.body()
        }
        return null
    }
}
