package com.example.features.admin.screens

import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.material3.MaterialTheme

import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.features.admin.providers.AdminSystemConfigViewModel
import com.example.features.admin.providers.AdminSystemConfigViewModelFactory
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
fun AdminSystemConfigScreen(
    onNavigate: (String) -> Unit,
    viewModel: AdminSystemConfigViewModel = viewModel(factory = AdminSystemConfigViewModelFactory(AdminRepositoryImpl(CamsApplication.instance.container.apiService)))
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    var maintenanceMode by remember { mutableStateOf(false) }
    var emailNotifications by remember { mutableStateOf(true) }
    var smsNotifications by remember { mutableStateOf(true) }

    AdminBaseScreen(
        title = "System Configuration",
        currentRoute = "/admin/system-config",
        onNavigate = onNavigate
    ) {
        LazyColumn(
            modifier = Modifier
                .fillMaxWidth().weight(1f)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            
            item {
                Text("Backend Settings", fontWeight = FontWeight.Bold, fontSize = 18.sp, color = MaterialTheme.colorScheme.onSurface)
            }
            item {
                CamsCard {
                    Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                            Text("Institution Name", fontWeight = FontWeight.Medium, color = MaterialTheme.colorScheme.onSurface)
                            Text(uiState.settings?.institutionName ?: "N/A", color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                        HorizontalDivider(color = Color(0xFFF3F4F6))
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                            Text("Academic Year", fontWeight = FontWeight.Medium, color = MaterialTheme.colorScheme.onSurface)
                            Text(uiState.settings?.academicYear ?: "N/A", color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                        HorizontalDivider(color = Color(0xFFF3F4F6))
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                            Text("Current Semester", fontWeight = FontWeight.Medium, color = MaterialTheme.colorScheme.onSurface)
                            Text(uiState.settings?.semester?.toString() ?: "N/A", color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                    }
                }
            }

            item {
                Text("Global Settings", fontWeight = FontWeight.Bold, fontSize = 18.sp, color = MaterialTheme.colorScheme.onSurface)
            }

            item {
                CamsCard {
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                        Column {
                            Text("Maintenance Mode", fontWeight = FontWeight.Bold, fontSize = 16.sp, color = MaterialTheme.colorScheme.onSurface)
                            Text("Disables access for non-admin users", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                        Switch(
                            checked = maintenanceMode,
                            onCheckedChange = { maintenanceMode = it },
                            colors = SwitchDefaults.colors(checkedThumbColor = CamsNavy, checkedTrackColor = CamsNavy.copy(alpha = 0.5f))
                        )
                    }
                }
            }

            item {
                Text("Notification Preferences", fontWeight = FontWeight.Bold, fontSize = 18.sp, color = MaterialTheme.colorScheme.onSurface)
            }

            item {
                CamsCard {
                    Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                            Text("Email Notifications", fontWeight = FontWeight.Medium, color = MaterialTheme.colorScheme.onSurface)
                            Switch(checked = emailNotifications, onCheckedChange = { emailNotifications = it })
                        }
                        HorizontalDivider(color = Color(0xFFF3F4F6))
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                            Text("SMS Notifications", fontWeight = FontWeight.Medium, color = MaterialTheme.colorScheme.onSurface)
                            Switch(checked = smsNotifications, onCheckedChange = { smsNotifications = it })
                        }
                    }
                }
            }

            item {
                Text("Integrations", fontWeight = FontWeight.Bold, fontSize = 18.sp, color = MaterialTheme.colorScheme.onSurface)
            }

            item {
                CamsCard(onClick = {}) {
                    Row(horizontalArrangement = Arrangement.SpaceBetween, modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                        Row(horizontalArrangement = Arrangement.spacedBy(12.dp), verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Filled.VpnKey, null, tint = MaterialTheme.colorScheme.primary)
                            Text("Manage API Keys", fontWeight = FontWeight.Medium, color = MaterialTheme.colorScheme.onSurface)
                        }
                        Icon(Icons.Filled.ChevronRight, null, tint = Color(0xFF64748B))
                    }
                }
            }
        }
    }
}
