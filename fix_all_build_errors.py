import re

def fix_all_build_errors():
    api_path = 'app/src/main/java/com/example/core/network/CamsApiService.kt'
    with open(api_path, 'r', encoding='utf-8') as f:
        api = f.read()

    # 1. Move HOD Endpoints into CamsApiService interface
    # Find where the interface ends
    interface_end_match = re.search(r'^}(\s*)$', api, flags=re.MULTILINE)
    
    # Extract the mistakenly placed endpoints
    wrong_endpoints = """
    // Additional HOD Endpoints
    @GET("hods/teaching-logs")
    suspend fun getHODTeachingLogsDashboard(): Response<HODTeachingLogsDashboardDto>

    @GET("hods/syllabus-metadata")
    suspend fun getHODSyllabusMetadata(): Response<HODSyllabusMetadataDto>

    @GET("hods/syllabus-courses")
    suspend fun getHODSyllabusCourses(): Response<List<HODSyllabusCourseDto>>

    @GET("hods/attendance-monitoring")
    suspend fun getHODAttendanceMonitoring(): Response<List<HODAttendanceMonitoringDto>>

    @GET("hods/department-reports")
    suspend fun getHODDepartmentReports(): Response<HODDepartmentReportDto>

    @GET("hods/research-monitoring")
    suspend fun getHODResearchMonitoring(): Response<List<HODResearchMonitoringDto>>

    @GET("hods/pending-proofs")
    suspend fun getHODPendingProofs(): Response<List<HODPendingProofDto>>

    @POST("hods/research-proofs/{id}/verify")
    suspend fun verifyResearchProof(@Path("id") proofId: String, @Body request: VerificationRequestDto): Response<Unit>

    @GET("hods/workloads")
    suspend fun getHODWorkloads(): Response<List<HODWorkloadDto>>

    @GET("hods/mentors")
    suspend fun getHODMentors(): Response<List<HODMentorDto>>

    @POST("hods/mentors/assign")
    suspend fun assignHODMentor(@Body request: MentorAssignmentRequestDto): Response<Unit>

    @GET("hods/academic-setup")
    suspend fun getAcademicSetup(): Response<AcademicSetupDto>

    @GET("hods/subject-allocations")
    suspend fun getSubjectAllocations(): Response<List<SubjectAllocationDto>>

    @GET("hods/substitutions")
    suspend fun getSubstitutions(): Response<List<HODSubstitutionDto>>
"""
    
    # Remove wrong endpoints from where they are (bottom of file)
    api = api.replace(wrong_endpoints, "")

    # Define Admin Endpoints to add
    admin_endpoints = """
    @GET("admins/payroll")
    suspend fun getFacultyPayrollAdmin(): Response<List<AdminPayrollDto>>

    @GET("admins/backups")
    suspend fun getBackupHistoryAdmin(): Response<List<AdminBackupDto>>

    @GET("admins/system-settings")
    suspend fun getSystemSettingsAdmin(): Response<AdminSystemSettingsDto>

    @GET("admins/audit-logs")
    suspend fun getAuditLogsAdmin(): Response<List<AdminAuditLogDto>>
"""

    # Insert HOD & Admin endpoints into the interface
    if interface_end_match:
        insert_pos = interface_end_match.start()
        api = api[:insert_pos] + wrong_endpoints + admin_endpoints + "\n" + api[insert_pos:]

    # Add Admin DTOs to the bottom of the file
    admin_dtos = """
@JsonClass(generateAdapter = true)
data class AdminPayrollDto(
    val id: String,
    val facultyId: String?,
    val facultyName: String?,
    val month: String?,
    val amount: Double?,
    val status: String?
)

@JsonClass(generateAdapter = true)
data class AdminBackupDto(
    val id: String,
    val filename: String?,
    val sizeBytes: Long?,
    val status: String?,
    val createdAt: String?
)

@JsonClass(generateAdapter = true)
data class AdminSystemSettingsDto(
    val institutionName: String?,
    val academicYear: String?,
    val semester: Int?
)

@JsonClass(generateAdapter = true)
data class AdminAuditLogDto(
    val id: String,
    val userId: String?,
    val action: String?,
    val details: String?,
    val timestamp: String?
)
"""
    api = api + admin_dtos

    with open(api_path, 'w', encoding='utf-8') as f:
        f.write(api)

    # 2. Update FacultyRepositoryImpl.kt
    faculty_repo_path = 'app/src/main/java/com/example/core/repository/FacultyRepositoryImpl.kt'
    with open(faculty_repo_path, 'r', encoding='utf-8') as f:
        faculty = f.read()

    # The issue is items.groupBy { it.weekday } mapping to TimetableDay and TimetableItem
    # The actual DTO FacultyTimetableItemDto has dayOfWeek instead of weekday
    # TimetableDay takes (dayName, periods: List<TimetablePeriod>)
    # TimetablePeriod takes (periodNo, time, subjectName, subjectCode, room, facultyName, type)
    
    faculty_replacement = """            val items = response.body() ?: return emptyList()
            // Group flat timetable items by dayOfWeek
            val grouped = items.groupBy { it.dayOfWeek }
            
            return grouped.map { (day, dayItems) ->
                TimetableDay(
                    dayName = day,
                    periods = dayItems.mapIndexed { index, itemDto ->
                        com.example.features.parent.models.TimetablePeriod(
                            periodNo = index + 1,
                            time = "${itemDto.startTime} - ${itemDto.endTime}",
                            subjectName = itemDto.courseName,
                            subjectCode = itemDto.courseCode,
                            room = itemDto.roomNo,
                            facultyName = "", // Not provided in DTO
                            type = itemDto.sessionType
                        )
                    }
                )
            }"""
    
    faculty = re.sub(r'val items = response\.body\(\) \?: return emptyList\(\).*?TimetableItem.*?\)\s*\}\s*\)\s*\}', faculty_replacement, faculty, flags=re.DOTALL)
    
    with open(faculty_repo_path, 'w', encoding='utf-8') as f:
        f.write(faculty)

    # 3. Update HODRepositoryImpl.kt return type mismatch
    hod_repo_path = 'app/src/main/java/com/example/core/repository/HODRepositoryImpl.kt'
    with open(hod_repo_path, 'r', encoding='utf-8') as f:
        hod = f.read()
        
    hod = hod.replace(
        """    override suspend fun getSubstitutions(): Result<List<HODSubstitutionDto>> {
        return try {
            val response = apiService.getSubstitutions()
            if (response.isSuccessful) {
                Result.success(response.body() ?: emptyList())
            } else {
                Result.failure(Exception("Failed to fetch substitutions"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }""",
        """    override suspend fun getSubstitutions(): Result<List<HODSubstitutionDto>> {
        return try {
            val response = apiService.getSubstitutions()
            if (response.isSuccessful) {
                Result.success(response.body() ?: emptyList<HODSubstitutionDto>())
            } else {
                Result.failure(Exception("Failed to fetch substitutions"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }"""
    )
    with open(hod_repo_path, 'w', encoding='utf-8') as f:
        f.write(hod)

    print("Fixed build errors in CamsApiService, FacultyRepositoryImpl, AdminRepository(by adding DTOs), and HODRepositoryImpl")

if __name__ == "__main__":
    fix_all_build_errors()
