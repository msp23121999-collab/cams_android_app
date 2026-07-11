package com.example.core.database.entities

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "departments")
data class DepartmentsEntity(
    @PrimaryKey val id: String,
    val name: String,
    val code: String,
    val description: String?,
    val isActive: Boolean = true
)
