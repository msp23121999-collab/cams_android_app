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
