package com.example.features.campus_life.screens

import androidx.compose.animation.*
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
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
import com.example.core.theme.*
import com.example.core.ui.CamsCard
import com.example.core.ui.CamsScreen
import com.example.features.campus_life.models.CertificationRecord
import com.example.features.campus_life.providers.CertificationsViewModel
import com.example.features.student.widgets.StudentDrawer
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CertificationsScreen(
    onNavigate: (String) -> Unit,
    viewModel: CertificationsViewModel = viewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    var showUploadModal by remember { mutableStateOf(false) }

    CamsScreen(
        scrollable = true,
        title = "Certifications",
        subtitle = "Professional Achievement Hub",
        onBackClick = { onNavigate(AppRoutes.STUDENT_DASHBOARD) },
        verticalArrangement = Arrangement.spacedBy(24.dp)
    ) {
        // Analytics Row
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                CertMetricCard(
                    Modifier.weight(1f),
                    "Total Certs",
                    uiState.certifications.size.toString(),
                    Icons.Filled.EmojiEvents,
                    CamsNavy.copy(alpha = 0.05f)
                )
                CertMetricCard(
                    Modifier.weight(1f),
                    "Verified",
                    uiState.certifications.count { it.verified }.toString(),
                    Icons.Filled.Verified,
                    Color(0xFFECFDF5)
                )
            }

            // Search & Filter
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                OutlinedTextField(
                    value = uiState.searchQuery,
                    onValueChange = { viewModel.updateSearch(it) },
                    placeholder = { Text("Search certificates...", fontSize = 14.sp) },
                    leadingIcon = { Icon(Icons.Filled.Search, contentDescription = null, tint = CamsTextSecondary) },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedContainerColor = Color.White,
                        unfocusedContainerColor = Color.White,
                        unfocusedBorderColor = Color.LightGray.copy(alpha = 0.5f),
                        focusedBorderColor = CamsNavy
                    )
                )
                
                val filters = listOf("All", "Internship", "Moot Court", "Professional Training", "Research Publication", "Legal Aid")
                Row(
                    modifier = Modifier.horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    filters.forEach { filter ->
                        FilterChip(
                            selected = uiState.categoryFilter == filter,
                            onClick = { viewModel.updateFilter(filter) },
                            label = { Text(filter) },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = CamsNavy,
                                selectedLabelColor = Color.White
                            ),
                            shape = RoundedCornerShape(12.dp)
                        )
                    }
                }
            }

            // Cert List
            val filteredList = uiState.certifications.filter {
                (uiState.categoryFilter == "All" || it.category == uiState.categoryFilter) &&
                (it.title.contains(uiState.searchQuery, ignoreCase = true) || it.authority.contains(uiState.searchQuery, ignoreCase = true))
            }

            if (filteredList.isEmpty()) {
                EmptyCertsView()
            } else {
                filteredList.forEach { cert ->
                    CertItemCard(cert = cert, onDelete = { viewModel.deleteCertification(it) })
                }
            }
            
            // Timeline
            CertTimeline(uiState.certifications)
            
            Spacer(modifier = Modifier.height(20.dp))
    }

    if (showUploadModal) {
        UploadCertDialog(
            onDismiss = { showUploadModal = false },
            onUpload = {
                viewModel.addCertification(it)
                showUploadModal = false
            }
        )
    }
}

@Composable
fun CertMetricCard(modifier: Modifier = Modifier, label: String, value: String, icon: ImageVector, bgColor: Color) {
    CamsCard(modifier = modifier) {
        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(16.dp)) {
            Surface(modifier = Modifier.size(52.dp), color = bgColor, shape = RoundedCornerShape(16.dp)) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(icon, contentDescription = null, tint = CamsNavy, modifier = Modifier.size(24.dp))
                }
            }
            Column {
                Text(label.uppercase(), style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Black, color = CamsTextSecondary, letterSpacing = 0.5.sp))
                Text(value, style = MaterialTheme.typography.headlineSmall.copy(fontWeight = FontWeight.Black, color = CamsTextPrimary))
            }
        }
    }
}

@Composable
fun CertItemCard(cert: CertificationRecord, onDelete: (String) -> Unit) {
    CamsCard(modifier = Modifier.fillMaxWidth()) {
        Column {
            Row(horizontalArrangement = Arrangement.SpaceBetween, modifier = Modifier.fillMaxWidth()) {
                Surface(
                    modifier = Modifier.size(48.dp),
                    color = CamsBackground,
                    shape = RoundedCornerShape(14.dp)
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(
                            when(cert.type) {
                                "internship" -> Icons.Filled.BusinessCenter
                                "moot" -> Icons.Filled.Balance
                                "publication" -> Icons.Filled.Description
                                else -> Icons.Filled.EmojiEvents
                            },
                            contentDescription = null,
                            tint = CamsNavy
                        )
                    }
                }
                
                if (cert.verified) {
                    Surface(color = Color(0xFFECFDF5), shape = CircleShape) {
                        Row(modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                            Icon(Icons.Filled.Verified, contentDescription = null, tint = Color(0xFF059669), modifier = Modifier.size(12.dp))
                            Text("Verified", style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Black, color = Color(0xFF059669)))
                        }
                    }
                }
            }
            
            Spacer(modifier = Modifier.height(16.dp))
            Text(cert.title, style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Black, color = CamsTextPrimary, lineHeight = 26.sp))
            Text(cert.authority, style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Bold, color = CamsTextSecondary))
            
            Spacer(modifier = Modifier.height(12.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Surface(color = CamsNavy.copy(alpha = 0.05f), shape = RoundedCornerShape(8.dp)) {
                    Text(cert.category.uppercase(), modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp), style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Black, color = CamsNavy, fontSize = 13.sp))
                }
                Surface(color = CamsBackground, shape = RoundedCornerShape(8.dp)) {
                    Text(cert.date, modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp), style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold, color = CamsTextSecondary, fontSize = 13.sp))
                }
            }
            
            Spacer(modifier = Modifier.height(16.dp))
            Row(
                modifier = Modifier.fillMaxWidth().background(CamsBackground, RoundedCornerShape(12.dp)).padding(horizontal = 12.dp, vertical = 8.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text("ID: ${cert.id}", style = MaterialTheme.typography.labelSmall.copy(color = CamsTextSecondary, fontWeight = FontWeight.Bold))
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    IconButton(onClick = { onDelete(cert.id) }, modifier = Modifier.size(32.dp)) {
                        Icon(Icons.Filled.Delete, contentDescription = null, tint = Color.Red.copy(alpha = 0.3f), modifier = Modifier.size(16.dp))
                    }
                    Surface(
                        onClick = {},
                        modifier = Modifier.size(32.dp),
                        color = Color.White,
                        shape = RoundedCornerShape(8.dp),
                        border = BorderStroke(1.dp, Color.LightGray.copy(alpha = 0.2f))
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Icon(Icons.Filled.Download, contentDescription = null, tint = CamsTextSecondary, modifier = Modifier.size(16.dp))
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun CertTimeline(certs: List<CertificationRecord>) {
    if (certs.isEmpty()) return
    
    CamsCard(modifier = Modifier.fillMaxWidth()) {
        Column {
            Text("ACHIEVEMENT TIMELINE", style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Black, color = CamsTextSecondary, letterSpacing = 1.sp))
            Spacer(modifier = Modifier.height(20.dp))
            
            certs.sortedByDescending { it.date }.take(3).forEachIndexed { index, cert ->
                Row(modifier = Modifier.fillMaxWidth()) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Surface(
                            modifier = Modifier.size(10.dp),
                            color = if (index == 0) CamsNavy else Color.LightGray,
                            shape = CircleShape,
                            border = BorderStroke(2.dp, Color.White)
                        ) {}
                        if (index < certs.size - 1 && index < 2) {
                            Box(modifier = Modifier.width(1.dp).height(40.dp).background(Color.LightGray.copy(alpha = 0.3f)))
                        }
                    }
                    Spacer(modifier = Modifier.width(16.dp))
                    Column {
                        Text(cert.title, style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.Black, color = CamsTextPrimary))
                        Text(cert.date, style = MaterialTheme.typography.labelSmall.copy(color = CamsTextSecondary, fontWeight = FontWeight.Bold))
                    }
                }
                if (index < 2) Spacer(modifier = Modifier.height(12.dp))
            }
        }
    }
}

@Composable
fun EmptyCertsView() {
    Column(
        modifier = Modifier.fillMaxWidth().padding(60.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Icon(Icons.Filled.EmojiEvents, contentDescription = null, modifier = Modifier.size(80.dp), tint = Slate100)
        Spacer(modifier = Modifier.height(20.dp))
        Text("No Achievements Yet", style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Black, color = Slate800))
        Text("Upload your training, moot court, or internship certificates to build your professional profile.", textAlign = TextAlign.Center, style = MaterialTheme.typography.bodySmall.copy(color = Slate400))
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun UploadCertDialog(onDismiss: () -> Unit, onUpload: (CertificationRecord) -> Unit) {
    var title by remember { mutableStateOf("") }
    var authority by remember { mutableStateOf("") }
    var date by remember { mutableStateOf("") }
    var category by remember { mutableStateOf("Professional Training") }

    val categories = listOf("Professional Training", "Internship", "Moot Court", "Research Publication", "Legal Aid", "Value-Added Course")

    Dialog(onDismissRequest = onDismiss, properties = DialogProperties(usePlatformDefaultWidth = false)) {
        Surface(
            modifier = Modifier.fillMaxWidth().padding(16.dp),
            shape = RoundedCornerShape(32.dp),
            color = Color.White
        ) {
            Column(modifier = Modifier.padding(24.dp).verticalScroll(rememberScrollState()), verticalArrangement = Arrangement.spacedBy(16.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    Icon(Icons.Filled.UploadFile, contentDescription = null, tint = Purple600)
                    Text("Upload Certificate", style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Black))
                }
                
                OutlinedTextField(value = title, onValueChange = { title = it }, label = { Text("Certificate Title") }, modifier = Modifier.fillMaxWidth())
                OutlinedTextField(value = authority, onValueChange = { authority = it }, label = { Text("Issuing Authority") }, modifier = Modifier.fillMaxWidth())
                
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

                OutlinedTextField(value = date, onValueChange = { date = it }, label = { Text("Completion Date (e.g. May 2026)") }, modifier = Modifier.fillMaxWidth())

                Surface(
                    onClick = {},
                    modifier = Modifier.fillMaxWidth().height(100.dp),
                    shape = RoundedCornerShape(16.dp),
                    color = Slate50,
                    border = BorderStroke(1.dp, Slate200)
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.Center) {
                        Icon(Icons.Filled.CloudUpload, contentDescription = null, tint = Slate400)
                        Text("Tap to select file", style = MaterialTheme.typography.labelSmall.copy(color = Slate500))
                    }
                }

                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    TextButton(onClick = onDismiss, modifier = Modifier.weight(1f)) { Text("Cancel") }
                    Button(
                        onClick = {
                            onUpload(CertificationRecord("LXC-${System.currentTimeMillis().toString().takeLast(4)}", title, authority, date, category, false, "training"))
                        },
                        modifier = Modifier.weight(1f),
                        colors = ButtonDefaults.buttonColors(containerColor = Purple600),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Text("Save")
                    }
                }
            }
        }
    }
}
