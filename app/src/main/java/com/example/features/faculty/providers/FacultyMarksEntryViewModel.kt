package com.example.features.faculty.providers

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.core.network.FacultyAttendanceSectionDto
import com.example.core.network.InternalMarkEntryRequest
import com.example.core.network.SaveInternalMarksRequest
import com.example.core.network.SubmitMarksRequest
import com.example.core.repository.FacultyRepository
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

data class MarksEntryRow(
    val studentId: String,
    val studentName: String,
    val registrationNumber: String,
    val internalExamMark: String,
    val assignmentMark: String,
    val presentationMark: String,
    val vivaVoiceMark: String,
    val attendanceMark: String,
    val status: String
) {
    val total: Double
        get() = listOf(internalExamMark, assignmentMark, presentationMark, vivaVoiceMark, attendanceMark)
            .sumOf { it.toDoubleOrNull() ?: 0.0 }
}

data class FacultyMarksEntryState(
    val sections: List<FacultyAttendanceSectionDto> = emptyList(),
    val selectedSection: FacultyAttendanceSectionDto? = null,
    val academicYear: String = "2026-2027",
    val rows: List<MarksEntryRow> = emptyList(),
    val isLoading: Boolean = true,
    val isLoadingMarks: Boolean = false,
    val error: String? = null,
    val marksError: String? = null,
    val isSaving: Boolean = false,
    val saveError: String? = null,
    val saveMessage: String? = null
)

class FacultyMarksEntryViewModel(private val repository: FacultyRepository) : ViewModel() {
    private val _uiState = MutableStateFlow(FacultyMarksEntryState())
    val uiState: StateFlow<FacultyMarksEntryState> = _uiState.asStateFlow()

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
        loadMarks()
    }

    fun setAcademicYear(year: String) {
        _uiState.update { it.copy(academicYear = year) }
    }

    fun loadMarks() {
        val section = _uiState.value.selectedSection ?: return
        val year = _uiState.value.academicYear
        viewModelScope.launch {
            _uiState.update { it.copy(isLoadingMarks = true, marksError = null) }
            try {
                val marks = repository.getInternalMarks(section.sectionId, section.subjectId, year)
                _uiState.update {
                    it.copy(
                        isLoadingMarks = false,
                        rows = marks.map { m ->
                            MarksEntryRow(
                                studentId = m.studentId,
                                studentName = m.studentName,
                                registrationNumber = m.registrationNumber,
                                internalExamMark = if (m.internalExamMark == 0.0) "" else m.internalExamMark.toString(),
                                assignmentMark = if (m.assignmentMark == 0.0) "" else m.assignmentMark.toString(),
                                presentationMark = if (m.presentationMark == 0.0) "" else m.presentationMark.toString(),
                                vivaVoiceMark = if (m.vivaVoiceMark == 0.0) "" else m.vivaVoiceMark.toString(),
                                attendanceMark = if (m.attendanceMark == 0.0) "" else m.attendanceMark.toString(),
                                status = m.status
                            )
                        }
                    )
                }
            } catch (e: Exception) {
                _uiState.update { it.copy(isLoadingMarks = false, marksError = e.message ?: "Failed to load marks") }
            }
        }
    }

    fun updateMark(studentId: String, field: String, value: String) {
        if (value.isNotEmpty() && value.toDoubleOrNull() == null) return
        _uiState.update { state ->
            state.copy(rows = state.rows.map { row ->
                if (row.studentId != studentId) row
                else when (field) {
                    "exam" -> row.copy(internalExamMark = value)
                    "assignment" -> row.copy(assignmentMark = value)
                    "presentation" -> row.copy(presentationMark = value)
                    "viva" -> row.copy(vivaVoiceMark = value)
                    "attendance" -> row.copy(attendanceMark = value)
                    else -> row
                }
            })
        }
    }

    fun saveDraft() {
        val state = _uiState.value
        val section = state.selectedSection ?: return
        viewModelScope.launch {
            _uiState.update { it.copy(isSaving = true, saveError = null, saveMessage = null) }
            try {
                val request = SaveInternalMarksRequest(
                    sectionId = section.sectionId,
                    subjectId = section.subjectId,
                    academicYear = state.academicYear,
                    semester = section.semester,
                    marks = state.rows.map { row ->
                        InternalMarkEntryRequest(
                            studentId = row.studentId,
                            internalExamMark = row.internalExamMark.toDoubleOrNull() ?: 0.0,
                            assignmentMark = row.assignmentMark.toDoubleOrNull() ?: 0.0,
                            presentationMark = row.presentationMark.toDoubleOrNull() ?: 0.0,
                            vivaVoiceMark = row.vivaVoiceMark.toDoubleOrNull() ?: 0.0,
                            attendanceMark = row.attendanceMark.toDoubleOrNull() ?: 0.0,
                            totalMark = row.total
                        )
                    }
                )
                repository.saveInternalMarks(request)
                _uiState.update { it.copy(isSaving = false, saveMessage = "Draft saved successfully.") }
                loadMarks()
            } catch (e: Exception) {
                _uiState.update { it.copy(isSaving = false, saveError = e.message ?: "Failed to save marks") }
            }
        }
    }

    fun submitToHod() {
        val state = _uiState.value
        val section = state.selectedSection ?: return
        viewModelScope.launch {
            _uiState.update { it.copy(isSaving = true, saveError = null, saveMessage = null) }
            try {
                // Ensure latest edits are saved before submitting.
                val saveRequest = SaveInternalMarksRequest(
                    sectionId = section.sectionId,
                    subjectId = section.subjectId,
                    academicYear = state.academicYear,
                    semester = section.semester,
                    marks = state.rows.map { row ->
                        InternalMarkEntryRequest(
                            studentId = row.studentId,
                            internalExamMark = row.internalExamMark.toDoubleOrNull() ?: 0.0,
                            assignmentMark = row.assignmentMark.toDoubleOrNull() ?: 0.0,
                            presentationMark = row.presentationMark.toDoubleOrNull() ?: 0.0,
                            vivaVoiceMark = row.vivaVoiceMark.toDoubleOrNull() ?: 0.0,
                            attendanceMark = row.attendanceMark.toDoubleOrNull() ?: 0.0,
                            totalMark = row.total
                        )
                    }
                )
                repository.saveInternalMarks(saveRequest)
                repository.submitInternalMarks(SubmitMarksRequest(section.sectionId, section.subjectId, state.academicYear))
                _uiState.update { it.copy(isSaving = false, saveMessage = "Marks submitted to HOD for approval.") }
                loadMarks()
            } catch (e: Exception) {
                _uiState.update { it.copy(isSaving = false, saveError = e.message ?: "Failed to submit marks") }
            }
        }
    }

    fun clearSaveStatus() {
        _uiState.update { it.copy(saveError = null, saveMessage = null) }
    }
}

class FacultyMarksEntryViewModelFactory(private val repository: FacultyRepository) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(FacultyMarksEntryViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return FacultyMarksEntryViewModel(repository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
