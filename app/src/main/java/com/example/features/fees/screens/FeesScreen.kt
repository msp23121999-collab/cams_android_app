package com.example.features.fees.screens

import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.compose.animation.*
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
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
import com.example.features.fees.models.*
import com.example.features.fees.providers.FeesUiState
import com.example.features.fees.providers.FeesViewModel
import com.example.features.fees.widgets.*
import com.example.features.student.widgets.StudentDrawer
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FeesScreen(
    onNavigate: (String) -> Unit = {},
    viewModel: FeesViewModel
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    CamsScreen(
        scrollable = false,
        title = "Fees & Finance",
        onBackClick = { onNavigate(AppRoutes.STUDENT_DASHBOARD) },
        verticalArrangement = Arrangement.spacedBy(0.dp)
    ) {
        if (uiState.isLoading) {
            FeesSkeleton()
        } else if (uiState.error != null) {
            com.example.core.ui.NetworkErrorView(
                message = "Error: ${uiState.error}",
                onRetry = { viewModel.fetchFees() },
                modifier = Modifier.fillMaxSize()
            )
        } else {
            Content(uiState, viewModel, onNavigate)
        }
    }
}

@Composable
private fun Content(
    uiState: FeesUiState,
    viewModel: FeesViewModel,
    onNavigate: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(20.dp)
    ) {
        Header(uiState)
        TabSelector(uiState.activeTab) { viewModel.setActiveTab(it) }
        
        AnimatedContent(
            targetState = uiState.activeTab,
            transitionSpec = {
                fadeIn() togetherWith fadeOut()
            },
            label = "TabContent"
        ) { tab ->
            when (tab) {
                "overview" -> OverviewTab(uiState)
                "ledger" -> LedgerTab(uiState)
                "payment" -> PaymentTab(uiState, viewModel)
                "receipts" -> ReceiptsTab(uiState)
                "scholarship" -> ScholarshipTab(uiState)
                "loans" -> LoansTab(uiState)
                "assistance" -> AssistanceTab(uiState)
            }
        }
    }
}

@Composable
private fun Header(uiState: FeesUiState) {
    val summary = uiState.summary ?: return
    CamsCard(
        modifier = Modifier.fillMaxWidth(),
    ) {
        Column {
            Text(
                "CAMS / STUDENT / FINANCIAL SERVICES",
                style = MaterialTheme.typography.labelSmall.copy(
                    fontWeight = FontWeight.Black,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    letterSpacing = 1.sp
                )
            )
            Text(
                "My Fees & Finance",
                style = MaterialTheme.typography.headlineMedium.copy(
                    fontWeight = FontWeight.Black,
                    color = MaterialTheme.colorScheme.onSurface
                )
            )
            Spacer(modifier = Modifier.height(16.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                InfoBox("Pending Balance", "₹${summary.pendingBalance.toInt()}", Modifier.weight(1f))
                InfoBox("Due Date", summary.dueDate ?: "N/A", Modifier.weight(1f))
            }
        }
    }
}

@Composable
private fun InfoBox(label: String, value: String, modifier: Modifier = Modifier) {
    Surface(
        modifier = modifier,
        color = CamsBackground,
        shape = RoundedCornerShape(16.dp),
        border = BorderStroke(1.dp, Color.LightGray.copy(alpha = 0.2f))
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            Text(label.uppercase(), fontSize = 13.sp, fontWeight = FontWeight.Black, color = MaterialTheme.colorScheme.onSurfaceVariant)
            Text(value, fontSize = 16.sp, fontWeight = FontWeight.Black, color = MaterialTheme.colorScheme.onSurface)
        }
    }
}

@Composable
private fun TabSelector(activeTab: String, onTabSelected: (String) -> Unit) {
    val tabs = listOf(
        "overview" to "Overview",
        "ledger" to "Ledger",
        "payment" to "Payments",
        "receipts" to "Receipts",
        "scholarship" to "Scholarship",
        "loans" to "Loans",
        "assistance" to "Assistance"
    )
    
    LazyRow(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        contentPadding = PaddingValues(horizontal = 4.dp)
    ) {
        items(tabs) { (id, label) ->
            val isSelected = activeTab == id
            Surface(
                onClick = { onTabSelected(id) },
                color = if (isSelected) CamsNavy else Color.White,
                shape = RoundedCornerShape(12.dp),
                border = BorderStroke(1.dp, if (isSelected) CamsNavy else Color.LightGray.copy(alpha = 0.2f)),
                shadowElevation = if (isSelected) 4.dp else 0.dp
            ) {
                Text(
                    text = label,
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 10.dp),
                    style = MaterialTheme.typography.labelMedium.copy(
                        fontWeight = FontWeight.Bold,
                        color = if (isSelected) Color.White else CamsTextPrimary
                    )
                )
            }
        }
    }
}

// --- TABS ---

@Composable
private fun OverviewTab(uiState: FeesUiState) {
    val summary = uiState.summary ?: return
    Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
        val cards = listOf(
            Triple("Base Fees", "₹${summary.totalFees.toInt()}", Icons.Filled.Payments),
            Triple("Scholarship", "₹${summary.scholarshipDeduction.toInt()}", Icons.Filled.EmojiEvents),
            Triple("Paid", "₹${summary.amountPaid.toInt()}", Icons.Filled.CheckCircle)
        )
        
        Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            cards.forEach { (label, value, icon) ->
                Box(Modifier.weight(1f)) {
                    SummaryCard(
                        label = label,
                        value = value,
                        note = "Academic 2026",
                        icon = { Icon(icon, contentDescription = null) },
                        iconBg = if (label == "Scholarship") Color(0xFFECFDF5) else CamsBackground,
                        iconColor = if (label == "Scholarship") Color(0xFF047857) else CamsTextSecondary
                    )
                }
            }
        }

        // Progress
        CamsCard(
            modifier = Modifier.fillMaxWidth(),
        ) {
            Column {
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                    Text("Payment Progress", fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurface)
                    val pct = if (summary.netFees > 0) (summary.amountPaid / summary.netFees * 100).toInt() else 100
                    Text("$pct% Cleared", fontWeight = FontWeight.Black, color = CamsNavy)
                }
                Spacer(modifier = Modifier.height(12.dp))
                LinearProgressIndicator(
                    progress = if (summary.netFees > 0) (summary.amountPaid / summary.netFees).toFloat() else 1f,
                    modifier = Modifier.fillMaxWidth().height(8.dp).clip(CircleShape),
                    color = CamsNavy,
                    trackColor = CamsBackground
                )
                Spacer(modifier = Modifier.height(12.dp))
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                    Text("₹0", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    Text("Target: ₹${summary.netFees.toInt()}", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            }
        }
        
        FinancialAlerts(uiState.notifications)
    }
}

@Composable
private fun FinancialAlerts(alerts: List<FinancialNotification>) {
    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        SectionHeader(icon = { Icon(Icons.Filled.Notifications, null) }, title = "Financial Alerts", subtitle = "Recent billing notifications")
        alerts.forEach { alert ->
            val color = when(alert.type) {
                "warning" -> Color(0xFFFFF7ED)
                "success" -> Color(0xFFECFDF5)
                else -> Color(0xFFEFF6FF)
            }
            val textColor = when(alert.type) {
                "warning" -> Color(0xFF9A3412)
                "success" -> Color(0xFF065F46)
                else -> Color(0xFF1E40AF)
            }
            Surface(
                modifier = Modifier.fillMaxWidth(),
                color = color,
                shape = RoundedCornerShape(16.dp),
                border = BorderStroke(1.dp, textColor.copy(alpha = 0.1f))
            ) {
                Row(modifier = Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    Icon(
                        if (alert.type == "success") Icons.Filled.CheckCircle else Icons.Filled.Info,
                        null,
                        tint = textColor,
                        modifier = Modifier.size(20.dp)
                    )
                    Column {
                        Text(alert.message, fontSize = 12.sp, fontWeight = FontWeight.Bold, color = textColor)
                        Text(alert.time, fontSize = 12.sp, color = textColor.copy(alpha = 0.7f))
                    }
                }
            }
        }
    }
}

@Composable
private fun LedgerTab(uiState: FeesUiState) {
    val records = uiState.summary?.records ?: return
    Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
        SectionHeader(icon = { Icon(Icons.Filled.List, null) }, title = "Fee Ledger", subtitle = "Itemised breakdown of charges")
        records.forEach { record ->
            LedgerItem(record)
        }
    }
}

@Composable
private fun LedgerItem(record: FeeRecord) {
    CamsCard(
        modifier = Modifier.fillMaxWidth(),
    ) {
        Column {
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(record.feeType, fontWeight = FontWeight.Black, fontSize = 14.sp, color = MaterialTheme.colorScheme.onSurface)
                    Text("Due: ${record.dueDate}", fontSize = 13.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
                StatusBadge(record.status)
            }
            HorizontalDivider(modifier = Modifier.padding(vertical = 12.dp), color = Color.LightGray.copy(alpha = 0.2f))
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Column {
                    Text("Net Amount", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    Text("₹${record.amount.toInt()}", fontSize = 16.sp, fontWeight = FontWeight.Black, color = CamsNavy)
                }
                if (record.scholarshipAmount > 0) {
                    Column(horizontalAlignment = Alignment.End) {
                        Text("Scholarship", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = Color(0xFF059669))
                        Text("-₹${record.scholarshipAmount.toInt()}", fontSize = 12.sp, fontWeight = FontWeight.Black, color = Color(0xFF059669))
                    }
                }
            }
        }
    }
}

@Composable
private fun PaymentTab(uiState: FeesUiState, viewModel: FeesViewModel) {
    val pendingRecords = uiState.summary?.records?.filter { it.status != "paid" } ?: emptyList()
    var selectedRecordId by remember { mutableStateOf(pendingRecords.firstOrNull()?.id ?: "") }
    var amount by remember { mutableStateOf("") }
    
    LaunchedEffect(selectedRecordId) {
        val rec = pendingRecords.find { it.id == selectedRecordId }
        if (rec != null) amount = rec.amount.toInt().toString()
    }

    Column(verticalArrangement = Arrangement.spacedBy(20.dp)) {
        SectionHeader(icon = { Icon(Icons.Filled.Payments, null) }, title = "Online Payment", subtitle = "Securely pay your pending dues")
        
        if (pendingRecords.isEmpty()) {
            Box(Modifier.fillMaxWidth().height(200.dp), contentAlignment = Alignment.Center) {
                Text("No pending fees found.", color = MaterialTheme.colorScheme.onSurfaceVariant, fontWeight = FontWeight.Bold)
            }
        } else {
            CamsCard(
                modifier = Modifier.fillMaxWidth(),
            ) {
                Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                    // Selector
                    Column {
                        Text("Select Fee Head", fontSize = 12.sp, fontWeight = FontWeight.Black, color = MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.padding(bottom = 8.dp))
                        pendingRecords.forEach { r ->
                            val isSelected = selectedRecordId == r.id
                            Surface(
                                onClick = { selectedRecordId = r.id },
                                modifier = Modifier.fillMaxWidth().padding(bottom = 8.dp),
                                color = if (isSelected) CamsNavy.copy(alpha = 0.05f) else Color.White,
                                shape = RoundedCornerShape(12.dp),
                                border = BorderStroke(1.dp, if (isSelected) CamsNavy else Color.LightGray.copy(alpha = 0.2f))
                            ) {
                                Row(Modifier.padding(12.dp), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                                    RadioButton(selected = isSelected, onClick = null, colors = RadioButtonDefaults.colors(selectedColor = CamsNavy))
                                    Column {
                                        Text(r.feeType, fontWeight = FontWeight.Bold, fontSize = 13.sp, color = MaterialTheme.colorScheme.onSurface)
                                        Text("Balance: ₹${r.amount.toInt()}", fontSize = 13.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                    }
                                }
                            }
                        }
                    }

                    // Amount
                    OutlinedTextField(
                        value = amount,
                        onValueChange = { amount = it },
                        modifier = Modifier.fillMaxWidth(),
                        label = { Text("Payment Amount (₹)") },
                        shape = RoundedCornerShape(16.dp),
                        prefix = { Text("₹") },
                        colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = CamsNavy, focusedLabelColor = CamsNavy)
                    )

                    Button(
                        onClick = { viewModel.payFee(selectedRecordId, amount.toDoubleOrNull() ?: 0.0, "UPI") },
                        modifier = Modifier.fillMaxWidth().height(56.dp),
                        shape = RoundedCornerShape(16.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = CamsNavy)
                    ) {
                        Icon(Icons.Filled.Shield, null)
                        Spacer(Modifier.width(8.dp))
                        Text("Pay ₹$amount Securely", fontWeight = FontWeight.Black)
                    }
                }
            }
        }
    }
}

@Composable
private fun ReceiptsTab(uiState: FeesUiState) {
    val receipts = uiState.receipts
    Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
        SectionHeader(icon = { Icon(Icons.Filled.Download, null) }, title = "Receipt Center", subtitle = "View and download past invoices")
        receipts.forEach { r ->
            CamsCard(
                modifier = Modifier.fillMaxWidth(),
            ) {
                Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    Box(Modifier.size(40.dp).background(CamsBackground, RoundedCornerShape(12.dp)), contentAlignment = Alignment.Center) {
                        Icon(Icons.Filled.FilePresent, null, tint = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                    Column(modifier = Modifier.weight(1f)) {
                        Text(r.head, fontWeight = FontWeight.Bold, fontSize = 13.sp, maxLines = 1, overflow = TextOverflow.Ellipsis, color = MaterialTheme.colorScheme.onSurface)
                        Text("${r.date} • ${r.mode}", fontSize = 13.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                    Column(horizontalAlignment = Alignment.End) {
                        Text("₹${r.amount.toInt()}", fontWeight = FontWeight.Black, fontSize = 14.sp, color = MaterialTheme.colorScheme.onSurface)
                        IconButton(onClick = { /* Download */ }, modifier = Modifier.size(24.dp)) {
                            Icon(Icons.Filled.Download, null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(16.dp))
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun ScholarshipTab(uiState: FeesUiState) {
    val scholarships = uiState.scholarships
    val summary = uiState.summary ?: return
    Column(verticalArrangement = Arrangement.spacedBy(20.dp)) {
        SectionHeader(icon = { Icon(Icons.Filled.EmojiEvents, null) }, title = "Scholarship Management", subtitle = "Your active benefits and status")
        
        if (summary.scholarshipDeduction > 0) {
            CamsCard(
                modifier = Modifier.fillMaxWidth(),
            ) {
                Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                    Box(Modifier.size(48.dp).background(Color(0xFFD1FAE5), CircleShape), contentAlignment = Alignment.Center) {
                        Icon(Icons.Filled.Verified, null, tint = Color(0xFF059669))
                    }
                    Column(modifier = Modifier.weight(1f)) {
                        Text("Benefit Active", fontSize = 12.sp, fontWeight = FontWeight.Black, color = Color(0xFF059669))
                        Text(summary.assignedScholarshipName ?: "Institutional Scholarship", fontWeight = FontWeight.Black, fontSize = 16.sp, color = Color(0xFF065F46))
                        Text("-₹${summary.scholarshipDeduction.toInt()} Deducted from Fees", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = Color(0xFF059669))
                    }
                }
            }
        } else {
             Box(Modifier.fillMaxWidth().height(150.dp), contentAlignment = Alignment.Center) {
                Text("No active scholarship assigned.", color = MaterialTheme.colorScheme.onSurfaceVariant, fontWeight = FontWeight.Bold)
            }
        }

        // Docs
        CamsCard(
            modifier = Modifier.fillMaxWidth(),
        ) {
            Column {
                Text("Supporting Documents", fontWeight = FontWeight.Bold, modifier = Modifier.padding(bottom = 12.dp), color = MaterialTheme.colorScheme.onSurface)
                listOf("Income Certificate", "Caste Certificate", "Aadhaar Card").forEach { doc ->
                    Row(
                        modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(doc, fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        TextButton(onClick = { /* Upload */ }) {
                            Icon(Icons.Filled.Upload, null, modifier = Modifier.size(14.dp), tint = MaterialTheme.colorScheme.primary)
                            Spacer(Modifier.width(4.dp))
                            Text("Upload", fontSize = 13.sp, fontWeight = FontWeight.Bold, color = CamsNavy)
                        }
                    }
                    if (doc != "Aadhaar Card") {
                        HorizontalDivider(color = Color.LightGray.copy(alpha = 0.2f))
                    }
                }
            }
        }
    }
}

@Composable
private fun LoansTab(uiState: FeesUiState) {
    val loan = uiState.loanDetails ?: return
    Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
        SectionHeader(icon = { Icon(Icons.Filled.AccountBalance, null) }, title = "Education Loan", subtitle = "Bank details and repayment info")
        
        CamsCard(
            modifier = Modifier.fillMaxWidth(),
        ) {
            Column {
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    Box(Modifier.size(40.dp).background(CamsBackground, CircleShape), contentAlignment = Alignment.Center) {
                         Icon(Icons.Filled.Business, null, tint = MaterialTheme.colorScheme.primary)
                    }
                    Column {
                        Text(loan.bank, fontWeight = FontWeight.Black, fontSize = 15.sp, color = MaterialTheme.colorScheme.onSurface)
                        Text(loan.branch, fontSize = 13.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                    Spacer(Modifier.weight(1f))
                    StatusBadge(loan.status)
                }
                Spacer(Modifier.height(20.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                    LoanInfoItem("Sanctioned", "₹${loan.sanctioned.toInt()}", Modifier.weight(1f))
                    LoanInfoItem("Interest", "${loan.interestRate}%", Modifier.weight(1f))
                    LoanInfoItem("EMI", "₹${loan.emi.toInt()}", Modifier.weight(1f))
                }
                Spacer(Modifier.height(16.dp))
                Surface(color = CamsBackground, shape = RoundedCornerShape(12.dp), modifier = Modifier.fillMaxWidth()) {
                    Row(Modifier.padding(12.dp), horizontalArrangement = Arrangement.SpaceBetween) {
                        Text("Outstanding Balance", fontSize = 13.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        Text("₹${loan.outstanding.toInt()}", fontSize = 13.sp, fontWeight = FontWeight.Black, color = MaterialTheme.colorScheme.onSurface)
                    }
                }
            }
        }
    }
}

@Composable
private fun LoanInfoItem(label: String, value: String, modifier: Modifier = Modifier) {
    Column(modifier = modifier) {
        Text(label.uppercase(), fontSize = 13.sp, fontWeight = FontWeight.Black, color = LexNovaSlateAccent)
        Text(value, fontSize = 13.sp, fontWeight = FontWeight.Black, color = LexNovaSlateDark)
    }
}

@Composable
private fun AssistanceTab(uiState: FeesUiState) {
    val requests = uiState.requests
    Column(verticalArrangement = Arrangement.spacedBy(20.dp)) {
        SectionHeader(icon = { Icon(Icons.Filled.SupportAgent, null) }, title = "Financial Assistance", subtitle = "Apply for concessions and aid")
        
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(24.dp),
            colors = CardDefaults.cardColors(containerColor = CamsNavy),
            elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
        ) {
            Column(modifier = Modifier.padding(24.dp)) {
                Text("New Application", color = Color.White.copy(alpha = 0.7f), fontSize = 13.sp, fontWeight = FontWeight.Bold)
                Text("Apply for Fee Concession", color = Color.White, fontWeight = FontWeight.Black, fontSize = 18.sp)
                Spacer(Modifier.height(12.dp))
                Text("Submit your academic or socio-economic credentials for administrative review.", color = Color.White.copy(alpha = 0.8f), fontSize = 12.sp)
                Spacer(Modifier.height(20.dp))
                Button(
                    onClick = { /* Apply */ },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.surface, contentColor = CamsNavy)
                ) {
                    Text("Begin Application", fontWeight = FontWeight.Black)
                }
            }
        }

        // History
        Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
            Text("Request History", fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurface)
            requests.forEach { r ->
                CamsCard(
                    modifier = Modifier.fillMaxWidth(),
                ) {
                    Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(r.type, fontWeight = FontWeight.Bold, fontSize = 13.sp, color = MaterialTheme.colorScheme.onSurface)
                            Text(r.date, fontSize = 13.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                        StatusBadge(r.status)
                    }
                }
            }
        }
    }
}

@Composable
private fun FeesSkeleton() {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(20.dp)
    ) {
        // Header Skeleton
        Box(Modifier.fillMaxWidth().height(180.dp).shimmerEffect().clip(RoundedCornerShape(32.dp)))
        
        // Tab Selector Skeleton
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            repeat(4) {
                Box(Modifier.width(100.dp).height(40.dp).shimmerEffect().clip(RoundedCornerShape(12.dp)))
            }
        }
        
        // Cards Skeleton
        Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            repeat(3) {
                Box(Modifier.weight(1f).height(120.dp).shimmerEffect().clip(RoundedCornerShape(24.dp)))
            }
        }
        
        // Large Card Skeleton
        Box(Modifier.fillMaxWidth().height(140.dp).shimmerEffect().clip(RoundedCornerShape(24.dp)))
        
        // List Skeleton
        repeat(3) {
            Box(Modifier.fillMaxWidth().height(80.dp).shimmerEffect().clip(RoundedCornerShape(20.dp)))
        }
    }
}
