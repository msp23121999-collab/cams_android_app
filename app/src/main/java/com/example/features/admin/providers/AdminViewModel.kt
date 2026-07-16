package com.example.features.admin.providers

import androidx.lifecycle.ViewModel
import com.example.features.admin.models.*
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.core.repository.AdminRepository
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

data class AdminState(
    val metrics: AdminDashboardMetrics = AdminDashboardMetrics(emptyList()),
    val systemHealth: List<SystemStatus> = emptyList(),
    val degrees: List<AdminDegree> = emptyList(),
    val isLoading: Boolean = false,
    val error: String? = null
)

class AdminViewModel(private val repository: AdminRepository) : ViewModel() {
    private val _uiState = MutableStateFlow(AdminState())
    val uiState: StateFlow<AdminState> = _uiState.asStateFlow()

    init {
        fetchDashboardData()
    }

    fun fetchDashboardData() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null) }
            try {
                val metrics = repository.getDashboardMetrics()
                val degrees = repository.getDegrees()
                
                _uiState.update { 
                    it.copy(
                        metrics = metrics,
                        degrees = degrees,
                        isLoading = false
                    ) 
                }
            } catch (e: Exception) {
                _uiState.update { it.copy(isLoading = false, error = e.message) }
            }
        }
    }
}

class AdminViewModelFactory(private val repository: AdminRepository) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(AdminViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return AdminViewModel(repository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
// --- Admin Academic Catalog ViewModel ---
data class AdminAcademicCatalogState(
    val departments: List<com.example.features.admin.models.AdminDepartment> = emptyList(),
    val degrees: List<com.example.features.admin.models.AdminDegree> = emptyList(),
    val courses: List<com.example.features.admin.models.AdminCourse> = emptyList(),
    val isLoading: Boolean = false,
    val error: String? = null
)

class AdminAcademicCatalogViewModel(private val repository: com.example.core.repository.AdminRepository) : androidx.lifecycle.ViewModel() {
    private val _uiState = kotlinx.coroutines.flow.MutableStateFlow(AdminAcademicCatalogState())
    val uiState: kotlinx.coroutines.flow.StateFlow<AdminAcademicCatalogState> = _uiState.asStateFlow()

    init { loadCatalog() }

    fun loadCatalog() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null) }
            try {
                val depts = repository.getDepartments()
                val degrees = repository.getDegrees()
                val courses = repository.getCourses()
                _uiState.update { it.copy(departments = depts, degrees = degrees, courses = courses, isLoading = false) }
            } catch (e: Exception) {
                _uiState.update { it.copy(isLoading = false, error = e.message) }
            }
        }
    }
}
// --- Admin Backups ViewModel ---