package com.example.features.academics.providers

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.features.academics.models.*
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class AcademicState(
    val timetable: List<TimetablePeriod> = emptyList(),
    val subjects: List<AcademicSubject> = emptyList(),
    val summary: AcademicSummary? = null,
    val activeTab: String = "timetable", // "timetable" or "academics"
    val isLoading: Boolean = true,
    val error: String? = null
)

class AcademicViewModel : ViewModel() {
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
                delay(1200)
                
                val mockTimetable = mutableListOf<TimetablePeriod>()
                val days = listOf("MONDAY", "TUESDAY", "WEDNESDAY", "THURSDAY", "FRIDAY")
                val periods = listOf(
                    "09:00" to "10:00",
                    "10:00" to "11:00",
                    "11:15" to "12:15",
                    "12:15" to "13:15",
                    "14:00" to "15:00",
                    "15:00" to "16:00"
                )

                val subjectsPool = listOf(
                    "LAW101" to "Constitutional Law II",
                    "LAW205" to "Criminal Procedure Code",
                    "LAW301" to "Family Law I",
                    "LAW105" to "Law of Evidence",
                    "LAW208" to "Property Law",
                    "LAW305" to "Jurisprudence"
                )

                days.forEach { day ->
                    periods.forEachIndexed { idx, times ->
                        if ((0..10).random() > 2) { // 80% chance of class
                            val sub = subjectsPool.random()
                            mockTimetable.add(
                                TimetablePeriod(
                                    subjectCode = sub.first,
                                    subjectName = sub.second,
                                    weekday = day,
                                    startTime = times.first,
                                    endTime = times.second,
                                    facultyName = "Dr. " + listOf("Sharma", "Reddy", "Singh", "Patel", "Das").random(),
                                    room = "Room " + (101..110).random()
                                )
                            )
                        }
                    }
                }
                
                val mockSubjects = listOf(
                    AcademicSubject("LAW101", "Constitutional Law II", 4, 6, "Core", 60, "Dr. Sharma", "B.A. LL.B.", "2023-28"),
                    AcademicSubject("LAW205", "Criminal Procedure Code", 4, 6, "Core", 60, "Prof. Reddy", "B.A. LL.B.", "2023-28"),
                    AcademicSubject("LAW301", "Family Law I", 3, 6, "Theory", 45, "Dr. Singh", "B.A. LL.B.", "2023-28"),
                    AcademicSubject("LAW105", "Law of Evidence", 4, 6, "Core", 60, "Dr. Patel", "B.A. LL.B.", "2023-28"),
                    AcademicSubject("LAW208", "Property Law", 3, 6, "Theory", 45, "Dr. Das", "B.A. LL.B.", "2023-28"),
                    AcademicSubject("LAW305", "Jurisprudence", 4, 6, "Core", 60, "Prof. Iyer", "B.A. LL.B.", "2023-28")
                )

                val totalCredits = mockSubjects.sumOf { it.credits }
                val coreCount = mockSubjects.count { it.type == "Core" }

                _uiState.update { it.copy(
                    timetable = mockTimetable,
                    subjects = mockSubjects,
                    summary = AcademicSummary(
                        totalSubjects = mockSubjects.size,
                        totalCredits = totalCredits,
                        coreSubjects = coreCount,
                        currentSemester = 6
                    ),
                    isLoading = false
                ) }
            } catch (e: Exception) {
                _uiState.update { it.copy(isLoading = false, error = e.message ?: "Failed to load") }
            }
        }
    }
}
