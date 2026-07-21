package com.example.features.admin.screens

import android.widget.Toast
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.ui.platform.LocalContext
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.*

import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.features.admin.providers.AdminAcademicCalendarViewModel
import com.example.features.admin.providers.AdminAcademicCalendarViewModelFactory
import com.example.core.repository.AdminRepositoryImpl
import com.example.CamsApplication

import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.core.theme.*
import com.example.core.ui.CamsCard
import com.example.features.admin.widgets.AdminBaseScreen
import com.example.core.navigation.AppRoutes

@Composable
fun AdminAcademicCalendarScreen(
    onNavigate: (String) -> Unit,
    viewModel: AdminAcademicCalendarViewModel = viewModel(factory = AdminAcademicCalendarViewModelFactory(AdminRepositoryImpl(CamsApplication.instance.container.apiService)))
) {
    val context = LocalContext.current
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    var showCreateDialog by remember { mutableStateOf(false) }
    var eventPendingDelete by remember { mutableStateOf<String?>(null) }

    LaunchedEffect(uiState.saveSuccess, uiState.saveError) {
        if (uiState.saveSuccess) {
            Toast.makeText(context, "Event scheduled", Toast.LENGTH_SHORT).show()
            showCreateDialog = false
            viewModel.clearSaveStatus()
        }
        uiState.saveError?.let {
            Toast.makeText(context, it, Toast.LENGTH_LONG).show()
            viewModel.clearSaveStatus()
        }
    }

    AdminBaseScreen(
        title = "Academic Calendar",
        subtitle = "Manage institutional events and holidays",
        currentRoute = AppRoutes.ADMIN_ACADEMIC_CALENDAR,
        onNavigate = onNavigate
    ) {
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
            Text("Upcoming Events", fontWeight = FontWeight.Bold, fontSize = 18.sp, color = MaterialTheme.colorScheme.onSurface)
            Button(onClick = { showCreateDialog = true }, colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF4F46E5)), shape = RoundedCornerShape(8.dp)) {
                Icon(Icons.Filled.Add, null, modifier = Modifier.size(16.dp))
                Spacer(Modifier.width(4.dp))
                Text("Schedule Event", fontSize = 12.sp, fontWeight = FontWeight.Bold)
            }
        }

        Spacer(Modifier.height(16.dp))

        uiState.error?.let {
            Text(it, color = Color(0xFFB91C1C), fontSize = 13.sp, modifier = Modifier.padding(bottom = 8.dp))
        }

        CamsCard(modifier = Modifier.fillMaxWidth().weight(1f)) {
            if (uiState.isLoading) {
                Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) { CircularProgressIndicator() }
            } else if (uiState.events.isEmpty()) {
                Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Text("No calendar events scheduled", color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            } else {
                LazyColumn(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    items(uiState.events, key = { it.id }) { event ->
                        val isHoliday = event.isHoliday
                        Row(
                            modifier = Modifier.fillMaxWidth().background(MaterialTheme.colorScheme.surface, RoundedCornerShape(12.dp)).border(1.dp, if (isHoliday) Color(0xFFF43F5E) else MaterialTheme.colorScheme.outlineVariant, RoundedCornerShape(12.dp)).padding(16.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    if (isHoliday) "HOLIDAY" else event.category.ifBlank { "ACADEMIC EVENT" }.uppercase(),
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = if (isHoliday) Color(0xFFBE123C) else Color(0xFF4338CA),
                                    modifier = Modifier.background(if (isHoliday) Color(0xFFFFE4E6) else Color(0xFFEEF2FF), RoundedCornerShape(4.dp)).padding(horizontal = 6.dp, vertical = 2.dp)
                                )
                                Spacer(Modifier.height(8.dp))
                                Text(event.title, fontWeight = FontWeight.Bold, fontSize = 14.sp, color = MaterialTheme.colorScheme.onSurface)
                                if (event.location.isNotBlank()) {
                                    Text(event.location, fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                }
                            }
                            Column(horizontalAlignment = Alignment.End) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(Icons.Filled.AccessTime, null, tint = MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.size(12.dp))
                                    Spacer(Modifier.width(4.dp))
                                    Text(event.startDate, fontSize = 12.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                }
                                TextButton(onClick = { eventPendingDelete = event.id }) {
                                    Text("Delete", fontSize = 12.sp, color = Color(0xFFB91C1C))
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    if (showCreateDialog) {
        ScheduleEventDialog(
            isSaving = uiState.isSaving,
            onDismiss = { showCreateDialog = false },
            onSubmit = { viewModel.createEvent(it) }
        )
    }

    eventPendingDelete?.let { id ->
        AlertDialog(
            onDismissRequest = { eventPendingDelete = null },
            title = { Text("Delete Event") },
            text = { Text("Delete this calendar event? This cannot be undone.") },
            confirmButton = {
                TextButton(onClick = { viewModel.deleteEvent(id); eventPendingDelete = null }) {
                    Text("Delete", color = Color(0xFFB91C1C))
                }
            },
            dismissButton = { TextButton(onClick = { eventPendingDelete = null }) { Text("Cancel") } }
        )
    }
}

@Composable
private fun ScheduleEventDialog(
    isSaving: Boolean,
    onDismiss: () -> Unit,
    onSubmit: (com.example.core.network.HODCalendarEventCreateRequest) -> Unit
) {
    var title by remember { mutableStateOf("") }
    var category by remember { mutableStateOf("Academic Event") }
    var startDate by remember { mutableStateOf("") }
    var endDate by remember { mutableStateOf("") }
    var location by remember { mutableStateOf("") }
    var description by remember { mutableStateOf("") }
    var isHoliday by remember { mutableStateOf(false) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Schedule Event") },
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
                        com.example.core.network.HODCalendarEventCreateRequest(
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
            ) { Text(if (isSaving) "Saving..." else "Schedule") }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text("Cancel") } }
    )
}
