package com.example.core.database.entities

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "notice_acknowledgements")
data class NoticeAcknowledgementsEntity(
    @PrimaryKey
    val id: String,
    val dummyData: String = ""
)
