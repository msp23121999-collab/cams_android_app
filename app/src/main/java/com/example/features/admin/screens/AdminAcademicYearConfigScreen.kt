package com.example.features.admin.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Save
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
fun AdminAcademicYearConfigScreen(onNavigate: (String) -> Unit) {
    AdminBaseScreen(
        title = "Academic Year Config",
        currentRoute = AppRoutes.ADMIN_ACADEMIC_YEAR_CONFIG,
        onNavigate = onNavigate
    ) {
        Column(
            modifier = Modifier.fillMaxSize().padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            CamsCard {
                Column(Modifier.padding(16.dp)) {
                    Text("Active Academic Year", fontWeight = FontWeight.Bold, fontSize = 16.sp, color = CamsTextPrimary)
                    Spacer(Modifier.height(16.dp))
                    OutlinedTextField(
                        value = "2023-2024",
                        onValueChange = {},
                        label = { Text("Select Year") },
                        modifier = Modifier.fillMaxWidth()
                    )
                    Spacer(Modifier.height(16.dp))
                    Button(onClick = { }, modifier = Modifier.fillMaxWidth(), colors = ButtonDefaults.buttonColors(containerColor = CamsNavy)) {
                        Icon(Icons.Filled.Save, contentDescription = null, modifier = Modifier.size(18.dp))
                        Spacer(Modifier.width(8.dp))
                        Text("Save Configuration")
                    }
                }
            }
            
            Text("Past Configurations", fontWeight = FontWeight.Bold, fontSize = 16.sp, color = CamsTextPrimary)
            val years = listOf("2022-2023", "2021-2022", "2020-2021")
            LazyColumn(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                items(years) { year ->
                    CamsCard {
                        Row(modifier = Modifier.fillMaxWidth().padding(16.dp), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                            Text(year, fontWeight = FontWeight.Bold, color = CamsTextPrimary)
                            Text("Locked", color = Color(0xFF64748B), fontSize = 12.sp)
                        }
                    }
                }
            }
        }
    }
}
