package com.example.features.faculty.screens

import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.*
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import com.example.features.faculty.providers.FacultyMaterialsViewModel
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.core.network.FacultyMaterialDto
import com.example.core.theme.*
import com.example.core.ui.CamsCard
import com.example.core.ui.NetworkErrorView
import com.example.features.faculty.widgets.FacultyBaseScreen

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FacultyStudyMaterialsScreen(
    viewModel: FacultyMaterialsViewModel,
    onNavigate: (String) -> Unit
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    val context = LocalContext.current
    var searchQuery by remember { mutableStateOf("") }
    var selectedCategory by remember { mutableStateOf("All") }
    var showUploadDialog by remember { mutableStateOf(false) }
    var materialPendingDelete by remember { mutableStateOf<FacultyMaterialDto?>(null) }

    val categories = listOf("All", "Notes", "QP", "Reference", "Lab")

    materialPendingDelete?.let { material ->
        AlertDialog(
            onDismissRequest = { materialPendingDelete = null },
            title = { Text("Delete Material?") },
            text = { Text("This will permanently delete \"${material.title}\".") },
            confirmButton = {
                TextButton(onClick = {
                    viewModel.deleteMaterial(material.id)
                    materialPendingDelete = null
                }) { Text("Delete", color = Color(0xFFEF4444)) }
            },
            dismissButton = { TextButton(onClick = { materialPendingDelete = null }) { Text("Cancel") } }
        )
    }

    Box(modifier = Modifier.fillMaxSize()) {
        FacultyBaseScreen(
            scrollable = false,
            title = "Study Materials",
            subtitle = "Manage digital academic resources",
            currentRoute = "/faculty/study-materials",
            onNavigate = onNavigate
        ) {
            if (state.isLoading) {
                Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator(color = CamsNavy)
                }
            } else if (state.error != null && state.materials.isEmpty()) {
                NetworkErrorView(message = state.error ?: "Failed to load study materials", onRetry = { viewModel.loadMaterials() })
            } else {
                // Search Bar
                OutlinedTextField(
                    value = searchQuery,
                    onValueChange = { searchQuery = it },
                    modifier = Modifier.fillMaxWidth(),
                    placeholder = { Text("Search materials...") },
                    leadingIcon = { Icon(Icons.Filled.Search, null, tint = MaterialTheme.colorScheme.onSurfaceVariant) },
                    shape = RoundedCornerShape(12.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        unfocusedBorderColor = MaterialTheme.colorScheme.outlineVariant,
                        focusedBorderColor = CamsNavy,
                        unfocusedContainerColor = Color.White,
                        focusedContainerColor = Color.White
                    ),
                    singleLine = true
                )

                Spacer(modifier = Modifier.height(16.dp))

                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    categories.forEach { cat ->
                        FilterChip(
                            selected = selectedCategory == cat,
                            onClick = { selectedCategory = cat },
                            label = { Text(cat) },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = CamsNavy,
                                selectedLabelColor = Color.White,
                                containerColor = MaterialTheme.colorScheme.surface,
                                labelColor = CamsTextSecondary
                            )
                        )
                    }
                }

                Spacer(modifier = Modifier.height(20.dp))

                CamsCard {
                    Row(
                        modifier = Modifier.fillMaxWidth().padding(16.dp),
                        horizontalArrangement = Arrangement.SpaceAround
                    ) {
                        MaterialStat("Total Files", "${state.materials.size}", Icons.Filled.InsertDriveFile)
                        MaterialStat("Subjects", "${state.materials.map { it.subject }.distinct().size}", Icons.Filled.Subject)
                        MaterialStat("Pending", "${state.materials.count { it.status == "Pending Approval" }}", Icons.Filled.HourglassTop)
                    }
                }

                Spacer(modifier = Modifier.height(24.dp))

                Text("Recent Uploads", fontWeight = FontWeight.Bold, fontSize = 16.sp, color = MaterialTheme.colorScheme.onSurface)
                Spacer(modifier = Modifier.height(12.dp))

                val filtered = state.materials.filter {
                    (selectedCategory == "All" || it.category == selectedCategory) &&
                        it.title.contains(searchQuery, ignoreCase = true)
                }

                if (filtered.isEmpty()) {
                    Text("No materials match your filters.", modifier = Modifier.padding(20.dp), color = MaterialTheme.colorScheme.onSurfaceVariant)
                } else {
                    LazyColumn(
                        modifier = Modifier.fillMaxSize(),
                        contentPadding = PaddingValues(bottom = 80.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        items(filtered, key = { it.id }) { m ->
                            MaterialRow(m, onDelete = { materialPendingDelete = m })
                        }
                    }
                }
            }
        }

        FloatingActionButton(
            onClick = { showUploadDialog = true },
            modifier = Modifier.align(Alignment.BottomEnd).padding(24.dp),
            containerColor = CamsNavy,
            contentColor = Color.White
        ) {
            Icon(Icons.Filled.FileUpload, contentDescription = "Upload Material")
        }
    }

    if (showUploadDialog) {
        UploadMaterialDialog(
            isSaving = state.isLoading,
            onDismiss = { showUploadDialog = false },
            onSubmit = onSubmit@{ fileUri, fileFormat, title, description, subject, unit, topic, category, status, onError ->
                val filePart = com.example.core.network.MultipartUploadHelper.prepareFilePart("file", fileUri, context)
                if (filePart == null) {
                    onError("Could not read the selected file.")
                    return@onSubmit
                }
                viewModel.uploadMaterialWithFile(
                    filePart = filePart,
                    fileFormat = fileFormat,
                    title = title,
                    description = description,
                    subject = subject,
                    unit = unit,
                    topic = topic,
                    category = category,
                    status = status,
                    onSuccess = { showUploadDialog = false },
                    onError = onError
                )
            }
        )
    }
}

@Composable
fun UploadMaterialDialog(
    isSaving: Boolean,
    onDismiss: () -> Unit,
    onSubmit: (
        fileUri: android.net.Uri,
        fileFormat: String,
        title: String,
        description: String,
        subject: String,
        unit: String,
        topic: String,
        category: String,
        status: String,
        onError: (String) -> Unit
    ) -> Unit
) {
    val context = LocalContext.current
    var title by remember { mutableStateOf("") }
    var description by remember { mutableStateOf("") }
    var subject by remember { mutableStateOf("") }
    var unit by remember { mutableStateOf("") }
    var topic by remember { mutableStateOf("") }
    var category by remember { mutableStateOf("Notes") }
    var status by remember { mutableStateOf("Draft") }
    var selectedFileUri by remember { mutableStateOf<android.net.Uri?>(null) }
    var selectedFileName by remember { mutableStateOf<String?>(null) }
    var validationError by remember { mutableStateOf<String?>(null) }

    val launcher = androidx.activity.compose.rememberLauncherForActivityResult(
        androidx.activity.result.contract.ActivityResultContracts.GetContent()
    ) { uri ->
        if (uri != null) {
            selectedFileUri = uri
            selectedFileName = try {
                context.contentResolver.query(uri, null, null, null, null)?.use { cursor ->
                    val nameIndex = cursor.getColumnIndex(android.provider.OpenableColumns.DISPLAY_NAME)
                    if (cursor.moveToFirst() && nameIndex >= 0) cursor.getString(nameIndex) else uri.lastPathSegment
                } ?: uri.lastPathSegment
            } catch (e: Exception) {
                uri.lastPathSegment
            }
        }
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Upload Study Material", fontWeight = FontWeight.Bold) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.verticalScroll(rememberScrollState())) {
                OutlinedTextField(value = title, onValueChange = { title = it }, label = { Text("Title") }, modifier = Modifier.fillMaxWidth())
                OutlinedTextField(value = description, onValueChange = { description = it }, label = { Text("Description") }, modifier = Modifier.fillMaxWidth())
                OutlinedTextField(value = subject, onValueChange = { subject = it }, label = { Text("Subject") }, modifier = Modifier.fillMaxWidth())
                OutlinedTextField(value = unit, onValueChange = { unit = it }, label = { Text("Unit") }, modifier = Modifier.fillMaxWidth())
                OutlinedTextField(value = topic, onValueChange = { topic = it }, label = { Text("Topic") }, modifier = Modifier.fillMaxWidth())

                Text("Category:", fontWeight = FontWeight.SemiBold)
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    listOf("Notes", "QP", "Reference", "Lab").forEach { cat ->
                        FilterChip(selected = category == cat, onClick = { category = cat }, label = { Text(cat) })
                    }
                }

                Text("Status:", fontWeight = FontWeight.SemiBold)
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    listOf("Draft", "Pending Approval").forEach { stat ->
                        FilterChip(selected = status == stat, onClick = { status = stat }, label = { Text(stat) })
                    }
                }

                OutlinedButton(onClick = { launcher.launch("*/*") }, modifier = Modifier.fillMaxWidth()) {
                    Icon(Icons.Filled.AttachFile, contentDescription = null, modifier = Modifier.size(18.dp))
                    Spacer(Modifier.width(8.dp))
                    Text(selectedFileName ?: "Choose File")
                }

                val displayedError = validationError
                if (displayedError != null) {
                    Text(displayedError, color = Color(0xFFEF4444), fontSize = 12.sp, fontWeight = FontWeight.Medium)
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    validationError = when {
                        title.isBlank() -> "Title is required"
                        subject.isBlank() -> "Subject is required"
                        selectedFileUri == null -> "Please choose a file to upload"
                        else -> null
                    }
                    if (validationError == null) {
                        val fileFormat = selectedFileName?.substringAfterLast('.', "pdf") ?: "pdf"
                        onSubmit(selectedFileUri!!, fileFormat, title, description, subject, unit, topic, category, status) { error ->
                            validationError = error
                        }
                    }
                },
                enabled = !isSaving,
                colors = ButtonDefaults.buttonColors(containerColor = CamsNavy)
            ) {
                if (isSaving) CircularProgressIndicator(modifier = Modifier.size(18.dp), color = Color.White, strokeWidth = 2.dp)
                else Text("Upload")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel", color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
        }
    )
}

@Composable
private fun MaterialStat(label: String, value: String, icon: ImageVector) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Box(
            modifier = Modifier.size(40.dp).background(CamsNavy.copy(alpha = 0.1f), CircleShape),
            contentAlignment = Alignment.Center
        ) {
            Icon(icon, null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(20.dp))
        }
        Spacer(modifier = Modifier.height(8.dp))
        Text(value, fontWeight = FontWeight.Black, fontSize = 16.sp, color = MaterialTheme.colorScheme.onSurface)
        Text(label, fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
    }
}

@Composable
fun MaterialRow(data: FacultyMaterialDto, onDelete: () -> Unit = {}) {
    var showMenu by remember { mutableStateOf(false) }
    Surface(
        color = Color.White,
        shape = RoundedCornerShape(12.dp),
        shadowElevation = 1.dp
    ) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier.size(44.dp).background(MaterialTheme.colorScheme.surfaceVariant, RoundedCornerShape(10.dp)),
                contentAlignment = Alignment.Center
            ) {
                val icon = when ((data.category ?: "").lowercase()) {
                    "notes" -> Icons.Filled.Description
                    "qp" -> Icons.Filled.Quiz
                    "lab" -> Icons.AutoMirrored.Filled.Assignment
                    else -> Icons.AutoMirrored.Filled.MenuBook
                }
                Icon(icon, null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(22.dp))
            }

            Spacer(modifier = Modifier.width(16.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(data.title, fontWeight = FontWeight.Bold, fontSize = 14.sp, color = MaterialTheme.colorScheme.onSurface)
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(data.category ?: "Notes", fontSize = 13.sp, color = CamsNavy, fontWeight = FontWeight.Medium)
                    Text(" • ", fontSize = 13.sp, color = Color.LightGray)
                    Text(data.status, fontSize = 13.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    if (!data.uploadedDate.isNullOrBlank()) {
                        Text(" • ", fontSize = 13.sp, color = Color.LightGray)
                        Text(data.uploadedDate.take(10), fontSize = 13.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                }
            }

            Box {
                IconButton(onClick = { showMenu = true }) {
                    Icon(Icons.Filled.MoreVert, contentDescription = "More options", tint = MaterialTheme.colorScheme.onSurfaceVariant)
                }
                DropdownMenu(expanded = showMenu, onDismissRequest = { showMenu = false }) {
                    DropdownMenuItem(text = { Text("Delete") }, onClick = { showMenu = false; onDelete() })
                }
            }
        }
    }
}
