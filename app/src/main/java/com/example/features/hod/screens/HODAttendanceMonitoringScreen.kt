package com.example.features.hod.screens

import androidx.compose.foundation.BorderStroke
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
import com.example.core.navigation.AppRoutes


import com.example.core.theme.CamsTextPrimary
import com.example.core.theme.CamsTextSecondary
import com.example.features.hod.widgets.HODBaseScreen

@Composable
fun HODAttendanceMonitoringScreen(onNavigate: (String) -> Unit) {
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
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.fillMaxWidth()) {
                KpiCard("Class Average", "88%", Icons.Filled.TrendingUp, Color(0xFF4338CA), Color(0xFFEEF2FF), Modifier.weight(1f))
                KpiCard("Critically Low", "5", Icons.Filled.Warning, Color(0xFFBE123C), Color(0xFFFFF1F2), Modifier.weight(1f))
            }
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.fillMaxWidth()) {
                KpiCard("On Track", "25", Icons.Filled.CheckCircle, Color(0xFFD97706), Color(0xFFFFFBEB), Modifier.weight(1f))
                KpiCard("Excellent", "12", Icons.Filled.Star, Color(0xFF059669), Color(0xFFECFDF5), Modifier.weight(1f))
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
                            Text("Student Attendance Records", fontWeight = FontWeight.Bold, fontSize = 14.sp, color = CamsTextPrimary)
                            Text("Live attendance statistics across all subjects", fontSize = 12.sp, color = CamsTextSecondary)
                        }
                    }

                    LazyColumn(contentPadding = PaddingValues(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                        items(10) { i ->
                            val percentage = if (i == 0) 65 else if (i < 4) 82 else 95
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
                                    Text("Student Name ${i+1}", fontWeight = FontWeight.Bold, fontSize = 14.sp, color = CamsTextPrimary)
                                    Spacer(Modifier.height(4.dp))
                                    Text("Roll No: 20260${i+1} • Sem 1", fontSize = 12.sp, color = CamsTextSecondary)
                                }
                                
                                Column(modifier = Modifier.weight(1f)) {
                                    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                                        Text("$percentage%", fontWeight = FontWeight.Bold, fontSize = 14.sp, color = color)
                                    }
                                    Spacer(Modifier.height(6.dp))
                                    LinearProgressIndicator(
                                        progress = { percentage / 100f },
                                        modifier = Modifier.fillMaxWidth().height(8.dp).background(MaterialTheme.colorScheme.surfaceVariant, CircleShape),
                                        color = color,
                                        trackColor = Color.Transparent
                                    )
                                }
                                
                                Box(modifier = Modifier.weight(1f), contentAlignment = Alignment.CenterEnd) {
                                    Text(
                                        if (percentage < 75) "Shortage" else "Eligible",
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

@Composable
private fun KpiCard(label: String, value: String, icon: androidx.compose.ui.graphics.vector.ImageVector, color: Color, bgColor: Color, modifier: Modifier) {
    Card(
        modifier = modifier,
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
        shape = RoundedCornerShape(12.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(label, fontSize = 12.sp, fontWeight = FontWeight.Bold, color = CamsTextSecondary, modifier = Modifier.fillMaxWidth())
            Spacer(Modifier.height(8.dp))
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                Box(Modifier.background(bgColor, RoundedCornerShape(8.dp)).padding(6.dp)) {
                    Icon(icon, null, tint = color, modifier = Modifier.size(20.dp))
                }
                Text(value, fontSize = 24.sp, fontWeight = FontWeight.Black, color = CamsTextPrimary)
            }
        }
    }
}