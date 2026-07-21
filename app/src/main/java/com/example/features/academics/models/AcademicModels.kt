package com.example.features.academics.models

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class TimetablePeriod(
    // Subject/faculty/room are nullable: the backend returns null when the underlying
    // course or faculty reference cannot be resolved, instead of placeholder data.
    @Json(name = "subject_code") val subjectCode: String? = null,
    @Json(name = "subject_name") val subjectName: String? = null,
    @Json(name = "weekday") val weekday: String,
    @Json(name = "start_time") val startTime: String,
    @Json(name = "end_time") val endTime: String,
    @Json(name = "faculty_name") val facultyName: String? = null,
    val room: String? = null
)

@JsonClass(generateAdapter = true)
data class AcademicSubject(
    val code: String,
    val name: String,
    val credits: Int,
    val semester: Int,
    val type: String, // Core, Foundation, Theory
    val hours: Int = 0,
    val faculty: String = "—",
    val degree: String = "—",
    val batch: String = "—"
)

@JsonClass(generateAdapter = true)
data class AcademicSummary(
    val totalSubjects: Int,
    val totalCredits: Int,
    val coreSubjects: Int,
    val currentSemester: Int
)
