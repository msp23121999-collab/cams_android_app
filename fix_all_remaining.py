import re

def fix_all():
    # 1. Fix CamsApiService.kt - add FacultyTimetableItemDto, fix ParentChildProfileDto and ParentChildMarkDto
    api_path = 'app/src/main/java/com/example/core/network/CamsApiService.kt'
    with open(api_path, 'r', encoding='utf-8') as f:
        api = f.read()

    # Add missing FacultyTimetableItemDto
    if "data class FacultyTimetableItemDto" not in api:
        api += """
@JsonClass(generateAdapter = true)
data class FacultyTimetableItemDto(
    val dayOfWeek: String = "",
    val startTime: String = "",
    val endTime: String = "",
    val courseName: String = "",
    val sessionType: String = "",
    val roomNo: String = "",
    val degreeName: String = "",
    val semester: String = ""
)
"""
        print("Added FacultyTimetableItemDto")

    # Fix ParentChildProfileDto - add missing fields batchYear, email
    api = api.replace(
        "data class ParentChildProfileDto(\n    val id: String,\n    @Json(name = \"full_name\") val fullName: String,\n    @Json(name = \"roll_no\") val rollNo: String,\n    val semester: String,\n    val batch: String,\n    val cgpa: Double,\n    @Json(name = \"mentor_name\") val mentorName: String,\n    @Json(name = \"mentor_email\") val mentorEmail: String,\n    @Json(name = \"mentor_phone\") val mentorPhone: String,\n    val dob: String,",
        "data class ParentChildProfileDto(\n    val id: String = \"\",\n    @Json(name = \"full_name\") val fullName: String = \"\",\n    @Json(name = \"roll_no\") val rollNo: String = \"\",\n    val semester: String = \"\",\n    val batch: String? = null,\n    @Json(name = \"batch_year\") val batchYear: String? = null,\n    val cgpa: Double? = 0.0,\n    @Json(name = \"mentor_name\") val mentorName: String? = \"N/A\",\n    @Json(name = \"mentor_email\") val mentorEmail: String? = \"\",\n    @Json(name = \"mentor_phone\") val mentorPhone: String? = \"\",\n    val email: String? = \"\",\n    val dob: String? = \"\","
    )
    print("Fixed ParentChildProfileDto")

    # Fix ParentChildMarkDto - add missing fields
    api = api.replace(
        "data class ParentChildMarkDto(\n    val subject: String,\n    @Json(name = \"academic_year\") val academicYear: String,\n    @Json(name = \"internal_1\") val internal1: String,\n    @Json(name = \"internal_2\") val internal2: String,\n    val model: String,\n    val assignments: String,\n    val attendance: String,\n    val total: String\n)",
        "data class ParentChildMarkDto(\n    val subject: String = \"\",\n    val subjectName: String = \"\",\n    @Json(name = \"academic_year\") val academicYear: String = \"\",\n    @Json(name = \"internal_1\") val internal1: String = \"\",\n    @Json(name = \"internal_2\") val internal2: String = \"\",\n    val model: String = \"\",\n    val assignments: String = \"\",\n    val attendance: String = \"\",\n    val total: String = \"\",\n    val internalExamMark: Double = 0.0,\n    val assignmentMark: Double = 0.0,\n    val presentationMark: Double = 0.0,\n    val vivaVoiceMark: Double = 0.0,\n    val attendanceMark: Double = 0.0,\n    val totalMark: Double = 0.0\n)"
    )
    print("Fixed ParentChildMarkDto")

    with open(api_path, 'w', encoding='utf-8') as f:
        f.write(api)

    # 2. Fix PrincipalViewModel.kt - update type references
    pvm_path = 'app/src/main/java/com/example/features/principal/providers/PrincipalViewModel.kt'
    with open(pvm_path, 'r', encoding='utf-8') as f:
        pvm = f.read()

    pvm = pvm.replace(
        'val compliance: com.example.core.network.PrincipalComplianceResponseDto? = null,',
        'val compliance: List<com.example.core.network.ResearchComplianceDto>? = null,'
    )
    pvm = pvm.replace(
        'val data: com.example.core.network.InfrastructureResponseDto? = null,',
        'val data: com.example.core.network.InfrastructureDto? = null,'
    )

    with open(pvm_path, 'w', encoding='utf-8') as f:
        f.write(pvm)
    print("Fixed PrincipalViewModel.kt")

    # 3. Fix PrincipalRepository.kt - events access
    pr_path = 'app/src/main/java/com/example/core/repository/PrincipalRepository.kt'
    with open(pr_path, 'r', encoding='utf-8') as f:
        pr = f.read()

    pr = pr.replace(
        "return response.body()?.events ?: emptyList()",
        "return response.body() ?: emptyList()"
    )
    with open(pr_path, 'w', encoding='utf-8') as f:
        f.write(pr)
    print("Fixed PrincipalRepository.kt")

    print("\nAll fixes applied successfully!")

if __name__ == "__main__":
    fix_all()
