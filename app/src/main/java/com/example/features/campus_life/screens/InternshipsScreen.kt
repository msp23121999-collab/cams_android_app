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
import com.example.features.campus_life.models.InternshipRecord
import com.example.features.campus_life.providers.InternshipsViewModel
import com.example.features.student.widgets.StudentDrawer
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*
import androidx.paging.compose.collectAsLazyPagingItems
import androidx.paging.LoadState
import android.content.Intent
import android.net.Uri
import androidx.compose.ui.platform.LocalContext

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun InternshipsScreen(
    onNavigate: (String) -> Unit,
    viewModel: InternshipsViewModel = viewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    var showAddModal by remember { mutableStateOf(false) }
    var selectedInternship by remember { mutableStateOf<InternshipRecord?>(null) }

    CamsScreen(scrollable = true,
        title = "Internships",
        subtitle = "Professional Development Tracker",
        onBackClick = { onNavigate(AppRoutes.STUDENT_DASHBOARD) },
        verticalArrangement = Arrangement.spacedBy(24.dp)
    ) {
        // Banner
            CamsCard {
                Column {
                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        Icon(Icons.Filled.Shield, contentDescription = null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(12.dp))
                        Text(
                            "PROFESSIONAL DEVELOPMENT",
                            style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Black, color = CamsNavy, letterSpacing = 1.sp)
                        )
                    }
                    Spacer(modifier = Modifier.height(12.dp))
                    Text(
                        "Internships & Clerkships",
                        style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Black, color = MaterialTheme.colorScheme.onSurface)
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        "Record and manage your legal internships and clerkships. Complete mandatory weeks for your degree audit.",
                        style = MaterialTheme.typography.bodySmall.copy(color = MaterialTheme.colorScheme.onSurfaceVariant, lineHeight = 18.sp)
                    )
                }
            }

            // Metrics Row
            val totalWeeks = uiState.internships.sumOf { calculateWeeks(it.startDate, it.endDate) }
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                InternshipMetricCard(Modifier.weight(1f), "Total", uiState.internships.size.toString(), Icons.Filled.BusinessCenter, CamsNavy)
                InternshipMetricCard(Modifier.weight(1f), "Verified", uiState.internships.count { it.status == "Verified" }.toString(), Icons.Filled.CheckCircle, Color(0xFF10B981))
            }
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                InternshipMetricCard(Modifier.weight(1f), "Pending", uiState.internships.count { it.status != "Verified" }.toString(), Icons.Filled.History, Color(0xFFF59E0B))
                InternshipMetricCard(Modifier.weight(1f), "Weeks", "$totalWeeks Wks", Icons.Filled.CalendarToday, Color(0xFF6366F1))
            }

            // Action Bar
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                OutlinedTextField(
                    value = uiState.searchQuery,
                    onValueChange = { viewModel.updateSearch(it) },
                    placeholder = { Text("Search internships...", fontSize = 14.sp) },
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
                    onClick = { showAddModal = true },
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = CamsNavy),
                    contentPadding = PaddingValues(horizontal = 16.dp, vertical = 12.dp)
                ) {
                    Icon(Icons.Filled.Add, contentDescription = null, modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Add", style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.Black))
                }
            }

            // List
            val pagingItems = viewModel.internshipsPagingFlow.collectAsLazyPagingItems()

            if (pagingItems.loadState.refresh is LoadState.Loading) {
                CircularProgressIndicator(modifier = Modifier.align(Alignment.CenterHorizontally), color = CamsNavy)
            } else if (pagingItems.itemCount == 0 && pagingItems.loadState.append.endOfPaginationReached) {
                EmptyInternshipsView()
            } else {
                for (index in 0 until pagingItems.itemCount) {
                    val intern = pagingItems[index]
                    if (intern != null) {
                        val matchesSearch = intern.organization.contains(uiState.searchQuery, ignoreCase = true) ||
                                            intern.role.contains(uiState.searchQuery, ignoreCase = true)
                        if (matchesSearch) {
                            InternshipListItem(
                                intern = intern,
                                isSelected = selectedInternship?.id == intern.id,
                                onClick = { selectedInternship = intern },
                                onDelete = { viewModel.deleteInternship(it) }
                            )
                        }
                    }
                }
            }
            
            Spacer(modifier = Modifier.height(20.dp))
    }

    if (showAddModal) {
        AddInternshipDialog(
            onDismiss = { showAddModal = false },
            onAdd = {
                viewModel.addInternship(it)
                showAddModal = false
            }
        )
    }

    if (selectedInternship != null) {
        InternshipDetailDialog(
            intern = selectedInternship!!,
            onDismiss = { selectedInternship = null }
        )
    }

    if (uiState.errorMsg != null) {
        AlertDialog(
            onDismissRequest = { viewModel.clearError() },
            title = { Text("Error") },
            text = { Text(uiState.errorMsg!!) },
            confirmButton = {
                TextButton(onClick = { viewModel.clearError() }) { Text("OK") }
            }
        )
    }
}

@Composable
fun InternshipMetricCard(modifier: Modifier = Modifier, label: String, value: String, icon: ImageVector, color: Color) {
    CamsCard(modifier = modifier) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Column {
                Text(label.uppercase(), style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Black, color = MaterialTheme.colorScheme.onSurfaceVariant, letterSpacing = 0.5.sp))
                Text(value, style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Black, color = MaterialTheme.colorScheme.onSurface))
            }
            Surface(color = color.copy(alpha = 0.1f), shape = RoundedCornerShape(12.dp)) {
                Icon(icon, contentDescription = null, tint = color, modifier = Modifier.padding(8.dp).size(20.dp))
            }
        }
    }
}

@Composable
fun InternshipListItem(
    intern: InternshipRecord,
    isSelected: Boolean,
    onClick: () -> Unit,
    onDelete: (String) -> Unit
) {
    CamsCard(
        onClick = onClick,
        modifier = Modifier.fillMaxWidth()
    ) {
        Column {
            Row(horizontalArrangement = Arrangement.SpaceBetween, modifier = Modifier.fillMaxWidth()) {
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Surface(
                        color = when (intern.status) {
                            "Verified" -> Color(0xFFECFDF5)
                            "Pending Verification" -> Color(0xFFFFFBEB)
                            else -> CamsBackground
                        },
                        shape = CircleShape
                    ) {
                        Text(
                            intern.status.uppercase(),
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 2.dp),
                            style = MaterialTheme.typography.labelSmall.copy(
                                fontWeight = FontWeight.Black,
                                color = when (intern.status) {
                                    "Verified" -> Color(0xFF059669)
                                    "Pending Verification" -> Color(0xFFB45309)
                                    else -> CamsTextSecondary
                                },
                                fontSize = 12.sp
                            )
                        )
                    }
                    Surface(color = CamsBackground, shape = RoundedCornerShape(4.dp)) {
                        Text(intern.type.uppercase(), modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp), style = MaterialTheme.typography.labelSmall.copy(fontSize = 12.sp, fontWeight = FontWeight.Bold, color = CamsNavy))
                    }
                }
                
                IconButton(onClick = { onDelete(intern.id) }, modifier = Modifier.size(40.dp)) {
                    Icon(Icons.Filled.Delete, contentDescription = "Delete", tint = Color.Red.copy(alpha = 0.3f), modifier = Modifier.size(16.dp))
                }
            }
            
            Spacer(modifier = Modifier.height(12.dp))
            Text(intern.organization, style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Black, color = MaterialTheme.colorScheme.onSurface))
            Text(intern.role, style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurfaceVariant))
            
            Spacer(modifier = Modifier.height(16.dp))
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                    Icon(Icons.Filled.CalendarToday, contentDescription = null, tint = MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.size(12.dp))
                    Text(formatDateRange(intern.startDate, intern.endDate), style = MaterialTheme.typography.labelSmall.copy(color = MaterialTheme.colorScheme.onSurfaceVariant))
                }
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                    Icon(Icons.Filled.Person, contentDescription = null, tint = MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.size(12.dp))
                    Text(intern.supervisor, style = MaterialTheme.typography.labelSmall.copy(color = MaterialTheme.colorScheme.onSurfaceVariant))
                }
            }
        }
    }
}

@Composable
fun EmptyInternshipsView() {
    Column(
        modifier = Modifier.fillMaxWidth().padding(40.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
                Icon(Icons.Filled.BusinessCenter, contentDescription = null, modifier = Modifier.size(64.dp), tint = MaterialTheme.colorScheme.primary.copy(alpha = 0.1f))
                Spacer(modifier = Modifier.height(16.dp))
                Text("No Records Found", style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Black, color = MaterialTheme.colorScheme.onSurface))
                Text("Start recording your internships and clerkships.", textAlign = TextAlign.Center, style = MaterialTheme.typography.bodySmall.copy(color = MaterialTheme.colorScheme.onSurfaceVariant))
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddInternshipDialog(onDismiss: () -> Unit, onAdd: (InternshipRecord) -> Unit) {
    var org by remember { mutableStateOf("") }
    var type by remember { mutableStateOf("Law Firm") }
    var role by remember { mutableStateOf("") }
    var supervisor by remember { mutableStateOf("") }
    var startDate by remember { mutableStateOf("") }
    var endDate by remember { mutableStateOf("") }
    var responsibilities by remember { mutableStateOf("") }

    val types = listOf("Supreme Court", "High Court", "Trial / District Court", "Law Firm", "NGO", "Corporate", "Government")

    Dialog(onDismissRequest = onDismiss, properties = DialogProperties(usePlatformDefaultWidth = false)) {
        Surface(
            modifier = Modifier.fillMaxWidth().padding(16.dp),
            shape = RoundedCornerShape(32.dp),
            color = Color.White
        ) {
            Column(modifier = Modifier.padding(24.dp).verticalScroll(rememberScrollState()).imePadding(), verticalArrangement = Arrangement.spacedBy(16.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    Icon(Icons.Filled.BusinessCenter, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                    Text("Add Internship", style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Black))
                }
                
                OutlinedTextField(value = org, onValueChange = { org = it }, label = { Text("Organization / Chambers") }, modifier = Modifier.fillMaxWidth())
                
                Column {
                    Text("Type", style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurfaceVariant))
                    Row(modifier = Modifier.horizontalScroll(rememberScrollState()), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        types.forEach { t ->
                            FilterChip(
                                selected = type == t,
                                onClick = { type = t },
                                label = { Text(t) },
                                colors = FilterChipDefaults.filterChipColors(
                                    selectedContainerColor = CamsNavy,
                                    selectedLabelColor = Color.White
                                )
                            )
                        }
                    }
                }

                OutlinedTextField(value = role, onValueChange = { role = it }, label = { Text("Your Role") }, modifier = Modifier.fillMaxWidth())
                OutlinedTextField(value = supervisor, onValueChange = { supervisor = it }, label = { Text("Supervisor Advocate") }, modifier = Modifier.fillMaxWidth())
                
                Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    OutlinedTextField(value = startDate, onValueChange = { startDate = it }, label = { Text("Start (YYYY-MM-DD)") }, modifier = Modifier.weight(1f))
                    OutlinedTextField(value = endDate, onValueChange = { endDate = it }, label = { Text("End (YYYY-MM-DD)") }, modifier = Modifier.weight(1f))
                }

                OutlinedTextField(value = responsibilities, onValueChange = { responsibilities = it }, label = { Text("Responsibilities") }, modifier = Modifier.fillMaxWidth(), minLines = 3)

                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    TextButton(onClick = onDismiss, modifier = Modifier.weight(1f)) { Text("Cancel", color = MaterialTheme.colorScheme.onSurfaceVariant) }
                    Button(
                        onClick = {
                            onAdd(InternshipRecord("i${System.currentTimeMillis()}", org, type, role, startDate, endDate, supervisor, responsibilities, "Pending Verification"))
                        },
                        modifier = Modifier.weight(1f),
                        colors = ButtonDefaults.buttonColors(containerColor = CamsNavy),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Text("Save")
                    }
                }
            }
        }
    }
}

@Composable
fun InternshipDetailDialog(intern: InternshipRecord, onDismiss: () -> Unit) {
    val context = LocalContext.current
    Dialog(onDismissRequest = onDismiss) {
        Surface(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(28.dp),
            color = Color.White
        ) {
            Column(modifier = Modifier.padding(24.dp), verticalArrangement = Arrangement.spacedBy(16.dp)) {
                Row(horizontalArrangement = Arrangement.SpaceBetween, modifier = Modifier.fillMaxWidth()) {
                    Surface(color = CamsNavy.copy(alpha = 0.1f), shape = CircleShape) {
                        Text(intern.status.uppercase(), modifier = Modifier.padding(horizontal = 12.dp, vertical = 4.dp), style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Black, color = CamsNavy))
                    }
                    IconButton(onClick = onDismiss, modifier = Modifier.size(40.dp)) {
                        Icon(Icons.Filled.Close, contentDescription = "Close", tint = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                }
                
                Text(intern.organization, style = MaterialTheme.typography.headlineSmall.copy(fontWeight = FontWeight.Black, color = MaterialTheme.colorScheme.onSurface))
                Text(intern.role, style = MaterialTheme.typography.titleMedium.copy(color = CamsNavy, fontWeight = FontWeight.Bold))
                
                HorizontalDivider(color = Color.LightGray.copy(alpha = 0.2f))
                
                DetailItem("Internship Type", intern.type, Icons.Filled.Business)
                DetailItem("Duration", "${intern.startDate} to ${intern.endDate}", Icons.Filled.CalendarToday)
                DetailItem("Supervisor", intern.supervisor, Icons.Filled.Person)
                
                if (intern.responsibilities.isNotBlank()) {
                    Column(modifier = Modifier.padding(top = 8.dp)) {
                        Text("Responsibilities", style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Black, color = MaterialTheme.colorScheme.onSurfaceVariant, letterSpacing = 1.sp))
                        Spacer(modifier = Modifier.height(8.dp))
                        Surface(color = CamsBackground, shape = RoundedCornerShape(12.dp), modifier = Modifier.fillMaxWidth()) {
                            Text(intern.responsibilities, modifier = Modifier.padding(12.dp), style = MaterialTheme.typography.bodySmall.copy(color = MaterialTheme.colorScheme.onSurface))
                        }
                    }
                }

                if (intern.certificateUrl != null) {
                    Button(
                        onClick = {
                            val intent = Intent(Intent.ACTION_VIEW, Uri.parse(intern.certificateUrl))
                            context.startActivity(intent)
                        },
                        modifier = Modifier.fillMaxWidth().height(48.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = CamsNavy),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Icon(Icons.Filled.Description, contentDescription = null)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("View Certificate")
                    }
                }
            }
        }
    }
}

@Composable
fun DetailItem(label: String, value: String, icon: ImageVector) {
    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(12.dp)) {
        Icon(icon, contentDescription = null, tint = MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.size(16.dp))
        Column {
            Text(label.uppercase(), style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Black, color = MaterialTheme.colorScheme.onSurfaceVariant, fontSize = 12.sp, letterSpacing = 0.5.sp))
            Text(value, style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurface))
        }
    }
}

private fun formatDateRange(start: String, end: String): String {
    return try {
        val parser = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
        val formatter = SimpleDateFormat("MMM yyyy", Locale.getDefault())
        val s = formatter.format(parser.parse(start)!!)
        val e = formatter.format(parser.parse(end)!!)
        "$s - $e"
    } catch (e: Exception) {
        "$start - $end"
    }
}

private fun calculateWeeks(start: String, end: String): Int {
    return try {
        val parser = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
        val s = parser.parse(start)!!.time
        val e = parser.parse(end)!!.time
        val diff = e - s
        (diff / (1000 * 60 * 60 * 24 * 7)).toInt().coerceAtLeast(1)
    } catch (e: Exception) {
        4
    }
}
