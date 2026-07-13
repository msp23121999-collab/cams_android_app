package com.example.features.faculty.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.core.theme.*
import com.example.features.faculty.widgets.FacultyBaseScreen

import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.core.repository.FacultyRepositoryImpl
import com.example.features.faculty.providers.FacultyNotificationsViewModel
import com.example.features.faculty.providers.FacultyNotificationsViewModelFactory
import com.example.core.network.ApiClient

@Composable
fun FacultyNotificationsScreen(onNavigate: (String) -> Unit) {
    val repository = remember { FacultyRepositoryImpl(com.example.CamsApplication.instance.container.apiService) }
    val factory = remember { FacultyNotificationsViewModelFactory(repository) }
    val viewModel: FacultyNotificationsViewModel = viewModel(factory = factory)
    val uiState by viewModel.uiState.collectAsState()
    FacultyBaseScreen(scrollable = false, 
        title = "Notifications",
        currentRoute = com.example.core.navigation.AppRoutes.FACULTY_NOTIFICATIONS,
        onNavigate = onNavigate
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp)
        ) {
            if (uiState.isLoading) {
                Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator(color = CamsNavy)
                }
            } else if (uiState.notifications.isEmpty()) {
                Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Text("No new notifications", color = CamsTextSecondary)
                }
            } else {
                LazyColumn(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    items(uiState.notifications) { notification ->
                        NotificationItem(notification)
                    }
                }
            }
        }
    }
}

@Composable
private fun NotificationItem(notification: com.example.core.network.NotificationDto) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        shape = RoundedCornerShape(16.dp)
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            horizontalArrangement = Arrangement.spacedBy(16.dp),
            verticalAlignment = Alignment.Top
        ) {
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .background(CamsNavy.copy(alpha = 0.1f), CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Icon(if (notification.isRead) Icons.Filled.Notifications else Icons.Filled.NotificationsActive, null, tint = CamsNavy, modifier = Modifier.size(20.dp))
            }
            Column(modifier = Modifier.weight(1f)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(notification.title, fontWeight = FontWeight.Bold, color = CamsTextPrimary, fontSize = 15.sp)
                    Text(notification.date.take(10), fontSize = 13.sp, color = Color(0xFF64748B))
                }
                Spacer(modifier = Modifier.height(4.dp))
                Text(notification.message, fontSize = 13.sp, color = CamsTextSecondary, lineHeight = 18.sp)
            }
        }
    }
}
