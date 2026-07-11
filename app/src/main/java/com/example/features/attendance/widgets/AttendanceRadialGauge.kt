package com.example.features.attendance.widgets

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.core.theme.LexNovaError
import com.example.core.theme.LexNovaSlateDark
import com.example.core.theme.LexNovaSlateMedium

@Composable
fun AttendanceRadialGauge(
    percentage: Double,
    modifier: Modifier = Modifier
) {
    val isBelowTarget = percentage < 75.0
    val progressColor = if (isBelowTarget) LexNovaError else Color(0xFF10B981)
    val backgroundColor = androidx.compose.ui.graphics.Color.White.copy(alpha = 0.6f)

    Box(modifier = modifier.size(144.dp), contentAlignment = Alignment.Center) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            val strokeWidth = 7.dp.toPx()
            val radius = (size.minDimension - strokeWidth) / 2
            
            // Draw background circle
            drawCircle(
                color = backgroundColor,
                radius = radius,
                style = Stroke(width = strokeWidth)
            )

            // Draw progress arc
            val sweepAngle = (percentage / 100.0) * 360f
            drawArc(
                color = progressColor,
                startAngle = -90f, // Start at 12 o'clock
                sweepAngle = sweepAngle.toFloat(),
                useCenter = false,
                topLeft = Offset(size.width / 2 - radius, size.height / 2 - radius),
                size = Size(radius * 2, radius * 2),
                style = Stroke(width = strokeWidth, cap = StrokeCap.Round)
            )
        }
        
        Column(
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = "${percentage.toInt()}%",
                fontSize = 28.sp,
                fontWeight = FontWeight.Black,
                color = LexNovaSlateDark
            )
            Text(
                text = "ATTENDED",
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold,
                color = LexNovaSlateMedium,
                letterSpacing = 1.sp
            )
        }
    }
}
