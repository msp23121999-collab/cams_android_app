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
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.material3.MaterialTheme
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.core.theme.*
import com.example.core.navigation.AppRoutes
import com.example.core.theme.*
import com.example.core.ui.CamsCard
import com.example.core.ui.CamsScreen
import com.example.features.campus_life.models.InnovationProject
import com.example.features.campus_life.providers.InnovationWallViewModel
import com.example.features.student.widgets.StudentDrawer
import kotlinx.coroutines.launch
import android.content.Intent
import androidx.compose.ui.platform.LocalContext

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun InnovationWallScreen(
    onNavigate: (String) -> Unit,
    viewModel: InnovationWallViewModel = viewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    var activeFilter by remember { mutableStateOf("All Projects") }
    var showSubmitDialog by remember { mutableStateOf(false) }
    var commentTargetId by remember { mutableStateOf<String?>(null) }

    val categories = listOf("All Projects", "Research", "Legal Technology", "Community Projects", "Startups")

    CamsScreen(scrollable = true,
        title = "Innovation Wall",
        subtitle = "Showcasing student-led breakthroughs",
        onBackClick = { onNavigate(AppRoutes.STUDENT_DASHBOARD) },
        verticalArrangement = Arrangement.spacedBy(24.dp)
    ) {
        // Header
            CamsCard {
                Column {
                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                        Icon(Icons.Filled.Lightbulb, contentDescription = null, tint = Color(0xFFF59E0B), modifier = Modifier.size(32.dp))
                        Text("Innovation Hub", style = MaterialTheme.typography.headlineSmall.copy(fontWeight = FontWeight.Black), color = MaterialTheme.colorScheme.onSurface)
                    }
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        "Showcasing groundbreaking legal tech projects and startups built by students.",
                        style = MaterialTheme.typography.bodySmall.copy(color = MaterialTheme.colorScheme.onSurfaceVariant)
                    )
                }
            }

            // Search & Filter
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedTextField(
                    value = uiState.searchQuery,
                    onValueChange = { viewModel.updateSearch(it) },
                    placeholder = { Text("Search projects...", fontSize = 14.sp) },
                    leadingIcon = { Icon(Icons.Filled.Search, contentDescription = null, tint = MaterialTheme.colorScheme.onSurfaceVariant) },
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(12.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedContainerColor = Color.White,
                        unfocusedContainerColor = Color.White,
                        unfocusedBorderColor = Color.LightGray.copy(alpha = 0.5f),
                        focusedBorderColor = CamsNavy
                    )
                )
                Button(
                    onClick = { showSubmitDialog = true },
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFF59E0B)),
                    shape = RoundedCornerShape(12.dp),
                    contentPadding = PaddingValues(horizontal = 16.dp)
                ) {
                    Icon(Icons.Filled.Add, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Submit", style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Black))
                }
            }

            Row(modifier = Modifier.horizontalScroll(rememberScrollState()), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                categories.forEach { cat ->
                    FilterChip(
                        selected = activeFilter == cat,
                        onClick = { activeFilter = cat },
                        label = { Text(cat.uppercase(), style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Black, fontSize = 13.sp)) },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = CamsNavy,
                            selectedLabelColor = Color.White
                        ),
                        shape = RoundedCornerShape(12.dp)
                    )
                }
            }

            val filteredProjects = uiState.projects
                .filter { activeFilter == "All Projects" || it.category == activeFilter }
                .filter { uiState.searchQuery.isBlank() || it.title.contains(uiState.searchQuery, ignoreCase = true) || it.description.contains(uiState.searchQuery, ignoreCase = true) }

            if (filteredProjects.isEmpty()) {
                Column(
                    modifier = Modifier.fillMaxWidth().padding(40.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Icon(Icons.Filled.Lightbulb, contentDescription = null, modifier = Modifier.size(48.dp), tint = Color.LightGray)
                    Spacer(modifier = Modifier.height(16.dp))
                    Text("No projects found in this category.", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            } else {
                filteredProjects.forEach { project ->
                    InnovationProjectCard(
                        project,
                        onLike = { viewModel.toggleLike(project.id) },
                        onComment = { commentTargetId = project.id }
                    )
                }
            }

            Spacer(modifier = Modifier.height(20.dp))
    }

    if (showSubmitDialog) {
        SubmitProjectDialog(
            onDismiss = { showSubmitDialog = false },
            onSubmit = { title, description, category, mentor, team ->
                viewModel.submitProject(title, description, category, mentor, team)
                showSubmitDialog = false
            }
        )
    }

    if (commentTargetId != null) {
        AddCommentDialog(
            onDismiss = { commentTargetId = null },
            onSubmit = { text ->
                viewModel.addComment(commentTargetId!!, text)
                commentTargetId = null
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
fun SubmitProjectDialog(
    onDismiss: () -> Unit,
    onSubmit: (title: String, description: String, category: String, mentor: String, team: List<String>) -> Unit
) {
    var title by remember { mutableStateOf("") }
    var description by remember { mutableStateOf("") }
    var category by remember { mutableStateOf("Legal Technology") }
    var mentor by remember { mutableStateOf("") }
    var teamText by remember { mutableStateOf("") }

    val categories = listOf("Research", "Legal Technology", "Community Projects", "Startups")

    Dialog(onDismissRequest = onDismiss, properties = DialogProperties(usePlatformDefaultWidth = false)) {
        Surface(
            modifier = Modifier.fillMaxWidth().padding(16.dp),
            shape = RoundedCornerShape(32.dp),
            color = Color.White
        ) {
            Column(modifier = Modifier.padding(24.dp).verticalScroll(rememberScrollState()).imePadding(), verticalArrangement = Arrangement.spacedBy(16.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    Icon(Icons.Filled.Lightbulb, contentDescription = null, tint = Color(0xFFF59E0B))
                    Text("Submit Project", style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Black))
                }

                OutlinedTextField(value = title, onValueChange = { title = it }, label = { Text("Project Title") }, modifier = Modifier.fillMaxWidth())
                OutlinedTextField(value = description, onValueChange = { description = it }, label = { Text("Description") }, modifier = Modifier.fillMaxWidth(), minLines = 3)

                Column {
                    Text("Category", style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold))
                    Row(modifier = Modifier.horizontalScroll(rememberScrollState()), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        categories.forEach { c ->
                            FilterChip(
                                selected = category == c,
                                onClick = { category = c },
                                label = { Text(c) }
                            )
                        }
                    }
                }

                OutlinedTextField(value = mentor, onValueChange = { mentor = it }, label = { Text("Faculty Mentor") }, modifier = Modifier.fillMaxWidth())
                OutlinedTextField(value = teamText, onValueChange = { teamText = it }, label = { Text("Team Members (comma-separated)") }, modifier = Modifier.fillMaxWidth())

                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    TextButton(onClick = onDismiss, modifier = Modifier.weight(1f)) { Text("Cancel", color = MaterialTheme.colorScheme.onSurfaceVariant) }
                    Button(
                        onClick = {
                            val team = teamText.split(",").map { it.trim() }.filter { it.isNotBlank() }
                            onSubmit(title, description, category, mentor, team)
                        },
                        modifier = Modifier.weight(1f),
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFF59E0B)),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Text("Submit")
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddCommentDialog(onDismiss: () -> Unit, onSubmit: (String) -> Unit) {
    var text by remember { mutableStateOf("") }
    Dialog(onDismissRequest = onDismiss) {
        Surface(modifier = Modifier.fillMaxWidth().padding(16.dp), shape = RoundedCornerShape(24.dp), color = Color.White) {
            Column(modifier = Modifier.padding(24.dp), verticalArrangement = Arrangement.spacedBy(16.dp)) {
                Text("Add Comment", style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Black))
                OutlinedTextField(value = text, onValueChange = { text = it }, label = { Text("Your comment") }, modifier = Modifier.fillMaxWidth(), minLines = 2)
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    TextButton(onClick = onDismiss, modifier = Modifier.weight(1f)) { Text("Cancel") }
                    Button(
                        onClick = { if (text.isNotBlank()) onSubmit(text) },
                        modifier = Modifier.weight(1f),
                        colors = ButtonDefaults.buttonColors(containerColor = CamsNavy),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Text("Post")
                    }
                }
            }
        }
    }
}

@Composable
fun InnovationProjectCard(project: InnovationProject, onLike: () -> Unit, onComment: () -> Unit) {
    val context = LocalContext.current
    CamsCard(modifier = Modifier.fillMaxWidth()) {
        Column {
            Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                Surface(
                    modifier = Modifier.size(48.dp),
                    shape = RoundedCornerShape(12.dp),
                    color = CamsBackground
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(Icons.Filled.Code, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                    }
                }
                Column(modifier = Modifier.weight(1f)) {
                    Text(project.title, style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Black), color = MaterialTheme.colorScheme.onSurface)
                    Row(modifier = Modifier.padding(top = 4.dp), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        Surface(color = CamsBackground, shape = CircleShape) {
                            Text(project.category.uppercase(), modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp), style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Black, fontSize = 7.sp, color = MaterialTheme.colorScheme.onSurfaceVariant))
                        }
                        project.badges.forEach { badge ->
                            Surface(color = Color(0xFFFFF7ED), shape = CircleShape) {
                                Row(modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp), verticalAlignment = Alignment.CenterVertically) {
                                    Icon(Icons.Filled.Stars, contentDescription = null, modifier = Modifier.size(8.dp), tint = Color(0xFFF59E0B))
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text(badge.uppercase(), style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Black, fontSize = 7.sp, color = Color(0xFFB45309)))
                                }
                            }
                        }
                    }
                }
                Icon(Icons.Filled.OpenInNew, contentDescription = null, tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f), modifier = Modifier.size(18.dp))
            }
            
            Spacer(modifier = Modifier.height(16.dp))
            Text(project.description, style = MaterialTheme.typography.bodySmall.copy(color = MaterialTheme.colorScheme.onSurfaceVariant))
            
            Spacer(modifier = Modifier.height(20.dp))
            Surface(color = CamsBackground, shape = RoundedCornerShape(16.dp)) {
                Row(modifier = Modifier.padding(12.dp).fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                    Column {
                        Text("MENTOR", style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Black, color = MaterialTheme.colorScheme.onSurfaceVariant, fontSize = 12.sp))
                        Text(project.mentor, style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurface))
                    }
                    Column(horizontalAlignment = Alignment.End) {
                        Text("TEAM", style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Black, color = MaterialTheme.colorScheme.onSurfaceVariant, fontSize = 12.sp))
                        Row(horizontalArrangement = Arrangement.spacedBy((-8).dp)) {
                            project.team.forEach { _ ->
                                Surface(modifier = Modifier.size(24.dp), shape = CircleShape, color = Color.LightGray, border = BorderStroke(1.dp, Color.White)) {}
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))
            HorizontalDivider(color = Color.LightGray.copy(alpha = 0.2f))
            Spacer(modifier = Modifier.height(12.dp))

            Row(horizontalArrangement = Arrangement.SpaceBetween, modifier = Modifier.fillMaxWidth()) {
                Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp),
                        modifier = Modifier.clickable { onLike() }
                    ) {
                        Icon(Icons.Filled.ThumbUp, contentDescription = null, modifier = Modifier.size(14.dp), tint = if (project.likedByMe) CamsNavy else MaterialTheme.colorScheme.onSurfaceVariant)
                        Text(project.likes.toString(), style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Black, color = if (project.likedByMe) CamsNavy else MaterialTheme.colorScheme.onSurface))
                    }
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp),
                        modifier = Modifier.clickable { onComment() }
                    ) {
                        Icon(Icons.Filled.ChatBubble, contentDescription = null, modifier = Modifier.size(14.dp), tint = MaterialTheme.colorScheme.onSurfaceVariant)
                        Text(project.comments.toString(), style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Black, color = MaterialTheme.colorScheme.onSurface))
                    }
                }
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(4.dp),
                    modifier = Modifier.clickable {
                        val shareIntent = Intent(Intent.ACTION_SEND).apply {
                            type = "text/plain"
                            putExtra(Intent.EXTRA_TEXT, "Check out this project on CAMS Innovation Wall: ${project.title}\n\n${project.description}")
                        }
                        context.startActivity(Intent.createChooser(shareIntent, "Share Project"))
                    }
                ) {
                    Icon(Icons.Filled.Share, contentDescription = null, modifier = Modifier.size(14.dp), tint = MaterialTheme.colorScheme.onSurfaceVariant)
                    Text("SHARE", style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Black, color = MaterialTheme.colorScheme.onSurfaceVariant))
                }
            }
        }
    }
}
