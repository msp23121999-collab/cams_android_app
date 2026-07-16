import re

def update_file():
    # 1. Update StudentRepository.kt
    student_repo_path = 'app/src/main/java/com/example/core/repository/StudentRepository.kt'
    with open(student_repo_path, 'r', encoding='utf-8') as f:
        student_content = f.read()
    
    student_content = student_content.replace(
        'return if (response.isSuccessful) response.body()?.events ?: emptyList() else emptyList()',
        'return if (response.isSuccessful) response.body() ?: emptyList() else emptyList()'
    )
    with open(student_repo_path, 'w', encoding='utf-8') as f:
        f.write(student_content)

    # 2. Update PrincipalRepository.kt
    principal_repo_path = 'app/src/main/java/com/example/core/repository/PrincipalRepository.kt'
    with open(principal_repo_path, 'r', encoding='utf-8') as f:
        principal_content = f.read()

    principal_content = principal_content.replace(
        'suspend fun getResearchCompliance(): com.example.core.network.PrincipalComplianceResponseDto?',
        'suspend fun getResearchCompliance(): List<com.example.core.network.ResearchComplianceDto>?'
    )
    principal_content = principal_content.replace(
        'suspend fun getInfrastructureDetails(): com.example.core.network.InfrastructureResponseDto?',
        'suspend fun getInfrastructureDetails(): com.example.core.network.InfrastructureDto?'
    )
    principal_content = principal_content.replace(
        'return response.body()?.events ?: emptyList()',
        'return response.body() ?: emptyList()'
    )

    with open(principal_repo_path, 'w', encoding='utf-8') as f:
        f.write(principal_content)

    # 3. Update CamsApiService.kt
    api_service_path = 'app/src/main/java/com/example/core/network/CamsApiService.kt'
    with open(api_service_path, 'r', encoding='utf-8') as f:
        api_content = f.read()

    api_content = api_content.replace(
        '@Json(name = "total_students") val totalStudents: Int,',
        '@Json(name = "total_students") val totalStudents: Int,\n    val totalDepartments: Int? = null,\n    val totalStaff: Int? = null,'
    )
    api_content = api_content.replace(
        'suspend fun getParentChildAttendance(@Query("child_id") childId: String?): Response<List<ParentChildAttendanceDto>>',
        'suspend fun getParentChildAttendance(@Query("child_id") childId: String?): Response<ParentChildAttendanceOverviewDto>'
    )
    api_content = api_content.replace(
        'data class ParentChildTimetableDayDto(\n    val day: String,\n    val periods: List<ParentChildTimetablePeriodDto>\n)',
        'data class ParentChildTimetableDayDto(\n    val dayOfWeek: String = "",\n    val startTime: String = "",\n    val endTime: String = "",\n    val subjectName: String = "",\n    val subjectCode: String = "",\n    val roomNo: String = "",\n    val facultyName: String = ""\n)'
    )

    # Add missing dtos
    missing_dtos = """
@JsonClass(generateAdapter = true)
data class ParentChildAttendanceOverviewDto(
    val percentage: Float = 0f,
    val total: Int = 0,
    val present: Int = 0,
    val absent: Int = 0,
    val od: Int = 0,
    val records: List<ParentChildAttendanceDto> = emptyList()
)

@JsonClass(generateAdapter = true)
data class VerificationRequestDto(
    val status: String,
    val remarks: String
)

@JsonClass(generateAdapter = true)
data class HODTeachingLogsDashboardDto(
    val total_lectures_conducted: Int = 0,
    val pending_diaries_count: Int = 0,
    val syllabus_status: List<HODAcademicMonitoringDto> = emptyList()
)
"""
    if "data class ParentChildAttendanceOverviewDto" not in api_content:
        api_content += "\n" + missing_dtos
    
    with open(api_service_path, 'w', encoding='utf-8') as f:
        f.write(api_content)
        
    print("Done applying fixes.")

if __name__ == "__main__":
    update_file()
