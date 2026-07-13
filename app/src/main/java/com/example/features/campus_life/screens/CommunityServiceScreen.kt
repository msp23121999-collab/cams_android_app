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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.core.theme.*
import com.example.core.navigation.AppRoutes
import com.example.core.theme.*
import com.example.core.ui.CamsCard
import com.example.core.ui.CamsScreen
import com.example.features.campus_life.models.CommunityServiceLog
import com.example.features.campus_life.models.CommunityServiceOpportunity
import com.example.features.campus_life.providers.CommunityServiceViewModel
import com.example.features.student.widgets.StudentDrawer
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CommunityServiceScreen(
    onNavigate: (String) -> Unit,
    viewModel: CommunityServiceViewModel = viewModel()
) {
    val uiState by viewModel.uiState.collectAsState()

    CamsScreen(scrollable = true,
        title = "Community Service",
        subtitle = "Social Impact Portal",
        onBackClick = { onNavigate(AppRoutes.STUDENT_DASHBOARD) },
        verticalArrangement = Arrangement.spacedBy(24.dp)
    ) {
        // Stats
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                ServiceStatCard(Modifier.weight(1f), "Service Hours", "145h", Icons.Filled.Schedule, Color(0xFF10B981))
                ServiceStatCard(Modifier.weight(1f), "NGO Collabs", "4", Icons.Filled.Language, Color(0xFF3B82F6))
            }
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                ServiceStatCard(Modifier.weight(1f), "Legal Aid", "6", Icons.Filled.Balance, CamsNavy)
                ServiceStatCard(Modifier.weight(1f), "Certificates", "3", Icons.Filled.Verified, Color(0xFFF59E0B))
            }

            // Upcoming Opportunities
            Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.SpaceBetween, modifier = Modifier.fillMaxWidth()) {
                    Text("UPCOMING OPPORTUNITIES", style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Black, color = CamsTextSecondary, letterSpacing = 1.sp))
                    TextButton(onClick = {}) { Text("Browse All", style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold, color = Color(0xFF10B981))) }
                }
                
                
                if (uiState.opportunities.isEmpty()) {
                    Text("No upcoming opportunities at the moment.", style = MaterialTheme.typography.bodySmall, color = CamsTextSecondary)
                } else {
                    uiState.opportunities.forEach { opp ->
                        ServiceOpportunityItem(opp)
                    }
                }
            }

            // Recent Service Log
            Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                Text("RECENT SERVICE LOG", style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Black, color = CamsTextSecondary, letterSpacing = 1.sp))
                
                CamsCard(modifier = Modifier.fillMaxWidth()) {
                    Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                        if (uiState.logs.isEmpty()) {
                            Text("No service logs recorded yet.", style = MaterialTheme.typography.bodySmall, color = CamsTextSecondary, modifier = Modifier.padding(vertical = 12.dp))
                        } else {
                            uiState.logs.forEachIndexed { index, log ->
                                ServiceLogItem(log)
                                if (index < uiState.logs.size - 1) {
                                    HorizontalDivider(color = Color.LightGray.copy(alpha = 0.2f))
                                }
                            }
                        }
                        
                        Button(
                            onClick = {},
                            modifier = Modifier.fillMaxWidth(),
                            colors = ButtonDefaults.buttonColors(containerColor = CamsBackground),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Text("View Full Log", style = MaterialTheme.typography.labelSmall.copy(color = CamsTextPrimary, fontWeight = FontWeight.Bold))
                        }
                    }
                }
            }
            
            Spacer(modifier = Modifier.height(20.dp))
    }
}

@Composable
fun ServiceStatCard(modifier: Modifier = Modifier, label: String, value: String, icon: ImageVector, color: Color) {
    CamsCard(modifier = modifier) {
        Column {
            Surface(color = color.copy(alpha = 0.1f), shape = RoundedCornerShape(12.dp)) {
                Icon(icon, contentDescription = null, tint = color, modifier = Modifier.padding(8.dp).size(20.dp))
            }
            Spacer(modifier = Modifier.height(12.dp))
            Text(value, style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Black, color = CamsTextPrimary))
            Text(label.uppercase(), style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Black, color = CamsTextSecondary, letterSpacing = 0.5.sp))
        }
    }
}

@Composable
fun ServiceOpportunityItem(opp: CommunityServiceOpportunity) {
    CamsCard(modifier = Modifier.fillMaxWidth()) {
        Column {
            Row(horizontalArrangement = Arrangement.SpaceBetween, modifier = Modifier.fillMaxWidth()) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(opp.title, style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Black, color = CamsTextPrimary))
                    Text(opp.ngo, style = MaterialTheme.typography.labelMedium.copy(color = Color(0xFF059669), fontWeight = FontWeight.Bold))
                }
                Surface(color = CamsBackground, shape = CircleShape) {
                    Row(modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp), verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Filled.Schedule, contentDescription = null, modifier = Modifier.size(10.dp), tint = CamsTextSecondary)
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(opp.hours, style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Black, fontSize = 12.sp, color = CamsTextPrimary))
                    }
                }
            }
            
            Spacer(modifier = Modifier.height(12.dp))
            Row(modifier = Modifier.horizontalScroll(rememberScrollState()), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                opp.tags.forEach { tag ->
                    Surface(color = CamsBackground, shape = CircleShape) {
                        Text(tag.uppercase(), modifier = Modifier.padding(horizontal = 10.dp, vertical = 2.dp), style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold, fontSize = 7.sp, color = CamsNavy))
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))
            HorizontalDivider(color = Color.LightGray.copy(alpha = 0.2f))
            Spacer(modifier = Modifier.height(16.dp))

            Row(horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically, modifier = Modifier.fillMaxWidth()) {
                Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        Icon(Icons.Filled.CalendarToday, contentDescription = null, tint = CamsTextSecondary, modifier = Modifier.size(12.dp))
                        Text(opp.date, style = MaterialTheme.typography.labelSmall.copy(color = CamsTextSecondary))
                    }
                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        Icon(Icons.Filled.LocationOn, contentDescription = null, tint = CamsTextSecondary, modifier = Modifier.size(12.dp))
                        Text(opp.location, style = MaterialTheme.typography.labelSmall.copy(color = CamsTextSecondary))
                    }
                }
                
                Button(
                    onClick = {},
                    modifier = Modifier.height(32.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = CamsNavy),
                    shape = RoundedCornerShape(8.dp),
                    contentPadding = PaddingValues(horizontal = 16.dp)
                ) {
                    Text("Apply Now", style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Black))
                }
            }
        }
    }
}

@Composable
fun ServiceLogItem(log: CommunityServiceLog) {
    Row(verticalAlignment = Alignment.Top, horizontalArrangement = Arrangement.spacedBy(16.dp), modifier = Modifier.fillMaxWidth()) {
        Surface(
            modifier = Modifier.size(8.dp).padding(top = 8.dp),
            color = if (log.status == "Verified") Color(0xFF10B981) else Color(0xFFF59E0B),
            shape = CircleShape
        ) {}
        
        Column(modifier = Modifier.weight(1f)) {
            Text(log.title, style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.Black, color = CamsTextPrimary))
            Row(modifier = Modifier.padding(top = 4.dp), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                Text(log.date.uppercase(), style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Black, color = CamsTextSecondary, fontSize = 12.sp))
                Text("+${log.hours} HRS", style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Black, color = Color(0xFF10B981), fontSize = 12.sp))
                Text(log.status.uppercase(), style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Black, color = if (log.status == "Verified") Color(0xFF10B981) else Color(0xFFF59E0B), fontSize = 12.sp))
            }
            
            if (log.certificate) {
                Surface(
                    onClick = {},
                    modifier = Modifier.padding(top = 8.dp),
                    color = CamsBackground,
                    shape = RoundedCornerShape(6.dp)
                ) {
                    Row(modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp), verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Filled.Download, contentDescription = null, tint = CamsNavy, modifier = Modifier.size(10.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("DOWNLOAD CERTIFICATE", style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Black, color = CamsNavy, fontSize = 12.sp))
                    }
                }
            }
        }
    }
}
