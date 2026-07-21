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

import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.features.admin.providers.AdminBackupsViewModel
import com.example.core.repository.AdminRepositoryImpl
import com.example.core.network.ApiClient

@Composable
fun AdminBackupsScreen(
    onNavigate: (String) -> Unit,
    viewModel: AdminBackupsViewModel = viewModel(
        factory = object : androidx.lifecycle.ViewModelProvider.Factory {
            override fun <T : androidx.lifecycle.ViewModel> create(modelClass: Class<T>): T {
                return AdminBackupsViewModel(AdminRepositoryImpl(com.example.CamsApplication.instance.container.apiService)) as T
            }
        }
    )
) {
    val context = androidx.compose.ui.platform.LocalContext.current
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    var backupPendingRestore by remember { mutableStateOf<com.example.features.admin.models.AdminBackup?>(null) }
    var backupPendingDelete by remember { mutableStateOf<com.example.features.admin.models.AdminBackup?>(null) }

    LaunchedEffect(uiState.actionMessage, uiState.actionError) {
        uiState.actionMessage?.let {
            android.widget.Toast.makeText(context, it, android.widget.Toast.LENGTH_SHORT).show()
            viewModel.clearActionStatus()
        }
        uiState.actionError?.let {
            android.widget.Toast.makeText(context, it, android.widget.Toast.LENGTH_LONG).show()
            viewModel.clearActionStatus()
        }
    }

    AdminBaseScreen(
        title = "Audit & Backup Manager",
        currentRoute = AppRoutes.ADMIN_BACKUPS,
        onNavigate = onNavigate
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            val lastBackup = uiState.backups.firstOrNull()
            val autoOn = uiState.settings.autoBackupEnabled

            CamsCard {
                Column(Modifier.padding(16.dp)) {
                    Text("System Backup Status", fontWeight = FontWeight.Bold, fontSize = 16.sp, color = MaterialTheme.colorScheme.onSurface)
                    Spacer(Modifier.height(12.dp))
                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                        val statusColor = if (autoOn) Color(0xFF10B981) else Color(0xFF64748B)
                        Box(Modifier.size(48.dp).background(statusColor.copy(alpha = 0.1f), CircleShape), contentAlignment = Alignment.Center) {
                            Icon(if (autoOn) Icons.Filled.CloudDone else Icons.Filled.CloudOff, null, tint = statusColor)
                        }
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                if (autoOn) "Auto-Backup is Active" else "Auto-Backup is Off",
                                fontWeight = FontWeight.Bold, fontSize = 14.sp, color = statusColor
                            )
                            Text(
                                lastBackup?.let { "Last backup: ${it.createdAt.take(16).replace("T", " ")}" } ?: "No backups yet",
                                fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Text(
                                "Scheduled daily at ${uiState.settings.scheduleTime} • keeps ${uiState.settings.retentionCount}",
                                fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                        Button(
                            onClick = { viewModel.runBackupNow() },
                            enabled = !uiState.isRunning,
                            colors = ButtonDefaults.buttonColors(containerColor = CamsNavy)
                        ) {
                            Text(if (uiState.isRunning) "Working..." else "Run Manual Backup", fontSize = 12.sp)
                        }
                    }
                }
            }

            CamsCard {
                Row(
                    modifier = Modifier.fillMaxWidth().padding(16.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text("Automatic Backups", fontWeight = FontWeight.Bold, fontSize = 16.sp, color = MaterialTheme.colorScheme.onSurface)
                        Text("Run a scheduled backup every day", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                    Switch(
                        checked = autoOn,
                        onCheckedChange = { viewModel.setAutoBackup(it) },
                        colors = SwitchDefaults.colors(checkedThumbColor = Color.White, checkedTrackColor = CamsNavy)
                    )
                }
            }

            Text("Backup History", fontWeight = FontWeight.Bold, fontSize = 16.sp, color = MaterialTheme.colorScheme.onSurface)
            
            if (uiState.isLoading) {
                Box(modifier = Modifier.fillMaxWidth().height(200.dp), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator()
                }
            } else if (uiState.error != null) {
                // Without this the screen rendered empty on failure — no message,
                // no retry — indistinguishable from genuinely having no data.
                com.example.core.ui.NetworkErrorView(
                    message = uiState.error ?: "Failed to load backups",
                    onRetry = { viewModel.fetchBackups() }
                )
            } else {
                LazyColumn(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    items(uiState.backups) { backup ->
                        CamsCard {
                            Row(
                                modifier = Modifier.fillMaxWidth().padding(16.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(backup.filename, fontWeight = FontWeight.Bold, fontSize = 14.sp, color = MaterialTheme.colorScheme.onSurface)
                                    Spacer(Modifier.height(4.dp))
                                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalAlignment = Alignment.CenterVertically) {
                                        Text("Size: ${backup.sizeBytes / 1024 / 1024} MB", fontSize = 12.sp, color = CamsNavy, fontWeight = FontWeight.Medium)
                                        Text("•", color = Color.LightGray)
                                        Text(backup.createdAt.take(10), fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                    }
                                    Spacer(Modifier.height(4.dp))
                                    Text(backup.status, fontSize = 12.sp, color = if (backup.status.equals("COMPLETED", true)) Color(0xFF10B981) else Color.Red)
                                }
                                Row {
                                    IconButton(onClick = { backupPendingRestore = backup }, enabled = !uiState.isRunning) {
                                        Icon(Icons.Filled.Restore, "Restore Backup", tint = MaterialTheme.colorScheme.primary)
                                    }
                                    IconButton(onClick = { backupPendingDelete = backup }) {
                                        Icon(Icons.Filled.Delete, "Delete Backup", tint = Color(0xFFB91C1C))
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    backupPendingRestore?.let { backup ->
        AlertDialog(
            onDismissRequest = { backupPendingRestore = null },
            icon = { Icon(Icons.Filled.Warning, null, tint = Color(0xFFB45309)) },
            title = { Text("Restore this backup?") },
            text = {
                Text(
                    "This will overwrite current system data with the contents of \"${backup.filename}\" " +
                        "(${backup.createdAt.take(10)}). Any changes made since that backup will be lost. " +
                        "This cannot be undone."
                )
            },
            confirmButton = {
                TextButton(onClick = {
                    viewModel.restoreBackup(backup.id)
                    backupPendingRestore = null
                }) { Text("Restore", color = Color(0xFFB91C1C)) }
            },
            dismissButton = { TextButton(onClick = { backupPendingRestore = null }) { Text("Cancel") } }
        )
    }

    backupPendingDelete?.let { backup ->
        AlertDialog(
            onDismissRequest = { backupPendingDelete = null },
            title = { Text("Delete Backup") },
            text = { Text("Delete \"${backup.filename}\"? This cannot be undone.") },
            confirmButton = {
                TextButton(onClick = {
                    viewModel.deleteBackup(backup.id)
                    backupPendingDelete = null
                }) { Text("Delete", color = Color(0xFFB91C1C)) }
            },
            dismissButton = { TextButton(onClick = { backupPendingDelete = null }) { Text("Cancel") } }
        )
    }
}
