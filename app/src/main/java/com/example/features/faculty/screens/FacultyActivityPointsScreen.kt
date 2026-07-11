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
fun FacultyActivityPointsScreen(onNavigate: (String) -> Unit) {
    var selectedTab by remember { mutableStateOf(0) }
    val tabs = listOf("Pending Queue", "Student Records", "Categories")

    FacultyBaseScreen(scrollable = false, 
        title = "Activity Points",
        currentRoute = com.example.core.navigation.AppRoutes.FACULTY_ACTIVITY_POINTS,
        onNavigate = onNavigate
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
                ActivityStatsCard("Pending", "24", Icons.Filled.Pending, Color(0xFFF59E0B), Modifier.weight(1f))
                ActivityStatsCard("Verified", "842", Icons.Filled.Verified, Color(0xFF10B981), Modifier.weight(1f))
                ActivityStatsCard("Avg Pts", "42", Icons.Filled.BarChart, Color(0xFF3B82F6), Modifier.weight(1f))
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
                0 -> PendingApprovalQueue()
                1 -> StudentPointsRecords()
                2 -> ActivityCategoriesList()
            }
        }
    }
}

@Composable
private fun ActivityStatsCard(
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
private fun PendingApprovalQueue() {
    val requests = listOf(
        PointRequest("Arjun V.", "NSS Camp", "Community Service", "15 Pts"),
        PointRequest("Sanya K.", "Hackathon Winner", "Technical", "25 Pts"),
        PointRequest("Karan S.", "Cricket Tournament", "Sports", "10 Pts")
    )

    LazyColumn(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        items(requests) { req ->
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
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(req.studentName, fontWeight = FontWeight.Bold, fontSize = 16.sp, color = CamsTextPrimary)
                            Text(req.activity, fontSize = 13.sp, color = CamsNavy)
                        }
                        Text(req.points, fontWeight = FontWeight.ExtraBold, color = Color(0xFF10B981), fontSize = 18.sp)
                    }
                    Spacer(modifier = Modifier.height(8.dp))
                    Text("Category: ${req.category}", fontSize = 12.sp, color = CamsTextSecondary)
                    Spacer(modifier = Modifier.height(16.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        OutlinedButton(
                            onClick = { /* Reject */ },
                            modifier = Modifier.weight(1f),
                            shape = RoundedCornerShape(12.dp),
                            colors = ButtonDefaults.outlinedButtonColors(contentColor = Color.Red)
                        ) {
                            Text("Reject")
                        }
                        Button(
                            onClick = { /* Approve */ },
                            modifier = Modifier.weight(1f),
                            shape = RoundedCornerShape(12.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = CamsNavy)
                        ) {
                            Text("Approve")
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun StudentPointsRecords() {
    val records = listOf(
        StudentRecord("Rahul M.", "152 Pts", "Exceeding"),
        StudentRecord("Pooja G.", "95 Pts", "On Track"),
        StudentRecord("Sameer L.", "45 Pts", "Below Target")
    )

    LazyColumn(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        items(records) { rec ->
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
                        Text(rec.name, fontWeight = FontWeight.Bold, color = CamsTextPrimary)
                        Text(rec.status, fontSize = 12.sp, color = when(rec.status) {
                            "Exceeding" -> Color(0xFF10B981)
                            "On Track" -> Color(0xFF3B82F6)
                            else -> Color(0xFFF59E0B)
                        })
                    }
                    Text(rec.totalPoints, fontWeight = FontWeight.Bold, color = CamsNavy)
                }
            }
        }
    }
}

@Composable
private fun ActivityCategoriesList() {
    val categories = listOf(
        CategoryItem("NSS/Social Work", "Max 50 Pts", Icons.Filled.Group),
        CategoryItem("Technical Events", "Max 40 Pts", Icons.Filled.Computer),
        CategoryItem("Sports", "Max 30 Pts", Icons.Filled.SportsBasketball),
        CategoryItem("Cultural", "Max 30 Pts", Icons.Filled.MusicNote)
    )

    LazyColumn(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        items(categories) { cat ->
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
                    Icon(cat.icon, null, tint = CamsNavy, modifier = Modifier.size(24.dp))
                    Spacer(modifier = Modifier.width(16.dp))
                    Column {
                        Text(cat.name, fontWeight = FontWeight.Bold, color = CamsTextPrimary)
                        Text(cat.limit, fontSize = 12.sp, color = CamsTextSecondary)
                    }
                }
            }
        }
    }
}

data class PointRequest(val studentName: String, val activity: String, val category: String, val points: String)
data class StudentRecord(val name: String, val totalPoints: String, val status: String)
data class CategoryItem(val name: String, val limit: String, val icon: ImageVector)
