package com.example.features.admin.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.core.theme.*
import com.example.core.ui.CamsCard
import com.example.features.admin.widgets.AdminBaseScreen

@Composable
fun AdminReportsScreen(onNavigate: (String) -> Unit) {
    AdminBaseScreen(
        title = "Reports & Exports",
        currentRoute = "/admin/reports",
        onNavigate = onNavigate
    ) {
        LazyColumn(
            modifier = Modifier
                .fillMaxWidth().weight(1f)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            item {
                Text("Generate Reports", fontWeight = FontWeight.Bold, fontSize = 18.sp, color = CamsTextPrimary)
            }

            item {
                ReportTypeCard(
                    "Academic Reports",
                    "Student attendance, internal marks, and exam results",
                    Icons.Filled.School,
                    Color(0xFF3B82F6)
                )
            }

            item {
                ReportTypeCard(
                    "Financial Reports",
                    "Fee collections, pending dues, and department budgets",
                    Icons.Filled.AccountBalanceWallet,
                    Color(0xFF10B981)
                )
            }

            item {
                ReportTypeCard(
                    "User Activity Reports",
                    "Login history, active sessions, and system usage",
                    Icons.Filled.DataUsage,
                    Color(0xFFF59E0B)
                )
            }

            item {
                Text("Recent Exports", fontWeight = FontWeight.Bold, fontSize = 18.sp, color = CamsTextPrimary)
            }

            item {
                CamsCard {
                    ExportItem("Sem_VI_Attendance_Report.xlsx", "Today, 10:30 AM")
                    ExportItem("Fee_Defaulters_List.pdf", "Yesterday, 04:15 PM")
                    ExportItem("Faculty_Leave_Summary.csv", "12 Oct 2023")
                }
            }
        }
    }
}

@Composable
private fun ReportTypeCard(title: String, description: String, icon: ImageVector, color: Color) {
    CamsCard(onClick = {}) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(Modifier.size(48.dp).background(color.copy(alpha = 0.1f), CircleShape), contentAlignment = Alignment.Center) {
                Icon(icon, null, tint = color, modifier = Modifier.size(24.dp))
            }
            Column(modifier = Modifier.weight(1f)) {
                Text(title, fontWeight = FontWeight.Bold, fontSize = 16.sp, color = CamsTextPrimary)
                Text(description, fontSize = 12.sp, color = CamsTextSecondary)
            }
            Icon(Icons.Filled.Download, null, tint = CamsNavy)
        }
    }
}

@Composable
private fun ExportItem(name: String, time: String) {
    Row(modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
        Column(modifier = Modifier.weight(1f)) {
            Text(name, fontWeight = FontWeight.Medium, fontSize = 14.sp, color = CamsTextPrimary)
            Text(time, fontSize = 12.sp, color = Color(0xFF64748B))
        }
        Icon(Icons.Filled.FileOpen, null, tint = CamsNavy)
    }
}
