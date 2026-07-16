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

data class AdminAcademicCalendarViewModelState(
    val data: Any? = null,
    val isLoading: Boolean = false,
    val error: String? = null,
    val events: List<Any> = emptyList()
)

class AdminAcademicCalendarViewModel(private val repository: AdminRepository) : ViewModel() {
    private val _uiState = MutableStateFlow(AdminAcademicCalendarViewModelState())
    val uiState: StateFlow<AdminAcademicCalendarViewModelState> = _uiState.asStateFlow()


    init { fetchEvents() }
    fun fetchEvents() {
        _uiState.update { it.copy(isLoading = false, events = listOf()) }
    }

}

class AdminAcademicCalendarViewModelFactory(private val repository: AdminRepository) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(AdminAcademicCalendarViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return AdminAcademicCalendarViewModel(repository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
