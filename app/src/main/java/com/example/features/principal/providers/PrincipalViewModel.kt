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
