package com.example.features.parent.screens

import android.content.Intent
import android.net.Uri
import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.core.navigation.AppRoutes
import com.example.core.theme.*
import com.example.core.ui.CamsCard
import com.example.core.ui.CamsScreen
import com.example.features.parent.providers.ParentDashboardViewModel
import com.example.features.parent.widgets.ParentBaseScreen
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ParentDashboardScreen(
    onNavigate: (String) -> Unit,
    viewModel: ParentDashboardViewModel
) {
    val uiState by viewModel.uiState.collectAsState()
    val context = LocalContext.current

    ParentBaseScreen(
        title = "Parent Portal",
        subtitle = "Monitoring your child's academic progress",
        currentRoute = AppRoutes.PARENT_DASHBOARD,
        onNavigate = onNavigate,
        scrollable = true
    ) {
        if (uiState.isLoading) {
            Box(modifier = Modifier.fillMaxWidth().height(200.dp), contentAlignment = Alignment.Center) {
                CircularProgressIndicator(color = CamsNavy)
            }
        } else if (uiState.error != null) {
            com.example.core.ui.NetworkErrorView(
                message = uiState.error ?: "Unknown error",
                onRetry = { viewModel.loadData() }
            )
        } else if (uiState.childProfileExtended == null) {
            Box(modifier = Modifier.fillMaxWidth().height(200.dp), contentAlignment = Alignment.Center) {
                Text("No dashboard data available.", color = CamsTextSecondary)
            }
        } else {
            val profile = uiState.childProfileExtended

            // Student Profile Quick info
            Text("Child Information", fontWeight = FontWeight.Black, color = CamsTextPrimary, fontSize = 16.sp)
            
            CamsCard(onClick = { onNavigate(AppRoutes.PARENT_PROFILE) }) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(16.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Box(
                        Modifier
                            .size(56.dp)
                            .background(CamsNavy.copy(alpha = 0.1f), CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(Icons.Filled.Person, null, tint = CamsNavy, modifier = Modifier.size(28.dp))
                    }
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            profile?.fullName ?: "",
                            fontWeight = FontWeight.Bold,
                            fontSize = 18.sp,
                            color = CamsTextPrimary,
                            maxLines = 1,
                            overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis
                        )
                        Text(
                            "Roll No: ${profile?.rollNo ?: ""} • ${profile?.semester ?: ""}",
                            fontSize = 13.sp,
                            color = CamsTextSecondary,
                            maxLines = 1,
                            overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis
                        )
                    }
                    Icon(
                        Icons.Filled.ChevronRight,
                        contentDescription = "View Profile",
                        tint = CamsTextSecondary
                    )
                }
            }

            // Grid of 4 Metric Cards
            Text("Academic Metrics", fontWeight = FontWeight.Black, color = CamsTextPrimary, fontSize = 16.sp)
            
            BoxWithConstraints {
                val isTablet = maxWidth > 600.dp
                if (isTablet) {
                    Row(horizontalArrangement = Arrangement.spacedBy(12.dp), modifier = Modifier.fillMaxWidth()) {
                        DashboardMetricCard(
                            label = "Overall Attendance",
                            value = "${if (uiState.subjectAttendance.isEmpty()) 0 else uiState.subjectAttendance.map { it.percentage.toDouble() }.average().toInt()}%",
                            subtext = "Attendance Status",
                            icon = Icons.Filled.CheckCircle,
                            color = Color(0xFF10B981), // Emerald
                            modifier = Modifier.weight(1f),
                            onClick = { onNavigate(AppRoutes.PARENT_ATTENDANCE) }
                        )
                        DashboardMetricCard(
                            label = "Outstanding Fees",
                            value = "₹${uiState.feeLedger?.pendingBalance?.toInt() ?: 0}",
                            subtext = "Fee Status",
                            icon = Icons.Filled.Payments,
                            color = Color(0xFFEF4444), // Red
                            modifier = Modifier.weight(1f),
                            onClick = { onNavigate(AppRoutes.PARENT_FEES) }
                        )
                        DashboardMetricCard(
                            label = "Weekly Schedule",
                            value = "Timetable",
                            subtext = "${uiState.timetable.size} Days active",
                            icon = Icons.Filled.CalendarMonth,
                            color = Color(0xFF6366F1), // Indigo
                            modifier = Modifier.weight(1f),
                            onClick = { onNavigate(AppRoutes.PARENT_TIMETABLE) }
                        )
                        DashboardMetricCard(
                            label = "Cumulative CGPA",
                            value = "${profile?.cgpa ?: 8.6} / 10",
                            subtext = "Academic Rating",
                            icon = Icons.Filled.School,
                            color = LexNovaPurple, // Purple
                            modifier = Modifier.weight(1f),
                            onClick = { onNavigate(AppRoutes.PARENT_MARKS) }
                        )
                    }
                } else {
                    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                        Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                            DashboardMetricCard(
                                label = "Overall Attendance",
                                value = "${if (uiState.subjectAttendance.isEmpty()) 0 else uiState.subjectAttendance.map { it.percentage.toDouble() }.average().toInt()}%",
                                subtext = "Attendance Status",
                                icon = Icons.Filled.CheckCircle,
                                color = Color(0xFF10B981), // Emerald
                                modifier = Modifier.weight(1f),
                                onClick = { onNavigate(AppRoutes.PARENT_ATTENDANCE) }
                            )
                            DashboardMetricCard(
                                label = "Outstanding Fees",
                                value = "₹${uiState.feeLedger?.pendingBalance?.toInt() ?: 0}",
                                subtext = "Fee Status",
                                icon = Icons.Filled.Payments,
                                color = Color(0xFFEF4444), // Red
                                modifier = Modifier.weight(1f),
                                onClick = { onNavigate(AppRoutes.PARENT_FEES) }
                            )
                        }
                        Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                            DashboardMetricCard(
                                label = "Weekly Schedule",
                                value = "Timetable",
                                subtext = "${uiState.timetable.size} Days active",
                                icon = Icons.Filled.CalendarMonth,
                                color = Color(0xFF6366F1), // Indigo
                                modifier = Modifier.weight(1f),
                                onClick = { onNavigate(AppRoutes.PARENT_TIMETABLE) }
                            )
                            DashboardMetricCard(
                                label = "Cumulative CGPA",
                                value = "${profile?.cgpa ?: 8.6} / 10",
                                subtext = "Academic Rating",
                                icon = Icons.Filled.School,
                                color = LexNovaPurple, // Purple
                                modifier = Modifier.weight(1f),
                                onClick = { onNavigate(AppRoutes.PARENT_MARKS) }
                            )
                        }
                    }
                }
            }

            // Notices & Support Section
            Text("Circular Notices (For Parents)", fontWeight = FontWeight.Black, color = CamsTextPrimary, fontSize = 16.sp)

            CamsCard(onClick = { onNavigate(AppRoutes.PARENT_CIRCULARS) }) {
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    uiState.notices.take(2).forEach { notice ->
                        Column {
                            Row(
                                horizontalArrangement = Arrangement.SpaceBetween,
                                modifier = Modifier.fillMaxWidth(),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Surface(color = CamsNavy.copy(alpha = 0.08f), shape = RoundedCornerShape(6.dp)) {
                                    Text(
                                        text = notice.category,
                                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                                        fontSize = 13.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = CamsNavy
                                    )
                                }
                                val pColor = when (notice.priority.uppercase()) {
                                    "HIGH" -> Color(0xFFEF4444)
                                    "LOW" -> Color(0xFF6B7280)
                                    else -> Color(0xFFF59E0B)
                                }
                                Surface(color = pColor.copy(alpha = 0.08f), shape = RoundedCornerShape(6.dp)) {
                                    Text(
                                        text = notice.priority.uppercase(),
                                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                                        fontSize = 13.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = pColor
                                    )
                                }
                            }
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                notice.title,
                                fontWeight = FontWeight.Bold,
                                fontSize = 14.sp,
                                color = CamsTextPrimary,
                                maxLines = 1,
                                overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis
                            )
                            Spacer(modifier = Modifier.height(2.dp))
                            Text(
                                notice.body,
                                fontSize = 12.sp,
                                color = CamsTextSecondary,
                                maxLines = 1
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                notice.publishDate,
                                fontSize = 12.sp,
                                color = CamsTextSecondary.copy(alpha = 0.7f)
                            )
                        }
                        HorizontalDivider(color = Color.LightGray.copy(alpha = 0.3f))
                    }
                    
                    Row(
                        horizontalArrangement = Arrangement.Center,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(
                            "View All Circular Notices",
                            fontWeight = FontWeight.Bold,
                            color = CamsNavy,
                            fontSize = 13.sp
                        )
                    }
                }
            }

            // Support Desk Card (real ACTION_SENDTO mailto support)
            Text("Institutional Support", fontWeight = FontWeight.Black, color = CamsTextPrimary, fontSize = 16.sp)

            CamsCard {
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Filled.SupportAgent, null, tint = CamsNavy)
                        Spacer(Modifier.width(8.dp))
                        Text("Administrative Desk Support", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleMedium)
                    }
                    
                    Text(
                        "For any academic, scheduling, or fee-related queries, contact our college administration desk directly.",
                        fontSize = 13.sp,
                        color = CamsTextSecondary
                    )

                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(CamsBackground, RoundedCornerShape(12.dp))
                            .padding(12.dp)
                    ) {
                        Icon(Icons.Filled.Mail, null, tint = CamsTextSecondary, modifier = Modifier.size(16.dp))
                        Spacer(Modifier.width(8.dp))
                        Text(
                            "admin@cams.local",
                            fontWeight = FontWeight.Bold,
                            fontSize = 14.sp,
                            color = CamsTextPrimary,
                            modifier = Modifier.weight(1f)
                        )
                        IconButton(onClick = {
                            val clipboard = context.getSystemService(android.content.Context.CLIPBOARD_SERVICE) as android.content.ClipboardManager
                            val clip = android.content.ClipData.newPlainText("Cams Admin Email", "admin@cams.local")
                            clipboard.setPrimaryClip(clip)
                            Toast.makeText(context, "Email copied to clipboard", Toast.LENGTH_SHORT).show()
                        }) {
                            Icon(Icons.Filled.ContentCopy, "Copy", tint = CamsNavy, modifier = Modifier.size(16.dp))
                        }
                    }

                    Button(
                        onClick = {
                            try {
                                val intent = Intent(Intent.ACTION_SENDTO).apply {
                                    data = Uri.parse("mailto:admin@cams.local")
                                    putExtra(Intent.EXTRA_SUBJECT, "Query regarding student: ${profile?.fullName ?: ""}")
                                }
                                context.startActivity(intent)
                            } catch (e: Exception) {
                                Toast.makeText(context, "Email client not found. Mail to: admin@cams.local", Toast.LENGTH_LONG).show()
                            }
                        },
                        modifier = Modifier.fillMaxWidth(),
                        colors = ButtonDefaults.buttonColors(containerColor = CamsNavy),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Icon(Icons.Filled.Send, null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Compose Support Email", fontWeight = FontWeight.Bold)
                    }
                }
            }
            
            Spacer(modifier = Modifier.height(16.dp))
        }
    }
}

@Composable
private fun DashboardMetricCard(
    label: String,
    value: String,
    icon: ImageVector,
    color: Color,
    modifier: Modifier,
    subtext: String,
    onClick: () -> Unit = {}
) {
    Card(
        onClick = onClick,
        modifier = modifier,
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
                modifier = Modifier.fillMaxWidth()
            ) {
                Box(
                    Modifier
                        .size(36.dp)
                        .background(color.copy(alpha = 0.1f), CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(icon, null, tint = color, modifier = Modifier.size(18.dp))
                }
                Icon(Icons.Filled.ArrowOutward, null, tint = CamsTextSecondary.copy(alpha = 0.5f), modifier = Modifier.size(14.dp))
            }
            Spacer(modifier = Modifier.height(16.dp))
            Text(
                value,
                fontWeight = FontWeight.Black,
                fontSize = 20.sp,
                color = CamsTextPrimary,
                maxLines = 1,
                overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis
            )
            Text(
                label,
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold,
                color = CamsTextSecondary,
                maxLines = 1,
                overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis
            )
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                subtext,
                fontSize = 13.sp,
                color = CamsTextSecondary.copy(alpha = 0.6f),
                maxLines = 1,
                overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis
            )
        }
    }
}
