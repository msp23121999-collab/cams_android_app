package com.example.features.hod.screens

import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.compose.foundation.BorderStroke
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
import com.example.features.hod.providers.HODAttendanceMonitoringViewModel
import com.example.features.hod.widgets.HODBaseScreen

@Composable
fun HODAttendanceMonitoringScreen(
    onNavigate: (String) -> Unit,
    viewModel: HODAttendanceMonitoringViewModel = viewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    HODBaseScreen(scrollable = false, 
        title = "Attendance Monitoring Console",
        subtitle = "Analyze class attendance percentages and flag shortages",
        currentRoute = AppRoutes.HOD_ATTENDANCE_MONITORING,
        onNavigate = onNavigate
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.background)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            if (uiState.isLoading) {
                Box(Modifier.fillMaxWidth().weight(1f), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator(color = Color(0xFF4338CA))
                }
            } else if (uiState.error != null) {
                Box(Modifier.fillMaxWidth().weight(1f), contentAlignment = Alignment.Center) {
                    Text("Error: ${uiState.error}", color = MaterialTheme.colorScheme.error)
                }
            } else {
                val data = uiState.attendanceData
                val classAvg = if (data.isNotEmpty()) data.map { it.attendancePercentage }.average() else 0.0
                val lowCount = data.sumOf { it.lowAttendanceCount ?: 0 }
                val excellent = data.count { it.attendancePercentage >= 90.0 }
                val onTrack = data.count { it.attendancePercentage in 75.0..89.9 }

                Row(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.fillMaxWidth()) {
                    KpiCard("Class Average", "${String.format("%.1f", classAvg)}%", Icons.Filled.TrendingUp, Color(0xFF4338CA), Color(0xFFEEF2FF), Modifier.weight(1f))
                    KpiCard("Critically Low", "$lowCount", Icons.Filled.Warning, Color(0xFFBE123C), Color(0xFFFFF1F2), Modifier.weight(1f))
                }
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.fillMaxWidth()) {
                    KpiCard("On Track Subjects", "$onTrack", Icons.Filled.CheckCircle, Color(0xFFD97706), Color(0xFFFFFBEB), Modifier.weight(1f))
                    KpiCard("Excellent Subjects", "$excellent", Icons.Filled.Star, Color(0xFF059669), Color(0xFFECFDF5), Modifier.weight(1f))
                }

                Card(
                    modifier = Modifier.fillMaxWidth().weight(1f),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)
                ) {
                    Column {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .background(MaterialTheme.colorScheme.background)
                                .border(BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant))
                                .padding(16.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(32.dp)
                                    .background(Color(0xFFEEF2FF), RoundedCornerShape(8.dp)),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Groups,
                                    contentDescription = null,
                                    tint = Color(0xFF4F46E5),
                                    modifier = Modifier.size(16.dp)
                                )
                            }
                            Column {
                                Text("Subject Attendance Records", fontWeight = FontWeight.Bold, fontSize = 14.sp, color = MaterialTheme.colorScheme.onSurface)
                                Text("Live attendance statistics across all subjects", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            }
                        }

                        if (data.isEmpty()) {
                            Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                                Text("No attendance records found.", color = MaterialTheme.colorScheme.onSurfaceVariant)
                            }
                        } else {
                            LazyColumn(contentPadding = PaddingValues(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                                items(data) { record ->
                                    val percentage = record.attendancePercentage
                                    val color = if (percentage < 75) Color(0xFFBE123C) else if (percentage < 90) Color(0xFFD97706) else Color(0xFF059669)
                                    val bgColor = if (percentage < 75) Color(0xFFFFF1F2) else if (percentage < 90) Color(0xFFFFFBEB) else Color(0xFFECFDF5)
                                    
                                    Row(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .background(MaterialTheme.colorScheme.surface, RoundedCornerShape(12.dp))
                                            .border(1.dp, MaterialTheme.colorScheme.outlineVariant, RoundedCornerShape(12.dp))
                                            .padding(16.dp),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Column(modifier = Modifier.weight(1.5f)) {
                                            Text(record.subject, fontWeight = FontWeight.Bold, fontSize = 14.sp, color = MaterialTheme.colorScheme.onSurface)
                                            Spacer(Modifier.height(4.dp))
                                            Text("Code: ${record.subjectCode} • Sem ${record.semester} • ${record.studentsCount} Students", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                        }
                                        
                                        Column(modifier = Modifier.weight(1f)) {
                                            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                                                Text("${String.format("%.1f", percentage)}%", fontWeight = FontWeight.Bold, fontSize = 14.sp, color = color)
                                            }
                                            Spacer(Modifier.height(6.dp))
                                            LinearProgressIndicator(
                                                progress = { (percentage / 100).toFloat().coerceIn(0f, 1f) },
                                                modifier = Modifier.fillMaxWidth().height(8.dp).background(MaterialTheme.colorScheme.surfaceVariant, CircleShape),
                                                color = color,
                                                trackColor = Color.Transparent
                                            )
                                        }
                                        
                                        Box(modifier = Modifier.weight(0.8f), contentAlignment = Alignment.CenterEnd) {
                                            Text(
                                                if (percentage < 75) "Low" else "Good",
                                                fontSize = 12.sp,
                                                fontWeight = FontWeight.Bold,
                                                color = color,
                                                modifier = Modifier.background(bgColor, RoundedCornerShape(6.dp)).padding(horizontal = 10.dp, vertical = 4.dp)
                                            )
                                        }
                                    }
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
private fun KpiCard(label: String, value: String, icon: androidx.compose.ui.graphics.vector.ImageVector, color: Color, bgColor: Color, modifier: Modifier) {
    Card(
        modifier = modifier,
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
        shape = RoundedCornerShape(12.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(label, fontSize = 12.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.fillMaxWidth())
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
