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
import com.example.core.network.FacultyProfileUpdateRequestDto
import com.example.core.repository.HODRepositoryImpl
import com.example.features.hod.providers.HODFacultyApprovalViewModel
import com.example.features.hod.providers.HODFacultyApprovalViewModelFactory

@Composable
fun HODFacultyApprovalScreen(onNavigate: (String) -> Unit) {
    val context = LocalContext.current
    val repository = remember { HODRepositoryImpl(com.example.CamsApplication.instance.container.apiService) }
    val factory = remember { HODFacultyApprovalViewModelFactory(repository) }
    val viewModel: HODFacultyApprovalViewModel = viewModel(factory = factory)
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    var requestPendingReview by remember { mutableStateOf<FacultyProfileUpdateRequestDto?>(null) }

    LaunchedEffect(uiState.reviewError) {
        uiState.reviewError?.let {
            Toast.makeText(context, it, Toast.LENGTH_SHORT).show()
            viewModel.clearReviewError()
        }
    }

    HODBaseScreen(
        title = "Faculty Profile Approvals",
        subtitle = "Review faculty profile change requests",
        currentRoute = AppRoutes.HOD_FACULTY_APPROVAL,
        onNavigate = onNavigate
    ) {
        CamsCard(modifier = Modifier.fillMaxWidth().weight(1f)) {
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                Text("Pending Requests", fontWeight = FontWeight.Bold, fontSize = 16.sp, color = MaterialTheme.colorScheme.onSurface)
                Text("${uiState.requests.size} Pending", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = Color(0xFF4F46E5), modifier = Modifier.background(Color(0xFFEEF2FF), RoundedCornerShape(8.dp)).padding(horizontal = 8.dp, vertical = 4.dp))
            }
            Spacer(Modifier.height(16.dp))

            uiState.error?.let {
                Text(it, color = Color(0xFFB91C1C), fontSize = 13.sp, modifier = Modifier.padding(bottom = 8.dp))
            }

            if (uiState.isLoading) {
                Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator()
                }
            } else if (uiState.requests.isEmpty()) {
                Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Text("No pending profile change requests", color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            } else {
                LazyColumn(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    items(uiState.requests, key = { it.id }) { req ->
                        CamsCard(containerColor = MaterialTheme.colorScheme.background, modifier = Modifier.fillMaxWidth()) {
                            Column(Modifier.padding(12.dp)) {
                                Text(req.fullName ?: "Unknown Faculty", fontWeight = FontWeight.Bold, fontSize = 14.sp, color = MaterialTheme.colorScheme.onSurface)
                                req.officialEmail?.let { Text(it, fontSize = 13.sp, color = MaterialTheme.colorScheme.onSurfaceVariant) }
                                req.requestedDesignation?.let { Text("Requested Designation: $it", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant) }
                                req.requestedDepartmentName?.let { Text("Requested Department: $it", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant) }
                                Spacer(Modifier.height(12.dp))
                                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                    Button(
                                        onClick = { viewModel.review(req.id, "REJECTED", null) },
                                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFFEF2F2), contentColor = Color(0xFFE11D48)),
                                        shape = RoundedCornerShape(8.dp)
                                    ) {
                                        Text("Reject", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                                    }
                                    Button(
                                        onClick = { requestPendingReview = req },
                                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFFFFBEB), contentColor = Color(0xFFD97706)),
                                        shape = RoundedCornerShape(8.dp)
                                    ) {
                                        Text("Request Changes", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                                    }
                                    Button(
                                        onClick = { viewModel.review(req.id, "APPROVED", null) },
                                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF1E293B)),
                                        shape = RoundedCornerShape(8.dp)
                                    ) {
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

    requestPendingReview?.let { req ->
        RequestChangesDialog(
            onDismiss = { requestPendingReview = null },
            onSubmit = {
                viewModel.review(req.id, "CHANGES_REQUESTED", it)
                requestPendingReview = null
            }
        )
    }
}

@Composable
private fun RequestChangesDialog(onDismiss: () -> Unit, onSubmit: (String) -> Unit) {
    var comments by remember { mutableStateOf("") }
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Request Changes") },
        text = {
            OutlinedTextField(
                value = comments,
                onValueChange = { comments = it },
                label = { Text("What needs to change?") },
                modifier = Modifier.fillMaxWidth()
            )
        },
        confirmButton = {
            TextButton(enabled = comments.isNotBlank(), onClick = { onSubmit(comments.trim()) }) { Text("Send") }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Cancel") }
        }
    )
}
