package com.example.features.faculty.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
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
import com.example.features.faculty.widgets.FacultyBaseScreen

import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.core.repository.FacultyRepositoryImpl
import com.example.features.faculty.providers.FacultyOnlineMeetingsViewModel
import com.example.features.faculty.providers.FacultyOnlineMeetingsViewModelFactory
import com.example.core.network.ApiClient

@Composable
fun FacultyOnlineMeetingsScreen(onNavigate: (String) -> Unit) {
    val repository = remember { FacultyRepositoryImpl(com.example.CamsApplication.instance.container.apiService) }
    val factory = remember { FacultyOnlineMeetingsViewModelFactory(repository) }
    val viewModel: FacultyOnlineMeetingsViewModel = viewModel(factory = factory)
    val uiState by viewModel.uiState.collectAsState()
    FacultyBaseScreen(scrollable = false, 
        title = "Online Meetings",
        currentRoute = com.example.core.navigation.AppRoutes.FACULTY_ONLINE_MEETINGS,
        onNavigate = onNavigate,
        floatingActionButton = {
            FloatingActionButton(
                onClick = { /* Schedule Meeting */ },
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
            Text("Upcoming Meetings", fontWeight = FontWeight.Bold, fontSize = 18.sp, color = CamsTextPrimary)
            Spacer(modifier = Modifier.height(12.dp))

            if (uiState.isLoading) {
                Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator(color = CamsNavy)
                }
            } else if (uiState.meetings.isEmpty()) {
                Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Text("No upcoming meetings", color = CamsTextSecondary)
                }
            } else {
                LazyColumn(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    items(uiState.meetings) { meeting ->
                        MeetingItem(meeting)
                    }
                }
            }
        }
    }
}

@Composable
private fun MeetingItem(meeting: com.example.core.network.OnlineMeetingDto) {
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
                        meeting.platform ?: "Online",
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = CamsNavy
                    )
                }
                Text(meeting.date ?: "TBA", fontSize = 12.sp, color = Color(0xFF64748B))
            }
            Spacer(modifier = Modifier.height(12.dp))
            Text(meeting.title, fontWeight = FontWeight.Bold, fontSize = 16.sp, color = CamsTextPrimary)
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                Icon(Icons.Filled.AccessTime, null, tint = Color(0xFF64748B), modifier = Modifier.size(14.dp))
                Text(meeting.time ?: "N/A", fontSize = 13.sp, color = Color(0xFF64748B))
            }
            Spacer(modifier = Modifier.height(16.dp))
            Button(
                onClick = { /* Join Meeting */ },
                modifier = Modifier.fillMaxWidth(),
                colors = ButtonDefaults.buttonColors(containerColor = CamsNavy),
                shape = RoundedCornerShape(12.dp)
            ) {
                Text("Join Now")
            }
        }
    }
}


