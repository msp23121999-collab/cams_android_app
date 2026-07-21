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

data class AcademicResourceState(
    val assignments: List<Assignment> = emptyList(),
    val materials: List<StudyMaterial> = emptyList(),
    val internalMarks: List<InternalMarkRecord> = emptyList(),
    val isLoading: Boolean = true,
    val error: String? = null
)

class AcademicResourceViewModel : ViewModel() {
    private val _uiState = MutableStateFlow(AcademicResourceState())
    val uiState: StateFlow<AcademicResourceState> = _uiState.asStateFlow()

    // Assuming we can instantiate or get repository here. For now, use the ApiClient manually or passed via constructor.
    // In a real app with DI (Hilt/Dagger) we inject this. Let's create an instance for now or get from context if possible.
    // Since we don't have DI setup shown, we'll initialize it.
    private val studentRepository = com.example.core.repository.StudentRepositoryImpl(com.example.CamsApplication.instance.container.apiService)

    init {
        fetchData()
    }

    fun fetchData() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null) }
            try {
                val assignmentsDto = studentRepository.getAssignments()
                val materialsDto = studentRepository.getStudyMaterials()
                val marksDto = studentRepository.getInternalMarks()

                val assignments = assignmentsDto.map { dto ->
                    Assignment(
                        id = dto.id,
                        title = dto.title,
                        type = dto.type,
                        subject = dto.subject,
                        description = dto.description ?: "",
                        issueDate = "", // Missing in DTO
                        deadline = dto.deadline,
                        status = dto.status,
                        facultyName = "Faculty", // Missing in DTO
                        mySubmission = dto.mySubmission?.let { sub ->
                            Submission(
                                id = sub.id,
                                assignmentId = dto.id,
                                submissionDate = sub.submittedAt,
                                status = sub.status,
                                submittedFileName = sub.submittedFileUrl?.substringAfterLast('/') ?: "",
                                submittedFileUrl = sub.submittedFileUrl ?: "",
                                submittedFileSize = "0 KB"
                            )
                        }
                    )
                }

                val materials = materialsDto.map { dto ->
                    StudyMaterial(
                        id = dto.id,
                        title = dto.title,
                        subject = dto.subjectName ?: "",
                        category = "Material",
                        uploadDate = dto.uploadDate ?: "",
                        fileUrl = dto.fileUrl
                    )
                }

                val internalMarks = marksDto.map { dto ->
                    InternalMarkRecord(
                        id = dto.subjectId,
                        subjectName = dto.subjectName,
                        examScore = dto.internalExamMark,
                        assignmentScore = dto.assignmentMark,
                        presentationScore = dto.presentationMark,
                        vivaScore = dto.vivaVoiceMark,
                        attendanceScore = dto.attendanceMark,
                        totalScore = dto.totalMark,
                        isApproved = dto.isApproved,
                        hodMessage = dto.hodMessage,
                        facultyReply = dto.facultyReply,
                        component = "Final",
                        maxMark = 100.0
                    )
                }

                _uiState.update { it.copy(
                    assignments = assignments,
                    materials = materials,
                    internalMarks = internalMarks,
                    isLoading = false
                ) }
            } catch (e: Exception) {
                _uiState.update { it.copy(isLoading = false, error = e.message ?: "Failed to load data") }
            }
        }
    }

    fun submitAssignment(id: String, fileName: String) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            try {
                val success = studentRepository.submitAssignment(id, fileName, null)
                if (success) {
                    fetchData()
                } else {
                    _uiState.update { it.copy(isLoading = false, error = "Failed to submit assignment") }
                }
            } catch(e: Exception) {
                _uiState.update { it.copy(isLoading = false, error = e.message) }
            }
        }
    }
}
