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
import com.example.features.faculty.providers.FacultyStudentsViewModel
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.core.theme.*
import com.example.core.ui.CamsCard
import com.example.core.ui.CamsScreen

@Composable
fun FacultyStudentDirectoryScreen(
    viewModel: FacultyStudentsViewModel,
    onNavigate: (String) -> Unit
) {
    val state by viewModel.uiState.collectAsState()
    var searchQuery by remember { mutableStateOf("") }
    
    val filteredStudents = state.students.filter { 
        it.name.contains(searchQuery, ignoreCase = true) || it.rollNo.contains(searchQuery, ignoreCase = true)
    }

    com.example.features.faculty.widgets.FacultyBaseScreen(scrollable = false, 
        title = "Student Directory",
        subtitle = "Search and view student profiles",
        currentRoute = "/faculty/students",
        onNavigate = onNavigate
    ) {
        if (state.isLoading) {
            Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator(color = CamsNavy)
            }
        } else {
            // Search Bar
            OutlinedTextField(
                value = searchQuery,
                onValueChange = { searchQuery = it },
                modifier = Modifier.fillMaxWidth(),
                placeholder = { Text("Search by name or roll number...", fontSize = 14.sp) },
                leadingIcon = { Icon(Icons.Filled.Search, null, tint = CamsTextSecondary) },
                shape = RoundedCornerShape(12.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedContainerColor = Color.White,
                    unfocusedContainerColor = Color.White,
                    focusedBorderColor = CamsNavy,
                    unfocusedBorderColor = Color.LightGray
                ),
                singleLine = true
            )

            // Filters Placeholder
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                DirectoryFilterChip("Semester: VI")
            }

            // Student List
            LazyColumn(
                verticalArrangement = Arrangement.spacedBy(12.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                items(filteredStudents) { student ->
                    StudentCard(student)
                }
            }
        }
    }
}

@Composable
private fun DirectoryFilterChip(label: String) {
    Surface(
        color = Color(0xFFF3F4F6),
        shape = RoundedCornerShape(8.dp)
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
            horizontalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            Text(label, fontSize = 13.sp, fontWeight = FontWeight.Bold, color = CamsTextSecondary)
            Icon(Icons.Filled.ArrowDropDown, null, modifier = Modifier.size(14.dp), tint = CamsTextSecondary)
        }
    }
}

@Composable
private fun StudentCard(student: com.example.core.network.FacultyStudentDto) {
    CamsCard {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(50.dp)
                    .clip(CircleShape)
                    .background(CamsNavy.copy(alpha = 0.1f)),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    student.name.take(1),
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                    color = CamsNavy
                )
            }
            
            Column(modifier = Modifier.weight(1f)) {
                Text(student.name, fontWeight = FontWeight.Bold, fontSize = 15.sp, color = CamsTextPrimary)
                Text("Roll No: ${student.rollNo}", fontSize = 13.sp, color = CamsTextSecondary)
            }
            
            Column(horizontalAlignment = Alignment.End) {
                Surface(color = Color(0xFFEEF2FF), shape = RoundedCornerShape(4.dp)) {
                    Text(
                        "${student.batch}",
                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Black,
                        color = Color(0xFF4338CA)
                    )
                }
                Text("Semester ${student.semester}", fontSize = 12.sp, color = Color(0xFF64748B))
            }
        }
    }
}
