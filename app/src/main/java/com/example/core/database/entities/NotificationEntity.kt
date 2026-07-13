package com.example.core.database.entities

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "notifications")
data class NotificationEntity(
    @PrimaryKey val id: String,
    val title: String,
    val message: String,
    val type: String,
    val timestamp: String,
    val isRead: Boolean,
    val priority: String? = null
)
