package com.example.features.hod.providers

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.core.network.*
import com.example.core.repository.HODRepository
import kotlinx.coroutines.async
import kotlinx.coroutines.coroutineScope
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
    val isVerifying: Boolean = false,
    val verifyError: String? = null,
    val verificationSuccess: Boolean = false
)

class HODResearchMonitoringViewModel(private val repository: HODRepository) : ViewModel() {
    private val _uiState = MutableStateFlow(HODResearchMonitoringState())
    val uiState: StateFlow<HODResearchMonitoringState> = _uiState.asStateFlow()

    init {
        loadAll()
    }

    fun loadAll() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null) }
            var softError: String? = null
            lateinit var monitoring: List<HODResearchMonitoringDto>
            lateinit var proofs: List<HODPendingProofDto>
            coroutineScope {
                val monitoringDeferred = async { try { repository.getResearchMonitoring() } catch (e: Exception) { softError = e.message; emptyList() } }
                val proofsDeferred = async { try { repository.getPendingProofs() } catch (e: Exception) { softError = e.message; emptyList() } }
                monitoring = monitoringDeferred.await()
                proofs = proofsDeferred.await()
            }
            _uiState.update { it.copy(monitoringData = monitoring, pendingProofs = proofs, isLoading = false, error = softError) }
        }
    }

    fun verifyProof(proofId: String, status: String, remarks: String) {
        viewModelScope.launch {
            _uiState.update { it.copy(isVerifying = true, verifyError = null, verificationSuccess = false) }
            try {
                val req = VerificationRequestDto(status = status, remarks = remarks)
                repository.verifyResearchProof(proofId, req)
                _uiState.update { it.copy(isVerifying = false, verificationSuccess = true) }
                loadAll()
            } catch (e: Exception) {
                _uiState.update { it.copy(isVerifying = false, verifyError = e.message ?: "Failed to verify proof") }
            }
        }
    }

    fun resetVerificationStatus() {
        _uiState.update { it.copy(verificationSuccess = false, verifyError = null) }
    }
}

class HODResearchMonitoringViewModelFactory(private val repository: HODRepository) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        @Suppress("UNCHECKED_CAST")
        return HODResearchMonitoringViewModel(repository) as T
    }
}

