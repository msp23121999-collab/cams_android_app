package com.example.features.career.screens

import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.compose.ui.draw.clip
import com.example.core.theme.LexNovaPurple
import com.example.core.ui.shimmerEffect
import com.example.features.career.providers.CareerViewModel
import com.example.features.student.widgets.StudentDrawer
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ActivityPointsScreen(
    viewModel: CareerViewModel = viewModel(),
    onNavigate: (String) -> Unit
) {
    val uiState by viewModel.uiState.collectAsState()
    val drawerState = rememberDrawerState(initialValue = DrawerValue.Closed)
    val scope = rememberCoroutineScope()

    ModalNavigationDrawer(
        drawerState = drawerState,
        drawerContent = {
            StudentDrawer(
                currentRoute = "/career/activity_points",
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
                    title = { Text("Activity Points") },
                    navigationIcon = {
                        IconButton(onClick = { scope.launch { drawerState.open() } }) {
                            Icon(Icons.Filled.Menu, contentDescription = "Menu")
                        }
                    },
                    colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.Transparent)
                )
            },
            floatingActionButton = {
                FloatingActionButton(onClick = { /* Apply for points */ }, containerColor = LexNovaPurple) {
                    Icon(Icons.Filled.Add, contentDescription = "Apply for points", tint = Color.White)
                }
            },
            containerColor = MaterialTheme.colorScheme.background
        ) { paddingValues ->
            Box(modifier = Modifier.fillMaxSize().padding(paddingValues)) {
                if (uiState.isLoading) {
                    ActivityPointsSkeleton()
                } else {
                    Column(modifier = Modifier.fillMaxSize()) {
                        // Stats Dashboard
                        Row(
                            modifier = Modifier.fillMaxWidth().padding(16.dp),
                            horizontalArrangement = Arrangement.spacedBy(16.dp)
                        ) {
                            val totalPoints = uiState.activityClaims.filter { it.status == "Approved" }.sumOf { it.pointsClaimed }
                            val pendingPoints = uiState.activityClaims.filter { it.status == "Pending" }.sumOf { it.pointsClaimed }
                            
                            StatCard(title = "Total Earned", value = "$totalPoints", color = Color(0xFF10B981), modifier = Modifier.weight(1f))
                            StatCard(title = "Pending", value = "$pendingPoints", color = Color(0xFFF59E0B), modifier = Modifier.weight(1f))
                        }
                        
                        // Data Table
                        Card(
                            modifier = Modifier.fillMaxSize().padding(16.dp),
                            shape = RoundedCornerShape(16.dp),
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                        ) {
                            Column(modifier = Modifier.fillMaxSize()) {
                                Text(
                                    "Applications Log",
                                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                                    modifier = Modifier.padding(16.dp)
                                )
                                HorizontalDivider()
                                
                                val scrollState = rememberScrollState()
                                
                                // Header Row
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .horizontalScroll(scrollState)
                                        .padding(horizontal = 16.dp, vertical = 12.dp),
                                    horizontalArrangement = Arrangement.spacedBy(16.dp)
                                ) {
                                    Text("Activity", modifier = Modifier.width(200.dp), fontWeight = FontWeight.Bold)
                                    Text("Category", modifier = Modifier.width(120.dp), fontWeight = FontWeight.Bold)
                                    Text("Date", modifier = Modifier.width(100.dp), fontWeight = FontWeight.Bold)
                                    Text("Points", modifier = Modifier.width(80.dp), fontWeight = FontWeight.Bold)
                                    Text("Status", modifier = Modifier.width(100.dp), fontWeight = FontWeight.Bold)
                                }
                                HorizontalDivider()

                                LazyColumn {
                                    items(uiState.activityClaims) { claim ->
                                        Row(
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .horizontalScroll(scrollState)
                                                .padding(horizontal = 16.dp, vertical = 16.dp),
                                            horizontalArrangement = Arrangement.spacedBy(16.dp),
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            Text(claim.activityName, modifier = Modifier.width(200.dp), style = MaterialTheme.typography.bodyMedium)
                                            Text(claim.category, modifier = Modifier.width(120.dp), style = MaterialTheme.typography.bodyMedium)
                                            Text(claim.date, modifier = Modifier.width(100.dp), style = MaterialTheme.typography.bodyMedium)
                                            Text("${claim.pointsClaimed}", modifier = Modifier.width(80.dp), style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold))
                                            
                                            val statusColor = when (claim.status) {
                                                "Approved" -> Color(0xFF10B981)
                                                "Pending" -> Color(0xFFF59E0B)
                                                else -> Color(0xFF64748B)
                                            }
                                            Surface(
                                                color = statusColor.copy(alpha = 0.1f),
                                                shape = RoundedCornerShape(8.dp),
                                                modifier = Modifier.width(100.dp)
                                            ) {
                                                Text(
                                                    text = claim.status,
                                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                                                    style = MaterialTheme.typography.labelSmall.copy(color = statusColor, fontWeight = FontWeight.Bold)
                                                )
                                            }
                                        }
                                        HorizontalDivider(color = Color.LightGray.copy(alpha = 0.3f))
                                    }
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
fun StatCard(title: String, value: String, color: Color, modifier: Modifier = Modifier) {
    Card(
        modifier = modifier,
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Text(text = title, style = MaterialTheme.typography.labelMedium.copy(color = Color(0xFF64748B)))
            Text(text = value, style = MaterialTheme.typography.headlineMedium.copy(color = color, fontWeight = FontWeight.Bold))
        }
    }
}

@Composable
private fun ActivityPointsSkeleton() {
    Column(modifier = Modifier.fillMaxSize()) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(16.dp),
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Box(Modifier.weight(1f).height(100.dp).shimmerEffect().clip(RoundedCornerShape(12.dp)))
            Box(Modifier.weight(1f).height(100.dp).shimmerEffect().clip(RoundedCornerShape(12.dp)))
        }
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp)
                .shimmerEffect()
                .clip(RoundedCornerShape(16.dp))
        )
    }
}
