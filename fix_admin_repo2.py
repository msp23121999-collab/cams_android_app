import re

def fix_admin_issues():
    # 1. Fix AdminRepository.kt getDashboardMetrics
    admin_repo_path = 'app/src/main/java/com/example/core/repository/AdminRepository.kt'
    with open(admin_repo_path, 'r', encoding='utf-8') as f:
        admin_repo = f.read()

    bad_metrics_mapping = """                metrics = dto.metrics.map { AdminMetric(it.id, it.label, it.value) },
                totalUsers = dto.totalUsers,
                totalStudents = dto.totalStudents,
                totalStaff = dto.totalStaff,
                totalDepartments = dto.totalDepartments"""
    
    good_metrics_mapping = """                metrics = listOf(
                    AdminMetric("total_users", "Total Users", dto.totalUsers.toString()),
                    AdminMetric("online_now", "Online Now", dto.onlineNow.toString()),
                    AdminMetric("storage_used", "Storage Used", dto.storageUsed),
                    AdminMetric("system_health", "System Health", dto.systemHealth)
                ),
                totalUsers = dto.totalUsers,
                totalStudents = 0,
                totalStaff = 0,
                totalDepartments = 0"""

    admin_repo = admin_repo.replace(bad_metrics_mapping, good_metrics_mapping)
    admin_repo = admin_repo.replace('apiService.getDegreesList()', 'apiService.getDegreesListAdmin()')
    with open(admin_repo_path, 'w', encoding='utf-8') as f:
        f.write(admin_repo)

    # 2. Fix CamsApiService.kt getDegreesList duplicated
    api_path = 'app/src/main/java/com/example/core/network/CamsApiService.kt'
    with open(api_path, 'r', encoding='utf-8') as f:
        api = f.read()

    # The one added at the bottom by my script was `suspend fun getDegreesList(): Response<List<AdminDegreeDto>>`
    api = api.replace(
        'suspend fun getDegreesList(): Response<List<AdminDegreeDto>>',
        'suspend fun getDegreesListAdmin(): Response<List<AdminDegreeDto>>'
    )
    with open(api_path, 'w', encoding='utf-8') as f:
        f.write(api)

    print("Fixed final admin issues")

if __name__ == '__main__':
    fix_admin_issues()
