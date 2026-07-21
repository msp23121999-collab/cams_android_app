package com.example.features.faculty.screens

import androidx.lifecycle.compose.collectAsStateWithLifecycle
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
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import androidx.compose.runtime.*
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.core.network.*
import com.example.core.theme.*
import com.example.core.ui.CamsCard
import com.example.core.ui.NetworkErrorView
import com.example.features.faculty.providers.FacultySmartClassroomViewModel
import com.example.features.faculty.widgets.FacultyBaseScreen

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FacultySmartClassroomScreen(
    viewModel: FacultySmartClassroomViewModel,
    onNavigate: (String) -> Unit
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    var selectedTab by remember { mutableStateOf(0) }
    val tabs = listOf("Activities", "Live Polls", "Session Log")
    var showLogActivity by remember { mutableStateOf(false) }
    var showCreatePoll by remember { mutableStateOf(false) }
    var showLogSummary by remember { mutableStateOf(false) }

    LaunchedEffect(state.saveSuccess) {
        if (state.saveSuccess) {
            showLogActivity = false
            showCreatePoll = false
            showLogSummary = false
            viewModel.clearSaveStatus()
        }
    }

    Box(modifier = Modifier.fillMaxSize()) {
        FacultyBaseScreen(
            scrollable = false,
            title = "Smart Classroom",
            subtitle = "Activity logging, live polls & session summaries",
            currentRoute = "/faculty/smart-classroom",
            onNavigate = onNavigate
        ) {
            if (state.isLoading) {
                Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator(color = CamsNavy)
                }
            } else if (state.error != null && state.activities.isEmpty() && state.interactions.isEmpty() && state.summaries.isEmpty()) {
                NetworkErrorView(message = state.error ?: "Failed to load classroom data", onRetry = { viewModel.loadAll() })
            } else if (state.sections.isEmpty()) {
                Box(Modifier.fillMaxWidth().height(200.dp), contentAlignment = Alignment.Center) {
                    Text("No classes assigned to you in the timetable yet.", color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            } else {
                TabRow(
                    selectedTabIndex = selectedTab,
                    containerColor = Color.Transparent,
                    contentColor = CamsNavy,
                    indicator = { tabPositions ->
                        TabRowDefaults.SecondaryIndicator(Modifier.tabIndicatorOffset(tabPositions[selectedTab]), color = CamsNavy)
                    },
                    divider = {}
                ) {
                    tabs.forEachIndexed { index, title ->
                        Tab(
                            selected = selectedTab == index,
                            onClick = { selectedTab = index },
                            text = { Text(title, fontSize = 13.sp, fontWeight = if (selectedTab == index) FontWeight.Bold else FontWeight.Medium) }
                        )
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                when (selectedTab) {
                    0 -> {
                        if (state.activities.isEmpty()) {
                            EmptyMessage("No classroom activities logged yet. Tap + to log one.")
                        } else {
                            LazyColumn(verticalArrangement = Arrangement.spacedBy(12.dp), contentPadding = PaddingValues(bottom = 88.dp)) {
                                items(state.activities, key = { it.id }) { activity -> ActivityCard(activity) }
                            }
                        }
                    }
                    1 -> {
                        if (state.interactions.isEmpty()) {
                            EmptyMessage("No live polls yet. Tap + to create one.")
                        } else {
                            LazyColumn(verticalArrangement = Arrangement.spacedBy(12.dp), contentPadding = PaddingValues(bottom = 88.dp)) {
                                items(state.interactions, key = { it.id }) { interaction -> InteractionCard(interaction) }
                            }
                        }
                    }
                    else -> {
                        if (state.summaries.isEmpty()) {
                            EmptyMessage("No session summaries logged yet. Tap + to add one.")
                        } else {
                            LazyColumn(verticalArrangement = Arrangement.spacedBy(12.dp), contentPadding = PaddingValues(bottom = 88.dp)) {
                                items(state.summaries, key = { it.id }) { summary -> SummaryCard(summary) }
                            }
                        }
                    }
                }
            }
        }

        if (state.sections.isNotEmpty()) {
            FloatingActionButton(
                onClick = {
                    when (selectedTab) {
                        0 -> showLogActivity = true
                        1 -> showCreatePoll = true
                        else -> showLogSummary = true
                    }
                },
                modifier = Modifier.align(Alignment.BottomEnd).padding(24.dp),
                containerColor = CamsNavy,
                contentColor = Color.White,
                shape = RoundedCornerShape(16.dp)
            ) {
                Icon(Icons.Filled.Add, contentDescription = "Add")
            }
        }
    }

    if (showLogActivity) {
        LogActivityDialog(
            sections = state.sections,
            isSaving = state.isSaving,
            saveError = state.saveError,
            onDismiss = { showLogActivity = false; viewModel.clearSaveStatus() },
            onSave = { request -> viewModel.logActivity(request) }
        )
    }
    if (showCreatePoll) {
        CreatePollDialog(
            sections = state.sections,
            isSaving = state.isSaving,
            saveError = state.saveError,
            onDismiss = { showCreatePoll = false; viewModel.clearSaveStatus() },
            onSave = { request -> viewModel.createPoll(request) }
        )
    }
    if (showLogSummary) {
        LogSummaryDialog(
            sections = state.sections,
            isSaving = state.isSaving,
            saveError = state.saveError,
            onDismiss = { showLogSummary = false; viewModel.clearSaveStatus() },
            onSave = { request -> viewModel.saveSessionSummary(request) }
        )
    }
}

@Composable
private fun EmptyMessage(message: String) {
    Box(Modifier.fillMaxWidth().padding(40.dp), contentAlignment = Alignment.Center) {
        Text(message, color = MaterialTheme.colorScheme.onSurfaceVariant, textAlign = androidx.compose.ui.text.style.TextAlign.Center)
    }
}

@Composable
private fun ActivityCard(activity: ClassroomActivityDto) {
    CamsCard {
        Column(Modifier.padding(4.dp)) {
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Text(activity.activityType, fontWeight = FontWeight.Bold, fontSize = 14.sp, color = CamsNavy)
                Text("${activity.durationMinutes} min", fontSize = 13.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
            Spacer(Modifier.height(4.dp))
            Text(activity.topic, fontSize = 14.sp, color = MaterialTheme.colorScheme.onSurface)
            if (!activity.remarks.isNullOrBlank()) {
                Text(activity.remarks, fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.padding(top = 4.dp))
            }
        }
    }
}

@Composable
private fun InteractionCard(interaction: StudentInteractionDto) {
    CamsCard {
        Column(Modifier.padding(4.dp)) {
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                Surface(color = if (interaction.type == "POLL") Color(0xFF3B82F6).copy(alpha = 0.1f) else Color(0xFF10B981).copy(alpha = 0.1f), shape = RoundedCornerShape(6.dp)) {
                    Text(
                        interaction.type,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp),
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = if (interaction.type == "POLL") Color(0xFF3B82F6) else Color(0xFF10B981)
                    )
                }
                Text(if (interaction.isActive) "Active" else "Closed", fontSize = 12.sp, color = if (interaction.isActive) Color(0xFF10B981) else MaterialTheme.colorScheme.onSurfaceVariant)
            }
            Spacer(Modifier.height(6.dp))
            Text(interaction.questionText, fontWeight = FontWeight.Bold, fontSize = 14.sp, color = MaterialTheme.colorScheme.onSurface)
            interaction.options?.let { opts ->
                Spacer(Modifier.height(4.dp))
                opts.forEach { opt -> Text("• $opt", fontSize = 13.sp, color = MaterialTheme.colorScheme.onSurfaceVariant) }
            }
            Spacer(Modifier.height(6.dp))
            Text("${interaction.responsesCount} responses", fontSize = 12.sp, fontWeight = FontWeight.Medium, color = CamsNavy)
        }
    }
}

@Composable
private fun SummaryCard(summary: SessionSummaryDto) {
    CamsCard {
        Column(Modifier.padding(4.dp)) {
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Text(summary.subjectCode, fontWeight = FontWeight.Bold, fontSize = 14.sp, color = CamsNavy)
                Text(summary.date, fontSize = 13.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
            Spacer(Modifier.height(4.dp))
            Text(summary.topicCovered, fontSize = 14.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurface)
            if (!summary.subtopicCovered.isNullOrBlank()) {
                Text(summary.subtopicCovered, fontSize = 13.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
            Text("Method: ${summary.teachingMethod}", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.padding(top = 4.dp))
        }
    }
}

@Composable
private fun SectionPicker(sections: List<FacultyAttendanceSectionDto>, selected: FacultyAttendanceSectionDto?, onSelect: (FacultyAttendanceSectionDto) -> Unit) {
    var expanded by remember { mutableStateOf(false) }
    Box(modifier = Modifier.fillMaxWidth()) {
        OutlinedButton(onClick = { expanded = true }, modifier = Modifier.fillMaxWidth()) {
            Text(selected?.let { "${it.subjectName} – Sec ${it.sectionName}" } ?: "Select Class", modifier = Modifier.weight(1f), maxLines = 1, overflow = TextOverflow.Ellipsis)
            Icon(Icons.Filled.ArrowDropDown, contentDescription = null)
        }
        DropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
            sections.forEach { section ->
                DropdownMenuItem(text = { Text("${section.subjectName} – Sec ${section.sectionName}") }, onClick = { onSelect(section); expanded = false })
            }
        }
    }
}

@Composable
private fun LogActivityDialog(
    sections: List<FacultyAttendanceSectionDto>,
    isSaving: Boolean,
    saveError: String?,
    onDismiss: () -> Unit,
    onSave: (CreateClassroomActivityRequest) -> Unit
) {
    var section by remember { mutableStateOf(sections.firstOrNull()) }
    var activityType by remember { mutableStateOf("Discussion") }
    var topic by remember { mutableStateOf("") }
    var duration by remember { mutableStateOf("") }
    var remarks by remember { mutableStateOf("") }
    var validationError by remember { mutableStateOf<String?>(null) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Log Classroom Activity", fontWeight = FontWeight.Black) },
        text = {
            Column(modifier = Modifier.verticalScroll(rememberScrollState()), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                SectionPicker(sections, section) { section = it }
                Text("Activity Type:", fontWeight = FontWeight.SemiBold, fontSize = 13.sp)
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    listOf("Discussion", "Group Work", "Case Study", "Debate").forEach { t ->
                        FilterChip(selected = activityType == t, onClick = { activityType = t }, label = { Text(t, fontSize = 12.sp) })
                    }
                }
                OutlinedTextField(value = topic, onValueChange = { topic = it }, label = { Text("Topic") }, modifier = Modifier.fillMaxWidth(), singleLine = true)
                OutlinedTextField(
                    value = duration, onValueChange = { duration = it }, label = { Text("Duration (minutes)") }, modifier = Modifier.fillMaxWidth(), singleLine = true,
                    keyboardOptions = androidx.compose.foundation.text.KeyboardOptions(keyboardType = androidx.compose.ui.text.input.KeyboardType.Number)
                )
                OutlinedTextField(value = remarks, onValueChange = { remarks = it }, label = { Text("Remarks (optional)") }, modifier = Modifier.fillMaxWidth(), minLines = 2)
                val displayedError = validationError ?: saveError
                if (displayedError != null) Text(displayedError, color = Color(0xFFEF4444), fontSize = 12.sp)
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    validationError = when {
                        section == null -> "Select a class"
                        topic.isBlank() -> "Topic is required"
                        duration.toIntOrNull() == null || duration.toInt() <= 0 -> "Enter a valid duration"
                        else -> null
                    }
                    if (validationError == null) {
                        onSave(CreateClassroomActivityRequest(section!!.sectionId, activityType, topic, duration.toInt(), remarks.ifBlank { null }))
                    }
                },
                enabled = !isSaving,
                colors = ButtonDefaults.buttonColors(containerColor = CamsNavy)
            ) {
                if (isSaving) CircularProgressIndicator(modifier = Modifier.size(18.dp), color = Color.White, strokeWidth = 2.dp) else Text("Save")
            }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text("Cancel") } }
    )
}

@Composable
private fun CreatePollDialog(
    sections: List<FacultyAttendanceSectionDto>,
    isSaving: Boolean,
    saveError: String?,
    onDismiss: () -> Unit,
    onSave: (CreateInteractionRequest) -> Unit
) {
    var section by remember { mutableStateOf(sections.firstOrNull()) }
    var type by remember { mutableStateOf("POLL") }
    var question by remember { mutableStateOf("") }
    var optionsText by remember { mutableStateOf("") }
    var validationError by remember { mutableStateOf<String?>(null) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Create Poll / Question", fontWeight = FontWeight.Black) },
        text = {
            Column(modifier = Modifier.verticalScroll(rememberScrollState()), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                SectionPicker(sections, section) { section = it }
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    listOf("POLL", "QUESTION").forEach { t ->
                        FilterChip(selected = type == t, onClick = { type = t }, label = { Text(t) })
                    }
                }
                OutlinedTextField(value = question, onValueChange = { question = it }, label = { Text("Question") }, modifier = Modifier.fillMaxWidth(), minLines = 2)
                if (type == "POLL") {
                    OutlinedTextField(
                        value = optionsText, onValueChange = { optionsText = it },
                        label = { Text("Options (comma-separated)") }, modifier = Modifier.fillMaxWidth(), minLines = 2
                    )
                }
                val displayedError = validationError ?: saveError
                if (displayedError != null) Text(displayedError, color = Color(0xFFEF4444), fontSize = 12.sp)
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    val options = optionsText.split(",").map { it.trim() }.filter { it.isNotBlank() }
                    validationError = when {
                        section == null -> "Select a class"
                        question.isBlank() -> "Question is required"
                        type == "POLL" && options.size < 2 -> "Enter at least 2 options for a poll"
                        else -> null
                    }
                    if (validationError == null) {
                        onSave(CreateInteractionRequest(section!!.sectionId, type, question, if (type == "POLL") options else null))
                    }
                },
                enabled = !isSaving,
                colors = ButtonDefaults.buttonColors(containerColor = CamsNavy)
            ) {
                if (isSaving) CircularProgressIndicator(modifier = Modifier.size(18.dp), color = Color.White, strokeWidth = 2.dp) else Text("Create")
            }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text("Cancel") } }
    )
}

@Composable
private fun LogSummaryDialog(
    sections: List<FacultyAttendanceSectionDto>,
    isSaving: Boolean,
    saveError: String?,
    onDismiss: () -> Unit,
    onSave: (CreateSessionSummaryRequest) -> Unit
) {
    var section by remember { mutableStateOf(sections.firstOrNull()) }
    var topicCovered by remember { mutableStateOf("") }
    var subtopicCovered by remember { mutableStateOf("") }
    var teachingMethod by remember { mutableStateOf("Lecture") }
    var remarks by remember { mutableStateOf("") }
    var validationError by remember { mutableStateOf<String?>(null) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Log Session Summary", fontWeight = FontWeight.Black) },
        text = {
            Column(modifier = Modifier.verticalScroll(rememberScrollState()), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                SectionPicker(sections, section) { section = it }
                OutlinedTextField(value = topicCovered, onValueChange = { topicCovered = it }, label = { Text("Topic Covered") }, modifier = Modifier.fillMaxWidth(), singleLine = true)
                OutlinedTextField(value = subtopicCovered, onValueChange = { subtopicCovered = it }, label = { Text("Subtopic (optional)") }, modifier = Modifier.fillMaxWidth(), singleLine = true)
                Text("Teaching Method:", fontWeight = FontWeight.SemiBold, fontSize = 13.sp)
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    listOf("Lecture", "Discussion", "Case Study").forEach { m ->
                        FilterChip(selected = teachingMethod == m, onClick = { teachingMethod = m }, label = { Text(m, fontSize = 12.sp) })
                    }
                }
                OutlinedTextField(value = remarks, onValueChange = { remarks = it }, label = { Text("Remarks (optional)") }, modifier = Modifier.fillMaxWidth(), minLines = 2)
                val displayedError = validationError ?: saveError
                if (displayedError != null) Text(displayedError, color = Color(0xFFEF4444), fontSize = 12.sp)
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    validationError = when {
                        section == null -> "Select a class"
                        topicCovered.isBlank() -> "Topic covered is required"
                        else -> null
                    }
                    if (validationError == null) {
                        onSave(
                            CreateSessionSummaryRequest(
                                sectionId = section!!.sectionId,
                                subjectCode = section!!.subjectCode,
                                topicCovered = topicCovered,
                                subtopicCovered = subtopicCovered.ifBlank { null },
                                teachingMethod = teachingMethod,
                                remarks = remarks.ifBlank { null }
                            )
                        )
                    }
                },
                enabled = !isSaving,
                colors = ButtonDefaults.buttonColors(containerColor = CamsNavy)
            ) {
                if (isSaving) CircularProgressIndicator(modifier = Modifier.size(18.dp), color = Color.White, strokeWidth = 2.dp) else Text("Save")
            }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text("Cancel") } }
    )
}
