package com.example.core.config

import com.example.BuildConfig

object AppConfig {
    var BASE_URL = BuildConfig.API_BASE_URL
    const val DATABASE_NAME = "cams_local_cache.db"
    const val CONNECT_TIMEOUT_MS = 30000L
    const val READ_TIMEOUT_MS = 30000L
    const val WRITE_TIMEOUT_MS = 30000L
    const val JWT_PREFERENCES_KEY = "cams_auth_prefs"
    const val TOKEN_BEARER_PREFIX = "Bearer "
    const val ENABLE_AI_ANALYTICS = true
    const val ENABLE_OFFLINE_MODE = false
}
