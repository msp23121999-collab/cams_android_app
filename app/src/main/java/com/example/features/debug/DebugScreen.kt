package com.example.features.debug

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.example.core.config.AppConfig
import com.example.core.network.GlobalNetworkHandler
import kotlinx.coroutines.launch
import java.net.HttpURLConnection
import java.net.URL
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DebugScreen(onBack: () -> Unit) {
    val coroutineScope = rememberCoroutineScope()
    var currentUrl by remember { mutableStateOf(AppConfig.BASE_URL) }
    var connectionStatus by remember { mutableStateOf("Not tested") }
    val networkError by GlobalNetworkHandler.networkError.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Debug Settings") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Filled.ArrowBack, "Back")
                    }
                }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Text("API Configuration", style = MaterialTheme.typography.titleLarge, fontWeight = androidx.compose.ui.text.font.FontWeight.Bold)
            
            OutlinedTextField(
                value = currentUrl,
                onValueChange = { currentUrl = it },
                label = { Text("Override BASE_URL") },
                modifier = Modifier.fillMaxWidth()
            )

            Button(
                onClick = {
                    // Override global base url
                    AppConfig.BASE_URL = currentUrl
                    connectionStatus = "Testing..."
                    coroutineScope.launch {
                        val isConnected = withContext(Dispatchers.IO) {
                            try {
                                val url = URL(currentUrl)
                                val connection = url.openConnection() as HttpURLConnection
                                connection.connectTimeout = 3000
                                connection.connect()
                                // Assume connection is successful if we get any HTTP response (even 404/500 for the root path)
                                connection.responseCode > 0 
                            } catch (e: Exception) {
                                false
                            }
                        }
                        connectionStatus = if (isConnected) "Connected" else "Failed"
                    }
                },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Test Connection & Apply Override")
            }

            val statusColor = when (connectionStatus) {
                "Connected" -> Color(0xFF10B981)
                "Failed" -> Color(0xFFEF4444)
                else -> MaterialTheme.colorScheme.onSurface
            }
            
            Text("Server Status: $connectionStatus", color = statusColor, fontWeight = androidx.compose.ui.text.font.FontWeight.Bold)

            HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))
            
            Text("API Error Log", style = MaterialTheme.typography.titleLarge, fontWeight = androidx.compose.ui.text.font.FontWeight.Bold)
            Surface(
                modifier = Modifier.fillMaxWidth().weight(1f),
                color = MaterialTheme.colorScheme.surfaceVariant,
                shape = MaterialTheme.shapes.medium
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text("Latest Network Error:", fontWeight = androidx.compose.ui.text.font.FontWeight.SemiBold)
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(networkError ?: "No API errors currently recorded.", color = if (networkError != null) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.onSurfaceVariant)
                }
            }
            
            Button(
                onClick = { GlobalNetworkHandler.clearError() },
                modifier = Modifier.fillMaxWidth(),
                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.secondary)
            ) {
                Text("Clear Error Log")
            }
        }
    }
}
