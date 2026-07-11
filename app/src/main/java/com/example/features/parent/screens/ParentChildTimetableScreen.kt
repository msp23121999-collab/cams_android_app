package com.example.features.parent.screens

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.core.theme.*
import com.example.core.ui.CamsCard
import com.example.core.ui.CamsScreen
import com.example.features.parent.providers.ParentTimetableViewModel
import com.example.features.parent.widgets.ParentDrawer
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ParentChildTimetableScreen(
    viewModel: ParentTimetableViewModel,
    onNavigate: (String) -> Unit
) {
    val uiState by viewModel.uiState.collectAsState()
    val drawerState = rememberDrawerState(initialValue = DrawerValue.Closed)
    val scope = rememberCoroutineScope()

    var selectedDay by remember { mutableStateOf("Monday") }
    val days = listOf("Monday", "Tuesday", "Wednesday", "Thursday", "Friday")

    ModalNavigationDrawer(
        drawerState = drawerState,
        drawerContent = {
            ParentDrawer(
                currentRoute = "/parent/timetable",
                onNavigate = {
                    scope.launch { drawerState.close() }
                    onNavigate(it)
                }
            )
        }
    ) {
        CamsScreen(scrollable = false,
            title = "Weekly Timetable",
            subtitle = "Child class schedules & room allocations",
            navigationIcon = {
                IconButton(onClick = { scope.launch { drawerState.open() } }) {
                    Icon(Icons.Filled.Menu, contentDescription = "Menu", tint = Color.White)
                }
            },
            actions = {
                IconButton(onClick = { onNavigate("LOGOUT") }) {
                    Icon(Icons.Filled.Logout, contentDescription = "Logout", tint = Color.White)
                }
            },
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            if (uiState.isLoading) {
                Box(modifier = Modifier.fillMaxWidth().height(200.dp), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator(color = CamsNavy)
                }
            } else {
                // Day selection tabs row
                ScrollableTabRow(
                    selectedTabIndex = days.indexOf(selectedDay),
                    edgePadding = 0.dp,
                    containerColor = MaterialTheme.colorScheme.surface,
                    contentColor = CamsNavy
                ) {
                    days.forEach { day ->
                        Tab(
                            selected = selectedDay == day,
                            onClick = { selectedDay = day },
                            text = {
                                Text(
                                    day,
                                    fontWeight = if (selectedDay == day) FontWeight.Black else FontWeight.Medium,
                                    fontSize = 14.sp,
                                    color = if (selectedDay == day) CamsNavy else CamsTextSecondary
                                )
                            }
                        )
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))

                val timetableForDay = uiState.timetable.find { it.dayName == selectedDay }

                if (timetableForDay == null || timetableForDay.periods.isEmpty()) {
                    Box(modifier = Modifier.fillMaxWidth().padding(40.dp), contentAlignment = Alignment.Center) {
                        Text("No schedule available for $selectedDay", color = CamsTextSecondary)
                    }
                } else {
                    timetableForDay.periods.forEach { period ->
                        val isFree = period.subjectName.lowercase().contains("free") || period.subjectName.lowercase().contains("break")
                        val cardBg = if (isFree) MaterialTheme.colorScheme.background else Color.White
                        val accentColor = if (isFree) Color(0xFF64748B) else CamsNavy

                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(16.dp),
                            colors = CardDefaults.cardColors(containerColor = cardBg),
                            elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(16.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                // Time Column
                                Column(
                                    modifier = Modifier.width(90.dp),
                                    verticalArrangement = Arrangement.Center
                                ) {
                                    Text(
                                        "Period ${period.periodNo}",
                                        fontWeight = FontWeight.Bold,
                                        color = accentColor,
                                        fontSize = 12.sp
                                    )
                                    Spacer(modifier = Modifier.height(2.dp))
                                    Text(
                                        period.time,
                                        fontSize = 13.sp,
                                        color = CamsTextSecondary,
                                        fontWeight = FontWeight.Medium
                                    )
                                }

                                Spacer(modifier = Modifier.width(16.dp))

                                // Subject Detail Column
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        period.subjectName,
                                        fontWeight = FontWeight.Black,
                                        fontSize = 15.sp,
                                        color = CamsTextPrimary
                                    )
                                    Spacer(modifier = Modifier.height(4.dp))
                                    Row(
                                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Surface(
                                            color = accentColor.copy(alpha = 0.08f),
                                            shape = RoundedCornerShape(6.dp)
                                        ) {
                                            Text(
                                                period.subjectCode,
                                                fontSize = 13.sp,
                                                fontWeight = FontWeight.Bold,
                                                color = accentColor,
                                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                            )
                                        }

                                        Text(
                                            period.room,
                                            fontSize = 12.sp,
                                            color = CamsTextSecondary,
                                            fontWeight = FontWeight.Bold
                                        )
                                    }
                                    if (period.instructor != "None") {
                                        Spacer(modifier = Modifier.height(4.dp))
                                        Text(
                                            "Instructor: ${period.instructor}",
                                            fontSize = 13.sp,
                                            color = CamsTextSecondary.copy(alpha = 0.8f)
                                        )
                                    }
                                }
                            }
                        }
                        Spacer(modifier = Modifier.height(12.dp))
                    }
                }
            }
        }
    }
}
