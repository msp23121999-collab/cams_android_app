package com.example.features.hod.widgets

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.core.theme.*
import com.example.core.navigation.AppRoutes

@Composable
fun HODDrawer(
    currentRoute: String,
    onNavigate: (String) -> Unit
) {
    ModalDrawerSheet(
        drawerContainerColor = MaterialTheme.colorScheme.surface,
        drawerShape = RoundedCornerShape(topEnd = 24.dp, bottomEnd = 24.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxHeight()
                .padding(20.dp)
        ) {
            // Drawer Header
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(16.dp),
                modifier = Modifier.padding(vertical = 20.dp)
            ) {
                Box(
                    modifier = Modifier
                        .size(50.dp)
                        .clip(CircleShape)
                        .background(CamsNavy),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(Icons.Filled.Category, null, tint = Color.White)
                }
                Column {
                    Text("CAMS HOD", fontWeight = FontWeight.Bold, fontSize = 18.sp, color = CamsNavy)
                    Text("Department Head Console", fontSize = 12.sp, color = Color(0xFF64748B))
                }
            }

            Divider(color = Color(0xFFF3F4F6))
            Spacer(modifier = Modifier.height(20.dp))

            // Navigation Items
            DrawerItem("Dashboard", Icons.Filled.Dashboard, AppRoutes.HOD_DASHBOARD, currentRoute, onNavigate)
            DrawerItem("Faculty Management", Icons.Filled.Group, AppRoutes.HOD_FACULTY_MGMT, currentRoute, onNavigate)
            DrawerItem("Student Management", Icons.Filled.School, AppRoutes.HOD_STUDENT_MGMT, currentRoute, onNavigate)
            DrawerItem("Academic Monitor", Icons.Filled.MenuBook, AppRoutes.HOD_ACADEMIC_PROGRESS, currentRoute, onNavigate)
            DrawerItem("Timetable", Icons.Filled.CalendarMonth, AppRoutes.HOD_TIMETABLE_MGMT, currentRoute, onNavigate)
            DrawerItem("Research", Icons.Filled.Science, AppRoutes.HOD_RESEARCH_OVERVIEW, currentRoute, onNavigate)
            DrawerItem("Approvals", Icons.Filled.FactCheck, AppRoutes.HOD_APPROVALS, currentRoute, onNavigate)

            Spacer(modifier = Modifier.weight(1f))

            Divider(color = Color(0xFFF3F4F6))
            Spacer(modifier = Modifier.height(10.dp))
            
            DrawerItem("Logout", Icons.Filled.Logout, "LOGOUT", currentRoute, onNavigate, color = Color.Red)
        }
    }
}

@Composable
private fun DrawerItem(
    label: String,
    icon: ImageVector,
    route: String,
    currentRoute: String,
    onNavigate: (String) -> Unit,
    color: Color = CamsTextPrimary
) {
    val isSelected = currentRoute == route
    
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp)
            .clickable { onNavigate(route) },
        color = if (isSelected) CamsNavy.copy(alpha = 0.05f) else Color.Transparent,
        shape = RoundedCornerShape(12.dp)
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(16.dp),
            modifier = Modifier.padding(16.dp)
        ) {
            Icon(
                icon,
                contentDescription = null,
                tint = if (isSelected) CamsNavy else if (color == Color.Red) Color.Red else CamsTextSecondary,
                modifier = Modifier.size(22.dp)
            )
            Text(
                label,
                fontSize = 15.sp,
                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                color = if (isSelected) CamsNavy else color
            )
        }
    }
}
