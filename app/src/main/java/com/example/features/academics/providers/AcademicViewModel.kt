package com.example.features.academics.providers

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.core.repository.StudentRepository
import com.example.features.academics.models.*
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class AcademicViewModelFactory(private val repository: StudentRepository) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(AcademicViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return AcademicViewModel(repository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}

data class AcademicState(
    val timetable: List<TimetablePeriod> = emptyList(),
    val subjects: List<AcademicSubject> = emptyList(),
    val summary: AcademicSummary? = null,
    val activeTab: String = "timetable", // "timetable" or "academics"
    val isLoading: Boolean = true,
    val error: String? = null
)

class AcademicViewModel(private val repository: StudentRepository) : ViewModel() {
    private val _uiState = MutableStateFlow(AcademicState())
    val uiState: StateFlow<AcademicState> = _uiState.asStateFlow()

    init {
        fetchAcademicData()
    }

    fun setActiveTab(tab: String) {
        _uiState.update { it.copy(activeTab = tab) }
    }

    fun fetchAcademicData() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null) }
            try {
                val timetableDtos = repository.getTimetable()
                val syllabusDtos = repository.getSyllabus()
                
                val timetable = timetableDtos.map { dto ->
                    TimetablePeriod(
                        subjectCode = dto.subjectCode,
                        subjectName = dto.subjectName,
                        weekday = dto.dayOfWeek,
                        startTime = dto.startTime,
                        endTime = dto.endTime,
                        facultyName = dto.facultyName,
                        room = dto.roomNo
                    )
                }
                
                val subjects = syllabusDtos.map { dto ->
                    AcademicSubject(
                        code = dto.subjectCode,
                        name = dto.subjectName,
                        credits = 4, // Default fallback
                        semester = dto.semester,
                        type = "Core", // Default fallback
                        hours = 0,
                        faculty = "Assigned Faculty",
                        degree = "B.A. LL.B.",
                        batch = "2023-28"
                    )
                }

                val totalCredits = subjects.sumOf { it.credits }
                val coreCount = subjects.count { it.type == "Core" }

                _uiState.update { it.copy(
                    timetable = timetable,
                    subjects = subjects,
                    summary = AcademicSummary(
                        totalSubjects = subjects.size,
                        totalCredits = totalCredits,
                        coreSubjects = coreCount,
                        currentSemester = subjects.firstOrNull()?.semester ?: 1
                    ),
                    isLoading = false
                ) }
            } catch (e: Exception) {
                _uiState.update { it.copy(isLoading = false, error = e.message ?: "Failed to load") }
            }
        }
    }
}
