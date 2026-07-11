package com.example.core.database.entities

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "chat_messages")
data class ChatMessagesEntity(
    @PrimaryKey
    val id: String,
    val dummyData: String = ""
)
