package com.example.features.faculty.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
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
import com.example.core.theme.*
import com.example.features.faculty.widgets.FacultyBaseScreen

@Composable
fun FacultyLegalEventsScreen(onNavigate: (String) -> Unit) {
    var selectedTab by remember { mutableStateOf(0) }
    val tabs = listOf("Active Events", "History", "Leaderboard")

    FacultyBaseScreen(scrollable = false, 
        title = "Legal Events Hub",
        currentRoute = com.example.core.navigation.AppRoutes.FACULTY_LEGAL_EVENTS,
        onNavigate = onNavigate,
        floatingActionButton = {
            FloatingActionButton(
                onClick = { /* Add Event */ },
                containerColor = CamsNavy,
                contentColor = Color.White,
                shape = RoundedCornerShape(16.dp)
            ) {
                Icon(Icons.Filled.Add, "Add Event")
            }
        }
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp)
        ) {
            // Stats Row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                EventStatsCard("Active", "12", Icons.Filled.EventAvailable, Color(0xFF3B82F6), Modifier.weight(1f))
                EventStatsCard("Pending", "5", Icons.Filled.PendingActions, Color(0xFFF59E0B), Modifier.weight(1f))
                EventStatsCard("Total", "48", Icons.Filled.Assignment, Color(0xFF10B981), Modifier.weight(1f))
            }

            Spacer(modifier = Modifier.height(20.dp))

            // Tabs
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
                        text = {
                            Text(
                                title,
                                fontWeight = if (selectedTab == index) FontWeight.Bold else FontWeight.Medium
                            )
                        }
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Content based on tab
            when (selectedTab) {
                0 -> ActiveEventsList()
                1 -> EventHistoryList()
                2 -> LeaderboardList()
            }
        }
    }
}

@Composable
private fun EventStatsCard(
    label: String,
    value: String,
    icon: ImageVector,
    color: Color,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier,
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        shape = RoundedCornerShape(16.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Icon(icon, null, tint = color, modifier = Modifier.size(24.dp))
            Spacer(modifier = Modifier.height(8.dp))
            Text(value, fontSize = 20.sp, fontWeight = FontWeight.Bold, color = CamsTextPrimary)
            Text(label, fontSize = 12.sp, color = CamsTextSecondary)
        }
    }
}

@Composable
private fun ActiveEventsList() {
    val events = listOf(
        LegalEvent("National Moot Court", "25 Oct 2023", "Main Auditorium", "Upcoming"),
        LegalEvent("Client Counseling", "12 Nov 2023", "Moot Court Hall", "Registration Open"),
        LegalEvent("Legal Aid Camp", "05 Dec 2023", "Rural Extension", "Planning")
    )

    LazyColumn(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        items(events) { event ->
            EventCard(event)
        }
    }
}

@Composable
private fun EventHistoryList() {
    val events = listOf(
        LegalEvent("Inter-College Debate", "15 Sep 2023", "Conference Hall", "Completed"),
        LegalEvent("Judgement Writing", "20 Aug 2023", "Seminar Hall", "Completed")
    )

    LazyColumn(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        items(events) { event ->
            EventCard(event)
        }
    }
}

@Composable
private fun LeaderboardList() {
    val leaders = listOf(
        Leader("Rahul Sharma", "1200 pts", "1st"),
        Leader("Priya Patel", "1150 pts", "2nd"),
        Leader("Amit Kumar", "1100 pts", "3rd")
    )

    Card(
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        shape = RoundedCornerShape(16.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            leaders.forEachIndexed { index, leader ->
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 12.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Surface(
                            modifier = Modifier.size(32.dp),
                            shape = RoundedCornerShape(8.dp),
                            color = when(index) {
                                0 -> Color(0xFFFFD700).copy(alpha = 0.2f)
                                1 -> Color(0xFFC0C0C0).copy(alpha = 0.2f)
                                2 -> Color(0xFFCD7F32).copy(alpha = 0.2f)
                                else -> Color.LightGray.copy(alpha = 0.2f)
                            }
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Text(leader.rank, fontWeight = FontWeight.Bold, fontSize = 12.sp)
                            }
                        }
                        Spacer(modifier = Modifier.width(12.dp))
                        Text(leader.name, fontWeight = FontWeight.Bold, color = CamsTextPrimary)
                    }
                    Text(leader.points, color = CamsNavy, fontWeight = FontWeight.Bold)
                }
                if (index < leaders.size - 1) Divider(color = Color(0xFFF3F4F6))
            }
        }
    }
}

@Composable
private fun EventCard(event: LegalEvent) {
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
                    Text(event.title, fontWeight = FontWeight.Bold, fontSize = 16.sp, color = CamsTextPrimary)
                    Spacer(modifier = Modifier.height(4.dp))
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Filled.Schedule, null, tint = CamsTextSecondary, modifier = Modifier.size(14.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(event.date, fontSize = 13.sp, color = CamsTextSecondary)
                    }
                }
                Surface(
                    color = when(event.status) {
                        "Upcoming" -> Color(0xFF3B82F6).copy(alpha = 0.1f)
                        "Registration Open" -> Color(0xFF10B981).copy(alpha = 0.1f)
                        "Completed" -> Color(0xFF64748B).copy(alpha = 0.1f)
                        else -> Color(0xFFF59E0B).copy(alpha = 0.1f)
                    },
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Text(
                        event.status,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold,
                        color = when(event.status) {
                            "Upcoming" -> Color(0xFF3B82F6)
                            "Registration Open" -> Color(0xFF10B981)
                            "Completed" -> Color(0xFF64748B)
                            else -> Color(0xFFF59E0B)
                        }
                    )
                }
            }
            Spacer(modifier = Modifier.height(12.dp))
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Filled.LocationOn, null, tint = CamsTextSecondary, modifier = Modifier.size(14.dp))
                Spacer(modifier = Modifier.width(4.dp))
                Text(event.location, fontSize = 13.sp, color = CamsTextSecondary)
            }
            Spacer(modifier = Modifier.height(16.dp))
            Button(
                onClick = { /* View Details */ },
                modifier = Modifier.fillMaxWidth(),
                colors = ButtonDefaults.buttonColors(containerColor = CamsNavy),
                shape = RoundedCornerShape(12.dp)
            ) {
                Text("Manage Event")
            }
        }
    }
}

data class LegalEvent(val title: String, val date: String, val location: String, val status: String)
data class Leader(val name: String, val points: String, val rank: String)
