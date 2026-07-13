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
            stats = emptyList(),
            modules = emptyList(),
            events = emptyList(),
            achievements = emptyList()
        )
    }
}
