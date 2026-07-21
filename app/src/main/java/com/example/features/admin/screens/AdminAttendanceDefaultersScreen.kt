package com.example.features.admin.screens

import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.*
import androidx.compose.ui.platform.LocalContext
import androidx.compose.runtime.*
import androidx.compose.material3.MaterialTheme

import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.features.admin.providers.AdminAttendanceViewModel
import com.example.features.admin.providers.AdminAttendanceViewModelFactory
import com.example.core.repository.AdminRepositoryImpl
import com.example.CamsApplication

import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.core.theme.*
import com.example.core.ui.CamsCard
import com.example.features.admin.widgets.AdminBaseScreen
import com.example.core.navigation.AppRoutes

@Composable
fun AdminAttendanceDefaultersScreen(
    onNavigate: (String) -> Unit,
    viewModel: AdminAttendanceViewModel = viewModel(factory = AdminAttendanceViewModelFactory(AdminRepositoryImpl(CamsApplication.instance.container.apiService)))
) {
    val context = LocalContext.current
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    var studentPendingAdjust by remember { mutableStateOf<com.example.features.admin.models.AdminAttendanceDefaulter?>(null) }

    LaunchedEffect(uiState.actionMessage, uiState.actionError) {
        uiState.actionMessage?.let {
            android.widget.Toast.makeText(context, it, android.widget.Toast.LENGTH_SHORT).show()
            viewModel.clearActionStatus()
        }
        uiState.actionError?.let {
            android.widget.Toast.makeText(context, it, android.widget.Toast.LENGTH_LONG).show()
            viewModel.clearActionStatus()
        }
    }

    AdminBaseScreen(
        title = "Attendance Defaulters",
        currentRoute = AppRoutes.ADMIN_ATTENDANCE_DEFAULTERS,
        onNavigate = onNavigate
    ) {
        Column(
            modifier = Modifier.fillMaxSize().padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                Text("Critical Defaulters (< 75%)", fontWeight = FontWeight.Bold, fontSize = 16.sp, color = MaterialTheme.colorScheme.onSurface)
                Text("${uiState.data.size} student(s)", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }

            uiState.error?.let {
                Text(it, color = Color(0xFFB91C1C), fontSize = 13.sp)
            }

            val defaulters = uiState.data

            if (uiState.isLoading) {
                Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) { CircularProgressIndicator() }
            } else if (defaulters.isEmpty()) {
                Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Text("No attendance defaulters", color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            } else {
                LazyColumn(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    items(defaulters, key = { it.studentId }) { student ->
                        CamsCard {
                            Column(modifier = Modifier.fillMaxWidth().padding(16.dp)) {
                                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                                    Column(Modifier.weight(1f)) {
                                        Text(student.studentName, fontWeight = FontWeight.Bold, fontSize = 15.sp, color = MaterialTheme.colorScheme.onSurface)
                                        Text(
                                            listOfNotNull(
                                                student.rollNo.takeIf { it.isNotBlank() },
                                                student.department.takeIf { it.isNotBlank() },
                                                student.semester.takeIf { it > 0 }?.let { "Sem $it" },
                                                student.section.takeIf { it.isNotBlank() }?.let { "Sec $it" }
                                            ).joinToString(" • "),
                                            fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                    }
                                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                        Icon(Icons.Filled.Warning, null, tint = Color(0xFFEF4444), modifier = Modifier.size(16.dp))
                                        Text("${student.attendancePercentage}%", fontWeight = FontWeight.Bold, color = Color(0xFFEF4444))
                                    }
                                }
                                Spacer(Modifier.height(10.dp))
                                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                    if (student.finePaid) {
                                        Text(
                                            "FINE PAID", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = Color(0xFF047857),
                                            modifier = Modifier.background(Color(0xFFD1FAE5), RoundedCornerShape(4.dp)).padding(horizontal = 6.dp, vertical = 2.dp)
                                        )
                                    } else {
                                        Button(
                                            onClick = { viewModel.markFinePaid(student.studentId) },
                                            enabled = !uiState.isSaving,
                                            colors = ButtonDefaults.buttonColors(containerColor = CamsNavy),
                                            shape = RoundedCornerShape(8.dp)
                                        ) { Text("Mark Fine Paid", fontSize = 12.sp) }
                                    }
                                    Spacer(Modifier.weight(1f))
                                    TextButton(onClick = { studentPendingAdjust = student }, enabled = !uiState.isSaving) {
                                        Text("Adjust %", fontSize = 12.sp)
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    studentPendingAdjust?.let { student ->
        AdjustAttendanceDialog(
            studentName = student.studentName,
            current = student.attendancePercentage,
            onDismiss = { studentPendingAdjust = null },
            onConfirm = { pct ->
                viewModel.adjustAttendance(student.studentId, pct)
                studentPendingAdjust = null
            }
        )
    }
}

@Composable
private fun AdjustAttendanceDialog(
    studentName: String,
    current: Double,
    onDismiss: () -> Unit,
    onConfirm: (Double) -> Unit
) {
    var text by remember { mutableStateOf(current.toString()) }
    val pct = text.toDoubleOrNull()
    val valid = pct != null && pct in 0.0..100.0

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Adjust Attendance") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text(studentName, fontWeight = FontWeight.Bold)
                Text(
                    "Manually override the recorded attendance percentage (e.g. after approved medical leave).",
                    fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                OutlinedTextField(
                    value = text,
                    onValueChange = { text = it.filter { c -> c.isDigit() || c == '.' } },
                    label = { Text("Attendance %") },
                    isError = text.isNotBlank() && !valid,
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
            }
        },
        confirmButton = { TextButton(enabled = valid, onClick = { onConfirm(pct!!) }) { Text("Save") } },
        dismissButton = { TextButton(onClick = onDismiss) { Text("Cancel") } }
    )
}


