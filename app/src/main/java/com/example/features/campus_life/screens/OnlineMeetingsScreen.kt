package com.example.features.campus_life.screens

import androidx.lifecycle.compose.collectAsStateWithLifecycle
import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
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
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.core.navigation.AppRoutes
import com.example.core.theme.*
import com.example.core.ui.CamsCard
import com.example.core.ui.CamsScreen
import com.example.features.campus_life.models.*
import com.example.features.campus_life.providers.OnlineMeetingsViewModel
import com.example.features.student.widgets.StudentDrawer
import com.example.core.theme.*
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun OnlineMeetingsScreen(
    viewModel: OnlineMeetingsViewModel = viewModel(),
    onNavigate: (String) -> Unit
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    var selectedMeeting by remember { mutableStateOf<OnlineMeeting?>(null) }
    val context = LocalContext.current

    CamsScreen(scrollable = true,
        title = "Online Meetings",
        subtitle = "Virtual Academic Hub",
        onBackClick = { onNavigate(AppRoutes.STUDENT_DASHBOARD) }
    ) {
        if (uiState.isLoading) {
            Box(modifier = Modifier.fillMaxWidth().height(300.dp), contentAlignment = Alignment.Center) {
                CircularProgressIndicator(color = CamsNavy)
            }
        } else {
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(24.dp)
            ) {
                // KPI Cards
                KPISection(uiState)

                // Tabs
                TabSwitcher(uiState.activeTab) { viewModel.updateTab(it) }

                // Filters
                CamsCard {
                    MeetingFilters(viewModel, uiState)
                }

                if (uiState.activeTab == "Calendar") {
                    CalendarPlaceholder()
                } else {
                    // List
                    Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                        if (uiState.filteredMeetings.isEmpty()) {
                            EmptyState(onRetry = { viewModel.fetchMeetings() })
                        } else {
                            uiState.filteredMeetings.forEach { meeting ->
                                MeetingCard(meeting, onDetail = { selectedMeeting = meeting }, onJoin = {
                                    viewModel.markAttendance(meeting.id)
                                    val intent = Intent(Intent.ACTION_VIEW, Uri.parse(meeting.meetingLink))
                                    context.startActivity(intent)
                                })
                            }
                        }
                    }
                }

            Spacer(modifier = Modifier.height(20.dp))
            }
        }
    }

    if (selectedMeeting != null) {
        MeetingDetailsDialog(
            meeting = selectedMeeting!!,
            onDismiss = { selectedMeeting = null },
            onJoin = {
                viewModel.markAttendance(selectedMeeting!!.id)
                val intent = Intent(Intent.ACTION_VIEW, Uri.parse(selectedMeeting!!.meetingLink))
                context.startActivity(intent)
            }
        )
    }
}

@Composable
private fun KPISection(state: com.example.features.campus_life.providers.OnlineMeetingsState) {
    val total = state.meetings.size
    val upcoming = state.meetings.count { it.status == MeetingStatus.UPCOMING || it.status == MeetingStatus.LIVE_NOW }
    val attended = state.meetings.count { it.attended }

    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
        MeetingKPICard(Modifier.weight(1f), "Total", total.toString(), Icons.Filled.VideoCall, CamsNavy)
        MeetingKPICard(Modifier.weight(1f), "Upcoming", upcoming.toString(), Icons.Filled.Schedule, Color(0xFFD97706))
        MeetingKPICard(Modifier.weight(1f), "Attended", attended.toString(), Icons.Filled.CheckCircle, Color(0xFF059669))
    }
}

@Composable
private fun MeetingKPICard(modifier: Modifier, label: String, value: String, icon: ImageVector, color: Color) {
    CamsCard(modifier = modifier) {
        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Surface(color = color.copy(alpha = 0.1f), shape = RoundedCornerShape(10.dp), modifier = Modifier.size(32.dp)) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(icon, contentDescription = null, tint = color, modifier = Modifier.size(16.dp))
                }
            }
            Column {
                Text(label.uppercase(), style = MaterialTheme.typography.labelSmall.copy(fontSize = 12.sp, fontWeight = FontWeight.Black), color = MaterialTheme.colorScheme.onSurfaceVariant)
                Text(value, style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Black), color = MaterialTheme.colorScheme.onSurface)
            }
        }
    }
}

@Composable
private fun TabSwitcher(activeTab: String, onTabSelect: (String) -> Unit) {
    CamsCard {
        Row(modifier = Modifier.padding(4.dp)) {
            listOf("Schedule", "History", "Calendar").forEach { tab ->
                val selected = activeTab == tab
                Button(
                    onClick = { onTabSelect(tab) },
                    modifier = Modifier.weight(1f).height(36.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = if (selected) CamsNavy else Color.Transparent,
                        contentColor = if (selected) Color.White else CamsTextSecondary
                    ),
                    contentPadding = PaddingValues(0.dp),
                    shape = RoundedCornerShape(12.dp),
                    elevation = null
                ) {
                    Text(tab, style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Black))
                }
            }
        }
    }
}

@Composable
private fun MeetingFilters(viewModel: OnlineMeetingsViewModel, state: com.example.features.campus_life.providers.OnlineMeetingsState) {
    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        TextField(
            value = state.searchQuery,
            onValueChange = { viewModel.updateSearch(it) },
            placeholder = { Text("Search by title or organizer...", fontSize = 14.sp) },
            leadingIcon = { Icon(Icons.Filled.Search, contentDescription = null, tint = MaterialTheme.colorScheme.onSurfaceVariant) },
            modifier = Modifier.fillMaxWidth(),
            colors = TextFieldDefaults.colors(
                focusedContainerColor = Color.Transparent,
                unfocusedContainerColor = Color.Transparent,
                focusedIndicatorColor = CamsNavy,
            ),
            shape = RoundedCornerShape(12.dp)
        )
        
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.horizontalScroll(rememberScrollState())) {
            FilterChip(
                selected = state.statusFilter == "All",
                onClick = { viewModel.updateFilters("All", state.categoryFilter, state.platformFilter) },
                label = { Text("All Status") }
            )
            FilterChip(
                selected = state.statusFilter == MeetingStatus.LIVE_NOW.label,
                onClick = { viewModel.updateFilters(MeetingStatus.LIVE_NOW.label, state.categoryFilter, state.platformFilter) },
                label = { Text("Live Now") }
            )
            FilterChip(
                selected = state.categoryFilter == "Guest Lecture",
                onClick = { viewModel.updateFilters(state.statusFilter, "Guest Lecture", state.platformFilter) },
                label = { Text("Guest Lectures") }
            )
        }
    }
}

@Composable
private fun MeetingCard(meeting: OnlineMeeting, onDetail: () -> Unit, onJoin: () -> Unit) {
    val isLive = meeting.status == MeetingStatus.LIVE_NOW
    
    CamsCard(
        modifier = Modifier.fillMaxWidth().clickable { onDetail() },
    ) {
        Column {
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                StatusBadge(meeting.status)
                Text(meeting.id, style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold), color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
            
            Spacer(modifier = Modifier.height(12.dp))
            Text(meeting.title, style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Black), color = MaterialTheme.colorScheme.onSurface, maxLines = 2, overflow = TextOverflow.Ellipsis)
            
            Spacer(modifier = Modifier.height(16.dp))
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                MetaIcon(Icons.Filled.Event, meeting.date)
                MetaIcon(Icons.Filled.Schedule, meeting.time)
            }

            Spacer(modifier = Modifier.height(20.dp))
            HorizontalDivider(color = Color.LightGray.copy(alpha = 0.1f))
            Spacer(modifier = Modifier.height(16.dp))
            
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    PlatformBadge(meeting.platform)
                    if (meeting.attended) {
                        Icon(Icons.Filled.CheckCircle, contentDescription = null, tint = Color(0xFF059669), modifier = Modifier.size(16.dp))
                    }
                }
                
                if (meeting.status != MeetingStatus.COMPLETED && meeting.status != MeetingStatus.CANCELLED) {
                    Button(
                        onClick = onJoin,
                        colors = ButtonDefaults.buttonColors(containerColor = if (isLive) Color(0xFFDC2626) else CamsNavy),
                        shape = RoundedCornerShape(12.dp),
                        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp)
                    ) {
                        Icon(if (isLive) Icons.Filled.Radio else Icons.Filled.Launch, contentDescription = null, modifier = Modifier.size(14.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(if (isLive) "Join Now" else "Launch", style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Black))
                    }
                } else if (meeting.recordingAvailable) {
                    Button(
                        onClick = { /* Launch Recording */ },
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF0284C7)),
                        shape = RoundedCornerShape(12.dp),
                        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp)
                    ) {
                        Icon(Icons.Filled.PlayArrow, contentDescription = null, modifier = Modifier.size(14.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Recording", style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Black))
                    }
                }
            }
        }
    }
}

@Composable
private fun StatusBadge(status: MeetingStatus) {
    val colors = when (status) {
        MeetingStatus.LIVE_NOW -> Color(0xFFFEF2F2) to Color(0xFFDC2626)
        MeetingStatus.UPCOMING, MeetingStatus.SCHEDULED -> Color(0xFFEEF2FF) to Color(0xFF4F46E5)
        MeetingStatus.COMPLETED -> Color(0xFFECFDF5) to Color(0xFF059669)
        else -> Zinc50 to Slate400
    }
    val bg = colors.first
    val content = colors.second
    Surface(color = bg, shape = RoundedCornerShape(8.dp)) {
        Row(modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
            if (status == MeetingStatus.LIVE_NOW) {
                Box(modifier = Modifier.size(6.dp).background(content, CircleShape))
            }
            Text(status.label.uppercase(), style = MaterialTheme.typography.labelSmall.copy(fontSize = 12.sp, fontWeight = FontWeight.Black), color = content)
        }
    }
}

@Composable
private fun PlatformBadge(platform: MeetingPlatform) {
    val details = when (platform) {
        MeetingPlatform.ZOOM -> Triple(Color(0xFFEFF6FF), Color(0xFF2563EB), Icons.Filled.Videocam)
        MeetingPlatform.GOOGLE_MEET -> Triple(Color(0xFFECFDF5), Color(0xFF059669), Icons.Filled.Language)
        MeetingPlatform.TEAMS -> Triple(Color(0xFFFAF5FF), Color(0xFF9333EA), Icons.Filled.BusinessCenter)
        else -> Triple(Zinc50, Slate600, Icons.Filled.Radio)
    }
    val bg = details.first
    val content = details.second
    val icon = details.third
    Surface(color = bg, border = BorderStroke(1.dp, Zinc200), shape = RoundedCornerShape(8.dp)) {
        Row(modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
            Icon(icon, contentDescription = null, modifier = Modifier.size(12.dp), tint = content)
            Text(platform.label, style = MaterialTheme.typography.labelSmall.copy(fontSize = 12.sp, fontWeight = FontWeight.Black), color = content)
        }
    }
}

@Composable
private fun MetaIcon(icon: ImageVector, text: String) {
    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
        Icon(icon, contentDescription = null, modifier = Modifier.size(14.dp), tint = Slate400)
        Text(text, style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Medium), color = Slate600)
    }
}

@Composable
private fun EmptyState(onRetry: (() -> Unit)? = null) {
    Column(
        modifier = Modifier.fillMaxWidth().padding(40.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Icon(Icons.Filled.VideocamOff, contentDescription = null, modifier = Modifier.size(64.dp), tint = Zinc200)
        Spacer(modifier = Modifier.height(16.dp))
        Text("No Meetings Found", style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Black), color = Slate900)
        Text("Try adjusting your filters or checking back later.", style = MaterialTheme.typography.bodySmall, color = Slate600, textAlign = TextAlign.Center)
        if (onRetry != null) {
            Spacer(modifier = Modifier.height(16.dp))
            Button(onClick = onRetry) {
                Text("Retry")
            }
        }
    }
}

@Composable
private fun CalendarPlaceholder() {
    Card(
        modifier = Modifier.fillMaxWidth().height(300.dp),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        border = BorderStroke(1.dp, Zinc200)
    ) {
        Box(contentAlignment = Alignment.Center, modifier = Modifier.fillMaxSize()) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Icon(Icons.Filled.CalendarMonth, contentDescription = null, modifier = Modifier.size(48.dp), tint = Zinc200)
                Spacer(modifier = Modifier.height(12.dp))
                Text("Calendar Integration", style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Black), color = Slate900)
                Text("Syncing with your schedule...", style = MaterialTheme.typography.labelSmall, color = Slate400)
            }
        }
    }
}

@Composable
private fun MeetingDetailsDialog(meeting: OnlineMeeting, onDismiss: () -> Unit, onJoin: () -> Unit) {
    Dialog(onDismissRequest = onDismiss, properties = DialogProperties(usePlatformDefaultWidth = false)) {
        Card(
            modifier = Modifier.fillMaxWidth().fillMaxHeight(0.85f).padding(16.dp),
            shape = RoundedCornerShape(32.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
        ) {
            Column(modifier = Modifier.fillMaxSize()) {
                // Header
                Row(modifier = Modifier.padding(24.dp), verticalAlignment = Alignment.Top, horizontalArrangement = Arrangement.SpaceBetween) {
                    Column(modifier = Modifier.weight(1f)) {
                        StatusBadge(meeting.status)
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(meeting.title, style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Black), color = Slate900)
                        Text(meeting.organizer, style = MaterialTheme.typography.labelSmall, color = Purple600)
                    }
                    IconButton(onClick = onDismiss, modifier = Modifier.background(Zinc50, CircleShape)) {
                        Icon(Icons.Filled.Close, contentDescription = null, tint = Slate600)
                    }
                }

                HorizontalDivider(color = Zinc200.copy(alpha = 0.5f))

                // Content
                Column(modifier = Modifier.weight(1f).verticalScroll(rememberScrollState()).padding(24.dp), verticalArrangement = Arrangement.spacedBy(24.dp)) {
                    // Quick Info
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                        DetailTag("Platform", meeting.platform.label, Icons.Filled.Videocam)
                        DetailTag("Duration", meeting.duration, Icons.Filled.Timer)
                    }

                    // Description
                    Column {
                        Text("SESSION OVERVIEW", style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Black, letterSpacing = 1.sp), color = Slate400)
                        Spacer(modifier = Modifier.height(8.dp))
                        Surface(color = Zinc50, shape = RoundedCornerShape(16.dp), border = BorderStroke(1.dp, Zinc200)) {
                            Text(meeting.description, modifier = Modifier.padding(16.dp), style = MaterialTheme.typography.bodySmall.copy(lineHeight = 20.sp), color = Slate600)
                        }
                    }

                    // Agenda
                    Column {
                        Text("MEETING AGENDA", style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Black, letterSpacing = 1.sp), color = Slate400)
                        Spacer(modifier = Modifier.height(12.dp))
                        meeting.agenda.forEachIndexed { index, item ->
                            Row(modifier = Modifier.padding(vertical = 4.dp), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                                Surface(color = Purple50, shape = CircleShape, modifier = Modifier.size(24.dp)) {
                                    Box(contentAlignment = Alignment.Center) {
                                        Text((index + 1).toString(), style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Black), color = Purple600)
                                    }
                                }
                                Text(item, style = MaterialTheme.typography.bodySmall, color = Slate900)
                            }
                        }
                    }

                    // Notes if completed
                    if (meeting.status == MeetingStatus.COMPLETED && meeting.notes.isNotBlank()) {
                        Column {
                            Text("MEETING MINUTES", style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Black, letterSpacing = 1.sp), color = Slate400)
                            Spacer(modifier = Modifier.height(8.dp))
                            Surface(color = Color(0xFFFFFBEB), shape = RoundedCornerShape(16.dp), border = BorderStroke(1.dp, Color(0xFFFEF3C7))) {
                                Text(meeting.notes, modifier = Modifier.padding(16.dp), style = MaterialTheme.typography.bodySmall.copy(lineHeight = 20.sp), color = Color(0xFF92400E))
                            }
                        }
                    }
                }

                // Action
                Box(modifier = Modifier.padding(24.dp)) {
                    if (meeting.status != MeetingStatus.COMPLETED && meeting.status != MeetingStatus.CANCELLED) {
                        Button(
                            onClick = onJoin,
                            modifier = Modifier.fillMaxWidth().height(56.dp),
                            shape = RoundedCornerShape(16.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = Slate900)
                        ) {
                            Icon(Icons.Filled.Launch, contentDescription = null)
                            Spacer(modifier = Modifier.width(12.dp))
                            Text("Launch Virtual Room", style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Black))
                        }
                    } else {
                        Button(
                            onClick = onDismiss,
                            modifier = Modifier.fillMaxWidth().height(56.dp),
                            shape = RoundedCornerShape(16.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = Slate900)
                        ) {
                            Text("Close Detail View", style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Black))
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun DetailTag(label: String, value: String, icon: ImageVector) {
    Surface(modifier = Modifier.height(40.dp), shape = RoundedCornerShape(20.dp), color = Zinc50, border = BorderStroke(1.dp, Zinc200)) {
        Row(modifier = Modifier.padding(horizontal = 12.dp), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            Icon(icon, contentDescription = null, modifier = Modifier.size(14.dp), tint = Slate400)
            Text("${label}:", style = MaterialTheme.typography.labelSmall, color = Slate400)
            Text(value, style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Black), color = Slate900)
        }
    }
}
