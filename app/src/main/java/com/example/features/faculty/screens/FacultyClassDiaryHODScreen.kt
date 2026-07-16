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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.core.theme.*
import com.example.features.faculty.widgets.FacultyBaseScreen

@Composable
fun FacultyClassDiaryHODScreen(onNavigate: (String) -> Unit) {
    FacultyBaseScreen(scrollable = false, 
        title = "Class Diary (HOD Review)",
        currentRoute = "/faculty/class-diary-hod",
        onNavigate = onNavigate
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp)
        ) {
            Text("HOD Remarks & Approval", fontWeight = FontWeight.Bold, fontSize = 18.sp, color = MaterialTheme.colorScheme.onSurface)
            Spacer(modifier = Modifier.height(12.dp))

            val reviews = listOf(
                DiaryReview("12 Oct 2023", "CS101", "Approved", "Well documented topics. Ensure students submit the assignment by Friday."),
                DiaryReview("11 Oct 2023", "CS302", "Correction Needed", "Please specify the lab experiments covered in detail."),
                DiaryReview("10 Oct 2023", "CS505", "Approved", "Excellent progress on the mobile app module.")
            )

            LazyColumn(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                items(reviews) { review ->
                    DiaryReviewItem(review)
                }
            }
        }
    }
}

@Composable
private fun DiaryReviewItem(review: DiaryReview) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        shape = RoundedCornerShape(16.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(review.date, fontSize = 12.sp, color = Color(0xFF64748B), fontWeight = FontWeight.Bold)
                    Text("Class: ${review.className}", fontWeight = FontWeight.Bold, color = CamsNavy, fontSize = 14.sp)
                }
                Surface(
                    color = if (review.status == "Approved") Color(0xFF10B981).copy(alpha = 0.1f) else Color(0xFFEF4444).copy(alpha = 0.1f),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Text(
                        review.status,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = if (review.status == "Approved") Color(0xFF10B981) else Color(0xFFEF4444)
                    )
                }
            }
            Spacer(modifier = Modifier.height(12.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalAlignment = Alignment.Top) {
                Icon(Icons.Filled.Comment, null, tint = Color(0xFF64748B), modifier = Modifier.size(16.dp))
                Column {
                    Text("HOD Remarks:", fontSize = 13.sp, color = Color(0xFF64748B), fontWeight = FontWeight.Bold)
                    Text(review.remarks, fontSize = 13.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            }
        }
    }
}

data class DiaryReview(val date: String, val className: String, val status: String, val remarks: String)
