package com.example.features.hod.providers

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.core.network.AcademicSetupDto
import com.example.core.network.FacultyWorkloadInfoDto
import com.example.core.network.HODFacultyResponseDto
import com.example.core.network.SubjectAllocationCreateDto
import com.example.core.network.SubjectAllocationDto
import com.example.core.network.SubjectInfoDto
import com.example.core.network.HODSubstitutionDto
import com.example.core.repository.HODRepository
import kotlinx.coroutines.async
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class HODSubjectAllocationUiState(
    val isLoading: Boolean = false,
    val setup: AcademicSetupDto? = null,
    val allocations: List<SubjectAllocationDto> = emptyList(),
    val subjects: List<SubjectInfoDto> = emptyList(),
    val faculty: List<FacultyWorkloadInfoDto> = emptyList(),
    val error: String? = null,
    val isSaving: Boolean = false,
    val saveError: String? = null,
    val saveSuccess: Boolean = false,
    val courseSections: List<com.example.core.network.AcademicSetupSectionDto> = emptyList(),
    val isLoadingSections: Boolean = false
)

class HODSubjectAllocationViewModel(
    private val repository: HODRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(HODSubjectAllocationUiState())
    val uiState: StateFlow<HODSubjectAllocationUiState> = _uiState.asStateFlow()

    init {
        loadData()
    }

    fun loadData() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null) }

            var setupResult: Result<AcademicSetupDto>? = null
            var allocResult: Result<List<SubjectAllocationDto>>? = null
            var subjectsResult: Result<List<SubjectInfoDto>>? = null
            var facultyResult: Result<List<FacultyWorkloadInfoDto>>? = null

            coroutineScope {
                val setupDeferred = async { repository.getAcademicSetup() }
                val allocDeferred = async { repository.getSubjectAllocations() }
                val subjectsDeferred = async { repository.getAllocationSubjects() }
                val facultyDeferred = async { repository.getAllocationFaculty() }
                setupResult = setupDeferred.await()
                allocResult = allocDeferred.await()
                subjectsResult = subjectsDeferred.await()
                facultyResult = facultyDeferred.await()
            }

            _uiState.update {
                it.copy(
                    isLoading = false,
                    setup = setupResult?.getOrNull(),
                    allocations = allocResult?.getOrNull() ?: emptyList(),
                    subjects = subjectsResult?.getOrNull() ?: emptyList(),
                    faculty = facultyResult?.getOrNull() ?: emptyList(),
                    error = setupResult?.exceptionOrNull()?.message
                        ?: allocResult?.exceptionOrNull()?.message
                        ?: subjectsResult?.exceptionOrNull()?.message
                        ?: facultyResult?.exceptionOrNull()?.message
                )
            }
        }
    }

    fun allocate(courseId: String, sectionId: String, facultyId: String) {
        viewModelScope.launch {
            _uiState.update { it.copy(isSaving = true, saveError = null, saveSuccess = false) }
            val result = repository.allocateSubjects(listOf(SubjectAllocationCreateDto(courseId, sectionId, facultyId)))
            if (result.isSuccess) {
                loadData()
                _uiState.update { it.copy(isSaving = false, saveSuccess = true) }
            } else {
                _uiState.update { it.copy(isSaving = false, saveError = result.exceptionOrNull()?.message ?: "Failed to save allocation") }
            }
        }
    }

    fun clearSaveStatus() {
        _uiState.update { it.copy(saveError = null, saveSuccess = false) }
    }

    fun loadSectionsForCourse(courseId: String) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoadingSections = true, courseSections = emptyList()) }
            val result = repository.getCourseSections(courseId)
            _uiState.update { it.copy(isLoadingSections = false, courseSections = result.getOrNull() ?: emptyList()) }
        }
    }
}

class HODSubjectAllocationViewModelFactory(private val repository: HODRepository) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(HODSubjectAllocationViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return HODSubjectAllocationViewModel(repository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}


data class HODSubstitutionUiState(
    val isLoading: Boolean = false,
    val substitutions: List<HODSubstitutionDto> = emptyList(),
    val availableFaculty: List<HODFacultyResponseDto> = emptyList(),
    val error: String? = null,
    val isSaving: Boolean = false,
    val saveError: String? = null,
    val saveSuccess: Boolean = false
)

class HODSubstitutionViewModel(
    private val repository: HODRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(HODSubstitutionUiState())
    val uiState: StateFlow<HODSubstitutionUiState> = _uiState.asStateFlow()

    init {
        loadData()
    }

    fun loadData() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null) }
            var softError: String? = null
            val subsResult = repository.getSubstitutions()
            val faculty = try { repository.getAvailableSubstituteFaculty() } catch (e: Exception) { softError = e.message; emptyList() }
            _uiState.update {
                it.copy(
                    isLoading = false,
                    substitutions = subsResult.getOrNull() ?: emptyList(),
                    availableFaculty = faculty,
                    error = subsResult.exceptionOrNull()?.message ?: softError
                )
            }
        }
    }

    fun assign(
        absentFacultyId: String, absentFacultyName: String,
        substituteFacultyId: String, substituteFacultyName: String,
        subject: String, section: String, date: String, periodLabel: String
    ) {
        viewModelScope.launch {
            _uiState.update { it.copy(isSaving = true, saveError = null, saveSuccess = false) }
            try {
                repository.assignSubstitution(absentFacultyId, absentFacultyName, substituteFacultyId, substituteFacultyName, subject, section, date, periodLabel)
                loadData()
                _uiState.update { it.copy(isSaving = false, saveSuccess = true) }
            } catch (e: Exception) {
                _uiState.update { it.copy(isSaving = false, saveError = e.message ?: "Failed to assign substitution") }
            }
        }
    }

    fun clearSaveStatus() {
        _uiState.update { it.copy(saveError = null, saveSuccess = false) }
    }
}

class HODSubstitutionViewModelFactory(private val repository: HODRepository) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(HODSubstitutionViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return HODSubstitutionViewModel(repository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
