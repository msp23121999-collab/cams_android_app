package com.example.features.hod.screens

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
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.core.theme.*
import com.example.core.ui.CamsCard
import com.example.features.hod.widgets.HODBaseScreen

@Composable
fun HODCircularsScreen(onNavigate: (String) -> Unit) {
    HODBaseScreen(
        title = "Department Circulars",
        currentRoute = "/hod/circulars",
        onNavigate = onNavigate,
        floatingActionButton = {
            FloatingActionButton(onClick = {}, containerColor = CamsNavy, contentColor = Color.White) {
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
            val circulars = listOf(
                Circular("NAAC Documentation", "Urgent: Complete department criteria by Oct 20.", "15 Oct 2023"),
                Circular("Faculty Meeting", "Meeting regarding semester exams schedule.", "12 Oct 2023"),
                Circular("Research Workshop", "Workshop on Research Methodology for all faculty.", "10 Oct 2023")
            )

            LazyColumn(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                items(circulars) { circular ->
                    CamsCard {
                        Row(horizontalArrangement = Arrangement.spacedBy(16.dp), verticalAlignment = Alignment.CenterVertically) {
                            Box(Modifier.size(40.dp).background(CamsNavy.copy(alpha = 0.1f), CircleShape), contentAlignment = Alignment.Center) {
                                Icon(Icons.Filled.Announcement, null, tint = CamsNavy, modifier = Modifier.size(20.dp))
                            }
                            Column {
                                Text(circular.title, fontWeight = FontWeight.Bold, fontSize = 15.sp, color = CamsTextPrimary)
                                Text(circular.content, fontSize = 13.sp, color = CamsTextSecondary)
                                Text(circular.date, fontSize = 13.sp, color = Color(0xFF64748B), fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                }
            }
        }
    }
}

data class Circular(val title: String, val content: String, val date: String)
