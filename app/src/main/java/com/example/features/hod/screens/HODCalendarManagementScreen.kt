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
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.core.network.HODCalendarEventCreateRequest
import com.example.core.network.HODCalendarEventDto
import com.example.core.repository.HODRepositoryImpl
import com.example.core.theme.*
import com.example.core.ui.CamsCard
import com.example.features.hod.providers.HODCalendarViewModel
import com.example.features.hod.providers.HODCalendarViewModelFactory
import com.example.features.hod.widgets.HODBaseScreen

@Composable
fun HODCalendarManagementScreen(onNavigate: (String) -> Unit) {
    val context = LocalContext.current
    var activeTab by remember { mutableStateOf("events") }
    val repository = remember { HODRepositoryImpl(com.example.CamsApplication.instance.container.apiService) }
    val factory = remember { HODCalendarViewModelFactory(repository) }
    val viewModel: HODCalendarViewModel = viewModel(factory = factory)
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    var showCreateDialog by remember { mutableStateOf(false) }
    var eventPendingDelete by remember { mutableStateOf<HODCalendarEventDto?>(null) }

    LaunchedEffect(uiState.saveSuccess, uiState.saveError) {
        if (uiState.saveSuccess) {
            Toast.makeText(context, "Event saved", Toast.LENGTH_SHORT).show()
            showCreateDialog = false
            viewModel.clearSaveStatus()
        }
        uiState.saveError?.let {
            Toast.makeText(context, it, Toast.LENGTH_SHORT).show()
            viewModel.clearSaveStatus()
        }
    }

    HODBaseScreen(
        title = "Department Calendar",
        subtitle = "Create, edit, and schedule calendar events",
        currentRoute = "/hod/calendar",
        onNavigate = onNavigate
    ) {
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
            ScrollableTabRow(
                selectedTabIndex = listOf("calendar", "events").indexOf(activeTab).coerceAtLeast(0),
                containerColor = Color.Transparent,
                contentColor = CamsNavy,
                edgePadding = 0.dp,
                divider = {},
                modifier = Modifier.weight(1f)
            ) {
                val tabs = listOf(
                    "calendar" to "Calendar View",
                    "events" to "Event List"
                )
                tabs.forEach { (id, label) ->
                    Tab(
                        selected = activeTab == id,
                        onClick = { activeTab = id },
                        text = { Text(label, fontWeight = if (activeTab == id) FontWeight.Bold else FontWeight.Medium) }
                    )
                }
            }

            Button(onClick = { showCreateDialog = true }, colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF4F46E5)), shape = RoundedCornerShape(20.dp)) {
                Icon(Icons.Filled.Add, null, modifier = Modifier.size(16.dp))
                Spacer(Modifier.width(4.dp))
                Text("CREATE EVENT", fontSize = 12.sp, fontWeight = FontWeight.Bold)
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        uiState.error?.let {
            Text(it, color = Color(0xFFB91C1C), fontSize = 13.sp, modifier = Modifier.padding(bottom = 8.dp))
        }

        val events = uiState.events.sortedBy { it.startDate }

        CamsCard(modifier = Modifier.fillMaxWidth().weight(1f)) {
            if (uiState.isLoading) {
                Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator()
                }
            } else if (activeTab == "events") {
                if (events.isEmpty()) {
                    Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        Text("No calendar events yet", color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                } else {
                    LazyColumn(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                        items(events, key = { it.id }) { ev ->
                            val isHoliday = ev.isHoliday
                            Row(
                                modifier = Modifier.fillMaxWidth().background(MaterialTheme.colorScheme.surface, RoundedCornerShape(12.dp)).border(if (isHoliday) 2.dp else 1.dp, if (isHoliday) Color(0xFFF43F5E) else MaterialTheme.colorScheme.outlineVariant, RoundedCornerShape(12.dp)).padding(12.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(if (isHoliday) "HOLIDAY" else ev.category.ifBlank { "ACADEMIC EVENT" }, fontSize = 12.sp, fontWeight = FontWeight.Bold, color = if (isHoliday) Color(0xFFBE123C) else Color(0xFF4338CA), modifier = Modifier.background(if (isHoliday) Color(0xFFFFE4E6) else Color(0xFFEEF2FF), RoundedCornerShape(4.dp)).padding(horizontal = 6.dp, vertical = 2.dp))
                                    Spacer(Modifier.height(4.dp))
                                    Text(ev.title, fontWeight = FontWeight.Bold, fontSize = 14.sp, color = MaterialTheme.colorScheme.onSurface)
                                    Text(ev.department.ifBlank { "Department" }, fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                }
                                Column(horizontalAlignment = Alignment.End) {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Icon(Icons.Filled.AccessTime, null, tint = Color(0xFF8B5CF6), modifier = Modifier.size(12.dp))
                                        Spacer(Modifier.width(4.dp))
                                        Text(ev.startDate, fontSize = 12.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                    }
                                    TextButton(onClick = { eventPendingDelete = ev }) {
                                        Text("Delete", color = Color(0xFFB91C1C), fontSize = 12.sp)
                                    }
                                }
                            }
                        }
                    }
                }
            } else {
                if (events.isEmpty()) {
                    Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        Text("No events scheduled", color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                } else {
                    LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        val grouped = events.groupBy { it.startDate.take(7) }
                        grouped.toSortedMap().forEach { (month, monthEvents) ->
                            item {
                                Text(month, fontWeight = FontWeight.Bold, fontSize = 14.sp, color = CamsNavy, modifier = Modifier.padding(top = 8.dp))
                            }
                            items(monthEvents, key = { it.id }) { ev ->
                                Row(
                                    modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Box(modifier = Modifier.size(8.dp).background(if (ev.isHoliday) Color(0xFFF43F5E) else Color(0xFF4338CA), RoundedCornerShape(4.dp)))
                                    Spacer(Modifier.width(8.dp))
                                    Text(ev.startDate, fontSize = 12.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.width(90.dp))
                                    Text(ev.title, fontSize = 13.sp, color = MaterialTheme.colorScheme.onSurface)
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    if (showCreateDialog) {
        CreateEventDialog(
            isSaving = uiState.isSaving,
            onDismiss = { showCreateDialog = false },
            onSubmit = { viewModel.createEvent(it) }
        )
    }

    eventPendingDelete?.let { ev ->
        AlertDialog(
            onDismissRequest = { eventPendingDelete = null },
            title = { Text("Delete Event") },
            text = { Text("Delete \"${ev.title}\"? This cannot be undone.") },
            confirmButton = {
                TextButton(onClick = {
                    viewModel.deleteEvent(ev.id)
                    eventPendingDelete = null
                }) { Text("Delete", color = Color(0xFFB91C1C)) }
            },
            dismissButton = {
                TextButton(onClick = { eventPendingDelete = null }) { Text("Cancel") }
            }
        )
    }
}

@Composable
private fun CreateEventDialog(
    isSaving: Boolean,
    onDismiss: () -> Unit,
    onSubmit: (HODCalendarEventCreateRequest) -> Unit
) {
    var title by remember { mutableStateOf("") }
    var category by remember { mutableStateOf("Academic Event") }
    var startDate by remember { mutableStateOf("") }
    var endDate by remember { mutableStateOf("") }
    var description by remember { mutableStateOf("") }
    var location by remember { mutableStateOf("") }
    var isHoliday by remember { mutableStateOf(false) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Create Calendar Event") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedTextField(value = title, onValueChange = { title = it }, label = { Text("Title") }, modifier = Modifier.fillMaxWidth(), singleLine = true)
                OutlinedTextField(value = category, onValueChange = { category = it }, label = { Text("Category") }, modifier = Modifier.fillMaxWidth(), singleLine = true)
                OutlinedTextField(value = startDate, onValueChange = { startDate = it }, label = { Text("Start Date (YYYY-MM-DD)") }, modifier = Modifier.fillMaxWidth(), singleLine = true)
                OutlinedTextField(value = endDate, onValueChange = { endDate = it }, label = { Text("End Date (YYYY-MM-DD)") }, modifier = Modifier.fillMaxWidth(), singleLine = true)
                OutlinedTextField(value = location, onValueChange = { location = it }, label = { Text("Location") }, modifier = Modifier.fillMaxWidth(), singleLine = true)
                OutlinedTextField(value = description, onValueChange = { description = it }, label = { Text("Description") }, modifier = Modifier.fillMaxWidth())
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Checkbox(checked = isHoliday, onCheckedChange = { isHoliday = it })
                    Text("Mark as holiday")
                }
            }
        },
        confirmButton = {
            TextButton(
                enabled = title.isNotBlank() && startDate.isNotBlank() && endDate.isNotBlank() && !isSaving,
                onClick = {
                    onSubmit(
                        HODCalendarEventCreateRequest(
                            title = title.trim(),
                            category = category.trim().ifBlank { "Academic Event" },
                            startDate = startDate.trim(),
                            endDate = endDate.trim(),
                            description = description.trim(),
                            academicYear = "",
                            department = "",
                            batch = "",
                            location = location.trim(),
                            isHoliday = isHoliday
                        )
                    )
                }
            ) { Text(if (isSaving) "Saving..." else "Create") }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Cancel") }
        }
    )
}
