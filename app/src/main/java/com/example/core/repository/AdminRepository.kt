package com.example.core.repository

import com.example.core.network.CamsApiService
import com.example.features.admin.models.*
import java.io.IOException

interface AdminRepository {
    suspend fun getUsers(): List<com.example.features.admin.models.AdminUser>
    suspend fun getDepartments(): List<AdminDepartment>
    suspend fun getDashboardMetrics(): AdminDashboardMetrics
    suspend fun getDegrees(): List<AdminDegree>
    suspend fun getBatches(): List<AdminBatch>
    suspend fun getCourses(): List<AdminCourse>
    suspend fun getBackupsHistory(): List<AdminBackup>

    // Phase 3, 4, 5 Additions
    suspend fun getAttendanceDefaultersAdmin(): List<AdminAttendanceDefaulter>
    suspend fun getAdminFeeStructures(): List<AdminFeeStructure>
    suspend fun getAdminScholarshipTypes(): List<AdminScholarshipType>
    suspend fun searchStudentsForFees(query: String): List<com.example.features.admin.models.AdminFeeStudent>?
    suspend fun getFacultyPayrollAdmin(): List<AdminPayroll>
    suspend fun getBackupHistoryAdmin(): List<AdminBackup>
    suspend fun getSystemSettingsAdmin(): AdminSystemSettings?
    suspend fun getAuditLogsAdmin(): List<AdminAuditLog>

}

class AdminRepositoryImpl(private val apiService: CamsApiService) : AdminRepository {
    override suspend fun getUsers(): List<com.example.features.admin.models.AdminUser> {
        val response = apiService.getAllUsers()
        if (response.isSuccessful) {
            return response.body()?.map {
                com.example.features.admin.models.AdminUser(
                    id = it.id,
                    email = it.email,
                    phone = it.phone,
                    fullName = it.fullName,
                    role = it.role,
                    isActive = it.isActive,
                    departmentId = it.departmentId
                )
            } ?: emptyList()
        }
        throw java.io.IOException("Failed to fetch users")
    }

    override suspend fun getDepartments(): List<AdminDepartment> {
        val response = apiService.getDepartmentsList()
        if (response.isSuccessful) {
            return response.body()?.map {
                AdminDepartment(it.id, it.code, it.name, it.hodId)
            } ?: emptyList()
        }
        throw java.io.IOException("Failed to fetch departments")
    }

    override suspend fun getDashboardMetrics(): AdminDashboardMetrics {
        val response = apiService.getAdminDashboardMetrics()
        if (response.isSuccessful) {
            val dto = response.body()!!
            return AdminDashboardMetrics(
                metrics = listOf(
                    AdminMetric("total_users", "Total Users", dto.totalUsers.toString()),
                    AdminMetric("online_now", "Online Now", dto.onlineNow.toString()),
                    AdminMetric("storage_used", "Storage Used", dto.storageUsed),
                    AdminMetric("system_health", "System Health", dto.systemHealth)
                ),
                totalUsers = dto.totalUsers,
                totalStudents = 0,
                totalStaff = 0,
                totalDepartments = 0
            )
        }
        throw java.io.IOException("Failed to fetch Admin dashboard metrics")
    }

    override suspend fun getDegrees(): List<AdminDegree> {
        val response = apiService.getDegreesListAdmin()
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
                AdminBatch(dto.id, dto.year, dto.status, dto.status == "Active")
            }
        }
        return emptyList()
    }

    override suspend fun getCourses(): List<AdminCourse> {
        val response = apiService.getAllCoursesAdmin()
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
        val response = apiService.getBackupsAdmin()
        if (response.isSuccessful) {
            return response.body()!!.map { dto ->
                AdminBackup(
                    id = dto.id,
                    filename = dto.filename ?: "",
                    sizeBytes = dto.sizeBytes ?: 0,
                    status = dto.status ?: "",
                    createdAt = dto.createdAt ?: ""
                )
            }
        }
        return emptyList()
    }

    override suspend fun getAttendanceDefaultersAdmin(): List<AdminAttendanceDefaulter> {
        return try {
            val response = apiService.getAttendanceDefaultersAdmin()
            if (response.isSuccessful) {
                response.body()?.map { AdminAttendanceDefaulter(it.studentId, it.studentName ?: "", it.department ?: "", it.attendancePercentage ?: 0.0, it.status ?: "") } ?: emptyList()
            } else emptyList()
        } catch (e: Exception) { emptyList() }
    }

    override suspend fun getAdminFeeStructures(): List<AdminFeeStructure> {
        return try {
            val response = apiService.getAdminFeeStructures()
            if (response.isSuccessful) {
                response.body()?.map { AdminFeeStructure(it.id, it.name ?: "", it.amount ?: 0.0, it.semester ?: 1, it.departmentId ?: "") } ?: emptyList()
            } else emptyList()
        } catch (e: Exception) { emptyList() }
    }

    override suspend fun getAdminScholarshipTypes(): List<AdminScholarshipType> {
        return try {
            val response = apiService.getAdminScholarshipTypes()
            if (response.isSuccessful) {
                response.body()?.map { AdminScholarshipType(it.id, it.name ?: "", it.percentage ?: 0.0) } ?: emptyList()
            } else emptyList()
        } catch (e: Exception) { emptyList() }
    }

    override suspend fun searchStudentsForFees(query: String): List<com.example.features.admin.models.AdminFeeStudent>? {
        return try {
            val response = apiService.searchStudentsForFees(query)
            if (response.isSuccessful) {
                response.body()?.map { 
                    com.example.features.admin.models.AdminFeeStudent(
                        studentId = it.studentId,
                        studentName = it.studentName ?: "",
                        department = it.department ?: "",
                        currentSemester = it.currentSemester ?: 1,
                        totalFees = it.totalFees ?: 0.0,
                        paidFees = it.paidFees ?: 0.0,
                        dueFees = it.dueFees ?: 0.0
                    )
                }
            } else null
        } catch (e: Exception) { null }
    }

    override suspend fun getFacultyPayrollAdmin(): List<AdminPayroll> {
        return try {
            val response = apiService.getFacultyPayrollAdmin()
            if (response.isSuccessful) {
                response.body()?.map { AdminPayroll(it.id, it.facultyId ?: "", it.facultyName ?: "", it.month ?: "", it.amount ?: 0.0, it.status ?: "") } ?: emptyList()
            } else emptyList()
        } catch (e: Exception) { emptyList() }
    }

    override suspend fun getBackupHistoryAdmin(): List<AdminBackup> {
        return try {
            val response = apiService.getBackupHistoryAdmin()
            if (response.isSuccessful) {
                response.body()?.map { AdminBackup(it.id, it.filename ?: "", it.sizeBytes ?: 0, it.status ?: "", it.createdAt ?: "") } ?: emptyList()
            } else emptyList()
        } catch (e: Exception) { emptyList() }
    }

    override suspend fun getSystemSettingsAdmin(): AdminSystemSettings? {
        return try {
            val response = apiService.getSystemSettingsAdmin()
            if (response.isSuccessful) {
                response.body()?.let { AdminSystemSettings(it.institutionName ?: "", it.academicYear ?: "", it.semester ?: 1) }
            } else null
        } catch (e: Exception) { null }
    }

    override suspend fun getAuditLogsAdmin(): List<AdminAuditLog> {
        return try {
            val response = apiService.getAuditLogsAdmin()
            if (response.isSuccessful) {
                response.body()?.map { AdminAuditLog(it.id, it.userId ?: "", it.action ?: "", it.details ?: "", it.timestamp ?: "") } ?: emptyList()
            } else {
                emptyList()
            }
        } catch (e: Exception) { emptyList() }
    }
}