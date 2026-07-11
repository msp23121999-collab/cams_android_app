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
fun HODTimetableSetupScreen(onNavigate: (String) -> Unit) {
    HODBaseScreen(
        title = "Timetable Setup",
        subtitle = "Configure academic terms, working days, and time slots",
        currentRoute = "/hod/timetable-setup",
        onNavigate = onNavigate,
        onBackClick = { onNavigate(AppRoutes.HOD_TIMETABLE_MGMT) }
    ) {
        CamsCard(modifier = Modifier.fillMaxWidth()) {
            Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                Text("Term Configuration", fontWeight = FontWeight.Bold, fontSize = 16.sp, color = CamsTextPrimary)
                
                Row(horizontalArrangement = Arrangement.spacedBy(12.dp), modifier = Modifier.fillMaxWidth()) {
                    OutlinedTextField(
                        value = "Fall 2026",
                        onValueChange = { },
                        label = { Text("Academic Term") },
                        modifier = Modifier.weight(1f),
                        colors = OutlinedTextFieldDefaults.colors(unfocusedBorderColor = MaterialTheme.colorScheme.outlineVariant)
                    )
                    OutlinedTextField(
                        value = "15 Weeks",
                        onValueChange = { },
                        label = { Text("Duration") },
                        modifier = Modifier.weight(1f),
                        colors = OutlinedTextFieldDefaults.colors(unfocusedBorderColor = MaterialTheme.colorScheme.outlineVariant)
                    )
                }
            }
        }
        
        Spacer(Modifier.height(16.dp))
        
        Row(Modifier.fillMaxWidth().weight(1f), horizontalArrangement = Arrangement.spacedBy(16.dp)) {
            // Working Days
            CamsCard(modifier = Modifier.weight(1f).fillMaxHeight()) {
                Text("Working Days", fontWeight = FontWeight.Bold, fontSize = 16.sp, color = CamsTextPrimary)
                Spacer(Modifier.height(12.dp))
                val days = listOf("Monday", "Tuesday", "Wednesday", "Thursday", "Friday", "Saturday")
                LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    items(days.size) { i ->
                        Row(
                            modifier = Modifier.fillMaxWidth().background(MaterialTheme.colorScheme.surface, RoundedCornerShape(8.dp)).border(1.dp, MaterialTheme.colorScheme.outlineVariant, RoundedCornerShape(8.dp)).padding(12.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(days[i], fontWeight = FontWeight.Medium, fontSize = 14.sp, color = CamsTextPrimary)
                            Switch(checked = i < 5, onCheckedChange = { })
                        }
                    }
                }
            }
            
            // Time Slots
            CamsCard(modifier = Modifier.weight(1f).fillMaxHeight()) {
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                    Text("Time Slots", fontWeight = FontWeight.Bold, fontSize = 16.sp, color = CamsTextPrimary)
                    IconButton(onClick = { }) {
                        Icon(Icons.Filled.Add, contentDescription = "Add Slot", tint = Color(0xFF4F46E5))
                    }
                }
                Spacer(Modifier.height(8.dp))
                LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    items(6) { i ->
                        val isBreak = i == 3
                        Row(
                            modifier = Modifier.fillMaxWidth().background(if(isBreak) Color(0xFFFFF7ED) else Color.White, RoundedCornerShape(8.dp)).border(1.dp, if(isBreak) Color(0xFFFED7AA) else MaterialTheme.colorScheme.outlineVariant, RoundedCornerShape(8.dp)).padding(12.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Column {
                                Text(if(isBreak) "Lunch Break" else "Period ${if(i>3) i else i+1}", fontWeight = FontWeight.Bold, fontSize = 14.sp, color = if(isBreak) Color(0xFFC2410C) else CamsTextPrimary)
                                Text("0${9+i}:00 - ${if(10+i > 12) (10+i)-12 else 10+i}:00 ${if(10+i>=12) "PM" else "AM"}", fontSize = 12.sp, color = CamsTextSecondary)
                            }
                            IconButton(onClick = { }) {
                                Icon(Icons.Filled.Delete, contentDescription = "Remove", tint = Color(0xFFEF4444), modifier = Modifier.size(20.dp))
                            }
                        }
                    }
                }
            }
        }
        
        Spacer(Modifier.height(16.dp))
        Button(
            onClick = { },
            modifier = Modifier.fillMaxWidth(),
            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF4F46E5)),
            shape = RoundedCornerShape(12.dp),
            contentPadding = PaddingValues(16.dp)
        ) {
            Text("Save Configuration", fontSize = 16.sp, fontWeight = FontWeight.Bold)
        }
    }
}
