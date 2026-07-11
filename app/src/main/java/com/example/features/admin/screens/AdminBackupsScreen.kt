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
import com.example.core.navigation.AppRoutes

@Composable
fun AdminBackupsScreen(onNavigate: (String) -> Unit) {
    var deepAuditEnabled by remember { mutableStateOf(true) }

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
            CamsCard {
                Column(Modifier.padding(16.dp)) {
                    Text("System Backup Status", fontWeight = FontWeight.Bold, fontSize = 16.sp, color = CamsTextPrimary)
                    Spacer(Modifier.height(12.dp))
                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                        Box(Modifier.size(48.dp).background(Color(0xFF10B981).copy(alpha = 0.1f), CircleShape), contentAlignment = Alignment.Center) {
                            Icon(Icons.Filled.CloudDone, null, tint = Color(0xFF10B981))
                        }
                        Column(modifier = Modifier.weight(1f)) {
                            Text("Auto-Backup is Active", fontWeight = FontWeight.Bold, fontSize = 14.sp, color = Color(0xFF10B981))
                            Text("Last Backup: 2 hours ago", fontSize = 12.sp, color = CamsTextSecondary)
                            Text("Next scheduled: 4 hours from now", fontSize = 12.sp, color = CamsTextSecondary)
                        }
                        Button(onClick = { }, colors = ButtonDefaults.buttonColors(containerColor = CamsNavy)) {
                            Text("Run Manual Backup", fontSize = 12.sp)
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
                        Text("Audit Log Configuration", fontWeight = FontWeight.Bold, fontSize = 16.sp, color = CamsTextPrimary)
                        Text("Record detailed user action tracking", fontSize = 12.sp, color = CamsTextSecondary)
                    }
                    Switch(
                        checked = deepAuditEnabled,
                        onCheckedChange = { deepAuditEnabled = it },
                        colors = SwitchDefaults.colors(checkedThumbColor = Color.White, checkedTrackColor = CamsNavy)
                    )
                }
            }

            Text("Backup History", fontWeight = FontWeight.Bold, fontSize = 16.sp, color = CamsTextPrimary)
            
            val backups = listOf(
                BackupRecord("Full Database Backup", "1.2 GB", "10 Oct 2023, 02:00 AM", "Success"),
                BackupRecord("Incremental Backup", "150 MB", "09 Oct 2023, 02:00 AM", "Success"),
                BackupRecord("Filesystem Archive", "4.5 GB", "08 Oct 2023, 02:00 AM", "Success")
            )
            
            LazyColumn(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                items(backups) { backup ->
                    CamsCard {
                        Row(
                            modifier = Modifier.fillMaxWidth().padding(16.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(backup.name, fontWeight = FontWeight.Bold, fontSize = 14.sp, color = CamsTextPrimary)
                                Spacer(Modifier.height(4.dp))
                                Row(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalAlignment = Alignment.CenterVertically) {
                                    Text("Size: ${backup.size}", fontSize = 12.sp, color = CamsNavy, fontWeight = FontWeight.Medium)
                                    Text("•", color = Color.LightGray)
                                    Text(backup.time, fontSize = 12.sp, color = CamsTextSecondary)
                                }
                            }
                            IconButton(onClick = {}) {
                                Icon(Icons.Filled.Restore, "Restore Backup", tint = CamsNavy)
                            }
                        }
                    }
                }
            }
        }
    }
}

data class BackupRecord(val name: String, val size: String, val time: String, val status: String)
