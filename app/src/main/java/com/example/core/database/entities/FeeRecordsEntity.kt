package com.example.core.database.entities

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "fee_records")
data class FeeRecordsEntity(
    @PrimaryKey
    val id: String,
    val dummyData: String = ""
)
