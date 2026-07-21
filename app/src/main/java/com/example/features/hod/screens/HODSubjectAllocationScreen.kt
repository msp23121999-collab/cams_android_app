package com.example.features.hod.screens

import android.widget.Toast
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
import com.example.core.network.FacultyWorkloadInfoDto
import com.example.core.network.SubjectInfoDto
import com.example.core.theme.*
import com.example.core.ui.CamsCard
import com.example.features.hod.widgets.HODBaseScreen
import com.example.core.navigation.AppRoutes
import com.example.features.hod.providers.HODSubjectAllocationViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HODSubjectAllocationScreen(
    onNavigate: (String) -> Unit,
    viewModel: HODSubjectAllocationViewModel = viewModel()
) {
    val context = LocalContext.current
    var activeTab by remember { mutableStateOf("allocation") }
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    var showAllocateDialog by remember { mutableStateOf(false) }

    LaunchedEffect(uiState.saveSuccess, uiState.saveError) {
        if (uiState.saveSuccess) {
            Toast.makeText(context, "Allocation saved", Toast.LENGTH_SHORT).show()
            showAllocateDialog = false
            viewModel.clearSaveStatus()
        }
        uiState.saveError?.let {
            Toast.makeText(context, it, Toast.LENGTH_SHORT).show()
            viewModel.clearSaveStatus()
        }
    }

    val subjectsById = uiState.subjects.associateBy { it.id }
    val facultyById = uiState.faculty.associateBy { it.id }

    HODBaseScreen(
        title = "Subject Allocation",
        subtitle = "Assign Faculty to Subjects for ${uiState.setup?.academicYear ?: "Term"}",
        currentRoute = AppRoutes.HOD_SUBJECT_ALLOCATION,
        onNavigate = onNavigate
    ) {
        // Tabs
        Row(Modifier.fillMaxWidth().background(MaterialTheme.colorScheme.surface, RoundedCornerShape(12.dp)).padding(4.dp)) {
            TabButton("Subject Allocation", activeTab == "allocation", Modifier.weight(1f)) { activeTab = "allocation" }
            TabButton("Faculty Workload", activeTab == "workload", Modifier.weight(1f)) { activeTab = "workload" }
        }

        Spacer(Modifier.height(16.dp))

        uiState.error?.let {
            Text(it, color = Color(0xFFB91C1C), fontSize = 13.sp, modifier = Modifier.padding(bottom = 8.dp))
        }

        CamsCard(modifier = Modifier.fillMaxWidth().weight(1f)) {
            if (activeTab == "allocation") {
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                    Text("Assign Faculty to Subjects", fontWeight = FontWeight.Bold, fontSize = 16.sp, color = MaterialTheme.colorScheme.onSurface)
                    Button(onClick = { showAllocateDialog = true }, colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF4F46E5)), shape = RoundedCornerShape(8.dp)) {
                        Text("New Allocation", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                    }
                }
                Spacer(Modifier.height(12.dp))

                if (uiState.isLoading) {
                    Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        CircularProgressIndicator()
                    }
                } else if (uiState.allocations.isEmpty()) {
                    Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        Text("No subjects allocated for current term.", color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                } else {
                    LazyColumn(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                        items(uiState.allocations, key = { it.id }) { alloc ->
                            val subject = subjectsById[alloc.courseId]
                            val faculty = facultyById[alloc.facultyId]
                            CamsCard(containerColor = MaterialTheme.colorScheme.background, modifier = Modifier.fillMaxWidth()) {
                                Column(Modifier.padding(12.dp)) {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Text(subject?.subjectCode ?: "?", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = Color(0xFF4F46E5), modifier = Modifier.background(Color(0xFFEEF2FF), RoundedCornerShape(4.dp)).padding(4.dp))
                                        Spacer(Modifier.width(8.dp))
                                        Text("Sem ${subject?.semester ?: "-"}", fontSize = 12.sp, color = Color(0xFF047857), modifier = Modifier.background(Color(0xFFECFDF5), RoundedCornerShape(4.dp)).padding(4.dp))
                                    }
                                    Spacer(Modifier.height(8.dp))
                                    Text(subject?.subjectName ?: "Unknown Subject", fontWeight = FontWeight.Bold, fontSize = 14.sp, color = MaterialTheme.colorScheme.onSurface)
                                    Spacer(Modifier.height(8.dp))
                                    Text("Assigned Faculty", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                    Spacer(Modifier.height(4.dp))
                                    Box(modifier = Modifier.fillMaxWidth().background(MaterialTheme.colorScheme.surface, RoundedCornerShape(8.dp)).border(1.dp, MaterialTheme.colorScheme.outlineVariant, RoundedCornerShape(8.dp)).padding(12.dp)) {
                                        Text(faculty?.fullName ?: "Unassigned", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurface)
                                    }
                                }
                            }
                        }
                    }
                }
            } else {
                if (uiState.isLoading) {
                    Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        CircularProgressIndicator()
                    }
                } else if (uiState.faculty.isEmpty()) {
                    Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        Text("No faculty workload data available.", color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                } else {
                    LazyColumn(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                        items(uiState.faculty, key = { it.id }) { fac ->
                            CamsCard(containerColor = MaterialTheme.colorScheme.background, modifier = Modifier.fillMaxWidth()) {
                                Row(
                                    modifier = Modifier.fillMaxWidth().padding(12.dp),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Column {
                                        Text(fac.fullName, fontWeight = FontWeight.Bold, fontSize = 14.sp, color = MaterialTheme.colorScheme.onSurface)
                                        Text(fac.designation ?: "Faculty", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                    }
                                    Text(
                                        "${fac.currentWorkloadHours}/${fac.maxWorkloadHours} hrs",
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 13.sp,
                                        color = if (fac.currentWorkloadHours >= fac.maxWorkloadHours) Color(0xFFE11D48) else Color(0xFF059669)
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    if (showAllocateDialog) {
        AllocateSubjectDialog(
            subjects = uiState.subjects,
            faculty = uiState.faculty,
            sections = uiState.courseSections,
            isLoadingSections = uiState.isLoadingSections,
            isSaving = uiState.isSaving,
            onSubjectSelected = { viewModel.loadSectionsForCourse(it.id) },
            onDismiss = { showAllocateDialog = false },
            onSubmit = { courseId, sectionId, facultyId -> viewModel.allocate(courseId, sectionId, facultyId) }
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun AllocateSubjectDialog(
    subjects: List<SubjectInfoDto>,
    faculty: List<FacultyWorkloadInfoDto>,
    sections: List<com.example.core.network.AcademicSetupSectionDto>,
    isLoadingSections: Boolean,
    isSaving: Boolean,
    onSubjectSelected: (SubjectInfoDto) -> Unit,
    onDismiss: () -> Unit,
    onSubmit: (courseId: String, sectionId: String, facultyId: String) -> Unit
) {
    var selectedSubject by remember { mutableStateOf<SubjectInfoDto?>(null) }
    var selectedSection by remember { mutableStateOf<com.example.core.network.AcademicSetupSectionDto?>(null) }
    var selectedFaculty by remember { mutableStateOf<FacultyWorkloadInfoDto?>(null) }
    var subjectMenuExpanded by remember { mutableStateOf(false) }
    var sectionMenuExpanded by remember { mutableStateOf(false) }
    var facultyMenuExpanded by remember { mutableStateOf(false) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("New Allocation") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                ExposedDropdownMenuBox(expanded = subjectMenuExpanded, onExpandedChange = { subjectMenuExpanded = it }) {
                    OutlinedTextField(
                        value = selectedSubject?.let { "${it.subjectCode} - ${it.subjectName}" } ?: "Select Subject",
                        onValueChange = {}, readOnly = true,
                        modifier = Modifier.fillMaxWidth().menuAnchor(),
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = subjectMenuExpanded) }
                    )
                    ExposedDropdownMenu(expanded = subjectMenuExpanded, onDismissRequest = { subjectMenuExpanded = false }) {
                        subjects.forEach { s ->
                            DropdownMenuItem(text = { Text("${s.subjectCode} - ${s.subjectName}") }, onClick = {
                                selectedSubject = s
                                selectedSection = null
                                subjectMenuExpanded = false
                                onSubjectSelected(s)
                            })
                        }
                    }
                }
                ExposedDropdownMenuBox(expanded = sectionMenuExpanded, onExpandedChange = { if (selectedSubject != null) sectionMenuExpanded = it }) {
                    OutlinedTextField(
                        value = selectedSection?.name ?: if (isLoadingSections) "Loading..." else "Select Section",
                        onValueChange = {}, readOnly = true,
                        enabled = selectedSubject != null,
                        modifier = Modifier.fillMaxWidth().menuAnchor(),
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = sectionMenuExpanded) }
                    )
                    ExposedDropdownMenu(expanded = sectionMenuExpanded, onDismissRequest = { sectionMenuExpanded = false }) {
                        sections.forEach { s ->
                            DropdownMenuItem(text = { Text(s.name) }, onClick = { selectedSection = s; sectionMenuExpanded = false })
                        }
                    }
                }
                ExposedDropdownMenuBox(expanded = facultyMenuExpanded, onExpandedChange = { facultyMenuExpanded = it }) {
                    OutlinedTextField(
                        value = selectedFaculty?.fullName ?: "Select Faculty",
                        onValueChange = {}, readOnly = true,
                        modifier = Modifier.fillMaxWidth().menuAnchor(),
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = facultyMenuExpanded) }
                    )
                    ExposedDropdownMenu(expanded = facultyMenuExpanded, onDismissRequest = { facultyMenuExpanded = false }) {
                        faculty.forEach { f ->
                            DropdownMenuItem(text = { Text(f.fullName) }, onClick = { selectedFaculty = f; facultyMenuExpanded = false })
                        }
                    }
                }
            }
        },
        confirmButton = {
            TextButton(
                enabled = selectedSubject != null && selectedSection != null && selectedFaculty != null && !isSaving,
                onClick = { onSubmit(selectedSubject!!.id, selectedSection!!.id, selectedFaculty!!.id) }
            ) { Text(if (isSaving) "Saving..." else "Save") }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Cancel") }
        }
    )
}

@Composable
private fun TabButton(text: String, selected: Boolean, modifier: Modifier, onClick: () -> Unit) {
    Button(
        onClick = onClick,
        modifier = modifier,
        colors = ButtonDefaults.buttonColors(
            containerColor = if (selected) Color(0xFFEEF2FF) else Color.Transparent,
            contentColor = if (selected) Color(0xFF4338CA) else Color(0xFF64748B)
        ),
        shape = RoundedCornerShape(8.dp),
        elevation = null
    ) {
        Text(text, fontSize = 13.sp, fontWeight = FontWeight.Bold)
    }
}
