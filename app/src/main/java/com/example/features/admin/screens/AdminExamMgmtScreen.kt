package com.example.features.admin.screens

import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.CamsApplication
import com.example.core.navigation.AppRoutes
import com.example.core.repository.AdminRepositoryImpl
import com.example.core.theme.*
import com.example.core.ui.CamsCard
import com.example.features.admin.providers.AdminExamViewModel
import com.example.features.admin.providers.AdminExamViewModelFactory
import com.example.features.admin.widgets.AdminBaseScreen

@Composable
fun AdminExamMgmtScreen(
    onNavigate: (String) -> Unit,
    viewModel: AdminExamViewModel = viewModel(
        factory = AdminExamViewModelFactory(AdminRepositoryImpl(CamsApplication.instance.container.apiService))
    )
) {
    val context = LocalContext.current
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    var showGenerateDialog by remember { mutableStateOf(false) }

    LaunchedEffect(uiState.saveSuccess, uiState.saveError) {
        if (uiState.saveSuccess) {
            Toast.makeText(context, "Hall tickets generated", Toast.LENGTH_SHORT).show()
            showGenerateDialog = false
            viewModel.clearSaveStatus()
        }
        uiState.saveError?.let {
            Toast.makeText(context, it, Toast.LENGTH_LONG).show()
            viewModel.clearSaveStatus()
        }
    }

    AdminBaseScreen(
        title = "Exam Management",
        subtitle = "Generate and track student hall tickets",
        currentRoute = AppRoutes.ADMIN_EXAM_MGMT,
        onNavigate = onNavigate
    ) {
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
            Column {
                Text("Hall Tickets", fontWeight = FontWeight.Bold, fontSize = 18.sp, color = MaterialTheme.colorScheme.onSurface)
                Text(
                    "${uiState.hallTickets.size} total • ${uiState.hallTickets.count { it.isIssued }} issued",
                    fontSize = 12.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            Button(onClick = { showGenerateDialog = true }, colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF4F46E5)), shape = RoundedCornerShape(8.dp)) {
                Icon(Icons.Filled.Add, null, modifier = Modifier.size(16.dp))
                Spacer(Modifier.width(4.dp))
                Text("Generate", fontSize = 12.sp, fontWeight = FontWeight.Bold)
            }
        }

        Spacer(Modifier.height(16.dp))

        uiState.error?.let {
            Text(it, color = Color(0xFFB91C1C), fontSize = 13.sp, modifier = Modifier.padding(bottom = 8.dp))
        }

        CamsCard(modifier = Modifier.fillMaxWidth().weight(1f)) {
            if (uiState.isLoading) {
                Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) { CircularProgressIndicator() }
            } else if (uiState.hallTickets.isEmpty()) {
                Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Text("No hall tickets generated yet", color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            } else {
                LazyColumn(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    items(uiState.hallTickets, key = { it.id }) { ticket ->
                        Row(
                            modifier = Modifier.fillMaxWidth()
                                .background(MaterialTheme.colorScheme.surface, RoundedCornerShape(12.dp))
                                .border(1.dp, MaterialTheme.colorScheme.outlineVariant, RoundedCornerShape(12.dp))
                                .padding(16.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(ticket.studentName ?: "Student", fontWeight = FontWeight.Bold, fontSize = 14.sp, color = MaterialTheme.colorScheme.onSurface)
                                Text(ticket.examName, fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                if (!ticket.examDate.isNullOrBlank() || !ticket.examCenter.isNullOrBlank()) {
                                    Text(
                                        listOfNotNull(ticket.examDate?.takeIf { it.isNotBlank() }, ticket.examCenter?.takeIf { it.isNotBlank() }).joinToString(" • "),
                                        fontSize = 11.sp,
                                        color = Color(0xFF64748B)
                                    )
                                }
                            }
                            Column(horizontalAlignment = Alignment.End) {
                                val (label, color, bg) = when {
                                    !ticket.isEligible -> Triple("NOT ELIGIBLE", Color(0xFFBE123C), Color(0xFFFFE4E6))
                                    ticket.isIssued -> Triple("ISSUED", Color(0xFF047857), Color(0xFFD1FAE5))
                                    else -> Triple("PENDING", Color(0xFFB45309), Color(0xFFFEF3C7))
                                }
                                Text(
                                    label,
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = color,
                                    modifier = Modifier.background(bg, RoundedCornerShape(4.dp)).padding(horizontal = 6.dp, vertical = 2.dp)
                                )
                            }
                        }
                    }
                }
            }
        }
    }

    if (showGenerateDialog) {
        GenerateHallTicketsDialog(
            students = uiState.students,
            isSearching = uiState.isSearching,
            isSaving = uiState.isSaving,
            onSearch = { viewModel.searchStudents(it) },
            onDismiss = { showGenerateDialog = false },
            onSubmit = { ids, exam, center, date -> viewModel.generate(ids, exam, center, date) }
        )
    }
}

@Composable
private fun GenerateHallTicketsDialog(
    students: List<com.example.features.admin.models.AdminFeeStudent>,
    isSearching: Boolean,
    isSaving: Boolean,
    onSearch: (String) -> Unit,
    onDismiss: () -> Unit,
    onSubmit: (studentIds: List<String>, examName: String, examCenter: String, examDate: String) -> Unit
) {
    var examName by remember { mutableStateOf("") }
    var examCenter by remember { mutableStateOf("") }
    var examDate by remember { mutableStateOf("") }
    var query by remember { mutableStateOf("") }
    val selected = remember { mutableStateListOf<String>() }

    LaunchedEffect(query) {
        kotlinx.coroutines.delay(350)
        onSearch(query)
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Generate Hall Tickets") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedTextField(value = examName, onValueChange = { examName = it }, label = { Text("Exam Name") }, modifier = Modifier.fillMaxWidth(), singleLine = true)
                OutlinedTextField(value = examCenter, onValueChange = { examCenter = it }, label = { Text("Exam Centre") }, modifier = Modifier.fillMaxWidth(), singleLine = true)
                OutlinedTextField(value = examDate, onValueChange = { examDate = it }, label = { Text("Exam Date (YYYY-MM-DD)") }, modifier = Modifier.fillMaxWidth(), singleLine = true)
                OutlinedTextField(
                    value = query,
                    onValueChange = { query = it },
                    label = { Text("Search students") },
                    trailingIcon = { if (isSearching) CircularProgressIndicator(Modifier.size(18.dp), strokeWidth = 2.dp) },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )
                if (selected.isNotEmpty()) {
                    Text("${selected.size} student(s) selected", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = Color(0xFF4F46E5))
                }
                LazyColumn(modifier = Modifier.heightIn(max = 180.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    items(students, key = { it.studentId }) { s ->
                        Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.fillMaxWidth()) {
                            Checkbox(
                                checked = selected.contains(s.studentId),
                                onCheckedChange = { checked ->
                                    if (checked) selected.add(s.studentId) else selected.remove(s.studentId)
                                }
                            )
                            Column {
                                Text(s.studentName, fontSize = 13.sp, fontWeight = FontWeight.Bold)
                                Text("${s.rollNo} • Sem ${s.currentSemester}", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            }
                        }
                    }
                }
            }
        },
        confirmButton = {
            TextButton(
                enabled = examName.isNotBlank() && selected.isNotEmpty() && !isSaving,
                onClick = { onSubmit(selected.toList(), examName.trim(), examCenter.trim(), examDate.trim()) }
            ) { Text(if (isSaving) "Generating..." else "Generate") }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text("Cancel") } }
    )
}
