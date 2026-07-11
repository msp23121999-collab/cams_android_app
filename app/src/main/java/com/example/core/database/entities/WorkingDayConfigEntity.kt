package com.example.core.database.entities

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "working_day_config")
data class WorkingDayConfigEntity(
    @PrimaryKey
    val id: String,
    val dummyData: String = ""
)
