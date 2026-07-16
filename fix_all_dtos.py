import re

with open('app/src/main/java/com/example/core/network/CamsApiService.kt', 'r', encoding='utf-8') as f:
    content = f.read()

content = re.sub(
    r'data class ResearchComplianceDto\([^)]+\)',
    r'data class ResearchComplianceDto(\n    val id: String,\n    val title: String,\n    val researcher: String,\n    val status: String,\n    @Json(name = "compliance_check") val complianceCheck: String,\n    val completedCount: Int = 0,\n    val pendingCount: Int = 0,\n    val overdueCount: Int = 0,\n    val overdueFacultyList: List<String> = emptyList(),\n    val facultyName: String = "",\n    val department: String = "",\n    val publicationTitle: String = "",\n    val dueDate: String = ""\n)',
    content
)

content = re.sub(
    r'data class HODSubstitutionDto\([^)]+\)',
    r'data class HODSubstitutionDto(\n    val id: String,\n    @Json(name = "faculty_id") val facultyId: String,\n    @Json(name = "substitute_id") val substituteId: String,\n    val date: String,\n    val period: Int,\n    val status: String = "",\n    val subject: String = "",\n    val absent_faculty: String = "",\n    val substitute_faculty: String = ""\n)',
    content
)

content = re.sub(
    r'data class SubjectAllocationDto\([^)]+\)',
    r'data class SubjectAllocationDto(\n    val id: String,\n    @Json(name = "subject_name") val subjectName: String,\n    @Json(name = "subject_code") val subjectCode: String,\n    val semester: Int,\n    @Json(name = "faculty_name") val facultyName: String,\n    val courseId: String = "",\n    val facultyId: String = ""\n)',
    content
)

with open('app/src/main/java/com/example/core/network/CamsApiService.kt', 'w', encoding='utf-8') as f:
    f.write(content)
