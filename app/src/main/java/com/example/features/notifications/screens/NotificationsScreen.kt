package com.example.features.notifications.screens

import androidx.compose.animation.*
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.core.theme.*
import com.example.core.ui.shimmerEffect
import com.example.features.notifications.models.NotificationRecord
import com.example.features.notifications.models.NotificationTypeMeta
import com.example.features.notifications.providers.NotificationViewModel
import com.example.features.student.widgets.StudentDrawer
import com.example.features.student.widgets.StudentDrawer
import kotlinx.coroutines.launch
import androidx.paging.compose.collectAsLazyPagingItems
import androidx.paging.LoadState

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NotificationsScreen(
    viewModel: NotificationViewModel = viewModel(),
    onNavigate: (String) -> Unit
) {
    val uiState by viewModel.uiState.collectAsState()
    val drawerState = rememberDrawerState(initialValue = DrawerValue.Closed)
    val scope = rememberCoroutineScope()

    ModalNavigationDrawer(
        drawerState = drawerState,
        drawerContent = {
            StudentDrawer(
                currentRoute = "/student/notifications",
                onNavigate = {
                    scope.launch { drawerState.close() }
                    onNavigate(it)
                }
            )
        }
    ) {
        Scaffold(
            containerColor = LexNovaSlateLight
        ) { paddingValues ->
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
            ) {
                val pagingItems = viewModel.notificationsPagingFlow.collectAsLazyPagingItems()

                NotificationHeader(
                    unreadCount = uiState.notifications.count { !it.isRead }, // Note: in real paging this should be from a separate stats endpoint
                    totalCount = uiState.notifications.size, // Note: in real paging this should be from a separate stats endpoint
                    onMenuClick = { scope.launch { drawerState.open() } },
                    onMarkAllRead = { viewModel.markAllAsRead() },
                    onRefresh = { pagingItems.refresh() },
                    onLogout = { onNavigate("LOGOUT") }
                )

                Surface(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp)
                        .offset(y = (-20).dp),
                    shape = RoundedCornerShape(20.dp),
                    color = Color.White,
                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.surfaceVariant),
                    shadowElevation = 4.dp
                ) {
                    Column {
                        // Tabs
                        Row(modifier = Modifier.fillMaxWidth()) {
                            TabButton("all", "All (${uiState.notifications.size})", uiState.activeTab == "all") { viewModel.setTab("all") }
                            TabButton("unread", "Unread (${uiState.notifications.count { !it.isRead }})", uiState.activeTab == "unread", badge = uiState.notifications.count { !it.isRead }) { viewModel.setTab("unread") }
                            TabButton("read", "Read (${uiState.notifications.count { it.isRead }})", uiState.activeTab == "read") { viewModel.setTab("read") }
                        }
                        
                        HorizontalDivider(color = androidx.compose.ui.graphics.Color.White)

                        // Search and Filter
                        Row(
                            modifier = Modifier.padding(12.dp),
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            OutlinedTextField(
                                value = uiState.searchQuery,
                                onValueChange = { viewModel.setSearchQuery(it) },
                                placeholder = { Text("Search notifications...", fontSize = 12.sp) },
                                modifier = Modifier.weight(1f).height(44.dp),
                                leadingIcon = { Icon(Icons.Filled.Search, null, modifier = Modifier.size(16.dp), tint = LexNovaSlateAccent) },
                                shape = RoundedCornerShape(12.dp),
                                colors = OutlinedTextFieldDefaults.colors(
                                    unfocusedContainerColor = androidx.compose.ui.graphics.Color.White,
                                    focusedContainerColor = Color.White,
                                    unfocusedBorderColor = androidx.compose.ui.graphics.Color.White
                                ),
                                singleLine = true,
                                textStyle = LocalTextStyle.current.copy(fontSize = 12.sp)
                            )
                            
                            TypeFilterButton(uiState.typeFilter) { viewModel.setTypeFilter(it) }
                            
                            if (uiState.notifications.any { it.isRead }) {
                                IconButton(
                                    onClick = { viewModel.deleteAllRead() },
                                    modifier = Modifier.size(44.dp).background(Color(0xFFFEF2F2), RoundedCornerShape(12.dp)).border(1.dp, Color(0xFFFEE2E2), RoundedCornerShape(12.dp))
                                ) {
                                    Icon(Icons.Filled.DeleteSweep, null, tint = Color(0xFFEF4444), modifier = Modifier.size(18.dp))
                                }
                            }
                        }

                        HorizontalDivider(color = androidx.compose.ui.graphics.Color.White)

                        // List
                        if (pagingItems.loadState.refresh is LoadState.Loading) {
                            LoadingState()
                        } else if (pagingItems.itemCount == 0 && pagingItems.loadState.append.endOfPaginationReached) {
                            EmptyState(
                                tab = uiState.activeTab,
                                isFiltered = uiState.searchQuery.isNotEmpty() || uiState.typeFilter != "all",
                                onRetry = { pagingItems.refresh() },
                                onClear = {
                                    viewModel.setSearchQuery("")
                                    viewModel.setTypeFilter("all")
                                }
                            )
                        } else {
                            LazyColumn(modifier = Modifier.heightIn(max = 500.dp)) {
                                items(pagingItems.itemCount) { index ->
                                    val notif = pagingItems[index]
                                    if (notif != null) {
                                        NotificationRow(notif, onMarkRead = { viewModel.markAsRead(notif.id) }, onDelete = { viewModel.deleteNotification(notif.id) })
                                    }
                                }
                            }
                        }
                        
                        if (pagingItems.itemCount > 0) {
                            Box(modifier = Modifier.fillMaxWidth().background(Color(0xFFF8FAFC)).padding(12.dp)) {
                                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                                    Text("Showing ${pagingItems.itemCount} notifications", fontSize = 13.sp, color = LexNovaSlateAccent)
                                    // Mark all read button omitted for brevity in Paging
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun NotificationHeader(
    unreadCount: Int,
    totalCount: Int,
    onMenuClick: () -> Unit,
    onMarkAllRead: () -> Unit,
    onRefresh: () -> Unit,
    onLogout: () -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(180.dp)
            .background(
                Brush.linearGradient(
                    colors = listOf(Color(0xFF9333EA), Color(0xFF7C3AED), Color(0xFF4F46E5))
                )
            )
            .padding(16.dp)
    ) {
        Column(modifier = Modifier.fillMaxSize()) {
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                IconButton(onClick = onMenuClick, modifier = Modifier.background(MaterialTheme.colorScheme.surface.copy(alpha = 0.15f), RoundedCornerShape(12.dp))) {
                    Icon(Icons.Filled.Menu, null, tint = Color.White)
                }
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    if (unreadCount > 0) {
                        Surface(
                            onClick = onMarkAllRead,
                            color = Color.White.copy(alpha = 0.15f),
                            shape = RoundedCornerShape(12.dp),
                            border = BorderStroke(1.dp, Color.White.copy(alpha = 0.2f))
                        ) {
                            Row(modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                                Icon(Icons.Filled.DoneAll, null, tint = Color.White, modifier = Modifier.size(14.dp))
                                Text("Mark all read", color = Color.White, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                    IconButton(onClick = onRefresh, modifier = Modifier.background(MaterialTheme.colorScheme.surface.copy(alpha = 0.1f), RoundedCornerShape(12.dp))) {
                        Icon(Icons.Filled.Refresh, null, tint = Color.White, modifier = Modifier.size(18.dp))
                    }
                    IconButton(onClick = onLogout, modifier = Modifier.background(MaterialTheme.colorScheme.surface.copy(alpha = 0.1f), RoundedCornerShape(12.dp))) {
                        Icon(Icons.Filled.Logout, null, tint = Color.White, modifier = Modifier.size(18.dp))
                    }
                }
            }
            
            Spacer(Modifier.height(24.dp))
            
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                Box(modifier = Modifier.size(56.dp).background(MaterialTheme.colorScheme.surface.copy(alpha = 0.15f), RoundedCornerShape(16.dp)).border(1.dp, Color.White.copy(alpha = 0.2f), RoundedCornerShape(16.dp)), contentAlignment = Alignment.Center) {
                    Icon(Icons.Filled.Notifications, null, tint = Color.White, modifier = Modifier.size(28.dp))
                    if (unreadCount > 0) {
                        Surface(
                            color = Color(0xFFF43F5E),
                            shape = CircleShape,
                            border = BorderStroke(2.dp, Color.White),
                            modifier = Modifier.align(Alignment.TopEnd).offset(x = 4.dp, y = (-4).dp)
                        ) {
                            Text(
                                if (unreadCount > 99) "99+" else unreadCount.toString(),
                                modifier = Modifier.padding(horizontal = 4.dp, vertical = 1.dp),
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Black,
                                color = Color.White
                            )
                        }
                    }
                }
                Column {
                    Text("Notifications", style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Black, color = Color.White)
                    Text(
                        if (unreadCount > 0) "${unreadCount} unread · ${totalCount} total" else "All caught up! · ${totalCount} total",
                        fontSize = 14.sp,
                        color = Color.White.copy(alpha = 0.8f)
                    )
                }
            }
        }
    }
}

@Composable
private fun RowScope.TabButton(id: String, label: String, isSelected: Boolean, badge: Int = 0, onClick: () -> Unit) {
    Box(
        modifier = Modifier
            .weight(1f)
            .clickable { onClick() }
            .padding(vertical = 14.dp),
        contentAlignment = Alignment.Center
    ) {
        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
            if (badge > 0) {
                Surface(color = Color(0xFFF43F5E), shape = CircleShape) {
                    Text(badge.toString(), modifier = Modifier.padding(horizontal = 4.dp, vertical = 1.dp), fontSize = 13.sp, fontWeight = FontWeight.Black, color = Color.White)
                }
            }
            Text(
                label,
                fontSize = 13.sp,
                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                color = if (isSelected) Color(0xFF7C3AED) else LexNovaSlateAccent
            )
        }
        if (isSelected) {
            Box(Modifier.align(Alignment.BottomCenter).fillMaxWidth().height(2.dp).background(Color(0xFF7C3AED), RoundedCornerShape(topStart = 2.dp, topEnd = 2.dp)))
        }
    }
}

@Composable
private fun TypeFilterButton(current: String, onSelect: (String) -> Unit) {
    var expanded by remember { mutableStateOf(false) }
    
    Box {
        Surface(
            onClick = { expanded = true },
            color = if (current != "all") Color(0xFFF5F3FF) else Color(0xFFF8FAFC),
            shape = RoundedCornerShape(12.dp),
            border = BorderStroke(1.dp, if (current != "all") Color(0xFFDDD6FE) else Color(0xFFE2E8F0)),
            modifier = Modifier.height(44.dp)
        ) {
            Row(modifier = Modifier.padding(horizontal = 12.dp), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Icon(Icons.Filled.FilterList, null, modifier = Modifier.size(16.dp), tint = if (current != "all") Color(0xFF7C3AED) else LexNovaSlateAccent)
                Text(if (current == "all") "Filter" else current.replace("_", " ").capitalize(), fontSize = 12.sp, fontWeight = FontWeight.Bold, color = if (current != "all") Color(0xFF7C3AED) else LexNovaSlateAccent)
                Icon(Icons.Filled.KeyboardArrowDown, null, modifier = Modifier.size(16.dp), tint = LexNovaSlateAccent)
            }
        }
        
        DropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }, modifier = Modifier.background(MaterialTheme.colorScheme.surface).border(1.dp, Color(0xFFE2E8F0), RoundedCornerShape(8.dp))) {
            val types = listOf("all", "marks_submission", "marks_approval", "attendance_lock", "leave_approval", "grievance_update", "material_upload", "new_assignment")
            types.forEach { type ->
                DropdownMenuItem(
                    text = { Text(type.replace("_", " ").capitalize(), fontSize = 12.sp, fontWeight = if (current == type) FontWeight.Bold else FontWeight.Medium) },
                    onClick = { onSelect(type); expanded = false },
                    leadingIcon = { 
                        val meta = getTypeMeta(type)
                        Icon(meta.icon, null, tint = meta.color, modifier = Modifier.size(16.dp))
                    }
                )
            }
        }
    }
}

@Composable
private fun NotificationRow(notif: NotificationRecord, onMarkRead: () -> Unit, onDelete: () -> Unit) {
    val meta = getTypeMeta(notif.type)
    
    Surface(
        onClick = onMarkRead,
        color = if (!notif.isRead) Color(0xFFF5F3FF).copy(alpha = 0.4f) else Color.White,
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(modifier = Modifier.padding(16.dp), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            Box(modifier = Modifier.size(40.dp).background(meta.bgColor, RoundedCornerShape(12.dp)).border(1.dp, meta.borderColor, RoundedCornerShape(12.dp)), contentAlignment = Alignment.Center) {
                Icon(meta.icon, null, tint = meta.color, modifier = Modifier.size(18.dp))
                if (!notif.isRead) {
                    Box(modifier = Modifier.size(8.dp).background(Color(0xFF8B5CF6), CircleShape).border(1.5.dp, Color.White, CircleShape).align(Alignment.TopEnd).offset(x = 2.dp, y = (-2).dp))
                }
            }
            
            Column(modifier = Modifier.weight(1f)) {
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Surface(color = meta.bgColor, shape = RoundedCornerShape(6.dp), border = BorderStroke(1.dp, meta.borderColor)) {
                        Text(meta.label.uppercase(), modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp), fontSize = 13.sp, fontWeight = FontWeight.Black, color = meta.color)
                    }
                    Text(formatRelativeTime(notif.createdAt), fontSize = 13.sp, color = LexNovaSlateAccent)
                }
                Spacer(Modifier.height(6.dp))
                Text(
                    notif.message,
                    fontSize = 14.sp,
                    fontWeight = if (!notif.isRead) FontWeight.Bold else FontWeight.Normal,
                    color = if (!notif.isRead) LexNovaSlateDark else LexNovaSlateAccent,
                    lineHeight = 20.sp
                )
                if (notif.isRead) {
                    Row(modifier = Modifier.padding(top = 4.dp), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                        Icon(Icons.Filled.Check, null, tint = Color(0xFFCBD5E1), modifier = Modifier.size(10.dp))
                        Text("Read", fontSize = 12.sp, color = Color(0xFFCBD5E1), fontWeight = FontWeight.Medium)
                    }
                }
            }
            
            IconButton(onClick = onDelete, modifier = Modifier.size(32.dp)) {
                Icon(Icons.Filled.DeleteOutline, null, tint = Color(0xFF94A3B8), modifier = Modifier.size(16.dp))
            }
        }
    }
    HorizontalDivider(color = androidx.compose.ui.graphics.Color.White, modifier = Modifier.padding(horizontal = 16.dp))
}

@Composable
private fun LoadingState() {
    Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(16.dp)) {
        repeat(4) {
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                Box(Modifier.size(40.dp).shimmerEffect().clip(RoundedCornerShape(12.dp)))
                Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Box(Modifier.width(80.dp).height(12.dp).shimmerEffect().clip(RoundedCornerShape(4.dp)))
                    Box(Modifier.fillMaxWidth().height(16.dp).shimmerEffect().clip(RoundedCornerShape(4.dp)))
                }
            }
        }
    }
}

@Composable
private fun EmptyState(tab: String, isFiltered: Boolean, onRetry: (() -> Unit)? = null, onClear: () -> Unit) {
    Column(
        modifier = Modifier.fillMaxWidth().padding(vertical = 60.dp, horizontal = 40.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Box(modifier = Modifier.size(64.dp).background(Color(0xFFF5F3FF), RoundedCornerShape(20.dp)).border(1.dp, Color(0xFFDDD6FE), RoundedCornerShape(20.dp)), contentAlignment = Alignment.Center) {
            Icon(Icons.Filled.NotificationsNone, null, tint = Color(0xFFC4B5FD), modifier = Modifier.size(32.dp))
        }
        Spacer(Modifier.height(20.dp))
        Text("No notifications", fontWeight = FontWeight.Black, fontSize = 16.sp, color = LexNovaSlateDark)
        Text(
            if (isFiltered) "No notifications match your filters." else if (tab == "unread") "You're all caught up!" else "You have no notifications yet.",
            fontSize = 13.sp,
            color = LexNovaSlateAccent,
            textAlign = TextAlign.Center,
            modifier = Modifier.padding(top = 4.dp)
        )
        if (isFiltered) {
            TextButton(onClick = onClear) {
                Text("Clear filters", fontSize = 12.sp, fontWeight = FontWeight.Bold)
            }
        }
        if (onRetry != null && !isFiltered) {
            Spacer(modifier = Modifier.height(16.dp))
            Button(onClick = onRetry) {
                Text("Retry")
            }
        }
    }
}

private fun getTypeMeta(type: String): NotificationTypeMeta {
    return when (type) {
        "marks_submission" -> NotificationTypeMeta("Marks Submitted", Icons.Filled.Description, Color(0xFF4338CA), Color(0xFFEEF2FF), Color(0xFFC7D2FE))
        "marks_approval" -> NotificationTypeMeta("Marks Approved", Icons.Filled.FactCheck, Color(0xFF059669), Color(0xFFECFDF5), Color(0xFFA7F3D0))
        "attendance_lock" -> NotificationTypeMeta("Attendance", Icons.Filled.CalendarMonth, Color(0xFFB45309), Color(0xFFFFFBEB), Color(0xFFFDE68A))
        "leave_approval" -> NotificationTypeMeta("Leave Approved", Icons.Filled.CheckCircle, Color(0xFF059669), Color(0xFFECFDF5), Color(0xFFA7F3D0))
        "grievance_update" -> NotificationTypeMeta("Grievance", Icons.Filled.ErrorOutline, Color(0xFFD97706), Color(0xFFFFF7ED), Color(0xFFFFEDD5))
        "material_upload" -> NotificationTypeMeta("Study Material", Icons.Filled.LibraryBooks, Color(0xFF7C3AED), Color(0xFFF5F3FF), Color(0xFFDDD6FE))
        "new_assignment" -> NotificationTypeMeta("Assignment", Icons.Filled.Assignment, Color(0xFF2563EB), Color(0xFFEFF6FF), Color(0xFFDBEAFE))
        else -> NotificationTypeMeta("Notification", Icons.Filled.Notifications, Color(0xFF52525B), Color(0xFFF8FAFC), Color(0xFFE2E8F0))
    }
}

private fun formatRelativeTime(iso: String): String {
    return try {
        val sdf = java.text.SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss", java.util.Locale.US)
        val date = sdf.parse(iso) ?: return ""
        val diff = System.currentTimeMillis() - date.time
        val mins = diff / 60000
        when {
            mins < 1 -> "Just now"
            mins < 60 -> "${mins}m ago"
            mins < 1440 -> "${mins / 60}h ago"
            mins < 10080 -> "${mins / 1440}d ago"
            else -> java.text.SimpleDateFormat("dd MMM yyyy", java.util.Locale.US).format(date)
        }
    } catch (e: Exception) {
        ""
    }
}

private fun String.capitalize() = replaceFirstChar { if (it.isLowerCase()) it.titlecase(java.util.Locale.US) else it.toString() }
