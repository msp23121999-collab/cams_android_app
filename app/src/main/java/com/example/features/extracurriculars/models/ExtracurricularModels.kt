package com.example.features.extracurriculars.models

import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class ServiceOpportunity(
    val id: String,
    val title: String,
    val ngoName: String,
    val date: String,
    val location: String,
    val spotsAvailable: Int,
    val hours: Int,
    val tags: List<String>
)

@JsonClass(generateAdapter = true)
data class ServiceLogEntry(
    val id: String,
    val title: String,
    val date: String,
    val hours: Int,
    val status: String // "Verified", "Pending"
)

@JsonClass(generateAdapter = true)
data class InnovationProject(
    val id: String,
    val title: String,
    val abstractText: String,
    val category: String, // "Legal Technology", "Community Projects", "Research", "Startups", "Moot Court Innovations"
    val mentor: String,
    val teamMembers: List<String>,
    val likes: Int,
    val comments: Int
)

@JsonClass(generateAdapter = true)
data class AcademicPublication(
    val id: String,
    val title: String,
    val abstractText: String,
    val practiceArea: String,
    val status: String,
    val awards: String?,
    val date: String,
    val guide: String,
    val isFeatured: Boolean
)
