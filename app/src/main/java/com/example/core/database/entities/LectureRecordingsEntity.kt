package com.example.core.database.entities

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "lecture_recordings")
data class LectureRecordingsEntity(
    @PrimaryKey
    val id: String,
    val dummyData: String = ""
)
