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
    val scholarshipTypes: List<com.example.features.admin.models.AdminScholarshipType> = emptyList()
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
