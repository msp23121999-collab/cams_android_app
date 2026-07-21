package com.example.features.admin.providers

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.core.network.AdminAcademicYearDto
import com.example.core.repository.AdminRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class AdminAcademicYearState(
    val isLoading: Boolean = true,
    val error: String? = null,
    val years: List<AdminAcademicYearDto> = emptyList(),
    val degrees: List<com.example.features.admin.models.AdminDegree> = emptyList(),
    val isSaving: Boolean = false,
    val saveError: String? = null,
    val saveSuccess: Boolean = false
)

class AdminAcademicYearViewModel(private val repository: AdminRepository) : ViewModel() {
    private val _uiState = MutableStateFlow(AdminAcademicYearState())
    val uiState: StateFlow<AdminAcademicYearState> = _uiState.asStateFlow()

    init { load() }

    fun load() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null) }
            try {
                val years = repository.getAcademicYears()
                val degrees = try { repository.getDegrees() } catch (e: Exception) { emptyList() }
                _uiState.update { it.copy(years = years, degrees = degrees, isLoading = false) }
            } catch (e: Exception) {
                _uiState.update { it.copy(isLoading = false, error = e.message ?: "Failed to load academic years") }
            }
        }
    }

    fun createYear(name: String, degreeId: String, batch: String, startDate: String, endDate: String, currentSemester: Int) {
        viewModelScope.launch {
            _uiState.update { it.copy(isSaving = true, saveError = null, saveSuccess = false) }
            try {
                repository.initializeAcademicYear(
                    mapOf(
                        "name" to name,
                        "degree_id" to degreeId,
                        "batch" to batch,
                        "start_date" to startDate,
                        "end_date" to endDate,
                        "current_semester" to currentSemester
                    )
                )
                load()
                _uiState.update { it.copy(isSaving = false, saveSuccess = true) }
            } catch (e: Exception) {
                _uiState.update { it.copy(isSaving = false, saveError = e.message ?: "Failed to create academic year") }
            }
        }
    }

    fun setSemester(ayId: String, semester: Int) {
        viewModelScope.launch {
            _uiState.update { it.copy(isSaving = true, saveError = null) }
            try {
                // The per-year update endpoint also syncs each student's semester
                // for this degree; the bulk /set-semester route is batch-wide and
                // takes a different payload, so it is not what this action wants.
                repository.updateAcademicYear(ayId, mapOf("current_semester" to semester))
                load()
                _uiState.update { it.copy(isSaving = false, saveSuccess = true) }
            } catch (e: Exception) {
                _uiState.update { it.copy(isSaving = false, saveError = e.message ?: "Failed to change semester") }
            }
        }
    }

    fun setActive(year: AdminAcademicYearDto, isActive: Boolean) {
        viewModelScope.launch {
            _uiState.update { it.copy(isSaving = true, saveError = null) }
            try {
                repository.updateAcademicYear(year.id, mapOf("is_active" to isActive))
                load()
                _uiState.update { it.copy(isSaving = false) }
            } catch (e: Exception) {
                _uiState.update { it.copy(isSaving = false, saveError = e.message ?: "Failed to update academic year") }
            }
        }
    }

    fun deleteYear(ayId: String) {
        viewModelScope.launch {
            _uiState.update { it.copy(isSaving = true, saveError = null) }
            try {
                repository.deleteAcademicYear(ayId)
                load()
                _uiState.update { it.copy(isSaving = false) }
            } catch (e: Exception) {
                _uiState.update { it.copy(isSaving = false, saveError = e.message ?: "Failed to delete academic year") }
            }
        }
    }

    fun clearSaveStatus() {
        _uiState.update { it.copy(saveError = null, saveSuccess = false) }
    }
}

class AdminAcademicYearViewModelFactory(private val repository: AdminRepository) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(AdminAcademicYearViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return AdminAcademicYearViewModel(repository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
