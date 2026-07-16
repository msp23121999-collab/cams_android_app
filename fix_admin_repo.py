import re

def fix_admin_repo():
    repo_path = 'app/src/main/java/com/example/core/repository/AdminRepository.kt'
    with open(repo_path, 'r', encoding='utf-8') as f:
        content = f.read()

    new_impl = """class AdminRepositoryImpl(private val apiService: CamsApiService) : AdminRepository {
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
                metrics = dto.metrics.map { AdminMetric(it.id, it.label, it.value) },
                totalUsers = dto.totalUsers,
                totalStudents = dto.totalStudents,
                totalStaff = dto.totalStaff,
                totalDepartments = dto.totalDepartments
            )
        }
        throw java.io.IOException("Failed to fetch Admin dashboard metrics")
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
                AdminBatch(dto.id, dto.year, dto.status, dto.status == "Active")
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

    override suspend fun searchStudentsForFees(query: String): List<Any>? {
        return try {
            val response = apiService.searchStudentsForFees(query)
            if (response.isSuccessful) response.body() else null
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
}"""
    
    # Replace the class AdminRepositoryImpl
    content = re.sub(r'class AdminRepositoryImpl\(.*$', new_impl, content, flags=re.MULTILINE | re.DOTALL)

    with open(repo_path, 'w', encoding='utf-8') as f:
        f.write(content)

    print("Updated AdminRepository.kt mappings")

if __name__ == "__main__":
    fix_admin_repo()
