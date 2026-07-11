package com.example.core.database.dao

import androidx.room.*
import com.example.core.database.entities.FacultyAbsencesEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface FacultyAbsencesDao {
    @Query("SELECT * FROM faculty_absences")
    fun getAll(): Flow<List<FacultyAbsencesEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(entity: FacultyAbsencesEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(entities: List<FacultyAbsencesEntity>)

    @Delete
    suspend fun delete(entity: FacultyAbsencesEntity)

    @Query("DELETE FROM faculty_absences")
    suspend fun deleteAll()
}
