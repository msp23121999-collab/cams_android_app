package com.example.features.admin.screens

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
import com.example.features.admin.widgets.AdminBaseScreen
import com.example.core.navigation.AppRoutes

@Composable
fun AdminExamMgmtScreen(onNavigate: (String) -> Unit) {
    AdminBaseScreen(
        title = "Exam Management",
        subtitle = "Schedule exams and manage halls",
        currentRoute = AppRoutes.ADMIN_EXAM_MGMT,
        onNavigate = onNavigate
    ) {
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.fillMaxWidth()) {
            KpiCard("Upcoming Exams", "8", Icons.Filled.Event, Color(0xFF6366F1), Modifier.weight(1f))
            KpiCard("Halls Assigned", "12", Icons.Filled.MeetingRoom, Color(0xFF10B981), Modifier.weight(1f))
        }
        
        Spacer(modifier = Modifier.height(16.dp))

        CamsCard(modifier = Modifier.fillMaxWidth().weight(1f)) {
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                Text("Exam Schedule", fontWeight = FontWeight.Bold, fontSize = 16.sp, color = CamsTextPrimary)
                Button(onClick = { }, colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF4F46E5)), shape = RoundedCornerShape(8.dp)) {
                    Text("Schedule Exam", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                }
            }
            Spacer(Modifier.height(12.dp))
            LazyColumn(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                items(4) { i ->
                    Row(
                        modifier = Modifier.fillMaxWidth().background(MaterialTheme.colorScheme.surface, RoundedCornerShape(12.dp)).border(1.dp, MaterialTheme.colorScheme.outlineVariant, RoundedCornerShape(12.dp)).padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text("Constitutional Law - Midterm", fontWeight = FontWeight.Bold, fontSize = 14.sp, color = CamsTextPrimary)
                            Spacer(Modifier.height(4.dp))
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.Filled.AccessTime, null, tint = CamsTextSecondary, modifier = Modifier.size(12.dp))
                                Spacer(Modifier.width(4.dp))
                                Text("Oct 15, 10:00 AM", fontSize = 12.sp, color = CamsTextSecondary)
                            }
                        }
                        Column(horizontalAlignment = Alignment.End) {
                            Text("Hall 20${i+1}", fontWeight = FontWeight.Bold, fontSize = 14.sp, color = CamsTextPrimary)
                            Spacer(Modifier.height(4.dp))
                            Text(
                                "SCHEDULED", 
                                fontSize = 13.sp, 
                                fontWeight = FontWeight.Bold, 
                                color = Color(0xFF4338CA), 
                                modifier = Modifier.background(Color(0xFFEEF2FF), RoundedCornerShape(4.dp)).padding(horizontal = 6.dp, vertical = 2.dp)
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
                Text(label, fontSize = 13.sp, fontWeight = FontWeight.Black, color = Color(0xFF64748B), modifier = Modifier.weight(1f))
                Box(Modifier.background(color.copy(alpha=0.1f), RoundedCornerShape(8.dp)).padding(4.dp)) {
                    Icon(icon, null, tint = color, modifier = Modifier.size(16.dp))
                }
            }
            Text(value, fontSize = 20.sp, fontWeight = FontWeight.Black, color = CamsTextPrimary, modifier = Modifier.padding(top = 8.dp))
        }
    }
}
