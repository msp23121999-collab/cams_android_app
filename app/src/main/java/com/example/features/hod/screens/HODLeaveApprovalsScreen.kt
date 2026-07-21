package com.example.features.hod.screens

import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.material3.MaterialTheme
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.features.leave.models.LeaveStatuses
import com.example.core.theme.*
import com.example.core.ui.CamsCard
import com.example.features.hod.widgets.HODBaseScreen
import com.example.core.navigation.AppRoutes
import com.example.features.hod.providers.HODLeaveApprovalsViewModel

@Composable
fun HODLeaveApprovalsScreen(
    onNavigate: (String) -> Unit,
    viewModel: HODLeaveApprovalsViewModel = viewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    // Note: the backend's HOD leave-approval endpoints only ever return leave
    // requests from faculty who report to (or share a department with) this HOD
    // — there is no "student leave" concept in this data source, so this screen
    // shows a single faculty-leave list rather than a fake student/faculty tab split.
    val filteredLeaves = uiState.pendingLeaves

    HODBaseScreen(
        title = "Leave Approvals",
        subtitle = "Manage faculty and student leave requests",
        currentRoute = AppRoutes.HOD_LEAVE_APPROVALS,
        onNavigate = onNavigate
    ) {
        val totalPending = uiState.pendingLeaves.count { it.status == "PENDING_HOD" || it.status == "PENDING" }
        val totalApproved = uiState.pendingLeaves.count { it.status in LeaveStatuses.HOD_APPROVED_SET }
        val totalRejected = uiState.pendingLeaves.count { it.status in LeaveStatuses.HOD_REJECTED_SET }

        Row(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.fillMaxWidth()) {
            KpiCard("Pending", "$totalPending", Icons.Filled.Schedule, Color(0xFFD97706), Modifier.weight(1f))
            KpiCard("Approved", "$totalApproved", Icons.Filled.CheckCircle, Color(0xFF059669), Modifier.weight(1f))
            KpiCard("Rejected", "$totalRejected", Icons.Filled.Cancel, Color(0xFFE11D48), Modifier.weight(1f))
            KpiCard("Total Requests", "${uiState.pendingLeaves.size}", Icons.Filled.Description, Color(0xFF7C3AED), Modifier.weight(1f))
        }

        Spacer(Modifier.height(16.dp))

        uiState.error?.let {
            Text(it, color = Color(0xFFB91C1C), fontSize = 13.sp, modifier = Modifier.padding(bottom = 8.dp))
        }

        if (uiState.isLoading) {
            Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator()
            }
        } else if (filteredLeaves.isEmpty()) {
            Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Text("No leave requests found.", color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
        } else {
            LazyColumn(verticalArrangement = Arrangement.spacedBy(12.dp), modifier = Modifier.weight(1f)) {
                items(filteredLeaves) { leave ->
                    val isPending = leave.status == "PENDING_HOD" || leave.status == "PENDING"
                    val statusColor = if (isPending) Color(0xFFB45309) else if (leave.status.contains("APPROVED")) Color(0xFF059669) else Color(0xFFE11D48)
                    val statusBg = if (isPending) Color(0xFFFEF3C7) else if (leave.status.contains("APPROVED")) Color(0xFFD1FAE5) else Color(0xFFFFE4E6)
                    
                    CamsCard(modifier = Modifier.fillMaxWidth()) {
                        Column(Modifier.padding(16.dp)) {
                            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                Text(
                                    leave.status.replace("_", " "), 
                                    fontSize = 12.sp, 
                                    fontWeight = FontWeight.Bold, 
                                    color = statusColor, 
                                    modifier = Modifier.background(statusBg, RoundedCornerShape(4.dp)).padding(horizontal = 6.dp, vertical = 2.dp)
                                )
                                Text(leave.type ?: "Leave", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant, fontWeight = FontWeight.Bold)
                            }
                            Spacer(Modifier.height(12.dp))
                            Text(leave.userName ?: "Unknown", fontWeight = FontWeight.Bold, fontSize = 16.sp, color = MaterialTheme.colorScheme.onSurface)
                            Text("From: ${leave.startDate} To: ${leave.endDate}", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            Spacer(Modifier.height(8.dp))
                            Text("Reason: ${leave.reason}", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant, fontStyle = androidx.compose.ui.text.font.FontStyle.Italic)
                            
                            if (isPending) {
                                Spacer(Modifier.height(16.dp))
                                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                    Button(
                                        onClick = { viewModel.approveLeave(leave.id, LeaveStatuses.REJECTED_BY_HOD, LeaveStatuses.REMARK_HOD_REJECTED) }, 
                                        modifier = Modifier.weight(1f), 
                                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFFEF2F2), contentColor = Color(0xFFE11D48)), 
                                        shape = RoundedCornerShape(8.dp)
                                    ) {
                                        Icon(Icons.Filled.ThumbDown, null, modifier = Modifier.size(16.dp))
                                        Spacer(Modifier.width(4.dp))
                                        Text("Reject", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                                    }
                                    Button(
                                        onClick = { viewModel.approveLeave(leave.id, LeaveStatuses.APPROVED_BY_HOD, LeaveStatuses.REMARK_HOD_APPROVED) }, 
                                        modifier = Modifier.weight(1f), 
                                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF059669)), 
                                        shape = RoundedCornerShape(8.dp)
                                    ) {
                                        Icon(Icons.Filled.ThumbUp, null, modifier = Modifier.size(16.dp))
                                        Spacer(Modifier.width(4.dp))
                                        Text("Approve", fontSize = 12.sp, fontWeight = FontWeight.Bold)
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
private fun KpiCard(label: String, value: String, icon: androidx.compose.ui.graphics.vector.ImageVector, color: Color, modifier: Modifier) {
    Card(modifier = modifier, colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface), elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)) {
        Column(modifier = Modifier.padding(12.dp)) {
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.Top) {
                Text(label, fontSize = 13.sp, fontWeight = FontWeight.Black, color = Color(0xFF64748B), modifier = Modifier.weight(1f))
                Box(Modifier.background(color.copy(alpha=0.1f), RoundedCornerShape(8.dp)).padding(4.dp)) {
                    Icon(icon, null, tint = color, modifier = Modifier.size(16.dp))
                }
            }
            Text(value, fontSize = 20.sp, fontWeight = FontWeight.Black, color = MaterialTheme.colorScheme.onSurface, modifier = Modifier.padding(top = 8.dp))
        }
    }
}

