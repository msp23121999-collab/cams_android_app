package com.example.features.faculty.providers

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.core.repository.FacultyRepository
import com.example.features.faculty.models.FacultyDashboardMetrics
import com.example.features.faculty.models.FacultySubject
import com.example.features.faculty.models.ActivitySummary
import com.example.features.parent.models.CollegeNotice
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import kotlinx.coroutines.async
import kotlinx.coroutines.coroutineScope

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Assignment
import androidx.compose.material.icons.filled.Notifications

import java.io.IOException

data class RecentActivity(
    val title: String,
    val time: String,
    val icon: ImageVector,
    val color: Color
)

data class UpcomingEvent(
    val title: String,
    val date: String,
    val time: String,
    val color: Color
)

data class FacultyDashboardState(
    val metrics: FacultyDashboardMetrics = FacultyDashboardMetrics(),
    val assignedSubjects: List<FacultySubject> = emptyList(),
    val activitySummary: ActivitySummary = ActivitySummary(),
    val notices: List<CollegeNotice> = emptyList(),
    val recentActivities: List<RecentActivity> = emptyList(),
    val upcomingEvents: List<UpcomingEvent> = emptyList(),
    val isLoading: Boolean = true,
    val isRefreshing: Boolean = false,
    val isOffline: Boolean = false,
    val error: String? = null
)

class FacultyDashboardViewModel(private val repository: FacultyRepository) : ViewModel() {
    private val _uiState = MutableStateFlow(FacultyDashboardState())
    val uiState: StateFlow<FacultyDashboardState> = _uiState.asStateFlow()

    init {
        loadDashboardData()
    }

    fun loadDashboardData(isRefresh: Boolean = false) {
        viewModelScope.launch {
            if (isRefresh) {
                _uiState.update { it.copy(isRefreshing = true, error = null, isOffline = false) }
            } else {
                _uiState.update { it.copy(isLoading = true, error = null, isOffline = false) }
            }
            
            try {
                var loadError: String? = null
                lateinit var metrics: FacultyDashboardMetrics
                lateinit var subjects: List<FacultySubject>
                lateinit var summary: ActivitySummary
                lateinit var notices: List<CollegeNotice>
                lateinit var notifications: List<com.example.core.network.NotificationDto>

                coroutineScope {
                    val metricsDeferred = async {
                        try { repository.getDashboardMetrics() } catch (e: Exception) { loadError = e.message; FacultyDashboardMetrics() }
                    }
                    val subjectsDeferred = async {
                        try { repository.getAssignedSubjects() } catch (e: Exception) { loadError = e.message; emptyList() }
                    }
                    val summaryDeferred = async {
                        try { repository.getActivitySummary() } catch (e: Exception) { loadError = e.message; ActivitySummary() }
                    }
                    val noticesDeferred = async {
                        try { repository.getNotices() } catch (e: Exception) { loadError = e.message; emptyList() }
                    }
                    val notificationsDeferred = async {
                        try { repository.getFacultyNotifications() } catch (e: Exception) { loadError = e.message; emptyList() }
                    }

                    metrics = metricsDeferred.await()
                    subjects = subjectsDeferred.await()
                    summary = summaryDeferred.await()
                    notices = noticesDeferred.await()
                    notifications = notificationsDeferred.await()
                }

                // Map notices to upcoming events
                val mappedEvents = notices.take(3).map { notice ->
                    UpcomingEvent(
                        title = notice.title,
                        date = notice.publishDate,
                        time = "", // Notices usually don't have time
                        color = Color(0xFF3B82F6) // Blue
                    )
                }

                // Map notifications to recent activities (fallback to summary if empty)
                val mappedActivities = if (notifications.isNotEmpty()) {
                    notifications.take(5).map { notif ->
                        RecentActivity(
                            title = notif.title ?: "Notification",
                            time = notif.date ?: "Just now",
                            icon = Icons.Filled.Notifications,
                            color = Color(0xFF10B981) // Green
                        )
                    }
                } else {
                    listOf(
                        RecentActivity("Classes Conducted", "${summary.classesConducted} this week", Icons.Filled.Assignment, Color(0xFFF59E0B)),
                        RecentActivity("Materials Uploaded", "${summary.studyMaterialsUploaded} total", Icons.Filled.Assignment, Color(0xFF8B5CF6))
                    )
                }
                
                _uiState.update { it.copy(
                    metrics = metrics,
                    assignedSubjects = subjects,
                    activitySummary = summary,
                    notices = notices,
                    upcomingEvents = mappedEvents,
                    recentActivities = mappedActivities,
                    isLoading = false,
                    isRefreshing = false,
                    isOffline = false,
                    error = loadError
                ) }
            } catch (e: IOException) {
                _uiState.update { it.copy(
                    isLoading = false,
                    isRefreshing = false,
                    isOffline = true,
                    error = "Network error. Please check your connection."
                ) }
            } catch (e: Exception) {
                _uiState.update { it.copy(
                    isLoading = false,
                    isRefreshing = false,
                    error = e.message ?: "An unknown error occurred"
                ) }
            }
        }
    }
    
    fun refresh() {
        loadDashboardData(isRefresh = true)
    }
    
    fun clearError() {
        _uiState.update { it.copy(error = null) }
    }
}

class FacultyDashboardViewModelFactory(private val repository: FacultyRepository) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(FacultyDashboardViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return FacultyDashboardViewModel(repository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
