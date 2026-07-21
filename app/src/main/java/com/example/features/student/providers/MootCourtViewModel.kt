package com.example.features.student.providers

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.core.network.MootCourtMemorialDto
import com.example.core.repository.StudentRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class MootCourtState(
    val memorials: List<MootCourtMemorialDto> = emptyList(),
    val isLoading: Boolean = true,
    val isSaving: Boolean = false,
    val error: String? = null
)

class MootCourtViewModel(
    private val studentRepository: StudentRepository
) : ViewModel() {
    private val _uiState = MutableStateFlow(MootCourtState())
    val uiState: StateFlow<MootCourtState> = _uiState.asStateFlow()

    init {
        fetchMemorials()
    }

    fun fetchMemorials() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null) }
            try {
                val memorials = studentRepository.getMootCourtMemorials()
                _uiState.update { it.copy(memorials = memorials, isLoading = false) }
            } catch (e: Exception) {
                _uiState.update { it.copy(isLoading = false, error = e.message ?: "Failed to load memorials") }
            }
        }
    }

    fun createMemorial(title: String, caseName: String?, content: String, onDone: (Boolean) -> Unit = {}) {
        viewModelScope.launch {
            _uiState.update { it.copy(isSaving = true) }
            try {
                val created = studentRepository.createMootCourtMemorial(title, caseName, content, "draft")
                if (created != null) {
                    _uiState.update { it.copy(memorials = listOf(created) + it.memorials, isSaving = false) }
                    onDone(true)
                } else {
                    _uiState.update { it.copy(isSaving = false, error = "Failed to create memorial") }
                    onDone(false)
                }
            } catch (e: Exception) {
                _uiState.update { it.copy(isSaving = false, error = e.message ?: "Failed to create memorial") }
                onDone(false)
            }
        }
    }

    fun updateMemorial(id: String, title: String?, caseName: String?, content: String?, status: String?, onDone: (Boolean) -> Unit = {}) {
        viewModelScope.launch {
            _uiState.update { it.copy(isSaving = true) }
            try {
                val updated = studentRepository.updateMootCourtMemorial(id, title, caseName, content, status)
                if (updated != null) {
                    _uiState.update { state ->
                        state.copy(
                            memorials = state.memorials.map { if (it.id == id) updated else it },
                            isSaving = false
                        )
                    }
                    onDone(true)
                } else {
                    _uiState.update { it.copy(isSaving = false, error = "Failed to update memorial") }
                    onDone(false)
                }
            } catch (e: Exception) {
                _uiState.update { it.copy(isSaving = false, error = e.message ?: "Failed to update memorial") }
                onDone(false)
            }
        }
    }

    fun deleteMemorial(id: String, onDone: (Boolean) -> Unit = {}) {
        viewModelScope.launch {
            try {
                val success = studentRepository.deleteMootCourtMemorial(id)
                if (success) {
                    _uiState.update { state -> state.copy(memorials = state.memorials.filterNot { it.id == id }) }
                }
                onDone(success)
            } catch (e: Exception) {
                _uiState.update { it.copy(error = e.message ?: "Failed to delete memorial") }
                onDone(false)
            }
        }
    }
}

class MootCourtViewModelFactory(
    private val studentRepository: StudentRepository
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(MootCourtViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return MootCourtViewModel(studentRepository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
