package com.example.features.faculty.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.*
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
    val state by viewModel.uiState.collectAsState()
    var searchQuery by remember { mutableStateOf("") }
    var selectedCategory by remember { mutableStateOf("All") }
    
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
                leadingIcon = { Icon(Icons.Filled.Search, null, tint = CamsTextSecondary) },
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
            Text("Recent Uploads", fontWeight = FontWeight.Bold, fontSize = 16.sp, color = CamsTextPrimary)
            Spacer(modifier = Modifier.height(12.dp))
            
            val filteredMaterials = state.materials.filter {
                (selectedCategory == "All" || it.type.equals(selectedCategory, true)) &&
                (searchQuery.isEmpty() || it.title.contains(searchQuery, true) || it.subject.contains(searchQuery, true))
            }

            if (filteredMaterials.isEmpty()) {
                Text("No materials match your filters.", modifier = Modifier.padding(20.dp), color = CamsTextSecondary)
            } else {
                LazyColumn(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    items(filteredMaterials) { item ->
                        MaterialRow(item)
                    }
                }
            }
        }
        
        Spacer(modifier = Modifier.height(80.dp))
    }
    
    // FAB for uploading new material
    Box(modifier = Modifier.fillMaxSize()) {
        FloatingActionButton(
            onClick = { /* Upload material */ },
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(24.dp),
            containerColor = CamsNavy,
            contentColor = Color.White,
            shape = RoundedCornerShape(16.dp)
        ) {
            Icon(Icons.Filled.FileUpload, contentDescription = "Upload Material")
        }
    }
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
            Icon(icon, null, tint = CamsNavy, modifier = Modifier.size(20.dp))
        }
        Spacer(modifier = Modifier.height(8.dp))
        Text(value, fontWeight = FontWeight.Black, fontSize = 16.sp, color = CamsTextPrimary)
        Text(label, fontSize = 11.sp, color = CamsTextSecondary)
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
                    "lab" -> Icons.Filled.Assignment
                    else -> Icons.Filled.MenuBook
                }
                Icon(icon, null, tint = CamsNavy, modifier = Modifier.size(22.dp))
            }
            
            Spacer(modifier = Modifier.width(16.dp))
            
            Column(modifier = Modifier.weight(1f)) {
                Text(data.title, fontWeight = FontWeight.Bold, fontSize = 14.sp, color = CamsTextPrimary)
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(data.type, fontSize = 13.sp, color = CamsNavy, fontWeight = FontWeight.Medium)
                    Text(" • ", fontSize = 13.sp, color = Color.LightGray)
                    Text(data.size, fontSize = 13.sp, color = CamsTextSecondary)
                    Text(" • ", fontSize = 13.sp, color = Color.LightGray)
                    Text(data.date, fontSize = 13.sp, color = CamsTextSecondary)
                }
            }
            
            IconButton(onClick = { /* More options */ }) {
                Icon(Icons.Filled.MoreVert, null, tint = CamsTextSecondary)
            }
        }
    }
}
