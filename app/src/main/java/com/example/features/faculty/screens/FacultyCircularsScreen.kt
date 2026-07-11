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

@Composable
fun FacultyCircularsScreen(onNavigate: (String) -> Unit) {
    FacultyBaseScreen(scrollable = false, 
        title = "Circulars & Notices",
        currentRoute = com.example.core.navigation.AppRoutes.FACULTY_CIRCULARS,
        onNavigate = onNavigate
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp)
        ) {
            val circulars = listOf(
                Circular("Revised Academic Calendar", "Academic Cell", "12 Oct 2023", "Important", Color(0xFFEF4444)),
                Circular("Research Grant Guidelines", "R&D Department", "10 Oct 2023", "General", Color(0xFF3B82F6)),
                Circular("Holiday Announcement", "Administration", "08 Oct 2023", "General", Color(0xFF3B82F6)),
                Circular("NAAC Accreditation Visit", "IQAC Cell", "05 Oct 2023", "Urgent", Color(0xFFF59E0B))
            )

            LazyColumn(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                items(circulars) { circular ->
                    CircularItem(circular)
                }
            }
        }
    }
}

@Composable
private fun CircularItem(circular: Circular) {
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
                    color = circular.tagColor.copy(alpha = 0.1f),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Text(
                        circular.tag,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = circular.tagColor
                    )
                }
                Text(circular.date, fontSize = 13.sp, color = Color(0xFF64748B))
            }
            Spacer(modifier = Modifier.height(12.dp))
            Text(circular.title, fontWeight = FontWeight.Bold, fontSize = 16.sp, color = CamsTextPrimary)
            Text("Issued by: ${circular.issuer}", fontSize = 12.sp, color = CamsTextSecondary)
            Spacer(modifier = Modifier.height(16.dp))
            OutlinedButton(
                onClick = { /* View PDF */ },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFE5E7EB))
            ) {
                Icon(Icons.Filled.PictureAsPdf, null, modifier = Modifier.size(18.dp), tint = Color.Red)
                Spacer(modifier = Modifier.width(8.dp))
                Text("View Document", color = CamsTextPrimary)
            }
        }
    }
}

data class Circular(val title: String, val issuer: String, val date: String, val tag: String, val tagColor: Color)
