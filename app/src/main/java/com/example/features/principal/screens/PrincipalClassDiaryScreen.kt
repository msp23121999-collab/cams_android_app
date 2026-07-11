package com.example.features.principal.screens

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
import com.example.features.principal.widgets.PrincipalBaseScreen
import com.example.core.navigation.AppRoutes

@Composable
fun PrincipalClassDiaryScreen(onNavigate: (String) -> Unit) {
    var activeTab by remember { mutableStateOf("overview") }

    PrincipalBaseScreen(
        title = "Class Diary & Academic Audit",
        subtitle = "College-wide teaching logs review and real-time attendance audits.",
        currentRoute = AppRoutes.PRINCIPAL_CLASS_DIARY,
        onNavigate = onNavigate
    ) {
        ScrollableTabRow(
            selectedTabIndex = listOf("overview", "logs", "attendance", "alerts").indexOf(activeTab).coerceAtLeast(0),
            containerColor = Color.Transparent,
            contentColor = CamsNavy,
            edgePadding = 0.dp,
            divider = {}
        ) {
            val tabs = listOf(
                "overview" to "Overview Console",
                "logs" to "Class Logs Feed",
                "attendance" to "Attendance Monitor",
                "alerts" to "Academic Alerts"
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

        if (activeTab == "overview") {
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.fillMaxWidth()) {
                KpiCard("Syllabus Completed", "85%", Icons.Filled.TrendingUp, Color(0xFF6366F1), Modifier.weight(1f))
                KpiCard("Classes Conducted", "1,240", Icons.Filled.Book, Color(0xFF10B981), Modifier.weight(1f))
                KpiCard("Active Faculty", "185", Icons.Filled.People, Color(0xFF8B5CF6), Modifier.weight(1f))
                KpiCard("College Attendance", "92%", Icons.Filled.CheckCircle, Color(0xFF10B981), Modifier.weight(1f))
            }
            
            Spacer(modifier = Modifier.height(16.dp))

            CamsCard(modifier = Modifier.fillMaxWidth().weight(1f)) {
                Text("Program Completion Ratios", fontWeight = FontWeight.Bold, fontSize = 16.sp, color = CamsTextPrimary)
                Spacer(Modifier.height(12.dp))
                LazyColumn(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    items(4) { i ->
                        Row(
                            modifier = Modifier.fillMaxWidth().background(MaterialTheme.colorScheme.surface, RoundedCornerShape(12.dp)).border(1.dp, MaterialTheme.colorScheme.outlineVariant, RoundedCornerShape(12.dp)).padding(12.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text("Department ${i+1}", fontWeight = FontWeight.Bold, fontSize = 14.sp, color = CamsTextPrimary)
                                Spacer(Modifier.height(4.dp))
                                LinearProgressIndicator(
                                    progress = 0.8f, 
                                    modifier = Modifier.fillMaxWidth().height(8.dp), 
                                    color = Color(0xFF4F46E5), 
                                    trackColor = MaterialTheme.colorScheme.outlineVariant
                                )
                                Spacer(Modifier.height(4.dp))
                                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                    Text("TOTAL LECTURES: 310", fontSize = 13.sp, color = CamsTextSecondary)
                                    Text("ACTIVE FACULTY: 45", fontSize = 13.sp, color = CamsTextSecondary)
                                }
                            }
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
