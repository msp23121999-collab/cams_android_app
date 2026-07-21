package com.example.features.principal.screens

import android.widget.Toast
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.CamsApplication
import com.example.core.navigation.AppRoutes
import com.example.core.network.ClassDiaryDto
import com.example.core.repository.PrincipalRepositoryImpl
import com.example.core.theme.*
import com.example.features.principal.providers.PrincipalClassDiaryViewModel
import com.example.features.principal.providers.PrincipalClassDiaryViewModelFactory
import com.example.features.principal.widgets.PrincipalBaseScreen

@Composable
fun PrincipalClassDiaryScreen(
    onNavigate: (String) -> Unit,
    viewModel: PrincipalClassDiaryViewModel = viewModel(
        factory = PrincipalClassDiaryViewModelFactory(PrincipalRepositoryImpl(CamsApplication.instance.container.apiService))
    )
) {
    val context = LocalContext.current
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    var entryPendingReview by remember { mutableStateOf<ClassDiaryDto?>(null) }
    var filter by remember { mutableStateOf("Submitted") }

    LaunchedEffect(uiState.reviewError) {
        uiState.reviewError?.let {
            Toast.makeText(context, it, Toast.LENGTH_SHORT).show()
            viewModel.clearReviewError()
        }
    }

    val filtered = uiState.entries.filter { entry ->
        when (filter) {
            "Submitted" -> (entry.hodStatus ?: "Pending") == "Pending" && entry.status == "Submitted"
            "Approved" -> entry.hodStatus == "Approved"
            "Rejected" -> entry.hodStatus == "Rejected"
            else -> true
        }
    }

    PrincipalBaseScreen(
        title = "Class Diary & Academic Audit",
        subtitle = "College-wide teaching logs review and real-time attendance audits.",
        currentRoute = AppRoutes.PRINCIPAL_CLASS_DIARY,
        onNavigate = onNavigate
    ) {
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.fillMaxWidth()) {
            listOf("Submitted", "Approved", "Rejected", "All").forEach { f ->
                FilterChip(selected = filter == f, onClick = { filter = f }, label = { Text(f, fontSize = 12.sp) })
            }
        }
        Spacer(Modifier.height(16.dp))

        uiState.error?.let { Text(it, color = Color(0xFFB91C1C), fontSize = 13.sp, modifier = Modifier.padding(bottom = 8.dp)) }

        if (uiState.isLoading) {
            Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) { CircularProgressIndicator(color = CamsNavy) }
        } else if (filtered.isEmpty()) {
            Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Text("No diary entries in this view", color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
        } else {
            LazyColumn(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                items(filtered, key = { it.id }) { entry ->
                    DiaryReviewItem(entry = entry, onReview = { entryPendingReview = entry })
                }
            }
        }
    }

    entryPendingReview?.let { entry ->
        ReviewDiaryDialog(
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
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
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
                        status, modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp), fontSize = 12.sp, fontWeight = FontWeight.Bold,
                        color = when (status) {
                            "Approved" -> Color(0xFF10B981)
                            "Rejected" -> Color(0xFFEF4444)
                            else -> Color(0xFFF59E0B)
                        }
                    )
                }
            }
            Spacer(Modifier.height(8.dp))
            Text(entry.topic ?: "-", fontWeight = FontWeight.Bold, fontSize = 14.sp, color = MaterialTheme.colorScheme.onSurface)
            if (status == "Pending" && entry.status == "Submitted") {
                Spacer(Modifier.height(12.dp))
                Button(onClick = onReview, colors = ButtonDefaults.buttonColors(containerColor = CamsNavy), shape = RoundedCornerShape(12.dp)) {
                    Text("Review")
                }
            }
        }
    }
}

@Composable
private fun ReviewDiaryDialog(
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
                Text("By ${entry.facultyName ?: "Unknown"} on ${entry.date}", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                OutlinedTextField(value = remarks, onValueChange = { remarks = it }, label = { Text("Remarks") }, modifier = Modifier.fillMaxWidth())
            }
        },
        confirmButton = {
            TextButton(enabled = !isSaving, onClick = { onSubmit("Approved", remarks.trim().ifBlank { null }) }) {
                Text(if (isSaving) "Saving..." else "Approve")
            }
        },
        dismissButton = {
            TextButton(enabled = !isSaving, onClick = { onSubmit("Rejected", remarks.trim().ifBlank { null }) }) {
                Text("Reject", color = Color(0xFFB91C1C))
            }
        }
    )
}
