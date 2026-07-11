package com.example.core.database.dao

import androidx.room.*
import com.example.core.database.entities.AuditLogsEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface AuditLogsDao {
    @Query("SELECT * FROM audit_logs")
    fun getAll(): Flow<List<AuditLogsEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(entity: AuditLogsEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(entities: List<AuditLogsEntity>)

    @Delete
    suspend fun delete(entity: AuditLogsEntity)

    @Query("DELETE FROM audit_logs")
    suspend fun deleteAll()
}
