package com.example.core.database.entities

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "timetable_approvals")
data class TimetableApprovalsEntity(
    @PrimaryKey
    val id: String,
    val dummyData: String = ""
)
