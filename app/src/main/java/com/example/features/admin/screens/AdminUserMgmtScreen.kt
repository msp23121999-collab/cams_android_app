package com.example.features.admin.screens

import androidx.lifecycle.compose.collectAsStateWithLifecycle
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
import com.example.features.admin.widgets.AdminBaseScreen
import androidx.compose.foundation.lazy.items
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.features.admin.providers.AdminUserViewModel
import com.example.core.navigation.AppRoutes

@Composable
fun AdminUserMgmtScreen(
    onNavigate: (String) -> Unit,
    viewModel: AdminUserViewModel = viewModel(
        factory = object : androidx.lifecycle.ViewModelProvider.Factory {
            override fun <T : androidx.lifecycle.ViewModel> create(modelClass: Class<T>): T {
                return AdminUserViewModel(com.example.core.repository.AdminRepositoryImpl(com.example.CamsApplication.instance.container.apiService)) as T
            }
        }
    )
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    var showAddUserDialog by remember { mutableStateOf(false) }

    AdminBaseScreen(
        title = "User Management",
        subtitle = "Manage platform users and access roles",
        currentRoute = AppRoutes.ADMIN_USER_MGMT,
        onNavigate = onNavigate
    ) {
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
            Text("User Directory", fontWeight = FontWeight.Bold, fontSize = 18.sp, color = MaterialTheme.colorScheme.onSurface)
            Button(
                onClick = { showAddUserDialog = true }, 
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF4F46E5)), 
                shape = RoundedCornerShape(8.dp)
            ) {
                Icon(Icons.Filled.PersonAdd, null, modifier = Modifier.size(16.dp))
                Spacer(Modifier.width(4.dp))
                Text("Add User", fontSize = 12.sp, fontWeight = FontWeight.Bold)
            }
        }
        
        Spacer(Modifier.height(16.dp))

        if (uiState.isLoading) {
            Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator(color = CamsNavy)
            }
        } else if (uiState.error != null) {
            // Without this the screen rendered empty on failure — no message,
            // no retry — indistinguishable from genuinely having no data.
            com.example.core.ui.NetworkErrorView(
                message = uiState.error ?: "Failed to load users",
                onRetry = { viewModel.fetchUsers() }
            )
        } else if (uiState.users.isEmpty()) {
            Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Icon(Icons.Filled.People, null, modifier = Modifier.size(64.dp), tint = Color.LightGray)
                    Text("No users found", color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            }
        } else {
            CamsCard(modifier = Modifier.fillMaxWidth().weight(1f)) {
                LazyColumn(modifier = Modifier.fillMaxWidth(), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    items(uiState.users) { user ->
                        UserListItem(user, onDelete = { viewModel.deleteUser(user) })
                    }
                }
            }
        }
    }

    if (showAddUserDialog) {
        AddUserDialog(
            onDismiss = { showAddUserDialog = false },
            onConfirm = { newUser ->
                viewModel.addUser(newUser)
                showAddUserDialog = false
            }
        )
    }
}

@Composable
private fun UserListItem(user: com.example.features.admin.models.AdminUser, onDelete: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(MaterialTheme.colorScheme.surface, RoundedCornerShape(12.dp))
            .border(1.dp, MaterialTheme.colorScheme.outlineVariant, RoundedCornerShape(12.dp))
            .padding(16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(Modifier.background(Color(0xFFEEF2FF), CircleShape).size(40.dp), contentAlignment = Alignment.Center) {
            Text(user.fullName.take(1).uppercase(), fontWeight = FontWeight.Bold, color = Color(0xFF4F46E5))
        }
        Spacer(Modifier.width(16.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(user.fullName, fontWeight = FontWeight.Bold, fontSize = 14.sp, color = MaterialTheme.colorScheme.onSurface)
            Text(user.email, fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
        Text(
            user.role, 
            fontSize = 12.sp, 
            fontWeight = FontWeight.Bold, 
            color = MaterialTheme.colorScheme.onSurfaceVariant, 
            modifier = Modifier.background(MaterialTheme.colorScheme.surfaceVariant, RoundedCornerShape(4.dp)).padding(horizontal = 8.dp, vertical = 4.dp)
        )
        Spacer(Modifier.width(8.dp))
        IconButton(onClick = onDelete) {
            Icon(Icons.Filled.Delete, contentDescription = "Delete", tint = Color(0xFFEF4444), modifier = Modifier.size(20.dp))
        }
    }
}

@Composable
private fun AddUserDialog(onDismiss: () -> Unit, onConfirm: (com.example.features.admin.models.AdminUser) -> Unit) {
    var name by remember { mutableStateOf("") }
    var email by remember { mutableStateOf("") }
    var role by remember { mutableStateOf("STUDENT") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Add New User", fontWeight = FontWeight.Bold) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                OutlinedTextField(value = name, onValueChange = { name = it }, label = { Text("Full Name") }, modifier = Modifier.fillMaxWidth())
                OutlinedTextField(value = email, onValueChange = { email = it }, label = { Text("Email Address") }, modifier = Modifier.fillMaxWidth())
                
                Text("Role", fontWeight = FontWeight.Bold, fontSize = 14.sp)
                val roles = listOf("STUDENT", "FACULTY", "HOD", "PRINCIPAL", "ADMIN")
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                    roles.take(3).forEach { r ->
                        FilterChip(
                            selected = role == r,
                            onClick = { role = r },
                            label = { Text(r, fontSize = 10.sp) }
                        )
                    }
                }
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                    roles.drop(3).forEach { r ->
                        FilterChip(
                            selected = role == r,
                            onClick = { role = r },
                            label = { Text(r, fontSize = 10.sp) }
                        )
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    onConfirm(
                        com.example.features.admin.models.AdminUser(
                            id = java.util.UUID.randomUUID().toString(),
                            email = email,
                            fullName = name,
                            role = role,
                            phone = "",
                            isActive = true,
                            departmentId = "DEPT1"
                        )
                    )
                },
                enabled = name.isNotBlank() && email.isNotBlank(),
                colors = ButtonDefaults.buttonColors(containerColor = CamsNavy)
            ) {
                Text("Add User")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Cancel") }
        }
    )
}
