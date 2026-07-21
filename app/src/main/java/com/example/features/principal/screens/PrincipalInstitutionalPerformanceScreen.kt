package com.example.features.principal.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
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
import com.example.core.repository.PrincipalRepositoryImpl
import com.example.core.theme.*
import com.example.core.ui.CamsCard
import com.example.features.principal.models.DepartmentPerformanceSummary
import com.example.features.principal.providers.PrincipalPerformanceViewModel
import com.example.features.principal.providers.PrincipalPerformanceViewModelFactory
import com.example.features.principal.widgets.PrincipalBaseScreen

@Composable
fun PrincipalInstitutionalPerformanceScreen(
    onNavigate: (String) -> Unit,
    viewModel: PrincipalPerformanceViewModel = viewModel(
        factory = PrincipalPerformanceViewModelFactory(PrincipalRepositoryImpl(CamsApplication.instance.container.apiService))
    )
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    PrincipalBaseScreen(
        title = "Institutional Performance",
        subtitle = "Department-by-department comparison across the institution",
        currentRoute = "/principal/performance",
        onNavigate = onNavigate
    ) {
        uiState.error?.let {
            Text(it, color = Color(0xFFB91C1C), fontSize = 13.sp, modifier = Modifier.padding(bottom = 8.dp))
        }

        if (uiState.isLoading) {
            Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) { CircularProgressIndicator() }
        } else if (uiState.departments.isEmpty()) {
            Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Text("No departments configured yet", color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
        } else {
            LazyColumn(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                items(uiState.departments, key = { it.deptId }) { dept ->
                    DepartmentPerformanceCard(dept)
                }
            }
        }
    }
}

@Composable
private fun DepartmentPerformanceCard(dept: DepartmentPerformanceSummary) {
    CamsCard {
        Text(dept.deptName, fontWeight = FontWeight.Bold, fontSize = 17.sp, color = MaterialTheme.colorScheme.onSurface)
        Spacer(Modifier.height(12.dp))

        Row(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.fillMaxWidth()) {
            MetricChip("Active Faculty", "${dept.activeFaculty}", Color(0xFF4338CA), Modifier.weight(1f))
            MetricChip("On Leave", "${dept.facultyOnLeave}", Color(0xFFB45309), Modifier.weight(1f))
            MetricChip("Avg Workload", "${dept.avgWorkloadHours}h", Color(0xFF047857), Modifier.weight(1f))
        }
        Spacer(Modifier.height(8.dp))
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.fillMaxWidth()) {
            MetricChip("Absences", "${dept.totalAbsences}", Color(0xFFB91C1C), Modifier.weight(1f))
            MetricChip("Substitutions", "${dept.completedSubstitutions}", Color(0xFF0369A1), Modifier.weight(1f))
        }
        Spacer(Modifier.height(8.dp))
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.fillMaxWidth()) {
            MetricChip("Verified Research", "${dept.verifiedResearch}", Color(0xFF6D28D9), Modifier.weight(1f))
            MetricChip("Materials Approved", "${dept.materialsApproved}", Color(0xFF0F766E), Modifier.weight(1f))
        }
    }
}

@Composable
private fun MetricChip(label: String, value: String, color: Color, modifier: Modifier) {
    Column(
        modifier = modifier
            .background(color.copy(alpha = 0.08f), RoundedCornerShape(10.dp))
            .padding(10.dp)
    ) {
        Text(value, fontWeight = FontWeight.Black, fontSize = 18.sp, color = color)
        Text(label, fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
    }
}
