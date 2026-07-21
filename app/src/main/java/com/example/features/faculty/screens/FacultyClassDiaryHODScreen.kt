package com.example.features.faculty.screens

import android.widget.Toast
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
import com.example.features.hod.widgets.HODBaseScreen

import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.core.network.ClassDiaryDto
import com.example.core.repository.FacultyRepositoryImpl
import com.example.features.faculty.providers.FacultyClassDiaryHodViewModel
import com.example.features.faculty.providers.FacultyClassDiaryHodViewModelFactory

@Composable
fun FacultyClassDiaryHODScreen(onNavigate: (String) -> Unit) {
    val context = LocalContext.current
    val repository = remember { FacultyRepositoryImpl(com.example.CamsApplication.instance.container.apiService) }
    val factory = remember { FacultyClassDiaryHodViewModelFactory(repository) }
    val viewModel: FacultyClassDiaryHodViewModel = viewModel(factory = factory)
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    var entryPendingReview by remember { mutableStateOf<ClassDiaryDto?>(null) }

    LaunchedEffect(uiState.reviewError) {
        uiState.reviewError?.let {
            Toast.makeText(context, it, Toast.LENGTH_SHORT).show()
            viewModel.clearReviewError()
        }
    }

    val submitted = uiState.entries.filter { it.status == "Submitted" }

    HODBaseScreen(scrollable = false,
        title = "Class Diary Review",
        subtitle = "Review and approve class diary entries submitted by department faculty",
        currentRoute = "/faculty/class-diary-hod",
        onNavigate = onNavigate
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp)
        ) {
            Text("HOD Remarks & Approval", fontWeight = FontWeight.Bold, fontSize = 18.sp, color = MaterialTheme.colorScheme.onSurface)
            Spacer(modifier = Modifier.height(12.dp))

            uiState.error?.let {
                Text(it, color = Color(0xFFB91C1C), fontSize = 13.sp, modifier = Modifier.padding(bottom = 8.dp))
            }

            if (uiState.isLoading) {
                Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator(color = CamsNavy)
                }
            } else if (submitted.isEmpty()) {
                Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Text("No submitted diary entries to review", color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            } else {
                LazyColumn(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    items(submitted, key = { it.id }) { entry ->
                        DiaryReviewItem(
                            entry = entry,
                            onReview = { entryPendingReview = entry }
                        )
                    }
                }
            }
        }
    }

    entryPendingReview?.let { entry ->
        ReviewDialog(
            entry = entry,
            isSaving = uiState.isReviewing,
            onDismiss = { entryPendingReview = null },
            onSubmit = { status, remarks ->
                viewModel.review(entry.id, status, remarks)
                entryPendingReview = null
            }
        )
    }
}

@Composable
private fun DiaryReviewItem(entry: ClassDiaryDto, onReview: () -> Unit) {
    val status = entry.hodStatus ?: "Pending"
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
                Column {
                    Text(entry.date, fontSize = 12.sp, color = Color(0xFF64748B), fontWeight = FontWeight.Bold)
                    Text("${entry.subject} — ${entry.section ?: ""}", fontWeight = FontWeight.Bold, color = CamsNavy, fontSize = 14.sp)
                    Text("By ${entry.facultyName ?: "Unknown"}", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
                Surface(
                    color = when (status) {
                        "Approved" -> Color(0xFF10B981).copy(alpha = 0.1f)
                        "Rejected" -> Color(0xFFEF4444).copy(alpha = 0.1f)
                        else -> Color(0xFFF59E0B).copy(alpha = 0.1f)
                    },
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Text(
                        status,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = when (status) {
                            "Approved" -> Color(0xFF10B981)
                            "Rejected" -> Color(0xFFEF4444)
                            else -> Color(0xFFF59E0B)
                        }
                    )
                }
            }
            Spacer(modifier = Modifier.height(8.dp))
            Text(entry.topic ?: "-", fontWeight = FontWeight.Bold, fontSize = 14.sp, color = MaterialTheme.colorScheme.onSurface)
            Spacer(modifier = Modifier.height(8.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalAlignment = Alignment.Top) {
                Icon(Icons.Filled.Chat, null, tint = Color(0xFF64748B), modifier = Modifier.size(16.dp))
                Column {
                    Text("HOD Remarks:", fontSize = 13.sp, color = Color(0xFF64748B), fontWeight = FontWeight.Bold)
                    Text(entry.hodRemarks?.ifBlank { "—" } ?: "—", fontSize = 13.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            }
            if (status == "Pending") {
                Spacer(modifier = Modifier.height(12.dp))
                Button(
                    onClick = onReview,
                    colors = ButtonDefaults.buttonColors(containerColor = CamsNavy),
                    shape = RoundedCornerShape(12.dp)
                ) { Text("Review") }
            }
        }
    }
}

@Composable
private fun ReviewDialog(
    entry: ClassDiaryDto,
    isSaving: Boolean,
    onDismiss: () -> Unit,
    onSubmit: (status: String, remarks: String?) -> Unit
) {
    var remarks by remember { mutableStateOf("") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Review Diary Entry") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text("${entry.subject} — ${entry.topic ?: ""}", fontWeight = FontWeight.Bold)
                OutlinedTextField(
                    value = remarks,
                    onValueChange = { remarks = it },
                    label = { Text("Remarks") },
                    modifier = Modifier.fillMaxWidth()
                )
            }
        },
        confirmButton = {
            TextButton(
                enabled = !isSaving,
                onClick = { onSubmit("Approved", remarks.trim().ifBlank { null }) }
            ) { Text(if (isSaving) "Saving..." else "Approve") }
        },
        dismissButton = {
            TextButton(
                enabled = !isSaving,
                onClick = { onSubmit("Rejected", remarks.trim().ifBlank { null }) }
            ) { Text("Reject", color = Color(0xFFB91C1C)) }
        }
    )
}
