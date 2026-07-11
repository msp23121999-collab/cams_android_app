package com.example.core.database.entities

import androidx.room.Entity
import androidx.room.PrimaryKey
import androidx.room.ColumnInfo

@Entity(tableName = "academic_events")
data class AcademicEventsEntity(
    @PrimaryKey
    @ColumnInfo(name = "id")
    val id: String,
    @ColumnInfo(name = "title")
    val title: String?,
    @ColumnInfo(name = "category")
    val category: String?,
    @ColumnInfo(name = "start_date")
    val startDate: String?,
    @ColumnInfo(name = "end_date")
    val endDate: String?,
    @ColumnInfo(name = "description")
    val description: String?,
    @ColumnInfo(name = "academic_year")
    val academicYear: String?,
    @ColumnInfo(name = "semester")
    val semester: String?,
    @ColumnInfo(name = "location")
    val location: String?,
    @ColumnInfo(name = "is_holiday")
    val isHoliday: Boolean?,
    @ColumnInfo(name = "created_at")
    val createdAt: String?,
    @ColumnInfo(name = "updated_at")
    val updatedAt: String?,
    @ColumnInfo(name = "created_by")
    val createdBy: String?
)
