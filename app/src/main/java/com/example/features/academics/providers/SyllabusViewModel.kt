package com.example.features.academics.providers

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.example.core.repository.StudentRepository

import com.example.features.academics.models.*
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import androidx.lifecycle.viewModelScope

class SyllabusViewModelFactory(private val studentRepository: StudentRepository) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(SyllabusViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return SyllabusViewModel(studentRepository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}

data class SyllabusUiState(
    val syllabus: Map<String, SyllabusProgress> = emptyMap(),
    val lessonPlanTracking: List<LessonPlanItem> = emptyList(),
    val isLoading: Boolean = false,
    val error: String? = null,
    val expandedSubject: String? = null
)

class SyllabusViewModel(private val studentRepository: StudentRepository) : ViewModel() {
    private val _uiState = MutableStateFlow(SyllabusUiState())
    val uiState: StateFlow<SyllabusUiState> = _uiState.asStateFlow()

    init {
        loadData()
    }

    fun toggleSubject(subject: String) {
        _uiState.update { 
            it.copy(expandedSubject = if (it.expandedSubject == subject) null else subject)
        }
    }

    fun loadData() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null) }
            try {
                val syllabusMap = studentRepository.getSyllabusProgress()
                val tracking = studentRepository.getLessonPlanTracking()

                _uiState.update { it.copy(
                    syllabus = syllabusMap,
                    lessonPlanTracking = tracking,
                    isLoading = false
                ) }
            } catch (e: Exception) {
                _uiState.update { it.copy(isLoading = false, error = e.message) }
            }
        }
    }
}
