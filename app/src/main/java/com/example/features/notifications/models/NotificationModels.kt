package com.example.features.notifications.models

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class NotificationRecord(
    val id: String,
    val type: String,
    val message: String,
    @Json(name = "is_read") val isRead: Boolean,
    @Json(name = "created_at") val createdAt: String
)

data class NotificationTypeMeta(
    val label: String,
    val icon: androidx.compose.ui.graphics.vector.ImageVector,
    val color: androidx.compose.ui.graphics.Color,
    val bgColor: androidx.compose.ui.graphics.Color,
    val borderColor: androidx.compose.ui.graphics.Color
)
