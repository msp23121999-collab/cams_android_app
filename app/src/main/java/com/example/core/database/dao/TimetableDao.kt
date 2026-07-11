package com.example.core.database.dao

import androidx.room.*
import com.example.core.database.entities.TimetableEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface TimetableDao {
    @Query("SELECT * FROM timetable")
    fun getAll(): Flow<List<TimetableEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(entity: TimetableEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(entities: List<TimetableEntity>)

    @Delete
    suspend fun delete(entity: TimetableEntity)

    @Query("DELETE FROM timetable")
    suspend fun deleteAll()
}
