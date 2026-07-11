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
fun PrincipalStudyMaterialsScreen(onNavigate: (String) -> Unit) {
    PrincipalBaseScreen(
        title = "Study Material Approvals",
        subtitle = "Review, approve, or reject and return learning resources uploaded by law faculty members.",
        currentRoute = AppRoutes.PRINCIPAL_STUDY_MATERIALS,
        onNavigate = onNavigate
    ) {
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.fillMaxWidth()) {
            KpiCard("Pending Validation", "12", Icons.Filled.MenuBook, Color(0xFF3B82F6), Modifier.weight(1f))
            KpiCard("Lecture Notes", "8", Icons.Filled.Description, Color(0xFFF59E0B), Modifier.weight(1f))
            KpiCard("Case Studies", "3", Icons.Filled.Layers, Color(0xFF10B981), Modifier.weight(1f))
            KpiCard("Question Banks", "1", Icons.Filled.FactCheck, Color(0xFFEF4444), Modifier.weight(1f))
        }
        
        Spacer(modifier = Modifier.height(16.dp))

        CamsCard(modifier = Modifier.fillMaxWidth().weight(1f)) {
            Text("Pending Validation Inbox", fontWeight = FontWeight.Bold, fontSize = 16.sp, color = CamsTextPrimary)
            
            Spacer(Modifier.height(12.dp))

            LazyColumn(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                items(5) { i ->
                    Row(
                        modifier = Modifier.fillMaxWidth().background(MaterialTheme.colorScheme.surface, RoundedCornerShape(12.dp)).border(1.dp, MaterialTheme.colorScheme.outlineVariant, RoundedCornerShape(12.dp)).padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(Modifier.background(Color(0xFFF3E8FF), RoundedCornerShape(8.dp)).padding(12.dp)) {
                            Text("FA", fontWeight = FontWeight.Bold, color = Color(0xFF6D28D9))
                        }
                        Spacer(Modifier.width(16.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Text("Constitutional Law Notes Unit ${i+1}", fontWeight = FontWeight.Bold, fontSize = 14.sp, color = CamsTextPrimary)
                            Text("Fundamental Rights & Duties", fontSize = 12.sp, color = CamsTextSecondary)
                            Spacer(Modifier.height(8.dp))
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text("Dr. A. Sharma", fontWeight = FontWeight.Bold, fontSize = 12.sp, color = CamsTextPrimary)
                                Spacer(Modifier.width(8.dp))
                                Text("LECTURE NOTES", fontSize = 13.sp, fontWeight = FontWeight.Bold, color = CamsTextSecondary, modifier = Modifier.background(MaterialTheme.colorScheme.surfaceVariant, RoundedCornerShape(4.dp)).padding(horizontal = 4.dp, vertical = 2.dp))
                            }
                        }
                        Button(onClick = { }, colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFEEF2FF)), shape = RoundedCornerShape(8.dp)) {
                            Icon(Icons.Filled.Visibility, null, tint = Color(0xFF4F46E5), modifier = Modifier.size(16.dp))
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
