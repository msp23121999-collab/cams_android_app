package com.example.features.faculty.screens

import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.*
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import com.example.features.faculty.providers.FacultyMaterialsViewModel
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.core.theme.*
import com.example.core.ui.CamsCard
import com.example.features.faculty.widgets.FacultyBaseScreen

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FacultyStudyMaterialsScreen(
    viewModel: FacultyMaterialsViewModel,
    onNavigate: (String) -> Unit
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    var searchQuery by remember { mutableStateOf("") }
    var selectedCategory by remember { mutableStateOf("All") }
    var showUploadDialog by remember { mutableStateOf(false) }
    
    val categories = listOf("All", "Notes", "QP", "Reference", "Lab")

    FacultyBaseScreen(scrollable = false, 
        title = "Study Materials",
        subtitle = "Manage digital academic resources",
        currentRoute = "/faculty/study-materials",
        onNavigate = onNavigate
    ) {
        if (state.isLoading) {
            Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator(color = CamsNavy)
            }
        } else {
            // 1. Search and Filter
            OutlinedTextField(
                value = searchQuery,
                onValueChange = { searchQuery = it },
                modifier = Modifier.fillMaxWidth(),
                placeholder = { Text("Search materials...") },
                leadingIcon = { Icon(Icons.Filled.Search, null, tint = MaterialTheme.colorScheme.onSurfaceVariant) },
                shape = RoundedCornerShape(12.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    unfocusedBorderColor = MaterialTheme.colorScheme.outlineVariant,
                    focusedBorderColor = CamsNavy,
                    unfocusedContainerColor = Color.White,
                    focusedContainerColor = Color.White
                )
            )
            
            Spacer(modifier = Modifier.height(16.dp))
            
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                categories.forEach { cat ->
                    val isSelected = selectedCategory == cat
                    FilterChip(
                        selected = isSelected,
                        onClick = { selectedCategory = cat },
                        label = { Text(cat) },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = CamsNavy,
                            selectedLabelColor = Color.White,
                            containerColor = MaterialTheme.colorScheme.surface,
                            labelColor = CamsTextSecondary
                        )
                    )
                }
            }
            
            Spacer(modifier = Modifier.height(20.dp))
            
            // 2. Statistics Card
            CamsCard {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    horizontalArrangement = Arrangement.SpaceAround
                ) {
                    MaterialStat("Total Files", "${state.materials.size}", Icons.Filled.InsertDriveFile)
                    MaterialStat("Downloads", "${state.materials.sumOf { it.downloads }}", Icons.Filled.Download)
                    MaterialStat("Subjects", "${state.materials.map { it.subject }.distinct().size}", Icons.Filled.Subject)
                }
            }
            
            Spacer(modifier = Modifier.height(24.dp))
            
            // 3. Material List
            Text("Recent Uploads", fontWeight = FontWeight.Bold, fontSize = 16.sp, color = MaterialTheme.colorScheme.onSurface)
            Spacer(modifier = Modifier.height(12.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                    OutlinedTextField(
                        value = searchQuery,
                        onValueChange = { searchQuery = it },
                        modifier = Modifier
                            .weight(1f)
                            .height(50.dp),
                        placeholder = { Text("Search materials...", color = MaterialTheme.colorScheme.onSurfaceVariant) },
                        leadingIcon = { Icon(Icons.Filled.Search, contentDescription = "Search", tint = MaterialTheme.colorScheme.onSurfaceVariant) },
                        shape = RoundedCornerShape(25.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = CamsNavy,
                            unfocusedBorderColor = Color.LightGray
                        ),
                        singleLine = true
                    )
                    Spacer(modifier = Modifier.width(12.dp))
                    Surface(
                        shape = CircleShape,
                        color = CamsBackground,
                        border = androidx.compose.foundation.BorderStroke(1.dp, Color.LightGray),
                        modifier = Modifier.size(50.dp),
                        onClick = { /* Open Filter Sheet */ }
                    ) {
                        Icon(Icons.Filled.FilterList, contentDescription = "Filter", tint = CamsTextPrimary, modifier = Modifier.padding(12.dp))
                    }
                }

                // Category Chips
                androidx.compose.foundation.lazy.LazyRow(
                    modifier = Modifier.padding(horizontal = 16.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(categories) { cat ->
                        FilterChip(
                            selected = selectedCategory == cat,
                            onClick = { selectedCategory = cat },
                            label = { Text(cat) },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = CamsNavy,
                                selectedLabelColor = Color.White
                            )
                        )
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Materials List
                val filtered = state.materials.filter {
                    (selectedCategory == "All" || it.type == selectedCategory) &&
                    it.title.contains(searchQuery, ignoreCase = true)
                }

                if (filtered.isEmpty()) {
                    Text("No materials match your filters.", modifier = Modifier.padding(20.dp), color = MaterialTheme.colorScheme.onSurfaceVariant)
                } else {
                    LazyColumn(
                        modifier = Modifier.fillMaxSize(),
                        contentPadding = PaddingValues(bottom = 80.dp), // padding for FAB
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        items(filtered) { m ->
                            MaterialRow(m)
                        }
                    }
                }

            // FAB for uploading new material
            Box(modifier = Modifier.fillMaxSize()) {
                FloatingActionButton(
                    onClick = { showUploadDialog = true },
                    modifier = Modifier
                        .align(Alignment.BottomEnd)
                        .padding(24.dp),
                    containerColor = CamsNavy,
                    contentColor = Color.White
                ) {
                    Icon(Icons.Filled.FileUpload, contentDescription = "Upload Material")
                }
            }
        }
        
        if (showUploadDialog) {
            UploadMaterialDialog(
                onDismiss = { showUploadDialog = false },
                onSubmit = { payload ->
                    viewModel.uploadMaterial(payload) {
                        showUploadDialog = false
                    }
                }
            )
        }
    }
}

@Composable
fun UploadMaterialDialog(onDismiss: () -> Unit, onSubmit: (com.example.core.network.UploadMaterialRequestDto) -> Unit) {
    var title by remember { mutableStateOf("") }
    var description by remember { mutableStateOf("") }
    var subject by remember { mutableStateOf("") }
    var unit by remember { mutableStateOf("") }
    var topic by remember { mutableStateOf("") }
    var category by remember { mutableStateOf("Notes") }
    var status by remember { mutableStateOf("Draft") }
    
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Upload Study Material", fontWeight = FontWeight.Bold) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.verticalScroll(rememberScrollState())) {
                OutlinedTextField(value = title, onValueChange = { title = it }, label = { Text("Title") }, modifier = Modifier.fillMaxWidth())
                OutlinedTextField(value = description, onValueChange = { description = it }, label = { Text("Description") }, modifier = Modifier.fillMaxWidth())
                OutlinedTextField(value = subject, onValueChange = { subject = it }, label = { Text("Subject") }, modifier = Modifier.fillMaxWidth())
                OutlinedTextField(value = unit, onValueChange = { unit = it }, label = { Text("Unit") }, modifier = Modifier.fillMaxWidth())
                OutlinedTextField(value = topic, onValueChange = { topic = it }, label = { Text("Topic") }, modifier = Modifier.fillMaxWidth())
                
                // Categories
                Text("Category:", fontWeight = FontWeight.SemiBold)
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    listOf("Notes", "QP", "Reference", "Lab").forEach { cat ->
                        FilterChip(
                            selected = category == cat,
                            onClick = { category = cat },
                            label = { Text(cat) }
                        )
                    }
                }
                
                // Status
                Text("Status:", fontWeight = FontWeight.SemiBold)
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    listOf("Draft", "Pending Approval").forEach { stat ->
                        FilterChip(
                            selected = status == stat,
                            onClick = { status = stat },
                            label = { Text(stat) }
                        )
                    }
                }
                
                // Placeholder for file selection
                Text("File will be attached automatically in this demo.", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
        },
        confirmButton = {
            Button(onClick = {
                val payload = com.example.core.network.UploadMaterialRequestDto(
                    title = title,
                    description = description,
                    subject = subject,
                    unit = unit,
                    topic = topic,
                    category = category,
                    keywords = listOf(),
                    fileUrl = "/mock-uploads/sample.pdf",
                    fileFormat = "pdf",
                    status = status
                )
                onSubmit(payload)
            }, colors = ButtonDefaults.buttonColors(containerColor = CamsNavy)) {
                Text("Upload")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel", color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
        }
    )
}

@Composable
private fun MaterialStat(label: String, value: String, icon: ImageVector) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Box(
            modifier = Modifier
                .size(40.dp)
                .background(CamsNavy.copy(alpha = 0.1f), CircleShape),
            contentAlignment = Alignment.Center
        ) {
            Icon(icon, null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(20.dp))
        }
        Spacer(modifier = Modifier.height(8.dp))
        Text(value, fontWeight = FontWeight.Black, fontSize = 16.sp, color = MaterialTheme.colorScheme.onSurface)
        Text(label, fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
    }
}
@Composable
fun MaterialRow(data: com.example.core.network.FacultyMaterialDto) {
    Surface(
        color = Color.White,
        shape = RoundedCornerShape(12.dp),
        shadowElevation = 1.dp
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(44.dp)
                    .background(MaterialTheme.colorScheme.surfaceVariant, RoundedCornerShape(10.dp)),
                contentAlignment = Alignment.Center
            ) {
                val icon = when(data.type.lowercase()) {
                    "notes" -> Icons.Filled.Description
                    "qp" -> Icons.Filled.Quiz
                    "lab" -> Icons.AutoMirrored.Filled.Assignment
                    else -> Icons.AutoMirrored.Filled.MenuBook
                }
                Icon(icon, null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(22.dp))
            }
            
            Spacer(modifier = Modifier.width(16.dp))
            
            Column(modifier = Modifier.weight(1f)) {
                Text(data.title, fontWeight = FontWeight.Bold, fontSize = 14.sp, color = MaterialTheme.colorScheme.onSurface)
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(data.type, fontSize = 13.sp, color = CamsNavy, fontWeight = FontWeight.Medium)
                    Text(" • ", fontSize = 13.sp, color = Color.LightGray)
                    Text(data.size, fontSize = 13.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    Text(" • ", fontSize = 13.sp, color = Color.LightGray)
                    Text(data.date, fontSize = 13.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            }
            
            IconButton(onClick = { /* More options */ }) {
                Icon(Icons.Filled.MoreVert, null, tint = MaterialTheme.colorScheme.onSurfaceVariant)
            }
        }
    }
}
