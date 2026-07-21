package com.example.features.campus_life.screens

import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.compose.animation.*
import androidx.compose.foundation.*
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
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.blur
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.core.theme.*
import com.example.core.ui.CamsScreen
import com.example.features.campus_life.models.*
import com.example.features.campus_life.providers.LexSphereViewModel
import com.example.features.student.widgets.StudentDrawer
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LexSphereScreen(
    onNavigate: (String) -> Unit,
    viewModel: LexSphereViewModel = viewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val drawerState = rememberDrawerState(initialValue = DrawerValue.Closed)
    val scope = rememberCoroutineScope()
    var selectedDrive by remember { mutableStateOf<InternshipDrive?>(null) }

    ModalNavigationDrawer(
        drawerState = drawerState,
        drawerContent = {
            StudentDrawer(
                currentRoute = "/student/lexsphere",
                onNavigate = { route ->
                    scope.launch { drawerState.close() }
                    onNavigate(route)
                }
            )
        }
    ) {
        CamsScreen(scrollable = true,
            title = "LexSphere",
            subtitle = "Professional Network & Placement",
            navigationIcon = {
                IconButton(onClick = { scope.launch { drawerState.open() } }) {
                    Icon(Icons.Filled.Menu, contentDescription = "Menu", tint = Color.White)
                }
            },
            verticalArrangement = Arrangement.spacedBy(24.dp)
        ) {
            // Ambient Background
            Box(modifier = Modifier.fillMaxWidth().height(0.dp)) {
                Box(modifier = Modifier.size(400.dp).align(Alignment.TopStart).offset(x = (-100).dp, y = (-100).dp).blur(120.dp).clip(CircleShape).background(Color(0xFFD1FAE5).copy(alpha = 0.5f)))
                Box(modifier = Modifier.size(300.dp).align(Alignment.BottomEnd).offset(x = 100.dp, y = 100.dp).blur(100.dp).clip(CircleShape).background(Color(0xFFFEF3C7).copy(alpha = 0.5f)))
            }

            // Header Section
                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                        Surface(
                            modifier = Modifier.size(56.dp),
                            shape = RoundedCornerShape(16.dp),
                            color = Color(0xFF10B981),
                            shadowElevation = 8.dp
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Icon(Icons.Filled.Work, contentDescription = null, tint = Color.White, modifier = Modifier.size(28.dp))
                            }
                        }
                        Column {
                            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                Text("Internship", style = MaterialTheme.typography.headlineSmall.copy(fontWeight = FontWeight.Black))
                                Surface(color = Color(0xFFFEF3C7), shape = CircleShape, border = BorderStroke(1.dp, Color(0xFFFDE68A))) {
                                    Text("ENTERPRISE", modifier = Modifier.padding(horizontal = 10.dp, vertical = 2.dp), style = MaterialTheme.typography.labelSmall.copy(fontSize = 13.sp, fontWeight = FontWeight.Black, color = Color(0xFFD97706), letterSpacing = 1.sp))
                                }
                            }
                            Text("Explore legal internships & opportunities", style = MaterialTheme.typography.bodySmall.copy(color = Slate500))
                        }
                    }

                    // Search Bar
                    OutlinedTextField(
                        value = uiState.searchQuery,
                        onValueChange = { viewModel.updateSearch(it) },
                        placeholder = { Text("Search firms, roles, mentors...") },
                        leadingIcon = { Icon(Icons.Filled.Search, contentDescription = null, tint = Slate400) },
                        modifier = Modifier.fillMaxWidth(),
                        shape = CircleShape,
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedContainerColor = Color.White,
                            unfocusedContainerColor = Color.White,
                            unfocusedBorderColor = Slate200,
                            focusedBorderColor = Color(0xFF10B981)
                        )
                    )

                    // KPI Cards
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                        KPICard(Modifier.weight(1f), "Active Apps", "12", Icons.Filled.Description, Color(0xFF3B82F6))
                        KPICard(Modifier.weight(1f), "Interviews", "04", Icons.Filled.CalendarToday, Color(0xFFF59E0B))
                    }

                    // AI Coach Preview
                    AICoachPreview()

                    // Live Drives
                    Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                            Text("Live Internship Drives", style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold))
                            Text("View All", style = MaterialTheme.typography.labelMedium.copy(color = Color(0xFF4F46E5), fontWeight = FontWeight.Bold))
                        }
                        uiState.drives.forEach { drive ->
                            DriveItemCard(drive, onApply = { selectedDrive = it })
                        }
                    }

                    // Alumni Network
                    AlumniNetworkSection()
        }

        if (selectedDrive != null) {
            ApplyDriveDialog(
                drive = selectedDrive!!,
                onDismiss = { selectedDrive = null },
                onSubmit = {
                    viewModel.submitApplication(it)
                    selectedDrive = null
                }
            )
        }

        if (uiState.errorMsg != null) {
            AlertDialog(
                onDismissRequest = { viewModel.clearError() },
                title = { Text("Application Failed") },
                text = { Text(uiState.errorMsg!!) },
                confirmButton = {
                    TextButton(onClick = { viewModel.clearError() }) { Text("OK") }
                }
            )
        }
    }
}

@Composable
fun KPICard(modifier: Modifier = Modifier, label: String, value: String, icon: ImageVector, color: Color) {
    Card(
        modifier = modifier,
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        border = BorderStroke(1.dp, Slate200)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Surface(color = color.copy(alpha = 0.1f), shape = RoundedCornerShape(12.dp)) {
                Icon(icon, contentDescription = null, tint = color, modifier = Modifier.padding(8.dp).size(20.dp))
            }
            Spacer(modifier = Modifier.height(16.dp))
            Text(value, style = MaterialTheme.typography.headlineMedium.copy(fontWeight = FontWeight.Black))
            Text(label, style = MaterialTheme.typography.labelSmall.copy(color = Slate500, fontWeight = FontWeight.Bold))
        }
    }
}

@Composable
fun AICoachPreview() {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        border = BorderStroke(1.dp, Slate200),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column {
            Box(modifier = Modifier.fillMaxWidth().height(4.dp).background(Brush.horizontalGradient(listOf(Color(0xFF10B981), Color(0xFF3B82F6), Color(0xFF6366F1)))))
            Column(modifier = Modifier.padding(20.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    Surface(color = Color(0xFFD1FAE5), shape = RoundedCornerShape(8.dp)) {
                        Icon(Icons.Filled.AutoAwesome, contentDescription = null, tint = Color(0xFF059669), modifier = Modifier.padding(6.dp).size(16.dp))
                    }
                    Column {
                        Text("AI Interview Coach", style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.Bold))
                        Text("Session: Tier-1 Firm Partner (M&A)", style = MaterialTheme.typography.labelSmall.copy(color = Slate500, fontSize = 12.sp, fontWeight = FontWeight.Black))
                    }
                }
                Spacer(modifier = Modifier.height(20.dp))
                Surface(color = Slate50, shape = RoundedCornerShape(16.dp), border = BorderStroke(1.dp, Slate200)) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text(
                            "\"Imagine our client is acquiring a tech startup. How would you structure the CPs in the SPA to protect IP assignment?\"",
                            style = MaterialTheme.typography.bodySmall.copy(fontStyle = androidx.compose.ui.text.font.FontStyle.Italic, color = Slate700)
                        )
                    }
                }
                Spacer(modifier = Modifier.height(16.dp))
                Button(
                    onClick = {},
                    modifier = Modifier.fillMaxWidth(),
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF0F172A)),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text("Resume Session")
                }
            }
        }
    }
}

@Composable
fun DriveItemCard(drive: InternshipDrive, onApply: (InternshipDrive) -> Unit) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        border = BorderStroke(1.dp, Slate200)
    ) {
        Row(modifier = Modifier.padding(16.dp), horizontalArrangement = Arrangement.spacedBy(16.dp), verticalAlignment = Alignment.CenterVertically) {
            Surface(modifier = Modifier.size(40.dp), shape = RoundedCornerShape(8.dp), color = Color(0xFFEEF2FF)) {
                Box(contentAlignment = Alignment.Center) {
                    Text(drive.name.take(1), style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Black, color = Color(0xFF4F46E5)))
                }
            }
            Column(modifier = Modifier.weight(1f)) {
                Text(drive.name, style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.Bold))
                Text(drive.role, style = MaterialTheme.typography.bodySmall.copy(color = Slate500))
            }
            Column(horizontalAlignment = Alignment.End) {
                Surface(
                    color = if (drive.status == "Hiring") Color(0xFFECFDF5) else Color(0xFFFEF2F2),
                    shape = RoundedCornerShape(4.dp)
                ) {
                    Text(
                        drive.status.uppercase(),
                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                        style = MaterialTheme.typography.labelSmall.copy(fontSize = 12.sp, fontWeight = FontWeight.Black, color = if (drive.status == "Hiring") Color(0xFF059669) else Color(0xFFDC2626))
                    )
                }
                Text(drive.date, style = MaterialTheme.typography.labelSmall.copy(fontSize = 13.sp, color = Slate400))
            }
            IconButton(onClick = { onApply(drive) }) {
                Icon(Icons.Filled.ChevronRight, contentDescription = "Open", tint = Slate400)
            }
        }
    }
}

@Composable
fun AlumniNetworkSection() {
    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        Text("Mentorship & Alumni Network", style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold))
        Row(modifier = Modifier.horizontalScroll(rememberScrollState()), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            listOf("Adv. Meera Desai", "Sr. Adv. K.R. Rao", "Adv. Samir Khan").forEach { name ->
                Card(
                    modifier = Modifier.width(140.dp),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    border = BorderStroke(1.dp, Slate200)
                ) {
                    Column(modifier = Modifier.padding(16.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                        Surface(modifier = Modifier.size(48.dp), shape = CircleShape, color = Slate100) {
                            Box(contentAlignment = Alignment.Center) {
                                Icon(Icons.Filled.Person, contentDescription = null, tint = Slate400)
                            }
                        }
                        Spacer(modifier = Modifier.height(12.dp))
                        Text(name, style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold), textAlign = TextAlign.Center)
                        Text("15 Yrs Exp", style = MaterialTheme.typography.labelSmall.copy(fontSize = 12.sp, color = Color(0xFF4F46E5), fontWeight = FontWeight.Black))
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ApplyDriveDialog(drive: InternshipDrive, onDismiss: () -> Unit, onSubmit: (InternshipApplication) -> Unit) {
    var rollNo by remember { mutableStateOf("") }
    var cgpa by remember { mutableStateOf("") }
    var phone by remember { mutableStateOf("") }
    var resumeUrl by remember { mutableStateOf("") }

    Dialog(onDismissRequest = onDismiss, properties = DialogProperties(usePlatformDefaultWidth = false)) {
        Surface(
            modifier = Modifier.fillMaxWidth().padding(16.dp),
            shape = RoundedCornerShape(24.dp),
            color = Color.White
        ) {
            Column(modifier = Modifier.padding(24.dp), verticalArrangement = Arrangement.spacedBy(16.dp)) {
                Text("Apply for ${drive.name}", style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Black))
                
                OutlinedTextField(value = rollNo, onValueChange = { rollNo = it }, label = { Text("Roll Number") }, modifier = Modifier.fillMaxWidth())
                OutlinedTextField(value = cgpa, onValueChange = { cgpa = it }, label = { Text("Current CGPA") }, modifier = Modifier.fillMaxWidth())
                OutlinedTextField(value = phone, onValueChange = { phone = it }, label = { Text("Contact Number") }, modifier = Modifier.fillMaxWidth())
                OutlinedTextField(value = resumeUrl, onValueChange = { resumeUrl = it }, label = { Text("Resume URL") }, modifier = Modifier.fillMaxWidth())

                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    TextButton(onClick = onDismiss, modifier = Modifier.weight(1f)) { Text("Cancel") }
                    Button(
                        onClick = {
                            onSubmit(InternshipApplication(drive.name, drive.role, "Student", "", rollNo, phone, cgpa, resumeUrl, "", "Just now"))
                        },
                        modifier = Modifier.weight(1f),
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF4F46E5))
                    ) {
                        Text("Submit")
                    }
                }
            }
        }
    }
}
