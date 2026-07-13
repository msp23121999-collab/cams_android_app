
package com.example.features.faculty.providers

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.core.network.*
import com.example.core.repository.FacultyRepository
import com.example.features.faculty.models.ResearchEntry
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class FacultyResearchState(
    val researchEntries: List<ResearchEntry> = emptyList(),
    val mentorStudents: List<FacultyMentorshipStudentDto> = emptyList(),
    val isLoading: Boolean = false,
    val error: String? = null
)

class FacultyResearchViewModel(private val repository: FacultyRepository) : ViewModel() {
    private val _uiState = MutableStateFlow(FacultyResearchState())
    val uiState: StateFlow<FacultyResearchState> = _uiState.asStateFlow()

    init {
        loadData()
    }

    fun loadData() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null) }
            try {
                val entries = repository.getResearchEntries()
                val students = repository.getMentorStudents()
                _uiState.update { it.copy(
                    researchEntries = entries,
                    mentorStudents = students,
                    isLoading = false
                ) }
            } catch (e: Exception) {
                _uiState.update { it.copy(isLoading = false, error = e.message) }
            }
        }
    }
}

class FacultyResearchViewModelFactory(private val repository: FacultyRepository) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T = FacultyResearchViewModel(repository) as T
}

data class FacultyInternshipsState(
    val drives: List<FacultyInternshipDriveDto> = emptyList(),
    val isLoading: Boolean = false,
    val error: String? = null
)

class FacultyInternshipsViewModel(private val repository: FacultyRepository) : ViewModel() {
    private val _uiState = MutableStateFlow(FacultyInternshipsState())
    val uiState = _uiState.asStateFlow()

    init { loadDrives() }

    fun loadDrives() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null) }
            try {
                val data = repository.getInternshipDrives()
                _uiState.update { it.copy(drives = data, isLoading = false) }
            } catch (e: Exception) {
                _uiState.update { it.copy(isLoading = false, error = e.message) }
            }
        }
    }
}

class FacultyInternshipsViewModelFactory(private val repository: FacultyRepository) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T = FacultyInternshipsViewModel(repository) as T
}

data class FacultyLegalEventsState(
    val events: List<FacultyLegalEventDto> = emptyList(),
    val isLoading: Boolean = false,
    val error: String? = null
)

class FacultyLegalEventsViewModel(private val repository: FacultyRepository) : ViewModel() {
    private val _uiState = MutableStateFlow(FacultyLegalEventsState())
    val uiState = _uiState.asStateFlow()

    init { loadEvents() }

    fun loadEvents() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null) }
            try {
                val data = repository.getLegalEvents()
                _uiState.update { it.copy(events = data, isLoading = false) }
            } catch (e: Exception) {
                _uiState.update { it.copy(isLoading = false, error = e.message) }
            }
        }
    }
}

class FacultyLegalEventsViewModelFactory(private val repository: FacultyRepository) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T = FacultyLegalEventsViewModel(repository) as T
}

data class FacultySalaryState(
    val slips: List<FacultySalarySlipDto> = emptyList(),
    val isLoading: Boolean = false,
    val error: String? = null
)

class FacultySalaryViewModel(private val repository: FacultyRepository) : ViewModel() {
    private val _uiState = MutableStateFlow(FacultySalaryState())
    val uiState = _uiState.asStateFlow()

    init { loadSlips() }

    fun loadSlips() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null) }
            try {
                val data = repository.getFacultySalarySlips()
                _uiState.update { it.copy(slips = data, isLoading = false) }
            } catch (e: Exception) {
                _uiState.update { it.copy(isLoading = false, error = e.message) }
            }
        }
    }
}

class FacultySalaryViewModelFactory(private val repository: FacultyRepository) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T = FacultySalaryViewModel(repository) as T
}


data class FacultyNotificationsState(
    val notifications: List<NotificationDto> = emptyList(),
    val isLoading: Boolean = false,
    val error: String? = null
)

class FacultyNotificationsViewModel(private val repository: FacultyRepository) : ViewModel() {
    private val _uiState = MutableStateFlow(FacultyNotificationsState())
    val uiState = _uiState.asStateFlow()

    init { loadNotifications() }

    fun loadNotifications() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null) }
            try {
                val data = repository.getFacultyNotifications()
                _uiState.update { it.copy(notifications = data, isLoading = false) }
            } catch (e: Exception) {
                _uiState.update { it.copy(isLoading = false, error = e.message) }
            }
        }
    }
}

class FacultyNotificationsViewModelFactory(private val repository: FacultyRepository) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T = FacultyNotificationsViewModel(repository) as T
}


data class FacultyOnlineMeetingsState(
    val meetings: List<OnlineMeetingDto> = emptyList(),
    val isLoading: Boolean = false,
    val error: String? = null
)

class FacultyOnlineMeetingsViewModel(private val repository: FacultyRepository) : ViewModel() {
    private val _uiState = MutableStateFlow(FacultyOnlineMeetingsState())
    val uiState = _uiState.asStateFlow()

    init { loadMeetings() }

    fun loadMeetings() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null) }
            try {
                val data = repository.getOnlineMeetings()
                _uiState.update { it.copy(meetings = data, isLoading = false) }
            } catch (e: Exception) {
                _uiState.update { it.copy(isLoading = false, error = e.message) }
            }
        }
    }
}

class FacultyOnlineMeetingsViewModelFactory(private val repository: FacultyRepository) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T = FacultyOnlineMeetingsViewModel(repository) as T
}

