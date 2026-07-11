package com.example.features.hod.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.core.theme.*
import com.example.core.ui.CamsCard
import com.example.features.hod.widgets.HODBaseScreen

@Composable
fun HODCalendarManagementScreen(onNavigate: (String) -> Unit) {
    var activeTab by remember { mutableStateOf("calendar") }

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
            
            Button(onClick = { }, colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF4F46E5)), shape = RoundedCornerShape(20.dp)) {
                Icon(Icons.Filled.Add, null, modifier = Modifier.size(16.dp))
                Spacer(Modifier.width(4.dp))
                Text("CREATE EVENT", fontSize = 12.sp, fontWeight = FontWeight.Bold)
            }
        }
        
        Spacer(modifier = Modifier.height(16.dp))
        
        CamsCard(modifier = Modifier.fillMaxWidth().weight(1f)) {
            if (activeTab == "events") {
                LazyColumn(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    items(4) { i ->
                        val isHoliday = i == 3
                        Row(
                            modifier = Modifier.fillMaxWidth().background(MaterialTheme.colorScheme.surface, RoundedCornerShape(12.dp)).border(1.dp, MaterialTheme.colorScheme.outlineVariant, RoundedCornerShape(12.dp)).border(if(isHoliday) 4.dp else 1.dp, if(isHoliday) Color(0xFFF43F5E) else MaterialTheme.colorScheme.outlineVariant, RoundedCornerShape(12.dp)).padding(12.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                    Text(if(isHoliday) "HOLIDAY" else "ACADEMIC EVENT", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = if(isHoliday) Color(0xFFBE123C) else Color(0xFF4338CA), modifier = Modifier.background(if(isHoliday) Color(0xFFFFE4E6) else Color(0xFFEEF2FF), RoundedCornerShape(4.dp)).padding(horizontal = 6.dp, vertical = 2.dp))
                                }
                                Spacer(Modifier.height(4.dp))
                                Text(if(isHoliday) "Diwali Vacation" else "Guest Lecture on Constitutional Law", fontWeight = FontWeight.Bold, fontSize = 14.sp, color = CamsTextPrimary)
                                Text("Department of Law", fontSize = 12.sp, color = CamsTextSecondary)
                            }
                            Column(horizontalAlignment = Alignment.End) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(Icons.Filled.AccessTime, null, tint = Color(0xFF8B5CF6), modifier = Modifier.size(12.dp))
                                    Spacer(Modifier.width(4.dp))
                                    Text("Oct 20, 2026", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = CamsTextSecondary)
                                }
                            }
                        }
                    }
                }
            } else {
                Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Text("Calendar View Grid Here", color = CamsTextSecondary)
                }
            }
        }
    }
}
