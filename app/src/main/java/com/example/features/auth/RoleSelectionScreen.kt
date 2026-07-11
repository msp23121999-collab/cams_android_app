package com.example.features.auth

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountBalance
import androidx.compose.material.icons.rounded.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.core.theme.*

@Composable
fun RoleSelectionScreen(
    onRoleSelected: (String) -> Unit
) {
    val roles = listOf(
        RoleInfo("Student", Icons.Rounded.School, "Access schedules, grades, assignments, & fees", Color(0xFF3B82F6)),
        RoleInfo("Parent", Icons.Rounded.FamilyRestroom, "Monitor academic progress, attendance, & dues", Color(0xFF10B981)),
        RoleInfo("Faculty", Icons.Rounded.LocalLibrary, "Manage classes, marks, attendance, & timetables", Color(0xFF8B5CF6)),
        RoleInfo("HOD", Icons.Rounded.AccountBox, "Approve timetables, manage departments, & staff", Color(0xFFF59E0B)),
        RoleInfo("Principal", Icons.Rounded.Domain, "Oversee college performance, approvals, & reports", Color(0xFFEC4899)),
        RoleInfo("Admin", Icons.Rounded.AdminPanelSettings, "Configure system, users, roles, & maintenance", Color(0xFF64748B))
    )

    Scaffold(
        containerColor = CamsBackground
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
            // 1. Curved Navy Blue Premium Header
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(bottomStart = 32.dp, bottomEnd = 32.dp))
                    .background(
                        Brush.verticalGradient(
                            colors = listOf(CamsNavy, Color(0xFF0F172A))
                        )
                    )
                    .padding(top = 40.dp, start = 24.dp, end = 24.dp, bottom = 48.dp)
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.fillMaxWidth()) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.Center
                    ) {
                        Surface(
                            modifier = Modifier.size(36.dp),
                            shape = CircleShape,
                            color = Color.White.copy(alpha = 0.15f)
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Icon(
                                    Icons.Filled.AccountBalance,
                                    contentDescription = null,
                                    tint = Color.White,
                                    modifier = Modifier.size(18.dp)
                                )
                            }
                        }
                        Spacer(modifier = Modifier.width(10.dp))
                        Text(
                            "CAMS ENTERPRISE",
                            style = MaterialTheme.typography.titleMedium.copy(
                                color = Color.White,
                                fontWeight = FontWeight.Bold,
                                fontSize = 16.sp,
                                letterSpacing = 2.sp
                            )
                        )
                    }
                    Spacer(modifier = Modifier.height(28.dp))
                    Text(
                        "Select Your Portal",
                        style = MaterialTheme.typography.headlineMedium.copy(
                            color = Color.White,
                            fontWeight = FontWeight.Black,
                            fontSize = 28.sp,
                            letterSpacing = (-0.5).sp
                        ),
                        textAlign = TextAlign.Center
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        "Choose the dedicated ecosystem to access your resources",
                        style = MaterialTheme.typography.bodyMedium.copy(
                            color = Color.White.copy(alpha = 0.65f)
                        ),
                        textAlign = TextAlign.Center,
                        modifier = Modifier.padding(horizontal = 16.dp)
                    )
                }
            }

            // 2. Grid Layout of Premium Cards (2-Columns)
            Column(
                modifier = Modifier
                    .padding(horizontal = 16.dp)
                    .offset(y = (-20).dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                for (i in roles.indices step 2) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        // Left Card
                        Box(modifier = Modifier.weight(1f)) {
                            PortalCard(role = roles[i], onRoleSelected = onRoleSelected)
                        }
                        // Right Card
                        Box(modifier = Modifier.weight(1f)) {
                            if (i + 1 < roles.size) {
                                PortalCard(role = roles[i + 1], onRoleSelected = onRoleSelected)
                            } else {
                                Spacer(modifier = Modifier.fillMaxWidth())
                            }
                        }
                    }
                }
            }
            Spacer(modifier = Modifier.height(32.dp))
        }
    }
}

@Composable
private fun PortalCard(
    role: RoleInfo,
    onRoleSelected: (String) -> Unit
) {
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .height(180.dp)
            .clip(RoundedCornerShape(20.dp))
            .clickable { onRoleSelected(role.name) },
        shape = RoundedCornerShape(20.dp),
        color = Color.White,
        shadowElevation = 4.dp,
        tonalElevation = 1.dp
    ) {
        Column(
            modifier = Modifier
                .padding(16.dp)
                .fillMaxSize(),
            horizontalAlignment = Alignment.Start,
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            // Icon Container with Tint
            Box(
                modifier = Modifier
                    .size(48.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(role.color.copy(alpha = 0.1f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = role.icon,
                    contentDescription = role.name,
                    tint = role.color,
                    modifier = Modifier.size(24.dp)
                )
            }

            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Text(
                    text = role.name,
                    style = MaterialTheme.typography.titleMedium.copy(
                        fontWeight = FontWeight.Bold,
                        color = CamsTextPrimary,
                        fontSize = 16.sp
                    )
                )
                Text(
                    text = role.description,
                    style = MaterialTheme.typography.bodySmall.copy(
                        color = CamsTextSecondary,
                        lineHeight = 15.sp,
                        fontSize = 11.sp
                    ),
                    maxLines = 3
                )
            }
        }
    }
}

data class RoleInfo(
    val name: String,
    val icon: ImageVector,
    val description: String,
    val color: Color
)

