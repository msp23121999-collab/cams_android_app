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

data class AdminTimetableViewModelState(
    val data: Any? = null,
    val isLoading: Boolean = false,
    val error: String? = null,
    val facultyAssignments: List<Any> = emptyList()
)

class AdminTimetableViewModel(private val repository: AdminRepository) : ViewModel() {
    private val _uiState = MutableStateFlow(AdminTimetableViewModelState())
    val uiState: StateFlow<AdminTimetableViewModelState> = _uiState.asStateFlow()


    init { fetchAssignments() }
    fun fetchAssignments() {
        _uiState.update { it.copy(isLoading = false, facultyAssignments = listOf()) }
    }

}

class AdminTimetableViewModelFactory(private val repository: AdminRepository) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(AdminTimetableViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return AdminTimetableViewModel(repository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
