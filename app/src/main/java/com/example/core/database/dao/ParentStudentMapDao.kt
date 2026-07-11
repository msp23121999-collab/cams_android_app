package com.example.core.database.dao

import androidx.room.*
import com.example.core.database.entities.ParentStudentMapEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface ParentStudentMapDao {
    @Query("SELECT * FROM parent_student_map")
    fun getAll(): Flow<List<ParentStudentMapEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(entity: ParentStudentMapEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(entities: List<ParentStudentMapEntity>)

    @Delete
    suspend fun delete(entity: ParentStudentMapEntity)

    @Query("DELETE FROM parent_student_map")
    suspend fun deleteAll()
}
