package com.example.features.faculty.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
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
import com.example.features.faculty.widgets.FacultyBaseScreen

@Composable
fun FacultyMentorStudentManagementScreen(onNavigate: (String) -> Unit) {
    FacultyBaseScreen(scrollable = false, 
        title = "Mentor Student Management",
        currentRoute = "/faculty/mentor-student-management",
        onNavigate = onNavigate
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp)
        ) {
            // Mentor Overview Card
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = CamsNavy),
                shape = RoundedCornerShape(24.dp)
            ) {
                Row(
                    modifier = Modifier.padding(24.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Column {
                        Text("Total Mentees", color = Color.White.copy(alpha = 0.7f), fontSize = 14.sp)
                        Text("24 Students", color = Color.White, fontSize = 24.sp, fontWeight = FontWeight.Black)
                    }
                    Box(
                        modifier = Modifier
                            .size(56.dp)
                            .background(MaterialTheme.colorScheme.surface.copy(alpha = 0.2f), CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(Icons.Filled.Groups, null, tint = Color.White, modifier = Modifier.size(32.dp))
                    }
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            Text("My Mentees", fontWeight = FontWeight.Bold, fontSize = 18.sp, color = CamsTextPrimary)
            Spacer(modifier = Modifier.height(12.dp))

            val mentees = listOf(
                Mentee("Rahul Sharma", "S101", "Excellent", "Last meeting: 05 Oct"),
                Mentee("Priya Verma", "S102", "Needs Attention", "Last meeting: 28 Sep"),
                Mentee("Amit Singh", "S103", "Good", "Last meeting: 10 Oct"),
                Mentee("Siddharth Malhotra", "S104", "Average", "Last meeting: 02 Oct")
            )

            LazyColumn(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                items(mentees) { mentee ->
                    MenteeItem(mentee)
                }
            }
        }
    }
}

@Composable
private fun MenteeItem(mentee: Mentee) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        shape = RoundedCornerShape(16.dp)
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            horizontalArrangement = Arrangement.spacedBy(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(48.dp)
                    .background(CamsNavy.copy(alpha = 0.1f), CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Icon(Icons.Filled.Person, null, tint = CamsNavy)
            }
            Column(modifier = Modifier.weight(1f)) {
                Text(mentee.name, fontWeight = FontWeight.Bold, color = CamsTextPrimary, fontSize = 16.sp)
                Text("ID: ${mentee.id}", fontSize = 12.sp, color = CamsTextSecondary)
                Spacer(modifier = Modifier.height(4.dp))
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                    Box(
                        modifier = Modifier
                            .size(8.dp)
                            .background(
                                if (mentee.progress == "Needs Attention") Color.Red else Color(0xFF10B981),
                                CircleShape
                            )
                    )
                    Text(mentee.progress, fontSize = 12.sp, fontWeight = FontWeight.Bold, color = CamsTextPrimary)
                }
            }
            IconButton(onClick = { /* Open Mentorship Diary */ }) {
                Icon(Icons.Filled.ChevronRight, null, tint = Color(0xFF64748B))
            }
        }
    }
}

data class Mentee(val name: String, val id: String, val progress: String, val lastUpdate: String)
