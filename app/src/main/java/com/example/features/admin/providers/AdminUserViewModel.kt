package com.example.features.admin.providers

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.core.repository.AdminRepository
import com.example.features.admin.models.AdminUser
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

data class AdminUserState(
    val users: List<AdminUser> = emptyList(),
    val isLoading: Boolean = false,
    val error: String? = null
)

class AdminUserViewModel(private val repository: AdminRepository) : ViewModel() {
    private val _uiState = MutableStateFlow(AdminUserState())
    val uiState: StateFlow<AdminUserState> = _uiState.asStateFlow()

    init {
        fetchUsers()
    }

    fun fetchUsers() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null) }
            try {
                val users = repository.getUsers()
                _uiState.update { it.copy(users = users, isLoading = false) }
            } catch (e: Exception) {
                _uiState.update { it.copy(isLoading = false, error = e.message) }
            }
        }
    }

    fun deleteUser(user: AdminUser) {
        // Mock implementation for now, delete user API needs to be linked
        _uiState.update { state -> 
            state.copy(users = state.users.filter { it.id != user.id })
        }
    }

    fun addUser(user: AdminUser) {
        _uiState.update { state -> 
            state.copy(users = state.users + user)
        }
    }
}
