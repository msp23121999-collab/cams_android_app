package com.example.features.faculty.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import com.example.features.faculty.widgets.FacultyBaseScreen

import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.core.network.ConversationDto
import com.example.core.network.MessageContactDto
import com.example.core.network.MessageDto
import com.example.core.repository.FacultyRepositoryImpl
import com.example.features.faculty.providers.FacultyCommunicationViewModel
import com.example.features.faculty.providers.FacultyCommunicationViewModelFactory

@Composable
fun FacultyCommunicationScreen(onNavigate: (String) -> Unit) {
    var searchText by remember { mutableStateOf("") }
    var showContactPicker by remember { mutableStateOf(false) }
    val repository = remember { FacultyRepositoryImpl(com.example.CamsApplication.instance.container.apiService) }
    val factory = remember { FacultyCommunicationViewModelFactory(repository) }
    val viewModel: FacultyCommunicationViewModel = viewModel(factory = factory)
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    if (uiState.activeThreadUserId != null) {
        val name = uiState.conversations.find { it.userId == uiState.activeThreadUserId }?.userName
            ?: uiState.contacts.find { it.id == uiState.activeThreadUserId }?.fullName
            ?: "Conversation"
        ThreadScreen(
            title = name,
            userId = uiState.activeThreadUserId!!,
            messages = uiState.activeThreadMessages,
            isLoading = uiState.isThreadLoading,
            isSending = uiState.isSending,
            error = uiState.threadError,
            onBack = { viewModel.closeThread() },
            onSend = { body -> viewModel.sendMessage(uiState.activeThreadUserId!!, body) }
        )
        return
    }

    FacultyBaseScreen(scrollable = false,
        title = "Communication",
        currentRoute = com.example.core.navigation.AppRoutes.FACULTY_COMMUNICATION,
        onNavigate = onNavigate,
        floatingActionButton = {
            FloatingActionButton(
                onClick = { showContactPicker = true },
                containerColor = CamsNavy,
                contentColor = Color.White,
                shape = RoundedCornerShape(16.dp)
            ) {
                Icon(Icons.Filled.Edit, "New Message")
            }
        }
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp)
        ) {
            OutlinedTextField(
                value = searchText,
                onValueChange = { searchText = it },
                placeholder = { Text("Search conversations...") },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                leadingIcon = { Icon(Icons.Filled.Search, null) },
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = CamsNavy,
                    unfocusedBorderColor = Color(0xFFE5E7EB)
                )
            )

            Spacer(modifier = Modifier.height(20.dp))

            uiState.error?.let {
                Text(it, color = Color(0xFFB91C1C), fontSize = 13.sp, modifier = Modifier.padding(bottom = 8.dp))
            }

            val filtered = uiState.conversations.filter {
                searchText.isBlank() || it.userName.contains(searchText, ignoreCase = true)
            }

            if (uiState.isLoading) {
                Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator(color = CamsNavy)
                }
            } else if (filtered.isEmpty()) {
                Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Text("No conversations yet. Tap + to start one.", color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            } else {
                LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    items(filtered, key = { it.userId }) { chat ->
                        ChatItem(chat, onClick = { viewModel.openThread(chat.userId) })
                    }
                }
            }
        }
    }

    if (showContactPicker) {
        ContactPickerDialog(
            contacts = uiState.contacts,
            onDismiss = { showContactPicker = false },
            onSelect = {
                showContactPicker = false
                viewModel.openThread(it.id)
            }
        )
    }
}

@Composable
private fun ChatItem(chat: ConversationDto, onClick: () -> Unit) {
    Surface(
        onClick = onClick,
        modifier = Modifier.fillMaxWidth(),
        color = MaterialTheme.colorScheme.surface,
        shape = RoundedCornerShape(16.dp)
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(48.dp)
                    .background(Color(0xFF10B981).copy(alpha = 0.1f), CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Icon(Icons.Filled.Person, null, tint = Color(0xFF10B981))
            }
            Column(modifier = Modifier.weight(1f)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(chat.userName, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurface, fontSize = 15.sp)
                    Text(chat.lastMessageAt.take(10), fontSize = 13.sp, color = Color(0xFF64748B))
                }
                Spacer(modifier = Modifier.height(4.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(chat.lastMessage, fontSize = 13.sp, color = MaterialTheme.colorScheme.onSurfaceVariant, maxLines = 1)
                    if (chat.unreadCount > 0) {
                        Surface(
                            color = CamsNavy,
                            shape = CircleShape
                        ) {
                            Text(
                                chat.unreadCount.toString(),
                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                                fontSize = 12.sp,
                                color = Color.White,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun ContactPickerDialog(
    contacts: List<MessageContactDto>,
    onDismiss: () -> Unit,
    onSelect: (MessageContactDto) -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("New Message") },
        text = {
            if (contacts.isEmpty()) {
                Text("No contacts available")
            } else {
                LazyColumn {
                    items(contacts, key = { it.id }) { contact ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickableRow { onSelect(contact) }
                                .padding(vertical = 12.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column {
                                Text(contact.fullName, fontWeight = FontWeight.Bold)
                                Text(contact.role, fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            }
                        }
                    }
                }
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) { Text("Cancel") }
        }
    )
}

private fun Modifier.clickableRow(onClick: () -> Unit): Modifier =
    this.clickable(onClick = onClick)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun ThreadScreen(
    title: String,
    userId: String,
    messages: List<MessageDto>,
    isLoading: Boolean,
    isSending: Boolean,
    error: String?,
    onBack: () -> Unit,
    onSend: (String) -> Unit
) {
    var draft by remember { mutableStateOf("") }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(title) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Filled.ArrowBack, "Back")
                    }
                }
            )
        },
        bottomBar = {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                OutlinedTextField(
                    value = draft,
                    onValueChange = { draft = it },
                    modifier = Modifier.weight(1f),
                    placeholder = { Text("Type a message...") },
                    shape = RoundedCornerShape(20.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                IconButton(
                    onClick = {
                        if (draft.isNotBlank() && !isSending) {
                            onSend(draft.trim())
                            draft = ""
                        }
                    }
                ) {
                    Icon(Icons.Filled.Send, "Send", tint = CamsNavy)
                }
            }
        }
    ) { padding ->
        Column(modifier = Modifier.padding(padding).fillMaxSize().padding(horizontal = 16.dp)) {
            error?.let {
                Text(it, color = Color(0xFFB91C1C), fontSize = 13.sp, modifier = Modifier.padding(vertical = 8.dp))
            }
            if (isLoading) {
                Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator(color = CamsNavy)
                }
            } else {
                LazyColumn(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                    reverseLayout = true
                ) {
                    items(messages.reversed(), key = { it.id }) { msg ->
                        val isMine = msg.senderId != userId
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = if (isMine) Arrangement.End else Arrangement.Start
                        ) {
                            Surface(
                                color = if (isMine) CamsNavy else MaterialTheme.colorScheme.surfaceVariant,
                                shape = RoundedCornerShape(14.dp)
                            ) {
                                Text(
                                    msg.body,
                                    modifier = Modifier.padding(horizontal = 14.dp, vertical = 10.dp),
                                    color = if (isMine) Color.White else MaterialTheme.colorScheme.onSurface
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}
