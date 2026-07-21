package com.example.features.admin.providers

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.core.repository.AdminRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class AdminReportsViewModelState(
    val isSearching: Boolean = false,
    val error: String? = null,
    val students: List<com.example.features.admin.models.AdminFeeStudent> = emptyList(),
    val selectedStudent: com.example.features.admin.models.AdminFeeStudent? = null,
    val faculty: List<com.example.core.network.FacultyWorkloadInfoDto> = emptyList(),
    val selectedFacultyId: String? = null
)

class AdminReportsViewModel(private val repository: AdminRepository) : ViewModel() {
    private val _uiState = MutableStateFlow(AdminReportsViewModelState())
    val uiState: StateFlow<AdminReportsViewModelState> = _uiState.asStateFlow()

    init { loadFaculty() }

    private fun loadFaculty() {
        viewModelScope.launch {
            try {
                val fac = repository.getAllocationFaculty()
                _uiState.update { it.copy(faculty = fac) }
            } catch (e: Exception) {
                // Faculty list is optional here — student reports still work without it.
            }
        }
    }

    fun searchStudents(query: String) {
        if (query.isBlank()) {
            _uiState.update { it.copy(students = emptyList()) }
            return
        }
        viewModelScope.launch {
            _uiState.update { it.copy(isSearching = true, error = null) }
            try {
                val results = repository.searchStudentsForFees(query) ?: emptyList()
                _uiState.update { it.copy(students = results, isSearching = false) }
            } catch (e: Exception) {
                _uiState.update { it.copy(isSearching = false, error = e.message ?: "Search failed") }
            }
        }
    }

    fun selectStudent(student: com.example.features.admin.models.AdminFeeStudent?) {
        _uiState.update { it.copy(selectedStudent = student, students = emptyList()) }
    }

    fun selectFaculty(facultyId: String?) {
        _uiState.update { it.copy(selectedFacultyId = facultyId) }
    }
}

class AdminReportsViewModelFactory(private val repository: AdminRepository) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(AdminReportsViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return AdminReportsViewModel(repository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
