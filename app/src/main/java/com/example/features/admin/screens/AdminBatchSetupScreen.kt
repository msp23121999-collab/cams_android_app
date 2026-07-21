package com.example.features.admin.screens

import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.material3.MaterialTheme
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

import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.features.admin.providers.AdminBatchSetupViewModel
import com.example.core.repository.AdminRepositoryImpl
import com.example.core.network.ApiClient

@Composable
fun AdminBatchSetupScreen(
    onNavigate: (String) -> Unit,
    viewModel: AdminBatchSetupViewModel = viewModel(
        factory = object : androidx.lifecycle.ViewModelProvider.Factory {
            override fun <T : androidx.lifecycle.ViewModel> create(modelClass: Class<T>): T {
                return AdminBatchSetupViewModel(AdminRepositoryImpl(com.example.CamsApplication.instance.container.apiService)) as T
            }
        }
    )
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    AdminBaseScreen(
        title = "Batch Setup",
        currentRoute = AppRoutes.ADMIN_BATCH_SETUP,
        onNavigate = onNavigate,
        floatingActionButton = {
            // Batches are academic-year cohorts; creation and full CRUD live on the
            // Academic Year Config screen, so send the user there rather than
            // duplicating a second create flow that could drift out of sync.
            FloatingActionButton(
                onClick = { onNavigate(AppRoutes.ADMIN_ACADEMIC_YEAR_CONFIG) },
                containerColor = CamsNavy,
                contentColor = Color.White
            ) {
                Icon(Icons.Filled.Add, "Add Batch")
            }
        }
    ) {
        Column(
            modifier = Modifier.fillMaxSize().padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            uiState.error?.let {
                Text(it, color = Color(0xFFB91C1C), fontSize = 13.sp)
            }
            if (uiState.isLoading) {
                Box(modifier = Modifier.fillMaxWidth().height(200.dp), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator()
                }
            } else if (uiState.batches.isEmpty()) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Text("No batches configured", color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            } else {
                LazyColumn(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    items(uiState.batches, key = { it.id }) { batch ->
                        CamsCard {
                            Row(modifier = Modifier.fillMaxWidth().padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
                                Column(Modifier.weight(1f)) {
                                    Text(batch.year, fontWeight = FontWeight.Bold, fontSize = 16.sp, color = MaterialTheme.colorScheme.onSurface)
                                    Spacer(Modifier.height(4.dp))
                                    Text("Status: ${batch.status}", fontSize = 13.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                }
                                if (batch.isActive) {
                                    Text(
                                        "ACTIVE",
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = Color(0xFF4338CA),
                                        modifier = Modifier
                                            .background(Color(0xFFEEF2FF), RoundedCornerShape(4.dp))
                                            .padding(horizontal = 6.dp, vertical = 2.dp)
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}
