package com.example.features.principal.screens

import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
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
import com.example.core.network.CreateLegalEventRequest
import com.example.core.network.FacultyLegalEventDto
import com.example.core.repository.PrincipalRepositoryImpl
import com.example.core.theme.*
import com.example.core.ui.CamsCard
import com.example.features.principal.providers.PrincipalEventsViewModel
import com.example.features.principal.providers.PrincipalEventsViewModelFactory
import com.example.features.principal.widgets.PrincipalBaseScreen

@Composable
fun PrincipalEventsManagementScreen(
    onNavigate: (String) -> Unit,
    viewModel: PrincipalEventsViewModel = viewModel(
        factory = PrincipalEventsViewModelFactory(PrincipalRepositoryImpl(CamsApplication.instance.container.apiService))
    )
) {
    val context = LocalContext.current
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    var tab by remember { mutableStateOf(0) }
    val tabs = listOf("Pending Review", "All Events")
    var showCreateDialog by remember { mutableStateOf(false) }
    var eventPendingReject by remember { mutableStateOf<FacultyLegalEventDto?>(null) }

    LaunchedEffect(uiState.saveSuccess, uiState.saveError) {
        if (uiState.saveSuccess) {
            Toast.makeText(context, "Event published", Toast.LENGTH_SHORT).show()
            showCreateDialog = false
            viewModel.clearSaveStatus()
        }
        uiState.saveError?.let {
            Toast.makeText(context, it, Toast.LENGTH_LONG).show()
            viewModel.clearSaveStatus()
        }
    }

    PrincipalBaseScreen(
        title = "Publish Legal & Campus Events",
        subtitle = "Publish guest lectures, debates, workshops, and inter-college contests directly to the Student Portal.",
        currentRoute = AppRoutes.PRINCIPAL_EVENTS_MGMT,
        onNavigate = onNavigate,
        floatingActionButton = {
            FloatingActionButton(onClick = { showCreateDialog = true }, containerColor = CamsNavy, contentColor = Color.White) {
                Icon(Icons.Filled.Add, "Publish Event")
            }
        }
    ) {
        Row(Modifier.fillMaxWidth().padding(bottom = 12.dp), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            tabs.forEachIndexed { i, t ->
                FilterChip(selected = tab == i, onClick = { tab = i }, label = { Text(t, fontSize = 12.sp) })
            }
        }

        uiState.error?.let { Text(it, color = Color(0xFFB91C1C), fontSize = 13.sp, modifier = Modifier.padding(bottom = 8.dp)) }

        val list = if (tab == 0) uiState.pendingEvents else uiState.allEvents

        if (uiState.isLoading) {
            Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) { CircularProgressIndicator() }
        } else if (list.isEmpty()) {
            Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Text(if (tab == 0) "No events awaiting review" else "No events yet", color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
        } else {
            LazyColumn(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                items(list, key = { it.id }) { event ->
                    EventCard(
                        event = event,
                        isPendingTab = tab == 0,
                        onApprove = { viewModel.approve(event.id) },
                        onReject = { eventPendingReject = event }
                    )
                }
            }
        }
    }

    if (showCreateDialog) {
        CreateEventDialog(
            isSaving = uiState.isSaving,
            onDismiss = { showCreateDialog = false },
            onSubmit = { viewModel.createAndPublish(it) }
        )
    }

    eventPendingReject?.let { event ->
        var remarks by remember { mutableStateOf("") }
        AlertDialog(
            onDismissRequest = { eventPendingReject = null },
            title = { Text("Reject Event") },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(event.title, fontWeight = FontWeight.Bold)
                    OutlinedTextField(value = remarks, onValueChange = { remarks = it }, label = { Text("Reason (optional)") }, modifier = Modifier.fillMaxWidth())
                }
            },
            confirmButton = {
                TextButton(onClick = { viewModel.reject(event.id, remarks.trim().ifBlank { null }); eventPendingReject = null }) {
                    Text("Reject", color = Color(0xFFB91C1C))
                }
            },
            dismissButton = { TextButton(onClick = { eventPendingReject = null }) { Text("Cancel") } }
        )
    }
}

@Composable
private fun EventCard(
    event: FacultyLegalEventDto,
    isPendingTab: Boolean,
    onApprove: () -> Unit,
    onReject: () -> Unit
) {
    CamsCard {
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.Top) {
            Column(Modifier.weight(1f)) {
                Text(event.title, fontWeight = FontWeight.Bold, fontSize = 15.sp, color = MaterialTheme.colorScheme.onSurface)
                Text(
                    "${event.category ?: "Event"} • ${event.date ?: ""} ${event.time ?: ""}",
                    fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                event.organizingInstitute?.takeIf { it.isNotBlank() }?.let {
                    Text(it, fontSize = 11.sp, color = Color(0xFF64748B))
                }
            }
            val status = event.status ?: "Pending"
            val (bg, fg) = when (status) {
                "Upcoming", "Registration Open" -> Color(0xFFD1FAE5) to Color(0xFF047857)
                "Rejected" -> Color(0xFFFFE4E6) to Color(0xFFB91C1C)
                else -> Color(0xFFFEF3C7) to Color(0xFFB45309)
            }
            Text(status, fontSize = 11.sp, fontWeight = FontWeight.Bold, color = fg, modifier = Modifier.background(bg, RoundedCornerShape(4.dp)).padding(horizontal = 6.dp, vertical = 2.dp))
        }
        if (isPendingTab) {
            Spacer(Modifier.height(12.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.fillMaxWidth()) {
                Button(onClick = onApprove, modifier = Modifier.weight(1f), colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF10B981))) {
                    Text("Approve & Publish")
                }
                OutlinedButton(onClick = onReject, modifier = Modifier.weight(1f), colors = ButtonDefaults.outlinedButtonColors(contentColor = Color.Red)) {
                    Text("Reject")
                }
            }
        }
    }
}

@Composable
private fun CreateEventDialog(
    isSaving: Boolean,
    onDismiss: () -> Unit,
    onSubmit: (CreateLegalEventRequest) -> Unit
) {
    var title by remember { mutableStateOf("") }
    var category by remember { mutableStateOf("Guest Lecture") }
    var description by remember { mutableStateOf("") }
    var date by remember { mutableStateOf("") }
    var time by remember { mutableStateOf("") }
    var duration by remember { mutableStateOf("60 min") }
    var mode by remember { mutableStateOf("Online") }
    var platform by remember { mutableStateOf("") }
    var totalSeats by remember { mutableStateOf("100") }
    val seats = totalSeats.toIntOrNull() ?: 0
    val valid = title.isNotBlank() && date.isNotBlank() && time.isNotBlank() && seats > 0

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Publish New Event") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedTextField(value = title, onValueChange = { title = it }, label = { Text("Title") }, modifier = Modifier.fillMaxWidth(), singleLine = true)
                OutlinedTextField(value = category, onValueChange = { category = it }, label = { Text("Category") }, modifier = Modifier.fillMaxWidth(), singleLine = true)
                OutlinedTextField(value = description, onValueChange = { description = it }, label = { Text("Description") }, modifier = Modifier.fillMaxWidth())
                OutlinedTextField(value = date, onValueChange = { date = it }, label = { Text("Date (e.g. 20 Aug 2026)") }, modifier = Modifier.fillMaxWidth(), singleLine = true)
                OutlinedTextField(value = time, onValueChange = { time = it }, label = { Text("Time (e.g. 10:00 AM)") }, modifier = Modifier.fillMaxWidth(), singleLine = true)
                OutlinedTextField(value = duration, onValueChange = { duration = it }, label = { Text("Duration") }, modifier = Modifier.fillMaxWidth(), singleLine = true)
                Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    FilterChip(selected = mode == "Online", onClick = { mode = "Online" }, label = { Text("Online") })
                    FilterChip(selected = mode == "Offline", onClick = { mode = "Offline" }, label = { Text("Offline") })
                }
                OutlinedTextField(value = platform, onValueChange = { platform = it }, label = { Text(if (mode == "Online") "Platform (e.g. Zoom)" else "Venue") }, modifier = Modifier.fillMaxWidth(), singleLine = true)
                OutlinedTextField(value = totalSeats, onValueChange = { totalSeats = it.filter { c -> c.isDigit() } }, label = { Text("Total Seats") }, modifier = Modifier.fillMaxWidth(), singleLine = true)
            }
        },
        confirmButton = {
            TextButton(
                enabled = valid && !isSaving,
                onClick = {
                    onSubmit(
                        CreateLegalEventRequest(
                            title = title.trim(), category = category.trim().ifBlank { "Event" }, description = description.trim(),
                            date = date.trim(), time = time.trim(), duration = duration.trim().ifBlank { "60 min" },
                            mode = mode, platform = platform.trim().ifBlank { null }, meetingLink = null,
                            totalSeats = seats, eventType = category.trim().ifBlank { "General" }
                        )
                    )
                }
            ) { Text(if (isSaving) "Publishing..." else "Publish") }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text("Cancel") } }
    )
}
