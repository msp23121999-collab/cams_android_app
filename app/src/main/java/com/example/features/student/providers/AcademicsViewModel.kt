package com.example.features.student.providers

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.core.network.TimetableSlotDto
import com.example.core.repository.StudentRepository
import com.example.features.student.models.Course
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class AcademicsState(
    val timetable: List<TimetableSlotDto> = emptyList(),
    val courses: List<Course> = emptyList(),
    val isLoading: Boolean = false,
    val error: String? = null
)

class AcademicsViewModel(
    private val studentRepository: StudentRepository
) : ViewModel() {
    private val _uiState = MutableStateFlow(AcademicsState())
    val uiState: StateFlow<AcademicsState> = _uiState.asStateFlow()

    init {
        fetchAcademicsData()
    }

    fun fetchAcademicsData() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null) }
            try {
                val timetable = studentRepository.getTimetable()
                val courseDtos = studentRepository.getCourses()
                val courses = courseDtos.map { dto ->
                    Course(
                        id = dto.id,
                        name = dto.subjectName ?: dto.name ?: "Subject",
                        code = dto.subjectCode ?: dto.code ?: "",
                        credits = dto.credits ?: 0
                    )
                }
                _uiState.update { it.copy(timetable = timetable, courses = courses, isLoading = false) }
            } catch (e: Exception) {
                _uiState.update { it.copy(isLoading = false, error = e.message ?: "Failed to load academics data") }
            }
        }
    }
}

class AcademicsViewModelFactory(
    private val studentRepository: StudentRepository
) : androidx.lifecycle.ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(AcademicsViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return AcademicsViewModel(studentRepository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
