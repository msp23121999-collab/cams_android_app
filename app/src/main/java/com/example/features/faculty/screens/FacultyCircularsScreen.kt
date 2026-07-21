package com.example.features.faculty.screens

import android.content.Intent
import android.net.Uri
import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.core.theme.*
import com.example.features.faculty.widgets.FacultyBaseScreen

import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.core.repository.FacultyRepositoryImpl
import com.example.features.faculty.providers.FacultyCircularsViewModel
import com.example.features.faculty.providers.FacultyCircularsViewModelFactory
import com.example.features.parent.models.CollegeNotice

@Composable
fun FacultyCircularsScreen(onNavigate: (String) -> Unit) {
    val context = LocalContext.current
    val repository = remember { FacultyRepositoryImpl(com.example.CamsApplication.instance.container.apiService) }
    val factory = remember { FacultyCircularsViewModelFactory(repository) }
    val viewModel: FacultyCircularsViewModel = viewModel(factory = factory)
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    FacultyBaseScreen(scrollable = false,
        title = "Circulars & Notices",
        currentRoute = com.example.core.navigation.AppRoutes.FACULTY_CIRCULARS,
        onNavigate = onNavigate
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp)
        ) {
            uiState.error?.let {
                Text(it, color = Color(0xFFB91C1C), fontSize = 13.sp, modifier = Modifier.padding(bottom = 8.dp))
            }

            if (uiState.isLoading) {
                Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator(color = CamsNavy)
                }
            } else if (uiState.circulars.isEmpty()) {
                Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Text("No circulars posted yet", color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            } else {
                LazyColumn(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    items(uiState.circulars, key = { it.id }) { circular ->
                        CircularItem(circular, onViewDocument = {
                            val url = circular.attachmentUrl
                            if (url.isNullOrBlank()) {
                                Toast.makeText(context, "No document attached", Toast.LENGTH_SHORT).show()
                            } else {
                                val base = com.example.core.config.AppConfig.BASE_URL
                                val origin = base.substringBefore("/api/v1")
                                val fullUrl = if (url.startsWith("http")) url else origin + url
                                try {
                                    context.startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(fullUrl)))
                                } catch (e: Exception) {
                                    Toast.makeText(context, "Could not open document", Toast.LENGTH_SHORT).show()
                                }
                            }
                        })
                    }
                }
            }
        }
    }
}

@Composable
private fun CircularItem(circular: CollegeNotice, onViewDocument: () -> Unit) {
    val tagColor = when (circular.priority) {
        "High" -> Color(0xFFB91C1C)
        "Medium" -> Color(0xFFF59E0B)
        else -> Color(0xFF3B82F6)
    }
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
                Surface(
                    color = tagColor.copy(alpha = 0.1f),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Text(
                        circular.category,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = tagColor
                    )
                }
                Text(circular.publishDate.take(10), fontSize = 13.sp, color = Color(0xFF64748B))
            }
            Spacer(modifier = Modifier.height(12.dp))
            Text(circular.title, fontWeight = FontWeight.Bold, fontSize = 16.sp, color = MaterialTheme.colorScheme.onSurface)
            Text(circular.body, fontSize = 13.sp, color = MaterialTheme.colorScheme.onSurfaceVariant, maxLines = 3)
            Spacer(modifier = Modifier.height(4.dp))
            Text("Issued by: ${circular.publisherName}", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
            Spacer(modifier = Modifier.height(16.dp))
            OutlinedButton(
                onClick = onViewDocument,
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFE5E7EB))
            ) {
                Icon(Icons.Filled.PictureAsPdf, null, modifier = Modifier.size(18.dp), tint = Color.Red)
                Spacer(modifier = Modifier.width(8.dp))
                Text("View Document", color = MaterialTheme.colorScheme.onSurface)
            }
        }
    }
}
