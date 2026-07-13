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
import androidx.paging.map

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
    val error: String? = null
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
                    description = dto.description,
                    category = dto.category,
                    membersCount = dto.membersCount,
                    role = dto.userRole,
                    icon = when(dto.category) {
                        "Academic" -> Icons.Filled.Mic
                        "Tech" -> Icons.Filled.Code
                        else -> Icons.Filled.TheaterComedy
                    }
                )
            }
        }
        .cachedIn(viewModelScope)

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
                        description = dto.description,
                        category = dto.category,
                        membersCount = dto.membersCount,
                        role = dto.userRole,
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
    }

    fun joinClub(clubId: Int) {
        viewModelScope.launch {
            try {
                repository.joinClub(clubId)
                fetchClubs() // Refresh
            } catch (e: Exception) {
                _uiState.update { it.copy(error = "Failed to join club: ${e.message}") }
            }
        }
    }

    fun leaveClub(clubId: Int) {
        // Implementation for leave if available in API
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
                    priority = "Medium",
                    publishDate = dto.date ?: "",
                    publisherName = "Admin",
                    publisherRole = "Admin",
                    audienceType = "All Students"
                )
            }
        }
        .cachedIn(viewModelScope)

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
                        priority = "Medium",
                        publishDate = dto.date ?: "",
                        publisherName = "Admin",
                        publisherRole = "Admin",
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
    val isLoading: Boolean = true
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
    val error: String? = null
)

class GrievancesViewModel(private val repository: StudentRepository) : ViewModel() {
    private val _uiState = MutableStateFlow(GrievancesState())
    val uiState: StateFlow<GrievancesState> = _uiState.asStateFlow()

    val grievancesPagingFlow: Flow<PagingData<Grievance>> = Pager(
        config = PagingConfig(pageSize = 20, enablePlaceholders = false),
        pagingSourceFactory = { GenericPagingSource({ skip, limit -> repository.getGrievancesPaged(skip, limit) }) }
    ).flow
        .map { pagingData ->
            pagingData.map { dto ->
                Grievance(
                    id = dto.id,
                    date = dto.date,
                    studentName = "Rahul Verma", // Current user
                    regNo = "2022LC014",
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
                val dtos = repository.getGrievances()
                val grievances = dtos.map { dto ->
                    Grievance(
                        id = dto.id,
                        date = dto.date,
                        studentName = "Rahul Verma", // Current user
                        regNo = "2022LC014",
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
            try {
                repository.submitGrievance(category, priority, subject, description)
                fetchGrievances() // Refresh
            } catch (e: Exception) {
                // Handle error
            }
        }
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
                        platform = when(dto.platform) {
                            "Zoom" -> MeetingPlatform.ZOOM
                            "Google Meet" -> MeetingPlatform.GOOGLE_MEET
                            else -> MeetingPlatform.TEAMS
                        },
                        meetingLink = dto.meetingLink,
                        status = when(dto.status) {
                            "LIVE" -> MeetingStatus.LIVE_NOW
                            "UPCOMING" -> MeetingStatus.UPCOMING
                            else -> MeetingStatus.COMPLETED
                        },
                        participants = dto.participants,
                        attended = false,
                        agenda = emptyList(),
                        description = ""
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

data class LexNovaState(
    val kpis: List<LexNovaKPI> = emptyList(),
    val timetable: List<TimetableEntry> = emptyList(),
    val documents: List<KnowledgeDocument> = emptyList(),
    val alumni: List<AlumniMentor> = emptyList(),
    val isLoading: Boolean = true,
    val activeTab: String = "Command Center"
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
                // Fetch from repository
                _uiState.update { it.copy(isLoading = false) }
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
}

// --- LexSphere ViewModel ---

data class LexSphereState(
    val drives: List<InternshipDrive> = emptyList(),
    val alumni: List<AlumniMentorDto> = emptyList(),
    val applications: List<InternshipApplication> = emptyList(),
    val isLoading: Boolean = true,
    val searchQuery: String = ""
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
                    InternshipDrive(dto.id, dto.company, dto.role, dto.stipend, dto.status, dto.deadline)
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
        _uiState.update { it.copy(applications = it.applications + app) }
    }
}

// --- Internships ViewModel ---

data class InternshipsState(
    val internships: List<InternshipRecord> = emptyList(),
    val isLoading: Boolean = true,
    val searchQuery: String = "",
    val typeFilter: String = "All"
)

class InternshipsViewModel(private val repository: StudentRepository) : ViewModel() {
    private val _uiState = MutableStateFlow(InternshipsState())
    val uiState: StateFlow<InternshipsState> = _uiState.asStateFlow()

    val internshipsPagingFlow: Flow<PagingData<InternshipRecord>> = Pager(
        config = PagingConfig(pageSize = 20, enablePlaceholders = false),
        pagingSourceFactory = { GenericPagingSource({ skip, limit -> repository.getInternshipsListPaged(skip, limit) }) }
    ).flow
        .map { pagingData ->
            pagingData.map { dto ->
                InternshipRecord(
                    dto.id, dto.organization, dto.sector, dto.role,
                    dto.startDate, dto.endDate, dto.mentor, dto.description, dto.status
                )
            }
        }
        .cachedIn(viewModelScope)

    init {
        fetchInternships()
    }

    fun fetchInternships() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            try {
                val dtos = repository.getInternshipsList()
                val internships = dtos.map { dto ->
                    InternshipRecord(
                        dto.id, dto.organization, dto.sector, dto.role,
                        dto.startDate, dto.endDate, dto.mentor, dto.description, dto.status
                    )
                }
                _uiState.update { it.copy(internships = internships, isLoading = false) }
            } catch (e: Exception) {
                _uiState.update { it.copy(isLoading = false) }
            }
        }
    }

    fun addInternship(internship: InternshipRecord) {
        _uiState.update { it.copy(internships = listOf(internship) + it.internships) }
    }

    fun deleteInternship(id: String) {
        _uiState.update { it.copy(internships = it.internships.filter { it.id != id }) }
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
    val categoryFilter: String = "All"
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
                        dto.id, dto.title, dto.issuer, dto.date, dto.category, dto.isVerified, dto.type
                    )
                }
                _uiState.update { it.copy(certifications = certifications, isLoading = false) }
            } catch (e: Exception) {
                _uiState.update { it.copy(isLoading = false) }
            }
        }
    }

    fun addCertification(cert: CertificationRecord) {
        _uiState.update { it.copy(certifications = listOf(cert) + it.certifications) }
    }

    fun deleteCertification(id: String) {
        _uiState.update { it.copy(certifications = it.certifications.filter { it.id != id }) }
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
                        claimedPoints = dto.pointsClaimed,
                        approvedPoints = dto.pointsAwarded,
                        status = dto.status,
                        description = dto.description,
                        supportingDocument = dto.attachmentUrl ?: "",
                        reviewedBy = dto.approvedBy,
                        reviewedAt = dto.approvedDate,
                        facultyRemarks = dto.remarks
                    )
                }
                _uiState.update { it.copy(applications = applications, isLoading = false) }
            } catch (e: Exception) {
                _uiState.update { it.copy(isLoading = false) }
            }
        }
    }

    fun submitApplication(app: ActivityPointApplication) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            try {
                val success = repository.claimActivityPoints(
                    title = app.title,
                    category = app.category,
                    description = app.description,
                    points = 0 // Initially claimed 0 or calculate from app logic
                )
                if (success) {
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
        _uiState.update { it.copy(applications = it.applications.filter { it.id != id }) }
    }

    fun clearMessages() {
        _uiState.update { it.copy(successMsg = null, errorMsg = null) }
    }
}

// --- Community Service ViewModel ---

data class CommunityServiceState(
    val opportunities: List<CommunityServiceOpportunity> = emptyList(),
    val logs: List<CommunityServiceLog> = emptyList(),
    val isLoading: Boolean = true
)

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
                val dto = repository.getCommunityServiceData()
                if (dto != null) {
                    val opportunities = dto.opportunities.map { e ->
                        CommunityServiceOpportunity(e.id, e.title, e.organizer, e.date, e.location, e.slots, e.duration, e.tags)
                    }
                    val logs = dto.logs.map { h ->
                        CommunityServiceLog(h.id, h.title, h.date, h.hours, h.status, h.isVerified)
                    }
                    _uiState.update { it.copy(opportunities = opportunities, logs = logs, isLoading = false) }
                } else {
                    _uiState.update { it.copy(isLoading = false) }
                }
            } catch (e: Exception) {
                _uiState.update { it.copy(isLoading = false) }
            }
        }
    }
}

// --- Innovation Wall ViewModel ---

data class InnovationWallState(
    val projects: List<InnovationProject> = emptyList(),
    val isLoading: Boolean = true
)

class InnovationWallViewModel(private val repository: StudentRepository) : ViewModel() {
    private val _uiState = MutableStateFlow(InnovationWallState())
    val uiState: StateFlow<InnovationWallState> = _uiState.asStateFlow()

    init {
        fetchProjects()
    }

    fun fetchProjects() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            try {
                val dtos = repository.getInnovationProjects()
                val projects = dtos.map { dto ->
                    InnovationProject(
                        dto.id.hashCode(), dto.title, dto.abstract, dto.tags.firstOrNull() ?: "General",
                        dto.authors.firstOrNull() ?: "Mentor", dto.authors, dto.likes, dto.comments, dto.tags
                    )
                }
                _uiState.update { it.copy(projects = projects, isLoading = false) }
            } catch (e: Exception) {
                _uiState.update { it.copy(isLoading = false) }
            }
        }
    }
}

// --- Project Showcase ViewModel ---

data class ProjectShowcaseState(
    val papers: List<ResearchPaper> = emptyList(),
    val isLoading: Boolean = true
)

class ProjectShowcaseViewModel(private val repository: StudentRepository) : ViewModel() {
    private val _uiState = MutableStateFlow(ProjectShowcaseState())
    val uiState: StateFlow<ProjectShowcaseState> = _uiState.asStateFlow()

    init {
        fetchPapers()
    }

    fun fetchPapers() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            try {
                // Fetch from repository if papers endpoint existed, 
                // for now mock or reuse innovation projects
                _uiState.update { it.copy(isLoading = false) }
            } catch (e: Exception) {
                _uiState.update { it.copy(isLoading = false) }
            }
        }
    }
}

class LegalEventsViewModel(private val repository: StudentRepository) : ViewModel() {
    private val _uiState = MutableStateFlow(LegalEventsState())
    val uiState: StateFlow<LegalEventsState> = _uiState.asStateFlow()

    val legalEventsPagingFlow: Flow<PagingData<LegalEvent>> = Pager(
        config = PagingConfig(pageSize = 20, enablePlaceholders = false),
        pagingSourceFactory = { GenericPagingSource({ skip, limit -> repository.getLegalEventsPaged(skip, limit) }) }
    ).flow
        .map { pagingData ->
            pagingData.map { dto ->
                LegalEvent(
                    id = dto.id,
                    title = dto.title,
                    category = dto.category,
                    speaker = Speaker(dto.speakerName, "Expert", "Legal Domain", "Expert bio", "EX"),
                    date = dto.date,
                    time = dto.time,
                    duration = "90 min",
                    status = when(dto.status) {
                        "REG_OPEN" -> EventStatus.REG_OPEN
                        "COMPLETED" -> EventStatus.COMPLETED
                        else -> EventStatus.UPCOMING
                    },
                    mode = "Online",
                    platform = "Zoom",
                    meetingLink = "",
                    totalSeats = 100,
                    availableSeats = 50,
                    registrationDeadline = dto.date,
                    description = "",
                    agenda = emptyList(),
                    activityPoints = dto.activityPoints
                )
            }
        }
        .cachedIn(viewModelScope)

    init {
        fetchEventsData()
    }

    fun fetchEventsData() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            try {
                val dtos = repository.getLegalEvents()
                val events = dtos.map { dto ->
                    LegalEvent(
                        id = dto.id,
                        title = dto.title,
                        category = dto.category,
                        speaker = Speaker(dto.speakerName, "Expert", "Legal Domain", "Expert bio", "EX"),
                        date = dto.date,
                        time = dto.time,
                        duration = "90 min",
                        status = when(dto.status) {
                            "REG_OPEN" -> EventStatus.REG_OPEN
                            "COMPLETED" -> EventStatus.COMPLETED
                            else -> EventStatus.UPCOMING
                        },
                        mode = "Online",
                        platform = "Zoom",
                        meetingLink = "",
                        totalSeats = 100,
                        availableSeats = 50,
                        registrationDeadline = dto.date,
                        description = "",
                        agenda = emptyList(),
                        activityPoints = dto.activityPoints
                    )
                }
                _uiState.update { it.copy(
                    events = events,
                    filteredEvents = events,
                    isLoading = false
                ) }
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
        _uiState.update { state ->
            val updated = state.events.map { 
                if (it.id == eventId) it.copy(isRegistered = true) else it 
            }
            state.copy(events = updated)
        }
        applyFilters()
    }

    fun submitQuestion(eventId: String, question: String, topic: String) {
        val newQ = JudgeQuestion(
            id = "Q-${(_uiState.value.questions.size + 1).toString().padStart(3, '0')}",
            eventId = eventId,
            eventTitle = _uiState.value.events.find { it.id == eventId }?.title ?: "",
            question = question,
            topic = topic,
            status = "Pending",
            submittedAt = "Today"
        )
        _uiState.update { it.copy(questions = it.questions + newQ) }
    }
}
