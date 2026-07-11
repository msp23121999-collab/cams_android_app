package com.example.features.admin.models

data class AdminDashboardMetrics(
    val totalUsers: String,
    val collectionToday: String,
    val pendingDues: String,
    val activeBatches: String
)

data class SystemStatus(
    val component: String,
    val status: String,
    val health: Int // 0-100
)

data class AdminDegree(val id: String, val name: String, val code: String)
data class AdminBatch(val id: String, val year: String, val status: String)
