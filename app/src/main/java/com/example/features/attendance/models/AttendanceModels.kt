package com.example.features.attendance.models

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class AttendanceRecord(
    val id: String,
    val date: String,
    val status: String,
    @Json(name = "subject_name") val subjectName: String,
    @Json(name = "subject_code") val subjectCode: String,
    @Json(name = "section_name") val sectionName: String
)

@JsonClass(generateAdapter = true)
data class AttendanceSummary(
    val percentage: Double,
    val total: Int,
    val present: Int,
    val absent: Int,
    val od: Int,
    val records: List<AttendanceRecord>
)
