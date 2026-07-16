package com.example.features.campus_life.screens

import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.compose.animation.*
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.core.theme.LexNovaPurple
import com.example.core.navigation.AppRoutes
import com.example.core.theme.*
import com.example.core.ui.CamsCard
import com.example.core.ui.CamsScreen
import com.example.features.campus_life.models.*
import com.example.features.campus_life.providers.ClubsViewModel
import com.example.features.student.widgets.StudentDrawer
import kotlinx.coroutines.launch
import androidx.paging.compose.collectAsLazyPagingItems
import androidx.paging.LoadState

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun StudentClubsScreen(
    viewModel: ClubsViewModel = viewModel(),
    onNavigate: (String) -> Unit
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    
    var activeTab by remember { mutableStateOf("directory") }
    var searchQuery by remember { mutableStateOf("") }
    var selectedClub by remember { mutableStateOf<Club?>(null) }
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    val scope = rememberCoroutineScope()

    val announcements = listOf(
        ClubAnnouncement("Intra-College Moot Registrations Open", "Moot Court Society", "Today", true),
        ClubAnnouncement("Parliamentary Debate Tryouts", "Debate & Literary Club", "Tomorrow", false),
        ClubAnnouncement("Legal Aid Camp at Slum Area 4", "Legal Aid Clinic", "Oct 15", false)
    )

    val pagingItems = viewModel.clubsPagingFlow.collectAsLazyPagingItems()

    val stats = remember(uiState.clubs) {
        val memberships = uiState.clubs.count { it.role != "None" } // Note: real paging would get this from a separate endpoint
        val leadership = uiState.clubs.count { it.role != "None" && it.role != "Member" }
        mapOf("attended" to "12", "leadership" to leadership.toString(), "memberships" to memberships.toString())
    }

    CamsScreen(scrollable = true,
        title = "Clubs & Societies",
        subtitle = "Empowering Student Communities",
        onBackClick = { onNavigate(AppRoutes.STUDENT_DASHBOARD) }
    ) {
        Column(
            modifier = Modifier.fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(24.dp)
        ) {
            CamsCard {
                Text(
                    "Join and participate in campus clubs. Stay updated with club events, leadership roles, and membership status.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    lineHeight = 18.sp
                )
            }

            // Tabs and Search
            CamsCard {
                Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth().drawBehind {
                            val strokeWidth = 1.dp.toPx()
                            val y = size.height - strokeWidth / 2
                            drawLine(
                                color = Color.LightGray.copy(alpha = 0.2f),
                                start = Offset(0f, y),
                                end = Offset(size.width, y),
                                strokeWidth = strokeWidth
                            )
                        },
                        horizontalArrangement = Arrangement.spacedBy(24.dp)
                    ) {
                        TabItem("Directory", activeTab == "directory") { activeTab = "directory" }
                        TabItem("My Clubs (${stats["memberships"]})", activeTab == "my-clubs") { activeTab = "my-clubs" }
                    }

                    OutlinedTextField(
                        value = searchQuery,
                        onValueChange = { searchQuery = it },
                        modifier = Modifier.fillMaxWidth(),
                        placeholder = { Text("Search clubs...", fontSize = 12.sp) },
                        leadingIcon = { Icon(Icons.Filled.Search, contentDescription = null, modifier = Modifier.size(16.dp), tint = MaterialTheme.colorScheme.primary) },
                        shape = RoundedCornerShape(12.dp),
                        singleLine = true,
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = CamsNavy,
                            unfocusedBorderColor = Color.LightGray.copy(alpha = 0.5f)
                        )
                    )
                }
            }

            // Main Content List
            Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                if (pagingItems.loadState.refresh is LoadState.Loading) {
                    CircularProgressIndicator(modifier = Modifier.align(Alignment.CenterHorizontally))
                } else if (pagingItems.itemCount == 0 && pagingItems.loadState.append.endOfPaginationReached) {
                    EmptyClubsView()
                } else {
                    for (index in 0 until pagingItems.itemCount) {
                        val club = pagingItems[index]
                        if (club != null) {
                            val matchesSearch = club.name.contains(searchQuery, ignoreCase = true) || 
                                              club.category.contains(searchQuery, ignoreCase = true)
                            val matchesTab = activeTab == "directory" || club.role != "None"
                            if (matchesSearch && matchesTab) {
                                ClubCard(
                                    club = club,
                                    onViewDashboard = { selectedClub = it },
                                    onJoin = { viewModel.joinClub(it.id) },
                                    onLeave = { viewModel.leaveClub(it.id) }
                                )
                            }
                        }
                    }
                }
            }

            // Announcements
            AnnouncementsSection(announcements)
            EngagementSection(stats)
            
            Spacer(Modifier.height(20.dp))
        }
    }

    if (selectedClub != null) {
        ModalBottomSheet(
            onDismissRequest = { selectedClub = null },
            sheetState = sheetState,
            containerColor = MaterialTheme.colorScheme.surface,
            dragHandle = { BottomSheetDefaults.DragHandle() }
        ) {
            ClubDashboardSheet(
                club = selectedClub!!,
                onClose = { scope.launch { sheetState.hide() }.invokeOnCompletion { selectedClub = null } },
                onLeave = { 
                    viewModel.leaveClub(selectedClub!!.id)
                    scope.launch { sheetState.hide() }.invokeOnCompletion { selectedClub = null }
                }
            )
        }
    }
}

@Composable
private fun TabItem(label: String, isSelected: Boolean, onClick: () -> Unit) {
    Column(
        modifier = Modifier.clickable { onClick() }.padding(bottom = 8.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            label,
            style = MaterialTheme.typography.labelLarge.copy(
                fontWeight = if (isSelected) FontWeight.Black else FontWeight.Bold
            ),
            color = if (isSelected) CamsNavy else CamsTextSecondary
        )
        if (isSelected) {
            Box(modifier = Modifier.width(40.dp).height(2.dp).background(CamsNavy, RoundedCornerShape(topStart = 4.dp, topEnd = 4.dp)))
        }
    }
}

@Composable
private fun ClubCard(
    club: Club,
    onViewDashboard: (Club) -> Unit,
    onJoin: (Club) -> Unit,
    onLeave: (Club) -> Unit
) {
    CamsCard(
        modifier = Modifier.fillMaxWidth(),
    ) {
        Column {
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = CamsBackground,
                    modifier = Modifier.size(48.dp)
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(club.icon, contentDescription = null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(24.dp))
                    }
                }
                Column {
                    Text(club.name, style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Black), color = MaterialTheme.colorScheme.onSurface)
                    Text(club.category.uppercase(), style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold), color = CamsNavy)
                }
            }

            Spacer(modifier = Modifier.height(12.dp))
            Text(
                club.description,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis
            )

            Spacer(modifier = Modifier.height(16.dp))
            HorizontalDivider(color = Color.LightGray.copy(alpha = 0.1f))
            Spacer(modifier = Modifier.height(16.dp))

            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                Row(horizontalArrangement = Arrangement.spacedBy(12.dp), verticalAlignment = Alignment.CenterVertically) {
                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                        Icon(Icons.Filled.Groups, contentDescription = null, tint = MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.size(14.dp))
                        Text("${club.membersCount}", style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold), color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                    if (club.role != "None") {
                        Surface(
                            color = CamsNavy.copy(alpha = 0.1f),
                            shape = RoundedCornerShape(4.dp),
                        ) {
                            Row(modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                                Icon(Icons.Filled.Shield, contentDescription = null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(12.dp))
                                Text(club.role, style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold), color = CamsNavy)
                            }
                        }
                    }
                }

                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    if (club.role != "None") {
                        OutlinedButton(
                            onClick = { onViewDashboard(club) },
                            shape = RoundedCornerShape(8.dp),
                            border = BorderStroke(1.dp, Color.LightGray.copy(alpha = 0.5f)),
                            colors = ButtonDefaults.outlinedButtonColors(contentColor = CamsTextPrimary),
                            contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp)
                        ) {
                            Text("Dashboard", style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold))
                        }
                    } else {
                        Button(
                            onClick = { onJoin(club) },
                            shape = RoundedCornerShape(8.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = CamsNavy),
                            contentPadding = PaddingValues(horizontal = 16.dp, vertical = 6.dp)
                        ) {
                            Text("Join Club", style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold))
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun EmptyClubsView() {
    Column(
        modifier = Modifier.fillMaxWidth().padding(vertical = 40.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Icon(Icons.Filled.Groups, contentDescription = null, modifier = Modifier.size(48.dp), tint = MaterialTheme.colorScheme.outlineVariant)
        Spacer(modifier = Modifier.height(8.dp))
        Text("No clubs found matching these filters.", style = MaterialTheme.typography.bodySmall, color = Color(0xFF94A3B8))
    }
}

@Composable
private fun AnnouncementsSection(announcements: List<ClubAnnouncement>) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.surfaceVariant)
    ) {
        Column(modifier = Modifier.padding(20.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Icon(Icons.Filled.Notifications, contentDescription = null, tint = Color(0xFFF59E0B), modifier = Modifier.size(16.dp))
                Text("Club Announcements", style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.Black))
            }
            Spacer(modifier = Modifier.height(16.dp))
            Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                announcements.forEach { ann ->
                    Row(modifier = Modifier.height(IntrinsicSize.Min)) {
                        Box(
                            modifier = Modifier
                                .fillMaxHeight()
                                .width(2.dp)
                                .background(LexNovaPurple.copy(alpha = 0.3f))
                        )
                        Column(modifier = Modifier.padding(start = 12.dp)) {
                            Text(ann.title, style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Bold), color = Color(0xFF1E293B))
                            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                Text(ann.club, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                Text(ann.date, style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold), color = if (ann.isUrgent) Color.Red else Color(0xFF94A3B8))
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun EngagementSection(stats: Map<String, String>) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.secondaryContainer),
        border = BorderStroke(1.dp, Color(0xFFEDE9FE))
    ) {
        Column(modifier = Modifier.padding(20.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Icon(Icons.Filled.BarChart, contentDescription = null, tint = Color(0xFF10B981), modifier = Modifier.size(16.dp))
                Text("My Engagement", style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.Black))
            }
            Spacer(modifier = Modifier.height(16.dp))
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                EngagementStatRow("Events Attended", stats["attended"] ?: "0", Color(0xFF10B981))
                EngagementStatRow("Leadership Roles", stats["leadership"] ?: "0", Color(0xFFF59E0B))
                EngagementStatRow("Club Meetings", "85% Avg", Color(0xFF3B82F6))
            }
        }
    }
}

@Composable
private fun EngagementStatRow(label: String, value: String, color: Color) {
    Row(
        modifier = Modifier.fillMaxWidth().background(MaterialTheme.colorScheme.surface, RoundedCornerShape(12.dp)).border(1.dp, MaterialTheme.colorScheme.surfaceVariant, RoundedCornerShape(12.dp)).padding(12.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(label, style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold), color = MaterialTheme.colorScheme.onSurfaceVariant)
        Text(value, style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Black), color = color)
    }
}

@Composable
private fun ClubDashboardSheet(
    club: Club,
    onClose: () -> Unit,
    onLeave: () -> Unit
) {
    Column(
        modifier = Modifier.fillMaxWidth().padding(24.dp).verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.spacedBy(24.dp)
    ) {
        // Header
        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(16.dp)) {
            Surface(
                shape = RoundedCornerShape(16.dp),
                color = MaterialTheme.colorScheme.background,
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
                modifier = Modifier.size(56.dp)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(club.icon, contentDescription = null, tint = LexNovaPurple, modifier = Modifier.size(28.dp))
                }
            }
            Column(modifier = Modifier.weight(1f)) {
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(club.name, style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Black))
                    Surface(color = Color(0xFFF3E8FF), shape = RoundedCornerShape(4.dp)) {
                        Text(
                            club.category.uppercase(),
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                            style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold, fontSize = 13.sp),
                            color = Color(0xFF9333EA)
                        )
                    }
                }
                Text("Lead by President: ${club.president}", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
        }

        // About
        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Text("ABOUT THE CLUB", style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Black, color = Color(0xFF94A3B8)))
            Surface(color = MaterialTheme.colorScheme.background, shape = RoundedCornerShape(12.dp), border = BorderStroke(1.dp, MaterialTheme.colorScheme.surfaceVariant)) {
                Text(
                    "${club.description} The club conducts weekly learning programs, hosts workshops, represents the college in national fests, and fosters strong professional legal practices.",
                    modifier = Modifier.padding(16.dp),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    lineHeight = 18.sp
                )
            }
        }

        // Stats
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            DashboardStatCard(Modifier.weight(1f), "TOTAL ACTIVE MEMBERS", "${club.membersCount}", Color(0xFF1E293B))
            DashboardStatCard(Modifier.weight(1f), "YOUR CURRENT ROLE", club.role, Color(0xFF9333EA))
        }

        // Schedule and Contact
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                DashboardInfoBox(Icons.Filled.CalendarToday, "NEXT MEETING SCHEDULE", club.nextMeeting ?: "No meeting scheduled", Color(0xFF9333EA))
            }
            Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                DashboardContactBox(club.contact, club.phone)
            }
        }

        // Actions
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
            TextButton(onClick = onLeave) {
                Text("Leave Club", color = Color.Red, style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.Bold))
            }
            Button(
                onClick = onClose,
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF0F172A)),
                shape = RoundedCornerShape(12.dp)
            ) {
                Text("Close Dashboard", style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.Bold))
            }
        }
        
        Spacer(modifier = Modifier.height(20.dp))
    }
}

@Composable
private fun DashboardStatCard(modifier: Modifier, label: String, value: String, valueColor: Color) {
    Card(
        modifier = modifier,
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            Text(label, style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Black, fontSize = 12.sp, color = Color(0xFF94A3B8)))
            Text(value, style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Black), color = valueColor)
        }
    }
}

@Composable
private fun DashboardInfoBox(icon: ImageVector, label: String, value: String, iconColor: Color) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                Icon(icon, contentDescription = null, tint = iconColor, modifier = Modifier.size(12.dp))
                Text(label, style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Black, fontSize = 12.sp, color = Color(0xFF94A3B8)))
            }
            Spacer(modifier = Modifier.height(4.dp))
            Text(value, style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold), color = Color(0xFF1E293B))
        }
    }
}

@Composable
private fun DashboardContactBox(email: String, phone: String) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                Icon(Icons.Filled.Info, contentDescription = null, tint = Color(0xFF3B82F6), modifier = Modifier.size(12.dp))
                Text("CLUB REGISTRY CONTACT", style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Black, fontSize = 12.sp, color = Color(0xFF94A3B8)))
            }
            Spacer(modifier = Modifier.height(4.dp))
            Text(email, style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold), color = LexNovaPurple)
            Text(phone, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
    }
}
