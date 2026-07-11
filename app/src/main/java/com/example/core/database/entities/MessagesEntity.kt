package com.example.core.database.entities

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "messages")
data class MessagesEntity(
    @PrimaryKey
    val id: String,
    val dummyData: String = ""
)
