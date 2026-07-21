package com.example.features.hod.screens

import androidx.lifecycle.compose.collectAsStateWithLifecycle
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
import androidx.compose.material.icons.automirrored.filled.*
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
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.core.navigation.AppRoutes
import com.example.core.theme.CamsTextPrimary
import com.example.core.theme.CamsTextSecondary
import com.example.features.hod.providers.HODAcademicMonitoringViewModel
import com.example.features.hod.widgets.HODBaseScreen

@Composable
fun HODAcademicMonitoringScreen(
    onNavigate: (String) -> Unit,
    viewModel: HODAcademicMonitoringViewModel = viewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
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

            if (uiState.isLoading) {
                Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator(color = Color(0xFF4338CA))
                }
            } else if (uiState.error != null) {
                Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Text(text = "Error: ${uiState.error}", color = MaterialTheme.colorScheme.error)
                }
            } else if (activeTab == "overview") {
                val data = uiState.dashboardData
                LazyColumn(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                    item {
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.fillMaxWidth()) {
                            KpiCard("Lectures Conducted", "${data?.total_lectures_conducted ?: 0}", Icons.Filled.CheckCircle, Color(0xFF059669), Color(0xFFECFDF5), Modifier.weight(1f))
                            KpiCard("Pending Diaries", "${data?.pending_diaries_count ?: 0}", Icons.Filled.Warning, Color(0xFFD97706), Color(0xFFFFFBEB), Modifier.weight(1f))
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
                                    Icon(Icons.AutoMirrored.Filled.MenuBook, contentDescription = null, tint = Color(0xFF8B5CF6), modifier = Modifier.size(16.dp))
                                    Text("SYLLABUS COVERAGE BY SUBJECT", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                }
                                Spacer(Modifier.height(16.dp))
                                Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                                    data?.syllabus_status?.forEach { status ->
                                        val percentage = status.completion.toFloat()
                                        val color = if (percentage >= 80) Color(0xFF059669) else if (percentage >= 60) Color(0xFF6D28D9) else Color(0xFFBE123C)
                                        Column {
                                            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                                Text(status.subject, fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurface, fontWeight = FontWeight.Bold)
                                                Text("${status.completion}%", fontSize = 12.sp, color = color, fontWeight = FontWeight.Bold)
                                            }
                                            Text(status.faculty, fontSize = 10.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
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
            } else if (activeTab == "faculty" || activeTab == "subjects") {
                val data = uiState.dashboardData
                val statuses = data?.syllabus_status ?: emptyList()
                if (statuses.isEmpty()) {
                    EmptyStateCard("No ${if (activeTab == "faculty") "faculty progress" else "subject tracking"} data available.")
                } else {
                    LazyColumn(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                        items(statuses) { status ->
                            Row(
                                modifier = Modifier.fillMaxWidth().background(MaterialTheme.colorScheme.surface, RoundedCornerShape(12.dp)).border(1.dp, MaterialTheme.colorScheme.outlineVariant, RoundedCornerShape(12.dp)).padding(12.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(status.subject, fontWeight = FontWeight.Bold, fontSize = 14.sp, color = MaterialTheme.colorScheme.onSurface)
                                    Text(status.faculty, fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                }
                                Text("${status.completion}%", fontSize = 13.sp, fontWeight = FontWeight.Bold, color = Color(0xFF4338CA))
                            }
                        }
                    }
                }
            } else if (activeTab == "attendance") {
                val attendance = uiState.attendanceData
                if (attendance.isEmpty()) {
                    EmptyStateCard("No attendance monitoring data available.")
                } else {
                    LazyColumn(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                        items(attendance) { a ->
                            Row(
                                modifier = Modifier.fillMaxWidth().background(MaterialTheme.colorScheme.surface, RoundedCornerShape(12.dp)).border(1.dp, MaterialTheme.colorScheme.outlineVariant, RoundedCornerShape(12.dp)).padding(12.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(a.subject, fontWeight = FontWeight.Bold, fontSize = 14.sp, color = MaterialTheme.colorScheme.onSurface)
                                    Text("${a.subjectCode} • Sem ${a.semester}", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                }
                            }
                        }
                    }
                }
            } else if (activeTab == "materials") {
                val materials = uiState.materials
                if (materials.isEmpty()) {
                    EmptyStateCard("No study materials pending review.")
                } else {
                    LazyColumn(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                        items(materials, key = { it.id }) { m ->
                            Row(
                                modifier = Modifier.fillMaxWidth().background(MaterialTheme.colorScheme.surface, RoundedCornerShape(12.dp)).border(1.dp, MaterialTheme.colorScheme.outlineVariant, RoundedCornerShape(12.dp)).padding(12.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(m.title, fontWeight = FontWeight.Bold, fontSize = 14.sp, color = MaterialTheme.colorScheme.onSurface)
                                    Text("${m.facultyName} • ${m.subject}", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                }
                            }
                        }
                    }
                }
            } else if (activeTab == "today") {
                val today = uiState.pendingEntries.filter { it.date == java.time.LocalDate.now().toString() }
                if (today.isEmpty()) {
                    EmptyStateCard("No pending classes for today.")
                } else {
                    LazyColumn(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                        items(today) { entry ->
                            Row(
                                modifier = Modifier.fillMaxWidth().background(MaterialTheme.colorScheme.surface, RoundedCornerShape(12.dp)).border(1.dp, MaterialTheme.colorScheme.outlineVariant, RoundedCornerShape(12.dp)).padding(12.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(entry.subject, fontWeight = FontWeight.Bold, fontSize = 14.sp, color = MaterialTheme.colorScheme.onSurface)
                                    Text("Section ${entry.section} • ${entry.hour} • ${entry.facultyName}", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                }
                                Text("PENDING DIARY", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = Color(0xFFB45309), modifier = Modifier.background(Color(0xFFFEF3C7), RoundedCornerShape(4.dp)).padding(horizontal = 6.dp, vertical = 2.dp))
                            }
                        }
                    }
                }
            } else {
                val alerts = uiState.pendingEntries
                if (alerts.isEmpty()) {
                    EmptyStateCard("No academic alerts. All class diaries are up to date.")
                } else {
                    LazyColumn(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                        items(alerts) { entry ->
                            Row(
                                modifier = Modifier.fillMaxWidth().background(MaterialTheme.colorScheme.surface, RoundedCornerShape(12.dp)).border(1.dp, Color(0xFFF43F5E), RoundedCornerShape(12.dp)).padding(12.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(Icons.Filled.Warning, null, tint = Color(0xFFB45309), modifier = Modifier.size(20.dp))
                                Spacer(Modifier.width(8.dp))
                                Column(modifier = Modifier.weight(1f)) {
                                    Text("Missing diary: ${entry.subject}", fontWeight = FontWeight.Bold, fontSize = 14.sp, color = MaterialTheme.colorScheme.onSurface)
                                    Text("${entry.facultyName} • Section ${entry.section} • ${entry.date}", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun EmptyStateCard(message: String) {
    Card(
        modifier = Modifier.fillMaxWidth().fillMaxHeight(0.5f),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)
    ) {
        Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Column(horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Icon(Icons.Filled.Description, contentDescription = null, tint = MaterialTheme.colorScheme.outlineVariant, modifier = Modifier.size(48.dp))
                Text(message, color = MaterialTheme.colorScheme.onSurfaceVariant, fontSize = 14.sp)
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
            Text(label, fontSize = 13.sp, fontWeight = FontWeight.Black, color = MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.fillMaxWidth())
            Spacer(Modifier.height(8.dp))
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                Box(Modifier.background(bgColor, RoundedCornerShape(8.dp)).padding(6.dp)) {
                    Icon(icon, null, tint = color, modifier = Modifier.size(16.dp))
                }
                Text(value, fontSize = 20.sp, fontWeight = FontWeight.Black, color = MaterialTheme.colorScheme.onSurface)
            }
        }
    }
}
