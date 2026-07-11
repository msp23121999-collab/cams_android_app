package com.example.core.database.entities

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "leaves")
data class LeavesEntity(
    @PrimaryKey
    val id: String,
    val dummyData: String = ""
)
