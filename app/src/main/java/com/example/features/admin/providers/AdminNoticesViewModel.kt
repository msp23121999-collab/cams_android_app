package com.example.features.admin.providers

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.core.network.NoticeDto
import com.example.core.network.NotificationDto
import com.example.core.repository.AdminRepository
import kotlinx.coroutines.async
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class AdminNoticesViewModelState(
    val isLoading: Boolean = true,
    val error: String? = null,
    val notices: List<NoticeDto> = emptyList(),
    val notifications: List<NotificationDto> = emptyList(),
    val unreadCount: Int = 0,
    val isSaving: Boolean = false,
    val saveError: String? = null,
    val saveSuccess: Boolean = false
)

class AdminNoticesViewModel(private val repository: AdminRepository) : ViewModel() {
    private val _uiState = MutableStateFlow(AdminNoticesViewModelState())
    val uiState: StateFlow<AdminNoticesViewModelState> = _uiState.asStateFlow()

    init { loadAll() }

    fun loadAll() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null) }
            try {
                coroutineScope {
                    val noticesDeferred = async {
                        try { repository.getNotices() } catch (e: Exception) { emptyList() }
                    }
                    val notifsDeferred = async {
                        try { repository.getNotifications() } catch (e: Exception) { emptyList() }
                    }
                    val notices = noticesDeferred.await()
                    val notifs = notifsDeferred.await()
                    _uiState.update {
                        it.copy(
                            notices = notices,
                            notifications = notifs,
                            unreadCount = notifs.count { n -> !n.isRead },
                            isLoading = false
                        )
                    }
                }
            } catch (e: Exception) {
                _uiState.update { it.copy(isLoading = false, error = e.message ?: "Failed to load") }
            }
        }
    }

    // Backwards-compatible entry point used by existing screens.
    fun fetchNotices() = loadAll()

    fun createNotice(title: String, body: String, audienceType: String) {
        viewModelScope.launch {
            _uiState.update { it.copy(isSaving = true, saveError = null, saveSuccess = false) }
            try {
                repository.createNotice(title, body, audienceType)
                loadAll()
                _uiState.update { it.copy(isSaving = false, saveSuccess = true) }
            } catch (e: Exception) {
                _uiState.update { it.copy(isSaving = false, saveError = e.message ?: "Failed to publish notice") }
            }
        }
    }

    fun deleteNotice(noticeId: String) {
        viewModelScope.launch {
            try {
                repository.deleteNotice(noticeId)
                loadAll()
            } catch (e: Exception) {
                _uiState.update { it.copy(error = e.message ?: "Failed to delete notice") }
            }
        }
    }

    fun markNotificationRead(notificationId: String) {
        viewModelScope.launch {
            try {
                repository.markNotificationRead(notificationId)
                loadAll()
            } catch (e: Exception) {
                _uiState.update { it.copy(error = e.message ?: "Failed to mark as read") }
            }
        }
    }

    fun markAllNotificationsRead() {
        viewModelScope.launch {
            try {
                repository.markAllNotificationsRead()
                loadAll()
            } catch (e: Exception) {
                _uiState.update { it.copy(error = e.message ?: "Failed to mark all as read") }
            }
        }
    }

    fun deleteNotification(notificationId: String) {
        viewModelScope.launch {
            try {
                repository.deleteNotification(notificationId)
                loadAll()
            } catch (e: Exception) {
                _uiState.update { it.copy(error = e.message ?: "Failed to delete notification") }
            }
        }
    }

    fun clearSaveStatus() {
        _uiState.update { it.copy(saveError = null, saveSuccess = false) }
    }
}

class AdminNoticesViewModelFactory(private val repository: AdminRepository) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(AdminNoticesViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return AdminNoticesViewModel(repository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
