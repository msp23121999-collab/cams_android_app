package com.example.core.database.entities

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "users")
data class UsersEntity(
    @PrimaryKey val id: String,
    val email: String,
    val phone: String,
    val fullName: String,
    val hashedPassword: String,
    val role: String,
    val isActive: Boolean,
    val departmentId: String?,
    val createdAt: String,
    val updatedAt: String,
    val isDeleted: Boolean,
    val deletedAt: String?
)
