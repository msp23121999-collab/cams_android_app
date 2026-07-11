package com.example.core.database.dao

import androidx.room.*
import com.example.core.database.entities.ExamsEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface ExamsDao {
    @Query("SELECT * FROM exams")
    fun getAll(): Flow<List<ExamsEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(entity: ExamsEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(entities: List<ExamsEntity>)

    @Delete
    suspend fun delete(entity: ExamsEntity)

    @Query("DELETE FROM exams")
    suspend fun deleteAll()
}
