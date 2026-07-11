package com.example.core.database.entities

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "exams")
data class ExamsEntity(
    @PrimaryKey
    val id: String,
    val dummyData: String = ""
)
