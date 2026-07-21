package com.example.features.campus_life.screens

import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.*
import androidx.compose.material.icons.filled.ArrowForward
import androidx.compose.material.icons.filled.KeyboardArrowRight
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
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import coil.compose.AsyncImage
import com.example.core.navigation.AppRoutes
import com.example.core.theme.*
import com.example.core.ui.CamsCard
import com.example.core.ui.CamsScreen
import com.example.features.campus_life.models.*
import com.example.features.campus_life.providers.CouncilViewModel
import com.example.features.student.widgets.StudentDrawer
import kotlinx.coroutines.launch
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties

private val Rose600 = Color(0xFFE11D48)
private val Rose50 = Color(0xFFFFF1F2)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun StudentCouncilScreen(
    viewModel: CouncilViewModel = viewModel(),
    onNavigate: (String) -> Unit
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    var showContactDialog by remember { mutableStateOf(false) }
    var showProposalDialog by remember { mutableStateOf(false) }
    var showFeedbackDialog by remember { mutableStateOf(false) }
    var showAllReps by remember { mutableStateOf(false) }

    CamsScreen(scrollable = true,
        title = "Student Council",
        subtitle = "Student Governance Portal",
        onBackClick = { onNavigate(AppRoutes.STUDENT_DASHBOARD) },
        verticalArrangement = Arrangement.spacedBy(24.dp)
    ) {
        if (uiState.isLoading) {
            Box(modifier = Modifier.fillMaxWidth().height(200.dp), contentAlignment = Alignment.Center) {
                CircularProgressIndicator(color = CamsNavy)
            }
        } else {
            CamsCard {
                Text(
                    "Your voice on campus. Meet your elected representatives, track ongoing initiatives, submit feedback, and view the council's transparency reports.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    lineHeight = 18.sp
                )
            }

            // Header Actions
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                OutlinedButton(
                    onClick = { showContactDialog = true },
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(12.dp),
                    border = BorderStroke(1.dp, Color.LightGray.copy(alpha = 0.5f)),
                    colors = ButtonDefaults.outlinedButtonColors(contentColor = CamsTextPrimary)
                ) {
                    Text("Contact Council", style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.Bold, fontSize = 14.sp))
                }
                Button(
                    onClick = { showProposalDialog = true },
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = CamsNavy)
                ) {
                    Text("Submit Proposal", style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.Bold, fontSize = 14.sp))
                }
            }

                // Metrics
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    MetricCard(Modifier.weight(1f), "Proposals", "${uiState.proposalsCount}", Icons.Filled.Adjust, CamsNavy)
                    MetricCard(Modifier.weight(1f), "Resolved", "${uiState.resolvedCount}", Icons.Filled.CheckCircle, Color(0xFF10B981))
                    MetricCard(Modifier.weight(1f), "Fund", "${uiState.fundUtilizationPercent}%", Icons.Filled.AttachMoney, Color(0xFFD97706))
                }

                // Initiatives
                InitiativesCard(uiState.initiatives)

                // Feedback
                FeedbackBoard(
                    uiState.feedback,
                    onUpvote = { viewModel.upvoteFeedback(it) },
                    onAddFeedback = { showFeedbackDialog = true }
                )

                // Representatives
                RepresentativesCard(
                    if (showAllReps) uiState.representatives else uiState.representatives.take(3),
                    hasMore = uiState.representatives.size > 3,
                    showingAll = showAllReps,
                    onToggle = { showAllReps = !showAllReps }
                )

                // Next Meeting removed due to lack of ViewModel state

                Spacer(modifier = Modifier.height(20.dp))
        }
    }

    if (showContactDialog) {
        Dialog(onDismissRequest = { showContactDialog = false }) {
            Surface(shape = RoundedCornerShape(24.dp), color = Color.White) {
                Column(modifier = Modifier.padding(24.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    Text("Contact Student Council", style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Black))
                    Text("Email: studentcouncil@cams.edu", style = MaterialTheme.typography.bodyMedium)
                    Text("Office: Student Activity Center, Room 204", style = MaterialTheme.typography.bodyMedium)
                    Text("Office Hours: Mon-Fri, 2 PM - 4 PM", style = MaterialTheme.typography.bodyMedium)
                    Button(onClick = { showContactDialog = false }, modifier = Modifier.fillMaxWidth(), colors = ButtonDefaults.buttonColors(containerColor = CamsNavy)) {
                        Text("Close")
                    }
                }
            }
        }
    }

    if (showProposalDialog) {
        ProposalDialog(
            onDismiss = { showProposalDialog = false },
            onSubmit = { title, description ->
                viewModel.submitProposal(title, description)
                showProposalDialog = false
            }
        )
    }

    if (showFeedbackDialog) {
        FeedbackDialog(
            onDismiss = { showFeedbackDialog = false },
            onSubmit = { title ->
                viewModel.submitFeedback(title)
                showFeedbackDialog = false
            }
        )
    }

    if (uiState.errorMsg != null || uiState.successMsg != null) {
        AlertDialog(
            onDismissRequest = { viewModel.clearMessages() },
            title = { Text(if (uiState.errorMsg != null) "Error" else "Success") },
            text = { Text(uiState.errorMsg ?: uiState.successMsg ?: "") },
            confirmButton = {
                TextButton(onClick = { viewModel.clearMessages() }) { Text("OK") }
            }
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun ProposalDialog(onDismiss: () -> Unit, onSubmit: (String, String) -> Unit) {
    var title by remember { mutableStateOf("") }
    var description by remember { mutableStateOf("") }
    Dialog(onDismissRequest = onDismiss, properties = DialogProperties(usePlatformDefaultWidth = false)) {
        Surface(modifier = Modifier.fillMaxWidth().padding(16.dp), shape = RoundedCornerShape(28.dp), color = Color.White) {
            Column(modifier = Modifier.padding(24.dp), verticalArrangement = Arrangement.spacedBy(16.dp)) {
                Text("Submit Proposal", style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Black))
                OutlinedTextField(value = title, onValueChange = { title = it }, label = { Text("Proposal Title") }, modifier = Modifier.fillMaxWidth())
                OutlinedTextField(value = description, onValueChange = { description = it }, label = { Text("Description") }, modifier = Modifier.fillMaxWidth(), minLines = 3)
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    TextButton(onClick = onDismiss, modifier = Modifier.weight(1f)) { Text("Cancel") }
                    Button(
                        onClick = { onSubmit(title, description) },
                        modifier = Modifier.weight(1f),
                        colors = ButtonDefaults.buttonColors(containerColor = CamsNavy),
                        shape = RoundedCornerShape(12.dp)
                    ) { Text("Submit") }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun FeedbackDialog(onDismiss: () -> Unit, onSubmit: (String) -> Unit) {
    var title by remember { mutableStateOf("") }
    Dialog(onDismissRequest = onDismiss) {
        Surface(modifier = Modifier.fillMaxWidth().padding(16.dp), shape = RoundedCornerShape(24.dp), color = Color.White) {
            Column(modifier = Modifier.padding(24.dp), verticalArrangement = Arrangement.spacedBy(16.dp)) {
                Text("Submit Feedback", style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Black))
                OutlinedTextField(value = title, onValueChange = { title = it }, label = { Text("Your feedback") }, modifier = Modifier.fillMaxWidth(), minLines = 2)
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    TextButton(onClick = onDismiss, modifier = Modifier.weight(1f)) { Text("Cancel") }
                    Button(
                        onClick = { if (title.isNotBlank()) onSubmit(title) },
                        modifier = Modifier.weight(1f),
                        colors = ButtonDefaults.buttonColors(containerColor = CamsNavy),
                        shape = RoundedCornerShape(12.dp)
                    ) { Text("Post") }
                }
            }
        }
    }
}

@Composable
private fun MetricCard(modifier: Modifier, label: String, value: String, icon: ImageVector, color: Color) {
    CamsCard(modifier = modifier) {
        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Surface(color = color.copy(alpha = 0.1f), shape = RoundedCornerShape(8.dp)) {
                    Icon(icon, contentDescription = null, tint = color, modifier = Modifier.size(14.dp).padding(2.dp))
                }
                Text(label.uppercase(), style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Black, fontSize = 12.sp), color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
            Text(value, style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Black), color = MaterialTheme.colorScheme.onSurface)
        }
    }
}

@Composable
private fun InitiativesCard(initiatives: List<CouncilInitiative>) {
    CamsCard {
        Column {
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Icon(Icons.Filled.Flag, contentDescription = null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(20.dp))
                Text("Council Initiatives Tracker", style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Black))
            }
            Spacer(modifier = Modifier.height(20.dp))
            Column(verticalArrangement = Arrangement.spacedBy(20.dp)) {
                initiatives.forEach { init ->
                    Column {
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.Bottom) {
                            Column {
                                Text(init.title, style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Bold), color = MaterialTheme.colorScheme.onSurface)
                                Text("${init.category} • ${init.status}", style = MaterialTheme.typography.labelSmall.copy(fontSize = 13.sp), color = MaterialTheme.colorScheme.onSurfaceVariant)
                            }
                            Text("${(init.progress * 100).toInt()}%", style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold), color = CamsNavy)
                        }
                        Spacer(modifier = Modifier.height(8.dp))
                        LinearProgressIndicator(
                            progress = { init.progress },
                            modifier = Modifier.fillMaxWidth().height(8.dp).clip(RoundedCornerShape(4.dp)),
                            color = CamsNavy,
                            trackColor = Color.LightGray.copy(alpha = 0.1f)
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun FeedbackBoard(feedback: List<StudentFeedback>, onUpvote: (Int) -> Unit, onAddFeedback: () -> Unit) {
    CamsCard {
        Column {
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Icon(Icons.AutoMirrored.Filled.Message, contentDescription = null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(20.dp))
                    Text("Top Student Feedback", style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Black))
                }
                Text(
                    "+ Add",
                    style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                    color = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.clickable { onAddFeedback() }
                )
            }
            Spacer(modifier = Modifier.height(20.dp))
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                feedback.forEach { item ->
                    Row(
                        modifier = Modifier.fillMaxWidth().background(CamsBackground, RoundedCornerShape(16.dp)).border(1.dp, Color.LightGray.copy(alpha = 0.1f), RoundedCornerShape(16.dp)).padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Column(
                            modifier = Modifier
                                .background(MaterialTheme.colorScheme.surface, RoundedCornerShape(8.dp))
                                .border(1.dp, Color.LightGray.copy(alpha = 0.1f), RoundedCornerShape(8.dp))
                                .clickable { onUpvote(item.id) }
                                .padding(horizontal = 8.dp, vertical = 4.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Icon(Icons.Filled.TrendingUp, contentDescription = null, tint = Color(0xFF10B981), modifier = Modifier.size(12.dp))
                            Text("${item.votes}", style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold))
                        }
                        Column(modifier = Modifier.weight(1f)) {
                            Text(item.topic, style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Bold), color = MaterialTheme.colorScheme.onSurface)
                            Surface(
                                color = when(item.status) {
                                    "Implemented" -> Color(0xFFECFDF5)
                                    "Approved" -> Color(0xFFEFF6FF)
                                    else -> Color(0xFFFFFBEB)
                                },
                                shape = RoundedCornerShape(4.dp)
                            ) {
                                Text(
                                    item.status,
                                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                                    style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold, fontSize = 13.sp),
                                    color = when(item.status) {
                                        "Implemented" -> Color(0xFF047857)
                                        "Approved" -> Color(0xFF1D4ED8)
                                        else -> Color(0xFFB45309)
                                    }
                                )
                            }
                        }
                        Icon(Icons.Filled.KeyboardArrowRight, contentDescription = null, tint = MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.size(16.dp))
                    }
                }
            }
        }
    }
}

@Composable
private fun RepresentativesCard(reps: List<CouncilRepresentative>, hasMore: Boolean, showingAll: Boolean, onToggle: () -> Unit) {
    CamsCard {
        Column {
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Icon(Icons.Filled.People, contentDescription = null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(20.dp))
                Text("Elected Representatives", style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Black))
            }
            Spacer(modifier = Modifier.height(20.dp))
            Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                reps.forEach { rep ->
                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                        AsyncImage(
                            model = rep.imageUrl,
                            contentDescription = rep.name,
                            modifier = Modifier.size(40.dp).clip(CircleShape).border(1.dp, Color.LightGray.copy(alpha = 0.1f), CircleShape),
                            contentScale = ContentScale.Crop
                        )
                        Column {
                            Text(rep.name, style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Bold), color = MaterialTheme.colorScheme.onSurface)
                            Text(rep.role, style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold), color = CamsNavy)
                        }
                    }
                }
            }
            if (hasMore) {
                Spacer(modifier = Modifier.height(20.dp))
                Button(
                    onClick = onToggle,
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.background, contentColor = CamsTextSecondary),
                    border = BorderStroke(1.dp, Color.LightGray.copy(alpha = 0.1f))
                ) {
                    Text(if (showingAll) "Show Less" else "View Full Committee", style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold))
                }
            }
        }
    }
}

