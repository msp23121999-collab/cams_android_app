package com.example.features.campus_life.screens

import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowForward
import androidx.compose.material.icons.filled.*
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
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.core.navigation.AppRoutes
import com.example.core.network.GrievanceDto
import com.example.core.theme.*
import com.example.core.ui.CamsCard
import com.example.core.ui.CamsScreen
import com.example.features.campus_life.models.Grievance
import com.example.features.campus_life.providers.GrievancesViewModel
import com.example.features.student.widgets.StudentDrawer
import com.example.core.theme.*
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun GrievancesScreen(
    viewModel: GrievancesViewModel,
    onNavigate: (String) -> Unit
) {
    val uiState by viewModel.uiState.collectAsState()
    var showForm by remember { mutableStateOf(false) }
    var selectedGrievance by remember { mutableStateOf<Grievance?>(null) }

    CamsScreen(scrollable = false,
        title = "Grievance Panel",
        subtitle = "Support & Compliance Portal",
        onBackClick = { onNavigate(AppRoutes.STUDENT_DASHBOARD) },
        actions = {
            IconButton(onClick = { showForm = true }) {
                Icon(Icons.Filled.AddCircle, contentDescription = "New", tint = Color.White)
            }
        }
    ) {
        if (uiState.isLoading) {
            Box(modifier = Modifier.fillMaxWidth().height(300.dp), contentAlignment = Alignment.Center) {
                CircularProgressIndicator(color = CamsNavy)
            }
        } else if (uiState.error != null) {
            com.example.core.ui.NetworkErrorView(
                message = uiState.error!!,
                onRetry = { viewModel.fetchGrievances() },
                modifier = Modifier.fillMaxSize()
            )
        } else {
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(24.dp)
            ) {
                // KPI Cards
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    GrievanceKPICard(Modifier.weight(1f), "Total", uiState.grievances.size.toString(), Icons.Filled.Message, CamsNavy)
                    GrievanceKPICard(
                        Modifier.weight(1f), 
                        "Pending", 
                        uiState.grievances.count { it.status == "Pending" }.toString(), 
                        Icons.Filled.Schedule, 
                        Color(0xFFD97706)
                    )
                }

                // List
                Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                    if (uiState.grievances.isEmpty()) {
                        EmptyGrievances()
                    } else {
                        uiState.grievances.forEach { grievance ->
                            GrievanceItem(grievance) { selectedGrievance = grievance }
                        }
                    }
                }

            Spacer(modifier = Modifier.height(20.dp))
            }
        }
    }

    if (showForm) {
        NewGrievanceDialog(
            onDismiss = { showForm = false },
            onSubmit = { cat, desc ->
                viewModel.addGrievance(cat, "Medium", "General", desc)
                showForm = false
            }
        )
    }

    if (selectedGrievance != null) {
        GrievanceDetailsDialog(grievance = selectedGrievance!!, onDismiss = { selectedGrievance = null })
    }
}

@Composable
private fun GrievanceKPICard(modifier: Modifier, label: String, value: String, icon: ImageVector, color: Color) {
    CamsCard(modifier = modifier) {
        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(16.dp)) {
            Surface(color = color.copy(alpha = 0.1f), shape = RoundedCornerShape(12.dp), modifier = Modifier.size(40.dp)) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(icon, contentDescription = null, tint = color, modifier = Modifier.size(20.dp))
                }
            }
            Column {
                Text(label.uppercase(), style = MaterialTheme.typography.labelSmall.copy(fontSize = 12.sp, fontWeight = FontWeight.Black), color = CamsTextSecondary)
                Text(value, style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Black, fontSize = 20.sp), color = CamsTextPrimary)
            }
        }
    }
}

@Composable
private fun NewGrievanceDialog(onDismiss: () -> Unit, onSubmit: (String, String) -> Unit) {
    var description by remember { mutableStateOf("") }
    var category by remember { mutableStateOf("Infrastructure") }

    Dialog(onDismissRequest = onDismiss) {
        Card(
            shape = RoundedCornerShape(24.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            modifier = Modifier.fillMaxWidth().padding(16.dp)
        ) {
            Column(modifier = Modifier.padding(24.dp), verticalArrangement = Arrangement.spacedBy(16.dp)) {
                Text("Raise Grievance", style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Black))
                
                OutlinedTextField(
                    value = category,
                    onValueChange = { category = it },
                    label = { Text("Category") },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp)
                )

                OutlinedTextField(
                    value = description,
                    onValueChange = { description = it },
                    label = { Text("Statement / Description") },
                    modifier = Modifier.fillMaxWidth().height(120.dp),
                    shape = RoundedCornerShape(12.dp)
                )

                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Button(
                        onClick = onDismiss,
                        modifier = Modifier.weight(1f),
                        colors = ButtonDefaults.buttonColors(containerColor = Zinc50, contentColor = Slate600),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Text("Cancel")
                    }
                    Button(
                        onClick = { if(description.isNotBlank()) onSubmit(category, description) },
                        modifier = Modifier.weight(1.5f),
                        colors = ButtonDefaults.buttonColors(containerColor = Purple600),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Text("Submit Ticket")
                    }
                }
            }
        }
    }
}

@Composable
private fun GrievanceItem(grievance: Grievance, onClick: () -> Unit) {
    CamsCard(
        modifier = Modifier.fillMaxWidth().clickable { onClick() },
    ) {
        Column {
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Text(grievance.id, style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Black), color = CamsNavy)
                Text(grievance.date, style = MaterialTheme.typography.labelSmall, color = CamsTextSecondary)
            }
            Spacer(modifier = Modifier.height(8.dp))
            Text(grievance.category, style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Black), color = CamsTextPrimary)
            Spacer(modifier = Modifier.height(12.dp))
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                Surface(
                    color = when(grievance.status) {
                        "Resolved" -> Color(0xFFECFDF5)
                        "Pending" -> Color(0xFFFFFBEB)
                        else -> Color(0xFFEFF6FF)
                    },
                    shape = RoundedCornerShape(4.dp)
                ) {
                    Text(
                        grievance.status.uppercase(),
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp),
                        style = MaterialTheme.typography.labelSmall.copy(fontSize = 12.sp, fontWeight = FontWeight.Black),
                        color = when(grievance.status) {
                            "Resolved" -> Color(0xFF059669)
                            "Pending" -> Color(0xFFD97706)
                            else -> Color(0xFF2563EB)
                        }
                    )
                }
                Icon(Icons.Filled.ArrowForward, contentDescription = null, modifier = Modifier.size(16.dp), tint = CamsNavy)
            }
        }
    }
}

@Composable
private fun GrievanceDetailsDialog(grievance: Grievance, onDismiss: () -> Unit) {
    Dialog(onDismissRequest = onDismiss, properties = DialogProperties(usePlatformDefaultWidth = false)) {
        Card(
            modifier = Modifier.fillMaxWidth().fillMaxHeight(0.8f).padding(16.dp),
            shape = RoundedCornerShape(32.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
        ) {
            Column(modifier = Modifier.fillMaxSize()) {
                Row(modifier = Modifier.padding(24.dp), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.SpaceBetween) {
                    Column {
                        Text(grievance.id, style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Black), color = Slate400)
                        Text(grievance.category, style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Black), color = Slate900)
                    }
                    IconButton(onClick = onDismiss, modifier = Modifier.background(Zinc50, CircleShape)) {
                        Icon(Icons.Filled.Close, contentDescription = null, tint = Slate600)
                    }
                }
                
                HorizontalDivider(color = Zinc200.copy(alpha = 0.5f))

                Column(modifier = Modifier.weight(1f).verticalScroll(rememberScrollState()).padding(24.dp), verticalArrangement = Arrangement.spacedBy(24.dp)) {
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                        DetailTag("Category", grievance.category, Icons.Filled.Category)
                    }

                    Column {
                        Text("FACTUAL STATEMENT", style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Black, letterSpacing = 1.sp), color = Slate400)
                        Spacer(modifier = Modifier.height(8.dp))
                        Surface(color = Zinc50, shape = RoundedCornerShape(16.dp), border = BorderStroke(1.dp, Zinc200)) {
                            Text(grievance.description, modifier = Modifier.padding(16.dp), style = MaterialTheme.typography.bodySmall.copy(lineHeight = 20.sp), color = Slate600)
                        }
                    }

                    Column {
                        Text("AUDIT LOG", style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Black, letterSpacing = 1.sp), color = Slate400)
                        Spacer(modifier = Modifier.height(12.dp))
                        AuditRow("Current Status", grievance.status, Icons.Filled.Assignment)
                    }
                }

                Box(modifier = Modifier.padding(24.dp)) {
                    Button(
                        onClick = onDismiss,
                        modifier = Modifier.fillMaxWidth().height(48.dp),
                        shape = RoundedCornerShape(16.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = Slate900)
                    ) {
                        Text("Close Audit View")
                    }
                }
            }
        }
    }
}

@Composable
fun EmptyGrievances() {
    Column(
        modifier = Modifier.fillMaxWidth().padding(40.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Icon(Icons.Filled.Inbox, contentDescription = null, modifier = Modifier.size(64.dp), tint = Color.LightGray)
        Spacer(modifier = Modifier.height(16.dp))
        Text("No grievances found", style = MaterialTheme.typography.titleMedium, color = CamsTextSecondary)
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

@Composable
private fun AuditRow(label: String, value: String, icon: ImageVector) {
    Row(modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(12.dp)) {
        Icon(icon, contentDescription = null, modifier = Modifier.size(16.dp), tint = Purple600)
        Column {
            Text(label, style = MaterialTheme.typography.labelSmall, color = Slate400)
            Text(value, style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Bold), color = Slate900)
        }
    }
}
