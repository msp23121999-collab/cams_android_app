package com.example.core.database.entities

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "notices")
data class NoticesEntity(
    @PrimaryKey
    val id: String,
    val dummyData: String = ""
)
