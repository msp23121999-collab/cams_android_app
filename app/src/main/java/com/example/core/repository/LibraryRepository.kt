package com.example.core.repository

import com.example.core.network.CamsApiService
import com.example.core.network.LibraryBookCreateRequest
import com.example.core.network.LibraryBookDto
import com.example.core.network.LibraryIssueCreateRequest
import com.example.core.network.LibraryIssueDto
import com.example.core.network.LibraryReturnRequest
import java.io.IOException

interface LibraryRepository {
    suspend fun getBooks(category: String? = null): List<LibraryBookDto>
    suspend fun createBook(request: LibraryBookCreateRequest)
    suspend fun updateBook(bookId: String, payload: Map<String, Any?>)
    suspend fun deleteBook(bookId: String)
    suspend fun getIssues(status: String? = null): List<LibraryIssueDto>
    suspend fun createIssue(request: LibraryIssueCreateRequest)
    suspend fun returnIssue(issueId: String, request: LibraryReturnRequest)
}

class LibraryRepositoryImpl(private val apiService: CamsApiService) : LibraryRepository {

    private fun errorDetail(response: retrofit2.Response<*>): String? = try {
        val body = response.errorBody()?.string()
        if (body.isNullOrBlank()) null
        else org.json.JSONObject(body).optString("detail", "").ifBlank { null }
    } catch (e: Exception) { null }

    override suspend fun getBooks(category: String?): List<LibraryBookDto> {
        val response = apiService.getLibraryBooks(category)
        if (response.isSuccessful) return response.body() ?: emptyList()
        throw IOException("Failed to load books: ${response.code()}")
    }

    override suspend fun createBook(request: LibraryBookCreateRequest) {
        val response = apiService.createLibraryBook(request)
        if (!response.isSuccessful) throw IOException(errorDetail(response) ?: "Failed to create book (error ${response.code()})")
    }

    override suspend fun updateBook(bookId: String, payload: Map<String, Any?>) {
        val response = apiService.updateLibraryBook(bookId, payload)
        if (!response.isSuccessful) throw IOException(errorDetail(response) ?: "Failed to update book (error ${response.code()})")
    }

    override suspend fun deleteBook(bookId: String) {
        val response = apiService.deleteLibraryBook(bookId)
        if (!response.isSuccessful) throw IOException(errorDetail(response) ?: "Failed to delete book (error ${response.code()})")
    }

    override suspend fun getIssues(status: String?): List<LibraryIssueDto> {
        val response = apiService.getLibraryIssues(status)
        if (response.isSuccessful) return response.body() ?: emptyList()
        throw IOException("Failed to load issues: ${response.code()}")
    }

    override suspend fun createIssue(request: LibraryIssueCreateRequest) {
        val response = apiService.createLibraryIssue(request)
        if (!response.isSuccessful) throw IOException(errorDetail(response) ?: "Failed to issue book (error ${response.code()})")
    }

    override suspend fun returnIssue(issueId: String, request: LibraryReturnRequest) {
        val response = apiService.returnLibraryIssue(issueId, request)
        if (!response.isSuccessful) throw IOException(errorDetail(response) ?: "Failed to return book (error ${response.code()})")
    }
}
