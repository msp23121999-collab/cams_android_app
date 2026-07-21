package com.example.features.hod.screens

import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
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
import com.example.core.ui.CamsCard
import com.example.features.hod.widgets.HODBaseScreen
import com.example.core.navigation.AppRoutes

import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.core.repository.HODRepositoryImpl
import com.example.features.hod.providers.HODProfileApprovalsViewModel
import com.example.features.hod.providers.HODProfileApprovalsViewModelFactory

@Composable
fun HODProfileApprovalsScreen(onNavigate: (String) -> Unit) {
    val context = LocalContext.current
    var activeTab by remember { mutableStateOf("faculty") }
    val repository = remember { HODRepositoryImpl(com.example.CamsApplication.instance.container.apiService) }
    val factory = remember { HODProfileApprovalsViewModelFactory(repository) }
    val viewModel: HODProfileApprovalsViewModel = viewModel(factory = factory)
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    LaunchedEffect(uiState.reviewError) {
        uiState.reviewError?.let {
            Toast.makeText(context, it, Toast.LENGTH_SHORT).show()
            viewModel.clearReviewError()
        }
    }

    HODBaseScreen(
        title = "Profile Approval Management",
        subtitle = "Review and approve modifications to faculty and student records",
        currentRoute = AppRoutes.HOD_PROFILE_APPROVALS,
        onNavigate = onNavigate
    ) {
        Row(Modifier.fillMaxWidth().background(MaterialTheme.colorScheme.surface, RoundedCornerShape(12.dp)).padding(4.dp)) {
            TabButton("Faculty Profiles (${uiState.facultyRequests.size})", activeTab == "faculty", Modifier.weight(1f)) { activeTab = "faculty" }
            TabButton("Student Profiles (${uiState.studentRequests.size})", activeTab == "student", Modifier.weight(1f)) { activeTab = "student" }
        }

        Spacer(Modifier.height(16.dp))

        uiState.error?.let {
            Text(it, color = Color(0xFFB91C1C), fontSize = 13.sp, modifier = Modifier.padding(bottom = 8.dp))
        }

        CamsCard(modifier = Modifier.fillMaxWidth().weight(1f)) {
            Text("Pending Requests Inbox", fontWeight = FontWeight.Bold, fontSize = 16.sp, color = MaterialTheme.colorScheme.onSurface)
            Spacer(Modifier.height(12.dp))

            if (uiState.isLoading) {
                Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator()
                }
            } else if (activeTab == "faculty") {
                if (uiState.facultyRequests.isEmpty()) {
                    Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        Text("No pending faculty profile requests", color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                } else {
                    LazyColumn(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                        items(uiState.facultyRequests, key = { it.id }) { req ->
                            CamsCard(containerColor = MaterialTheme.colorScheme.background, modifier = Modifier.fillMaxWidth()) {
                                Column(Modifier.padding(12.dp)) {
                                    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                        Column {
                                            Text(req.fullName ?: "Unknown Faculty", fontWeight = FontWeight.Bold, fontSize = 14.sp, color = MaterialTheme.colorScheme.onSurface)
                                            Text(req.requestedDesignation ?: req.departmentName ?: "-", fontSize = 13.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                        }
                                        Text("PENDING", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = Color(0xFFB45309), modifier = Modifier.background(Color(0xFFFEF3C7), RoundedCornerShape(4.dp)).padding(horizontal = 6.dp, vertical = 2.dp))
                                    }
                                    Spacer(Modifier.height(12.dp))
                                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                        Button(
                                            onClick = { viewModel.reviewFaculty(req.id, "REJECTED") },
                                            enabled = !uiState.isReviewing,
                                            modifier = Modifier.weight(1f),
                                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFFEF2F2), contentColor = Color(0xFFE11D48)),
                                            shape = RoundedCornerShape(8.dp)
                                        ) { Text("Reject", fontSize = 12.sp, fontWeight = FontWeight.Bold) }
                                        Button(
                                            onClick = { viewModel.reviewFaculty(req.id, "APPROVED") },
                                            enabled = !uiState.isReviewing,
                                            modifier = Modifier.weight(1f),
                                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF1E293B)),
                                            shape = RoundedCornerShape(8.dp)
                                        ) { Text("Approve", fontSize = 12.sp, fontWeight = FontWeight.Bold) }
                                    }
                                }
                            }
                        }
                    }
                }
            } else {
                if (uiState.studentRequests.isEmpty()) {
                    Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        Text("No pending student profile requests", color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                } else {
                    LazyColumn(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                        items(uiState.studentRequests, key = { it.id }) { student ->
                            CamsCard(containerColor = MaterialTheme.colorScheme.background, modifier = Modifier.fillMaxWidth()) {
                                Column(Modifier.padding(12.dp)) {
                                    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                        Column {
                                            Text(student.fullName, fontWeight = FontWeight.Bold, fontSize = 14.sp, color = MaterialTheme.colorScheme.onSurface)
                                            Text("Roll No: ${student.rollNo} • Sem ${student.semester}", fontSize = 13.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                        }
                                        Text("PENDING", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = Color(0xFFB45309), modifier = Modifier.background(Color(0xFFFEF3C7), RoundedCornerShape(4.dp)).padding(horizontal = 6.dp, vertical = 2.dp))
                                    }
                                    Spacer(Modifier.height(12.dp))
                                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                        Button(
                                            onClick = { viewModel.reviewStudent(student.id, "REJECT") },
                                            enabled = !uiState.isReviewing,
                                            modifier = Modifier.weight(1f),
                                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFFEF2F2), contentColor = Color(0xFFE11D48)),
                                            shape = RoundedCornerShape(8.dp)
                                        ) { Text("Reject", fontSize = 12.sp, fontWeight = FontWeight.Bold) }
                                        Button(
                                            onClick = { viewModel.reviewStudent(student.id, "APPROVE") },
                                            enabled = !uiState.isReviewing,
                                            modifier = Modifier.weight(1f),
                                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF1E293B)),
                                            shape = RoundedCornerShape(8.dp)
                                        ) { Text("Approve", fontSize = 12.sp, fontWeight = FontWeight.Bold) }
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
private fun TabButton(text: String, selected: Boolean, modifier: Modifier, onClick: () -> Unit) {
    Button(
        onClick = onClick,
        modifier = modifier,
        colors = ButtonDefaults.buttonColors(
            containerColor = if (selected) Color(0xFFEEF2FF) else Color.Transparent,
            contentColor = if (selected) Color(0xFF4338CA) else Color(0xFF64748B)
        ),
        shape = RoundedCornerShape(8.dp),
        elevation = null
    ) {
        Text(text, fontSize = 13.sp, fontWeight = FontWeight.Bold)
    }
}
