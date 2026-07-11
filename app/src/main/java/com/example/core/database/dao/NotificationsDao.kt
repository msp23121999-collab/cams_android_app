package com.example.core.database.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.example.core.database.entities.NotificationsEntity

@Dao
interface NotificationsDao {
    @Query("SELECT * FROM notifications")
    suspend fun getAll(): List<NotificationsEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(entity: NotificationsEntity)
}
