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
                
                // Also fetch subjects for the academics tab
                val mockSubjects = listOf(
                    AcademicSubject(code = "LAW301", name = "Constitutional Law I", credits = 4, semester = 5, type = "CORE", faculty = "Dr. John Doe"),
                    AcademicSubject(code = "LAW302", name = "Criminal Law", credits = 4, semester = 5, type = "CORE", faculty = "Prof. Jane Smith")
                )

                val summary = AcademicSummary(
                    totalSubjects = mockSubjects.size,
                    totalCredits = mockSubjects.sumOf { it.credits },
                    coreSubjects = mockSubjects.count { it.type == "CORE" },
                    currentSemester = 5
                )

                _uiState.update { 
                    it.copy(
                        timetable = periods, 
                        subjects = mockSubjects,
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
