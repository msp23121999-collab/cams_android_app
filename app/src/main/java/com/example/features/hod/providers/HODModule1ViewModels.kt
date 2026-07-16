package com.example.features.hod.providers

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.core.network.*
import com.example.core.repository.HODRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class HODAcademicMonitoringState(
    val dashboardData: HODTeachingLogsDashboardDto? = null,
    val isLoading: Boolean = false,
    val error: String? = null
)

class HODAcademicMonitoringViewModel(private val repository: HODRepository) : ViewModel() {
    private val _uiState = MutableStateFlow(HODAcademicMonitoringState())
    val uiState: StateFlow<HODAcademicMonitoringState> = _uiState.asStateFlow()

    init {
        fetchDashboard()
    }

    fun fetchDashboard() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null) }
            try {
                val data = repository.getTeachingLogsDashboard()
                _uiState.update { it.copy(dashboardData = data, isLoading = false) }
            } catch (e: Exception) {
                _uiState.update { it.copy(isLoading = false, error = e.message) }
            }
        }
    }
}

class HODAcademicMonitoringViewModelFactory(private val repository: HODRepository) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        return HODAcademicMonitoringViewModel(repository) as T
    }
}

data class HODSyllabusManagementState(
    val metadata: HODSyllabusMetadataDto? = null,
    val courses: List<HODCourseDto> = emptyList(),
    val isLoading: Boolean = false,
    val error: String? = null
)

class HODSyllabusManagementViewModel(private val repository: HODRepository) : ViewModel() {
    private val _uiState = MutableStateFlow(HODSyllabusManagementState())
    val uiState: StateFlow<HODSyllabusManagementState> = _uiState.asStateFlow()

    init {
        fetchSyllabusData()
    }

    fun fetchSyllabusData() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null) }
            try {
                val metadata = repository.getSyllabusMetadata()
                val courses = repository.getSyllabusCourses()
                _uiState.update { it.copy(metadata = metadata, courses = courses, isLoading = false) }
            } catch (e: Exception) {
                _uiState.update { it.copy(isLoading = false, error = e.message) }
            }
        }
    }
}

class HODSyllabusManagementViewModelFactory(private val repository: HODRepository) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        return HODSyllabusManagementViewModel(repository) as T
    }
}

data class HODAttendanceMonitoringState(
    val attendanceData: List<HODAttendanceMonitoringDto> = emptyList(),
    val isLoading: Boolean = false,
    val error: String? = null
)

class HODAttendanceMonitoringViewModel(private val repository: HODRepository) : ViewModel() {
    private val _uiState = MutableStateFlow(HODAttendanceMonitoringState())
    val uiState: StateFlow<HODAttendanceMonitoringState> = _uiState.asStateFlow()

    init {
        fetchAttendanceData()
    }

    fun fetchAttendanceData() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null) }
            try {
                val data = repository.getAttendanceMonitoring()
                _uiState.update { it.copy(attendanceData = data, isLoading = false) }
            } catch (e: Exception) {
                _uiState.update { it.copy(isLoading = false, error = e.message) }
            }
        }
    }
}

class HODAttendanceMonitoringViewModelFactory(private val repository: HODRepository) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        return HODAttendanceMonitoringViewModel(repository) as T
    }
}

data class HODDepartmentAnalyticsState(
    val reportData: HODDepartmentReportDto? = null,
    val isLoading: Boolean = false,
    val error: String? = null
)

class HODDepartmentAnalyticsViewModel(private val repository: HODRepository) : ViewModel() {
    private val _uiState = MutableStateFlow(HODDepartmentAnalyticsState())
    val uiState: StateFlow<HODDepartmentAnalyticsState> = _uiState.asStateFlow()

    init {
        fetchReportData()
    }

    fun fetchReportData() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null) }
            try {
                val data = repository.getDepartmentReports()
                _uiState.update { it.copy(reportData = data, isLoading = false) }
            } catch (e: Exception) {
                _uiState.update { it.copy(isLoading = false, error = e.message) }
            }
        }
    }
}

class HODDepartmentAnalyticsViewModelFactory(private val repository: HODRepository) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        return HODDepartmentAnalyticsViewModel(repository) as T
    }
}
