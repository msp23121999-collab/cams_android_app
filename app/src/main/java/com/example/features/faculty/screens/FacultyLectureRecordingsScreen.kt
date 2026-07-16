package com.example.features.faculty.screens

import androidx.lifecycle.compose.collectAsStateWithLifecycle
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
import androidx.compose.runtime.*
import com.example.features.faculty.providers.FacultyRecordingsViewModel
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.core.theme.*
import com.example.core.ui.CamsCard
import com.example.features.faculty.widgets.FacultyBaseScreen

@Composable
fun FacultyLectureRecordingsScreen(
    viewModel: FacultyRecordingsViewModel,
    onNavigate: (String) -> Unit
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()

    FacultyBaseScreen(scrollable = false, 
        title = "Lecture Recordings",
        subtitle = "Manage and share classroom session recordings",
        currentRoute = "/faculty/lecture-recordings",
        onNavigate = onNavigate
    ) {
        if (state.isLoading) {
            Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator(color = CamsNavy)
            }
        } else {
            // 1. Storage Overview
            CamsCard {
                Row(
                    modifier = Modifier.padding(16.dp).fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text("Cloud Storage", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        Text("12.4 GB / 50 GB used", fontSize = 16.sp, fontWeight = FontWeight.Bold, color = CamsNavy)
                    }
                    CircularProgressIndicator(
                        progress = 0.25f,
                        modifier = Modifier.size(40.dp),
                        color = CamsNavy,
                        trackColor = MaterialTheme.colorScheme.surfaceVariant,
                        strokeWidth = 4.dp
                    )
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // 2. Recent Recordings
            Text("Recent Recordings", fontWeight = FontWeight.Bold, fontSize = 18.sp, color = MaterialTheme.colorScheme.onSurface)
            Spacer(modifier = Modifier.height(12.dp))

            if (state.recordings.isEmpty()) {
                Text("No recordings found.", modifier = Modifier.padding(20.dp), color = MaterialTheme.colorScheme.onSurfaceVariant)
            } else {
                LazyColumn(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    items(state.recordings) { recording ->
                        RecordingRow(recording)
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(80.dp))
    }

    // FAB for new recording or upload
    Box(modifier = Modifier.fillMaxSize()) {
        FloatingActionButton(
            onClick = { /* Upload or record */ },
            modifier = Modifier.align(Alignment.BottomEnd).padding(24.dp),
            containerColor = CamsNavy,
            contentColor = Color.White,
            shape = RoundedCornerShape(16.dp)
        ) {
            Icon(Icons.Filled.CloudUpload, contentDescription = "Upload Recording")
        }
    }
}

@Composable
fun RecordingRow(data: com.example.core.network.FacultyRecordingDto) {
    Surface(
        color = Color.White,
        shape = RoundedCornerShape(12.dp),
        shadowElevation = 1.dp
    ) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier.size(50.dp).background(Color(0xFFFEE2E2), RoundedCornerShape(10.dp)),
                contentAlignment = Alignment.Center
            ) {
                Icon(Icons.Filled.PlayCircle, null, tint = Color(0xFFEF4444), modifier = Modifier.size(28.dp))
            }

            Spacer(modifier = Modifier.width(16.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(data.title, fontWeight = FontWeight.Bold, fontSize = 14.sp, color = MaterialTheme.colorScheme.onSurface)
                Text(data.subject, fontSize = 12.sp, color = CamsNavy, fontWeight = FontWeight.Medium)
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(data.duration, fontSize = 13.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    Text(" • ", color = Color.LightGray)
                    Text(data.date, fontSize = 13.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    Text(" • ", color = Color.LightGray)
                    Text("${data.views} views", fontSize = 13.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            }

            IconButton(onClick = { /* More options */ }) {
                Icon(Icons.Filled.Share, null, tint = MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.size(20.dp))
            }
        }
    }
}
