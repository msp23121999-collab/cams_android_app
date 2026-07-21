package com.example.features.admin.screens

import android.widget.Toast
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
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
import com.example.features.admin.providers.AdminNoticesViewModel
import com.example.features.admin.providers.AdminNoticesViewModelFactory
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
fun AdminCircularsScreen(
    onNavigate: (String) -> Unit,
    viewModel: AdminNoticesViewModel = viewModel(factory = AdminNoticesViewModelFactory(AdminRepositoryImpl(CamsApplication.instance.container.apiService)))
) {
    val context = LocalContext.current
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    var showCreateDialog by remember { mutableStateOf(false) }
    var noticePendingDelete by remember { mutableStateOf<String?>(null) }

    LaunchedEffect(uiState.saveSuccess, uiState.saveError) {
        if (uiState.saveSuccess) {
            Toast.makeText(context, "Circular published", Toast.LENGTH_SHORT).show()
            showCreateDialog = false
            viewModel.clearSaveStatus()
        }
        uiState.saveError?.let {
            Toast.makeText(context, it, Toast.LENGTH_LONG).show()
            viewModel.clearSaveStatus()
        }
    }

    AdminBaseScreen(
        title = "Circular Notices",
        subtitle = "Publish and manage official circulars",
        currentRoute = AppRoutes.ADMIN_CIRCULARS,
        onNavigate = onNavigate
    ) {
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
            Text("Published Circulars", fontWeight = FontWeight.Bold, fontSize = 18.sp, color = MaterialTheme.colorScheme.onSurface)
            Button(onClick = { showCreateDialog = true }, colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF4F46E5)), shape = RoundedCornerShape(8.dp)) {
                Icon(Icons.Filled.Campaign, null, modifier = Modifier.size(16.dp))
                Spacer(Modifier.width(4.dp))
                Text("New Circular", fontSize = 12.sp, fontWeight = FontWeight.Bold)
            }
        }

        Spacer(Modifier.height(16.dp))

        uiState.error?.let {
            Text(it, color = Color(0xFFB91C1C), fontSize = 13.sp, modifier = Modifier.padding(bottom = 8.dp))
        }

        CamsCard(modifier = Modifier.fillMaxWidth().weight(1f)) {
            if (uiState.isLoading) {
                Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) { CircularProgressIndicator() }
            } else if (uiState.notices.isEmpty()) {
                Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Text("No circulars published yet", color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            } else {
                LazyColumn(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    items(uiState.notices, key = { it.id }) { notice ->
                        Row(
                            modifier = Modifier.fillMaxWidth().background(MaterialTheme.colorScheme.surface, RoundedCornerShape(12.dp)).border(1.dp, MaterialTheme.colorScheme.outlineVariant, RoundedCornerShape(12.dp)).padding(16.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(notice.title, fontWeight = FontWeight.Bold, fontSize = 14.sp, color = MaterialTheme.colorScheme.onSurface)
                                Text(notice.body, fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant, maxLines = 2)
                                Text(
                                    "${notice.publisherName ?: "Admin"} • ${notice.date ?: ""}${notice.audienceType?.let { a -> " • $a" } ?: ""}",
                                    fontSize = 11.sp,
                                    color = Color(0xFF64748B)
                                )
                            }
                            IconButton(onClick = { noticePendingDelete = notice.id }) {
                                Icon(Icons.Filled.Delete, contentDescription = "Delete circular", tint = Color(0xFFB91C1C), modifier = Modifier.size(18.dp))
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
            onSubmit = { title, body, audience -> viewModel.createNotice(title, body, audience) }
        )
    }

    noticePendingDelete?.let { id ->
        AlertDialog(
            onDismissRequest = { noticePendingDelete = null },
            title = { Text("Delete Circular") },
            text = { Text("Are you sure you want to delete this circular? This cannot be undone.") },
            confirmButton = {
                TextButton(onClick = { viewModel.deleteNotice(id); noticePendingDelete = null }) {
                    Text("Delete", color = Color(0xFFB91C1C))
                }
            },
            dismissButton = { TextButton(onClick = { noticePendingDelete = null }) { Text("Cancel") } }
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
    val audiences = listOf("ALL", "STUDENT", "FACULTY", "HOD", "PARENT")
    var audience by remember { mutableStateOf(audiences[0]) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Publish Circular") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                OutlinedTextField(value = title, onValueChange = { title = it }, label = { Text("Title") }, modifier = Modifier.fillMaxWidth(), singleLine = true)
                OutlinedTextField(value = body, onValueChange = { body = it }, label = { Text("Message") }, modifier = Modifier.fillMaxWidth())
                Text("Audience", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurfaceVariant)
                LazyRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    items(audiences) { a ->
                        FilterChip(selected = audience == a, onClick = { audience = a }, label = { Text(a, fontSize = 11.sp) })
                    }
                }
            }
        },
        confirmButton = {
            TextButton(
                enabled = title.isNotBlank() && body.isNotBlank() && !isSaving,
                onClick = { onSubmit(title.trim(), body.trim(), audience) }
            ) { Text(if (isSaving) "Publishing..." else "Publish") }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text("Cancel") } }
    )
}
