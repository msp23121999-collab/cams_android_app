package com.example.core.navigation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Text
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.ui.unit.dp
import androidx.compose.ui.graphics.Color
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.*
import androidx.compose.material.icons.filled.Logout
import com.example.core.theme.CamsNavy
import com.example.core.theme.CamsTextPrimary
import com.example.core.theme.CamsTextSecondary
import com.example.core.theme.Slate300
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import android.net.Uri
import com.example.features.auth.LoginScreen
import com.example.features.auth.providers.AuthViewModel
import com.example.features.fees.screens.FeesScreen
import com.example.features.campus_life.screens.OnlineMeetingsScreen
import com.example.features.campus_life.screens.LexNovaScreen
import com.example.features.campus_life.screens.LegalEventsHubScreen
import com.example.features.splash.SplashScreen
import com.example.features.student.providers.DashboardViewModel
import com.example.features.student.providers.DashboardViewModelFactory
import com.example.features.attendance.providers.AttendanceViewModel
import com.example.features.attendance.providers.AttendanceViewModelFactory
import com.example.features.student.providers.*
import com.example.features.student.screens.*
import com.example.features.campus_life.providers.*
import com.example.features.campus_life.screens.*
import com.example.features.shared.UnauthorizedScreen
import com.example.features.academics.providers.TimetableViewModel
import com.example.features.academics.providers.TimetableViewModelFactory
import com.example.features.academics.providers.AcademicCalendarViewModel
import com.example.features.academics.providers.AcademicCalendarViewModelFactory
import com.example.features.academics.providers.SyllabusViewModel
import com.example.features.academics.providers.SyllabusViewModelFactory
import com.example.features.notifications.providers.NotificationViewModel
import com.example.features.notifications.providers.NotificationViewModelFactory
import com.example.features.fees.providers.FeesViewModel
import com.example.features.fees.providers.FeesViewModelFactory
import com.example.features.student.screens.StudentProfileScreen
import com.example.features.academics.screens.SyllabusCoverageScreen
import com.example.features.notifications.screens.NotificationsScreen

// Equivalent to GoRouter configuration in Flutter
object AppRoutes {
    const val DEBUG = "/debug"
    const val SPLASH = "/"
    const val LOGIN = "/login"
    const val ROLE_SELECTION = "/role-selection"
    const val UNAUTHORIZED = "/unauthorized"
    
    // Dashboards
    const val STUDENT_DASHBOARD = "/student/dashboard"
    const val PARENT_DASHBOARD = "/parent/dashboard"
    const val PARENT_MARKS = "/parent/marks"
    const val PARENT_FEES = "/parent/fees"
    const val PARENT_CIRCULARS = "/parent/circulars"
    const val PARENT_ATTENDANCE = "/parent/attendance"
    const val PARENT_CONTACT = "/parent/contact"
    const val PARENT_PROFILE = "/parent/profile"
    const val PARENT_TIMETABLE = "/parent/timetable"
    const val PARENT_SETTINGS = "/parent/settings"
    
    const val FACULTY_DASHBOARD = "/faculty/dashboard"
    const val FACULTY_PROFILE = "/faculty/profile"
    const val FACULTY_STUDENTS = "/faculty/students"
    const val FACULTY_CALENDAR = "/faculty/calendar"
    const val FACULTY_TIMETABLE = "/faculty/timetable"
    const val FACULTY_ATTENDANCE = "/faculty/attendance"
    const val FACULTY_ASSIGNMENTS = "/faculty/assignments"
    const val FACULTY_MARKS_ENTRY = "/faculty/marks-entry"
    const val FACULTY_STUDY_MATERIALS = "/faculty/study-materials"
    const val FACULTY_LECTURE_RECORDINGS = "/faculty/lecture-recordings"
    const val FACULTY_SMART_CLASSROOM = "/faculty/smart-classroom"
    const val FACULTY_LEGAL_EVENTS = "/faculty/legal-events"
    const val FACULTY_INTERNSHIPS = "/faculty/internships"
    const val FACULTY_ACTIVITY_POINTS = "/faculty/activity-points"
    const val FACULTY_RESEARCH_TRACKER = "/faculty/research-tracker"
    const val FACULTY_SALARY_SLIP = "/faculty/salary-slip"
    const val FACULTY_ONLINE_MEETINGS = "/faculty/online-meetings"
    const val FACULTY_NOTIFICATIONS = "/faculty/notifications"
    const val FACULTY_LEAVE_APPLY = "/faculty/leave-apply"
    const val FACULTY_COMMUNICATION = "/faculty/communication"
    const val FACULTY_CIRCULARS = "/faculty/circulars"
    const val FACULTY_ADVISOR_LEAVES = "/faculty/advisor-leaves"
    const val FACULTY_CLASS_STUDENT_MGMT = "/faculty/class-student-management"
    const val FACULTY_MENTOR_STUDENT_MGMT = "/faculty/mentor-student-management"
    const val FACULTY_CLASS_DIARY = "/faculty/class-diary"
    const val FACULTY_CLASS_DIARY_HOD = "/faculty/class-diary-hod"
    
    const val HOD_DASHBOARD = "/hod/dashboard"
    const val HOD_FACULTY_MGMT = "/hod/faculty-mgmt"
    const val HOD_STUDENT_MGMT = "/hod/student-mgmt"
    const val HOD_ACADEMIC_PROGRESS = "/hod/academic-progress"
    const val HOD_TIMETABLE_MGMT = "/hod/timetable-mgmt"
    const val HOD_TIMETABLE_SETUP = "/hod/timetable-setup"
    const val HOD_RESEARCH_OVERVIEW = "/hod/research-overview"
    const val HOD_APPROVALS = "/hod/approvals"
    const val HOD_MARK_APPROVALS = "/hod/mark-approvals"
    const val HOD_LEAVE_APPROVALS = "/hod/leave-approvals"
    const val HOD_FACULTY_WORKLOADS = "/hod/faculty-workloads"
    const val HOD_SUBJECT_ALLOCATION = "/hod/subject-allocation"
    const val HOD_SUBSTITUTION_MGMT = "/hod/substitution-mgmt"
    const val HOD_FACULTY_APPROVAL = "/hod/faculty-approval"

    const val HOD_MENTOR_ASSIGNMENT = "/hod/mentor-assignment"
    const val HOD_SYLLABUS_MGMT = "/hod/syllabus-mgmt"
    const val HOD_CLASS_ADVISOR = "/hod/class-advisor"
    const val HOD_ATTENDANCE_MONITORING = "/hod/attendance-monitoring"


    const val HOD_PROFILE_APPROVALS = "/hod/profile-approvals"
    const val HOD_ATTENDANCE_CORRECTION = "/hod/attendance-correction-approvals"
    const val HOD_CIRCULARS = "/hod/circulars"
    const val HOD_STUDY_MATERIALS = "/hod/study-materials"
    const val HOD_REPORTS = "/hod/reports"
    const val HOD_RESEARCH_MONITORING = "/hod/research-monitoring"
    const val HOD_CALENDAR = "/hod/calendar"
    const val HOD_COMMUNICATION = "/hod/communication"
    const val ADMIN_DASHBOARD = "/admin/dashboard"
    const val ADMIN_USER_MGMT = "/admin/user-mgmt"
    const val ADMIN_FEE_MGMT = "/admin/fee-mgmt"
    const val ADMIN_SALARY_MGMT = "/admin/salary-mgmt"
    const val ADMIN_EXAM_MGMT = "/admin/exam-mgmt"
    const val ADMIN_FACULTY_ASSIGNMENT = "/admin/faculty-assignment"
    const val ADMIN_ACADEMIC_CALENDAR = "/admin/academic-calendar"
    const val ADMIN_NOTIFICATIONS = "/admin/notifications"
    const val ADMIN_CIRCULARS = "/admin/circulars"
    const val ADMIN_REPORTS = "/admin/reports"
    const val ADMIN_SYSTEM_CONFIG = "/admin/system-config"
    const val ADMIN_LOGS = "/admin/logs"
    const val ADMIN_BACKUPS = "/admin/backups"
    
    const val ADMIN_ACADEMIC_YEAR_CONFIG = "/admin/academic-year-config"
    const val ADMIN_ATTENDANCE_DEFAULTERS = "/admin/attendance-defaulters"
    const val ADMIN_BATCH_SETUP = "/admin/batch-setup"
    const val ADMIN_COURSE_SETUP = "/admin/course-setup"
    const val ADMIN_COLLECT_FEE = "/admin/collect-fee"
    const val ADMIN_ACADEMIC_CATALOG = "/admin/academic-catalog"
    
    // Missing Admin Modules (Empty States)
    const val ADMIN_HOSTEL = "/admin/hostel"
    const val ADMIN_TRANSPORT = "/admin/transport"
    const val ADMIN_LIBRARY = "/admin/library"
    const val ADMIN_INVENTORY = "/admin/inventory"

    
    const val PRINCIPAL_DASHBOARD = "/principal/dashboard"
    const val PRINCIPAL_PERFORMANCE = "/principal/performance"
    const val PRINCIPAL_FACULTY_OVERVIEW = "/principal/faculty-overview"
    const val PRINCIPAL_BUDGET_GRANTS = "/principal/budget-grants"
    const val PRINCIPAL_STRATEGIC_NOTICES = "/principal/strategic-notices"
    const val PRINCIPAL_CALENDAR = "/principal/calendar"
    const val PRINCIPAL_APPROVALS = "/principal/approvals"
    const val PRINCIPAL_CLASS_DIARY = "/principal/class-diary"
    const val PRINCIPAL_EVENTS_MGMT = "/principal/events-mgmt"
    const val PRINCIPAL_INFRASTRUCTURE = "/principal/infrastructure"
    const val PRINCIPAL_RESEARCH_COMPLIANCE = "/principal/research-compliance"
    const val PRINCIPAL_STUDY_MATERIALS = "/principal/study-materials"
    const val PRINCIPAL_GRIEVANCES = "/principal/grievances"
    const val PRINCIPAL_CIRCULARS = "/principal/circulars"
    
    const val STUDENT_FEES = "/student/fees"
    const val STUDENT_LEAVE = "/student/leave"
    const val STUDENT_SYLLABUS = "/student/syllabus"
    const val CAMPUS_LIFE = "/student/campus_life"
    const val STUDENT_COUNCIL = "/student/council"
    const val ONLINE_MEETINGS = "/student/online-meetings"
    const val LEXNOVA = "/student/lexnova"
    const val LEGAL_EVENTS = "/student/legal-events"
    const val LEGAL_SKILLS = "/student/legal-skills"
    const val LEXSPHERE = "/student/lexsphere"
    const val INTERNSHIPS = "/student/internships"
    const val CERTIFICATIONS = "/student/certifications"
    const val ACTIVITY_POINTS = "/student/activity-points"
    const val COMMUNITY_SERVICE = "/student/community-service"
    const val INNOVATION_WALL = "/student/innovation-wall"
    const val PROJECT_SHOWCASE = "/student/project-showcase"
    
    // Additional Student Routes
    const val NOTIFICATIONS = "/student/notifications"
    const val ATTENDANCE = "/student/attendance"
    const val INTERNAL_MARKS = "/student/internal_marks"
    const val TIMETABLE = "/student/timetable"
    const val LEAVE = "/student/leave"
    const val ASSIGNMENTS = "/student/assignments"
    const val FEES = "/student/fees"
    const val CIRCULARS = "/student/circulars"
    const val STUDENT_HALL_TICKET = "/student/hall-ticket"
    const val STUDENT_PROFILE = "/student/profile"
    const val STUDENT_CALENDAR = "/student/calendar"
    const val STUDENT_ACADEMICS = "/student/academics"
    const val STUDENT_SETTINGS = "/student/settings"
}

@Composable
fun AppNavigation(
    navController: NavHostController, 
    authViewModel: AuthViewModel,
    container: com.example.core.di.AppContainer
) {
    val authState by authViewModel.uiState.collectAsState()
    var showLogoutDialog by remember { mutableStateOf(false) }

    if (showLogoutDialog) {
        AlertDialog(
            onDismissRequest = { showLogoutDialog = false },
            icon = {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.Logout,
                    contentDescription = "Logout Icon",
                    tint = CamsNavy
                )
            },
            title = {
                Text(
                    text = "Logout",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    color = CamsTextPrimary
                )
            },
            text = {
                Text(
                    text = "Are you sure you want to sign out of your CAMS account?",
                    style = MaterialTheme.typography.bodyMedium,
                    color = CamsTextSecondary,
                    textAlign = TextAlign.Center
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        showLogoutDialog = false
                        authViewModel.logout()
                        navController.navigate(AppRoutes.ROLE_SELECTION) {
                            popUpTo(0) { inclusive = true }
                        }
                    },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = CamsNavy
                    ),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text("Logout", color = Color.White)
                }
            },
            dismissButton = {
                OutlinedButton(
                    onClick = { showLogoutDialog = false },
                    shape = RoundedCornerShape(12.dp),
                    border = androidx.compose.foundation.BorderStroke(1.dp, Slate300)
                ) {
                    Text("Cancel", color = CamsTextPrimary)
                }
            },
            shape = RoundedCornerShape(24.dp),
            containerColor = Color.White
        )
    }

    val navigateToRoute: (String) -> Unit = { route ->
        if (route == "LOGOUT") {
            showLogoutDialog = true
        } else {
            navController.navigate(route)
        }
    }
    
    NavHost(navController = navController, startDestination = AppRoutes.SPLASH) {
                composable(AppRoutes.DEBUG) {
            com.example.features.debug.DebugScreen(onBack = { navController.popBackStack() })
        }
        composable(AppRoutes.SPLASH) {
            SplashScreen(
                onNavigate = {
                    if (authState.isLoading) return@SplashScreen
                    
                    val startRoute = if (authState.token != null) {
                        when (authState.role?.uppercase()) {
                            "STUDENT" -> AppRoutes.STUDENT_DASHBOARD
                            "PARENT" -> AppRoutes.PARENT_DASHBOARD
                            "FACULTY" -> AppRoutes.FACULTY_DASHBOARD
                            "HOD" -> AppRoutes.HOD_DASHBOARD
                            "ADMIN" -> AppRoutes.ADMIN_DASHBOARD
                            "PRINCIPAL" -> AppRoutes.PRINCIPAL_DASHBOARD
                            else -> AppRoutes.ROLE_SELECTION
                        }
                    } else {
                        AppRoutes.ROLE_SELECTION
                    }
                    
                    navController.navigate(startRoute) {
                        popUpTo(AppRoutes.SPLASH) { inclusive = true }
                    }
                }
            )
        }
        composable(AppRoutes.ROLE_SELECTION) {
            com.example.features.auth.RoleSelectionScreen(
                onRoleSelected = { role ->
                    navController.navigate("/login/${role.uppercase()}") {
                        popUpTo(AppRoutes.ROLE_SELECTION) { inclusive = false }
                    }
                }
            )
        }
        composable("/login/{role}") { backStackEntry ->
            val role = backStackEntry.arguments?.getString("role") ?: "STUDENT"
            LoginScreen(
                role = role,
                authViewModel = authViewModel,
                onLoginSuccess = { userRole ->
                    val route = when (userRole.uppercase()) {
                        "STUDENT" -> AppRoutes.STUDENT_DASHBOARD
                        "PARENT" -> AppRoutes.PARENT_DASHBOARD
                        "FACULTY" -> AppRoutes.FACULTY_DASHBOARD
                        "HOD" -> AppRoutes.HOD_DASHBOARD
                        "ADMIN", "SUPER_ADMIN" -> AppRoutes.ADMIN_DASHBOARD
                        "PRINCIPAL" -> AppRoutes.PRINCIPAL_DASHBOARD
                        else -> AppRoutes.UNAUTHORIZED
                    }
                    navController.navigate(route) {
                        popUpTo("/login/{role}") { inclusive = true }
                        popUpTo(AppRoutes.ROLE_SELECTION) { inclusive = true }
                    }
                },
                onBack = { navController.popBackStack() },
                onDebugTap = { navController.navigate(AppRoutes.DEBUG) }
            )
        }
        
        composable(AppRoutes.UNAUTHORIZED) {
            UnauthorizedScreen(
                onNavigateToLogin = {
                    authViewModel.logout()
                    navController.navigate(AppRoutes.ROLE_SELECTION) {
                        popUpTo(0) { inclusive = true }
                    }
                }
            )
        }

        // --- Role Guards for Dashboards ---
        
        composable(AppRoutes.STUDENT_DASHBOARD) { 
            RoleGuard(
                currentRole = authState.role,
                allowedRoles = listOf("STUDENT"),
                isLoading = authState.isLoading,
                onUnauthorized = { navController.navigate(AppRoutes.UNAUTHORIZED) }
            ) {
                val dashboardViewModel: com.example.features.student.providers.DashboardViewModel = viewModel(
                    factory = com.example.features.student.providers.DashboardViewModelFactory(container.studentRepository)
                )
                com.example.features.student.screens.StudentDashboardScreen(
                    viewModel = dashboardViewModel,
                    onNavigate = { route -> 
                        if (route != AppRoutes.STUDENT_DASHBOARD) {
                            navigateToRoute(route)
                        }
                    }
                ) 
            }
        }
        
        composable(AppRoutes.STUDENT_HALL_TICKET) {
            RoleGuard(
                currentRole = authState.role,
                allowedRoles = listOf("STUDENT"),
                isLoading = authState.isLoading,
                onUnauthorized = { navController.navigate(AppRoutes.UNAUTHORIZED) }
            ) {
                val hallTicketViewModel: com.example.features.student.providers.HallTicketViewModel = viewModel(
                    factory = com.example.features.student.providers.HallTicketViewModelFactory(container.studentRepository)
                )
                com.example.features.student.screens.hallticket.HallTicketScreen(
                    viewModel = hallTicketViewModel,
                    currentRoute = AppRoutes.STUDENT_HALL_TICKET,
                    onNavigate = { route ->
                        if (route != AppRoutes.STUDENT_HALL_TICKET) {
                            navigateToRoute(route)
                        }
                    }
                )
            }
        }
        
        composable("/student/attendance") { 
            RoleGuard(
                currentRole = authState.role,
                allowedRoles = listOf("STUDENT"),
                isLoading = authState.isLoading,
                onUnauthorized = { navController.navigate(AppRoutes.UNAUTHORIZED) }
            ) {
                val attendanceViewModel: com.example.features.student.providers.AttendanceViewModel = viewModel(
                    factory = com.example.features.student.providers.AttendanceViewModelFactory(container.studentRepository)
                )
                com.example.features.attendance.screens.AttendanceRegisterScreen(
                    viewModel = attendanceViewModel,
                    onNavigate = { route -> 
                        if (route != "/student/attendance") {
                            navigateToRoute(route)
                        }
                    }
                ) 
            }
        }

        composable("/student/marks") { 
            RoleGuard(
                currentRole = authState.role,
                allowedRoles = listOf("STUDENT"),
                isLoading = authState.isLoading,
                onUnauthorized = { navController.navigate(AppRoutes.UNAUTHORIZED) }
            ) {
                val marksViewModel: com.example.features.student.providers.MarksViewModel = viewModel(
                    factory = com.example.features.student.providers.MarksViewModelFactory(container.studentRepository)
                )
                com.example.features.academics.screens.InternalMarksScreen(
                    viewModel = marksViewModel,
                    onNavigate = { route -> navigateToRoute(route) }
                ) 
            }
        }

        composable("/student/fees") { 
            RoleGuard(
                currentRole = authState.role,
                allowedRoles = listOf("STUDENT"),
                isLoading = authState.isLoading,
                onUnauthorized = { navController.navigate(AppRoutes.UNAUTHORIZED) }
            ) {
                val feesViewModel: FeesViewModel = viewModel(
                    factory = FeesViewModelFactory(container.studentRepository)
                )
                com.example.features.fees.screens.FeesScreen(
                    viewModel = feesViewModel,
                    onNavigate = { route -> navigateToRoute(route) }
                ) 
            }
        }

        composable("/student/assignments") { 
            RoleGuard(
                currentRole = authState.role,
                allowedRoles = listOf("STUDENT"),
                isLoading = authState.isLoading,
                onUnauthorized = { navController.navigate(AppRoutes.UNAUTHORIZED) }
            ) {
                val assignmentsViewModel: com.example.features.student.providers.AssignmentsViewModel = viewModel(
                    factory = com.example.features.student.providers.AssignmentsViewModelFactory(container.studentRepository)
                )
                com.example.features.academics.screens.StudentAssignmentsScreen(
                    viewModel = assignmentsViewModel,
                    onNavigate = { route -> navigateToRoute(route) }
                ) 
            }
        }

        composable("/student/leaves") { 
            RoleGuard(
                currentRole = authState.role,
                allowedRoles = listOf("STUDENT"),
                isLoading = authState.isLoading,
                onUnauthorized = { navController.navigate(AppRoutes.UNAUTHORIZED) }
            ) {
                val leavesViewModel: com.example.features.student.providers.LeavesViewModel = viewModel(
                    factory = com.example.features.student.providers.LeavesViewModelFactory(container.studentRepository)
                )
                com.example.features.leave.screens.LeaveApplicationScreen(
                    viewModel = leavesViewModel,
                    onNavigate = { route -> navigateToRoute(route) }
                ) 
            }
        }

        composable("/student/grievances") { 
            RoleGuard(
                currentRole = authState.role,
                allowedRoles = listOf("STUDENT"),
                isLoading = authState.isLoading,
                onUnauthorized = { navController.navigate(AppRoutes.UNAUTHORIZED) }
            ) {
                val grievancesViewModel: GrievancesViewModel = viewModel(
                    factory = GrievancesViewModelFactory(container.studentRepository)
                )
                GrievancesScreen(
                    viewModel = grievancesViewModel,
                    onNavigate = { route -> navigateToRoute(route) }
                ) 
            }
        }

        composable("/student/study-materials") { 
            RoleGuard(
                currentRole = authState.role,
                allowedRoles = listOf("STUDENT"),
                isLoading = authState.isLoading,
                onUnauthorized = { navController.navigate(AppRoutes.UNAUTHORIZED) }
            ) {
                val studyMaterialsViewModel: com.example.features.student.providers.StudyMaterialsViewModel = viewModel(
                    factory = com.example.features.student.providers.StudyMaterialsViewModelFactory(container.studentRepository)
                )
                com.example.features.academics.screens.StudyMaterialsScreen(
                    viewModel = studyMaterialsViewModel,
                    onNavigate = { route -> navigateToRoute(route) }
                ) 
            }
        }

        composable("/student/profile") { 
            RoleGuard(
                currentRole = authState.role,
                allowedRoles = listOf("STUDENT"),
                isLoading = authState.isLoading,
                onUnauthorized = { navController.navigate(AppRoutes.UNAUTHORIZED) }
            ) {
                val viewModel: com.example.features.student.providers.StudentProfileViewModel = viewModel(factory = com.example.features.student.providers.StudentProfileViewModelFactory(container.studentRepository))
                StudentProfileScreen(
                    viewModel = viewModel,
                    onNavigate = { route -> 
                        if (route == "LOGOUT") {
                            showLogoutDialog = true
                        } else if (route != AppRoutes.STUDENT_PROFILE) {
                            navigateToRoute(route)
                        }
                    }
                ) 
            }
        }
        
        composable(AppRoutes.STUDENT_FEES) { 
            val feesViewModel: FeesViewModel = viewModel(
                factory = FeesViewModelFactory(container.studentRepository)
            )
            RoleGuard(
                currentRole = authState.role,
                allowedRoles = listOf("STUDENT"),
                isLoading = authState.isLoading,
                onUnauthorized = { navController.navigate(AppRoutes.UNAUTHORIZED) }
            ) {
                FeesScreen(
                    viewModel = feesViewModel,
                    onNavigate = { route -> 
                        if (route != AppRoutes.STUDENT_FEES) {
                            navigateToRoute(route)
                        }
                    }
                ) 
            }
        }
        
        composable(AppRoutes.STUDENT_LEAVE) { 
            val leaveViewModel: LeavesViewModel = viewModel(
                factory = LeavesViewModelFactory(container.studentRepository)
            )
            RoleGuard(
                currentRole = authState.role,
                allowedRoles = listOf("STUDENT"),
                isLoading = authState.isLoading,
                onUnauthorized = { navController.navigate(AppRoutes.UNAUTHORIZED) }
            ) {
                com.example.features.leave.screens.LeaveApplicationScreen(
                    viewModel = leaveViewModel,
                    onNavigate = { route -> 
                        if (route != AppRoutes.STUDENT_LEAVE) {
                            navigateToRoute(route)
                        }
                    }
                ) 
            }
        }
        
        composable("/student/assignments") { 
            val assignmentsViewModel: AssignmentsViewModel = viewModel(
                factory = AssignmentsViewModelFactory(container.studentRepository)
            )
            RoleGuard(
                currentRole = authState.role,
                allowedRoles = listOf("STUDENT"),
                isLoading = authState.isLoading,
                onUnauthorized = { navController.navigate(AppRoutes.UNAUTHORIZED) }
            ) {
                com.example.features.academics.screens.StudentAssignmentsScreen(
                    viewModel = assignmentsViewModel,
                    onNavigate = { route -> 
                        if (route != "/student/assignments") {
                            navigateToRoute(route)
                        }
                    }
                ) 
            }
        }
        
        composable("/student/materials") { 
            val materialsViewModel: StudyMaterialsViewModel = viewModel(
                factory = StudyMaterialsViewModelFactory(container.studentRepository)
            )
            RoleGuard(
                currentRole = authState.role,
                allowedRoles = listOf("STUDENT"),
                isLoading = authState.isLoading,
                onUnauthorized = { navController.navigate(AppRoutes.UNAUTHORIZED) }
            ) {
                com.example.features.academics.screens.StudyMaterialsScreen(
                    viewModel = materialsViewModel,
                    onNavigate = { route -> 
                        if (route != "/student/materials") {
                            navigateToRoute(route)
                        }
                    }
                ) 
            }
        }

        composable("/student/internal_marks") { 
            val internalMarksViewModel: MarksViewModel = viewModel(
                factory = MarksViewModelFactory(container.studentRepository)
            )
            RoleGuard(
                currentRole = authState.role,
                allowedRoles = listOf("STUDENT"),
                isLoading = authState.isLoading,
                onUnauthorized = { navController.navigate(AppRoutes.UNAUTHORIZED) }
            ) {
                com.example.features.academics.screens.InternalMarksScreen(
                    viewModel = internalMarksViewModel,
                    onNavigate = { route -> 
                        if (route != "/student/internal_marks") {
                            navigateToRoute(route)
                        }
                    }
                ) 
            }
        } 
        composable("/student/timetable") {
            val timetableViewModel: TimetableViewModel = viewModel(
                factory = TimetableViewModelFactory(container.studentRepository)
            )
            RoleGuard(
                currentRole = authState.role,
                allowedRoles = listOf("STUDENT"),
                isLoading = authState.isLoading,
                onUnauthorized = { navController.navigate(AppRoutes.UNAUTHORIZED) }
            ) {
                com.example.features.academics.screens.TimetableScreen(
                    viewModel = timetableViewModel,
                    onNavigate = { route ->
                        if (route != "/student/timetable") {
                            navigateToRoute(route)
                        }
                    }
                )
            }
        }
        
        composable(AppRoutes.STUDENT_SYLLABUS) { 
            RoleGuard(
                currentRole = authState.role,
                allowedRoles = listOf("STUDENT"),
                isLoading = authState.isLoading,
                onUnauthorized = { navController.navigate(AppRoutes.UNAUTHORIZED) }
            ) {
                val viewModel: com.example.features.academics.providers.SyllabusViewModel = viewModel(factory = com.example.features.academics.providers.SyllabusViewModelFactory(container.studentRepository))
                SyllabusCoverageScreen(
                    onNavigate = { route -> 
                        if (route != AppRoutes.STUDENT_SYLLABUS) {
                            navigateToRoute(route)
                        }
                    }
                ) 
            }
        }
        
        composable("/student/notifications") { 
            RoleGuard(
                currentRole = authState.role,
                allowedRoles = listOf("STUDENT"),
                isLoading = authState.isLoading,
                onUnauthorized = { navController.navigate(AppRoutes.UNAUTHORIZED) }
            ) {
                val viewModel: com.example.features.notifications.providers.NotificationViewModel = viewModel(factory = com.example.features.notifications.providers.NotificationViewModelFactory(container.studentRepository))
                NotificationsScreen(
                    viewModel = viewModel,
                    onNavigate = { route -> 
                        if (route != "/student/notifications") {
                            navigateToRoute(route)
                        }
                    }
                ) 
            }
        }

        composable(AppRoutes.CAMPUS_LIFE) {
            RoleGuard(
                currentRole = authState.role,
                allowedRoles = listOf("STUDENT"),
                isLoading = authState.isLoading,
                onUnauthorized = { navController.navigate(AppRoutes.UNAUTHORIZED) }
            ) {
                com.example.features.campus_life.screens.CampusLifeDashboardScreen(onNavigate = { route ->
                    if (route != AppRoutes.CAMPUS_LIFE) {
                        navigateToRoute(route)
                    }
                })
            }
        }
        
        composable("/student/clubs") { 
            RoleGuard(
                currentRole = authState.role,
                allowedRoles = listOf("STUDENT"),
                isLoading = authState.isLoading,
                onUnauthorized = { navController.navigate(AppRoutes.UNAUTHORIZED) }
            ) {
                val viewModel: ClubsViewModel = viewModel(factory = ClubsViewModelFactory(container.studentRepository))
                StudentClubsScreen(
                    viewModel = viewModel,
                    onNavigate = { route -> 
                        if (route != "/student/clubs") {
                            navigateToRoute(route)
                        }
                    }
                ) 
            }
        }
        
        composable("/student/grievances") { 
            RoleGuard(
                currentRole = authState.role,
                allowedRoles = listOf("STUDENT"),
                isLoading = authState.isLoading,
                onUnauthorized = { navController.navigate(AppRoutes.UNAUTHORIZED) }
            ) {
                val viewModel: GrievancesViewModel = viewModel(factory = GrievancesViewModelFactory(container.studentRepository))
                GrievancesScreen(
                    viewModel = viewModel,
                    onNavigate = { route -> 
                        if (route != "/student/grievances") {
                            navigateToRoute(route)
                        }
                    }
                ) 
            }
        }
        
        composable("/student/circulars") { 
            RoleGuard(
                currentRole = authState.role,
                allowedRoles = listOf("STUDENT"),
                isLoading = authState.isLoading,
                onUnauthorized = { navController.navigate(AppRoutes.UNAUTHORIZED) }
            ) {
                val viewModel: CircularsViewModel = viewModel(factory = CircularsViewModelFactory(container.studentRepository))
                CircularsScreen(
                    viewModel = viewModel,
                    onNavigate = { route -> 
                        if (route != "/student/circulars") {
                            navigateToRoute(route)
                        }
                    }
                ) 
            }
        }

        composable(AppRoutes.STUDENT_ACADEMICS) {
            RoleGuard(
                currentRole = authState.role,
                allowedRoles = listOf("STUDENT"),
                isLoading = authState.isLoading,
                onUnauthorized = { navController.navigate(AppRoutes.UNAUTHORIZED) }
            ) {
                val academicsViewModel: com.example.features.student.providers.AcademicsViewModel = viewModel(
                    factory = com.example.features.student.providers.AcademicsViewModelFactory(container.studentRepository)
                )
                com.example.features.student.screens.StudentAcademicsScreen(
                    viewModel = academicsViewModel,
                    onNavigate = { route ->
                        if (route != AppRoutes.STUDENT_ACADEMICS) {
                            navigateToRoute(route)
                        }
                    }
                )
            }
        }

        composable(AppRoutes.STUDENT_CALENDAR) {
            RoleGuard(
                currentRole = authState.role,
                allowedRoles = listOf("STUDENT"),
                isLoading = authState.isLoading,
                onUnauthorized = { navController.navigate(AppRoutes.UNAUTHORIZED) }
            ) {
                val viewModel: AcademicCalendarViewModel = viewModel(factory = AcademicCalendarViewModelFactory(container.studentRepository))
                com.example.features.academics.screens.StudentAcademicCalendarScreen(
                    viewModel = viewModel,
                    onNavigate = { route -> 
                    if (route != AppRoutes.STUDENT_CALENDAR) {
                        navigateToRoute(route)
                    }
                })
            }
        }
        
        composable(AppRoutes.STUDENT_SETTINGS) {
            RoleGuard(
                currentRole = authState.role,
                allowedRoles = listOf("STUDENT"),
                isLoading = authState.isLoading,
                onUnauthorized = { navController.navigate(AppRoutes.UNAUTHORIZED) }
            ) {
                val viewModel: com.example.features.student.providers.StudentProfileViewModel = viewModel(factory = com.example.features.student.providers.StudentProfileViewModelFactory(container.studentRepository))
                com.example.features.student.screens.StudentSettingsScreen(
                    viewModel = viewModel,
                    onNavigate = { route ->
                        if (route == "LOGOUT") {
                            showLogoutDialog = true
                        } else {
                            navigateToRoute(route)
                        }
                    }
                )
            }
        }

        composable(AppRoutes.ONLINE_MEETINGS) { 
            RoleGuard(
                currentRole = authState.role,
                allowedRoles = listOf("STUDENT"),
                isLoading = authState.isLoading,
                onUnauthorized = { navController.navigate(AppRoutes.UNAUTHORIZED) }
            ) {
                val viewModel: OnlineMeetingsViewModel = viewModel(factory = OnlineMeetingsViewModelFactory(container.studentRepository))
                OnlineMeetingsScreen(
                    viewModel = viewModel,
                    onNavigate = { route -> 
                        if (route != AppRoutes.ONLINE_MEETINGS) {
                            navigateToRoute(route)
                        }
                    }
                ) 
            }
        }
        
        composable(AppRoutes.STUDENT_COUNCIL) {
            RoleGuard(
                currentRole = authState.role,
                allowedRoles = listOf("STUDENT"),
                isLoading = authState.isLoading,
                onUnauthorized = { navController.navigate(AppRoutes.UNAUTHORIZED) }
            ) {
                val viewModel: CouncilViewModel = viewModel(factory = CouncilViewModelFactory(container.studentRepository))
                com.example.features.campus_life.screens.StudentCouncilScreen(
                    viewModel = viewModel,
                    onNavigate = { route ->
                        if (route != AppRoutes.STUDENT_COUNCIL) {
                            navigateToRoute(route)
                        }
                    }
                )
            }
        }
        
        composable(AppRoutes.PARENT_DASHBOARD) { 
            RoleGuard(
                currentRole = authState.role,
                allowedRoles = listOf("PARENT"),
                isLoading = authState.isLoading,
                onUnauthorized = { navController.navigate(AppRoutes.UNAUTHORIZED) }
            ) {
                val viewModel: com.example.features.parent.providers.ParentDashboardViewModel = viewModel(factory = com.example.features.parent.providers.ParentDashboardViewModelFactory(container.parentRepository))
                com.example.features.parent.screens.ParentDashboardScreen(
                    viewModel = viewModel,
                    onNavigate = { route ->
                        if (route != AppRoutes.PARENT_DASHBOARD) {
                            navigateToRoute(route)
                        }
                    }
                ) 
            }
        }

        composable(AppRoutes.PARENT_ATTENDANCE) {
            RoleGuard(
                currentRole = authState.role,
                allowedRoles = listOf("PARENT"),
                isLoading = authState.isLoading,
                onUnauthorized = { navController.navigate(AppRoutes.UNAUTHORIZED) }
            ) {
                val viewModel: com.example.features.parent.providers.ParentAttendanceViewModel = viewModel(factory = com.example.features.parent.providers.ParentAttendanceViewModelFactory(container.parentRepository))
                com.example.features.parent.screens.ParentAttendanceScreen(
                    viewModel = viewModel,
                    onNavigate = { route ->
                        if (route != AppRoutes.PARENT_ATTENDANCE) {
                            navigateToRoute(route)
                        }
                    }
                )
            }
        }

        composable(AppRoutes.PARENT_CONTACT) {
            RoleGuard(
                currentRole = authState.role,
                allowedRoles = listOf("PARENT"),
                isLoading = authState.isLoading,
                onUnauthorized = { navController.navigate(AppRoutes.UNAUTHORIZED) }
            ) {
                com.example.features.parent.screens.ContactCollegeScreen(onNavigate = { route ->
                    if (route != AppRoutes.PARENT_CONTACT) {
                        navigateToRoute(route)
                    }
                })
            }
        }

        composable(AppRoutes.PARENT_PROFILE) {
            RoleGuard(
                currentRole = authState.role,
                allowedRoles = listOf("PARENT"),
                isLoading = authState.isLoading,
                onUnauthorized = { navController.navigate(AppRoutes.UNAUTHORIZED) }
            ) {
                val viewModel: com.example.features.parent.providers.ParentProfileViewModel = viewModel(factory = com.example.features.parent.providers.ParentProfileViewModelFactory(container.parentRepository))
                com.example.features.parent.screens.ParentChildProfileScreen(
                    viewModel = viewModel,
                    onNavigate = { route ->
                        if (route != AppRoutes.PARENT_PROFILE) {
                            navigateToRoute(route)
                        }
                    }
                )
            }
        }

        composable(AppRoutes.PARENT_TIMETABLE) {
            RoleGuard(
                currentRole = authState.role,
                allowedRoles = listOf("PARENT"),
                isLoading = authState.isLoading,
                onUnauthorized = { navController.navigate(AppRoutes.UNAUTHORIZED) }
            ) {
                val viewModel: com.example.features.parent.providers.ParentTimetableViewModel = viewModel(factory = com.example.features.parent.providers.ParentTimetableViewModelFactory(container.parentRepository))
                com.example.features.parent.screens.ParentChildTimetableScreen(
                    viewModel = viewModel,
                    onNavigate = { route ->
                        if (route != AppRoutes.PARENT_TIMETABLE) {
                            navigateToRoute(route)
                        }
                    }
                )
            }
        }
        
        composable(AppRoutes.PARENT_SETTINGS) {
            RoleGuard(
                currentRole = authState.role,
                allowedRoles = listOf("PARENT"),
                isLoading = authState.isLoading,
                onUnauthorized = { navController.navigate(AppRoutes.UNAUTHORIZED) }
            ) {
                val viewModel: com.example.features.parent.providers.ParentProfileViewModel = viewModel(factory = com.example.features.parent.providers.ParentProfileViewModelFactory(container.parentRepository))
                com.example.features.parent.screens.ParentSettingsScreen(
                    viewModel = viewModel,
                    onNavigate = { route ->
                        if (route == "LOGOUT") {
                            showLogoutDialog = true
                        } else {
                            navigateToRoute(route)
                        }
                    }
                )
            }
        }
        
        composable(AppRoutes.FACULTY_DASHBOARD) { 
            RoleGuard(
                currentRole = authState.role,
                allowedRoles = listOf("FACULTY"),
                isLoading = authState.isLoading,
                onUnauthorized = { navController.navigate(AppRoutes.UNAUTHORIZED) }
            ) {
                val viewModel: com.example.features.faculty.providers.FacultyDashboardViewModel = viewModel(factory = com.example.features.faculty.providers.FacultyDashboardViewModelFactory(container.facultyRepository))
                com.example.features.faculty.screens.FacultyDashboardScreen(
                    viewModel = viewModel,
                    onNavigate = { route ->
                        navigateToRoute(route)
                    }
                ) 
            }
        }

        composable(AppRoutes.FACULTY_PROFILE) { 
            RoleGuard(
                currentRole = authState.role,
                allowedRoles = listOf("FACULTY"),
                isLoading = authState.isLoading,
                onUnauthorized = { navController.navigate(AppRoutes.UNAUTHORIZED) }
            ) {
                val viewModel: com.example.features.faculty.providers.FacultyProfileViewModel = viewModel(factory = com.example.features.faculty.providers.FacultyProfileViewModelFactory(container.facultyRepository))
                com.example.features.faculty.screens.FacultyProfileScreen(
                    viewModel = viewModel,
                    onNavigate = { route ->
                        navigateToRoute(route)
                    }
                ) 
            }
        }

        composable(AppRoutes.FACULTY_STUDENTS) { 
            RoleGuard(
                currentRole = authState.role,
                allowedRoles = listOf("FACULTY"),
                isLoading = authState.isLoading,
                onUnauthorized = { navController.navigate(AppRoutes.UNAUTHORIZED) }
            ) {
                val viewModel: com.example.features.faculty.providers.FacultyStudentsViewModel = viewModel(factory = com.example.features.faculty.providers.FacultyStudentsViewModelFactory(container.facultyRepository))
                com.example.features.faculty.screens.FacultyStudentDirectoryScreen(
                    viewModel = viewModel,
                    onNavigate = { route ->
                        navigateToRoute(route)
                    }
                ) 
            }
        }

        composable(AppRoutes.FACULTY_CALENDAR) { 
            RoleGuard(
                currentRole = authState.role,
                allowedRoles = listOf("FACULTY"),
                isLoading = authState.isLoading,
                onUnauthorized = { navController.navigate(AppRoutes.UNAUTHORIZED) }
            ) {
                com.example.features.faculty.screens.FacultyAcademicCalendarScreen(onNavigate = { route ->
                    navigateToRoute(route)
                }) 
            }
        }

        composable(AppRoutes.FACULTY_TIMETABLE) { 
            RoleGuard(
                currentRole = authState.role,
                allowedRoles = listOf("FACULTY"),
                isLoading = authState.isLoading,
                onUnauthorized = { navController.navigate(AppRoutes.UNAUTHORIZED) }
            ) {
                val viewModel: com.example.features.faculty.providers.FacultyTimetableViewModel = viewModel(factory = com.example.features.faculty.providers.FacultyTimetableViewModelFactory(container.facultyRepository))
                com.example.features.faculty.screens.FacultyTimetableScreen(
                    viewModel = viewModel,
                    onNavigate = { route ->
                        navigateToRoute(route)
                    }
                ) 
            }
        }

        composable(AppRoutes.FACULTY_ATTENDANCE) {
            RoleGuard(
                currentRole = authState.role,
                allowedRoles = listOf("FACULTY"),
                isLoading = authState.isLoading,
                onUnauthorized = { navController.navigate(AppRoutes.UNAUTHORIZED) }
            ) {
                val viewModel: com.example.features.faculty.providers.FacultyAttendanceViewModel = viewModel(factory = com.example.features.faculty.providers.FacultyAttendanceViewModelFactory(container.facultyRepository))
                com.example.features.faculty.screens.FacultyAttendanceScreen(
                    viewModel = viewModel,
                    onNavigate = { route ->
                        navigateToRoute(route)
                    }
                )
            }
        }

        composable(AppRoutes.FACULTY_ASSIGNMENTS) {
            RoleGuard(
                currentRole = authState.role,
                allowedRoles = listOf("FACULTY"),
                isLoading = authState.isLoading,
                onUnauthorized = { navController.navigate(AppRoutes.UNAUTHORIZED) }
            ) {
                val viewModel: com.example.features.faculty.providers.FacultyAssignmentsViewModel = viewModel(factory = com.example.features.faculty.providers.FacultyAssignmentsViewModelFactory(container.facultyRepository))
                com.example.features.faculty.screens.FacultyAssignmentsScreen(
                    viewModel = viewModel,
                    onNavigate = { route ->
                        navigateToRoute(route)
                    }
                )
            }
        }

        composable(AppRoutes.FACULTY_MARKS_ENTRY) {
            RoleGuard(
                currentRole = authState.role,
                allowedRoles = listOf("FACULTY"),
                isLoading = authState.isLoading,
                onUnauthorized = { navController.navigate(AppRoutes.UNAUTHORIZED) }
            ) {
                val viewModel: com.example.features.faculty.providers.FacultyStudentsViewModel = viewModel(factory = com.example.features.faculty.providers.FacultyStudentsViewModelFactory(container.facultyRepository))
                com.example.features.faculty.screens.FacultyMarksEntryScreen(
                    viewModel = viewModel,
                    onNavigate = { route ->
                        navigateToRoute(route)
                    }
                )
            }
        }

        composable(AppRoutes.FACULTY_STUDY_MATERIALS) {
            RoleGuard(
                currentRole = authState.role,
                allowedRoles = listOf("FACULTY"),
                isLoading = authState.isLoading,
                onUnauthorized = { navController.navigate(AppRoutes.UNAUTHORIZED) }
            ) {
                val viewModel: com.example.features.faculty.providers.FacultyMaterialsViewModel = viewModel(factory = com.example.features.faculty.providers.FacultyMaterialsViewModelFactory(container.facultyRepository))
                com.example.features.faculty.screens.FacultyStudyMaterialsScreen(
                    viewModel = viewModel,
                    onNavigate = { route ->
                        navigateToRoute(route)
                    }
                )
            }
        }

        composable(AppRoutes.FACULTY_LECTURE_RECORDINGS) {
            RoleGuard(
                currentRole = authState.role,
                allowedRoles = listOf("FACULTY"),
                isLoading = authState.isLoading,
                onUnauthorized = { navController.navigate(AppRoutes.UNAUTHORIZED) }
            ) {
                val viewModel: com.example.features.faculty.providers.FacultyRecordingsViewModel = viewModel(factory = com.example.features.faculty.providers.FacultyRecordingsViewModelFactory(container.facultyRepository))
                com.example.features.faculty.screens.FacultyLectureRecordingsScreen(
                    viewModel = viewModel,
                    onNavigate = { route ->
                        navigateToRoute(route)
                    }
                )
            }
        }

        composable(AppRoutes.FACULTY_SMART_CLASSROOM) {
            RoleGuard(
                currentRole = authState.role,
                allowedRoles = listOf("FACULTY"),
                isLoading = authState.isLoading,
                onUnauthorized = { navController.navigate(AppRoutes.UNAUTHORIZED) }
            ) {
                com.example.features.faculty.screens.FacultySmartClassroomScreen(onNavigate = { route ->
                    navigateToRoute(route)
                })
            }
        }

        composable(AppRoutes.FACULTY_LEGAL_EVENTS) {
            RoleGuard(
                currentRole = authState.role,
                allowedRoles = listOf("FACULTY"),
                isLoading = authState.isLoading,
                onUnauthorized = { navController.navigate(AppRoutes.UNAUTHORIZED) }
            ) {
                com.example.features.faculty.screens.FacultyLegalEventsScreen(onNavigate = { route ->
                    navigateToRoute(route)
                })
            }
        }

        composable(AppRoutes.FACULTY_INTERNSHIPS) {
            RoleGuard(
                currentRole = authState.role,
                allowedRoles = listOf("FACULTY"),
                isLoading = authState.isLoading,
                onUnauthorized = { navController.navigate(AppRoutes.UNAUTHORIZED) }
            ) {
                com.example.features.faculty.screens.FacultyInternshipsScreen(onNavigate = { route ->
                    navigateToRoute(route)
                })
            }
        }

        composable(AppRoutes.FACULTY_ACTIVITY_POINTS) {
            RoleGuard(
                currentRole = authState.role,
                allowedRoles = listOf("FACULTY"),
                isLoading = authState.isLoading,
                onUnauthorized = { navController.navigate(AppRoutes.UNAUTHORIZED) }
            ) {
                com.example.features.faculty.screens.FacultyActivityPointsScreen(onNavigate = { route ->
                    navigateToRoute(route)
                })
            }
        }

        composable(AppRoutes.FACULTY_RESEARCH_TRACKER) {
            RoleGuard(
                currentRole = authState.role,
                allowedRoles = listOf("FACULTY"),
                isLoading = authState.isLoading,
                onUnauthorized = { navController.navigate(AppRoutes.UNAUTHORIZED) }
            ) {
                com.example.features.faculty.screens.FacultyResearchTrackerScreen(onNavigate = { route ->
                    navigateToRoute(route)
                })
            }
        }

        composable(AppRoutes.FACULTY_SALARY_SLIP) {
            RoleGuard(
                currentRole = authState.role,
                allowedRoles = listOf("FACULTY"),
                isLoading = authState.isLoading,
                onUnauthorized = { navController.navigate(AppRoutes.UNAUTHORIZED) }
            ) {
                com.example.features.faculty.screens.FacultySalarySlipScreen(onNavigate = { route ->
                    navigateToRoute(route)
                })
            }
        }

        composable(AppRoutes.FACULTY_ONLINE_MEETINGS) {
            RoleGuard(
                currentRole = authState.role,
                allowedRoles = listOf("FACULTY"),
                isLoading = authState.isLoading,
                onUnauthorized = { navController.navigate(AppRoutes.UNAUTHORIZED) }
            ) {
                com.example.features.faculty.screens.FacultyOnlineMeetingsScreen(onNavigate = { route ->
                    navigateToRoute(route)
                })
            }
        }

        composable(AppRoutes.FACULTY_NOTIFICATIONS) {
            RoleGuard(
                currentRole = authState.role,
                allowedRoles = listOf("FACULTY"),
                isLoading = authState.isLoading,
                onUnauthorized = { navController.navigate(AppRoutes.UNAUTHORIZED) }
            ) {
                com.example.features.faculty.screens.FacultyNotificationsScreen(onNavigate = { route ->
                    navigateToRoute(route)
                })
            }
        }

        composable(AppRoutes.FACULTY_LEAVE_APPLY) {
            RoleGuard(
                currentRole = authState.role,
                allowedRoles = listOf("FACULTY"),
                isLoading = authState.isLoading,
                onUnauthorized = { navController.navigate(AppRoutes.UNAUTHORIZED) }
            ) {
                com.example.features.faculty.screens.FacultyLeaveApplyScreen(onNavigate = { route ->
                    navigateToRoute(route)
                })
            }
        }

        composable(AppRoutes.FACULTY_COMMUNICATION) {
            RoleGuard(
                currentRole = authState.role,
                allowedRoles = listOf("FACULTY"),
                isLoading = authState.isLoading,
                onUnauthorized = { navController.navigate(AppRoutes.UNAUTHORIZED) }
            ) {
                com.example.features.faculty.screens.FacultyCommunicationScreen(onNavigate = { route ->
                    navigateToRoute(route)
                })
            }
        }

        composable(AppRoutes.FACULTY_CIRCULARS) {
            RoleGuard(
                currentRole = authState.role,
                allowedRoles = listOf("FACULTY"),
                isLoading = authState.isLoading,
                onUnauthorized = { navController.navigate(AppRoutes.UNAUTHORIZED) }
            ) {
                com.example.features.faculty.screens.FacultyCircularsScreen(onNavigate = { route ->
                    navigateToRoute(route)
                })
            }
        }

        composable(AppRoutes.FACULTY_ADVISOR_LEAVES) {
            RoleGuard(
                currentRole = authState.role,
                allowedRoles = listOf("FACULTY"),
                isLoading = authState.isLoading,
                onUnauthorized = { navController.navigate(AppRoutes.UNAUTHORIZED) }
            ) {
                com.example.features.faculty.screens.FacultyAdvisorLeavesScreen(onNavigate = { route ->
                    navigateToRoute(route)
                })
            }
        }

        composable(AppRoutes.FACULTY_CLASS_STUDENT_MGMT) {
            RoleGuard(
                currentRole = authState.role,
                allowedRoles = listOf("FACULTY"),
                isLoading = authState.isLoading,
                onUnauthorized = { navController.navigate(AppRoutes.UNAUTHORIZED) }
            ) {
                com.example.features.faculty.screens.FacultyClassStudentManagementScreen(onNavigate = { route ->
                    navigateToRoute(route)
                })
            }
        }

        composable(AppRoutes.FACULTY_MENTOR_STUDENT_MGMT) {
            RoleGuard(
                currentRole = authState.role,
                allowedRoles = listOf("FACULTY"),
                isLoading = authState.isLoading,
                onUnauthorized = { navController.navigate(AppRoutes.UNAUTHORIZED) }
            ) {
                com.example.features.faculty.screens.FacultyMentorStudentManagementScreen(onNavigate = { route ->
                    navigateToRoute(route)
                })
            }
        }

        composable(AppRoutes.FACULTY_CLASS_DIARY) {
            RoleGuard(
                currentRole = authState.role,
                allowedRoles = listOf("FACULTY"),
                isLoading = authState.isLoading,
                onUnauthorized = { navController.navigate(AppRoutes.UNAUTHORIZED) }
            ) {
                com.example.features.faculty.screens.FacultyClassDiaryScreen(onNavigate = { route ->
                    navigateToRoute(route)
                })
            }
        }

        composable(AppRoutes.FACULTY_CLASS_DIARY_HOD) {
            RoleGuard(
                currentRole = authState.role,
                allowedRoles = listOf("FACULTY"),
                isLoading = authState.isLoading,
                onUnauthorized = { navController.navigate(AppRoutes.UNAUTHORIZED) }
            ) {
                com.example.features.faculty.screens.FacultyClassDiaryHODScreen(onNavigate = { route ->
                    navigateToRoute(route)
                })
            }
        }
        
        composable(AppRoutes.HOD_DASHBOARD) { 
            RoleGuard(
                currentRole = authState.role,
                allowedRoles = listOf("HOD"),
                isLoading = authState.isLoading,
                onUnauthorized = { navController.navigate(AppRoutes.UNAUTHORIZED) }
            ) {
                val viewModel: com.example.features.hod.providers.HODViewModel = viewModel(factory = com.example.features.hod.providers.HODViewModelFactory(container.hodRepository))
                com.example.features.hod.screens.HODDashboardScreen(
                    viewModel = viewModel,
                    onNavigate = { route ->
                        navigateToRoute(route)
                    }
                ) 
            }
        }

        composable(AppRoutes.HOD_FACULTY_MGMT) {
            RoleGuard(
                currentRole = authState.role,
                allowedRoles = listOf("HOD"),
                isLoading = authState.isLoading,
                onUnauthorized = { navController.navigate(AppRoutes.UNAUTHORIZED) }
            ) {
                val viewModel: com.example.features.hod.providers.HODFacultyViewModel = viewModel(factory = com.example.features.hod.providers.HODFacultyViewModelFactory(container.hodRepository))
                com.example.features.hod.screens.HODFacultyManagementScreen(
                    viewModel = viewModel,
                    onNavigate = { route ->
                        navigateToRoute(route)
                    }
                )
            }
        }

        composable(AppRoutes.HOD_STUDENT_MGMT) {
            RoleGuard(
                currentRole = authState.role,
                allowedRoles = listOf("HOD"),
                isLoading = authState.isLoading,
                onUnauthorized = { navController.navigate(AppRoutes.UNAUTHORIZED) }
            ) {
                val viewModel: com.example.features.hod.providers.HODStudentViewModel = viewModel(factory = com.example.features.hod.providers.HODStudentViewModelFactory(container.hodRepository))
                com.example.features.hod.screens.HODStudentManagementScreen(
                    viewModel = viewModel,
                    onNavigate = { route ->
                        navigateToRoute(route)
                    }
                )
            }
        }

        composable(AppRoutes.HOD_ACADEMIC_PROGRESS) {
            RoleGuard(
                currentRole = authState.role,
                allowedRoles = listOf("HOD"),
                isLoading = authState.isLoading,
                onUnauthorized = { navController.navigate(AppRoutes.UNAUTHORIZED) }
            ) {
                val viewModel: com.example.features.hod.providers.HODAcademicMonitoringViewModel = androidx.lifecycle.viewmodel.compose.viewModel(
                    factory = com.example.features.hod.providers.HODAcademicMonitoringViewModelFactory(container.hodRepository)
                )
                com.example.features.hod.screens.HODAcademicMonitoringScreen(
                    viewModel = viewModel,
                    onNavigate = { route -> navigateToRoute(route) }
                )
            }
        }

        composable(AppRoutes.HOD_TIMETABLE_MGMT) {
            RoleGuard(
                currentRole = authState.role,
                allowedRoles = listOf("HOD"),
                isLoading = authState.isLoading,
                onUnauthorized = { navController.navigate(AppRoutes.UNAUTHORIZED) }
            ) {
                com.example.features.hod.screens.HODTimetableManagementScreen(
                    viewModel = androidx.lifecycle.viewmodel.compose.viewModel(
                        factory = com.example.features.hod.providers.HODTimetableViewModelFactory(com.example.CamsApplication.instance.container.hodRepository)
                    ),
                    onNavigate = { route -> navigateToRoute(route) }
                )
            }
        }
        
        composable(AppRoutes.HOD_TIMETABLE_SETUP) {
            RoleGuard(
                currentRole = authState.role,
                allowedRoles = listOf("HOD"),
                isLoading = authState.isLoading,
                onUnauthorized = { navController.navigate(AppRoutes.UNAUTHORIZED) }
            ) {
                com.example.features.hod.screens.HODTimetableSetupScreen(onNavigate = { route ->
                    navigateToRoute(route)
                })
            }
        }

        composable(AppRoutes.HOD_RESEARCH_OVERVIEW) {
            RoleGuard(
                currentRole = authState.role,
                allowedRoles = listOf("HOD"),
                isLoading = authState.isLoading,
                onUnauthorized = { navController.navigate(AppRoutes.UNAUTHORIZED) }
            ) {
                val viewModel: com.example.features.hod.providers.HODResearchMonitoringViewModel = androidx.lifecycle.viewmodel.compose.viewModel(
                    factory = com.example.features.hod.providers.HODResearchMonitoringViewModelFactory(container.hodRepository)
                )
                com.example.features.hod.screens.HODResearchOverviewScreen(
                    viewModel = viewModel,
                    onNavigate = { route -> navigateToRoute(route) }
                )
            }
        }

        composable(AppRoutes.HOD_APPROVALS) {
            RoleGuard(
                currentRole = authState.role,
                allowedRoles = listOf("HOD"),
                isLoading = authState.isLoading,
                onUnauthorized = { navController.navigate(AppRoutes.UNAUTHORIZED) }
            ) {
                com.example.features.hod.screens.HODApprovalsScreen(
                    viewModel = androidx.lifecycle.viewmodel.compose.viewModel(
                        factory = com.example.features.hod.providers.HODApprovalsViewModelFactory(com.example.CamsApplication.instance.container.hodRepository)
                    ),
                    onNavigate = { route -> navigateToRoute(route) }
                )
            }
        }
        
        composable(AppRoutes.HOD_MARK_APPROVALS) {
            RoleGuard(
                currentRole = authState.role,
                allowedRoles = listOf("HOD"),
                isLoading = authState.isLoading,
                onUnauthorized = { navController.navigate(AppRoutes.UNAUTHORIZED) }
            ) {
                com.example.features.hod.screens.HODMarkApprovalsScreen(onNavigate = { route -> navigateToRoute(route) })
            }
        }
        
                composable(AppRoutes.HOD_FACULTY_WORKLOADS) {
            RoleGuard(
                currentRole = authState.role,
                allowedRoles = listOf("HOD"),
                isLoading = authState.isLoading,
                onUnauthorized = { navController.navigate(AppRoutes.UNAUTHORIZED) }
            ) {
                com.example.features.hod.screens.HODFacultyWorkloadsScreen(onNavigate = navigateToRoute)
            }
        }
        composable(AppRoutes.HOD_SUBJECT_ALLOCATION) {
            RoleGuard(
                currentRole = authState.role,
                allowedRoles = listOf("HOD"),
                isLoading = authState.isLoading,
                onUnauthorized = { navController.navigate(AppRoutes.UNAUTHORIZED) }
            ) {
                com.example.features.hod.screens.HODSubjectAllocationScreen(onNavigate = navigateToRoute)
            }
        }
        composable(AppRoutes.HOD_SUBSTITUTION_MGMT) {
            RoleGuard(
                currentRole = authState.role,
                allowedRoles = listOf("HOD"),
                isLoading = authState.isLoading,
                onUnauthorized = { navController.navigate(AppRoutes.UNAUTHORIZED) }
            ) {
                com.example.features.hod.screens.HODSubstitutionManagementScreen(onNavigate = navigateToRoute)
            }
        }
        composable(AppRoutes.HOD_FACULTY_APPROVAL) {
            RoleGuard(
                currentRole = authState.role,
                allowedRoles = listOf("HOD"),
                isLoading = authState.isLoading,
                onUnauthorized = { navController.navigate(AppRoutes.UNAUTHORIZED) }
            ) {
                com.example.features.hod.screens.HODFacultyApprovalScreen(onNavigate = navigateToRoute)
            }
        }
                composable(AppRoutes.HOD_MENTOR_ASSIGNMENT) {
            RoleGuard(
                currentRole = authState.role,
                allowedRoles = listOf("HOD"),
                isLoading = authState.isLoading,
                onUnauthorized = { navController.navigate(AppRoutes.UNAUTHORIZED) }
            ) {
                com.example.features.hod.screens.HODMentorAssignmentScreen(onNavigate = navigateToRoute)
            }
        }
        composable(AppRoutes.HOD_SYLLABUS_MGMT) {
            RoleGuard(
                currentRole = authState.role,
                allowedRoles = listOf("HOD"),
                isLoading = authState.isLoading,
                onUnauthorized = { navController.navigate(AppRoutes.UNAUTHORIZED) }
            ) {
                val viewModel: com.example.features.hod.providers.HODSyllabusManagementViewModel = androidx.lifecycle.viewmodel.compose.viewModel(
                    factory = com.example.features.hod.providers.HODSyllabusManagementViewModelFactory(container.hodRepository)
                )
                com.example.features.hod.screens.HODSyllabusManagementScreen(
                    viewModel = viewModel,
                    onNavigate = navigateToRoute
                )
            }
        }
        composable(AppRoutes.HOD_CLASS_ADVISOR) {
            RoleGuard(
                currentRole = authState.role,
                allowedRoles = listOf("HOD"),
                isLoading = authState.isLoading,
                onUnauthorized = { navController.navigate(AppRoutes.UNAUTHORIZED) }
            ) {
                com.example.features.hod.screens.HODClassAdvisorManagementScreen(onNavigate = navigateToRoute)
            }
        }
        composable(AppRoutes.HOD_ATTENDANCE_MONITORING) {
            RoleGuard(
                currentRole = authState.role,
                allowedRoles = listOf("HOD"),
                isLoading = authState.isLoading,
                onUnauthorized = { navController.navigate(AppRoutes.UNAUTHORIZED) }
            ) {
                val viewModel: com.example.features.hod.providers.HODAttendanceMonitoringViewModel = androidx.lifecycle.viewmodel.compose.viewModel(
                    factory = com.example.features.hod.providers.HODAttendanceMonitoringViewModelFactory(container.hodRepository)
                )
                com.example.features.hod.screens.HODAttendanceMonitoringScreen(
                    viewModel = viewModel,
                    onNavigate = navigateToRoute
                )
            }
        }
        composable(AppRoutes.HOD_LEAVE_APPROVALS) {
            RoleGuard(
                currentRole = authState.role,
                allowedRoles = listOf("HOD"),
                isLoading = authState.isLoading,
                onUnauthorized = { navController.navigate(AppRoutes.UNAUTHORIZED) }
            ) {
                com.example.features.hod.screens.HODLeaveApprovalsScreen(onNavigate = { route -> navigateToRoute(route) })
            }
        }
        
        composable(AppRoutes.HOD_PROFILE_APPROVALS) {
            RoleGuard(
                currentRole = authState.role,
                allowedRoles = listOf("HOD"),
                isLoading = authState.isLoading,
                onUnauthorized = { navController.navigate(AppRoutes.UNAUTHORIZED) }
            ) {
                com.example.features.hod.screens.HODProfileApprovalsScreen(onNavigate = { route -> navigateToRoute(route) })
            }
        }
        
        composable(AppRoutes.HOD_ATTENDANCE_CORRECTION) {
            RoleGuard(
                currentRole = authState.role,
                allowedRoles = listOf("HOD"),
                isLoading = authState.isLoading,
                onUnauthorized = { navController.navigate(AppRoutes.UNAUTHORIZED) }
            ) {
                com.example.features.hod.screens.HODAttendanceCorrectionApprovalsScreen(onNavigate = { route -> navigateToRoute(route) })
            }
        }

        composable(AppRoutes.HOD_CIRCULARS) {
            RoleGuard(
                currentRole = authState.role,
                allowedRoles = listOf("HOD"),
                isLoading = authState.isLoading,
                onUnauthorized = { navController.navigate(AppRoutes.UNAUTHORIZED) }
            ) {
                com.example.features.hod.screens.HODCircularsScreen(
                    viewModel = androidx.lifecycle.viewmodel.compose.viewModel(
                        factory = com.example.features.hod.providers.HODCircularsViewModelFactory(com.example.CamsApplication.instance.container.apiService)
                    ),
                    onNavigate = { route -> navigateToRoute(route) }
                )
            }
        }
        
        composable(AppRoutes.HOD_STUDY_MATERIALS) {
            RoleGuard(
                currentRole = authState.role,
                allowedRoles = listOf("HOD"),
                isLoading = authState.isLoading,
                onUnauthorized = { navController.navigate(AppRoutes.UNAUTHORIZED) }
            ) {
                com.example.features.hod.screens.HODStudyMaterialVerificationScreen(onNavigate = { route -> navigateToRoute(route) })
            }
        }
        
        composable(AppRoutes.HOD_REPORTS) {
            RoleGuard(
                currentRole = authState.role,
                allowedRoles = listOf("HOD"),
                isLoading = authState.isLoading,
                onUnauthorized = { navController.navigate(AppRoutes.UNAUTHORIZED) }
            ) {
                val viewModel: com.example.features.hod.providers.HODDepartmentAnalyticsViewModel = androidx.lifecycle.viewmodel.compose.viewModel(
                    factory = com.example.features.hod.providers.HODDepartmentAnalyticsViewModelFactory(container.hodRepository)
                )
                com.example.features.hod.screens.HODReportsAnalyticsScreen(
                    viewModel = viewModel,
                    onNavigate = { route -> navigateToRoute(route) }
                )
            }
        }
        
        composable(AppRoutes.HOD_RESEARCH_MONITORING) {
            RoleGuard(
                currentRole = authState.role,
                allowedRoles = listOf("HOD"),
                isLoading = authState.isLoading,
                onUnauthorized = { navController.navigate(AppRoutes.UNAUTHORIZED) }
            ) {
                val viewModel: com.example.features.hod.providers.HODResearchMonitoringViewModel = androidx.lifecycle.viewmodel.compose.viewModel(
                    factory = com.example.features.hod.providers.HODResearchMonitoringViewModelFactory(container.hodRepository)
                )
                com.example.features.hod.screens.HODResearchMonitoringScreen(
                    viewModel = viewModel,
                    onNavigate = { route -> navigateToRoute(route) }
                )
            }
        }
        
        composable(AppRoutes.HOD_CALENDAR) {
            RoleGuard(
                currentRole = authState.role,
                allowedRoles = listOf("HOD"),
                isLoading = authState.isLoading,
                onUnauthorized = { navController.navigate(AppRoutes.UNAUTHORIZED) }
            ) {
                com.example.features.hod.screens.HODCalendarManagementScreen(onNavigate = { route -> navigateToRoute(route) })
            }
        }
        
        composable(AppRoutes.HOD_COMMUNICATION) {
            RoleGuard(
                currentRole = authState.role,
                allowedRoles = listOf("HOD"),
                isLoading = authState.isLoading,
                onUnauthorized = { navController.navigate(AppRoutes.UNAUTHORIZED) }
            ) {
                com.example.features.hod.screens.HODCommunicationCenterScreen(onNavigate = { route -> navigateToRoute(route) })
            }
        }
        
        composable(AppRoutes.ADMIN_DASHBOARD) { 
            RoleGuard(
                currentRole = authState.role,
                allowedRoles = listOf("ADMIN", "SUPER_ADMIN"),
                isLoading = authState.isLoading,
                onUnauthorized = { navController.navigate(AppRoutes.UNAUTHORIZED) }
            ) {
                com.example.features.admin.screens.AdminDashboardScreen(onNavigate = { route ->
                    navigateToRoute(route)
                }) 
            }
        }
        
        composable(AppRoutes.ADMIN_USER_MGMT) {
            RoleGuard(
                currentRole = authState.role,
                allowedRoles = listOf("ADMIN", "SUPER_ADMIN"),
                isLoading = authState.isLoading,
                onUnauthorized = { navController.navigate(AppRoutes.UNAUTHORIZED) }
            ) {
                val adminUserViewModel: com.example.features.admin.providers.AdminUserViewModel = viewModel(
                    factory = container.provideAdminUserViewModelFactory()
                )
                com.example.features.admin.screens.AdminUserMgmtScreen(
                    onNavigate = { route -> navigateToRoute(route) },
                    viewModel = adminUserViewModel
                )
            }
        }

        composable(AppRoutes.ADMIN_FEE_MGMT) {
            RoleGuard(
                currentRole = authState.role,
                allowedRoles = listOf("ADMIN", "SUPER_ADMIN"),
                isLoading = authState.isLoading,
                onUnauthorized = { navController.navigate(AppRoutes.UNAUTHORIZED) }
            ) {
                com.example.features.admin.screens.AdminFeeMgmtScreen(onNavigate = { route -> navigateToRoute(route) })
            }
        }

        composable(AppRoutes.ADMIN_SALARY_MGMT) {
            RoleGuard(
                currentRole = authState.role,
                allowedRoles = listOf("ADMIN", "SUPER_ADMIN"),
                isLoading = authState.isLoading,
                onUnauthorized = { navController.navigate(AppRoutes.UNAUTHORIZED) }
            ) {
                com.example.features.admin.screens.AdminSalaryMgmtScreen(onNavigate = { route -> navigateToRoute(route) })
            }
        }
        
        composable(AppRoutes.ADMIN_EXAM_MGMT) {
            RoleGuard(
                currentRole = authState.role,
                allowedRoles = listOf("ADMIN", "SUPER_ADMIN"),
                isLoading = authState.isLoading,
                onUnauthorized = { navController.navigate(AppRoutes.UNAUTHORIZED) }
            ) {
                com.example.features.admin.screens.AdminExamMgmtScreen(onNavigate = { route -> navigateToRoute(route) })
            }
        }
        
        composable(AppRoutes.ADMIN_FACULTY_ASSIGNMENT) {
            RoleGuard(
                currentRole = authState.role,
                allowedRoles = listOf("ADMIN", "SUPER_ADMIN"),
                isLoading = authState.isLoading,
                onUnauthorized = { navController.navigate(AppRoutes.UNAUTHORIZED) }
            ) {
                com.example.features.admin.screens.AdminFacultyAssignmentScreen(onNavigate = { route -> navigateToRoute(route) })
            }
        }
        
        composable(AppRoutes.ADMIN_ACADEMIC_CALENDAR) {
            RoleGuard(
                currentRole = authState.role,
                allowedRoles = listOf("ADMIN", "SUPER_ADMIN"),
                isLoading = authState.isLoading,
                onUnauthorized = { navController.navigate(AppRoutes.UNAUTHORIZED) }
            ) {
                com.example.features.admin.screens.AdminAcademicCalendarScreen(onNavigate = { route -> navigateToRoute(route) })
            }
        }
        
        composable(AppRoutes.ADMIN_NOTIFICATIONS) {
            RoleGuard(
                currentRole = authState.role,
                allowedRoles = listOf("ADMIN", "SUPER_ADMIN"),
                isLoading = authState.isLoading,
                onUnauthorized = { navController.navigate(AppRoutes.UNAUTHORIZED) }
            ) {
                com.example.features.admin.screens.AdminNotificationsScreen(onNavigate = { route -> navigateToRoute(route) })
            }
        }
        
        composable(AppRoutes.ADMIN_CIRCULARS) {
            RoleGuard(
                currentRole = authState.role,
                allowedRoles = listOf("ADMIN", "SUPER_ADMIN"),
                isLoading = authState.isLoading,
                onUnauthorized = { navController.navigate(AppRoutes.UNAUTHORIZED) }
            ) {
                com.example.features.admin.screens.AdminCircularsScreen(onNavigate = { route -> navigateToRoute(route) })
            }
        }

        composable(AppRoutes.ADMIN_REPORTS) {
            RoleGuard(
                currentRole = authState.role,
                allowedRoles = listOf("ADMIN", "SUPER_ADMIN"),
                isLoading = authState.isLoading,
                onUnauthorized = { navController.navigate(AppRoutes.UNAUTHORIZED) }
            ) {
                com.example.features.admin.screens.AdminReportsScreen(onNavigate = { route ->
                    navigateToRoute(route)
                })
            }
        }

        composable(AppRoutes.ADMIN_SYSTEM_CONFIG) {
            RoleGuard(
                currentRole = authState.role,
                allowedRoles = listOf("ADMIN", "SUPER_ADMIN"),
                isLoading = authState.isLoading,
                onUnauthorized = { navController.navigate(AppRoutes.UNAUTHORIZED) }
            ) {
                com.example.features.admin.screens.AdminSystemConfigScreen(onNavigate = { route ->
                    navigateToRoute(route)
                })
            }
        }

        composable(AppRoutes.ADMIN_LOGS) {
            RoleGuard(
                currentRole = authState.role,
                allowedRoles = listOf("ADMIN", "SUPER_ADMIN"),
                isLoading = authState.isLoading,
                onUnauthorized = { navController.navigate(AppRoutes.UNAUTHORIZED) }
            ) {
                com.example.features.admin.screens.AdminLogsScreen(onNavigate = { route ->
                    navigateToRoute(route)
                })
            }
        }

        composable(AppRoutes.ADMIN_BACKUPS) {
            RoleGuard(
                currentRole = authState.role,
                allowedRoles = listOf("ADMIN", "SUPER_ADMIN"),
                isLoading = authState.isLoading,
                onUnauthorized = { navController.navigate(AppRoutes.UNAUTHORIZED) }
            ) {
                com.example.features.admin.screens.AdminBackupsScreen(onNavigate = { route ->
                    navigateToRoute(route)
                })
            }
        }
        
        composable(AppRoutes.ADMIN_ACADEMIC_YEAR_CONFIG) {
            RoleGuard(
                currentRole = authState.role,
                allowedRoles = listOf("ADMIN", "SUPER_ADMIN"),
                isLoading = authState.isLoading,
                onUnauthorized = { navController.navigate(AppRoutes.UNAUTHORIZED) }
            ) {
                com.example.features.admin.screens.AdminAcademicYearConfigScreen(onNavigate = { route -> navigateToRoute(route) })
            }
        }
        
        composable(AppRoutes.ADMIN_ATTENDANCE_DEFAULTERS) {
            RoleGuard(
                currentRole = authState.role,
                allowedRoles = listOf("ADMIN", "SUPER_ADMIN"),
                isLoading = authState.isLoading,
                onUnauthorized = { navController.navigate(AppRoutes.UNAUTHORIZED) }
            ) {
                com.example.features.admin.screens.AdminAttendanceDefaultersScreen(onNavigate = { route -> navigateToRoute(route) })
            }
        }
        
        composable(AppRoutes.ADMIN_BATCH_SETUP) {
            RoleGuard(
                currentRole = authState.role,
                allowedRoles = listOf("ADMIN", "SUPER_ADMIN"),
                isLoading = authState.isLoading,
                onUnauthorized = { navController.navigate(AppRoutes.UNAUTHORIZED) }
            ) {
                com.example.features.admin.screens.AdminBatchSetupScreen(onNavigate = { route -> navigateToRoute(route) })
            }
        }
        
        composable(AppRoutes.ADMIN_COURSE_SETUP) {
            RoleGuard(
                currentRole = authState.role,
                allowedRoles = listOf("ADMIN", "SUPER_ADMIN"),
                isLoading = authState.isLoading,
                onUnauthorized = { navController.navigate(AppRoutes.UNAUTHORIZED) }
            ) {
                com.example.features.admin.screens.AdminCourseSetupScreen(onNavigate = { route -> navigateToRoute(route) })
            }
        }
        
        composable(AppRoutes.ADMIN_COLLECT_FEE) {
            RoleGuard(
                currentRole = authState.role,
                allowedRoles = listOf("ADMIN", "SUPER_ADMIN"),
                isLoading = authState.isLoading,
                onUnauthorized = { navController.navigate(AppRoutes.UNAUTHORIZED) }
            ) {
                com.example.features.admin.screens.AdminCollectFeeScreen(onNavigate = { route -> navigateToRoute(route) })
            }
        }
        
        composable(AppRoutes.ADMIN_ACADEMIC_CATALOG) {
            RoleGuard(
                currentRole = authState.role,
                allowedRoles = listOf("ADMIN", "SUPER_ADMIN"),
                isLoading = authState.isLoading,
                onUnauthorized = { navController.navigate(AppRoutes.UNAUTHORIZED) }
            ) {
                com.example.features.admin.screens.AdminAcademicCatalogScreen(onNavigate = { route -> navigateToRoute(route) })
            }
        }
        
        composable(AppRoutes.PRINCIPAL_DASHBOARD) { 
            RoleGuard(
                currentRole = authState.role,
                allowedRoles = listOf("PRINCIPAL"),
                isLoading = authState.isLoading,
                onUnauthorized = { navController.navigate(AppRoutes.UNAUTHORIZED) }
            ) {
                val viewModel: com.example.features.principal.providers.PrincipalViewModel = viewModel(
                    factory = com.example.features.principal.providers.PrincipalViewModelFactory(container.principalRepository)
                )
                com.example.features.principal.screens.PrincipalDashboardScreen(
                    viewModel = viewModel,
                    onNavigate = { route ->
                        navigateToRoute(route)
                    }
                ) 
            }
        }

        composable(AppRoutes.PRINCIPAL_PERFORMANCE) {
            RoleGuard(
                currentRole = authState.role,
                allowedRoles = listOf("PRINCIPAL"),
                isLoading = authState.isLoading,
                onUnauthorized = { navController.navigate(AppRoutes.UNAUTHORIZED) }
            ) {
                com.example.features.principal.screens.PrincipalInstitutionalPerformanceScreen(onNavigate = { route -> navigateToRoute(route) })
            }
        }
        
        composable(AppRoutes.PRINCIPAL_FACULTY_OVERVIEW) {
            RoleGuard(
                currentRole = authState.role,
                allowedRoles = listOf("PRINCIPAL"),
                isLoading = authState.isLoading,
                onUnauthorized = { navController.navigate(AppRoutes.UNAUTHORIZED) }
            ) {
                com.example.features.shared.PlaceholderScreen("Faculty Overview", onNavigate = { route -> navigateToRoute(route) })
            }
        }
        
        composable(AppRoutes.PRINCIPAL_BUDGET_GRANTS) {
            RoleGuard(
                currentRole = authState.role,
                allowedRoles = listOf("PRINCIPAL"),
                isLoading = authState.isLoading,
                onUnauthorized = { navController.navigate(AppRoutes.UNAUTHORIZED) }
            ) {
                com.example.features.shared.PlaceholderScreen("Budget & Grants", onNavigate = { route -> navigateToRoute(route) })
            }
        }
        
        composable(AppRoutes.PRINCIPAL_STRATEGIC_NOTICES) {
            RoleGuard(
                currentRole = authState.role,
                allowedRoles = listOf("PRINCIPAL"),
                isLoading = authState.isLoading,
                onUnauthorized = { navController.navigate(AppRoutes.UNAUTHORIZED) }
            ) {
                com.example.features.shared.PlaceholderScreen("Strategic Notices", onNavigate = { route -> navigateToRoute(route) })
            }
        }
        
        composable(AppRoutes.PRINCIPAL_CALENDAR) {
            RoleGuard(
                currentRole = authState.role,
                allowedRoles = listOf("PRINCIPAL"),
                isLoading = authState.isLoading,
                onUnauthorized = { navController.navigate(AppRoutes.UNAUTHORIZED) }
            ) {
                com.example.features.shared.PlaceholderScreen("Institutional Calendar", onNavigate = { route -> navigateToRoute(route) })
            }
        }
        
        composable(AppRoutes.PRINCIPAL_APPROVALS) {
            RoleGuard(
                currentRole = authState.role,
                allowedRoles = listOf("PRINCIPAL"),
                isLoading = authState.isLoading,
                onUnauthorized = { navController.navigate(AppRoutes.UNAUTHORIZED) }
            ) {
                com.example.features.principal.screens.PrincipalApprovalsScreen(onNavigate = { route -> navigateToRoute(route) })
            }
        }
        
        composable(AppRoutes.PRINCIPAL_CLASS_DIARY) {
            RoleGuard(
                currentRole = authState.role,
                allowedRoles = listOf("PRINCIPAL"),
                isLoading = authState.isLoading,
                onUnauthorized = { navController.navigate(AppRoutes.UNAUTHORIZED) }
            ) {
                com.example.features.principal.screens.PrincipalClassDiaryScreen(onNavigate = { route -> navigateToRoute(route) })
            }
        }
        
        composable(AppRoutes.PRINCIPAL_EVENTS_MGMT) {
            RoleGuard(
                currentRole = authState.role,
                allowedRoles = listOf("PRINCIPAL"),
                isLoading = authState.isLoading,
                onUnauthorized = { navController.navigate(AppRoutes.UNAUTHORIZED) }
            ) {
                com.example.features.principal.screens.PrincipalEventsManagementScreen(onNavigate = { route -> navigateToRoute(route) })
            }
        }
        
        composable(AppRoutes.PRINCIPAL_INFRASTRUCTURE) {
            RoleGuard(
                currentRole = authState.role,
                allowedRoles = listOf("PRINCIPAL"),
                isLoading = authState.isLoading,
                onUnauthorized = { navController.navigate(AppRoutes.UNAUTHORIZED) }
            ) {
                val factory = com.example.features.principal.providers.PrincipalViewModelFactory(container.principalRepository)
                val viewModel: com.example.features.principal.providers.PrincipalInfrastructureViewModel = androidx.lifecycle.viewmodel.compose.viewModel(factory = factory)
                com.example.features.principal.screens.PrincipalInfrastructureScreen(onNavigate = { route -> navigateToRoute(route) }, viewModel = viewModel)
            }
        }
        
        composable(AppRoutes.PRINCIPAL_RESEARCH_COMPLIANCE) {
            RoleGuard(
                currentRole = authState.role,
                allowedRoles = listOf("PRINCIPAL"),
                isLoading = authState.isLoading,
                onUnauthorized = { navController.navigate(AppRoutes.UNAUTHORIZED) }
            ) {
                val factory = com.example.features.principal.providers.PrincipalViewModelFactory(container.principalRepository)
                val viewModel: com.example.features.principal.providers.PrincipalResearchViewModel = androidx.lifecycle.viewmodel.compose.viewModel(factory = factory)
                com.example.features.principal.screens.PrincipalResearchComplianceScreen(onNavigate = { route -> navigateToRoute(route) }, viewModel = viewModel)
            }
        }
        
        composable(AppRoutes.PRINCIPAL_STUDY_MATERIALS) {
            RoleGuard(
                currentRole = authState.role,
                allowedRoles = listOf("PRINCIPAL"),
                isLoading = authState.isLoading,
                onUnauthorized = { navController.navigate(AppRoutes.UNAUTHORIZED) }
            ) {
                com.example.features.principal.screens.PrincipalStudyMaterialsScreen(onNavigate = { route -> navigateToRoute(route) })
            }
        }
        composable(AppRoutes.PRINCIPAL_GRIEVANCES) {
            RoleGuard(
                currentRole = authState.role,
                allowedRoles = listOf("PRINCIPAL"),
                isLoading = authState.isLoading,
                onUnauthorized = { navController.navigate(AppRoutes.UNAUTHORIZED) }
            ) {
                com.example.features.principal.screens.PrincipalGrievancesScreen(onNavigate = { route -> navigateToRoute(route) })
            }
        }
        composable(AppRoutes.PRINCIPAL_CIRCULARS) {
            RoleGuard(
                currentRole = authState.role,
                allowedRoles = listOf("PRINCIPAL"),
                isLoading = authState.isLoading,
                onUnauthorized = { navController.navigate(AppRoutes.UNAUTHORIZED) }
            ) {
                com.example.features.principal.screens.PrincipalCircularsScreen(onNavigate = { route -> navigateToRoute(route) })
            }
        }
        
        composable(AppRoutes.LEXNOVA) { 
            RoleGuard(
                currentRole = authState.role,
                allowedRoles = listOf("STUDENT"),
                isLoading = authState.isLoading,
                onUnauthorized = { navController.navigate(AppRoutes.UNAUTHORIZED) }
            ) {
                val viewModel: LexNovaViewModel = viewModel(factory = LexNovaViewModelFactory(container.studentRepository))
                LexNovaScreen(
                    viewModel = viewModel,
                    onNavigate = { route -> 
                        if (route != AppRoutes.LEXNOVA) {
                            navigateToRoute(route)
                        }
                    }
                ) 
            }
        }
        
        composable(AppRoutes.INTERNSHIPS) { 
            RoleGuard(
                currentRole = authState.role,
                allowedRoles = listOf("STUDENT"),
                isLoading = authState.isLoading,
                onUnauthorized = { navController.navigate(AppRoutes.UNAUTHORIZED) }
            ) {
                val viewModel: InternshipsViewModel = viewModel(factory = InternshipsViewModelFactory(container.studentRepository))
                com.example.features.campus_life.screens.InternshipsScreen(
                    viewModel = viewModel,
                    onNavigate = { route -> 
                        if (route != AppRoutes.INTERNSHIPS) {
                            navigateToRoute(route)
                        }
                    }
                ) 
            }
        }
        
        composable(AppRoutes.CERTIFICATIONS) { 
            RoleGuard(
                currentRole = authState.role,
                allowedRoles = listOf("STUDENT"),
                isLoading = authState.isLoading,
                onUnauthorized = { navController.navigate(AppRoutes.UNAUTHORIZED) }
            ) {
                val viewModel: CertificationsViewModel = viewModel(factory = CertificationsViewModelFactory(container.studentRepository))
                com.example.features.campus_life.screens.CertificationsScreen(
                    viewModel = viewModel,
                    onNavigate = { route -> 
                        if (route != AppRoutes.CERTIFICATIONS) {
                            navigateToRoute(route)
                        }
                    }
                ) 
            }
        }
        
        composable(AppRoutes.PARENT_MARKS) {
            RoleGuard(
                currentRole = authState.role,
                allowedRoles = listOf("PARENT"),
                isLoading = authState.isLoading,
                onUnauthorized = { navController.navigate(AppRoutes.UNAUTHORIZED) }
            ) {
                val viewModel: com.example.features.parent.providers.ParentMarksViewModel = viewModel(factory = com.example.features.parent.providers.ParentMarksViewModelFactory(container.parentRepository))
                com.example.features.parent.screens.ExamResultsScreen(
                    viewModel = viewModel,
                    onNavigate = { route ->
                        if (route != AppRoutes.PARENT_MARKS) {
                            navigateToRoute(route)
                        }
                    }
                )
            }
        }
        
        composable(AppRoutes.PARENT_FEES) {
            RoleGuard(
                currentRole = authState.role,
                allowedRoles = listOf("PARENT"),
                isLoading = authState.isLoading,
                onUnauthorized = { navController.navigate(AppRoutes.UNAUTHORIZED) }
            ) {
                val viewModel: com.example.features.parent.providers.ParentFeesViewModel = viewModel(factory = com.example.features.parent.providers.ParentFeesViewModelFactory(container.parentRepository))
                com.example.features.parent.screens.FeeStatusScreen(
                    viewModel = viewModel,
                    onNavigate = { route ->
                        if (route != AppRoutes.PARENT_FEES) {
                            navigateToRoute(route)
                        }
                    }
                )
            }
        }
        
        composable(AppRoutes.PARENT_CIRCULARS) {
            RoleGuard(
                currentRole = authState.role,
                allowedRoles = listOf("PARENT"),
                isLoading = authState.isLoading,
                onUnauthorized = { navController.navigate(AppRoutes.UNAUTHORIZED) }
            ) {
                val viewModel: com.example.features.parent.providers.ParentNoticesViewModel = viewModel(factory = com.example.features.parent.providers.ParentNoticesViewModelFactory(container.parentRepository))
                com.example.features.parent.screens.ParentNoticesScreen(
                    viewModel = viewModel,
                    onNavigate = { route ->
                        if (route != AppRoutes.PARENT_CIRCULARS) {
                            navigateToRoute(route)
                        }
                    }
                )
            }
        }

        composable(AppRoutes.LEGAL_EVENTS) {
            RoleGuard(
                currentRole = authState.role,
                allowedRoles = listOf("STUDENT"),
                isLoading = authState.isLoading,
                onUnauthorized = { navController.navigate(AppRoutes.UNAUTHORIZED) }
            ) {
                val viewModel: LegalEventsViewModel = viewModel(factory = LegalEventsViewModelFactory(container.studentRepository))
                LegalEventsHubScreen(
                    viewModel = viewModel,
                    onNavigate = { route -> navigateToRoute(route) }
                )
            }
        }
        composable(AppRoutes.LEGAL_SKILLS) {
            RoleGuard(
                currentRole = authState.role,
                allowedRoles = listOf("STUDENT"),
                isLoading = authState.isLoading,
                onUnauthorized = { navController.navigate(AppRoutes.UNAUTHORIZED) }
            ) {
                val viewModel: LegalSkillsViewModel = viewModel(factory = LegalSkillsViewModelFactory(container.studentRepository))
                LegalSkillsScreen(
                    viewModel = viewModel,
                    onNavigate = { route -> navigateToRoute(route) }
                )
            }
        }
        composable(AppRoutes.LEXSPHERE) {
            RoleGuard(
                currentRole = authState.role,
                allowedRoles = listOf("STUDENT"),
                isLoading = authState.isLoading,
                onUnauthorized = { navController.navigate(AppRoutes.UNAUTHORIZED) }
            ) {
                val viewModel: LexSphereViewModel = viewModel(factory = LexSphereViewModelFactory(container.studentRepository))
                LexSphereScreen(
                    viewModel = viewModel,
                    onNavigate = { route -> navigateToRoute(route) }
                )
            }
        }
        composable(AppRoutes.ACTIVITY_POINTS) {
            RoleGuard(
                currentRole = authState.role,
                allowedRoles = listOf("STUDENT"),
                isLoading = authState.isLoading,
                onUnauthorized = { navController.navigate(AppRoutes.UNAUTHORIZED) }
            ) {
                val viewModel: ActivityPointsViewModel = viewModel(factory = ActivityPointsViewModelFactory(container.studentRepository))
                com.example.features.student.screens.ActivityPointsScreen(
                    viewModel = viewModel,
                    onNavigate = { route -> navigateToRoute(route) }
                )
            }
        }
        composable(AppRoutes.COMMUNITY_SERVICE) {
            RoleGuard(
                currentRole = authState.role,
                allowedRoles = listOf("STUDENT"),
                isLoading = authState.isLoading,
                onUnauthorized = { navController.navigate(AppRoutes.UNAUTHORIZED) }
            ) {
                val viewModel: CommunityServiceViewModel = viewModel(factory = CommunityServiceViewModelFactory(container.studentRepository))
                CommunityServiceScreen(
                    viewModel = viewModel,
                    onNavigate = { route -> navigateToRoute(route) }
                )
            }
        }
        composable(AppRoutes.INNOVATION_WALL) {
            RoleGuard(
                currentRole = authState.role,
                allowedRoles = listOf("STUDENT"),
                isLoading = authState.isLoading,
                onUnauthorized = { navController.navigate(AppRoutes.UNAUTHORIZED) }
            ) {
                val viewModel: InnovationWallViewModel = viewModel(factory = InnovationWallViewModelFactory(container.studentRepository))
                InnovationWallScreen(
                    viewModel = viewModel,
                    onNavigate = { route -> navigateToRoute(route) }
                )
            }
        }
        composable(AppRoutes.PROJECT_SHOWCASE) {
            RoleGuard(
                currentRole = authState.role,
                allowedRoles = listOf("STUDENT"),
                isLoading = authState.isLoading,
                onUnauthorized = { navController.navigate(AppRoutes.UNAUTHORIZED) }
            ) {
                val viewModel: ProjectShowcaseViewModel = viewModel(factory = ProjectShowcaseViewModelFactory(container.studentRepository))
                ProjectShowcaseScreen(
                    viewModel = viewModel,
                    onNavigate = { route -> navigateToRoute(route) }
                )
            }
        }
    }
}
