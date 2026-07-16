package com.example.features.student.widgets

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.*
import androidx.compose.material.icons.filled.*
import com.example.core.navigation.AppRoutes
import com.example.core.theme.*

@Composable
fun StudentDrawer(
    currentRoute: String,
    onNavigate: (String) -> Unit
) {
    ModalDrawerSheet(
        drawerContainerColor = MaterialTheme.colorScheme.surface,
        drawerContentColor = CamsTextPrimary
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
        ) {
            // Header
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
                        "Student Portal",
                        style = MaterialTheme.typography.bodySmall.copy(
                            color = Color.White.copy(alpha = 0.7f)
                        )
                    )
                }
            }

            Spacer(Modifier.height(16.dp))
            
            val drawerItemColors = NavigationDrawerItemDefaults.colors(
                selectedContainerColor = CamsNavy.copy(alpha = 0.1f),
                selectedIconColor = CamsNavy,
                selectedTextColor = CamsNavy,
                unselectedIconColor = CamsTextSecondary,
                unselectedTextColor = CamsTextSecondary
            )

            // Items
            NavigationDrawerItem(
                icon = { Icon(Icons.Filled.Dashboard, contentDescription = null) },
                label = { Text("Dashboard") },
                selected = currentRoute == AppRoutes.STUDENT_DASHBOARD,
                onClick = { onNavigate(AppRoutes.STUDENT_DASHBOARD) },
                modifier = Modifier.padding(NavigationDrawerItemDefaults.ItemPadding),
                colors = drawerItemColors
            )
        NavigationDrawerItem(
            icon = { Icon(Icons.Filled.Person, contentDescription = null) },
            label = { Text("Profile") },
            selected = currentRoute == AppRoutes.STUDENT_PROFILE,
            onClick = { onNavigate(AppRoutes.STUDENT_PROFILE) },
            modifier = Modifier.padding(NavigationDrawerItemDefaults.ItemPadding),
            colors = drawerItemColors
        )
        NavigationDrawerItem(
            icon = { Icon(Icons.Filled.EventAvailable, contentDescription = null) },
            label = { Text("Attendance") },
            selected = currentRoute == AppRoutes.ATTENDANCE,
            onClick = { onNavigate(AppRoutes.ATTENDANCE) },
            modifier = Modifier.padding(NavigationDrawerItemDefaults.ItemPadding),
            colors = drawerItemColors
        )
        NavigationDrawerItem(
            icon = { Icon(Icons.Filled.CalendarToday, contentDescription = null) },
            label = { Text("Timetable") },
            selected = currentRoute == AppRoutes.TIMETABLE,
            onClick = { onNavigate(AppRoutes.TIMETABLE) },
            modifier = Modifier.padding(NavigationDrawerItemDefaults.ItemPadding),
            colors = drawerItemColors
        )
        NavigationDrawerItem(
            icon = { Icon(Icons.Filled.CalendarMonth, contentDescription = null) },
            label = { Text("Academic Calendar") },
            selected = currentRoute == AppRoutes.STUDENT_CALENDAR,
            onClick = { onNavigate(AppRoutes.STUDENT_CALENDAR) },
            modifier = Modifier.padding(NavigationDrawerItemDefaults.ItemPadding),
            colors = drawerItemColors
        )
        NavigationDrawerItem(
            icon = { Icon(Icons.AutoMirrored.Filled.Assignment, contentDescription = null) },
            label = { Text("Assignments") },
            selected = currentRoute == AppRoutes.ASSIGNMENTS,
            onClick = { onNavigate(AppRoutes.ASSIGNMENTS) },
            modifier = Modifier.padding(NavigationDrawerItemDefaults.ItemPadding),
            colors = drawerItemColors
        )
        NavigationDrawerItem(
            icon = { Icon(Icons.Filled.School, contentDescription = null) },
            label = { Text("Internal Marks") },
            selected = currentRoute == AppRoutes.INTERNAL_MARKS,
            onClick = { onNavigate(AppRoutes.INTERNAL_MARKS) },
            modifier = Modifier.padding(NavigationDrawerItemDefaults.ItemPadding),
            colors = drawerItemColors
        )
        NavigationDrawerItem(
            icon = { Icon(Icons.Filled.ConfirmationNumber, contentDescription = null) },
            label = { Text("Hall Ticket") },
            selected = currentRoute == AppRoutes.STUDENT_HALL_TICKET,
            onClick = { onNavigate(AppRoutes.STUDENT_HALL_TICKET) },
            modifier = Modifier.padding(NavigationDrawerItemDefaults.ItemPadding),
            colors = drawerItemColors
        )
        NavigationDrawerItem(
            icon = { Icon(Icons.Filled.Book, contentDescription = null) },
            label = { Text("Study Materials") },
            selected = currentRoute.startsWith("/student/materials"),
            onClick = { onNavigate("/student/materials") },
            modifier = Modifier.padding(NavigationDrawerItemDefaults.ItemPadding),
            colors = drawerItemColors
        )
        NavigationDrawerItem(
            icon = { Icon(Icons.AutoMirrored.Filled.LibraryBooks, contentDescription = null) },
            label = { Text("Syllabus") },
            selected = currentRoute == AppRoutes.STUDENT_SYLLABUS,
            onClick = { onNavigate(AppRoutes.STUDENT_SYLLABUS) },
            modifier = Modifier.padding(NavigationDrawerItemDefaults.ItemPadding),
            colors = drawerItemColors
        )
        NavigationDrawerItem(
            icon = { Icon(Icons.Filled.Notifications, contentDescription = null) },
            label = { Text("Notifications") },
            selected = currentRoute == AppRoutes.NOTIFICATIONS,
            onClick = { onNavigate(AppRoutes.NOTIFICATIONS) },
            modifier = Modifier.padding(NavigationDrawerItemDefaults.ItemPadding),
            colors = drawerItemColors
        )
        NavigationDrawerItem(
            icon = { Icon(Icons.Filled.AccountBalanceWallet, contentDescription = null) },
            label = { Text("Fees") },
            selected = currentRoute == AppRoutes.STUDENT_FEES,
            onClick = { onNavigate(AppRoutes.STUDENT_FEES) },
            modifier = Modifier.padding(NavigationDrawerItemDefaults.ItemPadding),
            colors = drawerItemColors
        )
        NavigationDrawerItem(
            icon = { Icon(Icons.Filled.FlightTakeoff, contentDescription = null) },
            label = { Text("Leave") },
            selected = currentRoute == AppRoutes.STUDENT_LEAVE,
            onClick = { onNavigate(AppRoutes.STUDENT_LEAVE) },
            modifier = Modifier.padding(NavigationDrawerItemDefaults.ItemPadding),
            colors = drawerItemColors
        )
        NavigationDrawerItem(
            icon = { Icon(Icons.Filled.CompassCalibration, contentDescription = null) },
            label = { Text("Campus Life") },
            selected = currentRoute == AppRoutes.CAMPUS_LIFE,
            onClick = { onNavigate(AppRoutes.CAMPUS_LIFE) },
            modifier = Modifier.padding(NavigationDrawerItemDefaults.ItemPadding),
            colors = drawerItemColors
        )
        NavigationDrawerItem(
            icon = { Icon(Icons.Filled.Shield, contentDescription = null) },
            label = { Text("Student Council") },
            selected = currentRoute == AppRoutes.STUDENT_COUNCIL,
            onClick = { onNavigate(AppRoutes.STUDENT_COUNCIL) },
            modifier = Modifier.padding(NavigationDrawerItemDefaults.ItemPadding),
            colors = drawerItemColors
        )
        NavigationDrawerItem(
            icon = { Icon(Icons.Filled.Campaign, contentDescription = null) },
            label = { Text("Circulars") },
            selected = currentRoute == AppRoutes.CIRCULARS,
            onClick = { onNavigate(AppRoutes.CIRCULARS) },
            modifier = Modifier.padding(NavigationDrawerItemDefaults.ItemPadding),
            colors = drawerItemColors
        )
        NavigationDrawerItem(
            icon = { Icon(Icons.Filled.VideoCall, contentDescription = null) },
            label = { Text("Online Meetings") },
            selected = currentRoute == AppRoutes.ONLINE_MEETINGS,
            onClick = { onNavigate(AppRoutes.ONLINE_MEETINGS) },
            modifier = Modifier.padding(NavigationDrawerItemDefaults.ItemPadding),
            colors = drawerItemColors
        )
        NavigationDrawerItem(
            icon = { Icon(Icons.Filled.Scale, contentDescription = null) },
            label = { Text("LexNova AI Dashboard") },
            selected = currentRoute == AppRoutes.LEXNOVA,
            onClick = { onNavigate(AppRoutes.LEXNOVA) },
            modifier = Modifier.padding(NavigationDrawerItemDefaults.ItemPadding),
            colors = drawerItemColors
        )
        NavigationDrawerItem(
            icon = { Icon(Icons.Filled.EventSeat, contentDescription = null) },
            label = { Text("Legal Events Hub") },
            selected = currentRoute == AppRoutes.LEGAL_EVENTS,
            onClick = { onNavigate(AppRoutes.LEGAL_EVENTS) },
            modifier = Modifier.padding(NavigationDrawerItemDefaults.ItemPadding),
            colors = drawerItemColors
        )
        NavigationDrawerItem(
            icon = { Icon(Icons.Filled.WorkspacePremium, contentDescription = null) },
            label = { Text("Legal Skills & Certs") },
            selected = currentRoute == AppRoutes.LEGAL_SKILLS,
            onClick = { onNavigate(AppRoutes.LEGAL_SKILLS) },
            modifier = Modifier.padding(NavigationDrawerItemDefaults.ItemPadding),
            colors = drawerItemColors
        )
        NavigationDrawerItem(
            icon = { Icon(Icons.Filled.CardMembership, contentDescription = null) },
            label = { Text("Certifications") },
            selected = currentRoute == AppRoutes.CERTIFICATIONS,
            onClick = { onNavigate(AppRoutes.CERTIFICATIONS) },
            modifier = Modifier.padding(NavigationDrawerItemDefaults.ItemPadding),
            colors = drawerItemColors
        )
        NavigationDrawerItem(
            icon = { Icon(Icons.Filled.Stars, contentDescription = null) },
            label = { Text("Project Showcase") },
            selected = currentRoute == AppRoutes.PROJECT_SHOWCASE,
            onClick = { onNavigate(AppRoutes.PROJECT_SHOWCASE) },
            modifier = Modifier.padding(NavigationDrawerItemDefaults.ItemPadding),
            colors = drawerItemColors
        )
        NavigationDrawerItem(
            icon = { Icon(Icons.Filled.Lightbulb, contentDescription = null) },
            label = { Text("Innovation Wall") },
            selected = currentRoute == AppRoutes.INNOVATION_WALL,
            onClick = { onNavigate(AppRoutes.INNOVATION_WALL) },
            modifier = Modifier.padding(NavigationDrawerItemDefaults.ItemPadding),
            colors = drawerItemColors
        )
        NavigationDrawerItem(
            icon = { Icon(Icons.Filled.Favorite, contentDescription = null) },
            label = { Text("Community Service") },
            selected = currentRoute == AppRoutes.COMMUNITY_SERVICE,
            onClick = { onNavigate(AppRoutes.COMMUNITY_SERVICE) },
            modifier = Modifier.padding(NavigationDrawerItemDefaults.ItemPadding),
            colors = drawerItemColors
        )
        NavigationDrawerItem(
            icon = { Icon(Icons.Filled.EmojiEvents, contentDescription = null) },
            label = { Text("Activity Points") },
            selected = currentRoute == AppRoutes.ACTIVITY_POINTS,
            onClick = { onNavigate(AppRoutes.ACTIVITY_POINTS) },
            modifier = Modifier.padding(NavigationDrawerItemDefaults.ItemPadding),
            colors = drawerItemColors
        )
        NavigationDrawerItem(
            icon = { Icon(Icons.Filled.WorkOutline, contentDescription = null) },
            label = { Text("Internships") },
            selected = currentRoute == AppRoutes.INTERNSHIPS,
            onClick = { onNavigate(AppRoutes.INTERNSHIPS) },
            modifier = Modifier.padding(NavigationDrawerItemDefaults.ItemPadding),
            colors = drawerItemColors
        )
        NavigationDrawerItem(
            icon = { Icon(Icons.Filled.Language, contentDescription = null) },
            label = { Text("LexSphere Portal") },
            selected = currentRoute == AppRoutes.LEXSPHERE,
            onClick = { onNavigate(AppRoutes.LEXSPHERE) },
            modifier = Modifier.padding(NavigationDrawerItemDefaults.ItemPadding),
            colors = drawerItemColors
        )
        NavigationDrawerItem(
            icon = { Icon(Icons.Filled.Report, contentDescription = null) },
            label = { Text("Grievances") },
            selected = currentRoute == "/student/grievances",
            onClick = { onNavigate("/student/grievances") },
            modifier = Modifier.padding(NavigationDrawerItemDefaults.ItemPadding),
            colors = drawerItemColors
        )
        NavigationDrawerItem(
            icon = { Icon(Icons.Filled.Settings, contentDescription = null) },
            label = { Text("Settings") },
            selected = currentRoute == AppRoutes.STUDENT_SETTINGS,
            onClick = { onNavigate(AppRoutes.STUDENT_SETTINGS) },
            modifier = Modifier.padding(NavigationDrawerItemDefaults.ItemPadding),
            colors = drawerItemColors
        )
        
        Spacer(Modifier.height(24.dp))
        
        NavigationDrawerItem(
            icon = { Icon(Icons.AutoMirrored.Filled.Logout, contentDescription = null) },
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
}

