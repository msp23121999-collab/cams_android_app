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
import androidx.compose.ui.text.style.TextOverflow
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
import com.example.core.network.TransportPassDto
import com.example.core.network.TransportRouteDto
import com.example.core.network.TransportVehicleDto
import com.example.core.repository.AdminRepositoryImpl
import com.example.core.repository.TransportRepositoryImpl
import com.example.core.theme.*
import com.example.core.ui.CamsCard
import com.example.features.admin.models.AdminFeeStudent
import com.example.features.admin.providers.AdminTransportViewModel2
import com.example.features.admin.providers.AdminTransportViewModel2Factory
import com.example.features.admin.widgets.AdminBaseScreen

@Composable
fun AdminTransportScreen(
    onNavigate: (String) -> Unit,
    viewModel: AdminTransportViewModel2 = viewModel(
        factory = AdminTransportViewModel2Factory(
            TransportRepositoryImpl(CamsApplication.instance.container.apiService),
            AdminRepositoryImpl(CamsApplication.instance.container.apiService)
        )
    )
) {
    val context = LocalContext.current
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    var tab by remember { mutableStateOf(0) }
    val tabs = listOf("Routes", "Vehicles", "Passes")

    var showCreateRoute by remember { mutableStateOf(false) }
    var showCreateVehicle by remember { mutableStateOf(false) }
    var showIssuePass by remember { mutableStateOf(false) }
    var routePendingDelete by remember { mutableStateOf<TransportRouteDto?>(null) }
    var vehiclePendingDelete by remember { mutableStateOf<TransportVehicleDto?>(null) }
    var passPendingCancel by remember { mutableStateOf<TransportPassDto?>(null) }

    LaunchedEffect(uiState.saveSuccess, uiState.saveError) {
        if (uiState.saveSuccess) {
            Toast.makeText(context, "Saved", Toast.LENGTH_SHORT).show()
            showCreateRoute = false; showCreateVehicle = false; showIssuePass = false
            viewModel.clearSaveStatus()
        }
        uiState.saveError?.let {
            Toast.makeText(context, it, Toast.LENGTH_LONG).show()
            viewModel.clearSaveStatus()
        }
    }

    AdminBaseScreen(
        title = "Transport Management",
        subtitle = "Routes, vehicles and student bus passes",
        currentRoute = AppRoutes.ADMIN_TRANSPORT,
        onNavigate = onNavigate,
        floatingActionButton = {
            FloatingActionButton(
                onClick = {
                    when (tab) {
                        0 -> showCreateRoute = true
                        1 -> showCreateVehicle = true
                        else -> showIssuePass = true
                    }
                },
                containerColor = CamsNavy, contentColor = Color.White
            ) { Icon(Icons.Filled.Add, "Add") }
        }
    ) {
        Column(Modifier.fillMaxSize()) {
            TabRow(selectedTabIndex = tab, containerColor = MaterialTheme.colorScheme.surface, contentColor = CamsNavy) {
                tabs.forEachIndexed { i, t -> Tab(selected = tab == i, onClick = { tab = i }, text = { Text(t, fontWeight = FontWeight.Bold) }) }
            }
            Column(Modifier.fillMaxSize().padding(16.dp)) {
                uiState.error?.let { Text(it, color = Color(0xFFB91C1C), fontSize = 13.sp, modifier = Modifier.padding(bottom = 8.dp)) }
                if (uiState.isLoading) {
                    Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) { CircularProgressIndicator() }
                } else when (tab) {
                    0 -> RoutesTab(uiState.routes) { routePendingDelete = it }
                    1 -> VehiclesTab(uiState.vehicles) { vehiclePendingDelete = it }
                    else -> PassesTab(uiState.passes) { passPendingCancel = it }
                }
            }
        }
    }

    if (showCreateRoute) {
        CreateRouteDialog(
            isSaving = uiState.isSaving,
            onDismiss = { showCreateRoute = false },
            onSubmit = { n, c, sp, ep, fare -> viewModel.createRoute(n, c, sp, ep, fare) }
        )
    }
    if (showCreateVehicle) {
        CreateVehicleDialog(
            routes = uiState.routes,
            isSaving = uiState.isSaving,
            onDismiss = { showCreateVehicle = false },
            onSubmit = { reg, type, cap, dn, dp, routeId -> viewModel.createVehicle(reg, type, cap, dn, dp, routeId) }
        )
    }
    if (showIssuePass) {
        IssuePassDialog(
            routes = uiState.routes,
            students = uiState.studentResults,
            isSearching = uiState.isSearching,
            isSaving = uiState.isSaving,
            onSearch = { viewModel.searchStudents(it) },
            onDismiss = { showIssuePass = false },
            onSubmit = { routeId, studentId, from, to, fare -> viewModel.issuePass(routeId, studentId, from, to, fare) }
        )
    }

    routePendingDelete?.let { route ->
        AlertDialog(
            onDismissRequest = { routePendingDelete = null },
            title = { Text("Delete Route") },
            text = { Text("Delete \"${route.name}\"? This is only possible if it has no vehicles or passes.") },
            confirmButton = { TextButton(onClick = { viewModel.deleteRoute(route.id); routePendingDelete = null }) { Text("Delete", color = Color(0xFFB91C1C)) } },
            dismissButton = { TextButton(onClick = { routePendingDelete = null }) { Text("Cancel") } }
        )
    }
    vehiclePendingDelete?.let { vehicle ->
        AlertDialog(
            onDismissRequest = { vehiclePendingDelete = null },
            title = { Text("Delete Vehicle") },
            text = { Text("Delete vehicle \"${vehicle.registrationNo}\"?") },
            confirmButton = { TextButton(onClick = { viewModel.deleteVehicle(vehicle.id); vehiclePendingDelete = null }) { Text("Delete", color = Color(0xFFB91C1C)) } },
            dismissButton = { TextButton(onClick = { vehiclePendingDelete = null }) { Text("Cancel") } }
        )
    }
    passPendingCancel?.let { pass ->
        AlertDialog(
            onDismissRequest = { passPendingCancel = null },
            title = { Text("Cancel Pass") },
            text = { Text("Cancel the transport pass for ${pass.studentName ?: "this student"}?") },
            confirmButton = { TextButton(onClick = { viewModel.cancelPass(pass.id); passPendingCancel = null }) { Text("Cancel Pass", color = Color(0xFFB91C1C)) } },
            dismissButton = { TextButton(onClick = { passPendingCancel = null }) { Text("Back") } }
        )
    }
}

@Composable
private fun RoutesTab(routes: List<TransportRouteDto>, onDelete: (TransportRouteDto) -> Unit) {
    if (routes.isEmpty()) {
        Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) { Text("No routes yet", color = MaterialTheme.colorScheme.onSurfaceVariant) }
        return
    }
    LazyColumn(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        items(routes, key = { it.id }) { route ->
            CamsCard {
                Row(Modifier.fillMaxWidth().padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
                    Column(Modifier.weight(1f)) {
                        Text(route.name, fontWeight = FontWeight.Bold, fontSize = 15.sp, color = MaterialTheme.colorScheme.onSurface)
                        Text("${route.code} • ${route.startPoint} → ${route.endPoint}", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        Spacer(Modifier.height(4.dp))
                        Text(
                            "${route.vehicleCount} vehicle(s) • ${route.passCount} pass(es)${route.fare?.let { " • ₹${it.toInt()}/mo" } ?: ""}",
                            fontSize = 12.sp, fontWeight = FontWeight.Bold, color = CamsNavy
                        )
                    }
                    IconButton(onClick = { onDelete(route) }) { Icon(Icons.Filled.Delete, "Delete route", tint = Color(0xFFB91C1C), modifier = Modifier.size(18.dp)) }
                }
            }
        }
    }
}

@Composable
private fun VehiclesTab(vehicles: List<TransportVehicleDto>, onDelete: (TransportVehicleDto) -> Unit) {
    if (vehicles.isEmpty()) {
        Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) { Text("No vehicles yet", color = MaterialTheme.colorScheme.onSurfaceVariant) }
        return
    }
    LazyColumn(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        items(vehicles, key = { it.id }) { vehicle ->
            CamsCard {
                Row(Modifier.fillMaxWidth().padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
                    Column(Modifier.weight(1f)) {
                        Text(vehicle.registrationNo, fontWeight = FontWeight.Bold, fontSize = 15.sp, color = MaterialTheme.colorScheme.onSurface)
                        Text(
                            "${vehicle.vehicleType ?: "Bus"} • Capacity ${vehicle.capacity}${vehicle.routeName?.let { " • $it" } ?: ""}",
                            fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        vehicle.driverName?.let { Text("Driver: $it${vehicle.driverPhone?.let { p -> " ($p)" } ?: ""}", fontSize = 11.sp, color = Color(0xFF64748B)) }
                    }
                    val active = vehicle.status == "ACTIVE"
                    Text(
                        vehicle.status, fontSize = 11.sp, fontWeight = FontWeight.Bold,
                        color = if (active) Color(0xFF047857) else Color(0xFFB45309),
                        modifier = Modifier.background(if (active) Color(0xFFD1FAE5) else Color(0xFFFEF3C7), RoundedCornerShape(4.dp)).padding(horizontal = 6.dp, vertical = 2.dp)
                    )
                    IconButton(onClick = { onDelete(vehicle) }) { Icon(Icons.Filled.Delete, "Delete vehicle", tint = Color(0xFFB91C1C), modifier = Modifier.size(18.dp)) }
                }
            }
        }
    }
}

@Composable
private fun PassesTab(passes: List<TransportPassDto>, onCancel: (TransportPassDto) -> Unit) {
    if (passes.isEmpty()) {
        Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) { Text("No passes issued yet", color = MaterialTheme.colorScheme.onSurfaceVariant) }
        return
    }
    LazyColumn(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        items(passes, key = { it.id }) { pass ->
            CamsCard {
                Row(Modifier.fillMaxWidth().padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
                    Column(Modifier.weight(1f)) {
                        Text(pass.studentName ?: "Student", fontWeight = FontWeight.Bold, fontSize = 15.sp, color = MaterialTheme.colorScheme.onSurface)
                        Text("${pass.rollNo ?: ""} • ${pass.routeName ?: ""}", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        Text("${pass.validFrom} → ${pass.validTo}", fontSize = 11.sp, color = Color(0xFF64748B))
                    }
                    if (pass.status == "ACTIVE") {
                        TextButton(onClick = { onCancel(pass) }) { Text("Cancel", fontSize = 12.sp, color = Color(0xFFB91C1C)) }
                    } else {
                        Text(pass.status, fontSize = 11.sp, fontWeight = FontWeight.Bold, color = Color(0xFF64748B))
                    }
                }
            }
        }
    }
}

@Composable
private fun CreateRouteDialog(
    isSaving: Boolean,
    onDismiss: () -> Unit,
    onSubmit: (name: String, code: String, startPoint: String, endPoint: String, fare: Double?) -> Unit
) {
    var name by remember { mutableStateOf("") }
    var code by remember { mutableStateOf("") }
    var startPoint by remember { mutableStateOf("") }
    var endPoint by remember { mutableStateOf("") }
    var fare by remember { mutableStateOf("") }
    val valid = name.isNotBlank() && code.isNotBlank() && startPoint.isNotBlank() && endPoint.isNotBlank()

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("New Route") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedTextField(value = name, onValueChange = { name = it }, label = { Text("Route Name") }, modifier = Modifier.fillMaxWidth(), singleLine = true)
                OutlinedTextField(value = code, onValueChange = { code = it }, label = { Text("Code") }, modifier = Modifier.fillMaxWidth(), singleLine = true)
                OutlinedTextField(value = startPoint, onValueChange = { startPoint = it }, label = { Text("Start Point") }, modifier = Modifier.fillMaxWidth(), singleLine = true)
                OutlinedTextField(value = endPoint, onValueChange = { endPoint = it }, label = { Text("End Point") }, modifier = Modifier.fillMaxWidth(), singleLine = true)
                OutlinedTextField(value = fare, onValueChange = { fare = it.filter { c -> c.isDigit() || c == '.' } }, label = { Text("Monthly Fare (₹)") }, modifier = Modifier.fillMaxWidth(), singleLine = true)
            }
        },
        confirmButton = { TextButton(enabled = valid && !isSaving, onClick = { onSubmit(name.trim(), code.trim(), startPoint.trim(), endPoint.trim(), fare.toDoubleOrNull()) }) { Text(if (isSaving) "Creating..." else "Create") } },
        dismissButton = { TextButton(onClick = onDismiss) { Text("Cancel") } }
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun CreateVehicleDialog(
    routes: List<TransportRouteDto>,
    isSaving: Boolean,
    onDismiss: () -> Unit,
    onSubmit: (registrationNo: String, vehicleType: String, capacity: Int, driverName: String, driverPhone: String, routeId: String?) -> Unit
) {
    var registrationNo by remember { mutableStateOf("") }
    var vehicleType by remember { mutableStateOf("Bus") }
    var capacity by remember { mutableStateOf("40") }
    var driverName by remember { mutableStateOf("") }
    var driverPhone by remember { mutableStateOf("") }
    var routeId by remember { mutableStateOf<String?>(null) }
    var expanded by remember { mutableStateOf(false) }
    val cap = capacity.toIntOrNull() ?: 0
    val valid = registrationNo.isNotBlank() && cap > 0

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("New Vehicle") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedTextField(value = registrationNo, onValueChange = { registrationNo = it }, label = { Text("Registration No.") }, modifier = Modifier.fillMaxWidth(), singleLine = true)
                OutlinedTextField(value = vehicleType, onValueChange = { vehicleType = it }, label = { Text("Vehicle Type") }, modifier = Modifier.fillMaxWidth(), singleLine = true)
                OutlinedTextField(value = capacity, onValueChange = { capacity = it.filter { c -> c.isDigit() } }, label = { Text("Capacity") }, modifier = Modifier.fillMaxWidth(), singleLine = true)
                OutlinedTextField(value = driverName, onValueChange = { driverName = it }, label = { Text("Driver Name") }, modifier = Modifier.fillMaxWidth(), singleLine = true)
                OutlinedTextField(value = driverPhone, onValueChange = { driverPhone = it }, label = { Text("Driver Phone") }, modifier = Modifier.fillMaxWidth(), singleLine = true)
                ExposedDropdownMenuBox(expanded = expanded, onExpandedChange = { expanded = !expanded }) {
                    OutlinedTextField(
                        value = routes.firstOrNull { it.id == routeId }?.name ?: "Unassigned",
                        onValueChange = {}, readOnly = true, label = { Text("Route (optional)") },
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
                        modifier = Modifier.fillMaxWidth().menuAnchor()
                    )
                    ExposedDropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
                        DropdownMenuItem(text = { Text("Unassigned") }, onClick = { routeId = null; expanded = false })
                        routes.forEach { r -> DropdownMenuItem(text = { Text(r.name) }, onClick = { routeId = r.id; expanded = false }) }
                    }
                }
            }
        },
        confirmButton = {
            TextButton(enabled = valid && !isSaving, onClick = { onSubmit(registrationNo.trim(), vehicleType.trim(), cap, driverName.trim(), driverPhone.trim(), routeId) }) {
                Text(if (isSaving) "Creating..." else "Create")
            }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text("Cancel") } }
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun IssuePassDialog(
    routes: List<TransportRouteDto>,
    students: List<AdminFeeStudent>,
    isSearching: Boolean,
    isSaving: Boolean,
    onSearch: (String) -> Unit,
    onDismiss: () -> Unit,
    onSubmit: (routeId: String, studentId: String, validFrom: String, validTo: String, farePaid: Double?) -> Unit
) {
    var routeId by remember { mutableStateOf(routes.firstOrNull()?.id ?: "") }
    var expanded by remember { mutableStateOf(false) }
    var query by remember { mutableStateOf("") }
    var selectedStudent by remember { mutableStateOf<AdminFeeStudent?>(null) }
    var validFrom by remember { mutableStateOf("") }
    var validTo by remember { mutableStateOf("") }
    var farePaid by remember { mutableStateOf("") }

    LaunchedEffect(query) {
        kotlinx.coroutines.delay(350)
        onSearch(query)
    }

    val valid = routeId.isNotBlank() && selectedStudent != null

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Issue Transport Pass") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                if (routes.isEmpty()) {
                    Text("No routes configured yet.", color = Color(0xFFB91C1C), fontSize = 13.sp)
                } else {
                    ExposedDropdownMenuBox(expanded = expanded, onExpandedChange = { expanded = !expanded }) {
                        OutlinedTextField(
                            value = routes.firstOrNull { it.id == routeId }?.name ?: "",
                            onValueChange = {}, readOnly = true, label = { Text("Route") },
                            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
                            modifier = Modifier.fillMaxWidth().menuAnchor()
                        )
                        ExposedDropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
                            routes.forEach { r -> DropdownMenuItem(text = { Text(r.name) }, onClick = { routeId = r.id; expanded = false }) }
                        }
                    }
                }

                if (selectedStudent == null) {
                    OutlinedTextField(
                        value = query, onValueChange = { query = it }, label = { Text("Search student") },
                        trailingIcon = { if (isSearching) CircularProgressIndicator(Modifier.size(18.dp), strokeWidth = 2.dp) },
                        modifier = Modifier.fillMaxWidth(), singleLine = true
                    )
                    students.take(5).forEach { s ->
                        TextButton(onClick = { selectedStudent = s }, modifier = Modifier.fillMaxWidth()) {
                            Text("${s.studentName} (${s.rollNo})", modifier = Modifier.fillMaxWidth())
                        }
                    }
                } else {
                    Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.fillMaxWidth()) {
                        Text("${selectedStudent!!.studentName} (${selectedStudent!!.rollNo})", fontWeight = FontWeight.Bold, modifier = Modifier.weight(1f), maxLines = 1, overflow = TextOverflow.Ellipsis)
                        TextButton(onClick = { selectedStudent = null }) { Text("Change") }
                    }
                }

                OutlinedTextField(value = validFrom, onValueChange = { validFrom = it }, label = { Text("Valid From (YYYY-MM-DD, optional)") }, modifier = Modifier.fillMaxWidth(), singleLine = true)
                OutlinedTextField(value = validTo, onValueChange = { validTo = it }, label = { Text("Valid To (YYYY-MM-DD, optional)") }, modifier = Modifier.fillMaxWidth(), singleLine = true)
                OutlinedTextField(value = farePaid, onValueChange = { farePaid = it.filter { c -> c.isDigit() || c == '.' } }, label = { Text("Fare Paid (₹, optional)") }, modifier = Modifier.fillMaxWidth(), singleLine = true)
            }
        },
        confirmButton = {
            TextButton(
                enabled = valid && !isSaving,
                onClick = { onSubmit(routeId, selectedStudent!!.studentId, validFrom.trim(), validTo.trim(), farePaid.toDoubleOrNull()) }
            ) { Text(if (isSaving) "Issuing..." else "Issue") }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text("Cancel") } }
    )
}
