
package com.example.features.faculty.providers

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.core.network.*
import com.example.core.repository.FacultyRepository
import com.example.features.faculty.models.ResearchEntry
import kotlinx.coroutines.async
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class FacultyResearchState(
    val researchEntries: List<ResearchEntry> = emptyList(),
    val mentorStudents: List<FacultyMentorshipStudentDto> = emptyList(),
    val isLoading: Boolean = false,
    val error: String? = null,
    val isSaving: Boolean = false,
    val saveError: String? = null,
    val saveSuccess: Boolean = false
)

class FacultyResearchViewModel(private val repository: FacultyRepository) : ViewModel() {
    private val _uiState = MutableStateFlow(FacultyResearchState())
    val uiState: StateFlow<FacultyResearchState> = _uiState.asStateFlow()

    init {
        loadData()
    }

    fun loadData() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null) }
            try {
                var softError: String? = null
                lateinit var entries: List<ResearchEntry>
                lateinit var students: List<FacultyMentorshipStudentDto>
                coroutineScope {
                    val entriesDeferred = async { try { repository.getResearchEntries() } catch (e: Exception) { softError = e.message; emptyList() } }
                    val studentsDeferred = async { try { repository.getMentorStudents() } catch (e: Exception) { softError = e.message; emptyList() } }
                    entries = entriesDeferred.await()
                    students = studentsDeferred.await()
                }
                _uiState.update { it.copy(
                    researchEntries = entries,
                    mentorStudents = students,
                    isLoading = false,
                    error = softError
                ) }
            } catch (e: Exception) {
                _uiState.update { it.copy(isLoading = false, error = e.message ?: "Failed to load research data") }
            }
        }
    }

    fun addPublication(request: com.example.core.network.ResearchEntryRequest) {
        viewModelScope.launch {
            _uiState.update { it.copy(isSaving = true, saveError = null, saveSuccess = false) }
            try {
                repository.createResearchEntry(request)
                loadData()
                _uiState.update { it.copy(isSaving = false, saveSuccess = true) }
            } catch (e: Exception) {
                _uiState.update { it.copy(isSaving = false, saveError = e.message ?: "Failed to add publication") }
            }
        }
    }

    fun updatePublication(researchId: String, request: com.example.core.network.ResearchEntryRequest) {
        viewModelScope.launch {
            _uiState.update { it.copy(isSaving = true, saveError = null, saveSuccess = false) }
            try {
                repository.updateResearchEntry(researchId, request)
                loadData()
                _uiState.update { it.copy(isSaving = false, saveSuccess = true) }
            } catch (e: Exception) {
                _uiState.update { it.copy(isSaving = false, saveError = e.message ?: "Failed to update publication") }
            }
        }
    }

    fun deletePublication(researchId: String) {
        viewModelScope.launch {
            try {
                repository.deleteResearchEntry(researchId)
                loadData()
            } catch (e: Exception) {
                _uiState.update { it.copy(saveError = e.message ?: "Failed to delete publication") }
            }
        }
    }

    fun clearSaveStatus() {
        _uiState.update { it.copy(saveError = null, saveSuccess = false) }
    }
}

class FacultyResearchViewModelFactory(private val repository: FacultyRepository) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T = FacultyResearchViewModel(repository) as T
}

data class FacultyInternshipsState(
    val drives: List<FacultyInternshipDriveDto> = emptyList(),
    val applications: List<InternshipApplicationDto> = emptyList(),
    val partners: List<PartnerCompanyDto> = emptyList(),
    val isLoading: Boolean = false,
    val error: String? = null,
    val isReviewing: Boolean = false,
    val reviewError: String? = null,
    val isSavingPartner: Boolean = false,
    val partnerError: String? = null,
    val partnerSaveSuccess: Boolean = false
)

class FacultyInternshipsViewModel(private val repository: FacultyRepository) : ViewModel() {
    private val _uiState = MutableStateFlow(FacultyInternshipsState())
    val uiState = _uiState.asStateFlow()

    init { loadDrives() }

    fun loadDrives() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null) }
            try {
                var softError: String? = null
                lateinit var drives: List<FacultyInternshipDriveDto>
                lateinit var applications: List<InternshipApplicationDto>
                lateinit var partners: List<PartnerCompanyDto>
                coroutineScope {
                    val drivesDeferred = async { try { repository.getInternshipDrives() } catch (e: Exception) { softError = e.message; emptyList() } }
                    val applicationsDeferred = async { try { repository.getInternshipApplications() } catch (e: Exception) { softError = e.message; emptyList() } }
                    val partnersDeferred = async { try { repository.getPartnerCompanies() } catch (e: Exception) { softError = e.message; emptyList() } }
                    drives = drivesDeferred.await()
                    applications = applicationsDeferred.await()
                    partners = partnersDeferred.await()
                }
                _uiState.update { it.copy(drives = drives, applications = applications, partners = partners, isLoading = false, error = softError) }
            } catch (e: Exception) {
                _uiState.update { it.copy(isLoading = false, error = e.message ?: "Failed to load internships") }
            }
        }
    }

    fun reviewApplication(applicationId: String, status: String) {
        viewModelScope.launch {
            _uiState.update { it.copy(isReviewing = true, reviewError = null) }
            try {
                repository.reviewInternshipApplication(applicationId, status)
                loadDrives()
                _uiState.update { it.copy(isReviewing = false) }
            } catch (e: Exception) {
                _uiState.update { it.copy(isReviewing = false, reviewError = e.message ?: "Failed to update application") }
            }
        }
    }

    fun clearReviewError() {
        _uiState.update { it.copy(reviewError = null) }
    }

    fun createPartner(request: PartnerCompanyRequest) {
        viewModelScope.launch {
            _uiState.update { it.copy(isSavingPartner = true, partnerError = null, partnerSaveSuccess = false) }
            try {
                repository.createPartnerCompany(request)
                loadDrives()
                _uiState.update { it.copy(isSavingPartner = false, partnerSaveSuccess = true) }
            } catch (e: Exception) {
                _uiState.update { it.copy(isSavingPartner = false, partnerError = e.message ?: "Failed to save partner") }
            }
        }
    }

    fun deletePartner(partnerId: String) {
        viewModelScope.launch {
            try {
                repository.deletePartnerCompany(partnerId)
                loadDrives()
            } catch (e: Exception) {
                _uiState.update { it.copy(partnerError = e.message ?: "Failed to delete partner") }
            }
        }
    }

    fun clearPartnerStatus() {
        _uiState.update { it.copy(partnerError = null, partnerSaveSuccess = false) }
    }
}

class FacultyInternshipsViewModelFactory(private val repository: FacultyRepository) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T = FacultyInternshipsViewModel(repository) as T
}

data class FacultyLegalEventsState(
    val events: List<FacultyLegalEventDto> = emptyList(),
    val isLoading: Boolean = false,
    val error: String? = null,
    val isSaving: Boolean = false,
    val saveError: String? = null,
    val saveSuccess: Boolean = false
)

class FacultyLegalEventsViewModel(private val repository: FacultyRepository) : ViewModel() {
    private val _uiState = MutableStateFlow(FacultyLegalEventsState())
    val uiState = _uiState.asStateFlow()

    init { loadEvents() }

    fun loadEvents() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null) }
            try {
                val data = repository.getLegalEvents()
                _uiState.update { it.copy(events = data, isLoading = false) }
            } catch (e: Exception) {
                _uiState.update { it.copy(isLoading = false, error = e.message) }
            }
        }
    }

    fun postEvent(request: com.example.core.network.CreateLegalEventRequest) {
        viewModelScope.launch {
            _uiState.update { it.copy(isSaving = true, saveError = null, saveSuccess = false) }
            try {
                repository.postLegalEvent(request)
                loadEvents()
                _uiState.update { it.copy(isSaving = false, saveSuccess = true) }
            } catch (e: Exception) {
                _uiState.update { it.copy(isSaving = false, saveError = e.message ?: "Failed to submit event") }
            }
        }
    }

    fun clearSaveStatus() {
        _uiState.update { it.copy(saveError = null, saveSuccess = false) }
    }
}

class FacultyLegalEventsViewModelFactory(private val repository: FacultyRepository) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T = FacultyLegalEventsViewModel(repository) as T
}

data class FacultySalaryState(
    val slips: List<FacultySalarySlipDto> = emptyList(),
    val isLoading: Boolean = false,
    val error: String? = null
)

class FacultySalaryViewModel(private val repository: FacultyRepository) : ViewModel() {
    private val _uiState = MutableStateFlow(FacultySalaryState())
    val uiState = _uiState.asStateFlow()

    init { loadSlips() }

    fun loadSlips() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null) }
            try {
                val data = repository.getFacultySalarySlips()
                _uiState.update { it.copy(slips = data, isLoading = false) }
            } catch (e: Exception) {
                _uiState.update { it.copy(isLoading = false, error = e.message) }
            }
        }
    }
}

class FacultySalaryViewModelFactory(private val repository: FacultyRepository) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T = FacultySalaryViewModel(repository) as T
}


data class FacultyNotificationsState(
    val notifications: List<NotificationDto> = emptyList(),
    val isLoading: Boolean = false,
    val error: String? = null
)

class FacultyNotificationsViewModel(private val repository: FacultyRepository) : ViewModel() {
    private val _uiState = MutableStateFlow(FacultyNotificationsState())
    val uiState = _uiState.asStateFlow()

    init { loadNotifications() }

    fun loadNotifications() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null) }
            try {
                val data = repository.getFacultyNotifications()
                _uiState.update { it.copy(notifications = data, isLoading = false) }
            } catch (e: Exception) {
                _uiState.update { it.copy(isLoading = false, error = e.message ?: "Failed to load notifications") }
            }
        }
    }

    fun markAsRead(notificationId: String) {
        viewModelScope.launch {
            try {
                repository.markFacultyNotificationRead(notificationId)
                _uiState.update { state ->
                    state.copy(notifications = state.notifications.map {
                        if (it.id == notificationId) it.copy(isRead = true) else it
                    })
                }
            } catch (e: Exception) {
                _uiState.update { it.copy(error = e.message ?: "Failed to mark notification read") }
            }
        }
    }

    fun markAllAsRead() {
        viewModelScope.launch {
            try {
                repository.markAllFacultyNotificationsRead()
                _uiState.update { state ->
                    state.copy(notifications = state.notifications.map { it.copy(isRead = true) })
                }
            } catch (e: Exception) {
                _uiState.update { it.copy(error = e.message ?: "Failed to mark all as read") }
            }
        }
    }
}

class FacultyNotificationsViewModelFactory(private val repository: FacultyRepository) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T = FacultyNotificationsViewModel(repository) as T
}


data class FacultyOnlineMeetingsState(
    val meetings: List<OnlineMeetingDto> = emptyList(),
    val isLoading: Boolean = false,
    val error: String? = null,
    val isSaving: Boolean = false,
    val saveError: String? = null,
    val saveSuccess: Boolean = false
)

class FacultyOnlineMeetingsViewModel(private val repository: FacultyRepository) : ViewModel() {
    private val _uiState = MutableStateFlow(FacultyOnlineMeetingsState())
    val uiState = _uiState.asStateFlow()

    init { loadMeetings() }

    fun loadMeetings() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null) }
            try {
                val data = repository.getOnlineMeetings()
                _uiState.update { it.copy(meetings = data, isLoading = false) }
            } catch (e: Exception) {
                _uiState.update { it.copy(isLoading = false, error = e.message ?: "Failed to load meetings") }
            }
        }
    }

    fun scheduleMeeting(request: com.example.core.network.CreateMeetingRequest, onDone: (Boolean) -> Unit = {}) {
        viewModelScope.launch {
            _uiState.update { it.copy(isSaving = true, saveError = null, saveSuccess = false) }
            try {
                repository.createOnlineMeeting(request)
                loadMeetings()
                _uiState.update { it.copy(isSaving = false, saveSuccess = true) }
                onDone(true)
            } catch (e: Exception) {
                _uiState.update { it.copy(isSaving = false, saveError = e.message ?: "Failed to schedule meeting") }
                onDone(false)
            }
        }
    }

    fun deleteMeeting(meetingId: String, onDone: (Boolean) -> Unit = {}) {
        viewModelScope.launch {
            try {
                repository.deleteOnlineMeeting(meetingId)
                loadMeetings()
                onDone(true)
            } catch (e: Exception) {
                _uiState.update { it.copy(error = e.message ?: "Failed to delete meeting") }
                onDone(false)
            }
        }
    }

    fun clearSaveStatus() {
        _uiState.update { it.copy(saveError = null, saveSuccess = false) }
    }
}

class FacultyOnlineMeetingsViewModelFactory(private val repository: FacultyRepository) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T = FacultyOnlineMeetingsViewModel(repository) as T
}


data class FacultyActivityPointsState(
    val applications: List<ActivityPointDto> = emptyList(),
    val categories: List<ActivityPointCategoryDto> = emptyList(),
    val isLoading: Boolean = false,
    val error: String? = null,
    val isReviewing: Boolean = false,
    val reviewError: String? = null,
    val isSavingCategory: Boolean = false,
    val categoryError: String? = null,
    val categorySaveSuccess: Boolean = false
)

class FacultyActivityPointsViewModel(private val repository: FacultyRepository) : ViewModel() {
    private val _uiState = MutableStateFlow(FacultyActivityPointsState())
    val uiState: StateFlow<FacultyActivityPointsState> = _uiState.asStateFlow()

    init { loadApplications() }

    fun loadApplications() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null) }
            try {
                var softError: String? = null
                lateinit var applications: List<ActivityPointDto>
                lateinit var categories: List<ActivityPointCategoryDto>
                coroutineScope {
                    val applicationsDeferred = async { try { repository.getFacultyActivityPoints() } catch (e: Exception) { softError = e.message; emptyList() } }
                    val categoriesDeferred = async { try { repository.getActivityPointCategories() } catch (e: Exception) { softError = e.message; emptyList() } }
                    applications = applicationsDeferred.await()
                    categories = categoriesDeferred.await()
                }
                _uiState.update { it.copy(applications = applications, categories = categories, isLoading = false, error = softError) }
            } catch (e: Exception) {
                _uiState.update { it.copy(isLoading = false, error = e.message ?: "Failed to load applications") }
            }
        }
    }

    fun reviewApplication(applicationId: String, status: String, approvedPoints: Double, remarks: String?) {
        viewModelScope.launch {
            _uiState.update { it.copy(isReviewing = true, reviewError = null) }
            try {
                repository.reviewActivityPoints(applicationId, status, approvedPoints, remarks)
                loadApplications()
                _uiState.update { it.copy(isReviewing = false) }
            } catch (e: Exception) {
                _uiState.update { it.copy(isReviewing = false, reviewError = e.message ?: "Failed to update application") }
            }
        }
    }

    fun clearReviewError() {
        _uiState.update { it.copy(reviewError = null) }
    }

    fun createCategory(request: ActivityPointCategoryRequest) {
        viewModelScope.launch {
            _uiState.update { it.copy(isSavingCategory = true, categoryError = null, categorySaveSuccess = false) }
            try {
                repository.createActivityPointCategory(request)
                loadApplications()
                _uiState.update { it.copy(isSavingCategory = false, categorySaveSuccess = true) }
            } catch (e: Exception) {
                _uiState.update { it.copy(isSavingCategory = false, categoryError = e.message ?: "Failed to save category") }
            }
        }
    }

    fun deleteCategory(categoryId: String) {
        viewModelScope.launch {
            try {
                repository.deleteActivityPointCategory(categoryId)
                loadApplications()
            } catch (e: Exception) {
                _uiState.update { it.copy(categoryError = e.message ?: "Failed to delete category") }
            }
        }
    }

    fun clearCategoryStatus() {
        _uiState.update { it.copy(categoryError = null, categorySaveSuccess = false) }
    }
}

class FacultyActivityPointsViewModelFactory(private val repository: FacultyRepository) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T = FacultyActivityPointsViewModel(repository) as T
}

data class FacultyCommunicationState(
    val conversations: List<ConversationDto> = emptyList(),
    val contacts: List<MessageContactDto> = emptyList(),
    val isLoading: Boolean = false,
    val error: String? = null,
    val activeThreadUserId: String? = null,
    val activeThreadMessages: List<MessageDto> = emptyList(),
    val isThreadLoading: Boolean = false,
    val isSending: Boolean = false,
    val threadError: String? = null
)

class FacultyCommunicationViewModel(private val repository: FacultyRepository) : ViewModel() {
    private val _uiState = MutableStateFlow(FacultyCommunicationState())
    val uiState: StateFlow<FacultyCommunicationState> = _uiState.asStateFlow()

    init { loadConversations() }

    fun loadConversations() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null) }
            try {
                var softError: String? = null
                lateinit var conversations: List<ConversationDto>
                lateinit var contacts: List<MessageContactDto>
                coroutineScope {
                    val conversationsDeferred = async { try { repository.getConversations() } catch (e: Exception) { softError = e.message; emptyList() } }
                    val contactsDeferred = async { try { repository.getMessageContacts() } catch (e: Exception) { softError = e.message; emptyList() } }
                    conversations = conversationsDeferred.await()
                    contacts = contactsDeferred.await()
                }
                _uiState.update { it.copy(conversations = conversations, contacts = contacts, isLoading = false, error = softError) }
            } catch (e: Exception) {
                _uiState.update { it.copy(isLoading = false, error = e.message ?: "Failed to load conversations") }
            }
        }
    }

    fun openThread(userId: String) {
        _uiState.update { it.copy(activeThreadUserId = userId, activeThreadMessages = emptyList(), threadError = null) }
        viewModelScope.launch {
            _uiState.update { it.copy(isThreadLoading = true) }
            try {
                val messages = repository.getMessageThread(userId)
                repository.markThreadRead(userId)
                _uiState.update { it.copy(activeThreadMessages = messages, isThreadLoading = false) }
                loadConversations()
            } catch (e: Exception) {
                _uiState.update { it.copy(isThreadLoading = false, threadError = e.message ?: "Failed to load thread") }
            }
        }
    }

    fun closeThread() {
        _uiState.update { it.copy(activeThreadUserId = null, activeThreadMessages = emptyList()) }
    }

    fun sendMessage(receiverId: String, body: String) {
        if (body.isBlank()) return
        viewModelScope.launch {
            _uiState.update { it.copy(isSending = true, threadError = null) }
            try {
                repository.sendMessage(receiverId, body)
                val messages = repository.getMessageThread(receiverId)
                _uiState.update { it.copy(activeThreadMessages = messages, isSending = false) }
                loadConversations()
            } catch (e: Exception) {
                _uiState.update { it.copy(isSending = false, threadError = e.message ?: "Failed to send message") }
            }
        }
    }
}

class FacultyCommunicationViewModelFactory(private val repository: FacultyRepository) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T = FacultyCommunicationViewModel(repository) as T
}

data class FacultyCircularsState(
    val circulars: List<com.example.features.parent.models.CollegeNotice> = emptyList(),
    val isLoading: Boolean = false,
    val error: String? = null
)

class FacultyCircularsViewModel(private val repository: FacultyRepository) : ViewModel() {
    private val _uiState = MutableStateFlow(FacultyCircularsState())
    val uiState: StateFlow<FacultyCircularsState> = _uiState.asStateFlow()

    init { loadCirculars() }

    fun loadCirculars() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null) }
            try {
                val data = repository.getNotices()
                _uiState.update { it.copy(circulars = data, isLoading = false) }
            } catch (e: Exception) {
                _uiState.update { it.copy(isLoading = false, error = e.message ?: "Failed to load circulars") }
            }
        }
    }
}

class FacultyCircularsViewModelFactory(private val repository: FacultyRepository) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T = FacultyCircularsViewModel(repository) as T
}

data class FacultyAdvisorLeavesState(
    val leaves: List<AdvisorLeaveDto> = emptyList(),
    val isLoading: Boolean = false,
    val error: String? = null,
    val isReviewing: Boolean = false,
    val reviewError: String? = null
)

class FacultyAdvisorLeavesViewModel(private val repository: FacultyRepository) : ViewModel() {
    private val _uiState = MutableStateFlow(FacultyAdvisorLeavesState())
    val uiState: StateFlow<FacultyAdvisorLeavesState> = _uiState.asStateFlow()

    init { loadLeaves() }

    fun loadLeaves() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null) }
            try {
                val data = repository.getAdvisorStudentLeaves()
                _uiState.update { it.copy(leaves = data, isLoading = false) }
            } catch (e: Exception) {
                _uiState.update { it.copy(isLoading = false, error = e.message ?: "Failed to load leaves") }
            }
        }
    }

    fun reviewLeave(leaveId: String, status: String, remarks: String?) {
        viewModelScope.launch {
            _uiState.update { it.copy(isReviewing = true, reviewError = null) }
            try {
                repository.advisorApproveLeave(leaveId, status, remarks)
                loadLeaves()
                _uiState.update { it.copy(isReviewing = false) }
            } catch (e: Exception) {
                _uiState.update { it.copy(isReviewing = false, reviewError = e.message ?: "Failed to update leave") }
            }
        }
    }

    fun clearReviewError() {
        _uiState.update { it.copy(reviewError = null) }
    }
}

class FacultyAdvisorLeavesViewModelFactory(private val repository: FacultyRepository) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T = FacultyAdvisorLeavesViewModel(repository) as T
}

data class FacultyLeaveApplyState(
    val balances: LeaveBalanceDto? = null,
    val history: List<LeaveRequestDto> = emptyList(),
    val isLoading: Boolean = false,
    val error: String? = null,
    val isSubmitting: Boolean = false,
    val submitError: String? = null,
    val submitSuccess: Boolean = false
)

class FacultyLeaveApplyViewModel(private val repository: FacultyRepository) : ViewModel() {
    private val _uiState = MutableStateFlow(FacultyLeaveApplyState())
    val uiState: StateFlow<FacultyLeaveApplyState> = _uiState.asStateFlow()

    init { loadData() }

    fun loadData() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null) }
            try {
                var softError: String? = null
                var balances: LeaveBalanceDto? = null
                lateinit var history: List<LeaveRequestDto>
                coroutineScope {
                    val balancesDeferred = async { try { repository.getLeaveBalances() } catch (e: Exception) { softError = e.message; null } }
                    val historyDeferred = async { try { repository.getLeaveHistory() } catch (e: Exception) { softError = e.message; emptyList() } }
                    balances = balancesDeferred.await()
                    history = historyDeferred.await()
                }
                _uiState.update { it.copy(balances = balances, history = history, isLoading = false, error = softError) }
            } catch (e: Exception) {
                _uiState.update { it.copy(isLoading = false, error = e.message ?: "Failed to load leave data") }
            }
        }
    }

    fun applyForLeave(type: String, fromDate: String, toDate: String, reason: String, emergencyContact: String) {
        viewModelScope.launch {
            _uiState.update { it.copy(isSubmitting = true, submitError = null, submitSuccess = false) }
            try {
                repository.applyForLeave(type, fromDate, toDate, reason, emergencyContact)
                loadData()
                _uiState.update { it.copy(isSubmitting = false, submitSuccess = true) }
            } catch (e: Exception) {
                _uiState.update { it.copy(isSubmitting = false, submitError = e.message ?: "Failed to submit leave application") }
            }
        }
    }

    fun cancelLeave(leaveId: String) {
        viewModelScope.launch {
            try {
                repository.cancelLeave(leaveId)
                loadData()
            } catch (e: Exception) {
                _uiState.update { it.copy(submitError = e.message ?: "Failed to cancel leave") }
            }
        }
    }

    fun clearSubmitStatus() {
        _uiState.update { it.copy(submitError = null, submitSuccess = false) }
    }
}

class FacultyLeaveApplyViewModelFactory(private val repository: FacultyRepository) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T = FacultyLeaveApplyViewModel(repository) as T
}

data class FacultyClassStudentMgmtState(
    val isAdvisor: Boolean = false,
    val students: List<AdvisorStudentDto> = emptyList(),
    val isLoading: Boolean = false,
    val error: String? = null
)

class FacultyClassStudentMgmtViewModel(private val repository: FacultyRepository) : ViewModel() {
    private val _uiState = MutableStateFlow(FacultyClassStudentMgmtState())
    val uiState: StateFlow<FacultyClassStudentMgmtState> = _uiState.asStateFlow()

    init { loadData() }

    fun loadData() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null) }
            try {
                val assignment = repository.getAdvisorAssignment()
                if (!assignment.isAdvisor) {
                    _uiState.update { it.copy(isAdvisor = false, students = emptyList(), isLoading = false) }
                    return@launch
                }
                val students = repository.getAdvisorClassStudents()
                _uiState.update { it.copy(isAdvisor = true, students = students, isLoading = false) }
            } catch (e: Exception) {
                _uiState.update { it.copy(isLoading = false, error = e.message ?: "Failed to load class roster") }
            }
        }
    }
}

class FacultyClassStudentMgmtViewModelFactory(private val repository: FacultyRepository) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T = FacultyClassStudentMgmtViewModel(repository) as T
}

data class FacultyClassDiaryState(
    val sections: List<FacultyAttendanceSectionDto> = emptyList(),
    val entries: List<ClassDiaryDto> = emptyList(),
    val isLoading: Boolean = false,
    val error: String? = null,
    val isSaving: Boolean = false,
    val saveError: String? = null,
    val saveSuccess: Boolean = false
)

class FacultyClassDiaryViewModel(private val repository: FacultyRepository) : ViewModel() {
    private val _uiState = MutableStateFlow(FacultyClassDiaryState())
    val uiState: StateFlow<FacultyClassDiaryState> = _uiState.asStateFlow()

    init { loadData() }

    fun loadData() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null) }
            try {
                var softError: String? = null
                lateinit var sections: List<FacultyAttendanceSectionDto>
                lateinit var entries: List<ClassDiaryDto>
                coroutineScope {
                    val sectionsDeferred = async { try { repository.getAttendanceSections() } catch (e: Exception) { softError = e.message; emptyList() } }
                    val entriesDeferred = async { try { repository.getClassDiaries() } catch (e: Exception) { softError = e.message; emptyList() } }
                    sections = sectionsDeferred.await()
                    entries = entriesDeferred.await()
                }
                _uiState.update { it.copy(sections = sections, entries = entries, isLoading = false, error = softError) }
            } catch (e: Exception) {
                _uiState.update { it.copy(isLoading = false, error = e.message ?: "Failed to load class diary data") }
            }
        }
    }

    fun saveEntry(request: ClassDiaryRequest, existingId: String?) {
        viewModelScope.launch {
            _uiState.update { it.copy(isSaving = true, saveError = null, saveSuccess = false) }
            try {
                if (existingId != null) {
                    repository.updateClassDiary(existingId, request)
                } else {
                    repository.createClassDiary(request)
                }
                loadData()
                _uiState.update { it.copy(isSaving = false, saveSuccess = true) }
            } catch (e: Exception) {
                _uiState.update { it.copy(isSaving = false, saveError = e.message ?: "Failed to save diary entry") }
            }
        }
    }

    fun clearSaveStatus() {
        _uiState.update { it.copy(saveError = null, saveSuccess = false) }
    }
}

class FacultyClassDiaryViewModelFactory(private val repository: FacultyRepository) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T = FacultyClassDiaryViewModel(repository) as T
}

data class FacultyClassDiaryHodState(
    val entries: List<ClassDiaryDto> = emptyList(),
    val isLoading: Boolean = false,
    val error: String? = null,
    val isReviewing: Boolean = false,
    val reviewError: String? = null
)

class FacultyClassDiaryHodViewModel(private val repository: FacultyRepository) : ViewModel() {
    private val _uiState = MutableStateFlow(FacultyClassDiaryHodState())
    val uiState: StateFlow<FacultyClassDiaryHodState> = _uiState.asStateFlow()

    init { loadData() }

    fun loadData() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null) }
            try {
                val entries = repository.getClassDiaries()
                _uiState.update { it.copy(entries = entries, isLoading = false) }
            } catch (e: Exception) {
                _uiState.update { it.copy(isLoading = false, error = e.message ?: "Failed to load diary entries") }
            }
        }
    }

    fun review(id: String, hodStatus: String, hodRemarks: String?) {
        viewModelScope.launch {
            _uiState.update { it.copy(isReviewing = true, reviewError = null) }
            try {
                repository.reviewClassDiary(id, hodStatus, hodRemarks)
                loadData()
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

class FacultyClassDiaryHodViewModelFactory(private val repository: FacultyRepository) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T = FacultyClassDiaryHodViewModel(repository) as T
}
