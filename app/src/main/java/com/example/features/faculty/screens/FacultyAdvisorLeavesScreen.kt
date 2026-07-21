package com.example.features.faculty.screens

import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.core.theme.*
import com.example.features.faculty.widgets.FacultyBaseScreen

import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.core.network.AdvisorLeaveDto
import com.example.core.repository.FacultyRepositoryImpl
import com.example.features.faculty.providers.FacultyAdvisorLeavesViewModel
import com.example.features.faculty.providers.FacultyAdvisorLeavesViewModelFactory

@Composable
fun FacultyAdvisorLeavesScreen(onNavigate: (String) -> Unit) {
    val context = LocalContext.current
    val repository = remember { FacultyRepositoryImpl(com.example.CamsApplication.instance.container.apiService) }
    val factory = remember { FacultyAdvisorLeavesViewModelFactory(repository) }
    val viewModel: FacultyAdvisorLeavesViewModel = viewModel(factory = factory)
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    LaunchedEffect(uiState.reviewError) {
        uiState.reviewError?.let {
            Toast.makeText(context, it, Toast.LENGTH_SHORT).show()
            viewModel.clearReviewError()
        }
    }

    val pending = uiState.leaves.filter { it.status == "PENDING" }

    FacultyBaseScreen(scrollable = false,
        title = "Advisor Leave Approvals",
        currentRoute = com.example.core.navigation.AppRoutes.FACULTY_ADVISOR_LEAVES,
        onNavigate = onNavigate
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp)
        ) {
            Text("Pending Requests", fontWeight = FontWeight.Bold, fontSize = 18.sp, color = MaterialTheme.colorScheme.onSurface)
            Spacer(modifier = Modifier.height(12.dp))

            uiState.error?.let {
                Text(it, color = Color(0xFFB91C1C), fontSize = 13.sp, modifier = Modifier.padding(bottom = 8.dp))
            }

            if (uiState.isLoading) {
                Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator(color = CamsNavy)
                }
            } else if (pending.isEmpty()) {
                Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Text("No pending leave requests from your students", color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            } else {
                LazyColumn(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    items(pending, key = { it.id }) { leave ->
                        AdvisorLeaveItem(
                            leave = leave,
                            onApprove = { viewModel.reviewLeave(leave.id, "APPROVED", null) },
                            onReject = { viewModel.reviewLeave(leave.id, "REJECTED", null) }
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun AdvisorLeaveItem(leave: AdvisorLeaveDto, onApprove: () -> Unit, onReject: () -> Unit) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        shape = RoundedCornerShape(16.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(horizontalArrangement = Arrangement.spacedBy(12.dp), verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(40.dp)
                            .background(CamsNavy.copy(alpha = 0.1f), CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Text((leave.userName ?: "?").take(1), fontWeight = FontWeight.Bold, color = CamsNavy)
                    }
                    Column {
                        Text(leave.userName ?: "Unknown Student", fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurface)
                        Text(leave.type, fontSize = 12.sp, color = CamsNavy)
                    }
                }
                Text("${leave.fromDate} - ${leave.toDate}", fontSize = 13.sp, color = Color(0xFF64748B))
            }
            Spacer(modifier = Modifier.height(12.dp))
            Text("Reason: ${leave.reason}", fontSize = 13.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
            Spacer(modifier = Modifier.height(16.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Button(
                    onClick = onApprove,
                    modifier = Modifier.weight(1f),
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF10B981)),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Icon(Icons.Filled.Check, null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Approve")
                }
                OutlinedButton(
                    onClick = onReject,
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.outlinedButtonColors(contentColor = Color.Red),
                    border = androidx.compose.foundation.BorderStroke(1.dp, Color.Red.copy(alpha = 0.3f))
                ) {
                    Icon(Icons.Filled.Close, null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Reject")
                }
            }
        }
    }
}
