package com.example.core.database.dao

import androidx.room.*
import com.example.core.database.entities.StudentsEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface StudentsDao {
    @Query("SELECT * FROM students")
    fun getAll(): Flow<List<StudentsEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(entity: StudentsEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(entities: List<StudentsEntity>)

    @Delete
    suspend fun delete(entity: StudentsEntity)

    @Query("DELETE FROM students")
    suspend fun deleteAll()
}
