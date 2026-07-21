package com.example.features.hod.screens

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
import com.example.core.ui.CamsCard
import com.example.features.hod.widgets.HODBaseScreen
import com.example.core.navigation.AppRoutes

import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.core.network.CourseDto
import com.example.core.network.FacultyDto
import com.example.core.network.SectionDto
import com.example.core.network.TimetableSlotInputDto
import com.example.core.repository.HODRepositoryImpl
import com.example.features.hod.providers.HODTimetableViewModel
import com.example.features.hod.providers.HODTimetableViewModelFactory

private val WEEKDAYS = listOf("MONDAY", "TUESDAY", "WEDNESDAY", "THURSDAY", "FRIDAY", "SATURDAY")

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HODTimetableSetupScreen(onNavigate: (String) -> Unit) {
    val context = LocalContext.current
    val repository = remember { HODRepositoryImpl(com.example.CamsApplication.instance.container.apiService) }
    val factory = remember { HODTimetableViewModelFactory(repository) }
    val viewModel: HODTimetableViewModel = viewModel(factory = factory)
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    var draftSlots by remember { mutableStateOf(listOf<TimetableSlotInputDto>()) }
    var sectionMenuExpanded by remember { mutableStateOf(false) }

    LaunchedEffect(uiState.saveSuccess, uiState.saveError) {
        if (uiState.saveSuccess) {
            Toast.makeText(context, "Timetable saved", Toast.LENGTH_SHORT).show()
            viewModel.clearSaveStatus()
        }
        uiState.saveError?.let {
            Toast.makeText(context, it, Toast.LENGTH_SHORT).show()
            viewModel.clearSaveStatus()
        }
    }

    LaunchedEffect(uiState.timetableSlots) {
        draftSlots = uiState.timetableSlots.map {
            TimetableSlotInputDto(it.subjectId, it.facultyId, it.roomNo.orEmpty(), it.dayOfWeek, it.startTime, it.endTime)
        }
    }

    val sections = uiState.metadata?.sections ?: emptyList()
    val courses = uiState.metadata?.courses ?: emptyList()
    val faculty = uiState.metadata?.faculty ?: emptyList()
    val selectedSection = sections.find { it.id == uiState.selectedSectionId }

    HODBaseScreen(
        title = "Timetable Setup",
        subtitle = "Build the class timetable — subject, faculty, room, and time slots",
        currentRoute = "/hod/timetable-setup",
        onNavigate = onNavigate,
        onBackClick = { onNavigate(AppRoutes.HOD_TIMETABLE_MGMT) }
    ) {
        uiState.error?.let {
            Text(it, color = Color(0xFFB91C1C), fontSize = 13.sp, modifier = Modifier.padding(bottom = 8.dp))
        }

        CamsCard(modifier = Modifier.fillMaxWidth()) {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Text("Select Class", fontWeight = FontWeight.Bold, fontSize = 16.sp, color = MaterialTheme.colorScheme.onSurface)
                ExposedDropdownMenuBox(expanded = sectionMenuExpanded, onExpandedChange = { sectionMenuExpanded = it }) {
                    OutlinedTextField(
                        value = selectedSection?.label ?: "Select Section",
                        onValueChange = {}, readOnly = true,
                        modifier = Modifier.fillMaxWidth().menuAnchor(),
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = sectionMenuExpanded) }
                    )
                    ExposedDropdownMenu(expanded = sectionMenuExpanded, onDismissRequest = { sectionMenuExpanded = false }) {
                        sections.forEach { section ->
                            DropdownMenuItem(text = { Text(section.label) }, onClick = {
                                viewModel.selectSection(section.id)
                                sectionMenuExpanded = false
                            })
                        }
                    }
                }
            }
        }

        Spacer(Modifier.height(16.dp))

        CamsCard(modifier = Modifier.fillMaxWidth().weight(1f)) {
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                Text("Time Slots", fontWeight = FontWeight.Bold, fontSize = 16.sp, color = MaterialTheme.colorScheme.onSurface)
                IconButton(
                    onClick = {
                        if (courses.isNotEmpty() && faculty.isNotEmpty()) {
                            draftSlots = draftSlots + TimetableSlotInputDto(
                                subjectId = courses.first().id,
                                facultyId = faculty.first().id,
                                room = "",
                                weekday = "MONDAY",
                                startTime = "09:00",
                                endTime = "10:00"
                            )
                        } else {
                            Toast.makeText(context, "No subjects/faculty available to add a slot", Toast.LENGTH_SHORT).show()
                        }
                    },
                    enabled = selectedSection != null
                ) {
                    Icon(Icons.Filled.Add, contentDescription = "Add Slot", tint = Color(0xFF4F46E5))
                }
            }
            Spacer(Modifier.height(8.dp))

            if (uiState.isLoading) {
                Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator()
                }
            } else if (selectedSection == null) {
                Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Text("Select a class to configure its timetable.", color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            } else if (draftSlots.isEmpty()) {
                Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Text("No slots yet. Tap + to add one.", color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            } else {
                LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    items(draftSlots.size) { index ->
                        SlotEditorRow(
                            slot = draftSlots[index],
                            courses = courses,
                            faculty = faculty,
                            onChange = { updated -> draftSlots = draftSlots.toMutableList().also { it[index] = updated } },
                            onRemove = { draftSlots = draftSlots.toMutableList().also { it.removeAt(index) } }
                        )
                    }
                }
            }
        }

        Spacer(Modifier.height(16.dp))
        Button(
            onClick = { selectedSection?.let { viewModel.submitTimetable(it.id, draftSlots) } },
            enabled = selectedSection != null && !uiState.isSaving,
            modifier = Modifier.fillMaxWidth(),
            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF4F46E5)),
            shape = RoundedCornerShape(12.dp),
            contentPadding = PaddingValues(16.dp)
        ) {
            Text(if (uiState.isSaving) "Saving..." else "Save Timetable", fontSize = 16.sp, fontWeight = FontWeight.Bold)
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun SlotEditorRow(
    slot: TimetableSlotInputDto,
    courses: List<CourseDto>,
    faculty: List<FacultyDto>,
    onChange: (TimetableSlotInputDto) -> Unit,
    onRemove: () -> Unit
) {
    var subjectMenuExpanded by remember { mutableStateOf(false) }
    var facultyMenuExpanded by remember { mutableStateOf(false) }
    var dayMenuExpanded by remember { mutableStateOf(false) }
    val selectedCourse = courses.find { it.id == slot.subjectId }
    val selectedFaculty = faculty.find { it.id == slot.facultyId }

    Column(
        modifier = Modifier.fillMaxWidth().background(MaterialTheme.colorScheme.surface, RoundedCornerShape(8.dp)).border(1.dp, MaterialTheme.colorScheme.outlineVariant, RoundedCornerShape(8.dp)).padding(12.dp)
    ) {
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
            Text("Slot", fontWeight = FontWeight.Bold, fontSize = 13.sp, color = CamsTextPrimary)
            IconButton(onClick = onRemove) {
                Icon(Icons.Filled.Delete, contentDescription = "Remove", tint = Color(0xFFEF4444), modifier = Modifier.size(20.dp))
            }
        }
        ExposedDropdownMenuBox(expanded = subjectMenuExpanded, onExpandedChange = { subjectMenuExpanded = it }) {
            OutlinedTextField(
                value = selectedCourse?.let { "${it.code} - ${it.name}" } ?: "Select Subject",
                onValueChange = {}, readOnly = true,
                modifier = Modifier.fillMaxWidth().menuAnchor(),
                trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = subjectMenuExpanded) }
            )
            ExposedDropdownMenu(expanded = subjectMenuExpanded, onDismissRequest = { subjectMenuExpanded = false }) {
                courses.forEach { c -> DropdownMenuItem(text = { Text("${c.code} - ${c.name}") }, onClick = { onChange(slot.copy(subjectId = c.id)); subjectMenuExpanded = false }) }
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
                faculty.forEach { f -> DropdownMenuItem(text = { Text(f.fullName) }, onClick = { onChange(slot.copy(facultyId = f.id)); facultyMenuExpanded = false }) }
            }
        }
        OutlinedTextField(
            value = slot.room,
            onValueChange = { onChange(slot.copy(room = it)) },
            label = { Text("Room") },
            modifier = Modifier.fillMaxWidth(),
            singleLine = true
        )
        ExposedDropdownMenuBox(expanded = dayMenuExpanded, onExpandedChange = { dayMenuExpanded = it }) {
            OutlinedTextField(
                value = slot.weekday,
                onValueChange = {}, readOnly = true,
                modifier = Modifier.fillMaxWidth().menuAnchor(),
                trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = dayMenuExpanded) }
            )
            ExposedDropdownMenu(expanded = dayMenuExpanded, onDismissRequest = { dayMenuExpanded = false }) {
                WEEKDAYS.forEach { day -> DropdownMenuItem(text = { Text(day) }, onClick = { onChange(slot.copy(weekday = day)); dayMenuExpanded = false }) }
            }
        }
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            OutlinedTextField(
                value = slot.startTime,
                onValueChange = { onChange(slot.copy(startTime = it)) },
                label = { Text("Start (HH:MM)") },
                modifier = Modifier.weight(1f),
                singleLine = true
            )
            OutlinedTextField(
                value = slot.endTime,
                onValueChange = { onChange(slot.copy(endTime = it)) },
                label = { Text("End (HH:MM)") },
                modifier = Modifier.weight(1f),
                singleLine = true
            )
        }
    }
}
