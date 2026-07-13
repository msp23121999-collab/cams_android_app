package com.example.features.hod.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.core.navigation.AppRoutes


import com.example.core.theme.CamsTextPrimary
import com.example.core.theme.CamsTextSecondary
import com.example.features.hod.widgets.HODBaseScreen

import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.features.hod.providers.HODApprovalsViewModel

@Composable
fun HODApprovalsScreen(
    viewModel: HODApprovalsViewModel,
    onNavigate: (String) -> Unit
) {
    val uiState by viewModel.uiState.collectAsState()

    HODBaseScreen(scrollable = false, 
        title = "Pending Approvals",
        subtitle = "Manage faculty requests, mark approvals, and departmental decisions",
        currentRoute = AppRoutes.HOD_APPROVALS,
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
                KpiCard("Pending Faculty Leaves", "5", Icons.Filled.EventBusy, Color(0xFFE11D48), Color(0xFFFFF1F2), Modifier.weight(1f))
                KpiCard("Pending Mark Approvals", "8", Icons.Filled.FactCheck, Color(0xFF059669), Color(0xFFECFDF5), Modifier.weight(1f))
            }
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.fillMaxWidth()) {
                KpiCard("Student Shortages", "15", Icons.Filled.Warning, Color(0xFFD97706), Color(0xFFFFFBEB), Modifier.weight(1f))
                KpiCard("Syllabus Revisions", "2", Icons.Filled.MenuBook, Color(0xFF2563EB), Color(0xFFEFF6FF), Modifier.weight(1f))
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
                                imageVector = Icons.Default.CheckCircle,
                                contentDescription = null,
                                tint = Color(0xFF4F46E5),
                                modifier = Modifier.size(16.dp)
                            )
                        }
                        Column(modifier = Modifier.weight(1f)) {
                            Text("Recent Approval Requests", fontWeight = FontWeight.Bold, fontSize = 14.sp, color = CamsTextPrimary)
                            Text("Review and take action on pending requests", fontSize = 12.sp, color = CamsTextSecondary)
                        }
                        Button(
                            onClick = { },
                            colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.surface, contentColor = CamsTextPrimary),
                            border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
                            shape = RoundedCornerShape(8.dp),
                            contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp),
                            modifier = Modifier.height(32.dp)
                        ) {
                            Icon(Icons.Filled.FilterList, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(Modifier.width(6.dp))
                            Text("Filter", fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
                        }
                    }

                    LazyColumn(contentPadding = PaddingValues(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                        if (uiState.isLoading) {
                            item {
                                Box(modifier = Modifier.fillMaxWidth().height(100.dp), contentAlignment = Alignment.Center) {
                                    CircularProgressIndicator(color = Color(0xFF4F46E5))
                                }
                            }
                        } else if (uiState.pendingLeaves.isEmpty()) {
                            item {
                                Box(modifier = Modifier.fillMaxWidth().padding(40.dp), contentAlignment = Alignment.Center) {
                                    Text("No pending approvals", color = CamsTextSecondary)
                                }
                            }
                        } else {
                            items(uiState.pendingLeaves.size) { i ->
                                val approval = uiState.pendingLeaves[i]
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
                                        Icon(Icons.Filled.Notifications, null, tint = CamsTextSecondary, modifier = Modifier.size(20.dp))
                                    }
                                    Spacer(Modifier.width(16.dp))
                                    Column(modifier = Modifier.weight(1f)) {
                                        Text("${approval.type} Leave", fontWeight = FontWeight.Bold, fontSize = 14.sp, color = CamsTextPrimary)
                                        Spacer(Modifier.height(4.dp))
                                        Text("${approval.userName ?: "Unknown"} • ${approval.startDate} to ${approval.endDate}", fontSize = 12.sp, color = CamsTextSecondary)
                                    }
                                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                        IconButton(
                                            onClick = { viewModel.approveLeave(approval.id, "REJECTED", null) },
                                            modifier = Modifier.size(32.dp).background(Color(0xFFFFF1F2), RoundedCornerShape(8.dp))
                                        ) {
                                            Icon(Icons.Filled.Close, null, tint = Color(0xFFE11D48), modifier = Modifier.size(16.dp))
                                        }
                                        IconButton(
                                            onClick = { viewModel.approveLeave(approval.id, "APPROVED", null) },
                                            modifier = Modifier.size(32.dp).background(Color(0xFFECFDF5), RoundedCornerShape(8.dp))
                                        ) {
                                            Icon(Icons.Filled.Check, null, tint = Color(0xFF059669), modifier = Modifier.size(16.dp))
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
            Text(label, fontSize = 12.sp, fontWeight = FontWeight.Bold, color = CamsTextSecondary, modifier = Modifier.fillMaxWidth())
            Spacer(Modifier.height(8.dp))
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                Box(Modifier.background(bgColor, RoundedCornerShape(8.dp)).padding(6.dp)) {
                    Icon(icon, null, tint = color, modifier = Modifier.size(20.dp))
                }
                Text(value, fontSize = 24.sp, fontWeight = FontWeight.Black, color = CamsTextPrimary)
            }
        }
    }
}
