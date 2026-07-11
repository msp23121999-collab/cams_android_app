package com.example.features.hod.models

data class HODDashboardMetrics(
    val totalFaculty: String,
    val totalStudents: String,
    val pendingApprovals: String,
    val activeSubjects: String
)

data class HODActivity(
    val title: String,
    val time: String,
    val type: String
)
