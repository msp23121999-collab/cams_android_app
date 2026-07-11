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

@Composable
fun HODReportsAnalyticsScreen(onNavigate: (String) -> Unit) {
    var activeTab by remember { mutableStateOf("department") }

    HODBaseScreen(
        title = "Reports & Analytics",
        subtitle = "Generate official reports for review boards, academic councils, or department archives.",
        currentRoute = "/hod/reports",
        onNavigate = onNavigate
    ) {
        ScrollableTabRow(
            selectedTabIndex = listOf("department", "faculty", "students", "academic", "research", "export").indexOf(activeTab).coerceAtLeast(0),
            containerColor = Color.Transparent,
            contentColor = CamsNavy,
            edgePadding = 0.dp,
            divider = {}
        ) {
            val tabs = listOf(
                "department" to "Department Reports",
                "faculty" to "Faculty Reports",
                "students" to "Student Reports",
                "academic" to "Academic Analytics",
                "research" to "Research Analytics",
                "export" to "Export Reports"
            )
            tabs.forEach { (id, label) ->
                Tab(
                    selected = activeTab == id,
                    onClick = { activeTab = id },
                    text = { Text(label, fontWeight = if (activeTab == id) FontWeight.Bold else FontWeight.Medium) }
                )
            }
        }
        
        Spacer(modifier = Modifier.height(16.dp))
        
        if (activeTab == "department") {
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.fillMaxWidth()) {
                KpiCard("Total Faculty", "24", Icons.Filled.People, Color(0xFF64748B), Modifier.weight(1f))
                KpiCard("Avg Workload", "16 hrs", Icons.Filled.AccessTime, Color(0xFF8B5CF6), Modifier.weight(1f))
                KpiCard("Substitution Coverage", "95%", Icons.Filled.PublishedWithChanges, Color(0xFF10B981), Modifier.weight(1f))
                KpiCard("Syllabus Coverage", "82%", Icons.Filled.MenuBook, Color(0xFFF59E0B), Modifier.weight(1f))
            }
            
            Spacer(modifier = Modifier.height(16.dp))

            CamsCard(modifier = Modifier.fillMaxWidth().weight(1f)) {
                Text("Faculty Workload & Performance Summary", fontWeight = FontWeight.Bold, fontSize = 16.sp, color = CamsTextPrimary)
                Spacer(Modifier.height(12.dp))
                LazyColumn(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    items(5) { i ->
                        Row(
                            modifier = Modifier.fillMaxWidth().background(MaterialTheme.colorScheme.surface, RoundedCornerShape(12.dp)).border(1.dp, MaterialTheme.colorScheme.outlineVariant, RoundedCornerShape(12.dp)).padding(12.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text("Prof. Name ${i+1}", fontWeight = FontWeight.Bold, fontSize = 14.sp, color = CamsTextPrimary)
                                Text("prof${i+1}@example.com", fontSize = 12.sp, color = CamsTextSecondary)
                            }
                            Text("14 Hrs / Week", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = CamsTextPrimary)
                            Spacer(Modifier.width(16.dp))
                            Text("5 Verified", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = Color(0xFF059669), modifier = Modifier.background(Color(0xFFD1FAE5), RoundedCornerShape(4.dp)).padding(horizontal = 8.dp, vertical = 4.dp))
                        }
                    }
                }
            }
        } else {
            CamsCard(modifier = Modifier.fillMaxWidth().weight(1f)) {
                Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Text("$activeTab content goes here", color = CamsTextSecondary)
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
