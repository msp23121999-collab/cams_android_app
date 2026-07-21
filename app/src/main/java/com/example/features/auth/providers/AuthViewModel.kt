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

data class PasswordResetState(
    val isLoading: Boolean = false,
    val message: String? = null,
    val error: String? = null,
    val success: Boolean = false
)

data class EmailChangeState(
    val isLoading: Boolean = false,
    val message: String? = null,
    val error: String? = null,
    val success: Boolean = false
)

data class NotificationPreferencesState(
    val emailNotificationsEnabled: Boolean = true,
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

    private val _passwordResetState = MutableStateFlow(PasswordResetState())
    val passwordResetState: StateFlow<PasswordResetState> = _passwordResetState.asStateFlow()

    private val _emailChangeState = MutableStateFlow(EmailChangeState())
    val emailChangeState: StateFlow<EmailChangeState> = _emailChangeState.asStateFlow()

    private val _notificationPreferencesState = MutableStateFlow(NotificationPreferencesState())
    val notificationPreferencesState: StateFlow<NotificationPreferencesState> = _notificationPreferencesState.asStateFlow()

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
                // Register this device for push now that there is a session. The
                // token cannot be registered before sign-in (the endpoint requires
                // auth), so this is the first point it can succeed.
                com.example.core.services.PushTokenRegistrar.registerCurrentToken()

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

    fun requestPasswordReset(email: String) {
        viewModelScope.launch {
            _passwordResetState.update { PasswordResetState(isLoading = true) }
            val result = authRepository.forgotPassword(email)
            _passwordResetState.update {
                if (result.isSuccess) {
                    PasswordResetState(isLoading = false, message = result.getOrNull(), success = true)
                } else {
                    PasswordResetState(isLoading = false, error = result.exceptionOrNull()?.message ?: "Failed to send reset email")
                }
            }
        }
    }

    fun resetPassword(token: String, newPassword: String) {
        viewModelScope.launch {
            _passwordResetState.update { PasswordResetState(isLoading = true) }
            val result = authRepository.resetPassword(token, newPassword)
            _passwordResetState.update {
                if (result.isSuccess) {
                    PasswordResetState(isLoading = false, message = result.getOrNull(), success = true)
                } else {
                    PasswordResetState(isLoading = false, error = result.exceptionOrNull()?.message ?: "Invalid or expired reset token")
                }
            }
        }
    }

    fun clearPasswordResetState() {
        _passwordResetState.update { PasswordResetState() }
    }

    fun requestEmailChange(newEmail: String, currentPassword: String) {
        viewModelScope.launch {
            _emailChangeState.update { EmailChangeState(isLoading = true) }
            val result = authRepository.requestEmailChange(newEmail, currentPassword)
            _emailChangeState.update {
                if (result.isSuccess) {
                    EmailChangeState(isLoading = false, message = result.getOrNull(), success = true)
                } else {
                    EmailChangeState(isLoading = false, error = result.exceptionOrNull()?.message ?: "Failed to request email change")
                }
            }
        }
    }

    fun clearEmailChangeState() {
        _emailChangeState.update { EmailChangeState() }
    }

    fun loadNotificationPreferences() {
        viewModelScope.launch {
            _notificationPreferencesState.update { it.copy(isLoading = true, error = null) }
            val enabled = authRepository.getEmailNotificationsEnabled()
            _notificationPreferencesState.update { NotificationPreferencesState(emailNotificationsEnabled = enabled, isLoading = false) }
        }
    }

    fun setEmailNotificationsEnabled(enabled: Boolean) {
        viewModelScope.launch {
            // Optimistic update so the toggle feels responsive; reverted on failure.
            _notificationPreferencesState.update { it.copy(emailNotificationsEnabled = enabled, isLoading = true, error = null) }
            val result = authRepository.setEmailNotificationsEnabled(enabled)
            _notificationPreferencesState.update {
                if (result.isSuccess) {
                    it.copy(emailNotificationsEnabled = result.getOrElse { enabled }, isLoading = false)
                } else {
                    it.copy(emailNotificationsEnabled = !enabled, isLoading = false, error = result.exceptionOrNull()?.message ?: "Failed to update preference")
                }
            }
        }
    }

    fun logout() {
        viewModelScope.launch {
            // Detach this device first — the unregister call needs the session that
            // logout() is about to clear. Otherwise the next person to sign in on a
            // shared device keeps receiving the previous user's notifications.
            com.example.core.services.PushTokenRegistrar.unregisterCurrentToken()
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
