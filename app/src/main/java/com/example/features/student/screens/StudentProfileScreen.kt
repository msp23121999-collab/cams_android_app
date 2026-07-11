package com.example.features.student.screens

import androidx.compose.animation.*
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.core.navigation.AppRoutes
import com.example.core.theme.*
import com.example.core.ui.CamsCard
import com.example.core.ui.CamsScreen
import com.example.features.student.models.Internship
import com.example.features.student.models.MootCourt
import com.example.features.student.models.StudentProfileResponse
import com.example.features.student.providers.StudentProfileViewModel
import com.example.features.student.widgets.ProfileHeaderCard
import com.example.features.student.widgets.StudentDrawer
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun StudentProfileScreen(
    viewModel: StudentProfileViewModel = viewModel(),
    onNavigate: (String) -> Unit
) {
    val uiState by viewModel.uiState.collectAsState()
    val drawerState = rememberDrawerState(initialValue = DrawerValue.Closed)
    val scope = rememberCoroutineScope()

    ModalNavigationDrawer(
        drawerState = drawerState,
        drawerContent = {
            StudentDrawer(
                currentRoute = AppRoutes.STUDENT_PROFILE,
                onNavigate = {
                    scope.launch { drawerState.close() }
                    onNavigate(it)
                }
            )
        }
    ) {
        CamsScreen(
            title = "Student Profile",
            subtitle = "Manage your academic and personal information",
            navigationIcon = {
                IconButton(onClick = { scope.launch { drawerState.open() } }) {
                    Icon(Icons.Filled.Menu, contentDescription = "Menu", tint = Color.White)
                }
            },
            actions = {
                IconButton(onClick = { /* PDF Download */ }) {
                    Icon(Icons.Filled.Download, contentDescription = "Download Profile", tint = Color.White)
                }
                IconButton(onClick = { onNavigate("LOGOUT") }) {
                    Icon(Icons.Filled.Logout, contentDescription = "Logout", tint = Color.White)
                }
            },
            scrollable = false
        ) {
            if (uiState.isLoading) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator(color = CamsNavy)
                }
            } else if (uiState.error != null) {
                com.example.core.ui.NetworkErrorView(
                    message = uiState.error!!,
                    onRetry = { viewModel.fetchProfileData() },
                    modifier = Modifier.fillMaxSize()
                )
            } else {
                val profile = uiState.profile
                LazyColumn(
                    modifier = Modifier
                        .fillMaxSize()
                        .offset(y = (-40).dp),
                    verticalArrangement = Arrangement.spacedBy(20.dp)
                ) {
                    item {
                        ProfileHeaderCard(
                            fullName = profile?.fullName ?: "Unknown",
                            courseName = profile?.courseName ?: "Degree not fetched",
                            profilePhotoUrl = profile?.profilePhotoUrl,
                            rollNo = profile?.rollNo,
                            semester = profile?.semester,
                            department = profile?.departmentName,
                            section = profile?.section,
                            mentorName = profile?.mentorName,
                            mentorEmail = profile?.mentorEmail,
                            advisorName = profile?.classAdvisorName,
                            advisorEmail = profile?.classAdvisorEmail
                        )
                    }

                    item {
                        VerificationBanner(
                            status = profile?.verificationStatus ?: "DRAFT",
                            onVerifyClick = { viewModel.submitForVerification() }
                        )
                    }

                    item {
                        KPISection(profile, uiState.attendance?.percentage)
                    }

                    item {
                        TabNavigation(
                            activeTab = uiState.activeTab,
                            onTabSelected = { viewModel.setActiveTab(it) }
                        )
                    }

                    item {
                        AnimatedContent(
                            targetState = uiState.activeTab,
                            transitionSpec = {
                                fadeIn() togetherWith fadeOut()
                            },
                            label = "TabContent"
                        ) { tab ->
                            when (tab) {
                                "personal" -> PersonalTab(profile, viewModel)
                                "academic" -> AcademicTab(profile)
                                "experience" -> LegalExperienceTab(profile)
                                "skills" -> SkillsTab(profile)
                                "aitools" -> AiHubTab()
                                "documents" -> DocumentsTab(profile)
                                "advisor" -> AdvisorRemarksTab(uiState.mentorshipRecord)
                                else -> PlaceholderTab(tab)
                            }
                        }
                    }
                    
                    item {
                        Spacer(modifier = Modifier.height(60.dp))
                    }
                }
            }
        }
    }
}

@Composable
fun VerificationBanner(status: String, onVerifyClick: () -> Unit) {
    val (color, title, desc, icon) = when (status) {
        "DRAFT" -> Quadruple(Color(0xFF8B5CF6), "Draft Profile", "Please fill in all details and submit for verification.", Icons.Filled.Info)
        "SUBMITTED" -> Quadruple(Color(0xFFF59E0B), "Verification In Progress", "Your profile is under review by the staff.", Icons.Filled.Shield)
        "VERIFIED_LOCKED" -> Quadruple(Color(0xFF10B981), "Profile Verified", "Your profile has been fully verified and locked.", Icons.Filled.CheckCircle)
        else -> Quadruple(Color(0xFFF43F5E), "Correction Requested", "Please review and fix the details.", Icons.Filled.Error)
    }

    CamsCard(
        modifier = Modifier.fillMaxWidth(),
    ) {
        Row(
            modifier = Modifier.padding(4.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Icon(icon, contentDescription = null, tint = color, modifier = Modifier.size(32.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(title, fontWeight = FontWeight.Bold, fontSize = 16.sp, color = color)
                Text(desc, fontSize = 12.sp, color = CamsTextSecondary)
                if (status == "DRAFT") {
                    Spacer(modifier = Modifier.height(8.dp))
                    Button(
                        onClick = onVerifyClick,
                        colors = ButtonDefaults.buttonColors(containerColor = color),
                        shape = RoundedCornerShape(12.dp),
                        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 0.dp),
                        modifier = Modifier.height(36.dp)
                    ) {
                        Text("Submit for Verification", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}

data class Quadruple<A, B, C, D>(val first: A, val second: B, val third: C, val fourth: D)

@Composable
fun KPISection(profile: StudentProfileResponse?, attendance: Int?) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        KPICard("Attendance", "${attendance ?: "N/A"}%", Icons.Filled.CheckCircle, Modifier.weight(1f))
        KPICard("CGPA", "${profile?.cgpa ?: "N/A"}", Icons.Filled.EmojiEvents, Modifier.weight(1f))
        KPICard("Moots", "${profile?.mootCourts?.size ?: 0}", Icons.Filled.Gavel, Modifier.weight(1f))
        KPICard("Interns", "${profile?.internships?.size ?: 0}", Icons.Filled.Work, Modifier.weight(1f))
    }
}

@Composable
fun KPICard(label: String, value: String, icon: ImageVector, modifier: Modifier = Modifier) {
    CamsCard(
        modifier = modifier,
    ) {
        Column(
            modifier = Modifier.padding(4.dp).fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Box(
                modifier = Modifier
                    .size(32.dp)
                    .background(CamsNavy.copy(alpha = 0.1f), CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Icon(icon, contentDescription = null, tint = CamsNavy, modifier = Modifier.size(16.dp))
            }
            Spacer(modifier = Modifier.height(8.dp))
            Text(label.uppercase(), fontSize = 12.sp, fontWeight = FontWeight.Bold, color = CamsTextSecondary)
            Text(value, fontSize = 16.sp, fontWeight = FontWeight.ExtraBold, color = CamsTextPrimary)
        }
    }
}

@Composable
fun TabNavigation(activeTab: String, onTabSelected: (String) -> Unit) {
    val tabs = listOf(
        TabItem("personal", Icons.Filled.Person, "Personal"),
        TabItem("academic", Icons.Filled.School, "Academic"),
        TabItem("experience", Icons.Filled.Gavel, "Legal"),
        TabItem("skills", Icons.Filled.Psychology, "Skills"),
        TabItem("aitools", Icons.Filled.AutoAwesome, "AI Hub"),
        TabItem("documents", Icons.Filled.UploadFile, "Docs"),
        TabItem("advisor", Icons.Filled.Message, "Remarks")
    )

    ScrollableTabRow(
        selectedTabIndex = tabs.indexOfFirst { it.id == activeTab },
        containerColor = Color.Transparent,
        contentColor = CamsNavy,
        edgePadding = 16.dp,
        divider = {},
        indicator = { tabPositions ->
            TabRowDefaults.SecondaryIndicator(
                modifier = Modifier.tabIndicatorOffset(tabPositions[tabs.indexOfFirst { it.id == activeTab }]),
                color = CamsNavy
            )
        }
    ) {
        tabs.forEach { tab ->
            Tab(
                selected = activeTab == tab.id,
                onClick = { onTabSelected(tab.id) },
                text = {
                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                        Icon(tab.icon, contentDescription = null, modifier = Modifier.size(18.dp))
                        Text(tab.label, fontSize = 14.sp, fontWeight = FontWeight.Medium)
                    }
                }
            )
        }
    }
}

data class TabItem(val id: String, val icon: ImageVector, val label: String)

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun PersonalTab(profile: StudentProfileResponse?, viewModel: StudentProfileViewModel) {
    var showEditDialog by remember { mutableStateOf(false) }

    Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
        SectionHeader(Icons.Filled.Person, "Basic Details")
        InfoGrid(
            items = listOf(
                "Full Name" to (profile?.fullName ?: ""),
                "Student ID" to (profile?.rollNo ?: ""),
                "Date of Birth" to (profile?.dateOfBirth ?: ""),
                "Gender" to (profile?.gender ?: ""),
                "Blood Group" to (profile?.bloodGroup ?: ""),
                "Nationality" to (profile?.nationality ?: "")
            )
        )

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            SectionHeader(Icons.Filled.ContactPhone, "Contact Information")
            IconButton(
                onClick = { showEditDialog = true },
                modifier = Modifier.size(36.dp)
            ) {
                Icon(
                    imageVector = Icons.Filled.Edit,
                    contentDescription = "Edit Contact Info",
                    tint = CamsNavy,
                    modifier = Modifier.size(20.dp)
                )
            }
        }
        InfoGrid(
            items = listOf(
                "Mobile" to (profile?.mobileNumber ?: ""),
                "Email" to (profile?.email ?: ""),
                "Current Address" to (profile?.currentAddress ?: ""),
                "Permanent Address" to (profile?.permanentAddress ?: "")
            )
        )

        SectionHeader(Icons.Filled.FamilyRestroom, "Family Details")
        InfoGrid(
            items = listOf(
                "Father's Name" to (profile?.fatherName ?: ""),
                "Father's Occupation" to (profile?.fatherOccupation ?: ""),
                "Mother's Name" to (profile?.motherName ?: ""),
                "Mother's Occupation" to (profile?.motherOccupation ?: "")
            )
        )
    }

    if (showEditDialog && profile != null) {
        var tempMobile by remember { mutableStateOf(profile.mobileNumber ?: "") }
        var tempEmail by remember { mutableStateOf(profile.email ?: "") }
        var tempCurrentAddress by remember { mutableStateOf(profile.currentAddress ?: "") }
        var tempPermanentAddress by remember { mutableStateOf(profile.permanentAddress ?: "") }
        var tempBloodGroup by remember { mutableStateOf(profile.bloodGroup ?: "") }

        AlertDialog(
            onDismissRequest = { showEditDialog = false },
            title = {
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Icon(Icons.Filled.Edit, contentDescription = null, tint = CamsNavy)
                    Text("Edit Contact Details", fontWeight = FontWeight.Black, fontSize = 18.sp, color = CamsTextPrimary)
                }
            },
            text = {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .verticalScroll(rememberScrollState()),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    Text(
                        text = "Update your contact information below. These changes will update your academic records once saved.",
                        fontSize = 12.sp,
                        color = CamsTextSecondary,
                        lineHeight = 16.sp
                    )

                    OutlinedTextField(
                        value = tempMobile,
                        onValueChange = { tempMobile = it },
                        label = { Text("Mobile Number") },
                        placeholder = { Text("e.g. 9876543210") },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = CamsNavy,
                            focusedLabelColor = CamsNavy,
                            cursorColor = CamsNavy
                        ),
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(
                            keyboardType = KeyboardType.Phone,
                            imeAction = ImeAction.Next
                        )
                    )

                    OutlinedTextField(
                        value = tempEmail,
                        onValueChange = { tempEmail = it },
                        label = { Text("Email Address") },
                        placeholder = { Text("e.g. student@cams.local") },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = CamsNavy,
                            focusedLabelColor = CamsNavy,
                            cursorColor = CamsNavy
                        ),
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(
                            keyboardType = KeyboardType.Email,
                            imeAction = ImeAction.Next
                        )
                    )

                    OutlinedTextField(
                        value = tempBloodGroup,
                        onValueChange = { tempBloodGroup = it },
                        label = { Text("Blood Group") },
                        placeholder = { Text("e.g. O+") },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = CamsNavy,
                            focusedLabelColor = CamsNavy,
                            cursorColor = CamsNavy
                        ),
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(
                            keyboardType = KeyboardType.Text,
                            imeAction = ImeAction.Next
                        )
                    )

                    OutlinedTextField(
                        value = tempCurrentAddress,
                        onValueChange = { tempCurrentAddress = it },
                        label = { Text("Current Address") },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = CamsNavy,
                            focusedLabelColor = CamsNavy,
                            cursorColor = CamsNavy
                        ),
                        minLines = 2,
                        keyboardOptions = KeyboardOptions(
                            keyboardType = KeyboardType.Text,
                            imeAction = ImeAction.Next
                        )
                    )

                    OutlinedTextField(
                        value = tempPermanentAddress,
                        onValueChange = { tempPermanentAddress = it },
                        label = { Text("Permanent Address") },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = CamsNavy,
                            focusedLabelColor = CamsNavy,
                            cursorColor = CamsNavy
                        ),
                        minLines = 2,
                        keyboardOptions = KeyboardOptions(
                            keyboardType = KeyboardType.Text,
                            imeAction = ImeAction.Done
                        )
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        val updated = profile.copy(
                            mobileNumber = tempMobile,
                            email = tempEmail,
                            currentAddress = tempCurrentAddress,
                            permanentAddress = tempPermanentAddress,
                            bloodGroup = tempBloodGroup
                        )
                        viewModel.updateProfile(updated)
                        showEditDialog = false
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = CamsNavy),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text("Save", fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(
                    onClick = { showEditDialog = false }
                ) {
                    Text("Cancel", color = CamsNavy, fontWeight = FontWeight.Bold)
                }
            },
            shape = RoundedCornerShape(24.dp),
            containerColor = MaterialTheme.colorScheme.surface
        )
    }
}

@Composable
fun AcademicTab(profile: StudentProfileResponse?) {
    Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
        SectionHeader(Icons.Filled.School, "Semester Results")
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            listOf("Sem I" to "8.2", "Sem II" to "8.5", "Sem III" to "8.1", "Sem IV" to "8.8", "Sem V" to "8.6").forEach { (sem, sgpa) ->
                ResultCard(sem, sgpa, Modifier.weight(1f))
            }
        }
        
        CamsCard(
            modifier = Modifier.fillMaxWidth().height(240.dp),
        ) {
            Column(modifier = Modifier.padding(8.dp)) {
                Text("Performance Analytics", fontWeight = FontWeight.Bold, color = CamsTextPrimary)
                Spacer(modifier = Modifier.height(16.dp))
                // Simulated Chart
                Box(modifier = Modifier.fillMaxSize()) {
                    Canvas(modifier = Modifier.fillMaxSize()) {
                        val points = listOf(8.2f, 8.5f, 8.1f, 8.8f, 8.6f)
                        val maxVal = 10f
                        val width = size.width
                        val height = size.height
                        val stepX = width / (points.size - 1)
                        
                        // Draw Area
                        val path = androidx.compose.ui.graphics.Path()
                        path.moveTo(0f, height - (points[0] / maxVal * height))
                        points.forEachIndexed { index, point ->
                            if (index > 0) {
                                path.lineTo(index * stepX, height - (point / maxVal * height))
                            }
                        }
                        path.lineTo(width, height)
                        path.lineTo(0f, height)
                        path.close()
                        drawPath(path, Brush.verticalGradient(listOf(CamsNavy.copy(alpha = 0.2f), Color.Transparent)))
                        
                        // Draw Line
                        points.forEachIndexed { index, point ->
                            if (index > 0) {
                                drawLine(
                                    color = CamsNavy,
                                    start = androidx.compose.ui.geometry.Offset((index-1) * stepX, height - (points[index-1] / maxVal * height)),
                                    end = androidx.compose.ui.geometry.Offset(index * stepX, height - (point / maxVal * height)),
                                    strokeWidth = 3.dp.toPx()
                                )
                            }
                            drawCircle(CamsNavy, 4.dp.toPx(), androidx.compose.ui.geometry.Offset(index * stepX, height - (point / maxVal * height)))
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun LegalExperienceTab(profile: StudentProfileResponse?) {
    Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
        SectionHeader(Icons.Filled.Gavel, "Moot Court History")
        profile?.mootCourts?.forEach { moot ->
            MootCard(moot)
        }
        if (profile?.mootCourts.isNullOrEmpty()) {
            Text(
                text = "No moot court records found.",
                color = LexNovaSlateAccent,
                fontSize = 12.sp,
                fontStyle = androidx.compose.ui.text.font.FontStyle.Italic
            )
        }

        SectionHeader(Icons.Filled.Work, "Internships")
        profile?.internships?.forEach { internship ->
            InternshipCard(internship)
        }
    }
}

@Composable
fun SkillsTab(profile: StudentProfileResponse?) {
    Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
        SectionHeader(Icons.Filled.Psychology, "Bar Council Exam Prep")
        CamsCard(
            modifier = Modifier.fillMaxWidth(),
        ) {
            Column(modifier = Modifier.padding(8.dp)) {
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                    Text("Overall AIBE Readiness", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = CamsTextSecondary)
                    Text("${profile?.aibeReadiness ?: 0}%", fontSize = 12.sp, fontWeight = FontWeight.ExtraBold, color = CamsNavy)
                }
                Spacer(modifier = Modifier.height(8.dp))
                LinearProgressIndicator(
                    progress = { ((profile?.aibeReadiness ?: 0) / 100f).toFloat() },
                    modifier = Modifier.fillMaxWidth().height(8.dp).clip(CircleShape),
                    color = CamsNavy,
                    trackColor = CamsNavy.copy(alpha = 0.1f)
                )
            }
        }

        SectionHeader(Icons.Filled.BarChart, "Skills Assessment")
        CamsCard(
            modifier = Modifier.fillMaxWidth().height(240.dp),
        ) {
            Box(modifier = Modifier.padding(8.dp).fillMaxSize(), contentAlignment = Alignment.Center) {
                // Simplified Radar-like visualization or Bars
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    profile?.skillAssessment?.forEach { skill ->
                        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            Text(skill.skill, modifier = Modifier.width(80.dp), fontSize = 13.sp, fontWeight = FontWeight.Bold, color = CamsTextPrimary)
                            LinearProgressIndicator(
                                progress = skill.level / 100f,
                                modifier = Modifier.weight(1f).height(4.dp).clip(CircleShape),
                                color = CamsNavy,
                                trackColor = CamsNavy.copy(alpha = 0.1f)
                            )
                            Text("${skill.level}%", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = CamsTextSecondary)
                        }
                    }
                }
            }
        }

        SectionHeader(Icons.Filled.Description, "Research Publications")
        profile?.publications?.forEach { pub ->
            CamsCard(
                modifier = Modifier.fillMaxWidth(),
            ) {
                Column(modifier = Modifier.padding(8.dp)) {
                    Row(horizontalArrangement = Arrangement.SpaceBetween, modifier = Modifier.fillMaxWidth()) {
                        Text(pub.title, fontWeight = FontWeight.Bold, fontSize = 14.sp, modifier = Modifier.weight(1f), color = CamsTextPrimary)
                        Surface(color = CamsNavy.copy(alpha = 0.1f), shape = RoundedCornerShape(8.dp)) {
                            Text("Published", color = CamsNavy, fontSize = 12.sp, fontWeight = FontWeight.Bold, modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp))
                        }
                    }
                    Spacer(modifier = Modifier.height(4.dp))
                    Text("Published in: ${pub.journal} • ${pub.year}", fontSize = 13.sp, color = CamsTextSecondary)
                    if (pub.coAuthors != null) {
                        Spacer(modifier = Modifier.height(8.dp))
                        SuggestionChip(onClick = {}, label = { Text("Co-authored with ${pub.coAuthors}", fontSize = 12.sp) })
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun AiHubTab() {
    Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(24.dp),
            colors = CardDefaults.cardColors(containerColor = CamsNavy),
        ) {
            Box(modifier = Modifier.padding(24.dp)) {
                Column {
                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        Icon(Icons.Filled.AutoAwesome, contentDescription = null, tint = Color.White)
                        Text("CAMS AI Intelligence", color = Color.White, fontWeight = FontWeight.ExtraBold, fontSize = 20.sp)
                    }
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        "Your personal AI-powered legal toolkit. Summarize judgments, find precedents, and get tailored career paths.",
                        color = Color.White.copy(alpha = 0.8f),
                        fontSize = 13.sp
                    )
                }
            }
        }

        SectionHeader(Icons.Filled.Lightbulb, "Professional Quick Prompts")
        val prompts = listOf(
            "Draft a non-disclosure agreement...",
            "Find precedents for breach of lease...",
            "Compare Section 420 IPC with BNS...",
            "Summarize ratio of Kesavananda Bharati..."
        )
        FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            prompts.forEach { prompt ->
                CamsCard(
                    modifier = Modifier.clickable { },
                ) {
                    Text(prompt, modifier = Modifier.padding(8.dp), fontSize = 12.sp, color = CamsTextPrimary, fontWeight = FontWeight.Medium)
                }
            }
        }

        SectionHeader(Icons.Filled.TrackChanges, "AI Career Guidance")
        CamsCard(
            modifier = Modifier.fillMaxWidth(),
        ) {
            Row(modifier = Modifier.padding(8.dp), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                Box(modifier = Modifier.size(48.dp).background(CamsNavy.copy(alpha = 0.1f), CircleShape), contentAlignment = Alignment.Center) {
                    Text("92%", fontWeight = FontWeight.Bold, color = CamsNavy)
                }
                Column {
                    Text("Corporate Law Match", fontWeight = FontWeight.Bold, fontSize = 14.sp, color = CamsTextPrimary)
                    Text("Based on your GPA and Trilegal internship.", fontSize = 13.sp, color = CamsTextSecondary)
                }
                Spacer(modifier = Modifier.weight(1f))
                IconButton(onClick = {}) { Icon(Icons.Filled.ArrowForward, contentDescription = null, tint = CamsNavy) }
            }
        }
    }
}

@Composable
fun DocumentsTab(profile: StudentProfileResponse?) {
    val docs = listOf(
        "Aadhaar Card" to profile?.documentAadhaarUrl,
        "Community Cert" to profile?.documentCommunityUrl,
        "TC Certificate" to profile?.documentTcUrl,
        "Other" to profile?.documentOtherUrl
    )

    Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
        SectionHeader(Icons.Filled.Folder, "Document Repository")
        docs.chunked(2).forEach { rowDocs ->
            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                rowDocs.forEach { (label, url) ->
                    DocumentCard(label, url, Modifier.weight(1f))
                }
                if (rowDocs.size == 1) Spacer(modifier = Modifier.weight(1f))
            }
        }
    }
}

@Composable
fun DocumentCard(label: String, url: String?, modifier: Modifier = Modifier) {
    CamsCard(
        modifier = modifier,
    ) {
        Column(modifier = Modifier.padding(8.dp)) {
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Icon(Icons.Filled.Description, contentDescription = null, tint = if (url != null) CamsNavy else CamsTextSecondary)
                Surface(
                    color = (if (url != null) Color(0xFF10B981) else Color(0xFFF59E0B)).copy(alpha = 0.1f),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Text(
                        if (url != null) "Uploaded" else "Missing",
                        color = if (url != null) Color(0xFF10B981) else Color(0xFFF59E0B),
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                    )
                }
            }
            Spacer(modifier = Modifier.height(12.dp))
            Text(label, fontWeight = FontWeight.Bold, fontSize = 13.sp, color = CamsTextPrimary)
            Text("PDF or JPG", fontSize = 12.sp, color = CamsTextSecondary)
            Spacer(modifier = Modifier.height(16.dp))
            TextButton(
                onClick = {},
                contentPadding = PaddingValues(0.dp),
                modifier = Modifier.height(24.dp)
            ) {
                Text(if (url != null) "Download" else "Upload", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = CamsNavy)
            }
        }
    }
}

@Composable
fun AdvisorRemarksTab(record: com.example.features.student.models.MentorshipRecord?) {
    Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
        SectionHeader(Icons.Filled.Message, "Mentorship Notes")
        record?.let {
            RemarkCard("Academic Review", it.academicReview ?: "No review recorded.")
            RemarkCard("Improvement Plan", it.improvementPlan ?: "No plan recorded.")
            RemarkCard("General Remarks", it.remarks ?: "No remarks.")
        } ?: Box(modifier = Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
            CircularProgressIndicator(modifier = Modifier.size(24.dp))
        }
    }
}

@Composable
fun MootCard(moot: MootCourt) {
    CamsCard(
        modifier = Modifier.fillMaxWidth(),
    ) {
        Row(modifier = Modifier.padding(8.dp), verticalAlignment = Alignment.CenterVertically) {
            Column(modifier = Modifier.weight(1f)) {
                Text(moot.name, fontWeight = FontWeight.Bold, fontSize = 14.sp, color = CamsTextPrimary)
                Text("Role: ${moot.role} • ${moot.date}", fontSize = 13.sp, color = CamsTextSecondary)
            }
            Surface(
                color = CamsNavy.copy(alpha = 0.1f),
                shape = RoundedCornerShape(8.dp)
            ) {
                Text(moot.rank, color = CamsNavy, fontWeight = FontWeight.Bold, fontSize = 12.sp, modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp))
            }
        }
    }
}

@Composable
fun InternshipCard(internship: Internship) {
    CamsCard(
        modifier = Modifier.fillMaxWidth(),
    ) {
        Column(modifier = Modifier.padding(8.dp)) {
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Text(internship.organization ?: internship.company ?: "N/A", fontWeight = FontWeight.Bold, fontSize = 14.sp, color = CamsTextPrimary)
                Text(internship.status ?: "", color = CamsNavy, fontWeight = FontWeight.Bold, fontSize = 12.sp)
            }
            Text(internship.role ?: "Intern", fontSize = 12.sp, color = CamsTextSecondary)
            Spacer(modifier = Modifier.height(8.dp))
            Text(internship.responsibilities ?: "", fontSize = 13.sp, color = CamsTextSecondary)
        }
    }
}

@Composable
fun RemarkCard(title: String, content: String) {
    CamsCard(
        modifier = Modifier.fillMaxWidth(),
    ) {
        Column(modifier = Modifier.padding(8.dp)) {
            Text(title, fontWeight = FontWeight.Bold, fontSize = 12.sp, color = CamsNavy)
            Spacer(modifier = Modifier.height(4.dp))
            Text(content, fontSize = 13.sp, color = CamsTextSecondary, lineHeight = 18.sp)
        }
    }
}

@Composable
fun ResultCard(sem: String, sgpa: String, modifier: Modifier) {
    Card(
        modifier = modifier,
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = CamsNavy.copy(alpha = 0.05f)),
        border = BorderStroke(1.dp, CamsNavy.copy(alpha = 0.1f))
    ) {
        Column(modifier = Modifier.padding(8.dp), horizontalAlignment = Alignment.CenterHorizontally) {
            Text(sem, fontSize = 12.sp, fontWeight = FontWeight.Bold, color = CamsNavy)
            Text(sgpa, fontSize = 18.sp, fontWeight = FontWeight.ExtraBold, color = CamsNavy)
        }
    }
}

@Composable
fun SectionHeader(icon: ImageVector, title: String) {
    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        Icon(icon, contentDescription = null, tint = CamsNavy, modifier = Modifier.size(20.dp))
        Text(title, fontWeight = FontWeight.ExtraBold, fontSize = 18.sp, color = CamsTextPrimary)
    }
}

@Composable
fun InfoGrid(items: List<Pair<String, String>>) {
    CamsCard(
        modifier = Modifier.fillMaxWidth(),
    ) {
        Column(modifier = Modifier.padding(8.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
            items.forEach { (label, value) ->
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                    Text(label, fontSize = 12.sp, color = CamsTextSecondary, fontWeight = FontWeight.Medium)
                    Text(value, fontSize = 12.sp, color = CamsTextPrimary, fontWeight = FontWeight.Bold, textAlign = TextAlign.End, modifier = Modifier.weight(1f).padding(start = 16.dp))
                }
                if (label != items.last().first) {
                    HorizontalDivider(color = Color.LightGray.copy(alpha = 0.2f))
                }
            }
        }
    }
}

@Composable
fun PlaceholderTab(tab: String) {
    Box(modifier = Modifier.fillMaxWidth().height(200.dp), contentAlignment = Alignment.Center) {
        Text("Content for $tab coming soon...", color = LexNovaSlateAccent)
    }
}
