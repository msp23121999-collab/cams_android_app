package com.example.features.notifications.providers

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.example.core.repository.StudentRepository

import com.example.features.notifications.models.NotificationRecord
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.util.UUID
import androidx.lifecycle.viewModelScope

class NotificationViewModelFactory(private val studentRepository: StudentRepository) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(NotificationViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return NotificationViewModel() as T
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

class NotificationViewModel : ViewModel() {
    private val _uiState = MutableStateFlow(NotificationState())
    val uiState: StateFlow<NotificationState> = _uiState.asStateFlow()

    init {
        fetchNotifications()
    }

    fun fetchNotifications() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null) }
            try {
                delay(1200)
                val now = java.util.Calendar.getInstance()
                val sdf = java.text.SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss", java.util.Locale.US)
                
                val mockData = listOf(
                    NotificationRecord(UUID.randomUUID().toString(), "marks_submission", "Your Mid-Semester marks for Constitutional Law are uploaded.", false, sdf.format(now.apply { add(java.util.Calendar.HOUR, -2) }.time)),
                    NotificationRecord(UUID.randomUUID().toString(), "attendance_lock", "Attendance register for Subject LAW105 has been locked for Dec 2025.", false, sdf.format(now.apply { add(java.util.Calendar.HOUR, -3) }.time)),
                    NotificationRecord(UUID.randomUUID().toString(), "leave_approval", "Your leave application for medical emergency has been approved.", false, sdf.format(now.apply { add(java.util.Calendar.HOUR, -5) }.time)),
                    NotificationRecord(UUID.randomUUID().toString(), "material_upload", "Dr. Sharma uploaded new Study Material: 'Unit 4 - Criminal Procedure'.", true, sdf.format(now.apply { add(java.util.Calendar.DAY_OF_YEAR, -1) }.time)),
                    NotificationRecord(UUID.randomUUID().toString(), "new_assignment", "New Assignment: 'Case Analysis - Kesavananda Bharati v. State of Kerala'.", true, sdf.format(now.apply { add(java.util.Calendar.DAY_OF_YEAR, -2) }.time)),
                    NotificationRecord(UUID.randomUUID().toString(), "grievance_update", "Your grievance regarding Library Fine has been updated.", false, sdf.format(now.apply { add(java.util.Calendar.MINUTE, -45) }.time)),
                    NotificationRecord(UUID.randomUUID().toString(), "marks_approval", "Your Internal Marks for Property Law have been approved.", true, sdf.format(now.apply { add(java.util.Calendar.DAY_OF_YEAR, -3) }.time))
                )
                _uiState.update { it.copy(notifications = mockData, isLoading = false) }
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
