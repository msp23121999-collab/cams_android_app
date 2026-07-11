package com.example.features.academics.providers

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.core.repository.StudentRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import androidx.compose.ui.graphics.Color

data class CalendarEventModel(
    val id: String,
    val title: String,
    val date: String,
    val category: String,
    val color: Color
)

data class AcademicCalendarState(
    val events: List<CalendarEventModel> = emptyList(),
    val isLoading: Boolean = false,
    val error: String? = null
)

class AcademicCalendarViewModel(
    private val studentRepository: StudentRepository
) : ViewModel() {
    private val _uiState = MutableStateFlow(AcademicCalendarState(isLoading = true))
    val uiState: StateFlow<AcademicCalendarState> = _uiState.asStateFlow()

    init {
        refresh()
    }

    fun refresh() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null) }
            try {
                val dtos = studentRepository.getAcademicCalendar()
                val events = dtos.map { dto ->
                    CalendarEventModel(
                        id = dto.id,
                        title = dto.eventName,
                        date = dto.date,
                        category = if (dto.isHoliday) "Holiday" else "Academic",
                        color = if (dto.isHoliday) Color(0xFFEF4444) else Color(0xFF3B82F6)
                    )
                }
                _uiState.update { it.copy(events = events, isLoading = false) }
            } catch (e: Exception) {
                _uiState.update { it.copy(isLoading = false, error = e.message) }
            }
        }
    }
}

class AcademicCalendarViewModelFactory(
    private val studentRepository: StudentRepository
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(AcademicCalendarViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return AcademicCalendarViewModel(studentRepository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
