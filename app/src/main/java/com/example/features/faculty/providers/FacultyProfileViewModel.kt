package com.example.features.faculty.providers

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.core.repository.FacultyRepository
import com.example.features.faculty.models.FacultyProfile
import com.example.features.faculty.models.ResearchEntry
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

data class FacultyProfileState(
    val profile: FacultyProfile = FacultyProfile(),
    val researchList: List<ResearchEntry> = emptyList(),
    val isLoading: Boolean = true,
    val error: String? = null
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
                val profile = repository.getProfile()
                val research = repository.getResearchEntries()
                
                _uiState.update { it.copy(
                    profile = profile,
                    researchList = research,
                    isLoading = false
                ) }
            } catch (e: Exception) {
                _uiState.update { it.copy(isLoading = false, error = e.message) }
            }
        }
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
