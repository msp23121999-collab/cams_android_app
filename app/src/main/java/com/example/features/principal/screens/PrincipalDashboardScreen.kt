package com.example.features.principal.screens

import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.*
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.material3.MaterialTheme
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.features.principal.providers.PrincipalViewModel
import com.example.features.principal.models.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.core.navigation.AppRoutes
import com.example.core.theme.*
import com.example.core.ui.CamsCard
import com.example.features.principal.widgets.PrincipalBaseScreen

@Composable
fun PrincipalDashboardScreen(
    onNavigate: (String) -> Unit,
    viewModel: PrincipalViewModel = viewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    PrincipalBaseScreen(scrollable = false, 
        title = "Executive Dashboard",
        subtitle = "CAMS Institutional Oversight",
        currentRoute = AppRoutes.PRINCIPAL_DASHBOARD,
        onNavigate = onNavigate
    ) {
        LazyColumn(verticalArrangement = Arrangement.spacedBy(16.dp)) {
            // Metrics
            item {
                Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    KpiCard("Departments", uiState.metrics.totalDepartments, Icons.Filled.Business, Color(0xFF8B5CF6), Modifier.weight(1f))
                    KpiCard("Faculty", uiState.metrics.totalFaculty, Icons.Filled.People, Color(0xFFF59E0B), Modifier.weight(1f))
                }
            }
            item {
                Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    KpiCard("Students", uiState.metrics.totalStudents, Icons.Filled.School, Color(0xFFEF4444), Modifier.weight(1f))
                    KpiCard("Avg Attendance", uiState.metrics.averageAttendance, Icons.Filled.CheckCircle, Color(0xFF10B981), Modifier.weight(1f))
                }
            }

            // Attendance Overview
            item {
                Text("Today's Attendance Overview", fontWeight = FontWeight.Bold, fontSize = 18.sp, color = MaterialTheme.colorScheme.onSurface)
            }
            item {
                Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    StatCard("Students", "92%", Icons.Filled.Groups, CamsNavy, Modifier.weight(1f))
                    StatCard("Faculty", "95%", Icons.Filled.School, Color(0xFF10B981), Modifier.weight(1f))
                    StatCard("HODs", "100%", Icons.Filled.AssignmentInd, Color(0xFF8B5CF6), Modifier.weight(1f))
                }
            }

            // Institutional Academic Calendar
            item {
                CamsCard {
                    Text("Institutional Academic Calendar", fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurface)
                    Spacer(Modifier.height(12.dp))
                    if (uiState.calendarEvents.isEmpty()) {
                        Text("No upcoming events", color = MaterialTheme.colorScheme.onSurfaceVariant, fontSize = 14.sp)
                    } else {
                        uiState.calendarEvents.take(3).forEach { event ->
                            CalendarItem(event.eventName ?: event.title ?: "", event.date ?: "")
                        }
                    }
                    Spacer(Modifier.height(8.dp))
                    TextButton(onClick = { onNavigate(AppRoutes.PRINCIPAL_CALENDAR) }, modifier = Modifier.fillMaxWidth()) {
                        Text("View Full Calendar")
                    }
                }
            }

            // Quick Actions
            item {
                Text("Quick Actions", fontWeight = FontWeight.Bold, fontSize = 18.sp, color = MaterialTheme.colorScheme.onSurface)
            }
            item {
                Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    QuickActionCard("Add Calendar Event", Icons.Filled.AddCircleOutline, { onNavigate(AppRoutes.PRINCIPAL_CALENDAR) }, Modifier.weight(1f))
                    QuickActionCard("Publish Circular", Icons.Filled.Campaign, { onNavigate(AppRoutes.PRINCIPAL_CIRCULARS) }, Modifier.weight(1f))
                }
            }
            
            item { Spacer(Modifier.height(24.dp)) }
        }
    }
}

@Composable
private fun StatCard(label: String, value: String, icon: ImageVector, color: Color, modifier: Modifier) {
    CamsCard(modifier = modifier) {
        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Box(Modifier.size(36.dp).background(color.copy(alpha = 0.1f), CircleShape), contentAlignment = Alignment.Center) {
                Icon(icon, null, tint = color, modifier = Modifier.size(18.dp))
            }
            Text(value, fontWeight = FontWeight.Black, fontSize = 24.sp, color = MaterialTheme.colorScheme.onSurface)
            Text(label, fontSize = 13.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
    }
}

@Composable
private fun KpiCard(label: String, value: String, icon: ImageVector, color: Color, modifier: Modifier) {
    CamsCard(modifier = modifier) {
        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            Box(Modifier.size(40.dp).background(color.copy(alpha = 0.1f), RoundedCornerShape(8.dp)), contentAlignment = Alignment.Center) {
                Icon(icon, null, tint = color, modifier = Modifier.size(24.dp))
            }
            Column {
                Text(value, fontWeight = FontWeight.Bold, fontSize = 20.sp, color = MaterialTheme.colorScheme.onSurface)
                Text(label, fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
        }
    }
}

@Composable
private fun CalendarItem(title: String, date: String) {
    Row(Modifier.fillMaxWidth().padding(vertical = 8.dp), verticalAlignment = Alignment.CenterVertically) {
        Box(Modifier.size(8.dp).background(CamsNavy, CircleShape))
        Spacer(Modifier.width(12.dp))
        Column {
            Text(title, fontWeight = FontWeight.SemiBold, fontSize = 14.sp, color = MaterialTheme.colorScheme.onSurface)
            Text(date, fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
    }
}

@Composable
private fun QuickActionCard(label: String, icon: ImageVector, onClick: () -> Unit, modifier: Modifier) {
    Surface(
        shape = RoundedCornerShape(12.dp),
        color = Color.White,
        modifier = modifier.clickable(onClick = onClick)
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Icon(icon, null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(32.dp))
            Text(label, fontSize = 12.sp, fontWeight = FontWeight.Medium, color = MaterialTheme.colorScheme.onSurface, textAlign = androidx.compose.ui.text.style.TextAlign.Center)
        }
    }
}
