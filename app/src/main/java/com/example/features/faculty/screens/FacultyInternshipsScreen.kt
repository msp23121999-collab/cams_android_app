package com.example.features.faculty.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.core.theme.*
import com.example.features.faculty.widgets.FacultyBaseScreen

import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.core.repository.FacultyRepositoryImpl
import com.example.features.faculty.providers.FacultyInternshipsViewModel
import com.example.features.faculty.providers.FacultyInternshipsViewModelFactory
import com.example.core.network.ApiClient

@Composable
fun FacultyInternshipsScreen(onNavigate: (String) -> Unit) {
    val repository = remember { FacultyRepositoryImpl(com.example.CamsApplication.instance.container.apiService) }
    val factory = remember { FacultyInternshipsViewModelFactory(repository) }
    val viewModel: FacultyInternshipsViewModel = viewModel(factory = factory)
    val uiState by viewModel.uiState.collectAsState()
    var selectedTab by remember { mutableStateOf(0) }
    val tabs = listOf("Applications", "Opportunities", "Partners")

    FacultyBaseScreen(scrollable = false, 
        title = "Internships & Placements",
        currentRoute = com.example.core.navigation.AppRoutes.FACULTY_INTERNSHIPS,
        onNavigate = onNavigate,
        floatingActionButton = {
            FloatingActionButton(
                onClick = { /* Post Opportunity */ },
                containerColor = CamsNavy,
                contentColor = Color.White,
                shape = RoundedCornerShape(16.dp)
            ) {
                Icon(Icons.Filled.Add, "Post Opportunity")
            }
        }
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp)
        ) {
            // Stats Row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                InternshipStatsCard("Applicants", "156", Icons.Filled.Groups, Color(0xFF3B82F6), Modifier.weight(1f))
                InternshipStatsCard("Pending", "42", Icons.Filled.HourglassEmpty, Color(0xFFF59E0B), Modifier.weight(1f))
                InternshipStatsCard("Placed", "89", Icons.Filled.CheckCircle, Color(0xFF10B981), Modifier.weight(1f))
            }

            Spacer(modifier = Modifier.height(20.dp))

            // Tabs
            ScrollableTabRow(
                selectedTabIndex = selectedTab,
                containerColor = Color.Transparent,
                contentColor = CamsNavy,
                edgePadding = 0.dp,
                divider = {},
                indicator = { tabPositions ->
                    if (selectedTab < tabPositions.size) {
                        TabRowDefaults.SecondaryIndicator(
                            modifier = Modifier.tabIndicatorOffset(tabPositions[selectedTab]),
                            color = CamsNavy
                        )
                    }
                }
            ) {
                tabs.forEachIndexed { index, title ->
                    Tab(
                        selected = selectedTab == index,
                        onClick = { selectedTab = index },
                        text = {
                            Text(
                                title,
                                fontWeight = if (selectedTab == index) FontWeight.Bold else FontWeight.Medium
                            )
                        }
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Content based on tab
            when (selectedTab) {
                0 -> StudentApplicationsList() // Keeping mock as there is no endpoint
                1 -> InternshipOpportunitiesList(uiState.drives)
                2 -> PartnerCompaniesList() // Keeping mock as there is no endpoint
            }
        }
    }
}

@Composable
private fun InternshipStatsCard(
    label: String,
    value: String,
    icon: ImageVector,
    color: Color,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier,
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        shape = RoundedCornerShape(16.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Icon(icon, null, tint = color, modifier = Modifier.size(24.dp))
            Spacer(modifier = Modifier.height(8.dp))
            Text(value, fontSize = 20.sp, fontWeight = FontWeight.Bold, color = CamsTextPrimary)
            Text(label, fontSize = 12.sp, color = CamsTextSecondary)
        }
    }
}

@Composable
private fun StudentApplicationsList() {
    val applications = listOf(
        Application("Aditya Singh", "Google", "Software Intern", "Pending Review"),
        Application("Sneha Rao", "Microsoft", "UX Researcher", "Interviewing"),
        Application("Vikram Mehra", "Adobe", "Product Intern", "Offered")
    )

    LazyColumn(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        items(applications) { app ->
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                shape = RoundedCornerShape(16.dp)
            ) {
                Row(
                    modifier = Modifier.padding(16.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(app.studentName, fontWeight = FontWeight.Bold, fontSize = 16.sp, color = CamsTextPrimary)
                        Text("${app.role} @ ${app.company}", fontSize = 13.sp, color = CamsTextSecondary)
                        Spacer(modifier = Modifier.height(8.dp))
                        Surface(
                            color = when(app.status) {
                                "Offered" -> Color(0xFF10B981).copy(alpha = 0.1f)
                                "Interviewing" -> Color(0xFF3B82F6).copy(alpha = 0.1f)
                                else -> Color(0xFFF59E0B).copy(alpha = 0.1f)
                            },
                            shape = RoundedCornerShape(8.dp)
                        ) {
                            Text(
                                app.status,
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Bold,
                                color = when(app.status) {
                                    "Offered" -> Color(0xFF10B981)
                                    "Interviewing" -> Color(0xFF3B82F6)
                                    else -> Color(0xFFF59E0B)
                                }
                            )
                        }
                    }
                    IconButton(onClick = { /* Review */ }) {
                        Icon(Icons.Filled.ChevronRight, null, tint = CamsTextSecondary)
                    }
                }
            }
        }
    }
}

@Composable
private fun InternshipOpportunitiesList(opportunities: List<com.example.core.network.FacultyInternshipDriveDto>) {
    LazyColumn(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        items(opportunities) { opp ->
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                shape = RoundedCornerShape(16.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column {
                            Text(opp.role, fontWeight = FontWeight.Bold, fontSize = 16.sp, color = CamsTextPrimary)
                            Text(opp.company, fontSize = 13.sp, color = CamsNavy)
                        }
                        Text(opp.deadline ?: "No deadline", fontSize = 12.sp, color = CamsTextSecondary)
                    }
                    Spacer(modifier = Modifier.height(12.dp))
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Filled.LocationOn, null, tint = CamsTextSecondary, modifier = Modifier.size(14.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(opp.location ?: "Remote", fontSize = 13.sp, color = CamsTextSecondary)
                    }
                }
            }
        }
    }
}

@Composable
private fun PartnerCompaniesList() {
    val partners = listOf(
        Partner("Google", "Technology", "Active"),
        Partner("TCS", "Consulting", "Active"),
        Partner("L&T", "Engineering", "Pending Renewal")
    )

    LazyColumn(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        items(partners) { partner ->
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                shape = RoundedCornerShape(16.dp)
            ) {
                Row(
                    modifier = Modifier.padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Surface(
                        modifier = Modifier.size(40.dp),
                        shape = RoundedCornerShape(8.dp),
                        color = CamsNavy.copy(alpha = 0.1f)
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Text(partner.name.take(1), fontWeight = FontWeight.Bold, color = CamsNavy)
                        }
                    }
                    Spacer(modifier = Modifier.width(16.dp))
                    Column(modifier = Modifier.weight(1f)) {
                        Text(partner.name, fontWeight = FontWeight.Bold, color = CamsTextPrimary)
                        Text(partner.industry, fontSize = 12.sp, color = CamsTextSecondary)
                    }
                    Surface(
                        color = if(partner.status == "Active") Color(0xFF10B981).copy(alpha = 0.1f) else Color(0xFFF59E0B).copy(alpha = 0.1f),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Text(
                            partner.status,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            color = if(partner.status == "Active") Color(0xFF10B981) else Color(0xFFF59E0B)
                        )
                    }
                }
            }
        }
    }
}

data class Application(val studentName: String, val company: String, val role: String, val status: String)
data class Partner(val name: String, val industry: String, val status: String)
