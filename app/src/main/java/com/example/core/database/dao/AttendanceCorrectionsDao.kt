package com.example.core.database.dao

import androidx.room.*
import com.example.core.database.entities.AttendanceCorrectionsEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface AttendanceCorrectionsDao {
    @Query("SELECT * FROM attendance_corrections")
    fun getAll(): Flow<List<AttendanceCorrectionsEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(entity: AttendanceCorrectionsEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(entities: List<AttendanceCorrectionsEntity>)

    @Delete
    suspend fun delete(entity: AttendanceCorrectionsEntity)

    @Query("DELETE FROM attendance_corrections")
    suspend fun deleteAll()
}
