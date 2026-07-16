package com.example.features.admin.providers

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.core.repository.AdminRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class AdminReportsViewModelState(
    val data: Any? = null,
    val isLoading: Boolean = false,
    val error: String? = null,
    val reports: List<Any> = emptyList()
)

class AdminReportsViewModel(private val repository: AdminRepository) : ViewModel() {
    private val _uiState = MutableStateFlow(AdminReportsViewModelState())
    val uiState: StateFlow<AdminReportsViewModelState> = _uiState.asStateFlow()


    init { fetchReports() }
    fun fetchReports() {
        _uiState.update { it.copy(isLoading = false, reports = listOf()) }
    }

}

class AdminReportsViewModelFactory(private val repository: AdminRepository) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(AdminReportsViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return AdminReportsViewModel(repository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
