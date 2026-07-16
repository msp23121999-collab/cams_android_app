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

data class AdminNoticesViewModelState(
    val data: Any? = null,
    val isLoading: Boolean = false,
    val error: String? = null,
    val notices: List<Any> = emptyList()
)

class AdminNoticesViewModel(private val repository: AdminRepository) : ViewModel() {
    private val _uiState = MutableStateFlow(AdminNoticesViewModelState())
    val uiState: StateFlow<AdminNoticesViewModelState> = _uiState.asStateFlow()


    // We can use generic fetch for notices since they might not be in repository yet
    fun fetchNotices() {
        _uiState.update { it.copy(isLoading = false, data = listOf<Any>()) }
    }

}

class AdminNoticesViewModelFactory(private val repository: AdminRepository) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(AdminNoticesViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return AdminNoticesViewModel(repository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
