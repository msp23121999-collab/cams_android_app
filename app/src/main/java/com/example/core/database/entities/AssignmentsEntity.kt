package com.example.core.database.entities

import androidx.room.Entity
import androidx.room.PrimaryKey
import androidx.room.ColumnInfo

@Entity(tableName = "assignments")
data class AssignmentsEntity(
    @PrimaryKey
    @ColumnInfo(name = "id")
    val id: String,
    @ColumnInfo(name = "section_id")
    val sectionId: String?,
    @ColumnInfo(name = "faculty_id")
    val facultyId: String?,
    @ColumnInfo(name = "title")
    val title: String?,
    @ColumnInfo(name = "deadline")
    val deadline: String?,
    @ColumnInfo(name = "submission_count")
    val submissionCount: Int?,
    @ColumnInfo(name = "created_at")
    val createdAt: String?,
    @ColumnInfo(name = "updated_at")
    val updatedAt: String?,
    @ColumnInfo(name = "is_deleted")
    val isDeleted: Boolean?,
    @ColumnInfo(name = "deleted_at")
    val deletedAt: String?
)
