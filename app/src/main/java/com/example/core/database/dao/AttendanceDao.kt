package com.example.core.database.dao

import androidx.room.*
import com.example.core.database.entities.AttendanceEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface AttendanceDao {
    @Query("SELECT * FROM attendance")
    fun getAll(): Flow<List<AttendanceEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(entity: AttendanceEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(entities: List<AttendanceEntity>)

    @Delete
    suspend fun delete(entity: AttendanceEntity)

    @Query("DELETE FROM attendance")
    suspend fun deleteAll()
}
