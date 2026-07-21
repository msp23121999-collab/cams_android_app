package com.example.features.admin.providers

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.core.repository.AdminRepository
import com.example.features.admin.models.AdminCourse
import com.example.features.admin.models.AdminDegree
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class AdminCourseSetupState(
    val isLoading: Boolean = true,
    val error: String? = null,
    val degrees: List<AdminDegree> = emptyList(),
    val selectedDegreeId: String? = null,
    val courses: List<AdminCourse> = emptyList(),
    val isSaving: Boolean = false,
    val saveError: String? = null,
    val saveSuccess: Boolean = false
)

class AdminCourseSetupViewModel(private val repository: AdminRepository) : ViewModel() {
    private val _uiState = MutableStateFlow(AdminCourseSetupState())
    val uiState: StateFlow<AdminCourseSetupState> = _uiState.asStateFlow()

    init { loadDegrees() }

    private fun loadDegrees() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null) }
            try {
                val degrees = repository.getDegrees()
                _uiState.update { it.copy(degrees = degrees, isLoading = false) }
                // Default to the first degree so the screen shows data immediately.
                degrees.firstOrNull()?.let { selectDegree(it.id) }
            } catch (e: Exception) {
                _uiState.update { it.copy(isLoading = false, error = e.message ?: "Failed to load degrees") }
            }
        }
    }

    fun selectDegree(degreeId: String) {
        _uiState.update { it.copy(selectedDegreeId = degreeId, isLoading = true, error = null) }
        viewModelScope.launch {
            try {
                val courses = repository.getCoursesByDegree(degreeId)
                _uiState.update { it.copy(courses = courses, isLoading = false) }
            } catch (e: Exception) {
                _uiState.update { it.copy(isLoading = false, error = e.message ?: "Failed to load courses") }
            }
        }
    }

    private fun mutate(block: suspend () -> Unit) {
        viewModelScope.launch {
            _uiState.update { it.copy(isSaving = true, saveError = null, saveSuccess = false) }
            try {
                block()
                _uiState.value.selectedDegreeId?.let { id ->
                    val courses = repository.getCoursesByDegree(id)
                    _uiState.update { it.copy(courses = courses) }
                }
                _uiState.update { it.copy(isSaving = false, saveSuccess = true) }
            } catch (e: Exception) {
                _uiState.update { it.copy(isSaving = false, saveError = e.message ?: "Operation failed") }
            }
        }
    }

    fun createCourse(code: String, name: String, credits: Int, semester: Int) {
        val degreeId = _uiState.value.selectedDegreeId ?: return
        val deptId = _uiState.value.degrees.firstOrNull { it.id == degreeId }?.let { null }
        mutate {
            repository.createCourse(
                mapOf(
                    "code" to code, "name" to name, "credits" to credits,
                    "semester" to semester, "degree_id" to degreeId, "dept_id" to deptId
                )
            )
        }
    }

    fun updateCourse(courseId: String, code: String, name: String, credits: Int, semester: Int) = mutate {
        repository.updateCourse(
            courseId,
            mapOf("code" to code, "name" to name, "credits" to credits, "semester" to semester)
        )
    }

    fun deleteCourse(courseId: String) = mutate { repository.deleteCourse(courseId) }

    fun clearSaveStatus() {
        _uiState.update { it.copy(saveError = null, saveSuccess = false) }
    }
}

class AdminCourseSetupViewModelFactory(private val repository: AdminRepository) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(AdminCourseSetupViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return AdminCourseSetupViewModel(repository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
