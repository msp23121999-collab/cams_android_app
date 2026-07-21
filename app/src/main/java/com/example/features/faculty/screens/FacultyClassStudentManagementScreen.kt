package com.example.features.faculty.screens

import android.content.Intent
import android.net.Uri
import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.*
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
import com.example.core.network.AdvisorStudentDto
import com.example.core.repository.FacultyRepositoryImpl
import com.example.features.faculty.providers.FacultyClassStudentMgmtViewModel
import com.example.features.faculty.providers.FacultyClassStudentMgmtViewModelFactory

@Composable
fun FacultyClassStudentManagementScreen(onNavigate: (String) -> Unit) {
    val context = LocalContext.current
    var searchText by remember { mutableStateOf("") }
    val repository = remember { FacultyRepositoryImpl(com.example.CamsApplication.instance.container.apiService) }
    val factory = remember { FacultyClassStudentMgmtViewModelFactory(repository) }
    val viewModel: FacultyClassStudentMgmtViewModel = viewModel(factory = factory)
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    var studentPendingProfile by remember { mutableStateOf<AdvisorStudentDto?>(null) }

    FacultyBaseScreen(scrollable = false,
        title = "Class Student Management",
        currentRoute = "/faculty/class-student-management",
        onNavigate = onNavigate
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp)
        ) {
            OutlinedTextField(
                value = searchText,
                onValueChange = { searchText = it },
                placeholder = { Text("Search students in your class...") },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                leadingIcon = { Icon(Icons.Filled.Search, null) }
            )

            Spacer(modifier = Modifier.height(20.dp))

            uiState.error?.let {
                Text(it, color = Color(0xFFB91C1C), fontSize = 13.sp, modifier = Modifier.padding(bottom = 8.dp))
            }

            val filtered = uiState.students.filter {
                searchText.isBlank() || it.name.contains(searchText, ignoreCase = true) || it.rollNo.contains(searchText, ignoreCase = true)
            }

            if (uiState.isLoading) {
                Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator(color = CamsNavy)
                }
            } else if (!uiState.isAdvisor) {
                Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Text("You are not assigned as a Class Advisor", color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            } else if (filtered.isEmpty()) {
                Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Text("No students found", color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            } else {
                LazyColumn(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    items(filtered, key = { it.studentId }) { student ->
                        ClassStudentItem(
                            student = student,
                            onViewProfile = { studentPendingProfile = student },
                            onContact = {
                                val phone = student.phone
                                if (phone.isNullOrBlank()) {
                                    Toast.makeText(context, "No phone number on file", Toast.LENGTH_SHORT).show()
                                } else {
                                    try {
                                        context.startActivity(Intent(Intent.ACTION_DIAL, Uri.parse("tel:$phone")))
                                    } catch (e: Exception) {
                                        Toast.makeText(context, "Could not open dialer", Toast.LENGTH_SHORT).show()
                                    }
                                }
                            }
                        )
                    }
                }
            }
        }
    }

    studentPendingProfile?.let { student ->
        AlertDialog(
            onDismissRequest = { studentPendingProfile = null },
            title = { Text(student.name) },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    Text("Roll No: ${student.rollNo}")
                    Text("Department: ${student.department}")
                    Text("Semester: ${student.semester} (${student.yearOfStudy})")
                    Text("Attendance: ${student.attendancePercentage}%")
                    Text("Total Internal Marks: ${student.totalMarks}")
                    Text("Fee Status: ${student.feeStatus}")
                    Text("Last Leave Status: ${student.leaveStatus}")
                }
            },
            confirmButton = {
                TextButton(onClick = { studentPendingProfile = null }) { Text("Close") }
            }
        )
    }
}

@Composable
private fun ClassStudentItem(student: AdvisorStudentDto, onViewProfile: () -> Unit, onContact: () -> Unit) {
    val status = when {
        student.attendancePercentage < 75.0 -> "Low Attendance"
        student.leaveStatus == "PENDING" || student.leaveStatus == "PENDING_HOD" -> "On Leave"
        else -> "Regular"
    }
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
                Row(horizontalArrangement = Arrangement.spacedBy(12.dp), verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(40.dp)
                            .background(CamsNavy.copy(alpha = 0.1f), CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(student.name.take(1), fontWeight = FontWeight.Bold, color = CamsNavy)
                    }
                    Column {
                        Text(student.name, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurface)
                        Text("Roll No: ${student.rollNo}", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                }
                Surface(
                    color = when (status) {
                        "Regular" -> Color(0xFF10B981).copy(alpha = 0.1f)
                        "On Leave" -> Color(0xFF3B82F6).copy(alpha = 0.1f)
                        else -> Color(0xFFEF4444).copy(alpha = 0.1f)
                    },
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Text(
                        status,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = when (status) {
                            "Regular" -> Color(0xFF10B981)
                            "On Leave" -> Color(0xFF3B82F6)
                            else -> Color(0xFFEF4444)
                        }
                    )
                }
            }
            Spacer(modifier = Modifier.height(16.dp))
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                StatItem("Attendance", "${student.attendancePercentage}%")
                StatItem("Internal Marks", "${student.totalMarks}")
            }
            Spacer(modifier = Modifier.height(16.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedButton(
                    onClick = onViewProfile,
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text("View Profile", fontSize = 12.sp)
                }
                Button(
                    onClick = onContact,
                    modifier = Modifier.weight(1f),
                    colors = ButtonDefaults.buttonColors(containerColor = CamsNavy),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text("Contact", fontSize = 12.sp)
                }
            }
        }
    }
}

@Composable
private fun StatItem(label: String, value: String) {
    Column {
        Text(label, fontSize = 13.sp, color = Color(0xFF64748B))
        Text(value, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurface)
    }
}
