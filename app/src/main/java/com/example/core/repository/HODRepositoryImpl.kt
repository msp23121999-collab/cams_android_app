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
}
