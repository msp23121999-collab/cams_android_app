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

/**
 * Real institution-wide department comparison, backed by the same
 * /hod/reports/department data already verified for the HOD/Admin portals
 * (no fabricated "pass %" figures — only metrics the backend actually tracks).
 */
data class DepartmentPerformanceSummary(
    val deptId: String,
    val deptName: String,
    val activeFaculty: Int,
    val facultyOnLeave: Int,
    val avgWorkloadHours: Double,
    val totalAbsences: Int,
    val completedSubstitutions: Int,
    val verifiedResearch: Int,
    val materialsApproved: Int
)

data class LeaveApproval(
    val id: String,
    val applicantName: String,
    val leaveType: String,
    val startDate: String,
    val endDate: String,
    val reason: String,
    val departmentName: String? = null
)

data class PrincipalPendingFaculty(
    val id: String,
    val email: String,
    val fullName: String,
    val departmentName: String,
    val designation: String?
)

data class TimetableApproval(
    val id: String,
    val facultyName: String,
    val subjectName: String,
    val requestedChanges: String,
    val date: String
)
