package com.example.features.hod.providers

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.core.network.HODWorkloadDto
import com.example.core.network.HODMentorDto
import com.example.core.network.LeaveRequestDto
import com.example.core.network.FacultyStudentDto
import com.example.core.repository.HODRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

data class HODWorkloadsUiState(
    val workloads: List<HODWorkloadDto> = emptyList(),
    val isLoading: Boolean = true,
    val error: String? = null
)

class HODWorkloadsViewModel(private val repository: HODRepository) : ViewModel() {
    private val _uiState = MutableStateFlow(HODWorkloadsUiState())
    val uiState: StateFlow<HODWorkloadsUiState> = _uiState.asStateFlow()

    init {
        loadData()
    }

    private fun loadData() {
        viewModelScope.launch {
            try {
                _uiState.value = _uiState.value.copy(isLoading = true, error = null)
                val workloads = repository.getHODWorkloads()
                _uiState.value = _uiState.value.copy(
                    workloads = workloads,
                    isLoading = false
                )
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    error = e.message
                )
            }
        }
    }
}

class HODWorkloadsViewModelFactory(private val repository: HODRepository) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(HODWorkloadsViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return HODWorkloadsViewModel(repository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}

data class HODLeaveApprovalsUiState(
    val pendingLeaves: List<LeaveRequestDto> = emptyList(),
    val isLoading: Boolean = true,
    val error: String? = null
)

class HODLeaveApprovalsViewModel(private val repository: HODRepository) : ViewModel() {
    private val _uiState = MutableStateFlow(HODLeaveApprovalsUiState())
    val uiState: StateFlow<HODLeaveApprovalsUiState> = _uiState.asStateFlow()

    init {
        loadData()
    }

    fun loadData() {
        viewModelScope.launch {
            try {
                _uiState.value = _uiState.value.copy(isLoading = true, error = null)
                val leaves = repository.getPendingLeaveApprovals()
                _uiState.value = _uiState.value.copy(
                    pendingLeaves = leaves,
                    isLoading = false
                )
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    error = e.message
                )
            }
        }
    }

    fun approveLeave(id: String, status: String, remarks: String) {
        viewModelScope.launch {
            try {
                repository.approveLeave(id, status, remarks)
                loadData()
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(error = e.message)
            }
        }
    }
}

class HODLeaveApprovalsViewModelFactory(private val repository: HODRepository) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(HODLeaveApprovalsViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return HODLeaveApprovalsViewModel(repository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}

data class HODFacultyManagementUiState(
    val facultyData: List<FacultyStudentDto> = emptyList(),
    val isLoading: Boolean = true,
    val error: String? = null
)

class HODFacultyManagementViewModel(private val repository: HODRepository) : ViewModel() {
    private val _uiState = MutableStateFlow(HODFacultyManagementUiState())
    val uiState: StateFlow<HODFacultyManagementUiState> = _uiState.asStateFlow()

    init {
        loadData()
    }

    private fun loadData() {
        viewModelScope.launch {
            try {
                _uiState.value = _uiState.value.copy(isLoading = true, error = null)
                val data = repository.getFacultyManagementData()
                _uiState.value = _uiState.value.copy(
                    facultyData = data,
                    isLoading = false
                )
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    error = e.message
                )
            }
        }
    }
}

class HODFacultyManagementViewModelFactory(private val repository: HODRepository) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(HODFacultyManagementViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return HODFacultyManagementViewModel(repository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}

data class HODMentorAssignmentUiState(
    val mentors: List<HODMentorDto> = emptyList(),
    val students: List<FacultyStudentDto> = emptyList(),
    val isLoading: Boolean = true,
    val error: String? = null
)

class HODMentorAssignmentViewModel(private val repository: HODRepository) : ViewModel() {
    private val _uiState = MutableStateFlow(HODMentorAssignmentUiState())
    val uiState: StateFlow<HODMentorAssignmentUiState> = _uiState.asStateFlow()

    init {
        loadData()
    }

    fun loadData() {
        viewModelScope.launch {
            try {
                _uiState.value = _uiState.value.copy(isLoading = true, error = null)
                val mentors = repository.getHODMentors()
                val students = repository.getStudentManagementData()
                _uiState.value = _uiState.value.copy(
                    mentors = mentors,
                    students = students,
                    isLoading = false
                )
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    error = e.message
                )
            }
        }
    }

    fun assignMentor(studentId: String, facultyId: String) {
        viewModelScope.launch {
            try {
                repository.assignHODMentor(studentId, facultyId)
                loadData()
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(error = e.message)
            }
        }
    }
}

class HODMentorAssignmentViewModelFactory(private val repository: HODRepository) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(HODMentorAssignmentViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return HODMentorAssignmentViewModel(repository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
