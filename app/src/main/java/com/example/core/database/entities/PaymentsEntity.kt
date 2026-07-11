package com.example.core.database.entities

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "payments")
data class PaymentsEntity(
    @PrimaryKey
    val id: String,
    val dummyData: String = ""
)
