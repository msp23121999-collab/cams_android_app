package com.example.core.database.entities

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "faculty_absences")
data class FacultyAbsencesEntity(
    @PrimaryKey
    val id: String,
    val dummyData: String = ""
)
