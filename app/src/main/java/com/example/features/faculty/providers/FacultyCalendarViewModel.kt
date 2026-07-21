package com.example.features.faculty.providers

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.core.network.CalendarEventDto
import com.example.core.repository.FacultyRepository
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

data class FacultyCalendarState(
    val events: List<CalendarEventDto> = emptyList(),
    val isLoading: Boolean = true,
    val error: String? = null
)

class FacultyCalendarViewModel(private val repository: FacultyRepository) : ViewModel() {
    private val _uiState = MutableStateFlow(FacultyCalendarState())
    val uiState: StateFlow<FacultyCalendarState> = _uiState.asStateFlow()

    init {
        loadCalendar()
    }

    fun loadCalendar() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null) }
            try {
                val events = repository.getAcademicCalendar().sortedBy { it.date }
                _uiState.update { it.copy(events = events, isLoading = false) }
            } catch (e: Exception) {
                _uiState.update { it.copy(isLoading = false, error = e.message ?: "Failed to load academic calendar") }
            }
        }
    }
}

class FacultyCalendarViewModelFactory(private val repository: FacultyRepository) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(FacultyCalendarViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return FacultyCalendarViewModel(repository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
