package com.example.features.campus_life.providers

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import com.example.features.campus_life.models.*
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

data class CampusLifeUiState(
    val stats: List<CampusLifeStat> = emptyList(),
    val modules: List<ExperienceModule> = emptyList(),
    val events: List<CampusLifeEvent> = emptyList(),
    val achievements: List<Achievement> = emptyList(),
    val isLoading: Boolean = false
)

class CampusLifeViewModel : ViewModel() {
    private val _uiState = MutableStateFlow(CampusLifeUiState())
    val uiState: StateFlow<CampusLifeUiState> = _uiState.asStateFlow()

    init {
        loadDashboard()
    }

    private fun loadDashboard() {
        _uiState.value = CampusLifeUiState(
            stats = listOf(
                CampusLifeStat("Events Attended", "24", Icons.Filled.Event, 0xFF2563EB, 0xFFEFF6FF),
                CampusLifeStat("Clubs Joined", "3", Icons.Filled.Groups, 0xFF9333EA, 0xFFFAF5FF),
                CampusLifeStat("Volunteer Hrs", "45h", Icons.Filled.Schedule, 0xFF059669, 0xFFECFDF5),
                CampusLifeStat("Moot Courts", "4", Icons.Filled.Lightbulb, 0xFFD97706, 0xFFFFFBEB),
                CampusLifeStat("Leadership", "1", Icons.Filled.Shield, 0xFFE11D48, 0xFFFFF1F2)
            ),
            modules = listOf(
                ExperienceModule("Project Showcase", "Legal Research Portfolios", Icons.Filled.Layers, "/student/projects", 0xFFDBEAFE, 0xFFEEF2FF, 0xFF2563EB),
                ExperienceModule("Student Clubs", "Societies & Moot Groups", Icons.Filled.Groups, "/student/clubs", 0xFFF3E8FF, 0xFFFCE7F3, 0xFF9333EA),
                ExperienceModule("Community Service", "Pro-Bono & Legal Clinics", Icons.Filled.EmojiEvents, "/student/community-service", 0xFFD1FAE5, 0xFFF0FDFA, 0xFF059669)
            ),
            events = listOf(
                CampusLifeEvent(1, "National Moot Court Competition 2026", "Competition", "Oct 15, 2026", "09:00 AM", "Main Auditorium", true, 120, "https://images.unsplash.com/photo-1589829085413-56de8ae18c73?q=80&w=2000&auto=format&fit=crop"),
                CampusLifeEvent(2, "National Seminar on Constitutional Law", "Seminar", "Oct 20, 2026", "11:00 AM", "Conference Hall Block B", false, 85, "https://images.unsplash.com/photo-1505664194779-8beaceb93744?q=80&w=2000&auto=format&fit=crop"),
                CampusLifeEvent(3, "Law Society Debate Championship", "Event", "Oct 25, 2026", "02:00 PM", "Seminar Hall 1", false, 40, "https://images.unsplash.com/photo-1447069387366-eb16ab64a13d?q=80&w=2000&auto=format&fit=crop")
            ),
            achievements = listOf(
                Achievement("Best Memorial Award", "7th NLS Trilegal Dispute Resolution", "2 days ago", Icons.Filled.MilitaryTech, 0xFFD97706, 0xFFFFFBEB),
                Achievement("100 Hrs Legal Aid", "Community Service Module", "1 week ago", Icons.Filled.CheckCircle, 0xFF059669, 0xFFECFDF5),
                Achievement("Elected as Debate Sec.", "Law Society Clubs", "2 weeks ago", Icons.Filled.Groups, 0xFF9333EA, 0xFFFAF5FF)
            )
        )
    }
}
