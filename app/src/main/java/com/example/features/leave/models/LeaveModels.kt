package com.example.features.leave.models

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class LeaveRecord(
    val id: String,
    val type: String,
    @Json(name = "app_category") val appCategory: String, // "Leave", "OD"
    @Json(name = "session_type") val sessionType: String,
    @Json(name = "from_date") val fromDate: String,
    @Json(name = "to_date") val toDate: String,
    val reason: String,
    val status: String, // "PENDING", "APPROVED", "REJECTED"
    val remarks: String? = null,
    @Json(name = "num_days") val numDays: Double,
    @Json(name = "attachment_url") val attachmentUrl: String? = null
)

@JsonClass(generateAdapter = true)
data class SemesterTimeline(
    val semester: Int,
    @Json(name = "semester_start") val semesterStart: String,
    @Json(name = "semester_end") val semesterEnd: String,
    @Json(name = "progress_pct") val progressPct: Double,
    val periods: PeriodConfig,
    val structure: SemesterStructure
)

@JsonClass(generateAdapter = true)
data class PeriodConfig(
    @Json(name = "total_expected") val totalExpected: Int,
    @Json(name = "elapsed_so_far") val elapsedSoFar: Int,
    @Json(name = "per_day_regular") val perDayRegular: Int
)

@JsonClass(generateAdapter = true)
data class SemesterStructure(
    @Json(name = "regular_class_weeks") val regularClassWeeks: Int,
    @Json(name = "ia_weeks") val iaWeeks: Int,
    @Json(name = "end_sem_exam_weeks") val endSemExamWeeks: Int,
    @Json(name = "study_leave_weeks") val studyLeaveWeeks: Int
)

data class GeoPoint(
    val name: String,
    val lat: Double,
    val lon: Double
)

/**
 * Canonical leave-status strings exchanged with the backend.
 *
 * These were previously duplicated as bare literals across the HOD approval screens,
 * which is how two screens ended up sending different statuses for the same action —
 * one escalated to the Principal, the other terminated the request and deducted the
 * leave balance. Keep every status literal here so the screens cannot diverge again.
 */
object LeaveStatuses {
    const val PENDING = "PENDING"
    const val APPROVED_BY_HOD = "APPROVED_BY_HOD"
    const val REJECTED_BY_HOD = "REJECTED_BY_HOD"
    const val PENDING_PRINCIPAL = "PENDING_PRINCIPAL"
    const val APPROVED = "APPROVED"
    const val REJECTED = "REJECTED"

    /** Default remarks sent with an HOD decision; the backend requires non-empty remarks. */
    const val REMARK_HOD_APPROVED = "Approved by HOD"
    const val REMARK_HOD_REJECTED = "Rejected by HOD"

    /** Statuses that mean "an HOD has approved", for read-side counting/filtering. */
    val HOD_APPROVED_SET = setOf(APPROVED, APPROVED_BY_HOD)

    /** Statuses that mean "an HOD has rejected", for read-side counting/filtering. */
    val HOD_REJECTED_SET = setOf(REJECTED, REJECTED_BY_HOD)
}
