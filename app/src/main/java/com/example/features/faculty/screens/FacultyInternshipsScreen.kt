package com.example.features.faculty.screens

import android.widget.Toast
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.rememberScrollState
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
import com.example.core.network.PartnerCompanyRequest
import com.example.core.network.InternshipApplicationDto
import com.example.core.network.PartnerCompanyDto
import com.example.features.faculty.providers.FacultyInternshipsViewModel
import com.example.features.faculty.providers.FacultyInternshipsViewModelFactory

@Composable
fun FacultyInternshipsScreen(onNavigate: (String) -> Unit) {
    val context = LocalContext.current
    val repository = remember { FacultyRepositoryImpl(com.example.CamsApplication.instance.container.apiService) }
    val factory = remember { FacultyInternshipsViewModelFactory(repository) }
    val viewModel: FacultyInternshipsViewModel = viewModel(factory = factory)
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    var selectedTab by remember { mutableStateOf(0) }
    var showAddPartnerDialog by remember { mutableStateOf(false) }
    val tabs = listOf("Applications", "Opportunities", "Partners")

    LaunchedEffect(uiState.reviewError) {
        uiState.reviewError?.let {
            Toast.makeText(context, it, Toast.LENGTH_SHORT).show()
            viewModel.clearReviewError()
        }
    }
    LaunchedEffect(uiState.partnerSaveSuccess, uiState.partnerError) {
        if (uiState.partnerSaveSuccess) {
            Toast.makeText(context, "Partner saved", Toast.LENGTH_SHORT).show()
            showAddPartnerDialog = false
            viewModel.clearPartnerStatus()
        }
        uiState.partnerError?.let {
            Toast.makeText(context, it, Toast.LENGTH_SHORT).show()
            viewModel.clearPartnerStatus()
        }
    }

    val applicants = uiState.applications.size
    val pending = uiState.applications.count { it.status == "Applied" || it.status == "Shortlisted" }
    val placed = uiState.applications.count { it.status == "Selected" }

    FacultyBaseScreen(scrollable = false,
        title = "Internships & Placements",
        currentRoute = com.example.core.navigation.AppRoutes.FACULTY_INTERNSHIPS,
        onNavigate = onNavigate,
        floatingActionButton = {
            if (selectedTab == 2) {
                FloatingActionButton(
                    onClick = { showAddPartnerDialog = true },
                    containerColor = CamsNavy,
                    contentColor = Color.White,
                    shape = RoundedCornerShape(16.dp)
                ) {
                    Icon(Icons.Filled.Add, "Add Partner")
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
                InternshipStatsCard("Applicants", "$applicants", Icons.Filled.Groups, Color(0xFF3B82F6), Modifier.weight(1f))
                InternshipStatsCard("Pending", "$pending", Icons.Filled.HourglassEmpty, Color(0xFFF59E0B), Modifier.weight(1f))
                InternshipStatsCard("Placed", "$placed", Icons.Filled.CheckCircle, Color(0xFF10B981), Modifier.weight(1f))
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
                    0 -> StudentApplicationsList(uiState.applications, onReview = { id, status -> viewModel.reviewApplication(id, status) })
                    1 -> InternshipOpportunitiesList(uiState.drives)
                    2 -> PartnerCompaniesList(uiState.partners, onDelete = { viewModel.deletePartner(it) })
                }
            }
        }
    }

    if (showAddPartnerDialog) {
        AddPartnerDialog(
            isSaving = uiState.isSavingPartner,
            onDismiss = { showAddPartnerDialog = false },
            onSubmit = { viewModel.createPartner(it) }
        )
    }
}

@Composable
private fun InternshipStatsCard(
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
private fun StudentApplicationsList(
    applications: List<InternshipApplicationDto>,
    onReview: (String, String) -> Unit
) {
    if (applications.isEmpty()) {
        Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Text("No applications yet", color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
        return
    }
    LazyColumn(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        items(applications, key = { it.id }) { app ->
            var showMenu by remember { mutableStateOf(false) }
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
                    Column(modifier = Modifier.weight(1f)) {
                        Text(app.studentName ?: app.studentId, fontWeight = FontWeight.Bold, fontSize = 16.sp, color = MaterialTheme.colorScheme.onSurface)
                        Text("${app.role ?: "-"} @ ${app.companyName ?: "-"}", fontSize = 13.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        Spacer(modifier = Modifier.height(8.dp))
                        Surface(
                            color = when (app.status) {
                                "Selected" -> Color(0xFF10B981).copy(alpha = 0.1f)
                                "Shortlisted" -> Color(0xFF3B82F6).copy(alpha = 0.1f)
                                "Rejected" -> Color(0xFFB91C1C).copy(alpha = 0.1f)
                                else -> Color(0xFFF59E0B).copy(alpha = 0.1f)
                            },
                            shape = RoundedCornerShape(8.dp)
                        ) {
                            Text(
                                app.status,
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Bold,
                                color = when (app.status) {
                                    "Selected" -> Color(0xFF10B981)
                                    "Shortlisted" -> Color(0xFF3B82F6)
                                    "Rejected" -> Color(0xFFB91C1C)
                                    else -> Color(0xFFF59E0B)
                                }
                            )
                        }
                    }
                    Box {
                        IconButton(onClick = { showMenu = true }) {
                            Icon(Icons.Filled.ChevronRight, contentDescription = "Open", tint = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                        DropdownMenu(expanded = showMenu, onDismissRequest = { showMenu = false }) {
                            listOf("Shortlisted", "Selected", "Rejected").forEach { status ->
                                DropdownMenuItem(text = { Text(status) }, onClick = {
                                    onReview(app.id, status)
                                    showMenu = false
                                })
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun InternshipOpportunitiesList(opportunities: List<com.example.core.network.FacultyInternshipDriveDto>) {
    if (opportunities.isEmpty()) {
        Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Text("No internship drives posted", color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
        return
    }
    LazyColumn(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        items(opportunities) { opp ->
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
                        Column {
                            Text(opp.role, fontWeight = FontWeight.Bold, fontSize = 16.sp, color = MaterialTheme.colorScheme.onSurface)
                            Text(opp.company, fontSize = 13.sp, color = CamsNavy)
                        }
                        Text(opp.status, fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                    Spacer(modifier = Modifier.height(12.dp))
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Filled.CalendarToday, null, tint = MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.size(14.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(opp.driveDate ?: "No date set", fontSize = 13.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        opp.packageInfo?.let {
                            Spacer(modifier = Modifier.width(12.dp))
                            Icon(Icons.Filled.Payments, null, tint = MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.size(14.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(it, fontSize = 13.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun PartnerCompaniesList(partners: List<PartnerCompanyDto>, onDelete: (String) -> Unit) {
    if (partners.isEmpty()) {
        Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Text("No partner companies yet", color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
        return
    }
    LazyColumn(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        items(partners, key = { it.id }) { partner ->
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
                    Surface(
                        modifier = Modifier.size(40.dp),
                        shape = RoundedCornerShape(8.dp),
                        color = CamsNavy.copy(alpha = 0.1f)
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Text(partner.name.take(1), fontWeight = FontWeight.Bold, color = CamsNavy)
                        }
                    }
                    Spacer(modifier = Modifier.width(16.dp))
                    Column(modifier = Modifier.weight(1f)) {
                        Text(partner.name, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurface)
                        Text(partner.industry, fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                    Surface(
                        color = if (partner.status == "Active") Color(0xFF10B981).copy(alpha = 0.1f) else Color(0xFFF59E0B).copy(alpha = 0.1f),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Text(
                            partner.status,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            color = if (partner.status == "Active") Color(0xFF10B981) else Color(0xFFF59E0B)
                        )
                    }
                    IconButton(onClick = { onDelete(partner.id) }) {
                        Icon(Icons.Filled.Delete, "Delete", tint = MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.size(18.dp))
                    }
                }
            }
        }
    }
}

@Composable
private fun AddPartnerDialog(
    isSaving: Boolean,
    onDismiss: () -> Unit,
    onSubmit: (PartnerCompanyRequest) -> Unit
) {
    var name by remember { mutableStateOf("") }
    var industry by remember { mutableStateOf("") }
    var contactEmail by remember { mutableStateOf("") }
    var contactPhone by remember { mutableStateOf("") }
    val isValid = name.isNotBlank() && industry.isNotBlank()

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Add Partner Company") },
        text = {
            Column(
                modifier = Modifier.verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                OutlinedTextField(value = name, onValueChange = { name = it }, label = { Text("Company Name") }, modifier = Modifier.fillMaxWidth(), singleLine = true)
                OutlinedTextField(value = industry, onValueChange = { industry = it }, label = { Text("Industry") }, modifier = Modifier.fillMaxWidth(), singleLine = true)
                OutlinedTextField(value = contactEmail, onValueChange = { contactEmail = it }, label = { Text("Contact Email") }, modifier = Modifier.fillMaxWidth(), singleLine = true)
                OutlinedTextField(value = contactPhone, onValueChange = { contactPhone = it }, label = { Text("Contact Phone") }, modifier = Modifier.fillMaxWidth(), singleLine = true)
            }
        },
        confirmButton = {
            TextButton(
                enabled = isValid && !isSaving,
                onClick = {
                    onSubmit(
                        PartnerCompanyRequest(
                            name = name.trim(),
                            industry = industry.trim(),
                            contactEmail = contactEmail.trim().ifBlank { null },
                            contactPhone = contactPhone.trim().ifBlank { null }
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
