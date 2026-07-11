package com.example.features.principal.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
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
import com.example.core.theme.*
import com.example.core.ui.CamsCard
import com.example.features.principal.widgets.PrincipalBaseScreen

@Composable
fun PrincipalInstitutionalPerformanceScreen(onNavigate: (String) -> Unit) {
    PrincipalBaseScreen(
        title = "Institutional Performance",
        currentRoute = "/principal/performance",
        onNavigate = onNavigate
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Text("Institutional Metrics", fontWeight = FontWeight.Bold, fontSize = 18.sp, color = CamsTextPrimary)

            val metrics = listOf(
                Metric("Admission Rate", "88%", 0.88f, Color(0xFF3B82F6)),
                Metric("Graduation Rate", "92%", 0.92f, Color(0xFF10B981)),
                Metric("Placement Success", "75%", 0.75f, Color(0xFFF59E0B)),
                Metric("Research Output", "65%", 0.65f, Color(0xFF8B5CF6))
            )

            metrics.forEach { metric ->
                MetricCard(metric)
            }
        }
    }
}

@Composable
private fun MetricCard(metric: Metric) {
    CamsCard {
        Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                Text(metric.label, fontWeight = FontWeight.Bold, fontSize = 16.sp, color = CamsTextPrimary)
                Text(metric.value, fontWeight = FontWeight.Black, fontSize = 18.sp, color = metric.color)
            }
            LinearProgressIndicator(
                progress = metric.progress,
                modifier = Modifier.fillMaxWidth().height(8.dp),
                color = metric.color,
                trackColor = metric.color.copy(alpha = 0.1f),
                strokeCap = androidx.compose.ui.graphics.StrokeCap.Round
            )
        }
    }
}

data class Metric(val label: String, val value: String, val progress: Float, val color: Color)
