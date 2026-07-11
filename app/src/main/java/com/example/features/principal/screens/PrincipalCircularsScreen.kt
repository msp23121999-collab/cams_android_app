package com.example.features.principal.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.core.navigation.AppRoutes
import com.example.core.theme.*
import com.example.core.ui.CamsCard
import com.example.features.principal.widgets.PrincipalBaseScreen

@Composable
fun PrincipalCircularsScreen(onNavigate: (String) -> Unit) {
    var title by remember { mutableStateOf("") }
    var content by remember { mutableStateOf("") }
    var targetAudience by remember { mutableStateOf("All") }
    
    PrincipalBaseScreen(
        title = "Publish Notices & Circulars",
        currentRoute = AppRoutes.PRINCIPAL_CIRCULARS,
        onNavigate = onNavigate,
        floatingActionButton = {
            FloatingActionButton(onClick = { /* Publish */ }, containerColor = CamsNavy, contentColor = Color.White) {
                Icon(Icons.Filled.Send, "Publish")
            }
        }
    ) {
        CamsCard {
            Text("Create New Circular", fontWeight = FontWeight.Bold, fontSize = 18.sp, color = CamsTextPrimary)
            Spacer(modifier = Modifier.height(16.dp))
            OutlinedTextField(
                value = title,
                onValueChange = { title = it },
                modifier = Modifier.fillMaxWidth(),
                label = { Text("Circular Title") },
                singleLine = true
            )
            Spacer(modifier = Modifier.height(12.dp))
            OutlinedTextField(
                value = content,
                onValueChange = { content = it },
                modifier = Modifier.fillMaxWidth().height(120.dp),
                label = { Text("Content") },
                maxLines = 5
            )
            Spacer(modifier = Modifier.height(12.dp))
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                Text("Target Audience:", fontWeight = FontWeight.Medium, color = CamsTextPrimary)
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    FilterChip(selected = targetAudience == "All", onClick = { targetAudience = "All" }, label = { Text("All") })
                    FilterChip(selected = targetAudience == "Faculty", onClick = { targetAudience = "Faculty" }, label = { Text("Faculty") })
                    FilterChip(selected = targetAudience == "Students", onClick = { targetAudience = "Students" }, label = { Text("Students") })
                }
            }
        }
        
        Spacer(modifier = Modifier.height(24.dp))
        Text("Recent Circulars", fontWeight = FontWeight.Bold, fontSize = 18.sp, color = CamsTextPrimary)
        Spacer(modifier = Modifier.height(8.dp))
        
        val circulars = listOf(
            Circular("Holiday Notice - Diwali", "All", "Oct 20, 2024"),
            Circular("Faculty Meeting regarding NAAC", "Faculty", "Oct 18, 2024")
        )
        
        LazyColumn(verticalArrangement = Arrangement.spacedBy(12.dp)) {
            items(circulars) { circular ->
                CamsCard {
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                        Column {
                            Text(circular.title, fontWeight = FontWeight.Bold, fontSize = 16.sp, color = CamsTextPrimary)
                            Spacer(modifier = Modifier.height(4.dp))
                            Row(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalAlignment = Alignment.CenterVertically) {
                                Surface(color = CamsNavy.copy(alpha = 0.1f), shape = MaterialTheme.shapes.small) {
                                    Text(circular.audience, fontSize = 12.sp, color = CamsNavy, modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp))
                                }
                                Text(circular.date, fontSize = 12.sp, color = Color(0xFF64748B))
                            }
                        }
                        IconButton(onClick = { /* Edit/Delete */ }) {
                            Icon(Icons.Filled.MoreVert, null, tint = Color(0xFF64748B))
                        }
                    }
                }
            }
        }
    }
}

data class Circular(val title: String, val audience: String, val date: String)
