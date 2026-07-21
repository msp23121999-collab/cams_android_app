package com.example.features.auth

import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.rounded.LockReset
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.core.theme.*
import com.example.features.auth.providers.AuthViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ForgotPasswordScreen(
    authViewModel: AuthViewModel,
    onBack: () -> Unit,
    onHaveResetCode: () -> Unit = onBack
) {
    var email by remember { mutableStateOf("") }
    var localError by remember { mutableStateOf<String?>(null) }
    val resetState by authViewModel.passwordResetState.collectAsStateWithLifecycle()

    // Reset any leftover state from a previous visit to this screen.
    DisposableEffect(Unit) {
        authViewModel.clearPasswordResetState()
        onDispose { }
    }

    Scaffold(containerColor = MaterialTheme.colorScheme.background) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    Brush.verticalGradient(colors = listOf(CamsNavy.copy(alpha = 0.05f), CamsBackground))
                )
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
                    Icon(
                        imageVector = Icons.Rounded.LockReset,
                        contentDescription = null,
                        tint = CamsNavy,
                        modifier = Modifier.size(40.dp)
                    )
                }

                Spacer(modifier = Modifier.height(24.dp))

                Text(
                    "Forgot Password",
                    style = MaterialTheme.typography.headlineMedium.copy(
                        color = CamsNavy,
                        fontWeight = FontWeight.Black,
                        letterSpacing = 1.5.sp,
                        fontSize = 24.sp
                    ),
                    textAlign = TextAlign.Center
                )

                Spacer(modifier = Modifier.height(6.dp))

                Text(
                    "Enter the email associated with your account and we'll send you a link to reset your password.",
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
                    Column(
                        modifier = Modifier.padding(24.dp),
                        verticalArrangement = Arrangement.spacedBy(20.dp)
                    ) {
                        if (resetState.success) {
                            Text(
                                text = resetState.message ?: "If an account with that email exists, a reset link has been sent.",
                                style = MaterialTheme.typography.bodyMedium.copy(color = Color(0xFF10B981), fontWeight = FontWeight.Medium),
                                textAlign = TextAlign.Center,
                                modifier = Modifier.fillMaxWidth()
                            )
                            Button(
                                onClick = onHaveResetCode,
                                modifier = Modifier.fillMaxWidth().height(52.dp),
                                shape = RoundedCornerShape(14.dp),
                                colors = ButtonDefaults.buttonColors(containerColor = CamsNavy)
                            ) {
                                Text("I Have a Reset Code", color = Color.White, fontWeight = FontWeight.Bold)
                            }
                            TextButton(
                                onClick = onBack,
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Text("Back to Login", color = CamsNavy, fontWeight = FontWeight.Bold)
                            }
                        } else {
                            OutlinedTextField(
                                value = email,
                                onValueChange = { email = it; localError = null },
                                label = { Text("Email Address") },
                                leadingIcon = { Icon(Icons.Filled.Email, contentDescription = "Email Icon") },
                                trailingIcon = {
                                    if (email.isNotEmpty()) {
                                        IconButton(onClick = { email = "" }) {
                                            Icon(Icons.Filled.Clear, "Clear Email")
                                        }
                                    }
                                },
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(14.dp),
                                singleLine = true,
                                isError = resetState.error != null || localError != null
                            )

                            val displayedError = localError ?: resetState.error
                            if (displayedError != null) {
                                Text(
                                    text = displayedError,
                                    color = Color(0xFFEF4444),
                                    style = MaterialTheme.typography.bodySmall
                                )
                            }

                            Button(
                                onClick = {
                                    if (!android.util.Patterns.EMAIL_ADDRESS.matcher(email.trim()).matches()) {
                                        localError = "Enter a valid email address."
                                    } else {
                                        localError = null
                                        authViewModel.requestPasswordReset(email.trim())
                                    }
                                },
                                modifier = Modifier.fillMaxWidth().height(52.dp),
                                shape = RoundedCornerShape(14.dp),
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = CamsNavy,
                                    disabledContainerColor = CamsNavy.copy(alpha = 0.5f)
                                ),
                                enabled = !resetState.isLoading && email.isNotBlank()
                            ) {
                                Text(
                                    text = if (resetState.isLoading) "Sending..." else "Send Reset Link",
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
