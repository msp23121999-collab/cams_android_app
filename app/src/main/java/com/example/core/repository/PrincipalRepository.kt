package com.example.core.repository

import com.example.core.network.CamsApiService
import com.example.core.network.ApprovalRequest
import com.example.core.network.CalendarEventDto
import com.example.features.principal.models.*
import java.io.IOException
import kotlinx.coroutines.async
import kotlinx.coroutines.coroutineScope

interface PrincipalRepository {
    suspend fun getDashboardStats(): PrincipalDashboardMetrics
    suspend fun getPendingTimetableApprovals(): List<TimetableApproval>
    suspend fun approveTimetable(id: String, status: String, remarks: String)
    suspend fun getPendingLeaveApprovals(): List<LeaveApproval>
    suspend fun approveLeave(id: String, status: String, remarks: String)
    suspend fun getPendingFaculty(): List<PrincipalPendingFaculty>
    suspend fun approveFaculty(id: String)
    suspend fun rejectFaculty(id: String)
    suspend fun getAcademicCalendar(): List<CalendarEventDto>
    suspend fun getGrievances(): List<com.example.core.network.GrievanceDto>
    suspend fun resolveGrievance(id: String, status: String, comments: String?)
    suspend fun getCirculars(): List<com.example.core.network.NoticeDto>
    suspend fun publishCircular(title: String, body: String, targetAudience: String, priority: String = "Medium")
    suspend fun getResearchCompliance(): com.example.core.network.PrincipalComplianceResponseDto?
    suspend fun runComplianceScan(): Int
    suspend fun getInfrastructureDetails(): com.example.core.network.InfrastructureResponseDto?
    suspend fun getDepartmentPerformance(): List<DepartmentPerformanceSummary>
    suspend fun getClassDiaries(): List<com.example.core.network.ClassDiaryDto>
    suspend fun reviewClassDiary(id: String, status: String, remarks: String?)
    suspend fun getAllLegalEvents(): List<com.example.core.network.FacultyLegalEventDto>
    suspend fun getPendingLegalEvents(): List<com.example.core.network.FacultyLegalEventDto>
    suspend fun approveLegalEvent(eventId: String)
    suspend fun rejectLegalEvent(eventId: String, remarks: String?)
    suspend fun createLegalEvent(request: com.example.core.network.CreateLegalEventRequest)
    suspend fun getPendingStudyMaterials(): List<com.example.core.network.HodPendingMaterialDto>
    suspend fun reviewStudyMaterial(materialId: String, status: String, remarks: String)
    suspend fun getInstitutionCalendarEvents(): List<com.example.core.network.HODCalendarEventDto>
    suspend fun createInstitutionCalendarEvent(request: com.example.core.network.HODCalendarEventCreateRequest): com.example.core.network.HODCalendarEventDto
    suspend fun deleteInstitutionCalendarEvent(eventId: String)
    suspend fun getFacultyOverview(): List<com.example.core.network.PrincipalFacultyOverviewDto>
}

class PrincipalRepositoryImpl(private val apiService: CamsApiService) : PrincipalRepository {
    override suspend fun getDashboardStats(): PrincipalDashboardMetrics {
        return try {
            val response = apiService.getPrincipalDashboardStats()
            if (response.isSuccessful) {
                val dto = response.body() ?: throw IOException("Empty response body")
                PrincipalDashboardMetrics(
                    totalDepartments = dto.totalDepartments?.toString() ?: "0",
                    totalFaculty = dto.totalStaff?.toString() ?: "0",
                    totalStudents = dto.totalStudents?.toString() ?: "0",
                    averageAttendance = "N/A"
                )
            } else {
                throw IOException("Failed to fetch Principal dashboard stats: ${response.code()}")
            }
        } catch (e: IOException) {
            throw e
        } catch (e: Exception) {
            throw IOException("Failed to fetch Principal dashboard stats: ${e.message}")
        }
    }

    override suspend fun getAcademicCalendar(): List<CalendarEventDto> {
        return try {
            val response = apiService.getAcademicCalendar()
            if (response.isSuccessful) {
                response.body()?.events ?: emptyList()
            } else {
                emptyList()
            }
        } catch (e: Exception) {
            emptyList()
        }
    }

    override suspend fun getPendingTimetableApprovals(): List<TimetableApproval> {
        return try {
            val response = apiService.getPendingTimetableApprovals()
            if (response.isSuccessful) {
                (response.body() ?: emptyList()).map { dto ->
                    TimetableApproval(
                        id = dto.id,
                        facultyName = dto.facultyName,
                        subjectName = dto.subjectName,
                        requestedChanges = dto.requestedChanges,
                        date = dto.date
                    )
                }
            } else {
                emptyList()
            }
        } catch (e: Exception) {
            emptyList()
        }
    }

    override suspend fun approveTimetable(id: String, status: String, remarks: String) {
        val response = apiService.approveTimetable(id, ApprovalRequest(status, remarks))
        if (!response.isSuccessful) throw IOException("Failed to update timetable approval: ${response.code()}")
    }

    override suspend fun getPendingLeaveApprovals(): List<LeaveApproval> {
        return try {
            val response = apiService.getPendingLeaveApprovals()
            if (response.isSuccessful) {
                (response.body() ?: emptyList()).map { dto ->
                    LeaveApproval(
                        id = dto.id,
                        applicantName = dto.userName ?: "Unknown",
                        leaveType = dto.type,
                        startDate = dto.startDate,
                        endDate = dto.endDate,
                        reason = dto.reason,
                        departmentName = dto.departmentName
                    )
                }
            } else {
                emptyList()
            }
        } catch (e: Exception) {
            emptyList()
        }
    }

    override suspend fun approveLeave(id: String, status: String, remarks: String) {
        val response = apiService.approveLeave(id, ApprovalRequest(status, remarks))
        if (!response.isSuccessful) throw IOException("Failed to update leave approval: ${response.code()}")
    }

    override suspend fun getPendingFaculty(): List<PrincipalPendingFaculty> {
        return try {
            val response = apiService.getPrincipalPendingFaculty()
            if (response.isSuccessful) {
                (response.body() ?: emptyList()).map { dto ->
                    PrincipalPendingFaculty(
                        id = dto.id,
                        email = dto.email,
                        fullName = dto.fullName,
                        departmentName = dto.departmentName,
                        designation = dto.designation
                    )
                }
            } else {
                emptyList()
            }
        } catch (e: Exception) {
            emptyList()
        }
    }

    override suspend fun approveFaculty(id: String) {
        val response = apiService.approvePrincipalFaculty(id)
        if (!response.isSuccessful) throw IOException("Failed to approve faculty: ${response.code()}")
    }

    override suspend fun rejectFaculty(id: String) {
        val response = apiService.rejectPrincipalFaculty(id)
        if (!response.isSuccessful) throw IOException("Failed to reject faculty: ${response.code()}")
    }

    override suspend fun getGrievances(): List<com.example.core.network.GrievanceDto> {
        return try {
            val response = apiService.getGrievancesForApproval()
            if (response.isSuccessful) response.body() ?: emptyList() else emptyList()
        } catch (e: Exception) { emptyList() }
    }

    override suspend fun resolveGrievance(id: String, status: String, comments: String?) {
        val response = apiService.resolveGrievance(id, com.example.core.network.GrievanceResolveRequest(status, comments))
        if (!response.isSuccessful) throw IOException("Failed to update grievance: ${response.code()}")
    }

    override suspend fun getCirculars(): List<com.example.core.network.NoticeDto> {
        return try {
            val response = apiService.getPrincipalCirculars()
            if (response.isSuccessful) response.body() ?: emptyList() else emptyList()
        } catch (e: Exception) { emptyList() }
    }

    override suspend fun publishCircular(title: String, body: String, targetAudience: String, priority: String) {
        val audienceType = if (targetAudience.lowercase() == "all") null else targetAudience
        val response = apiService.publishPrincipalCircular(com.example.core.network.NoticeCreateRequest(title, body, audienceType, priority))
        if (!response.isSuccessful) throw IOException("Failed to publish circular: ${response.code()}")
    }

    override suspend fun getResearchCompliance(): com.example.core.network.PrincipalComplianceResponseDto? {
        return try {
            val response = apiService.getResearchCompliance()
            if (response.isSuccessful) response.body() else null
        } catch (e: Exception) { null }
    }

    override suspend fun runComplianceScan(): Int {
        val response = apiService.runComplianceScan()
        if (!response.isSuccessful) throw IOException("Failed to run compliance scan: ${response.code()}")
        return (response.body()?.get("flagged_faculties_count") as? Number)?.toInt() ?: 0
    }

    override suspend fun getInfrastructureDetails(): com.example.core.network.InfrastructureResponseDto? {
        return try {
            val response = apiService.getInfrastructureDetails()
            if (response.isSuccessful) response.body() else null
        } catch (e: Exception) { null }
    }

    override suspend fun getDepartmentPerformance(): List<DepartmentPerformanceSummary> {
        val deptResponse = apiService.getDepartmentsList()
        if (!deptResponse.isSuccessful) throw IOException("Failed to load departments: ${deptResponse.code()}")
        val departments = deptResponse.body() ?: emptyList()

        return coroutineScope {
            departments.map { dept ->
                async {
                    try {
                        val reportResponse = apiService.getDepartmentReportFor(dept.id)
                        val summary = reportResponse.body()?.summary
                        DepartmentPerformanceSummary(
                            deptId = dept.id,
                            deptName = dept.name,
                            activeFaculty = summary?.activeFaculty ?: 0,
                            facultyOnLeave = summary?.facultyOnLeave ?: 0,
                            avgWorkloadHours = summary?.avgWorkloadHours ?: 0.0,
                            totalAbsences = summary?.totalAbsences ?: 0,
                            completedSubstitutions = summary?.completedSubstitutions ?: 0,
                            verifiedResearch = summary?.totalVerifiedResearch ?: 0,
                            materialsApproved = summary?.materialsApproved ?: 0
                        )
                    } catch (e: Exception) {
                        DepartmentPerformanceSummary(dept.id, dept.name, 0, 0, 0.0, 0, 0, 0, 0)
                    }
                }
            }.map { it.await() }
        }
    }

    override suspend fun getClassDiaries(): List<com.example.core.network.ClassDiaryDto> {
        val response = apiService.getClassDiaries()
        if (response.isSuccessful) return response.body() ?: emptyList()
        throw IOException("Failed to load class diaries: ${response.code()}")
    }

    override suspend fun reviewClassDiary(id: String, status: String, remarks: String?) {
        val response = apiService.reviewClassDiary(id, com.example.core.network.DiaryReviewRequest(status, remarks))
        if (!response.isSuccessful) throw IOException("Failed to review diary entry: ${response.code()}")
    }

    override suspend fun getAllLegalEvents(): List<com.example.core.network.FacultyLegalEventDto> {
        val response = apiService.getLegalEvents()
        if (response.isSuccessful) return response.body() ?: emptyList()
        throw IOException("Failed to load events: ${response.code()}")
    }

    override suspend fun getPendingLegalEvents(): List<com.example.core.network.FacultyLegalEventDto> {
        val response = apiService.getPendingLegalEvents()
        if (response.isSuccessful) return response.body() ?: emptyList()
        throw IOException("Failed to load pending events: ${response.code()}")
    }

    override suspend fun approveLegalEvent(eventId: String) {
        val response = apiService.approveLegalEvent(eventId)
        if (!response.isSuccessful) throw IOException("Failed to approve event: ${response.code()}")
    }

    override suspend fun rejectLegalEvent(eventId: String, remarks: String?) {
        val response = apiService.rejectLegalEvent(eventId, remarks)
        if (!response.isSuccessful) throw IOException("Failed to reject event: ${response.code()}")
    }

    override suspend fun createLegalEvent(request: com.example.core.network.CreateLegalEventRequest) {
        val response = apiService.postLegalEvent(request)
        if (!response.isSuccessful) throw IOException("Failed to create event: ${response.code()}")
    }

    override suspend fun getPendingStudyMaterials(): List<com.example.core.network.HodPendingMaterialDto> {
        val response = apiService.getPrincipalPendingMaterials()
        if (response.isSuccessful) return response.body() ?: emptyList()
        throw IOException("Failed to load pending study materials: ${response.code()}")
    }

    override suspend fun reviewStudyMaterial(materialId: String, status: String, remarks: String) {
        val response = apiService.reviewPrincipalMaterial(materialId, com.example.core.network.MaterialReviewRequest(status, remarks))
        if (!response.isSuccessful) throw IOException("Failed to review study material: ${response.code()}")
    }

    override suspend fun getInstitutionCalendarEvents(): List<com.example.core.network.HODCalendarEventDto> {
        val response = apiService.getHodCalendarEvents()
        if (response.isSuccessful) return response.body() ?: emptyList()
        throw IOException("Failed to load calendar events: ${response.code()}")
    }

    override suspend fun createInstitutionCalendarEvent(request: com.example.core.network.HODCalendarEventCreateRequest): com.example.core.network.HODCalendarEventDto {
        val response = apiService.createHodCalendarEvent(request)
        if (response.isSuccessful) return response.body() ?: throw IOException("Empty response body")
        throw IOException("Failed to create calendar event: ${response.code()}")
    }

    override suspend fun deleteInstitutionCalendarEvent(eventId: String) {
        val response = apiService.deleteHodCalendarEvent(eventId)
        if (!response.isSuccessful) throw IOException("Failed to delete calendar event: ${response.code()}")
    }

    override suspend fun getFacultyOverview(): List<com.example.core.network.PrincipalFacultyOverviewDto> {
        val response = apiService.getPrincipalFacultyOverview()
        if (response.isSuccessful) return response.body() ?: emptyList()
        throw IOException("Failed to load faculty overview: ${response.code()}")
    }
}
