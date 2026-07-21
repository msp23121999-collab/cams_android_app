package com.example.features.hod.screens

import android.widget.Toast
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.material3.MaterialTheme
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
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
    val context = LocalContext.current
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    var selectedMentor by remember { mutableStateOf<HODMentorDto?>(null) }
    var selectedStudentIds by remember { mutableStateOf(setOf<String>()) }

    LaunchedEffect(uiState.saveSuccess, uiState.saveError) {
        if (uiState.saveSuccess) {
            Toast.makeText(context, "Mentor assignment saved", Toast.LENGTH_SHORT).show()
            selectedStudentIds = emptySet()
            viewModel.clearSaveStatus()
        }
        uiState.saveError?.let {
            Toast.makeText(context, it, Toast.LENGTH_SHORT).show()
            viewModel.clearSaveStatus()
        }
    }

    // Keep selectedMentor pointing at the freshest object after each reload.
    LaunchedEffect(uiState.mentors) {
        selectedMentor = uiState.mentors.find { it.id == selectedMentor?.id }
    }

    HODBaseScreen(
        title = "Student Mentorship Assignment",
        subtitle = "Allocate students to faculty mentors for academic guidance",
        currentRoute = AppRoutes.HOD_MENTOR_ASSIGNMENT,
        onNavigate = onNavigate
    ) {
        uiState.error?.let {
            Text(it, color = Color(0xFFB91C1C), fontSize = 13.sp, modifier = Modifier.padding(bottom = 8.dp))
        }

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
                        items(uiState.mentors, key = { it.id }) { mentor ->
                            val isSelected = selectedMentor?.id == mentor.id
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
                                    onClick = { selectedMentor = mentor; selectedStudentIds = emptySet() },
                                    colors = RadioButtonDefaults.colors(selectedColor = Color(0xFF4338CA))
                                )
                                Spacer(Modifier.width(8.dp))
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(mentor.name, fontWeight = FontWeight.Bold, fontSize = 13.sp, color = if (isSelected) Color(0xFF4338CA) else CamsTextPrimary)
                                    Text(mentor.email, fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                }
                                Text(
                                    "${mentor.students.size} Mentees",
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
                    val mentor = selectedMentor
                    if (mentor == null) {
                        Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                            Text("Select a faculty member to manage their mentees.", color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                    } else {
                        val currentMenteeIds = mentor.students.map { it.id }.toSet()
                        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                            Text("Mentees of ${mentor.name}", fontWeight = FontWeight.Bold, fontSize = 16.sp, color = MaterialTheme.colorScheme.onSurface)
                            Button(
                                onClick = {
                                    viewModel.assignMentor(mentor.id, (currentMenteeIds + selectedStudentIds).toList())
                                },
                                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF4F46E5)),
                                shape = RoundedCornerShape(8.dp),
                                enabled = selectedStudentIds.isNotEmpty() && !uiState.isSaving
                            ) {
                                Text(if (uiState.isSaving) "Saving..." else "Save Assignment", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                            }
                        }
                        Spacer(Modifier.height(16.dp))
                        LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                            items(uiState.students, key = { it.id }) { student ->
                                val isAssignedToThisMentor = currentMenteeIds.contains(student.id)
                                val isAssignedElsewhere = !student.mentorId.isNullOrBlank() && student.mentorId != mentor.id
                                val isChecked = selectedStudentIds.contains(student.id) || isAssignedToThisMentor

                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .background(if (isChecked) Color(0xFFEEF2FF) else Color.White, RoundedCornerShape(12.dp))
                                        .border(1.dp, if (isChecked) Color(0xFFC7D2FE) else MaterialTheme.colorScheme.surfaceVariant, RoundedCornerShape(12.dp))
                                        .padding(12.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Checkbox(
                                        checked = isChecked,
                                        onCheckedChange = { checked ->
                                            if (!isAssignedToThisMentor) {
                                                selectedStudentIds = if (checked) selectedStudentIds + student.id else selectedStudentIds - student.id
                                            }
                                        },
                                        enabled = !isAssignedToThisMentor
                                    )
                                    Spacer(Modifier.width(8.dp))
                                    Column(modifier = Modifier.weight(1f)) {
                                        Text(student.name, fontWeight = FontWeight.Bold, fontSize = 13.sp, color = MaterialTheme.colorScheme.onSurface)
                                        Text("Roll No: ${student.rollNo} • Sem ${student.semester}", fontSize = 13.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                    }
                                    when {
                                        isAssignedToThisMentor -> Text("Assigned to this Mentor", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = Color(0xFF059669), modifier = Modifier.background(Color(0xFFD1FAE5), RoundedCornerShape(4.dp)).padding(horizontal = 6.dp, vertical = 2.dp))
                                        isAssignedElsewhere -> Text("Other Mentor", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = Color(0xFFD97706), modifier = Modifier.background(Color(0xFFFEF3C7), RoundedCornerShape(4.dp)).padding(horizontal = 6.dp, vertical = 2.dp))
                                        else -> Text("Unassigned", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = Color(0xFF64748B), modifier = Modifier.background(MaterialTheme.colorScheme.surfaceVariant, RoundedCornerShape(4.dp)).padding(horizontal = 6.dp, vertical = 2.dp))
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
