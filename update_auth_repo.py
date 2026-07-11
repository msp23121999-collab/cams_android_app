with open("app/src/main/java/com/example/core/repository/AuthRepository.kt") as f:
    text = f.read()

import re

new_login = """
    override suspend fun login(email: String, password: String): Result<Unit> {
        return try {
            val response = apiService.login(com.example.core.network.LoginRequest(email, password))
            if (response.isSuccessful) {
                val body = response.body()
                if (body != null) {
                    authManager.saveToken(body.token)
                    authManager.saveRole(body.user.role)
                    
                    val user = UsersEntity(
                        id = body.user.id,
                        email = body.user.email,
                        fullName = body.user.fullName,
                        role = body.user.role,
                        phone = "Unknown",
                        hashedPassword = "N/A",
                        isActive = true,
                        departmentId = "DEPT1",
                        createdAt = "2023-01-01T00:00:00Z",
                        updatedAt = "2023-01-01T00:00:00Z",
                        isDeleted = false,
                        deletedAt = null
                    )
                    userDao.insert(user)
                    Result.success(Unit)
                } else {
                    Result.failure(Exception("Empty response body"))
                }
            } else {
                Result.failure(Exception("Login failed: ${response.code()}"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
"""

text = re.sub(r'override suspend fun login\(.*?\).*?catch \(e: Exception\) \{\n\s+Result\.failure\(e\)\n\s+\}\n\s+\}', new_login.strip(), text, flags=re.DOTALL)

with open("app/src/main/java/com/example/core/repository/AuthRepository.kt", "w") as f:
    f.write(text)

