package com.example.features.faculty.providers

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.core.repository.FacultyRepository
import com.example.features.faculty.models.FacultySubject
import com.example.core.network.FacultyStudentDto
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

data class FacultyAttendanceState(
    val subjects: List<FacultySubject> = emptyList(),
    val students: List<FacultyStudentDto> = emptyList(),
    val isLoading: Boolean = true,
    val error: String? = null
)

class FacultyAttendanceViewModel(private val repository: FacultyRepository) : ViewModel() {
    private val _uiState = MutableStateFlow(FacultyAttendanceState())
    val uiState: StateFlow<FacultyAttendanceState> = _uiState.asStateFlow()

    init {
        loadData()
    }

    fun loadData() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null) }
            try {
                val subjects = repository.getAssignedSubjects()
                val students = repository.getStudents()
                _uiState.update { it.copy(subjects = subjects, students = students, isLoading = false) }
            } catch (e: Exception) {
                _uiState.update { it.copy(isLoading = false, error = e.message) }
            }
        }
    }
}

class FacultyAttendanceViewModelFactory(private val repository: FacultyRepository) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(FacultyAttendanceViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return FacultyAttendanceViewModel(repository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
