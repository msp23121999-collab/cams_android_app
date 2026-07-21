package com.example.features.admin.widgets

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.*
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
fun AdminDrawer(
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
                    Icon(Icons.Filled.AdminPanelSettings, null, tint = Color.White)
                }
                Column {
                    Text("CAMS Admin", fontWeight = FontWeight.Bold, fontSize = 18.sp, color = CamsNavy)
                    Text("System Controls", fontSize = 12.sp, color = Color(0xFF64748B))
                }
            }

            HorizontalDivider(color = Color(0xFFF3F4F6))
            Spacer(modifier = Modifier.height(20.dp))

            // Navigation Items
            DrawerItem("Dashboard", Icons.Filled.Dashboard, AppRoutes.ADMIN_DASHBOARD, currentRoute, onNavigate)
            DrawerItem("Notifications", Icons.Filled.Notifications, AppRoutes.ADMIN_NOTIFICATIONS, currentRoute, onNavigate)
            DrawerItem("User Mgmt", Icons.Filled.People, AppRoutes.ADMIN_USER_MGMT, currentRoute, onNavigate)
            DrawerItem("Fee Mgmt", Icons.Filled.AccountBalanceWallet, AppRoutes.ADMIN_FEE_MGMT, currentRoute, onNavigate)
            DrawerItem("Collect Fee", Icons.Filled.Payment, AppRoutes.ADMIN_COLLECT_FEE, currentRoute, onNavigate)
            DrawerItem("Salary Mgmt", Icons.Filled.Payments, AppRoutes.ADMIN_SALARY_MGMT, currentRoute, onNavigate)
            DrawerItem("Exam Mgmt", Icons.Filled.Event, AppRoutes.ADMIN_EXAM_MGMT, currentRoute, onNavigate)
            DrawerItem("Faculty Assign", Icons.Filled.AssignmentInd, AppRoutes.ADMIN_FACULTY_ASSIGNMENT, currentRoute, onNavigate)
            DrawerItem("Academic Cal", Icons.Filled.CalendarMonth, AppRoutes.ADMIN_ACADEMIC_CALENDAR, currentRoute, onNavigate)
            DrawerItem("Academic Catalog", Icons.Filled.CollectionsBookmark, AppRoutes.ADMIN_ACADEMIC_CATALOG, currentRoute, onNavigate)
            DrawerItem("Course Setup", Icons.AutoMirrored.Filled.MenuBook, AppRoutes.ADMIN_COURSE_SETUP, currentRoute, onNavigate)
            DrawerItem("Batch Setup", Icons.Filled.Layers, AppRoutes.ADMIN_BATCH_SETUP, currentRoute, onNavigate)
            DrawerItem("Academic Year Config", Icons.Filled.DateRange, AppRoutes.ADMIN_ACADEMIC_YEAR_CONFIG, currentRoute, onNavigate)
            DrawerItem("Attendance Defaulters", Icons.Filled.WarningAmber, AppRoutes.ADMIN_ATTENDANCE_DEFAULTERS, currentRoute, onNavigate)
            DrawerItem("Circulars", Icons.Filled.Campaign, AppRoutes.ADMIN_CIRCULARS, currentRoute, onNavigate)
            DrawerItem("Reports", Icons.Filled.Assessment, AppRoutes.ADMIN_REPORTS, currentRoute, onNavigate)

            Spacer(modifier = Modifier.height(12.dp))
            Text("CAMPUS OPERATIONS", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = Color(0xFF94A3B8), modifier = Modifier.padding(start = 16.dp, bottom = 4.dp))
            DrawerItem("Hostel", Icons.Filled.Hotel, AppRoutes.ADMIN_HOSTEL, currentRoute, onNavigate)
            DrawerItem("Inventory", Icons.Filled.Inventory2, AppRoutes.ADMIN_INVENTORY, currentRoute, onNavigate)
            DrawerItem("Library", Icons.Filled.LocalLibrary, AppRoutes.ADMIN_LIBRARY, currentRoute, onNavigate)
            DrawerItem("Transport", Icons.Filled.DirectionsBus, AppRoutes.ADMIN_TRANSPORT, currentRoute, onNavigate)

            Spacer(modifier = Modifier.height(12.dp))
            Text("SYSTEM", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = Color(0xFF94A3B8), modifier = Modifier.padding(start = 16.dp, bottom = 4.dp))
            DrawerItem("System Config", Icons.Filled.Settings, AppRoutes.ADMIN_SYSTEM_CONFIG, currentRoute, onNavigate)
            DrawerItem("Logs", Icons.Filled.Receipt, AppRoutes.ADMIN_LOGS, currentRoute, onNavigate)
            DrawerItem("Backups", Icons.Filled.Backup, AppRoutes.ADMIN_BACKUPS, currentRoute, onNavigate)

            Spacer(modifier = Modifier.weight(1f))

            HorizontalDivider(color = Color(0xFFF3F4F6))
            Spacer(modifier = Modifier.height(10.dp))
            
            DrawerItem("Logout", Icons.AutoMirrored.Filled.Logout, "LOGOUT", currentRoute, onNavigate, color = Color.Red)
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
