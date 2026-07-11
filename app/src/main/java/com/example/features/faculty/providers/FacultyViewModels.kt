package com.example.features.faculty.providers

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.core.repository.FacultyRepository
import com.example.core.network.*
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

// --- Faculty Assignments ViewModel ---
data class FacultyAssignmentsState(
    val assignments: List<FacultyAssignmentDto> = emptyList(),
    val isLoading: Boolean = false,
    val error: String? = null
)

class FacultyAssignmentsViewModel(private val repository: FacultyRepository) : ViewModel() {
    private val _uiState = MutableStateFlow(FacultyAssignmentsState())
    val uiState: StateFlow<FacultyAssignmentsState> = _uiState.asStateFlow()

    init { loadAssignments() }

    fun loadAssignments() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null) }
            try {
                val data = repository.getAssignments()
                _uiState.update { it.copy(assignments = data, isLoading = false) }
            } catch (e: Exception) {
                _uiState.update { it.copy(isLoading = false, error = e.message) }
            }
        }
    }
}

class FacultyAssignmentsViewModelFactory(private val repository: FacultyRepository) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        return FacultyAssignmentsViewModel(repository) as T
    }
}

// --- Faculty Students ViewModel ---
data class FacultyStudentsState(
    val students: List<FacultyStudentDto> = emptyList(),
    val isLoading: Boolean = false,
    val error: String? = null
)

class FacultyStudentsViewModel(private val repository: FacultyRepository) : ViewModel() {
    private val _uiState = MutableStateFlow(FacultyStudentsState())
    val uiState: StateFlow<FacultyStudentsState> = _uiState.asStateFlow()

    init { loadStudents() }

    fun loadStudents() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null) }
            try {
                val data = repository.getStudents()
                _uiState.update { it.copy(students = data, isLoading = false) }
            } catch (e: Exception) {
                _uiState.update { it.copy(isLoading = false, error = e.message) }
            }
        }
    }
}

class FacultyStudentsViewModelFactory(private val repository: FacultyRepository) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        return FacultyStudentsViewModel(repository) as T
    }
}

// --- Faculty Study Materials ViewModel ---
data class FacultyMaterialsState(
    val materials: List<FacultyMaterialDto> = emptyList(),
    val isLoading: Boolean = false,
    val error: String? = null
)

class FacultyMaterialsViewModel(private val repository: FacultyRepository) : ViewModel() {
    private val _uiState = MutableStateFlow(FacultyMaterialsState())
    val uiState: StateFlow<FacultyMaterialsState> = _uiState.asStateFlow()

    init { loadMaterials() }

    fun loadMaterials() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null) }
            try {
                val data = repository.getStudyMaterials()
                _uiState.update { it.copy(materials = data, isLoading = false) }
            } catch (e: Exception) {
                _uiState.update { it.copy(isLoading = false, error = e.message) }
            }
        }
    }
}

class FacultyMaterialsViewModelFactory(private val repository: FacultyRepository) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        return FacultyMaterialsViewModel(repository) as T
    }
}

// --- Faculty Lecture Recordings ViewModel ---
data class FacultyRecordingsState(
    val recordings: List<FacultyRecordingDto> = emptyList(),
    val isLoading: Boolean = false,
    val error: String? = null
)

class FacultyRecordingsViewModel(private val repository: FacultyRepository) : ViewModel() {
    private val _uiState = MutableStateFlow(FacultyRecordingsState())
    val uiState: StateFlow<FacultyRecordingsState> = _uiState.asStateFlow()

    init { loadRecordings() }

    fun loadRecordings() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null) }
            try {
                val data = repository.getLectureRecordings()
                _uiState.update { it.copy(recordings = data, isLoading = false) }
            } catch (e: Exception) {
                _uiState.update { it.copy(isLoading = false, error = e.message) }
            }
        }
    }
}

class FacultyRecordingsViewModelFactory(private val repository: FacultyRepository) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        return FacultyRecordingsViewModel(repository) as T
    }
}
