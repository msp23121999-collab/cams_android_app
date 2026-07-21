package com.example.features.auth

import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Key
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material.icons.rounded.LockReset
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.core.theme.*
import com.example.features.auth.providers.AuthViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ResetPasswordScreen(
    authViewModel: AuthViewModel,
    onBack: () -> Unit,
    onResetSuccess: () -> Unit
) {
    var token by remember { mutableStateOf("") }
    var newPassword by remember { mutableStateOf("") }
    var confirmPassword by remember { mutableStateOf("") }
    var passwordVisible by remember { mutableStateOf(false) }
    var localError by remember { mutableStateOf<String?>(null) }

    val resetState by authViewModel.passwordResetState.collectAsStateWithLifecycle()

    DisposableEffect(Unit) {
        authViewModel.clearPasswordResetState()
        onDispose { }
    }

    Scaffold(containerColor = MaterialTheme.colorScheme.background) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(Brush.verticalGradient(colors = listOf(CamsNavy.copy(alpha = 0.05f), CamsBackground)))
                .verticalScroll(rememberScrollState())
                .padding(padding)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth().padding(horizontal = 8.dp, vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(onClick = onBack) {
                    Icon(Icons.Filled.ArrowBack, contentDescription = "Back", tint = MaterialTheme.colorScheme.primary)
                }
            }

            Column(
                modifier = Modifier.fillMaxWidth().padding(horizontal = 24.dp, vertical = 16.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Box(
                    modifier = Modifier
                        .size(80.dp)
                        .clip(RoundedCornerShape(24.dp))
                        .background(CamsNavy.copy(alpha = 0.12f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(Icons.Rounded.LockReset, contentDescription = null, tint = CamsNavy, modifier = Modifier.size(40.dp))
                }

                Spacer(modifier = Modifier.height(24.dp))

                Text(
                    "Reset Password",
                    style = MaterialTheme.typography.headlineMedium.copy(
                        color = CamsNavy, fontWeight = FontWeight.Black, letterSpacing = 1.5.sp, fontSize = 24.sp
                    ),
                    textAlign = TextAlign.Center
                )

                Spacer(modifier = Modifier.height(6.dp))

                Text(
                    "Paste the reset code from the email we sent you, then choose a new password.",
                    style = MaterialTheme.typography.bodyMedium.copy(color = MaterialTheme.colorScheme.onSurfaceVariant),
                    textAlign = TextAlign.Center
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            Column(modifier = Modifier.padding(horizontal = 24.dp)) {
                Surface(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(28.dp),
                    color = Color.White,
                    shadowElevation = 6.dp,
                    tonalElevation = 1.dp
                ) {
                    Column(modifier = Modifier.padding(24.dp), verticalArrangement = Arrangement.spacedBy(20.dp)) {
                        if (resetState.success) {
                            Text(
                                text = resetState.message ?: "Password has been reset successfully",
                                style = MaterialTheme.typography.bodyMedium.copy(color = Color(0xFF10B981), fontWeight = FontWeight.Medium),
                                textAlign = TextAlign.Center,
                                modifier = Modifier.fillMaxWidth()
                            )
                            Button(
                                onClick = onResetSuccess,
                                modifier = Modifier.fillMaxWidth().height(52.dp),
                                shape = RoundedCornerShape(14.dp),
                                colors = ButtonDefaults.buttonColors(containerColor = CamsNavy)
                            ) {
                                Text("Back to Login", color = Color.White, fontWeight = FontWeight.Bold)
                            }
                        } else {
                            OutlinedTextField(
                                value = token,
                                onValueChange = { token = it; localError = null },
                                label = { Text("Reset Code") },
                                leadingIcon = { Icon(Icons.Filled.Key, contentDescription = "Token Icon") },
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(14.dp),
                                singleLine = true
                            )

                            OutlinedTextField(
                                value = newPassword,
                                onValueChange = { newPassword = it; localError = null },
                                label = { Text("New Password") },
                                leadingIcon = { Icon(Icons.Filled.Key, contentDescription = "Password Icon") },
                                trailingIcon = {
                                    IconButton(onClick = { passwordVisible = !passwordVisible }) {
                                        Icon(
                                            if (passwordVisible) Icons.Filled.Visibility else Icons.Filled.VisibilityOff,
                                            "Toggle Password Visibility"
                                        )
                                    }
                                },
                                visualTransformation = if (passwordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(14.dp),
                                singleLine = true
                            )

                            OutlinedTextField(
                                value = confirmPassword,
                                onValueChange = { confirmPassword = it; localError = null },
                                label = { Text("Confirm New Password") },
                                leadingIcon = { Icon(Icons.Filled.Key, contentDescription = "Confirm Password Icon") },
                                visualTransformation = if (passwordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(14.dp),
                                singleLine = true
                            )

                            Text(
                                "Password must be at least 8 characters.",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )

                            val displayedError = localError ?: resetState.error
                            if (displayedError != null) {
                                Text(displayedError, color = Color(0xFFEF4444), style = MaterialTheme.typography.bodySmall)
                            }

                            Button(
                                onClick = {
                                    val strengthError = com.example.core.utils.PasswordValidator.validate(newPassword)
                                    when {
                                        token.isBlank() -> localError = "Reset code is required"
                                        strengthError != null -> localError = strengthError
                                        newPassword != confirmPassword -> localError = "Passwords do not match"
                                        else -> authViewModel.resetPassword(token.trim(), newPassword)
                                    }
                                },
                                modifier = Modifier.fillMaxWidth().height(52.dp),
                                shape = RoundedCornerShape(14.dp),
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = CamsNavy,
                                    disabledContainerColor = CamsNavy.copy(alpha = 0.5f)
                                ),
                                enabled = !resetState.isLoading
                            ) {
                                Text(
                                    text = if (resetState.isLoading) "Resetting..." else "Reset Password",
                                    style = MaterialTheme.typography.titleMedium.copy(color = Color.White, fontWeight = FontWeight.Bold, fontSize = 15.sp)
                                )
                            }
                        }
                    }
                }
            }
            Spacer(modifier = Modifier.height(48.dp))
        }
    }
}
