package com.example.features.faculty.screens

import android.widget.Toast
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.core.theme.*
import com.example.features.faculty.widgets.FacultyBaseScreen

import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.core.repository.FacultyRepositoryImpl
import com.example.core.network.ResearchEntryRequest
import com.example.features.faculty.providers.FacultyResearchViewModel
import com.example.features.faculty.providers.FacultyResearchViewModelFactory

@Composable
fun FacultyResearchTrackerScreen(onNavigate: (String) -> Unit) {
    val context = LocalContext.current
    val repository = remember { FacultyRepositoryImpl(com.example.CamsApplication.instance.container.apiService) }
    val factory = remember { FacultyResearchViewModelFactory(repository) }
    val viewModel: FacultyResearchViewModel = viewModel(factory = factory)
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    var selectedTab by remember { mutableStateOf(0) }
    var showAddDialog by remember { mutableStateOf(false) }
    var entryPendingEdit by remember { mutableStateOf<com.example.features.faculty.models.ResearchEntry?>(null) }
    val tabs = listOf("Publications", "Mentorship", "Grants")

    LaunchedEffect(uiState.saveSuccess, uiState.saveError) {
        if (uiState.saveSuccess) {
            Toast.makeText(context, "Saved", Toast.LENGTH_SHORT).show()
            showAddDialog = false
            entryPendingEdit = null
            viewModel.clearSaveStatus()
        }
        uiState.saveError?.let {
            Toast.makeText(context, it, Toast.LENGTH_SHORT).show()
            viewModel.clearSaveStatus()
        }
    }

    val publications = uiState.researchEntries.filter { it.researchType == "Journal Article" || it.researchType == "Publication" || it.researchType == "Conference Paper" }
    val grants = uiState.researchEntries.filter { it.researchType == "Grant" || it.grantAmount != null }

    FacultyBaseScreen(scrollable = false,
        title = "Research Tracker",
        currentRoute = com.example.core.navigation.AppRoutes.FACULTY_RESEARCH_TRACKER,
        onNavigate = onNavigate,
        floatingActionButton = {
            FloatingActionButton(
                onClick = { showAddDialog = true },
                containerColor = CamsNavy,
                contentColor = Color.White,
                shape = RoundedCornerShape(16.dp)
            ) {
                Icon(Icons.Filled.Add, "Add Publication")
            }
        }
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp)
        ) {
            // Stats Row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                ResearchStatsCard("Publications", "${publications.size}", Icons.Filled.Description, Color(0xFF3B82F6), Modifier.weight(1f))
                ResearchStatsCard("Grants", "${grants.size}", Icons.Filled.FormatQuote, Color(0xFF10B981), Modifier.weight(1f))
                ResearchStatsCard("Total Records", "${uiState.researchEntries.size}", Icons.Filled.TrendingUp, Color(0xFFF59E0B), Modifier.weight(1f))
            }

            Spacer(modifier = Modifier.height(20.dp))

            // Tabs
            ScrollableTabRow(
                selectedTabIndex = selectedTab,
                containerColor = Color.Transparent,
                contentColor = CamsNavy,
                edgePadding = 0.dp,
                divider = {},
                indicator = { tabPositions ->
                    if (selectedTab < tabPositions.size) {
                        TabRowDefaults.SecondaryIndicator(
                            modifier = Modifier.tabIndicatorOffset(tabPositions[selectedTab]),
                            color = CamsNavy
                        )
                    }
                }
            ) {
                tabs.forEachIndexed { index, title ->
                    Tab(
                        selected = selectedTab == index,
                        onClick = { selectedTab = index },
                        text = {
                            Text(
                                title,
                                fontWeight = if (selectedTab == index) FontWeight.Bold else FontWeight.Medium
                            )
                        }
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            uiState.error?.let {
                Text(it, color = Color(0xFFB91C1C), fontSize = 13.sp, modifier = Modifier.padding(bottom = 8.dp))
            }

            if (uiState.isLoading) {
                Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator(color = CamsNavy)
                }
            } else {
                when (selectedTab) {
                    0 -> FacultyPublicationsList(
                        papers = publications,
                        onEdit = { entryPendingEdit = it },
                        onDelete = { viewModel.deletePublication(it.id) }
                    )
                    1 -> StudentMentorshipList(uiState.mentorStudents)
                    2 -> ResearchGrantsList(
                        grants = grants,
                        onEdit = { entryPendingEdit = it },
                        onDelete = { viewModel.deletePublication(it.id) }
                    )
                }
            }
        }
    }

    if (showAddDialog) {
        ResearchEntryDialog(
            existing = null,
            isSaving = uiState.isSaving,
            onDismiss = { showAddDialog = false },
            onSubmit = { viewModel.addPublication(it) }
        )
    }

    entryPendingEdit?.let { entry ->
        ResearchEntryDialog(
            existing = entry,
            isSaving = uiState.isSaving,
            onDismiss = { entryPendingEdit = null },
            onSubmit = { viewModel.updatePublication(entry.id, it) }
        )
    }
}

@Composable
private fun ResearchStatsCard(
    label: String,
    value: String,
    icon: ImageVector,
    color: Color,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier,
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        shape = RoundedCornerShape(16.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Icon(icon, null, tint = color, modifier = Modifier.size(24.dp))
            Spacer(modifier = Modifier.height(8.dp))
            Text(value, fontSize = 20.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurface)
            Text(label, fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
    }
}

@Composable
private fun FacultyPublicationsList(
    papers: List<com.example.features.faculty.models.ResearchEntry>,
    onEdit: (com.example.features.faculty.models.ResearchEntry) -> Unit,
    onDelete: (com.example.features.faculty.models.ResearchEntry) -> Unit
) {
    if (papers.isEmpty()) {
        Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Text("No publications yet. Tap + to add one.", color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
        return
    }
    LazyColumn(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        items(papers, key = { it.id }) { paper ->
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                shape = RoundedCornerShape(16.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.Top
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(paper.title, fontWeight = FontWeight.Bold, fontSize = 16.sp, color = MaterialTheme.colorScheme.onSurface)
                            Text(paper.publication ?: "Unknown Journal", fontSize = 13.sp, color = CamsNavy)
                        }
                        Row {
                            IconButton(onClick = { onEdit(paper) }, modifier = Modifier.size(40.dp)) {
                                Icon(Icons.Filled.Edit, "Edit", tint = MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.size(16.dp))
                            }
                            IconButton(onClick = { onDelete(paper) }, modifier = Modifier.size(40.dp)) {
                                Icon(Icons.Filled.Delete, "Delete", tint = MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.size(16.dp))
                            }
                        }
                    }
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Filled.CalendarToday, null, tint = MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.size(14.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(paper.publicationDate?.take(4) ?: "N/A", fontSize = 13.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                        Surface(
                            color = if (paper.status == "Published") Color(0xFF10B981).copy(alpha = 0.1f) else Color(0xFF3B82F6).copy(alpha = 0.1f),
                            shape = RoundedCornerShape(8.dp)
                        ) {
                            Text(
                                paper.status ?: "Pending",
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                color = if (paper.status == "Published") Color(0xFF10B981) else Color(0xFF3B82F6)
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun StudentMentorshipList(students: List<com.example.core.network.FacultyMentorshipStudentDto>) {
    if (students.isEmpty()) {
        Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Text("No mentees assigned", color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
        return
    }
    LazyColumn(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        items(students) { mentee ->
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                shape = RoundedCornerShape(16.dp)
            ) {
                Row(
                    modifier = Modifier.padding(16.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(mentee.name, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurface)
                        Text(mentee.rollNo, fontSize = 12.sp, color = CamsNavy)
                    }
                    Surface(
                        color = CamsNavy.copy(alpha = 0.05f),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Text(
                            "Semester ${mentee.semester ?: "N/A"}",
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                            fontSize = 13.sp,
                            color = CamsNavy,
                            fontWeight = FontWeight.Medium
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun ResearchGrantsList(
    grants: List<com.example.features.faculty.models.ResearchEntry>,
    onEdit: (com.example.features.faculty.models.ResearchEntry) -> Unit,
    onDelete: (com.example.features.faculty.models.ResearchEntry) -> Unit
) {
    if (grants.isEmpty()) {
        Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Text("No grants recorded", color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
        return
    }
    LazyColumn(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        items(grants, key = { it.id }) { grant ->
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                shape = RoundedCornerShape(16.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(grant.title, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurface)
                        Row {
                            Text("₹ ${grant.grantAmount ?: "0.0"}", fontWeight = FontWeight.Bold, color = Color(0xFF10B981))
                            IconButton(onClick = { onEdit(grant) }, modifier = Modifier.size(40.dp)) {
                                Icon(Icons.Filled.Edit, "Edit", tint = MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.size(16.dp))
                            }
                            IconButton(onClick = { onDelete(grant) }, modifier = Modifier.size(40.dp)) {
                                Icon(Icons.Filled.Delete, "Delete", tint = MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.size(16.dp))
                            }
                        }
                    }
                    Spacer(modifier = Modifier.height(4.dp))
                    Text("Source: ${grant.publisher ?: "Unknown"}", fontSize = 13.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    Spacer(modifier = Modifier.height(12.dp))
                    Surface(
                        color = if (grant.status == "Approved") Color(0xFF10B981).copy(alpha = 0.1f) else Color(0xFFF59E0B).copy(alpha = 0.1f),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Text(
                            grant.status ?: "Pending",
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold,
                            color = if (grant.status == "Approved") Color(0xFF10B981) else Color(0xFFF59E0B)
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun ResearchEntryDialog(
    existing: com.example.features.faculty.models.ResearchEntry?,
    isSaving: Boolean,
    onDismiss: () -> Unit,
    onSubmit: (ResearchEntryRequest) -> Unit
) {
    var title by remember { mutableStateOf(existing?.title ?: "") }
    var publication by remember { mutableStateOf(existing?.publication ?: "") }
    var researchType by remember { mutableStateOf(existing?.researchType ?: "Journal Article") }
    var publisher by remember { mutableStateOf(existing?.publisher ?: "") }
    var publicationDate by remember { mutableStateOf(existing?.publicationDate ?: "") }
    var grantAmount by remember { mutableStateOf(existing?.grantAmount?.toString() ?: "") }

    val isValid = title.isNotBlank()

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(if (existing == null) "Add Publication" else "Edit Publication") },
        text = {
            Column(
                modifier = Modifier.verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                OutlinedTextField(value = title, onValueChange = { title = it }, label = { Text("Title") }, modifier = Modifier.fillMaxWidth(), singleLine = true)
                OutlinedTextField(value = publication, onValueChange = { publication = it }, label = { Text("Journal / Publication") }, modifier = Modifier.fillMaxWidth(), singleLine = true)
                OutlinedTextField(value = researchType, onValueChange = { researchType = it }, label = { Text("Type (Journal Article / Conference Paper / Grant)") }, modifier = Modifier.fillMaxWidth(), singleLine = true)
                OutlinedTextField(value = publisher, onValueChange = { publisher = it }, label = { Text("Publisher / Grant Source") }, modifier = Modifier.fillMaxWidth(), singleLine = true)
                OutlinedTextField(value = publicationDate, onValueChange = { publicationDate = it }, label = { Text("Date (YYYY-MM-DD)") }, modifier = Modifier.fillMaxWidth(), singleLine = true)
                OutlinedTextField(value = grantAmount, onValueChange = { grantAmount = it }, label = { Text("Grant Amount (optional)") }, modifier = Modifier.fillMaxWidth(), singleLine = true)
            }
        },
        confirmButton = {
            TextButton(
                enabled = isValid && !isSaving,
                onClick = {
                    onSubmit(
                        ResearchEntryRequest(
                            title = title.trim(),
                            publication = publication.trim().ifBlank { null },
                            grantAmount = grantAmount.trim().toDoubleOrNull(),
                            publisher = publisher.trim().ifBlank { null },
                            publicationDate = publicationDate.trim().ifBlank { null },
                            researchType = researchType.trim().ifBlank { null }
                        )
                    )
                }
            ) { Text(if (isSaving) "Saving..." else "Save") }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Cancel") }
        }
    )
}
