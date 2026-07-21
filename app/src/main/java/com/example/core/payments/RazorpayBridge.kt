package com.example.core.payments

import android.app.Activity
import com.razorpay.Checkout
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import org.json.JSONObject
import java.lang.ref.WeakReference

/**
 * Result of a Razorpay checkout attempt, delivered back to whichever ViewModel
 * triggered [RazorpayBridge.openCheckout]. This bridges the gap between the
 * single hosting Activity (which must implement Razorpay's listener interface)
 * and the Compose/ViewModel layer that has no direct Activity reference.
 */
sealed class RazorpayResult {
    data class Success(val razorpayPaymentId: String, val orderId: String?, val signature: String?) : RazorpayResult()
    data class Failure(val code: Int, val description: String?) : RazorpayResult()
}

/**
 * Singleton bridge that the hosting Activity (see MainActivity) registers itself
 * against, and that ViewModels use to launch a Razorpay checkout and observe the result.
 *
 * NOTE: Razorpay's Checkout.open() uses reflection to find a
 * PaymentResultWithDataListener/PaymentResultListener implementation on the exact
 * Activity instance passed in — so MainActivity itself must implement that interface
 * and forward callbacks here via [emitSuccess]/[emitFailure]. This object intentionally
 * does NOT implement the listener interface directly.
 */
object RazorpayBridge {

    private var activityRef: WeakReference<Activity>? = null

    private val _results = MutableSharedFlow<RazorpayResult>(extraBufferCapacity = 1)
    val results: SharedFlow<RazorpayResult> = _results

    fun attach(activity: Activity) {
        activityRef = WeakReference(activity)
        Checkout.preload(activity.applicationContext)
    }

    fun detach(activity: Activity) {
        if (activityRef?.get() === activity) {
            activityRef = null
        }
    }

    /**
     * Opens the Razorpay checkout UI. Requires an attached Activity (set via [attach] in
     * onCreate/onResume of the single hosting Activity). Returns false if no Activity
     * is currently attached (e.g. app backgrounded), so the caller can show an error.
     */
    fun openCheckout(
        keyId: String,
        orderId: String,
        amountRupees: Double,
        currency: String,
        name: String,
        description: String,
        prefillEmail: String? = null,
        prefillContact: String? = null
    ): Boolean {
        val activity = activityRef?.get() ?: return false
        val checkout = Checkout()
        checkout.setKeyID(keyId)
        val options = JSONObject().apply {
            put("order_id", orderId)
            put("amount", (amountRupees * 100).toInt())
            put("currency", currency)
            put("name", name)
            put("description", description)
            val prefill = JSONObject()
            if (!prefillEmail.isNullOrBlank()) prefill.put("email", prefillEmail)
            if (!prefillContact.isNullOrBlank()) prefill.put("contact", prefillContact)
            put("prefill", prefill)
        }
        return try {
            checkout.open(activity, options)
            true
        } catch (e: Exception) {
            _results.tryEmit(RazorpayResult.Failure(-1, e.message))
            false
        }
    }

    fun emitSuccess(razorpayPaymentId: String?, orderId: String?, signature: String?) {
        if (razorpayPaymentId != null) {
            _results.tryEmit(RazorpayResult.Success(razorpayPaymentId, orderId, signature))
        } else {
            _results.tryEmit(RazorpayResult.Failure(-1, "Missing payment id"))
        }
    }

    fun emitFailure(code: Int, description: String?) {
        _results.tryEmit(RazorpayResult.Failure(code, description))
    }
}
