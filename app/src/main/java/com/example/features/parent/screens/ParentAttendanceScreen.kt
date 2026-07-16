package com.example.features.parent.screens

import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.*
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
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
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
                    Icon(Icons.AutoMirrored.Filled.Logout, contentDescription = "Logout", tint = Color.White)
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
            } else if (uiState.summary == null || uiState.summary?.records?.isEmpty() == true) {
                Box(modifier = Modifier.fillMaxWidth().height(200.dp), contentAlignment = Alignment.Center) {
                    Text("No attendance records found.", color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            } else {
                val summary = uiState.summary!!
                BoxWithConstraints {
                    val isTablet = maxWidth > 600.dp
                    Column(verticalArrangement = Arrangement.spacedBy(24.dp)) {
                        // Summary Stats
                        Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                            AttendanceStatCard("Present", summary.present.toString(), Color(0xFF10B981), Modifier.weight(1f))
                            AttendanceStatCard("Absent", summary.absent.toString(), Color(0xFFEF4444), Modifier.weight(1f))
                            AttendanceStatCard("Overall", "${summary.percentage.toInt()}%", CamsNavy, Modifier.weight(1f))
                        }

                Text("Attendance Calendar - June 2024", fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurface)
                
                CamsCard {
                    LazyVerticalGrid(
                        columns = GridCells.Fixed(7),
                        modifier = Modifier.heightIn(min = 200.dp, max = 350.dp),
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp),
                        userScrollEnabled = false
                    ) {
                        items(summary.records) { record ->
                            val color = when (record.status) {
                                "Present" -> Color(0xFF10B981)
                                "Absent" -> Color(0xFFEF4444)
                                else -> Color(0xFFF3F4F6)
                            }
                            Box(
                                modifier = Modifier
                                    .aspectRatio(1f)
                                    .background(color.copy(alpha = 0.2f), CircleShape),
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

                if (uiState.subjectAttendance.isNotEmpty()) {
                    Text("Subject-wise Analysis", fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurface)
                    
                    uiState.subjectAttendance.forEach { subject ->
                        CamsCard {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(subject.subject, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurface, maxLines = 1, overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis)
                                    Text("${subject.present}/${subject.totalClasses} Classes attended", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant, maxLines = 1, overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis)
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
            Text(label, fontSize = 12.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurfaceVariant, maxLines = 1, overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis)
        }
    }
}

@Composable
private fun LegendItem(label: String, color: Color) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Box(Modifier.size(8.dp).background(color, CircleShape))
        Spacer(Modifier.width(4.dp))
        Text(label, fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
    }
}
