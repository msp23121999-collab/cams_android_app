package com.example.features.admin.providers

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.core.network.HODCalendarEventCreateRequest
import com.example.core.network.HODCalendarEventDto
import com.example.core.repository.AdminRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class AdminAcademicCalendarViewModelState(
    val isLoading: Boolean = true,
    val error: String? = null,
    val events: List<HODCalendarEventDto> = emptyList(),
    val isSaving: Boolean = false,
    val saveError: String? = null,
    val saveSuccess: Boolean = false
)

class AdminAcademicCalendarViewModel(private val repository: AdminRepository) : ViewModel() {
    private val _uiState = MutableStateFlow(AdminAcademicCalendarViewModelState())
    val uiState: StateFlow<AdminAcademicCalendarViewModelState> = _uiState.asStateFlow()

    init { fetchEvents() }

    fun fetchEvents() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null) }
            try {
                val events = repository.getCalendarEvents()
                _uiState.update { it.copy(events = events.sortedBy { e -> e.startDate }, isLoading = false) }
            } catch (e: Exception) {
                _uiState.update { it.copy(isLoading = false, error = e.message ?: "Failed to load calendar events") }
            }
        }
    }

    fun createEvent(request: HODCalendarEventCreateRequest) {
        viewModelScope.launch {
            _uiState.update { it.copy(isSaving = true, saveError = null, saveSuccess = false) }
            try {
                repository.createCalendarEvent(request)
                fetchEvents()
                _uiState.update { it.copy(isSaving = false, saveSuccess = true) }
            } catch (e: Exception) {
                _uiState.update { it.copy(isSaving = false, saveError = e.message ?: "Failed to create event") }
            }
        }
    }

    fun deleteEvent(eventId: String) {
        viewModelScope.launch {
            try {
                repository.deleteCalendarEvent(eventId)
                fetchEvents()
            } catch (e: Exception) {
                _uiState.update { it.copy(error = e.message ?: "Failed to delete event") }
            }
        }
    }

    fun clearSaveStatus() {
        _uiState.update { it.copy(saveError = null, saveSuccess = false) }
    }
}

class AdminAcademicCalendarViewModelFactory(private val repository: AdminRepository) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(AdminAcademicCalendarViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return AdminAcademicCalendarViewModel(repository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
