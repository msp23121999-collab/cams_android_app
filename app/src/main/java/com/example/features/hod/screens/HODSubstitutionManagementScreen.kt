package com.example.features.hod.screens

import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
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
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.core.theme.*
import com.example.core.ui.CamsCard
import com.example.features.hod.widgets.HODBaseScreen
import com.example.core.navigation.AppRoutes
import com.example.features.hod.providers.HODSubstitutionViewModel
import com.example.core.network.HODSubstitutionDto

@Composable
fun HODSubstitutionManagementScreen(
    onNavigate: (String) -> Unit,
    viewModel: HODSubstitutionViewModel = viewModel()
) {
    var activeTab by remember { mutableStateOf("allocation") }
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    
    val pendingOrAllocated = uiState.substitutions.filter { it.status == "PENDING" || it.status == "ALLOCATED" }
    val completed = uiState.substitutions.filter { it.status == "COMPLETED" }

    HODBaseScreen(
        title = "Substitution Management",
        subtitle = "Manage substitute allocations & track completion",
        currentRoute = AppRoutes.HOD_SUBSTITUTION_MGMT,
        onNavigate = onNavigate,
        floatingActionButton = {
            if (activeTab == "allocation") {
                FloatingActionButton(onClick = { /* Add Sub */ }, containerColor = Color(0xFF6D28D9)) {
                    Icon(Icons.Filled.Add, "Add Substitution", tint = Color.White)
                }
            }
        }
    ) {
        // KPI Cards
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.fillMaxWidth()) {
            KpiCard("Total Subs", "${uiState.substitutions.size}", Icons.Filled.Groups, Color(0xFF64748B), Modifier.weight(1f))
            KpiCard("Pending", "${uiState.substitutions.count { it.status == "PENDING" }}", Icons.Filled.Schedule, Color(0xFFD97706), Modifier.weight(1f))
            KpiCard("Completed", "${completed.size}", Icons.Filled.CheckCircle, Color(0xFF059669), Modifier.weight(1f))
        }

        Spacer(Modifier.height(16.dp))

        // Tabs
        Row(Modifier.fillMaxWidth().background(MaterialTheme.colorScheme.surface, RoundedCornerShape(12.dp)).padding(4.dp)) {
            TabButton("Substitute Allocation", activeTab == "allocation", Modifier.weight(1f)) { activeTab = "allocation" }
            TabButton("Completion Tracking", activeTab == "tracking", Modifier.weight(1f)) { activeTab = "tracking" }
        }

        Spacer(Modifier.height(16.dp))
        
        CamsCard(modifier = Modifier.fillMaxWidth().weight(1f)) {
            if (uiState.isLoading) {
                Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator()
                }
            } else if (activeTab == "allocation") {
                Text("Substitute Allocation Register", fontWeight = FontWeight.Bold, fontSize = 16.sp, color = MaterialTheme.colorScheme.onSurface)
                Spacer(Modifier.height(12.dp))
                if (pendingOrAllocated.isEmpty()) {
                    Text("No pending allocations.", color = MaterialTheme.colorScheme.onSurfaceVariant)
                } else {
                    LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        items(pendingOrAllocated) { sub ->
                            SubItem(sub)
                        }
                    }
                }
            } else {
                Text("Completion Tracker", fontWeight = FontWeight.Bold, fontSize = 16.sp, color = MaterialTheme.colorScheme.onSurface)
                Spacer(Modifier.height(12.dp))
                if (completed.isEmpty()) {
                    Text("No completed substitutions.", color = MaterialTheme.colorScheme.onSurfaceVariant)
                } else {
                    LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        items(completed) { sub ->
                            SubItem(sub)
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
                Text(label, fontSize = 13.sp, fontWeight = FontWeight.Black, color = Color(0xFF64748B), modifier = Modifier.weight(1f))
                Box(Modifier.background(color.copy(alpha=0.1f), RoundedCornerShape(8.dp)).padding(4.dp)) {
                    Icon(icon, null, tint = color, modifier = Modifier.size(16.dp))
                }
            }
            Text(value, fontSize = 20.sp, fontWeight = FontWeight.Black, color = MaterialTheme.colorScheme.onSurface, modifier = Modifier.padding(top = 8.dp))
        }
    }
}

@Composable
private fun TabButton(text: String, selected: Boolean, modifier: Modifier, onClick: () -> Unit) {
    Button(
        onClick = onClick,
        modifier = modifier,
        colors = ButtonDefaults.buttonColors(
            containerColor = if (selected) Color(0xFFF3F4F6) else Color.Transparent,
            contentColor = if (selected) Color(0xFF4338CA) else Color(0xFF64748B)
        ),
        shape = RoundedCornerShape(8.dp),
        elevation = null
    ) {
        Text(text, fontSize = 13.sp, fontWeight = FontWeight.Bold)
    }
}

@Composable
private fun SubItem(sub: HODSubstitutionDto) {
    val color = when(sub.status) {
        "PENDING" -> Color(0xFFD97706)
        "ALLOCATED" -> Color(0xFF2563EB)
        "COMPLETED" -> Color(0xFF059669)
        else -> Color(0xFF64748B)
    }
    Row(
        modifier = Modifier.fillMaxWidth().background(MaterialTheme.colorScheme.background, RoundedCornerShape(12.dp)).border(1.dp, MaterialTheme.colorScheme.surfaceVariant, RoundedCornerShape(12.dp)).padding(12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(sub.subject, fontWeight = FontWeight.Bold, fontSize = 13.sp, color = MaterialTheme.colorScheme.onSurface)
            Spacer(Modifier.height(4.dp))
            Text("Absent: ${sub.absent_faculty}", fontSize = 13.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
            Text("Sub: ${sub.substitute_faculty}", fontSize = 13.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
        Column(horizontalAlignment = Alignment.End) {
            Text(sub.date, fontSize = 12.sp, color = Color(0xFF64748B), fontWeight = FontWeight.Bold)
            Spacer(Modifier.height(8.dp))
            Text(sub.status, fontSize = 13.sp, fontWeight = FontWeight.Black, color = color, modifier = Modifier.background(color.copy(alpha=0.1f), RoundedCornerShape(4.dp)).padding(horizontal = 6.dp, vertical = 2.dp))
        }
    }
}
