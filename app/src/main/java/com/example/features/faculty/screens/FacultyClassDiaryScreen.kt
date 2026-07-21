package com.example.features.faculty.screens

import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
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

import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.core.network.ClassDiaryDto
import com.example.core.network.ClassDiaryRequest
import com.example.core.network.FacultyAttendanceSectionDto
import com.example.core.repository.FacultyRepositoryImpl
import com.example.features.faculty.providers.FacultyClassDiaryViewModel
import com.example.features.faculty.providers.FacultyClassDiaryViewModelFactory

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FacultyClassDiaryScreen(onNavigate: (String) -> Unit) {
    val context = LocalContext.current
    val repository = remember { FacultyRepositoryImpl(com.example.CamsApplication.instance.container.apiService) }
    val factory = remember { FacultyClassDiaryViewModelFactory(repository) }
    val viewModel: FacultyClassDiaryViewModel = viewModel(factory = factory)
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    var selectedSection by remember { mutableStateOf<FacultyAttendanceSectionDto?>(null) }
    var sectionMenuExpanded by remember { mutableStateOf(false) }
    var date by remember { mutableStateOf("") }
    var hour by remember { mutableStateOf("") }
    var unit by remember { mutableStateOf("") }
    var topic by remember { mutableStateOf("") }
    var subtopic by remember { mutableStateOf("") }
    var teachingMethod by remember { mutableStateOf("") }
    var learningOutcome by remember { mutableStateOf("") }
    var classActivity by remember { mutableStateOf("") }
    var remarks by remember { mutableStateOf("") }
    var editingEntry by remember { mutableStateOf<ClassDiaryDto?>(null) }

    LaunchedEffect(uiState.sections) {
        if (selectedSection == null) selectedSection = uiState.sections.firstOrNull()
    }

    LaunchedEffect(uiState.saveSuccess, uiState.saveError) {
        if (uiState.saveSuccess) {
            Toast.makeText(context, "Diary entry saved", Toast.LENGTH_SHORT).show()
            date = ""; hour = ""; unit = ""; topic = ""; subtopic = ""
            teachingMethod = ""; learningOutcome = ""; classActivity = ""; remarks = ""
            editingEntry = null
            viewModel.clearSaveStatus()
        }
        uiState.saveError?.let {
            Toast.makeText(context, it, Toast.LENGTH_SHORT).show()
            viewModel.clearSaveStatus()
        }
    }

    fun submit(status: String) {
        val section = selectedSection
        if (section == null) {
            Toast.makeText(context, "Select a section/subject first", Toast.LENGTH_SHORT).show()
            return
        }
        if (date.isBlank() || topic.isBlank()) {
            Toast.makeText(context, "Date and Topic are required", Toast.LENGTH_SHORT).show()
            return
        }
        viewModel.saveEntry(
            ClassDiaryRequest(
                date = date.trim(),
                subject = section.subjectName,
                course = section.courseName,
                semester = section.semester,
                section = section.sectionName,
                hour = hour.trim(),
                unit = unit.trim(),
                topic = topic.trim(),
                subtopic = subtopic.trim(),
                teachingMethod = teachingMethod.trim(),
                learningOutcome = learningOutcome.trim(),
                classActivity = classActivity.trim(),
                remarks = remarks.trim(),
                status = status
            ),
            editingEntry?.id
        )
    }

    FacultyBaseScreen(scrollable = false,
        title = "Class Diary",
        currentRoute = "/faculty/class-diary",
        onNavigate = onNavigate,
        floatingActionButton = {
            FloatingActionButton(
                onClick = { submit("Submitted") },
                containerColor = CamsNavy,
                contentColor = Color.White,
                shape = RoundedCornerShape(16.dp)
            ) {
                Icon(Icons.Filled.Save, "Save Entry")
            }
        }
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Text(if (editingEntry != null) "Edit Draft Entry" else "Add Daily Entry", fontWeight = FontWeight.Bold, fontSize = 18.sp, color = MaterialTheme.colorScheme.onSurface)

            uiState.error?.let {
                Text(it, color = Color(0xFFB91C1C), fontSize = 13.sp)
            }

            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                shape = RoundedCornerShape(16.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    ExposedDropdownMenuBox(expanded = sectionMenuExpanded, onExpandedChange = { sectionMenuExpanded = it }) {
                        OutlinedTextField(
                            value = selectedSection?.let { "${it.subjectName} — ${it.sectionName}" } ?: "Select Class",
                            onValueChange = {},
                            readOnly = true,
                            label = { Text("Subject / Section") },
                            modifier = Modifier.fillMaxWidth().menuAnchor(),
                            shape = RoundedCornerShape(12.dp),
                            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = sectionMenuExpanded) }
                        )
                        ExposedDropdownMenu(expanded = sectionMenuExpanded, onDismissRequest = { sectionMenuExpanded = false }) {
                            uiState.sections.forEach { section ->
                                DropdownMenuItem(
                                    text = { Text("${section.subjectName} — ${section.sectionName}") },
                                    onClick = { selectedSection = section; sectionMenuExpanded = false }
                                )
                            }
                        }
                    }
                    Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                        OutlinedTextField(value = date, onValueChange = { date = it }, label = { Text("Date (YYYY-MM-DD)") }, modifier = Modifier.weight(1f), shape = RoundedCornerShape(12.dp))
                        OutlinedTextField(value = hour, onValueChange = { hour = it }, label = { Text("Hour") }, modifier = Modifier.weight(1f), shape = RoundedCornerShape(12.dp))
                    }
                    OutlinedTextField(value = unit, onValueChange = { unit = it }, label = { Text("Unit") }, modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(12.dp))
                    OutlinedTextField(value = topic, onValueChange = { topic = it }, label = { Text("Topic Covered") }, modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(12.dp))
                    OutlinedTextField(value = subtopic, onValueChange = { subtopic = it }, label = { Text("Subtopic") }, modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(12.dp))
                    OutlinedTextField(value = teachingMethod, onValueChange = { teachingMethod = it }, label = { Text("Teaching Method") }, modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(12.dp))
                    OutlinedTextField(value = learningOutcome, onValueChange = { learningOutcome = it }, label = { Text("Learning Outcome") }, modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(12.dp))
                    OutlinedTextField(value = classActivity, onValueChange = { classActivity = it }, label = { Text("Class Activity") }, modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(12.dp))
                    OutlinedTextField(
                        value = remarks,
                        onValueChange = { remarks = it },
                        label = { Text("Remarks / Homework") },
                        modifier = Modifier.fillMaxWidth().height(100.dp),
                        shape = RoundedCornerShape(12.dp),
                        maxLines = 4
                    )
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        OutlinedButton(
                            onClick = { submit("Draft") },
                            enabled = !uiState.isSaving,
                            modifier = Modifier.weight(1f),
                            shape = RoundedCornerShape(12.dp)
                        ) { Text("Save Draft") }
                        Button(
                            onClick = { submit("Submitted") },
                            enabled = !uiState.isSaving,
                            modifier = Modifier.weight(1f),
                            colors = ButtonDefaults.buttonColors(containerColor = CamsNavy),
                            shape = RoundedCornerShape(12.dp)
                        ) { Text(if (uiState.isSaving) "Saving..." else "Submit") }
                    }
                }
            }

            Text("Previous Entries", fontWeight = FontWeight.Bold, fontSize = 18.sp, color = MaterialTheme.colorScheme.onSurface)

            if (uiState.isLoading) {
                Box(Modifier.fillMaxWidth().padding(24.dp), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator(color = CamsNavy)
                }
            } else if (uiState.entries.isEmpty()) {
                Text("No diary entries yet", color = MaterialTheme.colorScheme.onSurfaceVariant, fontSize = 13.sp)
            } else {
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    uiState.entries.forEach { entry ->
                        DiaryItem(entry, onEdit = if (entry.status == "Draft") {
                            {
                                editingEntry = entry
                                date = entry.date
                                hour = entry.hour ?: ""
                                unit = entry.unit ?: ""
                                topic = entry.topic ?: ""
                                subtopic = entry.subtopic ?: ""
                                teachingMethod = entry.teachingMethod ?: ""
                                learningOutcome = entry.learningOutcome ?: ""
                                classActivity = entry.classActivity ?: ""
                                remarks = entry.remarks ?: ""
                                selectedSection = uiState.sections.find { it.subjectName == entry.subject && it.sectionName == entry.section } ?: selectedSection
                            }
                        } else null)
                    }
                }
            }
        }
    }
}

@Composable
private fun DiaryItem(entry: ClassDiaryDto, onEdit: (() -> Unit)?) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        shape = RoundedCornerShape(16.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(entry.date, fontSize = 12.sp, color = Color(0xFF64748B), fontWeight = FontWeight.Bold)
                Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    Surface(color = CamsNavy.copy(alpha = 0.1f), shape = RoundedCornerShape(8.dp)) {
                        Text(
                            "${entry.subject} - ${entry.section ?: ""}",
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                            fontSize = 12.sp, fontWeight = FontWeight.Bold, color = CamsNavy
                        )
                    }
                    Surface(
                        color = if (entry.status == "Submitted") Color(0xFF10B981).copy(alpha = 0.1f) else Color(0xFFF59E0B).copy(alpha = 0.1f),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Text(
                            entry.status,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                            fontSize = 12.sp, fontWeight = FontWeight.Bold,
                            color = if (entry.status == "Submitted") Color(0xFF10B981) else Color(0xFFF59E0B)
                        )
                    }
                }
            }
            Spacer(modifier = Modifier.height(8.dp))
            Text(entry.topic ?: "-", fontWeight = FontWeight.Bold, fontSize = 15.sp, color = MaterialTheme.colorScheme.onSurface)
            Text(entry.remarks ?: "", fontSize = 13.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
            entry.hodStatus?.let { hodStatus ->
                if (hodStatus != "Pending") {
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        "HOD: $hodStatus${entry.hodRemarks?.let { if (it.isNotBlank()) " — $it" else "" } ?: ""}",
                        fontSize = 12.sp,
                        color = if (hodStatus == "Approved") Color(0xFF10B981) else Color(0xFFB91C1C)
                    )
                }
            }
            if (onEdit != null) {
                Spacer(modifier = Modifier.height(8.dp))
                TextButton(onClick = onEdit) { Text("Edit Draft") }
            }
        }
    }
}
