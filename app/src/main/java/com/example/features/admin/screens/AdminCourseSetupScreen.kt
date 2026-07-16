package com.example.features.admin.screens

import androidx.compose.foundation.layout.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.example.features.admin.widgets.AdminBaseScreen
import com.example.core.navigation.AppRoutes
import com.example.core.ui.EnterpriseEmptyState

@Composable
fun AdminCourseSetupScreen(onNavigate: (String) -> Unit) {
    AdminBaseScreen(
        title = "Course Setup",
        currentRoute = "admin-course-setup",
        onNavigate = onNavigate
    ) {
        EnterpriseEmptyState(
            title = "Module Not Provisioned",
            message = "This feature is currently not provisioned in the enterprise backend. Please contact the system administrator.",
            modifier = Modifier.fillMaxSize()
        )
    }
}
