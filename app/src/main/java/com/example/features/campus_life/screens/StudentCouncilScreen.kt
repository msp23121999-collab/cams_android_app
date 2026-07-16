package com.example.features.campus_life.screens

import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.*
import androidx.compose.material.icons.filled.ArrowForward
import androidx.compose.material.icons.filled.KeyboardArrowRight
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
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import coil.compose.AsyncImage
import com.example.core.navigation.AppRoutes
import com.example.core.theme.*
import com.example.core.ui.CamsCard
import com.example.core.ui.CamsScreen
import com.example.features.campus_life.models.*
import com.example.features.campus_life.providers.CouncilViewModel
import com.example.features.student.widgets.StudentDrawer
import kotlinx.coroutines.launch

private val Rose600 = Color(0xFFE11D48)
private val Rose50 = Color(0xFFFFF1F2)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun StudentCouncilScreen(
    viewModel: CouncilViewModel = viewModel(),
    onNavigate: (String) -> Unit
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    CamsScreen(scrollable = true,
        title = "Student Council",
        subtitle = "Student Governance Portal",
        onBackClick = { onNavigate(AppRoutes.STUDENT_DASHBOARD) },
        verticalArrangement = Arrangement.spacedBy(24.dp)
    ) {
        if (uiState.isLoading) {
            Box(modifier = Modifier.fillMaxWidth().height(200.dp), contentAlignment = Alignment.Center) {
                CircularProgressIndicator(color = CamsNavy)
            }
        } else {
            CamsCard {
                Text(
                    "Your voice on campus. Meet your elected representatives, track ongoing initiatives, submit feedback, and view the council's transparency reports.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    lineHeight = 18.sp
                )
            }

            // Header Actions
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                OutlinedButton(
                    onClick = { },
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(12.dp),
                    border = BorderStroke(1.dp, Color.LightGray.copy(alpha = 0.5f)),
                    colors = ButtonDefaults.outlinedButtonColors(contentColor = CamsTextPrimary)
                ) {
                    Text("Contact Council", style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.Bold, fontSize = 14.sp))
                }
                Button(
                    onClick = { },
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = CamsNavy)
                ) {
                    Text("Submit Proposal", style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.Bold, fontSize = 14.sp))
                }
            }

                // Metrics
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    MetricCard(Modifier.weight(1f), "Proposals", "12", Icons.Filled.Adjust, CamsNavy)
                    MetricCard(Modifier.weight(1f), "Resolved", "45", Icons.Filled.CheckCircle, Color(0xFF10B981))
                    MetricCard(Modifier.weight(1f), "Fund", "68%", Icons.Filled.AttachMoney, Color(0xFFD97706))
                }

                // Initiatives
                InitiativesCard(uiState.initiatives)

                // Feedback
                FeedbackBoard(uiState.feedback)

                // Representatives
                RepresentativesCard(uiState.representatives)

                // Next Meeting removed due to lack of ViewModel state

                Spacer(modifier = Modifier.height(20.dp))
        }
    }
}

@Composable
private fun MetricCard(modifier: Modifier, label: String, value: String, icon: ImageVector, color: Color) {
    CamsCard(modifier = modifier) {
        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Surface(color = color.copy(alpha = 0.1f), shape = RoundedCornerShape(8.dp)) {
                    Icon(icon, contentDescription = null, tint = color, modifier = Modifier.size(14.dp).padding(2.dp))
                }
                Text(label.uppercase(), style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Black, fontSize = 12.sp), color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
            Text(value, style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Black), color = MaterialTheme.colorScheme.onSurface)
        }
    }
}

@Composable
private fun InitiativesCard(initiatives: List<CouncilInitiative>) {
    CamsCard {
        Column {
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Icon(Icons.Filled.Flag, contentDescription = null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(20.dp))
                Text("Council Initiatives Tracker", style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Black))
            }
            Spacer(modifier = Modifier.height(20.dp))
            Column(verticalArrangement = Arrangement.spacedBy(20.dp)) {
                initiatives.forEach { init ->
                    Column {
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.Bottom) {
                            Column {
                                Text(init.title, style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Bold), color = MaterialTheme.colorScheme.onSurface)
                                Text("${init.category} • ${init.status}", style = MaterialTheme.typography.labelSmall.copy(fontSize = 13.sp), color = MaterialTheme.colorScheme.onSurfaceVariant)
                            }
                            Text("${(init.progress * 100).toInt()}%", style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold), color = CamsNavy)
                        }
                        Spacer(modifier = Modifier.height(8.dp))
                        LinearProgressIndicator(
                            progress = { init.progress },
                            modifier = Modifier.fillMaxWidth().height(8.dp).clip(RoundedCornerShape(4.dp)),
                            color = CamsNavy,
                            trackColor = Color.LightGray.copy(alpha = 0.1f)
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun FeedbackBoard(feedback: List<StudentFeedback>) {
    CamsCard {
        Column {
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Icon(Icons.AutoMirrored.Filled.Message, contentDescription = null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(20.dp))
                    Text("Top Student Feedback", style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Black))
                }
                Text("View All", style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold), color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
            Spacer(modifier = Modifier.height(20.dp))
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                feedback.forEach { item ->
                    Row(
                        modifier = Modifier.fillMaxWidth().background(CamsBackground, RoundedCornerShape(16.dp)).border(1.dp, Color.LightGray.copy(alpha = 0.1f), RoundedCornerShape(16.dp)).padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Column(
                            modifier = Modifier.background(MaterialTheme.colorScheme.surface, RoundedCornerShape(8.dp)).border(1.dp, Color.LightGray.copy(alpha = 0.1f), RoundedCornerShape(8.dp)).padding(horizontal = 8.dp, vertical = 4.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Icon(Icons.Filled.TrendingUp, contentDescription = null, tint = Color(0xFF10B981), modifier = Modifier.size(12.dp))
                            Text("${item.votes}", style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold))
                        }
                        Column(modifier = Modifier.weight(1f)) {
                            Text(item.topic, style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Bold), color = MaterialTheme.colorScheme.onSurface)
                            Surface(
                                color = when(item.status) {
                                    "Implemented" -> Color(0xFFECFDF5)
                                    "Approved" -> Color(0xFFEFF6FF)
                                    else -> Color(0xFFFFFBEB)
                                },
                                shape = RoundedCornerShape(4.dp)
                            ) {
                                Text(
                                    item.status,
                                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                                    style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold, fontSize = 13.sp),
                                    color = when(item.status) {
                                        "Implemented" -> Color(0xFF047857)
                                        "Approved" -> Color(0xFF1D4ED8)
                                        else -> Color(0xFFB45309)
                                    }
                                )
                            }
                        }
                        Icon(Icons.Filled.KeyboardArrowRight, contentDescription = null, tint = MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.size(16.dp))
                    }
                }
            }
        }
    }
}

@Composable
private fun RepresentativesCard(reps: List<CouncilRepresentative>) {
    CamsCard {
        Column {
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Icon(Icons.Filled.People, contentDescription = null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(20.dp))
                Text("Elected Representatives", style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Black))
            }
            Spacer(modifier = Modifier.height(20.dp))
            Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                reps.forEach { rep ->
                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                        AsyncImage(
                            model = rep.imageUrl,
                            contentDescription = rep.name,
                            modifier = Modifier.size(40.dp).clip(CircleShape).border(1.dp, Color.LightGray.copy(alpha = 0.1f), CircleShape),
                            contentScale = ContentScale.Crop
                        )
                        Column {
                            Text(rep.name, style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Bold), color = MaterialTheme.colorScheme.onSurface)
                            Text(rep.role, style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold), color = CamsNavy)
                        }
                    }
                }
            }
            Spacer(modifier = Modifier.height(20.dp))
            Button(
                onClick = { },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.background, contentColor = CamsTextSecondary),
                border = BorderStroke(1.dp, Color.LightGray.copy(alpha = 0.1f))
            ) {
                Text("View Full Committee", style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold))
            }
        }
    }
}

