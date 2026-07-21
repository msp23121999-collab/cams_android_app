package com.example.features.campus_life.providers

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.ui.graphics.Color
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.core.network.*
import com.example.core.repository.StudentRepository
import com.example.features.campus_life.models.*
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import androidx.lifecycle.ViewModelProvider
import androidx.paging.Pager
import androidx.paging.PagingConfig
import androidx.paging.PagingData
import androidx.paging.cachedIn
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.combine
import androidx.paging.map
import androidx.paging.filter

class ClubsViewModelFactory(private val repository: StudentRepository) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(ClubsViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return ClubsViewModel(repository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}

class CircularsViewModelFactory(private val repository: StudentRepository) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(CircularsViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return CircularsViewModel(repository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}

class CouncilViewModelFactory(private val repository: StudentRepository) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(CouncilViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return CouncilViewModel(repository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}

class GrievancesViewModelFactory(private val repository: StudentRepository) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(GrievancesViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return GrievancesViewModel(repository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}

class OnlineMeetingsViewModelFactory(private val repository: StudentRepository) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(OnlineMeetingsViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return OnlineMeetingsViewModel(repository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}

class LexNovaViewModelFactory(private val repository: StudentRepository) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(LexNovaViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return LexNovaViewModel(repository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}

class LegalEventsViewModelFactory(private val repository: StudentRepository) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(LegalEventsViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return LegalEventsViewModel(repository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}

class LegalSkillsViewModelFactory(private val repository: StudentRepository) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(LegalSkillsViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return LegalSkillsViewModel(repository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}

class LexSphereViewModelFactory(private val repository: StudentRepository) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(LexSphereViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return LexSphereViewModel(repository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}

class InternshipsViewModelFactory(private val repository: StudentRepository) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(InternshipsViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return InternshipsViewModel(repository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}

class CertificationsViewModelFactory(private val repository: StudentRepository) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(CertificationsViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return CertificationsViewModel(repository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}

class ActivityPointsViewModelFactory(private val repository: StudentRepository) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(ActivityPointsViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return ActivityPointsViewModel(repository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}

class CommunityServiceViewModelFactory(private val repository: StudentRepository) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(CommunityServiceViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return CommunityServiceViewModel(repository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}

class InnovationWallViewModelFactory(private val repository: StudentRepository) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(InnovationWallViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return InnovationWallViewModel(repository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}

class ProjectShowcaseViewModelFactory(private val repository: StudentRepository) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(ProjectShowcaseViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return ProjectShowcaseViewModel(repository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}

data class ClubsState(
    val clubs: List<Club> = emptyList(),
    val isLoading: Boolean = true,
    val error: String? = null,
    // Local overrides so join/leave reflect immediately in the paged list
    // instead of waiting for a full repage of stale cached pages.
    val roleOverrides: Map<String, String?> = emptyMap(),
    val announcements: List<ClubAnnouncement> = emptyList()
)

class ClubsViewModel(private val repository: StudentRepository) : ViewModel() {
    private val _uiState = MutableStateFlow(ClubsState())
    val uiState: StateFlow<ClubsState> = _uiState.asStateFlow()

    val clubsPagingFlow: Flow<PagingData<Club>> = Pager(
        config = PagingConfig(pageSize = 20, enablePlaceholders = false),
        pagingSourceFactory = { GenericPagingSource({ skip, limit -> repository.getClubsPaged(skip, limit) }) }
    ).flow
        .map { pagingData ->
            pagingData.map { dto ->
                Club(
                    id = dto.id,
                    name = dto.name,
                    description = dto.description ?: "",
                    category = dto.category ?: "General",
                    membersCount = dto.membersCount,
                    role = dto.userRole ?: "None",
                    president = dto.presidentName ?: "Not assigned",
                    icon = when(dto.category) {
                        "Academic" -> Icons.Filled.Mic
                        "Tech" -> Icons.Filled.Code
                        else -> Icons.Filled.TheaterComedy
                    }
                )
            }
        }
        .cachedIn(viewModelScope)
        .combine(_uiState) { pagingData, state ->
            pagingData.map { club ->
                if (state.roleOverrides.containsKey(club.id)) {
                    club.copy(role = state.roleOverrides[club.id] ?: "None")
                } else {
                    club
                }
            }
        }

    init {
        fetchClubs()
    }

    fun fetchClubs() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null) }
            try {
                val clubDtos = repository.getClubs()
                val clubs = clubDtos.map { dto ->
                    Club(
                        id = dto.id,
                        name = dto.name,
                        description = dto.description ?: "",
                        category = dto.category ?: "General",
                        membersCount = dto.membersCount,
                        role = dto.userRole ?: "None",
                        president = dto.presidentName ?: "Not assigned",
                        icon = when(dto.category) {
                            "Academic" -> Icons.Filled.Mic
                            "Tech" -> Icons.Filled.Code
                            else -> Icons.Filled.TheaterComedy
                        }
                    )
                }
                _uiState.update { it.copy(clubs = clubs, isLoading = false) }
            } catch (e: Exception) {
                _uiState.update { it.copy(isLoading = false, error = e.message) }
            }
        }
        fetchAnnouncements()
    }

    private fun fetchAnnouncements() {
        viewModelScope.launch {
            try {
                val dtos = repository.getClubAnnouncements()
                val announcements = dtos.map { dto ->
                    ClubAnnouncement(
                        title = dto.title,
                        club = dto.clubName,
                        date = formatAnnouncementDate(dto.createdAt),
                        isUrgent = dto.isUrgent
                    )
                }
                _uiState.update { it.copy(announcements = announcements) }
            } catch (e: Exception) {
                // Non-fatal — announcements section just stays empty
            }
        }
    }

    private fun formatAnnouncementDate(iso: String): String {
        return try {
            val parsed = java.time.LocalDateTime.parse(iso.substringBefore("."))
            val today = java.time.LocalDate.now()
            when (parsed.toLocalDate()) {
                today -> "Today"
                today.minusDays(1) -> "Yesterday"
                else -> parsed.toLocalDate().format(java.time.format.DateTimeFormatter.ofPattern("dd MMM"))
            }
        } catch (e: Exception) {
            iso
        }
    }

    fun joinClub(clubId: String) {
        viewModelScope.launch {
            try {
                repository.joinClub(clubId)
                _uiState.update { it.copy(roleOverrides = it.roleOverrides + (clubId to "Member")) }
                fetchClubs() // Refresh non-paged stats (My Clubs count, etc.)
            } catch (e: Exception) {
                _uiState.update { it.copy(error = "Failed to join club: ${e.message}") }
            }
        }
    }

    fun leaveClub(clubId: String) {
        viewModelScope.launch {
            try {
                repository.leaveClub(clubId)
                _uiState.update { it.copy(roleOverrides = it.roleOverrides + (clubId to null)) }
                fetchClubs() // Refresh non-paged stats (My Clubs count, etc.)
            } catch (e: Exception) {
                _uiState.update { it.copy(error = "Failed to leave club: ${e.message}") }
            }
        }
    }
}

data class CircularsState(
    val notices: List<CircularNotice> = emptyList(),
    val filteredNotices: List<CircularNotice> = emptyList(),
    val isLoading: Boolean = true,
    val searchQuery: String = "",
    val selectedCategory: String = "ALL",
    val selectedPriority: String = "ALL",
    val error: String? = null
)

class CircularsViewModel(private val repository: StudentRepository) : ViewModel() {
    private val _uiState = MutableStateFlow(CircularsState())
    val uiState: StateFlow<CircularsState> = _uiState.asStateFlow()

    val noticesPagingFlow: Flow<PagingData<CircularNotice>> = Pager(
        config = PagingConfig(pageSize = 20, enablePlaceholders = false),
        pagingSourceFactory = { GenericPagingSource({ skip, limit -> repository.getNoticesPaged(skip, limit) }) }
    ).flow
        .map { pagingData ->
            pagingData.map { dto ->
                CircularNotice(
                    id = dto.id,
                    title = dto.title,
                    body = dto.body,
                    category = dto.category ?: "",
                    priority = dto.priority ?: "Medium",
                    publishDate = dto.date ?: "",
                    publisherName = dto.publisherName ?: "Admin",
                    publisherRole = dto.publisherRole ?: "Admin",
                    audienceType = "All Students"
                )
            }
        }
        .cachedIn(viewModelScope)
        .combine(_uiState) { pagingData, state ->
            pagingData.filter { notice ->
                val matchesSearch = state.searchQuery.isEmpty() ||
                        notice.title.contains(state.searchQuery, ignoreCase = true) ||
                        notice.body.contains(state.searchQuery, ignoreCase = true)
                val matchesCategory = state.selectedCategory == "ALL" || notice.category == state.selectedCategory
                val matchesPriority = state.selectedPriority == "ALL" || notice.priority.equals(state.selectedPriority, ignoreCase = true)
                matchesSearch && matchesCategory && matchesPriority
            }
        }

    init {
        fetchCirculars()
    }

    fun fetchCirculars() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            try {
                val dtos = repository.getNotices()
                val notices = dtos.map { dto ->
                    CircularNotice(
                        id = dto.id,
                        title = dto.title,
                        body = dto.body,
                        category = dto.category ?: "",
                        priority = dto.priority ?: "Medium",
                        publishDate = dto.date ?: "",
                        publisherName = dto.publisherName ?: "Admin",
                        publisherRole = dto.publisherRole ?: "Admin",
                        audienceType = "All Students"
                    )
                }
                _uiState.update {
                    it.copy(
                        notices = notices,
                        filteredNotices = notices,
                        isLoading = false
                    ) 
                }
            } catch (e: Exception) {
                _uiState.update { it.copy(isLoading = false, error = e.message) }
            }
        }
    }

    fun updateSearchQuery(query: String) {
        _uiState.update { it.copy(searchQuery = query) }
        applyFilters()
    }

    fun updateCategory(category: String) {
        _uiState.update { it.copy(selectedCategory = category) }
        applyFilters()
    }

    fun updatePriority(priority: String) {
        _uiState.update { it.copy(selectedPriority = priority) }
        applyFilters()
    }

    private fun applyFilters() {
        val currentState = _uiState.value
        val filtered = currentState.notices.filter { n ->
            val matchesSearch = currentState.searchQuery.isEmpty() || 
                                n.title.contains(currentState.searchQuery, ignoreCase = true) || 
                                n.body.contains(currentState.searchQuery, ignoreCase = true)
            
            val matchesCategory = currentState.selectedCategory == "ALL" || n.category == currentState.selectedCategory
            
            val matchesPriority = currentState.selectedPriority == "ALL" || n.priority.equals(currentState.selectedPriority, ignoreCase = true)
            
            matchesSearch && matchesCategory && matchesPriority
        }
        _uiState.update { it.copy(filteredNotices = filtered) }
    }
}

data class CouncilState(
    val representatives: List<CouncilRepresentative> = emptyList(),
    val initiatives: List<CouncilInitiative> = emptyList(),
    val feedback: List<StudentFeedback> = emptyList(),
    val proposalsCount: Int = 0,
    val resolvedCount: Int = 0,
    val fundUtilizationPercent: Int = 0,
    val isLoading: Boolean = true,
    val errorMsg: String? = null,
    val successMsg: String? = null
)

class CouncilViewModel(private val repository: StudentRepository) : ViewModel() {
    private val _uiState = MutableStateFlow(CouncilState())
    val uiState: StateFlow<CouncilState> = _uiState.asStateFlow()

    init {
        fetchCouncilData()
    }

    fun fetchCouncilData() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            try {
                val dto = repository.getCouncilData()
                if (dto != null) {
                    _uiState.update { it.copy(
                        representatives = dto.representatives.map { r -> CouncilRepresentative(r.name, r.role, r.year, r.imageUrl ?: "") },
                        initiatives = dto.initiatives.map { i -> CouncilInitiative(i.id, i.title, i.status, i.progress, i.category) },
                        feedback = dto.feedback.map { f -> StudentFeedback(f.id, f.title, f.status, f.upvotes) },
                        proposalsCount = dto.metrics.proposals,
                        resolvedCount = dto.metrics.resolved,
                        fundUtilizationPercent = dto.metrics.fundUtilizationPercent,
                        isLoading = false
                    ) }
                } else {
                    _uiState.update { it.copy(isLoading = false) }
                }
            } catch (e: Exception) {
                _uiState.update { it.copy(isLoading = false) }
            }
        }
    }

    fun submitProposal(title: String, description: String) {
        viewModelScope.launch {
            val success = repository.submitCouncilProposal(title, description)
            if (success) {
                fetchCouncilData()
                _uiState.update { it.copy(successMsg = "Proposal submitted successfully!") }
            } else {
                _uiState.update { it.copy(errorMsg = "Failed to submit proposal. Please try again.") }
            }
        }
    }

    fun submitFeedback(title: String) {
        viewModelScope.launch {
            val dto = repository.submitCouncilFeedback(title)
            if (dto != null) {
                _uiState.update { it.copy(
                    feedback = it.feedback + StudentFeedback(dto.id, dto.title, dto.status, dto.upvotes),
                    successMsg = "Feedback submitted successfully!"
                ) }
            } else {
                _uiState.update { it.copy(errorMsg = "Failed to submit feedback. Please try again.") }
            }
        }
    }

    fun upvoteFeedback(id: Int) {
        viewModelScope.launch {
            val dto = repository.upvoteCouncilFeedback(id)
            if (dto != null) {
                _uiState.update { state ->
                    state.copy(feedback = state.feedback.map {
                        if (it.id == dto.id) StudentFeedback(dto.id, dto.title, dto.status, dto.upvotes) else it
                    })
                }
            } else {
                _uiState.update { it.copy(errorMsg = "Failed to upvote") }
            }
        }
    }

    fun clearMessages() {
        _uiState.update { it.copy(errorMsg = null, successMsg = null) }
    }
}

data class GrievancesState(
    val grievances: List<Grievance> = emptyList(),
    val filteredGrievances: List<Grievance> = emptyList(),
    val isLoading: Boolean = true,
    val searchQuery: String = "",
    val categoryFilter: String = "All",
    val priorityFilter: String = "All",
    val statusFilter: String = "All",
    val sortBy: String = "Newest",
    val error: String? = null,
    val submitError: String? = null
)

class GrievancesViewModel(private val repository: StudentRepository) : ViewModel() {
    private val _uiState = MutableStateFlow(GrievancesState())
    val uiState: StateFlow<GrievancesState> = _uiState.asStateFlow()
    private var currentStudentName: String = "Student"
    private var currentRegNo: String = "N/A"

    val grievancesPagingFlow: Flow<PagingData<Grievance>> = Pager(
        config = PagingConfig(pageSize = 20, enablePlaceholders = false),
        pagingSourceFactory = { GenericPagingSource({ skip, limit -> repository.getGrievancesPaged(skip, limit) }) }
    ).flow
        .map { pagingData ->
            pagingData.map { dto ->
                Grievance(
                    id = dto.id,
                    date = dto.date,
                    studentName = currentStudentName,
                    regNo = currentRegNo,
                    category = dto.category,
                    subject = dto.subject,
                    priority = dto.priority,
                    assignedOfficer = dto.assignedOfficer ?: "Unassigned",
                    status = dto.status,
                    description = dto.description,
                    resolutionDate = dto.resolutionDate ?: "-",
                    rating = dto.resolutionRating ?: 0,
                    feedback = dto.resolutionFeedback ?: ""
                )
            }
        }
        .cachedIn(viewModelScope)

    init {
        fetchGrievances()
    }

    fun fetchGrievances() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            try {
                val profile = repository.getProfile()
                if (profile != null) {
                    currentStudentName = profile.fullName.ifBlank { "Student" }
                    currentRegNo = profile.rollNo.ifBlank { "N/A" }
                }
                val dtos = repository.getGrievances()
                val grievances = dtos.map { dto ->
                    Grievance(
                        id = dto.id,
                        date = dto.date,
                        studentName = currentStudentName,
                        regNo = currentRegNo,
                        category = dto.category,
                        subject = dto.subject,
                        priority = dto.priority,
                        assignedOfficer = dto.assignedOfficer ?: "Unassigned",
                        status = dto.status,
                        description = dto.description,
                        resolutionDate = dto.resolutionDate ?: "-",
                        rating = dto.resolutionRating ?: 0,
                        feedback = dto.resolutionFeedback ?: ""
                    )
                }
                _uiState.update { it.copy(grievances = grievances, filteredGrievances = grievances, isLoading = false) }
                applyFilters()
            } catch (e: Exception) {
                _uiState.update { it.copy(isLoading = false) }
            }
        }
    }

    fun updateSearch(query: String) {
        _uiState.update { it.copy(searchQuery = query) }
        applyFilters()
    }

    fun updateFilters(category: String, priority: String, status: String) {
        _uiState.update { it.copy(categoryFilter = category, priorityFilter = priority, statusFilter = status) }
        applyFilters()
    }

    fun updateSort(sort: String) {
        _uiState.update { it.copy(sortBy = sort) }
        applyFilters()
    }

    private fun applyFilters() {
        val state = _uiState.value
        var result = state.grievances.filter { g ->
            val matchesSearch = state.searchQuery.isEmpty() || 
                                 g.id.contains(state.searchQuery, true) || 
                                 g.subject.contains(state.searchQuery, true)
            
            val matchesCat = state.categoryFilter == "All" || g.category == state.categoryFilter
            val matchesPri = state.priorityFilter == "All" || g.priority == state.priorityFilter
            val matchesStat = state.statusFilter == "All" || g.status == state.statusFilter
            
            matchesSearch && matchesCat && matchesPri && matchesStat
        }

        result = when (state.sortBy) {
            "Newest" -> result.sortedByDescending { it.date }
            "Oldest" -> result.sortedBy { it.date }
            "Priority" -> result.sortedByDescending { it.priority == "High" }
            else -> result
        }

        _uiState.update { it.copy(filteredGrievances = result) }
    }

    fun addGrievance(category: String, priority: String, subject: String, description: String) {
        viewModelScope.launch {
            _uiState.update { it.copy(submitError = null) }
            try {
                repository.submitGrievance(category, priority, subject, description)
                fetchGrievances() // Refresh
            } catch (e: Exception) {
                _uiState.update { it.copy(submitError = e.message ?: "Failed to submit grievance. Please try again.") }
            }
        }
    }

    fun clearSubmitError() {
        _uiState.update { it.copy(submitError = null) }
    }
}

data class OnlineMeetingsState(
    val meetings: List<OnlineMeeting> = emptyList(),
    val filteredMeetings: List<OnlineMeeting> = emptyList(),
    val isLoading: Boolean = true,
    val searchQuery: String = "",
    val statusFilter: String = "All",
    val categoryFilter: String = "All",
    val platformFilter: String = "All",
    val activeTab: String = "Schedule" // Schedule, History, Calendar
)

class OnlineMeetingsViewModel(private val repository: StudentRepository) : ViewModel() {
    private val _uiState = MutableStateFlow(OnlineMeetingsState())
    val uiState: StateFlow<OnlineMeetingsState> = _uiState.asStateFlow()

    init {
        fetchMeetings()
    }

    fun fetchMeetings() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            try {
                val dtos = repository.getOnlineMeetings()
                val meetings = dtos.map { dto ->
                    OnlineMeeting(
                        id = dto.id,
                        title = dto.title,
                        category = dto.category,
                        organizer = dto.organizer,
                        date = dto.date,
                        time = dto.time,
                        duration = dto.duration,
                        platform = MeetingPlatform.values().find { it.label.equals(dto.platform, ignoreCase = true) } ?: MeetingPlatform.CUSTOM,
                        meetingLink = dto.meetingLink,
                        status = MeetingStatus.values().find { it.label.equals(dto.status, ignoreCase = true) } ?: MeetingStatus.SCHEDULED,
                        participants = dto.participants,
                        attended = dto.attended,
                        agenda = dto.agenda,
                        description = dto.description,
                        notes = dto.notes ?: "",
                        recordingAvailable = dto.recordingAvailable,
                        recordingUrl = dto.recordingUrl,
                        room = dto.room
                    )
                }
                _uiState.update { it.copy(meetings = meetings, filteredMeetings = meetings, isLoading = false) }
                applyFilters()
            } catch (e: Exception) {
                _uiState.update { it.copy(isLoading = false) }
            }
        }
    }

    fun updateSearch(query: String) {
        _uiState.update { it.copy(searchQuery = query) }
        applyFilters()
    }

    fun updateFilters(status: String, category: String, platform: String) {
        _uiState.update { it.copy(statusFilter = status, categoryFilter = category, platformFilter = platform) }
        applyFilters()
    }

    fun updateTab(tab: String) {
        _uiState.update { it.copy(activeTab = tab) }
        applyFilters()
    }

    private fun applyFilters() {
        val state = _uiState.value
        var result = state.meetings.filter { m ->
            val matchesSearch = state.searchQuery.isEmpty() || 
                                m.title.contains(state.searchQuery, true) || 
                                m.organizer.contains(state.searchQuery, true)
            
            val matchesStatus = state.statusFilter == "All" || m.status.label == state.statusFilter
            val matchesCat = state.categoryFilter == "All" || m.category == state.categoryFilter
            val matchesPlat = state.platformFilter == "All" || m.platform.label == state.platformFilter
            
            val matchesTab = when(state.activeTab) {
                "History" -> m.status == MeetingStatus.COMPLETED || m.status == MeetingStatus.CANCELLED
                "Schedule" -> m.status != MeetingStatus.COMPLETED && m.status != MeetingStatus.CANCELLED
                else -> true
            }

            matchesSearch && matchesStatus && matchesCat && matchesPlat && matchesTab
        }
        _uiState.update { it.copy(filteredMeetings = result) }
    }

    fun markAttendance(meetingId: String) {
        _uiState.update { state ->
            val updated = state.meetings.map { 
                if (it.id == meetingId) it.copy(attended = true) else it 
            }
            state.copy(meetings = updated)
        }
        applyFilters()
    }
}

// --- LexNova ViewModel ---

data class LexNovaChatMessage(val text: String, val isUser: Boolean)

data class LexNovaState(
    val kpis: List<LexNovaKPI> = emptyList(),
    val timetable: List<TimetableEntry> = emptyList(),
    val documents: List<KnowledgeDocument> = emptyList(),
    val alumni: List<AlumniMentor> = emptyList(),
    val isLoading: Boolean = true,
    val activeTab: String = "Command Center",
    val chatMessages: List<LexNovaChatMessage> = listOf(LexNovaChatMessage("How can I assist your legal research today?", isUser = false)),
    val chatInput: String = "",
    val isSendingMessage: Boolean = false,
    val chatSessionId: String? = null
)

class LexNovaViewModel(private val repository: StudentRepository) : ViewModel() {
    private val _uiState = MutableStateFlow(LexNovaState())
    val uiState: StateFlow<LexNovaState> = _uiState.asStateFlow()

    init {
        fetchLexNovaData()
    }

    fun fetchLexNovaData() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            try {
                val dtos = repository.getLexNovaStats()
                val kpis = dtos.map { dto ->
                    LexNovaKPI(
                        dto.title, dto.value, dto.subtitle,
                        when(dto.type) {
                            "GPA" -> Icons.Filled.School
                            "Research" -> Icons.Filled.Bookmark
                            "Advocacy" -> Icons.Filled.Gavel
                            else -> Icons.Filled.Schedule
                        },
                        when(dto.type) {
                            "GPA" -> Color(0xFFD4AF37)
                            "Research" -> Color(0xFF10B981)
                            "Advocacy" -> Color(0xFF3B82F6)
                            else -> Color(0xFFF43F5E)
                        }
                    )
                }
                _uiState.update { it.copy(kpis = kpis, isLoading = false) }
            } catch (e: Exception) {
                _uiState.update { it.copy(isLoading = false) }
            }
        }
    }

    fun updateTab(tab: String) {
        _uiState.update { it.copy(activeTab = tab) }
    }

    fun updateChatInput(text: String) {
        _uiState.update { it.copy(chatInput = text) }
    }

    fun sendChatMessage() {
        val message = _uiState.value.chatInput.trim()
        if (message.isBlank()) return
        viewModelScope.launch {
            _uiState.update {
                it.copy(
                    chatMessages = it.chatMessages + LexNovaChatMessage(message, isUser = true),
                    chatInput = "",
                    isSendingMessage = true
                )
            }
            val response = repository.sendChatMessage(message, _uiState.value.chatSessionId)
            if (response != null) {
                _uiState.update {
                    it.copy(
                        chatMessages = it.chatMessages + LexNovaChatMessage(response.response, isUser = false),
                        chatSessionId = response.sessionId,
                        isSendingMessage = false
                    )
                }
            } else {
                _uiState.update {
                    it.copy(
                        chatMessages = it.chatMessages + LexNovaChatMessage("Sorry, I couldn't process that. Please try again.", isUser = false),
                        isSendingMessage = false
                    )
                }
            }
        }
    }
}

// --- Legal Events ViewModel ---

data class LegalEventsState(
    val events: List<LegalEvent> = emptyList(),
    val filteredEvents: List<LegalEvent> = emptyList(),
    val registrations: List<LegalEvent> = emptyList(),
    val questions: List<JudgeQuestion> = emptyList(),
    val debates: List<DebateEntry> = emptyList(),
    val isLoading: Boolean = true,
    val activeTab: String = "Upcoming",
    val search: String = "",
    val categoryFilter: String = "All"
)
// --- Legal Skills ViewModel ---

data class LegalSkillsState(
    val courses: List<LawCourse> = emptyList(),
    val registered: Map<String, LearningProgress> = emptyMap(),
    val workshops: List<Workshop> = emptyList(),
    val caseStudies: List<CaseStudy> = emptyList(),
    val mootActivities: List<MootActivity> = emptyList(),
    val isLoading: Boolean = true,
    val activeTab: String = "browse"
)

class LegalSkillsViewModel(private val repository: StudentRepository) : ViewModel() {
    private val _uiState = MutableStateFlow(LegalSkillsState())
    val uiState: StateFlow<LegalSkillsState> = _uiState.asStateFlow()

    init {
        fetchLegalSkillsData()
    }

    fun fetchLegalSkillsData() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            try {
                // This catalog mirrors the static COURSE_CATALOG/WORKSHOPS/CASE_STUDIES/
                // MOOT_ACTIVITIES constants in the web app (source of truth) — that app
                // has no backend for this feature either, it's a deliberately client-side
                // browse/track-progress catalog, not fetched from a server.
                _uiState.update {
                    it.copy(
                        courses = defaultCourseCatalog,
                        workshops = defaultWorkshops,
                        caseStudies = defaultCaseStudies,
                        mootActivities = defaultMootActivities,
                        isLoading = false
                    )
                }
            } catch (e: Exception) {
                _uiState.update { it.copy(isLoading = false) }
            }
        }
    }

    fun updateTab(tab: String) {
        _uiState.update { it.copy(activeTab = tab) }
    }

    fun registerCourse(courseId: String) {
        _uiState.update { state ->
            val updated = state.registered.toMutableMap()
            updated[courseId] = LearningProgress(courseId, 0, false)
            state.copy(registered = updated)
        }
    }

    fun incrementProgress(courseId: String) {
        _uiState.update { state ->
            val updated = state.registered.toMutableMap()
            val current = updated[courseId]
            if (current != null) {
                val next = (current.progress + 25).coerceAtMost(100)
                updated[courseId] = current.copy(progress = next, certificateEarned = next == 100)
            }
            state.copy(registered = updated)
        }
    }

    fun publishCourse(course: LawCourse) {
        _uiState.update { it.copy(courses = listOf(course) + it.courses) }
    }

    companion object {
        val defaultCourseCatalog = listOf(
            LawCourse("c1", "Constitutional Law Masterclass", "Public Law", "8 Weeks", 4, 142, "Justice R. Subrahmanian", "Advanced"),
            LawCourse("c2", "Legal Research & Writing", "Skills", "4 Weeks", 2, 98, "Dr. Anjali Verma", "Beginner"),
            LawCourse("c3", "Contract Drafting and Documentation", "Corporate", "6 Weeks", 3, 185, "Adv. Rohit Singhal", "Intermediate"),
            LawCourse("c4", "Cyber Law & Digital Evidence", "Technology", "6 Weeks", 3, 74, "Prof. K. Sandeep", "Intermediate"),
            LawCourse("c5", "Intellectual Property Rights (IPR)", "Corporate", "6 Weeks", 3, 82, "Meera Sen", "Intermediate"),
            LawCourse("c6", "Corporate Law Essentials", "Corporate", "5 Weeks", 3, 120, "Siddharth Birla", "Intermediate"),
            LawCourse("c7", "Moot Court Preparation Program", "Skills", "4 Weeks", 2, 210, "Dr. K. Menon", "Beginner"),
            LawCourse("c8", "Alternative Dispute Resolution (ADR)", "Litigation", "6 Weeks", 3, 95, "Judge Swati Shinde", "Advanced"),
            LawCourse("c9", "Criminal Litigation Practice", "Litigation", "8 Weeks", 4, 156, "Adv. Harish Salve", "Advanced"),
            LawCourse("c10", "Legal Ethics & Professional Responsibility", "Public Law", "3 Weeks", 1, 300, "Bar Council Member", "Beginner"),
            LawCourse("c11", "Arbitration & Mediation Certification", "Litigation", "6 Weeks", 3, 88, "Justice M. Lokur", "Advanced"),
            LawCourse("c12", "Courtroom Advocacy Skills", "Skills", "5 Weeks", 2, 110, "Adv. Mukul Rohatgi", "Intermediate"),
            LawCourse("c13", "AI & Law Fundamentals", "Technology", "4 Weeks", 2, 175, "Varun Sen", "Beginner"),
            LawCourse("c14", "Legal Technology and e-Governance", "Technology", "5 Weeks", 2, 65, "Dr. P. Sharma", "Intermediate")
        )

        val defaultWorkshops = listOf(
            Workshop(1, "Cross-Examination Strategies in Criminal Trials", "Advocate Harish Salve", "June 15, 2026", "10:00 AM", 420, "Guest Lecture"),
            Workshop(2, "Mediation Techniques in Matrimonial Disputes", "Judge Swati Shinde", "June 20, 2026", "02:30 PM", 180, "Webinar"),
            Workshop(3, "Mergers & Acquisitions: Corporate Due Diligence", "Siddharth Birla", "June 28, 2026", "05:00 PM", 310, "Workshop")
        )

        val defaultCaseStudies = listOf(
            CaseStudy("case-01", "Keshvananda Bharati v. State of Kerala", "1.2 MB", "Basic Structure Doctrine", "Judgment PDF"),
            CaseStudy("case-02", "Shreya Singhal v. Union of India", "850 KB", "Freedom of Speech (Sec 66A)", "Case Analysis"),
            CaseStudy("case-03", "Navtej Singh Johar Judgment", "1.5 MB", "Decriminalization of Homosexuality", "Summary")
        )

        val defaultMootActivities = listOf(
            MootActivity(1, "32nd National Moot Court Championship", "Oralist", "Ongoing", "July 5-7, 2026"),
            MootActivity(2, "Intra-College Mock Trial Briefing", "Defense Counsel", "Submissions Open", "June 18, 2026")
        )
    }
}

// --- LexSphere ViewModel ---

data class LexSphereState(
    val drives: List<InternshipDrive> = emptyList(),
    val alumni: List<AlumniMentorDto> = emptyList(),
    val applications: List<InternshipApplication> = emptyList(),
    val isLoading: Boolean = true,
    val searchQuery: String = "",
    val errorMsg: String? = null
)

class LexSphereViewModel(private val repository: StudentRepository) : ViewModel() {
    private val _uiState = MutableStateFlow(LexSphereState())
    val uiState: StateFlow<LexSphereState> = _uiState.asStateFlow()

    init {
        fetchLexSphereData()
    }

    fun fetchLexSphereData() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            try {
                val dtos = repository.getInternshipDrives()
                val alumniList = repository.getAlumniNetwork()
                val drives = dtos.map { dto ->
                    InternshipDrive(dto.id, dto.companyName, dto.role, dto.`package` ?: "N/A", dto.status, dto.driveDate ?: "TBD")
                }
                _uiState.update { it.copy(drives = drives, alumni = alumniList, isLoading = false) }
            } catch (e: Exception) {
                _uiState.update { it.copy(isLoading = false) }
            }
        }
    }

    fun updateSearch(query: String) {
        _uiState.update { it.copy(searchQuery = query) }
    }

    fun submitApplication(app: InternshipApplication) {
        viewModelScope.launch {
            try {
                val drive = _uiState.value.drives.find { it.name == app.driveName }
                val driveId = drive?.id
                if (driveId != null) {
                    repository.applyToInternshipDrive(driveId)
                }
                _uiState.update { it.copy(applications = it.applications + app) }
            } catch (e: Exception) {
                _uiState.update { it.copy(errorMsg = "Failed to submit application. Please try again.") }
            }
        }
    }

    fun clearError() {
        _uiState.update { it.copy(errorMsg = null) }
    }
}

// --- Internships ViewModel ---

data class InternshipsState(
    val internships: List<InternshipRecord> = emptyList(),
    val isLoading: Boolean = true,
    val searchQuery: String = "",
    val typeFilter: String = "All",
    val errorMsg: String? = null
)

class InternshipsViewModel(private val repository: StudentRepository) : ViewModel() {
    private val _uiState = MutableStateFlow(InternshipsState())
    val uiState: StateFlow<InternshipsState> = _uiState.asStateFlow()

    val internshipsPagingFlow: Flow<PagingData<InternshipRecord>> = _uiState
        .map { state -> PagingData.from(state.internships) }
        .cachedIn(viewModelScope)

    init {
        fetchInternships()
    }

    fun fetchInternships() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            try {
                val internships = repository.getPersonalInternships()
                _uiState.update { it.copy(internships = internships, isLoading = false) }
            } catch (e: Exception) {
                _uiState.update { it.copy(isLoading = false) }
            }
        }
    }

    fun addInternship(internship: InternshipRecord) {
        viewModelScope.launch {
            val success = repository.addInternship(internship)
            if (success) fetchInternships()
            else _uiState.update { it.copy(errorMsg = "Failed to save internship. Please try again.") }
        }
    }

    fun deleteInternship(id: String) {
        viewModelScope.launch {
            val success = repository.deleteInternship(id)
            if (success) fetchInternships()
            else _uiState.update { it.copy(errorMsg = "Failed to delete internship. Please try again.") }
        }
    }

    fun clearError() {
        _uiState.update { it.copy(errorMsg = null) }
    }

    fun updateSearch(query: String) {
        _uiState.update { it.copy(searchQuery = query) }
    }

    fun updateFilter(filter: String) {
        _uiState.update { it.copy(typeFilter = filter) }
    }
}

// --- Certifications ViewModel ---

data class CertificationsState(
    val certifications: List<CertificationRecord> = emptyList(),
    val isLoading: Boolean = true,
    val searchQuery: String = "",
    val categoryFilter: String = "All",
    val errorMsg: String? = null
)

class CertificationsViewModel(private val repository: StudentRepository) : ViewModel() {
    private val _uiState = MutableStateFlow(CertificationsState())
    val uiState: StateFlow<CertificationsState> = _uiState.asStateFlow()

    init {
        fetchCertifications()
    }

    fun fetchCertifications() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            try {
                val dtos = repository.getCertificationsList()
                val certifications = dtos.map { dto ->
                    CertificationRecord(
                        dto.id, dto.title, dto.issuer, dto.date, dto.category, dto.isVerified, dto.type, dto.fileUrl
                    )
                }
                _uiState.update { it.copy(certifications = certifications, isLoading = false) }
            } catch (e: Exception) {
                _uiState.update { it.copy(isLoading = false) }
            }
        }
    }

    fun addCertification(cert: CertificationRecord, filePart: okhttp3.MultipartBody.Part? = null) {
        viewModelScope.launch {
            val uploadedUrl = if (filePart != null) repository.uploadProfileDocument("certification", filePart) else null
            val dto = repository.createCertification(cert.title, cert.authority, cert.date, cert.category, cert.type, uploadedUrl)
            if (dto != null) {
                val created = CertificationRecord(dto.id, dto.title, dto.issuer, dto.date, dto.category, dto.isVerified, dto.type, dto.fileUrl)
                _uiState.update { it.copy(certifications = listOf(created) + it.certifications) }
            } else {
                _uiState.update { it.copy(errorMsg = "Failed to save certification. Please try again.") }
            }
        }
    }

    fun clearError() {
        _uiState.update { it.copy(errorMsg = null) }
    }

    fun deleteCertification(id: String) {
        val previous = _uiState.value.certifications
        _uiState.update { it.copy(certifications = it.certifications.filter { c -> c.id != id }) }
        viewModelScope.launch {
            val success = repository.deleteCertification(id)
            if (!success) {
                _uiState.update { it.copy(certifications = previous) }
            }
        }
    }

    fun updateSearch(query: String) {
        _uiState.update { it.copy(searchQuery = query) }
    }

    fun updateFilter(filter: String) {
        _uiState.update { it.copy(categoryFilter = filter) }
    }
}

// --- Activity Points ViewModel ---

data class ActivityPointsState(
    val applications: List<ActivityPointApplication> = emptyList(),
    val isLoading: Boolean = true,
    val successMsg: String? = null,
    val errorMsg: String? = null
)

class ActivityPointsViewModel(private val repository: StudentRepository) : ViewModel() {
    private val _uiState = MutableStateFlow(ActivityPointsState())
    val uiState: StateFlow<ActivityPointsState> = _uiState.asStateFlow()

    init {
        fetchApplications()
    }

    fun fetchApplications() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            try {
                val dtos = repository.getActivityPoints()
                val applications = dtos.map { dto ->
                    ActivityPointApplication(
                        id = dto.id,
                        title = dto.title,
                        category = dto.category,
                        date = dto.date,
                        claimedPoints = dto.claimedPoints.toInt(),
                        approvedPoints = dto.approvedPoints?.toInt(),
                        status = dto.status,
                        description = dto.description,
                        supportingDocument = dto.supportingDocument ?: "",
                        reviewedBy = dto.reviewedBy,
                        reviewedAt = dto.reviewedAt,
                        facultyRemarks = dto.facultyRemarks
                    )
                }
                _uiState.update { it.copy(applications = applications, isLoading = false) }
            } catch (e: Exception) {
                _uiState.update { it.copy(isLoading = false) }
            }
        }
    }

    fun submitApplication(app: ActivityPointApplication, filePart: okhttp3.MultipartBody.Part? = null) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            try {
                val uploadedUrl = if (filePart != null) repository.uploadActivityPointDocument(filePart) else null
                val dto = repository.claimActivityPoints(
                    title = app.title,
                    category = app.category,
                    description = app.description,
                    date = app.date,
                    points = app.claimedPoints.toDouble(),
                    supportingDocument = uploadedUrl ?: app.supportingDocument.ifBlank { null }
                )
                if (dto != null) {
                    fetchApplications() // Refresh list from backend
                    _uiState.update { it.copy(
                        isLoading = false,
                        successMsg = "Application submitted successfully!"
                    ) }
                } else {
                    _uiState.update { it.copy(isLoading = false, errorMsg = "Failed to submit application") }
                }
            } catch (e: Exception) {
                _uiState.update { it.copy(isLoading = false, errorMsg = e.message) }
            }
        }
    }

    fun deleteApplication(id: String) {
        val previous = _uiState.value.applications
        _uiState.update { it.copy(applications = it.applications.filter { it.id != id }) }
        viewModelScope.launch {
            val success = repository.deleteActivityPoint(id)
            if (!success) {
                _uiState.update { it.copy(applications = previous, errorMsg = "Failed to delete application") }
            }
        }
    }

    fun clearMessages() {
        _uiState.update { it.copy(successMsg = null, errorMsg = null) }
    }
}

// --- Community Service ViewModel ---

data class CommunityServiceState(
    val opportunities: List<CommunityServiceOpportunity> = emptyList(),
    val logs: List<CommunityServiceLog> = emptyList(),
    val isLoading: Boolean = true,
    val errorMsg: String? = null,
    val successMsg: String? = null
) {
    val totalHours: Int get() = logs.filter { it.status == "Verified" }.sumOf { it.hours }
    val ngoCollabs: Int get() = logs.map { it.organization }.distinct().size
    val legalAidCount: Int get() = logs.count { it.category == "legal_aid" }
    val certificateCount: Int get() = logs.count { it.certificate }
}

class CommunityServiceViewModel(private val repository: StudentRepository) : ViewModel() {
    private val _uiState = MutableStateFlow(CommunityServiceState())
    val uiState: StateFlow<CommunityServiceState> = _uiState.asStateFlow()

    init {
        fetchData()
    }

    fun fetchData() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            try {
                val opportunityDtos = repository.getServiceOpportunities()
                val logDtos = repository.getServiceLogs()
                val opportunities = opportunityDtos.map { e ->
                    CommunityServiceOpportunity(e.id, e.title, e.organizer, e.date, e.location, e.spots, e.hours, e.tags)
                }
                val logs = logDtos.map { h ->
                    CommunityServiceLog(
                        id = h.id,
                        title = h.title,
                        organization = h.organization,
                        category = h.category,
                        date = h.date,
                        hours = h.hours.toInt(),
                        status = h.status,
                        certificate = h.certificateUrl != null,
                        certificateUrl = h.certificateUrl,
                        description = h.description,
                        proofDocument = h.proofDocument
                    )
                }
                _uiState.update { it.copy(opportunities = opportunities, logs = logs, isLoading = false) }
            } catch (e: Exception) {
                _uiState.update { it.copy(isLoading = false) }
            }
        }
    }

    fun applyToOpportunity(opportunityId: Int) {
        viewModelScope.launch {
            val result = repository.applyToServiceOpportunity(opportunityId)
            if (result != null) {
                fetchData()
                _uiState.update { it.copy(successMsg = "Applied successfully!") }
            } else {
                _uiState.update { it.copy(errorMsg = "Failed to apply. Please try again.") }
            }
        }
    }

    fun logHours(title: String, organization: String, category: String, date: String, hours: Double, description: String, filePart: okhttp3.MultipartBody.Part? = null) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            val uploadedUrl = if (filePart != null) repository.uploadServiceDocument(filePart) else null
            val result = repository.logServiceHours(title, organization, category, date, hours, description, uploadedUrl)
            if (result != null) {
                fetchData()
                _uiState.update { it.copy(isLoading = false, successMsg = "Service hours logged successfully!") }
            } else {
                _uiState.update { it.copy(isLoading = false, errorMsg = "Failed to log hours. Please try again.") }
            }
        }
    }

    fun deleteLog(id: String) {
        val previous = _uiState.value.logs
        _uiState.update { it.copy(logs = it.logs.filter { l -> l.id != id }) }
        viewModelScope.launch {
            val success = repository.deleteServiceLog(id)
            if (!success) {
                _uiState.update { it.copy(logs = previous, errorMsg = "Failed to delete log") }
            }
        }
    }

    fun clearMessages() {
        _uiState.update { it.copy(errorMsg = null, successMsg = null) }
    }
}

// --- Innovation Wall ViewModel ---

data class InnovationWallState(
    val projects: List<InnovationProject> = emptyList(),
    val isLoading: Boolean = true,
    val searchQuery: String = "",
    val errorMsg: String? = null,
    val successMsg: String? = null
)

class InnovationWallViewModel(private val repository: StudentRepository) : ViewModel() {
    private val _uiState = MutableStateFlow(InnovationWallState())
    val uiState: StateFlow<InnovationWallState> = _uiState.asStateFlow()

    init {
        fetchProjects()
    }

    private fun InnovationProjectDto.toModel(): InnovationProject = InnovationProject(
        id = this.id, title = this.title, description = this.description,
        category = this.category, mentor = this.mentor, team = this.team,
        likes = this.likes, likedByMe = this.likedByMe, comments = this.comments, badges = this.badges
    )

    fun fetchProjects() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            try {
                val dtos = repository.getInnovationProjects()
                val projects = dtos.map { it.toModel() }
                _uiState.update { it.copy(projects = projects, isLoading = false) }
            } catch (e: Exception) {
                _uiState.update { it.copy(isLoading = false) }
            }
        }
    }

    fun updateSearch(query: String) {
        _uiState.update { it.copy(searchQuery = query) }
    }

    fun submitProject(title: String, description: String, category: String, mentor: String, team: List<String>) {
        viewModelScope.launch {
            val dto = repository.createInnovationProject(title, description, category, mentor, team)
            if (dto != null) {
                _uiState.update { it.copy(projects = listOf(dto.toModel()) + it.projects, successMsg = "Project submitted successfully!") }
            } else {
                _uiState.update { it.copy(errorMsg = "Failed to submit project. Please try again.") }
            }
        }
    }

    fun toggleLike(id: String) {
        viewModelScope.launch {
            val dto = repository.toggleInnovationProjectLike(id)
            if (dto != null) {
                val updated = dto.toModel()
                _uiState.update { state ->
                    state.copy(projects = state.projects.map { if (it.id == updated.id) updated else it })
                }
            } else {
                _uiState.update { it.copy(errorMsg = "Failed to update like") }
            }
        }
    }

    fun addComment(id: String, text: String) {
        viewModelScope.launch {
            val dto = repository.addInnovationProjectComment(id, text)
            if (dto != null) {
                val updated = dto.toModel()
                _uiState.update { state ->
                    state.copy(
                        projects = state.projects.map { if (it.id == updated.id) updated else it },
                        successMsg = "Comment added!"
                    )
                }
            } else {
                _uiState.update { it.copy(errorMsg = "Failed to add comment") }
            }
        }
    }

    fun clearMessages() {
        _uiState.update { it.copy(errorMsg = null, successMsg = null) }
    }
}

// --- Project Showcase ViewModel ---

data class ProjectShowcaseState(
    val papers: List<ResearchPaper> = emptyList(),
    val isLoading: Boolean = true,
    val searchQuery: String = "",
    val errorMsg: String? = null,
    val successMsg: String? = null
)

class ProjectShowcaseViewModel(private val repository: StudentRepository) : ViewModel() {
    private val _uiState = MutableStateFlow(ProjectShowcaseState())
    val uiState: StateFlow<ProjectShowcaseState> = _uiState.asStateFlow()

    init {
        fetchPapers()
    }

    private fun ResearchPaperDto.toModel(): ResearchPaper = ResearchPaper(
        id = this.id.toString(), title = this.title, abstract = this.abstract,
        category = this.category, guide = this.guide, team = this.team,
        submissionDate = this.submissionDate, fileSize = this.fileSize,
        status = this.status, featured = this.featured, awards = this.awards, fileUrl = this.fileUrl
    )

    fun fetchPapers() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            try {
                val dtos = repository.getResearchPapers()
                _uiState.update { it.copy(papers = dtos.map { it.toModel() }, isLoading = false) }
            } catch (e: Exception) {
                _uiState.update { it.copy(isLoading = false) }
            }
        }
    }

    fun updateSearch(query: String) {
        _uiState.update { it.copy(searchQuery = query) }
    }

    fun submitPaper(title: String, abstract: String, category: String, guide: String, team: List<String>, filePart: okhttp3.MultipartBody.Part? = null) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            val uploaded = if (filePart != null) repository.uploadResearchPaper(filePart) else null
            val dto = repository.submitResearchPaper(title, abstract, category, guide, team, uploaded?.fileUrl, uploaded?.fileSize)
            if (dto != null) {
                _uiState.update { it.copy(papers = listOf(dto.toModel()) + it.papers, isLoading = false, successMsg = "Paper submitted successfully!") }
            } else {
                _uiState.update { it.copy(isLoading = false, errorMsg = "Failed to submit paper. Please try again.") }
            }
        }
    }

    fun clearMessages() {
        _uiState.update { it.copy(errorMsg = null, successMsg = null) }
    }
}

private fun legalEventStatusFromLabel(label: String): EventStatus =
    EventStatus.values().find { it.label.equals(label, ignoreCase = true) } ?: EventStatus.UPCOMING

private fun LegalEventDto.toLegalEvent(registeredIds: Set<String>): LegalEvent = LegalEvent(
    id = id,
    title = title,
    category = category,
    speaker = Speaker(speaker.name, speaker.designation, speaker.type, speaker.bio, speaker.initials),
    date = date,
    time = time,
    duration = duration.ifBlank { "90 min" },
    status = legalEventStatusFromLabel(status),
    mode = mode,
    platform = platform,
    meetingLink = meetingLink ?: "",
    totalSeats = totalSeats,
    availableSeats = availableSeats,
    registrationDeadline = registrationDeadline ?: date,
    description = description,
    agenda = agenda,
    activityPoints = activityPoints,
    certificateAvailable = certificateAvailable,
    isRegistered = id in registeredIds
)

class LegalEventsViewModel(private val repository: StudentRepository) : ViewModel() {
    private val _uiState = MutableStateFlow(LegalEventsState())
    val uiState: StateFlow<LegalEventsState> = _uiState.asStateFlow()

    // Real registration ids fetched from the backend, plus any registered this
    // session — merged into the paged flow so Register reflects immediately.
    private val _registeredIds = MutableStateFlow<Set<String>>(emptySet())

    val legalEventsPagingFlow: Flow<PagingData<LegalEvent>> = Pager(
        config = PagingConfig(pageSize = 20, enablePlaceholders = false),
        pagingSourceFactory = { GenericPagingSource({ skip, limit -> repository.getLegalEventsPaged(skip, limit) }) }
    ).flow
        .cachedIn(viewModelScope)
        .combine(_registeredIds) { pagingData, registeredIds ->
            pagingData.map { dto -> dto.toLegalEvent(registeredIds) }
        }

    init {
        fetchEventsData()
    }

    fun fetchEventsData() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            try {
                val dtos = repository.getLegalEvents()
                val registrations = repository.getMyLegalEventRegistrations()
                val registeredIds = registrations.map { it.eventId }.toSet()
                _registeredIds.update { it + registeredIds }

                val questionDtos = repository.getMyLegalEventQuestions()
                val questions = questionDtos.map { q ->
                    JudgeQuestion(q.id, q.eventId, q.eventTitle, q.question, q.topic, q.status, q.submittedAt)
                }

                val events = dtos.map { it.toLegalEvent(registeredIds) }
                _uiState.update { it.copy(
                    events = events,
                    filteredEvents = events,
                    questions = questions,
                    isLoading = false
                ) }
                applyFilters()
            } catch (e: Exception) {
                _uiState.update { it.copy(isLoading = false) }
            }
        }
    }

    fun updateTab(tab: String) {
        _uiState.update { it.copy(activeTab = tab) }
        applyFilters()
    }

    fun updateSearch(query: String) {
        _uiState.update { it.copy(search = query) }
        applyFilters()
    }

    private fun applyFilters() {
        val state = _uiState.value
        val result = state.events.filter { e ->
            val matchesSearch = state.search.isEmpty() || e.title.contains(state.search, true) || e.speaker.name.contains(state.search, true)
            val matchesTab = when(state.activeTab) {
                "Upcoming" -> e.status != EventStatus.COMPLETED && e.status != EventStatus.CANCELLED
                "Past" -> e.status == EventStatus.COMPLETED
                "Registered" -> e.isRegistered
                else -> true
            }
            matchesSearch && matchesTab
        }
        _uiState.update { it.copy(filteredEvents = result) }
    }

    fun registerForEvent(eventId: String) {
        // Optimistic local update so the button flips immediately.
        _registeredIds.update { it + eventId }
        _uiState.update { state ->
            state.copy(events = state.events.map { if (it.id == eventId) it.copy(isRegistered = true) else it })
        }
        applyFilters()
        viewModelScope.launch {
            val success = repository.registerForLegalEvent(eventId)
            if (!success) {
                _registeredIds.update { it - eventId }
                _uiState.update { state ->
                    state.copy(events = state.events.map { if (it.id == eventId) it.copy(isRegistered = false) else it })
                }
                applyFilters()
            }
        }
    }

    fun submitQuestion(eventId: String, question: String, topic: String) {
        val eventTitle = _uiState.value.events.find { it.id == eventId }?.title ?: ""
        viewModelScope.launch {
            val success = repository.submitLegalEventQuestion(eventId, eventTitle, question, topic)
            if (success) {
                val questionDtos = repository.getMyLegalEventQuestions()
                val questions = questionDtos.map { q ->
                    JudgeQuestion(q.id, q.eventId, q.eventTitle, q.question, q.topic, q.status, q.submittedAt)
                }
                _uiState.update { it.copy(questions = questions) }
            }
        }
    }
}
