package com.example.features.admin.screens

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

@Composable
fun AdminAcademicCatalogScreen(onNavigate: (String) -> Unit) {
    var selectedTabIndex by remember { mutableStateOf(0) }
    val tabs = listOf("Degree Setup", "Course Setup")

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
                if (selectedTabIndex == 0) {
                    DegreeSetupView()
                } else {
                    CourseSetupView()
                }
            }
        }
    }
}

@Composable
private fun DegreeSetupView() {
    val degrees = listOf(
        DegreeItem("B.Tech Computer Science", "4 Years", "8 Semesters"),
        DegreeItem("M.Tech AI & Data Science", "2 Years", "4 Semesters")
    )
    LazyColumn(modifier = Modifier.fillMaxSize(), verticalArrangement = Arrangement.spacedBy(12.dp)) {
        items(degrees) { degree ->
            CamsCard {
                Column(modifier = Modifier.fillMaxWidth().padding(16.dp)) {
                    Text(degree.name, fontWeight = FontWeight.Bold, fontSize = 16.sp, color = CamsTextPrimary)
                    Spacer(Modifier.height(4.dp))
                    Text("${degree.duration} • ${degree.semesters}", fontSize = 13.sp, color = CamsTextSecondary)
                }
            }
        }
    }
}

@Composable
private fun CourseSetupView() {
    val courses = listOf(
        CourseItem("CS101", "Introduction to Programming", "3 Credits"),
        CourseItem("ME201", "Thermodynamics", "4 Credits"),
        CourseItem("EE102", "Basic Electrical", "3 Credits")
    )
    LazyColumn(modifier = Modifier.fillMaxSize(), verticalArrangement = Arrangement.spacedBy(12.dp)) {
        items(courses) { course ->
            CamsCard {
                Column(modifier = Modifier.fillMaxWidth().padding(16.dp)) {
                    Text(course.name, fontWeight = FontWeight.Bold, fontSize = 16.sp, color = CamsTextPrimary)
                    Spacer(Modifier.height(4.dp))
                    Text("${course.code} • ${course.credits}", fontSize = 13.sp, color = CamsTextSecondary)
                }
            }
        }
    }
}

data class DegreeItem(val name: String, val duration: String, val semesters: String)
data class CourseItem(val code: String, val name: String, val credits: String)
