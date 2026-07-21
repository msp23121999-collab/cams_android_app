package com.example.features.admin.screens

import android.widget.Toast
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Payment
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.material3.MaterialTheme
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.features.admin.providers.AdminFeesViewModel
import com.example.features.admin.providers.AdminFeesViewModelFactory
import com.example.core.repository.AdminRepositoryImpl
import com.example.CamsApplication
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.core.theme.*
import com.example.core.ui.CamsCard
import com.example.features.admin.models.AdminStudentFeeRecord as StudentFeeRecord
import com.example.features.admin.widgets.AdminBaseScreen
import com.example.core.navigation.AppRoutes

@Composable
fun AdminCollectFeeScreen(
    onNavigate: (String) -> Unit,
    viewModel: AdminFeesViewModel = viewModel(factory = AdminFeesViewModelFactory(AdminRepositoryImpl(CamsApplication.instance.container.apiService)))
) {
    val context = LocalContext.current
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    var searchQuery by remember { mutableStateOf("") }
    var recordPendingCollect by remember { mutableStateOf<StudentFeeRecord?>(null) }

    LaunchedEffect(searchQuery) {
        kotlinx.coroutines.delay(350)
        viewModel.searchStudents(searchQuery)
    }

    LaunchedEffect(uiState.collectSuccess, uiState.collectError) {
        if (uiState.collectSuccess) {
            Toast.makeText(context, "Payment recorded", Toast.LENGTH_SHORT).show()
            recordPendingCollect = null
            viewModel.clearCollectStatus()
        }
        uiState.collectError?.let {
            Toast.makeText(context, it, Toast.LENGTH_LONG).show()
            viewModel.clearCollectStatus()
        }
    }

    AdminBaseScreen(
        title = "Collect Fee",
        subtitle = "Search a student and record a fee payment",
        currentRoute = AppRoutes.ADMIN_COLLECT_FEE,
        onNavigate = onNavigate
    ) {
        Column(
            modifier = Modifier.fillMaxSize().padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            val selected = uiState.selectedStudent
            if (selected == null) {
                OutlinedTextField(
                    value = searchQuery,
                    onValueChange = { searchQuery = it },
                    label = { Text("Search Student by name or roll no") },
                    leadingIcon = { Icon(Icons.Filled.Search, contentDescription = null) },
                    trailingIcon = { if (uiState.isSearching) CircularProgressIndicator(Modifier.size(20.dp), strokeWidth = 2.dp) },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )
                LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    items(uiState.searchResults, key = { it.studentId }) { student ->
                        Row(
                            modifier = Modifier.fillMaxWidth()
                                .background(MaterialTheme.colorScheme.surface, RoundedCornerShape(12.dp))
                                .border(1.dp, MaterialTheme.colorScheme.outlineVariant, RoundedCornerShape(12.dp))
                                .padding(14.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(Modifier.weight(1f)) {
                                Text(student.studentName, fontWeight = FontWeight.Bold, fontSize = 14.sp, color = MaterialTheme.colorScheme.onSurface)
                                Text("${student.rollNo} • ${student.department} • Sem ${student.currentSemester}", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            }
                            TextButton(onClick = { viewModel.selectStudent(student) }) { Text("Select") }
                        }
                    }
                }
            } else {
                CamsCard {
                    Row(Modifier.padding(16.dp).fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                        Column(Modifier.weight(1f)) {
                            Text(selected.studentName, fontWeight = FontWeight.Bold, fontSize = 16.sp, color = MaterialTheme.colorScheme.onSurface)
                            Text("${selected.rollNo} • ${selected.department}", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                        TextButton(onClick = { viewModel.clearSelectedStudent(); searchQuery = "" }) { Text("Change") }
                    }
                }

                Text("Fee Records", fontWeight = FontWeight.Bold, fontSize = 14.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)

                if (uiState.isLoadingRecords) {
                    Box(Modifier.fillMaxWidth().padding(24.dp), contentAlignment = Alignment.Center) { CircularProgressIndicator() }
                } else if (uiState.studentFeeRecords.isEmpty()) {
                    Text("No fee records for this student.", color = MaterialTheme.colorScheme.onSurfaceVariant)
                } else {
                    LazyColumn(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                        items(uiState.studentFeeRecords, key = { it.recordId }) { rec ->
                            val paid = rec.status.equals("paid", ignoreCase = true) || rec.remainingAmount <= 0.0
                            Row(
                                modifier = Modifier.fillMaxWidth()
                                    .background(MaterialTheme.colorScheme.surface, RoundedCornerShape(12.dp))
                                    .border(1.dp, MaterialTheme.colorScheme.outlineVariant, RoundedCornerShape(12.dp))
                                    .padding(14.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column(Modifier.weight(1f)) {
                                    Text("${rec.feeType} • Sem ${rec.semester}", fontWeight = FontWeight.Bold, fontSize = 14.sp, color = MaterialTheme.colorScheme.onSurface)
                                    Text("Paid ₹${rec.paidAmount.toInt()} / ₹${rec.amount.toInt()} • Due ₹${rec.remainingAmount.toInt()}", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                }
                                if (paid) {
                                    Text("PAID", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = Color(0xFF10B981))
                                } else {
                                    Button(onClick = { recordPendingCollect = rec }, colors = ButtonDefaults.buttonColors(containerColor = CamsNavy), shape = RoundedCornerShape(8.dp)) {
                                        Text("Collect", fontSize = 12.sp)
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    recordPendingCollect?.let { rec ->
        CollectPaymentDialog(
            record = rec,
            isSaving = uiState.isCollecting,
            onDismiss = { recordPendingCollect = null },
            onConfirm = { amount, mode -> viewModel.collectFee(rec.recordId, amount, mode) }
        )
    }
}

@Composable
private fun CollectPaymentDialog(
    record: StudentFeeRecord,
    isSaving: Boolean,
    onDismiss: () -> Unit,
    onConfirm: (amount: Double, mode: String) -> Unit
) {
    var amountText by remember { mutableStateOf(record.remainingAmount.toInt().toString()) }
    val modes = listOf("Cash", "Card", "UPI", "Bank Transfer")
    var mode by remember { mutableStateOf(modes[0]) }
    val amount = amountText.toDoubleOrNull() ?: 0.0
    val valid = amount > 0.0 && amount <= record.remainingAmount + 0.01

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Collect ${record.feeType}") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Text("Remaining balance: ₹${record.remainingAmount.toInt()}", fontSize = 13.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                OutlinedTextField(
                    value = amountText,
                    onValueChange = { amountText = it.filter { c -> c.isDigit() } },
                    label = { Text("Amount (₹)") },
                    isError = amountText.isNotBlank() && !valid,
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
                Text("Mode", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurfaceVariant)
                Row(horizontalArrangement = Arrangement.spacedBy(6.dp), modifier = Modifier.fillMaxWidth()) {
                    modes.forEach { m ->
                        FilterChip(selected = mode == m, onClick = { mode = m }, label = { Text(m, fontSize = 11.sp) })
                    }
                }
            }
        },
        confirmButton = {
            TextButton(enabled = valid && !isSaving, onClick = { onConfirm(amount, mode) }) {
                Text(if (isSaving) "Saving..." else "Confirm")
            }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text("Cancel") } }
    )
}
