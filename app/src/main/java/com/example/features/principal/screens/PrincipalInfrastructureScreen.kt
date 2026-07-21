package com.example.features.principal.screens

import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.core.theme.*
import com.example.core.ui.CamsCard
import com.example.features.principal.widgets.PrincipalBaseScreen
import com.example.core.navigation.AppRoutes
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.features.principal.providers.*

@Composable
fun PrincipalInfrastructureScreen(onNavigate: (String) -> Unit, viewModel: PrincipalInfrastructureViewModel) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    PrincipalBaseScreen(
        title = "Campus Infrastructure Management",
        subtitle = "Define, structure, and visualize floors, classroom coordinates, and administrative blueprints.",
        currentRoute = AppRoutes.PRINCIPAL_INFRASTRUCTURE,
        onNavigate = onNavigate
    ) {
        if (uiState.isLoading) {
            Box(Modifier.fillMaxWidth().padding(32.dp), contentAlignment = Alignment.Center) { CircularProgressIndicator() }
        } else if (uiState.error != null) {
            // Without this the screen rendered empty on failure — no message,
            // no retry — indistinguishable from genuinely having no data.
            com.example.core.ui.NetworkErrorView(
                message = uiState.error ?: "Failed to load infrastructure",
                onRetry = { viewModel.loadInfrastructure() }
            )
        } else if (uiState.data == null || uiState.data!!.buildings.isEmpty()) {
            com.example.core.ui.EnterpriseEmptyState("No Infrastructure Data", "Infrastructure details are not currently available.")
        } else {
            val buildings = uiState.data!!.buildings
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                buildings.take(3).forEach { building ->
                    Card(modifier = Modifier.weight(1f).height(120.dp), colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface), border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)) {
                        Row(Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
                            Box(Modifier.background(MaterialTheme.colorScheme.surfaceVariant, RoundedCornerShape(8.dp)).padding(8.dp)) {
                                Icon(Icons.Filled.Domain, null, tint = Color(0xFF4F46E5))
                            }
                            Spacer(Modifier.width(12.dp))
                            Column {
                                Text(building.name, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurface)
                                Text(building.id.uppercase(), fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                Spacer(Modifier.height(8.dp))
                                Text("${building.floors} Floors • ${building.rooms.size} Rooms", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.background(MaterialTheme.colorScheme.surfaceVariant, RoundedCornerShape(4.dp)).padding(horizontal = 6.dp, vertical = 2.dp))
                            }
                        }
                    }
                }
            }
            
            Spacer(Modifier.height(20.dp))
            
            val firstBuilding = buildings.first()
            CamsCard(modifier = Modifier.fillMaxWidth().weight(1f)) {
                Text("${firstBuilding.name} - Rooms", fontWeight = FontWeight.Bold, fontSize = 16.sp, color = MaterialTheme.colorScheme.onSurface)
                Spacer(Modifier.height(16.dp))
                if (firstBuilding.rooms.isEmpty()) {
                     Text("No rooms configured.", color = MaterialTheme.colorScheme.onSurfaceVariant)
                } else {
                    LazyVerticalGrid(
                        columns = GridCells.Fixed(3),
                        horizontalArrangement = Arrangement.spacedBy(12.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        items(firstBuilding.rooms.size) { i ->
                            val room = firstBuilding.rooms[i]
                            Card(
                                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                                border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)
                            ) {
                                Column(Modifier.padding(16.dp)) {
                                    Icon(if(room.type == "Classroom") Icons.Filled.School else if(room.type == "Lab") Icons.Filled.Computer else Icons.Filled.BusinessCenter, null, tint = Color(0xFF4F46E5))
                                    Spacer(Modifier.height(8.dp))
                                    Text(room.name, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurface)
                                    Text(room.type, fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant, fontWeight = FontWeight.Bold)
                                    
                                    Spacer(Modifier.height(8.dp))
                                    HorizontalDivider(color = MaterialTheme.colorScheme.surfaceVariant)
                                    Spacer(Modifier.height(8.dp))
                                    Text("Capacity: ${room.capacity}", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}
