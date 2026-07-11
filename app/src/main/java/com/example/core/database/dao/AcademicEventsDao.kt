package com.example.core.database.dao

import androidx.room.*
import com.example.core.database.entities.AcademicEventsEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface AcademicEventsDao {
    @Query("SELECT * FROM academic_events")
    fun getAll(): Flow<List<AcademicEventsEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(entity: AcademicEventsEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(entities: List<AcademicEventsEntity>)

    @Delete
    suspend fun delete(entity: AcademicEventsEntity)

    @Query("DELETE FROM academic_events")
    suspend fun deleteAll()
}
