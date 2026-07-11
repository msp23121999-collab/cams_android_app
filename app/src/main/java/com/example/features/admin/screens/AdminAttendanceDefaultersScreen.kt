package com.example.features.admin.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.*
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
fun AdminAttendanceDefaultersScreen(onNavigate: (String) -> Unit) {
    AdminBaseScreen(
        title = "Attendance Defaulters",
        currentRoute = AppRoutes.ADMIN_ATTENDANCE_DEFAULTERS,
        onNavigate = onNavigate
    ) {
        Column(
            modifier = Modifier.fillMaxSize().padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Text("Critical Defaulters (< 75%)", fontWeight = FontWeight.Bold, fontSize = 16.sp, color = CamsTextPrimary)
            
            val defaulters = listOf(
                Defaulter("Alice Smith", "CS101", "68%"),
                Defaulter("Bob Jones", "ME201", "71%"),
                Defaulter("Charlie Brown", "EE102", "55%")
            )
            
            LazyColumn(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                items(defaulters) { student ->
                    CamsCard {
                        Row(modifier = Modifier.fillMaxWidth().padding(16.dp), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                            Column {
                                Text(student.name, fontWeight = FontWeight.Bold, fontSize = 15.sp, color = CamsTextPrimary)
                                Text(student.batch, fontSize = 12.sp, color = CamsTextSecondary)
                            }
                            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                Icon(Icons.Filled.Warning, null, tint = Color(0xFFEF4444), modifier = Modifier.size(16.dp))
                                Text(student.attendance, fontWeight = FontWeight.Bold, color = Color(0xFFEF4444))
                            }
                        }
                    }
                }
            }
        }
    }
}

data class Defaulter(val name: String, val batch: String, val attendance: String)
