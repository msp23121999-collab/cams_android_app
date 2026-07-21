package com.example.features.admin.screens

import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.*
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.*
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

import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.features.admin.providers.AdminAcademicCatalogViewModel
import com.example.core.repository.AdminRepositoryImpl
import com.example.core.network.ApiClient

@Composable
fun AdminAcademicCatalogScreen(
    onNavigate: (String) -> Unit,
    viewModel: AdminAcademicCatalogViewModel = viewModel(
        factory = object : androidx.lifecycle.ViewModelProvider.Factory {
            override fun <T : androidx.lifecycle.ViewModel> create(modelClass: Class<T>): T {
                return AdminAcademicCatalogViewModel(AdminRepositoryImpl(com.example.CamsApplication.instance.container.apiService)) as T
            }
        }
    )
) {
    val context = androidx.compose.ui.platform.LocalContext.current
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    var selectedTabIndex by remember { mutableStateOf(0) }
    var showCreateDialog by remember { mutableStateOf(false) }
    var pendingDelete by remember { mutableStateOf<Triple<String, String, String>?>(null) } // type, id, label
    val tabs = listOf("Departments", "Degree Setup", "Course Setup")

    LaunchedEffect(uiState.saveSuccess, uiState.saveError) {
        if (uiState.saveSuccess) {
            android.widget.Toast.makeText(context, "Saved", android.widget.Toast.LENGTH_SHORT).show()
            showCreateDialog = false
            viewModel.clearSaveStatus()
        }
        uiState.saveError?.let {
            android.widget.Toast.makeText(context, it, android.widget.Toast.LENGTH_LONG).show()
            viewModel.clearSaveStatus()
        }
    }

    AdminBaseScreen(
        title = "Academic Catalog",
        currentRoute = AppRoutes.ADMIN_ACADEMIC_CATALOG,
        onNavigate = onNavigate,
        floatingActionButton = {
            FloatingActionButton(onClick = { showCreateDialog = true }, containerColor = CamsNavy, contentColor = Color.White) {
                Icon(Icons.Filled.Add, "Add ${tabs[selectedTabIndex]}")
            }
        }
    ) {
        Column(modifier = Modifier.fillMaxSize()) {
            TabRow(
                selectedTabIndex = selectedTabIndex,
                containerColor = MaterialTheme.colorScheme.surface,
                contentColor = CamsNavy
            ) {
                tabs.forEachIndexed { index, title ->
                    Tab(
                        selected = selectedTabIndex == index,
                        onClick = { selectedTabIndex = index },
                        text = { Text(title, fontWeight = FontWeight.Bold) }
                    )
                }
            }
            
            Box(modifier = Modifier.fillMaxSize().padding(16.dp)) {
                if (uiState.isLoading) {
                    CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
                } else if (uiState.error != null) {
                    // Without this the screen rendered empty on failure — no message,
                    // no retry — indistinguishable from genuinely having no data.
                    com.example.core.ui.NetworkErrorView(
                        message = uiState.error ?: "Failed to load academic catalog",
                        onRetry = { viewModel.loadCatalog() }
                    )
                } else {
                    if (selectedTabIndex == 0) {
                        DepartmentSetupView(uiState.departments) { d -> pendingDelete = Triple("department", d.id, d.name) }
                    } else if (selectedTabIndex == 1) {
                        DegreeSetupView(uiState.degrees) { d -> pendingDelete = Triple("degree", d.id, d.name) }
                    } else {
                        CourseSetupView(uiState.courses) { c -> pendingDelete = Triple("course", c.id, c.name) }
                    }
                }
            }
        }
    }

    if (showCreateDialog) {
        when (selectedTabIndex) {
            0 -> CreateDepartmentDialog(
                isSaving = uiState.isSaving,
                onDismiss = { showCreateDialog = false },
                onSubmit = { n, c, cn, pl, dy, sc -> viewModel.createDepartment(n, c, cn, pl, dy, sc) }
            )
            1 -> CreateDegreeDialog(
                departments = uiState.departments,
                isSaving = uiState.isSaving,
                onDismiss = { showCreateDialog = false },
                onSubmit = { code, name, batch, pl, dy, deptId -> viewModel.createDegree(code, name, batch, pl, dy, deptId) }
            )
            else -> CreateCourseDialog(
                degrees = uiState.degrees,
                departments = uiState.departments,
                isSaving = uiState.isSaving,
                onDismiss = { showCreateDialog = false },
                onSubmit = { code, name, credits, sem, degId, deptId -> viewModel.createCourse(code, name, credits, sem, degId, deptId) }
            )
        }
    }

    pendingDelete?.let { (type, id, label) ->
        AlertDialog(
            onDismissRequest = { pendingDelete = null },
            title = { Text("Delete ${type.replaceFirstChar { it.uppercase() }}") },
            text = { Text("Delete \"$label\"? This cannot be undone.") },
            confirmButton = {
                TextButton(onClick = {
                    when (type) {
                        "department" -> viewModel.deleteDepartment(id)
                        "degree" -> viewModel.deleteDegree(id)
                        else -> viewModel.deleteCourse(id)
                    }
                    pendingDelete = null
                }) { Text("Delete", color = Color(0xFFB91C1C)) }
            },
            dismissButton = { TextButton(onClick = { pendingDelete = null }) { Text("Cancel") } }
        )
    }
}

@Composable
private fun CreateDepartmentDialog(
    isSaving: Boolean,
    onDismiss: () -> Unit,
    onSubmit: (name: String, code: String, courseName: String, programLevel: String, durationYears: Int, semCount: Int) -> Unit
) {
    var name by remember { mutableStateOf("") }
    var code by remember { mutableStateOf("") }
    var courseName by remember { mutableStateOf("") }
    var programLevel by remember { mutableStateOf("UG") }
    var durationYears by remember { mutableStateOf("3") }
    var semCount by remember { mutableStateOf("6") }
    val dy = durationYears.toIntOrNull() ?: 0
    val sc = semCount.toIntOrNull() ?: 0
    val valid = name.isNotBlank() && code.isNotBlank() && dy > 0 && sc > 0

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("New Department") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedTextField(value = name, onValueChange = { name = it }, label = { Text("Department Name") }, modifier = Modifier.fillMaxWidth(), singleLine = true)
                OutlinedTextField(value = code, onValueChange = { code = it }, label = { Text("Code") }, modifier = Modifier.fillMaxWidth(), singleLine = true)
                OutlinedTextField(value = courseName, onValueChange = { courseName = it }, label = { Text("Programme Name") }, modifier = Modifier.fillMaxWidth(), singleLine = true)
                OutlinedTextField(value = programLevel, onValueChange = { programLevel = it }, label = { Text("Level (UG/PG)") }, modifier = Modifier.fillMaxWidth(), singleLine = true)
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(value = durationYears, onValueChange = { durationYears = it.filter { c -> c.isDigit() } }, label = { Text("Years") }, modifier = Modifier.weight(1f), singleLine = true)
                    OutlinedTextField(value = semCount, onValueChange = { semCount = it.filter { c -> c.isDigit() } }, label = { Text("Semesters") }, modifier = Modifier.weight(1f), singleLine = true)
                }
            }
        },
        confirmButton = {
            TextButton(enabled = valid && !isSaving, onClick = { onSubmit(name.trim(), code.trim(), courseName.trim(), programLevel.trim(), dy, sc) }) {
                Text(if (isSaving) "Creating..." else "Create")
            }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text("Cancel") } }
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun CreateDegreeDialog(
    departments: List<com.example.features.admin.models.AdminDepartment>,
    isSaving: Boolean,
    onDismiss: () -> Unit,
    onSubmit: (code: String, name: String, batch: String, programLevel: String, durationYears: Int, deptId: String?) -> Unit
) {
    var code by remember { mutableStateOf("") }
    var name by remember { mutableStateOf("") }
    var batch by remember { mutableStateOf("") }
    var programLevel by remember { mutableStateOf("UG") }
    var durationYears by remember { mutableStateOf("3") }
    var deptId by remember { mutableStateOf(departments.firstOrNull()?.id) }
    var expanded by remember { mutableStateOf(false) }
    val dy = durationYears.toIntOrNull() ?: 0
    val valid = code.isNotBlank() && name.isNotBlank() && batch.isNotBlank() && dy > 0

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("New Degree") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedTextField(value = code, onValueChange = { code = it }, label = { Text("Code (e.g. BA LLB)") }, modifier = Modifier.fillMaxWidth(), singleLine = true)
                OutlinedTextField(value = name, onValueChange = { name = it }, label = { Text("Degree Name") }, modifier = Modifier.fillMaxWidth(), singleLine = true)
                OutlinedTextField(value = batch, onValueChange = { batch = it }, label = { Text("Applicable Batch (e.g. 2026-2031)") }, modifier = Modifier.fillMaxWidth(), singleLine = true)
                OutlinedTextField(value = programLevel, onValueChange = { programLevel = it }, label = { Text("Level (UG/PG)") }, modifier = Modifier.fillMaxWidth(), singleLine = true)
                OutlinedTextField(value = durationYears, onValueChange = { durationYears = it.filter { c -> c.isDigit() } }, label = { Text("Duration (years)") }, modifier = Modifier.fillMaxWidth(), singleLine = true)
                ExposedDropdownMenuBox(expanded = expanded, onExpandedChange = { expanded = !expanded }) {
                    OutlinedTextField(
                        value = departments.firstOrNull { it.id == deptId }?.name ?: "",
                        onValueChange = {}, readOnly = true, label = { Text("Department") },
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
                        modifier = Modifier.fillMaxWidth().menuAnchor()
                    )
                    ExposedDropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
                        departments.forEach { d ->
                            DropdownMenuItem(text = { Text(d.name) }, onClick = { deptId = d.id; expanded = false })
                        }
                    }
                }
            }
        },
        confirmButton = {
            TextButton(enabled = valid && !isSaving, onClick = { onSubmit(code.trim(), name.trim(), batch.trim(), programLevel.trim(), dy, deptId) }) {
                Text(if (isSaving) "Creating..." else "Create")
            }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text("Cancel") } }
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun CreateCourseDialog(
    degrees: List<com.example.features.admin.models.AdminDegree>,
    departments: List<com.example.features.admin.models.AdminDepartment>,
    isSaving: Boolean,
    onDismiss: () -> Unit,
    onSubmit: (code: String, name: String, credits: Int, semester: Int, degreeId: String?, deptId: String?) -> Unit
) {
    var code by remember { mutableStateOf("") }
    var name by remember { mutableStateOf("") }
    var credits by remember { mutableStateOf("4") }
    var semester by remember { mutableStateOf("1") }
    var degreeId by remember { mutableStateOf(degrees.firstOrNull()?.id) }
    var deptId by remember { mutableStateOf(departments.firstOrNull()?.id) }
    var degExpanded by remember { mutableStateOf(false) }
    var deptExpanded by remember { mutableStateOf(false) }
    val cr = credits.toIntOrNull() ?: 0
    val sem = semester.toIntOrNull() ?: 0
    val valid = code.isNotBlank() && name.isNotBlank() && cr > 0 && sem in 1..12

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("New Course") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedTextField(value = code, onValueChange = { code = it }, label = { Text("Course Code") }, modifier = Modifier.fillMaxWidth(), singleLine = true)
                OutlinedTextField(value = name, onValueChange = { name = it }, label = { Text("Course Name") }, modifier = Modifier.fillMaxWidth(), singleLine = true)
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(value = credits, onValueChange = { credits = it.filter { c -> c.isDigit() } }, label = { Text("Credits") }, modifier = Modifier.weight(1f), singleLine = true)
                    OutlinedTextField(value = semester, onValueChange = { semester = it.filter { c -> c.isDigit() } }, label = { Text("Semester") }, isError = semester.isNotBlank() && sem !in 1..12, modifier = Modifier.weight(1f), singleLine = true)
                }
                ExposedDropdownMenuBox(expanded = degExpanded, onExpandedChange = { degExpanded = !degExpanded }) {
                    OutlinedTextField(
                        value = degrees.firstOrNull { it.id == degreeId }?.let { "${it.code} — ${it.name}" } ?: "",
                        onValueChange = {}, readOnly = true, label = { Text("Degree") },
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = degExpanded) },
                        modifier = Modifier.fillMaxWidth().menuAnchor()
                    )
                    ExposedDropdownMenu(expanded = degExpanded, onDismissRequest = { degExpanded = false }) {
                        degrees.forEach { d ->
                            DropdownMenuItem(text = { Text("${d.code} — ${d.name}") }, onClick = { degreeId = d.id; degExpanded = false })
                        }
                    }
                }
                ExposedDropdownMenuBox(expanded = deptExpanded, onExpandedChange = { deptExpanded = !deptExpanded }) {
                    OutlinedTextField(
                        value = departments.firstOrNull { it.id == deptId }?.name ?: "",
                        onValueChange = {}, readOnly = true, label = { Text("Department") },
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = deptExpanded) },
                        modifier = Modifier.fillMaxWidth().menuAnchor()
                    )
                    ExposedDropdownMenu(expanded = deptExpanded, onDismissRequest = { deptExpanded = false }) {
                        departments.forEach { d ->
                            DropdownMenuItem(text = { Text(d.name) }, onClick = { deptId = d.id; deptExpanded = false })
                        }
                    }
                }
            }
        },
        confirmButton = {
            TextButton(enabled = valid && !isSaving, onClick = { onSubmit(code.trim(), name.trim(), cr, sem, degreeId, deptId) }) {
                Text(if (isSaving) "Creating..." else "Create")
            }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text("Cancel") } }
    )
}

@Composable
private fun CatalogRow(title: String, subtitle: String, onDelete: () -> Unit) {
    CamsCard {
        Row(modifier = Modifier.fillMaxWidth().padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
            Column(Modifier.weight(1f)) {
                Text(title, fontWeight = FontWeight.Bold, fontSize = 16.sp, color = MaterialTheme.colorScheme.onSurface)
                Spacer(Modifier.height(4.dp))
                Text(subtitle, fontSize = 13.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
            IconButton(onClick = onDelete) {
                Icon(Icons.Filled.Delete, contentDescription = "Delete $title", tint = Color(0xFFB91C1C), modifier = Modifier.size(18.dp))
            }
        }
    }
}

@Composable
private fun DepartmentSetupView(
    departments: List<com.example.features.admin.models.AdminDepartment>,
    onDelete: (com.example.features.admin.models.AdminDepartment) -> Unit
) {
    if (departments.isEmpty()) {
        Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Text("No departments yet", color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
        return
    }
    LazyColumn(modifier = Modifier.fillMaxSize(), verticalArrangement = Arrangement.spacedBy(12.dp)) {
        items(departments, key = { it.id }) { dept ->
            CatalogRow(dept.name, "Code: ${dept.code}${dept.courseName?.let { " • $it" } ?: ""}") { onDelete(dept) }
        }
    }
}

@Composable
private fun DegreeSetupView(
    degrees: List<com.example.features.admin.models.AdminDegree>,
    onDelete: (com.example.features.admin.models.AdminDegree) -> Unit
) {
    if (degrees.isEmpty()) {
        Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Text("No degrees yet", color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
        return
    }
    LazyColumn(modifier = Modifier.fillMaxSize(), verticalArrangement = Arrangement.spacedBy(12.dp)) {
        items(degrees, key = { it.id }) { degree ->
            CatalogRow(degree.name, "${degree.code} • ${degree.durationYears ?: 0} Years • ${degree.programLevel ?: "N/A"}") { onDelete(degree) }
        }
    }
}

@Composable
private fun CourseSetupView(
    courses: List<com.example.features.admin.models.AdminCourse>,
    onDelete: (com.example.features.admin.models.AdminCourse) -> Unit
) {
    if (courses.isEmpty()) {
        Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Text("No courses yet", color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
        return
    }
    LazyColumn(modifier = Modifier.fillMaxSize(), verticalArrangement = Arrangement.spacedBy(12.dp)) {
        items(courses, key = { it.id }) { course ->
            CatalogRow(course.name, "${course.code} • ${course.credits ?: 0} Credits • Sem ${course.semester}") { onDelete(course) }
        }
    }
}
