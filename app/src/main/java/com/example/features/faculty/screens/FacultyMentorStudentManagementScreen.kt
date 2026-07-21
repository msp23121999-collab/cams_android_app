package com.example.features.faculty.screens

import android.widget.Toast
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
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
import com.example.features.faculty.widgets.FacultyBaseScreen

import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.core.network.FacultyMentorshipRecordDto
import com.example.core.network.FacultyMentorshipStudentDto
import com.example.core.repository.FacultyRepository
import com.example.core.repository.FacultyRepositoryImpl
import com.example.features.faculty.providers.FacultyResearchViewModel
import com.example.features.faculty.providers.FacultyResearchViewModelFactory
import kotlinx.coroutines.launch

@Composable
fun FacultyMentorStudentManagementScreen(onNavigate: (String) -> Unit) {
    val repository = remember { FacultyRepositoryImpl(com.example.CamsApplication.instance.container.apiService) }
    val factory = remember { FacultyResearchViewModelFactory(repository) }
    val viewModel: FacultyResearchViewModel = viewModel(factory = factory)
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    var menteePendingDiary by remember { mutableStateOf<FacultyMentorshipStudentDto?>(null) }

    FacultyBaseScreen(scrollable = false,
        title = "Mentor Student Management",
        currentRoute = "/faculty/mentor-student-management",
        onNavigate = onNavigate
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp)
        ) {
            // Mentor Overview Card
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = CamsNavy),
                shape = RoundedCornerShape(24.dp)
            ) {
                Row(
                    modifier = Modifier.padding(24.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Column {
                        Text("Total Mentees", color = Color.White.copy(alpha = 0.7f), fontSize = 14.sp)
                        Text("${uiState.mentorStudents.size} Students", color = Color.White, fontSize = 24.sp, fontWeight = FontWeight.Black)
                    }
                    Box(
                        modifier = Modifier
                            .size(56.dp)
                            .background(MaterialTheme.colorScheme.surface.copy(alpha = 0.2f), CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(Icons.Filled.Groups, null, tint = Color.White, modifier = Modifier.size(32.dp))
                    }
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            Text("My Mentees", fontWeight = FontWeight.Bold, fontSize = 18.sp, color = MaterialTheme.colorScheme.onSurface)
            Spacer(modifier = Modifier.height(12.dp))

            uiState.error?.let {
                Text(it, color = Color(0xFFB91C1C), fontSize = 13.sp, modifier = Modifier.padding(bottom = 8.dp))
            }

            if (uiState.mentorStudents.isEmpty() && !uiState.isLoading) {
                Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Text("No mentees assigned", color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            } else {
                LazyColumn(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    items(uiState.mentorStudents, key = { it.id }) { mentee ->
                        MenteeItem(mentee, onOpenDiary = { menteePendingDiary = mentee })
                    }
                }
            }
        }
    }

    menteePendingDiary?.let { mentee ->
        MentorshipDiaryDialog(
            mentee = mentee,
            repository = repository,
            onDismiss = { menteePendingDiary = null }
        )
    }
}

@Composable
private fun MenteeItem(mentee: FacultyMentorshipStudentDto, onOpenDiary: () -> Unit) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        shape = RoundedCornerShape(16.dp)
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            horizontalArrangement = Arrangement.spacedBy(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(48.dp)
                    .background(CamsNavy.copy(alpha = 0.1f), CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Icon(Icons.Filled.Person, null, tint = MaterialTheme.colorScheme.primary)
            }
            Column(modifier = Modifier.weight(1f)) {
                Text(mentee.name, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurface, fontSize = 16.sp)
                Text("ID: ${mentee.rollNo}", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                Spacer(modifier = Modifier.height(4.dp))
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                    Box(
                        modifier = Modifier
                            .size(8.dp)
                            .background(Color(0xFF10B981), CircleShape)
                    )
                    Text("Semester ${mentee.semester ?: "N/A"}", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurface)
                }
            }
            IconButton(onClick = onOpenDiary) {
                Icon(Icons.Filled.ChevronRight, contentDescription = "Open", tint = Color(0xFF64748B))
            }
        }
    }
}

@Composable
private fun MentorshipDiaryDialog(
    mentee: FacultyMentorshipStudentDto,
    repository: FacultyRepository,
    onDismiss: () -> Unit
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    var isLoading by remember { mutableStateOf(true) }
    var isSaving by remember { mutableStateOf(false) }
    var meetingLog by remember { mutableStateOf("") }
    var academicReview by remember { mutableStateOf("") }
    var improvementPlan by remember { mutableStateOf("") }
    var remarks by remember { mutableStateOf("") }
    var followUp by remember { mutableStateOf("") }

    LaunchedEffect(mentee.id) {
        isLoading = true
        try {
            val record = repository.getMentorStudentRecord(mentee.id)
            meetingLog = record?.meetingLog ?: ""
            academicReview = record?.academicReview ?: ""
            improvementPlan = record?.improvementPlan ?: ""
            remarks = record?.remarks ?: ""
            followUp = record?.followUp ?: ""
        } catch (e: Exception) {
            Toast.makeText(context, e.message ?: "Failed to load mentorship record", Toast.LENGTH_SHORT).show()
        } finally {
            isLoading = false
        }
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Mentorship Diary — ${mentee.name}") },
        text = {
            if (isLoading) {
                Box(Modifier.fillMaxWidth().padding(24.dp), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator(color = CamsNavy)
                }
            } else {
                Column(
                    modifier = Modifier.verticalScroll(rememberScrollState()),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    OutlinedTextField(value = meetingLog, onValueChange = { meetingLog = it }, label = { Text("Meeting Log") }, modifier = Modifier.fillMaxWidth())
                    OutlinedTextField(value = academicReview, onValueChange = { academicReview = it }, label = { Text("Academic Review") }, modifier = Modifier.fillMaxWidth())
                    OutlinedTextField(value = improvementPlan, onValueChange = { improvementPlan = it }, label = { Text("Improvement Plan") }, modifier = Modifier.fillMaxWidth())
                    OutlinedTextField(value = remarks, onValueChange = { remarks = it }, label = { Text("Remarks") }, modifier = Modifier.fillMaxWidth())
                    OutlinedTextField(value = followUp, onValueChange = { followUp = it }, label = { Text("Follow-up (date/notes)") }, modifier = Modifier.fillMaxWidth())
                }
            }
        },
        confirmButton = {
            TextButton(
                enabled = !isLoading && !isSaving,
                onClick = {
                    isSaving = true
                    scope.launch {
                        try {
                            repository.saveMentorStudentRecord(
                                mentee.id,
                                FacultyMentorshipRecordDto(
                                    studentId = mentee.id,
                                    mentorId = null,
                                    meetingLog = meetingLog.ifBlank { null },
                                    academicReview = academicReview.ifBlank { null },
                                    improvementPlan = improvementPlan.ifBlank { null },
                                    remarks = remarks.ifBlank { null },
                                    followUp = followUp.ifBlank { null }
                                )
                            )
                            Toast.makeText(context, "Mentorship record saved", Toast.LENGTH_SHORT).show()
                            onDismiss()
                        } catch (e: Exception) {
                            Toast.makeText(context, e.message ?: "Failed to save", Toast.LENGTH_SHORT).show()
                        } finally {
                            isSaving = false
                        }
                    }
                }
            ) { Text(if (isSaving) "Saving..." else "Save") }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Close") }
        }
    )
}
