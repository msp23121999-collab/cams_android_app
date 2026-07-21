package com.example.features.principal.providers

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.core.network.BudgetExpenseCreateRequest
import com.example.core.network.BudgetLineItemCreateRequest
import com.example.core.network.BudgetLineItemDto
import com.example.core.network.BudgetSummaryDto
import com.example.core.network.GrantCreateRequest
import com.example.core.network.GrantDto
import com.example.core.repository.BudgetRepository
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.async
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class PrincipalBudgetState(
    val summary: BudgetSummaryDto? = null,
    val lineItems: List<BudgetLineItemDto> = emptyList(),
    val grants: List<GrantDto> = emptyList(),
    val isLoading: Boolean = true,
    val error: String? = null,
    val isSaving: Boolean = false,
    val saveError: String? = null,
    val saveSuccess: Boolean = false
)

class PrincipalBudgetViewModel(private val repository: BudgetRepository) : ViewModel() {
    private val _uiState = MutableStateFlow(PrincipalBudgetState())
    val uiState: StateFlow<PrincipalBudgetState> = _uiState.asStateFlow()

    init { load() }

    fun load() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null) }
            try {
                coroutineScope {
                    val summaryD = async { repository.getSummary() }
                    val lineItemsD = async { repository.getLineItems() }
                    val grantsD = async { repository.getGrants() }
                    _uiState.update {
                        it.copy(
                            summary = summaryD.await(),
                            lineItems = lineItemsD.await(),
                            grants = grantsD.await(),
                            isLoading = false
                        )
                    }
                }
            } catch (e: Exception) {
                _uiState.update { it.copy(isLoading = false, error = e.message ?: "Failed to load budget data") }
            }
        }
    }

    fun createLineItem(request: BudgetLineItemCreateRequest) {
        viewModelScope.launch {
            _uiState.update { it.copy(isSaving = true, saveError = null, saveSuccess = false) }
            try {
                repository.createLineItem(request)
                load()
                _uiState.update { it.copy(isSaving = false, saveSuccess = true) }
            } catch (e: Exception) {
                _uiState.update { it.copy(isSaving = false, saveError = e.message ?: "Failed to create budget line item") }
            }
        }
    }

    fun deleteLineItem(itemId: String) {
        viewModelScope.launch {
            _uiState.update { it.copy(isSaving = true, saveError = null) }
            try {
                repository.deleteLineItem(itemId)
                load()
                _uiState.update { it.copy(isSaving = false) }
            } catch (e: Exception) {
                _uiState.update { it.copy(isSaving = false, saveError = e.message ?: "Failed to delete budget line item") }
            }
        }
    }

    fun recordExpense(itemId: String, request: BudgetExpenseCreateRequest) {
        viewModelScope.launch {
            _uiState.update { it.copy(isSaving = true, saveError = null, saveSuccess = false) }
            try {
                repository.recordExpense(itemId, request)
                load()
                _uiState.update { it.copy(isSaving = false, saveSuccess = true) }
            } catch (e: Exception) {
                _uiState.update { it.copy(isSaving = false, saveError = e.message ?: "Failed to record expense") }
            }
        }
    }

    fun createGrant(request: GrantCreateRequest) {
        viewModelScope.launch {
            _uiState.update { it.copy(isSaving = true, saveError = null, saveSuccess = false) }
            try {
                repository.createGrant(request)
                load()
                _uiState.update { it.copy(isSaving = false, saveSuccess = true) }
            } catch (e: Exception) {
                _uiState.update { it.copy(isSaving = false, saveError = e.message ?: "Failed to create grant") }
            }
        }
    }

    fun updateGrantStatus(grantId: String, status: String) {
        viewModelScope.launch {
            _uiState.update { it.copy(isSaving = true, saveError = null) }
            try {
                repository.updateGrantStatus(grantId, status)
                load()
                _uiState.update { it.copy(isSaving = false) }
            } catch (e: Exception) {
                _uiState.update { it.copy(isSaving = false, saveError = e.message ?: "Failed to update grant") }
            }
        }
    }

    fun deleteGrant(grantId: String) {
        viewModelScope.launch {
            _uiState.update { it.copy(isSaving = true, saveError = null) }
            try {
                repository.deleteGrant(grantId)
                load()
                _uiState.update { it.copy(isSaving = false) }
            } catch (e: Exception) {
                _uiState.update { it.copy(isSaving = false, saveError = e.message ?: "Failed to delete grant") }
            }
        }
    }

    fun clearSaveStatus() {
        _uiState.update { it.copy(saveError = null, saveSuccess = false) }
    }
}

class PrincipalBudgetViewModelFactory(private val repository: BudgetRepository) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(PrincipalBudgetViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return PrincipalBudgetViewModel(repository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
