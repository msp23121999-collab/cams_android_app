package com.example.features.student.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.example.core.theme.*
import com.example.core.ui.CamsScreen
import com.example.core.ui.CamsCard
import com.example.features.student.widgets.StudentDrawer
import com.example.features.campus_life.providers.ActivityPointsViewModel
import com.example.features.campus_life.models.ActivityPointApplication
import com.example.core.navigation.AppRoutes
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ActivityPointsScreen(
    onNavigate: (String) -> Unit,
    viewModel: ActivityPointsViewModel = androidx.lifecycle.viewmodel.compose.viewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val scope = rememberCoroutineScope()
    
    var showNewAppModal by remember { mutableStateOf(false) }
    var selectedApp by remember { mutableStateOf<ActivityPointApplication?>(null) }
    
    var newTitle by remember { mutableStateOf("") }
    var newCategory by remember { mutableStateOf("") }
    var newPoints by remember { mutableStateOf("") }
    var newDescription by remember { mutableStateOf("") }

    CamsScreen(
        title = "Activity Points",
        subtitle = "Professional Development Tracker",
        onBackClick = { onNavigate(AppRoutes.STUDENT_DASHBOARD) },
        actions = {
            IconButton(onClick = { showNewAppModal = true }) {
                Icon(Icons.Filled.AddCircle, contentDescription = "New", tint = Color.White)
            }
        }
    ) {
        if (uiState.isLoading) {
            Box(modifier = Modifier.fillMaxWidth().height(300.dp), contentAlignment = Alignment.Center) {
                CircularProgressIndicator(color = CamsNavy)
            }
        } else if (uiState.errorMsg != null) {
            com.example.core.ui.NetworkErrorView(
                message = uiState.errorMsg!!,
                onRetry = { viewModel.fetchApplications() }
            )
        } else {
            val totalApproved = uiState.applications.sumOf { it.approvedPoints ?: 0 }
            val pendingCount = uiState.applications.count { it.status == "Pending" || it.status == "Under Verification" }

            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(24.dp)
            ) {
                // Stats Grid
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    StatCard(
                        title = "Earned",
                        value = totalApproved.toString(),
                        icon = Icons.Filled.Stars,
                        color = Color(0xFF10B981),
                        modifier = Modifier.weight(1f)
                    )
                    StatCard(
                        title = "Pending",
                        value = pendingCount.toString(),
                        icon = Icons.Filled.HourglassEmpty,
                        color = Color(0xFFF59E0B),
                        modifier = Modifier.weight(1f)
                    )
                }

                Text("Submissions Log", style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold))
                
                uiState.applications.forEach { app ->
                    ActivityLogCard(
                        app = app,
                        onClick = { selectedApp = app }
                    )
                }
            }
        }
    }
    
    // New App Modal
    if (showNewAppModal) {
        Dialog(onDismissRequest = { showNewAppModal = false }) {
            Card(
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                shape = RoundedCornerShape(16.dp),
                modifier = Modifier.fillMaxWidth().padding(16.dp)
            ) {
                Column(modifier = Modifier.padding(24.dp).verticalScroll(rememberScrollState()), verticalArrangement = Arrangement.spacedBy(16.dp)) {
                    Text("Apply for Activity Points", style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold))
                    
                    OutlinedTextField(
                        value = newTitle,
                        onValueChange = { newTitle = it },
                        label = { Text("Activity Title") },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp)
                    )
                    
                    OutlinedTextField(
                        value = newCategory,
                        onValueChange = { newCategory = it },
                        label = { Text("Category") },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp)
                    )
                    
                    OutlinedTextField(
                        value = newPoints,
                        onValueChange = { newPoints = it },
                        label = { Text("Points Claimed") },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp)
                    )
                    
                    OutlinedTextField(
                        value = newDescription,
                        onValueChange = { newDescription = it },
                        label = { Text("Description") },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp)
                    )
                    
                    Row(horizontalArrangement = Arrangement.spacedBy(12.dp), modifier = Modifier.fillMaxWidth()) {
                        OutlinedButton(
                            onClick = { showNewAppModal = false },
                            modifier = Modifier.weight(1f),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Text("Cancel", color = CamsTextSecondary)
                        }
                        Button(
                            onClick = { 
                                val points = newPoints.toIntOrNull() ?: 0
                                viewModel.submitApplication(
                                    ActivityPointApplication(
                                        id = "APP-${System.currentTimeMillis()}",
                                        title = newTitle,
                                        category = newCategory,
                                        date = "Today",
                                        claimedPoints = points,
                                        approvedPoints = null,
                                        status = "Pending",
                                        description = newDescription,
                                        supportingDocument = ""
                                    )
                                )
                                showNewAppModal = false 
                            },
                            modifier = Modifier.weight(1f),
                            colors = ButtonDefaults.buttonColors(containerColor = LexNovaPurple),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Text("Submit")
                        }
                    }
                }
            }
        }
    }
    
    // Details Modal
    if (selectedApp != null) {
        Dialog(onDismissRequest = { selectedApp = null }) {
            Card(
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                shape = RoundedCornerShape(16.dp),
                modifier = Modifier.fillMaxWidth().padding(16.dp)
            ) {
                Column(modifier = Modifier.padding(24.dp), verticalArrangement = Arrangement.spacedBy(16.dp)) {
                    Text("Application Details", style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold))
                    
                    Text("Title", fontWeight = FontWeight.Bold, fontSize = 12.sp, color = CamsTextSecondary)
                    Text(selectedApp!!.title, fontWeight = FontWeight.Bold, fontSize = 16.sp)
                    
                    Text("Status", fontWeight = FontWeight.Bold, fontSize = 12.sp, color = CamsTextSecondary)
                    Text(selectedApp!!.status, fontWeight = FontWeight.Bold, color = if(selectedApp!!.status == "Approved") Color(0xFF10B981) else Color(0xFFF59E0B))
                    
                    Text("Description", fontWeight = FontWeight.Bold, fontSize = 12.sp, color = CamsTextSecondary)
                    Text(selectedApp!!.description ?: "No description", fontSize = 14.sp)

                    Button(
                        onClick = { selectedApp = null },
                        modifier = Modifier.fillMaxWidth(),
                        colors = ButtonDefaults.buttonColors(containerColor = CamsNavy),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Text("Close")
                    }
                }
            }
        }
    }
}

@Composable
private fun StatCard(title: String, value: String, icon: androidx.compose.ui.graphics.vector.ImageVector, color: Color, modifier: Modifier) {
    Card(
        modifier = modifier,
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        border = androidx.compose.foundation.BorderStroke(1.dp, Color.LightGray.copy(alpha = 0.3f)),
        shape = RoundedCornerShape(16.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(horizontalArrangement = Arrangement.SpaceBetween, modifier = Modifier.fillMaxWidth()) {
                Text(title, style = MaterialTheme.typography.labelSmall.copy(color = CamsTextSecondary, fontWeight = FontWeight.Bold))
                Icon(icon, contentDescription = null, tint = color, modifier = Modifier.size(16.dp))
            }
            Spacer(Modifier.height(8.dp))
            Text(value, style = MaterialTheme.typography.headlineMedium.copy(fontWeight = FontWeight.Black))
        }
    }
}

@Composable
private fun ActivityLogCard(app: ActivityPointApplication, onClick: () -> Unit) {
    val statusColor = when (app.status) {
        "Approved" -> Color(0xFF10B981)
        "Rejected" -> Color(0xFFEF4444)
        "Under Verification", "Pending" -> Color(0xFF0EA5E9)
        else -> Color(0xFFF59E0B)
    }

    Card(
        onClick = onClick,
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        border = androidx.compose.foundation.BorderStroke(1.dp, Color.LightGray.copy(alpha = 0.3f)),
        shape = RoundedCornerShape(16.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
            Row(horizontalArrangement = Arrangement.SpaceBetween, modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                Text(app.id, style = MaterialTheme.typography.labelSmall.copy(fontFamily = androidx.compose.ui.text.font.FontFamily.Monospace, fontWeight = FontWeight.Bold, color = CamsTextSecondary))
                Surface(color = statusColor.copy(alpha = 0.1f), shape = RoundedCornerShape(4.dp), border = androidx.compose.foundation.BorderStroke(1.dp, statusColor.copy(alpha = 0.2f))) {
                    Text(app.status, fontSize = 12.sp, fontWeight = FontWeight.Bold, color = statusColor, modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp))
                }
            }
            Text(app.title, style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold), maxLines = 2, overflow = TextOverflow.Ellipsis)
            Row(horizontalArrangement = Arrangement.SpaceBetween, modifier = Modifier.fillMaxWidth()) {
                Column {
                    Text("Claimed", fontSize = 12.sp, color = CamsTextSecondary)
                    Text("${app.claimedPoints} pts", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = LexNovaPurple)
                }
                Column {
                    Text("Approved", fontSize = 12.sp, color = CamsTextSecondary)
                    Text(app.approvedPoints?.let { "$it pts" } ?: "-", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = Color(0xFF10B981))
                }
                Column {
                    Text("Date", fontSize = 12.sp, color = CamsTextSecondary)
                    Text(app.date, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}
