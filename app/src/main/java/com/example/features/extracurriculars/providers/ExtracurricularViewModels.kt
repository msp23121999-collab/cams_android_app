package com.example.features.extracurriculars.providers

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.features.extracurriculars.models.AcademicPublication
import com.example.features.extracurriculars.models.InnovationProject
import com.example.features.extracurriculars.models.ServiceLogEntry
import com.example.features.extracurriculars.models.ServiceOpportunity
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class ExtracurricularsState(
    val serviceOpportunities: List<ServiceOpportunity> = emptyList(),
    val serviceLogs: List<ServiceLogEntry> = emptyList(),
    val innovationProjects: List<InnovationProject> = emptyList(),
    val publications: List<AcademicPublication> = emptyList(),
    val isLoading: Boolean = true,
    val error: String? = null
)

class ExtracurricularsViewModel : ViewModel() {
    private val _uiState = MutableStateFlow(ExtracurricularsState())
    val uiState: StateFlow<ExtracurricularsState> = _uiState.asStateFlow()

    init {
        fetchData()
    }

    fun fetchData() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            delay(1000)

            val mockOpportunities = listOf(
                ServiceOpportunity("1", "Legal Aid Camp", "Justice For All", "2024-03-15", "Rural Center", 15, 8, listOf("Legal Aid", "Community")),
                ServiceOpportunity("2", "Environmental Law Awareness", "Green Earth NGO", "2024-03-22", "City Square", 20, 5, listOf("Environment", "Awareness")),
                ServiceOpportunity("3", "RTI Filing Workshop", "Transparency Init", "2024-04-05", "Campus Hall", 30, 4, listOf("Workshop", "Rights"))
            )

            val mockLogs = listOf(
                ServiceLogEntry("1", "Human Rights Campaign", "2023-11-10", 12, "Verified"),
                ServiceLogEntry("2", "Slum Legal Aid Clinic", "2024-01-20", 8, "Verified"),
                ServiceLogEntry("3", "Cyber Crime Awareness", "2024-02-15", 5, "Pending")
            )

            val mockProjects = listOf(
                InnovationProject("1", "LexNova Enterprise", "AI-powered legal workspace.", "Legal Technology", "Dr. A. Sharma", listOf("Student A", "Student B"), 120, 45),
                InnovationProject("2", "Project Nyaya", "Access to justice for underprivileged.", "Community Projects", "Prof. B. Singh", listOf("Student C", "Student D"), 85, 20),
                InnovationProject("3", "Constitutional Morality DB", "Database of landmark judgements.", "Research", "Dr. C. Patel", listOf("Student E"), 95, 30),
                InnovationProject("4", "LawyerUp", "Marketplace connecting clients with junior advocates.", "Startups", "Prof. D. Kumar", listOf("Student F", "Student G"), 150, 60)
            )

            val mockPublications = listOf(
                AcademicPublication("1", "AI in Judicial Decision Making", "An analysis of algorithmic bias...", "Legal Technology", "Published", "Best Paper Award", "2023-10-05", "Dr. A. Sharma", true),
                AcademicPublication("2", "Corporate Governance Post-2020", "A review of new regulations...", "Corporate Law", "Under Review", null, "2024-01-12", "Prof. B. Singh", false),
                AcademicPublication("3", "IPR in the Metaverse", "Navigating trademark disputes...", "IPR", "Published", null, "2023-12-20", "Dr. C. Patel", true)
            )

            _uiState.update {
                it.copy(
                    serviceOpportunities = mockOpportunities,
                    serviceLogs = mockLogs,
                    innovationProjects = mockProjects,
                    publications = mockPublications,
                    isLoading = false
                )
            }
        }
    }
}
