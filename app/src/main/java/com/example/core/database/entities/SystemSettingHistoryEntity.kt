package com.example.core.database.entities

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "system_setting_history")
data class SystemSettingHistoryEntity(
    @PrimaryKey
    val id: String,
    val dummyData: String = ""
)
