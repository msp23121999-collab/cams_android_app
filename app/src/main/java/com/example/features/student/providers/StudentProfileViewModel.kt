package com.example.features.student.providers

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.example.core.repository.StudentRepository

import com.example.features.student.models.AttendanceResponse
import com.example.features.student.models.Internship
import com.example.features.student.models.MentorshipRecord
import com.example.features.student.models.MootCourt
import com.example.features.student.models.StudentProfileResponse
import com.example.core.network.StudentInternalMarkDto
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import androidx.lifecycle.viewModelScope

class StudentProfileViewModelFactory(private val studentRepository: StudentRepository) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(StudentProfileViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return StudentProfileViewModel(studentRepository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}

data class ProfileState(
    val profile: StudentProfileResponse? = null,
    val attendance: AttendanceResponse? = null,
    val mentorshipRecord: MentorshipRecord? = null,
    val marks: List<StudentInternalMarkDto> = emptyList(),
    val isLoading: Boolean = true,
    val isSaving: Boolean = false,
    val error: String? = null,
    val activeTab: String = "personal",
    // AI Hub state
    val quickPromptResponse: String? = null,
    val isQuickPromptLoading: Boolean = false,
    val quickPromptError: String? = null,
    val careerGuidance: String? = null,
    val isCareerGuidanceLoading: Boolean = false,
    val careerGuidanceError: String? = null,
    val hasCareerGuidanceLoaded: Boolean = false
)

class StudentProfileViewModel(
    private val studentRepository: StudentRepository
) : ViewModel() {
    private val _uiState = MutableStateFlow(ProfileState())
    val uiState: StateFlow<ProfileState> = _uiState.asStateFlow()

    init {
        fetchProfileData()
    }

    fun setActiveTab(tab: String) {
        _uiState.update { it.copy(activeTab = tab) }
        if (tab == "advisor" && _uiState.value.mentorshipRecord == null) {
            fetchMentorshipRecord()
        }
    }

    fun fetchProfileData() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null) }
            try {
                val profile = studentRepository.getProfile()
                val attendanceSummary = studentRepository.getAttendance()
                val marks = try {
                    studentRepository.getInternalMarks().filter { it.isApproved }
                } catch (e: Exception) { emptyList() }

                if (profile != null) {
                    _uiState.update {
                        it.copy(
                            profile = profile,
                            attendance = AttendanceResponse(percentage = attendanceSummary?.percentage?.toInt()),
                            marks = marks,
                            isLoading = false,
                            error = null
                        )
                    }
                } else {
                    _uiState.update { it.copy(isLoading = false, error = "Profile not found") }
                }
            } catch (e: Exception) {
                _uiState.update { it.copy(isLoading = false, error = e.message ?: "Failed to load profile") }
            }
        }
    }

    fun fetchMentorshipRecord() {
        viewModelScope.launch {
            try {
                val record = studentRepository.getMentorshipRecord()
                if (record != null) {
                    _uiState.update { it.copy(mentorshipRecord = record) }
                }
            } catch (e: Exception) {
                // Ignore for now
            }
        }
    }

    fun updateProfile(updatedProfile: StudentProfileResponse) {
        viewModelScope.launch {
            _uiState.update { it.copy(isSaving = true) }
            try {
                val newProfile = studentRepository.updateProfile(updatedProfile)
                if (newProfile != null) {
                    _uiState.update { it.copy(profile = newProfile, isSaving = false) }
                } else {
                    _uiState.update { it.copy(isSaving = false, error = "Failed to update profile") }
                }
            } catch (e: Exception) {
                _uiState.update { it.copy(isSaving = false, error = e.message ?: "Failed to save profile") }
            }
        }
    }

    fun submitForVerification() {
        viewModelScope.launch {
            _uiState.update { it.copy(isSaving = true) }
            try {
                val newProfile = studentRepository.submitForVerification()
                if (newProfile != null) {
                    _uiState.update { it.copy(
                        profile = newProfile,
                        isSaving = false
                    ) }
                } else {
                    _uiState.update { it.copy(isSaving = false, error = "Failed to submit for verification") }
                }
            } catch (e: Exception) {
                _uiState.update { it.copy(isSaving = false) }
            }
        }
    }

    fun uploadDocument(documentType: String, file: okhttp3.MultipartBody.Part) {
        viewModelScope.launch {
            try {
                val fileUrl = studentRepository.uploadProfileDocument(documentType, file)
                if (fileUrl != null) {
                    fetchProfileData() // Refresh profile with the newly attached document
                } else {
                    _uiState.update { it.copy(error = "Failed to upload document") }
                }
            } catch (e: Exception) {
                _uiState.update { it.copy(error = e.message ?: "Failed to upload document") }
            }
        }
    }

    suspend fun changePassword(currentPassword: String, newPassword: String) {
        studentRepository.changePassword(currentPassword, newPassword)
    }

    fun sendQuickPrompt(prompt: String) {
        viewModelScope.launch {
            _uiState.update { it.copy(isQuickPromptLoading = true, quickPromptError = null, quickPromptResponse = null) }
            try {
                val result = studentRepository.sendChatMessage(prompt, null)
                if (result != null) {
                    _uiState.update { it.copy(isQuickPromptLoading = false, quickPromptResponse = result.response) }
                } else {
                    _uiState.update { it.copy(isQuickPromptLoading = false, quickPromptError = "Failed to get a response. Please try again.") }
                }
            } catch (e: Exception) {
                _uiState.update { it.copy(isQuickPromptLoading = false, quickPromptError = e.message ?: "Failed to get a response.") }
            }
        }
    }

    fun fetchCareerGuidance(force: Boolean = false) {
        if (!force && _uiState.value.hasCareerGuidanceLoaded) return
        viewModelScope.launch {
            _uiState.update { it.copy(isCareerGuidanceLoading = true, careerGuidanceError = null) }
            try {
                val profile = _uiState.value.profile
                val prompt = buildString {
                    append("Based on my academic profile (CGPA: ${profile?.cgpa ?: "N/A"}, ")
                    append("Department: ${profile?.departmentName ?: profile?.courseName ?: "Law"}, ")
                    append("Internships: ${profile?.internships?.joinToString { i -> i.organization ?: i.company ?: "N/A" } ?: "None"}), ")
                    append("suggest a suitable legal specialization area and briefly explain why.")
                }
                val result = studentRepository.sendChatMessage(prompt, null)
                if (result != null) {
                    _uiState.update {
                        it.copy(
                            isCareerGuidanceLoading = false,
                            careerGuidance = result.response,
                            hasCareerGuidanceLoaded = true
                        )
                    }
                } else {
                    _uiState.update { it.copy(isCareerGuidanceLoading = false, careerGuidanceError = "Failed to generate career guidance.") }
                }
            } catch (e: Exception) {
                _uiState.update { it.copy(isCareerGuidanceLoading = false, careerGuidanceError = e.message ?: "Failed to generate career guidance.") }
            }
        }
    }
}
