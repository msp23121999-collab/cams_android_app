package com.example.core.database.entities

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "staff_attendance")
data class StaffAttendanceEntity(
    @PrimaryKey
    val id: String,
    val dummyData: String = ""
)
