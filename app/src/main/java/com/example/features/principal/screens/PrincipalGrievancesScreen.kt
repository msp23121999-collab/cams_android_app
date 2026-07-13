package com.example.features.principal.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.core.navigation.AppRoutes
import com.example.core.theme.*
import com.example.core.ui.CamsCard
import com.example.features.principal.widgets.PrincipalBaseScreen

import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.features.principal.providers.PrincipalGrievancesViewModel
import com.example.core.repository.PrincipalRepositoryImpl
import com.example.core.network.ApiClient

@Composable
fun PrincipalGrievancesScreen(
    onNavigate: (String) -> Unit,
    viewModel: PrincipalGrievancesViewModel = viewModel(
        factory = object : androidx.lifecycle.ViewModelProvider.Factory {
            override fun <T : androidx.lifecycle.ViewModel> create(modelClass: Class<T>): T {
                return PrincipalGrievancesViewModel(PrincipalRepositoryImpl(com.example.CamsApplication.instance.container.apiService)) as T
            }
        }
    )
) {
    val uiState by viewModel.uiState.collectAsState()
    var searchQuery by remember { mutableStateOf("") }
    val grievances = uiState.grievances

    PrincipalBaseScreen(
        title = "Grievance Inbox",
        currentRoute = AppRoutes.PRINCIPAL_GRIEVANCES,
        onNavigate = onNavigate
    ) {
        OutlinedTextField(
            value = searchQuery,
            onValueChange = { searchQuery = it },
            modifier = Modifier.fillMaxWidth(),
            placeholder = { Text("Search grievances...") },
            leadingIcon = { Icon(Icons.Filled.Search, null) },
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = CamsNavy,
                unfocusedBorderColor = Color.LightGray
            ),
            shape = MaterialTheme.shapes.medium
        )

        Spacer(modifier = Modifier.height(16.dp))

        if (uiState.isLoading) {
            Box(modifier = Modifier.fillMaxWidth().height(200.dp), contentAlignment = Alignment.Center) {
                CircularProgressIndicator()
            }
        } else {
            LazyColumn(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                items(grievances.filter { it.subject.contains(searchQuery, ignoreCase = true) || it.id.contains(searchQuery, ignoreCase = true) }) { grievance ->
                    CamsCard {
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                            Column(modifier = Modifier.weight(1f)) {
                                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                    Text(grievance.id.take(8), fontWeight = FontWeight.Bold, color = CamsNavy, fontSize = 12.sp)
                                    Surface(color = if (grievance.priority.equals("High", true)) Color(0xFFFEE2E2) else Color(0xFFFEF3C7), shape = MaterialTheme.shapes.small) {
                                        Text(grievance.priority.capitalize(), fontSize = 12.sp, color = if (grievance.priority.equals("High", true)) Color.Red else Color(0xFFD97706), modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp))
                                    }
                                }
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(grievance.subject, fontWeight = FontWeight.Bold, fontSize = 16.sp, color = CamsTextPrimary)
                                Text("Category: ${grievance.category}", fontSize = 12.sp, color = CamsTextSecondary)
                                Spacer(modifier = Modifier.height(8.dp))
                                Text(grievance.description, fontSize = 14.sp, color = Color(0xFF64748B))
                            }
                        }
                    Spacer(modifier = Modifier.height(16.dp))
                    Row(horizontalArrangement = Arrangement.spacedBy(12.dp), modifier = Modifier.fillMaxWidth()) {
                        Button(
                            onClick = { /* Review */ },
                            modifier = Modifier.weight(1f),
                            colors = ButtonDefaults.buttonColors(containerColor = CamsNavy)
                        ) {
                            Text("Review Grievance")
                        }
                    }
                }
            }
        }
    }
    }
}
