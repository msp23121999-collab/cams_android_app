with open('app/src/main/java/com/example/core/network/CamsApiService.kt', 'r', encoding='utf-8') as f:
    content = f.read()

# Fix HODMentorDto
content = content.replace(
    '@Json(name = "faculty_id") val facultyId: String = "",',
    'val faculty_id: String = "",'
)

missing_dtos = """
@JsonClass(generateAdapter = true)
data class HODWorkloadDto(
    val id: String = "",
    val name: String = "",
    val email: String = "",
    val teaching_hours: Int = 0
)
"""

if "data class HODWorkloadDto" not in content:
    content += "\n" + missing_dtos
    print("Added HODWorkloadDto and fixed HODMentorDto")

with open('app/src/main/java/com/example/core/network/CamsApiService.kt', 'w', encoding='utf-8') as f:
    f.write(content)
