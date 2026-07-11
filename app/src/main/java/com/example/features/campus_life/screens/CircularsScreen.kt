package com.example.features.campus_life.screens

import android.content.Intent
import android.net.Uri
import androidx.compose.animation.*
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowForward
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.core.navigation.AppRoutes
import com.example.core.theme.*
import com.example.core.ui.CamsCard
import com.example.core.ui.CamsScreen
import com.example.core.ui.shimmerEffect
import com.example.features.campus_life.models.CircularNotice
import com.example.features.campus_life.providers.CircularsViewModel
import com.example.features.student.widgets.StudentDrawer
import com.example.core.theme.*
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CircularsScreen(
    viewModel: CircularsViewModel = viewModel(),
    onNavigate: (String) -> Unit
) {
    val uiState by viewModel.uiState.collectAsState()
    var selectedNotice by remember { mutableStateOf<CircularNotice?>(null) }

    val categories = listOf(
        "ALL", "Academic Announcement", "Examination Notice",
        "Department Circular", "Event Notification", "General Information"
    )
    val priorities = listOf("ALL", "HIGH", "MEDIUM", "LOW")

    CamsScreen(
        scrollable = true,
        title = "Circulars",
        subtitle = "Department Notices & Bulletins",
        onBackClick = { onNavigate(AppRoutes.STUDENT_DASHBOARD) }
    ) {
        Column(
            modifier = Modifier.fillMaxSize(),
            verticalArrangement = Arrangement.spacedBy(24.dp)
        ) {
            // Filters Toolbar
            CamsCard {
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    TextField(
                        value = uiState.searchQuery,
                        onValueChange = { viewModel.updateSearchQuery(it) },
                        placeholder = { Text("Search circulars...", fontSize = 14.sp) },
                        leadingIcon = { Icon(Icons.Filled.Search, contentDescription = null, modifier = Modifier.size(20.dp), tint = CamsTextSecondary) },
                        modifier = Modifier.fillMaxWidth(),
                        colors = TextFieldDefaults.colors(
                            focusedContainerColor = Color.Transparent,
                            unfocusedContainerColor = Color.Transparent,
                            focusedIndicatorColor = CamsNavy,
                        ),
                        shape = RoundedCornerShape(12.dp),
                        singleLine = true
                    )

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        FilterDropdown(
                            label = "Category",
                            options = categories,
                            selectedOption = uiState.selectedCategory,
                            onOptionSelected = { viewModel.updateCategory(it) },
                            modifier = Modifier.weight(1.5f)
                        )
                        FilterDropdown(
                            label = "Priority",
                            options = priorities,
                            selectedOption = uiState.selectedPriority,
                            onOptionSelected = { viewModel.updatePriority(it) },
                            modifier = Modifier.weight(1f)
                        )
                    }
                }
            }

            if (uiState.isLoading) {
                CircularsGridSkeleton()
            } else if (uiState.filteredNotices.isEmpty()) {
                EmptyState(onRetry = { viewModel.fetchCirculars() })
            } else {
                uiState.filteredNotices.forEach { notice ->
                    NoticeCard(notice = notice, onClick = { selectedNotice = notice })
                }
            }
            
            Spacer(Modifier.height(20.dp))
        }
    }

    if (selectedNotice != null) {
        NoticeDetailsDialog(notice = selectedNotice!!, onDismiss = { selectedNotice = null })
    }
}

@Composable
private fun FilterDropdown(
    label: String,
    options: List<String>,
    selectedOption: String,
    onOptionSelected: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    var expanded by remember { mutableStateOf(false) }

    Box(modifier = modifier) {
        Surface(
            onClick = { expanded = true },
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(12.dp),
            border = BorderStroke(1.dp, Color.LightGray.copy(alpha = 0.5f)),
            color = Color.White
        ) {
            Row(
                modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column {
                    Text(label.uppercase(), style = MaterialTheme.typography.labelSmall.copy(fontSize = 12.sp, fontWeight = FontWeight.Black), color = CamsTextSecondary)
                    Text(
                        if (selectedOption == "ALL") "All ${label}s" else selectedOption,
                        style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Bold),
                        color = CamsTextPrimary,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
                Icon(Icons.Filled.ArrowDropDown, contentDescription = null, tint = CamsTextSecondary)
            }
        }

        DropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false },
            modifier = Modifier.background(MaterialTheme.colorScheme.surface)
        ) {
            options.forEach { option ->
                DropdownMenuItem(
                    text = { Text(option, style = MaterialTheme.typography.bodySmall) },
                    onClick = {
                        onOptionSelected(option)
                        expanded = false
                    }
                )
            }
        }
    }
}

@Composable
private fun NoticeCard(notice: CircularNotice, onClick: () -> Unit) {
    val isHigh = notice.priority.equals("HIGH", ignoreCase = true)

    CamsCard(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() },
    ) {
        Column {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Surface(
                    color = CamsNavy.copy(alpha = 0.1f),
                    shape = RoundedCornerShape(4.dp)
                ) {
                    Text(notice.category.uppercase(), modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp), style = MaterialTheme.typography.labelSmall.copy(fontSize = 12.sp, fontWeight = FontWeight.Black), color = CamsNavy)
                }
                
                val priorityColor = when(notice.priority.uppercase()) {
                    "HIGH" -> Color(0xFFE11D48)
                    "LOW" -> Color(0xFF64748B)
                    else -> Color(0xFFD97706)
                }

                Surface(
                    color = priorityColor.copy(alpha = 0.1f),
                    shape = RoundedCornerShape(4.dp),
                ) {
                    Text(
                        notice.priority.uppercase(),
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp),
                        style = MaterialTheme.typography.labelSmall.copy(fontSize = 12.sp, fontWeight = FontWeight.Black),
                        color = priorityColor
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))
            Text(
                notice.title,
                style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Black, lineHeight = 18.sp, fontSize = 16.sp),
                color = CamsTextPrimary,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                notice.body,
                style = MaterialTheme.typography.bodySmall,
                color = CamsTextSecondary,
                maxLines = 3,
                overflow = TextOverflow.Ellipsis,
                lineHeight = 16.sp
            )

            Spacer(modifier = Modifier.height(20.dp))
            HorizontalDivider(color = Color.LightGray.copy(alpha = 0.1f))
            Spacer(modifier = Modifier.height(12.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                    Icon(Icons.Filled.CalendarToday, contentDescription = null, modifier = Modifier.size(12.dp), tint = CamsTextSecondary)
                    Text(notice.publishDate, style = MaterialTheme.typography.labelSmall, color = CamsTextSecondary)
                }
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                    Text("Read More", style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Black), color = CamsNavy)
                    Icon(Icons.Filled.ArrowForward, contentDescription = null, modifier = Modifier.size(12.dp), tint = CamsNavy)
                }
            }
        }
    }
}

@Composable
private fun NoticeDetailsDialog(notice: CircularNotice, onDismiss: () -> Unit) {
    val context = LocalContext.current
    val isHigh = notice.priority.equals("HIGH", ignoreCase = true)

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .fillMaxHeight(0.9f)
                .padding(16.dp),
            shape = RoundedCornerShape(32.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
        ) {
            Column(modifier = Modifier.fillMaxSize()) {
                // Header
                Row(
                    modifier = Modifier.padding(24.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    Surface(
                        color = if (isHigh) Rose600.copy(alpha = 0.1f) else Indigo600.copy(alpha = 0.1f),
                        shape = RoundedCornerShape(16.dp),
                        modifier = Modifier.size(48.dp)
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Icon(Icons.Filled.Notifications, contentDescription = null, tint = if (isHigh) Rose600 else Indigo600)
                        }
                    }
                    Column(modifier = Modifier.weight(1f)) {
                        Text(notice.title, style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Black), color = Slate900)
                        Text(notice.category.uppercase(), style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Black, letterSpacing = 1.sp), color = Slate400)
                    }
                    IconButton(onClick = onDismiss, modifier = Modifier.background(Zinc50, CircleShape).border(1.dp, Zinc200, CircleShape).size(32.dp)) {
                        Icon(Icons.Filled.Close, contentDescription = "Close", modifier = Modifier.size(16.dp), tint = Slate600)
                    }
                }

                HorizontalDivider(color = Zinc200.copy(alpha = 0.5f))

                // Content
                Column(modifier = Modifier.weight(1f).verticalScroll(rememberScrollState()).padding(24.dp)) {
                    // Info Bar
                    Surface(
                        color = Zinc50,
                        shape = RoundedCornerShape(20.dp),
                        border = BorderStroke(1.dp, Zinc200.copy(alpha = 0.5f))
                    ) {
                        Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                            InfoRow(Icons.Filled.Person, "Posted By", "${notice.publisherName}${if (notice.publisherRole != null) " (${notice.publisherRole})" else ""}")
                            InfoRow(Icons.Filled.CalendarToday, "Publish Date", notice.publishDate)
                            notice.expiryDate?.let { InfoRow(Icons.Filled.EventBusy, "Expires", it, Rose600) }
                            InfoRow(Icons.Filled.Groups, "Audience", notice.audienceType)
                        }
                    }

                    Spacer(modifier = Modifier.height(24.dp))
                    Text("NOTICE DETAILS", style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Black, letterSpacing = 1.sp), color = Slate400)
                    Spacer(modifier = Modifier.height(12.dp))
                    Text(notice.body, style = MaterialTheme.typography.bodyMedium.copy(lineHeight = 22.sp), color = Slate900)

                    if (notice.attachmentUrl != null) {
                        Spacer(modifier = Modifier.height(32.dp))
                        Text("ATTACHMENT", style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Black, letterSpacing = 1.sp), color = Slate400)
                        Spacer(modifier = Modifier.height(12.dp))
                        
                        val isPdf = notice.attachmentUrl.endsWith(".pdf", ignoreCase = true)
                        
                        Card(
                            modifier = Modifier.fillMaxWidth().clickable {
                                val intent = Intent(Intent.ACTION_VIEW, Uri.parse(notice.attachmentUrl))
                                context.startActivity(intent)
                            },
                            shape = RoundedCornerShape(20.dp),
                            border = BorderStroke(1.dp, Zinc200),
                            colors = CardDefaults.cardColors(containerColor = Zinc50)
                        ) {
                            Row(modifier = Modifier.padding(12.dp), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                                Surface(
                                    color = if (isPdf) Rose600.copy(alpha = 0.1f) else Indigo600.copy(alpha = 0.1f),
                                    shape = RoundedCornerShape(12.dp),
                                    modifier = Modifier.size(40.dp)
                                ) {
                                    Box(contentAlignment = Alignment.Center) {
                                        Text(if (isPdf) "PDF" else "IMG", style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Black), color = if (isPdf) Rose600 else Indigo600)
                                    }
                                }
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(notice.attachmentUrl.substringAfterLast("/"), style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold), color = Slate900, maxLines = 1, overflow = TextOverflow.Ellipsis)
                                    Text("Click to view attachment", style = MaterialTheme.typography.labelSmall, color = Slate400)
                                }
                            }
                        }
                    }
                }

                // Footer
                Box(modifier = Modifier.padding(24.dp)) {
                    Button(
                        onClick = onDismiss,
                        modifier = Modifier.fillMaxWidth().height(48.dp),
                        shape = RoundedCornerShape(16.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = Slate900)
                    ) {
                        Text("Close Notice", fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}

@Composable
private fun InfoRow(icon: ImageVector, label: String, value: String, valueColor: Color = Slate900) {
    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        Icon(icon, contentDescription = null, modifier = Modifier.size(14.dp), tint = Slate400)
        Text("${label}:", style = MaterialTheme.typography.labelSmall, color = Slate400)
        Text(value, style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Black), color = valueColor)
    }
}

@Composable
private fun EmptyState(onRetry: (() -> Unit)? = null) {
    Column(
        modifier = Modifier.fillMaxSize().padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Surface(color = Zinc50, shape = RoundedCornerShape(24.dp), modifier = Modifier.size(80.dp), border = BorderStroke(1.dp, Zinc200)) {
            Box(contentAlignment = Alignment.Center) {
                Icon(Icons.Filled.NotificationsNone, contentDescription = null, modifier = Modifier.size(32.dp), tint = Slate400)
            }
        }
        Spacer(modifier = Modifier.height(24.dp))
        Text("No Circulars Found", style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Black), color = Slate900)
        Text("There are no circulars matching your search or filters at the moment.", style = MaterialTheme.typography.bodySmall, color = Slate600, textAlign = androidx.compose.ui.text.style.TextAlign.Center)
        if (onRetry != null) {
            Spacer(modifier = Modifier.height(16.dp))
            Button(onClick = onRetry) {
                Text("Retry")
            }
        }
    }
}

@Composable
private fun CircularsGridSkeleton() {
    Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        repeat(3) {
            Box(Modifier.fillMaxWidth().height(200.dp).shimmerEffect().clip(RoundedCornerShape(20.dp)))
        }
    }
}

@Composable
private fun Icon(icon: ImageVector, contentDescription: String?, size: androidx.compose.ui.unit.Dp, tint: Color) {
    Icon(icon, contentDescription, modifier = Modifier.size(size), tint = tint)
}
