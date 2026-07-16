package com.example.features.student.providers

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.features.student.models.Course
import com.example.features.student.models.DashboardResponse
import com.example.features.student.models.MetricSchema
import com.example.features.student.models.Notice
import com.example.features.student.models.StudentProfileResponse
import com.example.features.student.models.CalendarEvent
import com.example.features.student.models.LibraryBook
import com.example.features.student.models.CareerStatus
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

import com.example.core.repository.StudentRepository

data class DashboardState(
    val profile: StudentProfileResponse? = null,
    val dashboardData: DashboardResponse? = null,
    val courses: List<Course> = emptyList(),
    val notices: List<Notice> = emptyList(),
    val calendarEvents: List<CalendarEvent> = emptyList(),
    val borrowedBooks: List<LibraryBook> = emptyList(),
    val careerStatuses: List<CareerStatus> = emptyList(),
    val isLoading: Boolean = true,
    val error: String? = null,
    val isOfflineMode: Boolean = false
)

class DashboardViewModel(
    private val studentRepository: StudentRepository
) : ViewModel() {
    private val _uiState = MutableStateFlow(DashboardState())
    val uiState: StateFlow<DashboardState> = _uiState.asStateFlow()

    init {
        fetchDashboardData()
    }

    fun fetchDashboardData() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null) }
            try {
                val profile = studentRepository.getProfile()
                val dashboard = studentRepository.getDashboard()
                val notices = studentRepository.getNotices()
                val academicCalendar = studentRepository.getAcademicCalendar()
                
                _uiState.update { 
                    it.copy(
                        profile = profile,
                        dashboardData = dashboard,
                        notices = notices.map { notice ->
                            Notice(
                                id = notice.id,
                                title = notice.title,
                                category = notice.category ?: "",
                                publisherName = "University",
                                publishDate = notice.date ?: "",
                                priority = "High" // Default
                            )
                        },
                        calendarEvents = academicCalendar.map { event ->
                            CalendarEvent(
                                id = event.id,
                                title = if (event.eventName.isNotBlank()) event.eventName else event.title ?: "",
                                startDate = event.date,
                                category = "Academic",
                                isHoliday = event.isHoliday
                            )
                        },
                        isLoading = false
                    )
                }
            } catch (e: Exception) {
                _uiState.update { 
                    it.copy(
                        profile = null,
                        dashboardData = null,
                        courses = emptyList(),
                        notices = emptyList(),
                        calendarEvents = emptyList(),
                        borrowedBooks = emptyList(),
                        isLoading = false, 
                        isOfflineMode = false,
                        error = e.message ?: "Failed to load dashboard"
                    ) 
                }
            }
            }
    }
}

class DashboardViewModelFactory(
    private val studentRepository: StudentRepository
) : androidx.lifecycle.ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(DashboardViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return DashboardViewModel(studentRepository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
