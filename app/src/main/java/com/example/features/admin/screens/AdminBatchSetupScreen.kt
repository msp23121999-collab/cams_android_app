package com.example.features.admin.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.core.theme.*
import com.example.core.ui.CamsCard
import com.example.features.admin.widgets.AdminBaseScreen
import com.example.core.navigation.AppRoutes

@Composable
fun AdminBatchSetupScreen(onNavigate: (String) -> Unit) {
    AdminBaseScreen(
        title = "Batch Setup",
        currentRoute = AppRoutes.ADMIN_BATCH_SETUP,
        onNavigate = onNavigate,
        floatingActionButton = {
            FloatingActionButton(onClick = {}, containerColor = CamsNavy, contentColor = Color.White) {
                Icon(Icons.Filled.Add, "Add Batch")
            }
        }
    ) {
        Column(
            modifier = Modifier.fillMaxSize().padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            val batches = listOf(
                BatchItem("CS 2023-2027", "Computer Science", "120 Students"),
                BatchItem("ME 2023-2027", "Mechanical Eng", "90 Students"),
                BatchItem("EE 2023-2027", "Electrical Eng", "100 Students")
            )
            
            LazyColumn(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                items(batches) { batch ->
                    CamsCard {
                        Column(modifier = Modifier.fillMaxWidth().padding(16.dp)) {
                            Text(batch.name, fontWeight = FontWeight.Bold, fontSize = 16.sp, color = CamsTextPrimary)
                            Spacer(Modifier.height(4.dp))
                            Text("${batch.department} • ${batch.students}", fontSize = 13.sp, color = CamsTextSecondary)
                        }
                    }
                }
            }
        }
    }
}

data class BatchItem(val name: String, val department: String, val students: String)
