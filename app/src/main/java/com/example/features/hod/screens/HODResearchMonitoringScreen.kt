package com.example.features.hod.screens

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
import com.example.features.hod.widgets.HODBaseScreen
import com.example.core.navigation.AppRoutes

@Composable
fun HODResearchMonitoringScreen(onNavigate: (String) -> Unit) {
    var activeTab by remember { mutableStateOf("monitoring") }

    HODBaseScreen(
        title = "Research Monitoring & Compliance",
        subtitle = "Track faculty research plans, verify published works, and run compliance scans.",
        currentRoute = "/hod/research-monitoring",
        onNavigate = onNavigate
    ) {
        ScrollableTabRow(
            selectedTabIndex = listOf("monitoring", "verification", "tracker").indexOf(activeTab).coerceAtLeast(0),
            containerColor = Color.Transparent,
            contentColor = CamsNavy,
            edgePadding = 0.dp,
            divider = {}
        ) {
            val tabs = listOf(
                "monitoring" to "Progress Monitoring",
                "verification" to "Pending Verifications",
                "tracker" to "Compliance Tracker"
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
        
        if (activeTab == "monitoring") {
            CamsCard(modifier = Modifier.fillMaxWidth().weight(1f)) {
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                    Text("Faculty Research Progress Logs", fontWeight = FontWeight.Bold, fontSize = 16.sp, color = CamsTextPrimary)
                    Button(onClick = { }, colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF4F46E5)), shape = RoundedCornerShape(8.dp)) {
                        Text("Execute Compliance Scan", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                    }
                }
                Spacer(Modifier.height(16.dp))
                LazyColumn(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    items(5) { i ->
                        Row(
                            modifier = Modifier.fillMaxWidth().background(MaterialTheme.colorScheme.surface, RoundedCornerShape(12.dp)).border(1.dp, MaterialTheme.colorScheme.outlineVariant, RoundedCornerShape(12.dp)).padding(12.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text("Research Title ${i+1}", fontWeight = FontWeight.Bold, fontSize = 14.sp, color = CamsTextPrimary)
                                Text("Prof. Smith • Journal Article", fontSize = 12.sp, color = CamsTextSecondary)
                            }
                            Column(horizontalAlignment = Alignment.End) {
                                val isOverdue = i == 1
                                Text(
                                    if(isOverdue) "OVERDUE" else "ACTIVE", 
                                    fontSize = 12.sp, 
                                    fontWeight = FontWeight.Bold, 
                                    color = if(isOverdue) Color(0xFFE11D48) else Color(0xFF1D4ED8), 
                                    modifier = Modifier.background(if(isOverdue) Color(0xFFFFE4E6) else Color(0xFFDBEAFE), RoundedCornerShape(4.dp)).padding(horizontal = 8.dp, vertical = 4.dp)
                                )
                                Spacer(Modifier.height(4.dp))
                                Text("Due: Oct 30, 2026", fontSize = 13.sp, color = CamsTextSecondary)
                            }
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
