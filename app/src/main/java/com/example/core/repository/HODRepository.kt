package com.example.core.repository

import com.example.features.hod.models.HODDashboardMetrics
import com.example.features.hod.models.HODActivity

interface HODRepository {
    suspend fun getDashboardMetrics(): HODDashboardMetrics
    suspend fun getRecentActivities(): List<HODActivity>
    suspend fun getFacultyManagementData(): List<com.example.core.network.FacultyStudentDto>
    suspend fun getStudentManagementData(): List<com.example.core.network.FacultyStudentDto>
    suspend fun getPendingLeaveApprovals(): List<com.example.core.network.LeaveRequestDto>
    suspend fun approveLeave(id: String, status: String, remarks: String? = null)
    suspend fun getTimetableMetadata(): com.example.core.network.HODTimetableMetadataDto
    suspend fun getTimetableSection(sectionId: String): List<com.example.core.network.TimetableSlotDto>
}
