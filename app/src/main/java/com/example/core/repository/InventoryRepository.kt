package com.example.core.repository

import com.example.core.network.CamsApiService
import com.example.core.network.InventoryItemCreateRequest
import com.example.core.network.InventoryItemDto
import com.example.core.network.InventoryTransactionDto
import com.example.core.network.StockMovementRequest
import java.io.IOException

interface InventoryRepository {
    suspend fun getItems(category: String? = null, lowStockOnly: Boolean? = null): List<InventoryItemDto>
    suspend fun createItem(request: InventoryItemCreateRequest)
    suspend fun updateItem(itemId: String, payload: Map<String, Any?>)
    suspend fun deleteItem(itemId: String)
    suspend fun recordMovement(itemId: String, request: StockMovementRequest)
    suspend fun getTransactions(itemId: String? = null): List<InventoryTransactionDto>
}

class InventoryRepositoryImpl(private val apiService: CamsApiService) : InventoryRepository {

    private fun errorDetail(response: retrofit2.Response<*>): String? = try {
        val body = response.errorBody()?.string()
        if (body.isNullOrBlank()) null
        else org.json.JSONObject(body).optString("detail", "").ifBlank { null }
    } catch (e: Exception) { null }

    override suspend fun getItems(category: String?, lowStockOnly: Boolean?): List<InventoryItemDto> {
        val response = apiService.getInventoryItems(category, lowStockOnly)
        if (response.isSuccessful) return response.body() ?: emptyList()
        throw IOException("Failed to load inventory items: ${response.code()}")
    }

    override suspend fun createItem(request: InventoryItemCreateRequest) {
        val response = apiService.createInventoryItem(request)
        if (!response.isSuccessful) throw IOException(errorDetail(response) ?: "Failed to create item (error ${response.code()})")
    }

    override suspend fun updateItem(itemId: String, payload: Map<String, Any?>) {
        val response = apiService.updateInventoryItem(itemId, payload)
        if (!response.isSuccessful) throw IOException(errorDetail(response) ?: "Failed to update item (error ${response.code()})")
    }

    override suspend fun deleteItem(itemId: String) {
        val response = apiService.deleteInventoryItem(itemId)
        if (!response.isSuccessful) throw IOException(errorDetail(response) ?: "Failed to delete item (error ${response.code()})")
    }

    override suspend fun recordMovement(itemId: String, request: StockMovementRequest) {
        val response = apiService.recordInventoryMovement(itemId, request)
        if (!response.isSuccessful) throw IOException(errorDetail(response) ?: "Failed to record movement (error ${response.code()})")
    }

    override suspend fun getTransactions(itemId: String?): List<InventoryTransactionDto> {
        val response = apiService.getInventoryTransactions(itemId)
        if (response.isSuccessful) return response.body() ?: emptyList()
        throw IOException("Failed to load transactions: ${response.code()}")
    }
}
