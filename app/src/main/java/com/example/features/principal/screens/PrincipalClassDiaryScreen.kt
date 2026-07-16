package com.example.features.principal.screens

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
import com.example.features.principal.widgets.PrincipalBaseScreen
import com.example.core.navigation.AppRoutes

@Composable
fun PrincipalClassDiaryScreen(onNavigate: (String) -> Unit) {
    var activeTab by remember { mutableStateOf("overview") }

    PrincipalBaseScreen(
        title = "Class Diary & Academic Audit",
        subtitle = "College-wide teaching logs review and real-time attendance audits.",
        currentRoute = AppRoutes.PRINCIPAL_CLASS_DIARY,
        onNavigate = onNavigate
    ) {
        Box(modifier = Modifier.fillMaxSize().padding(32.dp), contentAlignment = Alignment.Center) {
            com.example.core.ui.EnterpriseEmptyState(
                title = "No Class Diaries Found",
                message = "Class diary tracking is not currently active."
            )
        }
    }
}
