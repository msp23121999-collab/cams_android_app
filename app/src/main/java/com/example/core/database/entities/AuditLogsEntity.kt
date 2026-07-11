package com.example.core.database.entities

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "audit_logs")
data class AuditLogsEntity(
    @PrimaryKey
    val id: String,
    val dummyData: String = ""
)
