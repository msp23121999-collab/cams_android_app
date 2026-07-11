package com.example.features.admin.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.*
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
fun AdminNotificationsScreen(onNavigate: (String) -> Unit) {
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
            val tabs = listOf(
                "all" to "All (24)",
                "unread" to "Unread (3)",
                "read" to "Read (21)"
            )
            tabs.forEach { (id, label) ->
                Tab(
                    selected = activeTab == id,
                    onClick = { activeTab = id },
                    text = { Text(label, fontWeight = if (activeTab == id) FontWeight.Bold else FontWeight.Medium) }
                )
            }
        }
        
        Spacer(modifier = Modifier.height(16.dp))

        CamsCard(modifier = Modifier.fillMaxWidth().weight(1f)) {
            LazyColumn(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                items(10) { i ->
                    val isUnread = i < 3
                    Row(
                        modifier = Modifier.fillMaxWidth().background(if(isUnread) MaterialTheme.colorScheme.secondaryContainer else Color.White, RoundedCornerShape(12.dp)).border(1.dp, MaterialTheme.colorScheme.outlineVariant, RoundedCornerShape(12.dp)).padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(Modifier.background(if(isUnread) Color(0xFFEDE9FE) else MaterialTheme.colorScheme.surfaceVariant, RoundedCornerShape(8.dp)).padding(8.dp)) {
                            Icon(Icons.Filled.Notifications, null, tint = if(isUnread) Color(0xFF7C3AED) else CamsTextSecondary)
                        }
                        Spacer(Modifier.width(16.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Text("System Alert: Maintenance window scheduled", fontWeight = FontWeight.Bold, fontSize = 14.sp, color = CamsTextPrimary)
                            Spacer(Modifier.height(4.dp))
                            Text("The system will be undergoing maintenance on Sunday at 2 AM.", fontSize = 12.sp, color = CamsTextSecondary)
                            Spacer(Modifier.height(4.dp))
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.Filled.AccessTime, null, modifier = Modifier.size(10.dp), tint = CamsTextSecondary)
                                Spacer(Modifier.width(4.dp))
                                Text("2 hours ago", fontSize = 12.sp, color = CamsTextSecondary)
                            }
                        }
                        if (isUnread) {
                            Box(Modifier.size(8.dp).background(Color(0xFF7C3AED), CircleShape))
                        }
                    }
                }
            }
        }
    }
}
