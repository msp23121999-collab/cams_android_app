package com.example.features.academics.screens

import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.*
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.Color
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
import com.example.features.academics.providers.SyllabusUiState
import com.example.features.academics.providers.SyllabusViewModel
import com.example.features.student.widgets.StudentDrawer
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SyllabusCoverageScreen(
    onNavigate: (String) -> Unit = {},
    viewModel: SyllabusViewModel = viewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    CamsScreen(
        scrollable = false,
        title = "Syllabus Coverage",
        onBackClick = { onNavigate(AppRoutes.STUDENT_DASHBOARD) },
        actions = {
            IconButton(onClick = { viewModel.loadData() }) {
                val rotation = rememberInfiniteTransition(label = "rotate").animateFloat(
                    initialValue = 0f,
                    targetValue = 360f,
                    animationSpec = infiniteRepeatable(
                        animation = tween(1000, easing = LinearEasing),
                        repeatMode = RepeatMode.Restart
                    ),
                    label = "rotate"
                )
                Icon(
                    Icons.Filled.Refresh,
                    contentDescription = "Refresh",
                    modifier = if (uiState.isLoading) Modifier.rotate(rotation.value) else Modifier,
                    tint = Color.White
                )
            }
        }
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
        ) {
            if (uiState.isLoading && uiState.syllabus.isEmpty()) {
                SyllabusSkeleton()
            } else if (uiState.syllabus.isEmpty() && !uiState.isLoading) {
                EmptyState(onRetry = { viewModel.loadData() })
            } else {
                SyllabusList(uiState, viewModel)
            }
        }
    }
}

@Composable
private fun SyllabusList(uiState: SyllabusUiState, viewModel: SyllabusViewModel) {
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            HeaderSection()
        }
        items(uiState.syllabus.keys.toList()) { subject ->
            SubjectCard(
                subject = subject,
                progress = uiState.syllabus[subject]!!,
                tracking = uiState.lessonPlanTracking.filter { it.subject == subject },
                isExpanded = uiState.expandedSubject == subject,
                onToggle = { viewModel.toggleSubject(subject) }
            )
        }
        item {
            Spacer(Modifier.height(20.dp))
        }
    }
}

@Composable
private fun HeaderSection() {
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
                    .size(48.dp)
                    .background(CamsNavy.copy(alpha = 0.1f), CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Icon(Icons.Filled.Book, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
            }
            Column {
                Text(
                    "Syllabus Tracking",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Black,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Text(
                    "Monitor covered vs pending topics and unit progress.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

@Composable
private fun SubjectCard(
    subject: String,
    progress: SyllabusProgress,
    tracking: List<LessonPlanItem>,
    isExpanded: Boolean,
    onToggle: () -> Unit
) {
    val completedCount = tracking.count { it.status == "On Schedule" }
    val pendingCount = tracking.count { it.status == "Pending" }

    CamsCard(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onToggle() },
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            // Header Row
            Column(modifier = Modifier.padding(4.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                    Surface(
                        color = CamsNavy.copy(alpha = 0.1f),
                        shape = RoundedCornerShape(100.dp)
                    ) {
                        Text(
                            "COURSE SUBJECT",
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 2.dp),
                            style = MaterialTheme.typography.labelSmall.copy(
                                fontWeight = FontWeight.Black,
                                color = CamsNavy,
                                fontSize = 13.sp
                            )
                        )
                    }
                    Text(
                        "${progress.daysRemaining ?: 90} days remaining",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        fontWeight = FontWeight.Bold
                    )
                }

                Text(
                    subject,
                    style = MaterialTheme.typography.titleLarge.copy(
                        fontWeight = FontWeight.Black,
                        color = MaterialTheme.colorScheme.onSurface,
                        fontSize = 18.sp
                    ),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )

                // Progress Bar
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    LinearProgressIndicator(
                        progress = (progress.overallCompletion / 100.0).toFloat(),
                        modifier = Modifier
                            .weight(1f)
                            .height(8.dp)
                            .clip(CircleShape),
                        color = CamsNavy,
                        trackColor = Color.LightGray.copy(alpha = 0.2f)
                    )
                    Text(
                        "${progress.overallCompletion}%",
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.Black,
                        color = CamsNavy
                    )
                }

                // Badges Row
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalAlignment = Alignment.CenterVertically) {
                    StatusBadge(label = "$completedCount Covered", color = Color(0xFF047857), bgColor = Color(0xFFECFDF5))
                    StatusBadge(label = "$pendingCount Pending", color = Color(0xFFB45309), bgColor = Color(0xFFFFFBEB))
                    Spacer(Modifier.weight(1f))
                    Icon(
                        if (isExpanded) Icons.Filled.ExpandLess else Icons.Filled.ExpandMore,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            // Expanded Content
            AnimatedVisibility(visible = isExpanded) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    HorizontalDivider(color = Color.LightGray.copy(alpha = 0.2f))
                    Text(
                        "SYLLABUS COVERAGE TIMELINE",
                        style = MaterialTheme.typography.labelSmall.copy(
                            fontWeight = FontWeight.Black,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            letterSpacing = 1.sp
                        )
                    )
                    
                    tracking.forEach { item ->
                        TrackingItem(item)
                    }
                }
            }
        }
    }
}

@Composable
private fun TrackingItem(item: LessonPlanItem) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        color = CamsBackground.copy(alpha = 0.5f),
        border = BorderStroke(1.dp, Color.LightGray.copy(alpha = 0.2f))
    ) {
        Row(modifier = Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            Column(modifier = Modifier.weight(1f)) {
                Text(item.unit, fontWeight = FontWeight.Black, fontSize = 12.sp, color = CamsNavy, maxLines = 1, overflow = TextOverflow.Ellipsis)
                Text(item.plannedTopic, fontWeight = FontWeight.Bold, fontSize = 13.sp, color = MaterialTheme.colorScheme.onSurface, maxLines = 2, overflow = TextOverflow.Ellipsis)
                if (item.actualTopic != null && item.actualTopic != item.plannedTopic) {
                    Text("Taught: ${item.actualTopic}", fontSize = 13.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
                Text(item.dateTaught ?: "Scheduled Topic", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant, fontWeight = FontWeight.Bold)
            }
            StatusBadge(
                label = item.status,
                color = if (item.status == "On Schedule") Color(0xFF047857) else Color(0xFFB45309),
                bgColor = if (item.status == "On Schedule") Color(0xFFECFDF5) else Color(0xFFFFFBEB)
            )
        }
    }
}

@Composable
private fun StatusBadge(label: String, color: Color, bgColor: Color) {
    Surface(
        color = bgColor,
        shape = RoundedCornerShape(100.dp),
        border = BorderStroke(1.dp, color.copy(alpha = 0.2f))
    ) {
        Text(
            text = label.uppercase(),
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp),
            style = MaterialTheme.typography.labelSmall.copy(
                color = color,
                fontWeight = FontWeight.Black,
                fontSize = 13.sp
            )
        )
    }
}

@Composable
private fun SyllabusSkeleton() {
    Column(modifier = Modifier.fillMaxSize().padding(16.dp), verticalArrangement = Arrangement.spacedBy(16.dp)) {
        Box(Modifier.fillMaxWidth().height(100.dp).shimmerEffect().clip(RoundedCornerShape(24.dp)))
        repeat(3) {
            Box(Modifier.fillMaxWidth().height(160.dp).shimmerEffect().clip(RoundedCornerShape(24.dp)))
        }
    }
}

@Composable
private fun EmptyState(onRetry: (() -> Unit)? = null) {
    Column(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Icon(Icons.AutoMirrored.Filled.MenuBook, contentDescription = null, modifier = Modifier.size(64.dp), tint = MaterialTheme.colorScheme.outlineVariant)
        Spacer(Modifier.height(16.dp))
        Text("No Syllabus Data Loaded", fontWeight = FontWeight.Black, color = LexNovaSlateDark)
        Text(
            "Syllabus tracking schedules have not been published yet.",
            textAlign = TextAlign.Center,
            modifier = Modifier.padding(horizontal = 32.dp),
            color = LexNovaSlateAccent,
            fontSize = 13.sp
        )
        if (onRetry != null) {
            Spacer(modifier = Modifier.height(16.dp))
            Button(onClick = onRetry) {
                Text("Retry")
            }
        }
    }
}
