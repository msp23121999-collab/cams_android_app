package com.example.features.principal.providers

import androidx.lifecycle.ViewModel
import com.example.core.network.CalendarEventDto
import com.example.features.principal.models.*
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.core.repository.PrincipalRepository
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import kotlinx.coroutines.async
import kotlinx.coroutines.coroutineScope

data class PrincipalState(
    val metrics: PrincipalDashboardMetrics = PrincipalDashboardMetrics("0", "0", "0", "0"),
    val deptPerformance: List<DepartmentPerformance> = emptyList(),
    val pendingLeaves: List<LeaveApproval> = emptyList(),
    val pendingTimetables: List<TimetableApproval> = emptyList(),
    val calendarEvents: List<CalendarEventDto> = emptyList(),
    val isLoading: Boolean = false,
    val error: String? = null
)

class PrincipalViewModel(private val repository: PrincipalRepository) : ViewModel() {
    private val _uiState = MutableStateFlow(PrincipalState())
    val uiState: StateFlow<PrincipalState> = _uiState.asStateFlow()

    init {
        fetchDashboardData()
    }

    fun fetchDashboardData() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null) }
            try {
                val metrics = repository.getDashboardStats()
                val leaves = repository.getPendingLeaveApprovals()
                val timetables = repository.getPendingTimetableApprovals()
                val events = repository.getAcademicCalendar()
                
                _uiState.update { 
                    it.copy(
                        metrics = metrics,
                        pendingLeaves = leaves,
                        pendingTimetables = timetables,
                        calendarEvents = events,
                        isLoading = false
                    ) 
                }
            } catch (e: Exception) {
                _uiState.update { it.copy(isLoading = false, error = e.message) }
            }
        }
    }

    fun approveLeave(id: String, status: String, remarks: String) {
        viewModelScope.launch {
            try {
                repository.approveLeave(id, status, remarks)
                fetchDashboardData() // Refresh
            } catch (e: Exception) {
                _uiState.update { it.copy(error = "Failed to approve leave: ${e.message}") }
            }
        }
    }
}

class PrincipalViewModelFactory(private val repository: PrincipalRepository) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(PrincipalViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return PrincipalViewModel(repository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
// --- Principal Approvals ViewModel ---
data class PrincipalApprovalsState(
    val pendingLeaves: List<LeaveApproval> = emptyList(),
    val pendingFaculty: List<PrincipalPendingFaculty> = emptyList(),
    val pendingTimetables: List<TimetableApproval> = emptyList(),
    val isLoading: Boolean = false,
    val error: String? = null
)

class PrincipalApprovalsViewModel(private val repository: PrincipalRepository) : ViewModel() {
    private val _uiState = MutableStateFlow(PrincipalApprovalsState())
    val uiState: StateFlow<PrincipalApprovalsState> = _uiState.asStateFlow()

    init { loadApprovals() }

    fun loadApprovals() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null) }
            try {
                coroutineScope {
                    val leavesD = async { repository.getPendingLeaveApprovals() }
                    val facultyD = async { repository.getPendingFaculty() }
                    val timetablesD = async { repository.getPendingTimetableApprovals() }
                    _uiState.update {
                        it.copy(
                            pendingLeaves = leavesD.await(),
                            pendingFaculty = facultyD.await(),
                            pendingTimetables = timetablesD.await(),
                            isLoading = false
                        )
                    }
                }
            } catch (e: Exception) {
                _uiState.update { it.copy(isLoading = false, error = e.message) }
            }
        }
    }

    fun approveTimetable(id: String) {
        viewModelScope.launch {
            try {
                repository.approveTimetable(id, "APPROVED", "")
                loadApprovals()
            } catch (e: Exception) {
                _uiState.update { it.copy(error = e.message) }
            }
        }
    }

    fun rejectTimetable(id: String) {
        viewModelScope.launch {
            try {
                repository.approveTimetable(id, "REJECTED", "")
                loadApprovals()
            } catch (e: Exception) {
                _uiState.update { it.copy(error = e.message) }
            }
        }
    }

    fun approveLeave(id: String) {
        viewModelScope.launch {
            try {
                repository.approveLeave(id, "FINAL_APPROVED", "")
                loadApprovals()
            } catch (e: Exception) {
                _uiState.update { it.copy(error = e.message) }
            }
        }
    }

    fun rejectLeave(id: String) {
        viewModelScope.launch {
            try {
                repository.approveLeave(id, "REJECTED_BY_PRINCIPAL", "")
                loadApprovals()
            } catch (e: Exception) {
                _uiState.update { it.copy(error = e.message) }
            }
        }
    }

    fun approveFaculty(id: String) {
        viewModelScope.launch {
            try {
                repository.approveFaculty(id)
                loadApprovals()
            } catch (e: Exception) {
                _uiState.update { it.copy(error = e.message) }
            }
        }
    }

    fun rejectFaculty(id: String) {
        viewModelScope.launch {
            try {
                repository.rejectFaculty(id)
                loadApprovals()
            } catch (e: Exception) {
                _uiState.update { it.copy(error = e.message) }
            }
        }
    }

    fun clearError() {
        _uiState.update { it.copy(error = null) }
    }
}
// --- Principal Grievances ViewModel ---
data class PrincipalGrievancesState(
    val grievances: List<com.example.core.network.GrievanceDto> = emptyList(),
    val isLoading: Boolean = false,
    val error: String? = null,
    val isResolving: Boolean = false,
    val resolveError: String? = null
)

class PrincipalGrievancesViewModel(private val repository: PrincipalRepository) : ViewModel() {
    private val _uiState = MutableStateFlow(PrincipalGrievancesState())
    val uiState: StateFlow<PrincipalGrievancesState> = _uiState.asStateFlow()

    init { loadGrievances() }

    fun loadGrievances() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null) }
            try {
                val data = repository.getGrievances()
                _uiState.update { it.copy(grievances = data, isLoading = false) }
            } catch (e: Exception) {
                _uiState.update { it.copy(isLoading = false, error = e.message) }
            }
        }
    }

    fun resolve(id: String, status: String, comments: String?) {
        viewModelScope.launch {
            _uiState.update { it.copy(isResolving = true, resolveError = null) }
            try {
                repository.resolveGrievance(id, status, comments)
                loadGrievances()
                _uiState.update { it.copy(isResolving = false) }
            } catch (e: Exception) {
                _uiState.update { it.copy(isResolving = false, resolveError = e.message ?: "Failed to update grievance") }
            }
        }
    }

    fun clearResolveError() {
        _uiState.update { it.copy(resolveError = null) }
    }
}
// --- Principal Circulars ViewModel ---
data class PrincipalCircularsState(
    val circulars: List<com.example.core.network.NoticeDto> = emptyList(),
    val isLoading: Boolean = false,
    val error: String? = null,
    val isPublishing: Boolean = false,
    val publishError: String? = null,
    val publishSuccess: Boolean = false
)

class PrincipalCircularsViewModel(private val repository: PrincipalRepository) : ViewModel() {
    private val _uiState = MutableStateFlow(PrincipalCircularsState())
    val uiState: StateFlow<PrincipalCircularsState> = _uiState.asStateFlow()

    init { loadCirculars() }

    fun loadCirculars() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null) }
            try {
                val data = repository.getCirculars()
                _uiState.update { it.copy(circulars = data, isLoading = false) }
            } catch (e: Exception) {
                _uiState.update { it.copy(isLoading = false, error = e.message) }
            }
        }
    }

    fun publishCircular(title: String, body: String, targetAudience: String, priority: String = "Medium") {
        viewModelScope.launch {
            _uiState.update { it.copy(isPublishing = true, publishError = null, publishSuccess = false) }
            try {
                repository.publishCircular(title, body, targetAudience, priority)
                loadCirculars()
                _uiState.update { it.copy(isPublishing = false, publishSuccess = true) }
            } catch (e: Exception) {
                _uiState.update { it.copy(isPublishing = false, publishError = e.message ?: "Failed to publish circular") }
            }
        }
    }

    fun clearPublishStatus() {
        _uiState.update { it.copy(publishError = null, publishSuccess = false) }
    }
}

// --- Principal Research ViewModel ---
data class PrincipalResearchState(
    val compliance: com.example.core.network.PrincipalComplianceResponseDto? = null,
    val isLoading: Boolean = false,
    val error: String? = null,
    val isScanning: Boolean = false,
    val scanResultMessage: String? = null
)

class PrincipalResearchViewModel(private val repository: PrincipalRepository) : ViewModel() {
    private val _uiState = MutableStateFlow(PrincipalResearchState())
    val uiState: StateFlow<PrincipalResearchState> = _uiState.asStateFlow()

    init { loadResearch() }

    fun loadResearch() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null) }
            try {
                val data = repository.getResearchCompliance()
                _uiState.update { it.copy(compliance = data, isLoading = false) }
            } catch (e: Exception) {
                _uiState.update { it.copy(isLoading = false, error = e.message) }
            }
        }
    }

    fun runAuditScan() {
        viewModelScope.launch {
            _uiState.update { it.copy(isScanning = true, scanResultMessage = null) }
            try {
                val flagged = repository.runComplianceScan()
                loadResearch()
                _uiState.update { it.copy(isScanning = false, scanResultMessage = "Scan complete — $flagged faculty flagged for overdue compliance.") }
            } catch (e: Exception) {
                _uiState.update { it.copy(isScanning = false, scanResultMessage = e.message ?: "Compliance scan failed") }
            }
        }
    }

    fun clearScanResult() {
        _uiState.update { it.copy(scanResultMessage = null) }
    }
}

// --- Principal Institutional Performance ViewModel ---
data class PrincipalPerformanceState(
    val departments: List<DepartmentPerformanceSummary> = emptyList(),
    val isLoading: Boolean = true,
    val error: String? = null
)

class PrincipalPerformanceViewModel(private val repository: PrincipalRepository) : ViewModel() {
    private val _uiState = MutableStateFlow(PrincipalPerformanceState())
    val uiState: StateFlow<PrincipalPerformanceState> = _uiState.asStateFlow()

    init { load() }

    fun load() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null) }
            try {
                val depts = repository.getDepartmentPerformance()
                _uiState.update { it.copy(departments = depts, isLoading = false) }
            } catch (e: Exception) {
                _uiState.update { it.copy(isLoading = false, error = e.message ?: "Failed to load performance data") }
            }
        }
    }
}

class PrincipalPerformanceViewModelFactory(private val repository: PrincipalRepository) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(PrincipalPerformanceViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return PrincipalPerformanceViewModel(repository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}

// --- Principal Class Diary ViewModel ---
data class PrincipalClassDiaryState(
    val entries: List<com.example.core.network.ClassDiaryDto> = emptyList(),
    val isLoading: Boolean = true,
    val error: String? = null,
    val isReviewing: Boolean = false,
    val reviewError: String? = null
)

class PrincipalClassDiaryViewModel(private val repository: PrincipalRepository) : ViewModel() {
    private val _uiState = MutableStateFlow(PrincipalClassDiaryState())
    val uiState: StateFlow<PrincipalClassDiaryState> = _uiState.asStateFlow()

    init { load() }

    fun load() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null) }
            try {
                val entries = repository.getClassDiaries()
                _uiState.update { it.copy(entries = entries, isLoading = false) }
            } catch (e: Exception) {
                _uiState.update { it.copy(isLoading = false, error = e.message ?: "Failed to load class diaries") }
            }
        }
    }

    fun review(id: String, status: String, remarks: String?) {
        viewModelScope.launch {
            _uiState.update { it.copy(isReviewing = true, reviewError = null) }
            try {
                repository.reviewClassDiary(id, status, remarks)
                load()
                _uiState.update { it.copy(isReviewing = false) }
            } catch (e: Exception) {
                _uiState.update { it.copy(isReviewing = false, reviewError = e.message ?: "Failed to review entry") }
            }
        }
    }

    fun clearReviewError() {
        _uiState.update { it.copy(reviewError = null) }
    }
}

class PrincipalClassDiaryViewModelFactory(private val repository: PrincipalRepository) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(PrincipalClassDiaryViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return PrincipalClassDiaryViewModel(repository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}

// --- Principal Events Management ViewModel ---
data class PrincipalEventsState(
    val allEvents: List<com.example.core.network.FacultyLegalEventDto> = emptyList(),
    val pendingEvents: List<com.example.core.network.FacultyLegalEventDto> = emptyList(),
    val isLoading: Boolean = true,
    val error: String? = null,
    val isSaving: Boolean = false,
    val saveError: String? = null,
    val saveSuccess: Boolean = false
)

class PrincipalEventsViewModel(private val repository: PrincipalRepository) : ViewModel() {
    private val _uiState = MutableStateFlow(PrincipalEventsState())
    val uiState: StateFlow<PrincipalEventsState> = _uiState.asStateFlow()

    init { load() }

    fun load() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null) }
            try {
                coroutineScope {
                    val allD = async { repository.getAllLegalEvents() }
                    val pendingD = async { repository.getPendingLegalEvents() }
                    _uiState.update { it.copy(allEvents = allD.await(), pendingEvents = pendingD.await(), isLoading = false) }
                }
            } catch (e: Exception) {
                _uiState.update { it.copy(isLoading = false, error = e.message ?: "Failed to load events") }
            }
        }
    }

    fun approve(eventId: String) {
        viewModelScope.launch {
            _uiState.update { it.copy(isSaving = true, saveError = null) }
            try {
                repository.approveLegalEvent(eventId)
                load()
                _uiState.update { it.copy(isSaving = false) }
            } catch (e: Exception) {
                _uiState.update { it.copy(isSaving = false, saveError = e.message ?: "Failed to approve event") }
            }
        }
    }

    fun reject(eventId: String, remarks: String?) {
        viewModelScope.launch {
            _uiState.update { it.copy(isSaving = true, saveError = null) }
            try {
                repository.rejectLegalEvent(eventId, remarks)
                load()
                _uiState.update { it.copy(isSaving = false) }
            } catch (e: Exception) {
                _uiState.update { it.copy(isSaving = false, saveError = e.message ?: "Failed to reject event") }
            }
        }
    }

    fun createAndPublish(request: com.example.core.network.CreateLegalEventRequest) {
        viewModelScope.launch {
            _uiState.update { it.copy(isSaving = true, saveError = null, saveSuccess = false) }
            try {
                repository.createLegalEvent(request)
                // The faculty-post endpoint always creates as Pending; a Principal
                // publishing directly should see it go live immediately, so find
                // the just-created event (matching title+date, newest first) and
                // auto-approve it rather than leaving it stuck in the review queue.
                val pending = repository.getPendingLegalEvents()
                val created = pending.filter { it.title == request.title && it.date == request.date }.maxByOrNull { it.id }
                created?.let { repository.approveLegalEvent(it.id) }
                load()
                _uiState.update { it.copy(isSaving = false, saveSuccess = true) }
            } catch (e: Exception) {
                _uiState.update { it.copy(isSaving = false, saveError = e.message ?: "Failed to publish event") }
            }
        }
    }

    fun clearSaveStatus() {
        _uiState.update { it.copy(saveError = null, saveSuccess = false) }
    }
}

// --- Principal Study Materials ViewModel ---
data class PrincipalStudyMaterialsState2(
    val materials: List<com.example.core.network.HodPendingMaterialDto> = emptyList(),
    val isLoading: Boolean = true,
    val error: String? = null,
    val isReviewing: Boolean = false,
    val reviewError: String? = null
)

class PrincipalStudyMaterialsViewModel2(private val repository: PrincipalRepository) : ViewModel() {
    private val _uiState = MutableStateFlow(PrincipalStudyMaterialsState2())
    val uiState: StateFlow<PrincipalStudyMaterialsState2> = _uiState.asStateFlow()

    init { load() }

    fun load() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null) }
            try {
                val materials = repository.getPendingStudyMaterials()
                _uiState.update { it.copy(materials = materials, isLoading = false) }
            } catch (e: Exception) {
                _uiState.update { it.copy(isLoading = false, error = e.message ?: "Failed to load study materials") }
            }
        }
    }

    fun review(materialId: String, status: String, remarks: String) {
        viewModelScope.launch {
            _uiState.update { it.copy(isReviewing = true, reviewError = null) }
            try {
                repository.reviewStudyMaterial(materialId, status, remarks)
                load()
                _uiState.update { it.copy(isReviewing = false) }
            } catch (e: Exception) {
                _uiState.update { it.copy(isReviewing = false, reviewError = e.message ?: "Failed to review material") }
            }
        }
    }

    fun clearReviewError() {
        _uiState.update { it.copy(reviewError = null) }
    }
}

class PrincipalStudyMaterialsViewModelFactory2(private val repository: PrincipalRepository) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(PrincipalStudyMaterialsViewModel2::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return PrincipalStudyMaterialsViewModel2(repository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}

// --- Principal Institutional Calendar ViewModel ---
data class PrincipalCalendarState(
    val events: List<com.example.core.network.HODCalendarEventDto> = emptyList(),
    val isLoading: Boolean = true,
    val error: String? = null,
    val isSaving: Boolean = false,
    val saveError: String? = null,
    val saveSuccess: Boolean = false
)

class PrincipalCalendarViewModel(private val repository: PrincipalRepository) : ViewModel() {
    private val _uiState = MutableStateFlow(PrincipalCalendarState())
    val uiState: StateFlow<PrincipalCalendarState> = _uiState.asStateFlow()

    init { loadEvents() }

    fun loadEvents() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null) }
            try {
                val events = repository.getInstitutionCalendarEvents()
                _uiState.update { it.copy(events = events, isLoading = false) }
            } catch (e: Exception) {
                _uiState.update { it.copy(isLoading = false, error = e.message ?: "Failed to load calendar events") }
            }
        }
    }

    fun createEvent(request: com.example.core.network.HODCalendarEventCreateRequest) {
        viewModelScope.launch {
            _uiState.update { it.copy(isSaving = true, saveError = null, saveSuccess = false) }
            try {
                repository.createInstitutionCalendarEvent(request)
                loadEvents()
                _uiState.update { it.copy(isSaving = false, saveSuccess = true) }
            } catch (e: Exception) {
                _uiState.update { it.copy(isSaving = false, saveError = e.message ?: "Failed to create event") }
            }
        }
    }

    fun deleteEvent(eventId: String) {
        viewModelScope.launch {
            _uiState.update { it.copy(isSaving = true, saveError = null) }
            try {
                repository.deleteInstitutionCalendarEvent(eventId)
                loadEvents()
                _uiState.update { it.copy(isSaving = false) }
            } catch (e: Exception) {
                _uiState.update { it.copy(isSaving = false, saveError = e.message ?: "Failed to delete event") }
            }
        }
    }

    fun clearSaveStatus() {
        _uiState.update { it.copy(saveError = null, saveSuccess = false) }
    }
}

class PrincipalCalendarViewModelFactory(private val repository: PrincipalRepository) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(PrincipalCalendarViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return PrincipalCalendarViewModel(repository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}

// --- Principal Faculty Overview ViewModel ---
data class PrincipalFacultyOverviewState(
    val faculty: List<com.example.core.network.PrincipalFacultyOverviewDto> = emptyList(),
    val isLoading: Boolean = true,
    val error: String? = null
)

class PrincipalFacultyOverviewViewModel(private val repository: PrincipalRepository) : ViewModel() {
    private val _uiState = MutableStateFlow(PrincipalFacultyOverviewState())
    val uiState: StateFlow<PrincipalFacultyOverviewState> = _uiState.asStateFlow()

    init { load() }

    fun load() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null) }
            try {
                val faculty = repository.getFacultyOverview()
                _uiState.update { it.copy(faculty = faculty, isLoading = false) }
            } catch (e: Exception) {
                _uiState.update { it.copy(isLoading = false, error = e.message ?: "Failed to load faculty overview") }
            }
        }
    }
}

class PrincipalFacultyOverviewViewModelFactory(private val repository: PrincipalRepository) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(PrincipalFacultyOverviewViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return PrincipalFacultyOverviewViewModel(repository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}

class PrincipalEventsViewModelFactory(private val repository: PrincipalRepository) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(PrincipalEventsViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return PrincipalEventsViewModel(repository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}

// --- Principal Infrastructure ViewModel ---
data class PrincipalInfrastructureState(
    val data: com.example.core.network.InfrastructureResponseDto? = null,
    val isLoading: Boolean = false,
    val error: String? = null
)

class PrincipalInfrastructureViewModel(private val repository: PrincipalRepository) : ViewModel() {
    private val _uiState = MutableStateFlow(PrincipalInfrastructureState())
    val uiState: StateFlow<PrincipalInfrastructureState> = _uiState.asStateFlow()

    init { loadInfrastructure() }

    fun loadInfrastructure() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null) }
            try {
                val data = repository.getInfrastructureDetails()
                _uiState.update { it.copy(data = data, isLoading = false) }
            } catch (e: Exception) {
                _uiState.update { it.copy(isLoading = false, error = e.message) }
            }
        }
    }
}
