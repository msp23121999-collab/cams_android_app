package com.example.features.hod.screens

import android.widget.Toast
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.*
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
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.core.navigation.AppRoutes
import com.example.core.network.HODPendingProofDto
import com.example.core.theme.CamsTextPrimary
import com.example.core.theme.CamsTextSecondary
import com.example.features.hod.providers.HODResearchMonitoringViewModel
import com.example.features.hod.widgets.HODBaseScreen

@Composable
fun HODResearchOverviewScreen(
    onNavigate: (String) -> Unit,
    viewModel: HODResearchMonitoringViewModel = viewModel()
) {
    val context = LocalContext.current
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val publications = uiState.monitoringData.filter { it.status == "COMPLETED" }
    val inProgress = uiState.monitoringData.filter { it.status == "SUBMITTED" }
    val overdue = uiState.monitoringData.count { it.daysOverdue > 0 }
    var proofPendingReview by remember { mutableStateOf<HODPendingProofDto?>(null) }

    LaunchedEffect(uiState.verificationSuccess) {
        if (uiState.verificationSuccess) {
            Toast.makeText(context, "Proof reviewed", Toast.LENGTH_SHORT).show()
            viewModel.resetVerificationStatus()
        }
    }

    HODBaseScreen(
        title = "Research Overview",
        subtitle = "Monitor department publications, projects, and grants",
        currentRoute = AppRoutes.HOD_RESEARCH_OVERVIEW,
        onNavigate = onNavigate
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.background)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // KPI Cards
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.fillMaxWidth()) {
                KpiCard("Active Projects", "${inProgress.size}", Icons.Filled.Science, Color(0xFF7C3AED), MaterialTheme.colorScheme.secondaryContainer, Modifier.weight(1f))
                KpiCard("Completed", "${publications.size}", Icons.AutoMirrored.Filled.MenuBook, Color(0xFF059669), Color(0xFFECFDF5), Modifier.weight(1f))
            }
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.fillMaxWidth()) {
                KpiCard("Pending Approvals", "${uiState.pendingProofs.size}", Icons.Filled.EmojiEvents, Color(0xFFD97706), Color(0xFFFFFBEB), Modifier.weight(1f))
                KpiCard("Overdue", "$overdue", Icons.Filled.Description, Color(0xFFE11D48), Color(0xFFFFF1F2), Modifier.weight(1f))
            }

            uiState.error?.let {
                Text(it, color = Color(0xFFB91C1C), fontSize = 13.sp)
            }

            if (uiState.pendingProofs.isNotEmpty()) {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text("Pending Proof Verification", fontWeight = FontWeight.Bold, fontSize = 14.sp, color = MaterialTheme.colorScheme.onSurface)
                        Spacer(Modifier.height(12.dp))
                        uiState.pendingProofs.forEach { proof ->
                            Row(
                                modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(proof.title, fontWeight = FontWeight.Bold, fontSize = 13.sp, color = MaterialTheme.colorScheme.onSurface)
                                    Text("${proof.faculty_name} • ${proof.journal_name}", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                }
                                Button(
                                    onClick = { proofPendingReview = proof },
                                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF4F46E5)),
                                    shape = RoundedCornerShape(8.dp),
                                    contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp)
                                ) {
                                    Text("Review", fontSize = 12.sp)
                                }
                            }
                        }
                    }
                }
            }

            Card(
                modifier = Modifier.fillMaxWidth().weight(1f),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)
            ) {
                Column {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(MaterialTheme.colorScheme.background)
                            .border(BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant))
                            .padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(32.dp)
                                .background(Color(0xFFEEF2FF), RoundedCornerShape(8.dp)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.Description,
                                contentDescription = null,
                                tint = Color(0xFF4F46E5),
                                modifier = Modifier.size(16.dp)
                            )
                        }
                        Column(modifier = Modifier.weight(1f)) {
                            Text("Department Research", fontWeight = FontWeight.Bold, fontSize = 14.sp, color = MaterialTheme.colorScheme.onSurface)
                            Text("All research plans from department faculty", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                    }

                    if (uiState.isLoading) {
                        Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                            CircularProgressIndicator()
                        }
                    } else if (uiState.monitoringData.isEmpty()) {
                        Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                            Text("No research plans available.", color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                    } else {
                        LazyColumn(contentPadding = PaddingValues(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                            items(uiState.monitoringData) { item ->
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .background(MaterialTheme.colorScheme.surface, RoundedCornerShape(12.dp))
                                        .border(1.dp, MaterialTheme.colorScheme.outlineVariant, RoundedCornerShape(12.dp))
                                        .padding(16.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Box(
                                        modifier = Modifier.size(40.dp).background(MaterialTheme.colorScheme.surfaceVariant, CircleShape),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Icon(Icons.Filled.Description, null, tint = MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.size(20.dp))
                                    }
                                    Spacer(Modifier.width(16.dp))
                                    Column(modifier = Modifier.weight(1f)) {
                                        Text(item.title, fontWeight = FontWeight.Bold, fontSize = 14.sp, color = MaterialTheme.colorScheme.onSurface)
                                        Spacer(Modifier.height(4.dp))
                                        Text("${item.faculty_name} • ${item.type}", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                        if (item.daysOverdue > 0) {
                                            Text("${item.daysOverdue} days overdue", fontSize = 11.sp, color = Color(0xFFE11D48))
                                        }
                                    }
                                    Column(horizontalAlignment = Alignment.End) {
                                        Text(
                                            item.status,
                                            fontSize = 12.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = MaterialTheme.colorScheme.onSurface
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    proofPendingReview?.let { proof ->
        ReviewProofDialog(
            proof = proof,
            isSaving = uiState.isVerifying,
            onDismiss = { proofPendingReview = null },
            onSubmit = { status, remarks ->
                viewModel.verifyProof(proof.id, status, remarks)
                proofPendingReview = null
            }
        )
    }
}

@Composable
private fun ReviewProofDialog(
    proof: HODPendingProofDto,
    isSaving: Boolean,
    onDismiss: () -> Unit,
    onSubmit: (status: String, remarks: String) -> Unit
) {
    var remarks by remember { mutableStateOf("") }
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Review Publication Proof") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text(proof.title, fontWeight = FontWeight.Bold)
                Text("By ${proof.faculty_name}", fontSize = 13.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                Text("Journal: ${proof.journal_name}", fontSize = 13.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
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
                onClick = { onSubmit("VERIFIED", remarks) }
            ) { Text("Verify") }
        },
        dismissButton = {
            TextButton(
                enabled = !isSaving,
                onClick = { onSubmit("REJECTED", remarks) }
            ) { Text("Reject", color = Color(0xFFB91C1C)) }
        }
    )
}

@Composable
private fun KpiCard(label: String, value: String, icon: androidx.compose.ui.graphics.vector.ImageVector, color: Color, bgColor: Color, modifier: Modifier) {
    Card(
        modifier = modifier,
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
        shape = RoundedCornerShape(12.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(label, fontSize = 12.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.fillMaxWidth())
            Spacer(Modifier.height(8.dp))
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(modifier = Modifier.size(32.dp).background(bgColor, CircleShape), contentAlignment = Alignment.Center) {
                    Icon(icon, null, tint = color, modifier = Modifier.size(16.dp))
                }
                Spacer(Modifier.width(12.dp))
                Text(value, fontSize = 24.sp, fontWeight = FontWeight.ExtraBold, color = MaterialTheme.colorScheme.onSurface)
            }
        }
    }
}
