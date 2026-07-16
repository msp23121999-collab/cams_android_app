import re

with open('app/src/main/java/com/example/core/network/CamsApiService.kt', 'r', encoding='utf-8') as f:
    content = f.read()

dto_definitions = """
@JsonClass(generateAdapter = true)
data class RoomDto(
    val id: String,
    val name: String,
    val type: String,
    val capacity: Int
)

@JsonClass(generateAdapter = true)
data class BuildingDto(
    val id: String,
    val name: String,
    val floors: Int,
    val rooms: List<RoomDto>
)

@JsonClass(generateAdapter = true)
data class InfrastructureResponseDto(
    val buildings: List<BuildingDto>
)
"""

if "InfrastructureResponseDto" not in content:
    content += "\n" + dto_definitions

with open('app/src/main/java/com/example/core/network/CamsApiService.kt', 'w', encoding='utf-8') as f:
    f.write(content)
