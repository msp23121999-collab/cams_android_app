package com.example.features.principal.models

data class PrincipalDashboardMetrics(
    val totalDepartments: String,
    val totalFaculty: String,
    val totalStudents: String,
    val averageAttendance: String
)

data class DepartmentPerformance(
    val deptName: String,
    val resultPassPercentage: Double,
    val attendancePercentage: Double
)

data class LeaveApproval(
    val id: String,
    val applicantName: String,
    val leaveType: String,
    val startDate: String,
    val endDate: String,
    val reason: String
)

data class TimetableApproval(
    val id: String,
    val facultyName: String,
    val subjectName: String,
    val requestedChanges: String,
    val date: String
)
