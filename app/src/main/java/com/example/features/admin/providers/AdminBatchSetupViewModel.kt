package com.example.features.admin.providers

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.core.repository.AdminRepository
import com.example.features.admin.models.AdminBatch
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class AdminBatchSetupState(
    val batches: List<AdminBatch> = emptyList(),
    val isLoading: Boolean = false,
    val error: String? = null
)

class AdminBatchSetupViewModel(private val repository: AdminRepository) : ViewModel() {
    private val _uiState = MutableStateFlow(AdminBatchSetupState())
    val uiState: StateFlow<AdminBatchSetupState> = _uiState.asStateFlow()

    init { loadBatches() }

    fun loadBatches() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null) }
            try {
                val data = repository.getBatches()
                _uiState.update { it.copy(batches = data, isLoading = false) }
            } catch (e: Exception) {
                _uiState.update { it.copy(isLoading = false, error = e.message) }
            }
        }
    }
}
