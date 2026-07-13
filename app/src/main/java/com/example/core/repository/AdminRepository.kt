package com.example.core.repository

import com.example.core.network.CamsApiService
import com.example.features.admin.models.*
import java.io.IOException

interface AdminRepository {
    suspend fun getDashboardMetrics(): AdminDashboardMetrics
    suspend fun getDegrees(): List<AdminDegree>
    suspend fun getBatches(): List<AdminBatch>
    suspend fun getCourses(): List<AdminCourse>
    suspend fun getBackupsHistory(): List<AdminBackup>
}

class AdminRepositoryImpl(private val apiService: CamsApiService) : AdminRepository {
    override suspend fun getDashboardMetrics(): AdminDashboardMetrics {
        val response = apiService.getAdminDashboardMetrics()
        if (response.isSuccessful) {
            val dto = response.body()!!
            return AdminDashboardMetrics(
                totalUsers = dto.totalUsers.toString(),
                collectionToday = "₹12.5L", // Mocking currency for now
                pendingDues = "₹5.2L",
                activeBatches = "3"
            )
        }
        throw IOException("Failed to fetch Admin dashboard metrics")
    }

    override suspend fun getDegrees(): List<AdminDegree> {
        val response = apiService.getDegreesList()
        if (response.isSuccessful) {
            return response.body()!!.map { dto ->
                AdminDegree(
                    id = dto.id,
                    name = dto.name,
                    code = dto.code,
                    durationYears = dto.durationYears,
                    programLevel = dto.programLevel
                )
            }
        }
        return emptyList()
    }

    override suspend fun getBatches(): List<AdminBatch> {
        val response = apiService.getBatches()
        if (response.isSuccessful) {
            return response.body()!!.map { dto ->
                AdminBatch(dto.id, dto.year, dto.status)
            }
        }
        return emptyList()
    }

    override suspend fun getCourses(): List<AdminCourse> {
        val response = apiService.getAllCourses()
        if (response.isSuccessful) {
            return response.body()!!.map { dto ->
                AdminCourse(
                    id = dto.id,
                    code = dto.code,
                    name = dto.name,
                    semester = dto.semester,
                    credits = dto.credits
                )
            }
        }
        return emptyList()
    }

    override suspend fun getBackupsHistory(): List<AdminBackup> {
        val response = apiService.getBackups()
        if (response.isSuccessful) {
            return response.body()!!.map { dto ->
                AdminBackup(
                    id = dto.id,
                    filename = dto.filename,
                    sizeBytes = dto.sizeBytes,
                    status = dto.status,
                    createdAt = dto.createdAt
                )
            }
        }
        return emptyList()
    }
}
