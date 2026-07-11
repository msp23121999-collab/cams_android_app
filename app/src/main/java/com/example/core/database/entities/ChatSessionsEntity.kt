package com.example.core.database.entities

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "chat_sessions")
data class ChatSessionsEntity(
    @PrimaryKey
    val id: String,
    val dummyData: String = ""
)
