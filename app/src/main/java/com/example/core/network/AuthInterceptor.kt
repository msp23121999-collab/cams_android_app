package com.example.core.network

import com.squareup.moshi.Moshi
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import okhttp3.Interceptor
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import okhttp3.Response
import com.example.core.config.AppConfig

class AuthInterceptor(private val authManager: AuthManager) : Interceptor {

    private val moshi = Moshi.Builder().add(KotlinJsonAdapterFactory()).build()
    private val refreshHttpClient = OkHttpClient.Builder().build()
    private val refreshLock = Any()

    override fun intercept(chain: Interceptor.Chain): Response {
        val originalRequest = chain.request()
        val token = authManager.getToken()

        val newRequest = if (token != null) {
            originalRequest.newBuilder()
                .addHeader("Authorization", "Bearer $token")
                .build()
        } else {
            originalRequest
        }

        val response = chain.proceed(newRequest)

        val isAuthEndpoint = originalRequest.url.encodedPath.let {
            it.endsWith("/auth/login") || it.endsWith("/auth/refresh")
        }

        if (response.code == 401 && !isAuthEndpoint) {
            response.close()
            val newToken = synchronized(refreshLock) {
                // Re-check in case another thread already refreshed while we waited for the lock.
                val currentToken = authManager.getToken()
                if (currentToken != null && currentToken != token) currentToken else attemptRefresh()
            }

            if (newToken != null) {
                val retriedRequest = originalRequest.newBuilder()
                    .addHeader("Authorization", "Bearer $newToken")
                    .build()
                return chain.proceed(retriedRequest)
            }

            AuthEventBus.emit(AuthEvent.Unauthorized)
        }

        return response
    }

    private fun attemptRefresh(): String? {
        val refreshToken = authManager.getRefreshToken() ?: return null

        return try {
            val bodyJson = moshi.adapter(RefreshRequest::class.java)
                .toJson(RefreshRequest(refreshToken))
            val request = Request.Builder()
                .url(AppConfig.BASE_URL.trimEnd('/') + "/auth/refresh")
                .post(bodyJson.toRequestBody("application/json".toMediaType()))
                .build()

            refreshHttpClient.newCall(request).execute().use { refreshResponse ->
                if (!refreshResponse.isSuccessful) return null
                val responseBody = refreshResponse.body?.string() ?: return null
                val parsed = moshi.adapter(RefreshResponse::class.java).fromJson(responseBody)
                    ?: return null

                authManager.saveAuth(
                    accessToken = parsed.accessToken,
                    refreshToken = parsed.refreshToken ?: refreshToken,
                    role = authManager.getRole() ?: "",
                    subdomainTarget = authManager.getSubdomainTarget() ?: ""
                )
                parsed.accessToken
            }
        } catch (e: Exception) {
            null
        }
    }
}
