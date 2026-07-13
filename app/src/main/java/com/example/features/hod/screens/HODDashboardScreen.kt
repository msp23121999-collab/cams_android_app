package com.example.features.hod.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.*
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.features.hod.providers.HODViewModel
import com.example.features.hod.models.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.core.navigation.AppRoutes


import com.example.core.theme.CamsNavy
import com.example.core.theme.CamsTextPrimary
import com.example.core.theme.CamsTextSecondary
import com.example.features.hod.widgets.HODBaseScreen

@Composable
fun HODDashboardScreen(
    onNavigate: (String) -> Unit,
    viewModel: HODViewModel = viewModel()
) {
    val uiState by viewModel.uiState.collectAsState()

    HODBaseScreen(scrollable = true, 
        title = "HOD Command Center",
        subtitle = "Department Overview & Management",
        currentRoute = AppRoutes.HOD_DASHBOARD,
        onNavigate = onNavigate
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.background)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Quick Stats Grid
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.fillMaxWidth()) {
                DashboardStatCard("Total Faculty", uiState.metrics.totalFaculty, Icons.Filled.Group, Color(0xFF7C3AED), MaterialTheme.colorScheme.secondaryContainer, Modifier.weight(1f))
                DashboardStatCard("Total Students", uiState.metrics.totalStudents, Icons.Filled.School, Color(0xFF2563EB), Color(0xFFEFF6FF), Modifier.weight(1f))
            }
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.fillMaxWidth()) {
                DashboardStatCard("Pending Approvals", uiState.metrics.pendingApprovals, Icons.Filled.Warning, Color(0xFFD97706), Color(0xFFFFFBEB), Modifier.weight(1f))
                DashboardStatCard("Active Subjects", uiState.metrics.activeSubjects, Icons.Filled.MenuBook, Color(0xFF059669), Color(0xFFECFDF5), Modifier.weight(1f))
            }

            // Academic Operations
            Text("Academic Operations", fontSize = 16.sp, fontWeight = FontWeight.Bold, color = CamsTextPrimary, modifier = Modifier.padding(top = 8.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.fillMaxWidth()) {
                ActionCard("Subject\nAllocation", Icons.Filled.MenuBook, Color(0xFF4F46E5), Color(0xFFEEF2FF), Modifier.weight(1f)) { onNavigate(AppRoutes.HOD_SUBJECT_ALLOCATION) }
                ActionCard("Class\nAdvisors", Icons.Filled.Groups, Color(0xFF8B5CF6), MaterialTheme.colorScheme.secondaryContainer, Modifier.weight(1f)) { onNavigate(AppRoutes.HOD_CLASS_ADVISOR) }
                ActionCard("Timetable\nSetup", Icons.Filled.CalendarMonth, Color(0xFF0EA5E9), Color(0xFFF0F9FF), Modifier.weight(1f)) { onNavigate(AppRoutes.HOD_TIMETABLE_SETUP) }
            }

            // Monitoring & Approvals
            Text("Monitoring & Approvals", fontSize = 16.sp, fontWeight = FontWeight.Bold, color = CamsTextPrimary, modifier = Modifier.padding(top = 8.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.fillMaxWidth()) {
                ActionCard("Faculty\nLeaves", Icons.Filled.AssignmentTurnedIn, Color(0xFFE11D48), Color(0xFFFFF1F2), Modifier.weight(1f)) { onNavigate(AppRoutes.HOD_LEAVE_APPROVALS) }
                ActionCard("Marks\nApproval", Icons.Filled.FactCheck, Color(0xFF059669), Color(0xFFECFDF5), Modifier.weight(1f)) { onNavigate(AppRoutes.HOD_MARK_APPROVALS) }
                ActionCard("Attendance\nShortages", Icons.Filled.ReportProblem, Color(0xFFD97706), Color(0xFFFFFBEB), Modifier.weight(1f)) { onNavigate(AppRoutes.HOD_ATTENDANCE_MONITORING) }
            }

            // Recent Activity
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text("Recent Activity", fontSize = 16.sp, fontWeight = FontWeight.Bold, color = CamsTextPrimary)
                    Spacer(Modifier.height(16.dp))
                    
                    uiState.activities.forEachIndexed { index, activity ->
                        ActivityItem(activity.title, activity.time, Icons.Filled.Circle, Color(0xFF3B82F6))
                        if (index < uiState.activities.size - 1) {
                            HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant, modifier = Modifier.padding(vertical = 12.dp))
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun DashboardStatCard(label: String, value: String, icon: ImageVector, color: Color, bgColor: Color, modifier: Modifier) {
    Card(
        modifier = modifier.height(110.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
        shape = RoundedCornerShape(16.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.SpaceBetween, horizontalAlignment = Alignment.Start) {
            Box(Modifier.size(36.dp).background(bgColor, RoundedCornerShape(8.dp)), contentAlignment = Alignment.Center) {
                Icon(icon, null, tint = color, modifier = Modifier.size(20.dp))
            }
            Spacer(modifier = Modifier.height(12.dp))
            Text(label, fontSize = 12.sp, color = CamsTextSecondary, fontWeight = FontWeight.Medium)
            Text(value, fontWeight = FontWeight.Bold, fontSize = 24.sp, color = CamsTextPrimary)
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun ActionCard(title: String, icon: ImageVector, color: Color, bgColor: Color, modifier: Modifier, onClick: () -> Unit) {
    Card(
        modifier = modifier.height(110.dp),
        onClick = onClick,
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
        shape = RoundedCornerShape(16.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.Center, horizontalAlignment = Alignment.CenterHorizontally) {
            Box(Modifier.size(40.dp).background(bgColor, RoundedCornerShape(12.dp)), contentAlignment = Alignment.Center) {
                Icon(icon, null, tint = color, modifier = Modifier.size(20.dp))
            }
            Spacer(modifier = Modifier.height(12.dp))
            Text(title, fontSize = 13.sp, color = CamsTextPrimary, fontWeight = FontWeight.SemiBold, maxLines = 2, textAlign = androidx.compose.ui.text.style.TextAlign.Center)
        }
    }
}

@Composable
private fun ActivityItem(title: String, time: String, icon: ImageVector, color: Color) {
    Row(verticalAlignment = Alignment.Top, horizontalArrangement = Arrangement.spacedBy(12.dp)) {
        Box(modifier = Modifier.padding(top = 4.dp)) {
            Icon(icon, contentDescription = null, tint = color, modifier = Modifier.size(10.dp))
        }
        Column {
            Text(title, fontSize = 14.sp, color = CamsTextPrimary, fontWeight = FontWeight.Medium)
            Spacer(Modifier.height(4.dp))
            Text(time, fontSize = 12.sp, color = CamsTextSecondary)
        }
    }
}
