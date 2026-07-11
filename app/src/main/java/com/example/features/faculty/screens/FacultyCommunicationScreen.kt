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
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.core.theme.*
import com.example.features.faculty.widgets.FacultyBaseScreen

@Composable
fun FacultyCommunicationScreen(onNavigate: (String) -> Unit) {
    var searchText by remember { mutableStateOf("") }

    FacultyBaseScreen(scrollable = false, 
        title = "Communication",
        currentRoute = com.example.core.navigation.AppRoutes.FACULTY_COMMUNICATION,
        onNavigate = onNavigate,
        floatingActionButton = {
            FloatingActionButton(
                onClick = { /* New Message */ },
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

            val chats = listOf(
                Chat("HOD - Computer Science", "Please review the new syllabus draft.", "10:45 AM", 2, true),
                Chat("Registrar Office", "Documents for research grant verified.", "09:15 AM", 0, false),
                Chat("Staff Group", "Lunch meeting at 1 PM today.", "Yesterday", 5, true),
                Chat("Exam Cell", "Invigilation duty list updated.", "12 Oct", 0, false)
            )

            LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                items(chats) { chat ->
                    ChatItem(chat)
                }
            }
        }
    }
}

@Composable
private fun ChatItem(chat: Chat) {
    Surface(
        onClick = { /* Open Chat */ },
        modifier = Modifier.fillMaxWidth(),
        color = Color.White,
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
                    .background(if (chat.isGroup) Color(0xFF3B82F6).copy(alpha = 0.1f) else Color(0xFF10B981).copy(alpha = 0.1f), CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    if (chat.isGroup) Icons.Filled.Group else Icons.Filled.Person,
                    null,
                    tint = if (chat.isGroup) Color(0xFF3B82F6) else Color(0xFF10B981)
                )
            }
            Column(modifier = Modifier.weight(1f)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(chat.name, fontWeight = FontWeight.Bold, color = CamsTextPrimary, fontSize = 15.sp)
                    Text(chat.time, fontSize = 13.sp, color = Color(0xFF64748B))
                }
                Spacer(modifier = Modifier.height(4.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(chat.lastMessage, fontSize = 13.sp, color = CamsTextSecondary, maxLines = 1)
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

data class Chat(val name: String, val lastMessage: String, val time: String, val unreadCount: Int, val isGroup: Boolean)
