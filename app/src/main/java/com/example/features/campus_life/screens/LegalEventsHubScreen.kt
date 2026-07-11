package com.example.features.campus_life.screens

import androidx.compose.animation.*
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
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
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.core.navigation.AppRoutes
import com.example.core.theme.*
import com.example.core.ui.CamsCard
import com.example.core.ui.CamsScreen
import com.example.features.campus_life.providers.*
import com.example.features.campus_life.models.*
import com.example.core.theme.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LegalEventsHubScreen(
    onNavigate: (String) -> Unit,
    viewModel: LegalEventsViewModel = viewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    var selectedEvent by remember { mutableStateOf<LegalEvent?>(null) }

    CamsScreen(
        title = "Legal Events Hub",
        subtitle = "Judge Lectures • Workshops • Debates",
        onBackClick = { onNavigate(AppRoutes.STUDENT_DASHBOARD) }
    ) {
        // Tabs
            ScrollableTabRow(
                selectedTabIndex = when(uiState.activeTab) {
                    "Upcoming" -> 0
                    "Registered" -> 1
                    "Past" -> 2
                    "Ask Judge" -> 3
                    "Debate" -> 4
                    else -> 0
                },
                containerColor = Color.Transparent,
                contentColor = CamsNavy,
                edgePadding = 0.dp,
                divider = {},
                indicator = { tabPositions ->
                    if (uiState.activeTab != null) {
                        val index = when(uiState.activeTab) {
                            "Upcoming" -> 0
                            "Registered" -> 1
                            "Past" -> 2
                            "Ask Judge" -> 3
                            "Debate" -> 4
                            else -> 0
                        }
                        TabRowDefaults.SecondaryIndicator(
                            Modifier.tabIndicatorOffset(tabPositions[index]),
                            color = CamsNavy
                        )
                    }
                }
            ) {
                listOf("Upcoming", "Registered", "Past", "Ask Judge", "Debate").forEach { tab ->
                    Tab(
                        selected = uiState.activeTab == tab,
                        onClick = { viewModel.updateTab(tab) },
                        text = { Text(tab, style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.Bold)) }
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            if (uiState.isLoading) {
                LinearProgressIndicator(modifier = Modifier.fillMaxWidth(), color = CamsNavy)
            } else {
                when (uiState.activeTab) {
                    "Upcoming", "Past", "Registered" -> {
                        uiState.filteredEvents.forEach { event ->
                            LegalEventCard(
                                event = event,
                                onDetail = { selectedEvent = event },
                                onRegister = { viewModel.registerForEvent(event.id) }
                            )
                            Spacer(modifier = Modifier.height(16.dp))
                        }
                    }
                    "Ask Judge" -> {
                        AskJudgeSection(uiState.events, uiState.questions, onSubmit = { eid, q, t -> viewModel.submitQuestion(eid, q, t) })
                    }
                    "Debate" -> {
                        DebateArenaSection(uiState.debates)
                    }
                }
            }
            
            Spacer(modifier = Modifier.height(20.dp))
    }

    if (selectedEvent != null) {
        EventDetailDialog(
            event = selectedEvent!!,
            onDismiss = { selectedEvent = null },
            onRegister = { viewModel.registerForEvent(selectedEvent!!.id) }
        )
    }
}

@Composable
fun LegalEventCard(
    event: LegalEvent,
    onDetail: () -> Unit,
    onRegister: () -> Unit
) {
    CamsCard(
        onClick = onDetail,
        modifier = Modifier.fillMaxWidth()
    ) {
        Column {
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Surface(
                    shape = RoundedCornerShape(50),
                    color = if (event.status == EventStatus.LIVE_NOW) Color(0xFFFEF2F2) else CamsBackground,
                ) {
                    Row(modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp), verticalAlignment = Alignment.CenterVertically) {
                        if (event.status == EventStatus.LIVE_NOW) {
                            Box(modifier = Modifier.size(6.dp).clip(CircleShape).background(Color.Red))
                            Spacer(modifier = Modifier.width(4.dp))
                        }
                        Text(event.status.label.uppercase(), style = MaterialTheme.typography.labelSmall.copy(fontSize = 12.sp, fontWeight = FontWeight.Black, color = if (event.status == EventStatus.LIVE_NOW) Color.Red else CamsNavy))
                    }
                }
                Text(event.category.uppercase(), style = MaterialTheme.typography.labelSmall.copy(fontSize = 12.sp, fontWeight = FontWeight.Bold, color = CamsTextSecondary))
            }

            Spacer(modifier = Modifier.height(12.dp))
            Text(event.title, style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Black), color = CamsTextPrimary)
            
            Spacer(modifier = Modifier.height(16.dp))
            Row(verticalAlignment = Alignment.CenterVertically) {
                Surface(shape = RoundedCornerShape(12.dp), color = CamsNavy, modifier = Modifier.size(40.dp)) {
                    Box(contentAlignment = Alignment.Center) {
                        Text(event.speaker.initials, color = Color.White, style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.Black))
                    }
                }
                Spacer(modifier = Modifier.width(12.dp))
                Column {
                    Text(event.speaker.name, style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.Bold), color = CamsTextPrimary)
                    Text(event.speaker.designation, style = MaterialTheme.typography.labelSmall, color = CamsTextSecondary)
                }
            }

            Spacer(modifier = Modifier.height(16.dp))
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                EventMetaItem(Icons.Filled.CalendarToday, event.date, modifier = Modifier.weight(1f))
                EventMetaItem(Icons.Filled.Schedule, event.time, modifier = Modifier.weight(1f))
            }
            
            Spacer(modifier = Modifier.height(20.dp))
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.SpaceBetween, modifier = Modifier.fillMaxWidth()) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Filled.FlashOn, contentDescription = null, tint = Color(0xFFF59E0B), modifier = Modifier.size(14.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("${event.activityPoints} Activity Pts", style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold), color = Color(0xFFF59E0B))
                }
                
                if (event.isRegistered) {
                    Surface(shape = RoundedCornerShape(12.dp), color = Color(0xFFECFDF5)) {
                        Text("REGISTERED", modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp), style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Black, color = Color(0xFF10B981)))
                    }
                } else if (event.status == EventStatus.REG_OPEN) {
                    Button(
                        onClick = onRegister,
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = CamsNavy),
                        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp)
                    ) {
                        Text("REGISTER", style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Black))
                    }
                }
            }
        }
    }
}

@Composable
fun EventMetaItem(icon: ImageVector, text: String, modifier: Modifier = Modifier) {
    Surface(
        modifier = modifier,
        shape = RoundedCornerShape(12.dp),
        color = CamsBackground
    ) {
        Row(modifier = Modifier.padding(8.dp), verticalAlignment = Alignment.CenterVertically) {
            Icon(icon, contentDescription = null, tint = CamsNavy, modifier = Modifier.size(12.dp))
            Spacer(modifier = Modifier.width(6.dp))
            Text(text, style = MaterialTheme.typography.labelSmall.copy(fontSize = 12.sp), color = CamsTextSecondary, maxLines = 1)
        }
    }
}

@Composable
fun AskJudgeSection(
    events: List<LegalEvent>,
    questions: List<JudgeQuestion>,
    onSubmit: (String, String, String) -> Unit
) {
    var selectedEventId by remember { mutableStateOf("") }
    var topic by remember { mutableStateOf("") }
    var question by remember { mutableStateOf("") }

    Column(verticalArrangement = Arrangement.spacedBy(24.dp)) {
        CamsCard(modifier = Modifier.fillMaxWidth()) {
            Column {
                Text("Submit Your Question", style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Black), color = CamsTextPrimary)
                Text("Selected questions will be answered live by the guest judge", style = MaterialTheme.typography.labelSmall, color = CamsTextSecondary)
                
                Spacer(modifier = Modifier.height(24.dp))
                
                Text("Select Event", style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold), color = CamsTextPrimary)
                Spacer(modifier = Modifier.height(8.dp))
                OutlinedTextField(
                    value = selectedEventId,
                    onValueChange = { selectedEventId = it },
                    modifier = Modifier.fillMaxWidth(),
                    placeholder = { Text("Enter Event ID or Name") },
                    shape = RoundedCornerShape(12.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        unfocusedBorderColor = Color.LightGray.copy(alpha = 0.5f),
                        focusedBorderColor = CamsNavy
                    )
                )

                Spacer(modifier = Modifier.height(16.dp))
                Text("Topic", style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold), color = CamsTextPrimary)
                Spacer(modifier = Modifier.height(8.dp))
                OutlinedTextField(
                    value = topic,
                    onValueChange = { topic = it },
                    modifier = Modifier.fillMaxWidth(),
                    placeholder = { Text("e.g. Right to Privacy") },
                    shape = RoundedCornerShape(12.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        unfocusedBorderColor = Color.LightGray.copy(alpha = 0.5f),
                        focusedBorderColor = CamsNavy
                    )
                )

                Spacer(modifier = Modifier.height(16.dp))
                Text("Question", style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold), color = CamsTextPrimary)
                Spacer(modifier = Modifier.height(8.dp))
                OutlinedTextField(
                    value = question,
                    onValueChange = { question = it },
                    modifier = Modifier.fillMaxWidth(),
                    minLines = 3,
                    placeholder = { Text("Type your question here...") },
                    shape = RoundedCornerShape(12.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        unfocusedBorderColor = Color.LightGray.copy(alpha = 0.5f),
                        focusedBorderColor = CamsNavy
                    )
                )

                Spacer(modifier = Modifier.height(24.dp))
                Button(
                    onClick = { 
                        if (selectedEventId.isNotEmpty() && question.isNotEmpty()) {
                            onSubmit(selectedEventId, question, topic)
                            selectedEventId = ""; topic = ""; question = ""
                        }
                    },
                    modifier = Modifier.fillMaxWidth().height(56.dp),
                    shape = RoundedCornerShape(16.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = CamsNavy)
                ) {
                    Icon(Icons.Filled.Send, contentDescription = null)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("SUBMIT QUESTION", style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.Black))
                }
            }
        }

        Text("My Submitted Questions", style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Black), color = CamsTextPrimary)
        questions.forEach { q ->
            CamsCard(modifier = Modifier.fillMaxWidth()) {
                Column {
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        Surface(shape = RoundedCornerShape(50), color = CamsBackground) {
                            Text(q.status.uppercase(), modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp), style = MaterialTheme.typography.labelSmall.copy(fontSize = 12.sp, fontWeight = FontWeight.Black, color = CamsNavy))
                        }
                        Text(q.submittedAt, style = MaterialTheme.typography.labelSmall, color = CamsTextSecondary)
                    }
                    Spacer(modifier = Modifier.height(12.dp))
                    Text(q.question, style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold), color = CamsTextPrimary)
                    Spacer(modifier = Modifier.height(8.dp))
                    Text("${q.topic} • ${q.eventTitle}", style = MaterialTheme.typography.labelSmall, color = CamsTextSecondary)
                }
            }
        }
    }
}

@Composable
fun DebateArenaSection(debates: List<DebateEntry>) {
    Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
        CamsCard(modifier = Modifier.fillMaxWidth(), containerColor = CamsNavy) {
            Column {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Filled.EmojiEvents, contentDescription = null, tint = Color.White, modifier = Modifier.size(24.dp))
                    Spacer(modifier = Modifier.width(12.dp))
                    Text("Legal Debate Arena", style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Black, color = Color.White))
                }
                Spacer(modifier = Modifier.height(8.dp))
                Text("Present your arguments before a panel of distinguished judges.", style = MaterialTheme.typography.bodyMedium, color = Color.White.copy(alpha = 0.8f))
            }
        }

        debates.forEach { debate ->
            CamsCard(modifier = Modifier.fillMaxWidth()) {
                Column {
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        Surface(shape = RoundedCornerShape(50), color = if (debate.status == "Live") Color(0xFFFEF2F2) else CamsBackground) {
                            Text(debate.status.uppercase(), modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp), style = MaterialTheme.typography.labelSmall.copy(fontSize = 12.sp, fontWeight = FontWeight.Black, color = if (debate.status == "Live") Color.Red else CamsTextSecondary))
                        }
                        Text(debate.id, style = MaterialTheme.typography.labelSmall.copy(fontFamily = androidx.compose.ui.text.font.FontFamily.Monospace), color = CamsTextSecondary)
                    }
                    Spacer(modifier = Modifier.height(12.dp))
                    Text(debate.title, style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Black), color = CamsTextPrimary)
                    
                    Spacer(modifier = Modifier.height(16.dp))
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        EventMetaItem(Icons.Filled.CalendarToday, debate.date, modifier = Modifier.weight(1f))
                        EventMetaItem(Icons.Filled.Scale, debate.topic, modifier = Modifier.weight(1f))
                    }

                    if (debate.status == "Completed" && debate.studentScore != null) {
                        Spacer(modifier = Modifier.height(16.dp))
                        Surface(shape = RoundedCornerShape(16.dp), color = CamsBackground, modifier = Modifier.fillMaxWidth()) {
                            Row(modifier = Modifier.padding(16.dp), horizontalArrangement = Arrangement.SpaceBetween) {
                                Column {
                                    Text("YOUR SCORE", style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold), color = CamsNavy)
                                    Text("${debate.studentScore}/100", style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Black), color = CamsNavy)
                                }
                                Column(horizontalAlignment = Alignment.End) {
                                    Text("RANK", style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold), color = CamsNavy)
                                    Text("#${debate.rank}", style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Black), color = CamsNavy)
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
fun EventDetailDialog(
    event: LegalEvent,
    onDismiss: () -> Unit,
    onRegister: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        confirmButton = {
            if (!event.isRegistered && event.status == EventStatus.REG_OPEN) {
                Button(onClick = { onRegister(); onDismiss() }, colors = ButtonDefaults.buttonColors(containerColor = Purple600)) {
                    Text("Register Now")
                }
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Close") }
        },
        title = { Text(event.title, style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Black)) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                Text(event.description, style = MaterialTheme.typography.bodySmall)
                
                Text("Speaker", style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Surface(shape = RoundedCornerShape(8.dp), color = Indigo600, modifier = Modifier.size(32.dp)) {
                        Box(contentAlignment = Alignment.Center) { Text(event.speaker.initials, color = Color.White, fontSize = 12.sp, fontWeight = FontWeight.Bold) }
                    }
                    Spacer(modifier = Modifier.width(12.dp))
                    Column {
                        Text(event.speaker.name, style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold))
                        Text(event.speaker.designation, style = MaterialTheme.typography.labelSmall, color = Slate500, fontSize = 12.sp)
                    }
                }

                Text("Agenda", style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold))
                event.agenda.forEachIndexed { index, item ->
                    Row {
                        Text("${index + 1}.", style = MaterialTheme.typography.labelSmall, modifier = Modifier.width(20.dp))
                        Text(item, style = MaterialTheme.typography.labelSmall)
                    }
                }
            }
        },
        shape = RoundedCornerShape(24.dp),
        containerColor = MaterialTheme.colorScheme.surface
    )
}
