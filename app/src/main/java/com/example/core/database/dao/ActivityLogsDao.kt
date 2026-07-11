package com.example.core.database.dao

import androidx.room.*
import com.example.core.database.entities.ActivityLogsEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface ActivityLogsDao {
    @Query("SELECT * FROM activity_logs")
    fun getAll(): Flow<List<ActivityLogsEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(entity: ActivityLogsEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(entities: List<ActivityLogsEntity>)

    @Delete
    suspend fun delete(entity: ActivityLogsEntity)

    @Query("DELETE FROM activity_logs")
    suspend fun deleteAll()
}
