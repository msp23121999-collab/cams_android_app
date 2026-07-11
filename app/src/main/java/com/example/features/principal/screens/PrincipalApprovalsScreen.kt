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

@Composable
fun PrincipalApprovalsScreen(onNavigate: (String) -> Unit) {
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

        when (selectedTabIndex) {
            0 -> PrincipalLeavesTab()
            1 -> PrincipalFacultyOnboardingTab()
        }
    }
}

@Composable
fun PrincipalLeavesTab() {
    val leaves = listOf(
        LeaveRequest("Dr. Ananya Sharma", "Computer Science", "Sick Leave", "Oct 25 - Oct 27, 2024", "Pending"),
        LeaveRequest("Prof. Rajesh Kumar", "Mechanical Eng.", "Casual Leave", "Oct 28, 2024", "Pending")
    )
    
    LazyColumn(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        items(leaves) { leave ->
            CamsCard {
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                    Column {
                        Text(leave.name, fontWeight = FontWeight.Bold, fontSize = 16.sp, color = CamsTextPrimary)
                        Text(leave.dept, fontSize = 14.sp, color = CamsTextSecondary)
                        Spacer(modifier = Modifier.height(8.dp))
                        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                            Icon(Icons.Filled.Event, null, modifier = Modifier.size(16.dp), tint = CamsTextSecondary)
                            Text(leave.date, fontSize = 12.sp, color = CamsTextSecondary)
                        }
                    }
                    Surface(
                        color = Color(0xFFF59E0B).copy(alpha = 0.1f),
                        shape = MaterialTheme.shapes.small
                    ) {
                        Text(
                            leave.status,
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
                        onClick = { /* Approve */ },
                        modifier = Modifier.weight(1f),
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF10B981))
                    ) {
                        Text("Approve")
                    }
                    OutlinedButton(
                        onClick = { /* Reject */ },
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
fun PrincipalFacultyOnboardingTab() {
    val faculties = listOf(
        FacultyOnboarding("Dr. Vikram Singh", "Artificial Intelligence", "vk.singh@cams.edu", "Pending Review")
    )
    
    LazyColumn(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        items(faculties) { faculty ->
            CamsCard {
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                    Column {
                        Text(faculty.name, fontWeight = FontWeight.Bold, fontSize = 16.sp, color = CamsTextPrimary)
                        Text(faculty.dept, fontSize = 14.sp, color = CamsTextSecondary)
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
                        onClick = { /* Approve */ },
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

data class LeaveRequest(val name: String, val dept: String, val type: String, val date: String, val status: String)
data class FacultyOnboarding(val name: String, val dept: String, val email: String, val status: String)
