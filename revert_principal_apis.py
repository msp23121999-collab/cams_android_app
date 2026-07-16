import re

def fix_principal_apis():
    # 1. Update CamsApiService.kt
    api_path = 'app/src/main/java/com/example/core/network/CamsApiService.kt'
    with open(api_path, 'r', encoding='utf-8') as f:
        api = f.read()

    api = api.replace(
        "suspend fun getResearchCompliance(): Response<List<ResearchComplianceDto>>",
        "suspend fun getResearchCompliance(): Response<PrincipalComplianceResponseDto>"
    )
    api = api.replace(
        "suspend fun getInfrastructureDetails(): Response<InfrastructureDto>",
        "suspend fun getInfrastructureDetails(): Response<InfrastructureResponseDto>"
    )

    with open(api_path, 'w', encoding='utf-8') as f:
        f.write(api)

    # 2. Update PrincipalRepository.kt
    repo_path = 'app/src/main/java/com/example/core/repository/PrincipalRepository.kt'
    with open(repo_path, 'r', encoding='utf-8') as f:
        repo = f.read()

    repo = repo.replace(
        "suspend fun getResearchCompliance(): List<com.example.core.network.ResearchComplianceDto>?",
        "suspend fun getResearchCompliance(): com.example.core.network.PrincipalComplianceResponseDto?"
    )
    repo = repo.replace(
        "suspend fun getInfrastructureDetails(): com.example.core.network.InfrastructureDto?",
        "suspend fun getInfrastructureDetails(): com.example.core.network.InfrastructureResponseDto?"
    )

    with open(repo_path, 'w', encoding='utf-8') as f:
        f.write(repo)

    # 3. Update PrincipalViewModel.kt
    vm_path = 'app/src/main/java/com/example/features/principal/providers/PrincipalViewModel.kt'
    with open(vm_path, 'r', encoding='utf-8') as f:
        vm = f.read()

    vm = vm.replace(
        "val compliance: List<com.example.core.network.ResearchComplianceDto>? = null,",
        "val compliance: com.example.core.network.PrincipalComplianceResponseDto? = null,"
    )
    vm = vm.replace(
        "val data: com.example.core.network.InfrastructureDto? = null,",
        "val data: com.example.core.network.InfrastructureResponseDto? = null,"
    )

    with open(vm_path, 'w', encoding='utf-8') as f:
        f.write(vm)

    print("Reverted Principal API changes to match UI requirements")

if __name__ == "__main__":
    fix_principal_apis()
