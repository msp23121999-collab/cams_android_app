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

data class AdminBackupsViewModelState(
    
    val isLoading: Boolean = false,
    val error: String? = null,
    val backups: List<com.example.features.admin.models.AdminBackup> = emptyList()
)

class AdminBackupsViewModel(private val repository: AdminRepository) : ViewModel() {
    private val _uiState = MutableStateFlow(AdminBackupsViewModelState())
    val uiState: StateFlow<AdminBackupsViewModelState> = _uiState.asStateFlow()


    init { fetchBackups() }
    fun fetchBackups() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null) }
            try {
                val res = repository.getBackupHistoryAdmin()
                _uiState.update { it.copy(backups = res, isLoading = false) }
            } catch (e: Exception) {
                _uiState.update { it.copy(isLoading = false, error = e.message) }
            }
        }
    }

}

class AdminBackupsViewModelFactory(private val repository: AdminRepository) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(AdminBackupsViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return AdminBackupsViewModel(repository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
