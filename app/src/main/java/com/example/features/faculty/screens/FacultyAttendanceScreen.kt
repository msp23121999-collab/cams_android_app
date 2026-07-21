package com.example.features.faculty.screens

import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.core.network.FacultyAttendanceSectionDto
import com.example.core.theme.*
import com.example.core.ui.CamsCard
import com.example.core.ui.NetworkErrorView
import com.example.features.faculty.providers.AttendanceRow
import com.example.features.faculty.providers.FacultyAttendanceViewModel
import com.example.features.faculty.widgets.FacultyBaseScreen
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FacultyAttendanceScreen(
    onNavigate: (String) -> Unit,
    viewModel: FacultyAttendanceViewModel
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()

    LaunchedEffect(uiState.saveSuccess) {
        if (uiState.saveSuccess) {
            scope.launch {
                snackbarHostState.showSnackbar("Attendance submitted successfully")
                viewModel.clearSaveStatus()
            }
        }
    }

    FacultyBaseScreen(
        scrollable = false,
        title = "Attendance Entry",
        subtitle = "Mark daily student attendance",
        currentRoute = "/faculty/attendance",
        onNavigate = onNavigate
    ) {
        if (uiState.isLoading) {
            Box(modifier = Modifier.fillMaxWidth().height(300.dp), contentAlignment = Alignment.Center) {
                CircularProgressIndicator(color = CamsNavy)
            }
        } else if (uiState.error != null && uiState.sections.isEmpty()) {
            NetworkErrorView(message = uiState.error ?: "Failed to load classes", onRetry = { viewModel.loadSections() })
        } else if (uiState.sections.isEmpty()) {
            Box(modifier = Modifier.fillMaxWidth().height(200.dp), contentAlignment = Alignment.Center) {
                Text("No classes assigned to you in the timetable yet.", color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
        } else {
            // 1. Class Selection
            CamsCard {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text("Class Selection", fontWeight = FontWeight.Bold, fontSize = 16.sp, color = MaterialTheme.colorScheme.onSurface)
                    Spacer(modifier = Modifier.height(16.dp))

                    SectionDropdown(
                        sections = uiState.sections,
                        selected = uiState.selectedSection,
                        onSelect = { viewModel.selectSection(it) }
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        OutlinedTextField(
                            value = uiState.date,
                            onValueChange = { viewModel.setDate(it) },
                            label = { Text("Date (YYYY-MM-DD)") },
                            modifier = Modifier.weight(1f),
                            singleLine = true
                        )
                        HourDropdown(selected = uiState.hour, onSelect = { viewModel.setHour(it) }, modifier = Modifier.weight(1f))
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // 2. Statistics Summary
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                StatItem("Total", "${uiState.students.size}", CamsNavy, modifier = Modifier.weight(1f))
                StatItem("Present", "${uiState.students.count { it.status == "P" }}", Color(0xFF10B981), modifier = Modifier.weight(1f))
                StatItem("Absent", "${uiState.students.count { it.status == "A" }}", Color(0xFFEF4444), modifier = Modifier.weight(1f))
            }

            Spacer(modifier = Modifier.height(16.dp))

            Text(
                "Student List",
                fontWeight = FontWeight.Bold,
                fontSize = 18.sp,
                color = MaterialTheme.colorScheme.onSurface,
                modifier = Modifier.padding(horizontal = 4.dp, vertical = 8.dp)
            )

            if (uiState.isLoadingStudents) {
                Box(modifier = Modifier.fillMaxWidth().height(200.dp), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator(color = CamsNavy)
                }
            } else if (uiState.studentsError != null) {
                NetworkErrorView(message = uiState.studentsError ?: "Failed to load students", onRetry = { viewModel.loadStudents() })
            } else if (uiState.students.isEmpty()) {
                Box(modifier = Modifier.fillMaxWidth().height(150.dp), contentAlignment = Alignment.Center) {
                    Text("No students found for this class.", color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            } else {
                LazyColumn(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                    contentPadding = PaddingValues(bottom = 8.dp)
                ) {
                    items(uiState.students, key = { it.regNo }) { student ->
                        StudentAttendanceCard(
                            student = student,
                            onStatusChange = { newStatus -> viewModel.setStatus(student.regNo, newStatus) }
                        )
                    }
                }

                if (uiState.saveError != null) {
                    Text(uiState.saveError ?: "", color = Color(0xFFEF4444), fontSize = 13.sp, modifier = Modifier.padding(vertical = 4.dp))
                }

                Button(
                    onClick = { viewModel.submitAttendance() },
                    enabled = !uiState.isSaving,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 8.dp)
                        .height(56.dp),
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = CamsNavy)
                ) {
                    if (uiState.isSaving) {
                        CircularProgressIndicator(modifier = Modifier.size(20.dp), color = Color.White, strokeWidth = 2.dp)
                    } else {
                        Text("Submit Attendance", fontSize = 16.sp, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }

    SnackbarHost(hostState = snackbarHostState)
}

@Composable
private fun SectionDropdown(
    sections: List<FacultyAttendanceSectionDto>,
    selected: FacultyAttendanceSectionDto?,
    onSelect: (FacultyAttendanceSectionDto) -> Unit
) {
    var expanded by remember { mutableStateOf(false) }
    Column(modifier = Modifier.fillMaxWidth()) {
        Text("Class / Subject", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.padding(start = 4.dp, bottom = 4.dp))
        Box {
            Surface(
                onClick = { expanded = true },
                modifier = Modifier.fillMaxWidth(),
                color = MaterialTheme.colorScheme.surfaceVariant,
                shape = RoundedCornerShape(8.dp)
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 10.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        selected?.let { "${it.subjectName} – Section ${it.sectionName}" } ?: "Select Class",
                        fontSize = 14.sp,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Icon(Icons.Filled.ArrowDropDown, null, tint = MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.size(20.dp))
                }
            }
            DropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
                sections.forEach { section ->
                    DropdownMenuItem(
                        text = { Text("${section.subjectName} (${section.subjectCode}) – Section ${section.sectionName}") },
                        onClick = { onSelect(section); expanded = false }
                    )
                }
            }
        }
    }
}

@Composable
fun FilterDropdown(label: String, value: String, modifier: Modifier = Modifier) {
    Column(modifier = modifier) {
        Text(label, fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.padding(start = 4.dp, bottom = 4.dp))
        Surface(
            modifier = Modifier.fillMaxWidth(),
            color = MaterialTheme.colorScheme.surfaceVariant,
            shape = RoundedCornerShape(8.dp)
        ) {
            Row(
                modifier = Modifier.padding(horizontal = 12.dp, vertical = 10.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(value, fontSize = 14.sp, color = MaterialTheme.colorScheme.onSurface)
                Icon(Icons.Filled.ArrowDropDown, null, tint = MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.size(20.dp))
            }
        }
    }
}

@Composable
private fun HourDropdown(selected: Int, onSelect: (Int) -> Unit, modifier: Modifier = Modifier) {
    var expanded by remember { mutableStateOf(false) }
    Column(modifier = modifier) {
        Text("Hour", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.padding(start = 4.dp, bottom = 4.dp))
        Box {
            Surface(
                onClick = { expanded = true },
                modifier = Modifier.fillMaxWidth(),
                color = MaterialTheme.colorScheme.surfaceVariant,
                shape = RoundedCornerShape(8.dp)
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 10.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text("Hour $selected", fontSize = 14.sp, color = MaterialTheme.colorScheme.onSurface)
                    Icon(Icons.Filled.ArrowDropDown, null, tint = MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.size(20.dp))
                }
            }
            DropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
                (1..6).forEach { hour ->
                    DropdownMenuItem(text = { Text("Hour $hour") }, onClick = { onSelect(hour); expanded = false })
                }
            }
        }
    }
}

@Composable
fun StatItem(label: String, value: String, color: Color, modifier: Modifier = Modifier) {
    Surface(
        modifier = modifier,
        color = Color.White,
        shape = RoundedCornerShape(12.dp),
        shadowElevation = 2.dp
    ) {
        Column(
            modifier = Modifier.padding(12.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(label, fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
            Text(value, fontSize = 20.sp, fontWeight = FontWeight.Bold, color = color)
        }
    }
}

@Composable
fun StudentAttendanceCard(
    student: AttendanceRow,
    onStatusChange: (String) -> Unit
) {
    CamsCard {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(student.name, fontWeight = FontWeight.Bold, fontSize = 16.sp, color = MaterialTheme.colorScheme.onSurface)
                Text("${student.regNo} • ${student.overallAttendance}% overall", fontSize = 13.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }

            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                StatusToggle("P", student.status == "P", Color(0xFF10B981)) { onStatusChange("P") }
                StatusToggle("A", student.status == "A", Color(0xFFEF4444)) { onStatusChange("A") }
                StatusToggle("OD", student.status == "OD", Color(0xFF3B82F6)) { onStatusChange("OD") }
            }
        }
    }
}

@Composable
fun StatusToggle(
    label: String,
    isSelected: Boolean,
    color: Color,
    onClick: () -> Unit
) {
    Surface(
        modifier = Modifier
            .size(40.dp)
            .background(Color.Transparent),
        onClick = onClick,
        shape = CircleShape,
        color = if (isSelected) color else MaterialTheme.colorScheme.surfaceVariant,
        contentColor = if (isSelected) Color.White else CamsTextSecondary
    ) {
        Box(contentAlignment = Alignment.Center) {
            Text(label, fontSize = 12.sp, fontWeight = FontWeight.Bold)
        }
    }
}
