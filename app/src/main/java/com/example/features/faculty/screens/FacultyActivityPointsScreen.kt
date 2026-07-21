package com.example.features.faculty.screens

import android.widget.Toast
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

import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.core.network.ActivityPointCategoryDto
import com.example.core.network.ActivityPointCategoryRequest
import com.example.core.network.ActivityPointDto
import com.example.core.repository.FacultyRepositoryImpl
import com.example.features.faculty.providers.FacultyActivityPointsViewModel
import com.example.features.faculty.providers.FacultyActivityPointsViewModelFactory

@Composable
fun FacultyActivityPointsScreen(onNavigate: (String) -> Unit) {
    val context = LocalContext.current
    val repository = remember { FacultyRepositoryImpl(com.example.CamsApplication.instance.container.apiService) }
    val factory = remember { FacultyActivityPointsViewModelFactory(repository) }
    val viewModel: FacultyActivityPointsViewModel = viewModel(factory = factory)
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    var selectedTab by remember { mutableStateOf(0) }
    var showAddCategoryDialog by remember { mutableStateOf(false) }
    var applicationPendingReview by remember { mutableStateOf<ActivityPointDto?>(null) }
    val tabs = listOf("Pending Queue", "Student Records", "Categories")

    LaunchedEffect(uiState.reviewError) {
        uiState.reviewError?.let {
            Toast.makeText(context, it, Toast.LENGTH_SHORT).show()
            viewModel.clearReviewError()
        }
    }
    LaunchedEffect(uiState.categorySaveSuccess, uiState.categoryError) {
        if (uiState.categorySaveSuccess) {
            Toast.makeText(context, "Category saved", Toast.LENGTH_SHORT).show()
            showAddCategoryDialog = false
            viewModel.clearCategoryStatus()
        }
        uiState.categoryError?.let {
            Toast.makeText(context, it, Toast.LENGTH_SHORT).show()
            viewModel.clearCategoryStatus()
        }
    }

    val pending = uiState.applications.filter { it.status == "Pending Review" || it.status == "Under Verification" }
    val verified = uiState.applications.count { it.status == "Approved" }
    val avgPts = uiState.applications.filter { it.status == "Approved" }
        .map { it.approvedPoints ?: 0.0 }
        .let { if (it.isEmpty()) 0.0 else it.average() }

    FacultyBaseScreen(scrollable = false,
        title = "Activity Points",
        currentRoute = com.example.core.navigation.AppRoutes.FACULTY_ACTIVITY_POINTS,
        onNavigate = onNavigate,
        floatingActionButton = {
            if (selectedTab == 2) {
                FloatingActionButton(
                    onClick = { showAddCategoryDialog = true },
                    containerColor = CamsNavy,
                    contentColor = Color.White,
                    shape = RoundedCornerShape(16.dp)
                ) {
                    Icon(Icons.Filled.Add, "Add Category")
                }
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
                ActivityStatsCard("Pending", "${pending.size}", Icons.Filled.Pending, Color(0xFFF59E0B), Modifier.weight(1f))
                ActivityStatsCard("Verified", "$verified", Icons.Filled.Verified, Color(0xFF10B981), Modifier.weight(1f))
                ActivityStatsCard("Avg Pts", "%.1f".format(avgPts), Icons.Filled.BarChart, Color(0xFF3B82F6), Modifier.weight(1f))
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
                    0 -> PendingApprovalQueue(pending, onReview = { applicationPendingReview = it })
                    1 -> StudentPointsRecords(uiState.applications)
                    2 -> ActivityCategoriesList(uiState.categories, onDelete = { viewModel.deleteCategory(it) })
                }
            }
        }
    }

    if (showAddCategoryDialog) {
        AddCategoryDialog(
            isSaving = uiState.isSavingCategory,
            onDismiss = { showAddCategoryDialog = false },
            onSubmit = { viewModel.createCategory(it) }
        )
    }

    applicationPendingReview?.let { app ->
        ReviewApplicationDialog(
            application = app,
            isSaving = uiState.isReviewing,
            onDismiss = { applicationPendingReview = null },
            onSubmit = { status, points, remarks ->
                viewModel.reviewApplication(app.id, status, points, remarks)
                applicationPendingReview = null
            }
        )
    }
}

@Composable
private fun ActivityStatsCard(
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
private fun PendingApprovalQueue(
    requests: List<ActivityPointDto>,
    onReview: (ActivityPointDto) -> Unit
) {
    if (requests.isEmpty()) {
        Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Text("No pending applications", color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
        return
    }
    LazyColumn(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        items(requests, key = { it.id }) { req ->
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
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(req.studentName ?: "Unknown Student", fontWeight = FontWeight.Bold, fontSize = 16.sp, color = MaterialTheme.colorScheme.onSurface)
                            Text(req.title, fontSize = 13.sp, color = CamsNavy)
                        }
                        Text("${req.claimedPoints}", fontWeight = FontWeight.ExtraBold, color = Color(0xFF10B981), fontSize = 18.sp)
                    }
                    Spacer(modifier = Modifier.height(8.dp))
                    Text("Category: ${req.category}", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    Spacer(modifier = Modifier.height(16.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        OutlinedButton(
                            onClick = { onReview(req) },
                            modifier = Modifier.weight(1f),
                            shape = RoundedCornerShape(12.dp),
                            colors = ButtonDefaults.outlinedButtonColors(contentColor = Color.Red)
                        ) {
                            Text("Reject")
                        }
                        Button(
                            onClick = { onReview(req) },
                            modifier = Modifier.weight(1f),
                            shape = RoundedCornerShape(12.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = CamsNavy)
                        ) {
                            Text("Review")
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun StudentPointsRecords(applications: List<ActivityPointDto>) {
    val grouped = applications.groupBy { it.studentName ?: it.studentId ?: "Unknown" }
    if (grouped.isEmpty()) {
        Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Text("No student records yet", color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
        return
    }
    LazyColumn(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        items(grouped.entries.toList(), key = { it.key }) { (name, apps) ->
            val total = apps.filter { it.status == "Approved" }.sumOf { it.approvedPoints ?: 0.0 }
            val status = when {
                total >= 30 -> "Exceeding"
                total >= 15 -> "On Track"
                else -> "Needs Attention"
            }
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
                        Text(name, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurface)
                        Text(status, fontSize = 12.sp, color = when (status) {
                            "Exceeding" -> Color(0xFF10B981)
                            "On Track" -> Color(0xFF3B82F6)
                            else -> Color(0xFFF59E0B)
                        })
                    }
                    Text("$total pts", fontWeight = FontWeight.Bold, color = CamsNavy)
                }
            }
        }
    }
}

@Composable
private fun ActivityCategoriesList(categories: List<ActivityPointCategoryDto>, onDelete: (String) -> Unit) {
    if (categories.isEmpty()) {
        Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Text("No categories configured. Tap + to add one.", color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
        return
    }
    LazyColumn(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        items(categories, key = { it.id }) { cat ->
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                shape = RoundedCornerShape(16.dp)
            ) {
                Row(
                    modifier = Modifier.padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(Icons.Filled.Category, null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(24.dp))
                    Spacer(modifier = Modifier.width(16.dp))
                    Column(modifier = Modifier.weight(1f)) {
                        Text(cat.name, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurface)
                        Text("Max ${cat.maxPoints} points", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                    IconButton(onClick = { onDelete(cat.id) }) {
                        Icon(Icons.Filled.Delete, "Delete", tint = MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.size(18.dp))
                    }
                }
            }
        }
    }
}

@Composable
private fun AddCategoryDialog(
    isSaving: Boolean,
    onDismiss: () -> Unit,
    onSubmit: (ActivityPointCategoryRequest) -> Unit
) {
    var code by remember { mutableStateOf("") }
    var name by remember { mutableStateOf("") }
    var maxPoints by remember { mutableStateOf("") }
    var description by remember { mutableStateOf("") }
    val isValid = code.isNotBlank() && name.isNotBlank() && maxPoints.toDoubleOrNull() != null

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Add Category") },
        text = {
            Column(
                modifier = Modifier.verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                OutlinedTextField(value = code, onValueChange = { code = it }, label = { Text("Code (e.g. moot_court)") }, modifier = Modifier.fillMaxWidth(), singleLine = true)
                OutlinedTextField(value = name, onValueChange = { name = it }, label = { Text("Display Name") }, modifier = Modifier.fillMaxWidth(), singleLine = true)
                OutlinedTextField(value = maxPoints, onValueChange = { maxPoints = it }, label = { Text("Max Points") }, modifier = Modifier.fillMaxWidth(), singleLine = true)
                OutlinedTextField(value = description, onValueChange = { description = it }, label = { Text("Description (optional)") }, modifier = Modifier.fillMaxWidth())
            }
        },
        confirmButton = {
            TextButton(
                enabled = isValid && !isSaving,
                onClick = {
                    onSubmit(
                        ActivityPointCategoryRequest(
                            code = code.trim(),
                            name = name.trim(),
                            maxPoints = maxPoints.toDouble(),
                            description = description.trim().ifBlank { null }
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

@Composable
private fun ReviewApplicationDialog(
    application: ActivityPointDto,
    isSaving: Boolean,
    onDismiss: () -> Unit,
    onSubmit: (status: String, points: Double, remarks: String?) -> Unit
) {
    var approvedPoints by remember { mutableStateOf(application.claimedPoints.toString()) }
    var remarks by remember { mutableStateOf("") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Review Application") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text(application.title, fontWeight = FontWeight.Bold)
                Text("Claimed: ${application.claimedPoints} points", fontSize = 13.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                OutlinedTextField(
                    value = approvedPoints,
                    onValueChange = { approvedPoints = it },
                    label = { Text("Approved Points") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )
                OutlinedTextField(
                    value = remarks,
                    onValueChange = { remarks = it },
                    label = { Text("Remarks") },
                    modifier = Modifier.fillMaxWidth()
                )
            }
        },
        confirmButton = {
            TextButton(
                enabled = !isSaving,
                onClick = {
                    val pts = approvedPoints.toDoubleOrNull() ?: 0.0
                    onSubmit("Approved", pts, remarks.trim().ifBlank { null })
                }
            ) { Text(if (isSaving) "Saving..." else "Approve") }
        },
        dismissButton = {
            TextButton(
                enabled = !isSaving,
                onClick = { onSubmit("Rejected", 0.0, remarks.trim().ifBlank { null }) }
            ) { Text("Reject", color = Color(0xFFB91C1C)) }
        }
    )
}
