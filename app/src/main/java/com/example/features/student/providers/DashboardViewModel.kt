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
import com.example.core.network.SavedCitationDto
import kotlinx.coroutines.async
import kotlinx.coroutines.coroutineScope
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
    val citations: List<SavedCitationDto> = emptyList(),
    val isCitationsLoading: Boolean = false,
    val citationsError: String? = null,
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
                var profile: StudentProfileResponse? = null
                var dashboard: DashboardResponse? = null
                lateinit var notices: List<com.example.core.network.NoticeDto>
                lateinit var academicCalendar: List<com.example.core.network.CalendarEventDto>
                lateinit var courses: List<com.example.core.network.StudentCourseDto>
                lateinit var citations: List<SavedCitationDto>

                coroutineScope {
                    val profileDeferred = async { studentRepository.getProfile() }
                    val dashboardDeferred = async { studentRepository.getDashboard() }
                    val noticesDeferred = async { studentRepository.getNotices() }
                    val calendarDeferred = async { studentRepository.getAcademicCalendar() }
                    val coursesDeferred = async { studentRepository.getCourses() }
                    val citationsDeferred = async { try { studentRepository.getSavedCitations() } catch (e: Exception) { emptyList() } }

                    profile = profileDeferred.await()
                    dashboard = dashboardDeferred.await()
                    notices = noticesDeferred.await()
                    academicCalendar = calendarDeferred.await()
                    courses = coursesDeferred.await()
                    citations = citationsDeferred.await()
                }

                _uiState.update {
                    it.copy(
                        profile = profile,
                        dashboardData = dashboard,
                        courses = courses.map { course ->
                            Course(
                                id = course.id,
                                name = course.name ?: course.subjectName ?: "Untitled Subject",
                                code = course.code ?: course.subjectCode ?: "",
                                credits = course.credits ?: 0,
                                overallCompletion = 0
                            )
                        },
                        notices = notices.map { notice ->
                            Notice(
                                id = notice.id,
                                title = notice.title,
                                category = notice.category ?: "",
                                publisherName = notice.publisherName ?: notice.publisherRole ?: "College",
                                publishDate = notice.date ?: "",
                                priority = notice.priority ?: "Medium"
                            )
                        },
                        calendarEvents = academicCalendar.map { event ->
                            CalendarEvent(
                                id = event.id ?: "",
                                title = if (!event.eventName.isNullOrBlank()) event.eventName else event.title ?: "",
                                startDate = event.date ?: "",
                                category = "Academic",
                                isHoliday = event.isHoliday == true
                            )
                        },
                        citations = citations,
                        isLoading = false
                    )
                }
            } catch (e: Exception) {
                // An IOException here means the request never reached the server
                // (no connectivity, DNS failure, timeout) as opposed to the server
                // returning an error — that distinction is what the offline banner shows.
                val offline = e is java.io.IOException
                _uiState.update {
                    it.copy(
                        profile = null,
                        dashboardData = null,
                        courses = emptyList(),
                        notices = emptyList(),
                        calendarEvents = emptyList(),
                        borrowedBooks = emptyList(),
                        isLoading = false,
                        isOfflineMode = offline,
                        error = if (offline) "You appear to be offline. Showing no data." else (e.message ?: "Failed to load dashboard")
                    )
                }
            }
            }
    }

    fun addCitation(caseName: String, citationText: String, note: String?, onDone: (Boolean) -> Unit = {}) {
        viewModelScope.launch {
            _uiState.update { it.copy(isCitationsLoading = true, citationsError = null) }
            try {
                val created = studentRepository.createSavedCitation(caseName, citationText, note)
                if (created != null) {
                    _uiState.update { it.copy(citations = listOf(created) + it.citations, isCitationsLoading = false) }
                    onDone(true)
                } else {
                    _uiState.update { it.copy(isCitationsLoading = false, citationsError = "Failed to save citation") }
                    onDone(false)
                }
            } catch (e: Exception) {
                _uiState.update { it.copy(isCitationsLoading = false, citationsError = e.message ?: "Failed to save citation") }
                onDone(false)
            }
        }
    }

    fun deleteCitation(id: String) {
        viewModelScope.launch {
            try {
                val success = studentRepository.deleteSavedCitation(id)
                if (success) {
                    _uiState.update { state -> state.copy(citations = state.citations.filterNot { it.id == id }) }
                }
            } catch (e: Exception) {
                // best-effort; ignore
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
