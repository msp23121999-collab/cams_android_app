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
import com.example.features.admin.providers.AdminFeesViewModel
import com.example.features.admin.providers.AdminFeesViewModelFactory
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
fun AdminFeeMgmtScreen(
    onNavigate: (String) -> Unit,
    viewModel: AdminFeesViewModel = viewModel(factory = AdminFeesViewModelFactory(AdminRepositoryImpl(CamsApplication.instance.container.apiService)))
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    AdminBaseScreen(
        title = "Fee Management",
        subtitle = "Overview of institutional fee collection",
        currentRoute = AppRoutes.ADMIN_FEE_MGMT,
        onNavigate = onNavigate
    ) {
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.fillMaxWidth()) {
            KpiCard("Total Collected", "₹45.2L", Icons.Filled.AccountBalanceWallet, Color(0xFF10B981), Modifier.weight(1f))
            KpiCard("Pending Dues", "₹12.5L", Icons.Filled.PendingActions, Color(0xFFF59E0B), Modifier.weight(1f))
            KpiCard("Defaulters", "45", Icons.Filled.Warning, Color(0xFFEF4444), Modifier.weight(1f))
        }
        
        Spacer(modifier = Modifier.height(16.dp))

        CamsCard(modifier = Modifier.fillMaxWidth().weight(1f)) {
            Text("Recent Transactions", fontWeight = FontWeight.Bold, fontSize = 16.sp, color = MaterialTheme.colorScheme.onSurface)
            Spacer(Modifier.height(12.dp))
            LazyColumn(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                items(uiState.feeStructures) { struct ->
                    val isSuccess = true
                    Row(
                        modifier = Modifier.fillMaxWidth().background(MaterialTheme.colorScheme.surface, RoundedCornerShape(12.dp)).border(1.dp, MaterialTheme.colorScheme.outlineVariant, RoundedCornerShape(12.dp)).padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(struct.feeType, fontWeight = FontWeight.Bold, fontSize = 14.sp, color = MaterialTheme.colorScheme.onSurface)
                            Text("${struct.deptName} • Sem ${struct.semester}", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                        Column(horizontalAlignment = Alignment.End) {
                            Text("₹${struct.amount}", fontWeight = FontWeight.Bold, fontSize = 14.sp, color = MaterialTheme.colorScheme.onSurface)
                            Spacer(Modifier.height(4.dp))
                            Text(
                                if(isSuccess) "SUCCESS" else "FAILED", 
                                fontSize = 13.sp, 
                                fontWeight = FontWeight.Bold, 
                                color = if(isSuccess) Color(0xFF047857) else Color(0xFFB91C1C), 
                                modifier = Modifier.background(if(isSuccess) Color(0xFFD1FAE5) else Color(0xFFFEF2F2), RoundedCornerShape(4.dp)).padding(horizontal = 6.dp, vertical = 2.dp)
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun KpiCard(label: String, value: String, icon: androidx.compose.ui.graphics.vector.ImageVector, color: Color, modifier: Modifier) {
    Card(modifier = modifier, colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface), elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)) {
        Column(modifier = Modifier.padding(12.dp)) {
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.Top) {
                Text(label, maxLines = 1, overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis,  fontSize = 13.sp, fontWeight = FontWeight.Black, color = Color(0xFF64748B), modifier = Modifier.weight(1f))
                Box(Modifier.background(color.copy(alpha=0.1f), RoundedCornerShape(8.dp)).padding(4.dp)) {
                    Icon(icon, null, tint = color, modifier = Modifier.size(16.dp))
                }
            }
            Text(value, fontSize = 20.sp, fontWeight = FontWeight.Black, color = MaterialTheme.colorScheme.onSurface, modifier = Modifier.padding(top = 8.dp))
        }
    }
}
