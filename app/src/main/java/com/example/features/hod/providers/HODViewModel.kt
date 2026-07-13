package com.example.features.hod.providers

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.core.repository.HODRepository
import com.example.features.hod.models.HODDashboardMetrics
import com.example.features.hod.models.HODActivity
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

data class HODState(
    val metrics: HODDashboardMetrics = HODDashboardMetrics("0", "0", "0", "0"),
    val activities: List<HODActivity> = emptyList(),
    val isLoading: Boolean = false,
    val error: String? = null
)

class HODViewModel(private val repository: HODRepository) : ViewModel() {
    private val _uiState = MutableStateFlow(HODState())
    val uiState: StateFlow<HODState> = _uiState.asStateFlow()

    init {
        loadDashboardData()
    }

    fun loadDashboardData() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null) }
            try {
                val metrics = repository.getDashboardMetrics()
                val activities = repository.getRecentActivities()
                _uiState.update { it.copy(
                    metrics = metrics,
                    activities = activities,
                    isLoading = false
                ) }
            } catch (e: Exception) {
                _uiState.update { it.copy(isLoading = false, error = e.message) }
            }
        }
    }
}

class HODViewModelFactory(private val repository: HODRepository) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        return HODViewModel(repository) as T
    }
}

// --- HOD Faculty Management ViewModel ---
data class HODFacultyState(
    val faculty: List<com.example.core.network.FacultyStudentDto> = emptyList(),
    val isLoading: Boolean = false,
    val error: String? = null
)

class HODFacultyViewModel(private val repository: HODRepository) : ViewModel() {
    private val _uiState = MutableStateFlow(HODFacultyState())
    val uiState: StateFlow<HODFacultyState> = _uiState.asStateFlow()

    init { loadFaculty() }

    fun loadFaculty() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null) }
            try {
                val data = repository.getFacultyManagementData()
                _uiState.update { it.copy(faculty = data, isLoading = false) }
            } catch (e: Exception) {
                _uiState.update { it.copy(isLoading = false, error = e.message) }
            }
        }
    }
}

class HODFacultyViewModelFactory(private val repository: HODRepository) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        return HODFacultyViewModel(repository) as T
    }
}

// --- HOD Student Management ViewModel ---
data class HODStudentState(
    val students: List<com.example.core.network.FacultyStudentDto> = emptyList(),
    val isLoading: Boolean = false,
    val error: String? = null
)

class HODStudentViewModel(private val repository: HODRepository) : ViewModel() {
    private val _uiState = MutableStateFlow(HODStudentState())
    val uiState: StateFlow<HODStudentState> = _uiState.asStateFlow()

    init { loadStudents() }

    fun loadStudents() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null) }
            try {
                val data = repository.getStudentManagementData()
                _uiState.update { it.copy(students = data, isLoading = false) }
            } catch (e: Exception) {
                _uiState.update { it.copy(isLoading = false, error = e.message) }
            }
        }
    }
}

class HODStudentViewModelFactory(private val repository: HODRepository) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        return HODStudentViewModel(repository) as T
    }
}
// --- HOD Approvals ViewModel ---
data class HODApprovalsState(
    val pendingLeaves: List<com.example.core.network.LeaveRequestDto> = emptyList(),
    val isLoading: Boolean = false,
    val error: String? = null
)

class HODApprovalsViewModel(private val repository: HODRepository) : ViewModel() {
    private val _uiState = MutableStateFlow(HODApprovalsState())
    val uiState: StateFlow<HODApprovalsState> = _uiState.asStateFlow()

    init { loadPendingLeaves() }

    fun loadPendingLeaves() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null) }
            try {
                val data = repository.getPendingLeaveApprovals()
                _uiState.update { it.copy(pendingLeaves = data, isLoading = false) }
            } catch (e: Exception) {
                _uiState.update { it.copy(isLoading = false, error = e.message) }
            }
        }
    }

    fun approveLeave(id: String, status: String, remarks: String?) {
        viewModelScope.launch {
            try {
                repository.approveLeave(id, status, remarks)
                loadPendingLeaves()
            } catch (e: Exception) {
                _uiState.update { it.copy(error = e.message) }
            }
        }
    }
}

class HODApprovalsViewModelFactory(private val repository: HODRepository) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        return HODApprovalsViewModel(repository) as T
    }
}
// --- HOD Circulars ViewModel ---
data class HODCircularsState(
    val circulars: List<com.example.core.network.NoticeDto> = emptyList(),
    val isLoading: Boolean = false,
    val error: String? = null
)

class HODCircularsViewModel(private val apiService: com.example.core.network.CamsApiService) : ViewModel() {
    private val _uiState = MutableStateFlow(HODCircularsState())
    val uiState: StateFlow<HODCircularsState> = _uiState.asStateFlow()

    init { loadCirculars() }

    fun loadCirculars() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null) }
            try {
                val response = apiService.getNotices()
                if (response.isSuccessful) {
                    _uiState.update { it.copy(circulars = response.body() ?: emptyList(), isLoading = false) }
                } else {
                    _uiState.update { it.copy(isLoading = false, error = response.message()) }
                }
            } catch (e: Exception) {
                _uiState.update { it.copy(isLoading = false, error = e.message) }
            }
        }
    }
}

class HODCircularsViewModelFactory(private val apiService: com.example.core.network.CamsApiService) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        return HODCircularsViewModel(apiService) as T
    }
}
// --- HOD Timetable ViewModel ---
data class HODTimetableState(
    val metadata: com.example.core.network.HODTimetableMetadataDto? = null,
    val selectedSectionId: String? = null,
    val timetableSlots: List<com.example.core.network.TimetableSlotDto> = emptyList(),
    val isLoading: Boolean = false,
    val error: String? = null
)

class HODTimetableViewModel(private val repository: HODRepository) : ViewModel() {
    private val _uiState = MutableStateFlow(HODTimetableState())
    val uiState: StateFlow<HODTimetableState> = _uiState.asStateFlow()

    init { loadMetadata() }

    fun loadMetadata() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null) }
            try {
                val metadata = repository.getTimetableMetadata()
                val selectedSectionId = metadata.sections.firstOrNull()?.id
                _uiState.update { it.copy(metadata = metadata, selectedSectionId = selectedSectionId, isLoading = selectedSectionId != null) }
                
                if (selectedSectionId != null) {
                    loadSectionTimetable(selectedSectionId)
                } else {
                    _uiState.update { it.copy(isLoading = false) }
                }
            } catch (e: Exception) {
                _uiState.update { it.copy(isLoading = false, error = e.message) }
            }
        }
    }

    fun selectSection(sectionId: String) {
        _uiState.update { it.copy(selectedSectionId = sectionId) }
        loadSectionTimetable(sectionId)
    }

    private fun loadSectionTimetable(sectionId: String) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null) }
            try {
                val slots = repository.getTimetableSection(sectionId)
                _uiState.update { it.copy(timetableSlots = slots, isLoading = false) }
            } catch (e: Exception) {
                _uiState.update { it.copy(isLoading = false, error = e.message) }
            }
        }
    }
}

class HODTimetableViewModelFactory(private val repository: HODRepository) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        return HODTimetableViewModel(repository) as T
    }
}
