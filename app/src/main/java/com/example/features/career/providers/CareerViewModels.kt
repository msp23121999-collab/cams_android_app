package com.example.features.career.providers

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.core.repository.StudentRepository
import com.example.features.career.models.ActivityPointClaim
import com.example.features.career.models.InternshipDrive
import com.example.features.career.models.StudentCertification
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class CareerViewModelFactory(private val repository: StudentRepository) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(CareerViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return CareerViewModel(repository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}

data class CareerState(
    val drives: List<InternshipDrive> = emptyList(),
    val certifications: List<StudentCertification> = emptyList(),
    val activityClaims: List<ActivityPointClaim> = emptyList(),
    val isLoading: Boolean = true,
    val error: String? = null
)

class CareerViewModel(private val repository: StudentRepository) : ViewModel() {
    private val _uiState = MutableStateFlow(CareerState())
    val uiState: StateFlow<CareerState> = _uiState.asStateFlow()

    init {
        fetchData()
    }

    fun fetchData() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null) }
            
            try {
                // Fetch from repository
                val drivesDtos = repository.getInternshipDrives()
                val certDtos = repository.getCertificationsList()
                val pointDtos = repository.getActivityPoints()

                val drives = drivesDtos.map { dto ->
                    InternshipDrive(
                        id = dto.id,
                        companyName = dto.company,
                        role = dto.role,
                        location = "Remote/Hybrid", // Using a default as backend doesn't provide location yet
                        applicationStatus = dto.status,
                        description = "Check application portal for details",
                        stipend = dto.stipend
                    )
                }

                val certs = certDtos.map { dto ->
                    StudentCertification(
                        id = dto.id,
                        name = dto.title,
                        issuer = dto.issuer,
                        issueDate = dto.date,
                        expiryDate = null,
                        credentialId = dto.id,
                        isVerified = dto.isVerified
                    )
                }

                val claims = pointDtos.map { dto ->
                    ActivityPointClaim(
                        id = dto.id,
                        activityName = dto.title,
                        category = dto.category,
                        date = dto.date,
                        pointsClaimed = dto.pointsClaimed,
                        status = dto.status
                    )
                }

                _uiState.update { 
                    it.copy(
                        drives = drives,
                        certifications = certs,
                        activityClaims = claims,
                        isLoading = false
                    )
                }
            } catch (e: Exception) {
                _uiState.update { it.copy(isLoading = false, error = e.message) }
            }
        }
    }
}
