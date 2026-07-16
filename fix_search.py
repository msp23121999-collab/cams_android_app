import re

def fix_search_fees_typing():
    # 1. Add Domain Model to AdminModels.kt
    models_path = 'app/src/main/java/com/example/features/admin/models/AdminModels.kt'
    with open(models_path, 'r', encoding='utf-8') as f:
        models = f.read()
    
    if "data class AdminFeeStudent" not in models:
        fee_student_model = """
data class AdminFeeStudent(
    val studentId: String,
    val studentName: String,
    val department: String,
    val currentSemester: Int,
    val totalFees: Double,
    val paidFees: Double,
    val dueFees: Double
)
"""
        models += fee_student_model
        with open(models_path, 'w', encoding='utf-8') as f:
            f.write(models)
    
    # 2. Add DTO and update CamsApiService.kt
    api_path = 'app/src/main/java/com/example/core/network/CamsApiService.kt'
    with open(api_path, 'r', encoding='utf-8') as f:
        api = f.read()

    # Replace Response<List<Any>> with Response<List<AdminFeeStudentDto>>
    api = api.replace(
        'suspend fun searchStudentsForFees(@Query("query") query: String): Response<List<Any>>',
        'suspend fun searchStudentsForFees(@Query("query") query: String): Response<List<AdminFeeStudentDto>>'
    )

    if "data class AdminFeeStudentDto" not in api:
        fee_student_dto = """
@JsonClass(generateAdapter = true)
data class AdminFeeStudentDto(
    val studentId: String,
    val studentName: String?,
    val department: String?,
    val currentSemester: Int?,
    val totalFees: Double?,
    val paidFees: Double?,
    val dueFees: Double?
)
"""
        api += fee_student_dto
        with open(api_path, 'w', encoding='utf-8') as f:
            f.write(api)

    # 3. Update AdminRepository.kt
    repo_path = 'app/src/main/java/com/example/core/repository/AdminRepository.kt'
    with open(repo_path, 'r', encoding='utf-8') as f:
        repo = f.read()

    # Interface
    repo = repo.replace(
        'suspend fun searchStudentsForFees(query: String): List<Any>?',
        'suspend fun searchStudentsForFees(query: String): List<com.example.features.admin.models.AdminFeeStudent>?'
    )

    # Implementation
    bad_impl = """    override suspend fun searchStudentsForFees(query: String): List<com.example.features.admin.models.AdminFeeStudent>? {
        return try {
            val response = apiService.searchStudentsForFees(query)
            if (response.isSuccessful) response.body() else null
        } catch (e: Exception) { null }
    }"""
    
    good_impl = """    override suspend fun searchStudentsForFees(query: String): List<com.example.features.admin.models.AdminFeeStudent>? {
        return try {
            val response = apiService.searchStudentsForFees(query)
            if (response.isSuccessful) {
                response.body()?.map { 
                    com.example.features.admin.models.AdminFeeStudent(
                        studentId = it.studentId,
                        studentName = it.studentName ?: "",
                        department = it.department ?: "",
                        currentSemester = it.currentSemester ?: 1,
                        totalFees = it.totalFees ?: 0.0,
                        paidFees = it.paidFees ?: 0.0,
                        dueFees = it.dueFees ?: 0.0
                    )
                }
            } else null
        } catch (e: Exception) { null }
    }"""
    
    repo = repo.replace(bad_impl, good_impl)

    # Fallback if the replacement failed due to indentation or exact match issues
    if good_impl not in repo:
        # Regex replace
        repo = re.sub(
            r'override suspend fun searchStudentsForFees\(query: String\): List<com\.example\.features\.admin\.models\.AdminFeeStudent>\? \{.*?\n    \}',
            good_impl,
            repo,
            flags=re.DOTALL
        )
    
    with open(repo_path, 'w', encoding='utf-8') as f:
        f.write(repo)
    
    print("Fixed type safety for searchStudentsForFees")

if __name__ == "__main__":
    fix_search_fees_typing()
