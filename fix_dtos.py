import re

with open('app/src/main/java/com/example/core/network/CamsApiService.kt', 'r', encoding='utf-8') as f:
    content = f.read()

# Fix CalendarEventDto
content = re.sub(
    r'(data class CalendarEventDto\(\s*val id: String,\s*val date: String,\s*@Json\(name = "event_name"\) val eventName: String,\s*@Json\(name = "is_holiday"\) val isHoliday: Boolean\s*\))',
    r'data class CalendarEventDto(\n    val id: String,\n    val date: String,\n    @Json(name = "event_name") val eventName: String,\n    @Json(name = "is_holiday") val isHoliday: Boolean,\n    val title: String? = null\n)',
    content
)

# Fix InternalMarkDto
content = re.sub(
    r'(data class InternalMarkDto\(\s*val id: String,\s*@Json\(name = "subject_name"\) val subjectName: String,\s*@Json\(name = "subject_code"\) val subjectCode: String,\s*@Json\(name = "mark_obtained"\) val markObtained: Double,\s*@Json\(name = "max_mark"\) val maxMark: Double,\s*val component: String\s*\))',
    r'data class InternalMarkDto(\n    val id: String,\n    @Json(name = "subject_name") val subjectName: String,\n    @Json(name = "subject_code") val subjectCode: String,\n    @Json(name = "mark_obtained") val markObtained: Double,\n    @Json(name = "max_mark") val maxMark: Double,\n    val component: String,\n    val examType: String = "",\n    val mark: Double? = null\n)',
    content
)

# Fix InfrastructureDto
content = re.sub(
    r'(data class InfrastructureDto\(\s*val id: String,\s*@Json\(name = "room_no"\) val roomNo: String,\s*@Json\(name = "building_name"\) val buildingName: String,\s*@Json\(name = "room_type"\) val roomType: String,\s*val floor: Int,\s*@Json\(name = "is_active"\) val isActive: Boolean\s*\))',
    r'data class InfrastructureDto(\n    val id: String,\n    @Json(name = "room_no") val roomNo: String,\n    @Json(name = "building_name") val buildingName: String,\n    @Json(name = "room_type") val roomType: String,\n    val floor: Int,\n    @Json(name = "is_active") val isActive: Boolean,\n    val name: String = "",\n    val type: String = "",\n    val capacity: Int = 0\n)',
    content
)

# Fix ResearchComplianceDto
content = re.sub(
    r'(data class ResearchComplianceDto\(\s*val id: String,\s*@Json\(name = "faculty_id"\) val facultyId: String,\s*@Json\(name = "compliance_type"\) val complianceType: String,\s*val status: String,\s*@Json\(name = "due_date"\) val dueDate: String\s*\))',
    r'data class ResearchComplianceDto(\n    val id: String,\n    @Json(name = "faculty_id") val facultyId: String,\n    @Json(name = "compliance_type") val complianceType: String,\n    val status: String,\n    @Json(name = "due_date") val dueDate: String,\n    val completedCount: Int = 0,\n    val pendingCount: Int = 0,\n    val overdueCount: Int = 0,\n    val overdueFacultyList: List<String> = emptyList(),\n    val facultyName: String = "",\n    val department: String = "",\n    val publicationTitle: String = ""\n)',
    content
)

with open('app/src/main/java/com/example/core/network/CamsApiService.kt', 'w', encoding='utf-8') as f:
    f.write(content)
