package com.example.features.parent.providers

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.core.payments.RazorpayBridge
import com.example.core.payments.RazorpayResult
import com.example.core.repository.ParentRepository
import com.example.features.parent.models.*
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class ParentFeesState(
    val feeLedger: ChildFeeLedger? = null,
    val childProfile: ChildProfileExtended? = null,
    val isLoading: Boolean = true,
    val error: String? = null,
    val paymentInProgress: Boolean = false,
    val paymentMessage: String? = null,
    val paymentSuccess: Boolean = false
)

class ParentFeesViewModel(private val repository: ParentRepository) : ViewModel() {
    private val _uiState = MutableStateFlow(ParentFeesState())
    val uiState: StateFlow<ParentFeesState> = _uiState.asStateFlow()

    var currentChildId: String? = null
        private set

    private var pendingRecordId: String? = null
    private var pendingChildId: String? = null

    init {
        viewModelScope.launch {
            repository.selectedChildId.collect { id ->
                currentChildId = id
                loadData()
            }
        }
        viewModelScope.launch {
            RazorpayBridge.results.collect { result ->
                val recordId = pendingRecordId ?: return@collect
                val childId = pendingChildId
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
                            pendingRecordId = null
                            pendingChildId = null
                            return@collect
                        }
                        try {
                            val verifyResponse = repository.verifyFeePayment(
                                recordId, orderId, result.razorpayPaymentId, signature, childId
                            )
                            if (verifyResponse.isSuccessful && verifyResponse.body()?.status == "paid") {
                                _uiState.update {
                                    it.copy(
                                        paymentInProgress = false,
                                        paymentSuccess = true,
                                        paymentMessage = "Payment successful!"
                                    )
                                }
                                loadData()
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
                pendingChildId = null
            }
        }
    }

    fun clearPaymentMessage() {
        _uiState.update { it.copy(paymentMessage = null, paymentSuccess = false) }
    }

    fun payFee(recordId: String, amount: Double) {
        if (amount <= 0) {
            _uiState.update { it.copy(paymentMessage = "Enter a valid amount to pay.") }
            return
        }
        viewModelScope.launch {
            _uiState.update { it.copy(paymentInProgress = true, paymentMessage = null, paymentSuccess = false) }
            try {
                val childId = currentChildId
                val orderResponse = repository.createFeeOrder(recordId, amount, childId)
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
                pendingChildId = childId
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
                    pendingChildId = null
                    _uiState.update {
                        it.copy(paymentInProgress = false, paymentMessage = "Unable to open payment screen. Please try again.")
                    }
                }
            } catch (e: Exception) {
                pendingRecordId = null
                pendingChildId = null
                _uiState.update { it.copy(paymentInProgress = false, paymentMessage = "Payment error: ${e.message}") }
            }
        }
    }

    fun setChild(id: String) {
        currentChildId = id
        loadData()
    }

    fun loadData() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null) }
            try {
                val ledger = repository.getFeeStatus(currentChildId)
                val profile = repository.getChildProfile(currentChildId)
                _uiState.update { it.copy(feeLedger = ledger, childProfile = profile, isLoading = false) }
            } catch (e: Exception) {
                _uiState.update { it.copy(isLoading = false, error = e.message) }
            }
        }
    }
}

class ParentFeesViewModelFactory(private val repository: ParentRepository) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(ParentFeesViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return ParentFeesViewModel(repository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
