package com.example.features.admin.models

data class AdminUser(
    val id: String,
    val email: String,
    val phone: String?,
    val fullName: String,
    val role: String,
    val isActive: Boolean,
    val departmentId: String?,
    val departmentName: String? = null
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
    val courseName: String? = null,
    val programLevel: String? = null
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
    val rollNo: String,
    val department: String,
    val semester: Int,
    val section: String,
    val attendancePercentage: Double,
    val finePaid: Boolean
)

data class AdminFeeStructure(
    val id: String,
    val feeType: String,
    val amount: Double,
    val semester: Int,
    val deptName: String
)

data class AdminScholarshipType(
    val id: String,
    val name: String,
    val description: String,
    val reductionType: String,
    val reductionValue: Double
)

data class AdminPayroll(
    val id: String,
    val facultyId: String,
    val facultyName: String,
    val designation: String,
    val departmentName: String,
    val month: String,
    val amount: Double
)

data class AdminSystemSettings(
    val collegeName: String,
    val address: String,
    val affiliationNumber: String,
    val aicteUgcCode: String,
    val accreditationBody: String,
    val bankName: String,
    val bankAccountNo: String,
    val bankIfsc: String,
    val bankBranch: String
)

data class AdminAuditLog(
    val id: String,
    val userName: String,
    val action: String,
    val entityId: String,
    val timestamp: String
)

data class AdminFeeStudent(
    val studentId: String,
    // The backend user account id — required by any endpoint (e.g. library
    // issue) whose foreign key points at users.id rather than students.id.
    val userId: String?,
    val studentName: String,
    val rollNo: String,
    val department: String,
    val currentSemester: Int,
    val batchYear: Int
)

data class AdminStudentFeeRecord(
    val recordId: String,
    val feeType: String,
    val semester: Int,
    val amount: Double,
    val paidAmount: Double,
    val remainingAmount: Double,
    val status: String
)
