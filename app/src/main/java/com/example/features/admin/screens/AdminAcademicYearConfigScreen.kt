package com.example.features.admin.screens

import android.widget.Toast
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
import com.example.core.network.AdminAcademicYearDto
import com.example.core.repository.AdminRepositoryImpl
import com.example.core.theme.*
import com.example.core.ui.CamsCard
import com.example.features.admin.providers.AdminAcademicYearViewModel
import com.example.features.admin.providers.AdminAcademicYearViewModelFactory
import com.example.features.admin.widgets.AdminBaseScreen

@Composable
fun AdminAcademicYearConfigScreen(
    onNavigate: (String) -> Unit,
    viewModel: AdminAcademicYearViewModel = viewModel(
        factory = AdminAcademicYearViewModelFactory(AdminRepositoryImpl(CamsApplication.instance.container.apiService))
    )
) {
    val context = LocalContext.current
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    var showCreateDialog by remember { mutableStateOf(false) }
    var yearPendingDelete by remember { mutableStateOf<AdminAcademicYearDto?>(null) }
    var yearPendingSemester by remember { mutableStateOf<AdminAcademicYearDto?>(null) }

    LaunchedEffect(uiState.saveSuccess, uiState.saveError) {
        if (uiState.saveSuccess) {
            Toast.makeText(context, "Saved", Toast.LENGTH_SHORT).show()
            showCreateDialog = false
            viewModel.clearSaveStatus()
        }
        uiState.saveError?.let {
            Toast.makeText(context, it, Toast.LENGTH_LONG).show()
            viewModel.clearSaveStatus()
        }
    }

    AdminBaseScreen(
        title = "Academic Year Config",
        subtitle = "Manage cohorts, active years and current semester",
        currentRoute = "admin-academic-year-config",
        onNavigate = onNavigate
    ) {
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
            Text("Academic Years", fontWeight = FontWeight.Bold, fontSize = 18.sp, color = MaterialTheme.colorScheme.onSurface)
            Button(
                onClick = { showCreateDialog = true },
                enabled = uiState.degrees.isNotEmpty(),
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF4F46E5)),
                shape = RoundedCornerShape(8.dp)
            ) {
                Icon(Icons.Filled.Add, null, modifier = Modifier.size(16.dp))
                Spacer(Modifier.width(4.dp))
                Text("New Year", fontSize = 12.sp, fontWeight = FontWeight.Bold)
            }
        }

        Spacer(Modifier.height(16.dp))

        uiState.error?.let {
            Text(it, color = Color(0xFFB91C1C), fontSize = 13.sp, modifier = Modifier.padding(bottom = 8.dp))
        }

        CamsCard(modifier = Modifier.fillMaxWidth().weight(1f)) {
            if (uiState.isLoading) {
                Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) { CircularProgressIndicator() }
            } else if (uiState.years.isEmpty()) {
                Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Text("No academic years configured", color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            } else {
                LazyColumn(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    items(uiState.years, key = { it.id }) { year ->
                        Column(
                            modifier = Modifier.fillMaxWidth()
                                .background(MaterialTheme.colorScheme.surface, RoundedCornerShape(12.dp))
                                .border(1.dp, if (year.isActive) Color(0xFF4F46E5) else MaterialTheme.colorScheme.outlineVariant, RoundedCornerShape(12.dp))
                                .padding(14.dp)
                        ) {
                            Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                                Column(Modifier.weight(1f)) {
                                    Text(year.name, fontWeight = FontWeight.Bold, fontSize = 15.sp, color = MaterialTheme.colorScheme.onSurface)
                                    Text(
                                        "${year.degreeName ?: year.degreeCode ?: "Degree"} • Batch ${year.batch}",
                                        fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                    Text("${year.startDate} → ${year.endDate}", fontSize = 11.sp, color = Color(0xFF64748B))
                                }
                                if (year.isActive) {
                                    Text(
                                        "ACTIVE", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = Color(0xFF4338CA),
                                        modifier = Modifier.background(Color(0xFFEEF2FF), RoundedCornerShape(4.dp)).padding(horizontal = 6.dp, vertical = 2.dp)
                                    )
                                }
                            }
                            Spacer(Modifier.height(10.dp))
                            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                Text("Semester ${year.currentSemester}", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = CamsNavy)
                                TextButton(onClick = { yearPendingSemester = year }, enabled = !uiState.isSaving) { Text("Change", fontSize = 12.sp) }
                                Spacer(Modifier.weight(1f))
                                Switch(
                                    checked = year.isActive,
                                    onCheckedChange = { viewModel.setActive(year, it) },
                                    colors = SwitchDefaults.colors(checkedThumbColor = Color.White, checkedTrackColor = CamsNavy)
                                )
                                IconButton(onClick = { yearPendingDelete = year }) {
                                    Icon(Icons.Filled.Delete, "Delete academic year", tint = Color(0xFFB91C1C), modifier = Modifier.size(18.dp))
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    if (showCreateDialog) {
        CreateAcademicYearDialog(
            degrees = uiState.degrees,
            isSaving = uiState.isSaving,
            onDismiss = { showCreateDialog = false },
            onSubmit = { name, degreeId, batch, start, end, sem ->
                viewModel.createYear(name, degreeId, batch, start, end, sem)
            }
        )
    }

    yearPendingSemester?.let { year ->
        ChangeSemesterDialog(
            year = year,
            onDismiss = { yearPendingSemester = null },
            onConfirm = { sem ->
                viewModel.setSemester(year.id, sem)
                yearPendingSemester = null
            }
        )
    }

    yearPendingDelete?.let { year ->
        AlertDialog(
            onDismissRequest = { yearPendingDelete = null },
            title = { Text("Delete Academic Year") },
            text = { Text("Delete \"${year.name}\" (Batch ${year.batch})? This cannot be undone.") },
            confirmButton = {
                TextButton(onClick = { viewModel.deleteYear(year.id); yearPendingDelete = null }) {
                    Text("Delete", color = Color(0xFFB91C1C))
                }
            },
            dismissButton = { TextButton(onClick = { yearPendingDelete = null }) { Text("Cancel") } }
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun CreateAcademicYearDialog(
    degrees: List<com.example.features.admin.models.AdminDegree>,
    isSaving: Boolean,
    onDismiss: () -> Unit,
    onSubmit: (name: String, degreeId: String, batch: String, start: String, end: String, semester: Int) -> Unit
) {
    var name by remember { mutableStateOf("") }
    var batch by remember { mutableStateOf("") }
    var startDate by remember { mutableStateOf("") }
    var endDate by remember { mutableStateOf("") }
    var semesterText by remember { mutableStateOf("1") }
    var degreeId by remember { mutableStateOf(degrees.firstOrNull()?.id ?: "") }
    var expanded by remember { mutableStateOf(false) }

    val semester = semesterText.toIntOrNull() ?: 0
    val valid = name.isNotBlank() && degreeId.isNotBlank() && batch.isNotBlank() &&
        startDate.isNotBlank() && endDate.isNotBlank() && semester in 1..12

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("New Academic Year") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedTextField(value = name, onValueChange = { name = it }, label = { Text("Name (e.g. 2026-2027)") }, modifier = Modifier.fillMaxWidth(), singleLine = true)
                ExposedDropdownMenuBox(expanded = expanded, onExpandedChange = { expanded = !expanded }) {
                    OutlinedTextField(
                        value = degrees.firstOrNull { it.id == degreeId }?.let { "${it.code} — ${it.name}" } ?: "",
                        onValueChange = {},
                        readOnly = true,
                        label = { Text("Degree") },
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
                        modifier = Modifier.fillMaxWidth().menuAnchor()
                    )
                    ExposedDropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
                        degrees.forEach { d ->
                            DropdownMenuItem(text = { Text("${d.code} — ${d.name}") }, onClick = { degreeId = d.id; expanded = false })
                        }
                    }
                }
                OutlinedTextField(value = batch, onValueChange = { batch = it }, label = { Text("Batch (e.g. 2026-2031)") }, modifier = Modifier.fillMaxWidth(), singleLine = true)
                OutlinedTextField(value = startDate, onValueChange = { startDate = it }, label = { Text("Start Date (YYYY-MM-DD)") }, modifier = Modifier.fillMaxWidth(), singleLine = true)
                OutlinedTextField(value = endDate, onValueChange = { endDate = it }, label = { Text("End Date (YYYY-MM-DD)") }, modifier = Modifier.fillMaxWidth(), singleLine = true)
                OutlinedTextField(
                    value = semesterText,
                    onValueChange = { semesterText = it.filter { c -> c.isDigit() } },
                    label = { Text("Current Semester (1-12)") },
                    isError = semesterText.isNotBlank() && semester !in 1..12,
                    modifier = Modifier.fillMaxWidth(), singleLine = true
                )
            }
        },
        confirmButton = {
            TextButton(enabled = valid && !isSaving, onClick = { onSubmit(name.trim(), degreeId, batch.trim(), startDate.trim(), endDate.trim(), semester) }) {
                Text(if (isSaving) "Creating..." else "Create")
            }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text("Cancel") } }
    )
}

@Composable
private fun ChangeSemesterDialog(
    year: AdminAcademicYearDto,
    onDismiss: () -> Unit,
    onConfirm: (Int) -> Unit
) {
    var semesterText by remember { mutableStateOf(year.currentSemester.toString()) }
    val semester = semesterText.toIntOrNull() ?: 0
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Change Current Semester") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text(year.name, fontWeight = FontWeight.Bold)
                OutlinedTextField(
                    value = semesterText,
                    onValueChange = { semesterText = it.filter { c -> c.isDigit() } },
                    label = { Text("Semester (1-12)") },
                    isError = semesterText.isNotBlank() && semester !in 1..12,
                    modifier = Modifier.fillMaxWidth(), singleLine = true
                )
            }
        },
        confirmButton = { TextButton(enabled = semester in 1..12, onClick = { onConfirm(semester) }) { Text("Save") } },
        dismissButton = { TextButton(onClick = onDismiss) { Text("Cancel") } }
    )
}
