package com.example.features.academics.providers

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.example.core.repository.StudentRepository

import com.example.features.academics.models.*
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import androidx.lifecycle.viewModelScope

class SyllabusViewModelFactory(private val studentRepository: StudentRepository) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(SyllabusViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return SyllabusViewModel() as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}

data class SyllabusUiState(
    val syllabus: Map<String, SyllabusProgress> = emptyMap(),
    val lessonPlanTracking: List<LessonPlanItem> = emptyList(),
    val isLoading: Boolean = false,
    val error: String? = null,
    val expandedSubject: String? = null
)

class SyllabusViewModel : ViewModel() {
    private val _uiState = MutableStateFlow(SyllabusUiState())
    val uiState: StateFlow<SyllabusUiState> = _uiState.asStateFlow()

    init {
        loadData()
    }

    fun toggleSubject(subject: String) {
        _uiState.update { 
            it.copy(expandedSubject = if (it.expandedSubject == subject) null else subject)
        }
    }

    fun loadData() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null) }
            try {
                // Simulate network latency
                delay(1200)

                val mockSyllabus = mapOf(
                    "Constitutional Law II" to SyllabusProgress(
                        overallCompletion = 75,
                        daysRemaining = 45,
                        unitsProgress = listOf(
                            UnitProgress(
                                unit = "Unit I",
                                completedTopics = listOf("Fundamental Rights", "Directive Principles"),
                                remainingTopics = listOf("Fundamental Duties")
                            ),
                            UnitProgress(
                                unit = "Unit II",
                                completedTopics = listOf("The Executive", "The Parliament"),
                                remainingTopics = emptyList()
                            )
                        )
                    ),
                    "Criminal Procedure Code" to SyllabusProgress(
                        overallCompletion = 40,
                        daysRemaining = 45,
                        unitsProgress = listOf(
                            UnitProgress(
                                unit = "Unit I",
                                completedTopics = listOf("Definitions", "Constitution of Criminal Courts"),
                                remainingTopics = listOf("Power of Courts")
                            )
                        )
                    ),
                    "Family Law I" to SyllabusProgress(
                        overallCompletion = 90,
                        daysRemaining = 45,
                        unitsProgress = listOf(
                            UnitProgress(
                                unit = "Unit I",
                                completedTopics = listOf("Sources of Hindu Law", "Schools of Hindu Law"),
                                remainingTopics = emptyList()
                            )
                        )
                    )
                )

                val mockTracking = listOf(
                    LessonPlanItem(
                        subject = "Constitutional Law II",
                        unit = "Unit I",
                        plannedTopic = "Fundamental Rights",
                        actualTopic = "Fundamental Rights & Case Laws",
                        dateTaught = "2026-05-10",
                        status = "Covered"
                    ),
                    LessonPlanItem(
                        subject = "Constitutional Law II",
                        unit = "Unit I",
                        plannedTopic = "Directive Principles",
                        actualTopic = "DPSP Implementation",
                        dateTaught = "2026-05-15",
                        status = "Covered"
                    ),
                    LessonPlanItem(
                        subject = "Constitutional Law II",
                        unit = "Unit I",
                        plannedTopic = "Fundamental Duties",
                        status = "Pending"
                    ),
                    LessonPlanItem(
                        subject = "Criminal Procedure Code",
                        unit = "Unit I",
                        plannedTopic = "Definitions",
                        actualTopic = "Section 2 Definitions",
                        dateTaught = "2026-06-01",
                        status = "Covered"
                    )
                )

                _uiState.update { it.copy(
                    syllabus = mockSyllabus,
                    lessonPlanTracking = mockTracking,
                    isLoading = false
                ) }
            } catch (e: Exception) {
                _uiState.update { it.copy(isLoading = false, error = e.message) }
            }
        }
    }
}
