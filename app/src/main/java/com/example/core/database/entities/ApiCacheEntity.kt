package com.example.core.database.entities

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "api_cache")
data class ApiCacheEntity(
    @PrimaryKey
    val url: String,
    val responseBody: String,
    val timestamp: Long
)
