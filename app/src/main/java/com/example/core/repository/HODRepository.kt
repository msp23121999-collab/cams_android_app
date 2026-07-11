package com.example.core.repository

import com.example.features.hod.models.HODDashboardMetrics
import com.example.features.hod.models.HODActivity

interface HODRepository {
    suspend fun getDashboardMetrics(): HODDashboardMetrics
    suspend fun getRecentActivities(): List<HODActivity>
    suspend fun getFacultyManagementData(): List<com.example.core.network.FacultyStudentDto>
    suspend fun getStudentManagementData(): List<com.example.core.network.FacultyStudentDto>
}
