package com.example.core.database.entities

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "notifications")
data class NotificationsEntity(
    @PrimaryKey
    val id: String,
    val dummyData: String = ""
)
