package com.example.core.database.entities

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "sections")
data class SectionsEntity(
    @PrimaryKey val id: String,
    val courseId: String,
    val name: String,
    val academicYearId: String,
    val semester: Int,
    val isActive: Boolean = true
)
