package com.example.features.principal.screens

import android.widget.Toast
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.*
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.CamsApplication
import com.example.core.navigation.AppRoutes
import com.example.core.repository.PrincipalRepositoryImpl
import com.example.core.theme.*
import com.example.core.ui.CamsCard
import com.example.features.principal.providers.PrincipalCircularsViewModel
import com.example.features.principal.widgets.PrincipalBaseScreen

private val STRATEGIC_PRIORITIES = setOf("High", "Critical", "Urgent")

@Composable
fun PrincipalStrategicNoticesScreen(
    onNavigate: (String) -> Unit,
    viewModel: PrincipalCircularsViewModel = viewModel(
        factory = object : androidx.lifecycle.ViewModelProvider.Factory {
            override fun <T : androidx.lifecycle.ViewModel> create(modelClass: Class<T>): T {
                @Suppress("UNCHECKED_CAST")
                return PrincipalCircularsViewModel(PrincipalRepositoryImpl(CamsApplication.instance.container.apiService)) as T
            }
        }
    )
) {
    val context = LocalContext.current
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    var title by remember { mutableStateOf("") }
    var content by remember { mutableStateOf("") }
    var targetAudience by remember { mutableStateOf("All") }
    var priority by remember { mutableStateOf("High") }

    LaunchedEffect(uiState.publishSuccess, uiState.publishError) {
        if (uiState.publishSuccess) {
            Toast.makeText(context, "Strategic notice published", Toast.LENGTH_SHORT).show()
            title = ""
            content = ""
            viewModel.clearPublishStatus()
        }
        uiState.publishError?.let {
            Toast.makeText(context, it, Toast.LENGTH_LONG).show()
            viewModel.clearPublishStatus()
        }
    }

    val strategicNotices = uiState.circulars.filter { it.priority != null && STRATEGIC_PRIORITIES.contains(it.priority) }

    PrincipalBaseScreen(
        title = "Strategic Notices",
        subtitle = "Publish high-priority, institution-wide strategic notices that surface above regular circulars.",
        currentRoute = AppRoutes.PRINCIPAL_STRATEGIC_NOTICES,
        onNavigate = onNavigate,
        floatingActionButton = {
            FloatingActionButton(
                onClick = {
                    if (title.isNotBlank() && content.isNotBlank()) {
                        viewModel.publishCircular(title, content, targetAudience, priority)
                    } else {
                        Toast.makeText(context, "Title and content are required", Toast.LENGTH_SHORT).show()
                    }
                },
                containerColor = Color(0xFFB91C1C), contentColor = Color.White
            ) {
                Icon(Icons.AutoMirrored.Filled.Send, "Publish Strategic Notice")
            }
        }
    ) {
        CamsCard {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Filled.PriorityHigh, null, tint = Color(0xFFB91C1C))
                Spacer(Modifier.width(8.dp))
                Text("Publish Strategic Notice", fontWeight = FontWeight.Bold, fontSize = 18.sp, color = MaterialTheme.colorScheme.onSurface)
            }
            Spacer(modifier = Modifier.height(16.dp))
            OutlinedTextField(
                value = title, onValueChange = { title = it }, modifier = Modifier.fillMaxWidth(),
                label = { Text("Notice Title") }, singleLine = true
            )
            Spacer(modifier = Modifier.height(12.dp))
            OutlinedTextField(
                value = content, onValueChange = { content = it }, modifier = Modifier.fillMaxWidth().height(120.dp),
                label = { Text("Content") }, maxLines = 5
            )
            Spacer(modifier = Modifier.height(12.dp))
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                Text("Priority:", fontWeight = FontWeight.Medium, color = MaterialTheme.colorScheme.onSurface)
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    listOf("High", "Critical", "Urgent").forEach { p ->
                        FilterChip(selected = priority == p, onClick = { priority = p }, label = { Text(p) })
                    }
                }
            }
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
        Text("Active Strategic Notices", fontWeight = FontWeight.Bold, fontSize = 18.sp, color = MaterialTheme.colorScheme.onSurface)
        Spacer(modifier = Modifier.height(8.dp))

        if (uiState.isLoading) {
            Box(modifier = Modifier.fillMaxWidth().height(200.dp), contentAlignment = Alignment.Center) { CircularProgressIndicator() }
        } else if (uiState.error != null) {
            // Without this the screen rendered empty on failure — no message,
            // no retry — indistinguishable from genuinely having no data.
            com.example.core.ui.NetworkErrorView(
                message = uiState.error ?: "Failed to load notices",
                onRetry = { viewModel.loadCirculars() }
            )
        } else if (strategicNotices.isEmpty()) {
            Box(modifier = Modifier.fillMaxWidth().height(120.dp), contentAlignment = Alignment.Center) {
                Text("No strategic notices published yet", color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
        } else {
            LazyColumn(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                items(strategicNotices, key = { it.id }) { notice ->
                    CamsCard {
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.Top) {
                            Column(Modifier.weight(1f)) {
                                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                    Text(notice.title, fontWeight = FontWeight.Bold, fontSize = 16.sp, color = MaterialTheme.colorScheme.onSurface)
                                    Surface(color = Color(0xFFFEE2E2), shape = MaterialTheme.shapes.small) {
                                        Text(notice.priority ?: "High", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = Color(0xFFB91C1C), modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp))
                                    }
                                }
                                Spacer(Modifier.height(4.dp))
                                Text(notice.body, fontSize = 13.sp, color = Color(0xFF64748B))
                                Spacer(Modifier.height(4.dp))
                                Text(notice.date ?: "N/A", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            }
                        }
                    }
                }
            }
        }
    }
}
