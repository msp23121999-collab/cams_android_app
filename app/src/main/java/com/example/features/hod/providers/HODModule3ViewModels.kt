package com.example.features.hod.providers

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.core.network.HODWorkloadDto
import com.example.core.network.HODMentorDto
import com.example.core.network.LeaveRequestDto
import com.example.core.network.FacultyStudentDto
import com.example.core.repository.HODRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

data class HODWorkloadsUiState(
    val workloads: List<HODWorkloadDto> = emptyList(),
    val isLoading: Boolean = true,
    val error: String? = null
)

class HODWorkloadsViewModel(private val repository: HODRepository) : ViewModel() {
    private val _uiState = MutableStateFlow(HODWorkloadsUiState())
    val uiState: StateFlow<HODWorkloadsUiState> = _uiState.asStateFlow()

    init {
        loadData()
    }

    private fun loadData() {
        viewModelScope.launch {
            try {
                _uiState.value = _uiState.value.copy(isLoading = true, error = null)
                val workloads = repository.getHODWorkloads()
                _uiState.value = _uiState.value.copy(
                    workloads = workloads,
                    isLoading = false
                )
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    error = e.message
                )
            }
        }
    }
}

class HODWorkloadsViewModelFactory(private val repository: HODRepository) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(HODWorkloadsViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return HODWorkloadsViewModel(repository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}

data class HODLeaveApprovalsUiState(
    val pendingLeaves: List<LeaveRequestDto> = emptyList(),
    val isLoading: Boolean = true,
    val error: String? = null
)

class HODLeaveApprovalsViewModel(private val repository: HODRepository) : ViewModel() {
    private val _uiState = MutableStateFlow(HODLeaveApprovalsUiState())
    val uiState: StateFlow<HODLeaveApprovalsUiState> = _uiState.asStateFlow()

    init {
        loadData()
    }

    fun loadData() {
        viewModelScope.launch {
            try {
                _uiState.value = _uiState.value.copy(isLoading = true, error = null)
                val leaves = repository.getPendingLeaveApprovals()
                _uiState.value = _uiState.value.copy(
                    pendingLeaves = leaves,
                    isLoading = false
                )
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    error = e.message
                )
            }
        }
    }

    fun approveLeave(id: String, status: String, remarks: String) {
        viewModelScope.launch {
            try {
                repository.approveLeave(id, status, remarks)
                loadData()
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(error = e.message)
            }
        }
    }
}

class HODLeaveApprovalsViewModelFactory(private val repository: HODRepository) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(HODLeaveApprovalsViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return HODLeaveApprovalsViewModel(repository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}

data class HODMentorAssignmentUiState(
    val mentors: List<HODMentorDto> = emptyList(),
    val students: List<com.example.core.network.HODMentorStudentDto> = emptyList(),
    val isLoading: Boolean = true,
    val error: String? = null,
    val isSaving: Boolean = false,
    val saveError: String? = null,
    val saveSuccess: Boolean = false
)

class HODMentorAssignmentViewModel(private val repository: HODRepository) : ViewModel() {
    private val _uiState = MutableStateFlow(HODMentorAssignmentUiState())
    val uiState: StateFlow<HODMentorAssignmentUiState> = _uiState.asStateFlow()

    init {
        loadData()
    }

    fun loadData() {
        viewModelScope.launch {
            try {
                _uiState.value = _uiState.value.copy(isLoading = true, error = null)
                val payload = repository.getHODMentors()
                _uiState.value = _uiState.value.copy(
                    mentors = payload.faculty,
                    students = payload.students,
                    isLoading = false
                )
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    error = e.message ?: "Failed to load mentors"
                )
            }
        }
    }

    fun assignMentor(facultyId: String, studentIds: List<String>) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isSaving = true, saveError = null, saveSuccess = false)
            try {
                repository.assignHODMentor(facultyId, studentIds)
                loadData()
                _uiState.value = _uiState.value.copy(isSaving = false, saveSuccess = true)
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(isSaving = false, saveError = e.message ?: "Failed to assign mentor")
            }
        }
    }

    fun clearSaveStatus() {
        _uiState.value = _uiState.value.copy(saveError = null, saveSuccess = false)
    }
}

class HODMentorAssignmentViewModelFactory(private val repository: HODRepository) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(HODMentorAssignmentViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return HODMentorAssignmentViewModel(repository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}

data class HODMarkApprovalsUiState(
    val pendingGroups: List<com.example.core.network.HODPendingMarksGroupDto> = emptyList(),
    val isLoading: Boolean = true,
    val error: String? = null,
    val isApproving: Boolean = false
)

class HODMarkApprovalsViewModel(private val repository: HODRepository) : ViewModel() {
    private val _uiState = MutableStateFlow(HODMarkApprovalsUiState())
    val uiState: StateFlow<HODMarkApprovalsUiState> = _uiState.asStateFlow()

    init { loadData() }

    fun loadData() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, error = null)
            try {
                val groups = repository.getHODPendingMarks()
                _uiState.value = _uiState.value.copy(pendingGroups = groups, isLoading = false)
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(isLoading = false, error = e.message ?: "Failed to load pending marks")
            }
        }
    }

    fun approve(sectionId: String, subjectId: String, academicYear: String) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isApproving = true, error = null)
            try {
                repository.approveHODMarks(sectionId, subjectId, academicYear)
                loadData()
                _uiState.value = _uiState.value.copy(isApproving = false)
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(isApproving = false, error = e.message ?: "Failed to approve marks")
            }
        }
    }
}

class HODMarkApprovalsViewModelFactory(private val repository: HODRepository) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(HODMarkApprovalsViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return HODMarkApprovalsViewModel(repository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}

data class HODFacultyApprovalUiState(
    val requests: List<com.example.core.network.FacultyProfileUpdateRequestDto> = emptyList(),
    val isLoading: Boolean = true,
    val error: String? = null,
    val isReviewing: Boolean = false,
    val reviewError: String? = null
)

class HODFacultyApprovalViewModel(private val repository: HODRepository) : ViewModel() {
    private val _uiState = MutableStateFlow(HODFacultyApprovalUiState())
    val uiState: StateFlow<HODFacultyApprovalUiState> = _uiState.asStateFlow()

    init { loadData() }

    fun loadData() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, error = null)
            try {
                val requests = repository.getPendingProfileUpdateRequests()
                _uiState.value = _uiState.value.copy(requests = requests, isLoading = false)
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(isLoading = false, error = e.message ?: "Failed to load pending requests")
            }
        }
    }

    fun review(requestId: String, action: String, comments: String?) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isReviewing = true, reviewError = null)
            try {
                repository.reviewProfileUpdateRequest(requestId, action, comments)
                loadData()
                _uiState.value = _uiState.value.copy(isReviewing = false)
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(isReviewing = false, reviewError = e.message ?: "Failed to review request")
            }
        }
    }

    fun clearReviewError() {
        _uiState.value = _uiState.value.copy(reviewError = null)
    }
}

class HODFacultyApprovalViewModelFactory(private val repository: HODRepository) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(HODFacultyApprovalViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return HODFacultyApprovalViewModel(repository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}

data class HODAttendanceCorrectionUiState(
    val requests: List<com.example.core.network.AttendanceCorrectionDto> = emptyList(),
    val isLoading: Boolean = true,
    val error: String? = null,
    val isReviewing: Boolean = false,
    val reviewError: String? = null
)

class HODAttendanceCorrectionViewModel(private val repository: HODRepository) : ViewModel() {
    private val _uiState = MutableStateFlow(HODAttendanceCorrectionUiState())
    val uiState: StateFlow<HODAttendanceCorrectionUiState> = _uiState.asStateFlow()

    init { loadData() }

    fun loadData() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, error = null)
            try {
                val requests = repository.getAttendanceCorrectionRequests()
                _uiState.value = _uiState.value.copy(requests = requests, isLoading = false)
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(isLoading = false, error = e.message ?: "Failed to load correction requests")
            }
        }
    }

    fun approve(requestId: String) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isReviewing = true, reviewError = null)
            try {
                repository.approveAttendanceCorrection(requestId)
                loadData()
                _uiState.value = _uiState.value.copy(isReviewing = false)
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(isReviewing = false, reviewError = e.message ?: "Failed to approve correction")
            }
        }
    }

    fun reject(requestId: String, remarks: String) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isReviewing = true, reviewError = null)
            try {
                repository.rejectAttendanceCorrection(requestId, remarks)
                loadData()
                _uiState.value = _uiState.value.copy(isReviewing = false)
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(isReviewing = false, reviewError = e.message ?: "Failed to reject correction")
            }
        }
    }

    fun clearReviewError() {
        _uiState.value = _uiState.value.copy(reviewError = null)
    }
}

class HODAttendanceCorrectionViewModelFactory(private val repository: HODRepository) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(HODAttendanceCorrectionViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return HODAttendanceCorrectionViewModel(repository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}

data class HODClassAdvisorUiState(
    val classes: List<com.example.core.network.ClassAdvisorRowDto> = emptyList(),
    val faculty: List<com.example.core.network.ClassAdvisorFacultyDto> = emptyList(),
    val isLoading: Boolean = true,
    val error: String? = null,
    val isSaving: Boolean = false,
    val saveError: String? = null,
    val saveSuccess: Boolean = false
)

class HODClassAdvisorViewModel(private val repository: HODRepository) : ViewModel() {
    private val _uiState = MutableStateFlow(HODClassAdvisorUiState())
    val uiState: StateFlow<HODClassAdvisorUiState> = _uiState.asStateFlow()

    init { loadData() }

    fun loadData() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, error = null)
            try {
                val setup = repository.getHodClasses()
                _uiState.value = _uiState.value.copy(classes = setup.classes, faculty = setup.faculty, isLoading = false)
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(isLoading = false, error = e.message ?: "Failed to load class advisor setup")
            }
        }
    }

    fun assign(academicYearId: String, batch: String, sectionName: String, facultyId: String) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isSaving = true, saveError = null, saveSuccess = false)
            try {
                repository.assignClassAdvisor(academicYearId, batch, sectionName, facultyId)
                loadData()
                _uiState.value = _uiState.value.copy(isSaving = false, saveSuccess = true)
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(isSaving = false, saveError = e.message ?: "Failed to assign class advisor")
            }
        }
    }

    fun clearSaveStatus() {
        _uiState.value = _uiState.value.copy(saveError = null, saveSuccess = false)
    }
}

class HODClassAdvisorViewModelFactory(private val repository: HODRepository) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(HODClassAdvisorViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return HODClassAdvisorViewModel(repository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}

data class HODStudyMaterialsUiState(
    val materials: List<com.example.core.network.HodPendingMaterialDto> = emptyList(),
    val isLoading: Boolean = true,
    val error: String? = null,
    val isReviewing: Boolean = false,
    val reviewError: String? = null
)

class HODStudyMaterialsViewModel(private val repository: HODRepository) : ViewModel() {
    private val _uiState = MutableStateFlow(HODStudyMaterialsUiState())
    val uiState: StateFlow<HODStudyMaterialsUiState> = _uiState.asStateFlow()

    init { loadData() }

    fun loadData() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, error = null)
            try {
                val materials = repository.getHodPendingMaterials()
                _uiState.value = _uiState.value.copy(materials = materials, isLoading = false)
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(isLoading = false, error = e.message ?: "Failed to load pending materials")
            }
        }
    }

    fun review(materialId: String, status: String, remarks: String) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isReviewing = true, reviewError = null)
            try {
                repository.reviewHodMaterial(materialId, status, remarks)
                loadData()
                _uiState.value = _uiState.value.copy(isReviewing = false)
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(isReviewing = false, reviewError = e.message ?: "Failed to review material")
            }
        }
    }

    fun clearReviewError() {
        _uiState.value = _uiState.value.copy(reviewError = null)
    }
}

class HODStudyMaterialsViewModelFactory(private val repository: HODRepository) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(HODStudyMaterialsViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return HODStudyMaterialsViewModel(repository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}

data class HODProfileApprovalsUiState(
    val facultyRequests: List<com.example.core.network.FacultyProfileUpdateRequestDto> = emptyList(),
    val studentRequests: List<com.example.core.network.HODManagementStudentDto> = emptyList(),
    val isLoading: Boolean = true,
    val error: String? = null,
    val isReviewing: Boolean = false,
    val reviewError: String? = null
)

class HODProfileApprovalsViewModel(private val repository: HODRepository) : ViewModel() {
    private val _uiState = MutableStateFlow(HODProfileApprovalsUiState())
    val uiState: StateFlow<HODProfileApprovalsUiState> = _uiState.asStateFlow()

    init { loadData() }

    fun loadData() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, error = null)
            try {
                val facultyRequests = repository.getPendingProfileUpdateRequests()
                val students = repository.getHODManagementStudents().students
                val pendingStudents = students.filter {
                    it.verificationStatus == "SUBMITTED" || it.verificationStatus == "UNDER_HOD_VERIFICATION"
                }
                _uiState.value = _uiState.value.copy(
                    facultyRequests = facultyRequests,
                    studentRequests = pendingStudents,
                    isLoading = false
                )
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(isLoading = false, error = e.message ?: "Failed to load pending approvals")
            }
        }
    }

    fun reviewFaculty(requestId: String, action: String) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isReviewing = true, reviewError = null)
            try {
                repository.reviewProfileUpdateRequest(requestId, action, null)
                loadData()
                _uiState.value = _uiState.value.copy(isReviewing = false)
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(isReviewing = false, reviewError = e.message ?: "Failed to review faculty request")
            }
        }
    }

    fun reviewStudent(studentId: String, action: String) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isReviewing = true, reviewError = null)
            try {
                repository.verifyStudentProfile(studentId, action, null)
                loadData()
                _uiState.value = _uiState.value.copy(isReviewing = false)
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(isReviewing = false, reviewError = e.message ?: "Failed to review student profile")
            }
        }
    }

    fun clearReviewError() {
        _uiState.value = _uiState.value.copy(reviewError = null)
    }
}

class HODProfileApprovalsViewModelFactory(private val repository: HODRepository) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(HODProfileApprovalsViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return HODProfileApprovalsViewModel(repository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}

data class HODCalendarUiState(
    val events: List<com.example.core.network.HODCalendarEventDto> = emptyList(),
    val isLoading: Boolean = true,
    val error: String? = null,
    val isSaving: Boolean = false,
    val saveError: String? = null,
    val saveSuccess: Boolean = false
)

class HODCalendarViewModel(private val repository: HODRepository) : ViewModel() {
    private val _uiState = MutableStateFlow(HODCalendarUiState())
    val uiState: StateFlow<HODCalendarUiState> = _uiState.asStateFlow()

    init { loadEvents() }

    fun loadEvents() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, error = null)
            try {
                val events = repository.getCalendarEvents()
                _uiState.value = _uiState.value.copy(events = events, isLoading = false)
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(isLoading = false, error = e.message ?: "Failed to load calendar events")
            }
        }
    }

    fun createEvent(request: com.example.core.network.HODCalendarEventCreateRequest) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isSaving = true, saveError = null)
            try {
                repository.createCalendarEvent(request)
                loadEvents()
                _uiState.value = _uiState.value.copy(isSaving = false, saveSuccess = true)
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(isSaving = false, saveError = e.message ?: "Failed to create event")
            }
        }
    }

    fun deleteEvent(eventId: String) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isSaving = true, saveError = null)
            try {
                repository.deleteCalendarEvent(eventId)
                loadEvents()
                _uiState.value = _uiState.value.copy(isSaving = false)
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(isSaving = false, saveError = e.message ?: "Failed to delete event")
            }
        }
    }

    fun clearSaveStatus() {
        _uiState.value = _uiState.value.copy(saveError = null, saveSuccess = false)
    }
}

class HODCalendarViewModelFactory(private val repository: HODRepository) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(HODCalendarViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return HODCalendarViewModel(repository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
