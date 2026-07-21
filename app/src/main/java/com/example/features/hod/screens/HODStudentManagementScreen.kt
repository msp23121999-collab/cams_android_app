package com.example.features.hod.screens

import android.widget.Toast
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import com.example.features.hod.providers.HODStudentViewModel
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.core.navigation.AppRoutes

import com.example.core.network.HODManagementStudentDto
import com.example.core.theme.CamsTextPrimary
import com.example.core.theme.CamsTextSecondary
import com.example.features.hod.widgets.HODBaseScreen

@Composable
fun HODStudentManagementScreen(
    viewModel: HODStudentViewModel,
    onNavigate: (String) -> Unit
) {
    val context = LocalContext.current
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    var searchText by remember { mutableStateOf("") }
    var studentPendingVerify by remember { mutableStateOf<HODManagementStudentDto?>(null) }

    LaunchedEffect(state.verifyError) {
        state.verifyError?.let {
            Toast.makeText(context, it, Toast.LENGTH_SHORT).show()
            viewModel.clearVerifyError()
        }
    }

    val filtered = state.students.filter {
        searchText.isBlank() || it.fullName.contains(searchText, ignoreCase = true) || it.rollNo.contains(searchText, ignoreCase = true)
    }

    HODBaseScreen(
        title = "Student Management",
        subtitle = "Student directory, academic status & verification",
        currentRoute = AppRoutes.HOD_STUDENT_MGMT,
        onNavigate = onNavigate
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.background)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.fillMaxWidth()) {
                KpiCard("Total Students", "${state.metrics.totalStudents}", Icons.Filled.Group, Color(0xFF7C3AED), MaterialTheme.colorScheme.secondaryContainer, Modifier.weight(1f))
                KpiCard("Active Profiles", "${state.metrics.activeStudents}", Icons.Filled.CheckCircle, Color(0xFF059669), Color(0xFFECFDF5), Modifier.weight(1f))
            }
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.fillMaxWidth()) {
                KpiCard("On Leave", "${state.metrics.studentsOnLeave}", Icons.Filled.Pending, Color(0xFFD97706), Color(0xFFFFFBEB), Modifier.weight(1f))
                KpiCard("Avg Attendance", "${state.metrics.averageAttendance}%", Icons.Filled.Warning, Color(0xFFBE123C), Color(0xFFFFF1F2), Modifier.weight(1f))
            }

            state.error?.let {
                Text(it, color = Color(0xFFB91C1C), fontSize = 13.sp)
            }

            Card(
                modifier = Modifier.fillMaxWidth().weight(1f),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)
            ) {
                Column {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(MaterialTheme.colorScheme.background)
                            .border(BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant))
                            .padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(32.dp)
                                .background(Color(0xFFEEF2FF), RoundedCornerShape(8.dp)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.School,
                                contentDescription = null,
                                tint = Color(0xFF4F46E5),
                                modifier = Modifier.size(16.dp)
                            )
                        }
                        Column(modifier = Modifier.weight(1f)) {
                            Text("Student Directory", fontWeight = FontWeight.Bold, fontSize = 14.sp, color = MaterialTheme.colorScheme.onSurface)
                            Text("View and manage student records", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                    }

                    OutlinedTextField(
                        value = searchText,
                        onValueChange = { searchText = it },
                        placeholder = { Text("Search by name or roll no...") },
                        modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 8.dp),
                        shape = RoundedCornerShape(12.dp),
                        leadingIcon = { Icon(Icons.Filled.Search, null) },
                        singleLine = true
                    )

                    if (state.isLoading) {
                        Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                            CircularProgressIndicator(color = Color(0xFF4F46E5))
                        }
                    } else if (filtered.isEmpty()) {
                        Box(Modifier.fillMaxSize().padding(24.dp), contentAlignment = Alignment.Center) {
                            Text("No students found", color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                    } else {
                        LazyColumn(contentPadding = PaddingValues(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                            items(filtered, key = { it.id }) { student ->
                                StudentRow(student, onClick = { studentPendingVerify = student })
                            }
                        }
                    }
                }
            }
        }
    }

    studentPendingVerify?.let { student ->
        VerifyStudentDialog(
            student = student,
            isSaving = state.isVerifying,
            onDismiss = { studentPendingVerify = null },
            onSubmit = { action, remarks ->
                viewModel.verifyStudent(student.id, action, remarks)
                studentPendingVerify = null
            }
        )
    }
}

@Composable
private fun StudentRow(student: HODManagementStudentDto, onClick: () -> Unit) {
    val verificationStatus = student.verificationStatus ?: "DRAFT"
    val (badgeColor, badgeBg) = when (verificationStatus) {
        "VERIFIED_LOCKED" -> Color(0xFF059669) to Color(0xFFECFDF5)
        "REJECTED" -> Color(0xFFB91C1C) to Color(0xFFFFF1F2)
        "UNDER_HOD_VERIFICATION" -> Color(0xFFD97706) to Color(0xFFFFFBEB)
        else -> Color(0xFF6B7280) to Color(0xFFF3F4F6)
    }
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(MaterialTheme.colorScheme.surface, RoundedCornerShape(12.dp))
            .border(1.dp, MaterialTheme.colorScheme.outlineVariant, RoundedCornerShape(12.dp))
            .padding(16.dp)
            .then(Modifier),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier.size(40.dp).background(MaterialTheme.colorScheme.surfaceVariant, CircleShape),
            contentAlignment = Alignment.Center
        ) {
            Text(student.fullName.take(1), fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurfaceVariant, fontSize = 14.sp)
        }
        Spacer(Modifier.width(16.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(student.fullName, fontWeight = FontWeight.Bold, fontSize = 14.sp, color = MaterialTheme.colorScheme.onSurface)
            Spacer(Modifier.height(4.dp))
            Text("Roll No: ${student.rollNo} • Semester ${student.semester}", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
        Column(horizontalAlignment = Alignment.End) {
            Text(
                verificationStatus.replace('_', ' '),
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold,
                color = badgeColor,
                modifier = Modifier.background(badgeBg, RoundedCornerShape(16.dp)).padding(horizontal = 10.dp, vertical = 4.dp)
            )
            Spacer(Modifier.height(8.dp))
            Text("Att: ${student.attendanceRate}%", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant, fontWeight = FontWeight.SemiBold)
            TextButton(onClick = onClick, contentPadding = PaddingValues(0.dp)) {
                Text("Review", fontSize = 12.sp)
            }
        }
    }
}

@Composable
private fun VerifyStudentDialog(
    student: HODManagementStudentDto,
    isSaving: Boolean,
    onDismiss: () -> Unit,
    onSubmit: (action: String, remarks: String?) -> Unit
) {
    var remarks by remember { mutableStateOf(student.hodRemarks ?: "") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(student.fullName) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text("Roll No: ${student.rollNo}")
                Text("Current status: ${student.verificationStatus ?: "DRAFT"}")
                OutlinedTextField(
                    value = remarks,
                    onValueChange = { remarks = it },
                    label = { Text("HOD Remarks") },
                    modifier = Modifier.fillMaxWidth()
                )
            }
        },
        confirmButton = {
            TextButton(
                enabled = !isSaving,
                onClick = { onSubmit("APPROVE", remarks.trim().ifBlank { null }) }
            ) { Text(if (isSaving) "Saving..." else "Approve") }
        },
        dismissButton = {
            TextButton(
                enabled = !isSaving,
                onClick = { onSubmit("REJECT", remarks.trim().ifBlank { null }) }
            ) { Text("Reject", color = Color(0xFFB91C1C)) }
        }
    )
}

@Composable
private fun KpiCard(label: String, value: String, icon: androidx.compose.ui.graphics.vector.ImageVector, color: Color, bgColor: Color, modifier: Modifier) {
    Card(
        modifier = modifier,
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
        shape = RoundedCornerShape(12.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(label, fontSize = 12.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.fillMaxWidth())
            Spacer(Modifier.height(8.dp))
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                Box(Modifier.background(bgColor, RoundedCornerShape(8.dp)).padding(6.dp)) {
                    Icon(icon, null, tint = color, modifier = Modifier.size(20.dp))
                }
                Text(value, fontSize = 24.sp, fontWeight = FontWeight.Black, color = MaterialTheme.colorScheme.onSurface)
            }
        }
    }
}
