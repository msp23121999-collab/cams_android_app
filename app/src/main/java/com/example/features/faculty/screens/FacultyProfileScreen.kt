package com.example.features.faculty.screens

import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.*
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import coil.compose.AsyncImage
import com.example.core.theme.*
import com.example.core.ui.CamsCard
import com.example.core.ui.CamsScreen
import com.example.features.faculty.models.*
import com.example.features.faculty.providers.FacultyProfileViewModel

@Composable
fun FacultyProfileScreen(
    onNavigate: (String) -> Unit,
    viewModel: FacultyProfileViewModel
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val profile = uiState.profile
    val research = uiState.researchList
    val activity = uiState.activitySummary
    val subjects = uiState.subjects
    var showEditDialog by remember { mutableStateOf(false) }

    if (uiState.saveSuccess) {
        LaunchedEffect(Unit) {
            showEditDialog = false
        }
    }

    if (showEditDialog) {
        EditProfileDialog(
            profile = profile,
            isSaving = uiState.isSaving,
            saveError = uiState.saveError,
            onDismiss = {
                showEditDialog = false
                viewModel.clearSaveStatus()
            },
            onSave = { update -> viewModel.updateProfile(update) }
        )
    }

    com.example.features.faculty.widgets.FacultyBaseScreen(
        title = "Faculty Profile",
        subtitle = "Detailed academic & professional record",
        currentRoute = "/faculty/profile",
        onNavigate = onNavigate,
        floatingActionButton = {
            FloatingActionButton(onClick = { showEditDialog = true }, containerColor = CamsNavy) {
                Icon(Icons.Filled.Edit, contentDescription = "Edit Profile", tint = Color.White)
            }
        }
    ) {
        if (uiState.isLoading) {
            Box(modifier = Modifier.fillMaxWidth().height(300.dp), contentAlignment = Alignment.Center) {
                CircularProgressIndicator(color = CamsNavy)
            }
        } else if (uiState.error != null && profile.fullName.isBlank()) {
            com.example.core.ui.NetworkErrorView(
                message = uiState.error ?: "Failed to load faculty profile",
                onRetry = { viewModel.loadProfileData() }
            )
        } else {
            // 1. Profile Header
            ProfileHeader(profile)

            // 2. Personal Details
            PersonalDetailsSection(profile)

            // 3. Activity Summary
            ActivitySummarySection(activity)

            // 4. Research & Publications
            ResearchSection(research)

            // 5. Subject Expertise
            SubjectExpertiseSection(subjects)

            // 6. Professional Background (Qualifications & Experience)
            ProfessionalSection(profile)

            // 7. Experience Details
            ExperienceDetailsSection(profile)
        }
    }
}

@Composable
private fun SubjectExpertiseSection(subjects: List<FacultySubject>) {
    SectionTitle("Subject Expertise", Icons.AutoMirrored.Filled.MenuBook)
    CamsCard {
        if (subjects.isEmpty()) {
            Text("No subjects currently allocated.", fontSize = 13.sp, color = Color(0xFF64748B))
        } else {
        Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
            subjects.forEach { subject ->
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(subject.subjectName, fontWeight = FontWeight.Bold, fontSize = 14.sp, color = MaterialTheme.colorScheme.onSurface)
                        Text("${subject.degreeCode} | ${subject.batch}", fontSize = 13.sp, color = Color(0xFF64748B))
                    }
                    Surface(color = Color(0xFFEEF2FF), shape = RoundedCornerShape(4.dp)) {
                        Text(
                            subject.subjectCode,
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Black,
                            color = Color(0xFF4338CA)
                        )
                    }
                }
                if (subjects.indexOf(subject) < subjects.size - 1) {
                    HorizontalDivider(color = Color(0xFFF3F4F6))
                }
            }
        }
        }
    }
}

@Composable
private fun ProfileHeader(profile: FacultyProfile) {
    CamsCard {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(20.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            // Avatar
            Box(
                modifier = Modifier
                    .size(80.dp)
                    .clip(CircleShape)
                    .background(CamsNavy.copy(alpha = 0.1f))
                    .border(2.dp, CamsNavy.copy(alpha = 0.1f), CircleShape),
                contentAlignment = Alignment.Center
            ) {
                if (profile.profilePhotoUrl != null) {
                    AsyncImage(
                        model = profile.profilePhotoUrl,
                        contentDescription = "Profile Photo",
                        modifier = Modifier.fillMaxSize(),
                        contentScale = ContentScale.Crop
                    )
                } else {
                    Icon(Icons.Filled.Person, null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(40.dp))
                }
            }

            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Text(profile.fullName, fontWeight = FontWeight.Bold, fontSize = 20.sp, color = MaterialTheme.colorScheme.onSurface)
                Text(profile.designation, fontSize = 12.sp, color = CamsNavy, fontWeight = FontWeight.Bold)
                Surface(color = Color(0xFFF3F4F6), shape = RoundedCornerShape(8.dp)) {
                    Text(
                        "EMP CODE: ${profile.employeeCode}",
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp),
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF64748B)
                    )
                }
            }
        }
    }
}

@Composable
private fun PersonalDetailsSection(profile: FacultyProfile) {
    SectionTitle("Personal Details", Icons.Filled.Badge)
    CamsCard {
        Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
            DetailRow("Department", profile.departmentName)
            DetailRow("Specialization", profile.specialization)
            DetailRow("Email", profile.email)
            DetailRow("Phone", profile.phone)
        }
    }
}

@Composable
private fun ActivitySummarySection(activity: ActivitySummary) {
    SectionTitle("Lifetime Activity Summary", Icons.Filled.Analytics)
    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            SummaryItem("Classes", activity.classesConducted.toString(), Color(0xFF4F46E5), Modifier.weight(1f))
            SummaryItem("Attendance", activity.attendanceMarked.toString(), Color(0xFF059669), Modifier.weight(1f))
        }
        Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            SummaryItem("Materials", activity.studyMaterialsUploaded.toString(), Color(0xFF7C3AED), Modifier.weight(1f))
            SummaryItem("Assignments", activity.assignmentsCreated.toString(), Color(0xFFD97706), Modifier.weight(1f))
        }
    }
}

@Composable
private fun ResearchSection(researchList: List<ResearchEntry>) {
    SectionTitle("Research & Publications", Icons.AutoMirrored.Filled.LibraryBooks)
    if (researchList.isEmpty()) {
        CamsCard { Text("No research or publications recorded yet.", fontSize = 13.sp, color = Color(0xFF64748B)) }
    }
    researchList.forEach { research ->
        CamsCard(modifier = Modifier.padding(bottom = 8.dp)) {
            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Surface(color = Color(0xFFEEF2FF), shape = RoundedCornerShape(4.dp)) {
                        Text(
                            research.researchType,
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Black,
                            color = Color(0xFF4338CA)
                        )
                    }
                    research.publicationDate?.let {
                        Text(it, fontSize = 12.sp, color = Color(0xFF64748B))
                    }
                }
                Text(research.title, fontWeight = FontWeight.Bold, fontSize = 14.sp, color = MaterialTheme.colorScheme.onSurface)
                research.publication?.let {
                    Text(it, fontSize = 13.sp, color = Color(0xFF64748B))
                }
            }
        }
    }
}

@Composable
private fun ProfessionalSection(profile: FacultyProfile) {
    SectionTitle("Educational Qualifications", Icons.Filled.School)
    CamsCard {
        if (profile.educationalQualifications.isEmpty()) {
            Text("No educational qualifications on file.", fontSize = 13.sp, color = Color(0xFF64748B))
        }
        profile.educationalQualifications.forEachIndexed { index, qual ->
            Column {
                Text(qual.degree, fontWeight = FontWeight.Bold, fontSize = 14.sp, color = MaterialTheme.colorScheme.onSurface)
                Text("${qual.specialization} | ${qual.university}", fontSize = 13.sp, color = Color(0xFF64748B))
                if (index < profile.educationalQualifications.size - 1) {
                    HorizontalDivider(Modifier.padding(vertical = 8.dp), color = Color(0xFFF3F4F6))
                }
            }
        }
    }
}

@Composable
private fun SectionTitle(title: String, icon: ImageVector) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        modifier = Modifier.padding(top = 10.dp)
    ) {
        Icon(icon, null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(18.dp))
        Text(title, fontWeight = FontWeight.Bold, fontSize = 16.sp, color = MaterialTheme.colorScheme.onSurface)
    }
}

@Composable
private fun DetailRow(label: String, value: String) {
    Column {
        Text(label.uppercase(), fontSize = 13.sp, fontWeight = FontWeight.Bold, color = Color(0xFF64748B))
        Text(value, fontSize = 13.sp, color = MaterialTheme.colorScheme.onSurface)
    }
}

@Composable
private fun ExperienceDetailsSection(profile: FacultyProfile) {
    SectionTitle("Work Experience", Icons.Filled.WorkHistory)
    CamsCard {
        if (profile.experienceDetails.isEmpty()) {
            Text("No prior work experience on file.", fontSize = 13.sp, color = Color(0xFF64748B))
        }
        profile.experienceDetails.forEachIndexed { index, exp ->
            Column {
                Text(exp.designation, fontWeight = FontWeight.Bold, fontSize = 14.sp, color = MaterialTheme.colorScheme.onSurface)
                Text(exp.institutionName, fontSize = 12.sp, color = CamsNavy, fontWeight = FontWeight.Bold)
                Text("${exp.fromDate} - ${exp.toDate} (${exp.totalYears} Years)", fontSize = 13.sp, color = Color(0xFF64748B))
                
                if (index < profile.experienceDetails.size - 1) {
                    HorizontalDivider(Modifier.padding(vertical = 8.dp), color = Color(0xFFF3F4F6))
                }
            }
        }
    }
}

@Composable
private fun SummaryItem(label: String, value: String, color: Color, modifier: Modifier = Modifier) {
    Box(
        modifier = modifier
            .background(MaterialTheme.colorScheme.surface, RoundedCornerShape(16.dp))
            .border(1.dp, Color(0xFFF3F4F6), RoundedCornerShape(16.dp))
            .padding(16.dp)
    ) {
        Column {
            Text(value, fontSize = 20.sp, fontWeight = FontWeight.Black, color = color)
            Text(label, fontSize = 12.sp, fontWeight = FontWeight.Bold, color = Color(0xFF64748B))
        }
    }
}

@Composable
private fun EditProfileDialog(
    profile: FacultyProfile,
    isSaving: Boolean,
    saveError: String?,
    onDismiss: () -> Unit,
    onSave: (com.example.core.network.FacultyProfileUpdateRequest) -> Unit
) {
    var officialPhone by remember { mutableStateOf(profile.phone) }
    var alternatePhone by remember { mutableStateOf(profile.alternatePhone) }
    var personalEmail by remember { mutableStateOf(profile.personalEmail) }
    var currentAddress by remember { mutableStateOf(profile.currentAddress) }
    var permanentAddress by remember { mutableStateOf(profile.permanentAddress) }
    var city by remember { mutableStateOf(profile.city) }
    var state by remember { mutableStateOf(profile.state) }
    var pincode by remember { mutableStateOf(profile.pincode) }
    var gender by remember { mutableStateOf(profile.gender) }
    var dateOfBirth by remember { mutableStateOf(profile.dateOfBirth) }
    var bloodGroup by remember { mutableStateOf(profile.bloodGroup) }
    var maritalStatus by remember { mutableStateOf(profile.maritalStatus) }
    var nationality by remember { mutableStateOf(profile.nationality) }
    var community by remember { mutableStateOf(profile.community) }
    var validationError by remember { mutableStateOf<String?>(null) }
    val dateRegex = remember { Regex("""\d{4}-\d{2}-\d{2}""") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Edit Profile", fontWeight = FontWeight.Black) },
        text = {
            Column(
                modifier = Modifier.verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Text(
                    "Address, contact, and personal details update immediately. Identity fields (employee code, designation, department) require HOD/Principal approval and aren't editable here yet.",
                    fontSize = 12.sp,
                    color = Color(0xFF64748B)
                )
                OutlinedTextField(value = officialPhone, onValueChange = { officialPhone = it }, label = { Text("Official Phone") }, modifier = Modifier.fillMaxWidth(), singleLine = true, keyboardOptions = androidx.compose.foundation.text.KeyboardOptions(keyboardType = androidx.compose.ui.text.input.KeyboardType.Phone))
                OutlinedTextField(value = alternatePhone, onValueChange = { alternatePhone = it }, label = { Text("Alternate Phone") }, modifier = Modifier.fillMaxWidth(), singleLine = true, keyboardOptions = androidx.compose.foundation.text.KeyboardOptions(keyboardType = androidx.compose.ui.text.input.KeyboardType.Phone))
                OutlinedTextField(value = personalEmail, onValueChange = { personalEmail = it }, label = { Text("Personal Email") }, modifier = Modifier.fillMaxWidth(), singleLine = true, keyboardOptions = androidx.compose.foundation.text.KeyboardOptions(keyboardType = androidx.compose.ui.text.input.KeyboardType.Email))
                OutlinedTextField(value = currentAddress, onValueChange = { currentAddress = it }, label = { Text("Current Address") }, modifier = Modifier.fillMaxWidth(), minLines = 2)
                OutlinedTextField(value = permanentAddress, onValueChange = { permanentAddress = it }, label = { Text("Permanent Address") }, modifier = Modifier.fillMaxWidth(), minLines = 2)
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(value = city, onValueChange = { city = it }, label = { Text("City") }, modifier = Modifier.weight(1f), singleLine = true)
                    OutlinedTextField(value = state, onValueChange = { state = it }, label = { Text("State") }, modifier = Modifier.weight(1f), singleLine = true)
                }
                OutlinedTextField(value = pincode, onValueChange = { pincode = it }, label = { Text("Pincode") }, modifier = Modifier.fillMaxWidth(), singleLine = true, keyboardOptions = androidx.compose.foundation.text.KeyboardOptions(keyboardType = androidx.compose.ui.text.input.KeyboardType.Number))
                OutlinedTextField(value = gender, onValueChange = { gender = it }, label = { Text("Gender") }, modifier = Modifier.fillMaxWidth(), singleLine = true)
                OutlinedTextField(value = dateOfBirth, onValueChange = { dateOfBirth = it }, label = { Text("Date of Birth (YYYY-MM-DD)") }, modifier = Modifier.fillMaxWidth(), singleLine = true)
                OutlinedTextField(value = bloodGroup, onValueChange = { bloodGroup = it }, label = { Text("Blood Group") }, modifier = Modifier.fillMaxWidth(), singleLine = true)
                OutlinedTextField(value = maritalStatus, onValueChange = { maritalStatus = it }, label = { Text("Marital Status") }, modifier = Modifier.fillMaxWidth(), singleLine = true)
                OutlinedTextField(value = nationality, onValueChange = { nationality = it }, label = { Text("Nationality") }, modifier = Modifier.fillMaxWidth(), singleLine = true)
                OutlinedTextField(value = community, onValueChange = { community = it }, label = { Text("Community") }, modifier = Modifier.fillMaxWidth(), singleLine = true)

                val displayedError = validationError ?: saveError
                if (displayedError != null) {
                    Text(displayedError, color = Color(0xFFEF4444), fontSize = 12.sp, fontWeight = FontWeight.Medium)
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    validationError = when {
                        personalEmail.isNotBlank() && !android.util.Patterns.EMAIL_ADDRESS.matcher(personalEmail.trim()).matches() ->
                            "Enter a valid personal email address."
                        officialPhone.isNotBlank() && !android.util.Patterns.PHONE.matcher(officialPhone.trim()).matches() ->
                            "Enter a valid official phone number."
                        alternatePhone.isNotBlank() && !android.util.Patterns.PHONE.matcher(alternatePhone.trim()).matches() ->
                            "Enter a valid alternate phone number."
                        dateOfBirth.isNotBlank() && !dateRegex.matches(dateOfBirth) ->
                            "Date of birth must be in YYYY-MM-DD format."
                        else -> null
                    }
                    if (validationError == null) {
                        onSave(
                            com.example.core.network.FacultyProfileUpdateRequest(
                                maritalStatus = maritalStatus.ifBlank { null },
                                community = community.ifBlank { null },
                                alternatePhone = alternatePhone.ifBlank { null },
                                personalEmail = personalEmail.ifBlank { null },
                                currentAddress = currentAddress.ifBlank { null },
                                permanentAddress = permanentAddress.ifBlank { null },
                                city = city.ifBlank { null },
                                state = state.ifBlank { null },
                                pincode = pincode.ifBlank { null },
                                gender = gender.ifBlank { null },
                                dateOfBirth = dateOfBirth.ifBlank { null },
                                bloodGroup = bloodGroup.ifBlank { null },
                                nationality = nationality.ifBlank { null },
                                officialPhone = officialPhone.ifBlank { null }
                            )
                        )
                    }
                },
                colors = ButtonDefaults.buttonColors(containerColor = CamsNavy),
                enabled = !isSaving
            ) {
                if (isSaving) {
                    CircularProgressIndicator(modifier = Modifier.size(18.dp), color = Color.White, strokeWidth = 2.dp)
                } else {
                    Text("Save")
                }
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Cancel") }
        }
    )
}
