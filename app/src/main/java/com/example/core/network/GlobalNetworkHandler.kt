package com.example.core.network

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

object GlobalNetworkHandler {
    private val _networkError = MutableStateFlow<String?>(null)
    val networkError: StateFlow<String?> = _networkError.asStateFlow()

    fun emitError(message: String) {
        _networkError.value = message
    }

    fun clearError() {
        _networkError.value = null
    }
}
