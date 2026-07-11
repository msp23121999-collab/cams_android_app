package com.example.features.hod.screens

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
import com.example.features.hod.widgets.HODBaseScreen
import com.example.core.navigation.AppRoutes

@Composable
fun HODFacultyWorkloadsScreen(onNavigate: (String) -> Unit) {
    HODBaseScreen(
        title = "Faculty Workload Monitor",
        subtitle = "Teaching periods derived live from timetable",
        currentRoute = AppRoutes.HOD_FACULTY_WORKLOADS,
        onNavigate = onNavigate
    ) {
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.fillMaxWidth()) {
            KpiCard("Total Faculty", "24", Icons.Filled.Groups, Color(0xFF64748B), Modifier.weight(1f))
            KpiCard("Assigned", "20", Icons.Filled.CheckCircle, Color(0xFF10B981), Modifier.weight(1f))
            KpiCard("Unassigned", "4", Icons.Filled.Warning, Color(0xFFEF4444), Modifier.weight(1f))
        }

        Spacer(Modifier.height(16.dp))

        CamsCard(modifier = Modifier.fillMaxWidth().weight(1f)) {
            Text("Workload Distribution", fontWeight = FontWeight.Bold, fontSize = 16.sp, color = CamsTextPrimary)
            Spacer(Modifier.height(12.dp))
            LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                items(10) { i ->
                    val periods = if (i % 3 == 0) 18 else if (i % 3 == 1) 14 else 8
                    val status = if (periods > 16) "OVERLOADED" else if (periods >= 12) "BALANCED" else "UNDERLOADED"
                    val color = if (periods > 16) Color(0xFFEF4444) else if (periods >= 12) Color(0xFF10B981) else Color(0xFFF59E0B)
                    
                    Row(
                        modifier = Modifier.fillMaxWidth().background(MaterialTheme.colorScheme.background, RoundedCornerShape(12.dp)).border(1.dp, MaterialTheme.colorScheme.surfaceVariant, RoundedCornerShape(12.dp)).padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(Modifier.size(36.dp).background(color.copy(alpha=0.1f), CircleShape), contentAlignment = Alignment.Center) {
                            Text("F${i+1}", fontWeight = FontWeight.Bold, color = color, fontSize = 12.sp)
                        }
                        Spacer(Modifier.width(12.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Text("Faculty Member ${i+1}", fontWeight = FontWeight.Bold, fontSize = 13.sp, color = CamsTextPrimary)
                            Text("fac${i+1}@example.com", fontSize = 13.sp, color = CamsTextSecondary)
                        }
                        Column(horizontalAlignment = Alignment.End) {
                            Text("$periods Periods", fontWeight = FontWeight.Black, fontSize = 13.sp, color = CamsTextPrimary)
                            Spacer(Modifier.height(4.dp))
                            Text(status, fontSize = 13.sp, fontWeight = FontWeight.Black, color = color, modifier = Modifier.background(color.copy(alpha=0.1f), RoundedCornerShape(4.dp)).padding(horizontal = 6.dp, vertical = 2.dp))
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
            Text(value, fontSize = 20.sp, fontWeight = FontWeight.Black, color = CamsTextPrimary, modifier = Modifier.padding(top = 8.dp))
        }
    }
}
