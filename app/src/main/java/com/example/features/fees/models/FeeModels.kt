package com.example.features.fees.models

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class FeeRecord(
    @Json(name = "record_id") val id: String,
    @Json(name = "fee_type") val feeType: String,
    val amount: Double,
    @Json(name = "gross_amount") val grossAmount: Double,
    @Json(name = "scholarship_amount") val scholarshipAmount: Double = 0.0,
    @Json(name = "deduction_amount") val deductionAmount: Double = 0.0,
    @Json(name = "scholarship_name") val scholarshipName: String? = null,
    @Json(name = "deduction_reason") val deductionReason: String? = null,
    @Json(name = "due_date") val dueDate: String,
    val status: String // "paid", "partially_paid", "pending", "overdue"
)

@JsonClass(generateAdapter = true)
data class FeeSummary(
    @Json(name = "total_fees") val totalFees: Double,
    @Json(name = "scholarship_deduction") val scholarshipDeduction: Double,
    @Json(name = "other_deductions") val otherDeductions: Double = 0.0,
    @Json(name = "amount_paid") val amountPaid: Double,
    @Json(name = "pending_balance") val pendingBalance: Double,
    @Json(name = "due_date") val dueDate: String?,
    @Json(name = "assigned_scholarship_type_id") val assignedScholarshipTypeId: String?,
    @Json(name = "assigned_scholarship_name") val assignedScholarshipName: String? = null,
    @Json(name = "net_fees") val netFees: Double = 0.0,
    val records: List<FeeRecord> = emptyList()
)

@JsonClass(generateAdapter = true)
data class LoanDetails(
    val bank: String,
    val branch: String,
    @Json(name = "account_no") val accountNo: String,
    val sanctioned: Double,
    @Json(name = "interest_rate") val interestRate: Double,
    val emi: Double,
    val outstanding: Double,
    @Json(name = "moratorium_end") val moratoriumEnd: String,
    val status: String
)

@JsonClass(generateAdapter = true)
data class EmiHistory(
    val month: String,
    val amount: Double,
    val status: String,
    @Json(name = "txn_id") val txnId: String
)

@JsonClass(generateAdapter = true)
data class Receipt(
    val id: String,
    val date: String,
    val head: String,
    val amount: Double,
    val mode: String,
    val txn: String
)

@JsonClass(generateAdapter = true)
data class ScholarshipType(
    val id: String,
    val name: String,
    val description: String?,
    @Json(name = "reduction_value") val reductionValue: Double,
    @Json(name = "reduction_type") val reductionType: String, // "percentage", "flat"
    val scope: String,
    @Json(name = "program_level") val programLevel: String,
    @Json(name = "batch_year") val batchYear: Int? = null
)

@JsonClass(generateAdapter = true)
data class AssistanceRequest(
    val id: Int,
    val type: String,
    val date: String,
    val status: String,
    val amount: Double
)

@JsonClass(generateAdapter = true)
data class FinancialNotification(
    val id: Int,
    val type: String, // "warning", "info", "success"
    val message: String,
    val time: String
)
