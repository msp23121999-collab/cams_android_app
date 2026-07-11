package com.example.features.hod.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
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
import com.example.features.hod.widgets.HODBaseScreen
import com.example.core.navigation.AppRoutes

@Composable
fun HODStudyMaterialVerificationScreen(onNavigate: (String) -> Unit) {
    var activeTab by remember { mutableStateOf("review") }

    HODBaseScreen(
        title = "Study Materials Verification",
        subtitle = "Review academic notes, lecture slides, and case study files",
        currentRoute = "/hod/study-materials",
        onNavigate = onNavigate
    ) {
        ScrollableTabRow(
            selectedTabIndex = listOf("review", "verification", "revisions", "audit").indexOf(activeTab).coerceAtLeast(0),
            containerColor = Color.Transparent,
            contentColor = CamsNavy,
            edgePadding = 0.dp,
            divider = {}
        ) {
            val tabs = listOf(
                "review" to "Review Registry",
                "verification" to "Verification Console",
                "revisions" to "Revision Directives",
                "audit" to "System Audit Trail"
            )
            tabs.forEach { (id, label) ->
                Tab(
                    selected = activeTab == id,
                    onClick = { activeTab = id },
                    text = { Text(label, fontWeight = if (activeTab == id) FontWeight.Bold else FontWeight.Medium) }
                )
            }
        }
        
        Spacer(modifier = Modifier.height(16.dp))

        if (activeTab == "review") {
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.fillMaxWidth()) {
                KpiCard("Total Submitted", "120", Icons.Filled.Layers, Color(0xFF64748B), Modifier.weight(1f))
                KpiCard("Pending Review", "15", Icons.Filled.Inbox, Color(0xFF8B5CF6), Modifier.weight(1f))
                KpiCard("Approved", "95", Icons.Filled.CheckCircle, Color(0xFF10B981), Modifier.weight(1f))
                KpiCard("Rejected", "10", Icons.Filled.Cancel, Color(0xFFF43F5E), Modifier.weight(1f))
            }
            
            Spacer(modifier = Modifier.height(16.dp))

            CamsCard(modifier = Modifier.fillMaxWidth().weight(1f)) {
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                    Column {
                        Text("Syllabus Study Materials Directory", fontWeight = FontWeight.Bold, fontSize = 16.sp, color = CamsTextPrimary)
                        Text("Review academic notes, lecture slides, and case study files submitted by law faculty.", fontSize = 12.sp, color = CamsTextSecondary)
                    }
                    Button(onClick = { }, colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF4F46E5)), shape = RoundedCornerShape(8.dp)) {
                        Icon(Icons.Filled.Sync, null, modifier = Modifier.size(16.dp))
                        Spacer(Modifier.width(4.dp))
                        Text("Sync Data", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                    }
                }
                
                Spacer(Modifier.height(16.dp))

                LazyColumn(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    items(5) { i ->
                        Row(
                            modifier = Modifier.fillMaxWidth().background(MaterialTheme.colorScheme.surface, RoundedCornerShape(12.dp)).border(1.dp, MaterialTheme.colorScheme.outlineVariant, RoundedCornerShape(12.dp)).padding(12.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Box(modifier = Modifier.size(40.dp).background(Color(0xFFEEF2FF), RoundedCornerShape(8.dp)), contentAlignment = Alignment.Center) {
                                Icon(Icons.Filled.Description, null, tint = Color(0xFF4F46E5))
                            }
                            Spacer(Modifier.width(12.dp))
                            Column(modifier = Modifier.weight(1f)) {
                                Text("Constitutional Law - Unit ${i+1}", fontWeight = FontWeight.Bold, fontSize = 14.sp, color = CamsTextPrimary)
                                Text("Prof. Smith • Sem 1 • PDF", fontSize = 12.sp, color = CamsTextSecondary)
                            }
                            val isPending = i < 2
                            Text(
                                if(isPending) "PENDING" else "APPROVED", 
                                fontSize = 12.sp, 
                                fontWeight = FontWeight.Bold, 
                                color = if(isPending) Color(0xFFD97706) else Color(0xFF059669), 
                                modifier = Modifier.background(if(isPending) Color(0xFFFEF3C7) else Color(0xFFD1FAE5), RoundedCornerShape(4.dp)).padding(horizontal = 8.dp, vertical = 4.dp)
                            )
                        }
                    }
                }
            }
        } else {
            CamsCard(modifier = Modifier.fillMaxWidth().weight(1f)) {
                Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Text("$activeTab content goes here", color = CamsTextSecondary)
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
