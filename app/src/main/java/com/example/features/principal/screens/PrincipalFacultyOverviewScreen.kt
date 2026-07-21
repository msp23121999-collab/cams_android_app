package com.example.features.principal.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.CamsApplication
import com.example.core.navigation.AppRoutes
import com.example.core.network.PrincipalFacultyOverviewDto
import com.example.core.repository.PrincipalRepositoryImpl
import com.example.core.theme.*
import com.example.features.principal.providers.PrincipalFacultyOverviewViewModel
import com.example.features.principal.providers.PrincipalFacultyOverviewViewModelFactory
import com.example.features.principal.widgets.PrincipalBaseScreen

@Composable
fun PrincipalFacultyOverviewScreen(
    onNavigate: (String) -> Unit,
    viewModel: PrincipalFacultyOverviewViewModel = viewModel(
        factory = PrincipalFacultyOverviewViewModelFactory(PrincipalRepositoryImpl(CamsApplication.instance.container.apiService))
    )
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    var searchQuery by remember { mutableStateOf("") }
    var selectedDept by remember { mutableStateOf("All") }

    val departments = remember(uiState.faculty) {
        listOf("All") + uiState.faculty.mapNotNull { it.departmentName }.distinct().sorted()
    }
    val filtered = uiState.faculty.filter { f ->
        (selectedDept == "All" || f.departmentName == selectedDept) &&
            (searchQuery.isBlank() || f.fullName.contains(searchQuery, true) || (f.designation.contains(searchQuery, true)))
    }
    val onLeaveCount = uiState.faculty.count { it.isOnLeaveToday }
    val hodCount = uiState.faculty.count { it.role == "HOD" }

    PrincipalBaseScreen(
        title = "Faculty Overview",
        subtitle = "Institution-wide faculty directory across all departments.",
        currentRoute = AppRoutes.PRINCIPAL_FACULTY_OVERVIEW,
        onNavigate = onNavigate
    ) {
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.fillMaxWidth()) {
            KpiCard("Total Faculty", "${uiState.faculty.size}", Icons.Filled.Groups, Color(0xFF3B82F6), Modifier.weight(1f))
            KpiCard("HODs", "$hodCount", Icons.Filled.Star, Color(0xFF8B5CF6), Modifier.weight(1f))
            KpiCard("On Leave Today", "$onLeaveCount", Icons.Filled.EventBusy, Color(0xFFEF4444), Modifier.weight(1f))
            KpiCard("Departments", "${departments.size - 1}", Icons.Filled.Domain, Color(0xFF10B981), Modifier.weight(1f))
        }

        Spacer(Modifier.height(16.dp))

        OutlinedTextField(
            value = searchQuery,
            onValueChange = { searchQuery = it },
            modifier = Modifier.fillMaxWidth(),
            placeholder = { Text("Search by name or designation...") },
            leadingIcon = { Icon(Icons.Filled.Search, null) },
            singleLine = true,
            shape = MaterialTheme.shapes.medium
        )

        Spacer(Modifier.height(12.dp))

        ScrollableTabRow(
            selectedTabIndex = departments.indexOf(selectedDept).coerceAtLeast(0),
            containerColor = Color.Transparent,
            contentColor = CamsNavy,
            edgePadding = 0.dp,
            divider = {}
        ) {
            departments.forEach { dept ->
                Tab(
                    selected = selectedDept == dept,
                    onClick = { selectedDept = dept },
                    text = { Text(dept, fontSize = 12.sp, fontWeight = if (selectedDept == dept) FontWeight.Bold else FontWeight.Medium) }
                )
            }
        }

        Spacer(Modifier.height(12.dp))

        uiState.error?.let { Text(it, color = Color(0xFFB91C1C), fontSize = 13.sp, modifier = Modifier.padding(bottom = 8.dp)) }

        if (uiState.isLoading) {
            Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) { CircularProgressIndicator(color = CamsNavy) }
        } else if (filtered.isEmpty()) {
            Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Text("No faculty found", color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
        } else {
            LazyColumn(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                items(filtered, key = { it.id }) { faculty ->
                    FacultyRow(faculty)
                }
            }
        }
    }
}

@Composable
private fun FacultyRow(faculty: PrincipalFacultyOverviewDto) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Row(Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
            Box(
                modifier = Modifier.size(44.dp).background(if (faculty.role == "HOD") Color(0xFFF3E8FF) else Color(0xFFEEF2FF), CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Text(faculty.fullName.take(2).uppercase(), fontWeight = FontWeight.Bold, color = if (faculty.role == "HOD") Color(0xFF6D28D9) else Color(0xFF4338CA))
            }
            Spacer(Modifier.width(12.dp))
            Column(modifier = Modifier.weight(1f)) {
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    Text(faculty.fullName, fontWeight = FontWeight.Bold, fontSize = 14.sp, color = MaterialTheme.colorScheme.onSurface)
                    if (faculty.role == "HOD") {
                        Text("HOD", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = Color(0xFF6D28D9), modifier = Modifier.background(Color(0xFFF3E8FF), RoundedCornerShape(4.dp)).padding(horizontal = 4.dp, vertical = 1.dp))
                    }
                    if (faculty.isOnLeaveToday) {
                        Text("ON LEAVE", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = Color(0xFFB91C1C), modifier = Modifier.background(Color(0xFFFEE2E2), RoundedCornerShape(4.dp)).padding(horizontal = 4.dp, vertical = 1.dp))
                    }
                }
                Text(faculty.designation, fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                Text(faculty.departmentName ?: "Unassigned", fontSize = 12.sp, color = Color(0xFF64748B))
            }
            Column(horizontalAlignment = Alignment.End) {
                Text(faculty.email, fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                faculty.employeeCode?.let { Text(it, fontSize = 11.sp, fontWeight = FontWeight.Bold, color = CamsNavy) }
            }
        }
    }
}

@Composable
private fun KpiCard(label: String, value: String, icon: androidx.compose.ui.graphics.vector.ImageVector, color: Color, modifier: Modifier) {
    Card(modifier = modifier, colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface), elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)) {
        Column(modifier = Modifier.padding(12.dp)) {
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.Top) {
                Text(label, fontSize = 11.sp, fontWeight = FontWeight.Black, color = Color(0xFF64748B), modifier = Modifier.weight(1f), maxLines = 1)
                Box(Modifier.background(color.copy(alpha = 0.1f), RoundedCornerShape(8.dp)).padding(4.dp)) {
                    Icon(icon, null, tint = color, modifier = Modifier.size(16.dp))
                }
            }
            Text(value, fontSize = 20.sp, fontWeight = FontWeight.Black, color = MaterialTheme.colorScheme.onSurface, modifier = Modifier.padding(top = 8.dp))
        }
    }
}
