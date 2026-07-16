with open('app/src/main/java/com/example/core/network/CamsApiService.kt', 'r', encoding='utf-8') as f:
    content = f.read()

missing_dtos = """
@JsonClass(generateAdapter = true)
data class SubjectAllocationDto(
    val id: String = "",
    val courseId: String = "",
    val sectionId: String = "",
    val facultyId: String = "",
    @Json(name = "subject_name") val subjectName: String = "",
    @Json(name = "subject_code") val subjectCode: String = ""
)

@JsonClass(generateAdapter = true)
data class AcademicSetupDto(
    @Json(name = "academic_year") val academicYear: String = "",
    val semester: Int = 1
)

@JsonClass(generateAdapter = true)
data class OverdueFacultyDto(
    val facultyName: String = "",
    val department: String = "",
    val publicationTitle: String = "",
    val dueDate: String = ""
)

@JsonClass(generateAdapter = true)
data class PrincipalComplianceResponseDto(
    val completedCount: Int = 0,
    val pendingCount: Int = 0,
    val overdueCount: Int = 0,
    val overdueFacultyList: List<OverdueFacultyDto> = emptyList()
)
"""

additions = []
if "data class SubjectAllocationDto" not in content:
    additions.append("SubjectAllocationDto")
if "data class AcademicSetupDto" not in content:
    additions.append("AcademicSetupDto")
if "data class OverdueFacultyDto" not in content:
    additions.append("OverdueFacultyDto")
if "data class PrincipalComplianceResponseDto" not in content:
    additions.append("PrincipalComplianceResponseDto")

if additions:
    content += "\n" + missing_dtos
    print(f"Added missing DTOs: {', '.join(additions)}")
else:
    print("All DTOs already present")

with open('app/src/main/java/com/example/core/network/CamsApiService.kt', 'w', encoding='utf-8') as f:
    f.write(content)
