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

    init {
        fetchData()
    }

    fun fetchData() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null) }
            try {
                delay(1000)
                
                val mockAssignments = listOf(
                    Assignment(
                        id = "1",
                        title = "Constitutional Amendment Analysis",
                        type = "Case Law Analysis",
                        subject = "Constitutional Law",
                        description = "Analysis of recent amendments to the Constitution of India and their impact on Fundamental Rights.",
                        issueDate = "2026-06-25T10:00:00Z",
                        deadline = "2026-07-07T18:00:00Z",
                        status = "Published",
                        facultyName = "Dr. Amit Sharma",
                        semester = "IV",
                        section = "A",
                        attachments = listOf(Attachment("Reference_Case.pdf", "url", "2.4 MB", "PDF"))
                    ),
                    Assignment(
                        id = "2",
                        title = "Contract Breach Case Study",
                        type = "Legal Research Assignment",
                        subject = "Contracts II",
                        description = "Research assignment on damages for breach of contract with reference to the Indian Contract Act.",
                        issueDate = "2026-06-30T09:00:00Z",
                        deadline = "2026-07-08T17:00:00Z",
                        status = "Published",
                        facultyName = "Prof. Priya Verma",
                        semester = "IV",
                        section = "A"
                    ),
                    Assignment(
                        id = "3",
                        title = "Drafting an NDA",
                        type = "Theory Assignment",
                        subject = "Contracts II",
                        description = "Draft a standard Non-Disclosure Agreement for a tech startup.",
                        issueDate = "2026-06-15T10:00:00Z",
                        deadline = "2026-06-25T18:00:00Z",
                        status = "Published",
                        facultyName = "Prof. Priya Verma",
                        mySubmission = Submission(
                            id = "sub1",
                            assignmentId = "3",
                            submissionDate = "2026-06-24T14:30:00Z",
                            status = "Submitted",
                            submittedFileName = "NDA_Draft_v1.pdf",
                            submittedFileUrl = "url",
                            submittedFileSize = "1.2 MB",
                            evaluation = Evaluation(
                                marksObtained = 17.5,
                                totalMarks = 20,
                                grade = "A+",
                                feedback = "Excellent structure and clarity in the indemnity clause.",
                                remarks = "Well done.",
                                status = "Evaluated",
                                gradedDate = "2026-06-28T11:00:00Z",
                                gradedBy = "Prof. Priya Verma"
                            )
                        )
                    )
                )

                val mockMaterials = listOf(
                    StudyMaterial(
                        id = "m1",
                        title = "Module 1: Preamble and Basic Structure",
                        description = "Detailed notes on the Preamble and the Basic Structure Doctrine.",
                        subject = "Constitutional Law",
                        category = "Lecture Notes",
                        uploadDate = "2026-06-12T10:00:00Z",
                        fileUrl = "url",
                        facultyName = "Dr. Amit Sharma"
                    ),
                    StudyMaterial(
                        id = "m2",
                        title = "Fundamental Rights Presentation",
                        description = "PPT covering Articles 14 to 32 of the Indian Constitution.",
                        subject = "Constitutional Law",
                        category = "PPT Presentation",
                        uploadDate = "2026-06-14T09:00:00Z",
                        fileUrl = "url",
                        facultyName = "Dr. Amit Sharma"
                    ),
                    StudyMaterial(
                        id = "m5",
                        title = "Bailment and Pledge",
                        description = "Video lecture explaining the concepts of Bailment and Pledge.",
                        subject = "Contracts II",
                        category = "Video Resource Link",
                        uploadDate = "2026-06-12T11:00:00Z",
                        fileUrl = "https://www.youtube.com/watch?v=dQw4w9WgXcQ",
                        isVideo = true,
                        fileFormat = "MP4 Link",
                        facultyName = "Prof. Priya Verma"
                    )
                )

                val mockMarks = listOf(
                    InternalMarkRecord("mk1", "Constitutional Law", 8.5, 6.0, 2.5, 4.0, 5.0, 26.0, true, hodMessage = "Keep it up!"),
                    InternalMarkRecord("mk2", "Contracts II", 7.0, 5.0, 2.0, 3.5, 4.0, 21.5, true, facultyReply = "Improved drafting needed."),
                    InternalMarkRecord("mk3", "Criminal Law", 9.0, 6.5, 3.0, 4.5, 5.0, 28.0, true)
                )

                _uiState.update { it.copy(
                    assignments = mockAssignments,
                    materials = mockMaterials,
                    internalMarks = mockMarks,
                    isLoading = false
                ) }
            } catch (e: Exception) {
                _uiState.update { it.copy(isLoading = false, error = e.message ?: "Failed to load data") }
            }
        }
    }

    fun submitAssignment(id: String, fileName: String) {
        viewModelScope.launch {
            // Simulate upload
            delay(1500)
            _uiState.update { state ->
                val updated = state.assignments.map {
                    if (it.id == id) it.copy(status = "Submitted") else it
                }
                state.copy(assignments = updated)
            }
        }
    }
}
