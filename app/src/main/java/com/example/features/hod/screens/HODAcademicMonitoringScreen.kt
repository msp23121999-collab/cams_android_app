package com.example.features.hod.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
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
import com.example.core.navigation.AppRoutes


import com.example.core.theme.CamsTextPrimary
import com.example.core.theme.CamsTextSecondary
import com.example.features.hod.widgets.HODBaseScreen

@Composable
fun HODAcademicMonitoringScreen(onNavigate: (String) -> Unit) {
    var activeTab by remember { mutableStateOf("overview") }

    HODBaseScreen(scrollable = false, 
        title = "Academic Monitoring",
        subtitle = "Live academic health dashboard from Faculty Portal database",
        currentRoute = AppRoutes.HOD_ACADEMIC_PROGRESS,
        onNavigate = onNavigate
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.background)
                
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Tabs
            val tabs = listOf(
                "overview" to "Overview",
                "faculty" to "Faculty Progress",
                "subjects" to "Subject Tracker",
                "attendance" to "Attendance Monitor",
                "materials" to "Study Materials",
                "today" to "Today's Classes",
                "alerts" to "Academic Alerts"
            )

            LazyRow(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(MaterialTheme.colorScheme.surface, RoundedCornerShape(12.dp))
                    .border(1.dp, MaterialTheme.colorScheme.outlineVariant, RoundedCornerShape(12.dp))
                    .padding(4.dp),
                horizontalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                items(tabs) { tab ->
                    val isSelected = activeTab == tab.first
                    Button(
                        onClick = { activeTab = tab.first },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = if (isSelected) Color(0xFFEEF2FF) else Color.Transparent,
                            contentColor = if (isSelected) Color(0xFF4338CA) else Color(0xFF64748B)
                        ),
                        shape = RoundedCornerShape(8.dp),
                        elevation = null,
                        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp)
                    ) {
                        Text(tab.second, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                    }
                }
            }

            if (activeTab == "overview") {
                LazyColumn(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                    item {
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.fillMaxWidth()) {
                            KpiCard("Faculty Teaching", "24", Icons.Filled.Groups, Color(0xFF6D28D9), MaterialTheme.colorScheme.secondaryContainer, Modifier.weight(1f))
                            KpiCard("Subjects Running", "30", Icons.Filled.MenuBook, Color(0xFF4338CA), Color(0xFFEEF2FF), Modifier.weight(1f))
                        }
                    }
                    item {
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.fillMaxWidth()) {
                            KpiCard("Classes This Week", "120", Icons.Filled.CalendarToday, Color(0xFF0284C7), Color(0xFFF0F9FF), Modifier.weight(1f))
                            KpiCard("Classes Conducted", "95", Icons.Filled.CheckCircle, Color(0xFF059669), Color(0xFFECFDF5), Modifier.weight(1f))
                        }
                    }
                    item {
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.fillMaxWidth()) {
                            KpiCard("Avg. Attendance", "92%", Icons.Filled.Assessment, Color(0xFFD97706), Color(0xFFFFFBEB), Modifier.weight(1f))
                            KpiCard("Pending Approvals", "3", Icons.Filled.Description, Color(0xFFBE123C), Color(0xFFFFF1F2), Modifier.weight(1f))
                        }
                    }
                    item {
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.fillMaxWidth()) {
                            KpiCard("Avg. Syllabus", "65%", Icons.Filled.TrendingUp, Color(0xFFBE185D), Color(0xFFFDF2F8), Modifier.weight(1f))
                            KpiCard("Pending Logs", "2", Icons.Filled.Warning, Color(0xFFD97706), Color(0xFFFFFBEB), Modifier.weight(1f))
                        }
                    }

                    item {
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(16.dp),
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                            border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)
                        ) {
                            Column(modifier = Modifier.padding(16.dp)) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    Icon(Icons.Filled.MenuBook, contentDescription = null, tint = Color(0xFF8B5CF6), modifier = Modifier.size(16.dp))
                                    Text("SYLLABUS COVERAGE BY SUBJECT", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = CamsTextSecondary)
                                }
                                Spacer(Modifier.height(16.dp))
                                Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                                    for (i in 1..5) {
                                        val percentage = 50 + (i * 10)
                                        val color = if (percentage >= 80) Color(0xFF059669) else if (percentage >= 60) Color(0xFF6D28D9) else Color(0xFFBE123C)
                                        Column {
                                            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                                Text("Subject Name $i", fontSize = 12.sp, color = CamsTextPrimary, fontWeight = FontWeight.Bold)
                                                Text("$percentage%", fontSize = 12.sp, color = color, fontWeight = FontWeight.Bold)
                                            }
                                            Spacer(Modifier.height(4.dp))
                                            LinearProgressIndicator(
                                                progress = { percentage / 100f },
                                                modifier = Modifier.fillMaxWidth().height(8.dp).background(MaterialTheme.colorScheme.surfaceVariant, CircleShape),
                                                color = color,
                                                trackColor = Color.Transparent
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            } else {
                Card(
                    modifier = Modifier.fillMaxWidth().weight(1f),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)
                ) {
                    Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(12.dp)) {
                            Icon(Icons.Filled.Description, contentDescription = null, tint = MaterialTheme.colorScheme.outlineVariant, modifier = Modifier.size(48.dp))
                            Text("Select a tab to view specific monitoring data.", color = CamsTextSecondary, fontSize = 14.sp)
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun KpiCard(label: String, value: String, icon: androidx.compose.ui.graphics.vector.ImageVector, color: Color, bgColor: Color, modifier: Modifier) {
    Card(
        modifier = modifier,
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
        shape = RoundedCornerShape(12.dp)
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            Text(label, fontSize = 13.sp, fontWeight = FontWeight.Black, color = CamsTextSecondary, modifier = Modifier.fillMaxWidth())
            Spacer(Modifier.height(8.dp))
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                Box(Modifier.background(bgColor, RoundedCornerShape(8.dp)).padding(6.dp)) {
                    Icon(icon, null, tint = color, modifier = Modifier.size(16.dp))
                }
                Text(value, fontSize = 20.sp, fontWeight = FontWeight.Black, color = CamsTextPrimary)
            }
        }
    }
}