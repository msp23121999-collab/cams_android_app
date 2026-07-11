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

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Assignment
import androidx.compose.material.icons.filled.Notifications

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
    val error: String? = null
)

class FacultyDashboardViewModel(private val repository: FacultyRepository) : ViewModel() {
    private val _uiState = MutableStateFlow(FacultyDashboardState())
    val uiState: StateFlow<FacultyDashboardState> = _uiState.asStateFlow()

    init {
        loadDashboardData()
    }

    fun loadDashboardData() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null) }
            try {
                val metrics = repository.getDashboardMetrics()
                val subjects = repository.getAssignedSubjects()
                val summary = repository.getActivitySummary()
                val notices = repository.getNotices()
                
                _uiState.update { it.copy(
                    metrics = metrics,
                    assignedSubjects = subjects,
                    activitySummary = summary,
                    notices = notices,
                    isLoading = false
                ) }
            } catch (e: Exception) {
                _uiState.update { it.copy(isLoading = false, error = e.message) }
            }
        }
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
