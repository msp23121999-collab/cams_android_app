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
import kotlinx.coroutines.async
import kotlinx.coroutines.coroutineScope

data class HODAcademicMonitoringState(
    val dashboardData: HODTeachingLogsDashboardDto? = null,
    val attendanceData: List<HODAttendanceMonitoringDto> = emptyList(),
    val materials: List<HodPendingMaterialDto> = emptyList(),
    val pendingEntries: List<HODPendingEntryDto> = emptyList(),
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
                coroutineScope {
                    val dashboardDeferred = async { repository.getTeachingLogsDashboard() }
                    val attendanceDeferred = async {
                        try { repository.getAttendanceMonitoring() } catch (e: Exception) { emptyList() }
                    }
                    val materialsDeferred = async {
                        try { repository.getHodPendingMaterials() } catch (e: Exception) { emptyList() }
                    }
                    val pendingDeferred = async {
                        try { repository.getPendingEntries() } catch (e: Exception) { emptyList() }
                    }
                    val dashboard = dashboardDeferred.await()
                    val attendance = attendanceDeferred.await()
                    val materials = materialsDeferred.await()
                    val pending = pendingDeferred.await()
                    _uiState.update {
                        it.copy(
                            dashboardData = dashboard,
                            attendanceData = attendance,
                            materials = materials,
                            pendingEntries = pending,
                            isLoading = false
                        )
                    }
                }
            } catch (e: Exception) {
                _uiState.update { it.copy(isLoading = false, error = e.message) }
            }
        }
    }
}

class HODAcademicMonitoringViewModelFactory(private val repository: HODRepository) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        @Suppress("UNCHECKED_CAST")
        return HODAcademicMonitoringViewModel(repository) as T
    }
}

data class HODSyllabusManagementState(
    val metadata: HODSyllabusMetadataDto? = null,
    val courses: List<HODCourseDto> = emptyList(),
    val isLoading: Boolean = false,
    val error: String? = null,
    val selectedCourse: HODCourseDto? = null,
    val unitPlan: Map<String, List<String>> = emptyMap(),
    val isLoadingPlan: Boolean = false,
    val planError: String? = null,
    val isSavingPlan: Boolean = false,
    val savePlanSuccess: Boolean = false
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

    fun openCoursePlan(course: HODCourseDto) {
        _uiState.update { it.copy(selectedCourse = course, unitPlan = emptyMap(), planError = null) }
        viewModelScope.launch {
            _uiState.update { it.copy(isLoadingPlan = true) }
            try {
                val plan = repository.getCoursePlan(course.name)
                _uiState.update { it.copy(unitPlan = plan, isLoadingPlan = false) }
            } catch (e: Exception) {
                _uiState.update { it.copy(isLoadingPlan = false, planError = e.message ?: "Failed to load unit plan") }
            }
        }
    }

    fun saveCoursePlan(units: Map<String, List<String>>) {
        val course = _uiState.value.selectedCourse ?: return
        viewModelScope.launch {
            _uiState.update { it.copy(isSavingPlan = true, planError = null, savePlanSuccess = false) }
            try {
                repository.saveCoursePlan(course.name, units)
                _uiState.update { it.copy(isSavingPlan = false, unitPlan = units, savePlanSuccess = true) }
            } catch (e: Exception) {
                _uiState.update { it.copy(isSavingPlan = false, planError = e.message ?: "Failed to save unit plan") }
            }
        }
    }

    fun closeCoursePlan() {
        _uiState.update { it.copy(selectedCourse = null, unitPlan = emptyMap(), planError = null, savePlanSuccess = false) }
    }
}

class HODSyllabusManagementViewModelFactory(private val repository: HODRepository) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        @Suppress("UNCHECKED_CAST")
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
        @Suppress("UNCHECKED_CAST")
        return HODAttendanceMonitoringViewModel(repository) as T
    }
}

data class HODDepartmentAnalyticsState(
    val reportData: HODDepartmentReportDto? = null,
    val studentReportData: com.example.core.network.HODStudentReportDto? = null,
    val isLoading: Boolean = false,
    val error: String? = null,
    val isExporting: Boolean = false,
    val exportError: String? = null,
    val exportedCsv: Pair<String, String>? = null // fileName to csvContent
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
                coroutineScope {
                    val deptDeferred = async { repository.getDepartmentReports() }
                    val studentDeferred = async {
                        try { repository.getStudentReports() } catch (e: Exception) { null }
                    }
                    val dept = deptDeferred.await()
                    val students = studentDeferred.await()
                    _uiState.update { it.copy(reportData = dept, studentReportData = students, isLoading = false) }
                }
            } catch (e: Exception) {
                _uiState.update { it.copy(isLoading = false, error = e.message) }
            }
        }
    }

    fun exportDepartmentCsv() {
        viewModelScope.launch {
            _uiState.update { it.copy(isExporting = true, exportError = null) }
            try {
                val csv = repository.exportDepartmentReportCsv()
                _uiState.update { it.copy(isExporting = false, exportedCsv = "department_report.csv" to csv) }
            } catch (e: Exception) {
                _uiState.update { it.copy(isExporting = false, exportError = e.message ?: "Export failed") }
            }
        }
    }

    fun exportStudentCsv() {
        viewModelScope.launch {
            _uiState.update { it.copy(isExporting = true, exportError = null) }
            try {
                val csv = repository.exportStudentReportCsv()
                _uiState.update { it.copy(isExporting = false, exportedCsv = "student_report.csv" to csv) }
            } catch (e: Exception) {
                _uiState.update { it.copy(isExporting = false, exportError = e.message ?: "Export failed") }
            }
        }
    }

    fun clearExportState() {
        _uiState.update { it.copy(exportedCsv = null, exportError = null) }
    }
}

class HODDepartmentAnalyticsViewModelFactory(private val repository: HODRepository) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        @Suppress("UNCHECKED_CAST")
        return HODDepartmentAnalyticsViewModel(repository) as T
    }
}

