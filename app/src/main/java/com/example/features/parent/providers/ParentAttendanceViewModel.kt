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

data class ParentAttendanceState(
    val attendanceRecords: List<AttendanceRecord> = emptyList(),
    val subjectAttendance: List<SubjectAttendance> = emptyList(),
    val isLoading: Boolean = true,
    val error: String? = null
)

class ParentAttendanceViewModel(private val repository: ParentRepository) : ViewModel() {
    private val _uiState = MutableStateFlow(ParentAttendanceState())
    val uiState: StateFlow<ParentAttendanceState> = _uiState.asStateFlow()

    var currentChildId: String? = null
        private set

    init {
        viewModelScope.launch {
            repository.selectedChildId.collect { id ->
                currentChildId = id
                loadData()
            }
        }
    }

    fun setChild(id: String) {
        currentChildId = id
        loadData()
    }

    fun loadData() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null) }
            try {
                val records = repository.getAttendance(currentChildId)
                val subjects = repository.getSubjectAttendance(currentChildId)
                
                _uiState.update { it.copy(
                    attendanceRecords = records,
                    subjectAttendance = subjects,
                    isLoading = false
                ) }
            } catch (e: Exception) {
                _uiState.update { it.copy(isLoading = false, error = e.message) }
            }
        }
    }
}

class ParentAttendanceViewModelFactory(private val repository: ParentRepository) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(ParentAttendanceViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return ParentAttendanceViewModel(repository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
