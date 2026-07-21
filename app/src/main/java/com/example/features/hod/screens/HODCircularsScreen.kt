package com.example.features.hod.screens

import android.widget.Toast
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.material3.MaterialTheme
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

import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.core.network.NoticeDto
import com.example.features.hod.providers.HODCircularsViewModel

@Composable
fun HODCircularsScreen(
    viewModel: HODCircularsViewModel,
    onNavigate: (String) -> Unit
) {
    val context = LocalContext.current
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    var showCreateDialog by remember { mutableStateOf(false) }
    var circularPendingDelete by remember { mutableStateOf<NoticeDto?>(null) }

    LaunchedEffect(uiState.saveSuccess, uiState.saveError) {
        if (uiState.saveSuccess) {
            Toast.makeText(context, "Circular published", Toast.LENGTH_SHORT).show()
            showCreateDialog = false
            viewModel.clearSaveStatus()
        }
        uiState.saveError?.let {
            Toast.makeText(context, it, Toast.LENGTH_SHORT).show()
            viewModel.clearSaveStatus()
        }
    }

    HODBaseScreen(
        title = "Department Circulars",
        currentRoute = "/hod/circulars",
        onNavigate = onNavigate,
        floatingActionButton = {
            FloatingActionButton(onClick = { showCreateDialog = true }, containerColor = CamsNavy, contentColor = Color.White) {
                Icon(Icons.Filled.Add, null)
            }
        }
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            uiState.error?.let {
                Text(it, color = Color(0xFFB91C1C), fontSize = 13.sp)
            }

            if (uiState.isLoading) {
                Box(modifier = Modifier.fillMaxWidth().height(100.dp), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator(color = CamsNavy)
                }
            } else if (uiState.circulars.isEmpty()) {
                Box(modifier = Modifier.fillMaxWidth().padding(40.dp), contentAlignment = Alignment.Center) {
                    Text("No circulars found", color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            } else {
                LazyColumn(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    items(uiState.circulars, key = { it.id }) { circular ->
                        CamsCard {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Row(horizontalArrangement = Arrangement.spacedBy(16.dp), verticalAlignment = Alignment.CenterVertically, modifier = Modifier.weight(1f)) {
                                    Box(Modifier.size(40.dp).background(CamsNavy.copy(alpha = 0.1f), CircleShape), contentAlignment = Alignment.Center) {
                                        Icon(Icons.Filled.Announcement, null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(20.dp))
                                    }
                                    Column {
                                        Text(circular.title, fontWeight = FontWeight.Bold, fontSize = 15.sp, color = MaterialTheme.colorScheme.onSurface)
                                        Text(circular.body, fontSize = 13.sp, color = MaterialTheme.colorScheme.onSurfaceVariant, maxLines = 2)
                                        Text(circular.date ?: "", fontSize = 13.sp, color = Color(0xFF64748B), fontWeight = FontWeight.Bold)
                                    }
                                }
                                IconButton(onClick = { circularPendingDelete = circular }) {
                                    Icon(Icons.Filled.Delete, "Delete", tint = MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.size(18.dp))
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    if (showCreateDialog) {
        CreateCircularDialog(
            isSaving = uiState.isSaving,
            onDismiss = { showCreateDialog = false },
            onSubmit = { title, body, audience -> viewModel.createCircular(title, body, audience) }
        )
    }

    circularPendingDelete?.let { circular ->
        AlertDialog(
            onDismissRequest = { circularPendingDelete = null },
            title = { Text("Delete Circular") },
            text = { Text("Delete \"${circular.title}\"? This cannot be undone.") },
            confirmButton = {
                TextButton(onClick = {
                    viewModel.deleteCircular(circular.id)
                    circularPendingDelete = null
                }) { Text("Delete", color = Color(0xFFB91C1C)) }
            },
            dismissButton = {
                TextButton(onClick = { circularPendingDelete = null }) { Text("Cancel") }
            }
        )
    }
}

@Composable
private fun CreateCircularDialog(
    isSaving: Boolean,
    onDismiss: () -> Unit,
    onSubmit: (title: String, body: String, audience: String) -> Unit
) {
    var title by remember { mutableStateOf("") }
    var body by remember { mutableStateOf("") }
    var audience by remember { mutableStateOf("FACULTY") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("New Circular") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedTextField(value = title, onValueChange = { title = it }, label = { Text("Title") }, modifier = Modifier.fillMaxWidth(), singleLine = true)
                OutlinedTextField(value = body, onValueChange = { body = it }, label = { Text("Body") }, modifier = Modifier.fillMaxWidth())
                OutlinedTextField(value = audience, onValueChange = { audience = it }, label = { Text("Audience (FACULTY/HOD/ALL)") }, modifier = Modifier.fillMaxWidth(), singleLine = true)
            }
        },
        confirmButton = {
            TextButton(
                enabled = title.isNotBlank() && body.isNotBlank() && !isSaving,
                onClick = { onSubmit(title.trim(), body.trim(), audience.trim().ifBlank { "FACULTY" }) }
            ) { Text(if (isSaving) "Publishing..." else "Publish") }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Cancel") }
        }
    )
}
