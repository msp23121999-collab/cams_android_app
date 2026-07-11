package com.example.core.database.dao

import androidx.room.*
import com.example.core.database.entities.FacultyAssignmentsEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface FacultyAssignmentsDao {
    @Query("SELECT * FROM faculty_assignments")
    fun getAll(): Flow<List<FacultyAssignmentsEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(entity: FacultyAssignmentsEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(entities: List<FacultyAssignmentsEntity>)

    @Delete
    suspend fun delete(entity: FacultyAssignmentsEntity)

    @Query("DELETE FROM faculty_assignments")
    suspend fun deleteAll()
}
