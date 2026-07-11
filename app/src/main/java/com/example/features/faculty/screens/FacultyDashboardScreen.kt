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
import androidx.compose.runtime.*
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.features.faculty.providers.FacultyDashboardViewModel
import com.example.features.faculty.models.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.core.theme.*
import com.example.core.ui.CamsCard
import com.example.features.faculty.widgets.FacultyBaseScreen
import com.example.core.navigation.AppRoutes

@Composable
fun FacultyDashboardScreen(
    onNavigate: (String) -> Unit,
    viewModel: FacultyDashboardViewModel
) {
    val uiState by viewModel.uiState.collectAsState()
    val metrics = uiState.metrics
    val subjects = uiState.assignedSubjects
    // For now, since recentActivities and upcomingEvents were hardcoded in the old VM and not in current state, 
    // I will use empty lists or maintain them if they are important. 
    // Actually, I'll update the State to include them for consistency if they are used in UI.
    val activities = uiState.recentActivities
    val events = uiState.upcomingEvents

    FacultyBaseScreen(scrollable = true, 
        title = "Faculty Portal",
        subtitle = "Manage your classes and assessments",
        currentRoute = AppRoutes.FACULTY_DASHBOARD,
        onNavigate = onNavigate
    ) {
        // Stats Row
        Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            StatCard("Classes Today", metrics.classesToday, Icons.Filled.Event, CamsNavy, Modifier.weight(1f))
            StatCard("Assignments", metrics.pendingAssignments, Icons.Filled.Assignment, Color(0xFF8B5CF6), Modifier.weight(1f))
        }
        Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            StatCard("Attendance Due", metrics.pendingAttendance, Icons.Filled.CheckCircle, Color(0xFF10B981), Modifier.weight(1f))
            StatCard("Leaves Left", metrics.leaveBalance, Icons.Filled.FlightTakeoff, Color(0xFFF59E0B), Modifier.weight(1f))
        }

        Text("Today's Schedule", fontWeight = FontWeight.Bold, color = CamsTextPrimary, fontSize = 18.sp)
        CamsCard {
            if (subjects.isEmpty()) {
                Text("No classes scheduled for today", modifier = Modifier.padding(16.dp))
            } else {
                subjects.take(3).forEach { subject ->
                    ScheduleItem("09:00 AM", subject.subjectName, "Room ${subject.subjectCode.takeLast(3)}")
                }
            }
        }

        Text("Upcoming Events", fontWeight = FontWeight.Bold, color = CamsTextPrimary, fontSize = 18.sp)
        CamsCard {
            events.forEach { event ->
                Row(modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp), verticalAlignment = Alignment.CenterVertically) {
                    Box(Modifier.size(8.dp).background(event.color, CircleShape))
                    Spacer(Modifier.width(12.dp))
                    Column {
                        Text(event.title, fontWeight = FontWeight.Bold, fontSize = 14.sp, color = CamsTextPrimary)
                        Text("${event.date} • ${event.time}", fontSize = 12.sp, color = CamsTextSecondary)
                    }
                }
            }
        }

        Text("Recent Activities", fontWeight = FontWeight.Bold, color = CamsTextPrimary, fontSize = 18.sp)
        CamsCard {
            activities.forEach { activity ->
                Row(modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.SpaceBetween) {
                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                        Icon(activity.icon, null, tint = activity.color, modifier = Modifier.size(20.dp))
                        Column {
                            Text(activity.title, fontWeight = FontWeight.Bold, fontSize = 14.sp, color = CamsTextPrimary)
                            Text(activity.time, fontSize = 12.sp, color = CamsTextSecondary)
                        }
                    }
                    Icon(Icons.Filled.ChevronRight, null, tint = CamsTextSecondary)
                }
            }
        }
    }
}

@Composable
private fun StatCard(label: String, value: String, icon: ImageVector, color: Color, modifier: Modifier) {
    CamsCard(modifier = modifier) {
        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            Box(Modifier.size(32.dp).background(color.copy(alpha = 0.1f), CircleShape), contentAlignment = Alignment.Center) {
                Icon(icon, null, tint = color, modifier = Modifier.size(16.dp))
            }
            Column {
                Text(value, fontWeight = FontWeight.Black, fontSize = 20.sp, color = CamsTextPrimary)
                Text(label, fontSize = 12.sp, fontWeight = FontWeight.Bold, color = CamsTextSecondary)
            }
        }
    }
}

@Composable
private fun ScheduleItem(time: String, subject: String, room: String) {
    Row(modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp), verticalAlignment = Alignment.CenterVertically) {
        Column(Modifier.width(80.dp)) {
            Text(time, fontWeight = FontWeight.Bold, fontSize = 12.sp, color = CamsTextPrimary)
        }
        Column {
            Text(subject, fontWeight = FontWeight.Bold, fontSize = 14.sp, color = CamsTextPrimary)
            Text(room, fontSize = 13.sp, color = CamsTextSecondary)
        }
    }
}

@Composable
private fun TaskItem(title: String, subtitle: String) {
    Row(modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.SpaceBetween) {
        Column {
            Text(title, fontWeight = FontWeight.Bold, fontSize = 14.sp, color = CamsTextPrimary)
            Text(subtitle, fontSize = 13.sp, color = CamsTextSecondary)
        }
        Icon(Icons.Filled.ChevronRight, null, tint = CamsTextSecondary)
    }
}
