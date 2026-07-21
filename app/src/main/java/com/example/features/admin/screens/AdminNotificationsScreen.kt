package com.example.features.admin.screens

import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.*

import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.features.admin.providers.AdminNoticesViewModel
import com.example.features.admin.providers.AdminNoticesViewModelFactory
import com.example.core.repository.AdminRepositoryImpl
import com.example.CamsApplication

import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.core.theme.*
import com.example.core.ui.CamsCard
import com.example.features.admin.widgets.AdminBaseScreen
import com.example.core.navigation.AppRoutes

@Composable
fun AdminNotificationsScreen(
    onNavigate: (String) -> Unit,
    viewModel: AdminNoticesViewModel = viewModel(factory = AdminNoticesViewModelFactory(AdminRepositoryImpl(CamsApplication.instance.container.apiService)))
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    var activeTab by remember { mutableStateOf("all") }

    AdminBaseScreen(
        title = "Notification Centre",
        currentRoute = AppRoutes.ADMIN_NOTIFICATIONS,
        onNavigate = onNavigate
    ) {
        ScrollableTabRow(
            selectedTabIndex = listOf("all", "unread", "read").indexOf(activeTab).coerceAtLeast(0),
            containerColor = Color.Transparent,
            contentColor = CamsNavy,
            edgePadding = 0.dp,
            divider = {}
        ) {
            val tabs = listOf("all" to "All", "unread" to "Unread", "read" to "Read")
            tabs.forEach { (id, label) ->
                Tab(
                    selected = activeTab == id,
                    onClick = { activeTab = id },
                    text = { Text(label, fontWeight = if (activeTab == id) FontWeight.Bold else FontWeight.Medium) }
                )
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        val visible = when (activeTab) {
            "unread" -> uiState.notifications.filter { !it.isRead }
            "read" -> uiState.notifications.filter { it.isRead }
            else -> uiState.notifications
        }

        if (uiState.unreadCount > 0) {
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                Text("${uiState.unreadCount} unread", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = Color(0xFF7C3AED))
                TextButton(onClick = { viewModel.markAllNotificationsRead() }) { Text("Mark all read", fontSize = 12.sp) }
            }
            Spacer(modifier = Modifier.height(8.dp))
        }

        uiState.error?.let {
            Text(it, color = Color(0xFFB91C1C), fontSize = 13.sp, modifier = Modifier.padding(bottom = 8.dp))
        }

        CamsCard(modifier = Modifier.fillMaxWidth().weight(1f)) {
            if (uiState.isLoading) {
                Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) { CircularProgressIndicator() }
            } else if (visible.isEmpty()) {
                Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Text(
                        when (activeTab) {
                            "unread" -> "No unread notifications"
                            "read" -> "No read notifications"
                            else -> "No notifications yet"
                        },
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            } else {
                LazyColumn(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    items(visible, key = { it.id }) { notif ->
                        val isUnread = !notif.isRead
                        Row(
                            modifier = Modifier.fillMaxWidth()
                                .background(if (isUnread) MaterialTheme.colorScheme.secondaryContainer else MaterialTheme.colorScheme.surface, RoundedCornerShape(12.dp))
                                .border(1.dp, MaterialTheme.colorScheme.outlineVariant, RoundedCornerShape(12.dp))
                                .padding(16.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Box(Modifier.background(if (isUnread) Color(0xFFEDE9FE) else MaterialTheme.colorScheme.surfaceVariant, RoundedCornerShape(8.dp)).padding(8.dp)) {
                                Icon(Icons.Filled.Notifications, null, tint = if (isUnread) Color(0xFF7C3AED) else CamsTextSecondary)
                            }
                            Spacer(Modifier.width(16.dp))
                            Column(modifier = Modifier.weight(1f)) {
                                notif.title?.takeIf { it.isNotBlank() }?.let { t ->
                                    Text(t, fontWeight = FontWeight.Bold, fontSize = 14.sp, color = MaterialTheme.colorScheme.onSurface)
                                    Spacer(Modifier.height(4.dp))
                                }
                                Text(notif.message, fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                Spacer(Modifier.height(4.dp))
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(Icons.Filled.AccessTime, null, modifier = Modifier.size(10.dp), tint = MaterialTheme.colorScheme.onSurfaceVariant)
                                    Spacer(Modifier.width(4.dp))
                                    Text(notif.date ?: notif.createdAt ?: "", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                }
                            }
                            if (isUnread) {
                                IconButton(onClick = { viewModel.markNotificationRead(notif.id) }) {
                                    Icon(Icons.Filled.DoneAll, contentDescription = "Mark read", tint = Color(0xFF7C3AED), modifier = Modifier.size(18.dp))
                                }
                            } else {
                                IconButton(onClick = { viewModel.deleteNotification(notif.id) }) {
                                    Icon(Icons.Filled.Delete, contentDescription = "Delete notification", tint = Color(0xFF94A3B8), modifier = Modifier.size(18.dp))
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}
