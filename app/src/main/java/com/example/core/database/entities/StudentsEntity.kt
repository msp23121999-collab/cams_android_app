package com.example.core.database.entities

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "students")
data class StudentsEntity(
    @PrimaryKey val id: String,
    val userId: String,
    val rollNo: String,
    val fullName: String,
    val email: String,
    val semester: Int,
    val batch: String,
    val departmentId: String,
    val courseId: String,
    val sectionId: String,
    val mentorId: String?,
    val isActive: Boolean = true
)
