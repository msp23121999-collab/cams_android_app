package com.example.features.campus_life.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.items
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
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import coil.compose.AsyncImage
import com.example.core.theme.LexNovaPurple
import com.example.core.navigation.AppRoutes
import com.example.core.theme.*
import com.example.core.ui.CamsCard
import com.example.core.ui.CamsScreen
import com.example.features.campus_life.models.*
import com.example.features.campus_life.providers.CampusLifeViewModel
import com.example.features.student.widgets.StudentDrawer
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CampusLifeDashboardScreen(
    viewModel: CampusLifeViewModel = viewModel(),
    onNavigate: (String) -> Unit
) {
    val uiState by viewModel.uiState.collectAsState()

    CamsScreen(scrollable = true,
        title = "Campus Life",
        subtitle = "Connect, Collaborate and Contribute",
        onBackClick = { onNavigate(AppRoutes.STUDENT_DASHBOARD) },
        verticalArrangement = Arrangement.spacedBy(24.dp)
    ) {
        // Header Description
        CamsCard {
            Column(modifier = Modifier.fillMaxWidth()) {
                Text(
                    "Legal network hub. Discover events, research, and connect with societies.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = CamsTextSecondary,
                    lineHeight = 20.sp
                )
                Spacer(modifier = Modifier.height(16.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    Button(
                        onClick = { },
                        colors = ButtonDefaults.buttonColors(containerColor = CamsNavy),
                        shape = RoundedCornerShape(12.dp),
                        contentPadding = PaddingValues(horizontal = 20.dp, vertical = 12.dp)
                    ) {
                        Text("Calendar", style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.Bold))
                    }
                    OutlinedButton(
                        onClick = { },
                        shape = RoundedCornerShape(12.dp),
                        border = BorderStroke(1.dp, Color.LightGray.copy(alpha = 0.5f)),
                        colors = ButtonDefaults.outlinedButtonColors(contentColor = CamsTextPrimary),
                        contentPadding = PaddingValues(horizontal = 20.dp, vertical = 12.dp)
                    ) {
                        Text("Registrations", style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.Bold))
                    }
                }
            }
        }

        // Stats Row
        LazyRow(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            items(uiState.stats) { stat ->
                StatCard(stat)
            }
        }

            // Experience Modules
            Column {
                SectionHeader(Icons.Filled.GridView, "Experience Modules")
                Spacer(modifier = Modifier.height(16.dp))
                LazyRow(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    items(uiState.modules) { module ->
                        ExperienceModuleCard(Modifier.width(140.dp), module, onNavigate)
                    }
                }
            }

            // Exclusive Ecosystems (LexNova & Legal Events)
            Column {
                SectionHeader(Icons.Filled.AutoAwesome, "Exclusive Ecosystems", Color(0xFFD4AF37))
                Spacer(modifier = Modifier.height(16.dp))
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                    // LexNova Card
                    Card(
                        modifier = Modifier.weight(1f).height(120.dp).clickable { onNavigate("/student/lexnova") },
                        shape = RoundedCornerShape(24.dp),
                        colors = CardDefaults.cardColors(containerColor = Color(0xFF0B132B)),
                        border = BorderStroke(1.dp, Color(0xFFD4AF37).copy(alpha = 0.5f))
                    ) {
                        Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.Center) {
                            Icon(Icons.Filled.Scale, contentDescription = null, tint = Color(0xFFD4AF37), modifier = Modifier.size(24.dp))
                            Spacer(modifier = Modifier.height(8.dp))
                            Text("LEXNOVA", style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.Black, color = Color.White))
                            Text("AI Dashboard", style = MaterialTheme.typography.labelSmall.copy(fontSize = 12.sp, color = Color.White.copy(alpha = 0.6f)))
                        }
                    }

                    // Legal Events Hub Card
                    CamsCard(
                        modifier = Modifier.weight(1f).height(120.dp).clickable { onNavigate("/student/legal-events") },
                    ) {
                        Column(verticalArrangement = Arrangement.Center) {
                            Icon(Icons.Filled.EventSeat, contentDescription = null, tint = CamsNavy, modifier = Modifier.size(24.dp))
                            Spacer(modifier = Modifier.height(8.dp))
                            Text("LEGAL HUB", style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.Black, color = CamsTextPrimary))
                            Text("Lectures", style = MaterialTheme.typography.labelSmall.copy(fontSize = 12.sp, color = CamsTextSecondary))
                        }
                    }
                }
            }

            // Student Council Portal Highlight
            CamsCard(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { onNavigate("/student/council") },
            ) {
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(20.dp)) {
                    Surface(
                        color = CamsNavy.copy(alpha = 0.1f),
                        shape = RoundedCornerShape(16.dp),
                        modifier = Modifier.size(56.dp),
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Icon(Icons.Filled.Shield, contentDescription = null, tint = CamsNavy, modifier = Modifier.size(28.dp))
                        }
                    }
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            "Student Council",
                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Black),
                            color = CamsTextPrimary
                        )
                        Text(
                            "Your voice on campus hub.",
                            style = MaterialTheme.typography.bodySmall,
                            color = CamsTextSecondary
                        )
                    }
                    Icon(Icons.Filled.ArrowForward, contentDescription = null, tint = CamsNavy)
                }
            }

            // Featured Events
            Column {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    SectionHeader(Icons.Filled.Whatshot, "Featured Events", Color(0xFFF97316))
                    Text(
                        "View All",
                        style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                        color = CamsNavy,
                        modifier = Modifier.clickable { }
                    )
                }
                Spacer(modifier = Modifier.height(16.dp))
                uiState.events.forEach { event ->
                    EventCard(event)
                    Spacer(modifier = Modifier.height(16.dp))
                }
            }

            // Achievements Section
            Column {
                SectionHeader(Icons.Filled.Star, "Recent Achievements", Color(0xFFF59E0B))
                Spacer(modifier = Modifier.height(16.dp))
                CamsCard {
                    Column(verticalArrangement = Arrangement.spacedBy(20.dp)) {
                        uiState.achievements.forEach { achievement ->
                            AchievementItem(achievement)
                        }
                    }
                }
            }
            Spacer(Modifier.height(20.dp))
    }
}

@Composable
private fun SectionHeader(icon: androidx.compose.ui.graphics.vector.ImageVector, title: String, color: Color = CamsNavy) {
    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(12.dp)) {
        Icon(icon, contentDescription = null, tint = color, modifier = Modifier.size(20.dp))
        Text(title.uppercase(), style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Black, letterSpacing = 1.sp), color = CamsTextSecondary)
    }
}

@Composable
private fun StatCard(stat: CampusLifeStat) {
    CamsCard(
        modifier = Modifier
            .width(130.dp)
            .height(100.dp),
    ) {
        Column {
            Box(
                modifier = Modifier
                    .size(32.dp)
                    .clip(RoundedCornerShape(8.dp))
                    .background(CamsNavy.copy(alpha = 0.1f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(stat.icon, contentDescription = null, tint = CamsNavy, modifier = Modifier.size(16.dp))
            }
            Spacer(modifier = Modifier.weight(1f))
            Text(
                stat.value,
                style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Black, fontSize = 20.sp),
                color = CamsTextPrimary
            )
            Text(
                stat.label,
                style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Medium, fontSize = 13.sp),
                color = CamsTextSecondary
            )
        }
    }
}

@Composable
private fun ExperienceModuleCard(modifier: Modifier, module: ExperienceModule, onNavigate: (String) -> Unit) {
    CamsCard(
        modifier = modifier
            .height(140.dp)
            .clickable { onNavigate(module.path) },
    ) {
        Column(verticalArrangement = Arrangement.SpaceBetween, modifier = Modifier.fillMaxHeight()) {
            Surface(
                shape = RoundedCornerShape(12.dp),
                color = CamsBackground,
                modifier = Modifier.size(40.dp)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(module.icon, contentDescription = null, tint = CamsNavy, modifier = Modifier.size(20.dp))
                }
            }
            Column {
                Text(
                    module.title,
                    style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.Black),
                    color = CamsTextPrimary,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Text(
                    module.description,
                    style = MaterialTheme.typography.labelSmall.copy(fontSize = 13.sp),
                    color = CamsTextSecondary,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )
            }
        }
    }
}

@Composable
private fun EventCard(event: CampusLifeEvent) {
    CamsCard(
        modifier = Modifier.fillMaxWidth(),
    ) {
        Column {
            Box(modifier = Modifier.fillMaxWidth().height(160.dp).clip(RoundedCornerShape(12.dp))) {
                AsyncImage(
                    model = event.imageUrl,
                    contentDescription = null,
                    modifier = Modifier.fillMaxSize(),
                    contentScale = ContentScale.Crop
                )
                Surface(
                    modifier = Modifier.padding(12.dp),
                    color = Color.White.copy(alpha = 0.9f),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Text(
                        event.type,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                        style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Black, fontSize = 13.sp),
                        color = Color(0xFF1E293B)
                    )
                }
            }
            Column(modifier = Modifier.padding(vertical = 12.dp)) {
                Text(
                    event.title,
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Black, fontSize = 16.sp),
                    color = CamsTextPrimary,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )
                Spacer(modifier = Modifier.height(12.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    IconText(Icons.Filled.CalendarToday, event.date)
                    IconText(Icons.Filled.Schedule, event.time)
                }
                
                Spacer(modifier = Modifier.height(20.dp))
                
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    if (event.registered) {
                        Surface(
                            modifier = Modifier.weight(1f),
                            color = Color(0xFFECFDF5),
                            shape = RoundedCornerShape(12.dp),
                        ) {
                            Row(
                                modifier = Modifier.padding(vertical = 10.dp),
                                horizontalArrangement = Arrangement.Center,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(Icons.Filled.CheckCircle, contentDescription = null, tint = Color(0xFF059669), modifier = Modifier.size(14.dp))
                                Spacer(modifier = Modifier.width(6.dp))
                                Text("Registered", style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.Black), color = Color(0xFF059669))
                            }
                        }
                    } else {
                        Button(
                            onClick = { },
                            modifier = Modifier.weight(1f),
                            colors = ButtonDefaults.buttonColors(containerColor = CamsNavy),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Text("Register", style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.Black))
                        }
                    }
                    OutlinedButton(
                        onClick = { },
                        modifier = Modifier.weight(0.6f),
                        shape = RoundedCornerShape(12.dp),
                        border = BorderStroke(1.dp, Color.LightGray.copy(alpha = 0.5f)),
                        colors = ButtonDefaults.outlinedButtonColors(contentColor = CamsTextPrimary)
                    ) {
                        Text("Details", style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.Black))
                    }
                }
            }
        }
    }
}

@Composable
private fun IconText(icon: androidx.compose.ui.graphics.vector.ImageVector, text: String) {
    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
        Icon(icon, contentDescription = null, tint = LexNovaPurple, modifier = Modifier.size(14.dp))
        Text(text, style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Medium), color = MaterialTheme.colorScheme.onSurfaceVariant)
    }
}

@Composable
private fun AchievementItem(achievement: Achievement) {
    Row(verticalAlignment = Alignment.Top, horizontalArrangement = Arrangement.spacedBy(16.dp)) {
        Box(
            modifier = Modifier
                .size(44.dp)
                .clip(CircleShape)
                .background(Color(achievement.bgColor))
                .border(1.dp, Color(achievement.color).copy(alpha = 0.2f), CircleShape),
            contentAlignment = Alignment.Center
        ) {
            Icon(achievement.icon, contentDescription = null, tint = Color(achievement.color), modifier = Modifier.size(20.dp))
        }
        Column(modifier = Modifier.weight(1f)) {
            Text(
                achievement.title,
                style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Black),
                color = Color(0xFF1E293B)
            )
            Text(
                achievement.description,
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Text(
                achievement.timeAgo,
                style = MaterialTheme.typography.labelSmall.copy(fontSize = 12.sp, fontWeight = FontWeight.Bold),
                color = Color(0xFF94A3B8),
                modifier = Modifier.padding(top = 4.dp)
            )
        }
    }
}
