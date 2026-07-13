package com.example.features.academics.providers

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.core.repository.StudentRepository
import com.example.features.academics.models.AcademicSubject
import com.example.features.academics.models.TimetablePeriod
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class TimetableState(
    val timetable: List<TimetablePeriod> = emptyList(),
    val subjects: List<AcademicSubject> = emptyList(),
    val activeTab: String = "timetable",
    val isLoading: Boolean = false,
    val error: String? = null,
    val summary: AcademicSummary? = null
)

data class AcademicSummary(
    val totalSubjects: Int,
    val totalCredits: Int,
    val coreSubjects: Int,
    val currentSemester: Int
)

class TimetableViewModel(
    private val studentRepository: StudentRepository
) : ViewModel() {
    private val _uiState = MutableStateFlow(TimetableState(isLoading = true))
    val uiState: StateFlow<TimetableState> = _uiState.asStateFlow()

    init {
        refresh()
    }

    fun setActiveTab(tab: String) {
        _uiState.update { it.copy(activeTab = tab) }
    }

    fun refresh() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null) }
            try {
                val timetableDtos = studentRepository.getTimetable()
                val periods = timetableDtos.map { dto ->
                    TimetablePeriod(
                        weekday = dto.dayOfWeek,
                        startTime = dto.startTime,
                        endTime = dto.endTime,
                        subjectName = dto.subjectName,
                        subjectCode = dto.subjectCode,
                        facultyName = dto.facultyName,
                        room = dto.roomNo
                    )
                }
                
                val syllabusDtos = studentRepository.getSyllabus()
                val subjects = syllabusDtos.map { dto ->
                    AcademicSubject(
                        code = dto.subjectCode,
                        name = dto.subjectName,
                        credits = 4, // Default fallback
                        semester = dto.semester,
                        type = "CORE", // Default fallback
                        faculty = "Assigned Faculty" // Fallback since faculty isn't in SyllabusDto
                    )
                }

                val summary = AcademicSummary(
                    totalSubjects = subjects.size,
                    totalCredits = subjects.sumOf { it.credits },
                    coreSubjects = subjects.count { it.type == "CORE" },
                    currentSemester = subjects.firstOrNull()?.semester ?: 1
                )

                _uiState.update { 
                    it.copy(
                        timetable = periods, 
                        subjects = subjects,
                        summary = summary,
                        isLoading = false
                    ) 
                }
            } catch (e: Exception) {
                _uiState.update { it.copy(isLoading = false, error = e.message) }
            }
        }
    }
}

class TimetableViewModelFactory(
    private val studentRepository: StudentRepository
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(TimetableViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return TimetableViewModel(studentRepository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
