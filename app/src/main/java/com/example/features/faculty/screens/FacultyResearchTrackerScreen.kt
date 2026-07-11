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

@Composable
fun FacultyResearchTrackerScreen(onNavigate: (String) -> Unit) {
    var selectedTab by remember { mutableStateOf(0) }
    val tabs = listOf("Publications", "Mentorship", "Grants")

    FacultyBaseScreen(scrollable = false, 
        title = "Research Tracker",
        currentRoute = com.example.core.navigation.AppRoutes.FACULTY_RESEARCH_TRACKER,
        onNavigate = onNavigate,
        floatingActionButton = {
            FloatingActionButton(
                onClick = { /* Add Publication */ },
                containerColor = CamsNavy,
                contentColor = Color.White,
                shape = RoundedCornerShape(16.dp)
            ) {
                Icon(Icons.Filled.Add, "Add Publication")
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
                ResearchStatsCard("Publications", "18", Icons.Filled.Description, Color(0xFF3B82F6), Modifier.weight(1f))
                ResearchStatsCard("Citations", "452", Icons.Filled.FormatQuote, Color(0xFF10B981), Modifier.weight(1f))
                ResearchStatsCard("h-index", "12", Icons.Filled.TrendingUp, Color(0xFFF59E0B), Modifier.weight(1f))
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
                0 -> FacultyPublicationsList()
                1 -> StudentMentorshipList()
                2 -> ResearchGrantsList()
            }
        }
    }
}

@Composable
private fun ResearchStatsCard(
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
private fun FacultyPublicationsList() {
    val papers = listOf(
        Paper("AI in Higher Education", "International Journal of CS", "Published", "2023"),
        Paper("Blockchain for Academic Credentials", "IEEE Access", "Under Review", "2023"),
        Paper("Quantum Computing Paradigms", "Computing Surveys", "Published", "2022")
    )

    LazyColumn(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        items(papers) { paper ->
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                shape = RoundedCornerShape(16.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.Top
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(paper.title, fontWeight = FontWeight.Bold, fontSize = 16.sp, color = CamsTextPrimary)
                            Text(paper.journal, fontSize = 13.sp, color = CamsNavy)
                        }
                        Surface(
                            color = if(paper.status == "Published") Color(0xFF10B981).copy(alpha = 0.1f) else Color(0xFF3B82F6).copy(alpha = 0.1f),
                            shape = RoundedCornerShape(8.dp)
                        ) {
                            Text(
                                paper.status,
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                color = if(paper.status == "Published") Color(0xFF10B981) else Color(0xFF3B82F6)
                            )
                        }
                    }
                    Spacer(modifier = Modifier.height(12.dp))
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Filled.CalendarToday, null, tint = CamsTextSecondary, modifier = Modifier.size(14.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(paper.year, fontSize = 13.sp, color = CamsTextSecondary)
                    }
                }
            }
        }
    }
}

@Composable
private fun StudentMentorshipList() {
    val students = listOf(
        ResearchMentee("Abhinav Gupta", "NLP and LLMs", "Thesis Phase"),
        ResearchMentee("Ria Sharma", "Cybersecurity", "Proposal Phase"),
        ResearchMentee("Zaid Khan", "Machine Learning", "Data Collection")
    )

    LazyColumn(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        items(students) { mentee ->
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
                    Column {
                        Text(mentee.name, fontWeight = FontWeight.Bold, color = CamsTextPrimary)
                        Text(mentee.topic, fontSize = 12.sp, color = CamsNavy)
                    }
                    Surface(
                        color = CamsNavy.copy(alpha = 0.05f),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Text(
                            mentee.phase,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                            fontSize = 13.sp,
                            color = CamsNavy,
                            fontWeight = FontWeight.Medium
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun ResearchGrantsList() {
    val grants = listOf(
        Grant("Smart City IoT", "DST India", "₹ 15,00,000", "Approved"),
        Grant("Deep Learning for Health", "AICTE", "₹ 8,00,000", "Applied")
    )

    LazyColumn(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        items(grants) { grant ->
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
                        Text(grant.title, fontWeight = FontWeight.Bold, color = CamsTextPrimary)
                        Text(grant.amount, fontWeight = FontWeight.Bold, color = Color(0xFF10B981))
                    }
                    Spacer(modifier = Modifier.height(4.dp))
                    Text("Source: ${grant.source}", fontSize = 13.sp, color = CamsTextSecondary)
                    Spacer(modifier = Modifier.height(12.dp))
                    Surface(
                        color = if(grant.status == "Approved") Color(0xFF10B981).copy(alpha = 0.1f) else Color(0xFFF59E0B).copy(alpha = 0.1f),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Text(
                            grant.status,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold,
                            color = if(grant.status == "Approved") Color(0xFF10B981) else Color(0xFFF59E0B)
                        )
                    }
                }
            }
        }
    }
}

data class Paper(val title: String, val journal: String, val status: String, val year: String)
data class ResearchMentee(val name: String, val topic: String, val phase: String)
data class Grant(val title: String, val source: String, val amount: String, val status: String)
