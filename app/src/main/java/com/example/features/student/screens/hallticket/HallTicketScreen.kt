package com.example.features.student.screens.hallticket

import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.ConfirmationNumber
import androidx.compose.material.icons.filled.ErrorOutline
import com.example.core.network.HallTicketDto
import com.example.core.theme.LexNovaSlateLight
import com.example.core.theme.LexNovaSlateAccent
import com.example.core.theme.LexNovaPurple
import com.example.core.theme.CamsTextSecondary
import com.example.core.ui.EmptyStateView
import com.example.core.ui.shimmerEffect
import com.example.features.student.providers.HallTicketViewModel
import com.example.features.student.widgets.StudentBaseScreen

@Composable
fun HallTicketScreen(
    viewModel: HallTicketViewModel,
    currentRoute: String,
    onNavigate: (String) -> Unit
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val context = LocalContext.current

    StudentBaseScreen(
        title = "Hall Tickets",
        currentRoute = currentRoute,
        onNavigate = onNavigate,
        scrollable = false
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            if (uiState.isLoading) {
                LazyColumn(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    items(3) {
                        Box(modifier = Modifier.fillMaxWidth().height(140.dp).clip(RoundedCornerShape(16.dp)).shimmerEffect())
                    }
                }
            } else if (uiState.error != null) {
                com.example.core.ui.NetworkErrorView(
                    message = uiState.error!!,
                    onRetry = { viewModel.fetchHallTickets() },
                    modifier = Modifier.fillMaxSize()
                )
            } else if (uiState.tickets.isEmpty()) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    EmptyStateView(
                        icon = Icons.Filled.ConfirmationNumber,
                        title = "No Hall Tickets",
                        message = "Your hall tickets will appear here when issued."
                    )
                }
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    verticalArrangement = Arrangement.spacedBy(16.dp),
                    contentPadding = PaddingValues(bottom = 80.dp)
                ) {
                    items(uiState.tickets) { ticket ->
                        HallTicketCard(
                            ticket = ticket,
                            onDownload = {
                                val token = com.example.core.network.AuthManagerImpl(context).getToken() ?: ""
                                val base = com.example.core.config.AppConfig.BASE_URL.trimEnd('/')
                                val url = "$base/students/hall-tickets/${ticket.id}/download"
                                com.example.core.utils.DownloadHelper.downloadPdf(context, url, "HallTicket_${ticket.id}", token)
                            }
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun HallTicketCard(ticket: HallTicketDto, onDownload: () -> Unit) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = ticket.examCenter ?: "End Semester Examination",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(Modifier.height(4.dp))
                    Text(
                        text = "Date: ${ticket.examDate ?: "TBD"}",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                
                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = if (ticket.isEligible && ticket.isIssued) Color(0xFF10B981).copy(alpha = 0.1f) else Color(0xFFF43F5E).copy(alpha = 0.1f),
                ) {
                    Text(
                        text = if (ticket.isEligible && ticket.isIssued) "ISSUED" else "PENDING",
                        color = if (ticket.isEligible && ticket.isIssued) Color(0xFF10B981) else Color(0xFFF43F5E),
                        fontWeight = FontWeight.Bold,
                        fontSize = 12.sp,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                    )
                }
            }

            Spacer(Modifier.height(16.dp))
            HorizontalDivider(color = LexNovaSlateLight)
            Spacer(Modifier.height(16.dp))

            if (!ticket.isEligible) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(Color(0xFFFFFBEB), RoundedCornerShape(8.dp))
                        .padding(12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(Icons.Filled.ErrorOutline, contentDescription = null, tint = Color(0xFFF59E0B), modifier = Modifier.size(20.dp))
                    Spacer(Modifier.width(8.dp))
                    Text(
                        text = ticket.ineligibilityReason ?: "Ineligible due to fee dues or attendance shortage.",
                        style = MaterialTheme.typography.bodySmall,
                        color = Color(0xFFB45309)
                    )
                }
                Spacer(Modifier.height(16.dp))
            }

            Button(
                onClick = onDownload,
                modifier = Modifier.fillMaxWidth(),
                enabled = ticket.isEligible && ticket.isIssued,
                colors = ButtonDefaults.buttonColors(containerColor = LexNovaPurple)
            ) {
                Icon(Icons.Filled.Download, contentDescription = null, modifier = Modifier.size(18.dp))
                Spacer(Modifier.width(8.dp))
                Text("Download PDF", fontWeight = FontWeight.Bold)
            }
        }
    }
}
