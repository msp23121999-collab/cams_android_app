with open('app/src/main/java/com/example/core/network/CamsApiService.kt', 'r', encoding='utf-8') as f:
    content = f.read()

missing_dtos = """
@JsonClass(generateAdapter = true)
data class HODAcademicMonitoringDto(
    val subject: String = "",
    val faculty: String = "",
    val completion: Int = 0
)

@JsonClass(generateAdapter = true)
data class HODAttendanceMonitoringDto(
    val subject: String = "",
    val subjectCode: String = "",
    val semester: Int = 1,
    val studentsCount: Int = 0,
    val attendancePercentage: Double = 0.0,
    val lowAttendanceCount: Int = 0
)
"""

additions = []
if "data class HODAcademicMonitoringDto" not in content:
    additions.append("HODAcademicMonitoringDto")
if "data class HODAttendanceMonitoringDto" not in content:
    additions.append("HODAttendanceMonitoringDto")

if additions:
    content += "\n" + missing_dtos
    print(f"Added missing DTOs: {', '.join(additions)}")
else:
    print("All DTOs already present")

with open('app/src/main/java/com/example/core/network/CamsApiService.kt', 'w', encoding='utf-8') as f:
    f.write(content)
