package com.example.features.admin.screens

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import com.example.core.navigation.AppRoutes
import com.example.features.admin.widgets.AdminBaseScreen

@Composable
fun AdminCourseSetupScreen(onNavigate: (String) -> Unit) {
    // Navigates to Academic Catalog where the actual course setup is
    LaunchedEffect(Unit) {
        onNavigate(AppRoutes.ADMIN_ACADEMIC_CATALOG)
    }
    
    AdminBaseScreen(
        title = "Course Setup",
        currentRoute = AppRoutes.ADMIN_COURSE_SETUP,
        onNavigate = onNavigate
    ) {
        // Redirecting
    }
}
