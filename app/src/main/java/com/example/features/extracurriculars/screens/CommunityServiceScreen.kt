package com.example.features.extracurriculars.screens

import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.core.theme.LexNovaPurple
import com.example.features.extracurriculars.providers.ExtracurricularsViewModel
import com.example.features.student.widgets.StudentDrawer
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CommunityServiceScreen(
    viewModel: ExtracurricularsViewModel = viewModel(),
    onNavigate: (String) -> Unit
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val drawerState = rememberDrawerState(initialValue = DrawerValue.Closed)
    val scope = rememberCoroutineScope()

    ModalNavigationDrawer(
        drawerState = drawerState,
        drawerContent = {
            StudentDrawer(
                currentRoute = "/student/community_service",
                onNavigate = {
                    scope.launch { drawerState.close() }
                    onNavigate(it)
                }
            )
        }
    ) {
        Scaffold(
            topBar = {
                TopAppBar(
                    title = { Text("Community Service") },
                    navigationIcon = {
                        IconButton(onClick = { scope.launch { drawerState.open() } }) {
                            Icon(Icons.Filled.Menu, contentDescription = "Menu")
                        }
                    },
                    colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.Transparent)
                )
            },
            containerColor = MaterialTheme.colorScheme.background
        ) { paddingValues ->
            BoxWithConstraints(modifier = Modifier.padding(paddingValues)) {
                val isTablet = maxWidth > 600.dp
                
                if (uiState.isLoading) {
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        CircularProgressIndicator(color = LexNovaPurple)
                    }
                } else {
                    if (isTablet) {
                        Column(modifier = Modifier.fillMaxSize().padding(16.dp)) {
                            CommunityServiceStats()
                            Spacer(modifier = Modifier.height(24.dp))
                            Row(modifier = Modifier.weight(1f), horizontalArrangement = Arrangement.spacedBy(24.dp)) {
                                Column(modifier = Modifier.weight(1f)) {
                                    Text("Upcoming Opportunities", style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold))
                                    Spacer(modifier = Modifier.height(16.dp))
                                    OpportunitiesList(uiState.serviceOpportunities)
                                }
                                Column(modifier = Modifier.weight(1f)) {
                                    Text("Recent Service Log", style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold))
                                    Spacer(modifier = Modifier.height(16.dp))
                                    ServiceTimeline(uiState.serviceLogs)
                                }
                            }
                        }
                    } else {
                        LazyColumn(modifier = Modifier.fillMaxSize().padding(16.dp)) {
                            item {
                                CommunityServiceStats()
                                Spacer(modifier = Modifier.height(24.dp))
                            }
                            item {
                                Text("Recent Service Log", style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold))
                                Spacer(modifier = Modifier.height(16.dp))
                            }
                            item {
                                ServiceTimeline(uiState.serviceLogs)
                                Spacer(modifier = Modifier.height(32.dp))
                            }
                            item {
                                Text("Upcoming Opportunities", style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold))
                                Spacer(modifier = Modifier.height(16.dp))
                            }
                            items(uiState.serviceOpportunities) { opportunity ->
                                OpportunityCard(opportunity)
                                Spacer(modifier = Modifier.height(16.dp))
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun CommunityServiceStats() {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .horizontalScroll(rememberScrollState())
            .padding(vertical = 8.dp),
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        val stats = listOf(
            Triple("Service Hours", "25 hrs", Color(0xFF10B981)),
            Triple("NGO Collabs", "4", Color(0xFF3B82F6)),
            Triple("Legal Aid Camps", "2", Color(0xFFF59E0B)),
            Triple("Certificates", "3", Color(0xFF8B5CF6))
        )
        stats.forEach { (label, value, color) ->
            Card(
                modifier = Modifier.width(150.dp),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(text = label, style = MaterialTheme.typography.labelMedium.copy(color = Color(0xFF64748B)))
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(text = value, style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold, color = color))
                }
            }
        }
    }
}

@Composable
fun OpportunitiesList(opportunities: List<com.example.features.extracurriculars.models.ServiceOpportunity>) {
    LazyColumn(verticalArrangement = Arrangement.spacedBy(16.dp)) {
        items(opportunities) { opportunity ->
            OpportunityCard(opportunity)
        }
    }
}

@Composable
fun OpportunityCard(opportunity: com.example.features.extracurriculars.models.ServiceOpportunity) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = Color(0xFF10B981).copy(alpha = 0.1f),
                    modifier = Modifier.size(48.dp)
                ) {
                    Icon(Icons.Filled.VolunteerActivism, contentDescription = null, tint = Color(0xFF10B981), modifier = Modifier.padding(12.dp))
                }
                Spacer(modifier = Modifier.width(16.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Text(text = opportunity.title, style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold))
                    Text(text = opportunity.ngoName, style = MaterialTheme.typography.bodyMedium.copy(color = Color(0xFF64748B)))
                }
            }
            Spacer(modifier = Modifier.height(16.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                Text(text = "Date: ${opportunity.date}", style = MaterialTheme.typography.bodySmall.copy(color = Color(0xFF64748B)))
                Text(text = "Spots: ${opportunity.spotsAvailable}", style = MaterialTheme.typography.bodySmall.copy(color = Color(0xFF64748B)))
            }
            Spacer(modifier = Modifier.height(16.dp))
            Button(
                onClick = { /* Apply */ },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF10B981))
            ) {
                Text("Apply Now")
            }
        }
    }
}

@Composable
fun ServiceTimeline(logs: List<com.example.features.extracurriculars.models.ServiceLogEntry>) {
    Column {
        logs.forEachIndexed { index, log ->
            val isVerified = log.status == "Verified"
            val color = if (isVerified) Color(0xFF10B981) else Color(0xFFF59E0B)
            val icon = if (isVerified) Icons.Filled.CheckCircle else Icons.Filled.Pending
            
            Row(modifier = Modifier.height(IntrinsicSize.Min)) {
                Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.width(40.dp)) {
                    Icon(icon, contentDescription = null, tint = color, modifier = Modifier.size(24.dp).padding(top = 4.dp))
                    if (index < logs.lastIndex) {
                        Canvas(modifier = Modifier.fillMaxHeight().width(2.dp).padding(vertical = 4.dp)) {
                            drawLine(
                                color = Color.LightGray,
                                start = Offset(size.width/2, 0f),
                                end = Offset(size.width/2, size.height),
                                strokeWidth = 2.dp.toPx()
                            )
                        }
                    }
                }
                Column(modifier = Modifier.padding(bottom = 24.dp, start = 8.dp)) {
                    Text(text = log.title, style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold))
                    Text(text = "${log.date} • ${log.hours} hours", style = MaterialTheme.typography.bodyMedium.copy(color = Color(0xFF64748B)))
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(text = log.status, style = MaterialTheme.typography.labelSmall.copy(color = color, fontWeight = FontWeight.Bold))
                }
            }
        }
    }
}
