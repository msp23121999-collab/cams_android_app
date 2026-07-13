package com.example.features.parent.providers

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.core.repository.ParentRepository
import com.example.features.parent.models.*
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class ParentNoticesState(
    val notices: List<CollegeNotice> = emptyList(),
    val isLoading: Boolean = true,
    val error: String? = null
)

class ParentNoticesViewModel(private val repository: ParentRepository) : ViewModel() {
    private val _uiState = MutableStateFlow(ParentNoticesState())
    val uiState: StateFlow<ParentNoticesState> = _uiState.asStateFlow()

    var currentChildId: String? = null
        private set

    init {
        viewModelScope.launch {
            repository.selectedChildId.collect { id ->
                currentChildId = id
                loadData()
            }
        }
    }

    fun setChild(id: String) {
        currentChildId = id
        loadData()
    }

    fun loadData() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null) }
            try {
                val notices = repository.getNotices(currentChildId)
                _uiState.update { it.copy(notices = notices, isLoading = false) }
            } catch (e: Exception) {
                _uiState.update { it.copy(isLoading = false, error = e.message) }
            }
        }
    }
}

class ParentNoticesViewModelFactory(private val repository: ParentRepository) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(ParentNoticesViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return ParentNoticesViewModel(repository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
