package com.example.core.database.entities

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "system_settings")
data class SystemSettingsEntity(
    @PrimaryKey
    val id: String,
    val dummyData: String = ""
)
