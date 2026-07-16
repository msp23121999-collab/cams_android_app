package com.example.features.student.screens

import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.core.theme.*
import com.example.core.ui.CamsScreen
import com.example.core.ui.CamsCard
import com.example.features.student.widgets.StudentDrawer
import com.example.features.student.widgets.StudentBaseScreen
import com.example.core.ui.shimmerEffect
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun StudentAcademicsScreen(
    onNavigate: (String) -> Unit,
    viewModel: com.example.features.student.providers.AcademicsViewModel = androidx.lifecycle.viewmodel.compose.viewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val drawerState = rememberDrawerState(initialValue = DrawerValue.Closed)
    val scope = rememberCoroutineScope()
    var activeTab by remember { mutableStateOf("timetable") }

    StudentBaseScreen(
        title = "Academics",
        subtitle = "View your class timetable and enrolled subject details",
        currentRoute = "/student/academics",
        onNavigate = onNavigate,
        scrollable = false
    ) {
        if (uiState.isLoading) {
            Column(
                modifier = Modifier.fillMaxSize().padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Box(modifier = Modifier.fillMaxWidth().height(60.dp).shimmerEffect())
                Box(modifier = Modifier.fillMaxWidth().height(400.dp).shimmerEffect())
            }
        } else if (uiState.error != null) {
            com.example.core.ui.NetworkErrorView(
                message = uiState.error!!,
                onRetry = { viewModel.fetchAcademicsData() },
                modifier = Modifier.fillMaxSize()
            )
        } else {
            // Tab Switcher
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp)
                    .background(MaterialTheme.colorScheme.surface, RoundedCornerShape(16.dp))
                    .border(1.dp, Color.LightGray.copy(alpha = 0.3f), RoundedCornerShape(16.dp))
                    .padding(4.dp)
            ) {
                TabButton(
                    text = "Timetable",
                    icon = Icons.Filled.Schedule,
                    isSelected = activeTab == "timetable",
                    onClick = { activeTab = "timetable" },
                    modifier = Modifier.weight(1f)
                )
                TabButton(
                    text = "Subjects",
                    icon = Icons.Filled.School,
                    isSelected = activeTab == "academics",
                    onClick = { activeTab = "academics" },
                    modifier = Modifier.weight(1f)
                )
            }
            
            if (activeTab == "timetable") {
                TimetableContent(uiState.timetable)
            } else {
                SubjectsContent()
            }
        }
    }
}

@Composable
private fun TabButton(text: String, icon: androidx.compose.ui.graphics.vector.ImageVector, isSelected: Boolean, onClick: () -> Unit, modifier: Modifier) {
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(12.dp))
            .background(if (isSelected) LexNovaPurple else Color.Transparent)
            .clickable { onClick() }
            .padding(vertical = 12.dp),
        contentAlignment = Alignment.Center
    ) {
        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.Center) {
            Icon(icon, contentDescription = null, tint = if (isSelected) Color.White else CamsTextSecondary, modifier = Modifier.size(16.dp))
            Spacer(Modifier.width(8.dp))
            Text(
                text,
                color = if (isSelected) Color.White else CamsTextSecondary,
                fontWeight = FontWeight.Bold,
                fontSize = 14.sp
            )
        }
    }
}

@Composable
private fun TimetableContent(timetable: List<com.example.core.network.TimetableSlotDto>) {
    val days = listOf("Mon", "Tue", "Wed", "Thu", "Fri")
    val periods = listOf(
        "09:00 - 10:00",
        "10:00 - 11:00",
        "11:15 - 12:15",
        "12:15 - 13:15",
        "14:00 - 15:00",
        "15:00 - 16:00"
    )

    Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text("Weekly Class Schedule", style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold), maxLines = 1, overflow = TextOverflow.Ellipsis)
                Text("Academic Year 2025-26", style = MaterialTheme.typography.labelSmall.copy(color = MaterialTheme.colorScheme.onSurfaceVariant))
            }
            Spacer(Modifier.width(8.dp))
            Surface(
                color = Color(0xFF10B981).copy(alpha = 0.1f),
                shape = RoundedCornerShape(16.dp),
                border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFF10B981).copy(alpha = 0.2f))
            ) {
                Text("APPROVED", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = Color(0xFF10B981), modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp))
            }
        }
        
        Card(
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            border = androidx.compose.foundation.BorderStroke(1.dp, Color.LightGray.copy(alpha = 0.3f)),
            shape = RoundedCornerShape(16.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.horizontalScroll(rememberScrollState())) {
                Row(modifier = Modifier.background(CamsBackground).padding(vertical = 12.dp)) {
                    Box(modifier = Modifier.width(60.dp).padding(horizontal = 12.dp)) {
                        Text("Day", fontWeight = FontWeight.Bold, fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                    periods.forEach { period ->
                        Box(modifier = Modifier.width(120.dp).padding(horizontal = 8.dp), contentAlignment = Alignment.Center) {
                            Text(period, fontWeight = FontWeight.Bold, fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                    }
                }
                
                days.forEachIndexed { index, day ->
                    val dayFullName = when(day) {
                        "Mon" -> "Monday"
                        "Tue" -> "Tuesday"
                        "Wed" -> "Wednesday"
                        "Thu" -> "Thursday"
                        "Fri" -> "Friday"
                        else -> day
                    }
                    HorizontalDivider(color = Color.LightGray.copy(alpha = 0.3f))
                    Row(modifier = Modifier.padding(vertical = 12.dp), verticalAlignment = Alignment.CenterVertically) {
                        Box(modifier = Modifier.width(60.dp).padding(horizontal = 12.dp)) {
                            Text(day, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                        }
                        periods.forEachIndexed { pIndex, period ->
                            val slot = timetable.find { it.dayOfWeek == dayFullName && it.startTime == period }
                            Box(modifier = Modifier.width(120.dp).padding(horizontal = 8.dp)) {
                                if (slot != null) {
                                    Surface(
                                        color = LexNovaPurple.copy(alpha = 0.1f),
                                        shape = RoundedCornerShape(8.dp),
                                        modifier = Modifier.fillMaxWidth().height(80.dp)
                                    ) {
                                        Column(
                                            modifier = Modifier.padding(8.dp),
                                            verticalArrangement = Arrangement.SpaceBetween
                                        ) {
                                            Text(slot.subjectName, fontWeight = FontWeight.Bold, fontSize = 12.sp, color = LexNovaPurple, maxLines = 2, overflow = TextOverflow.Ellipsis)
                                            Text(slot.roomNo, fontSize = 12.sp, color = LexNovaPurple.copy(alpha = 0.8f), maxLines = 1, overflow = TextOverflow.Ellipsis)
                                        }
                                    }
                                } else {
                                    Box(
                                        modifier = Modifier.fillMaxWidth().height(80.dp).border(1.dp, Color.LightGray.copy(alpha = 0.3f), RoundedCornerShape(8.dp)),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Text("FREE", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f))
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun SubjectsContent() {
    Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
        // Summary Cards
        Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            StatCard("Total Subjects", "6", Icons.Filled.Book, LexNovaPurple, Modifier.weight(1f))
            StatCard("Total Credits", "24", Icons.Filled.Star, Color(0xFFF59E0B), Modifier.weight(1f))
        }
        
        Card(
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            border = androidx.compose.foundation.BorderStroke(1.dp, Color.LightGray.copy(alpha = 0.3f)),
            shape = RoundedCornerShape(16.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text("Enrolled Subjects", style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold))
                Spacer(Modifier.height(16.dp))
                
                val subjects = listOf(
                    "LAW101 - Introduction to Law & Legal System" to "4 Cr",
                    "BALL8105 - Criminal Law" to "4 Cr",
                    "LAW205 - Law of Contracts" to "4 Cr",
                    "LAW301 - Jurisprudence" to "3 Cr"
                )
                
                subjects.forEachIndexed { index, (name, credits) ->
                    if (index > 0) HorizontalDivider(color = Color.LightGray.copy(alpha = 0.3f), modifier = Modifier.padding(vertical = 12.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.weight(1f)) {
                            Box(modifier = Modifier.size(32.dp).background(LexNovaPurple.copy(alpha = 0.1f), CircleShape), contentAlignment = Alignment.Center) {
                                Text("${index+1}", color = LexNovaPurple, fontWeight = FontWeight.Bold)
                            }
                            Spacer(Modifier.width(12.dp))
                            Text(name, fontWeight = FontWeight.Bold, fontSize = 14.sp, maxLines = 2, overflow = TextOverflow.Ellipsis)
                        }
                        Spacer(Modifier.width(8.dp))
                        Surface(
                            color = CamsBackground,
                            shape = RoundedCornerShape(8.dp)
                        ) {
                            Text(credits, fontWeight = FontWeight.Bold, fontSize = 12.sp, modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp))
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun StatCard(title: String, value: String, icon: androidx.compose.ui.graphics.vector.ImageVector, color: Color, modifier: Modifier) {
    Card(
        modifier = modifier,
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        border = androidx.compose.foundation.BorderStroke(1.dp, Color.LightGray.copy(alpha = 0.3f)),
        shape = RoundedCornerShape(16.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(horizontalArrangement = Arrangement.SpaceBetween, modifier = Modifier.fillMaxWidth()) {
                Text(title, style = MaterialTheme.typography.labelSmall.copy(color = MaterialTheme.colorScheme.onSurfaceVariant, fontWeight = FontWeight.Bold))
                Icon(icon, contentDescription = null, tint = color, modifier = Modifier.size(16.dp))
            }
            Spacer(Modifier.height(8.dp))
            Text(value, style = MaterialTheme.typography.headlineMedium.copy(fontWeight = FontWeight.Black))
        }
    }
}
