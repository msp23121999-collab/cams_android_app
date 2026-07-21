package com.example.features.admin.screens

import android.widget.Toast
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.CamsApplication
import com.example.core.repository.AdminRepositoryImpl
import com.example.core.theme.*
import com.example.core.ui.CamsCard
import com.example.features.admin.models.AdminCourse
import com.example.features.admin.providers.AdminCourseSetupViewModel
import com.example.features.admin.providers.AdminCourseSetupViewModelFactory
import com.example.features.admin.widgets.AdminBaseScreen

@Composable
fun AdminCourseSetupScreen(
    onNavigate: (String) -> Unit,
    viewModel: AdminCourseSetupViewModel = viewModel(
        factory = AdminCourseSetupViewModelFactory(AdminRepositoryImpl(CamsApplication.instance.container.apiService))
    )
) {
    val context = LocalContext.current
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    var showCreateDialog by remember { mutableStateOf(false) }
    var courseBeingEdited by remember { mutableStateOf<AdminCourse?>(null) }
    var coursePendingDelete by remember { mutableStateOf<AdminCourse?>(null) }

    LaunchedEffect(uiState.saveSuccess, uiState.saveError) {
        if (uiState.saveSuccess) {
            Toast.makeText(context, "Saved", Toast.LENGTH_SHORT).show()
            showCreateDialog = false
            courseBeingEdited = null
            viewModel.clearSaveStatus()
        }
        uiState.saveError?.let {
            Toast.makeText(context, it, Toast.LENGTH_LONG).show()
            viewModel.clearSaveStatus()
        }
    }

    AdminBaseScreen(
        title = "Course Setup",
        subtitle = "Manage the courses offered under each degree",
        currentRoute = "admin-course-setup",
        onNavigate = onNavigate,
        floatingActionButton = {
            if (uiState.selectedDegreeId != null) {
                FloatingActionButton(
                    onClick = { showCreateDialog = true },
                    containerColor = CamsNavy,
                    contentColor = Color.White
                ) { Icon(Icons.Filled.Add, "Add Course") }
            }
        }
    ) {
        Column(Modifier.fillMaxSize().padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {

            if (uiState.degrees.isEmpty() && !uiState.isLoading) {
                Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Text("No degrees configured yet. Add a degree first.", color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
                return@AdminBaseScreen
            }

            DegreePicker(
                degrees = uiState.degrees,
                selectedId = uiState.selectedDegreeId,
                onSelect = { viewModel.selectDegree(it) }
            )

            uiState.error?.let {
                Text(it, color = Color(0xFFB91C1C), fontSize = 13.sp)
            }

            Text(
                "${uiState.courses.size} course(s)",
                fontSize = 12.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            if (uiState.isLoading) {
                Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) { CircularProgressIndicator() }
            } else if (uiState.courses.isEmpty()) {
                Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Text("No courses under this degree yet", color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            } else {
                LazyColumn(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    items(uiState.courses.sortedWith(compareBy({ it.semester }, { it.code })), key = { it.id }) { course ->
                        CamsCard {
                            Row(Modifier.fillMaxWidth().padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
                                Column(Modifier.weight(1f)) {
                                    Text(course.name, fontWeight = FontWeight.Bold, fontSize = 15.sp, color = MaterialTheme.colorScheme.onSurface)
                                    Spacer(Modifier.height(4.dp))
                                    Text(
                                        "${course.code} • ${course.credits ?: 0} credits • Semester ${course.semester}",
                                        fontSize = 12.sp,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                                IconButton(onClick = { courseBeingEdited = course }) {
                                    Icon(Icons.Filled.Edit, "Edit course", tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(18.dp))
                                }
                                IconButton(onClick = { coursePendingDelete = course }) {
                                    Icon(Icons.Filled.Delete, "Delete course", tint = Color(0xFFB91C1C), modifier = Modifier.size(18.dp))
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    if (showCreateDialog) {
        CourseDialog(
            title = "New Course",
            existing = null,
            isSaving = uiState.isSaving,
            onDismiss = { showCreateDialog = false },
            onSubmit = { code, name, credits, sem -> viewModel.createCourse(code, name, credits, sem) }
        )
    }

    courseBeingEdited?.let { course ->
        CourseDialog(
            title = "Edit Course",
            existing = course,
            isSaving = uiState.isSaving,
            onDismiss = { courseBeingEdited = null },
            onSubmit = { code, name, credits, sem -> viewModel.updateCourse(course.id, code, name, credits, sem) }
        )
    }

    coursePendingDelete?.let { course ->
        AlertDialog(
            onDismissRequest = { coursePendingDelete = null },
            title = { Text("Delete Course") },
            text = { Text("Delete \"${course.name}\" (${course.code})? This cannot be undone.") },
            confirmButton = {
                TextButton(onClick = { viewModel.deleteCourse(course.id); coursePendingDelete = null }) {
                    Text("Delete", color = Color(0xFFB91C1C))
                }
            },
            dismissButton = { TextButton(onClick = { coursePendingDelete = null }) { Text("Cancel") } }
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun DegreePicker(
    degrees: List<com.example.features.admin.models.AdminDegree>,
    selectedId: String?,
    onSelect: (String) -> Unit
) {
    var expanded by remember { mutableStateOf(false) }
    ExposedDropdownMenuBox(expanded = expanded, onExpandedChange = { expanded = !expanded }) {
        OutlinedTextField(
            value = degrees.firstOrNull { it.id == selectedId }?.let { "${it.code} — ${it.name}" } ?: "",
            onValueChange = {},
            readOnly = true,
            label = { Text("Degree") },
            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
            modifier = Modifier.fillMaxWidth().menuAnchor()
        )
        ExposedDropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
            degrees.forEach { d ->
                DropdownMenuItem(text = { Text("${d.code} — ${d.name}") }, onClick = { onSelect(d.id); expanded = false })
            }
        }
    }
}

@Composable
private fun CourseDialog(
    title: String,
    existing: AdminCourse?,
    isSaving: Boolean,
    onDismiss: () -> Unit,
    onSubmit: (code: String, name: String, credits: Int, semester: Int) -> Unit
) {
    var code by remember { mutableStateOf(existing?.code ?: "") }
    var name by remember { mutableStateOf(existing?.name ?: "") }
    var credits by remember { mutableStateOf((existing?.credits ?: 4).toString()) }
    var semester by remember { mutableStateOf((existing?.semester ?: 1).toString()) }
    val cr = credits.toIntOrNull() ?: 0
    val sem = semester.toIntOrNull() ?: 0
    val valid = code.isNotBlank() && name.isNotBlank() && cr > 0 && sem in 1..12

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(title) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedTextField(value = code, onValueChange = { code = it }, label = { Text("Course Code") }, modifier = Modifier.fillMaxWidth(), singleLine = true)
                OutlinedTextField(value = name, onValueChange = { name = it }, label = { Text("Course Name") }, modifier = Modifier.fillMaxWidth(), singleLine = true)
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(
                        value = credits, onValueChange = { credits = it.filter { c -> c.isDigit() } },
                        label = { Text("Credits") }, modifier = Modifier.weight(1f), singleLine = true
                    )
                    OutlinedTextField(
                        value = semester, onValueChange = { semester = it.filter { c -> c.isDigit() } },
                        label = { Text("Semester") }, isError = semester.isNotBlank() && sem !in 1..12,
                        modifier = Modifier.weight(1f), singleLine = true
                    )
                }
            }
        },
        confirmButton = {
            TextButton(enabled = valid && !isSaving, onClick = { onSubmit(code.trim(), name.trim(), cr, sem) }) {
                Text(if (isSaving) "Saving..." else "Save")
            }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text("Cancel") } }
    )
}
