package com.example.features.admin.providers

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.core.network.GenerateHallTicketsRequest
import com.example.core.network.HallTicketDto
import com.example.core.repository.AdminRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class AdminExamViewModelState(
    val isLoading: Boolean = true,
    val error: String? = null,
    val hallTickets: List<HallTicketDto> = emptyList(),
    val students: List<com.example.features.admin.models.AdminFeeStudent> = emptyList(),
    val isSearching: Boolean = false,
    val isSaving: Boolean = false,
    val saveError: String? = null,
    val saveSuccess: Boolean = false
)

class AdminExamViewModel(private val repository: AdminRepository) : ViewModel() {
    private val _uiState = MutableStateFlow(AdminExamViewModelState())
    val uiState: StateFlow<AdminExamViewModelState> = _uiState.asStateFlow()

    init { loadTickets() }

    fun loadTickets() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null) }
            try {
                val tickets = repository.getHallTickets()
                _uiState.update { it.copy(hallTickets = tickets, isLoading = false) }
            } catch (e: Exception) {
                _uiState.update { it.copy(isLoading = false, error = e.message ?: "Failed to load hall tickets") }
            }
        }
    }

    fun searchStudents(query: String) {
        if (query.isBlank()) {
            _uiState.update { it.copy(students = emptyList()) }
            return
        }
        viewModelScope.launch {
            _uiState.update { it.copy(isSearching = true) }
            try {
                val results = repository.searchStudentsForFees(query) ?: emptyList()
                _uiState.update { it.copy(students = results, isSearching = false) }
            } catch (e: Exception) {
                _uiState.update { it.copy(isSearching = false, error = e.message) }
            }
        }
    }

    fun generate(studentIds: List<String>, examName: String, examCenter: String, examDate: String) {
        viewModelScope.launch {
            _uiState.update { it.copy(isSaving = true, saveError = null, saveSuccess = false) }
            try {
                repository.generateHallTickets(
                    GenerateHallTicketsRequest(
                        studentIds = studentIds,
                        examName = examName,
                        examCenter = examCenter.ifBlank { null },
                        examDate = examDate.ifBlank { null }
                    )
                )
                loadTickets()
                _uiState.update { it.copy(isSaving = false, saveSuccess = true, students = emptyList()) }
            } catch (e: Exception) {
                _uiState.update { it.copy(isSaving = false, saveError = e.message ?: "Failed to generate hall tickets") }
            }
        }
    }

    fun clearSaveStatus() {
        _uiState.update { it.copy(saveError = null, saveSuccess = false) }
    }
}

class AdminExamViewModelFactory(private val repository: AdminRepository) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(AdminExamViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return AdminExamViewModel(repository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
