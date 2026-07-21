package com.example.core.services

import android.util.Log
import com.example.CamsApplication
import com.example.core.network.DeviceTokenRequest
import com.google.firebase.messaging.FirebaseMessaging
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await

/**
 * Sends this installation's FCM token to the backend.
 *
 * Previously `onNewToken` discarded the token, so the server had no way to reach the
 * device and server-originated push could not work at all.
 *
 * Registration is deliberately best-effort: a failure here must never block signing in
 * or interrupt the user. If it fails, the next app launch retries.
 */
object PushTokenRegistrar {

    private const val TAG = "PushTokenRegistrar"

    /** Fetch the current token and register it. Safe to call on every launch/login. */
    fun registerCurrentToken() {
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val token = FirebaseMessaging.getInstance().token.await()
                register(token)
            } catch (e: Exception) {
                Log.w(TAG, "Could not obtain FCM token: ${e.message}")
            }
        }
    }

    /** Register a specific token — used by onNewToken when FCM rotates it. */
    fun register(token: String) {
        if (token.isBlank()) return
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val api = CamsApplication.instance.container.apiService
                val response = api.registerDeviceToken(DeviceTokenRequest(token))
                if (!response.isSuccessful) {
                    // 401 is expected before sign-in; the next launch retries once
                    // there is a session.
                    Log.d(TAG, "Token registration returned HTTP ${response.code()}")
                }
            } catch (e: Exception) {
                Log.w(TAG, "Token registration failed: ${e.message}")
            }
        }
    }

    /**
     * Detach this device on sign-out.
     *
     * Without this, the next person to sign in on a shared device would keep receiving
     * the previous user's notifications until the token happened to rotate.
     */
    fun unregisterCurrentToken() {
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val token = FirebaseMessaging.getInstance().token.await()
                val api = CamsApplication.instance.container.apiService
                api.unregisterDeviceToken(DeviceTokenRequest(token))
            } catch (e: Exception) {
                Log.w(TAG, "Token unregistration failed: ${e.message}")
            }
        }
    }
}
