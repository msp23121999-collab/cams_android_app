package com.example.features.admin.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.*
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.features.admin.providers.AdminViewModel
import com.example.features.admin.models.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.core.theme.*
import com.example.features.admin.widgets.AdminBaseScreen
import com.example.core.navigation.AppRoutes

@Composable
fun AdminDashboardScreen(
    onNavigate: (String) -> Unit,
    viewModel: AdminViewModel = viewModel()
) {
    val uiState by viewModel.uiState.collectAsState()

    AdminBaseScreen(
        title = "Admin Portal Dashboard",
        subtitle = "Manage academic configurations, fee trackers, and user enrollment benchmarks.",
        currentRoute = AppRoutes.ADMIN_DASHBOARD,
        onNavigate = onNavigate
    ) {
        Text("Campus Overview", fontWeight = FontWeight.Bold, fontSize = 14.sp, color = CamsTextSecondary)
        Spacer(Modifier.height(8.dp))
        Row(horizontalArrangement = Arrangement.spacedBy(12.dp), modifier = Modifier.fillMaxWidth()) {
            DashboardStatCard("Total Users", uiState.metrics.totalUsers, Icons.Filled.Groups, Color(0xFF3B82F6), Modifier.weight(1f), onClick = { onNavigate(AppRoutes.ADMIN_USER_MGMT) })
            DashboardStatCard("System Health", "${uiState.systemHealth.map { it.health }.average().toInt()}%", Icons.Filled.Dns, Color(0xFF10B981), Modifier.weight(1f), onClick = {  })
        }
        Spacer(Modifier.height(12.dp))
        Row(horizontalArrangement = Arrangement.spacedBy(12.dp), modifier = Modifier.fillMaxWidth()) {
            DashboardStatCard("Active Batches", uiState.metrics.activeBatches, Icons.Filled.Layers, Color(0xFFF59E0B), Modifier.weight(1f), onClick = { onNavigate(AppRoutes.ADMIN_BATCH_SETUP) })
            DashboardStatCard("Fee Collection", uiState.metrics.collectionToday, Icons.Filled.AccountBalanceWallet, Color(0xFF8B5CF6), Modifier.weight(1f), onClick = { onNavigate(AppRoutes.ADMIN_COLLECT_FEE) })
        }
                
        Spacer(Modifier.height(24.dp))
                
        Text("Administrative Actions", fontWeight = FontWeight.Bold, fontSize = 14.sp, color = CamsTextSecondary)
        Spacer(Modifier.height(8.dp))
                
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.fillMaxWidth()) {
            QuickLinkItem("Academic Setup", Icons.Filled.CollectionsBookmark, { onNavigate(AppRoutes.ADMIN_ACADEMIC_CATALOG) }, Modifier.weight(1f))
            QuickLinkItem("Batch Config", Icons.Filled.Layers, { onNavigate(AppRoutes.ADMIN_BATCH_SETUP) }, Modifier.weight(1f))
        }
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.fillMaxWidth().padding(top = 8.dp)) {
            QuickLinkItem("Attendance Defaulters", Icons.Filled.WarningAmber, { onNavigate(AppRoutes.ADMIN_ATTENDANCE_DEFAULTERS) }, Modifier.weight(1f))
            QuickLinkItem("Collect Fee", Icons.Filled.Payment, { onNavigate(AppRoutes.ADMIN_COLLECT_FEE) }, Modifier.weight(1f))
        }
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.fillMaxWidth().padding(top = 8.dp)) {
            QuickLinkItem("Year Config", Icons.Filled.DateRange, { onNavigate(AppRoutes.ADMIN_ACADEMIC_YEAR_CONFIG) }, Modifier.weight(1f))
            QuickLinkItem("System Backups", Icons.Filled.Backup, { onNavigate(AppRoutes.ADMIN_BACKUPS) }, Modifier.weight(1f))
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun DashboardStatCard(label: String, value: String, icon: androidx.compose.ui.graphics.vector.ImageVector, color: Color, modifier: Modifier, onClick: () -> Unit) {
    Card(
        modifier = modifier, 
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface), 
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        onClick = onClick
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(horizontalArrangement = Arrangement.SpaceBetween, modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                Text(label, maxLines = 1, overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis,  fontSize = 12.sp, fontWeight = FontWeight.ExtraBold, color = CamsTextSecondary, modifier = Modifier.weight(1f))
                Box(Modifier.background(color.copy(alpha=0.1f), RoundedCornerShape(8.dp)).padding(8.dp)) {
                    Icon(icon, null, tint = color, modifier = Modifier.size(20.dp))
                }
            }
            Spacer(Modifier.height(16.dp))
            Text(value, fontSize = 28.sp, fontWeight = FontWeight.Black, color = CamsTextPrimary)
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun QuickLinkItem(label: String, icon: androidx.compose.ui.graphics.vector.ImageVector, onClick: () -> Unit, modifier: Modifier) {
    Card(
        modifier = modifier, 
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface), 
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
        onClick = onClick
    ) {
        Row(modifier = Modifier.padding(16.dp).fillMaxWidth(), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            Icon(icon, null, tint = CamsNavy, modifier = Modifier.size(20.dp))
            Text(label, maxLines = 1, overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis,  fontSize = 13.sp, fontWeight = FontWeight.Bold, color = CamsTextPrimary)
        }
    }
}
