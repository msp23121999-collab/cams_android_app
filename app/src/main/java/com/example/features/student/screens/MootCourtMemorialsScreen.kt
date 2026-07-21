package com.example.features.student.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Gavel
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.core.network.MootCourtMemorialDto
import com.example.core.ui.CamsCard
import com.example.core.ui.CamsScreen
import com.example.core.ui.EmptyStateView
import com.example.core.ui.NetworkErrorView
import com.example.core.ui.shimmerEffect
import com.example.features.student.providers.MootCourtViewModel

private val Purple650 = Color(0xFF7E22CE)
private val ColorTextSecondary = Color(0xFF64748B)

@Composable
fun MootCourtMemorialsScreen(
    viewModel: MootCourtViewModel = viewModel(),
    onNavigate: (String) -> Unit,
    onBack: () -> Unit
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    var showCreateDialog by remember { mutableStateOf(false) }
    var editingMemorial by remember { mutableStateOf<MootCourtMemorialDto?>(null) }

    CamsScreen(
        title = "Memorial Drafts",
        subtitle = "Your moot court memorial submissions",
        onBackClick = onBack,
        floatingActionButton = {
            FloatingActionButton(
                onClick = { showCreateDialog = true },
                containerColor = Purple650
            ) {
                Icon(Icons.Filled.Add, contentDescription = "New Memorial", tint = Color.White)
            }
        },
        scrollable = false
    ) {
        if (uiState.isLoading && uiState.memorials.isEmpty()) {
            Column(
                modifier = Modifier.fillMaxSize().padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Box(modifier = Modifier.fillMaxWidth().height(90.dp).shimmerEffect())
                Box(modifier = Modifier.fillMaxWidth().height(90.dp).shimmerEffect())
                Box(modifier = Modifier.fillMaxWidth().height(90.dp).shimmerEffect())
            }
        } else if (uiState.error != null && uiState.memorials.isEmpty()) {
            NetworkErrorView(
                message = uiState.error ?: "Failed to load memorials",
                onRetry = { viewModel.fetchMemorials() }
            )
        } else if (uiState.memorials.isEmpty()) {
            EmptyStateView(
                icon = Icons.Filled.Gavel,
                title = "No Memorial Drafts",
                message = "Create your first moot court memorial draft to get started.",
                buttonText = "+ New Memorial",
                onButtonClick = { showCreateDialog = true }
            )
        } else {
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                items(uiState.memorials, key = { it.id }) { memorial ->
                    MemorialCard(memorial, onClick = { editingMemorial = memorial })
                }
            }
        }
    }

    if (showCreateDialog) {
        MemorialEditDialog(
            title = "New Memorial",
            initialTitle = "",
            initialCaseName = "",
            initialContent = "",
            isSaving = uiState.isSaving,
            onDismiss = { showCreateDialog = false },
            onSave = { title, caseName, content ->
                viewModel.createMemorial(title, caseName.ifBlank { null }, content) { success ->
                    if (success) showCreateDialog = false
                }
            }
        )
    }

    editingMemorial?.let { memorial ->
        MemorialEditDialog(
            title = "Edit Memorial",
            initialTitle = memorial.title,
            initialCaseName = memorial.caseName ?: "",
            initialContent = memorial.content,
            initialStatus = memorial.status,
            isSaving = uiState.isSaving,
            showDelete = true,
            onDismiss = { editingMemorial = null },
            onDelete = {
                viewModel.deleteMemorial(memorial.id) { success ->
                    if (success) editingMemorial = null
                }
            },
            onSave = { newTitle, newCaseName, newContent ->
                viewModel.updateMemorial(memorial.id, newTitle, newCaseName.ifBlank { null }, newContent, memorial.status) { success ->
                    if (success) editingMemorial = null
                }
            }
        )
    }
}

@Composable
private fun MemorialCard(memorial: MootCourtMemorialDto, onClick: () -> Unit) {
    CamsCard(onClick = onClick) {
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.Top) {
            Column(modifier = Modifier.weight(1f)) {
                Text(memorial.title, fontSize = 15.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurface, maxLines = 1, overflow = TextOverflow.Ellipsis)
                if (!memorial.caseName.isNullOrBlank()) {
                    Text(memorial.caseName, fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.padding(top = 2.dp), maxLines = 1, overflow = TextOverflow.Ellipsis)
                }
                Text("Updated ${memorial.updatedAt.take(10)}", fontSize = 11.sp, color = ColorTextSecondary, modifier = Modifier.padding(top = 6.dp))
            }
            val (badgeColor, badgeText) = if (memorial.status == "submitted") Color(0xFF10B981) to "Submitted" else Color(0xFFD97706) to "Draft"
            Box(
                modifier = Modifier
                    .background(badgeColor.copy(alpha = 0.1f), RoundedCornerShape(6.dp))
                    .padding(horizontal = 8.dp, vertical = 4.dp)
            ) {
                Text(badgeText, fontSize = 11.sp, fontWeight = FontWeight.Bold, color = badgeColor)
            }
        }
    }
}

@Composable
private fun MemorialEditDialog(
    title: String,
    initialTitle: String,
    initialCaseName: String,
    initialContent: String,
    initialStatus: String = "draft",
    isSaving: Boolean = false,
    showDelete: Boolean = false,
    onDismiss: () -> Unit,
    onSave: (String, String, String) -> Unit,
    onDelete: () -> Unit = {}
) {
    var titleText by remember { mutableStateOf(initialTitle) }
    var caseName by remember { mutableStateOf(initialCaseName) }
    var content by remember { mutableStateOf(initialContent) }
    var showDeleteConfirm by remember { mutableStateOf(false) }

    if (showDeleteConfirm) {
        AlertDialog(
            onDismissRequest = { showDeleteConfirm = false },
            title = { Text("Delete Memorial?", fontWeight = FontWeight.Black, fontSize = 18.sp) },
            text = { Text("This will permanently delete this memorial draft. This action cannot be undone.") },
            confirmButton = {
                TextButton(onClick = {
                    showDeleteConfirm = false
                    onDelete()
                }) {
                    Text("Delete", color = Color(0xFFE11D48), fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteConfirm = false }) {
                    Text("Cancel", color = Purple650, fontWeight = FontWeight.Bold)
                }
            },
            shape = RoundedCornerShape(24.dp)
        )
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(title, fontWeight = FontWeight.Black, fontSize = 18.sp) },
        text = {
            Column(
                modifier = Modifier.fillMaxWidth().verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                OutlinedTextField(
                    value = titleText,
                    onValueChange = { titleText = it },
                    label = { Text("Title") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    shape = RoundedCornerShape(12.dp),
                    colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = Purple650, focusedLabelColor = Purple650, cursorColor = Purple650)
                )
                OutlinedTextField(
                    value = caseName,
                    onValueChange = { caseName = it },
                    label = { Text("Case Name (optional)") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    shape = RoundedCornerShape(12.dp),
                    colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = Purple650, focusedLabelColor = Purple650, cursorColor = Purple650)
                )
                OutlinedTextField(
                    value = content,
                    onValueChange = { content = it },
                    label = { Text("Content") },
                    modifier = Modifier.fillMaxWidth(),
                    minLines = 5,
                    shape = RoundedCornerShape(12.dp),
                    colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = Purple650, focusedLabelColor = Purple650, cursorColor = Purple650)
                )
                if (showDelete) {
                    TextButton(onClick = { showDeleteConfirm = true }) {
                        Text("Delete Memorial", color = Color(0xFFE11D48), fontWeight = FontWeight.Bold)
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = { onSave(titleText, caseName, content) },
                enabled = !isSaving && titleText.isNotBlank() && content.isNotBlank(),
                colors = ButtonDefaults.buttonColors(containerColor = Purple650),
                shape = RoundedCornerShape(12.dp)
            ) {
                if (isSaving) {
                    CircularProgressIndicator(modifier = Modifier.size(16.dp), color = Color.White, strokeWidth = 2.dp)
                } else {
                    Text("Save", fontWeight = FontWeight.Bold)
                }
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel", color = Purple650, fontWeight = FontWeight.Bold)
            }
        },
        shape = RoundedCornerShape(24.dp)
    )
}
