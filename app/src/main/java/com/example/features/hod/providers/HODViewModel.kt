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
