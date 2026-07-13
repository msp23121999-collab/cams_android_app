package com.example.features.academics.screens

import androidx.compose.animation.*
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.core.navigation.AppRoutes
import com.example.core.theme.*
import com.example.core.ui.CamsCard
import com.example.core.ui.CamsScreen
import com.example.core.ui.shimmerEffect
import com.example.features.academics.models.AcademicSubject
import com.example.features.academics.models.TimetablePeriod
import com.example.features.academics.providers.TimetableViewModel
import com.example.features.student.widgets.StudentDrawer
import kotlinx.coroutines.launch

import com.example.features.academics.providers.TimetableState

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TimetableScreen(
    viewModel: TimetableViewModel,
    onNavigate: (String) -> Unit
) {
    val uiState by viewModel.uiState.collectAsState()

    CamsScreen(
        scrollable = false,
        title = "Academics",
        onBackClick = { onNavigate(AppRoutes.STUDENT_DASHBOARD) }
    ) {
        // Tab Switcher
        TabSwitcher(uiState.activeTab) { viewModel.setActiveTab(it) }
        
        Spacer(Modifier.height(16.dp))

        // Tab Description
        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            Box(Modifier.weight(1f).height(1.dp).background(Color.LightGray.copy(alpha = 0.2f)))
            Text(
                if (uiState.activeTab == "timetable") "WEEKLY CLASS SCHEDULE" else "SUBJECTS & CREDITS",
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold,
                color = CamsTextSecondary,
                letterSpacing = 1.sp
            )
            Box(Modifier.weight(1f).height(1.dp).background(Color.LightGray.copy(alpha = 0.2f)))
        }

        Spacer(Modifier.height(20.dp))

        if (uiState.isLoading) {
            Crossfade(targetState = uiState.activeTab, label = "loading_fade") { tab ->
                when (tab) {
                    "timetable" -> TimetableSkeleton()
                    "academics" -> AcademicsSkeleton()
                }
            }
        } else {
            Crossfade(targetState = uiState.activeTab, label = "tab_fade") { tab ->
                when (tab) {
                    "timetable" -> TimetableTab(uiState.timetable)
                    "academics" -> AcademicsTab(uiState)
                }
            }
        }
    }
}

@Composable
private fun TabSwitcher(activeTab: String, onTabSelected: (String) -> Unit) {
    Surface(
        color = Color.White,
        shape = RoundedCornerShape(16.dp),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
        modifier = Modifier.wrapContentWidth()
    ) {
        Row(modifier = Modifier.padding(4.dp)) {
            TabItem("timetable", "Timetable", Icons.Filled.Schedule, activeTab == "timetable") { onTabSelected("timetable") }
            TabItem("academics", "Academics", Icons.Filled.School, activeTab == "academics") { onTabSelected("academics") }
        }
    }
}

@Composable
private fun TabItem(id: String, label: String, icon: ImageVector, isSelected: Boolean, onClick: () -> Unit) {
    Surface(
        color = if (isSelected) CamsNavy else Color.Transparent,
        shape = RoundedCornerShape(12.dp),
        modifier = Modifier
            .clickable { onClick() }
            .padding(horizontal = 4.dp)
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 10.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Icon(icon, null, tint = if (isSelected) Color.White else CamsTextSecondary, modifier = Modifier.size(16.dp))
            Text(label, fontWeight = FontWeight.Bold, fontSize = 14.sp, color = if (isSelected) Color.White else CamsTextSecondary)
        }
    }
}

@Composable
private fun TimetableTab(timetable: List<TimetablePeriod>) {
    val days = listOf("MONDAY", "TUESDAY", "WEDNESDAY", "THURSDAY", "FRIDAY")
    val periods = listOf("09:00", "10:00", "11:15", "12:15", "14:00", "15:00")
    
    Column(modifier = Modifier.fillMaxSize()) {
        Text("Weekly Class Schedule", fontWeight = FontWeight.Black, fontSize = 18.sp, color = CamsTextPrimary)
        Text("Academic Year 2025-26 — Room allocations are live", fontSize = 12.sp, color = CamsTextSecondary)
        
        Spacer(Modifier.height(16.dp))
        
        CamsCard(
            modifier = Modifier.fillMaxWidth().weight(1f),
        ) {
            Box(modifier = Modifier.horizontalScroll(rememberScrollState())) {
                Column {
                    // Header Row
                    Row(modifier = Modifier.background(CamsBackground).border(1.dp, Color.LightGray.copy(alpha = 0.2f))) {
                        Box(Modifier.width(80.dp).padding(12.dp), contentAlignment = Alignment.Center) {
                            Text("DAY", fontWeight = FontWeight.Black, fontSize = 12.sp, color = CamsTextSecondary)
                        }
                        periods.forEach { time ->
                            Box(Modifier.width(120.dp).padding(12.dp), contentAlignment = Alignment.Center) {
                                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                    Text("PERIOD", fontWeight = FontWeight.Black, fontSize = 13.sp, color = CamsTextSecondary)
                                    Text(time, fontWeight = FontWeight.Bold, fontSize = 12.sp, color = CamsTextPrimary)
                                }
                            }
                        }
                    }
                    
                    Column(modifier = Modifier.verticalScroll(rememberScrollState())) {
                        days.forEach { day ->
                            Row(modifier = Modifier.border(0.5.dp, Color.LightGray.copy(alpha = 0.2f))) {
                                Box(
                                    Modifier
                                        .width(80.dp)
                                        .height(120.dp)
                                        .background(CamsBackground)
                                        .padding(12.dp),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(day.take(3), fontWeight = FontWeight.Black, fontSize = 12.sp, color = CamsTextPrimary)
                                }
                                
                                periods.forEach { time ->
                                    val slot = timetable.find { it.weekday == day && it.startTime == time }
                                    Box(Modifier.width(120.dp).height(120.dp).padding(4.dp)) {
                                        if (slot != null) {
                                            TimetableSlotCard(slot)
                                        } else {
                                            Box(
                                                Modifier
                                                    .fillMaxSize()
                                                    .border(1.dp, Color.LightGray.copy(alpha = 0.2f), RoundedCornerShape(12.dp)),
                                                contentAlignment = Alignment.Center
                                            ) {
                                                Text("FREE", fontWeight = FontWeight.Black, fontSize = 13.sp, color = Color.LightGray.copy(alpha = 0.4f))
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
        Spacer(Modifier.height(16.dp))
    }
}

@Composable
private fun TimetableSlotCard(slot: TimetablePeriod) {
    val colorIndex = slot.subjectCode.hashCode().let { if (it < 0) -it else it } % 5
    val bgColor = listOf(Color(0xFFEEF2FF), Color(0xFFECFDF5), MaterialTheme.colorScheme.secondaryContainer, Color(0xFFFFFBEB), Color(0xFFF0F9FF))[colorIndex]
    val accentColor = listOf(Color(0xFF4338CA), Color(0xFF047857), Color(0xFF6D28D9), Color(0xFFB45309), Color(0xFF0369A1))[colorIndex]
    
    Surface(
        color = bgColor,
        shape = RoundedCornerShape(12.dp),
        border = BorderStroke(1.dp, accentColor.copy(alpha = 0.2f)),
        modifier = Modifier.fillMaxSize()
    ) {
        Column(modifier = Modifier.padding(10.dp), verticalArrangement = Arrangement.SpaceBetween) {
            Column {
                Text(slot.subjectName, fontWeight = FontWeight.Black, fontSize = 12.sp, color = accentColor, maxLines = 2, overflow = TextOverflow.Ellipsis)
                Text(slot.subjectCode, fontWeight = FontWeight.Bold, fontSize = 12.sp, color = accentColor.copy(alpha = 0.7f))
            }
            Text(slot.facultyName, fontWeight = FontWeight.Bold, fontSize = 12.sp, color = accentColor.copy(alpha = 0.8f), maxLines = 1)
        }
    }
}

@Composable
private fun AcademicsTab(uiState: TimetableState) {
    LazyColumn(modifier = Modifier.fillMaxSize(), verticalArrangement = Arrangement.spacedBy(16.dp)) {
        item {
            // Stats Row
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                StatCard("Total Subjects", uiState.summary?.totalSubjects?.toString() ?: "—", Icons.Filled.Book, CamsNavy, Modifier.weight(1f))
                StatCard("Total Credits", uiState.summary?.totalCredits?.toString() ?: "—", Icons.Filled.Star, Color(0xFFF59E0B), Modifier.weight(1f))
            }
        }
        item {
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                StatCard("Core Subjects", uiState.summary?.coreSubjects?.toString() ?: "—", Icons.Filled.WorkspacePremium, Color(0xFF10B981), Modifier.weight(1f))
                StatCard("Semester", uiState.summary?.currentSemester?.toString() ?: "—", Icons.Filled.Layers, Color(0xFF8B5CF6), Modifier.weight(1f))
            }
        }
        item {
            // Policy Banner
            Surface(
                color = CamsNavy.copy(alpha = 0.05f),
                shape = RoundedCornerShape(16.dp),
                border = BorderStroke(1.dp, CamsNavy.copy(alpha = 0.1f))
            ) {
                Row(modifier = Modifier.padding(16.dp), horizontalArrangement = Arrangement.spacedBy(12.dp), verticalAlignment = Alignment.Top) {
                    Icon(Icons.Filled.School, null, tint = CamsNavy, modifier = Modifier.size(20.dp))
                    Column {
                        Text("BCI Approved Curriculum — Five-Year Integrated B.A. LL.B.", fontWeight = FontWeight.Black, fontSize = 12.sp, color = CamsTextPrimary)
                        Text("Subjects listed below are part of your current semester's approved course load.", fontSize = 12.sp, color = CamsTextSecondary)
                    }
                }
            }
        }
        item {
            Text("Enrolled Subjects & Credit Distribution", fontWeight = FontWeight.Black, fontSize = 16.sp, color = CamsTextPrimary)
        }
        items(uiState.subjects) { subject ->
            SubjectListItem(subject)
        }
        item {
            Spacer(Modifier.height(20.dp))
        }
    }
}

@Composable
private fun StatCard(label: String, value: String, icon: ImageVector, color: Color, modifier: Modifier = Modifier) {
    CamsCard(
        modifier = modifier,
    ) {
        Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            Box(Modifier.size(32.dp).background(color.copy(alpha = 0.1f), CircleShape), contentAlignment = Alignment.Center) {
                Icon(icon, null, tint = color, modifier = Modifier.size(16.dp))
            }
            Column {
                Text(value, fontWeight = FontWeight.Black, fontSize = 18.sp, color = CamsTextPrimary)
                Text(label, fontSize = 12.sp, fontWeight = FontWeight.Bold, color = CamsTextSecondary)
            }
        }
    }
}

@Composable
private fun SubjectListItem(subject: AcademicSubject) {
    CamsCard(
        modifier = Modifier.fillMaxWidth(),
    ) {
        Column {
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                Surface(
                    color = CamsBackground,
                    shape = RoundedCornerShape(8.dp),
                    border = BorderStroke(1.dp, Color.LightGray.copy(alpha = 0.2f))
                ) {
                    Text(
                        subject.code,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp),
                        style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Black, color = CamsTextPrimary, fontSize = 12.sp)
                    )
                }
                Surface(
                    color = Color(0xFFECFDF5),
                    shape = RoundedCornerShape(100.dp)
                ) {
                    Text(
                        "ACTIVE",
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp),
                        style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Black, color = Color(0xFF047857), fontSize = 13.sp)
                    )
                }
            }
            Spacer(Modifier.height(12.dp))
            Text(subject.name, fontWeight = FontWeight.Black, fontSize = 15.sp, color = CamsTextPrimary)
            Text(subject.type, fontSize = 13.sp, color = CamsTextSecondary, fontWeight = FontWeight.Bold)
            
            HorizontalDivider(Modifier.padding(vertical = 12.dp), color = Color.LightGray.copy(alpha = 0.2f))
            
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Column {
                    Text("FACULTY", fontSize = 13.sp, fontWeight = FontWeight.Black, color = CamsTextSecondary)
                    Text(subject.faculty, fontSize = 12.sp, fontWeight = FontWeight.Bold, color = CamsTextPrimary)
                }
                Column(horizontalAlignment = Alignment.End) {
                    Text("CREDITS", fontSize = 13.sp, fontWeight = FontWeight.Black, color = CamsTextSecondary)
                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                        Text(subject.credits.toString(), fontSize = 18.sp, fontWeight = FontWeight.Black, color = CamsNavy)
                        Text("Cr", fontSize = 12.sp, fontWeight = FontWeight.Black, color = CamsTextSecondary)
                    }
                }
            }
        }
    }
}

@Composable
private fun TimetableSkeleton() {
    Column(modifier = Modifier.fillMaxSize()) {
        Box(Modifier.width(200.dp).height(24.dp).shimmerEffect().clip(RoundedCornerShape(4.dp)))
        Spacer(Modifier.height(4.dp))
        Box(Modifier.width(150.dp).height(16.dp).shimmerEffect().clip(RoundedCornerShape(4.dp)))
        
        Spacer(Modifier.height(16.dp))
        
        Card(
            modifier = Modifier.fillMaxWidth().weight(1f),
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            border = BorderStroke(1.dp, MaterialTheme.colorScheme.surfaceVariant)
        ) {
            Column {
                Row(modifier = Modifier.background(MaterialTheme.colorScheme.background).border(1.dp, MaterialTheme.colorScheme.surfaceVariant)) {
                    Box(Modifier.width(80.dp).height(40.dp))
                    repeat(3) {
                        Box(Modifier.width(120.dp).height(40.dp).padding(8.dp).shimmerEffect().clip(RoundedCornerShape(4.dp)))
                    }
                }
                repeat(4) {
                    Row(modifier = Modifier.border(0.5.dp, MaterialTheme.colorScheme.surfaceVariant)) {
                        Box(Modifier.width(80.dp).height(120.dp).background(MaterialTheme.colorScheme.background))
                        repeat(3) {
                            Box(Modifier.width(120.dp).height(120.dp).padding(8.dp).shimmerEffect().clip(RoundedCornerShape(12.dp)))
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun AcademicsSkeleton() {
    LazyColumn(modifier = Modifier.fillMaxSize(), verticalArrangement = Arrangement.spacedBy(16.dp)) {
        item {
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                Box(Modifier.weight(1f).height(80.dp).shimmerEffect().clip(RoundedCornerShape(20.dp)))
                Box(Modifier.weight(1f).height(80.dp).shimmerEffect().clip(RoundedCornerShape(20.dp)))
            }
        }
        item {
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                Box(Modifier.weight(1f).height(80.dp).shimmerEffect().clip(RoundedCornerShape(20.dp)))
                Box(Modifier.weight(1f).height(80.dp).shimmerEffect().clip(RoundedCornerShape(20.dp)))
            }
        }
        item {
            Box(Modifier.fillMaxWidth().height(60.dp).shimmerEffect().clip(RoundedCornerShape(16.dp)))
        }
        items(3) {
            Box(Modifier.fillMaxWidth().height(160.dp).shimmerEffect().clip(RoundedCornerShape(20.dp)))
        }
    }
}
