package com.example.features.auth

import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.rounded.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.material3.MaterialTheme
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
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
fun LoginScreen(
    onDebugTap: () -> Unit = {},
    role: String = "Student",
    authViewModel: AuthViewModel,
    onLoginSuccess: (String) -> Unit,
    onBack: () -> Unit = {}
) {
    // Normalize role string (e.g. STUDENT -> Student)
    val currentRole = remember(role) {
        val trimmed = role.trim().uppercase()
        when (trimmed) {
            "STUDENT" -> "Student"
            "PARENT" -> "Parent"
            "FACULTY" -> "Faculty"
            "HOD" -> "HOD"
            "PRINCIPAL" -> "Principal"
            "ADMIN" -> "Admin"
            else -> trimmed.lowercase().replaceFirstChar { if (it.isLowerCase()) it.titlecase() else it.toString() }
        }
    }

    val roleDetails = remember(currentRole) {
        when (currentRole) {
            "Student" -> RoleUIConfig(Icons.Rounded.School, Color(0xFF3B82F6), "STUDENT PORTAL")
            "Parent" -> RoleUIConfig(Icons.Rounded.FamilyRestroom, Color(0xFF10B981), "PARENT PORTAL")
            "Faculty" -> RoleUIConfig(Icons.Rounded.LocalLibrary, Color(0xFF8B5CF6), "FACULTY PORTAL")
            "HOD" -> RoleUIConfig(Icons.Rounded.AccountBox, Color(0xFFF59E0B), "HOD PORTAL")
            "Principal" -> RoleUIConfig(Icons.Rounded.Domain, Color(0xFFEC4899), "PRINCIPAL PORTAL")
            "Admin" -> RoleUIConfig(Icons.Rounded.AdminPanelSettings, Color(0xFF64748B), "ADMIN PORTAL")
            else -> RoleUIConfig(Icons.Rounded.School, Color(0xFF1A365D), "$currentRole Portal".uppercase())
        }
    }

    // Start with empty credentials per Enterprise standard
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var passwordVisible by remember { mutableStateOf(false) }

    // Focus states for input fields
    var isEmailFocused by remember { mutableStateOf(false) }
    var isPasswordFocused by remember { mutableStateOf(false) }

    // Debug screen secret trigger
    var tapCount by remember { mutableStateOf(0) }
    var lastTapTime by remember { mutableStateOf(0L) }

    val authState by authViewModel.uiState.collectAsStateWithLifecycle()

    // Trigger success callback on token change
    LaunchedEffect(authState.token, authState.role) {
        if (authState.token != null && authState.role != null) {
            onLoginSuccess(authState.role!!)
        }
    }

    // Physical Card Shake Animation Sequence on Login Error
    val shakeOffsetX = remember { Animatable(0f) }
    LaunchedEffect(authState.error) {
        if (authState.error != null) {
            for (i in 0..2) {
                shakeOffsetX.animateTo(-12f, animationSpec = tween(45, easing = LinearEasing))
                shakeOffsetX.animateTo(12f, animationSpec = tween(45, easing = LinearEasing))
            }
            shakeOffsetX.animateTo(0f, animationSpec = tween(45, easing = LinearEasing))
        }
    }

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    Brush.verticalGradient(
                        colors = listOf(CamsNavy.copy(alpha = 0.05f), CamsBackground)
                    )
                )
                .verticalScroll(rememberScrollState())
                .padding(padding)
        ) {
            // Action bar with back button
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 8.dp, vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(onClick = onBack) {
                    Icon(
                        Icons.Filled.ArrowBack,
                        contentDescription = "Back",
                        tint = MaterialTheme.colorScheme.primary
                    )
                }
            }

            // Top Section (Large Portal Icon, Title, Subtitle)
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 24.dp, vertical = 16.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Large Portal Icon with subtle halo
                Box(
                    modifier = Modifier
                        .size(80.dp)
                        .clip(RoundedCornerShape(24.dp))
                        .background(roleDetails.color.copy(alpha = 0.12f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = roleDetails.icon,
                        contentDescription = "${roleDetails.title} Icon",
                        tint = roleDetails.color,
                        modifier = Modifier.size(40.dp)
                    )
                }

                Spacer(modifier = Modifier.height(24.dp))

                // Portal Title (With hidden debug trigger)
                Text(
                    text = roleDetails.title,
                    style = MaterialTheme.typography.headlineMedium.copy(
                        color = CamsNavy,
                        fontWeight = FontWeight.Black,
                        letterSpacing = 1.5.sp,
                        fontSize = 24.sp
                    ),
                    textAlign = TextAlign.Center,
                    modifier = Modifier.clickable {
                        val now = System.currentTimeMillis()
                        if (now - lastTapTime > 500) {
                            tapCount = 1
                        } else {
                            tapCount++
                        }
                        lastTapTime = now
                        if (tapCount >= 5) {
                            tapCount = 0
                            onDebugTap()
                        }
                    }
                )

                Spacer(modifier = Modifier.height(6.dp))

                Text(
                    text = "Please sign in to continue",
                    style = MaterialTheme.typography.bodyMedium.copy(
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        fontWeight = FontWeight.Medium
                    ),
                    textAlign = TextAlign.Center
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Rounded Modern White Login Card
            Column(
                modifier = Modifier
                    .padding(horizontal = 24.dp)
                    .offset(x = shakeOffsetX.value.dp)
            ) {
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
                        // Email Address Input
                        val emailBorderColor by animateColorAsState(
                            targetValue = when {
                                authState.error != null -> Color(0xFFEF4444)
                                isEmailFocused -> roleDetails.color
                                else -> Color.LightGray.copy(alpha = 0.4f)
                            },
                            label = "emailBorder"
                        )
                        val emailBg by animateColorAsState(
                            targetValue = if (isEmailFocused) roleDetails.color.copy(alpha = 0.01f) else Color.White,
                            label = "emailBg"
                        )

                        OutlinedTextField(
                            value = email,
                            onValueChange = { email = it },
                            label = { Text("Email Address") },
                            leadingIcon = {
                                Icon(
                                    Icons.Filled.Email,
                                    contentDescription = "Email Icon",
                                    tint = if (isEmailFocused) roleDetails.color else CamsTextSecondary
                                )
                            },
                            trailingIcon = {
                                if (email.isNotEmpty()) {
                                    IconButton(onClick = { email = "" }) {
                                        Icon(Icons.Filled.Clear, "Clear Email", tint = MaterialTheme.colorScheme.onSurfaceVariant)
                                    }
                                }
                            },
                            modifier = Modifier
                                .fillMaxWidth()
                                .onFocusChanged { isEmailFocused = it.isFocused },
                            shape = RoundedCornerShape(14.dp),
                            singleLine = true,
                            textStyle = androidx.compose.ui.text.TextStyle(color = Color(0xFF1E293B)),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = emailBorderColor,
                                unfocusedBorderColor = emailBorderColor,
                                focusedContainerColor = emailBg,
                                unfocusedContainerColor = emailBg,
                                focusedLabelColor = roleDetails.color,
                                unfocusedLabelColor = CamsTextSecondary,
                                cursorColor = roleDetails.color
                            )
                        )

                        // Password Input
                        val passwordBorderColor by animateColorAsState(
                            targetValue = when {
                                authState.error != null -> Color(0xFFEF4444)
                                isPasswordFocused -> roleDetails.color
                                else -> Color.LightGray.copy(alpha = 0.4f)
                            },
                            label = "passBorder"
                        )
                        val passwordBg by animateColorAsState(
                            targetValue = if (isPasswordFocused) roleDetails.color.copy(alpha = 0.01f) else Color.White,
                            label = "passBg"
                        )

                        OutlinedTextField(
                            value = password,
                            onValueChange = { password = it },
                            label = { Text("Password") },
                            leadingIcon = {
                                Icon(
                                    Icons.Filled.Key,
                                    contentDescription = "Password Icon",
                                    tint = if (isPasswordFocused) roleDetails.color else CamsTextSecondary
                                )
                            },
                            trailingIcon = {
                                IconButton(onClick = { passwordVisible = !passwordVisible }) {
                                    val icon = if (passwordVisible) Icons.Filled.Visibility else Icons.Filled.VisibilityOff
                                    Icon(icon, "Toggle Password Visibility", tint = MaterialTheme.colorScheme.onSurfaceVariant)
                                }
                            },
                            modifier = Modifier
                                .fillMaxWidth()
                                .onFocusChanged { isPasswordFocused = it.isFocused },
                            shape = RoundedCornerShape(14.dp),
                            singleLine = true,
                            visualTransformation = if (passwordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                            textStyle = androidx.compose.ui.text.TextStyle(color = Color(0xFF1E293B)),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = passwordBorderColor,
                                unfocusedBorderColor = passwordBorderColor,
                                focusedContainerColor = passwordBg,
                                unfocusedContainerColor = passwordBg,
                                focusedLabelColor = roleDetails.color,
                                unfocusedLabelColor = CamsTextSecondary,
                                cursorColor = roleDetails.color
                            )
                        )

                        // Enterprise Quick Access Demo Login Button (Fills credentials, user presses SIGN IN)
                        Button(
                            onClick = {
                                email = "${currentRole.lowercase()}@cams.local"
                                password = "Password@1"
                            },
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(14.dp),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = roleDetails.color.copy(alpha = 0.08f),
                                contentColor = roleDetails.color
                            ),
                            elevation = ButtonDefaults.buttonElevation(defaultElevation = 0.dp)
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.Center
                            ) {
                                Icon(
                                    Icons.Rounded.FlashOn,
                                    contentDescription = null,
                                    tint = roleDetails.color,
                                    modifier = Modifier.size(16.dp)
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    "🚀 Use Demo Account",
                                    style = MaterialTheme.typography.titleSmall.copy(
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 13.sp
                                    )
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(4.dp))

                        // Large Sign In Button
                        Button(
                            onClick = {
                                authViewModel.login(email, password)
                            },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(52.dp),
                            shape = RoundedCornerShape(14.dp),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = CamsNavy,
                                disabledContainerColor = CamsNavy.copy(alpha = 0.5f)
                            ),
                            enabled = !authState.isLoading && email.isNotEmpty() && password.isNotEmpty()
                        ) {
                            Text(
                                text = if (authState.isLoading) "Signing in securely..." else "Sign In",
                                style = MaterialTheme.typography.titleMedium.copy(
                                    color = Color.White,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 15.sp
                                )
                            )
                        }

                        // Forgot Password
                        TextButton(
                            onClick = { /* Forgot password */ },
                            modifier = Modifier.align(Alignment.CenterHorizontally)
                        ) {
                            Text(
                                "Forgot Password?",
                                color = roleDetails.color,
                                fontWeight = FontWeight.Bold,
                                fontSize = 13.sp
                            )
                        }
                    }
                }
            }
            Spacer(modifier = Modifier.height(48.dp))
        }
    }
}

data class RoleUIConfig(
    val icon: ImageVector,
    val color: Color,
    val title: String
)
