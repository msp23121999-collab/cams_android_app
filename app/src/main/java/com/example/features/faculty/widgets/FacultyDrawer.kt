package com.example.features.faculty.widgets

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
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

@Composable
fun FacultyDrawer(
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
                    Icon(Icons.Filled.School, null, tint = Color.White)
                }
                Column {
                    Text("CAMS Faculty", fontWeight = FontWeight.Bold, fontSize = 18.sp, color = CamsNavy)
                    Text("Digital Workspace", fontSize = 12.sp, color = Color(0xFF64748B))
                }
            }

            HorizontalDivider(color = Color(0xFFF3F4F6))
            Spacer(modifier = Modifier.height(20.dp))

            // Navigation Items
            DrawerItem("Dashboard", Icons.Filled.Dashboard, "/faculty/dashboard", currentRoute, onNavigate)
            DrawerItem("Profile", Icons.Filled.Person, "/faculty/profile", currentRoute, onNavigate)
            DrawerItem("Student Directory", Icons.Filled.People, "/faculty/students", currentRoute, onNavigate)
            DrawerItem("Attendance", Icons.Filled.HowToReg, "/faculty/attendance", currentRoute, onNavigate)
            DrawerItem("Assignments", Icons.Filled.Assignment, "/faculty/assignments", currentRoute, onNavigate)
            DrawerItem("Marks Entry", Icons.Filled.Grade, "/faculty/marks-entry", currentRoute, onNavigate)
            DrawerItem("Study Materials", Icons.Filled.MenuBook, "/faculty/study-materials", currentRoute, onNavigate)
            DrawerItem("Lecture Recordings", Icons.Filled.VideoLibrary, "/faculty/lecture-recordings", currentRoute, onNavigate)
            DrawerItem("Smart Classroom", Icons.Filled.SettingsRemote, com.example.core.navigation.AppRoutes.FACULTY_SMART_CLASSROOM, currentRoute, onNavigate)
            DrawerItem("Legal Events", Icons.Filled.Gavel, com.example.core.navigation.AppRoutes.FACULTY_LEGAL_EVENTS, currentRoute, onNavigate)
            DrawerItem("Internships", Icons.Filled.BusinessCenter, com.example.core.navigation.AppRoutes.FACULTY_INTERNSHIPS, currentRoute, onNavigate)
            DrawerItem("Activity Points", Icons.Filled.Star, com.example.core.navigation.AppRoutes.FACULTY_ACTIVITY_POINTS, currentRoute, onNavigate)
            DrawerItem("Research Tracker", Icons.Filled.Science, com.example.core.navigation.AppRoutes.FACULTY_RESEARCH_TRACKER, currentRoute, onNavigate)
            DrawerItem("Timetable", Icons.Filled.Schedule, "/faculty/timetable", currentRoute, onNavigate)
            DrawerItem("Academic Calendar", Icons.Filled.CalendarMonth, "/faculty/calendar", currentRoute, onNavigate)
            DrawerItem("Salary Slip", Icons.Filled.Payments, com.example.core.navigation.AppRoutes.FACULTY_SALARY_SLIP, currentRoute, onNavigate)
            DrawerItem("Online Meetings", Icons.Filled.VideoCall, com.example.core.navigation.AppRoutes.FACULTY_ONLINE_MEETINGS, currentRoute, onNavigate)
            DrawerItem("Notifications", Icons.Filled.Notifications, com.example.core.navigation.AppRoutes.FACULTY_NOTIFICATIONS, currentRoute, onNavigate)
            DrawerItem("Apply Leave", Icons.Filled.EventBusy, com.example.core.navigation.AppRoutes.FACULTY_LEAVE_APPLY, currentRoute, onNavigate)
            DrawerItem("Communication", Icons.Filled.Chat, com.example.core.navigation.AppRoutes.FACULTY_COMMUNICATION, currentRoute, onNavigate)
            DrawerItem("Circulars", Icons.Filled.Campaign, com.example.core.navigation.AppRoutes.FACULTY_CIRCULARS, currentRoute, onNavigate)
            DrawerItem("Advisor Leaves", Icons.Filled.Checklist, com.example.core.navigation.AppRoutes.FACULTY_ADVISOR_LEAVES, currentRoute, onNavigate)
            DrawerItem("Class Management", Icons.Filled.ManageAccounts, com.example.core.navigation.AppRoutes.FACULTY_CLASS_STUDENT_MGMT, currentRoute, onNavigate)
            DrawerItem("Mentor Management", Icons.Filled.SupervisorAccount, com.example.core.navigation.AppRoutes.FACULTY_MENTOR_STUDENT_MGMT, currentRoute, onNavigate)
            DrawerItem("Class Diary", Icons.Filled.MenuBook, com.example.core.navigation.AppRoutes.FACULTY_CLASS_DIARY, currentRoute, onNavigate)
            DrawerItem("Diary HOD Review", Icons.Filled.RateReview, com.example.core.navigation.AppRoutes.FACULTY_CLASS_DIARY_HOD, currentRoute, onNavigate)

            Spacer(modifier = Modifier.weight(1f))

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
