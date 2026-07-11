package com.example.core.ui

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.example.core.network.NetworkMonitor
import kotlinx.coroutines.delay

@Composable
fun GlobalNetworkMonitorBanner(networkMonitor: NetworkMonitor) {
    val isConnected by networkMonitor.isConnected.collectAsState()
    var wasDisconnected by remember { mutableStateOf(false) }
    var showBackOnline by remember { mutableStateOf(false) }

    LaunchedEffect(isConnected) {
        if (!isConnected) {
            wasDisconnected = true
            showBackOnline = false
        } else if (wasDisconnected) {
            showBackOnline = true
            delay(3000)
            showBackOnline = false
            wasDisconnected = false
        }
    }

    AnimatedVisibility(
        visible = !isConnected || showBackOnline,
        enter = expandVertically(),
        exit = shrinkVertically()
    ) {
        val backgroundColor = if (!isConnected) Color.Red else Color(0xFF10B981) // Green
        val message = if (!isConnected) "No internet connection" else "Back online"
        
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(backgroundColor)
                .padding(vertical = 8.dp, horizontal = 16.dp),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = message,
                color = Color.White,
                fontWeight = FontWeight.Bold,
                textAlign = TextAlign.Center
            )
        }
    }
}
