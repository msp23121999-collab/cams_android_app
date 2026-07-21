package com.example.features.fees.providers

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.core.payments.RazorpayBridge
import com.example.core.payments.RazorpayResult
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
    val error: String? = null,
    val paymentInProgress: Boolean = false,
    val paymentMessage: String? = null,
    val paymentSuccess: Boolean = false,
    val documentAadhaarUrl: String? = null,
    val documentCommunityUrl: String? = null,
    val documentOtherUrl: String? = null,
    val isDocUploading: Boolean = false,
    val isLoanSaving: Boolean = false,
    val isRequestSubmitting: Boolean = false
)

class FeesViewModel(
    private val studentRepository: StudentRepository
) : ViewModel() {
    private val _uiState = MutableStateFlow(FeesUiState(isLoading = true))
    val uiState: StateFlow<FeesUiState> = _uiState.asStateFlow()

    private var pendingRecordId: String? = null

    init {
        fetchFees()
        viewModelScope.launch {
            RazorpayBridge.results.collect { result ->
                val recordId = pendingRecordId ?: return@collect
                when (result) {
                    is RazorpayResult.Success -> {
                        val orderId = result.orderId
                        val signature = result.signature
                        if (orderId == null || signature == null) {
                            _uiState.update {
                                it.copy(
                                    paymentInProgress = false,
                                    paymentMessage = "Payment could not be verified — missing order details."
                                )
                            }
                            return@collect
                        }
                        try {
                            val verifyResponse = studentRepository.verifyFeePayment(
                                recordId, orderId, result.razorpayPaymentId, signature
                            )
                            if (verifyResponse.isSuccessful && verifyResponse.body()?.status == "paid") {
                                _uiState.update {
                                    it.copy(
                                        paymentInProgress = false,
                                        paymentSuccess = true,
                                        paymentMessage = "Payment successful!"
                                    )
                                }
                                fetchFees()
                            } else {
                                _uiState.update {
                                    it.copy(
                                        paymentInProgress = false,
                                        paymentMessage = "Payment verification failed. Please contact the accounts office."
                                    )
                                }
                            }
                        } catch (e: Exception) {
                            _uiState.update {
                                it.copy(paymentInProgress = false, paymentMessage = "Payment verification error: ${e.message}")
                            }
                        }
                    }
                    is RazorpayResult.Failure -> {
                        // Razorpay error code 2 is a user-initiated cancellation.
                        val message = if (result.code == 2) "Payment cancelled." else (result.description ?: "Payment failed.")
                        _uiState.update { it.copy(paymentInProgress = false, paymentMessage = message) }
                    }
                }
                pendingRecordId = null
            }
        }
    }

    fun setActiveTab(tab: String) {
        _uiState.update { it.copy(activeTab = tab) }
        when (tab) {
            "receipts" -> loadReceipts()
            "loans" -> loadLoan()
            "assistance" -> loadAssistanceRequests()
            "scholarship" -> loadProfileDocuments()
        }
    }

    private fun loadReceipts() {
        viewModelScope.launch {
            try {
                val dtos = studentRepository.getFeeReceipts()
                val receipts = dtos.map { Receipt(id = it.id, date = it.date, head = it.head, amount = it.amount, mode = it.mode) }
                _uiState.update { it.copy(receipts = receipts) }
            } catch (e: Exception) {
                _uiState.update { it.copy(paymentMessage = "Failed to load receipts: ${e.message}") }
            }
        }
    }

    private fun loadLoan() {
        viewModelScope.launch {
            try {
                val dto = studentRepository.getStudentLoan()
                val loan = dto?.let {
                    LoanDetails(bank = it.bank, branch = it.branch, sanctioned = it.sanctioned,
                        interestRate = it.interestRate, emi = it.emi, outstanding = it.outstanding, status = it.status)
                }
                _uiState.update { it.copy(loanDetails = loan) }
            } catch (e: Exception) {
                _uiState.update { it.copy(paymentMessage = "Failed to load loan details: ${e.message}") }
            }
        }
    }

    fun saveLoan(bank: String, branch: String, sanctioned: Double, interestRate: Double, emi: Double, outstanding: Double) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoanSaving = true) }
            val result = studentRepository.upsertStudentLoan(bank, branch, sanctioned, interestRate, emi, outstanding)
            result.onSuccess { dto ->
                _uiState.update {
                    it.copy(
                        isLoanSaving = false,
                        loanDetails = LoanDetails(bank = dto.bank, branch = dto.branch, sanctioned = dto.sanctioned,
                            interestRate = dto.interestRate, emi = dto.emi, outstanding = dto.outstanding, status = dto.status)
                    )
                }
            }.onFailure { e ->
                _uiState.update { it.copy(isLoanSaving = false, paymentMessage = "Failed to save loan: ${e.message}") }
            }
        }
    }

    private fun loadAssistanceRequests() {
        viewModelScope.launch {
            try {
                val dtos = studentRepository.getAssistanceRequests()
                val requests = dtos.map { AssistanceRequest(id = it.id, type = it.type, date = it.createdAt, status = it.status, reason = it.reason) }
                _uiState.update { it.copy(requests = requests) }
            } catch (e: Exception) {
                _uiState.update { it.copy(paymentMessage = "Failed to load requests: ${e.message}") }
            }
        }
    }

    fun submitAssistanceRequest(type: String, reason: String) {
        viewModelScope.launch {
            _uiState.update { it.copy(isRequestSubmitting = true) }
            val result = studentRepository.createAssistanceRequest(type, reason)
            result.onSuccess {
                _uiState.update { it.copy(isRequestSubmitting = false) }
                loadAssistanceRequests()
            }.onFailure { e ->
                _uiState.update { it.copy(isRequestSubmitting = false, paymentMessage = "Failed to submit request: ${e.message}") }
            }
        }
    }

    private fun loadProfileDocuments() {
        viewModelScope.launch {
            try {
                val profile = studentRepository.getProfile()
                _uiState.update {
                    it.copy(
                        documentAadhaarUrl = profile?.documentAadhaarUrl,
                        documentCommunityUrl = profile?.documentCommunityUrl,
                        documentOtherUrl = profile?.documentOtherUrl
                    )
                }
            } catch (e: Exception) {
                // Non-fatal — leave doc status unknown
            }
        }
    }

    fun uploadScholarshipDocument(documentType: String, file: okhttp3.MultipartBody.Part) {
        viewModelScope.launch {
            _uiState.update { it.copy(isDocUploading = true) }
            try {
                val url = studentRepository.uploadProfileDocument(documentType, file)
                if (url != null) {
                    _uiState.update {
                        when (documentType) {
                            "aadhaar" -> it.copy(isDocUploading = false, documentAadhaarUrl = url)
                            "community" -> it.copy(isDocUploading = false, documentCommunityUrl = url)
                            else -> it.copy(isDocUploading = false, documentOtherUrl = url)
                        }
                    }
                } else {
                    _uiState.update { it.copy(isDocUploading = false, paymentMessage = "Upload failed") }
                }
            } catch (e: Exception) {
                _uiState.update { it.copy(isDocUploading = false, paymentMessage = "Upload error: ${e.message}") }
            }
        }
    }

    fun clearPaymentMessage() {
        _uiState.update { it.copy(paymentMessage = null, paymentSuccess = false) }
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
                            scholarshipAmount = 0.0,
                            paidAmount = dto.paidAmount,
                            remainingAmount = dto.remainingAmount,
                            totalAmount = dto.totalAmount,
                            semester = dto.semester
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
                            receipts = emptyList(),
                            notifications = emptyList()
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
        if (amount <= 0) {
            _uiState.update { it.copy(paymentMessage = "Enter a valid amount to pay.") }
            return
        }
        viewModelScope.launch {
            _uiState.update { it.copy(paymentInProgress = true, paymentMessage = null, paymentSuccess = false) }
            try {
                val orderResponse = studentRepository.createFeeOrder(recordId, amount)
                if (orderResponse.code() == 503) {
                    _uiState.update {
                        it.copy(
                            paymentInProgress = false,
                            paymentMessage = "Online payments are not yet available — contact the accounts office."
                        )
                    }
                    return@launch
                }
                if (!orderResponse.isSuccessful || orderResponse.body() == null) {
                    _uiState.update {
                        it.copy(paymentInProgress = false, paymentMessage = "Could not start payment. Please try again later.")
                    }
                    return@launch
                }
                val order = orderResponse.body()!!
                pendingRecordId = recordId
                val opened = RazorpayBridge.openCheckout(
                    keyId = order.keyId,
                    orderId = order.orderId,
                    amountRupees = order.amount,
                    currency = order.currency,
                    name = "CAMS Fee Payment",
                    description = "Fee payment for record $recordId"
                )
                if (!opened) {
                    pendingRecordId = null
                    _uiState.update {
                        it.copy(paymentInProgress = false, paymentMessage = "Unable to open payment screen. Please try again.")
                    }
                }
            } catch (e: Exception) {
                pendingRecordId = null
                _uiState.update { it.copy(paymentInProgress = false, paymentMessage = "Payment error: ${e.message}") }
            }
        }
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
