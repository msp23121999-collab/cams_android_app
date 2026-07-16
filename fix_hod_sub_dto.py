import re

with open('app/src/main/java/com/example/core/network/CamsApiService.kt', 'r', encoding='utf-8') as f:
    content = f.read()

dto_definitions = """
@JsonClass(generateAdapter = true)
data class HODSubstitutionDto(
    val id: String,
    @Json(name = "faculty_id") val facultyId: String,
    @Json(name = "substitute_id") val substituteId: String,
    val date: String,
    val period: Int,
    val status: String = ""
)
"""

if "HODSubstitutionDto" not in content:
    content += "\n" + dto_definitions

with open('app/src/main/java/com/example/core/network/CamsApiService.kt', 'w', encoding='utf-8') as f:
    f.write(content)
