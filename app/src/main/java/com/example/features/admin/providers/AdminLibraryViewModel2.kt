package com.example.features.admin.providers

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.core.network.LibraryBookCreateRequest
import com.example.core.network.LibraryBookDto
import com.example.core.network.LibraryIssueCreateRequest
import com.example.core.network.LibraryIssueDto
import com.example.core.network.LibraryReturnRequest
import com.example.core.repository.AdminRepository
import com.example.core.repository.LibraryRepository
import com.example.features.admin.models.AdminFeeStudent
import kotlinx.coroutines.async
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class AdminLibraryState2(
    val isLoading: Boolean = true,
    val error: String? = null,
    val books: List<LibraryBookDto> = emptyList(),
    val issues: List<LibraryIssueDto> = emptyList(),
    val studentResults: List<AdminFeeStudent> = emptyList(),
    val isSearching: Boolean = false,
    val isSaving: Boolean = false,
    val saveError: String? = null,
    val saveSuccess: Boolean = false
)

/** Named ViewModel2 to avoid colliding with the pre-existing dead AdminLibraryViewModel stub. */
class AdminLibraryViewModel2(
    private val repository: LibraryRepository,
    private val adminRepository: AdminRepository
) : ViewModel() {
    private val _uiState = MutableStateFlow(AdminLibraryState2())
    val uiState: StateFlow<AdminLibraryState2> = _uiState.asStateFlow()

    init { loadAll() }

    fun loadAll() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null) }
            try {
                coroutineScope {
                    val booksD = async { repository.getBooks() }
                    val issuesD = async { repository.getIssues() }
                    _uiState.update { it.copy(books = booksD.await(), issues = issuesD.await(), isLoading = false) }
                }
            } catch (e: Exception) {
                _uiState.update { it.copy(isLoading = false, error = e.message ?: "Failed to load library data") }
            }
        }
    }

    fun searchStudents(query: String) {
        if (query.isBlank()) {
            _uiState.update { it.copy(studentResults = emptyList()) }
            return
        }
        viewModelScope.launch {
            _uiState.update { it.copy(isSearching = true) }
            try {
                val results = adminRepository.searchStudentsForFees(query) ?: emptyList()
                _uiState.update { it.copy(studentResults = results, isSearching = false) }
            } catch (e: Exception) {
                _uiState.update { it.copy(isSearching = false, error = e.message) }
            }
        }
    }

    private fun mutate(block: suspend () -> Unit) {
        viewModelScope.launch {
            _uiState.update { it.copy(isSaving = true, saveError = null, saveSuccess = false) }
            try {
                block()
                loadAll()
                _uiState.update { it.copy(isSaving = false, saveSuccess = true, studentResults = emptyList()) }
            } catch (e: Exception) {
                _uiState.update { it.copy(isSaving = false, saveError = e.message ?: "Operation failed") }
            }
        }
    }

    fun createBook(title: String, author: String, accessionNo: String, isbn: String, category: String, publisher: String, totalCopies: Int) = mutate {
        repository.createBook(
            LibraryBookCreateRequest(
                title = title, accessionNo = accessionNo, author = author.ifBlank { null }, isbn = isbn.ifBlank { null },
                category = category.ifBlank { null }, publisher = publisher.ifBlank { null }, totalCopies = totalCopies
            )
        )
    }

    fun deleteBook(bookId: String) = mutate { repository.deleteBook(bookId) }

    fun issueBook(bookId: String, memberId: String, dueOn: String) = mutate {
        repository.createIssue(LibraryIssueCreateRequest(bookId = bookId, memberId = memberId, dueOn = dueOn.ifBlank { null }))
    }

    fun returnBook(issueId: String, fineAmount: Double?) = mutate {
        repository.returnIssue(issueId, LibraryReturnRequest(fineAmount = fineAmount))
    }

    fun clearSaveStatus() {
        _uiState.update { it.copy(saveError = null, saveSuccess = false) }
    }
}

class AdminLibraryViewModel2Factory(
    private val repository: LibraryRepository,
    private val adminRepository: AdminRepository
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(AdminLibraryViewModel2::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return AdminLibraryViewModel2(repository, adminRepository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
