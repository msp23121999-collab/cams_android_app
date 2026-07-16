import re
import os

target = 'app/src/main/java/com/example/features/student/providers/DashboardViewModel.kt'
with open(target, 'r') as f:
    content = f.read()

# We want to add isOfflineMode = false to DashboardState
if 'val isOfflineMode: Boolean' not in content:
    content = content.replace('val error: String? = null', 'val error: String? = null,\n    val isOfflineMode: Boolean = false')

# In catch block, use FallbackData
catch_block = """            } catch (e: Exception) {
                _uiState.update { 
                    it.copy(
                        profile = com.example.features.student.models.FallbackData.profile,
                        dashboardData = com.example.features.student.models.FallbackData.dashboard,
                        courses = com.example.features.student.models.FallbackData.courses,
                        notices = com.example.features.student.models.FallbackData.notices,
                        calendarEvents = com.example.features.student.models.FallbackData.calendarEvents,
                        borrowedBooks = com.example.features.student.models.FallbackData.borrowedBooks,
                        isLoading = false, 
                        isOfflineMode = true,
                        error = e.message ?: "Failed to load dashboard"
                    ) 
                }
            }"""

content = re.sub(r'\} catch \(e: Exception\) \{[^\}]+\}\s*\}', catch_block + '\n        }', content)

with open(target, 'w') as f:
    f.write(content)

