package com.example.features.leave.providers

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.core.repository.StudentRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class LeaveHistoryRecord(
    val id: String,
    val type: String,
    val fromDate: String,
    val toDate: String,
    val status: String,
    val remarks: String? = null
)

data class LeaveUiState(
    val currentAttendance: Double = 82.5,
    val projectedAttendance: Double = 80.0,
    val history: List<LeaveHistoryRecord> = emptyList(),
    val appType: String = "Leave",
    val leaveType: String = "Sick Leave",
    val odType: String = "Court Visit",
    val fromDate: String = "",
    val toDate: String = "",
    val reason: String = "",
    val courtName: String = "",
    val advocateName: String = "",
    val isLoading: Boolean = false,
    val error: String? = null
)

class LeaveViewModel(
    private val studentRepository: StudentRepository
) : ViewModel() {
    private val _uiState = MutableStateFlow(LeaveUiState(isLoading = true))
    val uiState: StateFlow<LeaveUiState> = _uiState.asStateFlow()

    init {
        refresh()
    }

    fun refresh() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null) }
            try {
                val dtos = studentRepository.getLeaves()
                val history = dtos.map { dto ->
                    LeaveHistoryRecord(
                        id = dto.id,
                        type = if (dto.reason.contains("Court", ignoreCase = true)) "OD" else "LEAVE",
                        fromDate = dto.startDate,
                        toDate = dto.endDate,
                        status = dto.status.uppercase(),
                        remarks = if (dto.status == "Rejected") "Insufficient documentation" else null
                    )
                }
                _uiState.update { it.copy(history = history, isLoading = false) }
            } catch (e: Exception) {
                _uiState.update { it.copy(isLoading = false, error = e.message) }
            }
        }
    }

    fun updateForm(
        appType: String? = null,
        leaveType: String? = null,
        odType: String? = null,
        fromDate: String? = null,
        toDate: String? = null,
        reason: String? = null,
        courtName: String? = null,
        advocateName: String? = null
    ) {
        _uiState.update { 
            it.copy(
                appType = appType ?: it.appType,
                leaveType = leaveType ?: it.leaveType,
                odType = odType ?: it.odType,
                fromDate = fromDate ?: it.fromDate,
                toDate = toDate ?: it.toDate,
                reason = reason ?: it.reason,
                courtName = courtName ?: it.courtName,
                advocateName = advocateName ?: it.advocateName
            )
        }
        // Recalculate projected attendance (mock logic)
        if (fromDate != null || toDate != null) {
            _uiState.update { it.copy(projectedAttendance = it.currentAttendance - 2.5) }
        }
    }

    fun submitApplication() {
        // Implement submission
    }
}

class LeaveViewModelFactory(
    private val studentRepository: StudentRepository
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(LeaveViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return LeaveViewModel(studentRepository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
