package com.example.features.campus_life.screens

import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.compose.animation.*
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.*
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.material3.MaterialTheme
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.blur
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.core.navigation.AppRoutes
import com.example.core.theme.*
import com.example.core.ui.CamsCard
import com.example.core.ui.CamsScreen
import com.example.features.campus_life.models.*
import com.example.features.campus_life.providers.*

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun LexNovaScreen(
    onNavigate: (String) -> Unit,
    viewModel: LexNovaViewModel = viewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    CamsScreen(
        scrollable = true,
        title = "LexNova Portal",
        subtitle = "Digital Legal Ecosystem",
        onBackClick = { onNavigate(AppRoutes.STUDENT_DASHBOARD) },
        verticalArrangement = Arrangement.spacedBy(24.dp)
    ) {
        LexNovaTabs(
            activeTab = uiState.activeTab,
            onTabSelected = { viewModel.updateTab(it) }
        )

            AnimatedContent(targetState = uiState.activeTab) { tab ->
                when (tab) {
                    "Command Center" -> LexNovaCommandCenter(uiState)
                    "Knowledge & Intelligence" -> LexNovaKnowledge(uiState, onNavigate)
                    "Careers" -> LexNovaCareers(onNavigate)
                    else -> LexNovaEmptyState(tab)
                }
            }
            
            Spacer(modifier = Modifier.height(20.dp))
    }
}

@Composable
fun LexNovaTabs(activeTab: String, onTabSelected: (String) -> Unit) {
    val tabs = listOf("Command Center", "Knowledge & Intelligence", "Careers")
    LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        items(tabs) { tab ->
            val isSelected = activeTab == tab
            FilterChip(
                selected = isSelected,
                onClick = { onTabSelected(tab) },
                label = { Text(tab) }
            )
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun LexNovaCommandCenter(state: LexNovaState) {
    Column(verticalArrangement = Arrangement.spacedBy(24.dp)) {
        // KPIs
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(16.dp)) {
            state.kpis.take(2).forEach { kpi ->
                LexNovaKPIItem(kpi, modifier = Modifier.weight(1f))
            }
        }

        LexNovaTimetable(state.timetable)
        
        LexNovaDigitalID()
    }
}

@Composable
fun LexNovaKPIItem(kpi: LexNovaKPI, modifier: Modifier = Modifier) {
    CamsCard(modifier = modifier) {
        Column {
            Icon(kpi.icon, contentDescription = null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(24.dp))
            Spacer(modifier = Modifier.height(12.dp))
            Text(kpi.value, style = MaterialTheme.typography.headlineSmall.copy(fontWeight = FontWeight.Black), color = MaterialTheme.colorScheme.onSurface)
            Text(kpi.label.uppercase(), style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold, letterSpacing = 1.sp), color = CamsNavy)
            Text(kpi.subText, style = MaterialTheme.typography.labelSmall.copy(fontSize = 12.sp), color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
    }
}

@Composable
fun LexNovaTimetable(timetable: List<TimetableEntry>, modifier: Modifier = Modifier) {
    CamsCard(modifier = modifier) {
        Column {
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.SpaceBetween, modifier = Modifier.fillMaxWidth()) {
                Text("Academic Command Center", style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold), color = MaterialTheme.colorScheme.onSurface)
                Surface(shape = RoundedCornerShape(4.dp), color = CamsBackground) {
                    Text("Fall 2026", modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp), style = MaterialTheme.typography.labelSmall.copy(fontSize = 12.sp, color = CamsNavy))
                }
            }
            Spacer(modifier = Modifier.height(20.dp))
            if (timetable.isEmpty()) {
                Box(modifier = Modifier.fillMaxWidth().padding(16.dp), contentAlignment = Alignment.Center) {
                    Text("No classes scheduled.", style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            } else {
                timetable.forEach { entry ->
                    Row(modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp), verticalAlignment = Alignment.CenterVertically) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.width(60.dp)) {
                            Text(entry.time.split(" - ")[0], style = MaterialTheme.typography.labelSmall.copy(fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant))
                            Box(modifier = Modifier.width(1.dp).height(12.dp).background(Color.LightGray))
                            Text(entry.time.split(" - ")[1], style = MaterialTheme.typography.labelSmall.copy(fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant))
                        }
                        Spacer(modifier = Modifier.width(12.dp))
                        Surface(
                            shape = RoundedCornerShape(12.dp),
                            color = if (entry.isLive) CamsNavy.copy(alpha = 0.05f) else CamsBackground,
                            modifier = Modifier.weight(1f)
                        ) {
                            Row(modifier = Modifier.padding(12.dp), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.SpaceBetween) {
                                Column {
                                    Text(entry.course, style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.Bold), color = MaterialTheme.colorScheme.onSurface)
                                    Text("${entry.professor} • ${entry.room}", style = MaterialTheme.typography.labelSmall.copy(fontSize = 13.sp), color = MaterialTheme.colorScheme.onSurfaceVariant)
                                }
                                if (entry.isLive) {
                                    Text("LIVE", style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Black, color = Color(0xFFF43F5E)))
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun LexNovaDigitalID(modifier: Modifier = Modifier) {
    CamsCard(modifier = modifier) {
        Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.fillMaxWidth()) {
            Text("Digital Identity Hub", style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.Bold), color = MaterialTheme.colorScheme.onSurface)
            Spacer(modifier = Modifier.height(20.dp))
            Box(modifier = Modifier.size(100.dp).background(CamsBackground, RoundedCornerShape(8.dp)).padding(8.dp)) {
                Icon(Icons.Filled.QrCode, contentDescription = null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.fillMaxSize())
            }
            Spacer(modifier = Modifier.height(12.dp))
            Text("Arjun Mehta", style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Black), color = MaterialTheme.colorScheme.onSurface)
            Text("ID: LNS-2024-8932", style = MaterialTheme.typography.labelSmall.copy(color = CamsNavy))
            Spacer(modifier = Modifier.height(20.dp))
            Surface(shape = RoundedCornerShape(12.dp), color = Color(0xFFECFDF5)) {
                Row(modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp), verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Filled.CheckCircle, contentDescription = null, tint = Color(0xFF10B981), modifier = Modifier.size(12.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("VERIFIED", style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold, color = Color(0xFF10B981)))
                }
            }
        }
    }
}

@Composable
fun LexNovaKnowledge(state: LexNovaState, onNavigate: (String) -> Unit) {
    Column(verticalArrangement = Arrangement.spacedBy(24.dp)) {
        // Legal Skills Entry
        CamsCard(
            onClick = { onNavigate("/student/legal-skills") }
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Surface(modifier = Modifier.size(56.dp), shape = RoundedCornerShape(16.dp), color = CamsNavy) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(Icons.Filled.WorkspacePremium, contentDescription = null, tint = Color.White, modifier = Modifier.size(28.dp))
                    }
                }
                Spacer(modifier = Modifier.width(20.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Text("Legal Skills & Certifications", style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Black), color = MaterialTheme.colorScheme.onSurface)
                    Text("LMS for specialized law courses", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
                Icon(Icons.AutoMirrored.Filled.ArrowForward, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
            }
        }

        // AI Mentor
        CamsCard(modifier = Modifier.imePadding()) {
            Column {
                Row(modifier = Modifier.fillMaxWidth().background(CamsNavy.copy(alpha = 0.05f)).padding(16.dp), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.SpaceBetween) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Surface(shape = RoundedCornerShape(8.dp), color = CamsNavy.copy(alpha = 0.1f)) {
                            Icon(Icons.Filled.AutoAwesome, contentDescription = null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.padding(4.dp).size(18.dp))
                        }
                        Spacer(modifier = Modifier.width(12.dp))
                        Column {
                            Text("LexNova AI Mentor", style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.Bold), color = MaterialTheme.colorScheme.onSurface)
                            Text("RESEARCH ASSISTANT", style = MaterialTheme.typography.labelSmall.copy(fontSize = 12.sp, color = CamsNavy))
                        }
                    }
                    Text("ONLINE", style = MaterialTheme.typography.labelSmall.copy(fontSize = 12.sp, color = Color(0xFF10B981)))
                }
                
                Box(modifier = Modifier.height(200.dp).padding(16.dp)) {
                    Text("How can I assist your legal research today?", color = MaterialTheme.colorScheme.onSurfaceVariant, style = MaterialTheme.typography.bodySmall)
                }

                Row(modifier = Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
                    TextField(
                        value = "",
                        onValueChange = {},
                        placeholder = { Text("Ask your AI Mentor...", fontSize = 12.sp) },
                        modifier = Modifier.weight(1f),
                        colors = TextFieldDefaults.colors(
                            unfocusedContainerColor = CamsBackground,
                            focusedContainerColor = CamsBackground,
                            unfocusedIndicatorColor = Color.Transparent,
                            focusedIndicatorColor = Color.Transparent
                        ),
                        shape = RoundedCornerShape(12.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    IconButton(onClick = {}, modifier = Modifier.background(CamsNavy, RoundedCornerShape(12.dp))) {
                        Icon(Icons.AutoMirrored.Filled.Send, contentDescription = null, tint = Color.White)
                    }
                }
            }
        }

        // Documents
        CamsCard {
            Column {
                Text("Knowledge Vault", style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.Bold), color = MaterialTheme.colorScheme.onSurface)
                Spacer(modifier = Modifier.height(20.dp))
                if (state.documents.isEmpty()) {
                    Box(modifier = Modifier.fillMaxWidth().padding(16.dp), contentAlignment = Alignment.Center) {
                        Text("No documents available in vault.", style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                } else {
                    state.documents.forEach { doc ->
                        Row(modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp), verticalAlignment = Alignment.CenterVertically) {
                            Surface(shape = RoundedCornerShape(8.dp), color = CamsBackground, modifier = Modifier.size(40.dp)) {
                                Box(contentAlignment = Alignment.Center) {
                                    Icon(doc.icon, contentDescription = null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(20.dp))
                                }
                            }
                            Spacer(modifier = Modifier.width(12.dp))
                            Column(modifier = Modifier.weight(1f)) {
                                Text(doc.title, style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.Bold), color = MaterialTheme.colorScheme.onSurface)
                                Text("${doc.author} • ${doc.size}", style = MaterialTheme.typography.labelSmall.copy(fontSize = 13.sp), color = MaterialTheme.colorScheme.onSurfaceVariant)
                            }
                            Icon(Icons.Filled.Download, contentDescription = null, tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f), modifier = Modifier.size(16.dp))
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun LexNovaCareers(onNavigate: (String) -> Unit) {
    Column(verticalArrangement = Arrangement.spacedBy(24.dp)) {
        CamsCard(
            onClick = { onNavigate("/student/lexsphere") }
        ) {
            Column {
                Surface(modifier = Modifier.size(64.dp), shape = RoundedCornerShape(16.dp), color = Color(0xFF10B981).copy(alpha = 0.1f)) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(Icons.Filled.Language, contentDescription = null, tint = Color(0xFF10B981), modifier = Modifier.size(32.dp))
                    }
                }
                Spacer(modifier = Modifier.height(24.dp))
                Text("LexSphere Internship Portal", style = MaterialTheme.typography.headlineSmall.copy(fontWeight = FontWeight.Black), color = MaterialTheme.colorScheme.onSurface)
                Text(
                    "The premier enterprise platform connecting law students with Tier-1 firms and global legal opportunities.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(modifier = Modifier.height(32.dp))
                Button(
                    onClick = { onNavigate("/student/lexsphere") },
                    modifier = Modifier.fillMaxWidth().height(56.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF10B981)),
                    shape = RoundedCornerShape(16.dp)
                ) {
                    Text("Enter Portal", style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.Black))
                }
            }
        }
        
        // Stats
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(16.dp)) {
            LexNovaKPIItem(LexNovaKPI("Active Drives", "450+", "Across India", Icons.Filled.Work, Color(0xFF3B82F6)), modifier = Modifier.weight(1f))
            LexNovaKPIItem(LexNovaKPI("Law Students", "12k+", "In Network", Icons.Filled.Group, Color(0xFFF59E0B)), modifier = Modifier.weight(1f))
        }
    }
}

@Composable
fun LexNovaEmptyState(tab: String) {
    CamsCard(modifier = Modifier.fillMaxWidth()) {
        Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.padding(vertical = 40.dp)) {
            Icon(Icons.Filled.Scale, contentDescription = null, modifier = Modifier.size(48.dp), tint = MaterialTheme.colorScheme.primary.copy(alpha = 0.1f))
            Spacer(modifier = Modifier.height(16.dp))
            Text("Module Category: $tab", style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold), color = MaterialTheme.colorScheme.onSurface)
            Text(
                "These premium enterprise modules are seamlessly integrated into the LexNova ecosystem.",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center,
                modifier = Modifier.padding(horizontal = 40.dp)
            )
        }
    }
}
