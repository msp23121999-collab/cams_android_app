package com.example.core.database.dao

import androidx.room.*
import com.example.core.database.entities.WorkingDayConfigEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface WorkingDayConfigDao {
    @Query("SELECT * FROM working_day_config")
    fun getAll(): Flow<List<WorkingDayConfigEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(entity: WorkingDayConfigEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(entities: List<WorkingDayConfigEntity>)

    @Delete
    suspend fun delete(entity: WorkingDayConfigEntity)

    @Query("DELETE FROM working_day_config")
    suspend fun deleteAll()
}
