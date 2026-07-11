package com.example.features.faculty.providers

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.core.repository.FacultyRepository
import com.example.features.parent.models.TimetableDay
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

data class FacultyTimetableState(
    val timetable: List<TimetableDay> = emptyList(),
    val isLoading: Boolean = true,
    val error: String? = null
)

class FacultyTimetableViewModel(private val repository: FacultyRepository) : ViewModel() {
    private val _uiState = MutableStateFlow(FacultyTimetableState())
    val uiState: StateFlow<FacultyTimetableState> = _uiState.asStateFlow()

    init {
        loadTimetable()
    }

    fun loadTimetable() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null) }
            try {
                val timetable = repository.getTimetable()
                _uiState.update { it.copy(timetable = timetable, isLoading = false) }
            } catch (e: Exception) {
                _uiState.update { it.copy(isLoading = false, error = e.message) }
            }
        }
    }
}

class FacultyTimetableViewModelFactory(private val repository: FacultyRepository) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(FacultyTimetableViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return FacultyTimetableViewModel(repository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
