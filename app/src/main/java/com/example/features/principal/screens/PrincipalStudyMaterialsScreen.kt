package com.example.features.principal.screens

import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.*
import androidx.compose.material.icons.filled.*
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
import com.example.core.network.HodPendingMaterialDto
import com.example.core.repository.PrincipalRepositoryImpl
import com.example.core.theme.*
import com.example.features.principal.providers.PrincipalStudyMaterialsViewModel2
import com.example.features.principal.providers.PrincipalStudyMaterialsViewModelFactory2
import com.example.features.principal.widgets.PrincipalBaseScreen

@Composable
fun PrincipalStudyMaterialsScreen(
    onNavigate: (String) -> Unit,
    viewModel: PrincipalStudyMaterialsViewModel2 = viewModel(
        factory = PrincipalStudyMaterialsViewModelFactory2(PrincipalRepositoryImpl(CamsApplication.instance.container.apiService))
    )
) {
    val context = LocalContext.current
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    var materialPendingReview by remember { mutableStateOf<HodPendingMaterialDto?>(null) }

    LaunchedEffect(uiState.reviewError) {
        uiState.reviewError?.let {
            Toast.makeText(context, it, Toast.LENGTH_SHORT).show()
            viewModel.clearReviewError()
        }
    }

    val categoryCounts = uiState.materials.groupingBy { it.category.ifBlank { "Other" } }.eachCount()

    PrincipalBaseScreen(
        title = "Study Material Approvals",
        subtitle = "Review, approve, or reject and return learning resources uploaded by law faculty members.",
        currentRoute = AppRoutes.PRINCIPAL_STUDY_MATERIALS,
        onNavigate = onNavigate
    ) {
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.fillMaxWidth()) {
            KpiCard("Pending Validation", "${uiState.materials.size}", Icons.AutoMirrored.Filled.MenuBook, Color(0xFF3B82F6), Modifier.weight(1f))
            val topCategories = categoryCounts.entries.sortedByDescending { it.value }.take(3)
            topCategories.getOrNull(0)?.let { KpiCard(it.key, "${it.value}", Icons.Filled.Description, Color(0xFFF59E0B), Modifier.weight(1f)) }
            topCategories.getOrNull(1)?.let { KpiCard(it.key, "${it.value}", Icons.Filled.Layers, Color(0xFF10B981), Modifier.weight(1f)) }
            topCategories.getOrNull(2)?.let { KpiCard(it.key, "${it.value}", Icons.AutoMirrored.Filled.FactCheck, Color(0xFFEF4444), Modifier.weight(1f)) }
        }

        Spacer(modifier = Modifier.height(16.dp))

        uiState.error?.let { Text(it, color = Color(0xFFB91C1C), fontSize = 13.sp, modifier = Modifier.padding(bottom = 8.dp)) }

        if (uiState.isLoading) {
            Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) { CircularProgressIndicator(color = CamsNavy) }
        } else if (uiState.materials.isEmpty()) {
            Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Text("No study materials awaiting validation", color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
        } else {
            LazyColumn(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                items(uiState.materials, key = { it.id }) { material ->
                    MaterialRow(material = material, onReview = { materialPendingReview = material })
                }
            }
        }
    }

    materialPendingReview?.let { material ->
        ReviewMaterialDialog(
            material = material,
            isSaving = uiState.isReviewing,
            onDismiss = { materialPendingReview = null },
            onSubmit = { status, remarks ->
                viewModel.review(material.id, status, remarks)
                materialPendingReview = null
            }
        )
    }
}

@Composable
private fun MaterialRow(material: HodPendingMaterialDto, onReview: () -> Unit) {
    Row(
        modifier = Modifier.fillMaxWidth().background(MaterialTheme.colorScheme.surface, RoundedCornerShape(12.dp)).border(1.dp, MaterialTheme.colorScheme.outlineVariant, RoundedCornerShape(12.dp)).padding(16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(Modifier.background(Color(0xFFF3E8FF), RoundedCornerShape(8.dp)).padding(12.dp)) {
            Text(material.facultyName.take(2).uppercase().ifBlank { "FA" }, fontWeight = FontWeight.Bold, color = Color(0xFF6D28D9))
        }
        Spacer(Modifier.width(16.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(material.title, fontWeight = FontWeight.Bold, fontSize = 14.sp, color = MaterialTheme.colorScheme.onSurface)
            Text(material.subject.ifBlank { "General" }, fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
            Spacer(Modifier.height(8.dp))
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(material.facultyName.ifBlank { "Faculty" }, fontWeight = FontWeight.Bold, fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurface)
                Spacer(Modifier.width(8.dp))
                Text(
                    material.category.uppercase().ifBlank { "MATERIAL" }, fontSize = 11.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.background(MaterialTheme.colorScheme.surfaceVariant, RoundedCornerShape(4.dp)).padding(horizontal = 4.dp, vertical = 2.dp)
                )
            }
        }
        Button(onClick = onReview, colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFEEF2FF)), shape = RoundedCornerShape(8.dp)) {
            Text("Review", color = Color(0xFF4F46E5), fontSize = 12.sp, fontWeight = FontWeight.Bold)
        }
    }
}

@Composable
private fun ReviewMaterialDialog(
    material: HodPendingMaterialDto,
    isSaving: Boolean,
    onDismiss: () -> Unit,
    onSubmit: (status: String, remarks: String) -> Unit
) {
    var remarks by remember { mutableStateOf("") }
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Review Study Material") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text(material.title, fontWeight = FontWeight.Bold)
                Text("By ${material.facultyName.ifBlank { "Faculty" }} — ${material.subject.ifBlank { "General" }}", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                if (material.description.isNotBlank()) {
                    Text(material.description, fontSize = 13.sp, color = MaterialTheme.colorScheme.onSurface)
                }
                OutlinedTextField(value = remarks, onValueChange = { remarks = it }, label = { Text("Remarks") }, modifier = Modifier.fillMaxWidth())
            }
        },
        confirmButton = {
            TextButton(enabled = !isSaving, onClick = { onSubmit("Approved", remarks.trim()) }) {
                Text(if (isSaving) "Saving..." else "Approve")
            }
        },
        dismissButton = {
            TextButton(enabled = !isSaving, onClick = { onSubmit("Rejected", remarks.trim()) }) {
                Text("Reject", color = Color(0xFFB91C1C))
            }
        }
    )
}

@Composable
private fun KpiCard(label: String, value: String, icon: androidx.compose.ui.graphics.vector.ImageVector, color: Color, modifier: Modifier) {
    Card(modifier = modifier, colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface), elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)) {
        Column(modifier = Modifier.padding(12.dp)) {
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.Top) {
                Text(label, fontSize = 12.sp, fontWeight = FontWeight.Black, color = Color(0xFF64748B), modifier = Modifier.weight(1f), maxLines = 1)
                Box(Modifier.background(color.copy(alpha=0.1f), RoundedCornerShape(8.dp)).padding(4.dp)) {
                    Icon(icon, null, tint = color, modifier = Modifier.size(16.dp))
                }
            }
            Text(value, fontSize = 20.sp, fontWeight = FontWeight.Black, color = MaterialTheme.colorScheme.onSurface, modifier = Modifier.padding(top = 8.dp))
        }
    }
}
