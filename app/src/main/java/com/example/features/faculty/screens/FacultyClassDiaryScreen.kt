package com.example.features.faculty.screens

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
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.core.theme.*
import com.example.features.faculty.widgets.FacultyBaseScreen

@Composable
fun FacultyClassDiaryScreen(onNavigate: (String) -> Unit) {
    var topicSelected by remember { mutableStateOf("") }
    var classSelected by remember { mutableStateOf("") }
    var description by remember { mutableStateOf("") }

    FacultyBaseScreen(scrollable = false, 
        title = "Class Diary",
        currentRoute = "/faculty/class-diary",
        onNavigate = onNavigate,
        floatingActionButton = {
            FloatingActionButton(
                onClick = { /* Save Diary Entry */ },
                containerColor = CamsNavy,
                contentColor = Color.White,
                shape = RoundedCornerShape(16.dp)
            ) {
                Icon(Icons.Filled.Save, "Save Entry")
            }
        }
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Text("Add Daily Entry", fontWeight = FontWeight.Bold, fontSize = 18.sp, color = MaterialTheme.colorScheme.onSurface)

            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                shape = RoundedCornerShape(16.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    OutlinedTextField(
                        value = classSelected,
                        onValueChange = { classSelected = it },
                        label = { Text("Select Class") },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp),
                        trailingIcon = { Icon(Icons.Filled.ArrowDropDown, null) }
                    )
                    OutlinedTextField(
                        value = topicSelected,
                        onValueChange = { topicSelected = it },
                        label = { Text("Topic Covered") },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp)
                    )
                    OutlinedTextField(
                        value = description,
                        onValueChange = { description = it },
                        label = { Text("Detailed Description / Homework") },
                        modifier = Modifier.fillMaxWidth().height(120.dp),
                        shape = RoundedCornerShape(12.dp),
                        maxLines = 5
                    )
                }
            }

            Text("Previous Entries", fontWeight = FontWeight.Bold, fontSize = 18.sp, color = MaterialTheme.colorScheme.onSurface)

            val diaryEntries = listOf(
                DiaryEntry("12 Oct 2023", "Introduction to JVM", "CS101 - Semester 1", "Completed Unit 1 basics"),
                DiaryEntry("11 Oct 2023", "Database Normalization", "CS302 - Semester 5", "Homework assigned: 3NF exercises"),
                DiaryEntry("10 Oct 2023", "Kotlin Coroutines", "CS505 - Semester 7", "Live coding session successful")
            )

            LazyColumn(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                items(diaryEntries) { entry ->
                    DiaryItem(entry)
                }
            }
        }
    }
}

@Composable
private fun DiaryItem(entry: DiaryEntry) {
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
                Text(entry.date, fontSize = 12.sp, color = Color(0xFF64748B), fontWeight = FontWeight.Bold)
                Surface(
                    color = CamsNavy.copy(alpha = 0.1f),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Text(
                        entry.className,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = CamsNavy
                    )
                }
            }
            Spacer(modifier = Modifier.height(8.dp))
            Text(entry.topic, fontWeight = FontWeight.Bold, fontSize = 15.sp, color = MaterialTheme.colorScheme.onSurface)
            Text(entry.description, fontSize = 13.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
    }
}

data class DiaryEntry(val date: String, val topic: String, val className: String, val description: String)
