package com.example.features.admin.screens

import android.widget.Toast
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.ui.platform.LocalContext
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.*

import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.features.admin.providers.AdminTimetableViewModel
import com.example.features.admin.providers.AdminTimetableViewModelFactory
import com.example.core.repository.AdminRepositoryImpl
import com.example.CamsApplication

import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.core.theme.*
import com.example.core.ui.CamsCard
import com.example.features.admin.widgets.AdminBaseScreen
import com.example.core.navigation.AppRoutes

@Composable
fun AdminFacultyAssignmentScreen(
    onNavigate: (String) -> Unit,
    viewModel: AdminTimetableViewModel = viewModel(factory = AdminTimetableViewModelFactory(AdminRepositoryImpl(CamsApplication.instance.container.apiService)))
) {
    val context = LocalContext.current
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    var showAssignDialog by remember { mutableStateOf(false) }

    LaunchedEffect(uiState.saveSuccess, uiState.saveError) {
        if (uiState.saveSuccess) {
            Toast.makeText(context, "Subject assigned", Toast.LENGTH_SHORT).show()
            showAssignDialog = false
            viewModel.clearSaveStatus()
        }
        uiState.saveError?.let {
            Toast.makeText(context, it, Toast.LENGTH_LONG).show()
            viewModel.clearSaveStatus()
        }
    }

    AdminBaseScreen(
        title = "Faculty Assignments",
        subtitle = "Assign subjects to faculty members",
        currentRoute = AppRoutes.ADMIN_FACULTY_ASSIGNMENT,
        onNavigate = onNavigate
    ) {
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
            Text("Current Assignments", fontWeight = FontWeight.Bold, fontSize = 18.sp, color = MaterialTheme.colorScheme.onSurface)
            Button(onClick = { showAssignDialog = true }, colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF4F46E5)), shape = RoundedCornerShape(8.dp)) {
                Icon(Icons.Filled.Add, null, modifier = Modifier.size(16.dp))
                Spacer(Modifier.width(4.dp))
                Text("New Assignment", fontSize = 12.sp, fontWeight = FontWeight.Bold)
            }
        }

        uiState.setup?.let { s ->
            Spacer(Modifier.height(8.dp))
            Text("${s.department} • ${s.academicYear}", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }

        Spacer(Modifier.height(16.dp))

        uiState.error?.let {
            Text(it, color = Color(0xFFB91C1C), fontSize = 13.sp, modifier = Modifier.padding(bottom = 8.dp))
        }

        val subjectById = uiState.subjects.associateBy { it.id }
        val facultyById = uiState.faculty.associateBy { it.id }

        CamsCard(modifier = Modifier.fillMaxWidth().weight(1f)) {
            if (uiState.isLoading) {
                Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) { CircularProgressIndicator() }
            } else if (uiState.facultyAssignments.isEmpty()) {
                Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Text("No subject allocations yet", color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            } else {
                LazyColumn(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    items(uiState.facultyAssignments, key = { it.id }) { alloc ->
                        val subject = subjectById[alloc.courseId]
                        val fac = facultyById[alloc.facultyId]
                        Row(
                            modifier = Modifier.fillMaxWidth().background(MaterialTheme.colorScheme.surface, RoundedCornerShape(12.dp)).border(1.dp, MaterialTheme.colorScheme.outlineVariant, RoundedCornerShape(12.dp)).padding(16.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Box(Modifier.background(Color(0xFFEEF2FF), CircleShape).size(40.dp), contentAlignment = Alignment.Center) {
                                Icon(Icons.Filled.Person, null, tint = Color(0xFF4F46E5))
                            }
                            Spacer(Modifier.width(16.dp))
                            Column(modifier = Modifier.weight(1f)) {
                                Text(fac?.fullName ?: "Unassigned Faculty", fontWeight = FontWeight.Bold, fontSize = 14.sp, color = MaterialTheme.colorScheme.onSurface)
                                Text(subject?.subjectName ?: alloc.courseId, fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            }
                            subject?.let {
                                Text(
                                    "Semester ${it.semester}",
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    modifier = Modifier.background(MaterialTheme.colorScheme.surfaceVariant, RoundedCornerShape(4.dp)).padding(horizontal = 8.dp, vertical = 4.dp)
                                )
                            }
                        }
                    }
                }
            }
        }
    }

    if (showAssignDialog) {
        AssignSubjectDialog(
            subjects = uiState.subjects,
            faculty = uiState.faculty,
            sections = uiState.setup?.sections ?: emptyList(),
            isSaving = uiState.isSaving,
            onDismiss = { showAssignDialog = false },
            onSubmit = { subjectId, sectionId, facultyId -> viewModel.allocate(subjectId, sectionId, facultyId) }
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun AssignSubjectDialog(
    subjects: List<com.example.core.network.SubjectInfoDto>,
    faculty: List<com.example.core.network.FacultyWorkloadInfoDto>,
    sections: List<com.example.core.network.AcademicSetupSectionDto>,
    isSaving: Boolean,
    onDismiss: () -> Unit,
    onSubmit: (subjectId: String, sectionId: String, facultyId: String) -> Unit
) {
    var subjectId by remember { mutableStateOf("") }
    var sectionId by remember { mutableStateOf("") }
    var facultyId by remember { mutableStateOf("") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("New Assignment") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                PickerField("Subject", subjects.map { it.id to "${it.subjectCode} — ${it.subjectName}" }, subjectId) { subjectId = it }
                PickerField("Section", sections.map { it.id to it.name }, sectionId) { sectionId = it }
                PickerField("Faculty", faculty.map { it.id to "${it.fullName} (${it.currentWorkloadHours}/${it.maxWorkloadHours} hrs)" }, facultyId) { facultyId = it }
            }
        },
        confirmButton = {
            TextButton(
                enabled = subjectId.isNotBlank() && sectionId.isNotBlank() && facultyId.isNotBlank() && !isSaving,
                onClick = { onSubmit(subjectId, sectionId, facultyId) }
            ) { Text(if (isSaving) "Assigning..." else "Assign") }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text("Cancel") } }
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun PickerField(
    label: String,
    options: List<Pair<String, String>>,
    selectedId: String,
    onSelect: (String) -> Unit
) {
    var expanded by remember { mutableStateOf(false) }
    val selectedLabel = options.firstOrNull { it.first == selectedId }?.second ?: ""
    ExposedDropdownMenuBox(expanded = expanded, onExpandedChange = { expanded = !expanded }) {
        OutlinedTextField(
            value = selectedLabel,
            onValueChange = {},
            readOnly = true,
            label = { Text(label) },
            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
            modifier = Modifier.fillMaxWidth().menuAnchor()
        )
        ExposedDropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
            if (options.isEmpty()) {
                DropdownMenuItem(text = { Text("No options available") }, onClick = { expanded = false })
            } else {
                options.forEach { (id, text) ->
                    DropdownMenuItem(text = { Text(text) }, onClick = { onSelect(id); expanded = false })
                }
            }
        }
    }
}
