package com.example.core.database.entities

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "courses")
data class CoursesEntity(
    @PrimaryKey val id: String,
    val departmentId: String,
    val name: String,
    val code: String,
    val durationYears: Int,
    val isActive: Boolean = true
)
