package com.example.features.parent.screens

import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.material3.MaterialTheme
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.core.theme.*
import com.example.core.ui.CamsScreen
import com.example.core.ui.CamsCard
import com.example.features.parent.providers.ParentMarksViewModel
import com.example.features.parent.widgets.ParentBaseScreen
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ExamResultsScreen(
    viewModel: ParentMarksViewModel,
    onNavigate: (String) -> Unit
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val context = androidx.compose.ui.platform.LocalContext.current

    ParentBaseScreen(
        title = "Exam Results",
        subtitle = "Semester Results & Internal Marks",
        currentRoute = "/parent/marks",
        onNavigate = onNavigate,
        scrollable = false
    ) {
        if (uiState.isLoading) {
            Box(modifier = Modifier.fillMaxWidth().height(200.dp), contentAlignment = Alignment.Center) {
                CircularProgressIndicator(color = LexNovaPurple)
            }
        } else if (uiState.error != null) {
            Box(modifier = Modifier.fillMaxWidth().height(200.dp), contentAlignment = Alignment.Center) {
                Text(uiState.error ?: "Failed to load results", color = Color.Red)
            }
        } else if (uiState.performance.isEmpty() && uiState.internalMarks.isEmpty()) {
            Box(modifier = Modifier.fillMaxWidth().height(200.dp), contentAlignment = Alignment.Center) {
                Text("No exam results available.", color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
        } else {
            PerformanceAnalytics(uiState.performance) {
                val currentChildId = viewModel.currentChildId ?: ""
                val token = com.example.core.network.AuthManagerImpl(context).getToken() ?: ""
                val base = com.example.core.config.AppConfig.BASE_URL.trimEnd('/')
                val url = "$base/students/parent/child/marks/export-pdf?child_id=$currentChildId"
                com.example.core.utils.DownloadHelper.downloadPdf(context, url, "Exam_Results_$currentChildId", token)
            }
            
            Text(
                "Internal Examination Marks",
                style = MaterialTheme.typography.titleLarge.copy(
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )
            )
            
            InternalMarksTable(uiState.internalMarks)
        }
    }
}

@Composable
fun PerformanceAnalytics(performance: List<com.example.features.parent.models.PerformanceData>, onDownloadClick: () -> Unit) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(modifier = Modifier.padding(24.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Filled.School, contentDescription = null, tint = LexNovaPurple)
                Spacer(modifier = Modifier.width(8.dp))
                Text("Semester Results & CGPA", style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold), modifier = Modifier.weight(1f))
                IconButton(onClick = onDownloadClick) {
                    Icon(Icons.Filled.Download, contentDescription = "Download PDF", tint = LexNovaPurple)
                }
            }
            Spacer(modifier = Modifier.height(24.dp))
            
            // Semester SGPA Grid
            Row(modifier = Modifier.horizontalScroll(rememberScrollState()), horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                performance.forEach { perf ->
                    Surface(
                        color = LexNovaPurple.copy(alpha = 0.05f),
                        shape = RoundedCornerShape(12.dp),
                        border = androidx.compose.foundation.BorderStroke(1.dp, LexNovaPurple.copy(alpha = 0.2f))
                    ) {
                        Column(modifier = Modifier.padding(16.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                            Text(perf.semester, style = MaterialTheme.typography.labelSmall.copy(color = LexNovaPurple, fontWeight = FontWeight.Bold))
                            Text("${perf.cgpa}", style = MaterialTheme.typography.headlineMedium.copy(fontWeight = FontWeight.Bold, color = LexNovaPurple))
                        }
                    }
                }
            }
            
            Spacer(modifier = Modifier.height(32.dp))
            Text("Attendance & Performance Analytics", style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold))
            Spacer(modifier = Modifier.height(16.dp))
            
            // Composed Chart
            Canvas(modifier = Modifier.fillMaxWidth().height(250.dp)) {
                val data = performance
                if (data.isEmpty()) return@Canvas
                
                val maxAttendance = 100f
                val maxCgpa = 10f
                
                val barWidth = 30.dp.toPx()
                val spacing = (size.width - (barWidth * data.size)) / (data.size + 1)
                
                val path = Path()
                var firstPoint = true
                val pathPoints = mutableListOf<Offset>()
                
                data.forEachIndexed { index, perf ->
                    val x = spacing + (index * (barWidth + spacing))
                    val centerX = x + (barWidth / 2)
                    
                    // Draw Attendance Bar (Right Y-Axis relative)
                    val attendanceHeight = (perf.attendance / maxAttendance) * size.height
                    val barY = size.height - attendanceHeight
                    drawRoundRect(
                        color = LexNovaPurple.copy(alpha = 0.15f),
                        topLeft = Offset(x, barY),
                        size = Size(barWidth, attendanceHeight),
                        cornerRadius = androidx.compose.ui.geometry.CornerRadius(4.dp.toPx(), 4.dp.toPx())
                    )
                    
                    // Calculate point for CGPA area chart (Left Y-Axis relative)
                    val cgpaHeight = ((perf.cgpa.toFloat()) / maxCgpa) * size.height
                    val cgpaY = size.height - cgpaHeight
                    
                    val point = Offset(centerX, cgpaY)
                    pathPoints.add(point)
                    
                    if (firstPoint) {
                        path.moveTo(point.x, point.y)
                        firstPoint = false
                    } else {
                        path.lineTo(point.x, point.y)
                    }
                }
                
                // Draw CGPA Area
                val fillPath = Path().apply {
                    addPath(path)
                    lineTo(pathPoints.last().x, size.height)
                    lineTo(pathPoints.first().x, size.height)
                    close()
                }
                drawPath(
                    path = fillPath,
                    brush = Brush.verticalGradient(
                        colors = listOf(LexNovaPurple.copy(alpha = 0.3f), Color.Transparent),
                        startY = 0f,
                        endY = size.height
                    )
                )
                
                // Draw CGPA Line
                drawPath(
                    path = path,
                    color = LexNovaPurple,
                    style = Stroke(width = 3.dp.toPx())
                )
                
                // Draw CGPA Points
                pathPoints.forEach { point ->
                    drawCircle(color = Color.White, radius = 6.dp.toPx(), center = point)
                    drawCircle(color = LexNovaPurple, radius = 4.dp.toPx(), center = point)
                }
            }
        }
    }
}

@Composable
fun InternalMarksTable(marks: List<com.example.features.parent.models.ChildInternalMark>) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        BoxWithConstraints {
            val isTablet = maxWidth > 600.dp
            val scrollState = rememberScrollState()
            Column(
                modifier = Modifier.fillMaxWidth()
                    .then(if (!isTablet) Modifier.horizontalScroll(scrollState) else Modifier)
            ) {
                Row(
                    modifier = Modifier.background(MaterialTheme.colorScheme.surfaceVariant).padding(16.dp),
                    horizontalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    Text("Subject", modifier = if (isTablet) Modifier.weight(2f) else Modifier.width(150.dp), fontWeight = FontWeight.Bold)
                    Text("Year", modifier = if (isTablet) Modifier.weight(1f) else Modifier.width(80.dp), fontWeight = FontWeight.Bold)
                    Text("Internal", modifier = if (isTablet) Modifier.weight(1f) else Modifier.width(80.dp), fontWeight = FontWeight.Bold)
                    Text("Assgn", modifier = if (isTablet) Modifier.weight(1f) else Modifier.width(80.dp), fontWeight = FontWeight.Bold)
                    Text("Pres", modifier = if (isTablet) Modifier.weight(1f) else Modifier.width(80.dp), fontWeight = FontWeight.Bold)
                    Text("Viva", modifier = if (isTablet) Modifier.weight(1f) else Modifier.width(80.dp), fontWeight = FontWeight.Bold)
                    Text("Attd", modifier = if (isTablet) Modifier.weight(1f) else Modifier.width(80.dp), fontWeight = FontWeight.Bold)
                    Text("Total", modifier = if (isTablet) Modifier.weight(1f) else Modifier.width(80.dp), fontWeight = FontWeight.Bold, color = LexNovaPurple)
                }
                
                marks.forEach { mark ->
                    Row(
                        modifier = Modifier.padding(16.dp),
                        horizontalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        Text(mark.subjectName, modifier = if (isTablet) Modifier.weight(2f) else Modifier.width(150.dp), maxLines = 2, overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis)
                        Text(mark.academicYear, modifier = if (isTablet) Modifier.weight(1f) else Modifier.width(80.dp))
                        Text(mark.internalExamMark, modifier = if (isTablet) Modifier.weight(1f) else Modifier.width(80.dp))
                        Text(mark.assignmentMark, modifier = if (isTablet) Modifier.weight(1f) else Modifier.width(80.dp))
                        Text(mark.presentationMark, modifier = if (isTablet) Modifier.weight(1f) else Modifier.width(80.dp))
                        Text(mark.vivaVoiceMark, modifier = if (isTablet) Modifier.weight(1f) else Modifier.width(80.dp))
                        Text(mark.attendanceMark, modifier = if (isTablet) Modifier.weight(1f) else Modifier.width(80.dp))
                        Text(mark.totalMark, modifier = if (isTablet) Modifier.weight(1f) else Modifier.width(80.dp), fontWeight = FontWeight.Bold, color = LexNovaPurple)
                    }
                    HorizontalDivider(color = Color.LightGray.copy(alpha = 0.3f))
                }
            }
        }
    }
}
