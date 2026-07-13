package com.example.features.principal.screens

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

@Composable
fun PrincipalInfrastructureScreen(onNavigate: (String) -> Unit) {
    PrincipalBaseScreen(
        title = "Campus Infrastructure Management",
        subtitle = "Define, structure, and visualize floors, classroom coordinates, and administrative blueprints.",
        currentRoute = AppRoutes.PRINCIPAL_INFRASTRUCTURE,
        onNavigate = onNavigate
    ) {
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            // Buildings Sidebar
            Card(modifier = Modifier.weight(1f).height(120.dp), colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface), border = BorderStroke(1.dp, Color(0xFF8B5CF6)), elevation = CardDefaults.cardElevation(2.dp)) {
                Row(Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
                    Box(Modifier.background(Color(0xFFEEF2FF), RoundedCornerShape(8.dp)).padding(8.dp)) {
                        Icon(Icons.Filled.Domain, null, tint = Color(0xFF4F46E5))
                    }
                    Spacer(Modifier.width(12.dp))
                    Column {
                        Text("Main Academic Block", fontWeight = FontWeight.Bold, color = CamsTextPrimary)
                        Text("MAB", fontSize = 12.sp, color = CamsTextSecondary)
                        Spacer(Modifier.height(8.dp))
                        Text("3 Floors • 15 Rooms", fontSize = 12.sp, color = CamsTextSecondary, modifier = Modifier.background(MaterialTheme.colorScheme.surfaceVariant, RoundedCornerShape(4.dp)).padding(horizontal = 6.dp, vertical = 2.dp))
                    }
                }
            }
            Card(modifier = Modifier.weight(1f).height(120.dp), colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface), border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)) {
                Row(Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
                    Box(Modifier.background(MaterialTheme.colorScheme.surfaceVariant, RoundedCornerShape(8.dp)).padding(8.dp)) {
                        Icon(Icons.Filled.Domain, null, tint = CamsTextSecondary)
                    }
                    Spacer(Modifier.width(12.dp))
                    Column {
                        Text("Justice Guild Hall", fontWeight = FontWeight.Bold, color = CamsTextPrimary)
                        Text("JGH", fontSize = 12.sp, color = CamsTextSecondary)
                        Spacer(Modifier.height(8.dp))
                        Text("2 Floors • 8 Rooms", fontSize = 12.sp, color = CamsTextSecondary, modifier = Modifier.background(MaterialTheme.colorScheme.surfaceVariant, RoundedCornerShape(4.dp)).padding(horizontal = 6.dp, vertical = 2.dp))
                    }
                }
            }
            Box(modifier = Modifier.weight(1f), contentAlignment = Alignment.TopEnd) {
                Button(onClick = { }, colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF4F46E5)), shape = RoundedCornerShape(8.dp)) {
                    Icon(Icons.Filled.Add, null, modifier = Modifier.size(16.dp))
                    Spacer(Modifier.width(4.dp))
                    Text("Add Floor", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                }
            }
        }
        
        Spacer(Modifier.height(20.dp))
        
        CamsCard(modifier = Modifier.fillMaxWidth().weight(1f)) {
            Text("Ground Floor", fontWeight = FontWeight.Bold, fontSize = 16.sp, color = CamsTextPrimary)
            Spacer(Modifier.height(16.dp))
            LazyVerticalGrid(
                columns = GridCells.Fixed(3),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                items(3) { i ->
                    val type = if(i==0) "Classroom" else if(i==1) "Office" else "Staff Room"
                    Card(
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)
                    ) {
                        Column(Modifier.padding(16.dp)) {
                            Icon(if(i==0) Icons.Filled.School else if(i==1) Icons.Filled.BusinessCenter else Icons.Filled.Groups, null, tint = Color(0xFF4F46E5))
                            Spacer(Modifier.height(8.dp))
                            Text("Room 10${i+1}", fontWeight = FontWeight.Bold, color = CamsTextPrimary)
                            Text(type, fontSize = 12.sp, color = CamsTextSecondary, fontWeight = FontWeight.Bold)
                            
                            if (i == 0) {
                                Spacer(Modifier.height(8.dp))
                                HorizontalDivider(color = MaterialTheme.colorScheme.surfaceVariant)
                                Spacer(Modifier.height(8.dp))
                                Text("L2", fontSize = 12.sp, color = CamsTextPrimary, fontWeight = FontWeight.Black)
                                Text("Batch 2026-2031", fontSize = 12.sp, color = CamsTextSecondary)
                                Text("1st Year - Section A", fontSize = 12.sp, color = CamsTextSecondary)
                            }
                        }
                    }
                }
            }
        }
    }
}
