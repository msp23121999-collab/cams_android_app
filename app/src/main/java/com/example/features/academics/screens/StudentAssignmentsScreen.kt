package com.example.features.academics.screens

import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.compose.foundation.BorderStroke
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
import androidx.compose.material.icons.automirrored.filled.*
import androidx.compose.material.icons.filled.ArrowForward
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
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
import com.example.features.academics.models.*
import com.example.features.student.providers.AssignmentsViewModel
import com.example.features.student.widgets.StudentDrawer
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*
import kotlin.math.roundToInt
import androidx.paging.compose.collectAsLazyPagingItems
import androidx.paging.LoadState

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun StudentAssignmentsScreen(
    viewModel: AssignmentsViewModel,
    onNavigate: (String) -> Unit
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    var activeTab by remember { mutableStateOf("Available") }

    CamsScreen(
        scrollable = false,
        title = "Assignments",
        onBackClick = { onNavigate(AppRoutes.STUDENT_DASHBOARD) }
    ) {
        if (uiState.isLoading) {
            AssignmentsSkeleton()
        } else if (uiState.error != null) {
            com.example.core.ui.NetworkErrorView(
                message = uiState.error!!,
                onRetry = { viewModel.fetchAssignments() },
                modifier = Modifier.fillMaxSize()
            )
        } else {
            // Tab Row
            ScrollableTabRow(
                selectedTabIndex = listOf("Available", "Submissions", "Marks", "Calendar").indexOf(activeTab),
                containerColor = Color.Transparent,
                contentColor = CamsNavy,
                edgePadding = 0.dp,
                divider = {},
                indicator = { tabPositions ->
                    TabRowDefaults.SecondaryIndicator(
                        Modifier.tabIndicatorOffset(tabPositions[listOf("Available", "Submissions", "Marks", "Calendar").indexOf(activeTab)]),
                        color = CamsNavy
                    )
                }
            ) {
                listOf("Available", "Submissions", "Marks", "Calendar").forEach { tab ->
                    Tab(
                        selected = activeTab == tab,
                        onClick = { activeTab = tab },
                        text = {
                            Text(
                                tab,
                                style = MaterialTheme.typography.labelLarge.copy(
                                    fontWeight = if (activeTab == tab) FontWeight.Bold else FontWeight.Medium,
                                    color = if (activeTab == tab) CamsNavy else CamsTextSecondary
                                )
                            )
                        }
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            Box(modifier = Modifier.fillMaxWidth()) {
                when (activeTab) {
                    "Available" -> AvailableAssignmentsTab(viewModel)
                    "Submissions" -> MySubmissionsTab(uiState.assignments)
                    "Marks" -> MarksFeedbackTab(uiState.assignments)
                    "Calendar" -> AssignmentCalendarTab(uiState.assignments)
                }
            }
        }
    }
}

@Composable
private fun AvailableAssignmentsTab(viewModel: AssignmentsViewModel) {
    var searchQuery by remember { mutableStateOf("") }
    val pagingItems = viewModel.assignmentsPagingFlow.collectAsLazyPagingItems()

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            OutlinedTextField(
                value = searchQuery,
                onValueChange = { searchQuery = it },
                modifier = Modifier.fillMaxWidth(),
                placeholder = { Text("Search assignments...", color = MaterialTheme.colorScheme.onSurfaceVariant) },
                leadingIcon = { Icon(Icons.Filled.Search, contentDescription = null, tint = MaterialTheme.colorScheme.onSurfaceVariant) },
                shape = RoundedCornerShape(16.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = CamsNavy,
                    unfocusedBorderColor = Color.LightGray.copy(alpha = 0.2f),
                    focusedContainerColor = Color.White,
                    unfocusedContainerColor = Color.White
                )
            )
        }

        if (pagingItems.loadState.refresh is LoadState.Loading) {
            item {
                AssignmentsSkeleton()
            }
        } else if (pagingItems.itemCount == 0 && pagingItems.loadState.append.endOfPaginationReached) {
            item {
                EmptyState(
                    icon = Icons.Filled.Inbox,
                    title = "No assignments found",
                    message = "Try adjusting your search or filters",
                    onRetry = { pagingItems.refresh() }
                )
            }
        } else {
            items(pagingItems.itemCount) { index ->
                val assignment = pagingItems[index]
                if (assignment != null && (searchQuery.isEmpty() || assignment.title.contains(searchQuery, true) || assignment.subject.contains(searchQuery, true))) {
                    AssignmentCard(assignment, viewModel)
                }
            }
        }
    }
}

@Composable
private fun AssignmentCard(assignment: Assignment, viewModel: AssignmentsViewModel) {
    val deadlineInfo = getDeadlineInfo(assignment.deadline)
    val typeConfig = getAssignmentTypeConfig(assignment.type)
    var showUploadModal by remember { mutableStateOf(false) }

    if (showUploadModal) {
        com.example.features.academics.widgets.FileUploadModal(
            onDismiss = { showUploadModal = false },
            onUpload = { _, name ->
                viewModel.submitAssignment(assignment.id, name, null)
                showUploadModal = false
            }
        )
    }

    CamsCard(
        modifier = Modifier.fillMaxWidth(),
    ) {
        Column {
            // Accent Strip
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(4.dp)
                    .background(
                        if (assignment.status == "Submitted") Color(0xFF3B82F6) 
                        else if (deadlineInfo.isUrgent) Color(0xFFF59E0B) 
                        else CamsNavy
                    )
            )

            Column(modifier = Modifier.padding(16.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.Top
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(12.dp), modifier = Modifier.weight(1f)) {
                        Box(
                            modifier = Modifier
                                .size(48.dp)
                                .clip(RoundedCornerShape(14.dp))
                                .background(typeConfig.bgColor.copy(alpha = 0.1f))
                                .border(1.dp, typeConfig.bgColor.copy(alpha = 0.2f), RoundedCornerShape(14.dp)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(typeConfig.icon, contentDescription = null, tint = typeConfig.bgColor, modifier = Modifier.size(24.dp))
                        }
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                assignment.title,
                                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.ExtraBold),
                                color = MaterialTheme.colorScheme.onSurface,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                            Text(
                                assignment.subject,
                                style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                                color = CamsNavy,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                        }
                    }

                    Spacer(modifier = Modifier.width(8.dp))
                    Box(
                        modifier = Modifier
                            .clip(CircleShape)
                            .background(deadlineInfo.color.copy(alpha = 0.1f))
                            .padding(horizontal = 10.dp, vertical = 4.dp)
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                            Box(modifier = Modifier.size(6.dp).clip(CircleShape).background(deadlineInfo.color))
                            Text(
                                deadlineInfo.label,
                                style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.ExtraBold),
                                color = deadlineInfo.color
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                Text(
                    assignment.description ?: "",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )

                Spacer(modifier = Modifier.height(16.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            "Faculty: Admin",
                            style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                            color = MaterialTheme.colorScheme.onSurface,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                        Text(
                            "Due: ${formatDeadline(assignment.deadline)}",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }

                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        if (assignment.status != "Submitted") {
                            Button(
                                onClick = { showUploadModal = true },
                                shape = RoundedCornerShape(12.dp),
                                colors = ButtonDefaults.buttonColors(containerColor = CamsNavy),
                                contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp)
                            ) {
                                Icon(Icons.Filled.Upload, contentDescription = null, modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(6.dp))
                                Text("Submit", style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.ExtraBold))
                            }
                        } else {
                            OutlinedButton(
                                onClick = { /* View details */ },
                                shape = RoundedCornerShape(12.dp),
                                border = BorderStroke(1.dp, Color.LightGray.copy(alpha = 0.2f)),
                                contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp)
                            ) {
                                Icon(Icons.Filled.CheckCircle, contentDescription = null, tint = Color(0xFF10B981), modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(6.dp))
                                Text("Submitted", style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.ExtraBold), color = Color(0xFF10B981))
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun MySubmissionsTab(assignments: List<Assignment>) {
    val submissions = assignments.filter { it.status == "Submitted" }
        .sortedByDescending { it.deadline }

    if (submissions.isEmpty()) {
        EmptyState(
            icon = Icons.Filled.CloudUpload,
            title = "No submissions yet",
            message = "Your submitted assignments will appear here"
        )
    } else {
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            items(submissions) { assignment ->
                SubmissionCard(assignment)
            }
        }
    }
}

@Composable
private fun SubmissionCard(assignment: Assignment) {
    CamsCard(
        modifier = Modifier.fillMaxWidth(),
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(44.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(CamsBackground),
                contentAlignment = Alignment.Center
            ) {
                Icon(Icons.Filled.Description, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
            }
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    assignment.title,
                    style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Text(
                    "Due on ${formatDeadline(assignment.deadline)}",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            Icon(Icons.AutoMirrored.Filled.ArrowForward, contentDescription = null, tint = Color.LightGray.copy(alpha = 0.4f))
        }
    }
}

@Composable
private fun MarksFeedbackTab(assignments: List<Assignment>) {
    val evaluated = assignments.filter { it.mySubmission?.evaluation?.status == "Evaluated" }

    if (evaluated.isEmpty()) {
        EmptyState(
            icon = Icons.Filled.EmojiEvents,
            title = "No marks yet",
            message = "Results will appear here after faculty evaluation"
        )
    } else {
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            item {
                PerformanceSummaryCard(evaluated)
            }
            items(evaluated) { assignment ->
                EvaluatedAssignmentCard(assignment)
            }
        }
    }
}

@Composable
private fun PerformanceSummaryCard(evaluated: List<Assignment>) {
    val totalObtained = evaluated.sumOf { it.mySubmission?.evaluation?.marksObtained ?: 0.0 }
    val totalPossible = evaluated.sumOf { it.mySubmission?.evaluation?.totalMarks ?: 0 }
    val percentage = if (totalPossible > 0) (totalObtained / totalPossible * 100).roundToInt() else 0

    CamsCard(
        modifier = Modifier.fillMaxWidth(),
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(24.dp)
        ) {
            Box(contentAlignment = Alignment.Center) {
                CircularProgressIndicator(
                    progress = { percentage / 100f },
                    modifier = Modifier.size(64.dp),
                    color = CamsNavy,
                    strokeWidth = 6.dp,
                    trackColor = CamsBackground,
                    strokeCap = StrokeCap.Round
                )
                Text(
                    "$percentage%",
                    style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Black),
                    color = MaterialTheme.colorScheme.onSurface
                )
            }
            Column {
                Text(
                    "Overall Performance",
                    style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Text(
                    "${totalObtained.roundToInt()}/$totalPossible marks",
                    style = MaterialTheme.typography.headlineSmall.copy(fontWeight = FontWeight.Black),
                    color = MaterialTheme.colorScheme.onSurface
                )
                Text(
                    "${evaluated.size} assignments evaluated",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

@Composable
private fun EvaluatedAssignmentCard(assignment: Assignment) {
    val evaluation = assignment.mySubmission?.evaluation ?: return
    CamsCard(
        modifier = Modifier.fillMaxWidth(),
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(horizontalArrangement = Arrangement.spacedBy(12.dp), verticalAlignment = Alignment.Top) {
                Box(
                    modifier = Modifier
                        .size(40.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .background(Color(0xFFF0FDF4)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(Icons.Filled.CheckCircle, contentDescription = null, tint = Color(0xFF22C55E))
                }
                Column(modifier = Modifier.weight(1f)) {
                    Text(assignment.title, style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold), color = MaterialTheme.colorScheme.onSurface)
                    Text(assignment.subject, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(12.dp))
                        .background(Color(0xFFF0FDF4))
                        .border(1.dp, Color(0xFFDCFCE7), RoundedCornerShape(12.dp))
                        .padding(horizontal = 12.dp, vertical = 6.dp)
                ) {
                    Text(
                        evaluation.grade,
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Black),
                        color = Color(0xFF15803D)
                    )
                }
            }
            
            Spacer(modifier = Modifier.height(16.dp))
            
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                Column(modifier = Modifier.weight(1f)) {
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        Text("Score", style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold), color = MaterialTheme.colorScheme.onSurfaceVariant)
                        Text("${evaluation.marksObtained.roundToInt()}/${evaluation.totalMarks}", style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Black), color = MaterialTheme.colorScheme.onSurface)
                    }
                    Spacer(modifier = Modifier.height(6.dp))
                    LinearProgressIndicator(
                        progress = { (evaluation.marksObtained / evaluation.totalMarks).toFloat() },
                        modifier = Modifier.fillMaxWidth().height(8.dp).clip(CircleShape),
                        color = Color(0xFF10B981),
                        trackColor = CamsBackground
                    )
                }
            }

            if (evaluation.feedback.isNotEmpty()) {
                Spacer(modifier = Modifier.height(16.dp))
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(16.dp))
                        .background(CamsBackground)
                        .padding(12.dp)
                ) {
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        Icon(Icons.AutoMirrored.Filled.Message, contentDescription = null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(14.dp))
                        Text(
                            "\"${evaluation.feedback}\"",
                            style = MaterialTheme.typography.bodySmall.copy(fontStyle = androidx.compose.ui.text.font.FontStyle.Italic),
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun AssignmentCalendarTab(assignments: List<Assignment>) {
    val pending = assignments.filter { it.mySubmission == null }
        .sortedBy { it.deadline }

    if (pending.isEmpty()) {
        EmptyState(
            icon = Icons.Filled.EventAvailable,
            title = "No pending deadlines",
            message = "You're all caught up with your assignments"
        )
    } else {
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            item {
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    StatsMiniCard(Modifier.weight(1f), "Pending", "${pending.size}", Color(0xFFEF4444))
                    val dueSoon = pending.count { isDueWithinWeek(it.deadline) }
                    StatsMiniCard(Modifier.weight(1f), "Due Soon", "$dueSoon", Color(0xFFF59E0B))
                }
            }
            items(pending) { assignment ->
                TimelineItem(assignment)
            }
        }
    }
}

@Composable
private fun StatsMiniCard(modifier: Modifier, label: String, value: String, color: Color) {
    CamsCard(
        modifier = modifier,
    ) {
        Column(modifier = Modifier.padding(16.dp), horizontalAlignment = Alignment.CenterHorizontally) {
            Text(value, style = MaterialTheme.typography.headlineSmall.copy(fontWeight = FontWeight.Black), color = color)
            Text(label, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
    }
}

@Composable
private fun TimelineItem(assignment: Assignment) {
    val dlInfo = getDeadlineInfo(assignment.deadline)
    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(16.dp)) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Box(modifier = Modifier.size(12.dp).clip(CircleShape).background(dlInfo.color))
            Box(modifier = Modifier.width(2.dp).height(80.dp).background(Color.LightGray.copy(alpha = 0.2f)))
        }
        CamsCard(
            modifier = Modifier.weight(1f),
        ) {
            Row(modifier = Modifier.padding(16.dp), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(assignment.title, style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold), color = MaterialTheme.colorScheme.onSurface)
                    Text(assignment.subject, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    Text("Due: ${formatDeadline(assignment.deadline)}", style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold), color = dlInfo.color)
                }
                Box(
                    modifier = Modifier
                        .clip(CircleShape)
                        .background(dlInfo.color.copy(alpha = 0.1f))
                        .padding(horizontal = 8.dp, vertical = 4.dp)
                ) {
                    Text(dlInfo.label, style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Black, fontSize = 13.sp), color = dlInfo.color)
                }
            }
        }
    }
}

@Composable
private fun EmptyState(icon: ImageVector, title: String, message: String, onRetry: (() -> Unit)? = null) {
    Column(
        modifier = Modifier.fillMaxWidth().padding(vertical = 40.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Icon(icon, contentDescription = null, modifier = Modifier.size(48.dp), tint = Color(0xFFCBD5E1))
        Spacer(modifier = Modifier.height(16.dp))
        Text(title, style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold), color = MaterialTheme.colorScheme.onSurfaceVariant)
        Text(message, style = MaterialTheme.typography.bodySmall, color = Color(0xFF94A3B8), textAlign = TextAlign.Center)
        if (onRetry != null) {
            Spacer(modifier = Modifier.height(16.dp))
            Button(onClick = onRetry) {
                Text("Retry")
            }
        }
    }
}

@Composable
private fun AssignmentsSkeleton() {
    Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(16.dp)) {
        Box(Modifier.fillMaxWidth().height(56.dp).shimmerEffect().clip(RoundedCornerShape(16.dp)))
        repeat(3) {
            Box(Modifier.fillMaxWidth().height(160.dp).shimmerEffect().clip(RoundedCornerShape(24.dp)))
        }
    }
}

data class DeadlineInfo(val label: String, val isUrgent: Boolean, val color: Color)

private fun getDeadlineInfo(deadline: String): DeadlineInfo {
    return try {
        val sdf = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'", Locale.US)
        val dl = sdf.parse(deadline)
        val now = Date()
        val diff = dl.time - now.time
        val daysDiff = diff / (1000 * 60 * 60 * 24)
        
        when {
            daysDiff < 0 -> DeadlineInfo("Overdue", true, Color(0xFFEF4444))
            daysDiff == 0L -> DeadlineInfo("Due Today", true, Color(0xFFF59E0B))
            daysDiff == 1L -> DeadlineInfo("Due Tomorrow", true, Color(0xFFF59E0B))
            daysDiff <= 3 -> DeadlineInfo("$daysDiff days left", true, Color(0xFFF97316))
            daysDiff <= 7 -> DeadlineInfo("$daysDiff days left", false, Color(0xFF3B82F6))
            else -> DeadlineInfo("$daysDiff days left", false, Color(0xFF10B981))
        }
    } catch (e: Exception) {
        DeadlineInfo("Unknown", false, Color(0xFF64748B))
    }
}

private fun isDueWithinWeek(deadline: String): Boolean {
    return try {
        val sdf = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'", Locale.US)
        val dl = sdf.parse(deadline)
        val now = Date()
        val diff = dl.time - now.time
        val daysDiff = diff / (1000 * 60 * 60 * 24)
        daysDiff in 0..7
    } catch (e: Exception) {
        false
    }
}

private fun formatDeadline(deadline: String): String {
    return try {
        val sdf = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'", Locale.US)
        val dl = sdf.parse(deadline)
        val outSdf = SimpleDateFormat("dd MMM, hh:mm a", Locale.US)
        outSdf.format(dl)
    } catch (e: Exception) {
        deadline
    }
}

private fun formatDateTime(dateTime: String): String {
    return try {
        val sdf = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'", Locale.US)
        val dt = sdf.parse(dateTime)
        val outSdf = SimpleDateFormat("dd MMM yyyy 'at' hh:mm a", Locale.US)
        outSdf.format(dt)
    } catch (e: Exception) {
        dateTime
    }
}

data class AssignmentTypeConfig(val icon: ImageVector, val bgColor: Color)

private fun getAssignmentTypeConfig(type: String): AssignmentTypeConfig {
    return when (type) {
        "Case Law Analysis" -> AssignmentTypeConfig(Icons.Filled.Gavel, Color(0xFF7C3AED))
        "Legal Research Assignment" -> AssignmentTypeConfig(Icons.Filled.Search, Color(0xFF2563EB))
        "Drafting" -> AssignmentTypeConfig(Icons.Filled.Edit, Color(0xFF0D9488))
        "Theory Assignment" -> AssignmentTypeConfig(Icons.Filled.Book, Color(0xFF4B5563))
        else -> AssignmentTypeConfig(Icons.Filled.Description, Color(0xFF6366F1))
    }
}

private fun getSubmissionStatus(submission: Submission?): String? {
    if (submission == null) return null
    return submission.status
}
