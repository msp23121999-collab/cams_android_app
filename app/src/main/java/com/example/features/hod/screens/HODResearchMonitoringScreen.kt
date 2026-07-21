package com.example.features.hod.screens

import androidx.lifecycle.compose.collectAsStateWithLifecycle
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.core.theme.*
import com.example.core.ui.CamsCard
import com.example.features.hod.providers.HODResearchMonitoringViewModel
import com.example.features.hod.widgets.HODBaseScreen
import com.example.core.navigation.AppRoutes

@Composable
fun HODResearchMonitoringScreen(
    onNavigate: (String) -> Unit,
    viewModel: HODResearchMonitoringViewModel = viewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    var activeTab by remember { mutableStateOf("monitoring") }
    val context = androidx.compose.ui.platform.LocalContext.current

    LaunchedEffect(uiState.verifyError) {
        uiState.verifyError?.let {
            android.widget.Toast.makeText(context, it, android.widget.Toast.LENGTH_SHORT).show()
            viewModel.resetVerificationStatus()
        }
    }

    HODBaseScreen(
        title = "Research Monitoring & Compliance",
        subtitle = "Track faculty research plans, verify published works, and run compliance scans.",
        currentRoute = AppRoutes.HOD_RESEARCH_MONITORING,
        onNavigate = onNavigate
    ) {
        ScrollableTabRow(
            selectedTabIndex = listOf("monitoring", "verification").indexOf(activeTab).coerceAtLeast(0),
            containerColor = Color.Transparent,
            contentColor = CamsNavy,
            edgePadding = 0.dp,
            divider = {}
        ) {
            val tabs = listOf(
                "monitoring" to "Progress Monitoring",
                "verification" to "Pending Verifications"
            )
            tabs.forEach { (id, label) ->
                Tab(
                    selected = activeTab == id,
                    onClick = { activeTab = id },
                    text = { Text(label, fontWeight = if (activeTab == id) FontWeight.Bold else FontWeight.Medium) }
                )
            }
        }
        
        Spacer(modifier = Modifier.height(16.dp))
        
        if (uiState.isLoading) {
            Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator(color = Color(0xFF4338CA))
            }
        } else if (uiState.error != null) {
            Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Text("Error: ${uiState.error}", color = MaterialTheme.colorScheme.error)
            }
        } else if (activeTab == "monitoring") {
            CamsCard(modifier = Modifier.fillMaxWidth().weight(1f)) {
                Text("Faculty Research Progress Logs", fontWeight = FontWeight.Bold, fontSize = 16.sp, color = MaterialTheme.colorScheme.onSurface)
                Spacer(Modifier.height(16.dp))
                
                val monitoringData = uiState.monitoringData
                if (monitoringData.isEmpty()) {
                    Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        Text("No research monitoring data.", color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                } else {
                    LazyColumn(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                        items(monitoringData) { item ->
                            Row(
                                modifier = Modifier.fillMaxWidth().background(MaterialTheme.colorScheme.surface, RoundedCornerShape(12.dp)).border(1.dp, MaterialTheme.colorScheme.outlineVariant, RoundedCornerShape(12.dp)).padding(12.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(item.title, fontWeight = FontWeight.Bold, fontSize = 14.sp, color = MaterialTheme.colorScheme.onSurface)
                                    Text("${item.faculty_name ?: "Unknown"} • ${item.type}", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                    Spacer(Modifier.height(4.dp))
                                    Text("Area: ${item.area}", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                }
                                Column(horizontalAlignment = Alignment.End) {
                                    val isOverdue = item.daysOverdue > 0
                                    Text(
                                        item.status.uppercase(),
                                        fontSize = 12.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = if (isOverdue) Color(0xFFE11D48) else Color(0xFF1D4ED8),
                                        modifier = Modifier.background(if (isOverdue) Color(0xFFFFE4E6) else Color(0xFFDBEAFE), RoundedCornerShape(4.dp)).padding(horizontal = 8.dp, vertical = 4.dp)
                                    )
                                    Spacer(Modifier.height(4.dp))
                                    Text(if (isOverdue) "${item.daysOverdue} days overdue" else "On track", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant, fontWeight = FontWeight.Bold)
                                }
                            }
                        }
                    }
                }
            }
        } else if (activeTab == "verification") {
            CamsCard(modifier = Modifier.fillMaxWidth().weight(1f)) {
                Text("Pending Proof Verifications", fontWeight = FontWeight.Bold, fontSize = 16.sp, color = MaterialTheme.colorScheme.onSurface)
                Spacer(Modifier.height(16.dp))
                val proofs = uiState.pendingProofs
                if (proofs.isEmpty()) {
                    Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        Text("No pending proofs to verify.", color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                } else {
                    LazyColumn(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                        items(proofs) { proof ->
                            Column(
                                modifier = Modifier.fillMaxWidth().background(MaterialTheme.colorScheme.surface, RoundedCornerShape(12.dp)).border(1.dp, MaterialTheme.colorScheme.outlineVariant, RoundedCornerShape(12.dp)).padding(16.dp)
                            ) {
                                Text(proof.title ?: "Untitled", fontWeight = FontWeight.Bold, fontSize = 14.sp, color = MaterialTheme.colorScheme.onSurface)
                                Text("Faculty: ${proof.faculty_name ?: "Unknown"} • Journal: ${proof.journal_name}", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                Text("ISSN/ISBN: ${proof.issn_isbn}", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                
                                Spacer(Modifier.height(12.dp))
                                Row(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.fillMaxWidth()) {
                                    Button(
                                        onClick = { viewModel.verifyProof(proof.id, "VERIFIED", "Approved by HOD") },
                                        enabled = !uiState.isVerifying,
                                        modifier = Modifier.weight(1f),
                                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF059669)),
                                        shape = RoundedCornerShape(8.dp)
                                    ) {
                                        Text("Verify & Approve", fontSize = 12.sp)
                                    }
                                    Button(
                                        onClick = { viewModel.verifyProof(proof.id, "REJECTED", "Rejected by HOD") },
                                        enabled = !uiState.isVerifying,
                                        modifier = Modifier.weight(1f),
                                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFDC2626)),
                                        shape = RoundedCornerShape(8.dp)
                                    ) {
                                        Text("Reject", fontSize = 12.sp)
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
