package com.example.features.auth.providers

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.core.network.AuthEvent
import com.example.core.network.AuthEventBus
import com.example.core.network.AuthManager
import com.example.core.repository.AuthRepository
import com.example.features.auth.models.User
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.flow.firstOrNull

// Equivalent to Riverpod's AuthNotifier state
data class AuthState(
    val user: User? = null,
    val token: String? = null,
    val role: String? = null,
    val subdomainTarget: String? = null,
    val isLoading: Boolean = false,
    val error: String? = null
)

// Equivalent to Riverpod's AuthNotifier
class AuthViewModel(
    private val authRepository: AuthRepository,
    private val authManager: AuthManager
) : ViewModel() {

    private val _uiState = MutableStateFlow(AuthState(isLoading = true))
    val uiState: StateFlow<AuthState> = _uiState.asStateFlow()

    init {
        loadInitialState()
        
        viewModelScope.launch {
            // Listen for 401 Unauthorized events from interceptor
            AuthEventBus.events.collect { event ->
                when (event) {
                    is AuthEvent.Unauthorized -> logout()
                }
            }
        }
    }

    private fun loadInitialState() {
        viewModelScope.launch {
            try {
                val token = authManager.tokenFlow.firstOrNull() ?: authManager.getToken()
                val role = authManager.roleFlow.firstOrNull() ?: authManager.getRole()
                val subdomainTarget = authManager.subdomainTargetFlow.firstOrNull() ?: authManager.getSubdomainTarget()
                
                if (token != null && role != null) {
                    // Try to get user from local db
                    val localUser = authRepository.getCurrentUser().firstOrNull()
                    val user = if (localUser != null) {
                        User(
                            id = localUser.id,
                            email = localUser.email,
                            fullName = localUser.fullName,
                            role = localUser.role,
                            departmentId = localUser.departmentId
                        )
                    } else {
                        User(id = "1", email = "user@cams.local", fullName = "Active User", role = role)
                    }

                    _uiState.update { 
                        it.copy(
                            token = token,
                            role = role,
                            subdomainTarget = subdomainTarget,
                            isLoading = false,
                            user = user
                        )
                    }
                } else {
                    _uiState.update { it.copy(isLoading = false) }
                }
            } catch (e: Exception) {
                _uiState.update { it.copy(isLoading = false) }
            }
        }
    }

    fun login(email: String, password: String) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null) }
            
            val result = authRepository.login(email, password)
            
            if (result.isSuccess) {
                val token = authManager.getToken()
                val role = authManager.getRole()
                val subdomainTarget = authManager.getSubdomainTarget()
                val localUser = authRepository.getCurrentUser().firstOrNull()
                
                val user = if (localUser != null) {
                    User(
                        id = localUser.id,
                        email = localUser.email,
                        fullName = localUser.fullName,
                        role = localUser.role,
                        departmentId = localUser.departmentId
                    )
                } else {
                    User(id = "1", email = email, fullName = "Logged In User", role = role ?: "UNKNOWN")
                }

                _uiState.update { 
                    it.copy(
                        isLoading = false,
                        token = token,
                        role = role,
                        subdomainTarget = subdomainTarget,
                        user = user,
                        error = null
                    )
                }
            } else {
                _uiState.update { 
                    it.copy(
                        isLoading = false, 
                        error = result.exceptionOrNull()?.message ?: "Login failed"
                    ) 
                }
            }
        }
    }

    fun logout() {
        viewModelScope.launch {
            authRepository.logout()
            _uiState.update { AuthState() }
        }
    }
}

// Factory for instantiation
class AuthViewModelFactory(
    private val authRepository: AuthRepository,
    private val authManager: AuthManager
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(AuthViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return AuthViewModel(authRepository, authManager) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
