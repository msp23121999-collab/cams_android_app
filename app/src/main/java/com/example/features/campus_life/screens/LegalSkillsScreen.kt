package com.example.features.campus_life.screens

import androidx.compose.animation.*
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.blur
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.core.theme.*
import com.example.core.ui.CamsScreen
import com.example.features.campus_life.models.*
import com.example.features.campus_life.providers.LegalSkillsViewModel
import com.example.features.student.widgets.StudentDrawer
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LegalSkillsScreen(
    onNavigate: (String) -> Unit,
    viewModel: LegalSkillsViewModel = viewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val drawerState = rememberDrawerState(initialValue = DrawerValue.Closed)
    val scope = rememberCoroutineScope()

    ModalNavigationDrawer(
        drawerState = drawerState,
        drawerContent = {
            StudentDrawer(
                currentRoute = "/student/legal-skills",
                onNavigate = { route ->
                    scope.launch { drawerState.close() }
                    onNavigate(route)
                }
            )
        }
    ) {
        CamsScreen(
        scrollable = true,
            title = "Legal Skills",
            subtitle = "Professional Upskilling Portal",
            navigationIcon = {
                IconButton(onClick = { scope.launch { drawerState.open() } }) {
                    Icon(Icons.Filled.Menu, contentDescription = "Menu", tint = Color.White)
                }
            },
            verticalArrangement = Arrangement.spacedBy(24.dp)
        ) {
            // Enterprise Hero Banner
            LegalSkillsHero(
                certifiedCount = uiState.registered.values.count { it.certificateEarned },
                activeCount = uiState.registered.values.count { it.progress < 100 }
            )

            // Navigation Tabs
            LegalSkillsTabs(
                activeTab = uiState.activeTab,
                onTabSelected = { viewModel.updateTab(it) }
            )

            // Tab Content
            when (uiState.activeTab) {
                "browse" -> CourseCatalogView(
                    courses = uiState.courses,
                    registered = uiState.registered,
                    onRegister = { viewModel.registerCourse(it) }
                )
                "my-learning" -> MyLearningView(
                    registered = uiState.registered,
                    courses = uiState.courses,
                    onIncrement = { viewModel.incrementProgress(it) },
                    onBrowse = { viewModel.updateTab("browse") }
                )
                "workshops" -> WorkshopsMootsView(
                    workshops = uiState.workshops,
                    moots = uiState.mootActivities
                )
                "resources" -> ResourceCenterView(
                    caseStudies = uiState.caseStudies
                )
                "faculty" -> FacultyPortalView(
                    onPublish = { viewModel.publishCourse(it) }
                )
            }
        }
    }
}

@Composable
fun LegalSkillsHero(certifiedCount: Int, activeCount: Int) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(32.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)
    ) {
        Box(modifier = Modifier.padding(24.dp)) {
            // Ambient Orbs
            Box(
                modifier = Modifier
                    .size(150.dp)
                    .align(Alignment.TopEnd)
                    .offset(x = 40.dp, y = (-40).dp)
                    .blur(60.dp)
                    .clip(CircleShape)
                    .background(Purple500.copy(alpha = 0.1f))
            )

            Column {
                Surface(
                    color = Purple50,
                    shape = CircleShape,
                    border = BorderStroke(1.dp, Purple100)
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Icon(Icons.Filled.Shield, contentDescription = null, tint = Purple600, modifier = Modifier.size(12.dp))
                        Text("ENTERPRISE LLMS", style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Black, color = Purple700, letterSpacing = 1.sp))
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                Text(
                    text = "Legal Skills &\nCertification Hub",
                    style = MaterialTheme.typography.headlineMedium.copy(fontWeight = FontWeight.Black, color = Slate900)
                )

                Spacer(modifier = Modifier.height(12.dp))

                Text(
                    text = "A premier learning management system bridging academia and legal practice. Enroll in specialized courses and earn credentials.",
                    style = MaterialTheme.typography.bodySmall.copy(color = Slate500, lineHeight = 18.sp)
                )

                Spacer(modifier = Modifier.height(24.dp))

                Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                    HeroStatCard(Icons.Filled.EmojiEvents, certifiedCount.toString(), "Certified")
                    HeroStatCard(Icons.Filled.MenuBook, activeCount.toString(), "Active")
                }
            }
        }
    }
}

@Composable
fun HeroStatCard(icon: ImageVector, value: String, label: String) {
    Surface(
        modifier = Modifier.width(100.dp),
        color = Color.White,
        shape = RoundedCornerShape(20.dp),
        border = BorderStroke(1.dp, Slate200),
        shadowElevation = 2.dp
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Icon(icon, contentDescription = null, tint = Purple500, modifier = Modifier.size(24.dp))
            Spacer(modifier = Modifier.height(8.dp))
            Text(value, style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Black, color = Slate900))
            Text(label, style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold, color = Slate400, letterSpacing = 0.5.sp))
        }
    }
}

@Composable
fun LegalSkillsTabs(activeTab: String, onTabSelected: (String) -> Unit) {
    val tabs = listOf(
        "browse" to "Browse",
        "my-learning" to "My Learning",
        "workshops" to "Workshops",
        "resources" to "Resources",
        "faculty" to "Faculty"
    )

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .horizontalScroll(rememberScrollState()),
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        tabs.forEach { (id, label) ->
            val selected = activeTab == id
            Surface(
                onClick = { onTabSelected(id) },
                color = if (selected) Purple600 else Color.White,
                shape = RoundedCornerShape(16.dp),
                border = if (selected) null else BorderStroke(1.dp, Slate200),
                shadowElevation = if (selected) 4.dp else 0.dp
            ) {
                Text(
                    text = label,
                    modifier = Modifier.padding(horizontal = 20.dp, vertical = 12.dp),
                    style = MaterialTheme.typography.labelLarge.copy(
                        fontWeight = FontWeight.Bold,
                        color = if (selected) Color.White else Slate600
                    )
                )
            }
        }
    }
}

@Composable
fun CourseCatalogView(
    courses: List<LawCourse>,
    registered: Map<String, LearningProgress>,
    onRegister: (String) -> Unit
) {
    Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
        Text("Course Catalog", style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold, color = Slate900))

        if (courses.isEmpty()) {
            Box(modifier = Modifier.fillMaxWidth().padding(32.dp), contentAlignment = Alignment.Center) {
                Text("No courses available in the catalog.", style = MaterialTheme.typography.bodyMedium, color = Slate500)
            }
        } else {
            courses.forEach { course ->
                val isEnrolled = registered.containsKey(course.id)
                CourseItemCard(course, isEnrolled, onRegister)
            }
        }
    }
}

@Composable
fun CourseItemCard(course: LawCourse, isEnrolled: Boolean, onRegister: (String) -> Unit) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        border = BorderStroke(1.dp, if (isEnrolled) Purple200 else Slate200)
    ) {
        Column(modifier = Modifier.padding(20.dp)) {
            Row(horizontalArrangement = Arrangement.SpaceBetween, modifier = Modifier.fillMaxWidth()) {
                Surface(color = Purple50, shape = RoundedCornerShape(8.dp)) {
                    Text(
                        course.category,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                        style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Black, color = Purple700)
                    )
                }
                Text(course.level, style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold, color = Slate400))
            }

            Spacer(modifier = Modifier.height(12.dp))

            Text(course.title, style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold, color = Slate900))
            Text(course.instructor, style = MaterialTheme.typography.bodySmall.copy(color = Slate500))

            Spacer(modifier = Modifier.height(16.dp))

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Slate50, RoundedCornerShape(12.dp))
                    .padding(12.dp),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                CourseStat(course.duration, "Duration")
                CourseStat("${course.credits} Cr", "Credits")
                CourseStat(course.enrolled.toString(), "Enrolled")
            }

            Spacer(modifier = Modifier.height(20.dp))

            if (isEnrolled) {
                Button(
                    onClick = {},
                    enabled = false,
                    modifier = Modifier.fillMaxWidth(),
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFECFDF5), disabledContainerColor = Color(0xFFECFDF5)),
                    shape = RoundedCornerShape(16.dp)
                ) {
                    Icon(Icons.Filled.CheckCircle, contentDescription = null, tint = Color(0xFF059669), modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("ENROLLED", style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.Black, color = Color(0xFF059669)))
                }
            } else {
                Button(
                    onClick = { onRegister(course.id) },
                    modifier = Modifier.fillMaxWidth(),
                    colors = ButtonDefaults.buttonColors(containerColor = Purple600),
                    shape = RoundedCornerShape(16.dp)
                ) {
                    Text("Register Now", style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.Black))
                }
            }
        }
    }
}

@Composable
fun CourseStat(value: String, label: String) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(value, style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.Bold, color = Slate900))
        Text(label, style = MaterialTheme.typography.labelSmall.copy(color = Slate400, fontSize = 12.sp))
    }
}

@Composable
fun MyLearningView(
    registered: Map<String, LearningProgress>,
    courses: List<LawCourse>,
    onIncrement: (String) -> Unit,
    onBrowse: () -> Unit
) {
    if (registered.isEmpty()) {
        Column(
            modifier = Modifier.fillMaxWidth().padding(40.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Icon(Icons.Filled.MenuBook, contentDescription = null, modifier = Modifier.size(64.dp), tint = Purple100)
            Spacer(modifier = Modifier.height(16.dp))
            Text("Your Portfolio is Empty", style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold))
            Text("Register for specialized legal courses to build your portfolio.", textAlign = TextAlign.Center, style = MaterialTheme.typography.bodySmall.copy(color = Slate500))
            Spacer(modifier = Modifier.height(24.dp))
            Button(onClick = onBrowse, colors = ButtonDefaults.buttonColors(containerColor = Purple600)) {
                Text("Browse Catalog")
            }
        }
    } else {
        Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
            registered.forEach { (id, progress) ->
                val course = courses.find { it.id == id } ?: return@forEach
                LearningProgressCard(course, progress, onIncrement)
            }
        }
    }
}

@Composable
fun LearningProgressCard(course: LawCourse, progress: LearningProgress, onIncrement: (String) -> Unit) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        border = BorderStroke(1.dp, Slate200)
    ) {
        Column(modifier = Modifier.padding(20.dp)) {
            Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                Surface(
                    modifier = Modifier.size(48.dp),
                    color = Purple50,
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(Icons.Filled.AutoStories, contentDescription = null, tint = Purple600)
                    }
                }
                Column {
                    Text(course.category.uppercase(), style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Black, color = Purple500, letterSpacing = 1.sp))
                    Text(course.title, style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold, color = Slate900))
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            Row(horizontalArrangement = Arrangement.SpaceBetween, modifier = Modifier.fillMaxWidth()) {
                Text(if (progress.progress == 100) "Completed" else "In Progress", style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold, color = if (progress.progress == 100) Color(0xFF059669) else Slate600))
                Text("${progress.progress}%", style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold, color = Slate900))
            }

            Spacer(modifier = Modifier.height(8.dp))

            LinearProgressIndicator(
                progress = { progress.progress / 100f },
                modifier = Modifier.fillMaxWidth().height(8.dp).clip(RoundedCornerShape(4.dp)),
                color = if (progress.progress == 100) Color(0xFF10B981) else Purple600,
                trackColor = Slate100
            )

            Spacer(modifier = Modifier.height(20.dp))

            if (progress.progress < 100) {
                Button(
                    onClick = { onIncrement(course.id) },
                    modifier = Modifier.fillMaxWidth(),
                    colors = ButtonDefaults.buttonColors(containerColor = Purple600),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Icon(Icons.Filled.PlayArrow, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Resume Module")
                }
            } else {
                OutlinedButton(
                    onClick = { },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    border = BorderStroke(1.dp, Purple200)
                ) {
                    Icon(Icons.Filled.Download, contentDescription = null, tint = Purple600, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Download Certificate", color = Purple600)
                }
            }
        }
    }
}

@Composable
fun WorkshopsMootsView(workshops: List<Workshop>, moots: List<MootActivity>) {
    Column(verticalArrangement = Arrangement.spacedBy(24.dp)) {
        Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
            Text("Workshops", style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold))
            if (workshops.isEmpty()) {
                Box(modifier = Modifier.fillMaxWidth().padding(16.dp), contentAlignment = Alignment.Center) {
                    Text("No upcoming workshops.", style = MaterialTheme.typography.bodyMedium, color = Slate500)
                }
            } else {
                workshops.forEach { WorkshopCard(it) }
            }
        }
        Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
            Text("Moot Activities", style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold))
            if (moots.isEmpty()) {
                Box(modifier = Modifier.fillMaxWidth().padding(16.dp), contentAlignment = Alignment.Center) {
                    Text("No moot activities scheduled.", style = MaterialTheme.typography.bodyMedium, color = Slate500)
                }
            } else {
                moots.forEach { MootCard(it) }
            }
        }
    }
}

@Composable
fun WorkshopCard(workshop: Workshop) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        border = BorderStroke(1.dp, Slate200)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(workshop.type.uppercase(), style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Black, color = Purple600))
            Text(workshop.title, style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold))
            Text("By ${workshop.speaker}", style = MaterialTheme.typography.bodySmall.copy(color = Purple700, fontWeight = FontWeight.Bold))
            Spacer(modifier = Modifier.height(12.dp))
            Row(
                modifier = Modifier.fillMaxWidth().background(Slate50, RoundedCornerShape(8.dp)).padding(8.dp),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(workshop.date, style = MaterialTheme.typography.labelSmall.copy(color = Slate600))
                Text(workshop.time, style = MaterialTheme.typography.labelSmall.copy(color = Slate600))
            }
        }
    }
}

@Composable
fun MootCard(moot: MootActivity) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        border = BorderStroke(1.dp, Slate200)
    ) {
        Box(modifier = Modifier.padding(16.dp)) {
            Icon(Icons.Filled.Balance, contentDescription = null, modifier = Modifier.size(64.dp).align(Alignment.TopEnd).alpha(0.05f), tint = Purple600)
            Column {
                Surface(color = Purple50, shape = RoundedCornerShape(4.dp)) {
                    Text(moot.status.uppercase(), modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp), style = MaterialTheme.typography.labelSmall.copy(fontSize = 13.sp, fontWeight = FontWeight.Black, color = Purple700))
                }
                Spacer(modifier = Modifier.height(8.dp))
                Text(moot.name, style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold))
                Text("Role: ${moot.role}", style = MaterialTheme.typography.bodySmall.copy(color = Slate500))
                Spacer(modifier = Modifier.height(8.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Filled.CalendarToday, contentDescription = null, modifier = Modifier.size(12.dp), tint = Purple500)
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(moot.date, style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold, color = Slate600))
                }
            }
        }
    }
}

@Composable
fun ResourceCenterView(caseStudies: List<CaseStudy>) {
    Column(verticalArrangement = Arrangement.spacedBy(24.dp)) {
        Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
            Text("Case Repository", style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold))
            if (caseStudies.isEmpty()) {
                Box(modifier = Modifier.fillMaxWidth().padding(32.dp), contentAlignment = Alignment.Center) {
                    Text("No case studies found.", style = MaterialTheme.typography.bodyMedium, color = Slate500)
                }
            } else {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(24.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    border = BorderStroke(1.dp, Slate200)
                ) {
                    Column {
                        caseStudies.forEachIndexed { index, case ->
                            Row(
                                modifier = Modifier.fillMaxWidth().clickable {}.padding(16.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                                    Surface(color = Purple50, shape = RoundedCornerShape(12.dp), modifier = Modifier.size(40.dp)) {
                                        Box(contentAlignment = Alignment.Center) {
                                            Icon(Icons.Filled.Description, contentDescription = null, tint = Purple600, modifier = Modifier.size(20.dp))
                                        }
                                    }
                                    Column {
                                        Text(case.title, style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.Bold))
                                        Text("${case.type} • ${case.size}", style = MaterialTheme.typography.labelSmall.copy(color = Slate500))
                                    }
                                }
                                Icon(Icons.Filled.Download, contentDescription = null, tint = Slate400, modifier = Modifier.size(18.dp))
                            }
                            if (index < caseStudies.size - 1) {
                                HorizontalDivider(modifier = Modifier.padding(horizontal = 16.dp), color = Slate100)
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun FacultyPortalView(onPublish: (LawCourse) -> Unit) {
    var title by remember { mutableStateOf("") }
    var instructor by remember { mutableStateOf("") }

    Card(
        modifier = Modifier.fillMaxWidth().imePadding(),
        shape = RoundedCornerShape(28.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        border = BorderStroke(1.dp, Slate200)
    ) {
        Column(modifier = Modifier.padding(24.dp)) {
            Text("Create Course Module", style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Black))
            Text("Publish a new certification course to the portal.", style = MaterialTheme.typography.bodySmall.copy(color = Slate500))

            Spacer(modifier = Modifier.height(24.dp))

            Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                OutlinedTextField(
                    value = title,
                    onValueChange = { title = it },
                    label = { Text("Course Title") },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp)
                )
                OutlinedTextField(
                    value = instructor,
                    onValueChange = { instructor = it },
                    label = { Text("Instructor Name") },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp)
                )
                Button(
                    onClick = {
                        if (title.isNotBlank() && instructor.isNotBlank()) {
                            onPublish(LawCourse(
                                id = "c${System.currentTimeMillis()}",
                                title = title,
                                category = "Corporate",
                                duration = "6 Weeks",
                                credits = 3,
                                enrolled = 0,
                                instructor = instructor,
                                level = "Intermediate"
                            ))
                            title = ""
                            instructor = ""
                        }
                    },
                    modifier = Modifier.fillMaxWidth().height(56.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = Purple600),
                    shape = RoundedCornerShape(16.dp)
                ) {
                    Icon(Icons.Filled.Add, contentDescription = null)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Publish Course", style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.Black))
                }
            }
        }
    }
}
