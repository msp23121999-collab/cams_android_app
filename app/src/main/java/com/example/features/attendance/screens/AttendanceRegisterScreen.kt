package com.example.features.attendance.screens

import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.filled.KeyboardArrowRight
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import com.example.core.navigation.AppRoutes
import com.example.core.theme.*
import com.example.core.ui.CamsCard
import com.example.core.ui.CamsScreen
import com.example.core.ui.shimmerEffect
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Calendar
import java.util.Locale
import java.util.*
import androidx.compose.material3.*
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.*
import androidx.compose.ui.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.ui.graphics.vector.ImageVector
import com.example.features.student.providers.AttendanceViewModel
import com.example.features.attendance.widgets.AttendanceRadialGauge
import com.example.features.student.widgets.StudentDrawer
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AttendanceRegisterScreen(
    viewModel: AttendanceViewModel,
    onNavigate: (String) -> Unit
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    var activeTab by remember { mutableStateOf("subjects") }

    CamsScreen(scrollable = false,
        title = "Attendance Register",
        onBackClick = { onNavigate(AppRoutes.STUDENT_DASHBOARD) }
    ) {
        if (uiState.isLoading) {
            AttendanceSkeleton()
        } else if (uiState.error != null) {
            com.example.core.ui.NetworkErrorView(
                message = uiState.error!!,
                onRetry = { viewModel.fetchAttendance() },
                modifier = Modifier.fillMaxSize()
            )
        } else {
            val summary = uiState.summary
            val percentage = summary?.percentage?.toDouble() ?: 0.0
            val isBelowTarget = percentage < 75.0

            Column(
                verticalArrangement = Arrangement.spacedBy(24.dp)
            ) {
                // Overview Cards & Radial Indicator
                Column(
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    // Overall Percentage Card
                    CamsCard(
                        modifier = Modifier.fillMaxWidth(),
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(20.dp)
                        ) {
                            AttendanceRadialGauge(percentage = percentage)

                            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                    Text(
                                        text = "Overall Attendance",
                                        modifier = Modifier.weight(1f),
                                        style = MaterialTheme.typography.titleMedium.copy(
                                            fontWeight = FontWeight.Black,
                                            color = MaterialTheme.colorScheme.onSurface
                                        )
                                    )
                                    Surface(
                                        color = if (isBelowTarget) Color(0xFFFFF1F2) else Color(0xFFECFDF5),
                                        shape = RoundedCornerShape(8.dp)
                                    ) {
                                        Text(
                                            text = if (isBelowTarget) "Deficit" else "Good",
                                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp),
                                            style = MaterialTheme.typography.labelSmall.copy(
                                                color = if (isBelowTarget) Color(0xFFBE123C) else Color(0xFF047857),
                                                fontWeight = FontWeight.ExtraBold
                                            )
                                        )
                                    }
                                }
                                Text(
                                    text = "Institutional mandate requires minimum 75.0% attendance in each course.",
                                    style = MaterialTheme.typography.bodySmall.copy(color = MaterialTheme.colorScheme.onSurfaceVariant)
                                )

                                // Margin Banner
                                MarginBanner(summary)
                            }
                        }
                    }

                    // Simple count cards grid
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        CountCard("Conducted", summary?.total ?: 0, CamsNavy, Modifier.weight(1f))
                        CountCard("Present", summary?.present ?: 0, Color(0xFF10B981), Modifier.weight(1f))
                        CountCard("Absent", summary?.absent ?: 0, Color(0xFFF43F5E), Modifier.weight(1f))
                        CountCard("On Duty", summary?.od ?: 0, Color(0xFF3B82F6), Modifier.weight(1f))
                    }
                }

                // Main Tab Panel
                CamsCard(
                    modifier = Modifier.fillMaxWidth(),
                ) {
                    Column {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            // Tabs
                            Surface(
                                color = CamsBackground,
                                shape = RoundedCornerShape(12.dp),
                                border = BorderStroke(1.dp, Color.LightGray.copy(alpha = 0.2f))
                            ) {
                                Row(
                                    modifier = Modifier.padding(4.dp),
                                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                                ) {
                                    val tabs = listOf(
                                        "subjects" to Icons.Filled.Book,
                                        "trends" to Icons.Filled.TrendingUp,
                                        "heatmap" to Icons.Filled.CalendarMonth,
                                        "logs" to Icons.Filled.WatchLater
                                    )
                                    tabs.forEach { (id, icon) ->
                                        val isSelected = activeTab == id
                                        TabPill(
                                            icon = icon,
                                            isSelected = isSelected,
                                            onClick = { activeTab = id }
                                        )
                                    }
                                }
                            }

                            val context = androidx.compose.ui.platform.LocalContext.current
                            Button(
                                onClick = {
                                    val token = com.example.core.network.AuthManagerImpl(context).getToken() ?: ""
                                    val base = com.example.core.config.AppConfig.BASE_URL.trimEnd('/')
                                    val url = "$base/students/attendance/export-pdf"
                                    com.example.core.utils.DownloadHelper.downloadPdf(context, url, "attendance_register", token)
                                },
                                shape = RoundedCornerShape(12.dp),
                                colors = ButtonDefaults.buttonColors(containerColor = CamsNavy),
                                contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp)
                            ) {
                                Icon(Icons.Filled.Download, contentDescription = null, modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(8.dp))
                                Text("Export PDF", style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold), maxLines = 1, overflow = TextOverflow.Ellipsis)
                            }
                        }

                        Spacer(modifier = Modifier.height(24.dp))

                        // Tab Content
                        Box(modifier = Modifier.fillMaxWidth()) {
                            when (activeTab) {
                                "subjects" -> SubjectsTab(summary)
                                "trends" -> TrendsTab(summary)
                                "heatmap" -> HeatmapTab(summary)
                                "logs" -> LogsTab(summary)
                            }
                        }
                    }
                }

                // Advisory Panel
                AdvisoryPanel(onNavigate)
            }
        }
    }
}

@Composable
fun TabPill(icon: androidx.compose.ui.graphics.vector.ImageVector, isSelected: Boolean, onClick: () -> Unit) {
    val bgColor = if (isSelected) Color.White else Color.Transparent
    val contentColor = if (isSelected) CamsNavy else CamsTextSecondary
    Surface(
        color = bgColor,
        shape = RoundedCornerShape(8.dp),
        modifier = Modifier.clickable { onClick() },
        shadowElevation = if (isSelected) 1.dp else 0.dp
    ) {
        Box(modifier = Modifier.padding(12.dp)) {
            Icon(icon, contentDescription = null, tint = contentColor, modifier = Modifier.size(18.dp))
        }
    }
}

@Composable
fun CountCard(label: String, count: Int, color: Color, modifier: Modifier = Modifier) {
    CamsCard(
        modifier = modifier,
    ) {
        Column {
            Text(
                label.uppercase(),
                style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Black, color = MaterialTheme.colorScheme.onSurfaceVariant, letterSpacing = 1.sp),
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                count.toString(),
                style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Black, color = color),
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}

@Composable
fun MarginBanner(summary: com.example.core.network.AttendanceSummaryResponse?) {
    if (summary == null || summary.total == 0) return
    val targetThreshold = 75
    val attendedCount = summary.present + summary.od
    val percentage = summary.percentage
    val isBelowTarget = percentage < targetThreshold
    
    val backgroundColor = if (isBelowTarget) Color(0xFFFFF1F2) else Color(0xFFECFDF5)
    val contentColor = if (isBelowTarget) Color(0xFFBE123C) else Color(0xFF047857)
    
    val message: String
    val details: String
    
    if (isBelowTarget) {
        val classesNeeded = Math.max(0, Math.ceil(3.0 * summary.total - 4.0 * attendedCount).toInt())
        message = "Deficit of $classesNeeded Classes"
        details = "Attend next $classesNeeded classes to reach $targetThreshold%."
    } else {
        val classesCanMiss = Math.max(0, Math.floor((4.0 * attendedCount - 3.0 * summary.total) / 3.0).toInt())
        if (classesCanMiss == 0) {
            message = "Borderline Standing"
            details = "Cannot afford to miss any upcoming classes."
        } else {
            message = "Buffer of $classesCanMiss Classes"
            details = "Can miss up to $classesCanMiss classes safely."
        }
    }

    Surface(
        color = backgroundColor,
        shape = RoundedCornerShape(12.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier.padding(12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Icon(
                if (isBelowTarget) Icons.Filled.Warning else Icons.Filled.CheckCircle,
                contentDescription = null,
                tint = contentColor,
                modifier = Modifier.size(16.dp)
            )
            Column {
                Text(message.uppercase(), style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Black, color = contentColor))
                Text(details, style = MaterialTheme.typography.bodySmall.copy(color = contentColor.copy(alpha = 0.8f)))
            }
        }
    }
}

@Composable
fun SubjectsTab(summary: com.example.core.network.AttendanceSummaryResponse?) {
    val records = summary?.records ?: emptyList()
    val subjectGroups = records.groupBy { it.subjectCode }.entries.toList()

    if (subjectGroups.isEmpty()) {
        EmptyTabState("No attendance records available yet.")
        return
    }

    Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        subjectGroups.chunked(2).forEach { rowEntries ->
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                rowEntries.forEach { entry ->
                    val code = entry.key
                    val subjRecords = entry.value
                    val name = subjRecords.first().subjectName
                    val total = subjRecords.size
                    val attended = subjRecords.count { it.status == "present" || it.status == "od" }
                    val pct = (attended.toDouble() / total * 100).toInt()
                    val isBelow = pct < 75

                    CamsCard(
                        modifier = Modifier
                            .weight(1f)
                            .padding(2.dp),
                    ) {
                        Column {
                            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(name, fontWeight = FontWeight.Bold, fontSize = 12.sp, maxLines = 1, overflow = TextOverflow.Ellipsis, color = MaterialTheme.colorScheme.onSurface)
                                    Text(code, fontSize = 13.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                }
                                Surface(
                                    color = (if (isBelow) Color(0xFFF43F5E) else Color(0xFF10B981)).copy(alpha = 0.1f),
                                    shape = RoundedCornerShape(6.dp)
                                ) {
                                    Text(
                                        "$pct%",
                                        color = if (isBelow) Color(0xFFF43F5E) else Color(0xFF10B981),
                                        fontSize = 13.sp,
                                        fontWeight = FontWeight.Black,
                                        modifier = Modifier.padding(horizontal = 4.dp, vertical = 2.dp)
                                    )
                                }
                            }
                            Spacer(modifier = Modifier.height(8.dp))
                            LinearProgressIndicator(
                                progress = pct / 100f,
                                modifier = Modifier.fillMaxWidth().height(4.dp).clip(CircleShape),
                                color = if (isBelow) Color(0xFFF43F5E) else Color(0xFF10B981),
                                trackColor = CamsNavy.copy(alpha = 0.05f)
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                "Attended $attended / $total",
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
                if (rowEntries.size < 2) {
                    Spacer(modifier = Modifier.weight(1f))
                }
            }
        }
    }
}

@Composable
fun TrendsTab(summary: com.example.core.network.AttendanceSummaryResponse?) {
    val records = summary?.records ?: emptyList()
    
    // Group by month
    val monthlyData = records.groupBy { 
        try {
            val sdf = SimpleDateFormat("yyyy-MM-dd", Locale.US)
            val date = sdf.parse(it.date)
            val cal = Calendar.getInstance().apply { if (date != null) time = date }
            "${cal.get(Calendar.YEAR)}-${cal.get(Calendar.MONTH) + 1}"
        } catch (e: Exception) {
            "Unknown"
        }
    }.map { (key, list) ->
        val sdf = SimpleDateFormat("yyyy-MM-dd", Locale.US)
        val date = sdf.parse(list.first().date)
        val cal = Calendar.getInstance().apply { if (date != null) time = date }
        val monthName = SimpleDateFormat("MMMM yyyy", Locale.US).format(cal.time)
        val total = list.size
        val attended = list.count { it.status == "present" || it.status == "od" }
        val pct = (attended.toDouble() / total * 100).toInt()
        Triple(monthName, pct, "$attended / $total")
    }.sortedByDescending { it.first } // Simple sort

    if (monthlyData.isEmpty()) {
        EmptyTabState("No attendance trend data available yet.")
        return
    }

    Column(modifier = Modifier.fillMaxWidth(), verticalArrangement = Arrangement.spacedBy(16.dp)) {
        Text("Month-wise Attendance", style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold))
        monthlyData.forEach { (name, pct, count) ->
            val isBelow = pct < 75
            CamsCard(
                modifier = Modifier.fillMaxWidth(),
            ) {
                Column {
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        Column {
                            Text(name, fontWeight = FontWeight.Bold, fontSize = 13.sp, color = MaterialTheme.colorScheme.onSurface)
                            Text("Conducted: $count", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                        Text("$pct%", fontWeight = FontWeight.Black, fontSize = 14.sp, color = if (isBelow) Color(0xFFF43F5E) else Color(0xFF10B981))
                    }
                    Spacer(modifier = Modifier.height(8.dp))
                    LinearProgressIndicator(
                        progress = pct / 100f,
                        modifier = Modifier.fillMaxWidth().height(4.dp).clip(CircleShape),
                        color = if (isBelow) Color(0xFFF43F5E) else Color(0xFF10B981),
                        trackColor = CamsNavy.copy(alpha = 0.05f)
                    )
                }
            }
        }
    }
}

@Composable
fun HeatmapTab(summary: com.example.core.network.AttendanceSummaryResponse?) {
    val records = summary?.records ?: emptyList()
    val sdf = SimpleDateFormat("yyyy-MM-dd", Locale.US)
    val heatmapDays = (0 until 35).map {
        val cal = Calendar.getInstance()
        cal.add(Calendar.DAY_OF_YEAR, -it)
        cal.time
    }.reversed()
    
    Column(modifier = Modifier.fillMaxWidth(), verticalArrangement = Arrangement.spacedBy(20.dp)) {
        Text("30-Day Attendance Grid", style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold))
        
        // Grid using simple non-nested Column + Rows to avoid nested scrolling crash
        Column(
            modifier = Modifier.fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            heatmapDays.chunked(7).forEach { week ->
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    week.forEach { date ->
                        val dateStr = sdf.format(date)
                        val dayCal = Calendar.getInstance().apply { time = date }
                        val dayRecords = records.filter { it.date == dateStr }
                        val color = when {
                            dayRecords.isEmpty() -> MaterialTheme.colorScheme.surfaceVariant
                            dayRecords.all { it.status == "present" } -> Color(0xFF10B981)
                            dayRecords.all { it.status == "absent" } -> Color(0xFFF43F5E)
                            dayRecords.all { it.status == "od" } -> Color(0xFF3B82F6)
                            else -> Color(0xFFFBBF24) // Mixed
                        }
                        
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .aspectRatio(1f)
                                .clip(RoundedCornerShape(8.dp))
                                .background(color),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = dayCal.get(Calendar.DAY_OF_MONTH).toString(),
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                color = if (dayRecords.isEmpty()) CamsTextSecondary else Color.White
                            )
                        }
                    }
                    if (week.size < 7) {
                        repeat(7 - week.size) {
                            Spacer(modifier = Modifier.weight(1f).aspectRatio(1f))
                        }
                    }
                }
            }
        }
        
        // Legend
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(16.dp)) {
            LegendItem("Present", Color(0xFF10B981))
            LegendItem("Absent", Color(0xFFF43F5E))
            LegendItem("OD", Color(0xFF3B82F6))
            LegendItem("Mixed", Color(0xFFFBBF24))
        }
    }
}

@Composable
fun EmptyTabState(message: String) {
    Box(
        modifier = Modifier.fillMaxWidth().padding(vertical = 32.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Icon(Icons.Filled.EventBusy, contentDescription = null, tint = CamsTextSecondary, modifier = Modifier.size(32.dp))
            Spacer(modifier = Modifier.height(8.dp))
            Text(message, style = MaterialTheme.typography.bodyMedium.copy(color = CamsTextSecondary), textAlign = TextAlign.Center)
        }
    }
}

@Composable
fun LegendItem(label: String, color: Color) {
    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
        Box(modifier = Modifier.size(8.dp).clip(CircleShape).background(color))
        Text(label, fontSize = 12.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurfaceVariant)
    }
}

@Composable
fun LogsTab(summary: com.example.core.network.AttendanceSummaryResponse?) {
    val records = summary?.records ?: emptyList()
    var search by remember { mutableStateOf("") }
    
    val filtered = records.filter { 
        it.subjectName.contains(search, ignoreCase = true) || 
        it.subjectCode.contains(search, ignoreCase = true) ||
        it.date.contains(search)
    }

    Column(modifier = Modifier.fillMaxWidth(), verticalArrangement = Arrangement.spacedBy(16.dp)) {
        OutlinedTextField(
            value = search,
            onValueChange = { search = it },
            modifier = Modifier.fillMaxWidth(),
            placeholder = { Text("Search by subject, code or date...") },
            leadingIcon = { Icon(Icons.Filled.Search, contentDescription = null) },
            shape = RoundedCornerShape(12.dp)
        )
        
        if (filtered.isEmpty()) {
            EmptyTabState(if (records.isEmpty()) "No attendance records available yet." else "No records match your search.")
            return
        }

        Column(
            modifier = Modifier.fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            filtered.forEach { record ->
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 8.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(record.date, fontWeight = FontWeight.Bold, fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurface)
                        Text(record.subjectName, fontSize = 13.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                    Surface(
                        color = when(record.status) {
                            "present" -> Color(0xFFECFDF5)
                            "absent" -> Color(0xFFFFF1F2)
                            else -> Color(0xFFEFF6FF)
                        },
                        shape = RoundedCornerShape(8.dp),
                        border = BorderStroke(1.dp, when(record.status) {
                            "present" -> Color(0xFFD1FAE5)
                            "absent" -> Color(0xFFFFE4E6)
                            else -> Color(0xFFDBEAFE)
                        })
                    ) {
                        Text(
                            record.status.uppercase(),
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Black,
                            color = when(record.status) {
                                "present" -> Color(0xFF059669)
                                "absent" -> Color(0xFFDC2626)
                                else -> Color(0xFF2563EB)
                            }
                        )
                    }
                }
                HorizontalDivider(color = MaterialTheme.colorScheme.surfaceVariant)
            }
        }
    }
}

@Composable
fun AdvisoryPanel(onNavigate: (String) -> Unit) {
    CamsCard(
        modifier = Modifier.fillMaxWidth(),
    ) {
        Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(20.dp)) {
            Box(modifier = Modifier.size(48.dp).clip(CircleShape).background(CamsNavy.copy(alpha = 0.1f)), contentAlignment = Alignment.Center) {
                Icon(Icons.Filled.Mail, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
            }
            Column(modifier = Modifier.weight(1f)) {
                Text("Need attendance assistance?", fontWeight = FontWeight.Bold, fontSize = 14.sp, color = MaterialTheme.colorScheme.onSurface)
                Text("File an OD/Leave application or contact your advisor.", fontSize = 13.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                TextButton(onClick = { onNavigate(AppRoutes.STUDENT_LEAVE) }, contentPadding = PaddingValues(0.dp)) {
                    Text("Go to Leave Applications", fontWeight = FontWeight.Bold, fontSize = 12.sp, color = CamsNavy)
                    Spacer(modifier = Modifier.width(4.dp))
                    Icon(Icons.Filled.KeyboardArrowRight, contentDescription = null, modifier = Modifier.size(14.dp), tint = MaterialTheme.colorScheme.primary)
                }
            }
        }
    }
}

@Composable
private fun AttendanceSkeleton() {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(24.dp)
    ) {
        // Overview Card Skeleton
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(16.dp)) {
            Box(Modifier.weight(2f).height(160.dp).shimmerEffect().clip(RoundedCornerShape(24.dp)))
            Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    Box(Modifier.weight(1f).height(74.dp).shimmerEffect().clip(RoundedCornerShape(16.dp)))
                    Box(Modifier.weight(1f).height(74.dp).shimmerEffect().clip(RoundedCornerShape(16.dp)))
                }
                Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    Box(Modifier.weight(1f).height(74.dp).shimmerEffect().clip(RoundedCornerShape(16.dp)))
                    Box(Modifier.weight(1f).height(74.dp).shimmerEffect().clip(RoundedCornerShape(16.dp)))
                }
            }
        }

        // Tab Panel Skeleton
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(24.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            border = BorderStroke(1.dp, MaterialTheme.colorScheme.surfaceVariant)
        ) {
            Column(modifier = Modifier.padding(20.dp)) {
                Box(Modifier.width(200.dp).height(40.dp).shimmerEffect().clip(RoundedCornerShape(12.dp)))
                Spacer(modifier = Modifier.height(24.dp))
                
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    repeat(2) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(16.dp)
                        ) {
                            repeat(2) {
                                Box(
                                    Modifier
                                        .weight(1f)
                                        .height(120.dp)
                                        .shimmerEffect()
                                        .clip(RoundedCornerShape(16.dp))
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}
