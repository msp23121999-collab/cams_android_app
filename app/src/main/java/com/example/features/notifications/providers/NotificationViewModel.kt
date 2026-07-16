package com.example.features.notifications.providers

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.core.repository.StudentRepository
import com.example.features.notifications.models.NotificationRecord
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import androidx.paging.Pager
import androidx.paging.PagingConfig
import androidx.paging.PagingData
import androidx.paging.cachedIn
import androidx.paging.map
import androidx.paging.filter
import com.example.core.network.GenericPagingSource
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.combine

class NotificationViewModelFactory(private val studentRepository: StudentRepository) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(NotificationViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return NotificationViewModel(studentRepository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}

data class NotificationState(
    val notifications: List<NotificationRecord> = emptyList(),
    val activeTab: String = "all", // "all", "unread", "read"
    val typeFilter: String = "all",
    val searchQuery: String = "",
    val isLoading: Boolean = true,
    val error: String? = null
)

class NotificationViewModel(private val repository: StudentRepository) : ViewModel() {
    private val _uiState = MutableStateFlow(NotificationState())
    val uiState: StateFlow<NotificationState> = _uiState.asStateFlow()

    val notificationsPagingFlow: Flow<PagingData<NotificationRecord>> = Pager(
        config = PagingConfig(pageSize = 20, enablePlaceholders = false),
        pagingSourceFactory = { GenericPagingSource({ skip, limit -> repository.getNotificationsPaged(skip, limit) }) }
    ).flow
        .map { pagingData ->
            pagingData.map { dto ->
                NotificationRecord(
                    id = dto.id,
                    type = dto.title,
                    message = dto.message,
                    isRead = dto.isRead,
                    createdAt = dto.date
                )
            }
        }
        .cachedIn(viewModelScope)
        .combine(_uiState) { pagingData, state ->
            pagingData.filter { notif ->
                val matchesTab = when (state.activeTab) {
                    "unread" -> !notif.isRead
                    "read" -> notif.isRead
                    else -> true
                }
                val matchesType = state.typeFilter == "all" || notif.type == state.typeFilter
                val matchesSearch = state.searchQuery.isEmpty() || notif.message.contains(state.searchQuery, ignoreCase = true)
                matchesTab && matchesType && matchesSearch
            }
        }

    init {
        fetchNotifications()
    }

    fun fetchNotifications() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null) }
            try {
                val dtos = repository.getNotifications()
                val data = dtos.map { dto ->
                    NotificationRecord(
                        id = dto.id,
                        type = dto.title, // "title" in DTO acts as type/title
                        message = dto.message,
                        isRead = dto.isRead,
                        createdAt = dto.date
                    )
                }
                _uiState.update { it.copy(notifications = data, isLoading = false) }
            } catch (e: Exception) {
                _uiState.update { it.copy(isLoading = false, error = e.message ?: "Failed to fetch notifications") }
            }
        }
    }

    fun setTab(tab: String) {
        _uiState.update { it.copy(activeTab = tab) }
    }

    fun setTypeFilter(type: String) {
        _uiState.update { it.copy(typeFilter = type) }
    }

    fun setSearchQuery(query: String) {
        _uiState.update { it.copy(searchQuery = query) }
    }

    fun markAsRead(id: String) {
        _uiState.update { state ->
            val updated = state.notifications.map {
                if (it.id == id) it.copy(isRead = true) else it
            }
            state.copy(notifications = updated)
        }
    }

    fun markAllAsRead() {
        _uiState.update { state ->
            val updated = state.notifications.map { it.copy(isRead = true) }
            state.copy(notifications = updated)
        }
    }

    fun deleteNotification(id: String) {
        _uiState.update { state ->
            val updated = state.notifications.filter { it.id != id }
            state.copy(notifications = updated)
        }
    }

    fun deleteAllRead() {
        _uiState.update { state ->
            val updated = state.notifications.filter { !it.isRead }
            state.copy(notifications = updated)
        }
    }
}
