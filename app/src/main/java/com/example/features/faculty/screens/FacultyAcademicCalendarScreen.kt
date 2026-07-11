package com.example.features.faculty.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.core.theme.*
import com.example.core.ui.CamsCard
import com.example.core.ui.CamsScreen

@Composable
fun FacultyAcademicCalendarScreen(
    onNavigate: (String) -> Unit
) {
    val events = listOf(
        CalendarEvent("July 15, 2024", "Internal Assessment I Begins", "Academics", Color(0xFF4F46E5)),
        CalendarEvent("July 22, 2024", "Last date for attendance entry", "Faculty Task", Color(0xFFD97706)),
        CalendarEvent("August 15, 2024", "Independence Day - Holiday", "Holiday", Color(0xFF059669)),
        CalendarEvent("August 28, 2024", "Guest Lecture: Constitutional Morality", "Event", Color(0xFF7C3AED)),
        CalendarEvent("Sept 05, 2024", "Teachers' Day Celebration", "Celebration", Color(0xFFEC4899))
    )

    com.example.features.faculty.widgets.FacultyBaseScreen(
        title = "Academic Calendar",
        subtitle = "Important dates and institutional events",
        currentRoute = "/faculty/calendar",
        onNavigate = onNavigate
    ) {
        events.forEach { event ->
            EventCard(event)
        }
    }
}

@Composable
private fun EventCard(event: CalendarEvent) {
    CamsCard {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(48.dp)
                    .background(event.color.copy(alpha = 0.1f), RoundedCornerShape(12.dp)),
                contentAlignment = Alignment.Center
            ) {
                Icon(Icons.Filled.Event, null, tint = event.color)
            }
            
            Column(modifier = Modifier.weight(1f)) {
                Text(event.date, fontSize = 13.sp, fontWeight = FontWeight.Bold, color = event.color)
                Text(event.title, fontWeight = FontWeight.Bold, fontSize = 15.sp, color = CamsTextPrimary)
            }
            
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

private data class CalendarEvent(
    val date: String,
    val title: String,
    val category: String,
    val color: Color
)
