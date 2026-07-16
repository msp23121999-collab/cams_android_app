package com.example.features.admin.models

data class AdminUser(
    val id: String,
    val email: String,
    val phone: String?,
    val fullName: String,
    val role: String,
    val isActive: Boolean,
    val departmentId: String?
)

data class AdminMetric(
    val id: String,
    val label: String,
    val value: String
)

data class AdminDashboardMetrics(
    val metrics: List<AdminMetric>,
    val totalUsers: Int? = null,
    val totalStudents: Int? = null,
    val totalStaff: Int? = null,
    val totalDepartments: Int? = null
)

data class SystemStatus(
    val component: String,
    val status: String,
    val health: Int // 0-100
)

data class AdminDepartment(
    val id: String,
    val code: String,
    val name: String,
    val hodId: String? = null
)

data class AdminDegree(
    val id: String, 
    val name: String, 
    val code: String,
    val durationYears: Int?,
    val programLevel: String?
)
data class AdminBatch(
    val id: String, 
    val year: String, 
    val status: String, 
    val isActive: Boolean = false
)

data class AdminCourse(
    val id: String,
    val code: String,
    val name: String,
    val semester: Int,
    val credits: Int?
)
data class AdminBackup(
    val id: String,
    val filename: String,
    val sizeBytes: Long,
    val status: String,
    val createdAt: String
)


data class AdminAttendanceDefaulter(
    val studentId: String,
    val studentName: String,
    val department: String,
    val attendancePercentage: Double,
    val status: String
)

data class AdminFeeStructure(
    val id: String,
    val name: String,
    val amount: Double,
    val semester: Int,
    val departmentId: String
)

data class AdminScholarshipType(
    val id: String,
    val name: String,
    val percentage: Double
)

data class AdminPayroll(
    val id: String,
    val facultyId: String,
    val facultyName: String,
    val month: String,
    val amount: Double,
    val status: String
)

data class AdminSystemSettings(
    val institutionName: String,
    val academicYear: String,
    val semester: Int
)

data class AdminAuditLog(
    val id: String,
    val userId: String,
    val action: String,
    val details: String,
    val timestamp: String
)

data class AdminFeeStudent(
    val studentId: String,
    val studentName: String,
    val department: String,
    val currentSemester: Int,
    val totalFees: Double,
    val paidFees: Double,
    val dueFees: Double
)
