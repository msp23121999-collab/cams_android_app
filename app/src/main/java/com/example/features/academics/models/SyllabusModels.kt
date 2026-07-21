package com.example.features.academics.models

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class SyllabusProgress(
    @Json(name = "overall_completion") val overallCompletion: Double,
    @Json(name = "days_remaining") val daysRemaining: Int? = null,
    @Json(name = "units_progress") val unitsProgress: List<UnitProgress> = emptyList()
)

@JsonClass(generateAdapter = true)
data class UnitProgress(
    val unit: String,
    @Json(name = "completed_topics") val completedTopics: List<String> = emptyList(),
    @Json(name = "remaining_topics") val remainingTopics: List<String> = emptyList()
)

@JsonClass(generateAdapter = true)
data class LessonPlanItem(
    val subject: String,
    val unit: String,
    @Json(name = "planned_topic") val plannedTopic: String,
    @Json(name = "actual_topic") val actualTopic: String? = null,
    @Json(name = "date_taught") val dateTaught: String? = null,
    val status: String // "Covered", "Pending"
)
