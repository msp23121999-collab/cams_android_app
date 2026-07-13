package com.example.features.parent.widgets

import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import com.example.core.navigation.AppRoutes
import com.example.core.theme.*

@Composable
fun ParentDrawer(
    currentRoute: String,
    onNavigate: (String) -> Unit
) {
    val drawerItemColors = NavigationDrawerItemDefaults.colors(
        selectedContainerColor = CamsNavy.copy(alpha = 0.1f),
        selectedIconColor = CamsNavy,
        selectedTextColor = CamsNavy,
        unselectedIconColor = CamsTextSecondary,
        unselectedTextColor = CamsTextSecondary
    )

    ModalDrawerSheet(
        drawerContainerColor = MaterialTheme.colorScheme.surface,
        drawerContentColor = CamsTextPrimary
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(CamsNavy)
                .padding(vertical = 32.dp, horizontal = 24.dp)
        ) {
            Column {
                Text(
                    "CAMS",
                    style = MaterialTheme.typography.headlineMedium.copy(
                        color = Color.White,
                        fontWeight = FontWeight.Black,
                        letterSpacing = 1.sp
                    )
                )
                Text(
                    "Parent Portal",
                    style = MaterialTheme.typography.bodySmall.copy(
                        color = Color.White.copy(alpha = 0.7f)
                    )
                )
            }
        }

        Spacer(Modifier.height(16.dp))

        NavigationDrawerItem(
            icon = { Icon(Icons.Filled.Dashboard, contentDescription = null) },
            label = { Text("Dashboard") },
            selected = currentRoute == AppRoutes.PARENT_DASHBOARD,
            onClick = { onNavigate(AppRoutes.PARENT_DASHBOARD) },
            modifier = Modifier.padding(NavigationDrawerItemDefaults.ItemPadding),
            colors = drawerItemColors
        )
        NavigationDrawerItem(
            icon = { Icon(Icons.Filled.Notifications, contentDescription = null) },
            label = { Text("Circular Notices") },
            selected = currentRoute == AppRoutes.PARENT_CIRCULARS,
            onClick = { onNavigate(AppRoutes.PARENT_CIRCULARS) },
            modifier = Modifier.padding(NavigationDrawerItemDefaults.ItemPadding),
            colors = drawerItemColors
        )
        NavigationDrawerItem(
            icon = { Icon(Icons.Filled.Person, contentDescription = null) },
            label = { Text("Child Profile") },
            selected = currentRoute == AppRoutes.PARENT_PROFILE,
            onClick = { onNavigate(AppRoutes.PARENT_PROFILE) },
            modifier = Modifier.padding(NavigationDrawerItemDefaults.ItemPadding),
            colors = drawerItemColors
        )
        NavigationDrawerItem(
            icon = { Icon(Icons.Filled.EventAvailable, contentDescription = null) },
            label = { Text("Attendance") },
            selected = currentRoute == AppRoutes.PARENT_ATTENDANCE,
            onClick = { onNavigate(AppRoutes.PARENT_ATTENDANCE) },
            modifier = Modifier.padding(NavigationDrawerItemDefaults.ItemPadding),
            colors = drawerItemColors
        )
        NavigationDrawerItem(
            icon = { Icon(Icons.Filled.School, contentDescription = null) },
            label = { Text("Marks") },
            selected = currentRoute == AppRoutes.PARENT_MARKS,
            onClick = { onNavigate(AppRoutes.PARENT_MARKS) },
            modifier = Modifier.padding(NavigationDrawerItemDefaults.ItemPadding),
            colors = drawerItemColors
        )
        NavigationDrawerItem(
            icon = { Icon(Icons.Filled.CalendarMonth, contentDescription = null) },
            label = { Text("Timetable") },
            selected = currentRoute == AppRoutes.PARENT_TIMETABLE,
            onClick = { onNavigate(AppRoutes.PARENT_TIMETABLE) },
            modifier = Modifier.padding(NavigationDrawerItemDefaults.ItemPadding),
            colors = drawerItemColors
        )
        NavigationDrawerItem(
            icon = { Icon(Icons.Filled.AccountBalanceWallet, contentDescription = null) },
            label = { Text("Fee Status") },
            selected = currentRoute == AppRoutes.PARENT_FEES,
            onClick = { onNavigate(AppRoutes.PARENT_FEES) },
            modifier = Modifier.padding(NavigationDrawerItemDefaults.ItemPadding),
            colors = drawerItemColors
        )
        NavigationDrawerItem(
            icon = { Icon(Icons.Filled.ContactSupport, contentDescription = null) },
            label = { Text("Contact College") },
            selected = currentRoute == AppRoutes.PARENT_CONTACT,
            onClick = { onNavigate(AppRoutes.PARENT_CONTACT) },
            modifier = Modifier.padding(NavigationDrawerItemDefaults.ItemPadding),
            colors = drawerItemColors
        )
        NavigationDrawerItem(
            icon = { Icon(Icons.Filled.Settings, contentDescription = null) },
            label = { Text("Settings") },
            selected = currentRoute == AppRoutes.PARENT_SETTINGS,
            onClick = { onNavigate(AppRoutes.PARENT_SETTINGS) },
            modifier = Modifier.padding(NavigationDrawerItemDefaults.ItemPadding),
            colors = drawerItemColors
        )

        Spacer(Modifier.weight(1f))

        NavigationDrawerItem(
            icon = { Icon(Icons.Filled.Logout, contentDescription = null) },
            label = { Text("Logout") },
            selected = false,
            onClick = { onNavigate("LOGOUT") },
            modifier = Modifier.padding(NavigationDrawerItemDefaults.ItemPadding),
            colors = NavigationDrawerItemDefaults.colors(
                unselectedIconColor = MaterialTheme.colorScheme.error,
                unselectedTextColor = MaterialTheme.colorScheme.error
            )
        )
        Spacer(Modifier.height(16.dp))
    }
}
