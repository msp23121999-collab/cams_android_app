package com.example.core.database.entities

import androidx.room.Entity
import androidx.room.PrimaryKey
import androidx.room.ColumnInfo

@Entity(tableName = "attendance")
data class AttendanceEntity(
    @PrimaryKey
    @ColumnInfo(name = "id")
    val id: String,
    @ColumnInfo(name = "student_id")
    val studentId: String?,
    @ColumnInfo(name = "section_id")
    val sectionId: String?,
    @ColumnInfo(name = "date")
    val date: String?,
    @ColumnInfo(name = "status")
    val status: String?,
    @ColumnInfo(name = "created_at")
    val createdAt: String?,
    @ColumnInfo(name = "updated_at")
    val updatedAt: String?,
    @ColumnInfo(name = "is_deleted")
    val isDeleted: Boolean?,
    @ColumnInfo(name = "deleted_at")
    val deletedAt: String?
)
