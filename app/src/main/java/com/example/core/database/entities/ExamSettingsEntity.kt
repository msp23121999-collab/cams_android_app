package com.example.core.database.entities

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "exam_settings")
data class ExamSettingsEntity(
    @PrimaryKey
    val id: String,
    val dummyData: String = ""
)
