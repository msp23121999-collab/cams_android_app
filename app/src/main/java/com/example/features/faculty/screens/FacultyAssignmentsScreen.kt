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
import com.example.core.network.FacultyAssignmentSubmissionDto
import com.example.core.network.CreateAssignmentRequest
import com.example.core.network.FacultyAssignmentDto
import com.example.core.network.GradeSubmissionRequest
import com.example.features.faculty.providers.FacultyAssignmentsViewModel
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.core.theme.*
import com.example.core.ui.CamsCard
import com.example.core.ui.NetworkErrorView
import com.example.features.faculty.widgets.FacultyBaseScreen

@Composable
fun FacultyAssignmentsScreen(
    viewModel: FacultyAssignmentsViewModel,
    onNavigate: (String) -> Unit
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    var selectedTab by remember { mutableStateOf(0) }
    val tabs = listOf("Active", "Grade Submissions", "Archived")
    var showCreateDialog by remember { mutableStateOf(false) }
    var selectedSubmission by remember { mutableStateOf<FacultyAssignmentSubmissionDto?>(null) }

    LaunchedEffect(state.saveSuccess) {
        if (state.saveSuccess) {
            showCreateDialog = false
            viewModel.clearSaveStatus()
        }
    }

    Box(modifier = Modifier.fillMaxSize()) {
        FacultyBaseScreen(
            scrollable = false,
            title = "Assignments",
            subtitle = "Create and grade student assignments",
            currentRoute = "/faculty/assignments",
            onNavigate = onNavigate
        ) {
            if (state.isLoading) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator(color = CamsNavy)
                }
            } else if (state.error != null && state.assignments.isEmpty()) {
                NetworkErrorView(message = state.error ?: "Failed to load assignments", onRetry = { viewModel.loadAssignments() })
            } else {
                TabRow(
                    selectedTabIndex = selectedTab,
                    containerColor = Color.Transparent,
                    contentColor = CamsNavy,
                    indicator = { tabPositions ->
                        TabRowDefaults.SecondaryIndicator(
                            Modifier.tabIndicatorOffset(tabPositions[selectedTab]),
                            color = CamsNavy
                        )
                    },
                    divider = {}
                ) {
                    tabs.forEachIndexed { index, title ->
                        Tab(
                            selected = selectedTab == index,
                            onClick = { selectedTab = index },
                            text = {
                                Text(
                                    title,
                                    fontSize = 14.sp,
                                    fontWeight = if (selectedTab == index) FontWeight.Bold else FontWeight.Medium
                                )
                            }
                        )
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                when (selectedTab) {
                    0 -> {
                        val active = state.assignments.filter { it.status == "Draft" || it.status == "Published" }
                        if (active.isEmpty()) {
                            EmptyTabMessage("No active assignments. Tap + to create one.")
                        } else {
                            LazyColumn(verticalArrangement = Arrangement.spacedBy(16.dp), contentPadding = PaddingValues(bottom = 88.dp)) {
                                items(active, key = { it.id }) { assignment ->
                                    val submissionCount = state.submissions.count { it.assignmentId == assignment.id }
                                    AssignmentCard(
                                        assignment = assignment,
                                        submissionCount = submissionCount,
                                        onDelete = { viewModel.deleteAssignment(assignment.id) }
                                    )
                                }
                            }
                        }
                    }
                    1 -> {
                        val pending = state.submissions.filter { it.status != "Evaluated" }
                        if (pending.isEmpty()) {
                            EmptyTabMessage("No submissions awaiting evaluation.")
                        } else {
                            LazyColumn(verticalArrangement = Arrangement.spacedBy(16.dp), contentPadding = PaddingValues(bottom = 88.dp)) {
                                items(pending, key = { it.id }) { submission ->
                                    val assignment = state.assignments.find { it.id == submission.assignmentId }
                                    SubmissionCard(
                                        submission = submission,
                                        assignmentTitle = assignment?.title ?: "Assignment",
                                        totalMarks = assignment?.totalMarks,
                                        onGrade = { selectedSubmission = submission }
                                    )
                                }
                            }
                        }
                    }
                    else -> {
                        val archived = state.assignments.filter { it.status == "Archived" || it.status == "Closed" }
                        if (archived.isEmpty()) {
                            EmptyTabMessage("No archived assignments.")
                        } else {
                            LazyColumn(verticalArrangement = Arrangement.spacedBy(16.dp), contentPadding = PaddingValues(bottom = 88.dp)) {
                                items(archived, key = { it.id }) { assignment ->
                                    val submissionCount = state.submissions.count { it.assignmentId == assignment.id }
                                    AssignmentCard(assignment = assignment, submissionCount = submissionCount, onDelete = { viewModel.deleteAssignment(assignment.id) })
                                }
                            }
                        }
                    }
                }
            }
        }

        FloatingActionButton(
            onClick = { showCreateDialog = true },
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(24.dp),
            containerColor = CamsNavy,
            contentColor = Color.White,
            shape = RoundedCornerShape(16.dp)
        ) {
            Icon(Icons.Filled.Add, contentDescription = "Add Assignment")
        }
    }

    if (showCreateDialog) {
        CreateAssignmentDialog(
            isSaving = state.isSaving,
            saveError = state.saveError,
            onDismiss = { showCreateDialog = false; viewModel.clearSaveStatus() },
            onSave = { request -> viewModel.createAssignment(request) }
        )
    }

    selectedSubmission?.let { submission ->
        val assignment = state.assignments.find { it.id == submission.assignmentId }
        GradeSubmissionDialog(
            submission = submission,
            totalMarks = assignment?.totalMarks,
            isSaving = state.isSaving,
            saveError = state.saveError,
            onDismiss = { selectedSubmission = null; viewModel.clearSaveStatus() },
            onSave = { request ->
                viewModel.gradeSubmission(submission.id, request) { success ->
                    if (success) selectedSubmission = null
                }
            }
        )
    }
}

@Composable
private fun EmptyTabMessage(message: String) {
    Text(
        message,
        modifier = Modifier.padding(20.dp),
        color = MaterialTheme.colorScheme.onSurfaceVariant,
        fontSize = 14.sp
    )
}

@Composable
fun AssignmentCard(assignment: FacultyAssignmentDto, submissionCount: Int, onDelete: () -> Unit = {}) {
    var showDeleteConfirm by remember { mutableStateOf(false) }

    if (showDeleteConfirm) {
        AlertDialog(
            onDismissRequest = { showDeleteConfirm = false },
            title = { Text("Delete Assignment?") },
            text = { Text("This will permanently delete \"${assignment.title}\" and all its submissions.") },
            confirmButton = {
                TextButton(onClick = { showDeleteConfirm = false; onDelete() }) { Text("Delete", color = Color(0xFFEF4444)) }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteConfirm = false }) { Text("Cancel") }
            }
        )
    }

    CamsCard {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Surface(color = CamsNavy.copy(alpha = 0.1f), shape = RoundedCornerShape(4.dp)) {
                    Text(
                        assignment.status,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp),
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = CamsNavy
                    )
                }
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        "Due: ${assignment.deadline ?: "N/A"}",
                        fontSize = 12.sp,
                        color = Color(0xFFEF4444),
                        fontWeight = FontWeight.Medium
                    )
                    IconButton(onClick = { showDeleteConfirm = true }, modifier = Modifier.size(40.dp)) {
                        Icon(Icons.Filled.Delete, contentDescription = "Delete", tint = Color(0xFFEF4444), modifier = Modifier.size(16.dp))
                    }
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            Text(assignment.title, fontSize = 16.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurface)
            if (!assignment.subject.isNullOrBlank()) {
                Text(assignment.subject, fontSize = 13.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }

            Spacer(modifier = Modifier.height(12.dp))

            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Filled.Description, null, tint = MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.size(16.dp))
                Spacer(Modifier.width(6.dp))
                Text("$submissionCount submissions received", fontSize = 13.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
        }
    }
}

@Composable
private fun SubmissionCard(
    submission: FacultyAssignmentSubmissionDto,
    assignmentTitle: String,
    totalMarks: Int?,
    onGrade: () -> Unit
) {
    CamsCard {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(assignmentTitle, fontSize = 14.sp, fontWeight = FontWeight.Bold, color = CamsNavy)
            Spacer(Modifier.height(4.dp))
            Text(submission.studentName ?: "Unknown Student", fontSize = 16.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurface)
            Text(submission.registerNumber ?: "", fontSize = 13.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
            Spacer(Modifier.height(12.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    if (submission.marksObtained != null) "Marks: ${submission.marksObtained}/${totalMarks ?: "-"}" else "Not yet graded",
                    fontSize = 13.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Button(
                    onClick = onGrade,
                    contentPadding = PaddingValues(horizontal = 12.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF3B82F6)),
                    shape = RoundedCornerShape(8.dp),
                    modifier = Modifier.height(32.dp)
                ) {
                    Text("Grade", fontSize = 12.sp, color = Color.White)
                }
            }
        }
    }
}

@Composable
private fun CreateAssignmentDialog(
    isSaving: Boolean,
    saveError: String?,
    onDismiss: () -> Unit,
    onSave: (CreateAssignmentRequest) -> Unit
) {
    var title by remember { mutableStateOf("") }
    var subject by remember { mutableStateOf("") }
    var unit by remember { mutableStateOf("") }
    var topic by remember { mutableStateOf("") }
    var description by remember { mutableStateOf("") }
    var instructions by remember { mutableStateOf("") }
    var totalMarks by remember { mutableStateOf("") }
    var deadline by remember { mutableStateOf("") }
    var semester by remember { mutableStateOf("") }
    var section by remember { mutableStateOf("") }
    var publishNow by remember { mutableStateOf(false) }
    var validationError by remember { mutableStateOf<String?>(null) }
    val dateRegex = remember { Regex("""\d{4}-\d{2}-\d{2}""") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("New Assignment", fontWeight = FontWeight.Black) },
        text = {
            Column(modifier = Modifier.verticalScroll(rememberScrollState()), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                OutlinedTextField(value = title, onValueChange = { title = it }, label = { Text("Title") }, modifier = Modifier.fillMaxWidth(), singleLine = true)
                OutlinedTextField(value = subject, onValueChange = { subject = it }, label = { Text("Subject") }, modifier = Modifier.fillMaxWidth(), singleLine = true)
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(value = semester, onValueChange = { semester = it }, label = { Text("Semester") }, modifier = Modifier.weight(1f), singleLine = true)
                    OutlinedTextField(value = section, onValueChange = { section = it }, label = { Text("Section") }, modifier = Modifier.weight(1f), singleLine = true)
                }
                OutlinedTextField(value = unit, onValueChange = { unit = it }, label = { Text("Unit") }, modifier = Modifier.fillMaxWidth(), singleLine = true)
                OutlinedTextField(value = topic, onValueChange = { topic = it }, label = { Text("Topic") }, modifier = Modifier.fillMaxWidth(), singleLine = true)
                OutlinedTextField(value = description, onValueChange = { description = it }, label = { Text("Description") }, modifier = Modifier.fillMaxWidth(), minLines = 2)
                OutlinedTextField(value = instructions, onValueChange = { instructions = it }, label = { Text("Instructions") }, modifier = Modifier.fillMaxWidth(), minLines = 2)
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(
                        value = totalMarks,
                        onValueChange = { totalMarks = it },
                        label = { Text("Total Marks") },
                        modifier = Modifier.weight(1f),
                        singleLine = true,
                        keyboardOptions = androidx.compose.foundation.text.KeyboardOptions(keyboardType = androidx.compose.ui.text.input.KeyboardType.Number)
                    )
                    OutlinedTextField(value = deadline, onValueChange = { deadline = it }, label = { Text("Deadline (YYYY-MM-DD)") }, modifier = Modifier.weight(1.5f), singleLine = true)
                }
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Checkbox(checked = publishNow, onCheckedChange = { publishNow = it })
                    Text("Publish immediately (unchecked = save as Draft)", fontSize = 13.sp)
                }
                val displayedError = validationError ?: saveError
                if (displayedError != null) {
                    Text(displayedError, color = Color(0xFFEF4444), fontSize = 12.sp, fontWeight = FontWeight.Medium)
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    validationError = when {
                        title.isBlank() -> "Title is required"
                        subject.isBlank() -> "Subject is required"
                        semester.isBlank() -> "Semester is required"
                        section.isBlank() -> "Section is required"
                        totalMarks.toIntOrNull() == null || totalMarks.toInt() <= 0 -> "Enter a valid total marks value"
                        !dateRegex.matches(deadline) -> "Deadline must be in YYYY-MM-DD format"
                        else -> null
                    }
                    if (validationError == null) {
                        onSave(
                            CreateAssignmentRequest(
                                title = title,
                                type = "Theory Assignment",
                                subject = subject,
                                unit = unit,
                                topic = topic,
                                description = description,
                                instructions = instructions,
                                totalMarks = totalMarks.toInt(),
                                deadline = deadline,
                                status = if (publishNow) "Published" else "Draft",
                                semester = semester,
                                section = section
                            )
                        )
                    }
                },
                enabled = !isSaving,
                colors = ButtonDefaults.buttonColors(containerColor = CamsNavy)
            ) {
                if (isSaving) CircularProgressIndicator(modifier = Modifier.size(18.dp), color = Color.White, strokeWidth = 2.dp)
                else Text("Save")
            }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text("Cancel") } }
    )
}

@Composable
private fun GradeSubmissionDialog(
    submission: FacultyAssignmentSubmissionDto,
    totalMarks: Int?,
    isSaving: Boolean,
    saveError: String?,
    onDismiss: () -> Unit,
    onSave: (GradeSubmissionRequest) -> Unit
) {
    var marks by remember { mutableStateOf(submission.marksObtained?.toString() ?: "") }
    var grade by remember { mutableStateOf(submission.grade ?: "") }
    var feedback by remember { mutableStateOf(submission.feedback ?: "") }
    var remarks by remember { mutableStateOf(submission.remarks ?: "") }
    var validationError by remember { mutableStateOf<String?>(null) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Grade Submission", fontWeight = FontWeight.Black) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Text("Student: ${submission.studentName ?: "Unknown"}", fontSize = 13.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                OutlinedTextField(
                    value = marks,
                    onValueChange = { marks = it },
                    label = { Text("Marks Obtained${if (totalMarks != null) " / $totalMarks" else ""}") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    keyboardOptions = androidx.compose.foundation.text.KeyboardOptions(keyboardType = androidx.compose.ui.text.input.KeyboardType.Decimal)
                )
                OutlinedTextField(value = grade, onValueChange = { grade = it }, label = { Text("Grade (e.g. A, B+)") }, modifier = Modifier.fillMaxWidth(), singleLine = true)
                OutlinedTextField(value = feedback, onValueChange = { feedback = it }, label = { Text("Feedback") }, modifier = Modifier.fillMaxWidth(), minLines = 2)
                OutlinedTextField(value = remarks, onValueChange = { remarks = it }, label = { Text("Remarks") }, modifier = Modifier.fillMaxWidth(), minLines = 2)
                val displayedError = validationError ?: saveError
                if (displayedError != null) {
                    Text(displayedError, color = Color(0xFFEF4444), fontSize = 12.sp, fontWeight = FontWeight.Medium)
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    val marksVal = marks.toDoubleOrNull()
                    validationError = when {
                        marksVal == null || marksVal < 0 -> "Enter a valid non-negative marks value"
                        totalMarks != null && marksVal > totalMarks -> "Marks cannot exceed the total marks ($totalMarks)"
                        grade.isBlank() -> "Grade is required"
                        else -> null
                    }
                    if (validationError == null) {
                        onSave(GradeSubmissionRequest(marksVal!!, grade, feedback, remarks, "Evaluated"))
                    }
                },
                enabled = !isSaving,
                colors = ButtonDefaults.buttonColors(containerColor = CamsNavy)
            ) {
                if (isSaving) CircularProgressIndicator(modifier = Modifier.size(18.dp), color = Color.White, strokeWidth = 2.dp)
                else Text("Submit Grade")
            }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text("Cancel") } }
    )
}
