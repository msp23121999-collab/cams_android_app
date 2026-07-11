package com.example.core.database.entities

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "faculty_workload")
data class FacultyWorkloadEntity(
    @PrimaryKey
    val id: String,
    val dummyData: String = ""
)
