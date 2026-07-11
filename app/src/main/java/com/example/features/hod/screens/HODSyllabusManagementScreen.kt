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
import com.example.core.navigation.AppRoutes

@Composable
fun HODSyllabusManagementScreen(onNavigate: (String) -> Unit) {
    HODBaseScreen(
        title = "Configure Department Syllabus",
        subtitle = "Select a semester to view courses and configure their units",
        currentRoute = "/hod/syllabus-mgmt",
        onNavigate = onNavigate
    ) {
        Row(Modifier.fillMaxWidth().background(MaterialTheme.colorScheme.surface, RoundedCornerShape(12.dp)).padding(4.dp)) {
            for (i in 1..4) {
                Button(
                    onClick = { },
                    modifier = Modifier.weight(1f),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = if (i == 1) Color(0xFFEEF2FF) else Color.Transparent,
                        contentColor = if (i == 1) Color(0xFF4338CA) else Color(0xFF64748B)
                    ),
                    shape = RoundedCornerShape(8.dp),
                    elevation = null
                ) {
                    Text("Semester $i", fontSize = 13.sp, fontWeight = FontWeight.Bold)
                }
            }
        }

        Spacer(Modifier.height(16.dp))

        CamsCard(modifier = Modifier.fillMaxWidth().weight(1f)) {
            Text("Courses List", fontWeight = FontWeight.Bold, fontSize = 16.sp, color = CamsTextPrimary)
            Spacer(Modifier.height(12.dp))
            LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                items(5) { i ->
                    Row(
                        modifier = Modifier.fillMaxWidth().background(MaterialTheme.colorScheme.background, RoundedCornerShape(12.dp)).border(1.dp, MaterialTheme.colorScheme.surfaceVariant, RoundedCornerShape(12.dp)).padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("${i+1}.", fontWeight = FontWeight.Bold, fontSize = 14.sp, color = Color(0xFF64748B), modifier = Modifier.width(32.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Text("Course Name ${i+1}", fontWeight = FontWeight.Bold, fontSize = 14.sp, color = CamsTextPrimary)
                            Text("Code: LAW10${i+1} • ${3 + i%2} Credits", fontSize = 13.sp, color = CamsTextSecondary)
                        }
                        Button(onClick = { }, colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF4F46E5)), shape = RoundedCornerShape(8.dp)) {
                            Text("Manage Unit", fontSize = 13.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        }
    }
}