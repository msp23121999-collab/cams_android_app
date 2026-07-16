package com.example.features.hod.providers

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.core.network.*
import com.example.core.repository.HODRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class HODResearchMonitoringState(
    val monitoringData: List<HODResearchMonitoringDto> = emptyList(),
    val pendingProofs: List<HODPendingProofDto> = emptyList(),
    val isLoading: Boolean = false,
    val error: String? = null,
    val verificationSuccess: Boolean = false
)

class HODResearchMonitoringViewModel(private val repository: HODRepository) : ViewModel() {
    private val _uiState = MutableStateFlow(HODResearchMonitoringState())
    val uiState: StateFlow<HODResearchMonitoringState> = _uiState.asStateFlow()

    init {
        fetchMonitoringData()
        fetchPendingProofs()
    }

    fun fetchMonitoringData() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null) }
            try {
                val data = repository.getResearchMonitoring()
                _uiState.update { it.copy(monitoringData = data, isLoading = false) }
            } catch (e: Exception) {
                _uiState.update { it.copy(isLoading = false, error = e.message) }
            }
        }
    }

    fun fetchPendingProofs() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null) }
            try {
                val data = repository.getPendingProofs()
                _uiState.update { it.copy(pendingProofs = data, isLoading = false) }
            } catch (e: Exception) {
                _uiState.update { it.copy(isLoading = false, error = e.message) }
            }
        }
    }

    fun verifyProof(proofId: String, status: String, remarks: String) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null, verificationSuccess = false) }
            try {
                val req = VerificationRequestDto(status = status, remarks = remarks)
                repository.verifyResearchProof(proofId, req)
                _uiState.update { it.copy(isLoading = false, verificationSuccess = true) }
                fetchPendingProofs() // Refresh the list
            } catch (e: Exception) {
                _uiState.update { it.copy(isLoading = false, error = e.message) }
            }
        }
    }
    
    fun resetVerificationStatus() {
        _uiState.update { it.copy(verificationSuccess = false) }
    }
}

class HODResearchMonitoringViewModelFactory(private val repository: HODRepository) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        return HODResearchMonitoringViewModel(repository) as T
    }
}
