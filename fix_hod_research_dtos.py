with open('app/src/main/java/com/example/core/network/CamsApiService.kt', 'r', encoding='utf-8') as f:
    content = f.read()

missing_dtos = """
@JsonClass(generateAdapter = true)
data class HODResearchMonitoringDto(
    val id: String = "",
    val title: String = "",
    val faculty_name: String = "",
    val type: String = "",
    val status: String = "",
    val latest_progress_percentage: Int = 0
)

@JsonClass(generateAdapter = true)
data class HODPendingProofDto(
    val id: String = "",
    val title: String = "",
    val faculty_name: String = "",
    val journal_name: String = "",
    val issn_isbn: String = ""
)
"""

additions = []
if "data class HODResearchMonitoringDto" not in content:
    additions.append("HODResearchMonitoringDto")
if "data class HODPendingProofDto" not in content:
    additions.append("HODPendingProofDto")

if additions:
    content += "\n" + missing_dtos
    print(f"Added missing DTOs: {', '.join(additions)}")
else:
    print("All DTOs already present")

with open('app/src/main/java/com/example/core/network/CamsApiService.kt', 'w', encoding='utf-8') as f:
    f.write(content)
