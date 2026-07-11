package com.example.core.database.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.example.core.database.entities.LeavesEntity

@Dao
interface LeavesDao {
    @Query("SELECT * FROM leaves")
    suspend fun getAll(): List<LeavesEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(entity: LeavesEntity)
}
