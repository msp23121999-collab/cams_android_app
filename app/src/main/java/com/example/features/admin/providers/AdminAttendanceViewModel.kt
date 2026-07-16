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

data class AdminAttendanceViewModelState(
    val data: List<com.example.features.admin.models.AdminAttendanceDefaulter> = emptyList(),
    val isLoading: Boolean = false,
    val error: String? = null,
    
)

class AdminAttendanceViewModel(private val repository: AdminRepository) : ViewModel() {
    private val _uiState = MutableStateFlow(AdminAttendanceViewModelState())
    val uiState: StateFlow<AdminAttendanceViewModelState> = _uiState.asStateFlow()


    init { fetchDefaulters() }
    fun fetchDefaulters() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null) }
            try {
                val res = repository.getAttendanceDefaultersAdmin()
                _uiState.update { it.copy(data = res, isLoading = false) }
            } catch (e: Exception) {
                _uiState.update { it.copy(isLoading = false, error = e.message) }
            }
        }
    }

}

class AdminAttendanceViewModelFactory(private val repository: AdminRepository) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(AdminAttendanceViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return AdminAttendanceViewModel(repository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
