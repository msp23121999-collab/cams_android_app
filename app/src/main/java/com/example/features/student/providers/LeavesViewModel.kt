package com.example.features.student.providers

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.core.network.LeaveRequestDto
import com.example.core.repository.StudentRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class LeavesState(
    val leaves: List<LeaveRequestDto> = emptyList(),
    val isLoading: Boolean = false,
    val isSubmitting: Boolean = false,
    val error: String? = null,
    val submitError: String? = null,
    val applySuccess: Boolean = false
)

class LeavesViewModel(
    private val studentRepository: StudentRepository
) : ViewModel() {
    private val _uiState = MutableStateFlow(LeavesState())
    val uiState: StateFlow<LeavesState> = _uiState.asStateFlow()

    init {
        fetchLeaves()
    }

    fun fetchLeaves() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null) }
            try {
                val leaves = studentRepository.getLeaves()
                _uiState.update { it.copy(leaves = leaves, isLoading = false) }
            } catch (e: Exception) {
                _uiState.update { it.copy(isLoading = false, error = e.message ?: "Failed to load leaves") }
            }
        }
    }

    fun applyLeave(type: String, fromDate: String, toDate: String, reason: String, appCategory: String = "Leave") {
        viewModelScope.launch {
            _uiState.update { it.copy(isSubmitting = true, submitError = null, applySuccess = false) }
            try {
                val success = studentRepository.applyLeave(type, fromDate, toDate, reason, appCategory)
                if (success) {
                    _uiState.update { it.copy(isSubmitting = false, applySuccess = true) }
                    fetchLeaves()
                } else {
                    _uiState.update { it.copy(isSubmitting = false, submitError = "Application failed. Please try again.") }
                }
            } catch (e: Exception) {
                _uiState.update { it.copy(isSubmitting = false, submitError = e.message ?: "Application failed. Please try again.") }
            }
        }
    }

    fun clearApplyStatus() {
        _uiState.update { it.copy(applySuccess = false, submitError = null) }
    }
}

class LeavesViewModelFactory(
    private val studentRepository: StudentRepository
) : androidx.lifecycle.ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(LeavesViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return LeavesViewModel(studentRepository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
