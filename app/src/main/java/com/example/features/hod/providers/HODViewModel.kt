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
        @Suppress("UNCHECKED_CAST")
        return HODViewModel(repository) as T
    }
}

// --- HOD Faculty Management ViewModel ---
data class HODFacultyState(
    val faculty: List<com.example.core.network.HODFacultyResponseDto> = emptyList(),
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
                val data = repository.getActiveFaculty()
                _uiState.update { it.copy(faculty = data, isLoading = false) }
            } catch (e: Exception) {
                _uiState.update { it.copy(isLoading = false, error = e.message ?: "Failed to load faculty list") }
            }
        }
    }
}

class HODFacultyViewModelFactory(private val repository: HODRepository) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        @Suppress("UNCHECKED_CAST")
        return HODFacultyViewModel(repository) as T
    }
}

// --- HOD Student Management ViewModel ---
data class HODStudentState(
    val students: List<com.example.core.network.HODManagementStudentDto> = emptyList(),
    val metrics: com.example.core.network.HODManagementMetricsDto = com.example.core.network.HODManagementMetricsDto(),
    val isLoading: Boolean = false,
    val error: String? = null,
    val isVerifying: Boolean = false,
    val verifyError: String? = null
)

class HODStudentViewModel(private val repository: HODRepository) : ViewModel() {
    private val _uiState = MutableStateFlow(HODStudentState())
    val uiState: StateFlow<HODStudentState> = _uiState.asStateFlow()

    init { loadStudents() }

    fun loadStudents() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null) }
            try {
                val data = repository.getHODManagementStudents()
                _uiState.update { it.copy(students = data.students, metrics = data.metrics, isLoading = false) }
            } catch (e: Exception) {
                _uiState.update { it.copy(isLoading = false, error = e.message ?: "Failed to load student roster") }
            }
        }
    }

    fun verifyStudent(studentId: String, action: String, remarks: String?) {
        viewModelScope.launch {
            _uiState.update { it.copy(isVerifying = true, verifyError = null) }
            try {
                repository.verifyStudentProfile(studentId, action, remarks)
                loadStudents()
                _uiState.update { it.copy(isVerifying = false) }
            } catch (e: Exception) {
                _uiState.update { it.copy(isVerifying = false, verifyError = e.message ?: "Failed to update verification status") }
            }
        }
    }

    fun clearVerifyError() {
        _uiState.update { it.copy(verifyError = null) }
    }
}

class HODStudentViewModelFactory(private val repository: HODRepository) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        @Suppress("UNCHECKED_CAST")
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
        @Suppress("UNCHECKED_CAST")
        return HODApprovalsViewModel(repository) as T
    }
}
// --- HOD Circulars ViewModel ---
data class HODCircularsState(
    val circulars: List<com.example.core.network.NoticeDto> = emptyList(),
    val isLoading: Boolean = false,
    val error: String? = null,
    val isSaving: Boolean = false,
    val saveError: String? = null,
    val saveSuccess: Boolean = false
)

class HODCircularsViewModel(private val apiService: com.example.core.network.CamsApiService) : ViewModel() {
    private val _uiState = MutableStateFlow(HODCircularsState())
    val uiState: StateFlow<HODCircularsState> = _uiState.asStateFlow()

    init { loadCirculars() }

    fun loadCirculars() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null) }
            try {
                val response = apiService.getHodNotices()
                if (response.isSuccessful) {
                    _uiState.update { it.copy(circulars = response.body() ?: emptyList(), isLoading = false) }
                } else {
                    _uiState.update { it.copy(isLoading = false, error = "Failed to load circulars: ${response.code()}") }
                }
            } catch (e: Exception) {
                _uiState.update { it.copy(isLoading = false, error = e.message ?: "Failed to load circulars") }
            }
        }
    }

    fun createCircular(title: String, body: String, audienceType: String) {
        viewModelScope.launch {
            _uiState.update { it.copy(isSaving = true, saveError = null, saveSuccess = false) }
            try {
                val response = apiService.createHodNotice(com.example.core.network.NoticeCreateRequest(title, body, audienceType))
                if (response.isSuccessful) {
                    loadCirculars()
                    _uiState.update { it.copy(isSaving = false, saveSuccess = true) }
                } else {
                    _uiState.update { it.copy(isSaving = false, saveError = "Failed to create circular: ${response.code()}") }
                }
            } catch (e: Exception) {
                _uiState.update { it.copy(isSaving = false, saveError = e.message ?: "Failed to create circular") }
            }
        }
    }

    fun deleteCircular(noticeId: String) {
        viewModelScope.launch {
            try {
                val response = apiService.deleteHodNotice(noticeId)
                if (response.isSuccessful) {
                    loadCirculars()
                } else {
                    _uiState.update { it.copy(error = "Failed to delete circular: ${response.code()}") }
                }
            } catch (e: Exception) {
                _uiState.update { it.copy(error = e.message ?: "Failed to delete circular") }
            }
        }
    }

    fun clearSaveStatus() {
        _uiState.update { it.copy(saveError = null, saveSuccess = false) }
    }
}

class HODCircularsViewModelFactory(private val apiService: com.example.core.network.CamsApiService) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        @Suppress("UNCHECKED_CAST")
        return HODCircularsViewModel(apiService) as T
    }
}
// --- HOD Timetable ViewModel ---
data class HODTimetableState(
    val metadata: com.example.core.network.HODTimetableMetadataDto? = null,
    val selectedSectionId: String? = null,
    val timetableSlots: List<com.example.core.network.TimetableSlotDto> = emptyList(),
    val isLoading: Boolean = false,
    val error: String? = null,
    val isSaving: Boolean = false,
    val saveError: String? = null,
    val saveSuccess: Boolean = false
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

    fun submitTimetable(sectionId: String, slots: List<com.example.core.network.TimetableSlotInputDto>) {
        viewModelScope.launch {
            _uiState.update { it.copy(isSaving = true, saveError = null, saveSuccess = false) }
            try {
                repository.submitTimetable(sectionId, slots)
                loadSectionTimetable(sectionId)
                _uiState.update { it.copy(isSaving = false, saveSuccess = true) }
            } catch (e: Exception) {
                _uiState.update { it.copy(isSaving = false, saveError = e.message ?: "Failed to submit timetable") }
            }
        }
    }

    fun clearSaveStatus() {
        _uiState.update { it.copy(saveError = null, saveSuccess = false) }
    }
}

class HODTimetableViewModelFactory(private val repository: HODRepository) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        @Suppress("UNCHECKED_CAST")
        return HODTimetableViewModel(repository) as T
    }
}

