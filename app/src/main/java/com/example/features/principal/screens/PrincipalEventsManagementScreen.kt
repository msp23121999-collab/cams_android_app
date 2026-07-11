package com.example.features.principal.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
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
fun PrincipalEventsManagementScreen(onNavigate: (String) -> Unit) {
    PrincipalBaseScreen(
        title = "Publish Legal & Campus Events",
        subtitle = "Publish guest lectures, debates, workshops, and inter-college contests directly to the Student Portal.",
        currentRoute = AppRoutes.PRINCIPAL_EVENTS_MGMT,
        onNavigate = onNavigate
    ) {
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.fillMaxWidth()) {
            KpiCard("Total Events", "24", Icons.Filled.Event, Color(0xFF6366F1), Modifier.weight(1f))
            KpiCard("Intra-College", "15", Icons.Filled.Business, Color(0xFF10B981), Modifier.weight(1f))
            KpiCard("Inter-College", "9", Icons.Filled.Public, Color(0xFF0EA5E9), Modifier.weight(1f))
            KpiCard("Live Now", "2", Icons.Filled.Sensors, Color(0xFFEF4444), Modifier.weight(1f))
        }
        
        Spacer(modifier = Modifier.height(16.dp))

        CamsCard(modifier = Modifier.fillMaxWidth().weight(1f)) {
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                Text("Campus Events", fontWeight = FontWeight.Bold, fontSize = 16.sp, color = CamsTextPrimary)
                Button(onClick = { }, colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF4F46E5)), shape = RoundedCornerShape(8.dp)) {
                    Icon(Icons.Filled.Add, null, modifier = Modifier.size(16.dp))
                    Spacer(Modifier.width(4.dp))
                    Text("Publish Event", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                }
            }
            
            Spacer(Modifier.height(12.dp))

            LazyColumn(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                items(5) { i ->
                    val isInter = i % 2 == 0
                    Row(
                        modifier = Modifier.fillMaxWidth().background(MaterialTheme.colorScheme.surface, RoundedCornerShape(12.dp)).border(1.dp, MaterialTheme.colorScheme.outlineVariant, RoundedCornerShape(12.dp)).padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Row(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalAlignment = Alignment.CenterVertically) {
                                Text(
                                    if(isInter) "🌍 Inter-College" else "🏫 Intra-College", 
                                    fontSize = 13.sp, 
                                    fontWeight = FontWeight.Bold, 
                                    color = if(isInter) Color(0xFF0369A1) else Color(0xFF047857), 
                                    modifier = Modifier.background(if(isInter) Color(0xFFE0F2FE) else Color(0xFFD1FAE5), RoundedCornerShape(4.dp)).padding(horizontal = 6.dp, vertical = 2.dp)
                                )
                                Text("Constitutional Law", fontSize = 13.sp, fontWeight = FontWeight.Bold, color = CamsTextSecondary, modifier = Modifier.background(MaterialTheme.colorScheme.surfaceVariant, RoundedCornerShape(4.dp)).border(1.dp, MaterialTheme.colorScheme.outlineVariant, RoundedCornerShape(4.dp)).padding(horizontal = 6.dp, vertical = 2.dp))
                            }
                            Spacer(Modifier.height(8.dp))
                            Text("National Moot Court Championship 2026", fontWeight = FontWeight.Bold, fontSize = 14.sp, color = CamsTextPrimary)
                            Text("Organized by: CAMS Law College", fontSize = 12.sp, color = CamsTextSecondary, fontWeight = FontWeight.Bold)
                            Spacer(Modifier.height(4.dp))
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.Filled.Gavel, null, tint = Color(0xFF8B5CF6), modifier = Modifier.size(12.dp))
                                Spacer(Modifier.width(4.dp))
                                Text("Dr. Ramesh Kumar (Supreme Court Judge)", fontSize = 12.sp, color = CamsTextPrimary, fontWeight = FontWeight.Bold)
                            }
                        }
                        Column(horizontalAlignment = Alignment.End) {
                            Text(
                                "REGISTRATION OPEN", 
                                fontSize = 12.sp, 
                                fontWeight = FontWeight.Bold, 
                                color = Color(0xFF065F46), 
                                modifier = Modifier.background(Color(0xFFD1FAE5), RoundedCornerShape(12.dp)).padding(horizontal = 8.dp, vertical = 4.dp)
                            )
                            Spacer(Modifier.height(8.dp))
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.Filled.AccessTime, null, tint = CamsTextSecondary, modifier = Modifier.size(12.dp))
                                Spacer(Modifier.width(4.dp))
                                Text("20 Jun 2026", fontSize = 13.sp, color = CamsTextSecondary, fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun KpiCard(label: String, value: String, icon: androidx.compose.ui.graphics.vector.ImageVector, color: Color, modifier: Modifier) {
    Card(modifier = modifier, colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface), elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)) {
        Column(modifier = Modifier.padding(12.dp)) {
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.Top) {
                Text(label, fontSize = 13.sp, fontWeight = FontWeight.Black, color = Color(0xFF64748B), modifier = Modifier.weight(1f))
                Box(Modifier.background(color.copy(alpha=0.1f), RoundedCornerShape(8.dp)).padding(4.dp)) {
                    Icon(icon, null, tint = color, modifier = Modifier.size(16.dp))
                }
            }
            Text(value, fontSize = 20.sp, fontWeight = FontWeight.Black, color = CamsTextPrimary, modifier = Modifier.padding(top = 8.dp))
        }
    }
}
