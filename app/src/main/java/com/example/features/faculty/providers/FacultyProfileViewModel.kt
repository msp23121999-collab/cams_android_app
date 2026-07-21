package com.example.features.faculty.providers

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.core.repository.FacultyRepository
import com.example.features.faculty.models.ActivitySummary
import com.example.features.faculty.models.FacultyProfile
import com.example.features.faculty.models.FacultySubject
import com.example.features.faculty.models.ResearchEntry
import kotlinx.coroutines.async
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

data class FacultyProfileState(
    val profile: FacultyProfile = FacultyProfile(),
    val researchList: List<ResearchEntry> = emptyList(),
    val activitySummary: ActivitySummary = ActivitySummary(),
    val subjects: List<FacultySubject> = emptyList(),
    val isLoading: Boolean = true,
    val isSaving: Boolean = false,
    val error: String? = null,
    val saveError: String? = null,
    val saveSuccess: Boolean = false
)

class FacultyProfileViewModel(private val repository: FacultyRepository) : ViewModel() {
    private val _uiState = MutableStateFlow(FacultyProfileState())
    val uiState: StateFlow<FacultyProfileState> = _uiState.asStateFlow()

    init {
        loadProfileData()
    }

    fun loadProfileData() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null) }
            try {
                var softError: String? = null
                lateinit var profile: FacultyProfile
                lateinit var research: List<ResearchEntry>
                lateinit var activitySummary: ActivitySummary
                lateinit var subjects: List<FacultySubject>

                coroutineScope {
                    val profileDeferred = async { repository.getProfile() }
                    val researchDeferred = async {
                        try { repository.getResearchEntries() } catch (e: Exception) { softError = e.message; emptyList() }
                    }
                    val activityDeferred = async {
                        try { repository.getActivitySummary() } catch (e: Exception) { softError = e.message; ActivitySummary() }
                    }
                    val subjectsDeferred = async {
                        try { repository.getAssignedSubjects() } catch (e: Exception) { softError = e.message; emptyList() }
                    }

                    profile = profileDeferred.await()
                    research = researchDeferred.await()
                    activitySummary = activityDeferred.await()
                    subjects = subjectsDeferred.await()
                }

                _uiState.update { it.copy(
                    profile = profile,
                    researchList = research,
                    activitySummary = activitySummary,
                    subjects = subjects,
                    isLoading = false,
                    error = softError
                ) }
            } catch (e: Exception) {
                _uiState.update { it.copy(isLoading = false, error = e.message) }
            }
        }
    }

    fun updateProfile(update: com.example.core.network.FacultyProfileUpdateRequest) {
        viewModelScope.launch {
            _uiState.update { it.copy(isSaving = true, saveError = null, saveSuccess = false) }
            try {
                val updated = repository.updateProfile(update)
                _uiState.update { it.copy(profile = updated, isSaving = false, saveSuccess = true) }
            } catch (e: Exception) {
                _uiState.update { it.copy(isSaving = false, saveError = e.message ?: "Failed to update profile") }
            }
        }
    }

    fun clearSaveStatus() {
        _uiState.update { it.copy(saveError = null, saveSuccess = false) }
    }
}

class FacultyProfileViewModelFactory(private val repository: FacultyRepository) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(FacultyProfileViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return FacultyProfileViewModel(repository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
