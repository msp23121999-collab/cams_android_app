package com.example.features.student.screens

import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.core.navigation.AppRoutes
import com.example.core.theme.*
import com.example.core.ui.CamsScreen
import com.example.features.student.providers.StudentProfileViewModel
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun StudentSettingsScreen(
    viewModel: StudentProfileViewModel,
    onNavigate: (String) -> Unit
) {
    val uiState by viewModel.uiState.collectAsState()
    var showChangePasswordDialog by rememberSaveable { mutableStateOf(false) }
    var currentPassword by rememberSaveable { mutableStateOf("") }
    var newPassword by rememberSaveable { mutableStateOf("") }
    var confirmPassword by rememberSaveable { mutableStateOf("") }
    var showCurrentPassword by rememberSaveable { mutableStateOf(false) }
    var showNewPassword by rememberSaveable { mutableStateOf(false) }
    var passwordError by rememberSaveable { mutableStateOf<String?>(null) }
    var passwordSuccess by rememberSaveable { mutableStateOf(false) }
    val scope = rememberCoroutineScope()

    CamsScreen(
        title = "Settings",
        subtitle = "Account & Preferences",
        onBackClick = { onNavigate(AppRoutes.STUDENT_DASHBOARD) }
    ) {
        Column(
            modifier = Modifier.fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Account Section
            SettingsSectionHeader(icon = Icons.Filled.Person, title = "Account")

            StudentSettingsCard(
                icon = Icons.Filled.Lock,
                title = "Change Password",
                subtitle = "Update your account password",
                onClick = { showChangePasswordDialog = true }
            )

            StudentSettingsCard(
                icon = Icons.Filled.Email,
                title = "Email Notifications",
                subtitle = "Manage email preferences",
                onClick = { }
            )

            Spacer(modifier = Modifier.height(8.dp))

            SettingsSectionHeader(icon = Icons.Filled.Settings, title = "Preferences")

            StudentSettingsCard(
                icon = Icons.Filled.Language,
                title = "Language",
                subtitle = "English",
                onClick = { }
            )

            StudentSettingsCard(
                icon = Icons.Filled.DarkMode,
                title = "Theme",
                subtitle = "System Default",
                onClick = { }
            )

            Spacer(modifier = Modifier.height(8.dp))

            SettingsSectionHeader(icon = Icons.Filled.Help, title = "Support")

            StudentSettingsCard(
                icon = Icons.Filled.Info,
                title = "About CAMS",
                subtitle = "Version 1.0.0 • Enterprise Edition",
                onClick = { }
            )

            StudentSettingsCard(
                icon = Icons.Filled.Policy,
                title = "Privacy Policy",
                subtitle = "View our privacy policy",
                onClick = { }
            )

            Spacer(modifier = Modifier.height(16.dp))

            Button(
                onClick = { onNavigate("LOGOUT") },
                modifier = Modifier.fillMaxWidth().height(52.dp),
                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error),
                shape = RoundedCornerShape(12.dp)
            ) {
                Icon(Icons.Filled.Logout, contentDescription = null)
                Spacer(modifier = Modifier.width(8.dp))
                Text("Sign Out", fontWeight = FontWeight.Bold)
            }
        }
    }

    if (showChangePasswordDialog) {
        AlertDialog(
            onDismissRequest = {
                showChangePasswordDialog = false
                passwordError = null
                passwordSuccess = false
                currentPassword = ""; newPassword = ""; confirmPassword = ""
            },
            title = {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Filled.Lock, contentDescription = null, tint = CamsNavy, modifier = Modifier.size(24.dp))
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Change Password", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold, color = CamsTextPrimary)
                }
            },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    if (passwordSuccess) {
                        Surface(modifier = Modifier.fillMaxWidth(), color = Color(0xFF10B981).copy(alpha = 0.1f), shape = RoundedCornerShape(8.dp)) {
                            Row(modifier = Modifier.padding(12.dp), verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.Filled.CheckCircle, contentDescription = null, tint = Color(0xFF10B981), modifier = Modifier.size(20.dp))
                                Spacer(modifier = Modifier.width(8.dp))
                                Text("Password updated successfully!", color = Color(0xFF10B981), style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.SemiBold)
                            }
                        }
                    }
                    if (passwordError != null) {
                        Surface(modifier = Modifier.fillMaxWidth(), color = MaterialTheme.colorScheme.error.copy(alpha = 0.1f), shape = RoundedCornerShape(8.dp)) {
                            Row(modifier = Modifier.padding(12.dp), verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.Filled.Error, contentDescription = null, tint = MaterialTheme.colorScheme.error, modifier = Modifier.size(20.dp))
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(passwordError!!, color = MaterialTheme.colorScheme.error, style = MaterialTheme.typography.bodySmall)
                            }
                        }
                    }
                    OutlinedTextField(
                        value = currentPassword,
                        onValueChange = { currentPassword = it; passwordError = null },
                        label = { Text("Current Password") },
                        visualTransformation = if (showCurrentPassword) VisualTransformation.None else PasswordVisualTransformation(),
                        trailingIcon = {
                            IconButton(onClick = { showCurrentPassword = !showCurrentPassword }) {
                                Icon(if (showCurrentPassword) Icons.Filled.VisibilityOff else Icons.Filled.Visibility, contentDescription = null)
                            }
                        },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true,
                        shape = RoundedCornerShape(10.dp)
                    )
                    OutlinedTextField(
                        value = newPassword,
                        onValueChange = { newPassword = it; passwordError = null },
                        label = { Text("New Password") },
                        visualTransformation = if (showNewPassword) VisualTransformation.None else PasswordVisualTransformation(),
                        trailingIcon = {
                            IconButton(onClick = { showNewPassword = !showNewPassword }) {
                                Icon(if (showNewPassword) Icons.Filled.VisibilityOff else Icons.Filled.Visibility, contentDescription = null)
                            }
                        },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true,
                        shape = RoundedCornerShape(10.dp)
                    )
                    OutlinedTextField(
                        value = confirmPassword,
                        onValueChange = { confirmPassword = it; passwordError = null },
                        label = { Text("Confirm New Password") },
                        visualTransformation = PasswordVisualTransformation(),
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true,
                        shape = RoundedCornerShape(10.dp)
                    )
                    Text("Password must be at least 8 characters with uppercase, lowercase, number, and special character.",
                        style = MaterialTheme.typography.labelSmall, color = CamsTextSecondary)
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        when {
                            currentPassword.isBlank() -> passwordError = "Current password is required"
                            newPassword.length < 8 -> passwordError = "Password must be at least 8 characters"
                            newPassword != confirmPassword -> passwordError = "Passwords do not match"
                            else -> {
                                scope.launch {
                                    try {
                                        viewModel.changePassword(currentPassword, newPassword)
                                        passwordSuccess = true
                                        currentPassword = ""; newPassword = ""; confirmPassword = ""
                                    } catch (e: Exception) {
                                        passwordError = e.message ?: "Failed to change password"
                                    }
                                }
                            }
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = CamsNavy),
                    shape = RoundedCornerShape(8.dp)
                ) { Text("Update Password") }
            },
            dismissButton = {
                OutlinedButton(onClick = {
                    showChangePasswordDialog = false
                    passwordError = null; passwordSuccess = false
                    currentPassword = ""; newPassword = ""; confirmPassword = ""
                }, shape = RoundedCornerShape(8.dp)) { Text("Cancel") }
            },
            shape = RoundedCornerShape(16.dp)
        )
    }
}

@Composable
private fun SettingsSectionHeader(icon: ImageVector, title: String) {
    Row(
        modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Icon(icon, contentDescription = null, modifier = Modifier.size(18.dp), tint = CamsNavy)
        Text(title.uppercase(), style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Black, letterSpacing = 1.5.sp), color = CamsNavy)
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun StudentSettingsCard(icon: ImageVector, title: String, subtitle: String, onClick: () -> Unit) {
    Card(
        onClick = onClick,
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Surface(modifier = Modifier.size(42.dp), shape = RoundedCornerShape(10.dp), color = CamsNavy.copy(alpha = 0.08f)) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(icon, contentDescription = null, modifier = Modifier.size(22.dp), tint = CamsNavy)
                }
            }
            Spacer(modifier = Modifier.width(16.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(title, style = MaterialTheme.typography.bodyLarge, fontWeight = FontWeight.SemiBold, color = CamsTextPrimary)
                Text(subtitle, style = MaterialTheme.typography.bodySmall, color = CamsTextSecondary)
            }
            Icon(Icons.Filled.ChevronRight, contentDescription = null, modifier = Modifier.size(20.dp), tint = CamsTextSecondary)
        }
    }
}
