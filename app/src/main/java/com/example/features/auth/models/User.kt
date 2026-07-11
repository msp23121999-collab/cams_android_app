package com.example.features.auth.models

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class User(
    val id: String,
    val email: String,
    @Json(name = "full_name") val fullName: String,
    val role: String,
    @Json(name = "department_id") val departmentId: String? = null
)

@JsonClass(generateAdapter = true)
data class LoginResponse(
    @Json(name = "access_token") val accessToken: String,
    @Json(name = "refresh_token") val refreshToken: String?,
    @Json(name = "subdomain_target") val subdomainTarget: String,
    val role: String
)
