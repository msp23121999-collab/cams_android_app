package com.example.features.fees.widgets

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.core.theme.LexNovaPurple
import com.example.core.theme.LexNovaSlateMedium

@Composable
fun StatusBadge(status: String) {
    val (bgColor, textColor, label) = when (status.lowercase()) {
        "paid" -> Triple(Color(0xFFECFDF5), Color(0xFF047857), "✓ Paid")
        "partially_paid" -> Triple(Color(0xFFEFF6FF), Color(0xFF1D4ED8), "⏳ Partially Paid")
        "pending" -> Triple(Color(0xFFFFFBEB), Color(0xFFB45309), "⏳ Pending")
        "overdue" -> Triple(Color(0xFFFEF2F2), Color(0xFFB91C1C), "⚠ Overdue")
        "approved" -> Triple(Color(0xFFECFDF5), Color(0xFF047857), "✓ Approved")
        "rejected" -> Triple(Color(0xFFFEF2F2), Color(0xFFB91C1C), "✗ Rejected")
        "active" -> Triple(Color(0xFFEFF6FF), Color(0xFF1D4ED8), "● Active")
        "under review" -> Triple(MaterialTheme.colorScheme.secondaryContainer, Color(0xFF6D28D9), "⟳ Under Review")
        else -> Triple(MaterialTheme.colorScheme.background, Color(0xFF64748B), status.uppercase())
    }

    Surface(
        color = bgColor,
        shape = RoundedCornerShape(100.dp),
        border = border(status)
    ) {
        Text(
            text = label,
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 2.dp),
            style = MaterialTheme.typography.labelSmall.copy(
                color = textColor,
                fontWeight = FontWeight.Bold,
                fontSize = 12.sp
            )
        )
    }
}

@Composable
private fun border(status: String): androidx.compose.foundation.BorderStroke? {
    val color = when (status.lowercase()) {
        "paid" -> Color(0xFFD1FAE5)
        "partially_paid" -> Color(0xFFDBEAFE)
        "pending" -> Color(0xFFFEF3C7)
        "overdue" -> Color(0xFFFEE2E2)
        "approved" -> Color(0xFFD1FAE5)
        "rejected" -> Color(0xFFFEE2E2)
        "active" -> Color(0xFFDBEAFE)
        "under review" -> Color(0xFFDDD6FE)
        else -> MaterialTheme.colorScheme.surfaceVariant
    }
    return androidx.compose.foundation.BorderStroke(1.dp, color)
}

@Composable
fun SectionHeader(
    icon: @Composable () -> Unit,
    title: String,
    subtitle: String? = null
) {
    Row(
        modifier = Modifier.fillMaxWidth().padding(bottom = 16.dp),
        verticalAlignment = Alignment.Top,
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Box(
            modifier = Modifier
                .size(40.dp)
                .background(Color(0xFFF3E8FF), RoundedCornerShape(12.dp)),
            contentAlignment = Alignment.Center
        ) {
            CompositionLocalProvider(LocalContentColor provides LexNovaPurple) {
                icon()
            }
        }
        Column {
            Text(
                text = title,
                style = MaterialTheme.typography.titleMedium.copy(
                    fontWeight = FontWeight.Black,
                    color = MaterialTheme.colorScheme.onSurface
                )
            )
            if (subtitle != null) {
                Text(
                    text = subtitle,
                    style = MaterialTheme.typography.bodySmall.copy(
                        fontWeight = FontWeight.Bold,
                        color = LexNovaSlateMedium,
                        fontSize = 13.sp
                    )
                )
            }
        }
    }
}

@Composable
fun SummaryCard(
    label: String,
    value: String,
    note: String,
    icon: @Composable () -> Unit,
    iconBg: Color,
    iconColor: Color
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.surfaceVariant)
    ) {
        Column(modifier = Modifier.padding(20.dp)) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                modifier = Modifier.padding(bottom = 12.dp)
            ) {
                Box(
                    modifier = Modifier
                        .size(32.dp)
                        .background(iconBg, RoundedCornerShape(8.dp)),
                    contentAlignment = Alignment.Center
                ) {
                    CompositionLocalProvider(LocalContentColor provides iconColor) {
                        icon()
                    }
                }
                Text(
                    text = label,
                    style = MaterialTheme.typography.labelMedium.copy(
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                )
            }
            Text(
                text = value,
                style = MaterialTheme.typography.headlineSmall.copy(
                    fontWeight = FontWeight.Black,
                    color = MaterialTheme.colorScheme.onSurface
                )
            )
            Text(
                text = note,
                style = MaterialTheme.typography.bodySmall.copy(
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    fontSize = 13.sp
                )
            )
        }
    }
}
