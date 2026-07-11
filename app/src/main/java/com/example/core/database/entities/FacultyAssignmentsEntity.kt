package com.example.core.database.entities

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "faculty_assignments")
data class FacultyAssignmentsEntity(
    @PrimaryKey
    val id: String,
    val dummyData: String = ""
)
