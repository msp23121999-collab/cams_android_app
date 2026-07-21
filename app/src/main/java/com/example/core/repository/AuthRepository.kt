package com.example.core.repository

import com.example.core.database.dao.UsersDao
import com.example.core.database.entities.UsersEntity
import com.example.core.network.AuthManager
import com.example.core.network.CamsApiService
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.flow.map

interface AuthRepository {
    fun getCurrentUser(): Flow<UsersEntity?>
    suspend fun login(email: String, password: String): Result<Unit>
    suspend fun logout()
    fun isLoggedIn(): Boolean
    suspend fun forgotPassword(email: String): Result<String>
    suspend fun resetPassword(token: String, newPassword: String): Result<String>
    suspend fun requestEmailChange(newEmail: String, currentPassword: String): Result<String>
    suspend fun confirmEmailChange(token: String): Result<String>
    suspend fun getEmailNotificationsEnabled(): Boolean
    suspend fun setEmailNotificationsEnabled(enabled: Boolean): Result<Boolean>
}

class OfflineFirstAuthRepository(
    private val apiService: CamsApiService,
    private val userDao: UsersDao,
    private val authManager: AuthManager
) : AuthRepository {

    override fun getCurrentUser(): Flow<UsersEntity?> {
        // Typically, you'd want to return the flow of the currently logged in user.
        // Assuming we store some generic ID or just take the first user for now.
        // In a real app, you'd store the user ID in AuthManager.
        return userDao.getAll().map { it.firstOrNull() }
    }

    override suspend fun login(email: String, password: String): Result<Unit> {
        return try {
            val loginResponse = apiService.login(com.example.core.network.LoginRequest(email, password))
            if (loginResponse.isSuccessful) {
                val loginBody = loginResponse.body()
                if (loginBody != null) {
                    authManager.saveAuth(
                        accessToken = loginBody.accessToken,
                        refreshToken = loginBody.refreshToken,
                        role = loginBody.role,
                        subdomainTarget = loginBody.subdomainTarget
                    )
                    
                    // Fetch full user details
                    val userResponse = apiService.getCurrentUser()
                    if (userResponse.isSuccessful) {
                        val userBody = userResponse.body()
                        if (userBody != null) {
                            val user = UsersEntity(
                                id = userBody.id,
                                email = userBody.email,
                                fullName = userBody.fullName,
                                role = userBody.role,
                                phone = "Unknown",
                                hashedPassword = "N/A",
                                isActive = true,
                                departmentId = userBody.departmentId,
                                createdAt = "2023-01-01T00:00:00Z",
                                updatedAt = "2023-01-01T00:00:00Z",
                                isDeleted = false,
                                deletedAt = null
                            )
                            userDao.insert(user)
                            com.example.core.network.GlobalNetworkHandler.clearError()
                            Result.success(Unit)
                        } else {
                            Result.failure(Exception("Failed to fetch current user profile details from server"))
                        }
                    } else {
                        val errorMsg = userResponse.errorBody()?.string() ?: "Profile request failed"
                        Result.failure(Exception("Failed to fetch profile: $errorMsg (code: ${userResponse.code()})"))
                    }
                } else {
                    Result.failure(Exception("Empty login response from server"))
                }
            } else if (loginResponse.code() == 401 || loginResponse.code() == 400) {
                Result.failure(Exception("Incorrect email or password. Please try again."))
            } else {
                Result.failure(Exception(extractErrorMessage(loginResponse.errorBody()?.string(), "Login failed. Please try again.")))
            }
        } catch (e: java.net.SocketTimeoutException) {
            Result.failure(Exception("The server took too long to respond. Please try again."))
        } catch (e: java.io.IOException) {
            Result.failure(Exception("Unable to reach the server. Check your internet connection."))
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    private fun extractErrorMessage(errorBody: String?, fallback: String): String {
        if (errorBody.isNullOrBlank()) return fallback
        return try {
            org.json.JSONObject(errorBody).optString("detail", fallback)
        } catch (e: Exception) {
            fallback
        }
    }

    override suspend fun logout() {
        authManager.clearAuth()
        // clear local db or specific tables if necessary
    }

    override fun isLoggedIn(): Boolean {
        return authManager.isLoggedIn()
    }

    override suspend fun forgotPassword(email: String): Result<String> {
        return try {
            val response = apiService.forgotPassword(com.example.core.network.ForgotPasswordRequest(email))
            if (response.isSuccessful) {
                Result.success(response.body()?.detail ?: "If an account with that email exists, a reset link has been sent.")
            } else {
                Result.failure(Exception(response.errorBody()?.string() ?: "Failed to request password reset"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun resetPassword(token: String, newPassword: String): Result<String> {
        return try {
            val response = apiService.resetPassword(com.example.core.network.ResetPasswordRequest(token, newPassword))
            if (response.isSuccessful) {
                Result.success(response.body()?.detail ?: "Password has been reset successfully")
            } else {
                Result.failure(Exception(response.errorBody()?.string() ?: "Invalid or expired reset token"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun requestEmailChange(newEmail: String, currentPassword: String): Result<String> {
        return try {
            val response = apiService.requestEmailChange(com.example.core.network.RequestEmailChangeRequest(newEmail, currentPassword))
            if (response.isSuccessful) {
                Result.success(response.body()?.detail ?: "Verification email sent to the new address")
            } else {
                Result.failure(Exception(response.errorBody()?.string() ?: "Failed to request email change"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun confirmEmailChange(token: String): Result<String> {
        return try {
            val response = apiService.confirmEmailChange(com.example.core.network.ConfirmEmailChangeRequest(token))
            if (response.isSuccessful) {
                Result.success(response.body()?.detail ?: "Email address updated successfully")
            } else {
                Result.failure(Exception(response.errorBody()?.string() ?: "Invalid or expired verification token"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun getEmailNotificationsEnabled(): Boolean {
        return try {
            val response = apiService.getCurrentUser()
            if (response.isSuccessful) response.body()?.emailNotificationsEnabled ?: true else true
        } catch (e: Exception) {
            true
        }
    }

    override suspend fun setEmailNotificationsEnabled(enabled: Boolean): Result<Boolean> {
        return try {
            val response = apiService.updateNotificationPreferences(
                com.example.core.network.NotificationPreferencesRequest(enabled)
            )
            if (response.isSuccessful) {
                Result.success(response.body()?.emailNotificationsEnabled ?: enabled)
            } else {
                Result.failure(Exception(response.errorBody()?.string() ?: "Failed to update notification preferences"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
