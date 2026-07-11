package com.example.core.navigation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect

@Composable
fun RoleGuard(
    currentRole: String?,
    allowedRoles: List<String>,
    isLoading: Boolean = false,
    onUnauthorized: () -> Unit,
    content: @Composable () -> Unit
) {
    LaunchedEffect(currentRole, isLoading) {
        if (!isLoading && (currentRole == null || !allowedRoles.contains(currentRole.uppercase()))) {
            onUnauthorized()
        }
    }

    if (!isLoading && currentRole != null && allowedRoles.contains(currentRole.uppercase())) {
        content()
    } else if (isLoading) {
        // You could show a loading indicator here if needed
    }
}
