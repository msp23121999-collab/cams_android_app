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

data class ParentProfileState(
    val childProfileExtended: ChildProfileExtended? = null,
    val isLoading: Boolean = true,
    val error: String? = null
)

class ParentProfileViewModel(private val repository: ParentRepository) : ViewModel() {
    private val _uiState = MutableStateFlow(ParentProfileState())
    val uiState: StateFlow<ParentProfileState> = _uiState.asStateFlow()

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
                val profile = repository.getChildProfile(currentChildId)
                _uiState.update { it.copy(childProfileExtended = profile, isLoading = false) }
            } catch (e: Exception) {
                _uiState.update { it.copy(isLoading = false, error = e.message) }
            }
        }
    }

    fun changePassword(currentPassword: String, newPassword: String) {
        viewModelScope.launch {
            try {
                repository.changePassword(currentPassword, newPassword)
            } catch (e: Exception) {
                throw e
            }
        }
    }
}

class ParentProfileViewModelFactory(private val repository: ParentRepository) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(ParentProfileViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return ParentProfileViewModel(repository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
