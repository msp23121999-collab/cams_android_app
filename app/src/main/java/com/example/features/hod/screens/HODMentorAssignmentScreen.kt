package com.example.features.hod.screens

import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.material3.MaterialTheme
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.core.theme.*
import com.example.core.ui.CamsCard
import com.example.features.hod.widgets.HODBaseScreen
import com.example.core.navigation.AppRoutes
import com.example.features.hod.providers.HODMentorAssignmentViewModel
import com.example.core.network.HODMentorDto

@Composable
fun HODMentorAssignmentScreen(
    onNavigate: (String) -> Unit,
    viewModel: HODMentorAssignmentViewModel = viewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    var selectedMentor by remember { mutableStateOf<HODMentorDto?>(null) }
    var selectedStudentIds by remember { mutableStateOf(setOf<String>()) }

    HODBaseScreen(
        title = "Student Mentorship Assignment",
        subtitle = "Allocate students to faculty mentors for academic guidance",
        currentRoute = AppRoutes.HOD_MENTOR_ASSIGNMENT,
        onNavigate = onNavigate
    ) {
        if (uiState.isLoading) {
            Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator()
            }
        } else {
            Row(Modifier.fillMaxWidth().weight(1f), horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                // Left Panel
                CamsCard(modifier = Modifier.weight(0.4f).fillMaxHeight()) {
                    Text("Department Staff", fontWeight = FontWeight.Bold, fontSize = 16.sp, color = MaterialTheme.colorScheme.onSurface)
                    Spacer(Modifier.height(12.dp))
                    LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        items(uiState.mentors) { mentor ->
                            val isSelected = selectedMentor?.faculty_id == mentor.faculty_id
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .background(if (isSelected) Color(0xFFEEF2FF) else Color.White, RoundedCornerShape(12.dp))
                                    .border(1.dp, if (isSelected) Color(0xFFC7D2FE) else MaterialTheme.colorScheme.surfaceVariant, RoundedCornerShape(12.dp))
                                    .padding(12.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                RadioButton(
                                    selected = isSelected,
                                    onClick = { selectedMentor = mentor },
                                    colors = RadioButtonDefaults.colors(selectedColor = Color(0xFF4338CA))
                                )
                                Spacer(Modifier.width(8.dp))
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(mentor.faculty_name, fontWeight = FontWeight.Bold, fontSize = 13.sp, color = if (isSelected) Color(0xFF4338CA) else CamsTextPrimary)
                                    Text(mentor.department, fontSize = 13.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                }
                                Text(
                                    "${mentor.total_students} Mentees", 
                                    fontSize = 12.sp, 
                                    fontWeight = FontWeight.Bold, 
                                    color = if (isSelected) Color(0xFF4338CA) else Color(0xFF64748B), 
                                    modifier = Modifier.background(if (isSelected) Color(0xFFE0E7FF) else MaterialTheme.colorScheme.surfaceVariant, RoundedCornerShape(4.dp)).padding(horizontal = 6.dp, vertical = 2.dp)
                                )
                            }
                        }
                    }
                }

                // Right Panel
                CamsCard(modifier = Modifier.weight(0.6f).fillMaxHeight()) {
                    if (selectedMentor == null) {
                        Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                            Text("Select a faculty member to manage their mentees.", color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                    } else {
                        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                            Text("Mentees of ${selectedMentor?.faculty_name}", fontWeight = FontWeight.Bold, fontSize = 16.sp, color = MaterialTheme.colorScheme.onSurface)
                            Button(
                                onClick = { 
                                    selectedStudentIds.forEach { studentId ->
                                        viewModel.assignMentor(studentId, selectedMentor!!.faculty_id)
                                    }
                                    selectedStudentIds = emptySet()
                                }, 
                                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF4F46E5)), 
                                shape = RoundedCornerShape(8.dp),
                                enabled = selectedStudentIds.isNotEmpty()
                            ) {
                                Text("Save Assignment", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                            }
                        }
                        Spacer(Modifier.height(16.dp))
                        LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                            items(uiState.students) { student ->
                                val isAssignedToThisMentor = null == selectedMentor?.faculty_id
                                val isSelected = selectedStudentIds.contains(student.id) || isAssignedToThisMentor
                                
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .background(if (isSelected) Color(0xFFEEF2FF) else Color.White, RoundedCornerShape(12.dp))
                                        .border(1.dp, if (isSelected) Color(0xFFC7D2FE) else MaterialTheme.colorScheme.surfaceVariant, RoundedCornerShape(12.dp))
                                        .padding(12.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Checkbox(
                                        checked = isSelected, 
                                        onCheckedChange = { checked -> 
                                            if (!isAssignedToThisMentor) {
                                                if (checked) {
                                                    selectedStudentIds = selectedStudentIds + student.id
                                                } else {
                                                    selectedStudentIds = selectedStudentIds - student.id
                                                }
                                            }
                                        },
                                        enabled = !isAssignedToThisMentor
                                    )
                                    Spacer(Modifier.width(8.dp))
                                    Column(modifier = Modifier.weight(1f)) {
                                        Text(student.name, fontWeight = FontWeight.Bold, fontSize = 13.sp, color = MaterialTheme.colorScheme.onSurface)
                                        Text("Roll No: ${student.rollNo ?: "N/A"} • ${"Dept"}", fontSize = 13.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                    }
                                    if (isAssignedToThisMentor) {
                                        Text("Assigned to this Mentor", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = Color(0xFF059669), modifier = Modifier.background(Color(0xFFD1FAE5), RoundedCornerShape(4.dp)).padding(horizontal = 6.dp, vertical = 2.dp))
                                    } else if (null == null) {
                                        Text("Unassigned", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = Color(0xFF64748B), modifier = Modifier.background(MaterialTheme.colorScheme.surfaceVariant, RoundedCornerShape(4.dp)).padding(horizontal = 6.dp, vertical = 2.dp))
                                    } else {
                                        Text("Other Mentor", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = Color(0xFFD97706), modifier = Modifier.background(Color(0xFFFEF3C7), RoundedCornerShape(4.dp)).padding(horizontal = 6.dp, vertical = 2.dp))
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
