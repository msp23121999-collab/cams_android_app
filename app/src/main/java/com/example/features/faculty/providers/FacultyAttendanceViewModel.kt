package com.example.features.faculty.providers

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.core.repository.FacultyRepository
import com.example.core.network.BulkAttendanceMarkRequest
import com.example.core.network.FacultyAttendanceSectionDto
import com.example.core.network.FacultyAttendanceStudentDto
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

data class AttendanceRow(
    val regNo: String,
    val name: String,
    val overallAttendance: Int,
    val status: String // "P", "A", "OD"
)

data class FacultyAttendanceState(
    val sections: List<FacultyAttendanceSectionDto> = emptyList(),
    val selectedSection: FacultyAttendanceSectionDto? = null,
    val date: String = SimpleDateFormat("yyyy-MM-dd", Locale.US).format(Date()),
    val hour: Int = 1,
    val students: List<AttendanceRow> = emptyList(),
    val isLoading: Boolean = true,
    val isLoadingStudents: Boolean = false,
    val isSaving: Boolean = false,
    val error: String? = null,
    val studentsError: String? = null,
    val saveError: String? = null,
    val saveSuccess: Boolean = false
)

class FacultyAttendanceViewModel(private val repository: FacultyRepository) : ViewModel() {
    private val _uiState = MutableStateFlow(FacultyAttendanceState())
    val uiState: StateFlow<FacultyAttendanceState> = _uiState.asStateFlow()

    init {
        loadSections()
    }

    fun loadSections() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null) }
            try {
                val sections = repository.getAttendanceSections()
                _uiState.update { it.copy(sections = sections, isLoading = false) }
                if (sections.isNotEmpty() && _uiState.value.selectedSection == null) {
                    selectSection(sections.first())
                }
            } catch (e: Exception) {
                _uiState.update { it.copy(isLoading = false, error = e.message ?: "Failed to load class list") }
            }
        }
    }

    fun selectSection(section: FacultyAttendanceSectionDto) {
        _uiState.update { it.copy(selectedSection = section) }
        loadStudents()
    }

    fun setHour(hour: Int) {
        _uiState.update { it.copy(hour = hour) }
    }

    fun setDate(date: String) {
        _uiState.update { it.copy(date = date) }
    }

    fun loadStudents() {
        val section = _uiState.value.selectedSection ?: return
        viewModelScope.launch {
            _uiState.update { it.copy(isLoadingStudents = true, studentsError = null) }
            try {
                val students = repository.getAttendanceStudents(section.sectionId, section.subjectId)
                _uiState.update {
                    it.copy(
                        isLoadingStudents = false,
                        students = students.map { s ->
                            AttendanceRow(s.regNo, s.name, s.overallAttendance, "P")
                        }
                    )
                }
            } catch (e: Exception) {
                _uiState.update { it.copy(isLoadingStudents = false, studentsError = e.message ?: "Failed to load students") }
            }
        }
    }

    fun setStatus(regNo: String, status: String) {
        _uiState.update { state ->
            state.copy(students = state.students.map { if (it.regNo == regNo) it.copy(status = status) else it })
        }
    }

    fun submitAttendance() {
        val state = _uiState.value
        val section = state.selectedSection ?: return
        viewModelScope.launch {
            _uiState.update { it.copy(isSaving = true, saveError = null, saveSuccess = false) }
            try {
                val request = BulkAttendanceMarkRequest(
                    date = state.date,
                    sectionId = section.sectionId,
                    subjectId = section.subjectId,
                    hour = state.hour,
                    studentStatuses = state.students.associate { it.regNo to it.status }
                )
                repository.markAttendanceBulk(request)
                _uiState.update { it.copy(isSaving = false, saveSuccess = true) }
            } catch (e: Exception) {
                _uiState.update { it.copy(isSaving = false, saveError = e.message ?: "Failed to submit attendance") }
            }
        }
    }

    fun clearSaveStatus() {
        _uiState.update { it.copy(saveError = null, saveSuccess = false) }
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
