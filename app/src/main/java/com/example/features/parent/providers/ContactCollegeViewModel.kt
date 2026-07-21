package com.example.features.parent.providers

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.core.repository.ParentRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class ContactCollegeState(
    val isSubmitting: Boolean = false,
    val successMsg: String? = null,
    val errorMsg: String? = null,
    val contacts: List<com.example.core.network.CollegeContactDto> = emptyList(),
    val campusName: String = "",
    val campusAddress: String = ""
)

class ContactCollegeViewModel(private val repository: ParentRepository) : ViewModel() {
    private val _uiState = MutableStateFlow(ContactCollegeState())
    val uiState: StateFlow<ContactCollegeState> = _uiState.asStateFlow()

    init {
        loadCollegeInfo()
    }

    fun loadCollegeInfo() {
        viewModelScope.launch {
            val info = repository.getCollegeInfo()
            if (info != null) {
                _uiState.update { it.copy(contacts = info.contacts, campusName = info.campusName, campusAddress = info.campusAddress) }
            }
        }
    }

    fun submitInquiry(name: String, email: String, subject: String, message: String) {
        viewModelScope.launch {
            _uiState.update { it.copy(isSubmitting = true) }
            val success = repository.submitInquiry(name, email, subject, message)
            _uiState.update {
                if (success) it.copy(isSubmitting = false, successMsg = "Your message has been sent to the college.")
                else it.copy(isSubmitting = false, errorMsg = "Failed to send message. Please try again.")
            }
        }
    }

    fun clearMessages() {
        _uiState.update { it.copy(successMsg = null, errorMsg = null) }
    }
}

class ContactCollegeViewModelFactory(private val repository: ParentRepository) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(ContactCollegeViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return ContactCollegeViewModel(repository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
