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

data class AdminSystemConfigViewModelState(
    
    val isLoading: Boolean = false,
    val error: String? = null,
    val settings: com.example.features.admin.models.AdminSystemSettings? = null
)

class AdminSystemConfigViewModel(private val repository: AdminRepository) : ViewModel() {
    private val _uiState = MutableStateFlow(AdminSystemConfigViewModelState())
    val uiState: StateFlow<AdminSystemConfigViewModelState> = _uiState.asStateFlow()


    init { fetchConfig() }
    fun fetchConfig() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null) }
            try {
                val res = repository.getSystemSettingsAdmin()
                _uiState.update { it.copy(settings = res, isLoading = false) }
            } catch (e: Exception) {
                _uiState.update { it.copy(isLoading = false, error = e.message) }
            }
        }
    }

}

class AdminSystemConfigViewModelFactory(private val repository: AdminRepository) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(AdminSystemConfigViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return AdminSystemConfigViewModel(repository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
