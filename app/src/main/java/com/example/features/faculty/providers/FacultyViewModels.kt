package com.example.features.faculty.providers

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.core.repository.FacultyRepository
import com.example.core.network.*
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

// --- Faculty Assignments ViewModel ---
data class FacultyAssignmentsState(
    val assignments: List<FacultyAssignmentDto> = emptyList(),
    val submissions: List<FacultyAssignmentSubmissionDto> = emptyList(),
    val isLoading: Boolean = false,
    val error: String? = null,
    val isSaving: Boolean = false,
    val saveError: String? = null,
    val saveSuccess: Boolean = false
)

class FacultyAssignmentsViewModel(private val repository: FacultyRepository) : ViewModel() {
    private val _uiState = MutableStateFlow(FacultyAssignmentsState())
    val uiState: StateFlow<FacultyAssignmentsState> = _uiState.asStateFlow()

    init { loadAssignments() }

    fun loadAssignments() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null) }
            try {
                var softError: String? = null
                val assignments = try { repository.getAssignments() } catch (e: Exception) { softError = e.message; emptyList() }
                val submissions = try { repository.getAssignmentSubmissions() } catch (e: Exception) { softError = e.message; emptyList() }
                _uiState.update { it.copy(assignments = assignments, submissions = submissions, isLoading = false, error = softError) }
            } catch (e: Exception) {
                _uiState.update { it.copy(isLoading = false, error = e.message) }
            }
        }
    }

    fun createAssignment(request: CreateAssignmentRequest, onDone: (Boolean) -> Unit = {}) {
        viewModelScope.launch {
            _uiState.update { it.copy(isSaving = true, saveError = null, saveSuccess = false) }
            try {
                repository.createAssignment(request)
                loadAssignments()
                _uiState.update { it.copy(isSaving = false, saveSuccess = true) }
                onDone(true)
            } catch (e: Exception) {
                _uiState.update { it.copy(isSaving = false, saveError = e.message ?: "Failed to create assignment") }
                onDone(false)
            }
        }
    }

    fun deleteAssignment(assignmentId: String) {
        viewModelScope.launch {
            try {
                repository.deleteAssignment(assignmentId)
                loadAssignments()
            } catch (e: Exception) {
                _uiState.update { it.copy(saveError = e.message ?: "Failed to delete assignment") }
            }
        }
    }

    fun gradeSubmission(submissionId: String, request: GradeSubmissionRequest, onDone: (Boolean) -> Unit = {}) {
        viewModelScope.launch {
            _uiState.update { it.copy(isSaving = true, saveError = null) }
            try {
                repository.gradeSubmission(submissionId, request)
                loadAssignments()
                _uiState.update { it.copy(isSaving = false) }
                onDone(true)
            } catch (e: Exception) {
                _uiState.update { it.copy(isSaving = false, saveError = e.message ?: "Failed to submit grade") }
                onDone(false)
            }
        }
    }

    fun clearSaveStatus() {
        _uiState.update { it.copy(saveError = null, saveSuccess = false) }
    }
}

class FacultyAssignmentsViewModelFactory(private val repository: FacultyRepository) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        @Suppress("UNCHECKED_CAST")
        return FacultyAssignmentsViewModel(repository) as T
    }
}

// --- Faculty Students ViewModel ---
data class FacultyStudentsState(
    val students: List<FacultyStudentDto> = emptyList(),
    val selectedSemester: Int? = null,
    val isLoading: Boolean = false,
    val error: String? = null
)

class FacultyStudentsViewModel(private val repository: FacultyRepository) : ViewModel() {
    private val _uiState = MutableStateFlow(FacultyStudentsState())
    val uiState: StateFlow<FacultyStudentsState> = _uiState.asStateFlow()

    init { loadStudents() }

    fun loadStudents() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null) }
            try {
                val data = repository.getStudents(_uiState.value.selectedSemester)
                _uiState.update { it.copy(students = data, isLoading = false) }
            } catch (e: Exception) {
                _uiState.update { it.copy(isLoading = false, error = e.message) }
            }
        }
    }

    fun setSemesterFilter(semester: Int?) {
        _uiState.update { it.copy(selectedSemester = semester) }
        loadStudents()
    }
}

class FacultyStudentsViewModelFactory(private val repository: FacultyRepository) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        @Suppress("UNCHECKED_CAST")
        return FacultyStudentsViewModel(repository) as T
    }
}

// --- Faculty Study Materials ViewModel ---
data class FacultyMaterialsState(
    val materials: List<FacultyMaterialDto> = emptyList(),
    val isLoading: Boolean = false,
    val error: String? = null
)

class FacultyMaterialsViewModel(private val repository: FacultyRepository) : ViewModel() {
    private val _uiState = MutableStateFlow(FacultyMaterialsState())
    val uiState: StateFlow<FacultyMaterialsState> = _uiState.asStateFlow()

    init { loadMaterials() }

    fun loadMaterials() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null) }
            try {
                val data = repository.getStudyMaterials()
                _uiState.update { it.copy(materials = data, isLoading = false) }
            } catch (e: Exception) {
                _uiState.update { it.copy(isLoading = false, error = e.message) }
            }
        }
    }

    fun uploadMaterial(payload: com.example.core.network.UploadMaterialRequestDto, onSuccess: () -> Unit) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null) }
            try {
                repository.uploadStudyMaterial(payload)
                loadMaterials()
                onSuccess()
            } catch (e: Exception) {
                _uiState.update { it.copy(isLoading = false, error = e.message) }
            }
        }
    }

    fun uploadMaterialWithFile(
        filePart: okhttp3.MultipartBody.Part,
        fileFormat: String,
        title: String,
        description: String,
        subject: String,
        unit: String,
        topic: String,
        category: String,
        status: String,
        onSuccess: () -> Unit,
        onError: (String) -> Unit
    ) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null) }
            try {
                val uploaded = repository.uploadMaterialFile(filePart)
                val payload = com.example.core.network.UploadMaterialRequestDto(
                    title = title,
                    description = description,
                    subject = subject,
                    unit = unit,
                    topic = topic,
                    category = category,
                    keywords = emptyList(),
                    fileUrl = uploaded.fileUrl,
                    fileFormat = fileFormat,
                    status = status
                )
                repository.uploadStudyMaterial(payload)
                loadMaterials()
                onSuccess()
            } catch (e: Exception) {
                _uiState.update { it.copy(isLoading = false) }
                onError(e.message ?: "Failed to upload material")
            }
        }
    }

    fun deleteMaterial(materialId: String) {
        viewModelScope.launch {
            try {
                repository.archiveStudyMaterial(materialId)
                loadMaterials()
            } catch (e: Exception) {
                _uiState.update { it.copy(error = e.message ?: "Failed to delete material") }
            }
        }
    }
}

class FacultyMaterialsViewModelFactory(private val repository: FacultyRepository) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        @Suppress("UNCHECKED_CAST")
        return FacultyMaterialsViewModel(repository) as T
    }
}

// --- Faculty Lecture Recordings ViewModel ---
data class FacultyRecordingsState(
    val recordings: List<FacultyRecordingDto> = emptyList(),
    val isLoading: Boolean = false,
    val error: String? = null,
    val isSaving: Boolean = false,
    val saveError: String? = null,
    val saveSuccess: Boolean = false
)

class FacultyRecordingsViewModel(private val repository: FacultyRepository) : ViewModel() {
    private val _uiState = MutableStateFlow(FacultyRecordingsState())
    val uiState: StateFlow<FacultyRecordingsState> = _uiState.asStateFlow()

    init { loadRecordings() }

    fun loadRecordings() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null) }
            try {
                val data = repository.getLectureRecordings()
                _uiState.update { it.copy(recordings = data, isLoading = false) }
            } catch (e: Exception) {
                _uiState.update { it.copy(isLoading = false, error = e.message) }
            }
        }
    }

    fun addRecording(request: com.example.core.network.CreateRecordingRequest) {
        viewModelScope.launch {
            _uiState.update { it.copy(isSaving = true, saveError = null, saveSuccess = false) }
            try {
                repository.createRecording(request)
                loadRecordings()
                _uiState.update { it.copy(isSaving = false, saveSuccess = true) }
            } catch (e: Exception) {
                _uiState.update { it.copy(isSaving = false, saveError = e.message ?: "Failed to add recording") }
            }
        }
    }

    fun deleteRecording(recordingId: String) {
        viewModelScope.launch {
            try {
                repository.deleteRecording(recordingId)
                loadRecordings()
            } catch (e: Exception) {
                _uiState.update { it.copy(error = e.message ?: "Failed to delete recording") }
            }
        }
    }

    fun clearSaveStatus() {
        _uiState.update { it.copy(saveError = null, saveSuccess = false) }
    }
}

class FacultyRecordingsViewModelFactory(private val repository: FacultyRepository) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        @Suppress("UNCHECKED_CAST")
        return FacultyRecordingsViewModel(repository) as T
    }
}

