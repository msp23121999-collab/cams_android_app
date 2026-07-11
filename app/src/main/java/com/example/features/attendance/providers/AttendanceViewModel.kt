package com.example.features.attendance.providers

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.features.attendance.models.AttendanceSummary
import com.example.core.repository.AttendanceRepository
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

data class AttendanceState(
    val summary: AttendanceSummary? = null,
    val isLoading: Boolean = false,
    val error: String? = null
)

class AttendanceViewModel(
    private val attendanceRepository: AttendanceRepository
) : ViewModel() {
    private val _uiState = MutableStateFlow(AttendanceState(isLoading = true))
    val uiState: StateFlow<AttendanceState> = _uiState.asStateFlow()

    init {
        observeAttendance()
        refresh()
    }

    private fun observeAttendance() {
        viewModelScope.launch {
            attendanceRepository.getAttendanceFlow().collectLatest { summary ->
                _uiState.update { it.copy(summary = summary, isLoading = summary == null) }
            }
        }
    }

    fun refresh() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            try {
                attendanceRepository.refreshAttendance()
            } catch (e: Exception) {
                _uiState.update { it.copy(error = e.message) }
            } finally {
                _uiState.update { it.copy(isLoading = false) }
            }
        }
    }
}

class AttendanceViewModelFactory(
    private val attendanceRepository: AttendanceRepository
) : androidx.lifecycle.ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(AttendanceViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return AttendanceViewModel(attendanceRepository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
