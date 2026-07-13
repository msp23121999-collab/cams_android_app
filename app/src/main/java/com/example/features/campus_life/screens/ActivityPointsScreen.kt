package com.example.features.campus_life.screens

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
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
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
import com.example.features.campus_life.models.ActivityPointApplication
import com.example.features.campus_life.providers.ActivityPointsViewModel
import com.example.features.student.widgets.StudentDrawer
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ActivityPointsScreen(
    onNavigate: (String) -> Unit,
    viewModel: ActivityPointsViewModel = viewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    var showSubmitModal by remember { mutableStateOf(false) }
    var selectedApp by remember { mutableStateOf<ActivityPointApplication?>(null) }

    CamsScreen(scrollable = true,
        title = "Activity Points",
        subtitle = "Additional Credits Tracker",
        onBackClick = { onNavigate(AppRoutes.STUDENT_DASHBOARD) },
        verticalArrangement = Arrangement.spacedBy(24.dp)
    ) {
        // Stats Dashboard
        val approvedPoints = uiState.applications.filter { it.status == "Approved" }.sumOf { it.approvedPoints ?: 0 }
        val pendingCount = uiState.applications.count { it.status == "Pending Review" }
        
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            ActivityStatCard(Modifier.weight(1f), "Earned", approvedPoints.toString(), Icons.Filled.Stars, Color(0xFF10B981))
            ActivityStatCard(Modifier.weight(1f), "Pending", pendingCount.toString(), Icons.Filled.History, Color(0xFFF59E0B))
        }
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            ActivityStatCard(Modifier.weight(1f), "Verified", uiState.applications.count { it.status == "Approved" }.toString(), Icons.Filled.FactCheck, CamsNavy)
            ActivityStatCard(Modifier.weight(1f), "Total", uiState.applications.size.toString(), Icons.Filled.Description, Color(0xFF6366F1))
        }

        // Applications Log
        Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                Text("SUBMISSIONS LOG", style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Black, color = CamsTextSecondary, letterSpacing = 1.sp))
                Button(
                    onClick = { showSubmitModal = true },
                    colors = ButtonDefaults.buttonColors(containerColor = CamsNavy),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Icon(Icons.Filled.Add, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Submit New", style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Black))
                }
            }
            
            if (uiState.applications.isEmpty()) {
                EmptyApplicationsView()
            } else {
                uiState.applications.forEach { app ->
                    ActivityApplicationItem(
                        app = app,
                        onClick = { selectedApp = app },
                        onDelete = { viewModel.deleteApplication(it) }
                    )
                }
            }
        }
        
        Spacer(modifier = Modifier.height(20.dp))
    }

    if (showSubmitModal) {
        SubmitApplicationDialog(
            onDismiss = { showSubmitModal = false },
            onAdd = {
                viewModel.submitApplication(it)
                showSubmitModal = false
            }
        )
    }

    if (selectedApp != null) {
        ActivityAppDetailDialog(
            app = selectedApp!!,
            onDismiss = { selectedApp = null }
        )
    }
}

@Composable
fun ActivityStatCard(modifier: Modifier = Modifier, label: String, value: String, icon: ImageVector, color: Color) {
    CamsCard(modifier = modifier) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Column {
                Text(label.uppercase(), style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Black, color = CamsTextSecondary, letterSpacing = 0.5.sp))
                Text(value, style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Black, color = CamsTextPrimary))
            }
            Surface(color = color.copy(alpha = 0.1f), shape = RoundedCornerShape(12.dp)) {
                Icon(icon, contentDescription = null, tint = color, modifier = Modifier.padding(8.dp).size(20.dp))
            }
        }
    }
}

@Composable
fun ActivityApplicationItem(
    app: ActivityPointApplication,
    onClick: () -> Unit,
    onDelete: (String) -> Unit
) {
    CamsCard(
        onClick = onClick,
        modifier = Modifier.fillMaxWidth()
    ) {
        Column {
            Row(horizontalArrangement = Arrangement.SpaceBetween, modifier = Modifier.fillMaxWidth()) {
                Surface(
                    color = when (app.status) {
                        "Approved" -> Color(0xFFECFDF5)
                        "Pending Review" -> Color(0xFFFFFBEB)
                        else -> CamsBackground
                    },
                    shape = CircleShape
                ) {
                    Text(
                        app.status.uppercase(),
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 2.dp),
                        style = MaterialTheme.typography.labelSmall.copy(
                            fontWeight = FontWeight.Black,
                            color = when (app.status) {
                                "Approved" -> Color(0xFF059669)
                                "Pending Review" -> Color(0xFFB45309)
                                else -> CamsTextSecondary
                            },
                            fontSize = 12.sp
                        )
                    )
                }
                
                IconButton(onClick = { onDelete(app.id) }, modifier = Modifier.size(24.dp)) {
                    Icon(Icons.Filled.Delete, contentDescription = null, tint = Color.Red.copy(alpha = 0.3f), modifier = Modifier.size(16.dp))
                }
            }
            
            Spacer(modifier = Modifier.height(12.dp))
            Text(app.title, style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Black, color = CamsTextPrimary))
            Text(app.category.replace("_", " ").uppercase(), style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold, color = CamsNavy, fontSize = 13.sp))
            
            Spacer(modifier = Modifier.height(16.dp))
            Row(horizontalArrangement = Arrangement.SpaceBetween, modifier = Modifier.fillMaxWidth()) {
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    Column {
                        Text("CLAIMED", style = MaterialTheme.typography.labelSmall.copy(color = CamsTextSecondary, fontWeight = FontWeight.Bold, fontSize = 12.sp))
                        Text("${app.claimedPoints} PTS", style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.Black, color = CamsTextPrimary))
                    }
                    if (app.approvedPoints != null) {
                        Column {
                            Text("APPROVED", style = MaterialTheme.typography.labelSmall.copy(color = Color(0xFF10B981), fontWeight = FontWeight.Bold, fontSize = 12.sp))
                            Text("${app.approvedPoints} PTS", style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.Black, color = Color(0xFF10B981)))
                        }
                    }
                }
                Text(app.date, style = MaterialTheme.typography.labelSmall.copy(color = CamsTextSecondary, fontWeight = FontWeight.Bold))
            }
        }
    }
}

@Composable
fun EmptyApplicationsView() {
    Column(
        modifier = Modifier.fillMaxWidth().padding(40.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
                Icon(Icons.Filled.Stars, contentDescription = null, modifier = Modifier.size(64.dp), tint = CamsNavy.copy(alpha = 0.1f))
                Spacer(modifier = Modifier.height(16.dp))
                Text("No Applications Found", style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Black, color = CamsTextPrimary))
                Text("Submit your achievements to earn activity points.", textAlign = TextAlign.Center, style = MaterialTheme.typography.bodySmall.copy(color = CamsTextSecondary))
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SubmitApplicationDialog(onDismiss: () -> Unit, onAdd: (ActivityPointApplication) -> Unit) {
    var title by remember { mutableStateOf("") }
    var category by remember { mutableStateOf("co_curricular") }
    var points by remember { mutableStateOf("5") }
    var date by remember { mutableStateOf("") }
    var description by remember { mutableStateOf("") }

    val categories = listOf("academic", "co_curricular", "extracurricular", "sports", "certification", "workshop", "seminar", "internship", "community_service")

    Dialog(onDismissRequest = onDismiss, properties = DialogProperties(usePlatformDefaultWidth = false)) {
        Surface(
            modifier = Modifier.fillMaxWidth().padding(16.dp),
            shape = RoundedCornerShape(32.dp),
            color = Color.White
        ) {
            Column(modifier = Modifier.padding(24.dp).verticalScroll(rememberScrollState()).imePadding(), verticalArrangement = Arrangement.spacedBy(16.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    Icon(Icons.Filled.AddCircle, contentDescription = null, tint = CamsNavy)
                    Text("Apply for Credits", style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Black))
                }
                
                OutlinedTextField(value = title, onValueChange = { title = it }, label = { Text("Activity Title") }, modifier = Modifier.fillMaxWidth())
                
                Column {
                    Text("Category", style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold, color = CamsTextSecondary))
                    Row(modifier = Modifier.horizontalScroll(rememberScrollState()), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        categories.forEach { c ->
                            FilterChip(
                                selected = category == c,
                                onClick = { category = c },
                                label = { Text(c.replace("_", " ").uppercase()) },
                                colors = FilterChipDefaults.filterChipColors(
                                    selectedContainerColor = CamsNavy,
                                    selectedLabelColor = Color.White
                                )
                            )
                        }
                    }
                }

                Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    OutlinedTextField(value = points, onValueChange = { points = it }, label = { Text("Points Claimed") }, modifier = Modifier.weight(1f))
                    OutlinedTextField(value = date, onValueChange = { date = it }, label = { Text("Date (YYYY-MM-DD)") }, modifier = Modifier.weight(1f))
                }

                OutlinedTextField(value = description, onValueChange = { description = it }, label = { Text("Activity Description") }, modifier = Modifier.fillMaxWidth(), minLines = 3)

                Surface(
                    onClick = {},
                    modifier = Modifier.fillMaxWidth().height(80.dp),
                    shape = RoundedCornerShape(16.dp),
                    color = CamsBackground,
                    border = BorderStroke(1.dp, Color.LightGray.copy(alpha = 0.5f))
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            Icon(Icons.Filled.CloudUpload, contentDescription = null, tint = CamsTextSecondary)
                            Text("Upload Support Document", style = MaterialTheme.typography.labelSmall.copy(color = CamsTextSecondary))
                        }
                    }
                }

                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    TextButton(onClick = onDismiss, modifier = Modifier.weight(1f)) { Text("Cancel", color = CamsTextSecondary) }
                    Button(
                        onClick = {
                            onAdd(ActivityPointApplication("AP-${System.currentTimeMillis().toString().takeLast(4)}", title, category, date, points.toIntOrNull() ?: 5, null, "Pending Review", description, "Document.pdf"))
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
fun ActivityAppDetailDialog(app: ActivityPointApplication, onDismiss: () -> Unit) {
    Dialog(onDismissRequest = onDismiss) {
        Surface(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(28.dp),
            color = Color.White
        ) {
            Column(modifier = Modifier.padding(24.dp), verticalArrangement = Arrangement.spacedBy(16.dp)) {
                Row(horizontalArrangement = Arrangement.SpaceBetween, modifier = Modifier.fillMaxWidth()) {
                    Surface(color = CamsNavy.copy(alpha = 0.1f), shape = CircleShape) {
                        Text(app.status.uppercase(), modifier = Modifier.padding(horizontal = 12.dp, vertical = 4.dp), style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Black, color = CamsNavy))
                    }
                    IconButton(onClick = onDismiss, modifier = Modifier.size(24.dp)) {
                        Icon(Icons.Filled.Close, contentDescription = null, tint = CamsTextSecondary)
                    }
                }
                
                Text(app.title, style = MaterialTheme.typography.headlineSmall.copy(fontWeight = FontWeight.Black, color = CamsTextPrimary))
                Text(app.category.replace("_", " ").uppercase(), style = MaterialTheme.typography.titleMedium.copy(color = CamsNavy, fontWeight = FontWeight.Bold))
                
                HorizontalDivider(color = Color.LightGray.copy(alpha = 0.2f))
                
                Row(horizontalArrangement = Arrangement.SpaceBetween, modifier = Modifier.fillMaxWidth()) {
                    Column {
                        Text("CLAIMED POINTS", style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Black, color = CamsTextSecondary))
                        Text("${app.claimedPoints} PTS", style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Black, color = CamsTextPrimary))
                    }
                    if (app.approvedPoints != null) {
                        Column(horizontalAlignment = Alignment.End) {
                            Text("APPROVED POINTS", style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Black, color = Color(0xFF10B981)))
                            Text("${app.approvedPoints} PTS", style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Black, color = Color(0xFF10B981)))
                        }
                    }
                }

                Surface(color = CamsBackground, shape = RoundedCornerShape(12.dp), modifier = Modifier.fillMaxWidth()) {
                    Column(modifier = Modifier.padding(12.dp)) {
                        Text("DESCRIPTION", style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Black, color = CamsTextSecondary, fontSize = 12.sp))
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(app.description, style = MaterialTheme.typography.bodySmall.copy(color = CamsTextPrimary))
                    }
                }

                if (app.reviewedBy != null) {
                    Column(modifier = Modifier.padding(top = 8.dp)) {
                        Text("REVIEWS & REMARKS", style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Black, color = CamsTextSecondary))
                        Spacer(modifier = Modifier.height(8.dp))
                        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            Surface(modifier = Modifier.size(24.dp), color = CamsNavy.copy(alpha = 0.1f), shape = CircleShape) {}
                            Text(app.reviewedBy, style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold, color = CamsTextPrimary))
                            Spacer(modifier = Modifier.weight(1f))
                            Text(app.reviewedAt ?: "", style = MaterialTheme.typography.labelSmall.copy(color = CamsTextSecondary))
                        }
                        if (app.facultyRemarks != null) {
                            Text(app.facultyRemarks, modifier = Modifier.padding(top = 4.dp), style = MaterialTheme.typography.bodySmall.copy(color = CamsTextSecondary))
                        }
                    }
                }

                Button(
                    onClick = {},
                    modifier = Modifier.fillMaxWidth().height(48.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = CamsNavy),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Icon(Icons.Filled.Description, contentDescription = null)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("View Support Document")
                }
            }
        }
    }
}
