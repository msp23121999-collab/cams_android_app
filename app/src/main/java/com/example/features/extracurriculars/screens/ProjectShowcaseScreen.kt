package com.example.features.extracurriculars.screens

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
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
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.core.theme.LexNovaPurple
import com.example.features.extracurriculars.models.AcademicPublication
import com.example.features.extracurriculars.providers.ExtracurricularsViewModel
import com.example.features.student.widgets.StudentDrawer
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProjectShowcaseScreen(
    viewModel: ExtracurricularsViewModel = viewModel(),
    onNavigate: (String) -> Unit
) {
    val uiState by viewModel.uiState.collectAsState()
    val drawerState = rememberDrawerState(initialValue = DrawerValue.Closed)
    val scope = rememberCoroutineScope()
    var showSubmissionModal by remember { mutableStateOf(false) }

    ModalNavigationDrawer(
        drawerState = drawerState,
        drawerContent = {
            StudentDrawer(
                currentRoute = "/student/publications",
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
                    title = { Text("Academic Publications") },
                    navigationIcon = {
                        IconButton(onClick = { scope.launch { drawerState.open() } }) {
                            Icon(Icons.Filled.Menu, contentDescription = "Menu")
                        }
                    },
                    colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.Transparent)
                )
            },
            floatingActionButton = {
                FloatingActionButton(onClick = { showSubmissionModal = true }, containerColor = LexNovaPurple) {
                    Icon(Icons.Filled.Add, contentDescription = "Submit Publication", tint = Color.White)
                }
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
                        Row(modifier = Modifier.fillMaxSize().padding(16.dp), horizontalArrangement = Arrangement.spacedBy(24.dp)) {
                            Column(modifier = Modifier.weight(1f)) {
                                PublicationsAnalytics()
                            }
                            Column(modifier = Modifier.weight(2f)) {
                                Text("All Submissions", style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold))
                                Spacer(modifier = Modifier.height(16.dp))
                                PublicationsList(uiState.publications)
                            }
                        }
                    } else {
                        LazyColumn(modifier = Modifier.fillMaxSize().padding(16.dp)) {
                            item {
                                PublicationsAnalytics()
                                Spacer(modifier = Modifier.height(32.dp))
                            }
                            item {
                                Text("All Submissions", style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold))
                                Spacer(modifier = Modifier.height(16.dp))
                            }
                            items(uiState.publications) { pub ->
                                PublicationCard(pub)
                                Spacer(modifier = Modifier.height(16.dp))
                            }
                        }
                    }
                }
            }
        }
        
        if (showSubmissionModal) {
            SubmissionModal(onDismiss = { showSubmissionModal = false })
        }
    }
}

@Composable
fun PublicationsList(publications: List<AcademicPublication>) {
    LazyColumn(verticalArrangement = Arrangement.spacedBy(16.dp)) {
        items(publications) { pub ->
            PublicationCard(pub)
        }
    }
}

@Composable
fun PublicationCard(pub: AcademicPublication) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Row(modifier = Modifier.padding(20.dp), verticalAlignment = Alignment.CenterVertically) {
            Column(modifier = Modifier.weight(1f)) {
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalAlignment = Alignment.CenterVertically) {
                    Surface(
                        color = LexNovaPurple.copy(alpha = 0.1f),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Text(text = pub.practiceArea, modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp), style = MaterialTheme.typography.labelSmall.copy(color = LexNovaPurple, fontWeight = FontWeight.Bold))
                    }
                    Text(text = pub.status, style = MaterialTheme.typography.labelSmall.copy(color = Color(0xFF64748B)))
                }
                Spacer(modifier = Modifier.height(12.dp))
                Text(text = pub.title, style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold))
                Spacer(modifier = Modifier.height(4.dp))
                Text(text = pub.abstractText, style = MaterialTheme.typography.bodyMedium.copy(color = Color(0xFF64748B)))
                Spacer(modifier = Modifier.height(12.dp))
                Text(text = "Guide: ${pub.guide} • ${pub.date}", style = MaterialTheme.typography.labelMedium.copy(color = Color(0xFF64748B)))
            }
            Spacer(modifier = Modifier.width(16.dp))
            IconButton(onClick = { /* Download PDF */ }, modifier = Modifier.background(LexNovaPurple.copy(alpha = 0.1f), RoundedCornerShape(12.dp))) {
                Icon(Icons.Filled.PictureAsPdf, contentDescription = "Download", tint = LexNovaPurple)
            }
        }
    }
}

@Composable
fun PublicationsAnalytics() {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(modifier = Modifier.padding(24.dp)) {
            Text("Submissions by Practice Area", style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold))
            Spacer(modifier = Modifier.height(24.dp))
            
            // Simple Bar Chart representation using Canvas
            Canvas(modifier = Modifier.fillMaxWidth().height(200.dp)) {
                val data = listOf(
                    "Corp" to 12,
                    "IPR" to 8,
                    "Crim" to 15,
                    "Tech" to 10
                )
                val maxVal = data.maxOf { it.second }.toFloat()
                val barWidth = 40.dp.toPx()
                val spacing = (size.width - (barWidth * data.size)) / (data.size + 1)
                
                data.forEachIndexed { index, (label, value) ->
                    val x = spacing + (index * (barWidth + spacing))
                    val height = (value / maxVal) * (size.height - 30.dp.toPx()) // Leave room for labels
                    val y = size.height - 30.dp.toPx() - height
                    
                    drawRoundRect(
                        color = LexNovaPurple,
                        topLeft = Offset(x, y),
                        size = Size(barWidth, height),
                        cornerRadius = androidx.compose.ui.geometry.CornerRadius(4.dp.toPx(), 4.dp.toPx())
                    )
                }
                
                // Draw baseline
                drawLine(
                    color = Color.LightGray,
                    start = Offset(0f, size.height - 30.dp.toPx()),
                    end = Offset(size.width, size.height - 30.dp.toPx()),
                    strokeWidth = 2.dp.toPx()
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SubmissionModal(onDismiss: () -> Unit) {
    var title by remember { mutableStateOf("") }
    var abstractText by remember { mutableStateOf("") }
    var category by remember { mutableStateOf("") }
    
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Submit Publication") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                OutlinedTextField(
                    value = title,
                    onValueChange = { title = it },
                    label = { Text("Title") },
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = abstractText,
                    onValueChange = { abstractText = it },
                    label = { Text("Abstract") },
                    modifier = Modifier.fillMaxWidth(),
                    minLines = 3
                )
                OutlinedTextField(
                    value = category,
                    onValueChange = { category = it },
                    label = { Text("Practice Category") },
                    modifier = Modifier.fillMaxWidth()
                )
                Button(
                    onClick = { /* File picker */ },
                    modifier = Modifier.fillMaxWidth(),
                    colors = ButtonDefaults.buttonColors(containerColor = Color.LightGray)
                ) {
                    Text("Upload PDF", color = Color.Black)
                }
            }
        },
        confirmButton = {
            Button(onClick = onDismiss, colors = ButtonDefaults.buttonColors(containerColor = LexNovaPurple)) {
                Text("Submit")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}
