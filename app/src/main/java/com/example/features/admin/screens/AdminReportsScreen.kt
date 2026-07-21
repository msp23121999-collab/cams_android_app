package com.example.features.admin.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
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
import com.example.core.navigation.AppRoutes
import com.example.core.repository.AdminRepositoryImpl
import com.example.core.theme.*
import com.example.core.ui.CamsCard
import com.example.features.admin.providers.AdminReportsViewModel
import com.example.features.admin.providers.AdminReportsViewModelFactory
import com.example.features.admin.widgets.AdminBaseScreen

@Composable
fun AdminReportsScreen(
    onNavigate: (String) -> Unit,
    viewModel: AdminReportsViewModel = viewModel(
        factory = AdminReportsViewModelFactory(AdminRepositoryImpl(CamsApplication.instance.container.apiService))
    )
) {
    val context = LocalContext.current
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    var query by remember { mutableStateOf("") }

    LaunchedEffect(query) {
        kotlinx.coroutines.delay(350)
        viewModel.searchStudents(query)
    }

    fun download(path: String, title: String) {
        val token = com.example.core.network.AuthManagerImpl(context).getToken() ?: ""
        val base = com.example.core.config.AppConfig.BASE_URL.trimEnd('/')
        com.example.core.utils.DownloadHelper.downloadPdf(context, "$base$path", title, token)
    }

    AdminBaseScreen(
        title = "Reports & Analytics",
        subtitle = "Generate official PDF reports",
        currentRoute = AppRoutes.ADMIN_REPORTS,
        onNavigate = onNavigate
    ) {
        LazyColumn(verticalArrangement = Arrangement.spacedBy(16.dp), modifier = Modifier.fillMaxSize()) {

            item {
                Text("Student Reports", fontWeight = FontWeight.Bold, fontSize = 16.sp, color = MaterialTheme.colorScheme.onSurface)
            }

            item {
                val selected = uiState.selectedStudent
                CamsCard {
                    Column(Modifier.padding(4.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                        if (selected == null) {
                            OutlinedTextField(
                                value = query,
                                onValueChange = { query = it },
                                label = { Text("Search student by name or roll no") },
                                leadingIcon = { Icon(Icons.Filled.Search, null) },
                                trailingIcon = { if (uiState.isSearching) CircularProgressIndicator(Modifier.size(18.dp), strokeWidth = 2.dp) },
                                modifier = Modifier.fillMaxWidth(),
                                singleLine = true
                            )
                            uiState.students.forEach { s ->
                                Row(
                                    Modifier.fillMaxWidth()
                                        .background(MaterialTheme.colorScheme.surface, RoundedCornerShape(10.dp))
                                        .border(1.dp, MaterialTheme.colorScheme.outlineVariant, RoundedCornerShape(10.dp))
                                        .padding(12.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Column(Modifier.weight(1f)) {
                                        Text(s.studentName, fontWeight = FontWeight.Bold, fontSize = 14.sp, color = MaterialTheme.colorScheme.onSurface)
                                        Text("${s.rollNo} • ${s.department}", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                    }
                                    TextButton(onClick = { viewModel.selectStudent(s) }) { Text("Select") }
                                }
                            }
                        } else {
                            Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                                Column(Modifier.weight(1f)) {
                                    Text(selected.studentName, fontWeight = FontWeight.Bold, fontSize = 15.sp, color = MaterialTheme.colorScheme.onSurface)
                                    Text("${selected.rollNo} • ${selected.department}", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                }
                                TextButton(onClick = { viewModel.selectStudent(null); query = "" }) { Text("Change") }
                            }
                            HorizontalDivider(color = Color(0xFFF3F4F6))
                            ReportRow("Student Profile / Resume", Icons.Filled.Person) {
                                download("/reports/student/${selected.studentId}/pdf", "student_profile_${selected.rollNo}")
                            }
                            ReportRow("Attendance Report", Icons.Filled.EventAvailable) {
                                download("/reports/attendance/pdf?student_id=${selected.studentId}", "attendance_${selected.rollNo}")
                            }
                            ReportRow("Internal Marks Report", Icons.Filled.Grade) {
                                download("/reports/marks/pdf?student_id=${selected.studentId}", "marks_${selected.rollNo}")
                            }
                            ReportRow("Fee Receipt / Statement", Icons.Filled.Receipt) {
                                download("/reports/fees/pdf?student_id=${selected.studentId}", "fees_${selected.rollNo}")
                            }
                        }
                    }
                }
            }

            item {
                Text("Faculty Reports", fontWeight = FontWeight.Bold, fontSize = 16.sp, color = MaterialTheme.colorScheme.onSurface)
            }

            item {
                CamsCard {
                    Column(Modifier.padding(4.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                        if (uiState.faculty.isEmpty()) {
                            Text("No faculty available", color = MaterialTheme.colorScheme.onSurfaceVariant, fontSize = 13.sp)
                        } else {
                            FacultyPicker(
                                faculty = uiState.faculty,
                                selectedId = uiState.selectedFacultyId,
                                onSelect = { viewModel.selectFaculty(it) }
                            )
                            val facId = uiState.selectedFacultyId
                            if (facId != null) {
                                HorizontalDivider(color = Color(0xFFF3F4F6))
                                ReportRow("Faculty Profile / Resume", Icons.Filled.Badge) {
                                    download("/reports/faculty/$facId/pdf", "faculty_profile")
                                }
                                SalarySlipRow { year, month ->
                                    download("/reports/salary/pdf?faculty_id=$facId&year=$year&month=$month", "salary_slip_${year}_$month")
                                }
                            }
                        }
                    }
                }
            }

            uiState.error?.let { err ->
                item { Text(err, color = Color(0xFFB91C1C), fontSize = 13.sp) }
            }
        }
    }
}

@Composable
private fun ReportRow(label: String, icon: androidx.compose.ui.graphics.vector.ImageVector, onDownload: () -> Unit) {
    Row(
        Modifier.fillMaxWidth().padding(vertical = 4.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Icon(icon, null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(20.dp))
        Text(label, fontSize = 14.sp, color = MaterialTheme.colorScheme.onSurface, modifier = Modifier.weight(1f))
        Button(onClick = onDownload, colors = ButtonDefaults.buttonColors(containerColor = CamsNavy), shape = RoundedCornerShape(8.dp)) {
            Icon(Icons.Filled.FileDownload, null, modifier = Modifier.size(15.dp))
            Spacer(Modifier.width(4.dp))
            Text("PDF", fontSize = 12.sp)
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun FacultyPicker(
    faculty: List<com.example.core.network.FacultyWorkloadInfoDto>,
    selectedId: String?,
    onSelect: (String) -> Unit
) {
    var expanded by remember { mutableStateOf(false) }
    ExposedDropdownMenuBox(expanded = expanded, onExpandedChange = { expanded = !expanded }) {
        OutlinedTextField(
            value = faculty.firstOrNull { it.id == selectedId }?.fullName ?: "",
            onValueChange = {},
            readOnly = true,
            label = { Text("Select faculty") },
            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
            modifier = Modifier.fillMaxWidth().menuAnchor()
        )
        ExposedDropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
            faculty.forEach { f ->
                DropdownMenuItem(text = { Text(f.fullName) }, onClick = { onSelect(f.id); expanded = false })
            }
        }
    }
}

@Composable
private fun SalarySlipRow(onDownload: (year: Int, month: Int) -> Unit) {
    var year by remember { mutableStateOf("2026") }
    var month by remember { mutableStateOf("6") }
    val y = year.toIntOrNull() ?: 0
    val m = month.toIntOrNull() ?: 0
    val valid = y in 2000..2100 && m in 1..12

    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            Icon(Icons.Filled.Payments, null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(20.dp))
            Text("Salary Slip", fontSize = 14.sp, color = MaterialTheme.colorScheme.onSurface, modifier = Modifier.weight(1f))
        }
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalAlignment = Alignment.CenterVertically) {
            OutlinedTextField(
                value = year, onValueChange = { year = it.filter { c -> c.isDigit() } },
                label = { Text("Year") }, singleLine = true, modifier = Modifier.weight(1f)
            )
            OutlinedTextField(
                value = month, onValueChange = { month = it.filter { c -> c.isDigit() } },
                label = { Text("Month") }, singleLine = true, isError = month.isNotBlank() && m !in 1..12,
                modifier = Modifier.weight(1f)
            )
            Button(
                onClick = { onDownload(y, m) },
                enabled = valid,
                colors = ButtonDefaults.buttonColors(containerColor = CamsNavy),
                shape = RoundedCornerShape(8.dp)
            ) {
                Icon(Icons.Filled.FileDownload, null, modifier = Modifier.size(15.dp))
                Spacer(Modifier.width(4.dp))
                Text("PDF", fontSize = 12.sp)
            }
        }
    }
}
