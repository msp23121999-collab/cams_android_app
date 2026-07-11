package com.example.core.database.entities

import androidx.room.Entity
import androidx.room.PrimaryKey
import androidx.room.ColumnInfo

@Entity(tableName = "academic_years")
data class AcademicYearsEntity(
    @PrimaryKey
    @ColumnInfo(name = "id")
    val id: String,
    @ColumnInfo(name = "name")
    val name: String?,
    @ColumnInfo(name = "start_date")
    val startDate: String?,
    @ColumnInfo(name = "end_date")
    val endDate: String?,
    @ColumnInfo(name = "regulation_id")
    val regulationId: String?,
    @ColumnInfo(name = "batch")
    val batch: String?,
    @ColumnInfo(name = "current_semester")
    val currentSemester: Int?,
    @ColumnInfo(name = "is_semester_open")
    val isSemesterOpen: Boolean?,
    @ColumnInfo(name = "is_exam_period")
    val isExamPeriod: Boolean?,
    @ColumnInfo(name = "is_active")
    val isActive: Boolean?,
    @ColumnInfo(name = "created_at")
    val createdAt: String?,
    @ColumnInfo(name = "updated_at")
    val updatedAt: String?,
    @ColumnInfo(name = "is_deleted")
    val isDeleted: Boolean?,
    @ColumnInfo(name = "deleted_at")
    val deletedAt: String?
)
