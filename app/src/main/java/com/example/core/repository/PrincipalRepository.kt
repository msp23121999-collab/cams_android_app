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
    suspend fun getAcademicCalendar(): List<CalendarEventDto>
}

class PrincipalRepositoryImpl(private val apiService: CamsApiService) : PrincipalRepository {
    override suspend fun getDashboardStats(): PrincipalDashboardMetrics {
        val response = apiService.getPrincipalDashboardStats()
        if (response.isSuccessful) {
            val dto = response.body()!!
            return PrincipalDashboardMetrics(
                totalDepartments = "8", // Can be calculated from other data if available
                totalFaculty = dto.totalFaculty.toString(),
                totalStudents = dto.totalStudents.toString(),
                averageAttendance = "85%" // Fetch from actual attendance reports if available
            )
        }
        throw IOException("Failed to fetch Principal dashboard stats")
    }

    override suspend fun getAcademicCalendar(): List<CalendarEventDto> {
        val response = apiService.getAcademicCalendar()
        if (response.isSuccessful) {
            return response.body()!!
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
                    applicantName = dto.applicantName,
                    leaveType = dto.leaveType,
                    startDate = dto.startDate,
                    endDate = dto.endDate,
                    reason = dto.reason
                )
            }
        }
        return emptyList()
    }

    override suspend fun approveLeave(id: String, status: String, remarks: String) {
        apiService.approveLeave(id, ApprovalRequest(status, remarks))
    }
}
