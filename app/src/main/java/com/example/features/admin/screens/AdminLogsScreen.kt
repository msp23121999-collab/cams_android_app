package com.example.features.admin.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
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

@Composable
fun AdminLogsScreen(onNavigate: (String) -> Unit) {
    var searchQuery by remember { mutableStateOf("") }

    AdminBaseScreen(
        title = "Access Logs",
        currentRoute = "/admin/logs",
        onNavigate = onNavigate
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            OutlinedTextField(
                value = searchQuery,
                onValueChange = { searchQuery = it },
                placeholder = { Text("Search IP, action, or user...") },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                leadingIcon = { Icon(Icons.Filled.Search, null) }
            )

            val logs = listOf(
                AccessLog("Failed Login Attempt", "IP: 192.168.1.45", "10 Oct, 10:30 AM", "Error"),
                AccessLog("User Data Export", "User: admin@cams.edu", "10 Oct, 09:15 AM", "Info"),
                AccessLog("Settings Updated", "Maintenance Mode: ON", "09 Oct, 11:00 PM", "Warning"),
                AccessLog("Successful Login", "User: principal@cams.edu", "09 Oct, 08:30 AM", "Success")
            )

            LazyColumn(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                items(logs) { log ->
                    LogCard(log)
                }
            }
        }
    }
}

@Composable
private fun LogCard(log: AccessLog) {
    CamsCard {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .background(
                        when(log.level) {
                            "Error" -> Color.Red.copy(alpha = 0.1f)
                            "Warning" -> Color(0xFFF59E0B).copy(alpha = 0.1f)
                            "Success" -> Color(0xFF10B981).copy(alpha = 0.1f)
                            else -> Color(0xFF3B82F6).copy(alpha = 0.1f)
                        },
                        CircleShape
                    ),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    when(log.level) {
                        "Error" -> Icons.Filled.Error
                        "Warning" -> Icons.Filled.Warning
                        "Success" -> Icons.Filled.CheckCircle
                        else -> Icons.Filled.Info
                    },
                    null,
                    tint = when(log.level) {
                        "Error" -> Color.Red
                        "Warning" -> Color(0xFFF59E0B)
                        "Success" -> Color(0xFF10B981)
                        else -> Color(0xFF3B82F6)
                    },
                    modifier = Modifier.size(20.dp)
                )
            }
            Column(modifier = Modifier.weight(1f)) {
                Text(log.action, fontWeight = FontWeight.Bold, fontSize = 14.sp, color = CamsTextPrimary)
                Text(log.details, fontSize = 12.sp, color = CamsTextSecondary)
            }
            Text(log.time, fontSize = 12.sp, color = Color(0xFF64748B))
        }
    }
}

data class AccessLog(val action: String, val details: String, val time: String, val level: String)
