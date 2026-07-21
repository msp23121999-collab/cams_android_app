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
import com.example.core.network.HostelAllocationDto
import com.example.core.network.HostelBlockDto
import com.example.core.network.HostelRoomDto
import com.example.core.repository.AdminRepositoryImpl
import com.example.core.repository.HostelRepositoryImpl
import com.example.core.theme.*
import com.example.core.ui.CamsCard
import com.example.features.admin.models.AdminFeeStudent
import com.example.features.admin.providers.AdminHostelViewModel2
import com.example.features.admin.providers.AdminHostelViewModel2Factory
import com.example.features.admin.widgets.AdminBaseScreen

@Composable
fun AdminHostelScreen(
    onNavigate: (String) -> Unit,
    viewModel: AdminHostelViewModel2 = viewModel(
        factory = AdminHostelViewModel2Factory(
            HostelRepositoryImpl(CamsApplication.instance.container.apiService),
            AdminRepositoryImpl(CamsApplication.instance.container.apiService)
        )
    )
) {
    val context = LocalContext.current
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    var tab by remember { mutableStateOf(0) }
    val tabs = listOf("Blocks", "Rooms", "Allocations")

    var showCreateBlock by remember { mutableStateOf(false) }
    var showCreateRoom by remember { mutableStateOf(false) }
    var showAllocate by remember { mutableStateOf(false) }
    var blockPendingDelete by remember { mutableStateOf<HostelBlockDto?>(null) }
    var roomPendingDelete by remember { mutableStateOf<HostelRoomDto?>(null) }
    var allocationPendingVacate by remember { mutableStateOf<HostelAllocationDto?>(null) }

    LaunchedEffect(uiState.saveSuccess, uiState.saveError) {
        if (uiState.saveSuccess) {
            Toast.makeText(context, "Saved", Toast.LENGTH_SHORT).show()
            showCreateBlock = false; showCreateRoom = false; showAllocate = false
            viewModel.clearSaveStatus()
        }
        uiState.saveError?.let {
            Toast.makeText(context, it, Toast.LENGTH_LONG).show()
            viewModel.clearSaveStatus()
        }
    }

    AdminBaseScreen(
        title = "Hostel Management",
        subtitle = "Blocks, rooms and student room allocations",
        currentRoute = AppRoutes.ADMIN_HOSTEL,
        onNavigate = onNavigate,
        floatingActionButton = {
            FloatingActionButton(
                onClick = {
                    when (tab) {
                        0 -> showCreateBlock = true
                        1 -> showCreateRoom = true
                        else -> showAllocate = true
                    }
                },
                containerColor = CamsNavy, contentColor = Color.White
            ) { Icon(Icons.Filled.Add, "Add") }
        }
    ) {
        Column(Modifier.fillMaxSize()) {
            TabRow(selectedTabIndex = tab, containerColor = MaterialTheme.colorScheme.surface, contentColor = CamsNavy) {
                tabs.forEachIndexed { i, t ->
                    Tab(selected = tab == i, onClick = { tab = i }, text = { Text(t, fontWeight = FontWeight.Bold) })
                }
            }

            Column(Modifier.fillMaxSize().padding(16.dp)) {
                uiState.error?.let { Text(it, color = Color(0xFFB91C1C), fontSize = 13.sp, modifier = Modifier.padding(bottom = 8.dp)) }

                if (uiState.isLoading) {
                    Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) { CircularProgressIndicator() }
                } else when (tab) {
                    0 -> BlocksTab(uiState.blocks) { blockPendingDelete = it }
                    1 -> RoomsTab(uiState.rooms) { roomPendingDelete = it }
                    else -> AllocationsTab(uiState.allocations) { allocationPendingVacate = it }
                }
            }
        }
    }

    if (showCreateBlock) {
        CreateBlockDialog(
            isSaving = uiState.isSaving,
            onDismiss = { showCreateBlock = false },
            onSubmit = { n, c, t, wn, wp, addr -> viewModel.createBlock(n, c, t, wn, wp, addr) }
        )
    }
    if (showCreateRoom) {
        CreateRoomDialog(
            blocks = uiState.blocks,
            isSaving = uiState.isSaving,
            onDismiss = { showCreateRoom = false },
            onSubmit = { blockId, num, floor, cap, type, rent -> viewModel.createRoom(blockId, num, floor, cap, type, rent) }
        )
    }
    if (showAllocate) {
        AllocateRoomDialog(
            rooms = uiState.rooms,
            students = uiState.studentResults,
            isSearching = uiState.isSearching,
            isSaving = uiState.isSaving,
            onSearch = { viewModel.searchStudents(it) },
            onDismiss = { showAllocate = false },
            onSubmit = { roomId, studentId, remarks -> viewModel.allocateRoom(roomId, studentId, remarks) }
        )
    }

    blockPendingDelete?.let { block ->
        AlertDialog(
            onDismissRequest = { blockPendingDelete = null },
            title = { Text("Delete Block") },
            text = { Text("Delete \"${block.name}\"? This is only possible if it has no rooms.") },
            confirmButton = { TextButton(onClick = { viewModel.deleteBlock(block.id); blockPendingDelete = null }) { Text("Delete", color = Color(0xFFB91C1C)) } },
            dismissButton = { TextButton(onClick = { blockPendingDelete = null }) { Text("Cancel") } }
        )
    }
    roomPendingDelete?.let { room ->
        AlertDialog(
            onDismissRequest = { roomPendingDelete = null },
            title = { Text("Delete Room") },
            text = { Text("Delete room \"${room.roomNumber}\"? This is only possible if it has no active occupants.") },
            confirmButton = { TextButton(onClick = { viewModel.deleteRoom(room.id); roomPendingDelete = null }) { Text("Delete", color = Color(0xFFB91C1C)) } },
            dismissButton = { TextButton(onClick = { roomPendingDelete = null }) { Text("Cancel") } }
        )
    }
    allocationPendingVacate?.let { alloc ->
        AlertDialog(
            onDismissRequest = { allocationPendingVacate = null },
            title = { Text("Vacate Room") },
            text = { Text("Mark ${alloc.studentName ?: "this student"}'s allocation in room ${alloc.roomNumber ?: ""} as vacated?") },
            confirmButton = { TextButton(onClick = { viewModel.vacateAllocation(alloc.id); allocationPendingVacate = null }) { Text("Vacate") } },
            dismissButton = { TextButton(onClick = { allocationPendingVacate = null }) { Text("Cancel") } }
        )
    }
}

@Composable
private fun BlocksTab(blocks: List<HostelBlockDto>, onDelete: (HostelBlockDto) -> Unit) {
    if (blocks.isEmpty()) {
        Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) { Text("No hostel blocks yet", color = MaterialTheme.colorScheme.onSurfaceVariant) }
        return
    }
    LazyColumn(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        items(blocks, key = { it.id }) { block ->
            CamsCard {
                Row(Modifier.fillMaxWidth().padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
                    Column(Modifier.weight(1f)) {
                        Text(block.name, fontWeight = FontWeight.Bold, fontSize = 16.sp, color = MaterialTheme.colorScheme.onSurface)
                        Text("${block.code} • ${block.hostelType}${block.wardenName?.let { " • Warden: $it" } ?: ""}", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        Spacer(Modifier.height(6.dp))
                        Text("${block.totalRooms} rooms • ${block.occupied}/${block.totalCapacity} occupied", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = CamsNavy)
                    }
                    IconButton(onClick = { onDelete(block) }) { Icon(Icons.Filled.Delete, "Delete block", tint = Color(0xFFB91C1C), modifier = Modifier.size(18.dp)) }
                }
            }
        }
    }
}

@Composable
private fun RoomsTab(rooms: List<HostelRoomDto>, onDelete: (HostelRoomDto) -> Unit) {
    if (rooms.isEmpty()) {
        Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) { Text("No rooms yet", color = MaterialTheme.colorScheme.onSurfaceVariant) }
        return
    }
    LazyColumn(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        items(rooms, key = { it.id }) { room ->
            CamsCard {
                Row(Modifier.fillMaxWidth().padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
                    Column(Modifier.weight(1f)) {
                        Text("${room.blockName ?: ""} — Room ${room.roomNumber}", fontWeight = FontWeight.Bold, fontSize = 15.sp, color = MaterialTheme.colorScheme.onSurface)
                        Text("Floor ${room.floor} • ${room.roomType ?: "Standard"}${room.monthlyRent?.let { " • ₹${it.toInt()}/mo" } ?: ""}", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                    val full = room.available <= 0
                    Text(
                        "${room.occupied}/${room.capacity}",
                        fontSize = 12.sp, fontWeight = FontWeight.Bold,
                        color = if (full) Color(0xFFB91C1C) else Color(0xFF047857),
                        modifier = Modifier.background(if (full) Color(0xFFFFE4E6) else Color(0xFFD1FAE5), RoundedCornerShape(4.dp)).padding(horizontal = 6.dp, vertical = 2.dp)
                    )
                    IconButton(onClick = { onDelete(room) }) { Icon(Icons.Filled.Delete, "Delete room", tint = Color(0xFFB91C1C), modifier = Modifier.size(18.dp)) }
                }
            }
        }
    }
}

@Composable
private fun AllocationsTab(allocations: List<HostelAllocationDto>, onVacate: (HostelAllocationDto) -> Unit) {
    if (allocations.isEmpty()) {
        Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) { Text("No allocations yet", color = MaterialTheme.colorScheme.onSurfaceVariant) }
        return
    }
    LazyColumn(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        items(allocations, key = { it.id }) { alloc ->
            CamsCard {
                Row(Modifier.fillMaxWidth().padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
                    Column(Modifier.weight(1f)) {
                        Text(alloc.studentName ?: "Student", fontWeight = FontWeight.Bold, fontSize = 15.sp, color = MaterialTheme.colorScheme.onSurface)
                        Text("${alloc.rollNo ?: ""} • ${alloc.blockName ?: ""} Room ${alloc.roomNumber ?: ""}", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        Text("Since ${alloc.allocatedOn}", fontSize = 11.sp, color = Color(0xFF64748B))
                    }
                    if (alloc.status == "ACTIVE") {
                        TextButton(onClick = { onVacate(alloc) }) { Text("Vacate", fontSize = 12.sp) }
                    } else {
                        Text("VACATED", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = Color(0xFF64748B))
                    }
                }
            }
        }
    }
}

@Composable
private fun CreateBlockDialog(
    isSaving: Boolean,
    onDismiss: () -> Unit,
    onSubmit: (name: String, code: String, type: String, wardenName: String, wardenPhone: String, address: String) -> Unit
) {
    var name by remember { mutableStateOf("") }
    var code by remember { mutableStateOf("") }
    var type by remember { mutableStateOf("BOYS") }
    var wardenName by remember { mutableStateOf("") }
    var wardenPhone by remember { mutableStateOf("") }
    var address by remember { mutableStateOf("") }
    val valid = name.isNotBlank() && code.isNotBlank()

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("New Hostel Block") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedTextField(value = name, onValueChange = { name = it }, label = { Text("Block Name") }, modifier = Modifier.fillMaxWidth(), singleLine = true)
                OutlinedTextField(value = code, onValueChange = { code = it }, label = { Text("Code") }, modifier = Modifier.fillMaxWidth(), singleLine = true)
                Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    FilterChip(selected = type == "BOYS", onClick = { type = "BOYS" }, label = { Text("Boys") })
                    FilterChip(selected = type == "GIRLS", onClick = { type = "GIRLS" }, label = { Text("Girls") })
                }
                OutlinedTextField(value = wardenName, onValueChange = { wardenName = it }, label = { Text("Warden Name") }, modifier = Modifier.fillMaxWidth(), singleLine = true)
                OutlinedTextField(value = wardenPhone, onValueChange = { wardenPhone = it }, label = { Text("Warden Phone") }, modifier = Modifier.fillMaxWidth(), singleLine = true)
                OutlinedTextField(value = address, onValueChange = { address = it }, label = { Text("Address") }, modifier = Modifier.fillMaxWidth())
            }
        },
        confirmButton = { TextButton(enabled = valid && !isSaving, onClick = { onSubmit(name.trim(), code.trim(), type, wardenName.trim(), wardenPhone.trim(), address.trim()) }) { Text(if (isSaving) "Creating..." else "Create") } },
        dismissButton = { TextButton(onClick = onDismiss) { Text("Cancel") } }
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun CreateRoomDialog(
    blocks: List<HostelBlockDto>,
    isSaving: Boolean,
    onDismiss: () -> Unit,
    onSubmit: (blockId: String, roomNumber: String, floor: Int, capacity: Int, type: String, rent: Double?) -> Unit
) {
    var blockId by remember { mutableStateOf(blocks.firstOrNull()?.id ?: "") }
    var expanded by remember { mutableStateOf(false) }
    var roomNumber by remember { mutableStateOf("") }
    var floor by remember { mutableStateOf("0") }
    var capacity by remember { mutableStateOf("2") }
    var type by remember { mutableStateOf("") }
    var rent by remember { mutableStateOf("") }
    val fl = floor.toIntOrNull() ?: 0
    val cap = capacity.toIntOrNull() ?: 0
    val valid = blockId.isNotBlank() && roomNumber.isNotBlank() && cap > 0

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("New Room") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                ExposedDropdownMenuBox(expanded = expanded, onExpandedChange = { expanded = !expanded }) {
                    OutlinedTextField(
                        value = blocks.firstOrNull { it.id == blockId }?.name ?: "",
                        onValueChange = {}, readOnly = true, label = { Text("Block") },
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
                        modifier = Modifier.fillMaxWidth().menuAnchor()
                    )
                    ExposedDropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
                        blocks.forEach { b -> DropdownMenuItem(text = { Text(b.name) }, onClick = { blockId = b.id; expanded = false }) }
                    }
                }
                OutlinedTextField(value = roomNumber, onValueChange = { roomNumber = it }, label = { Text("Room Number") }, modifier = Modifier.fillMaxWidth(), singleLine = true)
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(value = floor, onValueChange = { floor = it.filter { c -> c.isDigit() } }, label = { Text("Floor") }, modifier = Modifier.weight(1f), singleLine = true)
                    OutlinedTextField(value = capacity, onValueChange = { capacity = it.filter { c -> c.isDigit() } }, label = { Text("Capacity") }, modifier = Modifier.weight(1f), singleLine = true)
                }
                OutlinedTextField(value = type, onValueChange = { type = it }, label = { Text("Room Type") }, modifier = Modifier.fillMaxWidth(), singleLine = true)
                OutlinedTextField(value = rent, onValueChange = { rent = it.filter { c -> c.isDigit() } }, label = { Text("Monthly Rent (₹)") }, modifier = Modifier.fillMaxWidth(), singleLine = true)
            }
        },
        confirmButton = { TextButton(enabled = valid && !isSaving, onClick = { onSubmit(blockId, roomNumber.trim(), fl, cap, type.trim(), rent.toDoubleOrNull()) }) { Text(if (isSaving) "Creating..." else "Create") } },
        dismissButton = { TextButton(onClick = onDismiss) { Text("Cancel") } }
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun AllocateRoomDialog(
    rooms: List<HostelRoomDto>,
    students: List<AdminFeeStudent>,
    isSearching: Boolean,
    isSaving: Boolean,
    onSearch: (String) -> Unit,
    onDismiss: () -> Unit,
    onSubmit: (roomId: String, studentId: String, remarks: String) -> Unit
) {
    val availableRooms = rooms.filter { it.available > 0 }
    var roomId by remember { mutableStateOf(availableRooms.firstOrNull()?.id ?: "") }
    var expanded by remember { mutableStateOf(false) }
    var query by remember { mutableStateOf("") }
    var selectedStudent by remember { mutableStateOf<AdminFeeStudent?>(null) }
    var remarks by remember { mutableStateOf("") }

    LaunchedEffect(query) {
        kotlinx.coroutines.delay(350)
        onSearch(query)
    }

    val valid = roomId.isNotBlank() && selectedStudent != null

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Allocate Room") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                if (availableRooms.isEmpty()) {
                    Text("No rooms with free capacity.", color = Color(0xFFB91C1C), fontSize = 13.sp)
                } else {
                    ExposedDropdownMenuBox(expanded = expanded, onExpandedChange = { expanded = !expanded }) {
                        OutlinedTextField(
                            value = availableRooms.firstOrNull { it.id == roomId }?.let { "${it.blockName} — ${it.roomNumber} (${it.available} free)" } ?: "",
                            onValueChange = {}, readOnly = true, label = { Text("Room") },
                            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
                            modifier = Modifier.fillMaxWidth().menuAnchor()
                        )
                        ExposedDropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
                            availableRooms.forEach { r -> DropdownMenuItem(text = { Text("${r.blockName} — ${r.roomNumber} (${r.available} free)") }, onClick = { roomId = r.id; expanded = false }) }
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

                OutlinedTextField(value = remarks, onValueChange = { remarks = it }, label = { Text("Remarks (optional)") }, modifier = Modifier.fillMaxWidth())
            }
        },
        confirmButton = {
            TextButton(enabled = valid && !isSaving, onClick = { onSubmit(roomId, selectedStudent!!.studentId, remarks.trim()) }) {
                Text(if (isSaving) "Allocating..." else "Allocate")
            }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text("Cancel") } }
    )
}
