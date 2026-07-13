package com.example.features.parent.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
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
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.core.theme.*
import com.example.core.ui.CamsCard
import com.example.core.ui.CamsScreen
import com.example.features.parent.providers.ParentAttendanceViewModel
import com.example.features.parent.widgets.ParentDrawer
import kotlinx.coroutines.launch

@Composable
fun ParentAttendanceScreen(
    viewModel: ParentAttendanceViewModel,
    onNavigate: (String) -> Unit
) {
    val uiState by viewModel.uiState.collectAsState()
    val drawerState = rememberDrawerState(initialValue = DrawerValue.Closed)
    val scope = rememberCoroutineScope()

    ModalNavigationDrawer(
        drawerState = drawerState,
        drawerContent = {
            ParentDrawer(
                currentRoute = "/parent/attendance",
                onNavigate = {
                    scope.launch { drawerState.close() }
                    onNavigate(it)
                }
            )
        }
    ) {
        CamsScreen(
        scrollable = true,
            title = "Attendance",
            subtitle = "Monitoring child class participation",
            navigationIcon = {
                IconButton(onClick = { scope.launch { drawerState.open() } }) {
                    Icon(Icons.Filled.Menu, contentDescription = "Menu", tint = Color.White)
                }
            },
            actions = {
                IconButton(onClick = { onNavigate("LOGOUT") }) {
                    Icon(Icons.Filled.Logout, contentDescription = "Logout", tint = Color.White)
                }
            },
            verticalArrangement = Arrangement.spacedBy(24.dp)
        ) {
            if (uiState.isLoading) {
                Box(modifier = Modifier.fillMaxWidth().height(200.dp), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator(color = CamsNavy)
                }
            } else if (uiState.error != null) {
                Box(modifier = Modifier.fillMaxWidth().height(200.dp), contentAlignment = Alignment.Center) {
                    Text(uiState.error ?: "Failed to load attendance", color = Color.Red)
                }
            } else if (uiState.attendanceRecords.isEmpty()) {
                Box(modifier = Modifier.fillMaxWidth().height(200.dp), contentAlignment = Alignment.Center) {
                    Text("No attendance records found.", color = CamsTextSecondary)
                }
            } else {
                BoxWithConstraints {
                    val isTablet = maxWidth > 600.dp
                    Column(verticalArrangement = Arrangement.spacedBy(24.dp)) {
                        // Summary Stats
                        Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                            AttendanceStatCard("Present", uiState.attendanceRecords.count { it.status == "Present" }.toString(), Color(0xFF10B981), Modifier.weight(1f))
                            AttendanceStatCard("Absent", uiState.attendanceRecords.count { it.status == "Absent" }.toString(), Color(0xFFEF4444), Modifier.weight(1f))
                            AttendanceStatCard("Overall", "88%", CamsNavy, Modifier.weight(1f))
                        }

                Text("Attendance Calendar - June 2024", fontWeight = FontWeight.Bold, color = CamsTextPrimary)
                
                CamsCard {
                    LazyVerticalGrid(
                        columns = GridCells.Fixed(7),
                        modifier = Modifier.heightIn(min = 200.dp, max = 350.dp),
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp),
                        userScrollEnabled = false
                    ) {
                        items(uiState.attendanceRecords) { record ->
                            val color = when (record.status) {
                                "Present" -> Color(0xFF10B981)
                                "Absent" -> Color(0xFFEF4444)
                                else -> Color(0xFFF3F4F6)
                            }
                            Box(
                                modifier = Modifier
                                    .aspectRatio(1f)
                                    .background(color.copy(alpha = 0.2f), CircleShape)
                                    .background(if (record.status == "Absent") color.copy(alpha = 0.1f) else Color.Transparent, CircleShape),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    record.date.takeLast(2),
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = if (record.status == "Holiday") Color(0xFF64748B) else color
                                )
                            }
                        }
                    }
                    
                    Spacer(Modifier.height(16.dp))
                    Row(horizontalArrangement = Arrangement.spacedBy(16.dp), verticalAlignment = Alignment.CenterVertically) {
                        LegendItem("Present", Color(0xFF10B981))
                        LegendItem("Absent", Color(0xFFEF4444))
                        LegendItem("Holiday", Color(0xFF64748B))
                    }
                }

                Text("Subject-wise Analysis", fontWeight = FontWeight.Bold, color = CamsTextPrimary)
                
                uiState.subjectAttendance.forEach { subject ->
                    CamsCard {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(subject.subject, fontWeight = FontWeight.Bold, color = CamsTextPrimary, maxLines = 1, overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis)
                                Text("${subject.present}/${subject.totalClasses} Classes attended", fontSize = 12.sp, color = CamsTextSecondary, maxLines = 1, overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis)
                            }
                            Text("${subject.percentage}%", fontWeight = FontWeight.Black, fontSize = 18.sp, color = if (subject.percentage < 75) Color.Red else CamsNavy)
                        }
                        Spacer(Modifier.height(8.dp))
                        LinearProgressIndicator(
                            progress = { subject.percentage / 100f },
                            modifier = Modifier.fillMaxWidth().height(6.dp),
                            color = if (subject.percentage < 75) Color.Red else CamsNavy,
                            trackColor = MaterialTheme.colorScheme.surfaceVariant,
                            strokeCap = androidx.compose.ui.graphics.StrokeCap.Round
                        )
                    }
                }
                
                
                Spacer(Modifier.height(16.dp))
                    }
                }
            }
        }
    }
}

@Composable
private fun AttendanceStatCard(label: String, value: String, color: Color, modifier: Modifier) {
    CamsCard(modifier = modifier) {
        Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.fillMaxWidth()) {
            Text(value, fontSize = 20.sp, fontWeight = FontWeight.Black, color = color, maxLines = 1, overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis)
            Text(label, fontSize = 12.sp, fontWeight = FontWeight.Bold, color = CamsTextSecondary, maxLines = 1, overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis)
        }
    }
}

@Composable
private fun LegendItem(label: String, color: Color) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Box(Modifier.size(8.dp).background(color, CircleShape))
        Spacer(Modifier.width(4.dp))
        Text(label, fontSize = 12.sp, color = CamsTextSecondary)
    }
}
