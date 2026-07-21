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
    suspend fun getAdminStudentFees(studentId: String): List<AdminStudentFeeRecord>
    suspend fun adminCollectFee(feeRecordId: String, amount: Double, mode: String): Boolean
    suspend fun getFacultyPayrollAdmin(): List<AdminPayroll>
    suspend fun getBackupHistoryAdmin(): List<AdminBackup>
    suspend fun getSystemSettingsAdmin(): AdminSystemSettings?
    suspend fun getAuditLogsAdmin(): List<AdminAuditLog>

    // Notices & notifications
    suspend fun getNotices(): List<com.example.core.network.NoticeDto>
    suspend fun createNotice(title: String, body: String, audienceType: String)
    suspend fun deleteNotice(noticeId: String)
    suspend fun getNotifications(): List<com.example.core.network.NotificationDto>
    suspend fun markNotificationRead(notificationId: String)
    suspend fun markAllNotificationsRead()
    suspend fun deleteNotification(notificationId: String)

    // Academic calendar
    suspend fun getCalendarEvents(): List<com.example.core.network.HODCalendarEventDto>
    suspend fun createCalendarEvent(request: com.example.core.network.HODCalendarEventCreateRequest)
    suspend fun deleteCalendarEvent(eventId: String)

    // Subject allocation / faculty assignment
    suspend fun getAcademicSetup(): com.example.core.network.AcademicSetupDto
    suspend fun getSubjectAllocations(): List<com.example.core.network.SubjectAllocationDto>
    suspend fun getAllocationSubjects(): List<com.example.core.network.SubjectInfoDto>
    suspend fun getAllocationFaculty(): List<com.example.core.network.FacultyWorkloadInfoDto>
    suspend fun getCourseSections(courseId: String): List<com.example.core.network.AcademicSetupSectionDto>
    suspend fun allocateSubject(request: com.example.core.network.SubjectAllocationCreateDto)

    // Exam management (hall tickets)
    suspend fun getHallTickets(): List<com.example.core.network.HallTicketDto>
    suspend fun generateHallTickets(request: com.example.core.network.GenerateHallTicketsRequest)

    // System config
    suspend fun saveSystemSettings(settings: com.example.core.network.AdminSystemSettingsDto): com.example.core.network.AdminSystemSettingsDto
    suspend fun setEmailNotificationsEnabled(enabled: Boolean)

    // Backups
    suspend fun createBackup()
    suspend fun deleteBackup(backupId: String)
    suspend fun restoreBackup(backupId: String)
    suspend fun getBackupSettings(): com.example.core.network.AdminBackupSettingsDto
    suspend fun saveBackupSettings(settings: com.example.core.network.AdminBackupSettingsDto)

    // Academic years
    suspend fun getAcademicYears(): List<com.example.core.network.AdminAcademicYearDto>
    suspend fun initializeAcademicYear(payload: Map<String, Any?>)
    suspend fun updateAcademicYear(ayId: String, payload: Map<String, Any?>)
    suspend fun deleteAcademicYear(ayId: String)

    // Attendance defaulters
    suspend fun markAttendanceFinePaid(studentId: String)
    suspend fun adjustAttendancePercentage(studentId: String, percentage: Double)

    // Academic catalog CRUD
    suspend fun createDepartment(payload: Map<String, Any?>)
    suspend fun deleteDepartment(deptId: String)
    suspend fun createDegree(payload: Map<String, Any?>)
    suspend fun deleteDegree(degreeId: String)
    suspend fun getCoursesByDegree(degreeId: String): List<AdminCourse>
    suspend fun createCourse(payload: Map<String, Any?>)
    suspend fun updateCourse(courseId: String, payload: Map<String, Any?>)
    suspend fun deleteCourse(courseId: String)

}

class AdminRepositoryImpl(private val apiService: CamsApiService) : AdminRepository {
    override suspend fun getUsers(): List<com.example.features.admin.models.AdminUser> {
        return try {
            val response = apiService.getAllUsers()
            if (response.isSuccessful) {
                response.body()?.map {
                    com.example.features.admin.models.AdminUser(
                        id = it.id,
                        email = it.email,
                        phone = it.phone,
                        fullName = it.fullName,
                        role = it.role,
                        isActive = it.isActive,
                        departmentId = it.departmentId,
                        departmentName = it.departmentName
                    )
                } ?: emptyList()
            } else {
                emptyList()
            }
        } catch (e: Exception) { emptyList() }
    }

    override suspend fun getDepartments(): List<AdminDepartment> {
        return try {
            val response = apiService.getDepartmentsList()
            if (response.isSuccessful) {
                response.body()?.map {
                    AdminDepartment(it.id, it.code, it.name, it.courseName, it.programLevel)
                } ?: emptyList()
            } else {
                emptyList()
            }
        } catch (e: Exception) { emptyList() }
    }

    override suspend fun getDashboardMetrics(): AdminDashboardMetrics {
        return try {
            val response = apiService.getAdminDashboardMetrics()
            if (response.isSuccessful) {
                val dto = response.body() ?: throw IOException("Empty response body")
                AdminDashboardMetrics(
                    metrics = dto.metrics.map { AdminMetric(it.id, it.label, it.value) },
                    totalUsers = dto.totalUsers,
                    totalStudents = dto.totalStudents,
                    totalStaff = dto.totalStaff,
                    totalDepartments = dto.totalDepartments
                )
            } else {
                throw IOException("Failed to fetch Admin dashboard metrics: ${response.code()}")
            }
        } catch (e: IOException) {
            throw e
        } catch (e: Exception) {
            throw IOException("Failed to fetch Admin dashboard metrics: ${e.message}")
        }
    }

    override suspend fun getDegrees(): List<AdminDegree> {
        return try {
            val response = apiService.getDegreesListAdmin()
            if (response.isSuccessful) {
                (response.body() ?: emptyList()).map { dto ->
                    AdminDegree(
                        id = dto.id,
                        name = dto.name,
                        code = dto.code,
                        durationYears = dto.durationYears,
                        programLevel = dto.programLevel
                    )
                }
            } else {
                emptyList()
            }
        } catch (e: Exception) { emptyList() }
    }

    override suspend fun getBatches(): List<AdminBatch> {
        return try {
            val response = apiService.getBatches()
            if (response.isSuccessful) {
                (response.body() ?: emptyList()).map { dto ->
                    AdminBatch(
                        id = dto.id,
                        year = dto.batch.ifBlank { dto.name },
                        status = if (dto.isActive) "Active" else "Inactive",
                        isActive = dto.isActive
                    )
                }
            } else {
                emptyList()
            }
        } catch (e: Exception) { emptyList() }
    }

    override suspend fun getCourses(): List<AdminCourse> {
        return try {
            val response = apiService.getAllCoursesAdmin()
            if (response.isSuccessful) {
                (response.body() ?: emptyList()).map { dto ->
                    AdminCourse(
                        id = dto.id,
                        code = dto.code,
                        name = dto.name,
                        semester = dto.semester,
                        credits = dto.credits
                    )
                }
            } else {
                emptyList()
            }
        } catch (e: Exception) { emptyList() }
    }

    override suspend fun getBackupsHistory(): List<AdminBackup> {
        return try {
            val response = apiService.getBackupsAdmin()
            if (response.isSuccessful) {
                (response.body() ?: emptyList()).map { dto ->
                    AdminBackup(
                        id = dto.id,
                        filename = dto.filename ?: "",
                        sizeBytes = dto.sizeBytes ?: 0,
                        status = dto.status ?: "",
                        createdAt = dto.createdAt ?: ""
                    )
                }
            } else {
                emptyList()
            }
        } catch (e: Exception) { emptyList() }
    }

    override suspend fun getAttendanceDefaultersAdmin(): List<AdminAttendanceDefaulter> {
        return try {
            val response = apiService.getAttendanceDefaultersAdmin()
            if (response.isSuccessful) {
                response.body()?.map {
                    AdminAttendanceDefaulter(
                        studentId = it.studentId,
                        studentName = it.name ?: "",
                        rollNo = it.rollNo ?: "",
                        department = it.degreeName ?: it.degreeCode ?: "",
                        semester = it.semester ?: 0,
                        section = it.section ?: "",
                        attendancePercentage = it.attendancePercentage ?: 0.0,
                        finePaid = it.finePaid
                    )
                } ?: emptyList()
            } else emptyList()
        } catch (e: Exception) { emptyList() }
    }

    override suspend fun getAdminFeeStructures(): List<AdminFeeStructure> {
        return try {
            val response = apiService.getAdminFeeStructures()
            if (response.isSuccessful) {
                response.body()?.map { AdminFeeStructure(it.id, it.feeType ?: "", it.amount ?: 0.0, it.semester ?: 1, it.deptName ?: "") } ?: emptyList()
            } else emptyList()
        } catch (e: Exception) { emptyList() }
    }

    override suspend fun getAdminScholarshipTypes(): List<AdminScholarshipType> {
        return try {
            val response = apiService.getAdminScholarshipTypes()
            if (response.isSuccessful) {
                response.body()?.map { AdminScholarshipType(it.id, it.name ?: "", it.description ?: "", it.reductionType ?: "", it.reductionValue ?: 0.0) } ?: emptyList()
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
                        userId = it.userId,
                        studentName = it.name ?: "",
                        rollNo = it.rollNo ?: "",
                        department = it.departmentName ?: "",
                        currentSemester = it.semester ?: 1,
                        batchYear = it.batchYear ?: 0
                    )
                }
            } else null
        } catch (e: Exception) { null }
    }

    override suspend fun getAdminStudentFees(studentId: String): List<AdminStudentFeeRecord> {
        val response = apiService.getAdminStudentFees(studentId)
        if (response.isSuccessful) {
            return response.body()?.map {
                AdminStudentFeeRecord(
                    recordId = it.recordId,
                    feeType = it.feeType ?: "",
                    semester = it.semester ?: 0,
                    amount = it.amount ?: 0.0,
                    paidAmount = it.paidAmount ?: 0.0,
                    remainingAmount = it.remainingAmount ?: 0.0,
                    status = it.status ?: ""
                )
            } ?: emptyList()
        }
        throw IOException("Failed to load student fees: ${response.code()}")
    }

    override suspend fun adminCollectFee(feeRecordId: String, amount: Double, mode: String): Boolean {
        val response = apiService.adminCollectFee(
            com.example.core.network.AdminCollectFeeRequest(feeRecordId, amount, mode)
        )
        if (response.isSuccessful) return true
        val detail = try {
            val body = response.errorBody()?.string()
            if (body.isNullOrBlank()) null else org.json.JSONObject(body).optString("detail", "").ifBlank { null }
        } catch (e: Exception) { null }
        throw IOException(detail ?: "Failed to collect fee (error ${response.code()})")
    }

    override suspend fun getFacultyPayrollAdmin(): List<AdminPayroll> {
        return try {
            val response = apiService.getFacultyPayrollAdmin()
            if (response.isSuccessful) {
                val months = listOf("", "Jan", "Feb", "Mar", "Apr", "May", "Jun", "Jul", "Aug", "Sep", "Oct", "Nov", "Dec")
                response.body()?.map {
                    val monthLabel = (it.month?.let { m -> if (m in 1..12) "${months[m]} ${it.year ?: ""}".trim() else "" } ?: "")
                    AdminPayroll(
                        id = it.id,
                        facultyId = it.facultyId ?: "",
                        facultyName = it.facultyName ?: "",
                        designation = it.designation ?: "",
                        departmentName = it.departmentName ?: "",
                        month = monthLabel,
                        amount = it.netSalary ?: it.basic ?: 0.0
                    )
                } ?: emptyList()
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
                response.body()?.let {
                    AdminSystemSettings(
                        collegeName = it.collegeName ?: "",
                        address = it.address ?: "",
                        affiliationNumber = it.affiliationNumber ?: "",
                        aicteUgcCode = it.aicteUgcCode ?: "",
                        accreditationBody = it.accreditationBody ?: "",
                        bankName = it.bankName ?: "",
                        bankAccountNo = it.bankAccountNo ?: "",
                        bankIfsc = it.bankIfsc ?: "",
                        bankBranch = it.bankBranch ?: ""
                    )
                }
            } else null
        } catch (e: Exception) { null }
    }

    override suspend fun getAuditLogsAdmin(): List<AdminAuditLog> {
        return try {
            val response = apiService.getAuditLogsAdmin()
            if (response.isSuccessful) {
                response.body()?.map { AdminAuditLog(it.id, it.userName ?: "System", it.action ?: "", it.entityId ?: "", it.timestamp ?: "") } ?: emptyList()
            } else {
                emptyList()
            }
        } catch (e: Exception) { emptyList() }
    }

    // ---- Notices & notifications ----

    override suspend fun getNotices(): List<com.example.core.network.NoticeDto> {
        val response = apiService.getHodNotices()
        if (response.isSuccessful) return response.body() ?: emptyList()
        throw IOException("Failed to load notices: ${response.code()}")
    }

    override suspend fun createNotice(title: String, body: String, audienceType: String) {
        val response = apiService.createHodNotice(
            com.example.core.network.NoticeCreateRequest(title, body, audienceType)
        )
        if (!response.isSuccessful) throw IOException("Failed to publish notice: ${response.code()}")
    }

    override suspend fun deleteNotice(noticeId: String) {
        val response = apiService.deleteHodNotice(noticeId)
        if (!response.isSuccessful) throw IOException("Failed to delete notice: ${response.code()}")
    }

    override suspend fun getNotifications(): List<com.example.core.network.NotificationDto> {
        val response = apiService.getAdminNotifications()
        if (response.isSuccessful) return response.body() ?: emptyList()
        throw IOException("Failed to load notifications: ${response.code()}")
    }

    override suspend fun markNotificationRead(notificationId: String) {
        val response = apiService.markAdminNotificationRead(notificationId)
        if (!response.isSuccessful) throw IOException("Failed to mark notification read: ${response.code()}")
    }

    override suspend fun markAllNotificationsRead() {
        val response = apiService.markAllAdminNotificationsRead()
        if (!response.isSuccessful) throw IOException("Failed to mark all read: ${response.code()}")
    }

    override suspend fun deleteNotification(notificationId: String) {
        val response = apiService.deleteAdminNotification(notificationId)
        if (!response.isSuccessful) throw IOException("Failed to delete notification: ${response.code()}")
    }

    // ---- Academic calendar ----

    override suspend fun getCalendarEvents(): List<com.example.core.network.HODCalendarEventDto> {
        val response = apiService.getHodCalendarEvents()
        if (response.isSuccessful) return response.body() ?: emptyList()
        throw IOException("Failed to load calendar events: ${response.code()}")
    }

    override suspend fun createCalendarEvent(request: com.example.core.network.HODCalendarEventCreateRequest) {
        val response = apiService.createHodCalendarEvent(request)
        if (!response.isSuccessful) throw IOException("Failed to create calendar event: ${response.code()}")
    }

    override suspend fun deleteCalendarEvent(eventId: String) {
        val response = apiService.deleteHodCalendarEvent(eventId)
        if (!response.isSuccessful) throw IOException("Failed to delete calendar event: ${response.code()}")
    }

    // ---- Subject allocation / faculty assignment ----

    override suspend fun getAcademicSetup(): com.example.core.network.AcademicSetupDto {
        val response = apiService.getAcademicSetup()
        if (response.isSuccessful) return response.body() ?: throw IOException("Empty academic setup response")
        throw IOException("Failed to load academic setup: ${response.code()}")
    }

    override suspend fun getSubjectAllocations(): List<com.example.core.network.SubjectAllocationDto> {
        val response = apiService.getSubjectAllocations()
        if (response.isSuccessful) return response.body() ?: emptyList()
        throw IOException("Failed to load allocations: ${response.code()}")
    }

    override suspend fun getAllocationSubjects(): List<com.example.core.network.SubjectInfoDto> {
        val response = apiService.getAllocationSubjects()
        if (response.isSuccessful) return response.body() ?: emptyList()
        throw IOException("Failed to load subjects: ${response.code()}")
    }

    override suspend fun getAllocationFaculty(): List<com.example.core.network.FacultyWorkloadInfoDto> {
        val response = apiService.getAllocationFaculty()
        if (response.isSuccessful) return response.body() ?: emptyList()
        throw IOException("Failed to load faculty: ${response.code()}")
    }

    override suspend fun getCourseSections(courseId: String): List<com.example.core.network.AcademicSetupSectionDto> {
        val response = apiService.getCourseSections(courseId)
        if (response.isSuccessful) return response.body() ?: emptyList()
        throw IOException("Failed to load sections: ${response.code()}")
    }

    override suspend fun allocateSubject(request: com.example.core.network.SubjectAllocationCreateDto) {
        val response = apiService.allocateSubjects(listOf(request))
        if (response.isSuccessful) return
        val detail = try {
            val body = response.errorBody()?.string()
            if (body.isNullOrBlank()) null else org.json.JSONObject(body).optString("detail", "").ifBlank { null }
        } catch (e: Exception) { null }
        throw IOException(detail ?: "Failed to allocate subject (error ${response.code()})")
    }

    // ---- Exam management (hall tickets) ----

    override suspend fun getHallTickets(): List<com.example.core.network.HallTicketDto> {
        val response = apiService.getAdminHallTickets()
        if (response.isSuccessful) return response.body() ?: emptyList()
        throw IOException("Failed to load hall tickets: ${response.code()}")
    }

    override suspend fun generateHallTickets(request: com.example.core.network.GenerateHallTicketsRequest) {
        val response = apiService.generateHallTickets(request)
        if (response.isSuccessful) return
        throw IOException(errorDetail(response) ?: "Failed to generate hall tickets (error ${response.code()})")
    }

    // ---- System config ----

    override suspend fun saveSystemSettings(settings: com.example.core.network.AdminSystemSettingsDto): com.example.core.network.AdminSystemSettingsDto {
        val response = apiService.saveSystemSettings(settings)
        if (response.isSuccessful) return response.body() ?: settings
        throw IOException(errorDetail(response) ?: "Failed to save settings (error ${response.code()})")
    }

    override suspend fun setEmailNotificationsEnabled(enabled: Boolean) {
        val response = apiService.updateNotificationPreferences(
            com.example.core.network.NotificationPreferencesRequest(enabled)
        )
        if (!response.isSuccessful) throw IOException(errorDetail(response) ?: "Failed to update preference (error ${response.code()})")
    }

    // ---- Backups ----

    override suspend fun createBackup() {
        val response = apiService.createBackup()
        if (!response.isSuccessful) throw IOException(errorDetail(response) ?: "Failed to create backup (error ${response.code()})")
    }

    override suspend fun deleteBackup(backupId: String) {
        val response = apiService.deleteBackup(backupId)
        if (!response.isSuccessful) throw IOException(errorDetail(response) ?: "Failed to delete backup (error ${response.code()})")
    }

    override suspend fun restoreBackup(backupId: String) {
        val response = apiService.restoreBackup(backupId)
        if (!response.isSuccessful) throw IOException(errorDetail(response) ?: "Failed to restore backup (error ${response.code()})")
    }

    override suspend fun getBackupSettings(): com.example.core.network.AdminBackupSettingsDto {
        val response = apiService.getBackupSettings()
        if (response.isSuccessful) return response.body() ?: com.example.core.network.AdminBackupSettingsDto()
        throw IOException("Failed to load backup settings: ${response.code()}")
    }

    override suspend fun saveBackupSettings(settings: com.example.core.network.AdminBackupSettingsDto) {
        val response = apiService.saveBackupSettings(settings)
        if (!response.isSuccessful) throw IOException(errorDetail(response) ?: "Failed to save backup settings (error ${response.code()})")
    }

    // ---- Academic years ----

    override suspend fun getAcademicYears(): List<com.example.core.network.AdminAcademicYearDto> {
        val response = apiService.getBatches()
        if (response.isSuccessful) return response.body() ?: emptyList()
        throw IOException("Failed to load academic years: ${response.code()}")
    }

    override suspend fun initializeAcademicYear(payload: Map<String, Any?>) {
        val response = apiService.initializeAcademicYear(payload)
        if (!response.isSuccessful) throw IOException(errorDetail(response) ?: "Failed to create academic year (error ${response.code()})")
    }

    override suspend fun updateAcademicYear(ayId: String, payload: Map<String, Any?>) {
        val response = apiService.updateAcademicYear(ayId, payload)
        if (!response.isSuccessful) throw IOException(errorDetail(response) ?: "Failed to update academic year (error ${response.code()})")
    }

    override suspend fun deleteAcademicYear(ayId: String) {
        val response = apiService.deleteAcademicYear(ayId)
        if (!response.isSuccessful) throw IOException(errorDetail(response) ?: "Failed to delete academic year (error ${response.code()})")
    }

    // ---- Attendance defaulters ----

    override suspend fun markAttendanceFinePaid(studentId: String) {
        val response = apiService.markAttendanceFinePaid(studentId)
        if (!response.isSuccessful) throw IOException(errorDetail(response) ?: "Failed to mark fine paid (error ${response.code()})")
    }

    override suspend fun adjustAttendancePercentage(studentId: String, percentage: Double) {
        val response = apiService.adjustAttendancePercentage(studentId, mapOf("percentage" to percentage))
        if (!response.isSuccessful) throw IOException(errorDetail(response) ?: "Failed to adjust attendance (error ${response.code()})")
    }

    // ---- Academic catalog CRUD ----

    override suspend fun createDepartment(payload: Map<String, Any?>) {
        val response = apiService.createDepartment(payload)
        if (!response.isSuccessful) throw IOException(errorDetail(response) ?: "Failed to create department (error ${response.code()})")
    }

    override suspend fun deleteDepartment(deptId: String) {
        val response = apiService.deleteDepartment(deptId)
        if (!response.isSuccessful) throw IOException(errorDetail(response) ?: "Failed to delete department (error ${response.code()})")
    }

    override suspend fun createDegree(payload: Map<String, Any?>) {
        val response = apiService.createDegree(payload)
        if (!response.isSuccessful) throw IOException(errorDetail(response) ?: "Failed to create degree (error ${response.code()})")
    }

    override suspend fun deleteDegree(degreeId: String) {
        val response = apiService.deleteDegree(degreeId)
        if (!response.isSuccessful) throw IOException(errorDetail(response) ?: "Failed to delete degree (error ${response.code()})")
    }

    override suspend fun getCoursesByDegree(degreeId: String): List<AdminCourse> {
        val response = apiService.getCoursesByDegree(degreeId)
        if (response.isSuccessful) {
            return (response.body() ?: emptyList()).map {
                AdminCourse(id = it.id, code = it.code, name = it.name, semester = it.semester, credits = it.credits)
            }
        }
        throw IOException("Failed to load courses: ${response.code()}")
    }

    override suspend fun createCourse(payload: Map<String, Any?>) {
        val response = apiService.createCourse(payload)
        if (!response.isSuccessful) throw IOException(errorDetail(response) ?: "Failed to create course (error ${response.code()})")
    }

    override suspend fun updateCourse(courseId: String, payload: Map<String, Any?>) {
        val response = apiService.updateCourse(courseId, payload)
        if (!response.isSuccessful) throw IOException(errorDetail(response) ?: "Failed to update course (error ${response.code()})")
    }

    override suspend fun deleteCourse(courseId: String) {
        val response = apiService.deleteCourse(courseId)
        if (!response.isSuccessful) throw IOException(errorDetail(response) ?: "Failed to delete course (error ${response.code()})")
    }

    /** Pulls the backend's `detail` message out of an error body, if present. */
    private fun errorDetail(response: retrofit2.Response<*>): String? = try {
        val body = response.errorBody()?.string()
        if (body.isNullOrBlank()) null
        else org.json.JSONObject(body).optString("detail", "").ifBlank { null }
    } catch (e: Exception) { null }
}