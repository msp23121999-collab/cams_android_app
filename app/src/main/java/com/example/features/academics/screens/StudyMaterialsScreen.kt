package com.example.features.academics.screens

import android.content.Intent
import android.net.Uri
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.LibraryBooks
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.core.navigation.AppRoutes
import com.example.core.theme.*
import com.example.core.ui.CamsCard
import com.example.core.ui.CamsScreen
import com.example.core.ui.shimmerEffect
import com.example.features.academics.models.StudyMaterial
import com.example.features.student.providers.StudyMaterialsViewModel
import com.example.features.student.widgets.StudentDrawer
import kotlinx.coroutines.launch
import androidx.paging.compose.collectAsLazyPagingItems
import androidx.paging.LoadState

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun StudyMaterialsScreen(
    viewModel: StudyMaterialsViewModel,
    onNavigate: (String) -> Unit
) {
    val uiState by viewModel.uiState.collectAsState()
    var selectedCategory by remember { mutableStateOf("All") }
    var searchQuery by remember { mutableStateOf("") }

    val categories = listOf("All", "Core Materials", "Reference Books", "Case Studies", "Assignments")

    CamsScreen(
        scrollable = false,
        title = "Study Materials",
        subtitle = "Access your academic resources",
        onBackClick = { onNavigate(AppRoutes.STUDENT_DASHBOARD) }
    ) {
        if (uiState.isLoading) {
            MaterialsSkeleton()
        } else if (uiState.error != null) {
            com.example.core.ui.NetworkErrorView(
                message = uiState.error!!,
                onRetry = { viewModel.fetchMaterials() },
                modifier = Modifier.fillMaxSize()
            )
        } else {
            Column(
                modifier = Modifier.fillMaxSize()
            ) {
                // Search and Filters in a Card
                CamsCard(modifier = Modifier.padding(bottom = 16.dp)) {
                    OutlinedTextField(
                        value = searchQuery,
                        onValueChange = { searchQuery = it },
                        modifier = Modifier
                            .fillMaxWidth(),
                        placeholder = { Text("Search title, subject or faculty...") },
                        leadingIcon = { Icon(Icons.Filled.Search, contentDescription = null, tint = CamsNavy) },
                        shape = RoundedCornerShape(12.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = CamsNavy,
                            unfocusedBorderColor = Color.LightGray.copy(alpha = 0.5f)
                        ),
                        singleLine = true
                    )

                    Spacer(Modifier.height(12.dp))

                    LazyRow(
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        items(categories) { category ->
                            FilterChip(
                                selected = selectedCategory == category,
                                onClick = { selectedCategory = category },
                                label = { Text(category, fontSize = 12.sp) },
                                shape = RoundedCornerShape(8.dp),
                                colors = FilterChipDefaults.filterChipColors(
                                    selectedContainerColor = CamsNavy,
                                    selectedLabelColor = Color.White,
                                    containerColor = CamsBackground
                                ),
                                border = FilterChipDefaults.filterChipBorder(
                                    enabled = true,
                                    selected = selectedCategory == category,
                                    borderColor = Color.LightGray.copy(alpha = 0.2f),
                                    selectedBorderColor = CamsNavy,
                                    borderWidth = 1.dp
                                )
                            )
                        }
                    }
                }

                val pagingItems = viewModel.materialsPagingFlow.collectAsLazyPagingItems()

                if (pagingItems.loadState.refresh is LoadState.Loading) {
                    MaterialsSkeleton()
                } else if (pagingItems.itemCount == 0 && pagingItems.loadState.append.endOfPaginationReached) {
                    EmptyMaterialsState()
                } else {
                    LazyColumn(
                        modifier = Modifier.fillMaxSize(),
                        verticalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        items(pagingItems.itemCount) { index ->
                            val material = pagingItems[index]
                            if (material != null) {
                                val matchesCategory = selectedCategory == "All" || material.category == selectedCategory
                                val matchesSearch = searchQuery.isEmpty() ||
                                        material.title.contains(searchQuery, ignoreCase = true) || 
                                        material.subject.contains(searchQuery, ignoreCase = true) ||
                                        material.facultyName.contains(searchQuery, ignoreCase = true)
                                        
                                if (matchesCategory && matchesSearch) {
                                    MaterialCard(material)
                                }
                            }
                        }
                        item { Spacer(Modifier.height(20.dp)) }
                    }
                }
            }
        }
    }
}

@Composable
fun MaterialCard(material: StudyMaterial) {
    val context = LocalContext.current
    CamsCard(
        modifier = Modifier.fillMaxWidth(),
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Top
            ) {
                Row(modifier = Modifier.weight(1f), horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                    Surface(
                        shape = RoundedCornerShape(12.dp),
                        color = (if (material.isVideo) Color(0xFFEEF2FF) else Color(0xFFECFDF5)),
                        modifier = Modifier.size(48.dp)
                    ) {
                        Icon(
                            imageVector = if (material.isVideo) Icons.Filled.PlayCircle else Icons.Filled.Article,
                            contentDescription = null,
                            tint = if (material.isVideo) Color(0xFF6366F1) else Color(0xFF10B981),
                            modifier = Modifier.padding(12.dp)
                        )
                    }
                    Column {
                        Text(
                            text = material.title,
                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold, fontSize = 16.sp),
                            color = CamsTextPrimary,
                            maxLines = 2,
                            overflow = TextOverflow.Ellipsis
                        )
                        Text(
                            text = material.subject,
                            style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                            color = CamsNavy,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                }
                
                IconButton(
                    onClick = {
                        val intent = Intent(Intent.ACTION_VIEW, Uri.parse("https://example.com/download"))
                        context.startActivity(intent)
                    },
                    modifier = Modifier
                        .size(36.dp)
                        .background(CamsBackground, CircleShape)
                ) {
                    Icon(Icons.Filled.Download, contentDescription = "Download", tint = CamsNavy, modifier = Modifier.size(18.dp))
                }
            }

            Spacer(modifier = Modifier.height(16.dp))
            
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                        Icon(Icons.Filled.Person, contentDescription = null, modifier = Modifier.size(12.dp), tint = CamsTextSecondary)
                        Text(material.facultyName, style = MaterialTheme.typography.labelSmall, color = CamsTextSecondary)
                    }
                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                        Icon(Icons.Filled.Schedule, contentDescription = null, modifier = Modifier.size(12.dp), tint = CamsTextSecondary)
                        Text(material.uploadDate, style = MaterialTheme.typography.labelSmall, color = CamsTextSecondary)
                    }
                }

                Surface(
                    color = CamsBackground,
                    shape = RoundedCornerShape(4.dp),
                    border = BorderStroke(1.dp, Color.LightGray.copy(alpha = 0.2f))
                ) {
                    Text(
                        text = material.category.uppercase(),
                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                        style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Black, fontSize = 12.sp),
                        color = CamsTextSecondary
                    )
                }
            }
            
            if (material.attachments.isNotEmpty()) {
                Spacer(modifier = Modifier.height(12.dp))
                HorizontalDivider(color = Color.LightGray.copy(alpha = 0.1f))
                Spacer(modifier = Modifier.height(12.dp))
                LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    items(material.attachments) { attachment ->
                        Surface(
                            modifier = Modifier.clickable { },
                            shape = RoundedCornerShape(8.dp),
                            color = CamsBackground,
                            border = BorderStroke(1.dp, Color.LightGray.copy(alpha = 0.2f))
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(4.dp)
                            ) {
                                Icon(Icons.Filled.AttachFile, contentDescription = null, modifier = Modifier.size(10.dp), tint = CamsNavy)
                                Text(attachment.name, style = MaterialTheme.typography.labelSmall.copy(fontSize = 12.sp), color = CamsTextPrimary)
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun EmptyMaterialsState() {
    Column(
        modifier = Modifier.fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Icon(
            imageVector = Icons.Filled.LibraryBooks,
            contentDescription = null,
            modifier = Modifier.size(64.dp),
            tint = Color(0xFFCBD5E1)
        )
        Spacer(modifier = Modifier.height(16.dp))
        Text(
            "No materials found",
            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Text(
            "Try adjusting your filters or search query",
            style = MaterialTheme.typography.bodySmall,
            color = Color(0xFF94A3B8)
        )
    }
}

@Composable
private fun MaterialsSkeleton() {
    Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(16.dp)) {
        repeat(4) {
            Box(Modifier.fillMaxWidth().height(180.dp).shimmerEffect().clip(RoundedCornerShape(24.dp)))
        }
    }
}
