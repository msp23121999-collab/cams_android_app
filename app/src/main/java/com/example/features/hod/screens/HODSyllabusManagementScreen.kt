package com.example.features.hod.screens

import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import android.widget.Toast
import androidx.compose.ui.platform.LocalContext
import com.example.core.theme.*
import com.example.core.ui.CamsCard
import com.example.features.hod.widgets.HODBaseScreen
import com.example.core.navigation.AppRoutes
import com.example.core.network.HODCourseDto
import com.example.features.hod.providers.HODSyllabusManagementViewModel

@Composable
fun HODSyllabusManagementScreen(
    onNavigate: (String) -> Unit,
    viewModel: HODSyllabusManagementViewModel = viewModel()
) {
    val context = LocalContext.current
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    var selectedSemester by remember { mutableStateOf(1) }

    LaunchedEffect(uiState.savePlanSuccess) {
        if (uiState.savePlanSuccess) {
            Toast.makeText(context, "Unit plan saved", Toast.LENGTH_SHORT).show()
        }
    }

    HODBaseScreen(
        title = "Configure Department Syllabus",
        subtitle = "Select a semester to view courses and configure their units",
        currentRoute = AppRoutes.HOD_SYLLABUS_MGMT,
        onNavigate = onNavigate
    ) {
        if (uiState.isLoading) {
            Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator(color = Color(0xFF4338CA))
            }
        } else if (uiState.error != null) {
            Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Text("Error: ${uiState.error}", color = MaterialTheme.colorScheme.error)
            }
        } else {
            val semCount = uiState.metadata?.semCount ?: 10
            
            LazyRow(
                Modifier
                    .fillMaxWidth()
                    .background(MaterialTheme.colorScheme.surface, RoundedCornerShape(12.dp))
                    .padding(4.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(semCount) { i ->
                    val sem = i + 1
                    Button(
                        onClick = { selectedSemester = sem },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = if (selectedSemester == sem) Color(0xFFEEF2FF) else Color.Transparent,
                            contentColor = if (selectedSemester == sem) Color(0xFF4338CA) else Color(0xFF64748B)
                        ),
                        shape = RoundedCornerShape(8.dp),
                        elevation = null
                    ) {
                        Text("Semester $sem", fontSize = 13.sp, fontWeight = FontWeight.Bold)
                    }
                }
            }

            Spacer(Modifier.height(16.dp))

            CamsCard(modifier = Modifier.fillMaxWidth().weight(1f)) {
                Text("Courses List", fontWeight = FontWeight.Bold, fontSize = 16.sp, color = MaterialTheme.colorScheme.onSurface)
                Spacer(Modifier.height(12.dp))
                
                val filteredCourses = uiState.courses.filter { it.semester == selectedSemester }
                
                if (filteredCourses.isEmpty()) {
                    Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        Text("No courses found for Semester $selectedSemester.", color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                } else {
                    LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        items(filteredCourses) { course ->
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .background(MaterialTheme.colorScheme.background, RoundedCornerShape(12.dp))
                                    .border(1.dp, MaterialTheme.colorScheme.surfaceVariant, RoundedCornerShape(12.dp))
                                    .padding(12.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(course.name, fontWeight = FontWeight.Bold, fontSize = 14.sp, color = MaterialTheme.colorScheme.onSurface)
                                    Text("Code: ${course.code} • ${course.credits} Credits", fontSize = 13.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                }
                                Button(
                                    onClick = { viewModel.openCoursePlan(course) },
                                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF4F46E5)),
                                    shape = RoundedCornerShape(8.dp)
                                ) {
                                    Text("Manage Units", fontSize = 13.sp, fontWeight = FontWeight.Bold)
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    uiState.selectedCourse?.let { course ->
        ManageUnitsDialog(
            course = course,
            unitPlan = uiState.unitPlan,
            isLoading = uiState.isLoadingPlan,
            isSaving = uiState.isSavingPlan,
            error = uiState.planError,
            onDismiss = { viewModel.closeCoursePlan() },
            onSave = { units -> viewModel.saveCoursePlan(units) }
        )
    }
}

@Composable
private fun ManageUnitsDialog(
    course: HODCourseDto,
    unitPlan: Map<String, List<String>>,
    isLoading: Boolean,
    isSaving: Boolean,
    error: String?,
    onDismiss: () -> Unit,
    onSave: (Map<String, List<String>>) -> Unit
) {
    var units by remember(unitPlan) {
        mutableStateOf(
            if (unitPlan.isEmpty()) listOf("Unit 1" to "") else unitPlan.map { (k, v) -> k to v.joinToString("\n") }
        )
    }

    LaunchedEffect(unitPlan) {
        units = if (unitPlan.isEmpty()) listOf("Unit 1" to "") else unitPlan.map { (k, v) -> k to v.joinToString("\n") }
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Manage Units — ${course.name}") },
        text = {
            if (isLoading) {
                Box(Modifier.fillMaxWidth().height(120.dp), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator()
                }
            } else {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    error?.let { Text(it, color = Color(0xFFB91C1C), fontSize = 12.sp) }
                    units.forEachIndexed { index, (unitName, topics) ->
                        OutlinedTextField(
                            value = topics,
                            onValueChange = { newVal ->
                                units = units.toMutableList().also { it[index] = unitName to newVal }
                            },
                            label = { Text("$unitName (one topic per line)") },
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                    TextButton(onClick = {
                        units = units + ("Unit ${units.size + 1}" to "")
                    }) { Text("+ Add Unit") }
                }
            }
        },
        confirmButton = {
            TextButton(
                enabled = !isLoading && !isSaving,
                onClick = {
                    val payload = units.associate { (name, topics) ->
                        name to topics.split("\n").map { it.trim() }.filter { it.isNotBlank() }
                    }
                    onSave(payload)
                }
            ) { Text(if (isSaving) "Saving..." else "Save") }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Cancel") }
        }
    )
}
