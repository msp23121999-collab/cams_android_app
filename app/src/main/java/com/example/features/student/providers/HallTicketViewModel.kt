package com.example.features.student.providers

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.core.network.HallTicketDto
import com.example.core.repository.StudentRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

data class HallTicketState(
    val tickets: List<HallTicketDto> = emptyList(),
    val isLoading: Boolean = false,
    val error: String? = null
)

class HallTicketViewModel(private val repository: StudentRepository) : ViewModel() {
    private val _uiState = MutableStateFlow(HallTicketState())
    val uiState: StateFlow<HallTicketState> = _uiState

    init {
        fetchHallTickets()
    }

    fun fetchHallTickets() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, error = null)
            try {
                val tickets = repository.getHallTickets()
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    tickets = tickets
                )
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    error = e.message ?: "Failed to fetch hall tickets"
                )
            }
        }
    }
}

class HallTicketViewModelFactory(private val repository: StudentRepository) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(HallTicketViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return HallTicketViewModel(repository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
