package com.example.features.hod.providers

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.core.network.AcademicSetupDto
import com.example.core.network.SubjectAllocationDto
import com.example.core.network.HODSubstitutionDto
import com.example.core.repository.HODRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class HODSubjectAllocationUiState(
    val isLoading: Boolean = false,
    val setup: AcademicSetupDto? = null,
    val allocations: List<SubjectAllocationDto> = emptyList(),
    val error: String? = null
)

class HODSubjectAllocationViewModel(
    private val repository: HODRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(HODSubjectAllocationUiState())
    val uiState: StateFlow<HODSubjectAllocationUiState> = _uiState.asStateFlow()

    init {
        loadData()
    }

    private fun loadData() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null) }
            
            val setupResult = repository.getAcademicSetup()
            val allocResult = repository.getSubjectAllocations()
            
            _uiState.update { 
                it.copy(
                    isLoading = false,
                    setup = setupResult.getOrNull(),
                    allocations = allocResult.getOrNull() ?: emptyList(),
                    error = setupResult.exceptionOrNull()?.message ?: allocResult.exceptionOrNull()?.message
                )
            }
        }
    }
}

class HODSubjectAllocationViewModelFactory(private val repository: HODRepository) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(HODSubjectAllocationViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return HODSubjectAllocationViewModel(repository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}



data class HODSubstitutionUiState(
    val isLoading: Boolean = false,
    val substitutions: List<HODSubstitutionDto> = emptyList(),
    val error: String? = null
)

class HODSubstitutionViewModel(
    private val repository: HODRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(HODSubstitutionUiState())
    val uiState: StateFlow<HODSubstitutionUiState> = _uiState.asStateFlow()

    init {
        loadData()
    }

    private fun loadData() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null) }
            val result = repository.getSubstitutions()
            _uiState.update {
                it.copy(
                    isLoading = false,
                    substitutions = result.getOrNull() ?: emptyList(),
                    error = result.exceptionOrNull()?.message
                )
            }
        }
    }
}

class HODSubstitutionViewModelFactory(private val repository: HODRepository) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(HODSubstitutionViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return HODSubstitutionViewModel(repository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
