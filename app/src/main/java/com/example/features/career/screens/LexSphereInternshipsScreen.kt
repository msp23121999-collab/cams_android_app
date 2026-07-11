package com.example.features.career.screens

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.core.theme.LexNovaPurple
import com.example.features.career.models.InternshipDrive
import com.example.features.career.providers.CareerViewModel
import com.example.features.student.widgets.StudentDrawer
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LexSphereInternshipsScreen(
    viewModel: CareerViewModel = viewModel(),
    onNavigate: (String) -> Unit
) {
    val uiState by viewModel.uiState.collectAsState()
    val drawerState = rememberDrawerState(initialValue = DrawerValue.Closed)
    val scope = rememberCoroutineScope()
    var activeTab by remember { mutableStateOf("Explore") }
    
    // Master-Detail tracking for responsive UI
    var selectedDrive by remember { mutableStateOf<InternshipDrive?>(null) }
    
    ModalNavigationDrawer(
        drawerState = drawerState,
        drawerContent = {
            StudentDrawer(
                currentRoute = "/career/internships",
                onNavigate = {
                    scope.launch { drawerState.close() }
                    onNavigate(it)
                }
            )
        }
    ) {
        Scaffold(
            topBar = {
                TopAppBar(
                    title = { Text("LexSphere Internships") },
                    navigationIcon = {
                        if (selectedDrive != null) {
                            IconButton(onClick = { selectedDrive = null }) {
                                Icon(Icons.Filled.ArrowBack, contentDescription = "Back")
                            }
                        } else {
                            IconButton(onClick = { scope.launch { drawerState.open() } }) {
                                Icon(Icons.Filled.Menu, contentDescription = "Menu")
                            }
                        }
                    },
                    colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.Transparent)
                )
            },
            containerColor = MaterialTheme.colorScheme.background
        ) { paddingValues ->
            BoxWithConstraints(modifier = Modifier.padding(paddingValues)) {
                val isTablet = maxWidth > 600.dp
                
                if (uiState.isLoading) {
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        CircularProgressIndicator(color = LexNovaPurple)
                    }
                } else {
                    if (isTablet) {
                        Row(modifier = Modifier.fillMaxSize()) {
                            Box(modifier = Modifier.weight(1f)) {
                                InternshipsList(uiState.drives, activeTab, { activeTab = it }, { selectedDrive = it })
                            }
                            if (selectedDrive != null) {
                                Box(modifier = Modifier.weight(2f)) {
                                    DriveDetailsView(selectedDrive!!)
                                }
                            }
                        }
                    } else {
                        if (selectedDrive != null) {
                            DriveDetailsView(selectedDrive!!)
                        } else {
                            InternshipsList(uiState.drives, activeTab, { activeTab = it }, { selectedDrive = it })
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun InternshipsList(
    drives: List<InternshipDrive>, 
    activeTab: String, 
    onTabSelected: (String) -> Unit,
    onDriveSelected: (InternshipDrive) -> Unit
) {
    Column(modifier = Modifier.fillMaxSize()) {
        ScrollableTabRow(
            selectedTabIndex = listOf("Explore", "Applications", "Insights").indexOf(activeTab),
            edgePadding = 16.dp,
            containerColor = Color.Transparent,
            divider = {}
        ) {
            listOf("Explore", "Applications", "Insights").forEach { tab ->
                val isSelected = activeTab == tab
                Tab(
                    selected = isSelected,
                    onClick = { onTabSelected(tab) },
                    text = { 
                        Text(
                            text = tab, 
                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                        ) 
                    }
                )
            }
        }
        
        val filtered = if (activeTab == "Applications") drives.filter { it.applicationStatus != "Not Applied" } else drives
        
        LazyColumn(
            modifier = Modifier.fillMaxSize().padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            items(filtered) { drive ->
                Card(
                    modifier = Modifier.fillMaxWidth().clickable { onDriveSelected(drive) },
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    shape = RoundedCornerShape(16.dp),
                    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Surface(
                                shape = RoundedCornerShape(12.dp),
                                color = LexNovaPurple.copy(alpha = 0.1f),
                                modifier = Modifier.size(48.dp)
                            ) {
                                Icon(Icons.Filled.Work, contentDescription = null, tint = LexNovaPurple, modifier = Modifier.padding(12.dp))
                            }
                            Spacer(modifier = Modifier.width(16.dp))
                            Column {
                                Text(text = drive.companyName, style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold))
                                Text(text = drive.role, style = MaterialTheme.typography.bodyMedium.copy(color = Color(0xFF64748B)))
                            }
                        }
                        
                        if (activeTab == "Applications") {
                            Spacer(modifier = Modifier.height(16.dp))
                            Text("Status: ${drive.applicationStatus}", style = MaterialTheme.typography.labelMedium.copy(color = LexNovaPurple, fontWeight = FontWeight.Bold))
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun DriveDetailsView(drive: InternshipDrive) {
    Column(
        modifier = Modifier.fillMaxSize().padding(24.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Text(text = drive.companyName, style = MaterialTheme.typography.headlineMedium.copy(fontWeight = FontWeight.Bold))
        Text(text = drive.role, style = MaterialTheme.typography.titleLarge)
        
        HorizontalDivider()
        
        Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
            Text(text = "Location: ${drive.location}", style = MaterialTheme.typography.bodyMedium.copy(color = Color(0xFF64748B)))
            Text(text = "Stipend: ${drive.stipend}", style = MaterialTheme.typography.bodyMedium.copy(color = Color(0xFF64748B)))
        }
        
        Spacer(modifier = Modifier.height(8.dp))
        Text(text = "Description", style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold))
        Text(text = drive.description, style = MaterialTheme.typography.bodyMedium)
        
        Spacer(modifier = Modifier.height(24.dp))
        
        if (drive.applicationStatus != "Not Applied") {
            Text(text = "Application Funnel", style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold))
            Spacer(modifier = Modifier.height(16.dp))
            ApplicationFunnel(drive.applicationStatus)
        } else {
            Button(
                onClick = { /* Apply */ },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(containerColor = LexNovaPurple)
            ) {
                Text("Apply Now")
            }
        }
    }
}

@Composable
fun ApplicationFunnel(currentStatus: String) {
    val steps = listOf("Applied", "Assessment", "Interview", "Selected")
    val currentIndex = steps.indexOf(currentStatus).takeIf { it >= 0 } ?: 0
    
    Column {
        steps.forEachIndexed { index, stepName ->
            val isCompleted = index <= currentIndex
            val color = if (isCompleted) LexNovaPurple else Color.LightGray
            
            Row(modifier = Modifier.height(IntrinsicSize.Min)) {
                Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.width(40.dp)) {
                    Surface(
                        shape = RoundedCornerShape(50),
                        color = color,
                        modifier = Modifier.size(16.dp)
                    ) {}
                    if (index < steps.lastIndex) {
                        Canvas(modifier = Modifier.fillMaxHeight().width(2.dp).padding(vertical = 4.dp)) {
                            drawLine(
                                color = color,
                                start = Offset(size.width/2, 0f),
                                end = Offset(size.width/2, size.height),
                                strokeWidth = 2.dp.toPx()
                            )
                        }
                    }
                }
                Column(modifier = Modifier.padding(bottom = 32.dp, start = 8.dp)) {
                    Text(
                        text = stepName,
                        style = MaterialTheme.typography.titleMedium.copy(
                            fontWeight = if (isCompleted) FontWeight.Bold else FontWeight.Normal,
                            color = if (isCompleted) Color.Black else Color(0xFF64748B)
                        )
                    )
                }
            }
        }
    }
}
