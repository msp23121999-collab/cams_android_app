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

data class AdminFeesViewModelState(
    val isLoading: Boolean = false,
    val error: String? = null,
    val feeStructures: List<com.example.features.admin.models.AdminFeeStructure> = emptyList(),
    val scholarshipTypes: List<com.example.features.admin.models.AdminScholarshipType> = emptyList(),
    // Collect-fee flow
    val searchResults: List<com.example.features.admin.models.AdminFeeStudent> = emptyList(),
    val isSearching: Boolean = false,
    val selectedStudent: com.example.features.admin.models.AdminFeeStudent? = null,
    val studentFeeRecords: List<com.example.features.admin.models.AdminStudentFeeRecord> = emptyList(),
    val isLoadingRecords: Boolean = false,
    val isCollecting: Boolean = false,
    val collectError: String? = null,
    val collectSuccess: Boolean = false
)

class AdminFeesViewModel(private val repository: AdminRepository) : ViewModel() {
    private val _uiState = MutableStateFlow(AdminFeesViewModelState())
    val uiState: StateFlow<AdminFeesViewModelState> = _uiState.asStateFlow()


    init { fetchFeesData() }
    fun fetchFeesData() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null) }
            try {
                val structs = repository.getAdminFeeStructures()
                val scholarships = repository.getAdminScholarshipTypes()
                _uiState.update { it.copy(feeStructures = structs, scholarshipTypes = scholarships, isLoading = false) }
            } catch (e: Exception) {
                _uiState.update { it.copy(isLoading = false, error = e.message) }
            }
        }
    }

    fun searchStudents(query: String) {
        if (query.isBlank()) {
            _uiState.update { it.copy(searchResults = emptyList()) }
            return
        }
        viewModelScope.launch {
            _uiState.update { it.copy(isSearching = true) }
            try {
                val results = repository.searchStudentsForFees(query) ?: emptyList()
                _uiState.update { it.copy(searchResults = results, isSearching = false) }
            } catch (e: Exception) {
                _uiState.update { it.copy(isSearching = false, error = e.message) }
            }
        }
    }

    fun selectStudent(student: com.example.features.admin.models.AdminFeeStudent) {
        _uiState.update { it.copy(selectedStudent = student, searchResults = emptyList(), isLoadingRecords = true, studentFeeRecords = emptyList()) }
        viewModelScope.launch {
            try {
                val records = repository.getAdminStudentFees(student.studentId)
                _uiState.update { it.copy(studentFeeRecords = records, isLoadingRecords = false) }
            } catch (e: Exception) {
                _uiState.update { it.copy(isLoadingRecords = false, error = e.message) }
            }
        }
    }

    fun clearSelectedStudent() {
        _uiState.update { it.copy(selectedStudent = null, studentFeeRecords = emptyList()) }
    }

    fun collectFee(recordId: String, amount: Double, mode: String) {
        viewModelScope.launch {
            _uiState.update { it.copy(isCollecting = true, collectError = null, collectSuccess = false) }
            try {
                repository.adminCollectFee(recordId, amount, mode)
                // Refresh the selected student's records
                val student = _uiState.value.selectedStudent
                val records = if (student != null) repository.getAdminStudentFees(student.studentId) else emptyList()
                _uiState.update { it.copy(isCollecting = false, collectSuccess = true, studentFeeRecords = records) }
            } catch (e: Exception) {
                _uiState.update { it.copy(isCollecting = false, collectError = e.message ?: "Failed to collect fee") }
            }
        }
    }

    fun clearCollectStatus() {
        _uiState.update { it.copy(collectError = null, collectSuccess = false) }
    }
}

class AdminFeesViewModelFactory(private val repository: AdminRepository) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(AdminFeesViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return AdminFeesViewModel(repository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
