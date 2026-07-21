package com.example.features.faculty.screens

import android.content.Intent
import android.net.Uri
import android.widget.Toast
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
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
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.core.theme.*
import com.example.features.faculty.widgets.FacultyBaseScreen

import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.core.repository.FacultyRepositoryImpl
import com.example.core.network.CreateMeetingRequest
import com.example.features.faculty.providers.FacultyOnlineMeetingsViewModel
import com.example.features.faculty.providers.FacultyOnlineMeetingsViewModelFactory

@Composable
fun FacultyOnlineMeetingsScreen(onNavigate: (String) -> Unit) {
    val context = LocalContext.current
    val repository = remember { FacultyRepositoryImpl(com.example.CamsApplication.instance.container.apiService) }
    val factory = remember { FacultyOnlineMeetingsViewModelFactory(repository) }
    val viewModel: FacultyOnlineMeetingsViewModel = viewModel(factory = factory)
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    var showScheduleDialog by remember { mutableStateOf(false) }
    var meetingPendingDelete by remember { mutableStateOf<com.example.core.network.OnlineMeetingDto?>(null) }

    LaunchedEffect(uiState.saveSuccess, uiState.saveError) {
        if (uiState.saveSuccess) {
            Toast.makeText(context, "Meeting scheduled", Toast.LENGTH_SHORT).show()
            showScheduleDialog = false
            viewModel.clearSaveStatus()
        }
        uiState.saveError?.let {
            Toast.makeText(context, it, Toast.LENGTH_SHORT).show()
            viewModel.clearSaveStatus()
        }
    }

    FacultyBaseScreen(scrollable = false,
        title = "Online Meetings",
        currentRoute = com.example.core.navigation.AppRoutes.FACULTY_ONLINE_MEETINGS,
        onNavigate = onNavigate,
        floatingActionButton = {
            FloatingActionButton(
                onClick = { showScheduleDialog = true },
                containerColor = CamsNavy,
                contentColor = Color.White,
                shape = RoundedCornerShape(16.dp)
            ) {
                Icon(Icons.Filled.VideoCall, "Schedule Meeting")
            }
        }
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp)
        ) {
            Text("Upcoming Meetings", fontWeight = FontWeight.Bold, fontSize = 18.sp, color = MaterialTheme.colorScheme.onSurface)
            Spacer(modifier = Modifier.height(12.dp))

            uiState.error?.let {
                Text(it, color = Color(0xFFB91C1C), fontSize = 13.sp, modifier = Modifier.padding(bottom = 8.dp))
            }

            if (uiState.isLoading) {
                Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator(color = CamsNavy)
                }
            } else if (uiState.meetings.isEmpty()) {
                Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Text("No upcoming meetings", color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            } else {
                LazyColumn(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    items(uiState.meetings) { meeting ->
                        MeetingItem(
                            meeting = meeting,
                            onJoin = {
                                if (meeting.meetingLink.isNotBlank()) {
                                    try {
                                        context.startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(meeting.meetingLink)))
                                    } catch (e: Exception) {
                                        Toast.makeText(context, "Could not open meeting link", Toast.LENGTH_SHORT).show()
                                    }
                                } else {
                                    Toast.makeText(context, "No meeting link available", Toast.LENGTH_SHORT).show()
                                }
                            },
                            onDelete = { meetingPendingDelete = meeting }
                        )
                    }
                }
            }
        }
    }

    if (showScheduleDialog) {
        ScheduleMeetingDialog(
            isSaving = uiState.isSaving,
            onDismiss = { showScheduleDialog = false },
            onSubmit = { request -> viewModel.scheduleMeeting(request) }
        )
    }

    meetingPendingDelete?.let { meeting ->
        AlertDialog(
            onDismissRequest = { meetingPendingDelete = null },
            title = { Text("Delete Meeting") },
            text = { Text("Delete \"${meeting.title}\"? This cannot be undone.") },
            confirmButton = {
                TextButton(onClick = {
                    viewModel.deleteMeeting(meeting.id)
                    meetingPendingDelete = null
                }) { Text("Delete", color = Color(0xFFB91C1C)) }
            },
            dismissButton = {
                TextButton(onClick = { meetingPendingDelete = null }) { Text("Cancel") }
            }
        )
    }
}

@Composable
private fun MeetingItem(
    meeting: com.example.core.network.OnlineMeetingDto,
    onJoin: () -> Unit,
    onDelete: () -> Unit
) {
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
                Surface(
                    color = CamsNavy.copy(alpha = 0.1f),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Text(
                        meeting.platform.ifBlank { "Online" },
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = CamsNavy
                    )
                }
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(meeting.date.ifBlank { "TBA" }, fontSize = 12.sp, color = Color(0xFF64748B))
                    IconButton(onClick = onDelete, modifier = Modifier.size(40.dp)) {
                        Icon(Icons.Filled.Delete, "Delete", tint = Color(0xFF64748B), modifier = Modifier.size(16.dp))
                    }
                }
            }
            Spacer(modifier = Modifier.height(12.dp))
            Text(meeting.title, fontWeight = FontWeight.Bold, fontSize = 16.sp, color = MaterialTheme.colorScheme.onSurface)
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                Icon(Icons.Filled.AccessTime, null, tint = Color(0xFF64748B), modifier = Modifier.size(14.dp))
                Text(meeting.time.ifBlank { "N/A" }, fontSize = 13.sp, color = Color(0xFF64748B))
            }
            Spacer(modifier = Modifier.height(16.dp))
            Button(
                onClick = onJoin,
                modifier = Modifier.fillMaxWidth(),
                colors = ButtonDefaults.buttonColors(containerColor = CamsNavy),
                shape = RoundedCornerShape(12.dp)
            ) {
                Text("Join Now")
            }
        }
    }
}

@Composable
private fun ScheduleMeetingDialog(
    isSaving: Boolean,
    onDismiss: () -> Unit,
    onSubmit: (CreateMeetingRequest) -> Unit
) {
    var title by remember { mutableStateOf("") }
    var category by remember { mutableStateOf("Guest Lecture") }
    var date by remember { mutableStateOf("") }
    var time by remember { mutableStateOf("") }
    var duration by remember { mutableStateOf("60 min") }
    var platform by remember { mutableStateOf("Zoom") }
    var meetingLink by remember { mutableStateOf("") }
    var description by remember { mutableStateOf("") }

    val isValid = title.isNotBlank() && date.isNotBlank() && time.isNotBlank() &&
        meetingLink.isNotBlank() && (meetingLink.startsWith("http://") || meetingLink.startsWith("https://"))

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Schedule Meeting") },
        text = {
            Column(
                modifier = Modifier.verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                OutlinedTextField(value = title, onValueChange = { title = it }, label = { Text("Title") }, modifier = Modifier.fillMaxWidth(), singleLine = true)
                OutlinedTextField(value = category, onValueChange = { category = it }, label = { Text("Category") }, modifier = Modifier.fillMaxWidth(), singleLine = true)
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(value = date, onValueChange = { date = it }, label = { Text("Date (YYYY-MM-DD)") }, modifier = Modifier.weight(1f), singleLine = true)
                    OutlinedTextField(value = time, onValueChange = { time = it }, label = { Text("Time (HH:MM)") }, modifier = Modifier.weight(1f), singleLine = true)
                }
                OutlinedTextField(value = duration, onValueChange = { duration = it }, label = { Text("Duration") }, modifier = Modifier.fillMaxWidth(), singleLine = true)
                OutlinedTextField(value = platform, onValueChange = { platform = it }, label = { Text("Platform") }, modifier = Modifier.fillMaxWidth(), singleLine = true)
                OutlinedTextField(value = meetingLink, onValueChange = { meetingLink = it }, label = { Text("Meeting Link (https://...)") }, modifier = Modifier.fillMaxWidth(), singleLine = true)
                OutlinedTextField(value = description, onValueChange = { description = it }, label = { Text("Description") }, modifier = Modifier.fillMaxWidth())
            }
        },
        confirmButton = {
            TextButton(
                enabled = isValid && !isSaving,
                onClick = {
                    onSubmit(
                        CreateMeetingRequest(
                            title = title.trim(),
                            category = category.trim(),
                            date = date.trim(),
                            time = time.trim(),
                            duration = duration.trim(),
                            platform = platform.trim(),
                            meetingLink = meetingLink.trim(),
                            description = description.trim()
                        )
                    )
                }
            ) { Text(if (isSaving) "Scheduling..." else "Schedule") }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Cancel") }
        }
    )
}
