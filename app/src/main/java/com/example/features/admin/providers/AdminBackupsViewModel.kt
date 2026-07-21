package com.example.features.admin.providers

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.core.network.AdminBackupSettingsDto
import com.example.core.repository.AdminRepository
import kotlinx.coroutines.async
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class AdminBackupsViewModelState(
    val isLoading: Boolean = true,
    val error: String? = null,
    val backups: List<com.example.features.admin.models.AdminBackup> = emptyList(),
    val settings: AdminBackupSettingsDto = AdminBackupSettingsDto(),
    val isRunning: Boolean = false,
    val actionError: String? = null,
    val actionMessage: String? = null
)

class AdminBackupsViewModel(private val repository: AdminRepository) : ViewModel() {
    private val _uiState = MutableStateFlow(AdminBackupsViewModelState())
    val uiState: StateFlow<AdminBackupsViewModelState> = _uiState.asStateFlow()

    init { fetchBackups() }

    fun fetchBackups() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null) }
            try {
                coroutineScope {
                    val historyD = async { repository.getBackupHistoryAdmin() }
                    val settingsD = async {
                        try { repository.getBackupSettings() } catch (e: Exception) { AdminBackupSettingsDto() }
                    }
                    _uiState.update {
                        it.copy(backups = historyD.await(), settings = settingsD.await(), isLoading = false)
                    }
                }
            } catch (e: Exception) {
                _uiState.update { it.copy(isLoading = false, error = e.message ?: "Failed to load backups") }
            }
        }
    }

    fun runBackupNow() {
        viewModelScope.launch {
            _uiState.update { it.copy(isRunning = true, actionError = null, actionMessage = null) }
            try {
                repository.createBackup()
                fetchBackups()
                _uiState.update { it.copy(isRunning = false, actionMessage = "Backup created") }
            } catch (e: Exception) {
                _uiState.update { it.copy(isRunning = false, actionError = e.message ?: "Backup failed") }
            }
        }
    }

    fun restoreBackup(backupId: String) {
        viewModelScope.launch {
            _uiState.update { it.copy(isRunning = true, actionError = null, actionMessage = null) }
            try {
                repository.restoreBackup(backupId)
                _uiState.update { it.copy(isRunning = false, actionMessage = "Restore completed") }
            } catch (e: Exception) {
                _uiState.update { it.copy(isRunning = false, actionError = e.message ?: "Restore failed") }
            }
        }
    }

    fun deleteBackup(backupId: String) {
        viewModelScope.launch {
            _uiState.update { it.copy(actionError = null, actionMessage = null) }
            try {
                repository.deleteBackup(backupId)
                fetchBackups()
                _uiState.update { it.copy(actionMessage = "Backup deleted") }
            } catch (e: Exception) {
                _uiState.update { it.copy(actionError = e.message ?: "Delete failed") }
            }
        }
    }

    fun setAutoBackup(enabled: Boolean) {
        val previous = _uiState.value.settings
        val updated = previous.copy(autoBackupEnabled = enabled)
        _uiState.update { it.copy(settings = updated) }
        viewModelScope.launch {
            try {
                repository.saveBackupSettings(updated)
            } catch (e: Exception) {
                _uiState.update { it.copy(settings = previous, actionError = e.message ?: "Failed to update setting") }
            }
        }
    }

    fun clearActionStatus() {
        _uiState.update { it.copy(actionError = null, actionMessage = null) }
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
