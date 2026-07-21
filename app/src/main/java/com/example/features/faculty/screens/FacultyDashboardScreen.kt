package com.example.features.faculty.screens

import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.*
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
import com.example.features.faculty.widgets.FacultyBaseScreen
import com.example.core.navigation.AppRoutes
import com.example.features.faculty.providers.FacultyDashboardViewModel
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FacultyDashboardScreen(
    onNavigate: (String) -> Unit,
    viewModel: FacultyDashboardViewModel
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val metrics = uiState.metrics
    val subjects = uiState.assignedSubjects
    val activities = uiState.recentActivities
    val events = uiState.upcomingEvents
    
    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()

    LaunchedEffect(uiState.error) {
        uiState.error?.let {
            scope.launch {
                snackbarHostState.showSnackbar(it)
                viewModel.clearError()
            }
        }
    }

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
        contentWindowInsets = WindowInsets(0, 0, 0, 0)
    ) { paddingValues ->
        FacultyBaseScreen(
            scrollable = true, 
            title = "Faculty Portal",
            subtitle = "Manage your classes and assessments",
            currentRoute = AppRoutes.FACULTY_DASHBOARD,
            onNavigate = onNavigate
        ) {
            Box(modifier = Modifier.padding(paddingValues)) {
                Column(modifier = Modifier.fillMaxSize()) {
                    // Offline Banner
                    if (uiState.isOffline) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .background(Color(0xFFEF4444))
                                .padding(8.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.Filled.CloudOff, contentDescription = "Offline", tint = Color.White, modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(8.dp))
                                Text("No internet connection", color = Color.White, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                            }
                        }
                    }

                    // Refresh Button (Fallback for Pull-to-Refresh)
                    if (uiState.isLoading || uiState.isRefreshing) {
                        LinearProgressIndicator(modifier = Modifier.fillMaxWidth(), color = CamsNavy)
                    } else {
                        TextButton(
                            onClick = { viewModel.refresh() },
                            modifier = Modifier.align(Alignment.End)
                        ) {
                            Icon(Icons.Filled.Refresh, contentDescription = "Refresh", modifier = Modifier.size(16.dp))
                            Spacer(Modifier.width(4.dp))
                            Text("Refresh")
                        }
                    }

                    if (uiState.isLoading && metrics.classesToday.isEmpty()) {
                        // Skeleton Loader
                        repeat(3) {
                            CamsCard {
                                Box(modifier = Modifier.fillMaxWidth().height(100.dp).background(Color.LightGray.copy(alpha = 0.3f)))
                            }
                        }
                    } else {
                        // Stats Row
                        Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                            StatCard("Classes Today", metrics.classesToday, Icons.Filled.Event, CamsNavy, Modifier.weight(1f))
                            StatCard("Assignments", metrics.pendingAssignments, Icons.AutoMirrored.Filled.Assignment, Color(0xFF8B5CF6), Modifier.weight(1f))
                        }
                        Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                            StatCard("Attendance Due", metrics.pendingAttendance, Icons.Filled.CheckCircle, Color(0xFF10B981), Modifier.weight(1f))
                            StatCard("Leaves Left", metrics.leaveBalance, Icons.Filled.FlightTakeoff, Color(0xFFF59E0B), Modifier.weight(1f))
                        }

                        Spacer(Modifier.height(16.dp))
                        Text("Today's Schedule", fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurface, fontSize = 18.sp)
                        CamsCard {
                            if (subjects.isEmpty()) {
                                EmptyState("No classes scheduled for today")
                            } else {
                                subjects.take(3).forEach { subject ->
                                    ScheduleItem("09:00 AM", subject.subjectName, "Room ${subject.subjectCode.takeLast(3)}")
                                }
                            }
                        }

                        Spacer(Modifier.height(16.dp))
                        Text("Upcoming Events", fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurface, fontSize = 18.sp)
                        CamsCard {
                            if (events.isEmpty()) {
                                EmptyState("No upcoming events")
                            } else {
                                events.forEach { event ->
                                    Row(modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp), verticalAlignment = Alignment.CenterVertically) {
                                        Box(Modifier.size(8.dp).background(event.color, CircleShape))
                                        Spacer(Modifier.width(12.dp))
                                        Column {
                                            Text(event.title, fontWeight = FontWeight.Bold, fontSize = 14.sp, color = MaterialTheme.colorScheme.onSurface)
                                            if (event.date.isNotEmpty()) {
                                                Text(event.date, fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                            }
                                        }
                                    }
                                }
                            }
                        }

                        Spacer(Modifier.height(16.dp))
                        Text("Recent Activities", fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurface, fontSize = 18.sp)
                        CamsCard {
                            if (activities.isEmpty()) {
                                EmptyState("No recent activities")
                            } else {
                                activities.forEach { activity ->
                                    Row(modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.SpaceBetween) {
                                        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                                            Icon(activity.icon, null, tint = activity.color, modifier = Modifier.size(20.dp))
                                            Column {
                                                Text(activity.title, fontWeight = FontWeight.Bold, fontSize = 14.sp, color = MaterialTheme.colorScheme.onSurface)
                                                Text(activity.time, fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                            }
                                        }
                                    }
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
private fun EmptyState(message: String) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Icon(Icons.Filled.Inbox, contentDescription = "Empty", tint = Color.LightGray, modifier = Modifier.size(48.dp))
        Spacer(Modifier.height(8.dp))
        Text(message, color = Color.Gray, fontSize = 14.sp, fontWeight = FontWeight.Medium)
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
                Text(value, fontWeight = FontWeight.Black, fontSize = 20.sp, color = MaterialTheme.colorScheme.onSurface)
                Text(label, fontSize = 12.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
        }
    }
}

@Composable
private fun ScheduleItem(time: String, subject: String, room: String) {
    Row(modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp), verticalAlignment = Alignment.CenterVertically) {
        Column(Modifier.width(80.dp)) {
            Text(time, fontWeight = FontWeight.Bold, fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurface)
        }
        Column {
            Text(subject, fontWeight = FontWeight.Bold, fontSize = 14.sp, color = MaterialTheme.colorScheme.onSurface)
            Text(room, fontSize = 13.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
    }
}
