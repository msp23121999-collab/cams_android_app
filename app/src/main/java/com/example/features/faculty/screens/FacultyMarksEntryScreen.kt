package com.example.features.faculty.screens

import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.*
import com.example.features.faculty.providers.FacultyStudentsViewModel
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.core.theme.*
import com.example.core.ui.CamsCard
import com.example.features.faculty.widgets.FacultyBaseScreen

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FacultyMarksEntryScreen(
    viewModel: FacultyStudentsViewModel,
    onNavigate: (String) -> Unit
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    var selectedExamType by remember { mutableStateOf("Internal Assessment 1") }
    var selectedSubject by remember { mutableStateOf("Computer Networks (CS8591)") }
    
    val students = state.students.map { 
        StudentMarks(it.rollNo, it.name, "", "50")
    }
    
    var marksList by remember(state.students) { mutableStateOf(students) }

    FacultyBaseScreen(scrollable = false, 
        title = "Marks Entry",
        subtitle = "Enter and manage internal assessment marks",
        currentRoute = "/faculty/marks-entry",
        onNavigate = onNavigate
    ) {
        if (state.isLoading) {
            Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator(color = CamsNavy)
            }
        } else {
            // 1. Config Card
            CamsCard {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text("Assessment Configuration", fontWeight = FontWeight.Bold, fontSize = 16.sp, color = MaterialTheme.colorScheme.onSurface)
                    Spacer(modifier = Modifier.height(16.dp))
                    
                    FilterDropdown("Exam Type", selectedExamType, modifier = Modifier.fillMaxWidth())
                    Spacer(modifier = Modifier.height(12.dp))
                    FilterDropdown("Subject", selectedSubject, modifier = Modifier.fillMaxWidth())
                    
                    Spacer(modifier = Modifier.height(16.dp))
                    
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text("Max Marks", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            Text("50", fontSize = 18.sp, fontWeight = FontWeight.Bold, color = CamsNavy)
                        }
                        Column(horizontalAlignment = Alignment.End) {
                            Text("Passing Marks", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            Text("25", fontSize = 18.sp, fontWeight = FontWeight.Bold, color = Color(0xFFEF4444))
                        }
                    }
                }
            }
            
            Spacer(modifier = Modifier.height(20.dp))
            
            // 2. Marks Entry Header
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 8.dp, vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text("Student Details", modifier = Modifier.weight(1f), fontSize = 13.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurfaceVariant)
                Text("Marks", modifier = Modifier.width(80.dp), fontSize = 13.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurfaceVariant, textAlign = androidx.compose.ui.text.style.TextAlign.Center)
            }
            
            // 3. Student List
            LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.weight(1f)) {
                itemsIndexed(marksList) { index, student ->
                    StudentMarksRow(
                        student = student,
                        onMarksChange = { newValue ->
                            marksList = marksList.toMutableList().apply {
                                this[index] = student.copy(obtained = newValue)
                            }
                        }
                    )
                }
            }
            
            Spacer(modifier = Modifier.height(24.dp))
            
            // 4. Action Buttons
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                OutlinedButton(
                    onClick = { /* Save as Draft */ },
                    modifier = Modifier.weight(1f).height(50.dp),
                    shape = RoundedCornerShape(12.dp),
                    border = androidx.compose.foundation.BorderStroke(1.dp, CamsNavy)
                ) {
                    Text("Save Draft", color = CamsNavy)
                }
                
                Button(
                    onClick = { /* Publish Marks */ },
                    modifier = Modifier.weight(1f).height(50.dp),
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = CamsNavy)
                ) {
                    Text("Publish", fontWeight = FontWeight.Bold)
                }
            }
        }
        
        Spacer(modifier = Modifier.height(40.dp))
    }
}

@Composable
fun StudentMarksRow(
    student: StudentMarks,
    onMarksChange: (String) -> Unit
) {
    Surface(
        color = Color.White,
        shape = RoundedCornerShape(12.dp),
        shadowElevation = 1.dp
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(student.name, fontWeight = FontWeight.Bold, fontSize = 14.sp, color = MaterialTheme.colorScheme.onSurface)
                Text(student.rollNo, fontSize = 13.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
            
            OutlinedTextField(
                value = student.obtained,
                onValueChange = { if (it.length <= 3) onMarksChange(it) },
                modifier = Modifier.width(80.dp),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                singleLine = true,
                textStyle = LocalTextStyle.current.copy(textAlign = androidx.compose.ui.text.style.TextAlign.Center, fontWeight = FontWeight.Bold),
                colors = OutlinedTextFieldDefaults.colors(
                    unfocusedBorderColor = MaterialTheme.colorScheme.outlineVariant,
                    focusedBorderColor = CamsNavy
                ),
                shape = RoundedCornerShape(8.dp)
            )
        }
    }
}

data class StudentMarks(
    val rollNo: String,
    val name: String,
    val obtained: String,
    val max: String
)
