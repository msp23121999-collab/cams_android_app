package com.example.features.campus_life.screens

import androidx.compose.animation.*
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.core.theme.*
import com.example.core.navigation.AppRoutes
import com.example.core.ui.CamsCard
import com.example.core.ui.CamsScreen
import com.example.features.campus_life.models.ResearchPaper
import com.example.features.campus_life.providers.ProjectShowcaseViewModel
import com.example.features.student.widgets.StudentDrawer
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProjectShowcaseScreen(
    onNavigate: (String) -> Unit,
    viewModel: ProjectShowcaseViewModel = viewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    var activeTab by remember { mutableStateOf("all") }

    CamsScreen(scrollable = true,
        title = "Academic Showcase",
        subtitle = "Research papers and dissertations authored by students",
        onBackClick = { onNavigate(AppRoutes.STUDENT_DASHBOARD) },
        actions = {
            IconButton(onClick = {}) {
                Icon(Icons.Filled.Add, contentDescription = "Submit", tint = Color.White)
            }
        },
        verticalArrangement = Arrangement.spacedBy(20.dp)
    ) {
        // Search & Submit
            Surface(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                color = Color.White,
                border = BorderStroke(1.dp, Color.LightGray.copy(alpha = 0.3f))
            ) {
                Row(modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp), verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Filled.Search, contentDescription = null, tint = CamsTextSecondary, modifier = Modifier.size(20.dp))
                    Spacer(modifier = Modifier.width(12.dp))
                    Text("Search titles...", style = MaterialTheme.typography.bodyMedium.copy(color = CamsTextSecondary))
                }
            }

            // Tabs
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                TabItem(
                    text = "All Papers",
                    isSelected = activeTab == "all",
                    onClick = { activeTab = "all" },
                    modifier = Modifier.weight(1f)
                )
                TabItem(
                    text = "Featured",
                    isSelected = activeTab == "featured",
                    onClick = { activeTab = "featured" },
                    modifier = Modifier.weight(1f)
                )
            }

            // Papers List
            val filteredPapers = if (activeTab == "all") uiState.papers else uiState.papers.filter { it.featured }
            
            filteredPapers.forEach { paper ->
                ResearchPaperCard(paper)
            }
            
            Spacer(modifier = Modifier.height(20.dp))
    }
}

@Composable
fun TabItem(text: String, isSelected: Boolean, onClick: () -> Unit, modifier: Modifier = Modifier) {
    Column(
        modifier = modifier.clickable(onClick = onClick),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text.uppercase(),
            style = MaterialTheme.typography.labelSmall.copy(
                fontWeight = FontWeight.Black,
                color = if (isSelected) CamsNavy else CamsTextSecondary,
                letterSpacing = 1.sp
            ),
            modifier = Modifier.padding(vertical = 12.dp)
        )
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(2.dp)
                .background(if (isSelected) CamsNavy else Color.Transparent)
        )
    }
}

@Composable
fun ResearchPaperCard(paper: ResearchPaper) {
    CamsCard(modifier = Modifier.fillMaxWidth()) {
        Column {
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Surface(color = CamsNavy.copy(alpha = 0.1f), shape = CircleShape) {
                        Text(paper.category.uppercase(), modifier = Modifier.padding(horizontal = 10.dp, vertical = 2.dp), style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Black, color = CamsNavy, fontSize = 12.sp))
                    }
                    if (paper.featured) {
                        Surface(color = Color(0xFFFFF7ED), shape = CircleShape) {
                            Row(modifier = Modifier.padding(horizontal = 10.dp, vertical = 2.dp), verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.Filled.Star, contentDescription = null, tint = Color(0xFFF59E0B), modifier = Modifier.size(8.dp))
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("FEATURED", style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Black, color = Color(0xFFB45309), fontSize = 12.sp))
                            }
                        }
                    }
                }
                Surface(color = CamsBackground, shape = CircleShape) {
                    Text(paper.status.uppercase(), modifier = Modifier.padding(horizontal = 10.dp, vertical = 2.dp), style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Black, color = CamsTextSecondary, fontSize = 12.sp))
                }
            }

            Spacer(modifier = Modifier.height(16.dp))
            Text(paper.title, style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Black, color = CamsTextPrimary, lineHeight = 24.sp))
            Spacer(modifier = Modifier.height(8.dp))
            Text(paper.abstract, style = MaterialTheme.typography.bodySmall.copy(color = CamsTextSecondary))

            if (paper.awards.isNotEmpty()) {
                Spacer(modifier = Modifier.height(12.dp))
                paper.awards.forEach { award ->
                    Surface(color = Color(0xFFFFFBEB), shape = RoundedCornerShape(8.dp), border = BorderStroke(1.dp, Color(0xFFFEF3C7))) {
                        Row(modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp), verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Filled.Stars, contentDescription = null, tint = Color(0xFFF59E0B), modifier = Modifier.size(14.dp))
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(award, style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold, color = Color(0xFFB45309)))
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(20.dp))
            HorizontalDivider(color = Color.LightGray.copy(alpha = 0.2f))
            Spacer(modifier = Modifier.height(16.dp))

            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                Column(modifier = Modifier.weight(1f)) {
                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        Icon(Icons.Filled.Group, contentDescription = null, tint = CamsTextSecondary, modifier = Modifier.size(12.dp))
                        Text(paper.team.joinToString(", "), style = MaterialTheme.typography.labelSmall.copy(color = CamsTextSecondary))
                    }
                    Spacer(modifier = Modifier.height(4.dp))
                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        Icon(Icons.Filled.School, contentDescription = null, tint = CamsTextSecondary, modifier = Modifier.size(12.dp))
                        Text("Guide: ${paper.guide}", style = MaterialTheme.typography.labelSmall.copy(color = CamsTextSecondary))
                    }
                }
                
                Surface(
                    onClick = {},
                    modifier = Modifier.size(40.dp),
                    shape = RoundedCornerShape(10.dp),
                    color = CamsNavy,
                    shadowElevation = 2.dp
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(Icons.Filled.Download, contentDescription = null, tint = Color.White, modifier = Modifier.size(18.dp))
                    }
                }
            }
        }
    }
}
