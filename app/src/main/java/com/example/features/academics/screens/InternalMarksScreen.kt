package com.example.features.academics.screens

import androidx.lifecycle.compose.collectAsStateWithLifecycle
import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Label
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.core.navigation.AppRoutes
import com.example.core.theme.*
import com.example.core.ui.CamsCard
import com.example.core.ui.CamsScreen
import com.example.core.ui.shimmerEffect
import com.example.features.academics.models.InternalMarkRecord
import com.example.features.student.providers.MarksViewModel
import com.example.features.student.widgets.StudentDrawer
import kotlinx.coroutines.launch
import kotlin.math.roundToInt

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun InternalMarksScreen(
    viewModel: MarksViewModel,
    onNavigate: (String) -> Unit
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val context = LocalContext.current

    CamsScreen(scrollable = false,
        title = "Internal Marks",
        onBackClick = { onNavigate(AppRoutes.STUDENT_DASHBOARD) },
        actions = {
            IconButton(
                onClick = {
                    val token = com.example.core.network.AuthManagerImpl(context).getToken() ?: ""
                    val base = com.example.core.config.AppConfig.BASE_URL.trimEnd('/')
                    val url = "$base/marks/internal/student/me/export-pdf"
                    com.example.core.utils.DownloadHelper.downloadPdf(context, url, "internal_marks", token)
                },
                modifier = Modifier
                    .clip(RoundedCornerShape(12.dp))
                    .background(MaterialTheme.colorScheme.surface.copy(alpha = 0.15f))
            ) {
                Icon(Icons.Filled.FileDownload, contentDescription = "Download marks report", tint = Color.White)
            }
        }
    ) {
        if (uiState.isLoading) {
            MarksSkeleton()
        } else if (uiState.error != null) {
            com.example.core.ui.NetworkErrorView(
                message = uiState.error!!,
                onRetry = { viewModel.fetchMarks() },
                modifier = Modifier.fillMaxSize()
            )
        } else {
            Column(
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                StudentProfileCard(
                    studentName = uiState.studentName,
                    rollNo = uiState.rollNo,
                    degree = uiState.degree,
                    semester = uiState.semester
                )
                InternalMarksSummary(uiState.marks)
                if (uiState.marks.isEmpty()) {
                    Box(modifier = Modifier.fillMaxWidth().padding(32.dp), contentAlignment = Alignment.Center) {
                        Text("No internal marks available yet.", color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                } else {
                    MarksTable(uiState.marks)
                }
            }
        }
    }
}

@Composable
fun StudentProfileCard(
    studentName: String = "",
    rollNo: String = "",
    degree: String = "",
    semester: Int = 0
) {
    val initials = studentName.split(" ").filter { it.isNotBlank() }.take(2).mapNotNull { it.firstOrNull()?.uppercase() }.joinToString("").ifBlank { "?" }
    CamsCard(
        modifier = Modifier.fillMaxWidth(),
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(64.dp)
                    .clip(CircleShape)
                    .background(CamsNavy.copy(alpha = 0.1f)),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    initials,
                    style = MaterialTheme.typography.headlineSmall.copy(fontWeight = FontWeight.Black),
                    color = CamsNavy
                )
            }
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    studentName.ifBlank { "Student" },
                    style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Black),
                    color = MaterialTheme.colorScheme.onSurface
                )
                Text(
                    "Reg No: ${rollNo.ifBlank { "N/A" }} • Sem ${if (semester > 0) semester else "-"}",
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(modifier = Modifier.height(4.dp))
                Box(
                    modifier = Modifier
                        .clip(CircleShape)
                        .background(CamsNavy.copy(alpha = 0.05f))
                        .padding(horizontal = 10.dp, vertical = 2.dp)
                ) {
                    Text(
                        degree.ifBlank { "N/A" },
                        style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                        color = CamsNavy
                    )
                }
            }
        }
    }
}

@Composable
fun InternalMarksSummary(marks: List<InternalMarkRecord>) {
    val approvedMarks = marks.filter { it.isApproved }
    val totalObtained = approvedMarks.sumOf { it.totalScore }
    val totalMax = approvedMarks.sumOf { it.maxMark }
    val percentage = if (totalMax > 0) (totalObtained / totalMax * 100).toInt() else 0

    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
        SummaryStatCard(
            Modifier.weight(1f),
            "Overall Avg",
            "$percentage%",
            Icons.Filled.TrendingUp,
            Color(0xFF10B981)
        )
        SummaryStatCard(
            Modifier.weight(1f),
            "Total Score",
            "${totalObtained.toInt()}/${totalMax.toInt()}",
            Icons.Filled.BarChart,
            Color(0xFF3B82F6)
        )
    }
}

@Composable
fun SummaryStatCard(modifier: Modifier, label: String, value: String, icon: ImageVector, color: Color) {
    CamsCard(
        modifier = modifier,
    ) {
        Column {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(32.dp)
                        .clip(RoundedCornerShape(8.dp))
                        .background(color.copy(alpha = 0.1f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(icon, contentDescription = null, tint = color, modifier = Modifier.size(16.dp))
                }
                Text(
                    value,
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Black),
                    color = MaterialTheme.colorScheme.onSurface
                )
            }
            Spacer(modifier = Modifier.height(12.dp))
            Text(label, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
    }
}

@Composable
fun MarksTable(marks: List<InternalMarkRecord>) {
    CamsCard(
        modifier = Modifier.fillMaxWidth(),
    ) {
        Column {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(CamsBackground)
                    .padding(vertical = 12.dp, horizontal = 4.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(Icons.Filled.ListAlt, contentDescription = null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(18.dp))
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    "Detailed Assessment",
                    style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.Bold),
                    color = MaterialTheme.colorScheme.onSurface
                )
            }
            
            HorizontalDivider(color = Color.LightGray.copy(alpha = 0.2f))

            val scrollState = rememberScrollState()
            
            Column(modifier = Modifier.horizontalScroll(scrollState)) {
                // Table Header
                Row(
                    modifier = Modifier
                        .background(CamsBackground.copy(alpha = 0.5f))
                        .padding(horizontal = 16.dp, vertical = 12.dp),
                    horizontalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    TableHeaderCell("Subject", 160.dp)
                    TableHeaderCell("Exam", 70.dp)
                    TableHeaderCell("Assign.", 70.dp)
                    TableHeaderCell("Present.", 70.dp)
                    TableHeaderCell("Viva", 70.dp)
                    TableHeaderCell("Attend.", 70.dp)
                    TableHeaderCell("Total", 80.dp)
                    TableHeaderCell("Status", 100.dp)
                }

                marks.forEach { mark ->
                    Column {
                        Row(
                            modifier = Modifier.padding(horizontal = 16.dp, vertical = 16.dp),
                            horizontalArrangement = Arrangement.spacedBy(16.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(modifier = Modifier.width(160.dp)) {
                                Text(
                                    mark.subjectName,
                                    style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Bold),
                                    color = MaterialTheme.colorScheme.onSurface,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                                if (mark.facultyReply != null) {
                                    Text(
                                        mark.facultyReply,
                                        style = MaterialTheme.typography.labelSmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                                        maxLines = 2,
                                        overflow = TextOverflow.Ellipsis
                                    )
                                }
                            }
                            TableBodyCell(if (mark.isApproved) "${mark.examScore.toInt()}" else "-", 70.dp)
                            TableBodyCell(if (mark.isApproved) "${mark.assignmentScore.toInt()}" else "-", 70.dp)
                            TableBodyCell(if (mark.isApproved) "${mark.presentationScore.toInt()}" else "-", 70.dp)
                            TableBodyCell(if (mark.isApproved) "${mark.vivaScore.toInt()}" else "-", 70.dp)
                            TableBodyCell(if (mark.isApproved) "${mark.attendanceScore.toInt()}" else "-", 70.dp)
                            TableBodyCell(if (mark.isApproved) "${mark.totalScore.toInt()}" else "-", 80.dp)

                            Box(
                                modifier = Modifier
                                    .width(100.dp)
                                    .clip(RoundedCornerShape(8.dp))
                                    .background((if (mark.isApproved) Color(0xFF10B981) else Color(0xFFF59E0B)).copy(alpha = 0.1f))
                                    .padding(vertical = 4.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    if (mark.isApproved) "Approved" else "Pending",
                                    style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Black),
                                    color = if (mark.isApproved) Color(0xFF047857) else Color(0xFFB45309)
                                )
                            }
                        }
                        HorizontalDivider(color = Color.LightGray.copy(alpha = 0.2f))
                    }
                }
            }
        }
    }
}

@Composable
fun TableHeaderCell(text: String, width: androidx.compose.ui.unit.Dp) {
    Text(
        text,
        modifier = Modifier.width(width),
        style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
        color = MaterialTheme.colorScheme.onSurfaceVariant,
        textAlign = TextAlign.Center
    )
}

@Composable
fun TableBodyCell(text: String, width: androidx.compose.ui.unit.Dp) {
    Text(
        text,
        modifier = Modifier.width(width),
        style = MaterialTheme.typography.bodySmall,
        color = MaterialTheme.colorScheme.onSurface,
        textAlign = TextAlign.Center
    )
}

@Composable
private fun MarksSkeleton() {
    Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(16.dp)) {
        Box(Modifier.fillMaxWidth().height(120.dp).shimmerEffect().clip(RoundedCornerShape(24.dp)))
        Box(Modifier.fillMaxWidth().height(80.dp).shimmerEffect().clip(RoundedCornerShape(20.dp)))
        Box(Modifier.fillMaxWidth().height(300.dp).shimmerEffect().clip(RoundedCornerShape(24.dp)))
    }
}
