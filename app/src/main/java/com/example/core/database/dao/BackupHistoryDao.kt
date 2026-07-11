package com.example.core.database.dao

import androidx.room.*
import com.example.core.database.entities.BackupHistoryEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface BackupHistoryDao {
    @Query("SELECT * FROM backup_history")
    fun getAll(): Flow<List<BackupHistoryEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(entity: BackupHistoryEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(entities: List<BackupHistoryEntity>)

    @Delete
    suspend fun delete(entity: BackupHistoryEntity)

    @Query("DELETE FROM backup_history")
    suspend fun deleteAll()
}
