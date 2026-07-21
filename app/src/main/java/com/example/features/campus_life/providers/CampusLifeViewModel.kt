package com.example.features.campus_life.providers

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.core.repository.StudentRepository
import com.example.features.campus_life.models.*
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class CampusLifeUiState(
    val stats: List<CampusLifeStat> = emptyList(),
    val modules: List<ExperienceModule> = emptyList(),
    val events: List<CampusLifeEvent> = emptyList(),
    val achievements: List<Achievement> = emptyList(),
    val isLoading: Boolean = false
)

class CampusLifeViewModel(private val repository: StudentRepository) : ViewModel() {
    private val _uiState = MutableStateFlow(CampusLifeUiState())
    val uiState: StateFlow<CampusLifeUiState> = _uiState.asStateFlow()

    init {
        loadDashboard()
    }

    fun loadDashboard() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            try {
                val clubs = try { repository.getClubs() } catch (e: Exception) { emptyList() }
                val legalEvents = try { repository.getLegalEvents() } catch (e: Exception) { emptyList() }
                val certifications = try { repository.getCertificationsList() } catch (e: Exception) { emptyList() }

                val clubsJoined = clubs.count { it.userRole != null }

                val stats = listOf(
                    CampusLifeStat("Clubs Joined", "$clubsJoined", Icons.Filled.Groups, 0xFF3B82F6, 0xFFEFF6FF),
                    CampusLifeStat("Legal Events", "${legalEvents.size}", Icons.Filled.EventSeat, 0xFF7C3AED, 0xFFF5F3FF),
                    CampusLifeStat("Certifications", "${certifications.size}", Icons.Filled.WorkspacePremium, 0xFF059669, 0xFFECFDF5)
                )

                val events = legalEvents
                    .sortedBy { it.date }
                    .take(3)
                    .map { dto ->
                        CampusLifeEvent(
                            id = dto.id.hashCode(),
                            title = dto.title,
                            type = dto.category,
                            date = dto.date,
                            time = dto.time,
                            venue = dto.speaker.name,
                            registered = false,
                            participants = 0,
                            imageUrl = ""
                        )
                    }

                val achievements = certifications.take(3).map { cert ->
                    Achievement(
                        title = cert.title,
                        description = "${cert.issuer} • ${cert.category}",
                        timeAgo = cert.date,
                        icon = if (cert.isVerified) Icons.Filled.Verified else Icons.Filled.WorkspacePremium,
                        color = if (cert.isVerified) 0xFF059669 else 0xFFD97706,
                        bgColor = if (cert.isVerified) 0xFFECFDF5 else 0xFFFFFBEB
                    )
                }

                _uiState.update {
                    it.copy(
                        stats = stats,
                        modules = defaultModules,
                        events = events,
                        achievements = achievements,
                        isLoading = false
                    )
                }
            } catch (e: Exception) {
                _uiState.update { it.copy(isLoading = false) }
            }
        }
    }

    companion object {
        // Static navigation shortcuts — not user data, so no backend call needed.
        val defaultModules = listOf(
            ExperienceModule("LexSphere", "Legal community feed", Icons.Filled.Forum, "/student/lexsphere", 0xFFEEF2FF, 0xFFE0E7FF, 0xFF4338CA),
            ExperienceModule("Legal Skills", "Practice modules", Icons.Filled.Gavel, "/student/legal-skills", 0xFFF0FDF4, 0xFFDCFCE7, 0xFF15803D),
            ExperienceModule("Internships", "Find opportunities", Icons.Filled.Work, "/student/internships", 0xFFFFF7ED, 0xFFFFEDD5, 0xFFC2410C),
            ExperienceModule("Community Service", "Log volunteer hours", Icons.Filled.VolunteerActivism, "/student/community-service", 0xFFFDF4FF, 0xFFF5D0FE, 0xFFA21CAF),
            ExperienceModule("Innovation Wall", "Share your projects", Icons.Filled.Lightbulb, "/student/innovation-wall", 0xFFFEFCE8, 0xFFFEF08A, 0xFFA16207)
        )
    }
}

class CampusLifeViewModelFactory(private val repository: StudentRepository) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(CampusLifeViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return CampusLifeViewModel(repository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
