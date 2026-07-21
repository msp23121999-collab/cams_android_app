package com.example.features.principal.screens

import android.widget.Toast
import androidx.compose.foundation.background
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
import com.example.core.network.BudgetExpenseCreateRequest
import com.example.core.network.BudgetLineItemCreateRequest
import com.example.core.network.BudgetLineItemDto
import com.example.core.network.GrantCreateRequest
import com.example.core.network.GrantDto
import com.example.core.repository.BudgetRepositoryImpl
import com.example.core.theme.*
import com.example.core.ui.CamsCard
import com.example.features.principal.providers.PrincipalBudgetViewModel
import com.example.features.principal.providers.PrincipalBudgetViewModelFactory
import com.example.features.principal.widgets.PrincipalBaseScreen
import java.text.NumberFormat
import java.util.Locale

private fun formatMoney(amount: Double): String {
    val fmt = NumberFormat.getCurrencyInstance(Locale("en", "IN"))
    fmt.maximumFractionDigits = 0
    return fmt.format(amount)
}

@Composable
fun PrincipalBudgetGrantsScreen(
    onNavigate: (String) -> Unit,
    viewModel: PrincipalBudgetViewModel = viewModel(
        factory = PrincipalBudgetViewModelFactory(BudgetRepositoryImpl(CamsApplication.instance.container.apiService))
    )
) {
    val context = LocalContext.current
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    var tab by remember { mutableStateOf(0) }
    var showCreateLineItemDialog by remember { mutableStateOf(false) }
    var showCreateGrantDialog by remember { mutableStateOf(false) }
    var itemForExpense by remember { mutableStateOf<BudgetLineItemDto?>(null) }
    var itemPendingDelete by remember { mutableStateOf<BudgetLineItemDto?>(null) }
    var grantPendingDelete by remember { mutableStateOf<GrantDto?>(null) }

    LaunchedEffect(uiState.saveSuccess, uiState.saveError) {
        if (uiState.saveSuccess) {
            Toast.makeText(context, "Saved", Toast.LENGTH_SHORT).show()
            showCreateLineItemDialog = false
            showCreateGrantDialog = false
            itemForExpense = null
            viewModel.clearSaveStatus()
        }
        uiState.saveError?.let {
            Toast.makeText(context, it, Toast.LENGTH_LONG).show()
            viewModel.clearSaveStatus()
        }
    }

    PrincipalBaseScreen(
        title = "Budget & Grants",
        subtitle = "Track institutional budget allocations, expenses, and external research grants.",
        currentRoute = AppRoutes.PRINCIPAL_BUDGET_GRANTS,
        onNavigate = onNavigate,
        floatingActionButton = {
            FloatingActionButton(
                onClick = { if (tab == 0) showCreateLineItemDialog = true else showCreateGrantDialog = true },
                containerColor = CamsNavy, contentColor = Color.White
            ) {
                Icon(Icons.Filled.Add, "Add")
            }
        }
    ) {
        val summary = uiState.summary
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.fillMaxWidth()) {
            KpiCard("Allocated", formatMoney(summary?.totalAllocated ?: 0.0), Icons.Filled.AccountBalance, Color(0xFF3B82F6), Modifier.weight(1f))
            KpiCard("Spent", formatMoney(summary?.totalSpent ?: 0.0), Icons.Filled.TrendingDown, Color(0xFFEF4444), Modifier.weight(1f))
            KpiCard("Remaining", formatMoney(summary?.totalRemaining ?: 0.0), Icons.Filled.Savings, Color(0xFF10B981), Modifier.weight(1f))
            KpiCard("Active Grants", "${summary?.activeGrantsCount ?: 0}", Icons.Filled.Handshake, Color(0xFF8B5CF6), Modifier.weight(1f))
        }

        Spacer(Modifier.height(16.dp))

        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            FilterChip(selected = tab == 0, onClick = { tab = 0 }, label = { Text("Budget Line Items") })
            FilterChip(selected = tab == 1, onClick = { tab = 1 }, label = { Text("Grants") })
        }

        Spacer(Modifier.height(12.dp))

        uiState.error?.let { Text(it, color = Color(0xFFB91C1C), fontSize = 13.sp, modifier = Modifier.padding(bottom = 8.dp)) }

        if (uiState.isLoading) {
            Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) { CircularProgressIndicator(color = CamsNavy) }
        } else if (tab == 0) {
            if (uiState.lineItems.isEmpty()) {
                Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) { Text("No budget line items yet", color = MaterialTheme.colorScheme.onSurfaceVariant) }
            } else {
                LazyColumn(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    items(uiState.lineItems, key = { it.id }) { item ->
                        LineItemCard(item, onRecordExpense = { itemForExpense = item }, onDelete = { itemPendingDelete = item })
                    }
                }
            }
        } else {
            if (uiState.grants.isEmpty()) {
                Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) { Text("No grants recorded yet", color = MaterialTheme.colorScheme.onSurfaceVariant) }
            } else {
                LazyColumn(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    items(uiState.grants, key = { it.id }) { grant ->
                        GrantCard(
                            grant,
                            onAdvanceStatus = { newStatus -> viewModel.updateGrantStatus(grant.id, newStatus) },
                            onDelete = { grantPendingDelete = grant }
                        )
                    }
                }
            }
        }
    }

    if (showCreateLineItemDialog) {
        CreateLineItemDialog(
            isSaving = uiState.isSaving,
            onDismiss = { showCreateLineItemDialog = false },
            onSubmit = { viewModel.createLineItem(it) }
        )
    }

    if (showCreateGrantDialog) {
        CreateGrantDialog(
            isSaving = uiState.isSaving,
            onDismiss = { showCreateGrantDialog = false },
            onSubmit = { viewModel.createGrant(it) }
        )
    }

    itemForExpense?.let { item ->
        RecordExpenseDialog(
            item = item,
            isSaving = uiState.isSaving,
            onDismiss = { itemForExpense = null },
            onSubmit = { viewModel.recordExpense(item.id, it) }
        )
    }

    itemPendingDelete?.let { item ->
        AlertDialog(
            onDismissRequest = { itemPendingDelete = null },
            title = { Text("Delete Budget Line Item") },
            text = { Text("Delete \"${item.title}\"? This cannot be undone.") },
            confirmButton = {
                TextButton(onClick = { viewModel.deleteLineItem(item.id); itemPendingDelete = null }) { Text("Delete", color = Color(0xFFB91C1C)) }
            },
            dismissButton = { TextButton(onClick = { itemPendingDelete = null }) { Text("Cancel") } }
        )
    }

    grantPendingDelete?.let { grant ->
        AlertDialog(
            onDismissRequest = { grantPendingDelete = null },
            title = { Text("Delete Grant") },
            text = { Text("Delete \"${grant.title}\"? This cannot be undone.") },
            confirmButton = {
                TextButton(onClick = { viewModel.deleteGrant(grant.id); grantPendingDelete = null }) { Text("Delete", color = Color(0xFFB91C1C)) }
            },
            dismissButton = { TextButton(onClick = { grantPendingDelete = null }) { Text("Cancel") } }
        )
    }
}

@Composable
private fun LineItemCard(item: BudgetLineItemDto, onRecordExpense: () -> Unit, onDelete: () -> Unit) {
    CamsCard {
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.Top) {
            Column(Modifier.weight(1f)) {
                Text(item.title, fontWeight = FontWeight.Bold, fontSize = 15.sp, color = MaterialTheme.colorScheme.onSurface)
                Text("${item.category} • FY ${item.fiscalYear} • ${item.departmentName ?: "Institution-Wide"}", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
            Text(item.status, fontSize = 11.sp, fontWeight = FontWeight.Bold, color = Color(0xFF10B981), modifier = Modifier.background(Color(0xFFD1FAE5), RoundedCornerShape(4.dp)).padding(horizontal = 6.dp, vertical = 2.dp))
        }
        Spacer(Modifier.height(12.dp))
        LinearProgressIndicator(
            progress = { if (item.allocatedAmount > 0) (item.spentAmount / item.allocatedAmount).toFloat().coerceIn(0f, 1f) else 0f },
            modifier = Modifier.fillMaxWidth(),
            color = if (item.remainingAmount < 0) Color(0xFFEF4444) else CamsNavy
        )
        Spacer(Modifier.height(8.dp))
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
            Text("Spent: ${formatMoney(item.spentAmount)}", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
            Text("Allocated: ${formatMoney(item.allocatedAmount)}", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurface)
        }
        Spacer(Modifier.height(12.dp))
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            Button(onClick = onRecordExpense, colors = ButtonDefaults.buttonColors(containerColor = CamsNavy)) { Text("Record Expense") }
            OutlinedButton(onClick = onDelete, colors = ButtonDefaults.outlinedButtonColors(contentColor = Color(0xFFB91C1C))) { Text("Delete") }
        }
    }
}

@Composable
private fun GrantCard(grant: GrantDto, onAdvanceStatus: (String) -> Unit, onDelete: () -> Unit) {
    CamsCard {
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.Top) {
            Column(Modifier.weight(1f)) {
                Text(grant.title, fontWeight = FontWeight.Bold, fontSize = 15.sp, color = MaterialTheme.colorScheme.onSurface)
                Text("${grant.fundingAgency} • ${grant.departmentName ?: "Institution-Wide"}", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                grant.principalInvestigator?.let { Text("PI: $it", fontSize = 12.sp, color = Color(0xFF64748B)) }
            }
            val (bg, fg) = when (grant.status) {
                "DISBURSED" -> Color(0xFFD1FAE5) to Color(0xFF047857)
                "APPROVED" -> Color(0xFFDBEAFE) to Color(0xFF1D4ED8)
                "COMPLETED" -> Color(0xFFE5E7EB) to Color(0xFF374151)
                "REJECTED" -> Color(0xFFFFE4E6) to Color(0xFFB91C1C)
                else -> Color(0xFFFEF3C7) to Color(0xFFB45309)
            }
            Text(grant.status, fontSize = 11.sp, fontWeight = FontWeight.Bold, color = fg, modifier = Modifier.background(bg, RoundedCornerShape(4.dp)).padding(horizontal = 6.dp, vertical = 2.dp))
        }
        Spacer(Modifier.height(8.dp))
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
            Text("Disbursed: ${formatMoney(grant.disbursedAmount)}", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
            Text("Sanctioned: ${formatMoney(grant.sanctionedAmount)}", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurface)
        }
        Spacer(Modifier.height(12.dp))
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            when (grant.status) {
                "PROPOSED" -> Button(onClick = { onAdvanceStatus("APPROVED") }, colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF10B981))) { Text("Approve") }
                "APPROVED" -> Button(onClick = { onAdvanceStatus("DISBURSED") }, colors = ButtonDefaults.buttonColors(containerColor = CamsNavy)) { Text("Mark Disbursed") }
                "DISBURSED" -> Button(onClick = { onAdvanceStatus("COMPLETED") }, colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF64748B))) { Text("Mark Completed") }
            }
            OutlinedButton(onClick = onDelete, colors = ButtonDefaults.outlinedButtonColors(contentColor = Color(0xFFB91C1C))) { Text("Delete") }
        }
    }
}

@Composable
private fun CreateLineItemDialog(isSaving: Boolean, onDismiss: () -> Unit, onSubmit: (BudgetLineItemCreateRequest) -> Unit) {
    var fiscalYear by remember { mutableStateOf("2026-2027") }
    var title by remember { mutableStateOf("") }
    var category by remember { mutableStateOf("General") }
    var allocated by remember { mutableStateOf("") }
    val allocatedAmount = allocated.toDoubleOrNull() ?: 0.0

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Create Budget Line Item") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedTextField(value = title, onValueChange = { title = it }, label = { Text("Title") }, modifier = Modifier.fillMaxWidth(), singleLine = true)
                OutlinedTextField(value = category, onValueChange = { category = it }, label = { Text("Category") }, modifier = Modifier.fillMaxWidth(), singleLine = true)
                OutlinedTextField(value = fiscalYear, onValueChange = { fiscalYear = it }, label = { Text("Fiscal Year") }, modifier = Modifier.fillMaxWidth(), singleLine = true)
                OutlinedTextField(value = allocated, onValueChange = { allocated = it.filter { c -> c.isDigit() || c == '.' } }, label = { Text("Allocated Amount (INR)") }, modifier = Modifier.fillMaxWidth(), singleLine = true)
            }
        },
        confirmButton = {
            TextButton(
                enabled = title.isNotBlank() && allocatedAmount > 0 && !isSaving,
                onClick = { onSubmit(BudgetLineItemCreateRequest(fiscalYear.trim(), title.trim(), category.trim().ifBlank { "General" }, null, allocatedAmount, null)) }
            ) { Text(if (isSaving) "Saving..." else "Create") }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text("Cancel") } }
    )
}

@Composable
private fun RecordExpenseDialog(item: BudgetLineItemDto, isSaving: Boolean, onDismiss: () -> Unit, onSubmit: (BudgetExpenseCreateRequest) -> Unit) {
    var description by remember { mutableStateOf("") }
    var amount by remember { mutableStateOf("") }
    var date by remember { mutableStateOf("") }
    val amountValue = amount.toDoubleOrNull() ?: 0.0

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Record Expense — ${item.title}") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text("Remaining budget: ${formatMoney(item.remainingAmount)}", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                OutlinedTextField(value = description, onValueChange = { description = it }, label = { Text("Description") }, modifier = Modifier.fillMaxWidth())
                OutlinedTextField(value = amount, onValueChange = { amount = it.filter { c -> c.isDigit() || c == '.' } }, label = { Text("Amount (INR)") }, modifier = Modifier.fillMaxWidth(), singleLine = true)
                OutlinedTextField(value = date, onValueChange = { date = it }, label = { Text("Date (YYYY-MM-DD)") }, modifier = Modifier.fillMaxWidth(), singleLine = true)
            }
        },
        confirmButton = {
            TextButton(
                enabled = description.isNotBlank() && amountValue > 0 && date.isNotBlank() && !isSaving,
                onClick = { onSubmit(BudgetExpenseCreateRequest(description.trim(), amountValue, date.trim())) }
            ) { Text(if (isSaving) "Saving..." else "Record") }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text("Cancel") } }
    )
}

@Composable
private fun CreateGrantDialog(isSaving: Boolean, onDismiss: () -> Unit, onSubmit: (GrantCreateRequest) -> Unit) {
    var title by remember { mutableStateOf("") }
    var agency by remember { mutableStateOf("") }
    var pi by remember { mutableStateOf("") }
    var sanctioned by remember { mutableStateOf("") }
    var startDate by remember { mutableStateOf("") }
    var endDate by remember { mutableStateOf("") }
    val sanctionedAmount = sanctioned.toDoubleOrNull() ?: 0.0

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Add Grant") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedTextField(value = title, onValueChange = { title = it }, label = { Text("Grant Title") }, modifier = Modifier.fillMaxWidth(), singleLine = true)
                OutlinedTextField(value = agency, onValueChange = { agency = it }, label = { Text("Funding Agency") }, modifier = Modifier.fillMaxWidth(), singleLine = true)
                OutlinedTextField(value = pi, onValueChange = { pi = it }, label = { Text("Principal Investigator") }, modifier = Modifier.fillMaxWidth(), singleLine = true)
                OutlinedTextField(value = sanctioned, onValueChange = { sanctioned = it.filter { c -> c.isDigit() || c == '.' } }, label = { Text("Sanctioned Amount (INR)") }, modifier = Modifier.fillMaxWidth(), singleLine = true)
                OutlinedTextField(value = startDate, onValueChange = { startDate = it }, label = { Text("Start Date (YYYY-MM-DD)") }, modifier = Modifier.fillMaxWidth(), singleLine = true)
                OutlinedTextField(value = endDate, onValueChange = { endDate = it }, label = { Text("End Date (YYYY-MM-DD)") }, modifier = Modifier.fillMaxWidth(), singleLine = true)
            }
        },
        confirmButton = {
            TextButton(
                enabled = title.isNotBlank() && agency.isNotBlank() && sanctionedAmount > 0 && !isSaving,
                onClick = {
                    onSubmit(
                        GrantCreateRequest(
                            title = title.trim(), fundingAgency = agency.trim(), departmentId = null,
                            principalInvestigator = pi.trim().ifBlank { null }, sanctionedAmount = sanctionedAmount,
                            startDate = startDate.trim().ifBlank { null }, endDate = endDate.trim().ifBlank { null }, notes = null
                        )
                    )
                }
            ) { Text(if (isSaving) "Saving..." else "Add Grant") }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text("Cancel") } }
    )
}

@Composable
private fun KpiCard(label: String, value: String, icon: androidx.compose.ui.graphics.vector.ImageVector, color: Color, modifier: Modifier) {
    Card(modifier = modifier, colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface), elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)) {
        Column(modifier = Modifier.padding(12.dp)) {
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.Top) {
                Text(label, fontSize = 11.sp, fontWeight = FontWeight.Black, color = Color(0xFF64748B), modifier = Modifier.weight(1f), maxLines = 1)
                Box(Modifier.background(color.copy(alpha = 0.1f), RoundedCornerShape(8.dp)).padding(4.dp)) {
                    Icon(icon, null, tint = color, modifier = Modifier.size(16.dp))
                }
            }
            Text(value, fontSize = 16.sp, fontWeight = FontWeight.Black, color = MaterialTheme.colorScheme.onSurface, modifier = Modifier.padding(top = 8.dp), maxLines = 1)
        }
    }
}
