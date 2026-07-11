package com.example.features.admin.providers

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.core.database.dao.UsersDao
import com.example.core.database.entities.UsersEntity
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

data class AdminUserState(
    val users: List<UsersEntity> = emptyList(),
    val isLoading: Boolean = false,
    val error: String? = null
)

class AdminUserViewModel(private val usersDao: UsersDao) : ViewModel() {
    private val _uiState = MutableStateFlow(AdminUserState())
    val uiState: StateFlow<AdminUserState> = _uiState.asStateFlow()

    init {
        fetchUsers()
    }

    fun fetchUsers() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            usersDao.getAll().collect { userList ->
                _uiState.update { it.copy(users = userList, isLoading = false) }
            }
        }
    }

    fun addUser(user: UsersEntity) {
        viewModelScope.launch {
            usersDao.insert(user)
        }
    }

    fun deleteUser(user: UsersEntity) {
        viewModelScope.launch {
            usersDao.delete(user)
        }
    }
}
