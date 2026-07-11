package com.example.features.student.providers

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.features.academics.models.InternalMarkRecord
import com.example.core.network.InternalMarkDto
import com.example.core.repository.StudentRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class MarksState(
    val marks: List<InternalMarkRecord> = emptyList(),
    val isLoading: Boolean = false,
    val error: String? = null
)

class MarksViewModel(
    private val studentRepository: StudentRepository
) : ViewModel() {
    private val _uiState = MutableStateFlow(MarksState())
    val uiState: StateFlow<MarksState> = _uiState.asStateFlow()

    init {
        fetchMarks()
    }

    fun fetchMarks() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null) }
            try {
                val dtos = studentRepository.getInternalMarks()
                val marks = dtos.map { dto ->
                    InternalMarkRecord(
                        id = dto.id,
                        subjectName = dto.subjectName,
                        examScore = dto.markObtained,
                        assignmentScore = 0.0,
                        presentationScore = 0.0,
                        vivaScore = 0.0,
                        attendanceScore = 0.0,
                        totalScore = dto.markObtained
                    )
                }
                _uiState.update { it.copy(marks = marks, isLoading = false) }
            } catch (e: Exception) {
                _uiState.update { it.copy(isLoading = false, error = e.message ?: "Failed to load marks") }
            }
        }
    }
}

class MarksViewModelFactory(
    private val studentRepository: StudentRepository
) : androidx.lifecycle.ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(MarksViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return MarksViewModel(studentRepository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
