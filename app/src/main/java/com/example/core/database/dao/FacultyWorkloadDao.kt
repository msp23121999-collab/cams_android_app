package com.example.core.database.dao

import androidx.room.*
import com.example.core.database.entities.FacultyWorkloadEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface FacultyWorkloadDao {
    @Query("SELECT * FROM faculty_workload")
    fun getAll(): Flow<List<FacultyWorkloadEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(entity: FacultyWorkloadEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(entities: List<FacultyWorkloadEntity>)

    @Delete
    suspend fun delete(entity: FacultyWorkloadEntity)

    @Query("DELETE FROM faculty_workload")
    suspend fun deleteAll()
}
