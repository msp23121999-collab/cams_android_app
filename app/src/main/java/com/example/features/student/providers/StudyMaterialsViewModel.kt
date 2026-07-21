package com.example.features.student.providers

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.features.academics.models.StudyMaterial
import com.example.core.network.StudyMaterialDto
import com.example.core.repository.StudentRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import androidx.paging.Pager
import androidx.paging.PagingConfig
import androidx.paging.PagingData
import androidx.paging.cachedIn
import androidx.paging.map
import com.example.core.network.GenericPagingSource
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

data class StudyMaterialsState(
    val materials: List<StudyMaterial> = emptyList(),
    val isLoading: Boolean = false,
    val error: String? = null
)

class StudyMaterialsViewModel(
    private val studentRepository: StudentRepository
) : ViewModel() {
    private val _uiState = MutableStateFlow(StudyMaterialsState())
    val uiState: StateFlow<StudyMaterialsState> = _uiState.asStateFlow()

    val materialsPagingFlow: Flow<PagingData<StudyMaterial>> = Pager(
        config = PagingConfig(pageSize = 20, enablePlaceholders = false),
        pagingSourceFactory = { GenericPagingSource({ skip, limit -> studentRepository.getStudyMaterialsPaged(skip, limit) }) }
    ).flow
        .map { pagingData ->
            pagingData.map { dto ->
                StudyMaterial(
                    id = dto.id,
                    title = dto.title,
                    subject = dto.subjectName ?: "General",
                    category = dto.type ?: "Lecture Notes",
                    uploadDate = dto.uploadDate ?: "",
                    fileUrl = dto.fileUrl,
                    facultyName = dto.facultyName ?: "Faculty"
                )
            }
        }
        .cachedIn(viewModelScope)

    init {
        fetchMaterials()
    }

    fun fetchMaterials() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null) }
            try {
                val dtos = studentRepository.getStudyMaterials()
                val materials = dtos.map { dto ->
                    StudyMaterial(
                        id = dto.id,
                        title = dto.title,
                        subject = dto.subjectName ?: "General",
                        category = dto.type ?: "Lecture Notes",
                        uploadDate = dto.uploadDate ?: "",
                        fileUrl = dto.fileUrl,
                        facultyName = dto.facultyName ?: "Faculty"
                    )
                }
                _uiState.update { it.copy(materials = materials, isLoading = false) }
            } catch (e: Exception) {
                _uiState.update { it.copy(isLoading = false, error = e.message ?: "Failed to load study materials") }
            }
        }
    }
}

class StudyMaterialsViewModelFactory(
    private val studentRepository: StudentRepository
) : androidx.lifecycle.ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(StudyMaterialsViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return StudyMaterialsViewModel(studentRepository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
