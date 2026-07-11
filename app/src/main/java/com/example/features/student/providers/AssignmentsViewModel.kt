package com.example.features.student.providers

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.features.academics.models.Assignment
import com.example.features.academics.models.Submission
import com.example.features.academics.models.Evaluation
import com.example.core.network.AssignmentDto
import com.example.core.repository.StudentRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class AssignmentsState(
    val assignments: List<Assignment> = emptyList(),
    val isLoading: Boolean = false,
    val error: String? = null,
    val submissionSuccess: Boolean = false
)

class AssignmentsViewModel(
    private val studentRepository: StudentRepository
) : ViewModel() {
    private val _uiState = MutableStateFlow(AssignmentsState())
    val uiState: StateFlow<AssignmentsState> = _uiState.asStateFlow()

    init {
        fetchAssignments()
    }

    fun fetchAssignments() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null) }
            try {
                val dtos = studentRepository.getAssignments()
                val assignments = dtos.map { dto ->
                    Assignment(
                        id = dto.id,
                        title = dto.title,
                        type = dto.type,
                        subject = dto.subject,
                        description = dto.description ?: "",
                        issueDate = "",
                        deadline = dto.deadline,
                        status = dto.status,
                        facultyName = "Faculty",
                        mySubmission = dto.mySubmission?.let { sub ->
                            Submission(
                                id = sub.id,
                                assignmentId = dto.id,
                                submissionDate = sub.submissionDate,
                                status = sub.status,
                                submittedFileName = sub.submittedFile ?: "",
                                submittedFileUrl = sub.submittedFile ?: "",
                                submittedFileSize = "",
                                evaluation = sub.evaluation?.let { ev ->
                                    Evaluation(
                                        marksObtained = ev.marksObtained ?: 0.0,
                                        totalMarks = ev.totalMarks?.toInt() ?: 0,
                                        grade = ev.grade ?: "",
                                        feedback = ev.feedback ?: "",
                                        remarks = "",
                                        status = "Evaluated",
                                        gradedDate = "",
                                        gradedBy = ""
                                    )
                                }
                            )
                        }
                    )
                }
                _uiState.update { it.copy(assignments = assignments, isLoading = false) }
            } catch (e: Exception) {
                _uiState.update { it.copy(isLoading = false, error = e.message ?: "Failed to load assignments") }
            }
        }
    }

    fun submitAssignment(asgId: String, file: String?, text: String?) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null, submissionSuccess = false) }
            try {
                val success = studentRepository.submitAssignment(asgId, file, text)
                if (success) {
                    _uiState.update { it.copy(isLoading = false, submissionSuccess = true) }
                    fetchAssignments()
                } else {
                    _uiState.update { it.copy(isLoading = false, error = "Submission failed") }
                }
            } catch (e: Exception) {
                _uiState.update { it.copy(isLoading = false, error = e.message ?: "Submission failed") }
            }
        }
    }
}

class AssignmentsViewModelFactory(
    private val studentRepository: StudentRepository
) : androidx.lifecycle.ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(AssignmentsViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return AssignmentsViewModel(studentRepository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
