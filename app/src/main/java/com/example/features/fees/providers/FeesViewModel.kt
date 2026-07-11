package com.example.features.fees.providers

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.core.repository.StudentRepository
import com.example.features.fees.models.*
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class FeesUiState(
    val summary: FeeSummary? = null,
    val activeTab: String = "overview",
    val receipts: List<Receipt> = emptyList(),
    val scholarships: List<ScholarshipType> = emptyList(),
    val loanDetails: LoanDetails? = null,
    val requests: List<AssistanceRequest> = emptyList(),
    val notifications: List<FinancialNotification> = emptyList(),
    val isLoading: Boolean = false,
    val error: String? = null
)

class FeesViewModel(
    private val studentRepository: StudentRepository
) : ViewModel() {
    private val _uiState = MutableStateFlow(FeesUiState(isLoading = true))
    val uiState: StateFlow<FeesUiState> = _uiState.asStateFlow()

    init {
        fetchFees()
    }

    fun setActiveTab(tab: String) {
        _uiState.update { it.copy(activeTab = tab) }
    }

    fun fetchFees() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null) }
            try {
                // Fetch from repository
                val summaryResponse = studentRepository.getFees()
                
                if (summaryResponse != null) {
                    // Convert DTOs to models expected by FeesScreen
                    val records = summaryResponse.records.map { dto ->
                        FeeRecord(
                            id = dto.id,
                            feeType = dto.title,
                            amount = dto.amount,
                            grossAmount = dto.amount,
                            dueDate = dto.dueDate,
                            status = dto.status.lowercase(),
                            scholarshipAmount = 0.0
                        )
                    }

                    val summary = FeeSummary(
                        totalFees = summaryResponse.totalFees,
                        scholarshipDeduction = summaryResponse.scholarshipDeduction,
                        amountPaid = summaryResponse.amountPaid,
                        pendingBalance = summaryResponse.pendingBalance,
                        netFees = summaryResponse.netFees,
                        dueDate = summaryResponse.dueDate,
                        assignedScholarshipTypeId = null,
                        records = records
                    )

                    _uiState.update { 
                        it.copy(
                            summary = summary,
                            isLoading = false,
                            receipts = listOf(Receipt("1", "2026-01-10", "Semester Fees", 45000.0, "Online", "TXN123456")),
                            notifications = listOf(FinancialNotification(1, "success", "Fee payment successful", "2 hours ago"))
                        ) 
                    }
                } else {
                    _uiState.update { it.copy(isLoading = false, error = "No fee data available") }
                }
            } catch (e: Exception) {
                _uiState.update { it.copy(isLoading = false, error = e.message) }
            }
        }
    }

    fun payFee(recordId: String, amount: Double, mode: String) {
        // Implement payment logic
    }
}

class FeesViewModelFactory(
    private val studentRepository: StudentRepository
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(FeesViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return FeesViewModel(studentRepository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
