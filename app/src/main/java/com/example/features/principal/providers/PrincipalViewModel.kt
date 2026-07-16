package com.example.features.principal.providers

import androidx.lifecycle.ViewModel
import com.example.core.network.CalendarEventDto
import com.example.features.principal.models.*
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.core.repository.PrincipalRepository
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

data class PrincipalState(
    val metrics: PrincipalDashboardMetrics = PrincipalDashboardMetrics("0", "0", "0", "0"),
    val deptPerformance: List<DepartmentPerformance> = emptyList(),
    val pendingLeaves: List<LeaveApproval> = emptyList(),
    val pendingTimetables: List<TimetableApproval> = emptyList(),
    val calendarEvents: List<CalendarEventDto> = emptyList(),
    val isLoading: Boolean = false,
    val error: String? = null
)

class PrincipalViewModel(private val repository: PrincipalRepository) : ViewModel() {
    private val _uiState = MutableStateFlow(PrincipalState())
    val uiState: StateFlow<PrincipalState> = _uiState.asStateFlow()

    init {
        fetchDashboardData()
    }

    fun fetchDashboardData() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null) }
            try {
                val metrics = repository.getDashboardStats()
                val leaves = repository.getPendingLeaveApprovals()
                val timetables = repository.getPendingTimetableApprovals()
                val events = repository.getAcademicCalendar()
                
                _uiState.update { 
                    it.copy(
                        metrics = metrics,
                        pendingLeaves = leaves,
                        pendingTimetables = timetables,
                        calendarEvents = events,
                        isLoading = false
                    ) 
                }
            } catch (e: Exception) {
                _uiState.update { it.copy(isLoading = false, error = e.message) }
            }
        }
    }

    fun approveLeave(id: String, status: String, remarks: String) {
        viewModelScope.launch {
            try {
                repository.approveLeave(id, status, remarks)
                fetchDashboardData() // Refresh
            } catch (e: Exception) {
                _uiState.update { it.copy(error = "Failed to approve leave: ${e.message}") }
            }
        }
    }
}

class PrincipalViewModelFactory(private val repository: PrincipalRepository) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(PrincipalViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return PrincipalViewModel(repository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
// --- Principal Approvals ViewModel ---
data class PrincipalApprovalsState(
    val pendingLeaves: List<LeaveApproval> = emptyList(),
    val pendingFaculty: List<PrincipalPendingFaculty> = emptyList(),
    val isLoading: Boolean = false,
    val error: String? = null
)

class PrincipalApprovalsViewModel(private val repository: PrincipalRepository) : ViewModel() {
    private val _uiState = MutableStateFlow(PrincipalApprovalsState())
    val uiState: StateFlow<PrincipalApprovalsState> = _uiState.asStateFlow()

    init { loadApprovals() }

    fun loadApprovals() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null) }
            try {
                val leaves = repository.getPendingLeaveApprovals()
                val faculty = repository.getPendingFaculty()
                _uiState.update { it.copy(pendingLeaves = leaves, pendingFaculty = faculty, isLoading = false) }
            } catch (e: Exception) {
                _uiState.update { it.copy(isLoading = false, error = e.message) }
            }
        }
    }

    fun approveLeave(id: String) {
        viewModelScope.launch {
            try {
                repository.approveLeave(id, "FINAL_APPROVED", "")
                loadApprovals()
            } catch (e: Exception) {
                _uiState.update { it.copy(error = e.message) }
            }
        }
    }

    fun rejectLeave(id: String) {
        viewModelScope.launch {
            try {
                repository.approveLeave(id, "REJECTED_BY_PRINCIPAL", "")
                loadApprovals()
            } catch (e: Exception) {
                _uiState.update { it.copy(error = e.message) }
            }
        }
    }

    fun approveFaculty(id: String) {
        viewModelScope.launch {
            try {
                repository.approveFaculty(id)
                loadApprovals()
            } catch (e: Exception) {
                _uiState.update { it.copy(error = e.message) }
            }
        }
    }
}
// --- Principal Grievances ViewModel ---
data class PrincipalGrievancesState(
    val grievances: List<com.example.core.network.GrievanceDto> = emptyList(),
    val isLoading: Boolean = false,
    val error: String? = null
)

class PrincipalGrievancesViewModel(private val repository: PrincipalRepository) : ViewModel() {
    private val _uiState = MutableStateFlow(PrincipalGrievancesState())
    val uiState: StateFlow<PrincipalGrievancesState> = _uiState.asStateFlow()

    init { loadGrievances() }

    fun loadGrievances() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null) }
            try {
                val data = repository.getGrievances()
                _uiState.update { it.copy(grievances = data, isLoading = false) }
            } catch (e: Exception) {
                _uiState.update { it.copy(isLoading = false, error = e.message) }
            }
        }
    }
}
// --- Principal Circulars ViewModel ---
data class PrincipalCircularsState(
    val circulars: List<com.example.core.network.NoticeDto> = emptyList(),
    val isLoading: Boolean = false,
    val error: String? = null
)

class PrincipalCircularsViewModel(private val repository: PrincipalRepository) : ViewModel() {
    private val _uiState = MutableStateFlow(PrincipalCircularsState())
    val uiState: StateFlow<PrincipalCircularsState> = _uiState.asStateFlow()

    init { loadCirculars() }

    fun loadCirculars() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null) }
            try {
                val data = repository.getCirculars()
                _uiState.update { it.copy(circulars = data, isLoading = false) }
            } catch (e: Exception) {
                _uiState.update { it.copy(isLoading = false, error = e.message) }
            }
        }
    }

    fun publishCircular(title: String, body: String, targetAudience: String) {
        viewModelScope.launch {
            try {
                repository.publishCircular(title, body, targetAudience)
                loadCirculars()
            } catch (e: Exception) {
                _uiState.update { it.copy(error = e.message) }
            }
        }
    }
}

// --- Principal Research ViewModel ---
data class PrincipalResearchState(
    val compliance: com.example.core.network.PrincipalComplianceResponseDto? = null,
    val isLoading: Boolean = false,
    val error: String? = null
)

class PrincipalResearchViewModel(private val repository: PrincipalRepository) : ViewModel() {
    private val _uiState = MutableStateFlow(PrincipalResearchState())
    val uiState: StateFlow<PrincipalResearchState> = _uiState.asStateFlow()

    init { loadResearch() }

    fun loadResearch() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null) }
            try {
                val data = repository.getResearchCompliance()
                _uiState.update { it.copy(compliance = data, isLoading = false) }
            } catch (e: Exception) {
                _uiState.update { it.copy(isLoading = false, error = e.message) }
            }
        }
    }
}

// --- Principal Infrastructure ViewModel ---
data class PrincipalInfrastructureState(
    val data: com.example.core.network.InfrastructureResponseDto? = null,
    val isLoading: Boolean = false,
    val error: String? = null
)

class PrincipalInfrastructureViewModel(private val repository: PrincipalRepository) : ViewModel() {
    private val _uiState = MutableStateFlow(PrincipalInfrastructureState())
    val uiState: StateFlow<PrincipalInfrastructureState> = _uiState.asStateFlow()

    init { loadInfrastructure() }

    fun loadInfrastructure() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null) }
            try {
                val data = repository.getInfrastructureDetails()
                _uiState.update { it.copy(data = data, isLoading = false) }
            } catch (e: Exception) {
                _uiState.update { it.copy(isLoading = false, error = e.message) }
            }
        }
    }
}
