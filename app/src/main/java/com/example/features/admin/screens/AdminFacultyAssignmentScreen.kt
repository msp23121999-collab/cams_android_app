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
fun AdminFacultyAssignmentScreen(onNavigate: (String) -> Unit) {
    AdminBaseScreen(
        title = "Faculty Assignments",
        subtitle = "Assign subjects to faculty members",
        currentRoute = AppRoutes.ADMIN_FACULTY_ASSIGNMENT,
        onNavigate = onNavigate
    ) {
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
            Text("Current Assignments", fontWeight = FontWeight.Bold, fontSize = 18.sp, color = CamsTextPrimary)
            Button(onClick = { }, colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF4F46E5)), shape = RoundedCornerShape(8.dp)) {
                Icon(Icons.Filled.Add, null, modifier = Modifier.size(16.dp))
                Spacer(Modifier.width(4.dp))
                Text("New Assignment", fontSize = 12.sp, fontWeight = FontWeight.Bold)
            }
        }
        
        Spacer(Modifier.height(16.dp))

        CamsCard(modifier = Modifier.fillMaxWidth().weight(1f)) {
            LazyColumn(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                items(6) { i ->
                    Row(
                        modifier = Modifier.fillMaxWidth().background(MaterialTheme.colorScheme.surface, RoundedCornerShape(12.dp)).border(1.dp, MaterialTheme.colorScheme.outlineVariant, RoundedCornerShape(12.dp)).padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(Modifier.background(Color(0xFFEEF2FF), CircleShape).size(40.dp), contentAlignment = Alignment.Center) {
                            Icon(Icons.Filled.Person, null, tint = Color(0xFF4F46E5))
                        }
                        Spacer(Modifier.width(16.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Text("Prof. Name ${i+1}", fontWeight = FontWeight.Bold, fontSize = 14.sp, color = CamsTextPrimary)
                            Text("Constitutional Law", fontSize = 12.sp, color = CamsTextSecondary)
                        }
                        Text(
                            "Semester ${i%3 + 1}", 
                            fontSize = 12.sp, 
                            fontWeight = FontWeight.Bold, 
                            color = CamsTextSecondary, 
                            modifier = Modifier.background(MaterialTheme.colorScheme.surfaceVariant, RoundedCornerShape(4.dp)).padding(horizontal = 8.dp, vertical = 4.dp)
                        )
                        Spacer(Modifier.width(16.dp))
                        Icon(Icons.Filled.MoreVert, null, tint = CamsTextSecondary)
                    }
                }
            }
        }
    }
}
