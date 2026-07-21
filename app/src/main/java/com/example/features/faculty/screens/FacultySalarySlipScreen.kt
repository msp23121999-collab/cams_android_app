package com.example.features.faculty.screens

import android.widget.Toast
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.core.theme.*
import com.example.features.faculty.widgets.FacultyBaseScreen

import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.core.repository.FacultyRepositoryImpl
import com.example.features.faculty.providers.FacultySalaryViewModel
import com.example.features.faculty.providers.FacultySalaryViewModelFactory
import java.text.DateFormatSymbols

@Composable
fun FacultySalarySlipScreen(onNavigate: (String) -> Unit) {
    val repository = remember { FacultyRepositoryImpl(com.example.CamsApplication.instance.container.apiService) }
    val factory = remember { FacultySalaryViewModelFactory(repository) }
    val viewModel: FacultySalaryViewModel = viewModel(factory = factory)
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    FacultyBaseScreen(scrollable = false, 
        title = "Salary Slips",
        currentRoute = com.example.core.navigation.AppRoutes.FACULTY_SALARY_SLIP,
        onNavigate = onNavigate
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp)
        ) {
            // Summary Card
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = CamsNavy),
                shape = RoundedCornerShape(24.dp)
            ) {
                Column(
                    modifier = Modifier.padding(24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text("Total Net Pay (Last Month)", color = Color.White.copy(alpha = 0.7f), fontSize = 14.sp)
                        val lastSlip = uiState.slips.firstOrNull()
                        val netPay = lastSlip?.netSalary ?: 0.0
                        val earnings = lastSlip?.basic ?: 0.0
                        val deductions = lastSlip?.totalDeductions ?: 0.0
                    Text("₹ %,.2f".format(netPay), color = Color.White, fontSize = 32.sp, fontWeight = FontWeight.Black)
                    Spacer(modifier = Modifier.height(16.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceEvenly
                    ) {
                        SummaryItem("Earnings", "₹ %,.0f".format(earnings), Icons.Filled.TrendingUp)
                        SummaryItem("Deductions", "₹ %,.0f".format(deductions), Icons.Filled.TrendingDown)
                    }
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            Text("Payment History", fontWeight = FontWeight.Bold, fontSize = 18.sp, color = MaterialTheme.colorScheme.onSurface)
            Spacer(modifier = Modifier.height(12.dp))

            if (uiState.isLoading) {
                Box(Modifier.fillMaxWidth().weight(1f), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator(color = CamsNavy)
                }
            } else if (uiState.error != null) {
                Box(Modifier.fillMaxWidth().weight(1f), contentAlignment = Alignment.Center) {
                    Text(uiState.error ?: "Failed to load salary slips", color = Color(0xFFB91C1C))
                }
            } else if (uiState.slips.isEmpty()) {
                Box(Modifier.fillMaxWidth().weight(1f), contentAlignment = Alignment.Center) {
                    Text("No salary slips available", color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            } else {
                LazyColumn(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    items(uiState.slips) { slip ->
                        SalarySlipItem(slip)
                    }
                }
            }
        }
    }
}

@Composable
private fun SummaryItem(label: String, value: String, icon: ImageVector) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Box(
            modifier = Modifier
                .size(32.dp)
                .background(MaterialTheme.colorScheme.surface.copy(alpha = 0.2f), CircleShape),
            contentAlignment = Alignment.Center
        ) {
            Icon(icon, null, tint = Color.White, modifier = Modifier.size(16.dp))
        }
        Spacer(modifier = Modifier.height(4.dp))
        Text(value, color = Color.White, fontWeight = FontWeight.Bold, fontSize = 14.sp)
        Text(label, color = Color.White.copy(alpha = 0.7f), fontSize = 12.sp)
    }
}

@Composable
private fun SalarySlipItem(slip: com.example.core.network.FacultySalarySlipDto) {
    val context = LocalContext.current
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        shape = RoundedCornerShape(16.dp)
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(horizontalArrangement = Arrangement.spacedBy(12.dp), verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .size(40.dp)
                        .background(CamsNavy.copy(alpha = 0.1f), CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(Icons.Filled.ReceiptLong, null, tint = MaterialTheme.colorScheme.primary)
                }
                    val monthName = DateFormatSymbols().months.getOrNull(slip.month - 1) ?: "Unknown"
                    Text("$monthName ${slip.year}", fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurface)
                }
            Column(horizontalAlignment = Alignment.End) {
                Text("₹ %,.2f".format(slip.netSalary), fontWeight = FontWeight.Black, color = CamsNavy)
                IconButton(
                    onClick = {
                        val url = slip.pdfUrl
                        if (url.isNullOrBlank()) {
                            Toast.makeText(context, "No PDF available for this slip", Toast.LENGTH_SHORT).show()
                            return@IconButton
                        }
                        val token = com.example.core.network.AuthManagerImpl(context).getToken() ?: ""
                        val base = com.example.core.config.AppConfig.BASE_URL
                        val origin = base.substringBefore("/api/v1")
                        val fullUrl = if (url.startsWith("http")) url else origin + url
                        val monthName = DateFormatSymbols().months.getOrNull(slip.month - 1) ?: "slip"
                        com.example.core.utils.DownloadHelper.downloadPdf(context, fullUrl, "Salary_${monthName}_${slip.year}", token)
                    },
                    modifier = Modifier.size(40.dp)
                ) {
                    Icon(Icons.Filled.Download, "Download", tint = Color(0xFF10B981), modifier = Modifier.size(16.dp))
                }
            }
        }
    }
}


