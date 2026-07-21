package com.example.features.hod.screens

import android.content.Intent
import android.net.Uri
import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.border
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

import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.core.network.HodPendingMaterialDto
import com.example.core.repository.HODRepositoryImpl
import com.example.features.hod.providers.HODStudyMaterialsViewModel
import com.example.features.hod.providers.HODStudyMaterialsViewModelFactory

@Composable
fun HODStudyMaterialVerificationScreen(onNavigate: (String) -> Unit) {
    val context = LocalContext.current
    val repository = remember { HODRepositoryImpl(com.example.CamsApplication.instance.container.apiService) }
    val factory = remember { HODStudyMaterialsViewModelFactory(repository) }
    val viewModel: HODStudyMaterialsViewModel = viewModel(factory = factory)
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    var materialPendingReview by remember { mutableStateOf<HodPendingMaterialDto?>(null) }

    LaunchedEffect(uiState.reviewError) {
        uiState.reviewError?.let {
            Toast.makeText(context, it, Toast.LENGTH_SHORT).show()
            viewModel.clearReviewError()
        }
    }

    HODBaseScreen(
        title = "Study Materials Verification",
        subtitle = "Review academic notes, lecture slides, and case study files",
        currentRoute = "/hod/study-materials",
        onNavigate = onNavigate
    ) {
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.fillMaxWidth()) {
            KpiCard("Pending Review", "${uiState.materials.size}", Icons.Filled.Inbox, Color(0xFF8B5CF6), Modifier.weight(1f))
        }

        Spacer(modifier = Modifier.height(16.dp))

        uiState.error?.let {
            Text(it, color = Color(0xFFB91C1C), fontSize = 13.sp, modifier = Modifier.padding(bottom = 8.dp))
        }

        CamsCard(modifier = Modifier.fillMaxWidth().weight(1f)) {
            Column {
                Text("Pending Study Materials", fontWeight = FontWeight.Bold, fontSize = 16.sp, color = MaterialTheme.colorScheme.onSurface)
                Text("Materials uploaded by faculty in your department awaiting verification.", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)

                Spacer(Modifier.height(16.dp))

                if (uiState.isLoading) {
                    Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        CircularProgressIndicator()
                    }
                } else if (uiState.materials.isEmpty()) {
                    Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        Text("No materials pending review", color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                } else {
                    LazyColumn(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                        items(uiState.materials, key = { it.id }) { material ->
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .background(MaterialTheme.colorScheme.surface, RoundedCornerShape(12.dp))
                                    .border(1.dp, MaterialTheme.colorScheme.outlineVariant, RoundedCornerShape(12.dp))
                                    .padding(12.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Box(modifier = Modifier.size(40.dp).background(Color(0xFFEEF2FF), RoundedCornerShape(8.dp)), contentAlignment = Alignment.Center) {
                                    Icon(Icons.Filled.Description, null, tint = Color(0xFF4F46E5))
                                }
                                Spacer(Modifier.width(12.dp))
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(material.title, fontWeight = FontWeight.Bold, fontSize = 14.sp, color = MaterialTheme.colorScheme.onSurface)
                                    Text("${material.facultyName} • ${material.subject} • ${material.category}", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                }
                                TextButton(onClick = { materialPendingReview = material }) {
                                    Text("Review")
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    materialPendingReview?.let { material ->
        ReviewMaterialDialog(
            material = material,
            isSaving = uiState.isReviewing,
            onDismiss = { materialPendingReview = null },
            onOpenFile = {
                material.fileUrl?.let { url ->
                    val base = com.example.core.config.AppConfig.BASE_URL
                    val origin = base.substringBefore("/api/v1")
                    val fullUrl = if (url.startsWith("http")) url else origin + url
                    try {
                        context.startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(fullUrl)))
                    } catch (e: Exception) {
                        Toast.makeText(context, "Could not open file", Toast.LENGTH_SHORT).show()
                    }
                }
            },
            onSubmit = { status, remarks ->
                viewModel.review(material.id, status, remarks)
                materialPendingReview = null
            }
        )
    }
}

@Composable
private fun ReviewMaterialDialog(
    material: HodPendingMaterialDto,
    isSaving: Boolean,
    onDismiss: () -> Unit,
    onOpenFile: () -> Unit,
    onSubmit: (status: String, remarks: String) -> Unit
) {
    var remarks by remember { mutableStateOf("") }
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(material.title) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text("Uploaded by ${material.facultyName}", fontSize = 13.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                Text("Subject: ${material.subject}", fontSize = 13.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                if (!material.fileUrl.isNullOrBlank()) {
                    TextButton(onClick = onOpenFile) { Text("View File") }
                }
                OutlinedTextField(
                    value = remarks,
                    onValueChange = { remarks = it },
                    label = { Text("Remarks") },
                    modifier = Modifier.fillMaxWidth()
                )
            }
        },
        confirmButton = {
            TextButton(enabled = !isSaving, onClick = { onSubmit("Approved", remarks.trim()) }) { Text(if (isSaving) "Saving..." else "Approve") }
        },
        dismissButton = {
            TextButton(enabled = !isSaving, onClick = { onSubmit("Rejected", remarks.trim()) }) { Text("Reject", color = Color(0xFFB91C1C)) }
        }
    )
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
