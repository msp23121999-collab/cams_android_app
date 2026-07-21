package com.example.features.student.screens

import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.*
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.material3.MaterialTheme
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
import com.example.BuildConfig
import com.example.core.navigation.AppRoutes
import com.example.core.theme.*
import com.example.core.ui.CamsScreen
import com.example.features.auth.providers.AuthViewModel
import com.example.features.student.providers.StudentProfileViewModel
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun StudentSettingsScreen(
    viewModel: StudentProfileViewModel,
    authViewModel: AuthViewModel,
    onNavigate: (String) -> Unit
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    var showChangePasswordDialog by rememberSaveable { mutableStateOf(false) }
    var currentPassword by rememberSaveable { mutableStateOf("") }
    var newPassword by rememberSaveable { mutableStateOf("") }
    var confirmPassword by rememberSaveable { mutableStateOf("") }
    var showCurrentPassword by rememberSaveable { mutableStateOf(false) }
    var showNewPassword by rememberSaveable { mutableStateOf(false) }
    var passwordError by rememberSaveable { mutableStateOf<String?>(null) }
    var passwordSuccess by rememberSaveable { mutableStateOf(false) }
    val scope = rememberCoroutineScope()

    var showChangeEmailDialog by rememberSaveable { mutableStateOf(false) }
    var showAboutDialog by rememberSaveable { mutableStateOf(false) }
    var newEmail by rememberSaveable { mutableStateOf("") }
    var currentPasswordForEmail by rememberSaveable { mutableStateOf("") }
    val emailChangeState by authViewModel.emailChangeState.collectAsStateWithLifecycle()
    val notificationPrefsState by authViewModel.notificationPreferencesState.collectAsStateWithLifecycle()

    LaunchedEffect(Unit) {
        authViewModel.loadNotificationPreferences()
    }

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
                icon = Icons.Filled.AlternateEmail,
                title = "Change Email",
                subtitle = uiState.profile?.email ?: "Update your account email",
                onClick = {
                    authViewModel.clearEmailChangeState()
                    newEmail = ""
                    currentPasswordForEmail = ""
                    showChangeEmailDialog = true
                }
            )

            StudentSettingsCard(
                icon = Icons.Filled.Email,
                title = "Email Notifications",
                subtitle = if (notificationPrefsState.emailNotificationsEnabled) "Enabled" else "Disabled",
                onClick = { authViewModel.setEmailNotificationsEnabled(!notificationPrefsState.emailNotificationsEnabled) },
                trailing = {
                    Switch(
                        checked = notificationPrefsState.emailNotificationsEnabled,
                        onCheckedChange = { authViewModel.setEmailNotificationsEnabled(it) },
                        enabled = !notificationPrefsState.isLoading
                    )
                }
            )

            Spacer(modifier = Modifier.height(8.dp))

            SettingsSectionHeader(icon = Icons.Filled.Settings, title = "Preferences")

            StudentSettingsCard(
                icon = Icons.Filled.Language,
                title = "Language",
                subtitle = "Coming soon",
                onClick = { },
                enabled = false
            )

            StudentSettingsCard(
                icon = Icons.Filled.DarkMode,
                title = "Theme",
                subtitle = "Coming soon",
                onClick = { },
                enabled = false
            )

            Spacer(modifier = Modifier.height(8.dp))

            SettingsSectionHeader(icon = Icons.AutoMirrored.Filled.Help, title = "Support")

            StudentSettingsCard(
                icon = Icons.Filled.Info,
                title = "About CAMS",
                subtitle = "Version ${BuildConfig.VERSION_NAME} • Enterprise Edition",
                onClick = { showAboutDialog = true }
            )

            StudentSettingsCard(
                icon = Icons.Filled.Policy,
                title = "Privacy Policy",
                subtitle = "Coming soon",
                onClick = { },
                enabled = false
            )

            Spacer(modifier = Modifier.height(16.dp))

            Button(
                onClick = { onNavigate("LOGOUT") },
                modifier = Modifier.fillMaxWidth().height(52.dp),
                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error),
                shape = RoundedCornerShape(12.dp)
            ) {
                Icon(Icons.AutoMirrored.Filled.Logout, contentDescription = null)
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
                    Icon(Icons.Filled.Lock, contentDescription = null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(24.dp))
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Change Password", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurface)
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
                        style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        val strengthError = com.example.core.utils.PasswordValidator.validate(newPassword)
                        when {
                            currentPassword.isBlank() -> passwordError = "Current password is required"
                            strengthError != null -> passwordError = strengthError
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

    if (showChangeEmailDialog) {
        AlertDialog(
            onDismissRequest = { showChangeEmailDialog = false },
            title = {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Filled.AlternateEmail, contentDescription = null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(24.dp))
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Change Email", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurface)
                }
            },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    if (emailChangeState.success) {
                        Surface(modifier = Modifier.fillMaxWidth(), color = Color(0xFF10B981).copy(alpha = 0.1f), shape = RoundedCornerShape(8.dp)) {
                            Row(modifier = Modifier.padding(12.dp), verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.Filled.CheckCircle, contentDescription = null, tint = Color(0xFF10B981), modifier = Modifier.size(20.dp))
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    emailChangeState.message ?: "Verification email sent to the new address",
                                    color = Color(0xFF10B981),
                                    style = MaterialTheme.typography.bodyMedium,
                                    fontWeight = FontWeight.SemiBold
                                )
                            }
                        }
                    } else {
                        Text(
                            "Current email: ${uiState.profile?.email ?: "—"}",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        OutlinedTextField(
                            value = newEmail,
                            onValueChange = { newEmail = it },
                            label = { Text("New Email Address") },
                            modifier = Modifier.fillMaxWidth(),
                            singleLine = true,
                            shape = RoundedCornerShape(10.dp)
                        )
                        OutlinedTextField(
                            value = currentPasswordForEmail,
                            onValueChange = { currentPasswordForEmail = it },
                            label = { Text("Current Password") },
                            modifier = Modifier.fillMaxWidth(),
                            singleLine = true,
                            visualTransformation = PasswordVisualTransformation(),
                            shape = RoundedCornerShape(10.dp)
                        )
                        Text(
                            "We'll send a verification link to your new address. Your email won't change until you confirm it.",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        if (emailChangeState.error != null) {
                            Text(emailChangeState.error ?: "", color = MaterialTheme.colorScheme.error, style = MaterialTheme.typography.bodySmall)
                        }
                    }
                }
            },
            confirmButton = {
                if (emailChangeState.success) {
                    Button(
                        onClick = { showChangeEmailDialog = false },
                        colors = ButtonDefaults.buttonColors(containerColor = CamsNavy),
                        shape = RoundedCornerShape(8.dp)
                    ) { Text("Done") }
                } else {
                    Button(
                        onClick = { authViewModel.requestEmailChange(newEmail.trim(), currentPasswordForEmail) },
                        enabled = !emailChangeState.isLoading && newEmail.isNotBlank() && currentPasswordForEmail.isNotBlank(),
                        colors = ButtonDefaults.buttonColors(containerColor = CamsNavy),
                        shape = RoundedCornerShape(8.dp)
                    ) { Text(if (emailChangeState.isLoading) "Sending..." else "Send Verification") }
                }
            },
            dismissButton = {
                OutlinedButton(onClick = { showChangeEmailDialog = false }, shape = RoundedCornerShape(8.dp)) { Text("Cancel") }
            },
            shape = RoundedCornerShape(16.dp)
        )
    }

    if (showAboutDialog) {
        AlertDialog(
            onDismissRequest = { showAboutDialog = false },
            title = {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Filled.Info, contentDescription = null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(24.dp))
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("About CAMS", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurface)
                }
            },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text("CAMS Enterprise Edition", style = MaterialTheme.typography.bodyLarge, fontWeight = FontWeight.SemiBold)
                    Text("Version ${BuildConfig.VERSION_NAME} (${BuildConfig.VERSION_CODE})", style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    Text(
                        "A comprehensive college administration and management system for students, parents, faculty, and administrators.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = { showAboutDialog = false },
                    colors = ButtonDefaults.buttonColors(containerColor = CamsNavy),
                    shape = RoundedCornerShape(8.dp)
                ) { Text("Close") }
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
        Icon(icon, contentDescription = null, modifier = Modifier.size(18.dp), tint = MaterialTheme.colorScheme.primary)
        Text(title.uppercase(), style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Black, letterSpacing = 1.5.sp), color = CamsNavy)
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun StudentSettingsCard(
    icon: ImageVector,
    title: String,
    subtitle: String,
    onClick: () -> Unit,
    enabled: Boolean = true,
    trailing: (@Composable () -> Unit)? = null
) {
    val contentAlpha = if (enabled) 1f else 0.5f
    Card(
        onClick = onClick,
        enabled = enabled,
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Surface(modifier = Modifier.size(42.dp), shape = RoundedCornerShape(10.dp), color = CamsNavy.copy(alpha = 0.08f * contentAlpha)) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(icon, contentDescription = null, modifier = Modifier.size(22.dp), tint = MaterialTheme.colorScheme.primary.copy(alpha = contentAlpha))
                }
            }
            Spacer(modifier = Modifier.width(16.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(title, style = MaterialTheme.typography.bodyLarge, fontWeight = FontWeight.SemiBold, color = MaterialTheme.colorScheme.onSurface.copy(alpha = contentAlpha))
                Text(subtitle, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = contentAlpha))
            }
            if (trailing != null) {
                trailing()
            } else {
                Icon(Icons.Filled.ChevronRight, contentDescription = null, modifier = Modifier.size(20.dp), tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = contentAlpha))
            }
        }
    }
}
