package com.example.core.database.entities

import androidx.room.Entity
import androidx.room.PrimaryKey
import androidx.room.ColumnInfo

@Entity(tableName = "attendance_corrections")
data class AttendanceCorrectionsEntity(
    @PrimaryKey
    @ColumnInfo(name = "id")
    val id: String,
    @ColumnInfo(name = "student_reg_no")
    val studentRegNo: String?,
    @ColumnInfo(name = "student_name")
    val studentName: String?,
    @ColumnInfo(name = "subject")
    val subject: String?,
    @ColumnInfo(name = "date")
    val date: String?,
    @ColumnInfo(name = "previous_status")
    val previousStatus: String?,
    @ColumnInfo(name = "updated_status")
    val updatedStatus: String?,
    @ColumnInfo(name = "reason")
    val reason: String?,
    @ColumnInfo(name = "requested_at")
    val requestedAt: String?,
    @ColumnInfo(name = "status")
    val status: String?
)
