package com.example.features.leave.screens

import androidx.compose.animation.*
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.core.theme.*
import com.example.core.ui.shimmerEffect
import com.example.features.student.providers.LeavesViewModel
import com.example.features.student.widgets.StudentDrawer
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LeaveApplicationScreen(
    onNavigate: (String) -> Unit = {},
    viewModel: LeavesViewModel
) {
    val uiState by viewModel.uiState.collectAsState()
    val drawerState = rememberDrawerState(initialValue = DrawerValue.Closed)
    val scope = rememberCoroutineScope()

    var showPolicy by remember { mutableStateOf(false) }

    if (showPolicy) {
        AlertDialog(
            onDismissRequest = { showPolicy = false },
            title = { Text("University Leave & OD Policy", fontWeight = FontWeight.Black) },
            text = {
                Column(modifier = Modifier.verticalScroll(rememberScrollState())) {
                    Text("1. Attendance Requirements", fontWeight = FontWeight.Bold)
                    Text("A minimum of 75% attendance is mandatory. Condonation may be granted between 65-75% on medical grounds.", fontSize = 13.sp)
                    Spacer(Modifier.height(8.dp))
                    Text("2. On-Duty (OD) Leave", fontWeight = FontWeight.Bold)
                    Text("Granted for representing the University in competitions, seminars, or court visits. Max 10% exemption per semester.", fontSize = 13.sp)
                    Spacer(Modifier.height(8.dp))
                    Text("3. Procedure", fontWeight = FontWeight.Bold)
                    Text("Submit applications at least 24 hours in advance. Must be approved by Faculty Mentor, Class Advisor, and HOD.", fontSize = 13.sp)
                }
            },
            confirmButton = {
                TextButton(onClick = { showPolicy = false }) { Text("Acknowledge", color = LexNovaPurple) }
            },
            shape = RoundedCornerShape(24.dp),
            containerColor = MaterialTheme.colorScheme.surface
        )
    }

    ModalNavigationDrawer(
        drawerState = drawerState,
        drawerContent = {
            StudentDrawer(
                onNavigate = { route ->
                    scope.launch { drawerState.close() }
                    onNavigate(route)
                },
                currentRoute = "/student/leave"
            )
        }
    ) {
        Scaffold(
            topBar = {
                TopAppBar(
                    title = { Text("Leave & OD Module", fontWeight = FontWeight.Black) },
                    navigationIcon = {
                        IconButton(onClick = { scope.launch { drawerState.open() } }) {
                            Icon(Icons.Filled.Menu, contentDescription = "Menu")
                        }
                    },
                    actions = {
                        IconButton(onClick = { showPolicy = true }) {
                            Icon(Icons.Filled.Info, contentDescription = "Policy", tint = LexNovaPurple)
                        }
                        IconButton(onClick = { onNavigate("LOGOUT") }) {
                            Icon(Icons.Filled.Logout, contentDescription = "Logout", tint = Color.Gray)
                        }
                    },
                    colors = TopAppBarDefaults.topAppBarColors(
                        containerColor = MaterialTheme.colorScheme.surface,
                        titleContentColor = LexNovaSlateDark
                    )
                )
            },
            containerColor = LexNovaSlateLight
        ) { padding ->
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding)
            ) {
                if (uiState.isLoading) {
                    LeaveSkeleton()
                } else {
                    LeaveContent(uiState, viewModel)
                }
            }
        }
    }
}

@Composable
private fun LeaveContent(uiState: com.example.features.student.providers.LeavesState, viewModel: LeavesViewModel) {
    var appType by remember { mutableStateOf("Leave") }
    var leaveType by remember { mutableStateOf("Sick Leave") }
    var odType by remember { mutableStateOf("Court Visit") }
    var fromDate by remember { mutableStateOf("") }
    var toDate by remember { mutableStateOf("") }
    var reason by remember { mutableStateOf("") }
    
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(20.dp)
    ) {
        DashboardWidgets(uiState)
        
        // Application Form
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(32.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            border = BorderStroke(1.dp, MaterialTheme.colorScheme.surfaceVariant)
        ) {
            Column(modifier = Modifier.padding(24.dp), verticalArrangement = Arrangement.spacedBy(16.dp)) {
                Text("New Application", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Black)
                
                // Toggle
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(LexNovaSlateLight, RoundedCornerShape(12.dp))
                        .padding(4.dp)
                ) {
                    listOf("Leave", "OD").forEach { type ->
                        val isSelected = appType == type
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .clip(RoundedCornerShape(8.dp))
                                .background(if (isSelected) Color.White else Color.Transparent)
                                .clickable { appType = type }
                                .padding(vertical = 10.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = type,
                                fontWeight = FontWeight.Bold,
                                color = if (isSelected) LexNovaPurple else LexNovaSlateAccent
                            )
                        }
                    }
                }

                // Category Selection
                Column {
                    Text("Category", fontSize = 13.sp, fontWeight = FontWeight.Black, color = LexNovaSlateAccent)
                    val options = if (appType == "Leave") {
                        listOf("Sick Leave", "Casual Leave", "Medical Leave")
                    } else {
                        listOf("Moot Court Competition", "Court Visit", "Internship Program")
                    }
                    
                    var expanded by remember { mutableStateOf(false) }
                    Box {
                        OutlinedButton(
                            onClick = { expanded = true },
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Text(if (appType == "Leave") leaveType else odType)
                            Spacer(Modifier.weight(1f))
                            Icon(Icons.Filled.ArrowDropDown, null)
                        }
                        DropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
                            options.forEach { opt ->
                                DropdownMenuItem(
                                    text = { Text(opt) },
                                    onClick = {
                                        if (appType == "Leave") leaveType = opt
                                        else odType = opt
                                        expanded = false
                                    }
                                )
                            }
                        }
                    }
                }

                // Dates
                Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    OutlinedTextField(
                        value = fromDate,
                        onValueChange = { fromDate = it },
                        modifier = Modifier.weight(1f),
                        label = { Text("From (YYYY-MM-DD)") },
                        shape = RoundedCornerShape(12.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = LexNovaPurple,
                            focusedLabelColor = LexNovaPurple,
                            cursorColor = LexNovaPurple
                        ),
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(
                            keyboardType = KeyboardType.Text,
                            imeAction = ImeAction.Next
                        )
                    )
                    OutlinedTextField(
                        value = toDate,
                        onValueChange = { toDate = it },
                        modifier = Modifier.weight(1f),
                        label = { Text("To (YYYY-MM-DD)") },
                        shape = RoundedCornerShape(12.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = LexNovaPurple,
                            focusedLabelColor = LexNovaPurple,
                            cursorColor = LexNovaPurple
                        ),
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(
                            keyboardType = KeyboardType.Text,
                            imeAction = ImeAction.Next
                        )
                    )
                }

                // Reason
                OutlinedTextField(
                    value = reason,
                    onValueChange = { reason = it },
                    modifier = Modifier.fillMaxWidth(),
                    label = { Text("Reason / Purpose") },
                    shape = RoundedCornerShape(12.dp),
                    minLines = 3,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = LexNovaPurple,
                        focusedLabelColor = LexNovaPurple,
                        cursorColor = LexNovaPurple
                    ),
                    keyboardOptions = KeyboardOptions(
                        keyboardType = KeyboardType.Text,
                        imeAction = ImeAction.Done
                    )
                )

                Button(
                    onClick = { 
                        viewModel.applyLeave(
                            type = if (appType == "Leave") leaveType else odType,
                            fromDate = fromDate,
                            toDate = toDate,
                            reason = reason
                        )
                    },
                    modifier = Modifier.fillMaxWidth().height(56.dp),
                    shape = RoundedCornerShape(16.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = LexNovaPurple)
                ) {
                    Icon(Icons.Filled.Send, null)
                    Spacer(Modifier.width(8.dp))
                    Text("Submit Application", fontWeight = FontWeight.Black)
                }
            }
        }
        
        ApplicationHistory(uiState)
    }
}

@Composable
private fun DashboardWidgets(uiState: com.example.features.student.providers.LeavesState) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        WidgetCard(
            label = "Approved",
            value = "${uiState.leaves.count { it.status == "Approved" }}",
            color = Color(0xFF10B981),
            modifier = Modifier.weight(1f)
        )
        WidgetCard(
            label = "Pending",
            value = "${uiState.leaves.count { it.status == "Pending" }}",
            color = LexNovaPurple,
            modifier = Modifier.weight(1f)
        )
    }
}

@Composable
private fun WidgetCard(label: String, value: String, color: Color, modifier: Modifier = Modifier) {
    Card(
        modifier = modifier,
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.surfaceVariant)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(label.uppercase(), fontSize = 12.sp, fontWeight = FontWeight.Black, color = LexNovaSlateAccent)
            Text(value, fontSize = 20.sp, fontWeight = FontWeight.Black, color = color)
        }
    }
}

@Composable
private fun ApplicationHistory(uiState: com.example.features.student.providers.LeavesState) {
    Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
        Text("Recent Applications", fontWeight = FontWeight.Black, color = LexNovaSlateDark)
        uiState.leaves.forEach { record ->
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.surfaceVariant)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                        Column {
                            Text(record.type, fontWeight = FontWeight.Black, fontSize = 14.sp)
                            Text("${record.startDate} - ${record.endDate}", fontSize = 13.sp, color = LexNovaSlateAccent)
                        }
                        StatusBadge(record.status)
                    }
                }
            }
        }
    }
}

@Composable
fun StatusBadge(status: String) {
    val (bgColor, textColor) = when (status) {
        "APPROVED" -> Color(0xFFECFDF5) to Color(0xFF047857)
        "REJECTED" -> Color(0xFFFEF2F2) to Color(0xFFB91C1C)
        else -> Color(0xFFFFFBEB) to Color(0xFFB45309)
    }
    Surface(
        color = bgColor,
        shape = RoundedCornerShape(100.dp)
    ) {
        Text(
            text = status,
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp),
            style = MaterialTheme.typography.labelSmall.copy(color = textColor, fontWeight = FontWeight.Bold)
        )
    }
}

@Composable
private fun LeaveSkeleton() {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(20.dp)
    ) {
        Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            Box(Modifier.weight(1f).height(80.dp).shimmerEffect().clip(RoundedCornerShape(24.dp)))
            Box(Modifier.weight(1f).height(80.dp).shimmerEffect().clip(RoundedCornerShape(24.dp)))
        }
        Box(Modifier.fillMaxWidth().height(400.dp).shimmerEffect().clip(RoundedCornerShape(32.dp)))
        repeat(2) {
            Box(Modifier.fillMaxWidth().height(100.dp).shimmerEffect().clip(RoundedCornerShape(20.dp)))
        }
    }
}
