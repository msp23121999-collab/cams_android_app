with open('app/src/main/java/com/example/core/network/CamsApiService.kt', 'r', encoding='utf-8') as f:
    content = f.read()

# Update HODResearchMonitoringDto to include area
content = content.replace(
    'val latest_progress_percentage: Int = 0\n)',
    'val latest_progress_percentage: Int = 0,\n    val area: String = ""\n)'
)

missing_dtos = """
@JsonClass(generateAdapter = true)
data class HODMentorDto(
    @Json(name = "faculty_id") val facultyId: String = "",
    val faculty_name: String = "",
    val department: String = "",
    val total_students: Int = 0
)

@JsonClass(generateAdapter = true)
data class HODDepartmentReportDto(
    val totalFaculty: Int = 0,
    val totalStudents: Int = 0,
    val attendanceAverage: String = "0%",
    val syllabusCompletionAverage: String = "0%",
    val performanceDistribution: Map<String, Int> = emptyMap()
)
"""

additions = []
if "data class HODMentorDto" not in content:
    additions.append("HODMentorDto")
if "data class HODDepartmentReportDto" not in content:
    additions.append("HODDepartmentReportDto")

if additions:
    content += "\n" + missing_dtos
    print(f"Added missing DTOs: {', '.join(additions)}")
else:
    print("All DTOs already present")

with open('app/src/main/java/com/example/core/network/CamsApiService.kt', 'w', encoding='utf-8') as f:
    f.write(content)
