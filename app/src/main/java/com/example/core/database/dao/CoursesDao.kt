package com.example.core.database.dao

import androidx.room.*
import com.example.core.database.entities.CoursesEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface CoursesDao {
    @Query("SELECT * FROM courses")
    fun getAll(): Flow<List<CoursesEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(entity: CoursesEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(entities: List<CoursesEntity>)

    @Delete
    suspend fun delete(entity: CoursesEntity)

    @Query("DELETE FROM courses")
    suspend fun deleteAll()
}
