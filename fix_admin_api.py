import re

def fix_admin_repository_and_api():
    api_path = 'app/src/main/java/com/example/core/network/CamsApiService.kt'
    with open(api_path, 'r', encoding='utf-8') as f:
        api = f.read()

    # Add missing endpoints
    admin_missing_endpoints = """
    @GET("admins/users")
    suspend fun getAllUsers(): Response<List<AdminUserDto>>

    @GET("admins/departments")
    suspend fun getDepartmentsList(): Response<List<AdminDepartmentDto>>

    @GET("admins/degrees-list")
    suspend fun getDegreesList(): Response<List<AdminDegreeDto>>

    @GET("admins/courses-list")
    suspend fun getAllCourses(): Response<List<AdminCourseDto>>

    @GET("admins/backups-list")
    suspend fun getBackups(): Response<List<AdminBackupDto>>

    @GET("admins/attendance-defaulters-admin")
    suspend fun getAttendanceDefaultersAdmin(): Response<List<AdminAttendanceDefaulterDto>>

    @GET("admins/fee-structures")
    suspend fun getAdminFeeStructures(): Response<List<AdminFeeStructureDto>>

    @GET("admins/scholarship-types")
    suspend fun getAdminScholarshipTypes(): Response<List<AdminScholarshipTypeDto>>

    @GET("admins/search-fees")
    suspend fun searchStudentsForFees(@Query("query") query: String): Response<List<Any>>
"""

    # Add to the end of interface
    interface_end_match = re.search(r'^}(\s*)$', api, flags=re.MULTILINE)
    if interface_end_match:
        insert_pos = interface_end_match.start()
        api = api[:insert_pos] + admin_missing_endpoints + "\n" + api[insert_pos:]

    # Add missing DTOs
    admin_missing_dtos = """
@JsonClass(generateAdapter = true)
data class AdminUserDto(
    val id: String,
    val email: String,
    val phone: String?,
    val fullName: String,
    val role: String,
    val isActive: Boolean,
    val departmentId: String?
)

@JsonClass(generateAdapter = true)
data class AdminDepartmentDto(
    val id: String,
    val code: String,
    val name: String,
    val hodId: String?
)

@JsonClass(generateAdapter = true)
data class AdminDegreeDto(
    val id: String, 
    val name: String, 
    val code: String,
    val durationYears: Int?,
    val programLevel: String?
)

@JsonClass(generateAdapter = true)
data class AdminCourseDto(
    val id: String,
    val code: String,
    val name: String,
    val semester: Int,
    val credits: Int?
)

@JsonClass(generateAdapter = true)
data class AdminAttendanceDefaulterDto(
    val studentId: String,
    val studentName: String?,
    val department: String?,
    val attendancePercentage: Double?,
    val status: String?
)

@JsonClass(generateAdapter = true)
data class AdminFeeStructureDto(
    val id: String,
    val name: String?,
    val amount: Double?,
    val semester: Int?,
    val departmentId: String?
)

@JsonClass(generateAdapter = true)
data class AdminScholarshipTypeDto(
    val id: String,
    val name: String?,
    val percentage: Double?
)
"""
    api = api + admin_missing_dtos

    with open(api_path, 'w', encoding='utf-8') as f:
        f.write(api)

    print("Added missing Admin endpoints and DTOs")

if __name__ == "__main__":
    fix_admin_repository_and_api()
