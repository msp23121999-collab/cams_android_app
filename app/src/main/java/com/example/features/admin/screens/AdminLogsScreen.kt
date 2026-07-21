package com.example.features.admin.screens

import androidx.lifecycle.compose.collectAsStateWithLifecycle
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
import androidx.compose.material3.MaterialTheme

import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.features.admin.providers.AdminLogsViewModel
import com.example.features.admin.providers.AdminLogsViewModelFactory
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

@Composable
fun AdminLogsScreen(
    onNavigate: (String) -> Unit,
    viewModel: AdminLogsViewModel = viewModel(factory = AdminLogsViewModelFactory(AdminRepositoryImpl(CamsApplication.instance.container.apiService)))
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

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

            val query = searchQuery.trim().lowercase()
            val logs = uiState.logs.filter {
                query.isBlank() ||
                    it.action.lowercase().contains(query) ||
                    it.userName.lowercase().contains(query)
            }

            uiState.error?.let {
                Text(it, color = Color(0xFFB91C1C), fontSize = 13.sp)
            }

            if (uiState.isLoading) {
                Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) { CircularProgressIndicator() }
            } else if (logs.isEmpty()) {
                Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Text(
                        if (uiState.logs.isEmpty()) "No activity logs recorded yet" else "No logs match \"$searchQuery\"",
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            } else {
                LazyColumn(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    items(logs, key = { it.id }) { log ->
                        LogCard(log)
                    }
                }
            }
        }
    }
}

@Composable
private fun LogCard(log: com.example.features.admin.models.AdminAuditLog) {
    // Severity is derived from the action text, since the backend audit log
    // records a free-form action string rather than an explicit level.
    val level = when {
        log.action.contains("fail", ignoreCase = true) || log.action.contains("error", ignoreCase = true) -> "Error"
        log.action.contains("delete", ignoreCase = true) || log.action.contains("restore", ignoreCase = true) -> "Warning"
        log.action.contains("success", ignoreCase = true) || log.action.contains("creat", ignoreCase = true) -> "Success"
        else -> "Info"
    }
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
                        when(level) {
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
                    when(level) {
                        "Error" -> Icons.Filled.Error
                        "Warning" -> Icons.Filled.Warning
                        "Success" -> Icons.Filled.CheckCircle
                        else -> Icons.Filled.Info
                    },
                    null,
                    tint = when(level) {
                        "Error" -> Color.Red
                        "Warning" -> Color(0xFFF59E0B)
                        "Success" -> Color(0xFF10B981)
                        else -> Color(0xFF3B82F6)
                    },
                    modifier = Modifier.size(20.dp)
                )
            }
            Column(modifier = Modifier.weight(1f)) {
                Text(log.action, fontWeight = FontWeight.Bold, fontSize = 14.sp, color = MaterialTheme.colorScheme.onSurface, maxLines = 2)
                Text("by ${log.userName}", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
            Text(log.timestamp.take(16).replace("T", " "), fontSize = 12.sp, color = Color(0xFF64748B))
        }
    }
}

data class AccessLog(val action: String, val details: String, val time: String, val level: String)
