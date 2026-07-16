package com.example.features.admin.screens

import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.*
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.core.theme.*
import com.example.core.ui.CamsCard
import com.example.features.admin.widgets.AdminBaseScreen
import com.example.core.navigation.AppRoutes

import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.features.admin.providers.AdminAcademicCatalogViewModel
import com.example.core.repository.AdminRepositoryImpl
import com.example.core.network.ApiClient

@Composable
fun AdminAcademicCatalogScreen(
    onNavigate: (String) -> Unit,
    viewModel: AdminAcademicCatalogViewModel = viewModel(
        factory = object : androidx.lifecycle.ViewModelProvider.Factory {
            override fun <T : androidx.lifecycle.ViewModel> create(modelClass: Class<T>): T {
                return AdminAcademicCatalogViewModel(AdminRepositoryImpl(com.example.CamsApplication.instance.container.apiService)) as T
            }
        }
    )
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    var selectedTabIndex by remember { mutableStateOf(0) }
    val tabs = listOf("Departments", "Degree Setup", "Course Setup")

    AdminBaseScreen(
        title = "Academic Catalog",
        currentRoute = AppRoutes.ADMIN_ACADEMIC_CATALOG,
        onNavigate = onNavigate,
        floatingActionButton = {
            FloatingActionButton(onClick = {}, containerColor = CamsNavy, contentColor = Color.White) {
                Icon(Icons.Filled.Add, "Add Item")
            }
        }
    ) {
        Column(modifier = Modifier.fillMaxSize()) {
            TabRow(
                selectedTabIndex = selectedTabIndex,
                containerColor = MaterialTheme.colorScheme.surface,
                contentColor = CamsNavy
            ) {
                tabs.forEachIndexed { index, title ->
                    Tab(
                        selected = selectedTabIndex == index,
                        onClick = { selectedTabIndex = index },
                        text = { Text(title, fontWeight = FontWeight.Bold) }
                    )
                }
            }
            
            Box(modifier = Modifier.fillMaxSize().padding(16.dp)) {
                if (uiState.isLoading) {
                    CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
                } else {
                    if (selectedTabIndex == 0) {
                        DepartmentSetupView(uiState.departments)
                    } else if (selectedTabIndex == 1) {
                        DegreeSetupView(uiState.degrees)
                    } else {
                        CourseSetupView(uiState.courses)
                    }
                }
            }
        }
    }
}

@Composable
private fun DepartmentSetupView(departments: List<com.example.features.admin.models.AdminDepartment>) {
    LazyColumn(modifier = Modifier.fillMaxSize(), verticalArrangement = Arrangement.spacedBy(12.dp)) {
        items(departments) { dept ->
            CamsCard {
                Column(modifier = Modifier.fillMaxWidth().padding(16.dp)) {
                    Text(dept.name, fontWeight = FontWeight.Bold, fontSize = 16.sp, color = MaterialTheme.colorScheme.onSurface)
                    Spacer(Modifier.height(4.dp))
                    Text("Code: ${dept.code} • HOD: ${dept.hodId ?: "Not Assigned"}", fontSize = 13.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            }
        }
    }
}

@Composable
private fun DegreeSetupView(degrees: List<com.example.features.admin.models.AdminDegree>) {
    LazyColumn(modifier = Modifier.fillMaxSize(), verticalArrangement = Arrangement.spacedBy(12.dp)) {
        items(degrees) { degree ->
            CamsCard {
                Column(modifier = Modifier.fillMaxWidth().padding(16.dp)) {
                    Text(degree.name, fontWeight = FontWeight.Bold, fontSize = 16.sp, color = MaterialTheme.colorScheme.onSurface)
                    Spacer(Modifier.height(4.dp))
                    Text("${degree.durationYears ?: 0} Years • Level: ${degree.programLevel ?: "N/A"}", fontSize = 13.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            }
        }
    }
}

@Composable
private fun CourseSetupView(courses: List<com.example.features.admin.models.AdminCourse>) {
    LazyColumn(modifier = Modifier.fillMaxSize(), verticalArrangement = Arrangement.spacedBy(12.dp)) {
        items(courses) { course ->
            CamsCard {
                Column(modifier = Modifier.fillMaxWidth().padding(16.dp)) {
                    Text(course.name, fontWeight = FontWeight.Bold, fontSize = 16.sp, color = MaterialTheme.colorScheme.onSurface)
                    Spacer(Modifier.height(4.dp))
                    Text("${course.code} • ${course.credits ?: 0} Credits • Sem ${course.semester}", fontSize = 13.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            }
        }
    }
}
