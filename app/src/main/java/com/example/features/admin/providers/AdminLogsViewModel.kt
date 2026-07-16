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

data class AdminLogsViewModelState(
    
    val isLoading: Boolean = false,
    val error: String? = null,
    val logs: List<com.example.features.admin.models.AdminAuditLog> = emptyList()
)

class AdminLogsViewModel(private val repository: AdminRepository) : ViewModel() {
    private val _uiState = MutableStateFlow(AdminLogsViewModelState())
    val uiState: StateFlow<AdminLogsViewModelState> = _uiState.asStateFlow()


    init { fetchLogs() }
    fun fetchLogs() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null) }
            try {
                val res = repository.getAuditLogsAdmin() 
                _uiState.update { it.copy(logs = res, isLoading = false) }
            } catch (e: Exception) {
                _uiState.update { it.copy(isLoading = false, error = e.message) }
            }
        }
    }

}

class AdminLogsViewModelFactory(private val repository: AdminRepository) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(AdminLogsViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return AdminLogsViewModel(repository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
