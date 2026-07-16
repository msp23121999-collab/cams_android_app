import re

with open('app/src/main/java/com/example/core/network/CamsApiService.kt', 'r', encoding='utf-8') as f:
    content = f.read()

dto_definitions = """
@JsonClass(generateAdapter = true)
data class HODSyllabusMetadataDto(
    val semCount: Int = 10
)

@JsonClass(generateAdapter = true)
data class HODCourseDto(
    val id: String,
    val name: String = "",
    val code: String = "",
    val semester: Int = 1,
    val credits: Int = 3
)

@JsonClass(generateAdapter = true)
data class PrincipalComplianceResponseDto(
    val data: List<ResearchComplianceDto> = emptyList()
)
"""

if "HODSyllabusMetadataDto" not in content:
    content += "\n" + dto_definitions

# And fix HODSubstitutionDto to have 'status'
content = re.sub(
    r'(data class SubstitutionDto\(\s*val id: String,\s*@Json\(name = "faculty_id"\) val facultyId: String,\s*@Json\(name = "substitute_id"\) val substituteId: String,\s*val date: String,\s*val period: Int\s*\))',
    r'data class SubstitutionDto(\n    val id: String,\n    @Json(name = "faculty_id") val facultyId: String,\n    @Json(name = "substitute_id") val substituteId: String,\n    val date: String,\n    val period: Int,\n    val status: String = ""\n)',
    content
)


with open('app/src/main/java/com/example/core/network/CamsApiService.kt', 'w', encoding='utf-8') as f:
    f.write(content)
