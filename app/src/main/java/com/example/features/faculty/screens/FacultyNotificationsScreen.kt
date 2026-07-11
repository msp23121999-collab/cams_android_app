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

@Composable
fun FacultyNotificationsScreen(onNavigate: (String) -> Unit) {
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
            val notifications = listOf(
                Notification("Exam Duty Assigned", "You have been assigned as invigilator for CS101 on 20th Oct.", "2 hours ago", Icons.Filled.Assignment, Color(0xFF3B82F6)),
                Notification("New Research Grant", "Your proposal for DST-SERB has been approved for initial funding.", "1 day ago", Icons.Filled.Science, Color(0xFF10B981)),
                Notification("Meeting Reminder", "Academic council meeting starts in 30 minutes.", "30 mins ago", Icons.Filled.Schedule, Color(0xFFF59E0B)),
                Notification("Student Grievance", "A new grievance has been filed regarding internal marks by Zaid Khan.", "5 hours ago", Icons.Filled.Warning, Color(0xFFEF4444))
            )

            LazyColumn(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                items(notifications) { notification ->
                    NotificationItem(notification)
                }
            }
        }
    }
}

@Composable
private fun NotificationItem(notification: Notification) {
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
                    .background(notification.color.copy(alpha = 0.1f), CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Icon(notification.icon, null, tint = notification.color, modifier = Modifier.size(20.dp))
            }
            Column(modifier = Modifier.weight(1f)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(notification.title, fontWeight = FontWeight.Bold, color = CamsTextPrimary, fontSize = 15.sp)
                    Text(notification.time, fontSize = 13.sp, color = Color(0xFF64748B))
                }
                Spacer(modifier = Modifier.height(4.dp))
                Text(notification.message, fontSize = 13.sp, color = CamsTextSecondary, lineHeight = 18.sp)
            }
        }
    }
}

data class Notification(val title: String, val message: String, val time: String, val icon: ImageVector, val color: Color)
