package com.example.core.database.entities

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "parent_student_map")
data class ParentStudentMapEntity(
    @PrimaryKey
    val id: String,
    val dummyData: String = ""
)
