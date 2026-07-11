package com.example.features.parent.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.core.theme.*
import com.example.core.ui.CamsScreen
import com.example.core.ui.CamsCard
import com.example.features.parent.providers.ParentFeesViewModel
import com.example.features.parent.widgets.ParentDrawer
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FeeStatusScreen(
    viewModel: ParentFeesViewModel,
    onNavigate: (String) -> Unit
) {
    val uiState by viewModel.uiState.collectAsState()
    val drawerState = rememberDrawerState(initialValue = DrawerValue.Closed)
    val scope = rememberCoroutineScope()
    var isDownloading by remember { mutableStateOf(false) }

    ModalNavigationDrawer(
        drawerState = drawerState,
        drawerContent = {
            ParentDrawer(
                currentRoute = "/parent/fees",
                onNavigate = {
                    scope.launch { drawerState.close() }
                    onNavigate(it)
                }
            )
        }
    ) {
        CamsScreen(scrollable = false,
            title = "Fee Status",
            subtitle = "Tuition, Scholarships & Outstanding Balance",
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
            verticalArrangement = Arrangement.spacedBy(24.dp)
        ) {
            if (uiState.isLoading) {
                Box(modifier = Modifier.fillMaxWidth().height(200.dp), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator(color = LexNovaPurple)
                }
            } else if (uiState.feeLedger != null) {
                val ledger = uiState.feeLedger!!
                
                // Top Banner Card
                CamsCard {
                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.SpaceBetween, modifier = Modifier.fillMaxWidth()) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text("Child Fee Ledger Accounts", style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold))
                            Text("Review fees for ${uiState.childProfile?.fullName ?: "your child"}", style = MaterialTheme.typography.bodySmall.copy(color = CamsTextSecondary))
                        }
                        Button(
                            onClick = { isDownloading = true },
                            enabled = !isDownloading,
                            colors = ButtonDefaults.buttonColors(containerColor = CamsNavy),
                            shape = RoundedCornerShape(12.dp),
                            contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp)
                        ) {
                            Icon(if (isDownloading) Icons.Filled.CheckCircle else Icons.Filled.AccountBalanceWallet, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(if (isDownloading) "Generating..." else "Download PDF", fontSize = 12.sp)
                        }
                    }
                }

                // Metrics Grid
                val cards = listOf(
                    Triple("Gross Fees", "₹${ledger.totalFees}", CamsNavy),
                    Triple("Scholarships", "₹${ledger.scholarshipDeduction}", Color(0xFF10B981)),
                    Triple("Net Payable", "₹${ledger.netFees}", Color(0xFF6366F1)),
                    Triple("Paid", "₹${ledger.amountPaid}", Color(0xFF059669)),
                    Triple("Outstanding", "₹${ledger.pendingBalance}", Color(0xFFEF4444))
                )

                Row(modifier = Modifier.horizontalScroll(rememberScrollState()), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    cards.forEach { (label, value, color) ->
                        CamsCard(modifier = Modifier.width(150.dp)) {
                            Text(label, fontSize = 12.sp, fontWeight = FontWeight.Bold, color = CamsTextSecondary)
                            Spacer(Modifier.height(4.dp))
                            Text(value, fontSize = 18.sp, fontWeight = FontWeight.Black, color = color)
                        }
                    }
                }

                Text("Ledger Details", style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold))

                uiState.feeLedger!!.records.forEach { record ->
                    CamsCard {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(record.feeType, fontWeight = FontWeight.Bold, color = CamsTextPrimary)
                                Text("Due: ${record.dueDate ?: "N/A"}", fontSize = 12.sp, color = CamsTextSecondary)
                            }
                            Column(horizontalAlignment = Alignment.End) {
                                Text("₹${record.amount}", fontWeight = FontWeight.Black, color = CamsNavy)
                                val statusColor = when (record.status) {
                                    "paid" -> Color(0xFF10B981)
                                    "partially_paid" -> Color(0xFF3B82F6)
                                    else -> Color(0xFFF59E0B)
                                }
                                Surface(color = statusColor.copy(alpha = 0.1f), shape = RoundedCornerShape(8.dp)) {
                                    Text(
                                        record.status.uppercase(),
                                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp),
                                        fontSize = 13.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = statusColor
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}
