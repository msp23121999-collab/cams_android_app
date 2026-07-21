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
import androidx.paging.Pager
import androidx.paging.PagingConfig
import androidx.paging.PagingData
import androidx.paging.cachedIn
import androidx.paging.map
import com.example.core.network.GenericPagingSource
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

data class AssignmentsState(
    val assignments: List<Assignment> = emptyList(),
    val isLoading: Boolean = false,
    val isUploading: Boolean = false,
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

    val assignmentsPagingFlow: Flow<PagingData<Assignment>> = Pager(
        config = PagingConfig(pageSize = 20, enablePlaceholders = false),
        pagingSourceFactory = { GenericPagingSource({ skip, limit -> studentRepository.getAssignmentsPaged(skip, limit) }) }
    ).flow
        .map { pagingData ->
            pagingData.map { dto ->
                Assignment(
                    id = dto.id,
                    title = dto.title,
                    type = dto.type,
                    subject = dto.subject,
                    unit = dto.unit,
                    topic = dto.topic,
                    description = dto.description ?: "",
                    instructions = dto.instructions,
                    totalMarks = dto.totalMarks ?: 100,
                    issueDate = "",
                    deadline = dto.deadline,
                    status = dto.status,
                    facultyName = "Faculty",
                    semester = dto.semester,
                    section = dto.section,
                    mySubmission = dto.mySubmission?.let { sub ->
                        Submission(
                            id = sub.id,
                            assignmentId = dto.id,
                            submissionDate = sub.submittedAt,
                            status = sub.status,
                            submittedFileName = sub.submittedFileUrl?.substringAfterLast('/') ?: "",
                            submittedFileUrl = sub.submittedFileUrl ?: "",
                            submittedFileSize = "",
                            submittedText = sub.submittedText,
                            evaluation = if (sub.marksObtained != null || sub.grade != null || sub.feedback != null) {
                                Evaluation(
                                    marksObtained = sub.marksObtained ?: 0.0,
                                    totalMarks = dto.totalMarks ?: 100,
                                    grade = sub.grade ?: "",
                                    feedback = sub.feedback ?: "",
                                    remarks = "",
                                    status = "Evaluated",
                                    gradedDate = "",
                                    gradedBy = ""
                                )
                            } else null
                        )
                    }
                )
            }
        }
        .cachedIn(viewModelScope)

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
                        unit = dto.unit,
                        topic = dto.topic,
                        description = dto.description ?: "",
                        instructions = dto.instructions,
                        totalMarks = dto.totalMarks ?: 100,
                        issueDate = "",
                        deadline = dto.deadline,
                        status = dto.status,
                        facultyName = dto.facultyName ?: "Faculty",
                        semester = dto.semester,
                        section = dto.section,
                        mySubmission = dto.mySubmission?.let { sub ->
                            Submission(
                                id = sub.id,
                                assignmentId = dto.id,
                                submissionDate = sub.submittedAt,
                                status = sub.status,
                                submittedFileName = sub.submittedFileUrl?.substringAfterLast('/') ?: "",
                                submittedFileUrl = sub.submittedFileUrl ?: "",
                                submittedFileSize = "",
                                submittedText = sub.submittedText,
                                evaluation = if (sub.marksObtained != null || sub.grade != null || sub.feedback != null) {
                                    Evaluation(
                                        marksObtained = sub.marksObtained ?: 0.0,
                                        totalMarks = dto.totalMarks ?: 100,
                                        grade = sub.grade ?: "",
                                        feedback = sub.feedback ?: "",
                                        remarks = "",
                                        status = "Evaluated",
                                        gradedDate = "",
                                        gradedBy = ""
                                    )
                                } else null
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

    /**
     * Uploads the picked file's real bytes first, then submits the assignment with the
     * resulting file_url. Replaces the previous stub that only passed the file name.
     */
    fun submitAssignmentWithFile(asgId: String, filePart: okhttp3.MultipartBody.Part, text: String?) {
        viewModelScope.launch {
            _uiState.update { it.copy(isUploading = true, error = null, submissionSuccess = false) }
            try {
                val uploadResult = studentRepository.uploadAssignmentSubmission(asgId, filePart)
                if (uploadResult == null) {
                    _uiState.update { it.copy(isUploading = false, error = "File upload failed") }
                    return@launch
                }
                val success = studentRepository.submitAssignment(asgId, uploadResult.fileUrl, text)
                if (success) {
                    _uiState.update { it.copy(isUploading = false, submissionSuccess = true) }
                    fetchAssignments()
                } else {
                    _uiState.update { it.copy(isUploading = false, error = "Submission failed") }
                }
            } catch (e: Exception) {
                _uiState.update { it.copy(isUploading = false, error = e.message ?: "Submission failed") }
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
