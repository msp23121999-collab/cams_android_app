package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue

import androidx.activity.enableEdgeToEdge
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.compose.rememberNavController
import com.example.features.auth.providers.AuthViewModel
import com.example.features.auth.providers.AuthViewModelFactory
import com.example.core.theme.AppTheme
import com.example.core.navigation.AppNavigation

import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.background

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val container = (application as CamsApplication).container
        enableEdgeToEdge()
        setContent {
            AppTheme {
                val authViewModel: AuthViewModel = viewModel(
                    factory = AuthViewModelFactory(container.authRepository, container.authManager)
                )
                
                val navController = rememberNavController()
                val networkError by com.example.core.network.GlobalNetworkHandler.networkError.collectAsState()

                androidx.compose.foundation.layout.Column(modifier = Modifier.fillMaxSize().statusBarsPadding()) {
                    com.example.core.ui.GlobalNetworkMonitorBanner(
                        networkMonitor = container.networkMonitor
                    )
                    if (networkError != null) {
                        com.example.core.ui.NetworkErrorView(
                            message = networkError!!,
                            onRetry = { com.example.core.network.GlobalNetworkHandler.clearError() },
                            modifier = Modifier // Removed padding since column has statusBarsPadding
                        )
                    }
                    androidx.compose.foundation.layout.Box(modifier = Modifier.weight(1f)) {
                        AppNavigation(
                            navController = navController, 
                            authViewModel = authViewModel,
                            container = container
                        )
                    }
                }
            }
        }
    }
}
