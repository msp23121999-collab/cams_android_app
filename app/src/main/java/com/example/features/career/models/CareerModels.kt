package com.example.features.career.models

import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class InternshipDrive(
    val id: String,
    val companyName: String,
    val role: String,
    val location: String,
    val applicationStatus: String, // "Applied", "Assessment", "Interview", "Selected", "Not Applied"
    val description: String,
    val stipend: String
)

@JsonClass(generateAdapter = true)
data class StudentCertification(
    val id: String,
    val name: String,
    val issuer: String,
    val issueDate: String,
    val expiryDate: String?,
    val credentialId: String,
    val isVerified: Boolean
)

@JsonClass(generateAdapter = true)
data class ActivityPointClaim(
    val id: String,
    val activityName: String,
    val category: String,
    val date: String,
    val pointsClaimed: Int,
    val status: String // "Pending", "Approved", "Rejected"
)
