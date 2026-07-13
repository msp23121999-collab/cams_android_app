package com.example.features.principal.widgets

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
fun PrincipalDrawer(
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
                    Icon(Icons.Filled.MilitaryTech, null, tint = Color.White)
                }
                Column {
                    Text("CAMS Principal", fontWeight = FontWeight.Bold, fontSize = 18.sp, color = CamsNavy)
                    Text("Executive Overview", fontSize = 12.sp, color = Color(0xFF64748B))
                }
            }

            HorizontalDivider(color = Color(0xFFF3F4F6))
            Spacer(modifier = Modifier.height(20.dp))

            androidx.compose.foundation.lazy.LazyColumn(modifier = Modifier.weight(1f)) {
                item { DrawerItem("Dashboard", Icons.Filled.Dashboard, AppRoutes.PRINCIPAL_DASHBOARD, currentRoute, onNavigate) }
                item { DrawerItem("Approvals", Icons.Filled.Rule, AppRoutes.PRINCIPAL_APPROVALS, currentRoute, onNavigate) }
                item { DrawerItem("Grievance Inbox", Icons.Filled.HelpOutline, AppRoutes.PRINCIPAL_GRIEVANCES, currentRoute, onNavigate) }
                item { DrawerItem("Publish Notices", Icons.Filled.Campaign, AppRoutes.PRINCIPAL_CIRCULARS, currentRoute, onNavigate) }
                
                item { Spacer(modifier = Modifier.height(16.dp)) }
                item { HorizontalDivider(color = Color(0xFFF3F4F6)) }
                item { Spacer(modifier = Modifier.height(16.dp)) }
                
                item { DrawerItem("Institutional Perf", Icons.Filled.Assessment, AppRoutes.PRINCIPAL_PERFORMANCE, currentRoute, onNavigate) }
                item { DrawerItem("Faculty Overview", Icons.Filled.Groups, AppRoutes.PRINCIPAL_FACULTY_OVERVIEW, currentRoute, onNavigate) }
                item { DrawerItem("Class Diary", Icons.Filled.MenuBook, AppRoutes.PRINCIPAL_CLASS_DIARY, currentRoute, onNavigate) }
                item { DrawerItem("Events Mgmt", Icons.Filled.Event, AppRoutes.PRINCIPAL_EVENTS_MGMT, currentRoute, onNavigate) }
                item { DrawerItem("Infrastructure", Icons.Filled.Domain, AppRoutes.PRINCIPAL_INFRASTRUCTURE, currentRoute, onNavigate) }
                item { DrawerItem("Research Comp.", Icons.Filled.Science, AppRoutes.PRINCIPAL_RESEARCH_COMPLIANCE, currentRoute, onNavigate) }
                item { DrawerItem("Study Materials", Icons.Filled.LibraryBooks, AppRoutes.PRINCIPAL_STUDY_MATERIALS, currentRoute, onNavigate) }
                item { DrawerItem("Budget & Grants", Icons.Filled.AccountBalance, AppRoutes.PRINCIPAL_BUDGET_GRANTS, currentRoute, onNavigate) }
                item { DrawerItem("Institutional Cal", Icons.Filled.CalendarMonth, AppRoutes.PRINCIPAL_CALENDAR, currentRoute, onNavigate) }
            }

            HorizontalDivider(color = Color(0xFFF3F4F6))
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
