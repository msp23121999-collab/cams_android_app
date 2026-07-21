package com.example.features.admin.screens

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
import com.example.core.network.InventoryItemDto
import com.example.core.repository.InventoryRepositoryImpl
import com.example.core.theme.*
import com.example.core.ui.CamsCard
import com.example.features.admin.providers.AdminInventoryViewModel2
import com.example.features.admin.providers.AdminInventoryViewModel2Factory
import com.example.features.admin.widgets.AdminBaseScreen

@Composable
fun AdminInventoryScreen(
    onNavigate: (String) -> Unit,
    viewModel: AdminInventoryViewModel2 = viewModel(
        factory = AdminInventoryViewModel2Factory(InventoryRepositoryImpl(CamsApplication.instance.container.apiService))
    )
) {
    val context = LocalContext.current
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    var showCreateDialog by remember { mutableStateOf(false) }
    var itemPendingMovement by remember { mutableStateOf<InventoryItemDto?>(null) }
    var itemPendingDelete by remember { mutableStateOf<InventoryItemDto?>(null) }

    LaunchedEffect(uiState.saveSuccess, uiState.saveError) {
        if (uiState.saveSuccess) {
            Toast.makeText(context, "Saved", Toast.LENGTH_SHORT).show()
            showCreateDialog = false
            itemPendingMovement = null
            viewModel.clearSaveStatus()
        }
        uiState.saveError?.let {
            Toast.makeText(context, it, Toast.LENGTH_LONG).show()
            viewModel.clearSaveStatus()
        }
    }

    AdminBaseScreen(
        title = "Inventory Management",
        subtitle = "Stock items, reorder levels and stock movement history",
        currentRoute = AppRoutes.ADMIN_INVENTORY,
        onNavigate = onNavigate,
        floatingActionButton = {
            FloatingActionButton(onClick = { showCreateDialog = true }, containerColor = CamsNavy, contentColor = Color.White) {
                Icon(Icons.Filled.Add, "Add Item")
            }
        }
    ) {
        Column(Modifier.fillMaxSize().padding(16.dp)) {
            val lowStockCount = uiState.items.count { it.isLowStock }
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                Text("${uiState.items.size} item(s)", fontWeight = FontWeight.Bold, fontSize = 16.sp, color = MaterialTheme.colorScheme.onSurface)
                if (lowStockCount > 0) {
                    Text(
                        "$lowStockCount LOW STOCK", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = Color(0xFFB45309),
                        modifier = Modifier.background(Color(0xFFFEF3C7), RoundedCornerShape(4.dp)).padding(horizontal = 6.dp, vertical = 2.dp)
                    )
                }
            }
            Spacer(Modifier.height(12.dp))

            uiState.error?.let { Text(it, color = Color(0xFFB91C1C), fontSize = 13.sp, modifier = Modifier.padding(bottom = 8.dp)) }

            if (uiState.isLoading) {
                Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) { CircularProgressIndicator() }
            } else if (uiState.items.isEmpty()) {
                Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) { Text("No inventory items yet", color = MaterialTheme.colorScheme.onSurfaceVariant) }
            } else {
                LazyColumn(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    items(uiState.items, key = { it.id }) { item ->
                        CamsCard {
                            Row(Modifier.fillMaxWidth().padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
                                Column(Modifier.weight(1f)) {
                                    Text(item.name, fontWeight = FontWeight.Bold, fontSize = 15.sp, color = MaterialTheme.colorScheme.onSurface)
                                    Text(
                                        "${item.code}${item.category?.let { " • $it" } ?: ""}${item.location?.let { " • $it" } ?: ""}",
                                        fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                    Spacer(Modifier.height(4.dp))
                                    Text(
                                        "${item.quantity} ${item.unit} in stock",
                                        fontSize = 12.sp, fontWeight = FontWeight.Bold,
                                        color = if (item.isLowStock) Color(0xFFB45309) else Color(0xFF047857)
                                    )
                                }
                                TextButton(onClick = { itemPendingMovement = item }) { Text("Stock", fontSize = 12.sp) }
                                IconButton(onClick = { itemPendingDelete = item }) {
                                    Icon(Icons.Filled.Delete, "Delete item", tint = Color(0xFFB91C1C), modifier = Modifier.size(18.dp))
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    if (showCreateDialog) {
        CreateItemDialog(
            isSaving = uiState.isSaving,
            onDismiss = { showCreateDialog = false },
            onSubmit = { n, c, cat, u, q, mq, up, loc, sup -> viewModel.createItem(n, c, cat, u, q, mq, up, loc, sup) }
        )
    }

    itemPendingMovement?.let { item ->
        StockMovementDialog(
            item = item,
            isSaving = uiState.isSaving,
            onDismiss = { itemPendingMovement = null },
            onSubmit = { movement, qty, reason -> viewModel.recordMovement(item.id, movement, qty, reason) }
        )
    }

    itemPendingDelete?.let { item ->
        AlertDialog(
            onDismissRequest = { itemPendingDelete = null },
            title = { Text("Delete Item") },
            text = { Text("Delete \"${item.name}\"? This cannot be undone.") },
            confirmButton = { TextButton(onClick = { viewModel.deleteItem(item.id); itemPendingDelete = null }) { Text("Delete", color = Color(0xFFB91C1C)) } },
            dismissButton = { TextButton(onClick = { itemPendingDelete = null }) { Text("Cancel") } }
        )
    }
}

@Composable
private fun CreateItemDialog(
    isSaving: Boolean,
    onDismiss: () -> Unit,
    onSubmit: (name: String, code: String, category: String, unit: String, quantity: Int, minQuantity: Int, unitPrice: Double?, location: String, supplier: String) -> Unit
) {
    var name by remember { mutableStateOf("") }
    var code by remember { mutableStateOf("") }
    var category by remember { mutableStateOf("") }
    var unit by remember { mutableStateOf("pcs") }
    var quantity by remember { mutableStateOf("0") }
    var minQuantity by remember { mutableStateOf("0") }
    var unitPrice by remember { mutableStateOf("") }
    var location by remember { mutableStateOf("") }
    var supplier by remember { mutableStateOf("") }
    val qty = quantity.toIntOrNull() ?: 0
    val minQty = minQuantity.toIntOrNull() ?: 0
    val valid = name.isNotBlank() && code.isNotBlank()

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("New Inventory Item") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedTextField(value = name, onValueChange = { name = it }, label = { Text("Item Name") }, modifier = Modifier.fillMaxWidth(), singleLine = true)
                OutlinedTextField(value = code, onValueChange = { code = it }, label = { Text("Code") }, modifier = Modifier.fillMaxWidth(), singleLine = true)
                OutlinedTextField(value = category, onValueChange = { category = it }, label = { Text("Category") }, modifier = Modifier.fillMaxWidth(), singleLine = true)
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(value = unit, onValueChange = { unit = it }, label = { Text("Unit") }, modifier = Modifier.weight(1f), singleLine = true)
                    OutlinedTextField(value = quantity, onValueChange = { quantity = it.filter { c -> c.isDigit() } }, label = { Text("Quantity") }, modifier = Modifier.weight(1f), singleLine = true)
                }
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(value = minQuantity, onValueChange = { minQuantity = it.filter { c -> c.isDigit() } }, label = { Text("Reorder Level") }, modifier = Modifier.weight(1f), singleLine = true)
                    OutlinedTextField(value = unitPrice, onValueChange = { unitPrice = it.filter { c -> c.isDigit() || c == '.' } }, label = { Text("Unit Price (₹)") }, modifier = Modifier.weight(1f), singleLine = true)
                }
                OutlinedTextField(value = location, onValueChange = { location = it }, label = { Text("Location") }, modifier = Modifier.fillMaxWidth(), singleLine = true)
                OutlinedTextField(value = supplier, onValueChange = { supplier = it }, label = { Text("Supplier") }, modifier = Modifier.fillMaxWidth(), singleLine = true)
            }
        },
        confirmButton = {
            TextButton(enabled = valid && !isSaving, onClick = {
                onSubmit(name.trim(), code.trim(), category.trim(), unit.trim().ifBlank { "pcs" }, qty, minQty, unitPrice.toDoubleOrNull(), location.trim(), supplier.trim())
            }) { Text(if (isSaving) "Creating..." else "Create") }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text("Cancel") } }
    )
}

@Composable
private fun StockMovementDialog(
    item: InventoryItemDto,
    isSaving: Boolean,
    onDismiss: () -> Unit,
    onSubmit: (movement: String, quantity: Int, reason: String) -> Unit
) {
    var movement by remember { mutableStateOf("IN") }
    var quantity by remember { mutableStateOf("") }
    var reason by remember { mutableStateOf("") }
    val qty = quantity.toIntOrNull() ?: 0
    val valid = qty > 0

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Stock Movement — ${item.name}") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text("Current stock: ${item.quantity} ${item.unit}", fontSize = 13.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    FilterChip(selected = movement == "IN", onClick = { movement = "IN" }, label = { Text("Stock In") })
                    FilterChip(selected = movement == "OUT", onClick = { movement = "OUT" }, label = { Text("Stock Out") })
                    FilterChip(selected = movement == "ADJUST", onClick = { movement = "ADJUST" }, label = { Text("Adjust") })
                }
                OutlinedTextField(
                    value = quantity, onValueChange = { quantity = it.filter { c -> c.isDigit() } },
                    label = { Text(if (movement == "ADJUST") "New Total Quantity" else "Quantity") },
                    modifier = Modifier.fillMaxWidth(), singleLine = true
                )
                OutlinedTextField(value = reason, onValueChange = { reason = it }, label = { Text("Reason") }, modifier = Modifier.fillMaxWidth())
            }
        },
        confirmButton = { TextButton(enabled = valid && !isSaving, onClick = { onSubmit(movement, qty, reason.trim()) }) { Text(if (isSaving) "Saving..." else "Save") } },
        dismissButton = { TextButton(onClick = onDismiss) { Text("Cancel") } }
    )
}
