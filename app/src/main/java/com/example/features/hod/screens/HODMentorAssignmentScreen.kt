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
fun HODMentorAssignmentScreen(onNavigate: (String) -> Unit) {
    HODBaseScreen(
        title = "Student Mentorship Assignment",
        subtitle = "Allocate students to faculty mentors for academic guidance",
        currentRoute = "/hod/mentor-assignment",
        onNavigate = onNavigate
    ) {
        Row(Modifier.fillMaxWidth().weight(1f), horizontalArrangement = Arrangement.spacedBy(16.dp)) {
            // Left Panel
            CamsCard(modifier = Modifier.weight(0.4f).fillMaxHeight()) {
                Text("Department Staff", fontWeight = FontWeight.Bold, fontSize = 16.sp, color = CamsTextPrimary)
                Spacer(Modifier.height(12.dp))
                LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    items(6) { i ->
                        Row(
                            modifier = Modifier.fillMaxWidth().background(if(i==0) Color(0xFFEEF2FF) else Color.White, RoundedCornerShape(12.dp)).border(1.dp, if(i==0) Color(0xFFC7D2FE) else MaterialTheme.colorScheme.surfaceVariant, RoundedCornerShape(12.dp)).padding(12.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text("Faculty Member ${i+1}", fontWeight = FontWeight.Bold, fontSize = 13.sp, color = if(i==0) Color(0xFF4338CA) else CamsTextPrimary)
                                Text("fac${i+1}@example.com", fontSize = 13.sp, color = CamsTextSecondary)
                            }
                            Text("${i*2 + 5} Mentees", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = if(i==0) Color(0xFF4338CA) else Color(0xFF64748B), modifier = Modifier.background(if(i==0) Color(0xFFE0E7FF) else MaterialTheme.colorScheme.surfaceVariant, RoundedCornerShape(4.dp)).padding(horizontal = 6.dp, vertical = 2.dp))
                        }
                    }
                }
            }

            // Right Panel
            CamsCard(modifier = Modifier.weight(0.6f).fillMaxHeight()) {
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                    Text("Mentees of Faculty Member 1", fontWeight = FontWeight.Bold, fontSize = 16.sp, color = CamsTextPrimary)
                    Button(onClick = { }, colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF4F46E5)), shape = RoundedCornerShape(8.dp)) {
                        Text("Save Assignment", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                    }
                }
                Spacer(Modifier.height(16.dp))
                LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    items(10) { i ->
                        Row(
                            modifier = Modifier.fillMaxWidth().background(if(i<3) Color(0xFFEEF2FF) else Color.White, RoundedCornerShape(12.dp)).border(1.dp, if(i<3) Color(0xFFC7D2FE) else MaterialTheme.colorScheme.surfaceVariant, RoundedCornerShape(12.dp)).padding(12.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Checkbox(checked = i < 3, onCheckedChange = { })
                            Spacer(Modifier.width(8.dp))
                            Column(modifier = Modifier.weight(1f)) {
                                Text("Student Name ${i+1}", fontWeight = FontWeight.Bold, fontSize = 13.sp, color = CamsTextPrimary)
                                Text("Roll No: 20260${i+1} • Sem 1", fontSize = 13.sp, color = CamsTextSecondary)
                            }
                            if (i < 3) {
                                Text("Selected", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = Color(0xFF059669), modifier = Modifier.background(Color(0xFFD1FAE5), RoundedCornerShape(4.dp)).padding(horizontal = 6.dp, vertical = 2.dp))
                            } else if (i % 2 == 0) {
                                Text("Unassigned", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = Color(0xFF64748B), modifier = Modifier.background(MaterialTheme.colorScheme.surfaceVariant, RoundedCornerShape(4.dp)).padding(horizontal = 6.dp, vertical = 2.dp))
                            } else {
                                Text("Other Mentor", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = Color(0xFFD97706), modifier = Modifier.background(Color(0xFFFEF3C7), RoundedCornerShape(4.dp)).padding(horizontal = 6.dp, vertical = 2.dp))
                            }
                        }
                    }
                }
            }
        }
    }
}