package com.example.features.admin.providers

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.core.network.InventoryItemCreateRequest
import com.example.core.network.InventoryItemDto
import com.example.core.network.InventoryTransactionDto
import com.example.core.network.StockMovementRequest
import com.example.core.repository.InventoryRepository
import kotlinx.coroutines.async
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class AdminInventoryState2(
    val isLoading: Boolean = true,
    val error: String? = null,
    val items: List<InventoryItemDto> = emptyList(),
    val transactions: List<InventoryTransactionDto> = emptyList(),
    val isSaving: Boolean = false,
    val saveError: String? = null,
    val saveSuccess: Boolean = false
)

/** Named ViewModel2 to avoid colliding with the pre-existing dead AdminInventoryViewModel stub. */
class AdminInventoryViewModel2(private val repository: InventoryRepository) : ViewModel() {
    private val _uiState = MutableStateFlow(AdminInventoryState2())
    val uiState: StateFlow<AdminInventoryState2> = _uiState.asStateFlow()

    init { loadAll() }

    fun loadAll() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null) }
            try {
                coroutineScope {
                    val itemsD = async { repository.getItems() }
                    val txD = async { repository.getTransactions() }
                    _uiState.update { it.copy(items = itemsD.await(), transactions = txD.await(), isLoading = false) }
                }
            } catch (e: Exception) {
                _uiState.update { it.copy(isLoading = false, error = e.message ?: "Failed to load inventory") }
            }
        }
    }

    private fun mutate(block: suspend () -> Unit) {
        viewModelScope.launch {
            _uiState.update { it.copy(isSaving = true, saveError = null, saveSuccess = false) }
            try {
                block()
                loadAll()
                _uiState.update { it.copy(isSaving = false, saveSuccess = true) }
            } catch (e: Exception) {
                _uiState.update { it.copy(isSaving = false, saveError = e.message ?: "Operation failed") }
            }
        }
    }

    fun createItem(name: String, code: String, category: String, unit: String, quantity: Int, minQuantity: Int, unitPrice: Double?, location: String, supplier: String) = mutate {
        repository.createItem(
            InventoryItemCreateRequest(
                name = name, code = code, category = category.ifBlank { null }, unit = unit.ifBlank { "pcs" },
                quantity = quantity, minQuantity = minQuantity, unitPrice = unitPrice,
                location = location.ifBlank { null }, supplier = supplier.ifBlank { null }
            )
        )
    }

    fun deleteItem(itemId: String) = mutate { repository.deleteItem(itemId) }

    fun recordMovement(itemId: String, movement: String, quantity: Int, reason: String) = mutate {
        repository.recordMovement(itemId, StockMovementRequest(movement = movement, quantity = quantity, reason = reason.ifBlank { null }))
    }

    fun clearSaveStatus() {
        _uiState.update { it.copy(saveError = null, saveSuccess = false) }
    }
}

class AdminInventoryViewModel2Factory(private val repository: InventoryRepository) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(AdminInventoryViewModel2::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return AdminInventoryViewModel2(repository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
