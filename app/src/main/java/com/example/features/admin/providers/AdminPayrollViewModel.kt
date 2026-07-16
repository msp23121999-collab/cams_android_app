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

data class AdminPayrollViewModelState(
    
    val isLoading: Boolean = false,
    val error: String? = null,
    val payrollRecords: List<com.example.features.admin.models.AdminPayroll> = emptyList()
)

class AdminPayrollViewModel(private val repository: AdminRepository) : ViewModel() {
    private val _uiState = MutableStateFlow(AdminPayrollViewModelState())
    val uiState: StateFlow<AdminPayrollViewModelState> = _uiState.asStateFlow()


    init { fetchPayroll() }
    fun fetchPayroll() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null) }
            try {
                val res = repository.getFacultyPayrollAdmin() 
                _uiState.update { it.copy(payrollRecords = res, isLoading = false) }
            } catch (e: Exception) {
                _uiState.update { it.copy(isLoading = false, error = e.message) }
            }
        }
    }

}

class AdminPayrollViewModelFactory(private val repository: AdminRepository) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(AdminPayrollViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return AdminPayrollViewModel(repository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
