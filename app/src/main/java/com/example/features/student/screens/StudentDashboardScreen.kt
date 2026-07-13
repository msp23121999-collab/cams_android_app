package com.example.features.student.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowForward
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.core.navigation.AppRoutes
import com.example.features.student.widgets.StudentDrawer
import com.example.core.ui.CamsCard
import com.example.core.ui.CamsScreen
import com.example.core.ui.shimmerEffect
import com.example.features.student.providers.DashboardViewModel
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.features.student.models.*
import kotlinx.coroutines.launch

// UI Design System Colors
private val ColorPrimary = Color(0xFF1A365D)
private val ColorBackground = Color(0xFFF8FAFC)
private val ColorSurface = Color(0xFFFFFFFF)
private val ColorTextPrimary = Color(0xFF0F172A)
private val ColorTextSecondary = Color(0xFF64748B)

private val Purple650 = Color(0xFF7E22CE)
private val Indigo600 = Color(0xFF4F46E5)
private val Emerald500 = Color(0xFF10B981)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun StudentDashboardScreen(
    onNavigate: (String) -> Unit,
    viewModel: DashboardViewModel = viewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val drawerState = rememberDrawerState(initialValue = DrawerValue.Closed)
    val scope = rememberCoroutineScope()

    ModalNavigationDrawer(
        drawerState = drawerState,
        drawerContent = {
            StudentDrawer(
                currentRoute = AppRoutes.STUDENT_DASHBOARD,
                onNavigate = { route ->
                    scope.launch { drawerState.close() }
                    onNavigate(route)
                }
            )
        }
    ) {
        com.example.core.ui.CamsScreen(
            title = "Student Dashboard",
            subtitle = "Welcome back, ${uiState.profile?.fullName?.split(" ")?.firstOrNull() ?: "Student"}",
            navigationIcon = {
                IconButton(onClick = { scope.launch { drawerState.open() } }) {
                    Icon(Icons.Filled.Menu, contentDescription = "Menu", tint = Color.White)
                }
            },
            actions = {
                IconButton(onClick = { onNavigate("LOGOUT") }) {
                    Icon(Icons.Filled.ExitToApp, contentDescription = "Logout", tint = Color.White)
                }
            },
            isOfflineMode = uiState.isOfflineMode,
            scrollable = false,
            onRetry = { viewModel.fetchDashboardData() }
        ) {
            if (uiState.isLoading && uiState.profile == null) {
                Column(
                    modifier = Modifier.fillMaxSize().padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    Box(modifier = Modifier.fillMaxWidth().height(120.dp).shimmerEffect())
                    Box(modifier = Modifier.fillMaxWidth().height(100.dp).shimmerEffect())
                    Box(modifier = Modifier.fillMaxWidth().height(150.dp).shimmerEffect())
                }
            } else if (uiState.error != null && uiState.profile == null) {
                com.example.core.ui.NetworkErrorView(
                    message = uiState.error ?: "Failed to load dashboard",
                    onRetry = { viewModel.fetchDashboardData() }
                )
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                        item { uiState.profile?.let { ProfileCard(it, onNavigate) } }
                        item { QuickActions(onNavigate) }
                        item { LawSubjectsDashboard(uiState.courses) }
                        item { MootCourtAndResearchHub(uiState.calendarEvents) }
                        item { QuickWidgets(uiState.borrowedBooks, uiState.dashboardData, onNavigate) }
                        item { RecentNotices(uiState.notices, onNavigate) }
                    }
                }
            }
        }
    }


@Composable
private fun ProfileCard(profile: StudentProfileResponse, onNavigate: (String) -> Unit) {
    CamsCard {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .size(80.dp)
                        .background(Brush.linearGradient(listOf(Purple650, Indigo600)), RoundedCornerShape(16.dp))
                        .padding(2.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(ColorSurface, RoundedCornerShape(14.dp)),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(profile.fullName.take(1), fontSize = 32.sp, fontWeight = FontWeight.ExtraBold, color = Purple650)
                    }
                }
                Spacer(modifier = Modifier.width(16.dp))
                Column {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = profile.fullName,
                            fontSize = 20.sp,
                            fontWeight = FontWeight.Black,
                            color = MaterialTheme.colorScheme.onSurface,
                            modifier = Modifier.weight(1f),
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Box(
                            modifier = Modifier
                                .background(Purple650.copy(alpha = 0.1f), RoundedCornerShape(4.dp))
                                .padding(horizontal = 6.dp, vertical = 2.dp)
                        ) {
                            Text("VERIFIED", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = Purple650, maxLines = 1, overflow = TextOverflow.Ellipsis)
                        }
                    }
                    Text(
                        text = "Reg: ${profile.rollNo} • ${profile.courseName} • Sem ${profile.semester}",
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        fontWeight = FontWeight.SemiBold,
                        modifier = Modifier.padding(top = 4.dp),
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    Row(modifier = Modifier.padding(top = 8.dp), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        PillBadge(icon = Icons.Filled.CheckCircle, text = "88% Attendance", color = Emerald500) { onNavigate(AppRoutes.ATTENDANCE) }
                        PillBadge(icon = Icons.Filled.Star, text = "CGPA: ${profile.cgpa}", color = Purple650) { onNavigate(AppRoutes.INTERNAL_MARKS) }
                    }
                }
            }
            Spacer(modifier = Modifier.height(16.dp))
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(ColorBackground, RoundedCornerShape(12.dp))
                    .padding(12.dp)
            ) {
                Column {
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        Text("PROFILE COMPLETION", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurfaceVariant, maxLines = 1, overflow = TextOverflow.Ellipsis, modifier = Modifier.weight(1f))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("85%", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = Indigo600, maxLines = 1, overflow = TextOverflow.Ellipsis)
                    }
                    Spacer(modifier = Modifier.height(6.dp))
                    LinearProgressIndicator(
                        progress = { 0.85f },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(6.dp)
                            .clip(RoundedCornerShape(3.dp)),
                        color = Indigo600,
                        trackColor = ColorTextSecondary.copy(alpha = 0.2f),
                    )
                }
            }
        }
    }
@Composable
private fun PillBadge(icon: androidx.compose.ui.graphics.vector.ImageVector, text: String, color: Color, onClick: () -> Unit) {
    Row(
        modifier = Modifier
            .background(ColorBackground, RoundedCornerShape(8.dp))
            .border(1.dp, ColorTextSecondary.copy(alpha = 0.1f), RoundedCornerShape(8.dp))
            .clickable { onClick() }
            .padding(horizontal = 8.dp, vertical = 4.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(icon, null, tint = color, modifier = Modifier.size(12.dp))
        Spacer(modifier = Modifier.width(4.dp))
        Text(text, fontSize = 12.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurfaceVariant, maxLines = 1, overflow = TextOverflow.Ellipsis, modifier = Modifier.weight(1f, fill = false))
    }
}

@Composable
private fun QuickActions(onNavigate: (String) -> Unit) {
    CamsCard {
            Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.padding(bottom = 12.dp)) {
                Icon(Icons.Filled.FlashOn, null, tint = Purple650, modifier = Modifier.size(16.dp))
                Spacer(modifier = Modifier.width(8.dp))
                Text("Quick Actions", fontSize = 14.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurface)
            }
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                QuickActionItem(Icons.Filled.CalendarToday, "Timetable") { onNavigate(AppRoutes.TIMETABLE) }
                QuickActionItem(Icons.Filled.Send, "Apply Leave") { onNavigate(AppRoutes.LEAVE) }
                QuickActionItem(Icons.Filled.Assignment, "Assignments") { onNavigate(AppRoutes.ASSIGNMENTS) }
                QuickActionItem(Icons.Filled.Payment, "Pay Fees") { onNavigate(AppRoutes.STUDENT_FEES) }
            }
        }
}

@Composable
private fun QuickActionItem(icon: androidx.compose.ui.graphics.vector.ImageVector, label: String, onClick: () -> Unit) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier
            .clickable { onClick() }
            .padding(4.dp)
    ) {
        Box(
            modifier = Modifier
                .size(48.dp)
                .background(ColorBackground, CircleShape)
                .border(1.dp, ColorTextSecondary.copy(alpha = 0.1f), CircleShape),
            contentAlignment = Alignment.Center
        ) {
            Icon(icon, null, tint = Purple650, modifier = Modifier.size(20.dp))
        }
        Spacer(modifier = Modifier.height(4.dp))
        Text(label, fontSize = 12.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurfaceVariant, maxLines = 1, overflow = TextOverflow.Ellipsis)
    }
}

@Composable
private fun LawSubjectsDashboard(courses: List<Course>) {
    CamsCard {
            Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.padding(bottom = 16.dp)) {
                Icon(Icons.Filled.Book, null, tint = Indigo600, modifier = Modifier.size(16.dp))
                Spacer(modifier = Modifier.width(8.dp))
                Text("Current Semester Subjects", fontSize = 14.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurface)
            }
            LazyRow(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                items(courses) { course ->
                    SubjectCard(course.code, course.name, "${course.credits} Credits", course.overallCompletion / 100f, Indigo600)
                }
            }
        }
}

@Composable
private fun SubjectCard(code: String, name: String, credits: String, coverage: Float, color: Color) {
    Box(
        modifier = Modifier
            .width(200.dp)
            .background(ColorBackground, RoundedCornerShape(12.dp))
            .border(1.dp, ColorTextSecondary.copy(alpha = 0.1f), RoundedCornerShape(12.dp))
            .padding(12.dp)
    ) {
        Column {
            Text(code, fontSize = 13.sp, fontWeight = FontWeight.Bold, color = Indigo600, letterSpacing = 1.sp)
            Text(name, fontSize = 13.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurface, maxLines = 1, overflow = TextOverflow.Ellipsis, modifier = Modifier.padding(vertical = 4.dp))
            Text(credits, fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant, fontWeight = FontWeight.Medium)
            Spacer(modifier = Modifier.height(12.dp))
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Text("SYLLABUS COVERAGE", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurfaceVariant)
                Text("${(coverage * 100).toInt()}%", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = color)
            }
            Spacer(modifier = Modifier.height(4.dp))
            LinearProgressIndicator(
                progress = { coverage },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(4.dp)
                    .clip(RoundedCornerShape(2.dp)),
                color = color,
                trackColor = ColorTextSecondary.copy(alpha = 0.2f),
            )
        }
    }
}

@Composable
private fun MootCourtAndResearchHub(events: List<CalendarEvent>) {
    Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
        CamsCard(modifier = Modifier.weight(1f)) {
                Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.padding(bottom = 12.dp)) {
                    Icon(Icons.Filled.Gavel, null, tint = Purple650, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Moot Court Center", fontSize = 14.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurface)
                }
                val mootEvent = events.firstOrNull { !it.isHoliday }
                mootEvent?.let { event ->
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(Purple650.copy(alpha = 0.05f), RoundedCornerShape(12.dp))
                            .border(1.dp, Purple650.copy(alpha = 0.2f), RoundedCornerShape(12.dp))
                            .padding(12.dp)
                    ) {
                        Column {
                            Box(
                                modifier = Modifier
                                    .background(MaterialTheme.colorScheme.surface, RoundedCornerShape(4.dp))
                                    .border(1.dp, Purple650.copy(alpha = 0.2f), RoundedCornerShape(4.dp))
                                    .padding(horizontal = 4.dp, vertical = 2.dp)
                            ) {
                                Text("Upcoming • ${event.startDate}", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = Purple650)
                            }
                            Text(event.title, fontSize = 12.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurface, modifier = Modifier.padding(top = 8.dp))
                            Text("Campus Center", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.padding(top = 2.dp, bottom = 8.dp))
                            Button(
                                onClick = { },
                                colors = ButtonDefaults.buttonColors(containerColor = Purple650),
                                shape = RoundedCornerShape(8.dp),
                                modifier = Modifier.fillMaxWidth().height(32.dp),
                                contentPadding = PaddingValues(0.dp)
                            ) {
                                Text("Memorial Drafts", fontSize = 12.sp, fontWeight = FontWeight.Black)
                            }
                        }
                    }
                }
        }

        CamsCard(modifier = Modifier.weight(1f)) {
                Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.padding(bottom = 12.dp)) {
                    Icon(Icons.Filled.AccountTree, null, tint = Indigo600, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Legal Research", fontSize = 14.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurface)
                }
                Text("Saved Citations", fontSize = 13.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurfaceVariant, letterSpacing = 1.sp)
                Spacer(modifier = Modifier.height(8.dp))
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Box(modifier = Modifier.fillMaxWidth().height(100.dp), contentAlignment = Alignment.Center) {
                        Text("No saved citations yet.", style = MaterialTheme.typography.bodySmall, color = ColorTextSecondary)
                    }
                }
        }
    }
}

@Composable
private fun CitationItem(text: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(ColorBackground, RoundedCornerShape(8.dp))
            .border(1.dp, ColorTextSecondary.copy(alpha = 0.1f), RoundedCornerShape(8.dp))
            .padding(8.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.weight(1f)) {
            Box(modifier = Modifier.size(6.dp).background(Indigo600, CircleShape))
            Spacer(modifier = Modifier.width(6.dp))
            Text(text, fontSize = 12.sp, fontWeight = FontWeight.SemiBold, color = MaterialTheme.colorScheme.onSurface, maxLines = 1, overflow = TextOverflow.Ellipsis)
        }
        Icon(Icons.Filled.ChevronRight, null, tint = ColorTextSecondary, modifier = Modifier.size(14.dp))
    }
}

@Composable
private fun QuickWidgets(
    books: List<LibraryBook>,
    dashboard: DashboardResponse?,
    onNavigate: (String) -> Unit
) {
    Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
        WidgetCard(
            title = "Digital Library",
            icon = Icons.Filled.LibraryBooks,
            iconColor = Purple650,
            content = books.firstOrNull()?.let { "Currently Borrowed: ${it.title}. Due ${it.dueDate}." } ?: "No books borrowed.",
            modifier = Modifier.weight(1f)
        )
        val fees = dashboard?.metrics?.firstOrNull { it.id == "fees" }?.value ?: "N/A"
        WidgetCard(
            title = "Fee Status",
            icon = Icons.Filled.CreditCard,
            iconColor = Purple650,
            content = "Total Due Balance: $fees.",
            modifier = Modifier.weight(1f)
        )
    }
}

@Composable
private fun WidgetCard(title: String, icon: androidx.compose.ui.graphics.vector.ImageVector, iconColor: Color, content: String, modifier: Modifier) {
    CamsCard(modifier = modifier) {
            Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.padding(bottom = 8.dp)) {
                Icon(icon, null, tint = iconColor, modifier = Modifier.size(16.dp))
                Spacer(modifier = Modifier.width(8.dp))
                Text(title, fontSize = 14.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurface)
            }
            Text(content, fontSize = 13.sp, color = MaterialTheme.colorScheme.onSurfaceVariant, lineHeight = 16.sp, maxLines = 2, overflow = TextOverflow.Ellipsis)
    }
}

@Composable
private fun RecentNotices(notices: List<Notice>, onNavigate: (String) -> Unit) {
    CamsCard(
        modifier = Modifier.fillMaxWidth(),
        onClick = { onNavigate(AppRoutes.CIRCULARS) }
    ) {
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.SpaceBetween, modifier = Modifier.fillMaxWidth().padding(bottom = 16.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Filled.Campaign, null, tint = Purple650, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Recent Notices", fontSize = 14.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurface)
                }
                Box(modifier = Modifier.background(Purple650.copy(alpha = 0.1f), RoundedCornerShape(4.dp)).padding(horizontal = 6.dp, vertical = 2.dp)) {
                    Text("${notices.size} Total", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = Purple650, maxLines = 1, overflow = TextOverflow.Ellipsis)
                }
            }
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                if (notices.isEmpty()) {
                    com.example.core.ui.EmptyStateView(
                        icon = Icons.Filled.Campaign,
                        title = "No Recent Notices",
                        message = "You're all caught up!"
                    )
                } else {
                    notices.take(3).forEach { notice ->
                        NoticeItem(notice.title, notice.category, notice.priority)
                    }
                }
            }
    }
}

@Composable
private fun NoticeItem(title: String, category: String, priority: String) {
    val (priorityColor, priorityBg) = when(priority) {
        "High" -> Color(0xFFE11D48) to Color(0xFFFFF1F2)
        "Medium" -> Color(0xFFD97706) to Color(0xFFFFF7ED)
        else -> Color(0xFF64748B) to MaterialTheme.colorScheme.background
    }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(ColorBackground, RoundedCornerShape(12.dp))
            .border(1.dp, ColorTextSecondary.copy(alpha = 0.1f), RoundedCornerShape(12.dp))
            .padding(12.dp),
        verticalAlignment = Alignment.Top,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Row(verticalAlignment = Alignment.Top, modifier = Modifier.weight(1f)) {
            Box(modifier = Modifier.size(8.dp).background(Purple650, CircleShape).padding(top = 4.dp))
            Spacer(modifier = Modifier.width(8.dp))
            Column {
                Text(title, fontSize = 12.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurface, maxLines = 1, overflow = TextOverflow.Ellipsis)
                Text(category, fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.padding(top = 2.dp), maxLines = 1, overflow = TextOverflow.Ellipsis)
            }
        }
        Box(
            modifier = Modifier
                .background(priorityBg, RoundedCornerShape(4.dp))
                .border(1.dp, priorityColor.copy(alpha = 0.2f), RoundedCornerShape(4.dp))
                .padding(horizontal = 6.dp, vertical = 2.dp)
        ) {
            Text(priority, fontSize = 12.sp, fontWeight = FontWeight.Black, color = priorityColor)
        }
    }
}
