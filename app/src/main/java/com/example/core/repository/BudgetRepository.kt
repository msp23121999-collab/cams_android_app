package com.example.core.repository

import com.example.core.network.BudgetExpenseCreateRequest
import com.example.core.network.BudgetExpenseDto
import com.example.core.network.BudgetLineItemCreateRequest
import com.example.core.network.BudgetLineItemDto
import com.example.core.network.BudgetSummaryDto
import com.example.core.network.CamsApiService
import com.example.core.network.GrantCreateRequest
import com.example.core.network.GrantDto
import java.io.IOException

interface BudgetRepository {
    suspend fun getSummary(): BudgetSummaryDto
    suspend fun getLineItems(): List<BudgetLineItemDto>
    suspend fun createLineItem(request: BudgetLineItemCreateRequest)
    suspend fun deleteLineItem(itemId: String)
    suspend fun recordExpense(itemId: String, request: BudgetExpenseCreateRequest)
    suspend fun getExpenses(itemId: String): List<BudgetExpenseDto>
    suspend fun getGrants(): List<GrantDto>
    suspend fun createGrant(request: GrantCreateRequest)
    suspend fun updateGrantStatus(grantId: String, status: String)
    suspend fun deleteGrant(grantId: String)
}

class BudgetRepositoryImpl(private val apiService: CamsApiService) : BudgetRepository {

    private fun errorDetail(response: retrofit2.Response<*>): String? = try {
        val body = response.errorBody()?.string()
        if (body.isNullOrBlank()) null
        else org.json.JSONObject(body).optString("detail", "").ifBlank { null }
    } catch (e: Exception) { null }

    override suspend fun getSummary(): BudgetSummaryDto {
        val response = apiService.getBudgetSummary()
        if (response.isSuccessful) return response.body() ?: throw IOException("Empty response body")
        throw IOException("Failed to load budget summary: ${response.code()}")
    }

    override suspend fun getLineItems(): List<BudgetLineItemDto> {
        val response = apiService.getBudgetLineItems()
        if (response.isSuccessful) return response.body() ?: emptyList()
        throw IOException("Failed to load budget line items: ${response.code()}")
    }

    override suspend fun createLineItem(request: BudgetLineItemCreateRequest) {
        val response = apiService.createBudgetLineItem(request)
        if (!response.isSuccessful) throw IOException(errorDetail(response) ?: "Failed to create budget line item (error ${response.code()})")
    }

    override suspend fun deleteLineItem(itemId: String) {
        val response = apiService.deleteBudgetLineItem(itemId)
        if (!response.isSuccessful) throw IOException(errorDetail(response) ?: "Failed to delete budget line item (error ${response.code()})")
    }

    override suspend fun recordExpense(itemId: String, request: BudgetExpenseCreateRequest) {
        val response = apiService.recordBudgetExpense(itemId, request)
        if (!response.isSuccessful) throw IOException(errorDetail(response) ?: "Failed to record expense (error ${response.code()})")
    }

    override suspend fun getExpenses(itemId: String): List<BudgetExpenseDto> {
        val response = apiService.getBudgetExpenses(itemId)
        if (response.isSuccessful) return response.body() ?: emptyList()
        throw IOException("Failed to load expenses: ${response.code()}")
    }

    override suspend fun getGrants(): List<GrantDto> {
        val response = apiService.getGrants()
        if (response.isSuccessful) return response.body() ?: emptyList()
        throw IOException("Failed to load grants: ${response.code()}")
    }

    override suspend fun createGrant(request: GrantCreateRequest) {
        val response = apiService.createGrant(request)
        if (!response.isSuccessful) throw IOException(errorDetail(response) ?: "Failed to create grant (error ${response.code()})")
    }

    override suspend fun updateGrantStatus(grantId: String, status: String) {
        val response = apiService.updateGrant(grantId, mapOf("status" to status))
        if (!response.isSuccessful) throw IOException(errorDetail(response) ?: "Failed to update grant (error ${response.code()})")
    }

    override suspend fun deleteGrant(grantId: String) {
        val response = apiService.deleteGrant(grantId)
        if (!response.isSuccessful) throw IOException(errorDetail(response) ?: "Failed to delete grant (error ${response.code()})")
    }
}
