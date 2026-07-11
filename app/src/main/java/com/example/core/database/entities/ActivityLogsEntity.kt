package com.example.core.database.entities

import androidx.room.Entity
import androidx.room.PrimaryKey
import androidx.room.ColumnInfo

@Entity(tableName = "activity_logs")
data class ActivityLogsEntity(
    @PrimaryKey
    @ColumnInfo(name = "id")
    val id: String,
    @ColumnInfo(name = "user_id")
    val userId: String?,
    @ColumnInfo(name = "action")
    val action: String?,
    @ColumnInfo(name = "action_metadata")
    val actionMetadata: String?,
    @ColumnInfo(name = "created_at")
    val createdAt: String?,
    @ColumnInfo(name = "updated_at")
    val updatedAt: String?,
    @ColumnInfo(name = "is_deleted")
    val isDeleted: Boolean?,
    @ColumnInfo(name = "deleted_at")
    val deletedAt: String?
)
