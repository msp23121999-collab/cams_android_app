package com.example.core.database.entities

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "faculty_profiles")
data class FacultyProfilesEntity(
    @PrimaryKey val facultyId: String,
    val userId: String,
    val fullName: String,
    val designation: String,
    val departmentName: String,
    val email: String,
    val phone: String,
    val employeeCode: String,
    val specialization: String,
    val isActive: Boolean = true
)
