package com.example.features.student.providers

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.example.core.repository.StudentRepository

import com.example.features.student.models.AttendanceResponse
import com.example.features.student.models.Internship
import com.example.features.student.models.MentorshipRecord
import com.example.features.student.models.MootCourt
import com.example.features.student.models.StudentProfileResponse
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
    val isLoading: Boolean = true,
    val isSaving: Boolean = false,
    val error: String? = null,
    val activeTab: String = "personal"
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
                // Assuming attendance is separate or can be fetched
                val attendanceDto = studentRepository.getDashboard() // Or a specific attendance call
                
                if (profile != null) {
                    _uiState.update { 
                        it.copy(
                            profile = profile,
                            attendance = AttendanceResponse(percentage = profile.cgpa?.let { (it * 10).toInt() } ?: 75), // Mocking attendance from CGPA if missing
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
                delay(500)
                val mockMentorship = MentorshipRecord(
                    meetingLog = "Alex has shown remarkable improvement in drafting skills this semester.",
                    academicReview = "Excellent academic performance, particularly in Criminal Procedure.",
                    improvementPlan = "Needs to balance academic workload with extracurricular legal aid activities.",
                    remarks = "Good progress overall.",
                    followUp = "Plan to review next month."
                )
                _uiState.update { it.copy(mentorshipRecord = mockMentorship) }
            } catch (e: Exception) {
                // Ignore for now
            }
        }
    }

    fun updateProfile(updatedProfile: StudentProfileResponse) {
        viewModelScope.launch {
            _uiState.update { it.copy(isSaving = true) }
            try {
                // Simulate API call
                delay(1500)
                _uiState.update { it.copy(profile = updatedProfile, isSaving = false) }
            } catch (e: Exception) {
                _uiState.update { it.copy(isSaving = false, error = e.message ?: "Failed to save profile") }
            }
        }
    }

    fun submitForVerification() {
        viewModelScope.launch {
            _uiState.update { it.copy(isSaving = true) }
            try {
                delay(1000)
                val current = _uiState.value.profile
                if (current != null) {
                    _uiState.update { it.copy(
                        profile = current.copy(verificationStatus = "SUBMITTED"),
                        isSaving = false
                    ) }
                }
            } catch (e: Exception) {
                _uiState.update { it.copy(isSaving = false) }
            }
        }
    }
}
