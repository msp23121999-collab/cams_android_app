package com.example.features.faculty.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.core.theme.*
import com.example.core.ui.CamsCard
import com.example.core.ui.CamsScreen

import androidx.compose.runtime.*
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.features.faculty.providers.FacultyTimetableViewModel

data class TimetableSlot(
    val startTime: String,
    val endTime: String,
    val subject: String,
    val batch: String,
    val room: String
)

@Composable
fun FacultyTimetableScreen(
    onNavigate: (String) -> Unit,
    viewModel: FacultyTimetableViewModel
) {
    val uiState by viewModel.uiState.collectAsState()
    val days = listOf("Monday", "Tuesday", "Wednesday", "Thursday", "Friday", "Saturday")
    var selectedDay by remember { mutableStateOf("Monday") }

    com.example.features.faculty.widgets.FacultyBaseScreen(
        title = "Class Timetable",
        subtitle = "Your daily schedule and room allocations",
        currentRoute = "/faculty/timetable",
        onNavigate = onNavigate
    ) {
        // Day Selector
        Row(
            modifier = Modifier.fillMaxWidth().padding(bottom = 16.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            days.forEach { day ->
                val shortDay = day.take(3).uppercase()
                DayChip(shortDay, selectedDay == day) {
                    selectedDay = day
                }
            }
        }

        if (uiState.isLoading) {
            Box(Modifier.fillMaxWidth().height(200.dp), contentAlignment = Alignment.Center) {
                CircularProgressIndicator(color = CamsNavy)
            }
        } else {
            val timetableForDay = uiState.timetable.find { d -> d.dayName == selectedDay }
            if (timetableForDay == null || timetableForDay.periods.isEmpty()) {
                Box(Modifier.fillMaxWidth().padding(40.dp), contentAlignment = Alignment.Center) {
                    Text("No classes scheduled for $selectedDay", color = CamsTextSecondary)
                }
            } else {
                timetableForDay.periods.forEach { period ->
                    TimetableCard(
                        TimetableSlot(
                            startTime = period.time.split("-").firstOrNull()?.trim() ?: "N/A",
                            endTime = period.time.split("-").lastOrNull()?.trim() ?: "N/A",
                            subject = period.subjectName,
                            batch = period.instructor ?: "N/A", 
                            room = period.room
                        )
                    )
                }
            }
        }
    }
}

@Composable
private fun DayChip(label: String, isSelected: Boolean, onClick: () -> Unit = {}) {
    Surface(
        onClick = onClick,
        color = if (isSelected) CamsNavy else Color.White,
        shape = RoundedCornerShape(12.dp),
        border = if (isSelected) null else androidx.compose.foundation.BorderStroke(1.dp, Color.LightGray),
        modifier = Modifier.width(50.dp)
    ) {
        Box(
            modifier = Modifier.padding(vertical = 8.dp),
            contentAlignment = Alignment.Center
        ) {
            Text(
                label,
                fontSize = 13.sp,
                fontWeight = FontWeight.Bold,
                color = if (isSelected) Color.White else CamsTextSecondary
            )
        }
    }
}

@Composable
private fun TimetableCard(slot: TimetableSlot) {
    CamsCard {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Column(
                modifier = Modifier.width(80.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(slot.startTime, fontWeight = FontWeight.Black, fontSize = 14.sp, color = CamsTextPrimary)
                Icon(Icons.Filled.AccessTime, null, tint = Color.LightGray, modifier = Modifier.size(14.dp))
                Text(slot.endTime, fontSize = 13.sp, color = Color(0xFF64748B))
            }
            
            HorizontalDivider(modifier = Modifier.height(50.dp).width(1.dp), color = Color(0xFFF3F4F6))
            
            Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Text(slot.subject, fontWeight = FontWeight.Bold, fontSize = 16.sp, color = if (slot.subject == "LUNCH BREAK") Color(0xFF64748B) else CamsTextPrimary)
                if (slot.batch.isNotEmpty()) {
                    Text(slot.batch, fontSize = 13.sp, color = CamsNavy, fontWeight = FontWeight.Bold)
                }
                if (slot.room.isNotEmpty()) {
                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                        Icon(Icons.Filled.LocationOn, null, tint = Color(0xFF64748B), modifier = Modifier.size(12.dp))
                        Text(slot.room, fontSize = 13.sp, color = Color(0xFF64748B))
                    }
                }
            }
        }
    }
}


