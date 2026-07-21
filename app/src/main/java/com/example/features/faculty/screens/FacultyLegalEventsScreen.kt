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
import androidx.compose.material.icons.automirrored.filled.*
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.core.network.CreateLegalEventRequest
import com.example.core.network.FacultyLegalEventDto
import com.example.core.theme.*
import com.example.core.ui.NetworkErrorView
import com.example.features.faculty.widgets.FacultyBaseScreen

import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.core.repository.FacultyRepositoryImpl
import com.example.features.faculty.providers.FacultyLegalEventsViewModel
import com.example.features.faculty.providers.FacultyLegalEventsViewModelFactory

@Composable
fun FacultyLegalEventsScreen(onNavigate: (String) -> Unit) {
    val repository = remember { FacultyRepositoryImpl(com.example.CamsApplication.instance.container.apiService) }
    val factory = remember { FacultyLegalEventsViewModelFactory(repository) }
    val viewModel: FacultyLegalEventsViewModel = viewModel(factory = factory)
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    var selectedTab by remember { mutableStateOf(0) }
    val tabs = listOf("Active Events", "History")
    var showCreateDialog by remember { mutableStateOf(false) }
    var selectedEvent by remember { mutableStateOf<FacultyLegalEventDto?>(null) }

    LaunchedEffect(uiState.saveSuccess) {
        if (uiState.saveSuccess) {
            showCreateDialog = false
            viewModel.clearSaveStatus()
        }
    }

    FacultyBaseScreen(
        scrollable = false,
        title = "Legal Events Hub",
        currentRoute = com.example.core.navigation.AppRoutes.FACULTY_LEGAL_EVENTS,
        onNavigate = onNavigate,
        floatingActionButton = {
            FloatingActionButton(
                onClick = { showCreateDialog = true },
                containerColor = CamsNavy,
                contentColor = Color.White,
                shape = RoundedCornerShape(16.dp)
            ) {
                Icon(Icons.Filled.Add, "Add Event")
            }
        }
    ) {
        Column(modifier = Modifier.fillMaxSize().padding(16.dp)) {
            if (uiState.isLoading) {
                Box(Modifier.fillMaxWidth().height(200.dp), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator(color = CamsNavy)
                }
            } else if (uiState.error != null && uiState.events.isEmpty()) {
                NetworkErrorView(message = uiState.error ?: "Failed to load legal events", onRetry = { viewModel.loadEvents() })
            } else {
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    EventStatsCard(
                        "Active", "${uiState.events.count { it.status == "Upcoming" || it.status == "Registration Open" }}",
                        Icons.Filled.EventAvailable, Color(0xFF3B82F6), Modifier.weight(1f)
                    )
                    EventStatsCard(
                        "Pending", "${uiState.events.count { it.status == "Pending" }}",
                        Icons.Filled.PendingActions, Color(0xFFF59E0B), Modifier.weight(1f)
                    )
                    EventStatsCard("Total", "${uiState.events.size}", Icons.AutoMirrored.Filled.Assignment, Color(0xFF10B981), Modifier.weight(1f))
                }

                Spacer(modifier = Modifier.height(20.dp))

                ScrollableTabRow(
                    selectedTabIndex = selectedTab,
                    containerColor = Color.Transparent,
                    contentColor = CamsNavy,
                    edgePadding = 0.dp,
                    divider = {},
                    indicator = { tabPositions ->
                        if (selectedTab < tabPositions.size) {
                            TabRowDefaults.SecondaryIndicator(
                                modifier = Modifier.tabIndicatorOffset(tabPositions[selectedTab]),
                                color = CamsNavy
                            )
                        }
                    }
                ) {
                    tabs.forEachIndexed { index, title ->
                        Tab(
                            selected = selectedTab == index,
                            onClick = { selectedTab = index },
                            text = { Text(title, fontWeight = if (selectedTab == index) FontWeight.Bold else FontWeight.Medium) }
                        )
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                val shown = when (selectedTab) {
                    0 -> uiState.events.filter { it.status == "Upcoming" || it.status == "Registration Open" || it.status == "Pending" }
                    else -> uiState.events.filter { it.status == "Rejected" }
                }

                if (shown.isEmpty()) {
                    Text(
                        if (selectedTab == 0) "No active or pending events." else "No past events.",
                        modifier = Modifier.padding(20.dp),
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                } else {
                    LazyColumn(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                        items(shown, key = { it.id }) { event ->
                            EventCard(event, onViewDetails = { selectedEvent = event })
                        }
                    }
                }
            }
        }
    }

    if (showCreateDialog) {
        CreateEventDialog(
            isSaving = uiState.isSaving,
            saveError = uiState.saveError,
            onDismiss = { showCreateDialog = false; viewModel.clearSaveStatus() },
            onSave = { request -> viewModel.postEvent(request) }
        )
    }

    selectedEvent?.let { event ->
        EventDetailsDialog(event = event, onDismiss = { selectedEvent = null })
    }
}

@Composable
private fun EventStatsCard(label: String, value: String, icon: ImageVector, color: Color, modifier: Modifier = Modifier) {
    Card(
        modifier = modifier,
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        shape = RoundedCornerShape(16.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp), horizontalAlignment = Alignment.CenterHorizontally) {
            Icon(icon, null, tint = color, modifier = Modifier.size(24.dp))
            Spacer(modifier = Modifier.height(8.dp))
            Text(value, fontSize = 20.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurface)
            Text(label, fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
    }
}

@Composable
private fun EventCard(event: FacultyLegalEventDto, onViewDetails: () -> Unit) {
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
                verticalAlignment = Alignment.Top
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(event.title, fontWeight = FontWeight.Bold, fontSize = 16.sp, color = MaterialTheme.colorScheme.onSurface)
                    Spacer(modifier = Modifier.height(4.dp))
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Filled.Schedule, null, tint = MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.size(14.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(event.date ?: "TBA", fontSize = 13.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                }
                Surface(
                    color = when (event.status) {
                        "Upcoming" -> Color(0xFF3B82F6).copy(alpha = 0.1f)
                        "Registration Open" -> Color(0xFF10B981).copy(alpha = 0.1f)
                        "Rejected" -> Color(0xFFEF4444).copy(alpha = 0.1f)
                        else -> Color(0xFFF59E0B).copy(alpha = 0.1f)
                    },
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Text(
                        event.status ?: "Pending",
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold,
                        color = when (event.status) {
                            "Upcoming" -> Color(0xFF3B82F6)
                            "Registration Open" -> Color(0xFF10B981)
                            "Rejected" -> Color(0xFFEF4444)
                            else -> Color(0xFFF59E0B)
                        }
                    )
                }
            }
            Spacer(modifier = Modifier.height(12.dp))
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Filled.LocationOn, null, tint = MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.size(14.dp))
                Spacer(modifier = Modifier.width(4.dp))
                Text(event.mode?.let { "${it}${event.platform?.let { p -> " • $p" } ?: ""}" } ?: "Mode TBA", fontSize = 13.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
            Spacer(modifier = Modifier.height(16.dp))
            Button(
                onClick = onViewDetails,
                modifier = Modifier.fillMaxWidth(),
                colors = ButtonDefaults.buttonColors(containerColor = CamsNavy),
                shape = RoundedCornerShape(12.dp)
            ) {
                Text("View Details")
            }
        }
    }
}

@Composable
private fun EventDetailsDialog(event: FacultyLegalEventDto, onDismiss: () -> Unit) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(event.title, fontWeight = FontWeight.Black) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                Text(event.description ?: "No description provided.", fontSize = 13.sp)
                Spacer(Modifier.height(8.dp))
                Text("Date: ${event.date ?: "TBA"} ${event.time ?: ""}", fontSize = 13.sp, fontWeight = FontWeight.Medium)
                Text("Duration: ${event.duration ?: "N/A"}", fontSize = 13.sp)
                Text("Mode: ${event.mode ?: "N/A"}${event.platform?.let { " ($it)" } ?: ""}", fontSize = 13.sp)
                if (!event.meetingLink.isNullOrBlank()) {
                    Text("Link: ${event.meetingLink}", fontSize = 13.sp, color = CamsNavy)
                }
                if (event.totalSeats != null) {
                    Text("Seats: ${event.availableSeats ?: "-"} / ${event.totalSeats} available", fontSize = 13.sp)
                }
                if (!event.rejectionRemarks.isNullOrBlank()) {
                    Spacer(Modifier.height(8.dp))
                    Text("Rejection reason: ${event.rejectionRemarks}", fontSize = 13.sp, color = Color(0xFFEF4444))
                }
            }
        },
        confirmButton = { TextButton(onClick = onDismiss) { Text("Close") } }
    )
}

@Composable
private fun CreateEventDialog(
    isSaving: Boolean,
    saveError: String?,
    onDismiss: () -> Unit,
    onSave: (CreateLegalEventRequest) -> Unit
) {
    var title by remember { mutableStateOf("") }
    var category by remember { mutableStateOf("") }
    var description by remember { mutableStateOf("") }
    var date by remember { mutableStateOf("") }
    var time by remember { mutableStateOf("") }
    var duration by remember { mutableStateOf("") }
    var mode by remember { mutableStateOf("Online") }
    var platform by remember { mutableStateOf("") }
    var meetingLink by remember { mutableStateOf("") }
    var totalSeats by remember { mutableStateOf("") }
    var validationError by remember { mutableStateOf<String?>(null) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Propose Legal Event", fontWeight = FontWeight.Black) },
        text = {
            Column(modifier = Modifier.verticalScroll(rememberScrollState()), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Text(
                    "This will be submitted for Principal review before it appears as a live event.",
                    fontSize = 12.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                OutlinedTextField(value = title, onValueChange = { title = it }, label = { Text("Title") }, modifier = Modifier.fillMaxWidth(), singleLine = true)
                OutlinedTextField(value = category, onValueChange = { category = it }, label = { Text("Category") }, modifier = Modifier.fillMaxWidth(), singleLine = true)
                OutlinedTextField(value = description, onValueChange = { description = it }, label = { Text("Description") }, modifier = Modifier.fillMaxWidth(), minLines = 2)
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(value = date, onValueChange = { date = it }, label = { Text("Date") }, modifier = Modifier.weight(1f), singleLine = true)
                    OutlinedTextField(value = time, onValueChange = { time = it }, label = { Text("Time") }, modifier = Modifier.weight(1f), singleLine = true)
                }
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(value = duration, onValueChange = { duration = it }, label = { Text("Duration") }, modifier = Modifier.weight(1f), singleLine = true)
                    OutlinedTextField(
                        value = totalSeats, onValueChange = { totalSeats = it }, label = { Text("Total Seats") }, modifier = Modifier.weight(1f), singleLine = true,
                        keyboardOptions = androidx.compose.foundation.text.KeyboardOptions(keyboardType = androidx.compose.ui.text.input.KeyboardType.Number)
                    )
                }
                Text("Mode:", fontWeight = FontWeight.SemiBold)
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    listOf("Online", "Offline", "Hybrid").forEach { m ->
                        FilterChip(selected = mode == m, onClick = { mode = m }, label = { Text(m) })
                    }
                }
                if (mode != "Offline") {
                    OutlinedTextField(value = platform, onValueChange = { platform = it }, label = { Text("Platform (e.g. Zoom)") }, modifier = Modifier.fillMaxWidth(), singleLine = true)
                    OutlinedTextField(value = meetingLink, onValueChange = { meetingLink = it }, label = { Text("Meeting Link") }, modifier = Modifier.fillMaxWidth(), singleLine = true)
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
                        category.isBlank() -> "Category is required"
                        date.isBlank() -> "Date is required"
                        totalSeats.toIntOrNull() == null || totalSeats.toInt() <= 0 -> "Enter a valid seat count"
                        mode != "Offline" && meetingLink.isNotBlank() && !android.util.Patterns.WEB_URL.matcher(meetingLink).matches() -> "Enter a valid meeting URL"
                        else -> null
                    }
                    if (validationError == null) {
                        onSave(
                            CreateLegalEventRequest(
                                title = title,
                                category = category,
                                description = description,
                                date = date,
                                time = time,
                                duration = duration,
                                mode = mode,
                                platform = platform.ifBlank { null },
                                meetingLink = meetingLink.ifBlank { null },
                                totalSeats = totalSeats.toInt(),
                                eventType = "Intra-College"
                            )
                        )
                    }
                },
                enabled = !isSaving,
                colors = ButtonDefaults.buttonColors(containerColor = CamsNavy)
            ) {
                if (isSaving) CircularProgressIndicator(modifier = Modifier.size(18.dp), color = Color.White, strokeWidth = 2.dp)
                else Text("Submit for Review")
            }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text("Cancel") } }
    )
}
