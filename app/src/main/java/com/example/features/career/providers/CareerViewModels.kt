package com.example.features.career.providers

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.features.career.models.ActivityPointClaim
import com.example.features.career.models.InternshipDrive
import com.example.features.career.models.StudentCertification
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class CareerState(
    val drives: List<InternshipDrive> = emptyList(),
    val certifications: List<StudentCertification> = emptyList(),
    val activityClaims: List<ActivityPointClaim> = emptyList(),
    val isLoading: Boolean = true,
    val error: String? = null
)

class CareerViewModel : ViewModel() {
    private val _uiState = MutableStateFlow(CareerState())
    val uiState: StateFlow<CareerState> = _uiState.asStateFlow()

    init {
        fetchData()
    }

    fun fetchData() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            delay(1000)
            
            val mockDrives = listOf(
                InternshipDrive("1", "Supreme Court of India", "Judicial Clerk", "New Delhi", "Interview", "Assist honorable judges with legal research.", "Unpaid"),
                InternshipDrive("2", "Tier-1 Law Firm", "Legal Intern", "Mumbai", "Assessment", "Corporate law research and drafting.", "₹15,000/mo"),
                InternshipDrive("3", "Tech Corp Legal", "In-house Intern", "Bangalore", "Applied", "Contract review and compliance.", "₹20,000/mo"),
                InternshipDrive("4", "Human Rights NGO", "Research Intern", "Remote", "Selected", "Human rights advocacy research.", "Unpaid")
            )
            
            val mockCertifications = listOf(
                StudentCertification("c1", "Advanced Corporate Law", "LexAcademy", "2023-05-10", null, "CRED1234", true),
                StudentCertification("c2", "IP Rights & Patents", "WIPO", "2023-08-15", null, "CRED5678", true),
                StudentCertification("c3", "Mediation & Arbitration", "NALSAR", "2024-01-05", "2026-01-05", "CRED9012", false)
            )

            val mockClaims = listOf(
                ActivityPointClaim("ap1", "Moot Court Competition", "Advocacy", "2023-11-20", 50, "Approved"),
                ActivityPointClaim("ap2", "Legal Aid Clinic Volunteer", "Community", "2023-12-10", 30, "Approved"),
                ActivityPointClaim("ap3", "Published Research Paper", "Research", "2024-01-15", 40, "Pending")
            )

            _uiState.update { 
                it.copy(
                    drives = mockDrives,
                    certifications = mockCertifications,
                    activityClaims = mockClaims,
                    isLoading = false
                )
            }
        }
    }
}
