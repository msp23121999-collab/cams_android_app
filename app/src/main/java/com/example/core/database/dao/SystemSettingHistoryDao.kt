package com.example.core.database.dao

import androidx.room.*
import com.example.core.database.entities.SystemSettingHistoryEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface SystemSettingHistoryDao {
    @Query("SELECT * FROM system_setting_history")
    fun getAll(): Flow<List<SystemSettingHistoryEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(entity: SystemSettingHistoryEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(entities: List<SystemSettingHistoryEntity>)

    @Delete
    suspend fun delete(entity: SystemSettingHistoryEntity)

    @Query("DELETE FROM system_setting_history")
    suspend fun deleteAll()
}
