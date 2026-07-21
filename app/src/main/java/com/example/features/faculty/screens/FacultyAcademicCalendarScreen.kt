package com.example.features.faculty.screens

import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.core.network.CalendarEventDto
import com.example.core.theme.*
import com.example.core.ui.CamsCard
import com.example.features.faculty.providers.FacultyCalendarViewModel

@Composable
fun FacultyAcademicCalendarScreen(
    onNavigate: (String) -> Unit,
    viewModel: FacultyCalendarViewModel
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    com.example.features.faculty.widgets.FacultyBaseScreen(
        title = "Academic Calendar",
        subtitle = "Important dates and institutional events",
        currentRoute = "/faculty/calendar",
        onNavigate = onNavigate
    ) {
        if (uiState.isLoading) {
            Box(Modifier.fillMaxWidth().height(200.dp), contentAlignment = Alignment.Center) {
                CircularProgressIndicator(color = CamsNavy)
            }
        } else if (uiState.error != null && uiState.events.isEmpty()) {
            com.example.core.ui.NetworkErrorView(
                message = uiState.error ?: "Failed to load academic calendar",
                onRetry = { viewModel.loadCalendar() }
            )
        } else if (uiState.events.isEmpty()) {
            Box(Modifier.fillMaxWidth().padding(40.dp), contentAlignment = Alignment.Center) {
                Text("No calendar events published yet.", color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
        } else {
            uiState.events.forEach { event ->
                EventCard(event)
            }
        }
    }
}

@Composable
private fun EventCard(event: CalendarEventDto) {
    val color = if (event.isHoliday) Color(0xFFEF4444) else CamsNavy
    CamsCard {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(48.dp)
                    .background(color.copy(alpha = 0.1f), RoundedCornerShape(12.dp)),
                contentAlignment = Alignment.Center
            ) {
                Icon(if (event.isHoliday) Icons.Filled.EventBusy else Icons.Filled.Event, null, tint = color)
            }

            Column(modifier = Modifier.weight(1f)) {
                Text(event.date ?: "", fontSize = 13.sp, fontWeight = FontWeight.Bold, color = color)
                Text(event.title ?: event.eventName ?: "Untitled Event", fontWeight = FontWeight.Bold, fontSize = 15.sp, color = MaterialTheme.colorScheme.onSurface)
                if (!event.venue.isNullOrBlank()) {
                    Text(event.venue, fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
                if (!event.desc.isNullOrBlank()) {
                    Text(event.desc, fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            }

            if (!event.category.isNullOrBlank()) {
                Surface(
                    color = Color(0xFFF3F4F6),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Text(
                        event.category,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp),
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF64748B)
                    )
                }
            }
        }
    }
}
