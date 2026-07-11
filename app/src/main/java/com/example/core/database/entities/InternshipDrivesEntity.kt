package com.example.core.database.entities

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "internship_drives")
data class InternshipDrivesEntity(
    @PrimaryKey
    val id: String,
    val dummyData: String = ""
)
