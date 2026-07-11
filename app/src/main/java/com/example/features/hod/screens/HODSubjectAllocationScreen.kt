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
fun HODSubjectAllocationScreen(onNavigate: (String) -> Unit) {
    var activeTab by remember { mutableStateOf("allocation") }
    
    HODBaseScreen(
        title = "Subject Allocation",
        subtitle = "Assign Faculty to Subjects",
        currentRoute = AppRoutes.HOD_SUBJECT_ALLOCATION,
        onNavigate = onNavigate
    ) {
        // Tabs
        Row(Modifier.fillMaxWidth().background(MaterialTheme.colorScheme.surface, RoundedCornerShape(12.dp)).padding(4.dp)) {
            TabButton("Subject Allocation", activeTab == "allocation", Modifier.weight(1f)) { activeTab = "allocation" }
            TabButton("Faculty Workload", activeTab == "workload", Modifier.weight(1f)) { activeTab = "workload" }
            TabButton("History", activeTab == "history", Modifier.weight(1f)) { activeTab = "history" }
        }

        Spacer(Modifier.height(16.dp))

        CamsCard(modifier = Modifier.fillMaxWidth().weight(1f)) {
            if (activeTab == "allocation") {
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                    Text("Assign Faculty to Subjects", fontWeight = FontWeight.Bold, fontSize = 16.sp, color = CamsTextPrimary)
                    Button(onClick = { }, colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF4F46E5)), shape = RoundedCornerShape(8.dp)) {
                        Text("Save Allocations", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                    }
                }
                Spacer(Modifier.height(12.dp))
                LazyColumn(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    items(5) { i ->
                        CamsCard(containerColor = MaterialTheme.colorScheme.background, modifier = Modifier.fillMaxWidth()) {
                            Column(Modifier.padding(12.dp)) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Text("LAW10${i}", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = Color(0xFF4F46E5), modifier = Modifier.background(Color(0xFFEEF2FF), RoundedCornerShape(4.dp)).padding(4.dp))
                                    Spacer(Modifier.width(8.dp))
                                    Text("Sem 1", fontSize = 12.sp, color = Color(0xFF047857), modifier = Modifier.background(Color(0xFFECFDF5), RoundedCornerShape(4.dp)).padding(4.dp))
                                }
                                Spacer(Modifier.height(8.dp))
                                Text("Subject Name ${i+1}", fontWeight = FontWeight.Bold, fontSize = 14.sp, color = CamsTextPrimary)
                                Spacer(Modifier.height(8.dp))
                                Text("Assign Faculty:", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = CamsTextSecondary)
                                Spacer(Modifier.height(4.dp))
                                Box(modifier = Modifier.fillMaxWidth().background(MaterialTheme.colorScheme.surface, RoundedCornerShape(8.dp)).border(1.dp, MaterialTheme.colorScheme.outlineVariant, RoundedCornerShape(8.dp)).padding(12.dp)) {
                                    Text(if (i == 0) "Dr. Alice Brown" else "-- Select Faculty --", fontSize = 12.sp, color = if (i == 0) CamsTextPrimary else Color(0xFF64748B))
                                }
                            }
                        }
                    }
                }
            } else {
                Text("Coming Soon", fontWeight = FontWeight.Bold, fontSize = 16.sp, color = CamsTextPrimary)
            }
        }
    }
}

@Composable
private fun TabButton(text: String, selected: Boolean, modifier: Modifier, onClick: () -> Unit) {
    Button(
        onClick = onClick,
        modifier = modifier,
        colors = ButtonDefaults.buttonColors(
            containerColor = if (selected) Color(0xFFEEF2FF) else Color.Transparent,
            contentColor = if (selected) Color(0xFF4338CA) else Color(0xFF64748B)
        ),
        shape = RoundedCornerShape(8.dp),
        elevation = null
    ) {
        Text(text, fontSize = 13.sp, fontWeight = FontWeight.Bold)
    }
}
