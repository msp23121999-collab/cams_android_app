package com.example.features.extracurriculars.screens

import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.core.theme.LexNovaPurple
import com.example.features.extracurriculars.models.InnovationProject
import com.example.features.extracurriculars.providers.ExtracurricularsViewModel
import com.example.features.student.widgets.StudentDrawer
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun InnovationWallScreen(
    viewModel: ExtracurricularsViewModel = viewModel(),
    onNavigate: (String) -> Unit
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val drawerState = rememberDrawerState(initialValue = DrawerValue.Closed)
    val scope = rememberCoroutineScope()
    var activeCategory by remember { mutableStateOf("All") }

    ModalNavigationDrawer(
        drawerState = drawerState,
        drawerContent = {
            StudentDrawer(
                currentRoute = "/student/innovation_wall",
                onNavigate = {
                    scope.launch { drawerState.close() }
                    onNavigate(it)
                }
            )
        }
    ) {
        Scaffold(
            topBar = {
                TopAppBar(
                    title = { Text("Innovation Wall") },
                    navigationIcon = {
                        IconButton(onClick = { scope.launch { drawerState.open() } }) {
                            Icon(Icons.Filled.Menu, contentDescription = "Menu")
                        }
                    },
                    colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.Transparent)
                )
            },
            containerColor = MaterialTheme.colorScheme.background
        ) { paddingValues ->
            Column(modifier = Modifier.padding(paddingValues).fillMaxSize()) {
                if (uiState.isLoading) {
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        CircularProgressIndicator(color = LexNovaPurple)
                    }
                } else {
                    val categories = listOf("All", "Legal Technology", "Community Projects", "Research", "Startups", "Moot Court Innovations")
                    
                    ScrollableTabRow(
                        selectedTabIndex = categories.indexOf(activeCategory).takeIf { it >= 0 } ?: 0,
                        edgePadding = 16.dp,
                        containerColor = Color.Transparent,
                        divider = {}
                    ) {
                        categories.forEach { tab ->
                            val isSelected = activeCategory == tab
                            Tab(
                                selected = isSelected,
                                onClick = { activeCategory = tab },
                                text = { 
                                    Text(
                                        text = tab, 
                                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                                    ) 
                                }
                            )
                        }
                    }
                    
                    val filteredProjects = if (activeCategory == "All") {
                        uiState.innovationProjects
                    } else {
                        uiState.innovationProjects.filter { it.category == activeCategory }
                    }
                    
                    LazyVerticalGrid(
                        columns = GridCells.Adaptive(minSize = 300.dp),
                        contentPadding = PaddingValues(16.dp),
                        horizontalArrangement = Arrangement.spacedBy(16.dp),
                        verticalArrangement = Arrangement.spacedBy(16.dp),
                        modifier = Modifier.fillMaxSize()
                    ) {
                        items(filteredProjects) { project ->
                            InnovationProjectCard(project)
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun InnovationProjectCard(project: InnovationProject) {
    val gradientBrush = when (project.category) {
        "Legal Technology" -> Brush.linearGradient(listOf(Color(0xFF3B82F6).copy(alpha = 0.8f), Color(0xFF2563EB).copy(alpha = 0.8f)))
        "Community Projects" -> Brush.linearGradient(listOf(Color(0xFF10B981).copy(alpha = 0.8f), Color(0xFF059669).copy(alpha = 0.8f)))
        "Research" -> Brush.linearGradient(listOf(Color(0xFFF59E0B).copy(alpha = 0.8f), Color(0xFFD97706).copy(alpha = 0.8f)))
        "Startups" -> Brush.linearGradient(listOf(Color(0xFF8B5CF6).copy(alpha = 0.8f), Color(0xFF6D28D9).copy(alpha = 0.8f)))
        else -> Brush.linearGradient(listOf(Color(0xFF64748B).copy(alpha = 0.8f), Color.DarkGray.copy(alpha = 0.8f)))
    }

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Column(modifier = Modifier.background(gradientBrush).padding(20.dp)) {
            Row(horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically, modifier = Modifier.fillMaxWidth()) {
                Surface(
                    color = Color.White.copy(alpha = 0.2f),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Text(text = project.category, modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp), style = MaterialTheme.typography.labelSmall.copy(color = Color.White, fontWeight = FontWeight.Bold))
                }
                Icon(Icons.Filled.Lightbulb, contentDescription = null, tint = Color.White)
            }
            Spacer(modifier = Modifier.height(16.dp))
            Text(text = project.title, style = MaterialTheme.typography.titleLarge.copy(color = Color.White, fontWeight = FontWeight.Bold))
            Spacer(modifier = Modifier.height(8.dp))
            Text(text = project.abstractText, style = MaterialTheme.typography.bodyMedium.copy(color = Color.White.copy(alpha = 0.9f)))
            Spacer(modifier = Modifier.height(16.dp))
            Row(horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically, modifier = Modifier.fillMaxWidth()) {
                Text(text = "Mentor: ${project.mentor}", style = MaterialTheme.typography.labelMedium.copy(color = Color.White.copy(alpha = 0.7f)))
                
                Row(horizontalArrangement = Arrangement.spacedBy(16.dp), verticalAlignment = Alignment.CenterVertically) {
                    var isLiked by remember { mutableStateOf(false) }
                    var likesCount by remember { mutableStateOf((10..50).random()) }
                    
                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(4.dp), modifier = Modifier.clickable { 
                        isLiked = !isLiked
                        if (isLiked) likesCount++ else likesCount--
                    }) {
                        Icon(if (isLiked) Icons.Filled.Favorite else Icons.Filled.FavoriteBorder, contentDescription = "Like", tint = if (isLiked) Color.Red else Color.White, modifier = Modifier.size(20.dp))
                        Text(likesCount.toString(), color = Color.White)
                    }
                    
                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                        Icon(Icons.Filled.ChatBubbleOutline, contentDescription = "Comment", tint = Color.White, modifier = Modifier.size(20.dp))
                        Text(((5..20).random()).toString(), color = Color.White)
                    }
                }
            }
        }
    }
}
