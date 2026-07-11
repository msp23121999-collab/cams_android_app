package com.example.features.parent.providers

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.core.repository.ParentRepository
import com.example.features.parent.models.*
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class ParentDashboardState(
    val childProfileExtended: ChildProfileExtended? = null,
    val performance: List<PerformanceData> = emptyList(),
    val notices: List<CollegeNotice> = emptyList(),
    val subjectAttendance: List<SubjectAttendance> = emptyList(),
    val timetable: List<TimetableDay> = emptyList(),
    val feeLedger: ChildFeeLedger? = null,
    val isLoading: Boolean = true,
    val error: String? = null
)

class ParentDashboardViewModel(private val repository: ParentRepository) : ViewModel() {
    private val _uiState = MutableStateFlow(ParentDashboardState())
    val uiState: StateFlow<ParentDashboardState> = _uiState.asStateFlow()

    init {
        loadData()
    }

    fun loadData() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null) }
            try {
                val profileExtended = repository.getChildProfile()
                val performance = repository.getPerformanceAnalytics()
                val notices = repository.getNotices()
                val subjectAttendance = repository.getSubjectAttendance()
                val timetable = repository.getTimetable()
                val feeLedger = repository.getFeeStatus()
                
                _uiState.update { it.copy(
                    childProfileExtended = profileExtended,
                    performance = performance,
                    notices = notices,
                    subjectAttendance = subjectAttendance,
                    timetable = timetable,
                    feeLedger = feeLedger,
                    isLoading = false
                ) }
            } catch (e: Exception) {
                _uiState.update { it.copy(isLoading = false, error = e.message) }
            }
        }
    }
}

class ParentDashboardViewModelFactory(private val repository: ParentRepository) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(ParentDashboardViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return ParentDashboardViewModel(repository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
