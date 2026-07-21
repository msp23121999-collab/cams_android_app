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
import com.example.core.theme.*
import com.example.core.ui.CamsCard
import com.example.features.hod.widgets.HODBaseScreen

import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.core.network.NoticeDto
import com.example.features.hod.providers.HODCircularsViewModel
import com.example.features.hod.providers.HODCircularsViewModelFactory

@Composable
fun HODCommunicationCenterScreen(onNavigate: (String) -> Unit) {
    val context = LocalContext.current
    var activeTab by remember { mutableStateOf("announcements") }
    val factory = remember { HODCircularsViewModelFactory(com.example.CamsApplication.instance.container.apiService) }
    val viewModel: HODCircularsViewModel = viewModel(factory = factory)
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    var showCreateDialog by remember { mutableStateOf(false) }

    LaunchedEffect(uiState.saveSuccess, uiState.saveError) {
        if (uiState.saveSuccess) {
            Toast.makeText(context, "Announcement sent", Toast.LENGTH_SHORT).show()
            showCreateDialog = false
            viewModel.clearSaveStatus()
        }
        uiState.saveError?.let {
            Toast.makeText(context, it, Toast.LENGTH_SHORT).show()
            viewModel.clearSaveStatus()
        }
    }

    val myAnnouncements = uiState.circulars.filter { it.publisherRole.equals("HOD", ignoreCase = true) }
    val principalNotices = uiState.circulars.filter { it.publisherRole.equals("PRINCIPAL", ignoreCase = true) }

    HODBaseScreen(
        title = "Communication Center",
        subtitle = "Official communication and announcement hub for the department",
        currentRoute = "/hod/communication",
        onNavigate = onNavigate
    ) {
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.fillMaxWidth()) {
            KpiCard("My Announcements", "${myAnnouncements.size}", Icons.Filled.CheckCircle, Color(0xFF10B981), Modifier.weight(1f))
            KpiCard("Principal Notices", "${principalNotices.size}", Icons.Filled.Campaign, Color(0xFF3B82F6), Modifier.weight(1f))
        }

        Spacer(modifier = Modifier.height(16.dp))

        ScrollableTabRow(
            selectedTabIndex = listOf("announcements", "received").indexOf(activeTab).coerceAtLeast(0),
            containerColor = Color.Transparent,
            contentColor = CamsNavy,
            edgePadding = 0.dp,
            divider = {}
        ) {
            val tabs = listOf(
                "announcements" to "My Announcements",
                "received" to "Principal Notices"
            )
            tabs.forEach { (id, label) ->
                Tab(
                    selected = activeTab == id,
                    onClick = { activeTab = id },
                    text = { Text(label, fontWeight = if (activeTab == id) FontWeight.Bold else FontWeight.Medium) }
                )
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        uiState.error?.let {
            Text(it, color = Color(0xFFB91C1C), fontSize = 13.sp)
        }

        CamsCard(modifier = Modifier.fillMaxWidth().weight(1f)) {
            if (activeTab == "announcements") {
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                    Text("HOD Announcement History", fontWeight = FontWeight.Bold, fontSize = 16.sp, color = MaterialTheme.colorScheme.onSurface)
                    Button(onClick = { showCreateDialog = true }, colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF18181B)), shape = RoundedCornerShape(8.dp)) {
                        Icon(Icons.Filled.Add, null, modifier = Modifier.size(16.dp))
                        Spacer(Modifier.width(4.dp))
                        Text("Create Announcement", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                    }
                }
                Spacer(Modifier.height(16.dp))
                NoticeList(myAnnouncements, uiState.isLoading, "No announcements sent yet")
            } else {
                NoticeList(principalNotices, uiState.isLoading, "No notices from the Principal")
            }
        }
    }

    if (showCreateDialog) {
        CreateAnnouncementDialog(
            isSaving = uiState.isSaving,
            onDismiss = { showCreateDialog = false },
            onSubmit = { title, body, audience -> viewModel.createCircular(title, body, audience) }
        )
    }
}

@Composable
private fun NoticeList(notices: List<NoticeDto>, isLoading: Boolean, emptyMessage: String) {
    if (isLoading) {
        Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            CircularProgressIndicator()
        }
    } else if (notices.isEmpty()) {
        Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Text(emptyMessage, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
    } else {
        LazyColumn(verticalArrangement = Arrangement.spacedBy(12.dp)) {
            items(notices, key = { it.id }) { notice ->
                Row(
                    modifier = Modifier.fillMaxWidth().background(MaterialTheme.colorScheme.surface, RoundedCornerShape(12.dp)).border(1.dp, MaterialTheme.colorScheme.outlineVariant, RoundedCornerShape(12.dp)).padding(12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(notice.title, fontWeight = FontWeight.Bold, fontSize = 14.sp, color = MaterialTheme.colorScheme.onSurface)
                        Text(notice.body, fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant, maxLines = 2)
                    }
                    Text(notice.date ?: "", fontSize = 12.sp, color = Color(0xFF64748B), fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}

@Composable
private fun CreateAnnouncementDialog(
    isSaving: Boolean,
    onDismiss: () -> Unit,
    onSubmit: (title: String, body: String, audience: String) -> Unit
) {
    var title by remember { mutableStateOf("") }
    var body by remember { mutableStateOf("") }
    var audience by remember { mutableStateOf("FACULTY") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Create Announcement") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedTextField(value = title, onValueChange = { title = it }, label = { Text("Title") }, modifier = Modifier.fillMaxWidth(), singleLine = true)
                OutlinedTextField(value = body, onValueChange = { body = it }, label = { Text("Message") }, modifier = Modifier.fillMaxWidth())
                OutlinedTextField(value = audience, onValueChange = { audience = it }, label = { Text("Audience (FACULTY/HOD/ALL)") }, modifier = Modifier.fillMaxWidth(), singleLine = true)
            }
        },
        confirmButton = {
            TextButton(
                enabled = title.isNotBlank() && body.isNotBlank() && !isSaving,
                onClick = { onSubmit(title.trim(), body.trim(), audience.trim().ifBlank { "FACULTY" }) }
            ) { Text(if (isSaving) "Sending..." else "Send") }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Cancel") }
        }
    )
}

@Composable
private fun KpiCard(label: String, value: String, icon: androidx.compose.ui.graphics.vector.ImageVector, color: Color, modifier: Modifier) {
    Card(modifier = modifier, colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface), elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)) {
        Column(modifier = Modifier.padding(12.dp)) {
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.Top) {
                Text(label, fontSize = 13.sp, fontWeight = FontWeight.Black, color = Color(0xFF64748B), modifier = Modifier.weight(1f))
                Box(Modifier.background(color.copy(alpha=0.1f), RoundedCornerShape(8.dp)).padding(4.dp)) {
                    Icon(icon, null, tint = color, modifier = Modifier.size(16.dp))
                }
            }
            Text(value, fontSize = 20.sp, fontWeight = FontWeight.Black, color = MaterialTheme.colorScheme.onSurface, modifier = Modifier.padding(top = 8.dp))
        }
    }
}
