package com.example.core.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.core.theme.*

import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import com.example.core.network.GlobalNetworkHandler

@Composable
fun CamsScreen(
    title: String,
    subtitle: String? = null,
    onBackClick: (() -> Unit)? = null,
    navigationIcon: @Composable (() -> Unit)? = null,
    actions: @Composable (RowScope.() -> Unit)? = null,
    floatingActionButton: @Composable (() -> Unit)? = null,
    scrollable: Boolean = true,
    verticalArrangement: Arrangement.Vertical = Arrangement.Top,
    onRetry: (() -> Unit)? = null,
    isOfflineMode: Boolean = false,
    content: @Composable ColumnScope.() -> Unit
) {
    val networkError by GlobalNetworkHandler.networkError.collectAsState()

    Scaffold(
        modifier = Modifier.systemBarsPadding().imePadding(),
        containerColor = CamsBackground,
        floatingActionButton = floatingActionButton ?: {},
        topBar = {
            // We use a custom header instead of a standard TopAppBar for the curved effect
        }
    ) { padding ->
        Column(modifier = Modifier.fillMaxSize().padding(padding)) {

            if (isOfflineMode) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(Color(0xFFE65100))
                        .padding(vertical = 8.dp, horizontal = 16.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        // No offline write queue exists, so don't promise syncing.
                        "You're offline. Reconnect to load the latest data.",
                        color = Color.White,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
            Box(modifier = Modifier.fillMaxSize().weight(1f, fill = false)) {
                // 1. Curved Navy Blue Header
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(240.dp) // Fixed height for header background
                        .clip(RoundedCornerShape(bottomStart = 40.dp, bottomEnd = 40.dp))
                        .background(CamsNavy)
                )
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                ) {
                    // Header Content
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 20.dp, vertical = 20.dp)
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            if (onBackClick != null) {
                                IconButton(onClick = onBackClick) {
                                    Icon(Icons.Filled.ArrowBack, contentDescription = "Back", tint = Color.White)
                                }
                                Spacer(modifier = Modifier.width(4.dp))
                            } else if (navigationIcon != null) {
                                navigationIcon()
                                Spacer(modifier = Modifier.width(8.dp))
                            }
                            
                            Text(
                                title,
                                style = MaterialTheme.typography.headlineMedium.copy(
                                    color = Color.White,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 24.sp
                                ),
                                modifier = Modifier.weight(1f)
                            )
                            if (actions != null) {
                                Row(content = actions)
                            }
                        }
                        if (subtitle != null) {
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                subtitle,
                                style = MaterialTheme.typography.bodyMedium.copy(
                                    color = Color.White.copy(alpha = 0.7f),
                                    fontSize = 14.sp
                                ),
                                modifier = Modifier.padding(horizontal = if (onBackClick != null) 48.dp else 0.dp)
                            )
                        }
                    }
                    // Main Content
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .then(if (scrollable) Modifier.verticalScroll(rememberScrollState()) else Modifier)
                            .padding(start = 16.dp, end = 16.dp, top = 16.dp, bottom = 40.dp),
                        verticalArrangement = verticalArrangement
                    ) {
                        content()
                    }
                }
            }
        }
    }
}

@Composable
fun CamsCard(
    modifier: Modifier = Modifier,
    onClick: (() -> Unit)? = null,
    containerColor: Color = Color.White,
    content: @Composable ColumnScope.() -> Unit
) {
    Surface(
        onClick = onClick ?: {},
        enabled = onClick != null,
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        color = containerColor,
        shadowElevation = 2.dp, // Subtle shadow
        tonalElevation = 0.dp
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            content = content
        )
    }
}
