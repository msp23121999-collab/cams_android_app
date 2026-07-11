package com.example.features.parent.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.core.theme.*
import com.example.core.ui.CamsCard
import com.example.core.ui.CamsScreen
import com.example.features.parent.providers.ParentProfileViewModel
import com.example.features.parent.widgets.ParentDrawer
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ParentChildProfileScreen(
    viewModel: ParentProfileViewModel,
    onNavigate: (String) -> Unit
) {
    val uiState by viewModel.uiState.collectAsState()
    val drawerState = rememberDrawerState(initialValue = DrawerValue.Closed)
    val scope = rememberCoroutineScope()

    ModalNavigationDrawer(
        drawerState = drawerState,
        drawerContent = {
            ParentDrawer(
                currentRoute = "/parent/profile",
                onNavigate = {
                    scope.launch { drawerState.close() }
                    onNavigate(it)
                }
            )
        }
    ) {
        CamsScreen(
            scrollable = true,
            title = "Child Profile",
            subtitle = "Detailed academic & personal record",
            navigationIcon = {
                IconButton(onClick = { scope.launch { drawerState.open() } }) {
                    Icon(Icons.Filled.Menu, contentDescription = "Menu", tint = Color.White)
                }
            },
            actions = {
                IconButton(onClick = { onNavigate("LOGOUT") }) {
                    Icon(Icons.Filled.Logout, contentDescription = "Logout", tint = Color.White)
                }
            },
            verticalArrangement = Arrangement.spacedBy(24.dp)
        ) {
            if (uiState.isLoading) {
                Box(modifier = Modifier.fillMaxWidth().height(200.dp), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator(color = CamsNavy)
                }
            } else if (uiState.childProfileExtended != null) {
                val profile = uiState.childProfileExtended!!

                // Header Profile Card
                CamsCard {
                    Column(modifier = Modifier.fillMaxWidth()) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(16.dp)
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(72.dp)
                                    .background(CamsNavy.copy(alpha = 0.1f), CircleShape),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = profile.fullName.split(" ").map { it.take(1) }.joinToString(""),
                                    fontSize = 24.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = CamsNavy
                                )
                            }

                            Column {
                                Text(
                                    profile.fullName,
                                    fontWeight = FontWeight.Black,
                                    fontSize = 22.sp,
                                    color = CamsTextPrimary
                                )
                                Text(
                                    "Roll No: ${profile.rollNo}",
                                    fontSize = 14.sp,
                                    color = CamsTextSecondary,
                                    fontWeight = FontWeight.Medium
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(16.dp))

                        // Sem, Batch, CGPA pills
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            InfoBadge(label = profile.semester, color = CamsNavy)
                            InfoBadge(label = "Batch ${profile.batch}", color = LexNovaPurple)
                            InfoBadge(label = "CGPA: ${profile.cgpa}", color = Color(0xFF10B981))
                        }
                    }
                }

                // Faculty Mentor Card
                CamsCard {
                    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Filled.SupportAgent, null, tint = CamsNavy)
                            Spacer(Modifier.width(8.dp))
                            Text("Faculty Mentor", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleMedium)
                        }
                        Divider(color = Color.LightGray.copy(alpha = 0.3f))
                        Text(profile.mentorName, fontWeight = FontWeight.Bold, fontSize = 16.sp, color = CamsTextPrimary)
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Filled.Email, null, tint = CamsTextSecondary, modifier = Modifier.size(14.dp))
                            Spacer(Modifier.width(6.dp))
                            Text(profile.mentorEmail, fontSize = 13.sp, color = CamsTextSecondary)
                        }
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Filled.Phone, null, tint = CamsTextSecondary, modifier = Modifier.size(14.dp))
                            Spacer(Modifier.width(6.dp))
                            Text(profile.mentorPhone, fontSize = 13.sp, color = CamsTextSecondary)
                        }
                    }
                }

                // Personal Details Card
                CamsCard {
                    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Filled.Badge, null, tint = CamsNavy)
                            Spacer(Modifier.width(8.dp))
                            Text("Personal Details", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleMedium)
                        }
                        Divider(color = Color.LightGray.copy(alpha = 0.3f))
                        
                        DetailRow("Date of Birth", profile.dob)
                        DetailRow("Gender", profile.gender)
                        DetailRow("Blood Group", profile.bloodGroup)
                        DetailRow("Nationality", profile.nationality)
                        DetailRow("Aadhaar Number", profile.aadhaarNo)
                    }
                }

                // Contact Details Card
                CamsCard {
                    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Filled.ContactPhone, null, tint = CamsNavy)
                            Spacer(Modifier.width(8.dp))
                            Text("Contact Details", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleMedium)
                        }
                        Divider(color = Color.LightGray.copy(alpha = 0.3f))
                        
                        DetailRow("Mobile Number", profile.contactMobile)
                        DetailRow("Email Address", profile.contactEmail)
                        DetailRow("Emergency Contact", profile.emergencyContact)
                        DetailRow("Emergency Mobile", profile.emergencyPhone)
                    }
                }

                // Parents/Guardian Information Card
                CamsCard {
                    Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Filled.People, null, tint = CamsNavy)
                            Spacer(Modifier.width(8.dp))
                            Text("Parent / Guardian Information", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleMedium)
                        }
                        Divider(color = Color.LightGray.copy(alpha = 0.3f))

                        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                            Text("Father's Profile", fontWeight = FontWeight.Bold, color = CamsNavy, fontSize = 14.sp)
                            DetailRow("Name", profile.fatherName)
                            DetailRow("Occupation", profile.fatherOccupation)
                            DetailRow("Mobile", profile.fatherMobile)
                            DetailRow("Email", profile.fatherEmail)
                        }

                        Divider(color = Color.LightGray.copy(alpha = 0.3f))

                        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                            Text("Mother's Profile", fontWeight = FontWeight.Bold, color = CamsNavy, fontSize = 14.sp)
                            DetailRow("Name", profile.motherName)
                            DetailRow("Occupation", profile.motherOccupation)
                            DetailRow("Mobile", profile.motherMobile)
                            DetailRow("Email", profile.motherEmail)
                        }
                    }
                }

                // Certifications Card
                CamsCard {
                    Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Filled.MilitaryTech, null, tint = CamsNavy)
                            Spacer(Modifier.width(8.dp))
                            Text("Child Certifications & Achievements", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleMedium)
                        }
                        Divider(color = Color.LightGray.copy(alpha = 0.3f))

                        if (profile.certifications.isEmpty()) {
                            Text("No certifications uploaded yet.", color = CamsTextSecondary, fontSize = 13.sp)
                        } else {
                            profile.certifications.forEach { cert ->
                                Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                                    Row(
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically,
                                        modifier = Modifier.fillMaxWidth()
                                    ) {
                                        Text(cert.title, fontWeight = FontWeight.Bold, fontSize = 14.sp, color = CamsTextPrimary, modifier = Modifier.weight(1f))
                                        Surface(
                                            color = Color(0xFFECFDF5),
                                            shape = RoundedCornerShape(6.dp)
                                        ) {
                                            Text(
                                                cert.status,
                                                color = Color(0xFF047857),
                                                fontWeight = FontWeight.Bold,
                                                fontSize = 13.sp,
                                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                            )
                                        }
                                    }
                                    Text("${cert.authority} • ${cert.category}", fontSize = 12.sp, color = CamsTextSecondary)
                                    Text("Issued on: ${cert.date}", fontSize = 13.sp, color = CamsTextSecondary.copy(alpha = 0.8f))
                                }
                                Spacer(modifier = Modifier.height(8.dp))
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun InfoBadge(label: String, color: Color) {
    Surface(
        color = color.copy(alpha = 0.1f),
        shape = RoundedCornerShape(8.dp)
    ) {
        Text(
            text = label,
            color = color,
            fontWeight = FontWeight.Bold,
            fontSize = 12.sp,
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
        )
    }
}

@Composable
private fun DetailRow(label: String, value: String) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(label, color = CamsTextSecondary, fontSize = 13.sp, fontWeight = FontWeight.Medium)
        Text(value, color = CamsTextPrimary, fontSize = 13.sp, fontWeight = FontWeight.Bold)
    }
}
