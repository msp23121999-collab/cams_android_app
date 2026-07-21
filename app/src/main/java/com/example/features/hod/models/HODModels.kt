package com.example.features.hod.models

data class HODDashboardMetrics(
    val departmentHealthIndex: String,
    val activeFacultyCount: String,
    val avgWorkloadHours: String,
    val pendingVerifications: String
)

data class HODActivity(
    val title: String,
    val time: String,
    val type: String
)
