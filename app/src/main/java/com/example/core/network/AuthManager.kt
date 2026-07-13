package com.example.core.network

import android.content.Context
import androidx.security.crypto.EncryptedSharedPreferences
import androidx.security.crypto.MasterKey
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow

interface AuthManager {
    fun saveAuth(accessToken: String, refreshToken: String?, role: String, subdomainTarget: String)
    fun getToken(): String?
    fun getRefreshToken(): String?
    fun getRole(): String?
    fun getSubdomainTarget(): String?
    fun clearAuth()
    fun isLoggedIn(): Boolean
    val tokenFlow: Flow<String?>
    val refreshTokenFlow: Flow<String?>
    val roleFlow: Flow<String?>
    val subdomainTargetFlow: Flow<String?>
    
    // Compatibility methods
    fun saveToken(token: String)
    fun clearToken()
}

class AuthManagerImpl(context: Context) : AuthManager {

    private val masterKey = MasterKey.Builder(context)
        .setKeyScheme(MasterKey.KeyScheme.AES256_GCM)
        .build()

    private val sharedPreferences = try {
        EncryptedSharedPreferences.create(
            context,
            "cams_auth_prefs",
            masterKey,
            EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
            EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM
        )
    } catch (e: Exception) {
        context.getSharedPreferences("cams_auth_prefs", Context.MODE_PRIVATE).edit().clear().commit()
        try {
            val file = java.io.File(context.filesDir.parent + "/shared_prefs/cams_auth_prefs.xml")
            if (file.exists()) {
                file.delete()
            }
        } catch (ignored: Exception) {}
        
        EncryptedSharedPreferences.create(
            context,
            "cams_auth_prefs",
            masterKey,
            EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
            EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM
        )
    }

    private val _tokenFlow = MutableStateFlow<String?>(getToken())
    override val tokenFlow: Flow<String?> = _tokenFlow.asStateFlow()

    private val _refreshTokenFlow = MutableStateFlow<String?>(getRefreshToken())
    override val refreshTokenFlow: Flow<String?> = _refreshTokenFlow.asStateFlow()

    private val _roleFlow = MutableStateFlow<String?>(getRole())
    override val roleFlow: Flow<String?> = _roleFlow.asStateFlow()

    private val _subdomainTargetFlow = MutableStateFlow<String?>(getSubdomainTarget())
    override val subdomainTargetFlow: Flow<String?> = _subdomainTargetFlow.asStateFlow()

    override fun saveAuth(accessToken: String, refreshToken: String?, role: String, subdomainTarget: String) {
        sharedPreferences.edit()
            .putString("auth_token", accessToken)
            .putString("refresh_token", refreshToken)
            .putString("user_role", role)
            .putString("subdomain_target", subdomainTarget)
            .apply()
        _tokenFlow.value = accessToken
        _refreshTokenFlow.value = refreshToken
        _roleFlow.value = role
        _subdomainTargetFlow.value = subdomainTarget
    }

    override fun getToken(): String? {
        return sharedPreferences.getString("auth_token", null)
    }

    override fun getRefreshToken(): String? {
        return sharedPreferences.getString("refresh_token", null)
    }

    override fun getRole(): String? {
        return sharedPreferences.getString("user_role", null)
    }

    override fun getSubdomainTarget(): String? {
        return sharedPreferences.getString("subdomain_target", null)
    }

    override fun clearAuth() {
        sharedPreferences.edit()
            .remove("auth_token")
            .remove("refresh_token")
            .remove("user_role")
            .remove("subdomain_target")
            .apply()
        _tokenFlow.value = null
        _refreshTokenFlow.value = null
        _roleFlow.value = null
        _subdomainTargetFlow.value = null
    }
    
    override fun isLoggedIn(): Boolean {
        return getToken() != null
    }

    override fun saveToken(token: String) {
        saveAuth(token, getRefreshToken(), getRole() ?: "UNKNOWN", getSubdomainTarget() ?: "")
    }

    override fun clearToken() {
        clearAuth()
    }
}
