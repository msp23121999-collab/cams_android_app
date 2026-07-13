package com.example.features.principal.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.core.navigation.AppRoutes
import com.example.core.theme.*
import com.example.core.ui.CamsCard
import com.example.features.principal.widgets.PrincipalBaseScreen

import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.features.principal.providers.PrincipalApprovalsViewModel
import com.example.core.repository.PrincipalRepositoryImpl
import com.example.core.network.ApiClient

@Composable
fun PrincipalApprovalsScreen(
    onNavigate: (String) -> Unit,
    viewModel: PrincipalApprovalsViewModel = viewModel(
        factory = object : androidx.lifecycle.ViewModelProvider.Factory {
            override fun <T : androidx.lifecycle.ViewModel> create(modelClass: Class<T>): T {
                return PrincipalApprovalsViewModel(PrincipalRepositoryImpl(com.example.CamsApplication.instance.container.apiService)) as T
            }
        }
    )
) {
    val uiState by viewModel.uiState.collectAsState()
    var selectedTabIndex by remember { mutableStateOf(0) }
    val tabs = listOf("Leave Requests", "Faculty Onboarding")

    PrincipalBaseScreen(
        title = "Approvals & Onboarding",
        currentRoute = AppRoutes.PRINCIPAL_APPROVALS,
        onNavigate = onNavigate
    ) {
        TabRow(
            selectedTabIndex = selectedTabIndex,
            containerColor = Color.Transparent,
            contentColor = CamsNavy,
            indicator = { tabPositions ->
                if (selectedTabIndex < tabPositions.size) {
                    TabRowDefaults.SecondaryIndicator(
                        Modifier.tabIndicatorOffset(tabPositions[selectedTabIndex]),
                        color = CamsNavy
                    )
                }
            }
        ) {
            tabs.forEachIndexed { index, title ->
                Tab(
                    selected = selectedTabIndex == index,
                    onClick = { selectedTabIndex = index },
                    text = { Text(title, fontWeight = FontWeight.Bold) }
                )
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        if (uiState.isLoading) {
            Box(modifier = Modifier.fillMaxWidth().height(200.dp), contentAlignment = Alignment.Center) {
                CircularProgressIndicator()
            }
        } else {
            when (selectedTabIndex) {
                0 -> PrincipalLeavesTab(
                    leaves = uiState.pendingLeaves,
                    onApprove = { viewModel.approveLeave(it) },
                    onReject = { viewModel.rejectLeave(it) }
                )
                1 -> PrincipalFacultyOnboardingTab(
                    faculties = uiState.pendingFaculty,
                    onApprove = { viewModel.approveFaculty(it) }
                )
            }
        }
    }
}

@Composable
fun PrincipalLeavesTab(
    leaves: List<com.example.features.principal.models.LeaveApproval>,
    onApprove: (String) -> Unit,
    onReject: (String) -> Unit
) {
    
    LazyColumn(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        items(leaves) { leave ->
            CamsCard {
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                    Column {
                        Text(leave.applicantName, fontWeight = FontWeight.Bold, fontSize = 16.sp, color = CamsTextPrimary)
                        Text(leave.departmentName ?: "Unknown Department", fontSize = 14.sp, color = CamsTextSecondary)
                        Spacer(modifier = Modifier.height(8.dp))
                        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                            Icon(Icons.Filled.Event, null, modifier = Modifier.size(16.dp), tint = CamsTextSecondary)
                            Text("${leave.startDate} to ${leave.endDate}", fontSize = 12.sp, color = CamsTextSecondary)
                        }
                    }
                    Surface(
                        color = Color(0xFFF59E0B).copy(alpha = 0.1f),
                        shape = MaterialTheme.shapes.small
                    ) {
                        Text(
                            "Pending",
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFFB45309)
                        )
                    }
                }
                Spacer(modifier = Modifier.height(16.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(12.dp), modifier = Modifier.fillMaxWidth()) {
                    Button(
                        onClick = { onApprove(leave.id) },
                        modifier = Modifier.weight(1f),
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF10B981))
                    ) {
                        Text("Approve")
                    }
                    OutlinedButton(
                        onClick = { onReject(leave.id) },
                        modifier = Modifier.weight(1f),
                        colors = ButtonDefaults.outlinedButtonColors(contentColor = Color.Red)
                    ) {
                        Text("Reject")
                    }
                }
            }
        }
    }
}

@Composable
fun PrincipalFacultyOnboardingTab(
    faculties: List<com.example.features.principal.models.PrincipalPendingFaculty>,
    onApprove: (String) -> Unit
) {
    LazyColumn(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        items(faculties) { faculty ->
            CamsCard {
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                    Column {
                        Text(faculty.fullName, fontWeight = FontWeight.Bold, fontSize = 16.sp, color = CamsTextPrimary)
                        Text(faculty.departmentName, fontSize = 14.sp, color = CamsTextSecondary)
                        Spacer(modifier = Modifier.height(8.dp))
                        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                            Icon(Icons.Filled.Email, null, modifier = Modifier.size(16.dp), tint = CamsTextSecondary)
                            Text(faculty.email, fontSize = 12.sp, color = CamsTextSecondary)
                        }
                    }
                }
                Spacer(modifier = Modifier.height(16.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(12.dp), modifier = Modifier.fillMaxWidth()) {
                    Button(
                        onClick = { onApprove(faculty.id) },
                        modifier = Modifier.weight(1f),
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF10B981))
                    ) {
                        Text("Approve Onboarding")
                    }
                }
            }
        }
    }
}
