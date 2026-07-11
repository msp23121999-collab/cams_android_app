package com.example.core.database.dao

import androidx.room.*
import com.example.core.database.entities.SystemSettingsEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface SystemSettingsDao {
    @Query("SELECT * FROM system_settings")
    fun getAll(): Flow<List<SystemSettingsEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(entity: SystemSettingsEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(entities: List<SystemSettingsEntity>)

    @Delete
    suspend fun delete(entity: SystemSettingsEntity)

    @Query("DELETE FROM system_settings")
    suspend fun deleteAll()
}
