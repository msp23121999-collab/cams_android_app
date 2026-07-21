package com.example.features.extracurriculars.providers

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.core.repository.StudentRepository
import com.example.features.extracurriculars.models.AcademicPublication
import com.example.features.extracurriculars.models.InnovationProject
import com.example.features.extracurriculars.models.ServiceLogEntry
import com.example.features.extracurriculars.models.ServiceOpportunity
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class ExtracurricularsViewModelFactory(private val repository: StudentRepository) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(ExtracurricularsViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return ExtracurricularsViewModel(repository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}

data class ExtracurricularsState(
    val serviceOpportunities: List<ServiceOpportunity> = emptyList(),
    val serviceLogs: List<ServiceLogEntry> = emptyList(),
    val innovationProjects: List<InnovationProject> = emptyList(),
    val publications: List<AcademicPublication> = emptyList(),
    val isLoading: Boolean = true,
    val error: String? = null
)

class ExtracurricularsViewModel(private val repository: StudentRepository) : ViewModel() {
    private val _uiState = MutableStateFlow(ExtracurricularsState())
    val uiState: StateFlow<ExtracurricularsState> = _uiState.asStateFlow()

    init {
        fetchData()
    }

    fun fetchData() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null) }
            try {
                val opportunitiesDto = repository.getServiceOpportunities()
                val logsDto = repository.getServiceLogs()
                val projectsDto = repository.getInnovationProjects()

                val opportunities = opportunitiesDto.map { dto ->
                    ServiceOpportunity(
                        id = dto.id.toString(),
                        title = dto.title,
                        ngoName = dto.organizer,
                        date = dto.date,
                        location = dto.location,
                        spotsAvailable = dto.spots,
                        hours = dto.hours.filter { it.isDigit() }.toIntOrNull() ?: 0,
                        tags = dto.tags
                    )
                }

                val logs = logsDto.map { dto ->
                    ServiceLogEntry(
                        id = dto.id,
                        title = dto.title,
                        date = dto.date,
                        hours = dto.hours.toInt(),
                        status = dto.status
                    )
                }

                val projects = projectsDto.map { dto ->
                    InnovationProject(
                        id = dto.id,
                        title = dto.title,
                        abstractText = dto.description,
                        category = dto.category,
                        mentor = dto.mentor,
                        teamMembers = dto.team,
                        likes = dto.likes,
                        comments = dto.comments
                    )
                }

                _uiState.update {
                    it.copy(
                        serviceOpportunities = opportunities,
                        serviceLogs = logs,
                        innovationProjects = projects,
                        publications = emptyList(), // No backend endpoint available yet
                        isLoading = false
                    )
                }
            } catch (e: Exception) {
                _uiState.update { it.copy(isLoading = false, error = e.message ?: "Failed to load") }
            }
        }
    }
}
