package com.example.features.admin.providers

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.core.network.AcademicSetupDto
import com.example.core.network.FacultyWorkloadInfoDto
import com.example.core.network.SubjectAllocationDto
import com.example.core.network.SubjectInfoDto
import com.example.core.repository.AdminRepository
import kotlinx.coroutines.async
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class AdminTimetableViewModelState(
    val isLoading: Boolean = true,
    val error: String? = null,
    val setup: AcademicSetupDto? = null,
    val facultyAssignments: List<SubjectAllocationDto> = emptyList(),
    val subjects: List<SubjectInfoDto> = emptyList(),
    val faculty: List<FacultyWorkloadInfoDto> = emptyList(),
    val isSaving: Boolean = false,
    val saveError: String? = null,
    val saveSuccess: Boolean = false
)

class AdminTimetableViewModel(private val repository: AdminRepository) : ViewModel() {
    private val _uiState = MutableStateFlow(AdminTimetableViewModelState())
    val uiState: StateFlow<AdminTimetableViewModelState> = _uiState.asStateFlow()

    init { fetchAssignments() }

    fun fetchAssignments() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null) }
            try {
                coroutineScope {
                    val setupD = async { try { repository.getAcademicSetup() } catch (e: Exception) { null } }
                    val allocD = async { try { repository.getSubjectAllocations() } catch (e: Exception) { emptyList() } }
                    val subjD = async { try { repository.getAllocationSubjects() } catch (e: Exception) { emptyList() } }
                    val facD = async { try { repository.getAllocationFaculty() } catch (e: Exception) { emptyList() } }
                    _uiState.update {
                        it.copy(
                            setup = setupD.await(),
                            facultyAssignments = allocD.await(),
                            subjects = subjD.await(),
                            faculty = facD.await(),
                            isLoading = false
                        )
                    }
                }
            } catch (e: Exception) {
                _uiState.update { it.copy(isLoading = false, error = e.message ?: "Failed to load assignments") }
            }
        }
    }

    fun allocate(subjectId: String, sectionId: String, facultyId: String) {
        viewModelScope.launch {
            _uiState.update { it.copy(isSaving = true, saveError = null, saveSuccess = false) }
            try {
                repository.allocateSubject(
                    com.example.core.network.SubjectAllocationCreateDto(
                        courseId = subjectId,
                        sectionId = sectionId,
                        facultyId = facultyId
                    )
                )
                fetchAssignments()
                _uiState.update { it.copy(isSaving = false, saveSuccess = true) }
            } catch (e: Exception) {
                _uiState.update { it.copy(isSaving = false, saveError = e.message ?: "Failed to allocate subject") }
            }
        }
    }

    fun clearSaveStatus() {
        _uiState.update { it.copy(saveError = null, saveSuccess = false) }
    }
}

class AdminTimetableViewModelFactory(private val repository: AdminRepository) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(AdminTimetableViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return AdminTimetableViewModel(repository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
