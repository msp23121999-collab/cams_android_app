package com.example.core.database.dao

import androidx.room.*
import com.example.core.database.entities.BackupConfigurationsEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface BackupConfigurationsDao {
    @Query("SELECT * FROM backup_configurations")
    fun getAll(): Flow<List<BackupConfigurationsEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(entity: BackupConfigurationsEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(entities: List<BackupConfigurationsEntity>)

    @Delete
    suspend fun delete(entity: BackupConfigurationsEntity)

    @Query("DELETE FROM backup_configurations")
    suspend fun deleteAll()
}
