package com.example.features.faculty.providers

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.core.network.*
import com.example.core.repository.FacultyRepository
import kotlinx.coroutines.async
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

data class FacultySmartClassroomState(
    val sections: List<FacultyAttendanceSectionDto> = emptyList(),
    val activities: List<ClassroomActivityDto> = emptyList(),
    val interactions: List<StudentInteractionDto> = emptyList(),
    val summaries: List<SessionSummaryDto> = emptyList(),
    val isLoading: Boolean = true,
    val error: String? = null,
    val isSaving: Boolean = false,
    val saveError: String? = null,
    val saveSuccess: Boolean = false
)

class FacultySmartClassroomViewModel(private val repository: FacultyRepository) : ViewModel() {
    private val _uiState = MutableStateFlow(FacultySmartClassroomState())
    val uiState: StateFlow<FacultySmartClassroomState> = _uiState.asStateFlow()

    init { loadAll() }

    fun loadAll() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null) }
            try {
                var softError: String? = null
                lateinit var sections: List<FacultyAttendanceSectionDto>
                lateinit var activities: List<ClassroomActivityDto>
                lateinit var interactions: List<StudentInteractionDto>
                lateinit var summaries: List<SessionSummaryDto>

                coroutineScope {
                    val sectionsDeferred = async { try { repository.getAttendanceSections() } catch (e: Exception) { softError = e.message; emptyList() } }
                    val activitiesDeferred = async { try { repository.getClassroomActivities() } catch (e: Exception) { softError = e.message; emptyList() } }
                    val interactionsDeferred = async { try { repository.getClassroomInteractions() } catch (e: Exception) { softError = e.message; emptyList() } }
                    val summariesDeferred = async { try { repository.getSessionSummaries() } catch (e: Exception) { softError = e.message; emptyList() } }

                    sections = sectionsDeferred.await()
                    activities = activitiesDeferred.await()
                    interactions = interactionsDeferred.await()
                    summaries = summariesDeferred.await()
                }

                _uiState.update {
                    it.copy(
                        sections = sections,
                        activities = activities,
                        interactions = interactions,
                        summaries = summaries,
                        isLoading = false,
                        error = softError
                    )
                }
            } catch (e: Exception) {
                _uiState.update { it.copy(isLoading = false, error = e.message) }
            }
        }
    }

    fun logActivity(request: CreateClassroomActivityRequest, onDone: (Boolean) -> Unit = {}) {
        viewModelScope.launch {
            _uiState.update { it.copy(isSaving = true, saveError = null, saveSuccess = false) }
            try {
                repository.createClassroomActivity(request)
                loadAll()
                _uiState.update { it.copy(isSaving = false, saveSuccess = true) }
                onDone(true)
            } catch (e: Exception) {
                _uiState.update { it.copy(isSaving = false, saveError = e.message ?: "Failed to log activity") }
                onDone(false)
            }
        }
    }

    fun createPoll(request: CreateInteractionRequest, onDone: (Boolean) -> Unit = {}) {
        viewModelScope.launch {
            _uiState.update { it.copy(isSaving = true, saveError = null, saveSuccess = false) }
            try {
                repository.createClassroomInteraction(request)
                loadAll()
                _uiState.update { it.copy(isSaving = false, saveSuccess = true) }
                onDone(true)
            } catch (e: Exception) {
                _uiState.update { it.copy(isSaving = false, saveError = e.message ?: "Failed to create poll") }
                onDone(false)
            }
        }
    }

    fun saveSessionSummary(request: CreateSessionSummaryRequest, onDone: (Boolean) -> Unit = {}) {
        viewModelScope.launch {
            _uiState.update { it.copy(isSaving = true, saveError = null, saveSuccess = false) }
            try {
                repository.createSessionSummary(request)
                loadAll()
                _uiState.update { it.copy(isSaving = false, saveSuccess = true) }
                onDone(true)
            } catch (e: Exception) {
                _uiState.update { it.copy(isSaving = false, saveError = e.message ?: "Failed to save session summary") }
                onDone(false)
            }
        }
    }

    fun clearSaveStatus() {
        _uiState.update { it.copy(saveError = null, saveSuccess = false) }
    }
}

class FacultySmartClassroomViewModelFactory(private val repository: FacultyRepository) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(FacultySmartClassroomViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return FacultySmartClassroomViewModel(repository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
