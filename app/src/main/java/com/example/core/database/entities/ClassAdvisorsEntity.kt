package com.example.core.database.entities

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "class_advisors")
data class ClassAdvisorsEntity(
    @PrimaryKey
    val id: String,
    val dummyData: String = ""
)
