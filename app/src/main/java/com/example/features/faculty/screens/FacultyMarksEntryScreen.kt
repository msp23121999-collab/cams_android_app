package com.example.features.faculty.screens

import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.core.network.FacultyAttendanceSectionDto
import com.example.core.theme.*
import com.example.core.ui.CamsCard
import com.example.core.ui.NetworkErrorView
import com.example.features.faculty.providers.FacultyMarksEntryViewModel
import com.example.features.faculty.providers.MarksEntryRow
import com.example.features.faculty.widgets.FacultyBaseScreen
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FacultyMarksEntryScreen(
    viewModel: FacultyMarksEntryViewModel,
    onNavigate: (String) -> Unit
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()

    LaunchedEffect(state.saveMessage) {
        state.saveMessage?.let {
            scope.launch { snackbarHostState.showSnackbar(it) }
            viewModel.clearSaveStatus()
        }
    }

    Box(modifier = Modifier.fillMaxSize()) {
        FacultyBaseScreen(
            scrollable = false,
            title = "Marks Entry",
            subtitle = "Enter and submit internal assessment marks",
            currentRoute = "/faculty/marks-entry",
            onNavigate = onNavigate
        ) {
            if (state.isLoading) {
                Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator(color = CamsNavy)
                }
            } else if (state.error != null && state.sections.isEmpty()) {
                NetworkErrorView(message = state.error ?: "Failed to load classes", onRetry = { viewModel.loadSections() })
            } else if (state.sections.isEmpty()) {
                Box(Modifier.fillMaxWidth().height(200.dp), contentAlignment = Alignment.Center) {
                    Text("No classes assigned to you in the timetable yet.", color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            } else {
                CamsCard {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text("Assessment Configuration", fontWeight = FontWeight.Bold, fontSize = 16.sp, color = MaterialTheme.colorScheme.onSurface)
                        Spacer(modifier = Modifier.height(16.dp))

                        SectionDropdown(sections = state.sections, selected = state.selectedSection, onSelect = { viewModel.selectSection(it) })

                        Spacer(modifier = Modifier.height(12.dp))

                        OutlinedTextField(
                            value = state.academicYear,
                            onValueChange = { viewModel.setAcademicYear(it) },
                            label = { Text("Academic Year") },
                            modifier = Modifier.fillMaxWidth(),
                            singleLine = true,
                            trailingIcon = { IconButton(onClick = { viewModel.loadMarks() }) { Icon(Icons.Filled.Refresh, contentDescription = "Reload") } }
                        )
                    }
                }

                Spacer(modifier = Modifier.height(20.dp))

                if (state.isLoadingMarks) {
                    Box(Modifier.fillMaxWidth().height(200.dp), contentAlignment = Alignment.Center) {
                        CircularProgressIndicator(color = CamsNavy)
                    }
                } else if (state.marksError != null) {
                    NetworkErrorView(message = state.marksError ?: "Failed to load marks", onRetry = { viewModel.loadMarks() })
                } else if (state.rows.isEmpty()) {
                    Box(Modifier.fillMaxWidth().height(150.dp), contentAlignment = Alignment.Center) {
                        Text("No students found for this class.", color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                } else {
                    Text(
                        "Exam / Assignment / Presentation / Viva / Attendance",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 8.dp)
                    )

                    LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.weight(1f)) {
                        items(state.rows, key = { it.studentId }) { row ->
                            StudentMarksRow(row = row, onMarkChange = { field, value -> viewModel.updateMark(row.studentId, field, value) })
                        }
                    }

                    if (state.saveError != null) {
                        Text(state.saveError ?: "", color = Color(0xFFEF4444), fontSize = 13.sp, modifier = Modifier.padding(vertical = 4.dp))
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                        OutlinedButton(
                            onClick = { viewModel.saveDraft() },
                            enabled = !state.isSaving,
                            modifier = Modifier.weight(1f).height(50.dp),
                            shape = RoundedCornerShape(12.dp),
                            border = androidx.compose.foundation.BorderStroke(1.dp, CamsNavy)
                        ) {
                            Text("Save Draft", color = CamsNavy)
                        }

                        Button(
                            onClick = { viewModel.submitToHod() },
                            enabled = !state.isSaving,
                            modifier = Modifier.weight(1f).height(50.dp),
                            shape = RoundedCornerShape(12.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = CamsNavy)
                        ) {
                            if (state.isSaving) CircularProgressIndicator(modifier = Modifier.size(18.dp), color = Color.White, strokeWidth = 2.dp)
                            else Text("Submit to HOD", fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        }

        SnackbarHost(hostState = snackbarHostState, modifier = Modifier.align(Alignment.BottomCenter))
    }
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
fun StudentMarksRow(row: MarksEntryRow, onMarkChange: (String, String) -> Unit) {
    Surface(color = Color.White, shape = RoundedCornerShape(12.dp), shadowElevation = 1.dp) {
        Column(modifier = Modifier.fillMaxWidth().padding(12.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.SpaceBetween, modifier = Modifier.fillMaxWidth()) {
                Column {
                    Text(row.studentName, fontWeight = FontWeight.Bold, fontSize = 14.sp, color = MaterialTheme.colorScheme.onSurface)
                    Text(row.registrationNumber, fontSize = 13.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
                Text("Total: ${row.total}", fontSize = 13.sp, fontWeight = FontWeight.Bold, color = CamsNavy)
            }
            Spacer(Modifier.height(8.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                MarkField("Exam", row.internalExamMark, Modifier.weight(1f)) { onMarkChange("exam", it) }
                MarkField("Assign.", row.assignmentMark, Modifier.weight(1f)) { onMarkChange("assignment", it) }
                MarkField("Present.", row.presentationMark, Modifier.weight(1f)) { onMarkChange("presentation", it) }
                MarkField("Viva", row.vivaVoiceMark, Modifier.weight(1f)) { onMarkChange("viva", it) }
                MarkField("Attend.", row.attendanceMark, Modifier.weight(1f)) { onMarkChange("attendance", it) }
            }
        }
    }
}

@Composable
private fun MarkField(label: String, value: String, modifier: Modifier, onChange: (String) -> Unit) {
    Column(modifier = modifier) {
        Text(label, fontSize = 10.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
        OutlinedTextField(
            value = value,
            onValueChange = { if (it.length <= 5) onChange(it) },
            modifier = Modifier.fillMaxWidth(),
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
            singleLine = true,
            textStyle = LocalTextStyle.current.copy(textAlign = androidx.compose.ui.text.style.TextAlign.Center, fontSize = 13.sp),
            colors = OutlinedTextFieldDefaults.colors(unfocusedBorderColor = MaterialTheme.colorScheme.outlineVariant, focusedBorderColor = CamsNavy),
            shape = RoundedCornerShape(8.dp)
        )
    }
}
