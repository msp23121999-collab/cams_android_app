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
            } else {
                val errorMsg = loginResponse.errorBody()?.string() ?: "Invalid credentials"
                Result.failure(Exception("Login failed: $errorMsg (code: ${loginResponse.code()})"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun logout() {
        authManager.clearAuth()
        // clear local db or specific tables if necessary
    }

    override fun isLoggedIn(): Boolean {
        return authManager.isLoggedIn()
    }
}
