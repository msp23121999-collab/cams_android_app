package com.example.core.database.entities

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "legal_events")
data class LegalEventsEntity(
    @PrimaryKey
    val id: String,
    val dummyData: String = ""
)
