package com.example.features.principal.screens

import android.widget.Toast
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.*
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.material3.MaterialTheme
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
import com.example.features.principal.providers.PrincipalCircularsViewModel
import com.example.core.repository.PrincipalRepositoryImpl
import com.example.core.network.ApiClient

@Composable
fun PrincipalCircularsScreen(
    onNavigate: (String) -> Unit,
    viewModel: PrincipalCircularsViewModel = viewModel(
        factory = object : androidx.lifecycle.ViewModelProvider.Factory {
            override fun <T : androidx.lifecycle.ViewModel> create(modelClass: Class<T>): T {
                return PrincipalCircularsViewModel(PrincipalRepositoryImpl(com.example.CamsApplication.instance.container.apiService)) as T
            }
        }
    )
) {
    val context = LocalContext.current
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    var title by remember { mutableStateOf("") }
    var content by remember { mutableStateOf("") }
    var targetAudience by remember { mutableStateOf("All") }

    LaunchedEffect(uiState.publishSuccess, uiState.publishError) {
        if (uiState.publishSuccess) {
            Toast.makeText(context, "Circular published", Toast.LENGTH_SHORT).show()
            title = ""
            content = ""
            viewModel.clearPublishStatus()
        }
        uiState.publishError?.let {
            Toast.makeText(context, it, Toast.LENGTH_LONG).show()
            viewModel.clearPublishStatus()
        }
    }

    PrincipalBaseScreen(
        title = "Publish Notices & Circulars",
        currentRoute = AppRoutes.PRINCIPAL_CIRCULARS,
        onNavigate = onNavigate,
        floatingActionButton = {
            FloatingActionButton(
                onClick = {
                    if (title.isNotBlank() && content.isNotBlank()) {
                        viewModel.publishCircular(title, content, targetAudience)
                    } else {
                        Toast.makeText(context, "Title and content are required", Toast.LENGTH_SHORT).show()
                    }
                },
                containerColor = CamsNavy, contentColor = Color.White
            ) {
                Icon(Icons.AutoMirrored.Filled.Send, "Publish")
            }
        }
    ) {
        CamsCard {
            Text("Create New Circular", fontWeight = FontWeight.Bold, fontSize = 18.sp, color = MaterialTheme.colorScheme.onSurface)
            Spacer(modifier = Modifier.height(16.dp))
            OutlinedTextField(
                value = title,
                onValueChange = { title = it },
                modifier = Modifier.fillMaxWidth(),
                label = { Text("Circular Title") },
                singleLine = true
            )
            Spacer(modifier = Modifier.height(12.dp))
            OutlinedTextField(
                value = content,
                onValueChange = { content = it },
                modifier = Modifier.fillMaxWidth().height(120.dp),
                label = { Text("Content") },
                maxLines = 5
            )
            Spacer(modifier = Modifier.height(12.dp))
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                Text("Target Audience:", fontWeight = FontWeight.Medium, color = MaterialTheme.colorScheme.onSurface)
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    FilterChip(selected = targetAudience == "All", onClick = { targetAudience = "All" }, label = { Text("All") })
                    FilterChip(selected = targetAudience == "Faculty", onClick = { targetAudience = "Faculty" }, label = { Text("Faculty") })
                    FilterChip(selected = targetAudience == "Students", onClick = { targetAudience = "Students" }, label = { Text("Students") })
                }
            }
        }
        
        Spacer(modifier = Modifier.height(24.dp))
        Text("Recent Circulars", fontWeight = FontWeight.Bold, fontSize = 18.sp, color = MaterialTheme.colorScheme.onSurface)
        Spacer(modifier = Modifier.height(8.dp))
        
        if (uiState.isLoading) {
            Box(modifier = Modifier.fillMaxWidth().height(200.dp), contentAlignment = Alignment.Center) {
                CircularProgressIndicator()
            }
        } else if (uiState.error != null) {
            // Without this the screen rendered empty on failure — no message,
            // no retry — indistinguishable from genuinely having no data.
            com.example.core.ui.NetworkErrorView(
                message = uiState.error ?: "Failed to load circulars",
                onRetry = { viewModel.loadCirculars() }
            )
        } else {
            LazyColumn(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                items(uiState.circulars) { circular ->
                    CamsCard {
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                            Column {
                                Text(circular.title, fontWeight = FontWeight.Bold, fontSize = 16.sp, color = MaterialTheme.colorScheme.onSurface)
                                Spacer(modifier = Modifier.height(4.dp))
                                Row(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalAlignment = Alignment.CenterVertically) {
                                    Surface(color = CamsNavy.copy(alpha = 0.1f), shape = MaterialTheme.shapes.small) {
                                        Text(circular.category ?: "All", fontSize = 12.sp, color = CamsNavy, modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp))
                                    }
                                    Text(circular.date ?: "N/A", fontSize = 12.sp, color = Color(0xFF64748B))
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}
