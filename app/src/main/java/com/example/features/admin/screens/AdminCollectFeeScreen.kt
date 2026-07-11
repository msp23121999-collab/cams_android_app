package com.example.features.admin.screens

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Payment
import androidx.compose.material.icons.filled.Search
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
fun AdminCollectFeeScreen(onNavigate: (String) -> Unit) {
    var searchQuery by remember { mutableStateOf("") }
    
    AdminBaseScreen(
        title = "Collect Fee",
        currentRoute = AppRoutes.ADMIN_COLLECT_FEE,
        onNavigate = onNavigate
    ) {
        Column(
            modifier = Modifier.fillMaxSize().padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            OutlinedTextField(
                value = searchQuery,
                onValueChange = { searchQuery = it },
                label = { Text("Search Student by ID or Name") },
                leadingIcon = { Icon(Icons.Filled.Search, contentDescription = null) },
                modifier = Modifier.fillMaxWidth()
            )
            
            CamsCard {
                Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    Text("Select Fee Type", fontWeight = FontWeight.Bold, fontSize = 16.sp, color = CamsTextPrimary)
                    
                    val feeTypes = listOf("Tuition Fee", "Hostel Fee", "Exam Fee", "Library Fine")
                    var selectedFeeType by remember { mutableStateOf(feeTypes[0]) }
                    
                    feeTypes.forEach { type ->
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            RadioButton(
                                selected = selectedFeeType == type,
                                onClick = { selectedFeeType = type },
                                colors = RadioButtonDefaults.colors(selectedColor = CamsNavy)
                            )
                            Text(type, fontSize = 14.sp)
                        }
                    }
                    
                    Spacer(Modifier.height(8.dp))
                    
                    OutlinedTextField(
                        value = "",
                        onValueChange = {},
                        label = { Text("Amount (₹)") },
                        modifier = Modifier.fillMaxWidth()
                    )
                    
                    Button(onClick = { }, modifier = Modifier.fillMaxWidth(), colors = ButtonDefaults.buttonColors(containerColor = CamsNavy)) {
                        Icon(Icons.Filled.Payment, contentDescription = null, modifier = Modifier.size(18.dp))
                        Spacer(Modifier.width(8.dp))
                        Text("Process Payment")
                    }
                }
            }
        }
    }
}
