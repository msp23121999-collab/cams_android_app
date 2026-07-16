with open('app/src/main/java/com/example/core/repository/HODRepositoryImpl.kt', 'r', encoding='utf-8') as f:
    content = f.read()

# Fix getAcademicSetup
old_setup = """    override suspend fun getAcademicSetup(): Result<com.example.core.network.AcademicSetupDto> {
        return try {
            val response = apiService.getAcademicSetup()
            if (response.isSuccessful) {
                response.body()?.let { Result.success(it) } ?: Result.failure(Exception("Empty body"))
            } else {
                Result.failure(Exception("Failed: ${response.code()}"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }"""
new_setup = """    override suspend fun getAcademicSetup(): Result<com.example.core.network.AcademicSetupDto> {
        return try {
            val response = apiService.getAcademicSetup()
            if (response.isSuccessful && response.body() != null) {
                Result.success(response.body()!!)
            } else {
                Result.failure(Exception("Failed or empty"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }"""
content = content.replace(old_setup, new_setup)

# Fix getSubjectAllocations
old_alloc = """    override suspend fun getSubjectAllocations(): Result<List<com.example.core.network.SubjectAllocationDto>> {
        return try {
            val response = apiService.getSubjectAllocations()
            if (response.isSuccessful) {
                response.body()?.let { Result.success(it) } ?: Result.failure(Exception("Empty body"))
            } else {
                Result.failure(Exception("Failed: ${response.code()}"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }"""
new_alloc = """    override suspend fun getSubjectAllocations(): Result<List<com.example.core.network.SubjectAllocationDto>> {
        return try {
            val response = apiService.getSubjectAllocations()
            if (response.isSuccessful && response.body() != null) {
                Result.success(response.body()!!)
            } else {
                Result.failure(Exception("Failed or empty"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }"""
content = content.replace(old_alloc, new_alloc)

# Fix getSubstitutions
old_subs = """    override suspend fun getSubstitutions(): Result<List<com.example.core.network.HODSubstitutionDto>> {
        return try {
            val response = apiService.getSubstitutions()
            if (response.isSuccessful) {
                Result.success(response.body() ?: emptyList())
            } else {
                Result.failure(Exception("Failed: ${response.code()}"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }"""
new_subs = """    override suspend fun getSubstitutions(): Result<List<com.example.core.network.HODSubstitutionDto>> {
        return try {
            val response = apiService.getSubstitutions()
            if (response.isSuccessful) {
                val body = response.body()
                if (body != null) Result.success(body) else Result.success(emptyList())
            } else {
                Result.failure(Exception("Failed: ${response.code()}"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }"""
content = content.replace(old_subs, new_subs)

with open('app/src/main/java/com/example/core/repository/HODRepositoryImpl.kt', 'w', encoding='utf-8') as f:
    f.write(content)

# Update CamsApiService.kt
api_path = 'app/src/main/java/com/example/core/network/CamsApiService.kt'
with open(api_path, 'r', encoding='utf-8') as f:
    api_content = f.read()

missing_apis = """
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

if "suspend fun getHODTeachingLogsDashboard()" not in api_content:
    # insert before @JsonClass
    idx = api_content.rfind('@JsonClass')
    api_content = api_content[:idx] + missing_apis + "\n" + api_content[idx:]

missing_dtos = """
@JsonClass(generateAdapter = true)
data class MentorAssignmentRequestDto(
    val studentId: String,
    val facultyId: String
)

@JsonClass(generateAdapter = true)
data class HODSyllabusMetadataDto(
    val totalSubjects: Int = 0,
    val completedSubjects: Int = 0,
    val delayedSubjects: Int = 0
)

@JsonClass(generateAdapter = true)
data class HODSyllabusCourseDto(
    val courseName: String = "",
    val facultyName: String = "",
    val completionPercentage: Int = 0,
    val status: String = "ON_TRACK"
)
"""

if "data class MentorAssignmentRequestDto" not in api_content:
    api_content += "\n" + missing_dtos

with open(api_path, 'w', encoding='utf-8') as f:
    f.write(api_content)
