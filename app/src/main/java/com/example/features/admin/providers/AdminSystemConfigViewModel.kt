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
    val isLoading: Boolean = true,
    val error: String? = null,
    val settings: com.example.features.admin.models.AdminSystemSettings? = null,
    val emailNotificationsEnabled: Boolean = true,
    val isSaving: Boolean = false,
    val saveError: String? = null,
    val saveSuccess: Boolean = false
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

    fun saveSettings(updated: com.example.features.admin.models.AdminSystemSettings) {
        viewModelScope.launch {
            _uiState.update { it.copy(isSaving = true, saveError = null, saveSuccess = false) }
            try {
                repository.saveSystemSettings(
                    com.example.core.network.AdminSystemSettingsDto(
                        collegeName = updated.collegeName,
                        address = updated.address,
                        affiliationNumber = updated.affiliationNumber,
                        aicteUgcCode = updated.aicteUgcCode,
                        accreditationBody = updated.accreditationBody,
                        bankName = updated.bankName,
                        bankAccountNo = updated.bankAccountNo,
                        bankIfsc = updated.bankIfsc,
                        bankBranch = updated.bankBranch
                    )
                )
                fetchConfig()
                _uiState.update { it.copy(isSaving = false, saveSuccess = true) }
            } catch (e: Exception) {
                _uiState.update { it.copy(isSaving = false, saveError = e.message ?: "Failed to save settings") }
            }
        }
    }

    fun setEmailNotifications(enabled: Boolean) {
        // Optimistic toggle; reverted if the backend rejects the change.
        val previous = _uiState.value.emailNotificationsEnabled
        _uiState.update { it.copy(emailNotificationsEnabled = enabled) }
        viewModelScope.launch {
            try {
                repository.setEmailNotificationsEnabled(enabled)
            } catch (e: Exception) {
                _uiState.update { it.copy(emailNotificationsEnabled = previous, saveError = e.message ?: "Failed to update preference") }
            }
        }
    }

    fun clearSaveStatus() {
        _uiState.update { it.copy(saveError = null, saveSuccess = false) }
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
