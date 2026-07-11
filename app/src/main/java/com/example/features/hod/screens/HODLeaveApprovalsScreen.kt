package com.example.features.hod.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
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
fun HODLeaveApprovalsScreen(onNavigate: (String) -> Unit) {
    var activeTab by remember { mutableStateOf("faculty") }
    
    HODBaseScreen(
        title = "Leave Approvals",
        subtitle = "Manage faculty and student leave requests",
        currentRoute = AppRoutes.HOD_LEAVE_APPROVALS,
        onNavigate = onNavigate
    ) {
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.fillMaxWidth()) {
            KpiCard("Pending", "5", Icons.Filled.Schedule, Color(0xFFD97706), Modifier.weight(1f))
            KpiCard("Approved", "42", Icons.Filled.CheckCircle, Color(0xFF059669), Modifier.weight(1f))
            KpiCard("Rejected", "3", Icons.Filled.Cancel, Color(0xFFE11D48), Modifier.weight(1f))
            KpiCard("OD Total", "12", Icons.Filled.Description, Color(0xFF7C3AED), Modifier.weight(1f))
        }

        Spacer(Modifier.height(16.dp))

        Row(Modifier.fillMaxWidth().background(MaterialTheme.colorScheme.surface, RoundedCornerShape(12.dp)).padding(4.dp)) {
            TabButton("Faculty Leaves", activeTab == "faculty", Modifier.weight(1f)) { activeTab = "faculty" }
            TabButton("Student Leaves", activeTab == "student", Modifier.weight(1f)) { activeTab = "student" }
        }

        Spacer(Modifier.height(16.dp))

        LazyColumn(verticalArrangement = Arrangement.spacedBy(12.dp), modifier = Modifier.weight(1f)) {
            items(5) { i ->
                CamsCard(modifier = Modifier.fillMaxWidth()) {
                    Column(Modifier.padding(16.dp)) {
                        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                            Text(if (i % 3 == 0) "PENDING HOD" else "APPROVED", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = if (i % 3 == 0) Color(0xFFB45309) else Color(0xFF059669), modifier = Modifier.background(if (i % 3 == 0) Color(0xFFFEF3C7) else Color(0xFFD1FAE5), RoundedCornerShape(4.dp)).padding(horizontal = 6.dp, vertical = 2.dp))
                            Text("Medical Leave", fontSize = 12.sp, color = CamsTextSecondary, fontWeight = FontWeight.Bold)
                        }
                        Spacer(Modifier.height(12.dp))
                        Text(if (activeTab == "faculty") "Dr. Alice Brown" else "Student $i", fontWeight = FontWeight.Bold, fontSize = 16.sp, color = CamsTextPrimary)
                        Text("From: 2026-07-08 To: 2026-07-10", fontSize = 12.sp, color = CamsTextSecondary)
                        Spacer(Modifier.height(8.dp))
                        Text("Reason: Need to attend medical checkup.", fontSize = 12.sp, color = CamsTextSecondary, fontStyle = androidx.compose.ui.text.font.FontStyle.Italic)
                        
                        if (i % 3 == 0) {
                            Spacer(Modifier.height(16.dp))
                            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                Button(onClick = { }, modifier = Modifier.weight(1f), colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFFEF2F2), contentColor = Color(0xFFE11D48)), shape = RoundedCornerShape(8.dp)) {
                                    Icon(Icons.Filled.ThumbDown, null, modifier = Modifier.size(16.dp))
                                    Spacer(Modifier.width(4.dp))
                                    Text("Reject", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                                }
                                Button(onClick = { }, modifier = Modifier.weight(1f), colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF059669)), shape = RoundedCornerShape(8.dp)) {
                                    Icon(Icons.Filled.ThumbUp, null, modifier = Modifier.size(16.dp))
                                    Spacer(Modifier.width(4.dp))
                                    Text("Approve", fontSize = 12.sp, fontWeight = FontWeight.Bold)
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
            containerColor = if (selected) Color(0xFFEEF2FF) else Color.Transparent,
            contentColor = if (selected) Color(0xFF4338CA) else Color(0xFF64748B)
        ),
        shape = RoundedCornerShape(8.dp),
        elevation = null
    ) {
        Text(text, fontSize = 13.sp, fontWeight = FontWeight.Bold)
    }
}
