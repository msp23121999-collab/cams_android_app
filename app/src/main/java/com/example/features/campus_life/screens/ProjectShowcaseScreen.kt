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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.core.theme.*
import com.example.core.navigation.AppRoutes
import com.example.core.ui.CamsCard
import com.example.core.ui.CamsScreen
import com.example.features.campus_life.models.ResearchPaper
import com.example.features.campus_life.providers.ProjectShowcaseViewModel
import com.example.features.student.widgets.StudentDrawer
import kotlinx.coroutines.launch
import android.content.Intent
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.ui.platform.LocalContext
import com.example.core.network.MultipartUploadHelper

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProjectShowcaseScreen(
    onNavigate: (String) -> Unit,
    viewModel: ProjectShowcaseViewModel = viewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    var activeTab by remember { mutableStateOf("all") }
    var showSubmitDialog by remember { mutableStateOf(false) }

    CamsScreen(scrollable = true,
        title = "Academic Showcase",
        subtitle = "Research papers and dissertations authored by students",
        onBackClick = { onNavigate(AppRoutes.STUDENT_DASHBOARD) },
        actions = {
            IconButton(onClick = { showSubmitDialog = true }) {
                Icon(Icons.Filled.Add, contentDescription = "Submit", tint = Color.White)
            }
        },
        verticalArrangement = Arrangement.spacedBy(20.dp)
    ) {
        // Search & Submit
            Surface(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                color = Color.White,
                border = BorderStroke(1.dp, Color.LightGray.copy(alpha = 0.3f))
            ) {
                Row(modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp), verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Filled.Search, contentDescription = null, tint = MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.size(20.dp))
                    Spacer(modifier = Modifier.width(12.dp))
                    androidx.compose.foundation.text.BasicTextField(
                        value = uiState.searchQuery,
                        onValueChange = { viewModel.updateSearch(it) },
                        modifier = Modifier.weight(1f),
                        textStyle = MaterialTheme.typography.bodyMedium.copy(color = MaterialTheme.colorScheme.onSurface),
                        decorationBox = { innerTextField ->
                            if (uiState.searchQuery.isEmpty()) {
                                Text("Search titles...", style = MaterialTheme.typography.bodyMedium.copy(color = MaterialTheme.colorScheme.onSurfaceVariant))
                            }
                            innerTextField()
                        }
                    )
                }
            }

            // Tabs
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                TabItem(
                    text = "All Papers",
                    isSelected = activeTab == "all",
                    onClick = { activeTab = "all" },
                    modifier = Modifier.weight(1f)
                )
                TabItem(
                    text = "Featured",
                    isSelected = activeTab == "featured",
                    onClick = { activeTab = "featured" },
                    modifier = Modifier.weight(1f)
                )
            }

            // Papers List
            val filteredPapers = uiState.papers
                .filter { activeTab == "all" || it.featured }
                .filter { uiState.searchQuery.isBlank() || it.title.contains(uiState.searchQuery, ignoreCase = true) }

            filteredPapers.forEach { paper ->
                ResearchPaperCard(paper)
            }

            Spacer(modifier = Modifier.height(20.dp))
    }

    if (showSubmitDialog) {
        SubmitPaperDialog(
            onDismiss = { showSubmitDialog = false },
            onSubmit = { title, abstract, category, guide, team, filePart ->
                viewModel.submitPaper(title, abstract, category, guide, team, filePart)
                showSubmitDialog = false
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
fun SubmitPaperDialog(
    onDismiss: () -> Unit,
    onSubmit: (title: String, abstract: String, category: String, guide: String, team: List<String>, filePart: okhttp3.MultipartBody.Part?) -> Unit
) {
    val context = LocalContext.current
    var title by remember { mutableStateOf("") }
    var abstract by remember { mutableStateOf("") }
    var category by remember { mutableStateOf("Corporate") }
    var guide by remember { mutableStateOf("") }
    var teamText by remember { mutableStateOf("") }
    var selectedFileUri by remember { mutableStateOf<Uri?>(null) }
    var selectedFileName by remember { mutableStateOf<String?>(null) }

    val categories = listOf("Corporate", "Criminal", "Cyber Law", "Constitutional", "Family Law", "Others")

    val filePickerLauncher = rememberLauncherForActivityResult(ActivityResultContracts.GetContent()) { uri: Uri? ->
        selectedFileUri = uri
        selectedFileName = uri?.lastPathSegment ?: "paper.pdf"
    }

    Dialog(onDismissRequest = onDismiss, properties = DialogProperties(usePlatformDefaultWidth = false)) {
        Surface(
            modifier = Modifier.fillMaxWidth().padding(16.dp),
            shape = RoundedCornerShape(32.dp),
            color = Color.White
        ) {
            Column(modifier = Modifier.padding(24.dp).verticalScroll(rememberScrollState()).imePadding(), verticalArrangement = Arrangement.spacedBy(16.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    Icon(Icons.Filled.Description, contentDescription = null, tint = CamsNavy)
                    Text("Submit Research Paper", style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Black))
                }

                OutlinedTextField(value = title, onValueChange = { title = it }, label = { Text("Paper Title") }, modifier = Modifier.fillMaxWidth())
                OutlinedTextField(value = abstract, onValueChange = { abstract = it }, label = { Text("Abstract") }, modifier = Modifier.fillMaxWidth(), minLines = 3)

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

                OutlinedTextField(value = guide, onValueChange = { guide = it }, label = { Text("Faculty Guide") }, modifier = Modifier.fillMaxWidth())
                OutlinedTextField(value = teamText, onValueChange = { teamText = it }, label = { Text("Co-authors (comma-separated)") }, modifier = Modifier.fillMaxWidth())

                Surface(
                    onClick = { filePickerLauncher.launch("application/pdf") },
                    modifier = Modifier.fillMaxWidth().height(80.dp),
                    shape = RoundedCornerShape(16.dp),
                    color = CamsBackground,
                    border = BorderStroke(1.dp, Color.LightGray.copy(alpha = 0.5f))
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            Icon(if (selectedFileUri != null) Icons.Filled.InsertDriveFile else Icons.Filled.CloudUpload, contentDescription = null, tint = MaterialTheme.colorScheme.onSurfaceVariant)
                            Text(selectedFileName ?: "Upload PDF", style = MaterialTheme.typography.labelSmall.copy(color = MaterialTheme.colorScheme.onSurfaceVariant))
                        }
                    }
                }

                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    TextButton(onClick = onDismiss, modifier = Modifier.weight(1f)) { Text("Cancel", color = MaterialTheme.colorScheme.onSurfaceVariant) }
                    Button(
                        onClick = {
                            val filePart = selectedFileUri?.let { MultipartUploadHelper.prepareFilePart("file", it, context) }
                            val team = teamText.split(",").map { it.trim() }.filter { it.isNotBlank() }
                            onSubmit(title, abstract, category, guide, team, filePart)
                        },
                        modifier = Modifier.weight(1f),
                        colors = ButtonDefaults.buttonColors(containerColor = CamsNavy),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Text("Submit")
                    }
                }
            }
        }
    }
}

@Composable
fun TabItem(text: String, isSelected: Boolean, onClick: () -> Unit, modifier: Modifier = Modifier) {
    Column(
        modifier = modifier.clickable(onClick = onClick),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text.uppercase(),
            style = MaterialTheme.typography.labelSmall.copy(
                fontWeight = FontWeight.Black,
                color = if (isSelected) CamsNavy else CamsTextSecondary,
                letterSpacing = 1.sp
            ),
            modifier = Modifier.padding(vertical = 12.dp)
        )
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(2.dp)
                .background(if (isSelected) CamsNavy else Color.Transparent)
        )
    }
}

@Composable
fun ResearchPaperCard(paper: ResearchPaper) {
    val context = LocalContext.current
    CamsCard(modifier = Modifier.fillMaxWidth()) {
        Column {
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Surface(color = CamsNavy.copy(alpha = 0.1f), shape = CircleShape) {
                        Text(paper.category.uppercase(), modifier = Modifier.padding(horizontal = 10.dp, vertical = 2.dp), style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Black, color = CamsNavy, fontSize = 12.sp))
                    }
                    if (paper.featured) {
                        Surface(color = Color(0xFFFFF7ED), shape = CircleShape) {
                            Row(modifier = Modifier.padding(horizontal = 10.dp, vertical = 2.dp), verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.Filled.Star, contentDescription = null, tint = Color(0xFFF59E0B), modifier = Modifier.size(8.dp))
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("FEATURED", style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Black, color = Color(0xFFB45309), fontSize = 12.sp))
                            }
                        }
                    }
                }
                Surface(color = CamsBackground, shape = CircleShape) {
                    Text(paper.status.uppercase(), modifier = Modifier.padding(horizontal = 10.dp, vertical = 2.dp), style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Black, color = MaterialTheme.colorScheme.onSurfaceVariant, fontSize = 12.sp))
                }
            }

            Spacer(modifier = Modifier.height(16.dp))
            Text(paper.title, style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Black, color = MaterialTheme.colorScheme.onSurface, lineHeight = 24.sp))
            Spacer(modifier = Modifier.height(8.dp))
            Text(paper.abstract, style = MaterialTheme.typography.bodySmall.copy(color = MaterialTheme.colorScheme.onSurfaceVariant))

            if (paper.awards.isNotEmpty()) {
                Spacer(modifier = Modifier.height(12.dp))
                paper.awards.forEach { award ->
                    Surface(color = Color(0xFFFFFBEB), shape = RoundedCornerShape(8.dp), border = BorderStroke(1.dp, Color(0xFFFEF3C7))) {
                        Row(modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp), verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Filled.Stars, contentDescription = null, tint = Color(0xFFF59E0B), modifier = Modifier.size(14.dp))
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(award, style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold, color = Color(0xFFB45309)))
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(20.dp))
            HorizontalDivider(color = Color.LightGray.copy(alpha = 0.2f))
            Spacer(modifier = Modifier.height(16.dp))

            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                Column(modifier = Modifier.weight(1f)) {
                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        Icon(Icons.Filled.Group, contentDescription = null, tint = MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.size(12.dp))
                        Text(paper.team.joinToString(", "), style = MaterialTheme.typography.labelSmall.copy(color = MaterialTheme.colorScheme.onSurfaceVariant))
                    }
                    Spacer(modifier = Modifier.height(4.dp))
                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        Icon(Icons.Filled.School, contentDescription = null, tint = MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.size(12.dp))
                        Text("Guide: ${paper.guide}", style = MaterialTheme.typography.labelSmall.copy(color = MaterialTheme.colorScheme.onSurfaceVariant))
                    }
                }
                
                Surface(
                    onClick = {
                        if (paper.fileUrl != null) {
                            val origin = com.example.core.config.AppConfig.BASE_URL.substringBefore("/api/v1")
                            val fullUrl = if (paper.fileUrl.startsWith("http")) paper.fileUrl else origin + paper.fileUrl
                            context.startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(fullUrl)))
                        }
                    },
                    modifier = Modifier.size(40.dp),
                    shape = RoundedCornerShape(10.dp),
                    color = CamsNavy,
                    shadowElevation = 2.dp
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(Icons.Filled.Download, contentDescription = null, tint = Color.White, modifier = Modifier.size(18.dp))
                    }
                }
            }
        }
    }
}
