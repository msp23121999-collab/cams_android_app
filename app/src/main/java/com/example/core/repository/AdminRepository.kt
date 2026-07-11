package com.example.core.repository

import com.example.core.network.CamsApiService
import com.example.features.admin.models.*
import java.io.IOException

interface AdminRepository {
    suspend fun getDashboardMetrics(): AdminDashboardMetrics
    suspend fun getDegrees(): List<AdminDegree>
    suspend fun getBatches(): List<AdminBatch>
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
        val response = apiService.getDegrees()
        if (response.isSuccessful) {
            return response.body()!!.map { dto ->
                AdminDegree(dto.id, dto.name, dto.code)
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
}
