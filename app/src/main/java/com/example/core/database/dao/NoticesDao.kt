package com.example.core.database.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.example.core.database.entities.NoticesEntity

@Dao
interface NoticesDao {
    @Query("SELECT * FROM notices")
    suspend fun getAll(): List<NoticesEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(entity: NoticesEntity)
}
