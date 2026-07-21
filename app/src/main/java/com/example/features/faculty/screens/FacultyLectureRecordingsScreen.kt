package com.example.features.faculty.screens

import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import com.example.features.faculty.providers.FacultyRecordingsViewModel
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.core.network.CreateRecordingRequest
import com.example.core.network.FacultyRecordingDto
import com.example.core.theme.*
import com.example.core.ui.CamsCard
import com.example.core.ui.NetworkErrorView
import com.example.features.faculty.widgets.FacultyBaseScreen

@Composable
fun FacultyLectureRecordingsScreen(
    viewModel: FacultyRecordingsViewModel,
    onNavigate: (String) -> Unit
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    val context = LocalContext.current
    var showAddDialog by remember { mutableStateOf(false) }
    var recordingPendingDelete by remember { mutableStateOf<FacultyRecordingDto?>(null) }

    LaunchedEffect(state.saveSuccess) {
        if (state.saveSuccess) {
            showAddDialog = false
            viewModel.clearSaveStatus()
        }
    }

    recordingPendingDelete?.let { recording ->
        AlertDialog(
            onDismissRequest = { recordingPendingDelete = null },
            title = { Text("Delete Recording?") },
            text = { Text("This will permanently remove \"${recording.title}\".") },
            confirmButton = {
                TextButton(onClick = { viewModel.deleteRecording(recording.id); recordingPendingDelete = null }) {
                    Text("Delete", color = Color(0xFFEF4444))
                }
            },
            dismissButton = { TextButton(onClick = { recordingPendingDelete = null }) { Text("Cancel") } }
        )
    }

    Box(modifier = Modifier.fillMaxSize()) {
        FacultyBaseScreen(
            scrollable = false,
            title = "Lecture Recordings",
            subtitle = "Manage and share classroom session recordings",
            currentRoute = "/faculty/lecture-recordings",
            onNavigate = onNavigate
        ) {
            if (state.isLoading) {
                Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator(color = CamsNavy)
                }
            } else if (state.error != null && state.recordings.isEmpty()) {
                NetworkErrorView(message = state.error ?: "Failed to load recordings", onRetry = { viewModel.loadRecordings() })
            } else {
                Text("Recent Recordings", fontWeight = FontWeight.Bold, fontSize = 18.sp, color = MaterialTheme.colorScheme.onSurface)
                Spacer(modifier = Modifier.height(12.dp))

                if (state.recordings.isEmpty()) {
                    Text("No recordings found. Tap + to add a link to a recorded session.", modifier = Modifier.padding(20.dp), color = MaterialTheme.colorScheme.onSurfaceVariant)
                } else {
                    LazyColumn(
                        verticalArrangement = Arrangement.spacedBy(12.dp),
                        contentPadding = PaddingValues(bottom = 80.dp)
                    ) {
                        items(state.recordings, key = { it.id }) { recording ->
                            RecordingRow(
                                data = recording,
                                onPlay = {
                                    if (!recording.driveLink.isNullOrBlank()) {
                                        val intent = android.content.Intent(android.content.Intent.ACTION_VIEW, android.net.Uri.parse(recording.driveLink))
                                        context.startActivity(intent)
                                    }
                                },
                                onShare = {
                                    if (!recording.driveLink.isNullOrBlank()) {
                                        val shareIntent = android.content.Intent(android.content.Intent.ACTION_SEND).apply {
                                            type = "text/plain"
                                            putExtra(android.content.Intent.EXTRA_TEXT, "${recording.title}: ${recording.driveLink}")
                                        }
                                        context.startActivity(android.content.Intent.createChooser(shareIntent, "Share recording link"))
                                    }
                                },
                                onDelete = { recordingPendingDelete = recording }
                            )
                        }
                    }
                }
            }
        }

        FloatingActionButton(
            onClick = { showAddDialog = true },
            modifier = Modifier.align(Alignment.BottomEnd).padding(24.dp),
            containerColor = CamsNavy,
            contentColor = Color.White,
            shape = RoundedCornerShape(16.dp)
        ) {
            Icon(Icons.Filled.Add, contentDescription = "Add Recording Link")
        }
    }

    if (showAddDialog) {
        AddRecordingDialog(
            isSaving = state.isSaving,
            saveError = state.saveError,
            onDismiss = { showAddDialog = false; viewModel.clearSaveStatus() },
            onSave = { request -> viewModel.addRecording(request) }
        )
    }
}

@Composable
private fun AddRecordingDialog(
    isSaving: Boolean,
    saveError: String?,
    onDismiss: () -> Unit,
    onSave: (CreateRecordingRequest) -> Unit
) {
    var title by remember { mutableStateOf("") }
    var course by remember { mutableStateOf("") }
    var semester by remember { mutableStateOf("") }
    var section by remember { mutableStateOf("") }
    var unit by remember { mutableStateOf("") }
    var topic by remember { mutableStateOf("") }
    var recordingDate by remember { mutableStateOf("") }
    var driveLink by remember { mutableStateOf("") }
    var validationError by remember { mutableStateOf<String?>(null) }
    val dateRegex = remember { Regex("""\d{4}-\d{2}-\d{2}""") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Add Recording Link", fontWeight = FontWeight.Black) },
        text = {
            Column(modifier = Modifier.verticalScroll(rememberScrollState()), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                OutlinedTextField(value = title, onValueChange = { title = it }, label = { Text("Title") }, modifier = Modifier.fillMaxWidth(), singleLine = true)
                OutlinedTextField(value = course, onValueChange = { course = it }, label = { Text("Course/Subject") }, modifier = Modifier.fillMaxWidth(), singleLine = true)
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(value = semester, onValueChange = { semester = it }, label = { Text("Semester") }, modifier = Modifier.weight(1f), singleLine = true)
                    OutlinedTextField(value = section, onValueChange = { section = it }, label = { Text("Section") }, modifier = Modifier.weight(1f), singleLine = true)
                }
                OutlinedTextField(value = unit, onValueChange = { unit = it }, label = { Text("Unit (optional)") }, modifier = Modifier.fillMaxWidth(), singleLine = true)
                OutlinedTextField(value = topic, onValueChange = { topic = it }, label = { Text("Topic (optional)") }, modifier = Modifier.fillMaxWidth(), singleLine = true)
                OutlinedTextField(value = recordingDate, onValueChange = { recordingDate = it }, label = { Text("Recording Date (YYYY-MM-DD)") }, modifier = Modifier.fillMaxWidth(), singleLine = true)
                OutlinedTextField(value = driveLink, onValueChange = { driveLink = it }, label = { Text("Drive/Video Link (https://...)") }, modifier = Modifier.fillMaxWidth(), singleLine = true)

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
                        course.isBlank() -> "Course/Subject is required"
                        semester.isBlank() -> "Semester is required"
                        section.isBlank() -> "Section is required"
                        !dateRegex.matches(recordingDate) -> "Recording date must be in YYYY-MM-DD format"
                        !android.util.Patterns.WEB_URL.matcher(driveLink).matches() -> "Enter a valid https:// link"
                        else -> null
                    }
                    if (validationError == null) {
                        onSave(
                            CreateRecordingRequest(
                                title = title,
                                course = course,
                                semester = semester,
                                section = section,
                                unit = unit.ifBlank { null },
                                topic = topic.ifBlank { null },
                                recordingDate = recordingDate,
                                driveLink = driveLink
                            )
                        )
                    }
                },
                enabled = !isSaving,
                colors = ButtonDefaults.buttonColors(containerColor = CamsNavy)
            ) {
                if (isSaving) CircularProgressIndicator(modifier = Modifier.size(18.dp), color = Color.White, strokeWidth = 2.dp)
                else Text("Add")
            }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text("Cancel") } }
    )
}

@Composable
fun RecordingRow(
    data: FacultyRecordingDto,
    onPlay: () -> Unit,
    onShare: () -> Unit,
    onDelete: () -> Unit
) {
    Surface(
        color = Color.White,
        shape = RoundedCornerShape(12.dp),
        shadowElevation = 1.dp,
        onClick = onPlay
    ) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier.size(50.dp).background(Color(0xFFFEE2E2), RoundedCornerShape(10.dp)),
                contentAlignment = Alignment.Center
            ) {
                Icon(Icons.Filled.PlayCircle, null, tint = Color(0xFFEF4444), modifier = Modifier.size(28.dp))
            }

            Spacer(modifier = Modifier.width(16.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(data.title, fontWeight = FontWeight.Bold, fontSize = 14.sp, color = MaterialTheme.colorScheme.onSurface)
                Text(data.course ?: "General", fontSize = 12.sp, color = CamsNavy, fontWeight = FontWeight.Medium)
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(data.durationDisplay, fontSize = 13.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    Text(" • ", color = Color.LightGray)
                    Text(data.recordingDate ?: data.uploadDate ?: "N/A", fontSize = 13.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            }

            Row {
                IconButton(onClick = onShare) {
                    Icon(Icons.Filled.Share, contentDescription = "Share", tint = MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.size(20.dp))
                }
                IconButton(onClick = onDelete) {
                    Icon(Icons.Filled.Delete, contentDescription = "Delete", tint = Color(0xFFEF4444), modifier = Modifier.size(20.dp))
                }
            }
        }
    }
}
