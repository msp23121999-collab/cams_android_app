package com.example.features.hod.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
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
import com.example.features.hod.widgets.HODBaseScreen
import com.example.core.navigation.AppRoutes

@Composable
fun HODSubstitutionManagementScreen(onNavigate: (String) -> Unit) {
    var activeTab by remember { mutableStateOf("allocation") }
    
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
            KpiCard("Total Subs", "42", Icons.Filled.Groups, Color(0xFF64748B), Modifier.weight(1f))
            KpiCard("Pending", "12", Icons.Filled.Schedule, Color(0xFFD97706), Modifier.weight(1f))
            KpiCard("Completed", "28", Icons.Filled.CheckCircle, Color(0xFF059669), Modifier.weight(1f))
        }

        Spacer(Modifier.height(16.dp))

        // Tabs
        Row(Modifier.fillMaxWidth().background(MaterialTheme.colorScheme.surface, RoundedCornerShape(12.dp)).padding(4.dp)) {
            TabButton("Substitute Allocation", activeTab == "allocation", Modifier.weight(1f)) { activeTab = "allocation" }
            TabButton("Completion Tracking", activeTab == "tracking", Modifier.weight(1f)) { activeTab = "tracking" }
        }

        Spacer(Modifier.height(16.dp))
        
        CamsCard(modifier = Modifier.fillMaxWidth()) {
            if (activeTab == "allocation") {
                Text("Substitute Allocation Register", fontWeight = FontWeight.Bold, fontSize = 16.sp, color = CamsTextPrimary)
                Spacer(Modifier.height(12.dp))
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    repeat(5) { i ->
                        SubItem(
                            absent = "Dr. John Doe",
                            substitute = "Prof. Jane Smith",
                            subject = "Constitutional Law",
                            date = "2026-07-07",
                            status = if (i % 2 == 0) "PENDING" else "ALLOCATED"
                        )
                    }
                }
            } else {
                Text("Completion Tracker", fontWeight = FontWeight.Bold, fontSize = 16.sp, color = CamsTextPrimary)
                Spacer(Modifier.height(12.dp))
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    repeat(4) { i ->
                        SubItem(
                            absent = "Dr. Alice Brown",
                            substitute = "Prof. Bob White",
                            subject = "Criminal Law",
                            date = "2026-07-05",
                            status = if (i == 0) "COMPLETED" else "ALLOCATED"
                        )
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
            Text(value, fontSize = 20.sp, fontWeight = FontWeight.Black, color = CamsTextPrimary, modifier = Modifier.padding(top = 8.dp))
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
private fun SubItem(absent: String, substitute: String, subject: String, date: String, status: String) {
    val color = when(status) {
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
            Text("$subject", fontWeight = FontWeight.Bold, fontSize = 13.sp, color = CamsTextPrimary)
            Spacer(Modifier.height(4.dp))
            Text("Absent: $absent", fontSize = 13.sp, color = CamsTextSecondary)
            Text("Sub: $substitute", fontSize = 13.sp, color = CamsTextSecondary)
        }
        Column(horizontalAlignment = Alignment.End) {
            Text(date, fontSize = 12.sp, color = Color(0xFF64748B), fontWeight = FontWeight.Bold)
            Spacer(Modifier.height(8.dp))
            Text(status, fontSize = 13.sp, fontWeight = FontWeight.Black, color = color, modifier = Modifier.background(color.copy(alpha=0.1f), RoundedCornerShape(4.dp)).padding(horizontal = 6.dp, vertical = 2.dp))
        }
    }
}
