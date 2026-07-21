package com.example.features.faculty.screens

import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.rememberScrollState
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
import com.example.core.network.LeaveRequestDto
import com.example.core.repository.FacultyRepositoryImpl
import com.example.features.faculty.providers.FacultyLeaveApplyViewModel
import com.example.features.faculty.providers.FacultyLeaveApplyViewModelFactory

private val LEAVE_TYPES = listOf("Casual Leave (CL)", "Sick Leave (SL)", "Earned Leave (EL)", "On Duty Leave (OD)")
private val CANCELABLE_STATUSES = setOf("PENDING", "PENDING_HOD", "PENDING_PRINCIPAL", "UNDER_REVIEW", "SUBMITTED")

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FacultyLeaveApplyScreen(onNavigate: (String) -> Unit) {
    val context = LocalContext.current
    val repository = remember { FacultyRepositoryImpl(com.example.CamsApplication.instance.container.apiService) }
    val factory = remember { FacultyLeaveApplyViewModelFactory(repository) }
    val viewModel: FacultyLeaveApplyViewModel = viewModel(factory = factory)
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    var leaveType by remember { mutableStateOf(LEAVE_TYPES[0]) }
    var startDate by remember { mutableStateOf("") }
    var endDate by remember { mutableStateOf("") }
    var reason by remember { mutableStateOf("") }
    var emergencyContact by remember { mutableStateOf("") }
    var typeMenuExpanded by remember { mutableStateOf(false) }

    LaunchedEffect(uiState.submitSuccess, uiState.submitError) {
        if (uiState.submitSuccess) {
            Toast.makeText(context, "Leave application submitted", Toast.LENGTH_SHORT).show()
            startDate = ""; endDate = ""; reason = ""; emergencyContact = ""
            viewModel.clearSubmitStatus()
        }
        uiState.submitError?.let {
            Toast.makeText(context, it, Toast.LENGTH_SHORT).show()
            viewModel.clearSubmitStatus()
        }
    }

    val isValid = startDate.isNotBlank() && endDate.isNotBlank() && reason.isNotBlank() && emergencyContact.isNotBlank()

    FacultyBaseScreen(scrollable = true,
        title = "Apply Leave",
        currentRoute = com.example.core.navigation.AppRoutes.FACULTY_LEAVE_APPLY,
        onNavigate = onNavigate
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            uiState.error?.let {
                Text(it, color = Color(0xFFB91C1C), fontSize = 13.sp)
            }

            if (uiState.isLoading) {
                Box(Modifier.fillMaxWidth().padding(24.dp), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator(color = CamsNavy)
                }
            } else {
                // Leave Balances
                val balances = uiState.balances
                Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    BalanceCard("CL", balances?.casualLeave?.let { "%.0f".format(it) } ?: "-", Color(0xFF3B82F6), Modifier.weight(1f))
                    BalanceCard("SL", balances?.sickLeave?.let { "%.0f".format(it) } ?: "-", Color(0xFF10B981), Modifier.weight(1f))
                    BalanceCard("EL", balances?.earnedLeave?.let { "%.0f".format(it) } ?: "-", Color(0xFFF59E0B), Modifier.weight(1f))
                }

                Text("Request New Leave", fontWeight = FontWeight.Bold, fontSize = 18.sp, color = MaterialTheme.colorScheme.onSurface)

                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                    shape = RoundedCornerShape(16.dp)
                ) {
                    Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                        ExposedDropdownMenuBox(
                            expanded = typeMenuExpanded,
                            onExpandedChange = { typeMenuExpanded = it }
                        ) {
                            OutlinedTextField(
                                value = leaveType,
                                onValueChange = {},
                                readOnly = true,
                                label = { Text("Leave Type") },
                                modifier = Modifier.fillMaxWidth().menuAnchor(),
                                shape = RoundedCornerShape(12.dp),
                                trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = typeMenuExpanded) }
                            )
                            ExposedDropdownMenu(expanded = typeMenuExpanded, onDismissRequest = { typeMenuExpanded = false }) {
                                LEAVE_TYPES.forEach { type ->
                                    DropdownMenuItem(text = { Text(type) }, onClick = {
                                        leaveType = type
                                        typeMenuExpanded = false
                                    })
                                }
                            }
                        }
                        Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                            OutlinedTextField(
                                value = startDate,
                                onValueChange = { startDate = it },
                                label = { Text("Start Date (YYYY-MM-DD)") },
                                modifier = Modifier.weight(1f),
                                shape = RoundedCornerShape(12.dp),
                                trailingIcon = { Icon(Icons.Filled.CalendarToday, null) }
                            )
                            OutlinedTextField(
                                value = endDate,
                                onValueChange = { endDate = it },
                                label = { Text("End Date (YYYY-MM-DD)") },
                                modifier = Modifier.weight(1f),
                                shape = RoundedCornerShape(12.dp),
                                trailingIcon = { Icon(Icons.Filled.CalendarToday, null) }
                            )
                        }
                        OutlinedTextField(
                            value = emergencyContact,
                            onValueChange = { emergencyContact = it },
                            label = { Text("Emergency Contact") },
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(12.dp)
                        )
                        OutlinedTextField(
                            value = reason,
                            onValueChange = { reason = it },
                            label = { Text("Reason for Leave") },
                            modifier = Modifier.fillMaxWidth().height(100.dp),
                            shape = RoundedCornerShape(12.dp),
                            maxLines = 4
                        )
                        Button(
                            onClick = { viewModel.applyForLeave(leaveType, startDate, endDate, reason, emergencyContact) },
                            enabled = isValid && !uiState.isSubmitting,
                            modifier = Modifier.fillMaxWidth(),
                            colors = ButtonDefaults.buttonColors(containerColor = CamsNavy),
                            shape = RoundedCornerShape(12.dp),
                            contentPadding = PaddingValues(16.dp)
                        ) {
                            Text(if (uiState.isSubmitting) "Submitting..." else "Submit Application", fontWeight = FontWeight.Bold)
                        }
                    }
                }

                Text("Leave History", fontWeight = FontWeight.Bold, fontSize = 18.sp, color = MaterialTheme.colorScheme.onSurface)
                if (uiState.history.isEmpty()) {
                    Text("No leave requests yet", color = MaterialTheme.colorScheme.onSurfaceVariant, fontSize = 13.sp)
                } else {
                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        uiState.history.forEach { leave ->
                            LeaveHistoryItem(leave, onCancel = { viewModel.cancelLeave(leave.id) })
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun BalanceCard(label: String, value: String, color: Color, modifier: Modifier = Modifier) {
    Card(
        modifier = modifier,
        colors = CardDefaults.cardColors(containerColor = color.copy(alpha = 0.05f)),
        shape = RoundedCornerShape(16.dp),
        border = androidx.compose.foundation.BorderStroke(1.dp, color.copy(alpha = 0.1f))
    ) {
        Column(
            modifier = Modifier.padding(12.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(value, fontSize = 20.sp, fontWeight = FontWeight.Black, color = color)
            Text(label, fontSize = 12.sp, fontWeight = FontWeight.Bold, color = color.copy(alpha = 0.7f))
        }
    }
}

@Composable
private fun LeaveHistoryItem(leave: LeaveRequestDto, onCancel: () -> Unit) {
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
                Column {
                    Text(leave.type, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurface)
                    Text("${leave.startDate} to ${leave.endDate}", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
                Surface(
                    color = when (leave.status) {
                        "APPROVED", "FINAL_APPROVED" -> Color(0xFF10B981).copy(alpha = 0.1f)
                        "REJECTED", "REJECTED_BY_HOD", "REJECTED_BY_PRINCIPAL", "CANCELLED" -> Color(0xFFB91C1C).copy(alpha = 0.1f)
                        else -> Color(0xFFF59E0B).copy(alpha = 0.1f)
                    },
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Text(
                        leave.status.replace('_', ' '),
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = when (leave.status) {
                            "APPROVED", "FINAL_APPROVED" -> Color(0xFF10B981)
                            "REJECTED", "REJECTED_BY_HOD", "REJECTED_BY_PRINCIPAL", "CANCELLED" -> Color(0xFFB91C1C)
                            else -> Color(0xFFF59E0B)
                        }
                    )
                }
            }
            if (leave.status in CANCELABLE_STATUSES) {
                Spacer(modifier = Modifier.height(8.dp))
                TextButton(onClick = onCancel) {
                    Text("Cancel Request", color = Color(0xFFB91C1C))
                }
            }
        }
    }
}
