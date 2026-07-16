package com.example.features.faculty.screens

import androidx.lifecycle.compose.collectAsStateWithLifecycle
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
import com.example.core.ui.CamsCard
import com.example.features.faculty.widgets.FacultyBaseScreen

import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.features.faculty.providers.FacultyAttendanceViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FacultyAttendanceScreen(
    onNavigate: (String) -> Unit,
    viewModel: FacultyAttendanceViewModel
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    var selectedSubject by remember { mutableStateOf<com.example.features.faculty.models.FacultySubject?>(null) }
    var selectedDegree by remember { mutableStateOf("B.Tech CSE") }
    var selectedBatch by remember { mutableStateOf("2021-2025") }
    
    LaunchedEffect(uiState.subjects) {
        if (selectedSubject == null && uiState.subjects.isNotEmpty()) {
            selectedSubject = uiState.subjects.first()
        }
    }
    
    val students = uiState.students.map { 
        AttendanceStudent(it.rollNo, it.name, "P") 
    }
    
    var attendanceList by remember(uiState.students) { mutableStateOf(students) }

    FacultyBaseScreen(scrollable = false, 
        title = "Attendance Entry",
        subtitle = "Mark daily student attendance",
        currentRoute = "/faculty/attendance",
        onNavigate = onNavigate
    ) {
        // 1. Filter Section
        CamsCard {
            Column(modifier = Modifier.padding(16.dp)) {
                Text(
                    "Class Selection",
                    fontWeight = FontWeight.Bold,
                    fontSize = 16.sp,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Spacer(modifier = Modifier.height(16.dp))
                
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    FilterDropdown("Degree", selectedDegree, modifier = Modifier.weight(1f))
                    FilterDropdown("Batch", selectedBatch, modifier = Modifier.weight(1f))
                }
                
                Spacer(modifier = Modifier.height(12.dp))
                
                FilterDropdown("Subject", selectedSubject?.subjectName ?: "Select Subject", modifier = Modifier.fillMaxWidth())
            }
        }
        
        Spacer(modifier = Modifier.height(16.dp))
        
        // 2. Statistics Summary
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            StatItem("Total", "${attendanceList.size}", CamsNavy, modifier = Modifier.weight(1f))
            StatItem("Present", "${attendanceList.count { it.status == "P" }}", Color(0xFF10B981), modifier = Modifier.weight(1f))
            StatItem("Absent", "${attendanceList.count { it.status == "A" }}", Color(0xFFEF4444), modifier = Modifier.weight(1f))
        }
        
        Spacer(modifier = Modifier.height(16.dp))
        
        // 3. Student List
        Text(
            "Student List",
            fontWeight = FontWeight.Bold,
            fontSize = 18.sp,
            color = MaterialTheme.colorScheme.onSurface,
            modifier = Modifier.padding(horizontal = 4.dp, vertical = 8.dp)
        )
        
        LazyColumn(
            modifier = Modifier.heightIn(max = 1000.dp), // Height adjustment for scrolling within the base screen Column
            verticalArrangement = Arrangement.spacedBy(12.dp),
            contentPadding = PaddingValues(bottom = 80.dp)
        ) {
            items(attendanceList) { student ->
                StudentAttendanceCard(
                    student = student,
                    onStatusChange = { newStatus ->
                        attendanceList = attendanceList.map {
                            if (it.rollNo == student.rollNo) it.copy(status = newStatus) else it
                        }
                    }
                )
            }
        }
        
        // Submit Button (Fixed at bottom potentially, or just at the end of list)
        Button(
            onClick = { /* Submit attendance */ },
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 16.dp)
                .height(56.dp),
            shape = RoundedCornerShape(12.dp),
            colors = ButtonDefaults.buttonColors(containerColor = CamsNavy)
        ) {
            Text("Submit Attendance", fontSize = 16.sp, fontWeight = FontWeight.Bold)
        }
    }
}

@Composable
fun FilterDropdown(label: String, value: String, modifier: Modifier = Modifier) {
    Column(modifier = modifier) {
        Text(label, fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.padding(start = 4.dp, bottom = 4.dp))
        Surface(
            modifier = Modifier.fillMaxWidth(),
            color = MaterialTheme.colorScheme.surfaceVariant,
            shape = RoundedCornerShape(8.dp),
            border = null
        ) {
            Row(
                modifier = Modifier.padding(horizontal = 12.dp, vertical = 10.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(value, fontSize = 14.sp, color = MaterialTheme.colorScheme.onSurface)
                Icon(Icons.Filled.ArrowDropDown, null, tint = MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.size(20.dp))
            }
        }
    }
}

@Composable
fun StatItem(label: String, value: String, color: Color, modifier: Modifier = Modifier) {
    Surface(
        modifier = modifier,
        color = Color.White,
        shape = RoundedCornerShape(12.dp),
        shadowElevation = 2.dp
    ) {
        Column(
            modifier = Modifier.padding(12.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(label, fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
            Text(value, fontSize = 20.sp, fontWeight = FontWeight.Bold, color = color)
        }
    }
}

@Composable
fun StudentAttendanceCard(
    student: AttendanceStudent,
    onStatusChange: (String) -> Unit
) {
    CamsCard {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(student.name, fontWeight = FontWeight.Bold, fontSize = 16.sp, color = MaterialTheme.colorScheme.onSurface)
                Text(student.rollNo, fontSize = 13.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
            
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                StatusToggle("P", student.status == "P", Color(0xFF10B981)) { onStatusChange("P") }
                StatusToggle("A", student.status == "A", Color(0xFFEF4444)) { onStatusChange("A") }
                StatusToggle("OD", student.status == "OD", Color(0xFF3B82F6)) { onStatusChange("OD") }
            }
        }
    }
}

@Composable
fun StatusToggle(
    label: String,
    isSelected: Boolean,
    color: Color,
    onClick: () -> Unit
) {
    Surface(
        modifier = Modifier
            .size(40.dp)
            .background(Color.Transparent),
        onClick = onClick,
        shape = CircleShape,
        color = if (isSelected) color else MaterialTheme.colorScheme.surfaceVariant,
        contentColor = if (isSelected) Color.White else CamsTextSecondary
    ) {
        Box(contentAlignment = Alignment.Center) {
            Text(label, fontSize = 12.sp, fontWeight = FontWeight.Bold)
        }
    }
}

data class AttendanceStudent(
    val rollNo: String,
    val name: String,
    val status: String // P, A, OD
)
