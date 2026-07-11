package com.example.core.database.entities

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "marks")
data class MarksEntity(
    @PrimaryKey
    val id: String,
    val dummyData: String = ""
)
