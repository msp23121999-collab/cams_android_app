package com.example.features.faculty.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import androidx.compose.runtime.*
import com.example.features.faculty.providers.FacultyAssignmentsViewModel
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.core.theme.*
import com.example.core.ui.CamsCard
import com.example.features.faculty.widgets.FacultyBaseScreen

@Composable
fun FacultyAssignmentsScreen(
    viewModel: FacultyAssignmentsViewModel,
    onNavigate: (String) -> Unit
) {
    val state by viewModel.uiState.collectAsState()
    var selectedTab by remember { mutableStateOf(0) }
    val tabs = listOf("Active", "Evaluation", "Archived")

    FacultyBaseScreen(scrollable = false, 
        title = "Assignments",
        subtitle = "Create and grade student assignments",
        currentRoute = "/faculty/assignments",
        onNavigate = onNavigate
    ) {
        if (state.isLoading) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator(color = CamsNavy)
            }
        } else {
            // 1. Tab Navigation
            TabRow(
                selectedTabIndex = selectedTab,
                containerColor = Color.Transparent,
                contentColor = CamsNavy,
                indicator = { tabPositions ->
                    TabRowDefaults.SecondaryIndicator(
                        Modifier.tabIndicatorOffset(tabPositions[selectedTab]),
                        color = CamsNavy
                    )
                },
                divider = {}
            ) {
                tabs.forEachIndexed { index, title ->
                    Tab(
                        selected = selectedTab == index,
                        onClick = { selectedTab = index },
                        text = {
                            Text(
                                title,
                                fontSize = 14.sp,
                                fontWeight = if (selectedTab == index) FontWeight.Bold else FontWeight.Medium
                            )
                        }
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // 2. Main Content
            val filteredAssignments = when (selectedTab) {
                0 -> state.assignments.filter { it.status == "Active" }
                1 -> state.assignments.filter { it.status == "Evaluation" }
                else -> state.assignments.filter { it.status == "Archived" }
            }

            if (filteredAssignments.isEmpty()) {
                Text(
                    "No assignments found in this category.",
                    modifier = Modifier.padding(20.dp),
                    color = CamsTextSecondary,
                    fontSize = 14.sp
                )
            } else {
                LazyColumn(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                    items(filteredAssignments) { assignment ->
                        AssignmentCard(assignment, isEvaluation = selectedTab == 1)
                    }
                }
            }
        }
        
        Spacer(modifier = Modifier.height(80.dp))
    }
    
    // FAB for adding new assignment
    Box(modifier = Modifier.fillMaxSize()) {
        FloatingActionButton(
            onClick = { /* Add new assignment */ },
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(24.dp),
            containerColor = CamsNavy,
            contentColor = Color.White,
            shape = RoundedCornerShape(16.dp)
        ) {
            Icon(Icons.Filled.Add, contentDescription = "Add Assignment")
        }
    }
}

@Composable
fun AssignmentCard(assignment: com.example.core.network.FacultyAssignmentDto, isEvaluation: Boolean = false) {
    CamsCard {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Surface(
                    color = CamsNavy.copy(alpha = 0.1f),
                    shape = RoundedCornerShape(4.dp)
                ) {
                    Text(
                        assignment.id,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp),
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = CamsNavy
                    )
                }
                
                Text(
                    "Due: ${assignment.dueDate}",
                    fontSize = 12.sp,
                    color = if (isEvaluation) Color(0xFF64748B) else Color(0xFFEF4444),
                    fontWeight = FontWeight.Medium
                )
            }
            
            Spacer(modifier = Modifier.height(8.dp))
            
            Text(
                assignment.title,
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold,
                color = CamsTextPrimary
            )
            
            Spacer(modifier = Modifier.height(16.dp))
            
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text("Submissions", fontSize = 13.sp, color = CamsTextSecondary)
                    Text("${assignment.submitted}/${assignment.total}", fontSize = 14.sp, fontWeight = FontWeight.Bold, color = CamsTextPrimary)
                }
                
                val progress = if (assignment.total > 0) assignment.submitted.toFloat() / assignment.total.toFloat() else 0f
                LinearProgressIndicator(
                    progress = progress,
                    modifier = Modifier
                        .width(100.dp)
                        .height(8.dp),
                    color = if (progress > 0.8f) Color(0xFF10B981) else CamsNavy,
                    trackColor = MaterialTheme.colorScheme.surfaceVariant,
                    strokeCap = androidx.compose.ui.graphics.StrokeCap.Round
                )
                
                Button(
                    onClick = { /* View details */ },
                    contentPadding = PaddingValues(horizontal = 12.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = if (isEvaluation) Color(0xFF3B82F6) else MaterialTheme.colorScheme.surfaceVariant),
                    shape = RoundedCornerShape(8.dp),
                    modifier = Modifier.height(32.dp)
                ) {
                    Text(
                        if (isEvaluation) "Grade" else "View",
                        fontSize = 12.sp,
                        color = if (isEvaluation) Color.White else CamsNavy
                    )
                }
            }
        }
    }
}
