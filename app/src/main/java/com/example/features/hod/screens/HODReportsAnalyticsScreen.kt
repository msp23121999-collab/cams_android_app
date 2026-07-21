package com.example.features.hod.screens

import android.content.Intent
import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.*
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.FileProvider
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.core.theme.*
import com.example.core.ui.CamsCard
import com.example.features.hod.providers.HODDepartmentAnalyticsViewModel
import com.example.features.hod.widgets.HODBaseScreen
import com.example.core.navigation.AppRoutes
import java.io.File

@Composable
fun HODReportsAnalyticsScreen(
    onNavigate: (String) -> Unit,
    viewModel: HODDepartmentAnalyticsViewModel = viewModel()
) {
    val context = LocalContext.current
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    var activeTab by remember { mutableStateOf("department") }

    LaunchedEffect(uiState.exportedCsv) {
        uiState.exportedCsv?.let { (fileName, csv) ->
            try {
                val dir = File(context.cacheDir, "reports").apply { mkdirs() }
                val file = File(dir, fileName)
                file.writeText(csv)
                val uri = FileProvider.getUriForFile(context, "${context.packageName}.fileprovider", file)
                val intent = Intent(Intent.ACTION_SEND).apply {
                    type = "text/csv"
                    putExtra(Intent.EXTRA_STREAM, uri)
                    addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                }
                context.startActivity(Intent.createChooser(intent, "Share $fileName"))
            } catch (e: Exception) {
                Toast.makeText(context, "Could not export file", Toast.LENGTH_SHORT).show()
            }
            viewModel.clearExportState()
        }
    }

    LaunchedEffect(uiState.exportError) {
        uiState.exportError?.let {
            Toast.makeText(context, it, Toast.LENGTH_SHORT).show()
            viewModel.clearExportState()
        }
    }

    HODBaseScreen(
        title = "Reports & Analytics",
        subtitle = "Generate official reports for review boards, academic councils, or department archives.",
        currentRoute = AppRoutes.HOD_REPORTS,
        onNavigate = onNavigate
    ) {
        ScrollableTabRow(
            selectedTabIndex = listOf("department", "students", "export").indexOf(activeTab).coerceAtLeast(0),
            containerColor = Color.Transparent,
            contentColor = CamsNavy,
            edgePadding = 0.dp,
            divider = {}
        ) {
            val tabs = listOf(
                "department" to "Department Reports",
                "students" to "Student Reports",
                "export" to "Export Reports"
            )
            tabs.forEach { (id, label) ->
                Tab(
                    selected = activeTab == id,
                    onClick = { activeTab = id },
                    text = { Text(label, fontWeight = if (activeTab == id) FontWeight.Bold else FontWeight.Medium) }
                )
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        if (uiState.isLoading) {
            Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator(color = Color(0xFF4338CA))
            }
        } else if (uiState.error != null) {
            Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Text("Error: ${uiState.error}", color = MaterialTheme.colorScheme.error)
            }
        } else if (activeTab == "department") {
            val summary = uiState.reportData?.summary
            val breakdown = uiState.reportData?.facultyBreakdown ?: emptyList()

            Row(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.fillMaxWidth()) {
                KpiCard("Active Faculty", "${summary?.activeFaculty ?: 0}", Icons.Filled.People, Color(0xFF64748B), Modifier.weight(1f))
                KpiCard("On Leave", "${summary?.facultyOnLeave ?: 0}", Icons.Filled.EventBusy, Color(0xFFF59E0B), Modifier.weight(1f))
                KpiCard("Avg Workload (hrs)", "${summary?.avgWorkloadHours ?: 0.0}", Icons.Filled.PublishedWithChanges, Color(0xFF10B981), Modifier.weight(1f))
                KpiCard("Verified Research", "${summary?.totalVerifiedResearch ?: 0}", Icons.AutoMirrored.Filled.MenuBook, Color(0xFF8B5CF6), Modifier.weight(1f))
            }

            Spacer(modifier = Modifier.height(16.dp))

            CamsCard(modifier = Modifier.fillMaxWidth().weight(1f)) {
                Text("Faculty Breakdown", fontWeight = FontWeight.Bold, fontSize = 16.sp, color = MaterialTheme.colorScheme.onSurface)
                Spacer(Modifier.height(12.dp))
                if (breakdown.isEmpty()) {
                    Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        Text("No faculty data available.", color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                } else {
                    LazyColumn(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                        items(breakdown, key = { it.facultyId }) { fac ->
                            Row(
                                modifier = Modifier.fillMaxWidth().background(MaterialTheme.colorScheme.surface, RoundedCornerShape(12.dp)).border(1.dp, MaterialTheme.colorScheme.outlineVariant, RoundedCornerShape(12.dp)).padding(12.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(fac.facultyName, fontWeight = FontWeight.Bold, fontSize = 14.sp, color = MaterialTheme.colorScheme.onSurface)
                                    Text("${fac.designation} • ${fac.workloadHours} hrs/week", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                }
                                Text("${fac.verifiedResearch} research • ${fac.materialsSubmitted} materials", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurface)
                            }
                        }
                    }
                }
            }
        } else if (activeTab == "students") {
            val summary = uiState.studentReportData?.summary
            val rows = uiState.studentReportData?.studentRows ?: emptyList()

            Row(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.fillMaxWidth()) {
                KpiCard("Students", "${summary?.totalStudents ?: 0}", Icons.Filled.Groups, Color(0xFF64748B), Modifier.weight(1f))
                KpiCard("Attendance Avg", "${summary?.overallAttendancePct ?: 0.0}%", Icons.Filled.PublishedWithChanges, Color(0xFF10B981), Modifier.weight(1f))
                KpiCard("Avg CGPA", "${summary?.averageCgpa ?: 0.0}", Icons.Filled.Grade, Color(0xFF8B5CF6), Modifier.weight(1f))
                KpiCard("With Arrears", "${summary?.totalArrears ?: 0}", Icons.Filled.Warning, Color(0xFFB45309), Modifier.weight(1f))
            }

            Spacer(modifier = Modifier.height(16.dp))

            CamsCard(modifier = Modifier.fillMaxWidth().weight(1f)) {
                Text("Student Breakdown", fontWeight = FontWeight.Bold, fontSize = 16.sp, color = MaterialTheme.colorScheme.onSurface)
                Spacer(Modifier.height(12.dp))
                if (rows.isEmpty()) {
                    Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        Text("No student data available.", color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                } else {
                    LazyColumn(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                        items(rows, key = { it.studentId }) { s ->
                            Row(
                                modifier = Modifier.fillMaxWidth().background(MaterialTheme.colorScheme.surface, RoundedCornerShape(12.dp)).border(1.dp, MaterialTheme.colorScheme.outlineVariant, RoundedCornerShape(12.dp)).padding(12.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(s.name, fontWeight = FontWeight.Bold, fontSize = 14.sp, color = MaterialTheme.colorScheme.onSurface)
                                    Text("Roll ${s.rollNo} • Sem ${s.semester} • CGPA ${s.cgpa}", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                }
                                Text("${s.attendancePct}% attendance", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurface)
                            }
                        }
                    }
                }
            }
        } else {
            CamsCard(modifier = Modifier.fillMaxWidth().weight(1f)) {
                Column(Modifier.fillMaxWidth(), horizontalAlignment = Alignment.CenterHorizontally) {
                    Text("Export Reports", fontWeight = FontWeight.Bold, fontSize = 16.sp, color = MaterialTheme.colorScheme.onSurface)
                    Spacer(Modifier.height(16.dp))
                    Button(
                        onClick = { viewModel.exportDepartmentCsv() },
                        enabled = !uiState.isExporting,
                        modifier = Modifier.fillMaxWidth(0.8f),
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF1E293B)),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Icon(Icons.Filled.Download, null, modifier = Modifier.size(16.dp))
                        Spacer(Modifier.width(8.dp))
                        Text(if (uiState.isExporting) "Exporting..." else "Export Department Report (CSV)")
                    }
                    Spacer(Modifier.height(12.dp))
                    Button(
                        onClick = { viewModel.exportStudentCsv() },
                        enabled = !uiState.isExporting,
                        modifier = Modifier.fillMaxWidth(0.8f),
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF1E293B)),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Icon(Icons.Filled.Download, null, modifier = Modifier.size(16.dp))
                        Spacer(Modifier.width(8.dp))
                        Text(if (uiState.isExporting) "Exporting..." else "Export Student Report (CSV)")
                    }
                }
            }
        }
    }
}

@Composable
private fun KpiCard(label: String, value: String, icon: androidx.compose.ui.graphics.vector.ImageVector, color: Color, modifier: Modifier) {
    Card(
        modifier = modifier,
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
        shape = RoundedCornerShape(12.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Icon(icon, contentDescription = null, tint = color, modifier = Modifier.size(24.dp))
            Spacer(modifier = Modifier.height(8.dp))
            Text(value, fontSize = 24.sp, fontWeight = FontWeight.Black, color = MaterialTheme.colorScheme.onSurface)
            Text(label, fontSize = 12.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
    }
}
