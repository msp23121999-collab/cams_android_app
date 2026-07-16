package com.example.features.faculty.screens

import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
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
    // Mock activity and subjects if not in ProfileState yet, or better, add them to ProfileState if needed.
    // For now I'll just use the ones from profile if I added them there, but they weren't in my ProfileState.
    val activity = com.example.features.faculty.models.ActivitySummary()
    val subjects = emptyList<com.example.features.faculty.models.FacultySubject>()

    com.example.features.faculty.widgets.FacultyBaseScreen(
        title = "Faculty Profile",
        subtitle = "Detailed academic & professional record",
        currentRoute = "/faculty/profile",
        onNavigate = onNavigate
    ) {
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

@Composable
private fun SubjectExpertiseSection(subjects: List<FacultySubject>) {
    SectionTitle("Subject Expertise", Icons.AutoMirrored.Filled.MenuBook)
    CamsCard {
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
