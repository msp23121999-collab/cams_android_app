package com.example.features.faculty.screens

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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.core.theme.*
import com.example.features.faculty.widgets.FacultyBaseScreen

@Composable
fun FacultySalarySlipScreen(onNavigate: (String) -> Unit) {
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
                    Text("₹ 85,450.00", color = Color.White, fontSize = 32.sp, fontWeight = FontWeight.Black)
                    Spacer(modifier = Modifier.height(16.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceEvenly
                    ) {
                        SummaryItem("Earnings", "₹ 95,000", Icons.Filled.TrendingUp)
                        SummaryItem("Deductions", "₹ 9,550", Icons.Filled.TrendingDown)
                    }
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            Text("Payment History", fontWeight = FontWeight.Bold, fontSize = 18.sp, color = CamsTextPrimary)
            Spacer(modifier = Modifier.height(12.dp))

            val salarySlips = listOf(
                SalarySlip("October 2023", "₹ 85,450.00", "Paid on 01 Oct"),
                SalarySlip("September 2023", "₹ 85,450.00", "Paid on 01 Sep"),
                SalarySlip("August 2023", "₹ 82,200.00", "Paid on 02 Aug"),
                SalarySlip("July 2023", "₹ 82,200.00", "Paid on 01 Jul")
            )

            LazyColumn(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                items(salarySlips) { slip ->
                    SalarySlipItem(slip)
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
private fun SalarySlipItem(slip: SalarySlip) {
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
                    Icon(Icons.Filled.ReceiptLong, null, tint = CamsNavy)
                }
                Column {
                    Text(slip.month, fontWeight = FontWeight.Bold, color = CamsTextPrimary)
                    Text(slip.status, fontSize = 12.sp, color = CamsTextSecondary)
                }
            }
            Column(horizontalAlignment = Alignment.End) {
                Text(slip.amount, fontWeight = FontWeight.Black, color = CamsNavy)
                IconButton(onClick = { /* Download PDF */ }, modifier = Modifier.size(24.dp)) {
                    Icon(Icons.Filled.Download, "Download", tint = Color(0xFF10B981), modifier = Modifier.size(16.dp))
                }
            }
        }
    }
}

data class SalarySlip(val month: String, val amount: String, val status: String)
